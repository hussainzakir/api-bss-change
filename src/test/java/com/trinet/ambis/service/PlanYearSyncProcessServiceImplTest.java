package com.trinet.ambis.service;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.persistence.dao.hrp.PlanYearChangeAuditDao;
import com.trinet.ambis.persistence.dao.ps.PsSubmitDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanYearChangeAudit;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.impl.PlanYearSyncProcessServiceImpl;
import com.trinet.ambis.service.model.PlanYearRequest;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.Utils;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class PlanYearSyncProcessServiceImplTest extends ServiceUnitTest {
	@InjectMocks
	private PlanYearSyncProcessServiceImpl planYearSyncProcessService;

	@Mock
	private CompanyService companyService;

	@Mock
	private RealmPlanYearService realmPlanYearService;

	@Mock
	private ProcessStatusService processStatusService;
	
	@Mock
	PlanYearChangeAuditDao planYearChangeAuditDao;
	
	@Mock
	private PsSubmitDataDao psSubmitDataDao;
	
	@Mock
	private CacheService cacheService;
	
	private static final String USER_ID = "000011233390";
	
	private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;
	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;
	private MockedStatic<PlanYearChangeAudit> mockStaticPlanYearChangeAudit;

	@Before
	public void setUp() {
		mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(USER_ID);
		mockStaticPlanYearChangeAudit = Mockito.mockStatic(PlanYearChangeAudit.class);
	}

	@After
	public void tearDown() {
		if (mockStaticAppRulesAndConfigsUtils != null)
			mockStaticAppRulesAndConfigsUtils.close();
		if (mockStaticBSSSecurityUtils != null)
			mockStaticBSSSecurityUtils.close();
		if(mockStaticPlanYearChangeAudit != null)
			mockStaticPlanYearChangeAudit.close();
	}

	@Test
	public void testUpdatePlanYearSyncProcessStatus_whenPlanYearChanged_shouldCreateProcess() {
		// Arrange
		String companyCode = "2U0D";
		String benefitStartDate = "2025-01-01";
		String quarter = "Q1";
		String companyBenStartDate = "01-Jan-2025";

		PlanYearRequest request = new PlanYearRequest();
		request.setCompanyCode(companyCode);
		request.setBenefitStartDate(benefitStartDate);
		request.setQuarter(quarter);
		request.setServiceOrderNumber("1009");
		request.setCommonOwnerCompany(companyCode);
		request.setQuarterException(true);
		
		Company company = new Company();
		company.setId(1L);
		RealmPlanYear currentPlanYear = new RealmPlanYear();
		currentPlanYear.setId(100L);
		company.setRealmPlanYear(currentPlanYear);
		company.setBenefitStartDate(companyBenStartDate);

		RealmPlanYear newPlanYear = new RealmPlanYear();
		newPlanYear.setId(200L);
		newPlanYear.setPlanYearStart(Utils.convertStringToDate("01-Jan-2025", "dd-MMM-yyyy"));
		newPlanYear.setPlanYearEnd(Utils.convertStringToDate("31-Dec-2025", "dd-MMM-yyyy"));

		Date currentDate = Utils.convertStringToDate("01-Jan-2025", "dd-MMM-yyyy");
		
		when(companyService.getCompanyDetails(eq(companyCode), anyBoolean(), anyString(), isNull()))
				.thenReturn(company);
		when(realmPlanYearService.findRealmPlanYearBy(currentDate,quarter)).thenReturn(newPlanYear);
		doNothing().when(psSubmitDataDao).updateBenefitStartDateAndQuarterForPlanYearSync(eq(company), anyString());
		
		// Act
		planYearSyncProcessService.updatePlanYearSyncProcessStatus(request);

		// Assert
		Mockito.verify(planYearChangeAuditDao).save(Mockito.any(PlanYearChangeAudit.class));
		verify(processStatusService, times(1)).createStrategySyncProcess(eq(companyCode), anyString(),
				eq(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName()),
				eq(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getIdentifierName()));
		verify(psSubmitDataDao, times(1)).updateBenefitStartDateAndQuarterForPlanYearSync(eq(company), anyString());
	}
	
}
