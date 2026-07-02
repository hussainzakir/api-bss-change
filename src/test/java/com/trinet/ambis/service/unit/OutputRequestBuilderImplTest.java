package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.BSSReportDetails;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputData;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.service.impl.outputs.OutputRequestBuilderImpl;
import com.trinet.ambis.service.outputs.OutputReportDataService;
import com.trinet.ambis.service.AppRulesConfigService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
public class OutputRequestBuilderImplTest extends ServiceUnitTest {
	
	@InjectMocks
	private OutputRequestBuilderImpl outputRequestBuilderImpl;
	
	@Mock
	private OutputReportDataService outputReportDataService;
	
	@Mock
	HttpServletRequest httpRequest;

	@Mock
	private AppRulesConfigService appRulesConfigService;

	@Before
	public void setUp() {
		ReflectionTestUtils.setField(AppRulesAndConfigsUtils.class, "appRulesConfigService", appRulesConfigService);
	}

	@Test
	public void prepareBssReportRequestTest() {
		OutputData outputData = new OutputData();
		Company company = new Company();
		
		OutputRequest outputRequest = new OutputRequest();
		outputRequest.setBenefitTypes(Arrays.asList("Med"));
		Mockito.when(outputReportDataService.getData(any(), any(), any())).thenReturn(outputData);
		java.util.Map<String, String> configMap = new java.util.HashMap<>();
		configMap.put("BSS_OUTPUT_PHASE2", "false");
		Mockito.when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(configMap);

		BSSReportDetails bssReportDetails = outputRequestBuilderImpl.prepareBssReportRequest(outputRequest, company,
				httpRequest);
		
		assertNotNull(bssReportDetails);
		assertNotNull(bssReportDetails.getData());
	}

	@Test
	public void prepareBssReportRequestWithNewTemplateIdsTrueTest() {
		OutputData outputData = new OutputData();
		Company company = new Company();

		OutputRequest outputRequest = new OutputRequest();
		outputRequest.setBenefitTypes(Arrays.asList("Med"));
		Mockito.when(outputReportDataService.getData(any(), any(), any())).thenReturn(outputData);
		java.util.Map<String, String> configMap = new java.util.HashMap<>();
		configMap.put("BSS_OUTPUT_PHASE2", "true");
		Mockito.when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(configMap);

		BSSReportDetails bssReportDetails = outputRequestBuilderImpl.prepareBssReportRequest(outputRequest, company,
				httpRequest);

		assertNotNull(bssReportDetails);
		assertNotNull(bssReportDetails.getData());
	}

	@Test
	public void prepareBssReportRequestWithNewTemplateIdsFalseTest() {
		OutputData outputData = new OutputData();
		Company company = new Company();

		OutputRequest outputRequest = new OutputRequest();
		outputRequest.setBenefitTypes(Arrays.asList("Med"));
		Mockito.when(outputReportDataService.getData(any(), any(), any())).thenReturn(outputData);
		java.util.Map<String, String> configMap = new java.util.HashMap<>();
		configMap.put("BSS_OUTPUT_PHASE2", "false");
		Mockito.when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(configMap);

		BSSReportDetails bssReportDetails = outputRequestBuilderImpl.prepareBssReportRequest(outputRequest, company,
				httpRequest);

		assertNotNull(bssReportDetails);
		assertNotNull(bssReportDetails.getData());
	}

}
