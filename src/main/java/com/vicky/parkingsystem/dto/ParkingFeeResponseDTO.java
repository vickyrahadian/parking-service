package com.vicky.parkingsystem.dto;

import lombok.Data;

@Data
public class ParkingFeeResponseDTO {
    private String licensePlate;
    private long hoursParked;
    private double totalFee;
}