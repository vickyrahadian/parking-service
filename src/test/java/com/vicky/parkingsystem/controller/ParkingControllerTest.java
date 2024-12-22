package com.vicky.parkingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vicky.parkingsystem.dto.EntryRequestDTO;
import com.vicky.parkingsystem.dto.EntryResponseDTO;
import com.vicky.parkingsystem.dto.ExitRequestDTO;
import com.vicky.parkingsystem.dto.ParkingFeeResponseDTO;
import com.vicky.parkingsystem.service.ParkingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParkingController.class)
class ParkingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParkingService parkingService;

    @Test
    void testEnterParking() throws Exception {
        // Arrange
        EntryRequestDTO request = new EntryRequestDTO();
        request.setLicensePlate("ABC123");

        EntryResponseDTO response = new EntryResponseDTO();
        response.setLicensePlate("ABC123");
        response.setEntryTime(LocalDateTime.parse("2023-12-22T10:00:00"));

        when(parkingService.enterParking(any(EntryRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/parking/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("ABC123"))
                .andExpect(jsonPath("$.entryTime").value("2023-12-22T10:00:00"));
    }

    @Test
    void testExitParking() throws Exception {
        // Arrange
        ExitRequestDTO request = new ExitRequestDTO();
        request.setLicensePlate("ABC123");

        ParkingFeeResponseDTO response = new ParkingFeeResponseDTO();
        response.setLicensePlate("ABC123");
        response.setHoursParked(3);
        response.setTotalFee(15.0);

        when(parkingService.exitParking(any(ExitRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/parking/exit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("ABC123"))
                .andExpect(jsonPath("$.hoursParked").value(3))
                .andExpect(jsonPath("$.totalFee").value(15.0));
    }
}
