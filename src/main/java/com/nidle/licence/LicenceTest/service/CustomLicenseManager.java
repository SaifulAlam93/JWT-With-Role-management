package com.nidle.licence.LicenceTest.service;

import com.nidle.licence.LicenceTest.dtos.LicenseCheckModel;
import de.schlichtherle.license.*;
import de.schlichtherle.xml.GenericCertificate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.shared.utils.StringUtils;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

/**
 * Custom license manager for additional verification of server hardware information
 *
 * @author zifangsky
 * @date 2018/4/23
 * @since 1.0.0
 */
public class CustomLicenseManager extends LicenseManager{
    private static Logger logger = LogManager.getLogger(CustomLicenseManager.class);

    //XML encoding
    private static final String XML_CHARSET = "UTF-8";
    //Default BUFSIZE
    private static final int DEFAULT_BUFSIZE = 8 * 1024;

    public CustomLicenseManager() {

    }

    public CustomLicenseManager(LicenseParam param) {
        super(param);
    }

    /**
     * Replication create method
     * @author zifangsky
     * @date 2018/4/23 10:36
     * @since 1.0.0
     * @param
     * @return byte[]
     */
    @Override
    protected synchronized byte[] create(
            LicenseContent content,
            LicenseNotary notary)
            throws Exception {
        initialize(content);
        this.validateCreate(content);
        final GenericCertificate certificate = notary.sign(content);
        return getPrivacyGuard().cert2key(certificate);
    }

    /**
     * Copy the install method, where the validate method calls the validate method in this class to verify the IP address, Mac address and other information
     * @author zifangsky
     * @date 2018/4/23 10:40
     * @since 1.0.0
     * @param
     * @return de.schlichtherle.license.LicenseContent
     */
    @Override
    protected synchronized LicenseContent install(
            final byte[] key,
            final LicenseNotary notary)
            throws Exception {
        final GenericCertificate certificate = getPrivacyGuard().key2cert(key);

        notary.verify(certificate);
        final LicenseContent content = (LicenseContent)this.load(certificate.getEncoded());
        this.validate(content);
        setLicenseKey(key);
        setCertificate(certificate);

        return content;
    }

    /**
     * Copy the verify method, call the validate method in this class, and verify the IP address, Mac address and other information
     * @author zifangsky
     * @date 2018/4/23 10:40
     * @since 1.0.0
     * @param
     * @return de.schlichtherle.license.LicenseContent
     */
    @Override
    protected synchronized LicenseContent verify(final LicenseNotary notary)
            throws Exception {
        GenericCertificate certificate = getCertificate();

        // Load license key from preferences,
        final byte[] key = getLicenseKey();
        if (null == key){
            throw new NoLicenseInstalledException(getLicenseParam().getSubject());
        }

        certificate = getPrivacyGuard().key2cert(key);
        notary.verify(certificate);
        final LicenseContent content = (LicenseContent)this.load(certificate.getEncoded());
        this.validate(content);
        setCertificate(certificate);

        return content;
    }

    /**
     * Verify the parameter information of the generated certificate
     * @author zifangsky
     * @date 2018/5/2 15:43
     * @since 1.0.0
     * @param content Certificate body
     */
    protected synchronized void validateCreate(final LicenseContent content)
            throws LicenseContentException {
        final LicenseParam param = getLicenseParam();

        final Date now = new Date();
        final Date notBefore = content.getNotBefore();
        final Date notAfter = content.getNotAfter();
        if (null != notAfter && now.after(notAfter)){
            throw new LicenseContentException("Certificate expiration time cannot be earlier than current time");
        }
        if (null != notBefore && null != notAfter && notAfter.before(notBefore)){
            throw new LicenseContentException("Certificate effective time cannot be later than certificate expiration time");
        }
        final String consumerType = content.getConsumerType();
        if (null == consumerType){
            throw new LicenseContentException("User type cannot be empty");
        }
    }


