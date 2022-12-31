package com.nidle.licence.LicenceTest.dtos;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * License Parameters required to generate a class
 *
 * @author zifangsky
 * @date 2018/4/19
 * @since 1.0.0
 */
@Data
public class LicenseCreatorParam implements Serializable {

    private static final long serialVersionUID = -7793154252684580872L;
    /**
     * Certificate subject
     */
    private String subject;

    /**
     * Key alias
     */
    private String privateAlias;

    /**
     * Key and password (it needs to be kept properly and cannot be known by the user)
     */
    private String keyPass;

    /**
     * Password to access the secret key library
     */
    private String storePass;

    /**
     * Certificate generation path
     */
    private String licensePath;

    /**
     * Keystore store path
     */
    private String privateKeysStorePath;

    /**
     * Certificate effective time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date issuedTime = new Date();

    /**
     * Certificate expiration time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expiryTime;

    /**
     * customer type
     */
    private String consumerType = "user";

    /**
     * Number of users
     */
    private Integer consumerAmount = 1;

    /**
     * Description information
     */
    private String description = "";

    /**
     * Additional server hardware verification information
     */
    private LicenseCheckModel licenseCheckModel;

    //Omit setter and getter methods

    @Override
    public String toString() {
        return "LicenseCreatorParam{" +
                "subject='" + subject + '\'' +
                ", privateAlias='" + privateAlias + '\'' +
                ", keyPass='" + keyPass + '\'' +
                ", storePass='" + storePass + '\'' +
                ", licensePath='" + licensePath + '\'' +
                ", privateKeysStorePath='" + privateKeysStorePath + '\'' +
                ", issuedTime=" + issuedTime +
                ", expiryTime=" + expiryTime +
                ", consumerType='" + consumerType + '\'' +
                ", consumerAmount=" + consumerAmount +
                ", description='" + description + '\'' +
                ", licenseCheckModel=" + licenseCheckModel +
                '}';
    }
}
