package com.vicky.parkingsystem.controller;

import com.vicky.parkingsystem.dto.EntryRequestDTO;
import com.vicky.parkingsystem.dto.EntryResponseDTO;
import com.vicky.parkingsystem.dto.ExitRequestDTO;
import com.vicky.parkingsystem.dto.ParkingFeeResponseDTO;
import com.vicky.parkingsystem.service.ParkingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
@Tag(name = "Parking Controller", description = "Manage parking operations")
public class ParkingController {

    private final ParkingService parkingService;

    @PostMapping("/entry")
    @Operation(summary = "Register vehicle entry", description = "Registers a vehicle entering the parking lot")
    public ResponseEntity<EntryResponseDTO> enterParking(@Valid @RequestBody EntryRequestDTO request) {
        EntryResponseDTO response = parkingService.enterParking(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exit")
    @Operation(summary = "Register vehicle exit", description = "Registers a vehicle exiting the parking lot and calculates the fee")
    public ResponseEntity<ParkingFeeResponseDTO> exitParking(@Valid @RequestBody ExitRequestDTO request) {
        ParkingFeeResponseDTO response = parkingService.exitParking(request);
        return ResponseEntity.ok(response);
    }
}