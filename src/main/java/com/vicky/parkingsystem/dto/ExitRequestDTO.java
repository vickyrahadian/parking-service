package com.vicky.parkingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExitRequestDTO {
    @NotBlank(message = "License plate must not be blank")
    private String licensePlate;
}
