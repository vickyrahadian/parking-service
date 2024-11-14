package com.vicky.parkingsystem.service;

import com.vicky.parkingsystem.dto.*;
import com.vicky.parkingsystem.model.Parking;
import com.vicky.parkingsystem.model.Vehicle;
import com.vicky.parkingsystem.repository.ParkingRepository;
import com.vicky.parkingsystem.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingService {

    private final VehicleRepository vehicleRepository;
    private final ParkingRepository parkingRepository;

    @Value("${app.parking.hourly-rate}")
    private double hourlyRate;

    public EntryResponseDTO enterParking(EntryRequestDTO request) {
        log.info("Entering parking for vehicle with license plate : {}", request.getLicensePlate());

        Vehicle vehicle = vehicleRepository.findByLicensePlate(request.getLicensePlate())
                .orElseGet(() -> {
                    Vehicle newVehicle = new Vehicle();
                    newVehicle.setLicensePlate(request.getLicensePlate());
                    log.info("New vehicle created with license plate : {}", request.getLicensePlate());
                    return vehicleRepository.save(newVehicle);
                });

        if (parkingRepository.findByVehicleAndExitTimeIsNull(vehicle).isPresent()) {
            log.warn("Vehicle with license plate {} is already parked", request.getLicensePlate());
            throw new IllegalArgumentException("Vehicle with license plate " + request.getLicensePlate() + " is already parked.");
        }

        // Create a new parking record
        Parking parking = new Parking();
        parking.setVehicle(vehicle);
        parking.setEntryTime(LocalDateTime.now());
        Parking savedParking = parkingRepository.save(parking);

        EntryResponseDTO response = new EntryResponseDTO();
        response.setLicensePlate(savedParking.getVehicle().getLicensePlate());
        response.setEntryTime(savedParking.getEntryTime());
        log.info("Vehicle with license plate {} entered at {}", request.getLicensePlate(), savedParking.getEntryTime());

        return response;
    }

    public ParkingFeeResponseDTO exitParking(ExitRequestDTO request) {
        log.info("Exiting parking for vehicle with license plate : {}", request.getLicensePlate());

        Vehicle vehicle = vehicleRepository.findByLicensePlate(request.getLicensePlate())
                .orElseThrow(() -> {
                    log.error("Vehicle with license plate {} not found", request.getLicensePlate());
                    return new IllegalArgumentException("Vehicle not found");
                });

        Parking parking = parkingRepository.findByVehicleAndExitTimeIsNull(vehicle)
                .orElseThrow(() -> {
                    log.error("No active parking session for vehicle with license plate {}", request.getLicensePlate());
                    return new IllegalArgumentException("No active parking session found for this vehicle");
                });

        // Set the exit time and calculate duration and fee
        parking.setExitTime(LocalDateTime.now());
        long hoursParked = Duration.between(parking.getEntryTime(), parking.getExitTime()).toHours();
        parking.setDuration(hoursParked);
        parking.setFee(hoursParked * hourlyRate);
        parkingRepository.save(parking);

        // Prepare response DTO
        ParkingFeeResponseDTO response = new ParkingFeeResponseDTO();
        response.setLicensePlate(parking.getVehicle().getLicensePlate());
        response.setHoursParked(parking.getDuration());
        response.setTotalFee(parking.getFee());
        log.info("Vehicle with license plate {} exited. Duration: {} hours, Fee: ${}", request.getLicensePlate(), parking.getDuration(), parking.getFee());

        return response;
    }
}
