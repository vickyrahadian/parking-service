package com.vicky.parkingsystem.service;

import com.vicky.parkingsystem.dto.*;
import com.vicky.parkingsystem.model.Parking;
import com.vicky.parkingsystem.model.Vehicle;
import com.vicky.parkingsystem.repository.ParkingRepository;
import com.vicky.parkingsystem.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final VehicleRepository vehicleRepository;
    private final ParkingRepository parkingRepository;

    @Value("${app.parking.hourly-rate}")
    private double hourlyRate;

    public EntryResponseDTO enterParking(EntryRequestDTO request) {

        Vehicle vehicle = vehicleRepository.findByLicensePlate(request.getLicensePlate())
                .orElseGet(() -> {
                    Vehicle newVehicle = new Vehicle();
                    newVehicle.setLicensePlate(request.getLicensePlate());
                    return vehicleRepository.save(newVehicle);
                });

        if (parkingRepository.findByVehicleAndExitTimeIsNull(vehicle).isPresent()) {
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
        return response;
    }

    public ParkingFeeResponseDTO exitParking(ExitRequestDTO request) {
        Vehicle vehicle = vehicleRepository.findByLicensePlate(request.getLicensePlate())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        Parking parking = parkingRepository.findByVehicleAndExitTimeIsNull(vehicle)
                .orElseThrow(() -> new IllegalArgumentException("No active parking session found for this vehicle"));

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

        return response;
    }
}
