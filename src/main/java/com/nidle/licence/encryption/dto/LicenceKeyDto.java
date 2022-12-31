package com.nidle.licence.encryption.dto;

import lombok.Data;

@Data
public class LicenceKeyDto {
    public LicenceKeyDto() {
    }

    private String fileName;
    private String fileDownloadUri;
    private String fileType;
    private long size;


    private Long id;
    private String macAddress;
    private String hostName;
    private String licenceType;
    private String companyName;

    private String licenceStartDate;
    private String licenceExpDate;

    private Integer linePermit;
    private Integer userPermit;
    private String modules;



    public LicenceKeyDto(String fileName, String fileDownloadUri, String fileType, long size) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileType = fileType;
        this.size = size;
    }
}
