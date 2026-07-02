package com.trinet.ambis.persistence.dao.test;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.exception.CompanyNotFound;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.impl.StrategyPreLoadDaoImpl;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.ApplicationContextProvider;
import com.trinet.ambis.util.CommonUtils;

@RunWith(MockitoJUnitRunner.class)
public class StrategyPreLoadDaoImplTest extends ServiceUnitTest{

	@InjectMocks
	StrategyPreLoadDaoImpl strategyPreLoadDaoImpl;

	@Mock
	PsDao psDao;

	@Mock
	EmailGenService emailGenService;

	@Mock
	CompanyService companyService;

	@Mock
	StrategyService strategyService;

	@Mock
	StrategyDao strategyDao;

	@Mock
	ProcessStatusService processStatusService;

	private static final Logger logger = LoggerFactory.getLogger(StrategyPreLoadDaoImplTest.class);

	Company company = null;
	ProcessStatus ps = null;
    private MockedStatic<ApplicationContextProvider> applicationContextProviderMockedStatic;
    private MockedStatic<CommonUtils> commonUtilsMockedStatic;

    @After
    public void tearDown() {
        if (applicationContextProviderMockedStatic != null)
        applicationContextProviderMockedStatic.close();
        if (commonUtilsMockedStatic != null)
        commonUtilsMockedStatic.close();
    }

	@Before
	public void setup() {
        applicationContextProviderMockedStatic = Mockito.mockStatic(ApplicationContextProvider.class);
        commonUtilsMockedStatic = Mockito.mockStatic(CommonUtils.class);
        company = new Company();
		company.setId(1L);
		company.setCode("COMPANY1");
		company.setPlanStartDate("01-JAN-2018");
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(10);
		company.setRealmPlanYear(rpy);

		ps = new ProcessStatus();
		ps.setId(1L);
		ps.setProcessIdentifer("PRE_LOAD");
		ps.setProcessStatus("I");
		
		ApplicationContext context = mock(ApplicationContext.class);
        applicationContextProviderMockedStatic.when(ApplicationContextProvider::getApplicationContext).thenReturn(context);
		when(context.getBean("emailGenService")).thenReturn(emailGenService);
		when(context.getBean("psDao")).thenReturn(psDao);
		when(context.getBean("companyService")).thenReturn(companyService);
		when(context.getBean("strategyService")).thenReturn(strategyService);
		when(context.getBean("strategyDao")).thenReturn(strategyDao);
		when(context.getBean("processStatusService")).thenReturn(processStatusService);
	}

	@Test
	public void createEntityManager() {

		ApplicationContext context = mock(ApplicationContext.class);
        applicationContextProviderMockedStatic.when(ApplicationContextProvider::getApplicationContext).thenReturn(null);

		strategyPreLoadDaoImpl.createEntityManager();
        applicationContextProviderMockedStatic.when(ApplicationContextProvider::getApplicationContext).thenReturn(context);
		when(context.getBean("emailGenService")).thenReturn(emailGenService);
		when(context.getBean("psDao")).thenReturn(psDao);
		when(context.getBean("companyService")).thenReturn(companyService);
		when(context.getBean("strategyService")).thenReturn(strategyService);
		when(context.getBean("strategyDao")).thenReturn(strategyDao);
		when(context.getBean("processStatusService")).thenReturn(processStatusService);
		strategyPreLoadDaoImpl.createEntityManager();
	}

	@Test
	public void preLoadBssStrategies() throws Exception {


		String emplId = "123456789";
		String userId = "123456789";
		String peodId = "SOI";
		String quarter = "SM";
		Long relamYearId = 20L;
		Date payrollCutOffDate = new Date();
		List<String> companyList = null;
		List<Strategy> strategies = new ArrayList<Strategy>();

		/*
		 * Null companyList
		 */

		when(processStatusService.createPreLoadProcess(quarter, emplId)).thenReturn(ps);
		when(psDao.getPreLoadClients(peodId, quarter, relamYearId, payrollCutOffDate)).thenReturn(companyList);
		when(processStatusService.isStrategySummariesProcessed(company.getCode())).thenReturn(true);

		doNothing().when(emailGenService).createPreLoadEmail(anyInt(), anyString(), anyString());

		strategyPreLoadDaoImpl.preLoadBssStrategies(peodId, quarter, relamYearId, payrollCutOffDate, emplId);

		/*
		 * Empty companyList
		 */
		companyList = new ArrayList<String>();
		when(processStatusService.createPreLoadProcess(quarter, emplId)).thenReturn(ps);
		when(psDao.getPreLoadClients(peodId, quarter, relamYearId, payrollCutOffDate)).thenReturn(companyList);
		when(processStatusService.isStrategySummariesProcessed(company.getCode())).thenReturn(true);

		strategyPreLoadDaoImpl.preLoadBssStrategies(peodId, quarter, relamYearId, payrollCutOffDate, emplId);

		/*
		 * One non-renewal company in companyList
		 */
		companyList.add(company.getCode());
		when(processStatusService.createPreLoadProcess(quarter, emplId)).thenReturn(ps);
		when(psDao.getPreLoadClients(peodId, quarter, relamYearId, payrollCutOffDate)).thenReturn(companyList);
		when(processStatusService.isStrategySummariesProcessed(company.getCode())).thenReturn(true);

		strategyPreLoadDaoImpl.preLoadBssStrategies(peodId, quarter, relamYearId, payrollCutOffDate, emplId);

		/*
		 * One renewal company in companyList; empty strategies
		 */
		company.setRenewalCompany(true);
		when(processStatusService.createPreLoadProcess(quarter, emplId)).thenReturn(ps);
		when(psDao.getPreLoadClients(peodId, quarter, relamYearId, payrollCutOffDate)).thenReturn(companyList);
		when(processStatusService.isStrategySummariesProcessed(company.getCode())).thenReturn(true);

		strategyPreLoadDaoImpl.preLoadBssStrategies(peodId, quarter, relamYearId, payrollCutOffDate, emplId);

		/*
		 * One renewal company in companyList; one strategy
		 */
		company.setRenewalCompany(true);
		strategies.add(new Strategy());
		when(processStatusService.createPreLoadProcess(quarter, emplId)).thenReturn(ps);
		when(psDao.getPreLoadClients(peodId, quarter, relamYearId, payrollCutOffDate)).thenReturn(companyList);
		when(processStatusService.isStrategySummariesProcessed(company.getCode())).thenReturn(true);

		strategyPreLoadDaoImpl.preLoadBssStrategies(peodId, quarter, relamYearId, payrollCutOffDate, emplId);

		/*
		 * Exception for getCompanyDetails
		 */
		CompanyNotFound exception = new CompanyNotFound();
		when(processStatusService.createPreLoadProcess(quarter, emplId)).thenReturn(ps);
		when(psDao.getPreLoadClients(peodId, quarter, relamYearId, payrollCutOffDate)).thenReturn(companyList);

		when(processStatusService.isStrategySummariesProcessed(company.getCode())).thenReturn(true);

		strategyPreLoadDaoImpl.preLoadBssStrategies(peodId, quarter, relamYearId, payrollCutOffDate, emplId);
	}

}