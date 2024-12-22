package com.vicky.parkingsystem.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vicky.parkingsystem.controller.ParkingController;
import com.vicky.parkingsystem.dto.EntryRequestDTO;
import com.vicky.parkingsystem.dto.ExitRequestDTO;
import com.vicky.parkingsystem.service.ParkingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParkingController.class)
class GlobalExceptionHandlerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParkingService parkingService;

    @Test
    void testHandleIllegalArgumentException() throws Exception {
        // Arrange
        EntryRequestDTO request = new EntryRequestDTO();
        request.setLicensePlate("INVALID_PLATE");

        doThrow(new IllegalArgumentException("Invalid license plate"))
                .when(parkingService).enterParking(any(EntryRequestDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/parking/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid license plate"))
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testHandleValidationExceptions() throws Exception {
        // Arrange
        EntryRequestDTO request = new EntryRequestDTO(); // No license plate provided to trigger validation error

        // Act & Assert
        mockMvc.perform(post("/api/parking/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.licensePlate").value("License plate must not be blank"));
    }

    @Test
    void testHandleGlobalException() throws Exception {
        // Arrange
        ExitRequestDTO request = new ExitRequestDTO();
        request.setLicensePlate("ABC123");

        doThrow(new RuntimeException("Unexpected error"))
                .when(parkingService).exitParking(any(ExitRequestDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/parking/exit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
