package com.vicky.parkingsystem.repository;

import com.vicky.parkingsystem.model.Parking;
import com.vicky.parkingsystem.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParkingRepository extends JpaRepository<Parking, Long> {
    Optional<Parking> findByVehicleAndExitTimeIsNull(Vehicle vehicle);
}
