package com.nidle.licence.LicenceTest.service;

import com.nidle.licence.LicenceTest.dtos.LicenseCreatorParam;
import de.schlichtherle.license.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.prefs.Preferences;


/**
 * License Generating class
 *
 * @author zifangsky
 * @date 2018/4/19
 * @since 1.0.0
 */
public class LicenseCreator {
    private static Logger logger = LogManager.getLogger(LicenseCreator.class);
    private final static X500Principal DEFAULT_HOLDER_AND_ISSUER = new X500Principal("CN=localhost, OU=localhost, O=localhost, L=SH, ST=SH, C=CN");
    private LicenseCreatorParam param;

    public LicenseCreator(LicenseCreatorParam param) {
        this.param = param;
    }

    /**
     * Generate License certificate
     * @author zifangsky
     * @date 2018/4/20 10:58
     * @since 1.0.0
     * @return boolean
     */
    public boolean generateLicense(){
        try {
            LicenseManager licenseManager = new CustomLicenseManager(initLicenseParam());
            LicenseContent licenseContent = initLicenseContent();

            licenseManager.store(licenseContent,new File(param.getLicensePath()));

            return true;
        }catch (Exception e){
            logger.error(MessageFormat.format("Certificate generation failed:{0}",param),e);
            return false;
        }
    }

    /**
     * Initialize certificate generation parameters
     * @author zifangsky
     * @date 2018/4/20 10:56
     * @since 1.0.0
     * @return de.schlichtherle.license.LicenseParam
     */
    private LicenseParam initLicenseParam(){
        Preferences preferences = Preferences.userNodeForPackage(LicenseCreator.class);

        //Set the secret key to encrypt the certificate content
        CipherParam cipherParam = new DefaultCipherParam(param.getStorePass());

//        KeyStoreParam privateStoreParam = new CustomKeyStoreParam(LicenseCreator.class
//                ,param.getPrivateKeysStorePath()
//                ,param.getPrivateAlias()
//                ,param.getStorePass()
//                ,param.getKeyPass());
        KeyStoreParam privateStoreParam = new KeyStoreParam() {
            @Override
            public InputStream getStream() throws IOException {
                return  new ByteArrayInputStream(param.getPrivateKeysStorePath().getBytes
                        (Charset.forName("UTF-8")));
            }

            @Override
            public String getAlias() {
                return param.getPrivateAlias();
            }

            @Override
            public String getStorePwd() {
                return param.getStorePass();
            }

            @Override
            public String getKeyPwd() {
                return param.getKeyPass();
            }
        };

        LicenseParam licenseParam = new DefaultLicenseParam(param.getSubject()
                ,preferences
                ,privateStoreParam
                ,cipherParam);

        return licenseParam;
    }

    /**
     * Set certificate generation body information
     * @author zifangsky
     * @date 2018/4/20 10:57
     * @since 1.0.0
     * @return de.schlichtherle.license.LicenseContent
     */
    private LicenseContent initLicenseContent(){
        LicenseContent licenseContent = new LicenseContent();
        licenseContent.setHolder(DEFAULT_HOLDER_AND_ISSUER);
        licenseContent.setIssuer(DEFAULT_HOLDER_AND_ISSUER);

        licenseContent.setSubject(param.getSubject());
        licenseContent.setIssued(param.getIssuedTime());
        licenseContent.setNotBefore(param.getIssuedTime());
        licenseContent.setNotAfter(param.getExpiryTime());
        licenseContent.setConsumerType(param.getConsumerType());
        licenseContent.setConsumerAmount(param.getConsumerAmount());
        licenseContent.setInfo(param.getDescription());

        //Extended verification server hardware information
        licenseContent.setExtra(param.getLicenseCheckModel());

        return licenseContent;
    }

}
