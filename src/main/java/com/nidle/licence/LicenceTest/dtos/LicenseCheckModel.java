package com.nidle.licence.LicenceTest.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LicenseCheckModel implements Serializable {

    private static final long serialVersionUID = 8600137500316662317L;
    /**
     * IP addresses that can be allowed
     */
    private List<String> ipAddress;

    /**
     * Allowable MAC address
     */
    private List<String> macAddress;

    /**
     * Allowable CPU serial number
     */
    private String cpuSerial;

    /**
     * Allowed motherboard serial number
     */
    private String mainBoardSerial;

    //Omit setter and getter methods

    @Override
    public String toString() {
        return "LicenseCheckModel{" +
                "ipAddress=" + ipAddress +
                ", macAddress=" + macAddress +
                ", cpuSerial='" + cpuSerial + '\'' +
                ", mainBoardSerial='" + mainBoardSerial + '\'' +
                '}';
    }
}
