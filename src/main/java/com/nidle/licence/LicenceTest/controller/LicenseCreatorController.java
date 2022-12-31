package com.nidle.licence.LicenceTest.controller;


import com.nidle.licence.LicenceTest.dtos.LicenseCheckModel;

import com.nidle.licence.LicenceTest.dtos.LicenseCreatorParam;
import com.nidle.licence.LicenceTest.service.AbstractServerInfos;
import com.nidle.licence.LicenceTest.service.LicenseCreator;
import com.nidle.licence.LicenceTest.service.LinuxServerInfos;
import com.nidle.licence.LicenceTest.service.WindowsServerInfos;
import org.apache.maven.shared.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * It is used to generate certificate files and cannot be placed in the code deployed to customers
 *
 * @author zifangsky
 * @date 2018/4/26
 * @since 1.0.0
 */
@RestController
@RequestMapping("/license")
public class LicenseCreatorController {

    /**
     * Certificate generation path
     */
    @Value("${license.licensePath}")
    private String licensePath;

    /**
     * Get server hardware information
     *
     * @param osName Operating system type. If it is empty, it will be judged automatically
     * @return com.ccx.models.license.LicenseCheckModel
     * @author zifangsky
     * @date 2018/4/26 13:13
     * @since 1.0.0
     */
    @RequestMapping(value = "/getServerInfos", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public LicenseCheckModel getServerInfos(@RequestParam(value = "osName", required = false) String osName) {
        //Operating system type
        if (StringUtils.isBlank(osName)) {
            osName = System.getProperty("os.name");
        }
        osName = osName.toLowerCase();

        AbstractServerInfos abstractServerInfos = null;

        //Choose different data acquisition methods according to different operating system types
        if (osName.startsWith("windows")) {
            abstractServerInfos = new WindowsServerInfos();
        } else if (osName.startsWith("linux")) {
            abstractServerInfos = new LinuxServerInfos();
        } else {//Other server types
            abstractServerInfos = new LinuxServerInfos();
        }

        return abstractServerInfos.getServerInfos();
    }

    /**
     * Generate certificate
     *
     * @param param Parameters required for certificate generation, such as: {"subject": "CCX models", "privatealias": "privatekey", "KEYPASS": "5t7zz5y0djfcqtxvzkh5ldgjjsgmzq", "storepass": "3538cef8e7", "licensepath": "C: / users / zifangsky / desktop"/ license.lic ","privateKeysStorePath":"C:/Users/zifangsky/Desktop/ privateKeys.keystore ","issuedTime":"2018-04-26 14:48:12", }}
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author zifangsky
     * @date 2018/4/26 13:13
     * @since 1.0.0
     */
    @RequestMapping(value = "/generateLicense", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public Map<String, Object> generateLicense(@RequestBody(required = true) LicenseCreatorParam param) {
        Map<String, Object> resultMap = new HashMap<>(2);

        if (StringUtils.isBlank(param.getLicensePath())) {
            param.setLicensePath(licensePath);
        }

        LicenseCreator licenseCreator = new LicenseCreator(param);
        boolean result = licenseCreator.generateLicense();

        if (result) {
            resultMap.put("result", "ok");
            resultMap.put("msg", param);
        } else {
            resultMap.put("result", "error");
            resultMap.put("msg", "Certificate file generation failed!");
        }
        return resultMap;
    }
}