    /**
     * Copy the validate method, add the IP address, Mac address and other information verification
     * @author zifangsky
     * @date 2018/4/23 10:40
     * @since 1.0.0
     * @param content LicenseContent
     */
    @Override
    protected synchronized void validate(final LicenseContent content)
            throws LicenseContentException {
        //1. First call the validate method of the parent class
        super.validate(content);

        //2. Then verify the user-defined License parameters
        //Allowed parameter information in License
        LicenseCheckModel expectedCheckModel = (LicenseCheckModel) content.getExtra();
        //Real parameter information of current server
        LicenseCheckModel serverCheckModel = getServerInfos();

        if(expectedCheckModel != null && serverCheckModel != null){
            //Verify IP address
            if(!checkIpAddress(expectedCheckModel.getIpAddress(),serverCheckModel.getIpAddress())){
                throw new LicenseContentException("Of the current server IP Not authorized");
            }

            //Verify Mac address
            if(!checkIpAddress(expectedCheckModel.getMacAddress(),serverCheckModel.getMacAddress())){
                throw new LicenseContentException("Of the current server Mac Address is not within the scope of authorization");
            }

            //Verify the main board serial number
            if(!checkSerial(expectedCheckModel.getMainBoardSerial(),serverCheckModel.getMainBoardSerial())){
                throw new LicenseContentException("The current server's motherboard serial number is not within the scope of authorization");
            }

            //Verify CPU serial number
            if(!checkSerial(expectedCheckModel.getCpuSerial(),serverCheckModel.getCpuSerial())){
                throw new LicenseContentException("Of the current server CPU Serial number is not within the scope of authorization");
            }
        }else{
            throw new LicenseContentException("Unable to get server hardware information");
        }
    }


    /**
     * Rewrite XMLDecoder to parse XML
     * @author zifangsky
     * @date 2018/4/25 14:02
     * @since 1.0.0
     * @param encoded XML Type string
     * @return java.lang.Object
     */
    private Object load(String encoded){
        BufferedInputStream inputStream = null;
        XMLDecoder decoder = null;
        try {
            inputStream = new BufferedInputStream(new ByteArrayInputStream(encoded.getBytes(XML_CHARSET)));

            decoder = new XMLDecoder(new BufferedInputStream(inputStream, DEFAULT_BUFSIZE),null,null);

            return decoder.readObject();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            try {
                if(decoder != null){
                    decoder.close();
                }
                if(inputStream != null){
                    inputStream.close();
                }
            } catch (Exception e) {
                logger.error("XMLDecoder analysis XML fail",e);
            }
        }

        return null;
    }

    /**
     * Obtain the License parameters that need additional verification for the current server
     * @author zifangsky
     * @date 2018/4/23 14:33
     * @since 1.0.0
     * @return demo.LicenseCheckModel
     */
    private LicenseCheckModel getServerInfos(){
        //Operating system type
        String osName = System.getProperty("os.name").toLowerCase();
        AbstractServerInfos abstractServerInfos = null;

        //Choose different data acquisition methods according to different operating system types
        if (osName.startsWith("windows")) {
            abstractServerInfos = new WindowsServerInfos();
        } else if (osName.startsWith("linux")) {
            abstractServerInfos = new LinuxServerInfos();
        }else{//Other server types
            abstractServerInfos = new LinuxServerInfos();
        }

        return abstractServerInfos.getServerInfos();
    }

    /**
     * Verify that the IP/Mac address of the current server is within the allowed IP range < br / >
     * Returns true if there is an IP within the allowed IP/Mac address range
     * @author zifangsky
     * @date 2018/4/24 11:44
     * @since 1.0.0
     * @return boolean
     */
    private boolean checkIpAddress(List<String> expectedList,List<String> serverList){
        if(expectedList != null && expectedList.size() > 0){
            if(serverList != null && serverList.size() > 0){
                for(String expected : expectedList){
                    if(serverList.contains(expected.trim())){
                        return true;
                    }
                }
            }

            return false;
        }else {
            return true;
        }
    }

    /**
     * Verify that the serial number of the current server hardware (motherboard, CPU, etc.) is within the allowable range
     * @author zifangsky
     * @date 2018/4/24 14:38
     * @since 1.0.0
     * @return boolean
     */
    private boolean checkSerial(String expectedSerial,String serverSerial){
        if(StringUtils.isNotBlank(expectedSerial)){
            if(StringUtils.isNotBlank(serverSerial)){
                if(expectedSerial.equals(serverSerial)){
                    return true;
                }
            }

            return false;
        }else{
            return true;
        }
    }

}
