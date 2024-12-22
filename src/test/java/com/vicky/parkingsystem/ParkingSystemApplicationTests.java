package com.vicky.parkingsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

class ParkingSystemApplicationTest {

	@Test
	void testMain() {
		// Mock SpringApplication
		mock(SpringApplication.class);

		// Run the main method with a mock SpringApplication
		try {
			ParkingSystemApplication.main(new String[] {});
		} catch (Exception e) {
			// Main method doesn't return anything; just ensure no exceptions are thrown
			fail("Main method should not throw any exceptions");
		}
	}
}