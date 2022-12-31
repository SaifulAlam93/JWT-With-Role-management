package com.nidle.licence.configuration;

import com.nidle.licence.encryption.dto.LicenceFinalKeyDto;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class FileEncryptor {

//    public static void main( String[] args ) {
//
//        try {
//            encryptFile( "F:\\MicroServiceProject\\product\\licence.key", "password" );
////            decryptFile( "E:\\SaifulAlam\\Desktop\\encriptionTest\\test.txt.encryptedKey", "password" );
//        } catch (IOException | GeneralSecurityException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }

    //Arbitrarily selected 8-byte salt sequence:
    private static final byte[] salt = {
            (byte) 0x43, (byte) 0x76, (byte) 0x95, (byte) 0xc7,
            (byte) 0x5b, (byte) 0xd7, (byte) 0x45, (byte) 0x17
    };

    private static Cipher makeCipher(String pass, Boolean decryptMode) throws GeneralSecurityException {

        //Use a KeyFactory to derive the corresponding key from the passphrase:
        PBEKeySpec keySpec = new PBEKeySpec(pass.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(keySpec);

        //Create parameters from the salt and an arbitrary number of iterations:
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 42);

        /*Dump the key to a file for testing: */
        FileEncryptor.keyToFile(key);

        //Set up the cipher:
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");

        //Set the cipher mode to decryption or encryption:
        if (decryptMode) {
            cipher.init(Cipher.ENCRYPT_MODE, key, pbeParamSpec);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, key, pbeParamSpec);
        }

        return cipher;
    }


    /**
     * Encrypts one file to a second file using a key derived from a passphrase:
     **/
    public static void encryptFile(String fileName, String pass)
            throws IOException, GeneralSecurityException {
        BufferedWriter writer = null;

        byte[] decData;
        byte[] encData;
        File inFile = new File(fileName);
//        try {
//            System.out.println(inFile.getCanonicalPath());
//
//            writer = new BufferedWriter(new FileWriter(inFile));
//            writer.write(json);
//        }catch (Exception ignored){
//
//        }

        //Generate the cipher using pass:
        Cipher cipher = FileEncryptor.makeCipher(pass, true);

        //Read in the file:
        FileInputStream inStream = new FileInputStream(inFile);

        int blockSize = 8;
        //Figure out how many bytes are padded
        int paddedCount = blockSize - ((int) inFile.length() % blockSize);

        //Figure out full size including padding
        int padded = (int) inFile.length() + paddedCount;

        decData = new byte[padded];


        inStream.read(decData);

        inStream.close();

        //Write out padding bytes as per PKCS5 algorithm
        for (int i = (int) inFile.length(); i < padded; ++i) {
            decData[i] = (byte) paddedCount;
        }

        //Encrypt the file data:
        encData = cipher.doFinal(decData);


        //Write the encrypted data to a new file:
        FileOutputStream outStream = new FileOutputStream(new File(fileName + ".encrypted" + ".nidle"));
        outStream.write(encData);
        outStream.close();
    }


    /**
     * Decrypts one file to a second file using a key derived from a passphrase:
     **/
    public static void decryptFile(String fileName, String pass)
            throws GeneralSecurityException, IOException {
        byte[] encData;
        byte[] decData;
        File inFile = new File(fileName);

        //Generate the cipher using pass:
        Cipher cipher = FileEncryptor.makeCipher(pass, false);

        //Read in the file:
        FileInputStream inStream = new FileInputStream(inFile);
        encData = new byte[(int) inFile.length()];
        inStream.read(encData);
        inStream.close();
        //Decrypt the file data:
        decData = cipher.doFinal(encData);

        //Figure out how much padding to remove

        int padCount = decData[decData.length - 1];

        //Naive check, will fail if plaintext file actually contained
        //this at the end
        //For robust check, check that padCount bytes at the end have same value
        if (padCount >= 1 && padCount <= 8) {
            decData = Arrays.copyOfRange(decData, 0, decData.length - padCount);
        }

        //Write the decrypted data to a new file:
        FileOutputStream target = new FileOutputStream(new File(inFile.getCanonicalPath() + ".decryptKey"));
        target.write(decData);
        target.close();
        try {
            File file = new File(fileName + ".decryptKey");

            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();

                String str = new String(data, StandardCharsets.UTF_8);
                JSONObject json = new JSONObject(str);
                LicenceFinalKeyDto licenceFinalKeyDto = new LicenceFinalKeyDto(
                        json.getString("macAddress"),
                        json.getString("hostName"),
                        json.getString("licenceType"),
                        json.getString("companyName"),
                        json.getString("licenceStartDate"),
                        json.getString("licenceExpDate"),
                        json.getInt("linePermit"),
                        json.getInt("userPermit"),
                        json.getString("macAddress"));
                String technology = json.getString("macAddress");
                System.out.println(technology);
                String hostName = json.getString("hostName");
                System.out.println("hostName: " + hostName);
                String licenceType = json.getString("licenceType");
                System.out.println("licenceType: "+licenceType);
                String companyName = json.getString("companyName");
                System.out.println("companyName: "+companyName);
                String licenceStartDate = json.getString("licenceStartDate");
                System.out.println("licenceStartDate: "+licenceStartDate);
                String licenceExpDate = json.getString("licenceExpDate");
                System.out.println("licenceExpDate: "+ licenceExpDate);
                Integer userPermit = json.getInt("userPermit");
                System.out.println("userPermit: "+userPermit);
                Integer modules = json.getInt("modules");
                System.out.println("modules: " + modules);
                System.out.println(str);
            } catch (Exception ignored) {
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Record the key to a text file for testing:
     **/
    private static void keyToFile(SecretKey key) {
        try {
            File keyFile = new File("E:\\SaifulAlam\\Desktop\\encriptionTest\\keyfile.txt");
            FileWriter keyStream = new FileWriter(keyFile);
            String encodedKey = "\n" + "Encoded version of key:  " + key.getEncoded().toString();
            keyStream.write(key.toString());
            keyStream.write(encodedKey);
            keyStream.close();
        } catch (IOException e) {
            System.err.println("Failure writing key to file");
            e.printStackTrace();
        }

    }
}