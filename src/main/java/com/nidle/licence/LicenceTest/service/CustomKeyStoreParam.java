package com.nidle.licence.LicenceTest.service;//package com.softron.masterdata.LicenceTest.service;
//
//import de.schlichtherle.license.KeyStoreParam;
//
//import java.io.IOException;
//import java.io.InputStream;
//
//public class CustomKeyStoreParam implements KeyStoreParam {
//    String privateKeysStorePath;
//    String privateAlias;
//    String storePass;
//    String keyPass;
//
//    Class<LicenseCreator> licenseCreatorClass;
//    public CustomKeyStoreParam(Class<LicenseCreator> licenseCreatorClass,
//                               String privateKeysStorePath,
//                               String privateAlias,
//                               String storePass,
//                               String keyPass) {
//        this.licenseCreatorClass = licenseCreatorClass;
//        this.privateKeysStorePath = privateKeysStorePath;
//        this.privateAlias = privateAlias;
//        this.storePass = storePass;
//        this.keyPass = keyPass;
//    }
//
//    @Override
//    public InputStream getStream() throws IOException {
//        return licenseCreatorClass.;
//    }
//
//    @Override
//    public String getAlias() {
//        return privateKeysStorePath;
//    }
//
//    @Override
//    public String getStorePwd() {
//        return privateAlias;
//    }
//
//    @Override
//    public String getKeyPwd() {
//        return storePass;
//    }
//}
