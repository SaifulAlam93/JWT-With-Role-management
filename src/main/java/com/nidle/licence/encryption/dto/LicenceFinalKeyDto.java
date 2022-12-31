package com.nidle.licence.encryption.dto;

import lombok.Data;

@Data
public final class LicenceFinalKeyDto {
    public LicenceFinalKeyDto( String macAddress, String hostName, String licenceType, String companyName, String licenceStartDate, String licenceExpDate, Integer linePermit, Integer userPermit, String modules) {
        this.macAddress = macAddress;
        this.hostName = hostName;
        this.licenceType = licenceType;
        this.companyName = companyName;
        this.licenceStartDate = licenceStartDate;
        this.licenceExpDate = licenceExpDate;
        this.linePermit = linePermit;
        this.userPermit = userPermit;
        this.modules = modules;
    }

    public final String macAddress;
    public final String hostName;
    public final String licenceType;
    public final String companyName;

    public final String licenceStartDate;
    public final String licenceExpDate;

    public final Integer linePermit;
    public final Integer userPermit;
    public final String modules;

}
