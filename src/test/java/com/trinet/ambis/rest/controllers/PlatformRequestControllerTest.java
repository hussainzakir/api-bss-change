package com.trinet.ambis.rest.controllers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.AppRulesConfigService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.QueuedSubmitService;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.service.submit.SubmitService;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
public class PlatformRequestControllerTest extends ServiceUnitTest {

	@InjectMocks
	private PlatformRequestController platformRequestController;

	@Mock
	private SubmitService submitService;

	@Mock
	private QueuedSubmitService queuedSubmitService;

	@Mock
	private CompanyService companyService;

	@Mock
	private AppRulesConfigService appRulesConfigService;

	@Before
	public void setUp() {
		AppRulesAndConfigsUtils.setAppRuleConfigService(appRulesConfigService);
	}

	@Test
	public void defaultSubmit_queueEnabled() {

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		String companyCode = "";
		String peoId = "";
		String quarter = "";
		Map<String, String> rulesConfigs = new HashMap<>();
		rulesConfigs.put("SUBMIT_QUE_ENABLED", "true");
		String userId = ProcessStatusEnum.TERMED_CLIENT_DEFAULT_SUBMIT.getProcessName();
		Company company = new Company();
		company.setRenewalCompany(true);

		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(rulesConfigs);

		platformRequestController.termDefaultSubmit(request, companyCode, peoId, quarter);

		verify(queuedSubmitService, times(1)).createAsyncDefaultSubmitProcess(companyCode, userId);

	}

	@Test
	public void defaultSubmit_queueDisabled() {

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		String companyCode = "";
		String peoId = "";
		String quarter = "";
		Map<String, String> rulesConfigs = new HashMap<>();
		rulesConfigs.put("SUBMIT_QUE_ENABLED", "false");
		String userId = ProcessStatusEnum.TERMED_CLIENT_DEFAULT_SUBMIT.getProcessName();
		Company company = new Company();
		company.setRenewalCompany(true);

		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(rulesConfigs);

		platformRequestController.termDefaultSubmit(request, companyCode, peoId, quarter);

		verify(queuedSubmitService, times(0)).createAsyncDefaultSubmitProcess(companyCode, userId);
		verify(submitService, times(1)).defaultSubmit(companyCode, quarter, userId);
	}

	@Test
	public void defaultSubmit_newCompany() {

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		String companyCode = "TEST";
		String peoId = "PAS";
		String quarter = "Q1";
		Map<String, String> rulesConfigs = new HashMap<>();
		rulesConfigs.put("SUBMIT_QUE_ENABLED", "true");
		String userId = ProcessStatusEnum.TERMED_CLIENT_DEFAULT_SUBMIT.getProcessName();
		Company company = new Company();

		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(rulesConfigs);

		platformRequestController.termDefaultSubmit(request, companyCode, peoId, quarter);

		verify(queuedSubmitService, times(1)).createAsyncDefaultSubmitProcess(companyCode, userId);
		verify(submitService, times(0)).defaultSubmit(anyString(), anyString(), anyString());
	}

}
