package com.trinet.ambis.rest.controllers;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.service.ProspectStrategySyncService;
import com.trinet.ambis.service.model.ProspectStrategySyncData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


@RunWith(MockitoJUnitRunner.class)
public class ProspectStrategySyncControllerTest  {

	@InjectMocks
	ProspectStrategySyncController prospectStrategySyncController;

	@Mock
	ProspectStrategySyncService prospectStrategySyncService;

	MockMvc mockMvc;

	private static final String COMPANY_CODE = "a1b2c3";

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(prospectStrategySyncController).build();
	}

	@Test
	public void syncCensusStrategyDataTest1() throws Exception {
		// given
		// data
		List<ProspectStrategySyncData> prospectStrategySyncData = prepareProspectStrategySyncData();
		// method mocks
		doNothing().when(prospectStrategySyncService).handleCensusChangeEvent(prospectStrategySyncData, COMPANY_CODE);
		// when
		mockMvc.perform(MockMvcRequestBuilders
				.put(URIConstants.VERSION_AND_ROOT + URIConstants.PROSPECT_CENSUS_SYNC, "001", "00002222256",
						COMPANY_CODE)
				.content(new ObjectMapper().writeValueAsString(prospectStrategySyncData))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
		// then
		// assertions
		verify(prospectStrategySyncService, times(1)).handleCensusChangeEvent(prospectStrategySyncData, COMPANY_CODE);
	}

	@Test
	public void syncCensusStrategyDataTest() throws Exception {
		// given
		// data
		List<String> employeeIdsToDelete = prepareEmployeeIdsToDelete();
		// method mocks
		doNothing().when(prospectStrategySyncService).handleCensusDeleteEvent(employeeIdsToDelete, COMPANY_CODE);
		// when
		mockMvc.perform(MockMvcRequestBuilders
				.delete(URIConstants.VERSION_AND_ROOT + URIConstants.PROSPECT_CENSUS_SYNC, "001", "00002222256",
						COMPANY_CODE)
				.content(new ObjectMapper().writeValueAsString(employeeIdsToDelete))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
		// then
		// assertions
		verify(prospectStrategySyncService, times(1)).handleCensusDeleteEvent(employeeIdsToDelete, COMPANY_CODE);
	}
	
	@Test
	public void syncProspectStrategyDataForAddEventTest() throws Exception {
		// given
		// data
		List<ProspectStrategySyncData> prospectStrategySyncData = prepareProspectStrategySyncData();
		// method mocks
		doNothing().when(prospectStrategySyncService).handleCensusAddEvent(prospectStrategySyncData, COMPANY_CODE);
		// when
		mockMvc.perform(MockMvcRequestBuilders
				.post(URIConstants.VERSION_AND_ROOT + URIConstants.PROSPECT_CENSUS_SYNC, "001", "00002222256",
						COMPANY_CODE)
				.content(new ObjectMapper().writeValueAsString(prospectStrategySyncData))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
		// then
		// assertions
		verify(prospectStrategySyncService, times(1)).handleCensusAddEvent(prospectStrategySyncData, COMPANY_CODE);

	}

	@Test
	public void rateSyncOnDependentChangeTest() throws Exception {
		// given
		// data
		List<String> employeeIds = List.of("Emp1", "Emp2");
		// method mocks
		doNothing().when(prospectStrategySyncService).rateSyncOnDependentChange(employeeIds, COMPANY_CODE);
		// when
		mockMvc.perform(MockMvcRequestBuilders
				.post(URIConstants.VERSION_AND_ROOT + URIConstants.PROSPECT_TIB_RATE_SYNC_ON_DEPENDENT_CHANGE, "001",
						"00002222256", COMPANY_CODE)
				.content(new ObjectMapper().writeValueAsString(employeeIds))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
		// then
		verify(prospectStrategySyncService, times(1)).rateSyncOnDependentChange(employeeIds, COMPANY_CODE);
	}

    @Test
    public void rateSyncOnCensusDependentChangeTest() throws Exception {
        //given
        //data
        List<ProspectCensusResponse> censusResponse = prepareProspectCensus();
        // method mocks
        doNothing().when(prospectStrategySyncService).rateSyncOnCensusDependentChange(censusResponse, COMPANY_CODE);
        // when
        mockMvc.perform(MockMvcRequestBuilders
                .post(URIConstants.VERSION_AND_ROOT + URIConstants.PROSPECT_TIB_RATE_SYNC_ON_CENSUS_DEPENDENT_CHANGE, "001",
                        "00002222256", COMPANY_CODE)
                .content(new ObjectMapper().writeValueAsString(censusResponse))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
        // then
        verify(prospectStrategySyncService, times(1)).rateSyncOnCensusDependentChange(censusResponse, COMPANY_CODE);

    }
	
	private List<ProspectStrategySyncData> prepareProspectStrategySyncData() {
		List<ProspectStrategySyncData> prospectStrategySyncDataList = new ArrayList<>();
		ProspectStrategySyncData prospectStrategySyncData = new ProspectStrategySyncData();
		prospectStrategySyncData.setEmployeeId("0000000123456");
		prospectStrategySyncData.setK1(false);
		prospectStrategySyncDataList.add(prospectStrategySyncData);
		return prospectStrategySyncDataList;
	}

	private List<String> prepareEmployeeIdsToDelete() {
		return List.of("Emp1", "Emp2");
	}

    private List<ProspectCensusResponse> prepareProspectCensus() {
        return List.of(
                ProspectCensusResponse.builder().employeeId("0000000123456").employeeName("John").state("CA")
                        .gender("M").k1(true).salary(BigDecimal.valueOf(6000)).zip("90210").dob("2000-01-01")
                        .dependents(List.of(
                                ProspectCensusResponse.Dependents.builder().dob("2001-09-28").relation("SP")
                                        .covgElection(true).includeInCost(true).build(),
                                ProspectCensusResponse.Dependents.builder().dob("2022-03-31").relation("CH")
                                        .covgElection(true).includeInCost(true).build()))
                        .build(),
                ProspectCensusResponse.builder().employeeId("0000000123457").employeeName("katty Scott").state("TX")
                        .gender("F").k1(false).salary(BigDecimal.valueOf(4500)).zip("77001").dob("1975-09-28")
                        .dependents(List.of(
                                ProspectCensusResponse.Dependents.builder().dob("2001-09-28").relation("SP")
                                        .covgElection(false).includeInCost(true).build(),
                                ProspectCensusResponse.Dependents.builder().dob("2022-03-31").relation("CH")
                                        .covgElection(true).includeInCost(false).build()))
                        .build());

    }

}