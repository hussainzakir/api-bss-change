package com.trinet.ambis.rest.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.trinet.ambis.service.PlanYearSyncProcessService;
import com.trinet.ambis.service.model.PlanYearRequest;
import com.trinet.ambis.service.unit.ServiceUnitTest;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;

@RunWith(MockitoJUnitRunner.class)
public class PlanYearSyncProcessControllerTest extends ServiceUnitTest {
	@InjectMocks
	PlanYearSyncProcessController planYearSyncProcessController;

	@Mock
	private PlanYearSyncProcessService planYearSyncProcessService;

	private MockMvc mockMvc;

	private PlanYearRequest planYearRequest;

	@Mock
	ObjectMapper objectMapper;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(planYearSyncProcessController).build();
	}

	@Test
	public void updatePlanYearSyncProcessStatusTest() throws Exception {
		planYearRequest = new PlanYearRequest();
		planYearRequest.setCompanyCode("2U0D");
		planYearRequest.setBenefitStartDate("2026-10-14");
		planYearRequest.setQuarter("Q4");

		doNothing().when(planYearSyncProcessService).updatePlanYearSyncProcessStatus(planYearRequest);
		planYearSyncProcessController.updatePlanYearSyncProcessStatus(planYearRequest);
		verify(planYearSyncProcessService, times(1)).updatePlanYearSyncProcessStatus(planYearRequest);
	}
}
