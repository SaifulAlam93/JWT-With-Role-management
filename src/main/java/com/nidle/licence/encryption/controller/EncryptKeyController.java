package com.nidle.licence.encryption.controller;

import com.nidle.licence.configuration.Response;
import com.nidle.licence.configuration.ResponseUtils;
import com.nidle.licence.encryption.dto.LicenceKeyDto;
import com.nidle.licence.encryption.service.EncryptKeyService;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
//@RequestMapping(value = "/api/vi")
public class EncryptKeyController {

    private static final Logger logger = LoggerFactory.getLogger(EncryptKeyController.class);

    @Autowired
    private EncryptKeyService encryptKeyService;

    @PostMapping("/generateKey2")
    public Response uploadFile2(@RequestBody LicenceKeyDto licenceKeyDto, @RequestParam("file") MultipartFile file) {

      try {

          String fileName = encryptKeyService.storeFile(file);

          String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                  .path("/downloadFile/")
                  .path(fileName)
                  .toUriString();

          return ResponseUtils.getSuccessResponse(HttpStatus.OK, new LicenceKeyDto(fileName, fileDownloadUri,
                  file.getContentType(), file.getSize()), "File has been retrieved successfully");

      }catch (Exception e)
      {
          return ResponseUtils.getFailResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error occured");
      }

    }


    @PostMapping("/api/v1/uploadFileOld")
    public LicenceKeyDto uploadFileOld(@RequestParam("file") MultipartFile file) {
        String fileName = encryptKeyService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new LicenceKeyDto(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }


    @PostMapping("/api/v1/uploadFile")
    public LicenceKeyDto uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = encryptKeyService.storeFile2(file);

//        encryptKeyService.fileUpload( file);
//        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
//                .path("/downloadFile/")
//                .path(fileName)
//                .toUriString();


        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/").path(fileName)
                .toUriString();

        return new LicenceKeyDto(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }
    @PostMapping("/uploadMultipleFiles")
    public List<LicenceKeyDto> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v1/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = encryptKeyService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }


    @PostMapping("/api/v1/generateKey")
    public Response keyGenerate(HttpServletRequest request, HttpServletResponse response, @RequestBody LicenceKeyDto licenceKeyDto) throws IOException {
        return  encryptKeyService.writeFile( request,  response, licenceKeyDto);
    }

    @PostMapping(value = "/api/v1/upload")
    public Response create(@RequestParam(value = "file",required = false) final MultipartFile files )
            throws IOException,
            IllegalStateException {
        return encryptKeyService.fileUpload( files);
    }

//    @RequestMapping("/api/v1/downloadFile/{fileName}")
//    public void downloadPDFResource(HttpServletRequest request, HttpServletResponse response,@PathVariable(value = "fileName") String fileName) throws IOException {
//
//        File file = new File( fileName);
//        if (file.exists()) {
//
//            //get the mimetype
//            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
//            if (mimeType == null) {
//                //unknown mimetype so set the mimetype to application/octet-stream
//                mimeType = "application/octet-stream";
//            }
//
//            response.setContentType(mimeType);
//
//            /**
//             * In a regular HTTP response, the Content-Disposition response header is a
//             * header indicating if the content is expected to be displayed inline in the
//             * browser, that is, as a Web page or as part of a Web page, or as an
//             * attachment, that is downloaded and saved locally.
//             *
//             */
//
//            /**
//             * Here we have mentioned it to show inline
//             */
//            response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));
//
//            //Here we have mentioned it to show as attachment
//            //response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + file.getName() + "\""));
//
//            response.setContentLength((int) file.length());
//
//            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
//
//            FileCopyUtils.copy(inputStream, response.getOutputStream());
//
//        }
//    }





}
