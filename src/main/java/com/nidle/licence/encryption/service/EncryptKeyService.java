package com.nidle.licence.encryption.service;



import com.google.gson.Gson;
import com.nidle.licence.configuration.FileEncryptor;
import com.nidle.licence.configuration.NoRecordExistsException;
import com.nidle.licence.configuration.Response;
import com.nidle.licence.configuration.ResponseUtils;
import com.nidle.licence.encryption.dto.LicenceKeyDto;
import com.nidle.licence.encryption.entity.LicenceKey;
import com.nidle.licence.encryption.fileProperties.FileStorageException;
import com.nidle.licence.encryption.fileProperties.FileStorageProperties;
import com.nidle.licence.encryption.fileProperties.MyFileNotFoundException;
import com.nidle.licence.encryption.repository.EncryptKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Scanner;

@Service
public class EncryptKeyService {


    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptKeyService.class);
    private final String root = "Licence";
    @Value("${legal.file.upload.path}")
    private String FILE_DIRECTORY_PATH_LINUX;

    @Value("${reporting.file.upload.path}")
    private String FILE_DIRECTORY_PATH_WINDOWS;

    @Autowired
    EncryptKeyRepository encryptKeyRepository;



    public EncryptKeyService() {
    }
//    private final Path fileStorageLocation;

    //    EncryptKeyService(){
//        try {
//            this.fileStorageLocation = Paths.get(FILE_DIRECTORY_PATH);
//            boolean dirExists = Files.exists(fileStorageLocation);
//            if(!dirExists) {
//                try {
//                    Files.createDirectories(fileStorageLocation);
//                } catch (IOException ignored) {
//
//                }
//            }       } catch (Exception ex) {
//            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
//        }
//    };
    public Path getPath(){

        Path path;
        try {
            path = Paths.get(FILE_DIRECTORY_PATH_WINDOWS);
            boolean dirExists = Files.exists(path);
            if(!dirExists) {
                try {
                    Files.createDirectories(path);
                } catch (IOException ignored) {

                }
            }       } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
        return path;
    };

//    @Autowired
//    public EncryptKeyService(FileStorageProperties fileStorageProperties) {
//        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
//                .toAbsolutePath().normalize();
//
//        try {
//            Files.createDirectories(this.fileStorageLocation);
//        } catch (Exception ex) {
//            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
//        }
//    }

    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = getPath().resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
//            Path filePath = getPath().resolve(fileName).normalize();
            Path filePath = Paths.get(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
    public String generateKey(LicenceKeyDto licenceKeyDto) throws IOException {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            File tempFile = File.createTempFile("data", ".txt");

            fw = new FileWriter(tempFile);
            bw = new BufferedWriter(fw);
            bw.write("This is the temporary data written to temp file");
        } catch (IOException e) {
e.printStackTrace();        }
        finally {
            fw.close();
            bw.close();
        }

        return null;
    }


    public Response writeFile(HttpServletRequest request, HttpServletResponse response, LicenceKeyDto licenceKeyDto) throws IOException {
        try {
            LicenceKey licenceKey = new LicenceKey();
            licenceKey = saveData(licenceKeyDto, null);
            BufferedWriter writer = null;
            File logFile = null;
            try {
//                //create a temporary file
//                String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                logFile = new File("licence.key");

                System.out.println(logFile.getCanonicalPath());

                writer = new BufferedWriter(new FileWriter(logFile));
                writer.write(new Gson().toJson(licenceKeyDto));

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    writer.close();
                } catch (Exception ignored) {
                }
            }
            FileEncryptor.encryptFile( logFile.getCanonicalPath(),"password");

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/downloadFile/").path(logFile.getName()).path(".encrypted.nidle")
                    .toUriString();

            LicenceKeyDto licenceKeyDto1 = new LicenceKeyDto(logFile.getName(), fileDownloadUri,
                    null, logFile.getTotalSpace());

            return ResponseUtils.getSuccessResponse(HttpStatus.OK, licenceKeyDto1, String.format("%s Key created successfully", root));
        } catch (NoRecordExistsException e) {
            return ResponseUtils.getFailResponse(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            LOGGER.info("Exception Occur in " + root + " Update ", e);
            return ResponseUtils.getFailResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occurred");
        }

    }

    public LicenceKey saveData(LicenceKeyDto licenceKeyDto, Long id) {
        LicenceKey licenceKey = new LicenceKey();
        BeanUtils.copyProperties(licenceKeyDto, licenceKey);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        licenceKey.setLicenceStartDate(LocalDateTime.parse(licenceKeyDto.getLicenceStartDate(), formatter));
        licenceKey.setLicenceExpDate(LocalDateTime.parse(licenceKeyDto.getLicenceExpDate(), formatter));

        if (id != null) {
            licenceKey = encryptKeyRepository.findById(licenceKeyDto.getId()).orElseThrow(() -> new NoRecordExistsException(String.format("%s doesn't exist for id %s", root, licenceKeyDto.getId())));
        } else {
            licenceKey = encryptKeyRepository.save(licenceKey);
        }

        return licenceKey;
    }

    public String storeFile2(MultipartFile file) {
        // Normalize file name
        String fileName = "";

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            Path dirPathObj = getPath().resolve(file.getOriginalFilename());
            fileName = String.valueOf(dirPathObj);
            Files.copy(file.getInputStream(), dirPathObj, StandardCopyOption.REPLACE_EXISTING);

            FileEncryptor.decryptFile(String.valueOf(dirPathObj),"password");

            return fileName+".decryptKey";
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
        catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
    public Response fileUpload(MultipartFile files) {
        String fileName = StringUtils.cleanPath(files.getOriginalFilename());

        try {
            InputStream inputStream = files.getInputStream();
            Path filePath = Paths.get(("E:\\temp\\app\\nidle\\upload\\" + files.getOriginalFilename() + getPath() + files.getName()));
            Files.copy(inputStream,filePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println(files.getOriginalFilename());
        }catch (IOException e){
            e.printStackTrace();
        }
        LOGGER.info( files.getOriginalFilename());
        try {
            FileEncryptor.decryptFile(getPath() + files.getName(),"password");
            FileEncryptor.decryptFile("E:\\temp\\app\\nidle\\upload\\" + files.getOriginalFilename(),"password");
            try {
                File myObj = new File("E:\\temp\\app\\nidle\\upload\\" + files.getOriginalFilename());
                Scanner myReader = new Scanner(myObj);
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    System.out.println(data);
                }
                myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }catch (Exception ignored){}

        return null;
    }
}
