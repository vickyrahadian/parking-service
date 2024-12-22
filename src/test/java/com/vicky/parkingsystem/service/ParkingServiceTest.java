package com.vicky.parkingsystem.service;

import com.vicky.parkingsystem.dto.EntryRequestDTO;
import com.vicky.parkingsystem.dto.EntryResponseDTO;
import com.vicky.parkingsystem.dto.ExitRequestDTO;
import com.vicky.parkingsystem.dto.ParkingFeeResponseDTO;
import com.vicky.parkingsystem.model.Parking;
import com.vicky.parkingsystem.model.Vehicle;
import com.vicky.parkingsystem.repository.ParkingRepository;
import com.vicky.parkingsystem.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.InjectMocks;
import org.mockito.Mock;


import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @InjectMocks
    private ParkingService parkingService;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ParkingRepository parkingRepository;

    @BeforeEach
    void setUp() {
        parkingService = new ParkingService(vehicleRepository, parkingRepository);
        parkingService.setHourlyRate(5.0);
    }

    @Test
    void testEnterParking_NewVehicle() {
        // Arrange
        String licensePlate = "ABC123";
        EntryRequestDTO request = new EntryRequestDTO();
        request.setLicensePlate(licensePlate);

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(parkingRepository.findByVehicleAndExitTimeIsNull(any(Vehicle.class))).thenReturn(Optional.empty());
        when(parkingRepository.save(any(Parking.class))).thenAnswer(invocation -> {
            Parking parking = invocation.getArgument(0);
            parking.setEntryTime(LocalDateTime.now());
            return parking;
        });

        // Act
        EntryResponseDTO response = parkingService.enterParking(request);

        // Assert
        assertNotNull(response);
        assertEquals(licensePlate, response.getLicensePlate());
        assertNotNull(response.getEntryTime());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
        verify(parkingRepository, times(1)).save(any(Parking.class));
    }

    @Test
    void testEnterParking_ExistingVehicleAlreadyParked() {
        // Arrange
        String licensePlate = "ABC123";
        EntryRequestDTO request = new EntryRequestDTO();
        request.setLicensePlate(licensePlate);

        Vehicle existingVehicle = new Vehicle();
        existingVehicle.setLicensePlate(licensePlate);

        Parking activeParking = new Parking();
        activeParking.setVehicle(existingVehicle);
        activeParking.setEntryTime(LocalDateTime.now());

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(existingVehicle));
        when(parkingRepository.findByVehicleAndExitTimeIsNull(existingVehicle)).thenReturn(Optional.of(activeParking));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parkingService.enterParking(request));
        assertEquals("Vehicle with license plate " + licensePlate + " is already parked.", exception.getMessage());
        verify(parkingRepository, never()).save(any(Parking.class));
    }

    @Test
    void testExitParking() {
        // Arrange
        String licensePlate = "ABC123";
        ExitRequestDTO request = new ExitRequestDTO();
        request.setLicensePlate(licensePlate);

        Vehicle existingVehicle = new Vehicle();
        existingVehicle.setLicensePlate(licensePlate);

        Parking activeParking = new Parking();
        activeParking.setVehicle(existingVehicle);
        activeParking.setEntryTime(LocalDateTime.now().minusHours(3));
        activeParking.setExitTime(LocalDateTime.now());

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(existingVehicle));
        when(parkingRepository.findByVehicleAndExitTimeIsNull(existingVehicle)).thenReturn(Optional.of(activeParking));
        when(parkingRepository.save(any(Parking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ParkingFeeResponseDTO response = parkingService.exitParking(request);

        // Assert
        assertNotNull(response);
        assertEquals(licensePlate, response.getLicensePlate());
        assertEquals(3, response.getHoursParked());
        assertEquals(15.0, response.getTotalFee()); // Assuming hourly rate is set to $5

        verify(parkingRepository, times(1)).save(activeParking);
    }

    @Test
    void testExitParking_NoActiveSession() {
        // Arrange
        String licensePlate = "ABC123";
        ExitRequestDTO request = new ExitRequestDTO();
        request.setLicensePlate(licensePlate);

        Vehicle existingVehicle = new Vehicle();
        existingVehicle.setLicensePlate(licensePlate);

        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(existingVehicle));
        when(parkingRepository.findByVehicleAndExitTimeIsNull(existingVehicle)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parkingService.exitParking(request));
        assertEquals("No active parking session found for this vehicle", exception.getMessage());
        verify(parkingRepository, never()).save(any(Parking.class));
    }

    @Test
    void testExitParking_VehicleNotFound() {
        // Arrange
        String licensePlate = "NON_EXISTENT_PLATE";
        ExitRequestDTO request = new ExitRequestDTO();
        request.setLicensePlate(licensePlate);

        // Mock the repository to return an empty result
        when(vehicleRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parkingService.exitParking(request));

        // Validate the exception message
        assertEquals("Vehicle not found", exception.getMessage());

        // Verify no further interactions occurred with the parking repository
        verify(parkingRepository, never()).findByVehicleAndExitTimeIsNull(any());
        verify(parkingRepository, never()).save(any(Parking.class));
    }

}