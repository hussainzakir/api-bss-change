package com.trinet.ambis.rest.controllers;

import com.trinet.ambis.service.FlexRateService;
import com.trinet.ambis.service.model.RateUpdateDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FlexRateControllerTest {

	@InjectMocks
	private FlexRateController flexRateController;

	@Mock
	private FlexRateService flexRateService;

	private static final String COMPANY_CODE = "G48";

	private RateUpdateDto dto;

	@Before
	public void setUp() {
		dto = new RateUpdateDto();
		dto.setCompanyCode(COMPANY_CODE);
		dto.setRateGroupId("RG1");
		dto.setQuarter("EXIII");
		dto.setEffectiveDate("2026-04-23");
	}

	/**
	 * GIVEN a RateUpdateDto with a changed rate group
	 * WHEN updateRateGroup is called
	 * THEN processRateUpdateEvent returns true and the response status is ACCEPTED
	 */
	@Test
	public void testUpdateRateGroup_Changed() {
		// Arrange
		when(flexRateService.processRateUpdateEvent(dto)).thenReturn(true);

		// Act
		ResponseEntity<?> response = flexRateController.updateRateGroup(COMPANY_CODE, dto);

		// Assert
		assertNotNull(response);
		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertNull(response.getBody());
		verify(flexRateService, times(1)).processRateUpdateEvent(dto);
	}

	/**
	 * GIVEN a RateUpdateDto with the same rate group
	 * WHEN updateRateGroup is called
	 * THEN processRateUpdateEvent returns false and the response status is OK
	 */
	@Test
	public void testUpdateRateGroup_Unchanged() {
		// Arrange
		when(flexRateService.processRateUpdateEvent(dto)).thenReturn(false);

		// Act
		ResponseEntity<?> response = flexRateController.updateRateGroup(COMPANY_CODE, dto);

		// Assert
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNull(response.getBody());
		verify(flexRateService, times(1)).processRateUpdateEvent(dto);
	}

	/**
	 * GIVEN a RateUpdateDto and the service throws an exception
	 * WHEN updateRateGroup is called
	 * THEN the controller throws the same RuntimeException
	 */
	@Test(expected = RuntimeException.class)
	public void testUpdateRateGroup_ServiceThrows() {
		// Arrange
		when(flexRateService.processRateUpdateEvent(dto)).thenThrow(new RuntimeException("Service error"));

		// Act
		flexRateController.updateRateGroup(COMPANY_CODE, dto);
	}
}