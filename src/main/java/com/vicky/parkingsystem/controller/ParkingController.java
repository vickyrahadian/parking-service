package com.vicky.parkingsystem.controller;

import com.vicky.parkingsystem.dto.EntryRequestDTO;
import com.vicky.parkingsystem.dto.EntryResponseDTO;
import com.vicky.parkingsystem.dto.ExitRequestDTO;
import com.vicky.parkingsystem.dto.ParkingFeeResponseDTO;
import com.vicky.parkingsystem.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    @PostMapping("/entry")
    public ResponseEntity<EntryResponseDTO> enterParking(@Valid @RequestBody EntryRequestDTO request) {
        EntryResponseDTO response = parkingService.enterParking(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exit")
    public ResponseEntity<ParkingFeeResponseDTO> exitParking(@Valid @RequestBody ExitRequestDTO request) {
        ParkingFeeResponseDTO response = parkingService.exitParking(request);
        return ResponseEntity.ok(response);
    }
}