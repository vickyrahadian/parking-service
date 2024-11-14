package com.vicky.parkingsystem.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EntryResponseDTO {
    private String licensePlate;
    private LocalDateTime entryTime;
}
