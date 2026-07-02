package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.helper.RenewalServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.service.BandCodesService;
import com.trinet.ambis.service.BenefitPlanService;
import com.trinet.ambis.service.FlexRateService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.model.FlexRateResponse;
import com.trinet.ambis.service.model.PayInRateInfo;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.ps.impl.BenEligRulesDaoImpl;
import com.trinet.ambis.persistence.dao.ps.impl.BenEligRulesImpl;
import com.trinet.ambis.persistence.dao.ps.impl.BenProgInactivateDaoImpl;
import com.trinet.ambis.persistence.dao.ps.impl.BenefitProgramDaoImpl;
import com.trinet.ambis.persistence.dao.ps.impl.EligConfigDaoImpl;
import com.trinet.ambis.persistence.dao.ps.impl.PsSubmitDataDaoImpl;
import com.trinet.ambis.persistence.dao.ps.impl.SavedPlanOptns;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.GroupRate;
import com.trinet.ambis.persistence.model.GroupRatePK;
import com.trinet.ambis.persistence.model.SubmitPayload;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.model.StrategyBenefitGroup;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.SubmissionInfo;
import com.trinet.ambis.service.submit.SubmitService;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.ApplicationContextProvider;
import com.trinet.ambis.util.RulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class PsSubmitTransactionTest extends ServiceUnitTest {

	private static final String MOCK_SQL = "MOCK-QUERY";

	PsSubmitDataDaoImpl psSubmitDataDaoImpl;
	EntityManager psEntityManager;
	EntityManager bssEM;
	Connection mockCn;
	PreparedStatement mockStmt;
	Session mockSession;
	Query mockedBatchQuery;
	org.hibernate.query.Query mockHibernateQuery;
	@Mock
	BandCodesService bandCodesService;
	@Mock
	FlexRateService flexRateService;

	private Company company = CommonServiceHelper.jsonToObject(companyJson(), Company.class);
	private StrategyData strategyData = CommonServiceHelper.jsonToObject(strategyDataJson(), StrategyData.class);
	private BenefitGroupStrategy benefitGroupStrategy = CommonServiceHelper.jsonToObject(benefitGroupStrategyJson(),
			BenefitGroupStrategy.class);
	private Query mockHasBenProgGrp = mock(Query.class);
	private Query mockDelEeBpg = mock(Query.class);
	private Query mockSelBssEe = mock(Query.class);
	private Query mockInsEeBpg = mock(Query.class);
	private Query mockInsBenPlanYear = mock(Query.class);
	private Query mockSelCurrPlans = mock(Query.class);
	private Query mockSelLeavePlans = mock(Query.class);
	private Query mockSelCurrSavPlans = mock(Query.class);
	private Query mockSelCurrSavOptns = mock(Query.class);
	private Query mockSelCurrFSAPlans = mock(Query.class);
	private Query mockSelCurrFSAOptns = mock(Query.class);
	private Query mockSelMaxOptionCost = mock(Query.class);
	private Query mockInsSavedOptn = mock(Query.class);
	private Query mockInsSavedCost = mock(Query.class);
	private Query mockInsSavedPlan = mock(Query.class);
	private Query mockDelLvSvPlan = mock(Query.class);
	private Query mockDelLvSvOptn = mock(Query.class);
	private Query mockDelLvSvCost = mock(Query.class);
	private Query mockDelDefnPlan = mock(Query.class);
	private Query mockDelDefnOptn = mock(Query.class);
	private Query mockDelDefnCost = mock(Query.class);
	private Query mockSelClonePlanTypes = mock(Query.class);
	private Query mockInsDefnPlan = mock(Query.class);
	private Query mockInsDefnOptn = mock(Query.class);
	private Query mockInsDefnCost = mock(Query.class);
	private Query mockDelFutDefnPgm = mock(Query.class);
	private Query mockDelFutDefnPlan = mock(Query.class);
	private Query mockDelFutDefnOptn = mock(Query.class);
	private Query mockDelFutDefnCost = mock(Query.class);
	private Query mockDelElgPygrp = mock(Query.class);
	private Query mockDelElgBnstat = mock(Query.class);
	private Query mockDelElgEeclas = mock(Query.class);
	private Query mockDelElgCnfig1 = mock(Query.class);
	private Query mockDelElgRules = mock(Query.class);
	private Query mockSelCloneElig = mock(Query.class);
	private Query mockInsEligRules = mock(Query.class);
	private Query mockInsEligPygrp = mock(Query.class);
	private Query mockInsEligBnstat = mock(Query.class);
	private Query mockInsEligEeclas = mock(Query.class);
	private Query mockUpdEligEeclas = mock(Query.class);
	private Query mockUpdEligRule = mock(Query.class);
	private Query mockNativeQuery = mock(Query.class);
	private Query mockUpdClientOptns = mock(Query.class);
	private Query mockInsBenPgm = mock(Query.class);
	private Query mockInsSetupPlan = mock(Query.class);
	private Query mockInsSetupOptn = mock(Query.class);
	private Query mockUpdClOpt2a = mock(Query.class);
	private Query mockSelDfltEligRule = mock(Query.class);
	private Query mockUpdUseCnfig1 = mock(Query.class);
	private Query mockInsEligCnfig1 = mock(Query.class);
	private Query mockUpdFlgCnfig1 = mock(Query.class);
	private Query mockSelEligCnfg = mock(Query.class);
	private Query mockInsElgCfgEfdt = mock(Query.class);
	private Query mockInsEligCnfgTbl = mock(Query.class);
	private Query mockDelPrclBnPgm = mock(Query.class);
	private Query mockInsPrclBnPgm = mock(Query.class);
	private Query mockDelExcessCrInc = mock(Query.class);
	private Query mockDelExcessCrTbl = mock(Query.class);
	private Query mockInsExcessCrInc = mock(Query.class);
	private Query mockInsExcessCrTbl = mock(Query.class);
	private Query mockUpdNonSelected = mock(Query.class);
	private Query mockFixIneligPlans = mock(Query.class);
	private Query mockSelLifePlans = mock(Query.class);
	private Query mockSelDpPlans = mock(Query.class);
	private Query mockUpdStdNonSelected = mock(Query.class);
	private Query mockUpdLtdNonSelected = mock(Query.class);
	private Query mockDelWaiveOptn = mock(Query.class);
	private Query mockUpdAddlWaive = mock(Query.class);
	private Query mockUpdWaive = mock(Query.class);
	private Query mockSelHdhpHsa = mock(Query.class);
	private Query mockSelOptnWaive = mock(Query.class);
	private Query mockInsCloneOptnWaive = mock(Query.class);
	private Query mockUpdEligRuleWaitPer = mock(Query.class);
	private Query mockSelWaitPerEvnt = mock(Query.class);
	private Query mockUpdEventRule = mock(Query.class);
	private Query mockUpdWaiveCvg = mock(Query.class);
	private Query mockDelPlanCommuter = mock(Query.class);
	private Query mockDelOptnCommuter = mock(Query.class);
	private Query mockDelCostCommuter = mock(Query.class);
	private Query mockUpdCost = mock(Query.class);
	private Query mockUpdOptn2 = mock(Query.class);
	private Query mockUpdOpt2a = mock(Query.class);
	private Query mockDelRateData = mock(Query.class);
	private Query mockSelWaiveKeys = mock(Query.class);
	private Query mockMrgWaiverAllow = mock(Query.class);
	private Query mockMrgWaiveRtTbl = mock(Query.class);
	private Query mockMrgWaiveRtData = mock(Query.class);
	private Query mockSelBssRateId = mock(Query.class);
	private Query mockInsBssRateId = mock(Query.class);
	private Query mockInsSuppCost = mock(Query.class);
	private Query mockSelLifeDisbCost = mock(Query.class);
	private Query mockSelLifeDisbRateId = mock(Query.class);
	private Query mockUpdLifeDisbCost = mock(Query.class);
	private Query mockDelFutRateData = mock(Query.class);
	private Query mockDelFutRateTbl = mock(Query.class);
	private Query mockDelFutPrclPgm = mock(Query.class);
	private Query mockDelFutPrclEfdt = mock(Query.class);
	private Query mockSelInactBenProg = mock(Query.class);
	private Query mockMrgInactOptn2 = mock(Query.class);
	private Query mockMrgInactPrclPgm = mock(Query.class);
	private Query mockMrgDefnPgm = mock(Query.class);
	private Query mockMrgInactDefnPlan = mock(Query.class);
	private Query mockMrgInactDefnOptn = mock(Query.class);
	private Query mockDelExactOpt2a = mock(Query.class);
	private Query mockDelMeasurments = mock(Query.class);
	private Query mockInsMeasurments = mock(Query.class);
	private Query mockUpdClientOptnsAleStatus = mock(Query.class);
	private SubmitStatusService mockSubmitStatusService;
	private SubmitService mockSubmitService;
	private RealmDataDao mockRealmDataDao;
	private BenefitPlanService mockBenefitPlanService;
	private RealmPlanYearService mockRealmPlanYearService;

    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;
    private MockedStatic<CommonServiceHelper> commonServiceHelperMockedStatic;
    private MockedStatic<BenefitCategoriesHelper> benefitCategoriesHelperMockedStatic;
    private MockedStatic<RenewalServiceHelper> renewalServiceHelperMockedStatic;

    @Before
    public void setup() {
        ApplicationContextProvider provider = new ApplicationContextProvider();
        provider.setApplicationContext(new AnnotationConfigApplicationContext(SubmitTestConfig.class));
        psEntityManager = mock(EntityManager.class);
        bssEM = mock(EntityManager.class);
        when(bssEM.getTransaction()).thenReturn(mock(EntityTransaction.class));
        when(psEntityManager.getTransaction()).thenReturn(mock(EntityTransaction.class));

        rulesAndConfigsUtilsMockedStatic = org.mockito.Mockito.mockStatic(RulesAndConfigsUtils.class);
        commonServiceHelperMockedStatic = org.mockito.Mockito.mockStatic(CommonServiceHelper.class);
        benefitCategoriesHelperMockedStatic = org.mockito.Mockito.mockStatic(BenefitCategoriesHelper.class);
        renewalServiceHelperMockedStatic = org.mockito.Mockito.mockStatic(RenewalServiceHelper.class);

        mockCn = mock(Connection.class);
        mockStmt = mock(PreparedStatement.class);
        mockSession = mock(Session.class);
        mockedBatchQuery = mock(Query.class);
        mockHibernateQuery = mock(org.hibernate.query.Query.class);

        setupBatchMocksAndStubs();

        BenefitGroup benefitGroup = convertStrategyBGToBenefitGroup(strategyData.getBenefitGroups().get(0));
        benefitGroup.setEligConfig1("A000");
        benefitGroupStrategy.setBenefitGroup(benefitGroup);
        List<BenefitGroupStrategy> bgsList = new ArrayList<>();
        bgsList.add(benefitGroupStrategy);
        when(SubmitTestConfig.getStrategyGroupService().getBenefitGroupStrategy(987654, "A")).thenReturn(bgsList);
        when(SubmitTestConfig.getPsCompanyDao().getCompanyDetailsByEffdt(any(Company.class), any(java.util.Date.class)))
                .thenReturn(company);
        when(SubmitTestConfig.getStrategyGroupDataDao().getMedStrategyPortfolios(any(Long.class)))
                .thenReturn(getMockPortfolios());

        mockQueries();

        psSubmitDataDaoImpl = new PsSubmitDataDaoImpl();
        psSubmitDataDaoImpl.createEntityManager();
        psSubmitDataDaoImpl.setEntityManager(psEntityManager);
        psSubmitDataDaoImpl.setBssEntityManager(bssEM);
	    psSubmitDataDaoImpl.setBandCodesService(bandCodesService);
	    psSubmitDataDaoImpl.setFlexRateService(flexRateService);
        ApplicationContext context = ApplicationContextProvider.getApplicationContext();
        mockSubmitStatusService = (SubmitStatusService) context.getBean("submitStatusService");
        mockSubmitService = (SubmitService) context.getBean("submitService");

        mockRealmDataDao = mock(RealmDataDao.class);
        psSubmitDataDaoImpl.setRealmDataDao(mockRealmDataDao);
        mockBenefitPlanService = mock(BenefitPlanService.class);
        psSubmitDataDaoImpl.setBenefitPlanService(mockBenefitPlanService);
        mockRealmPlanYearService = mock(RealmPlanYearService.class);
        psSubmitDataDaoImpl.setRealmPlanYearService(mockRealmPlanYearService);

        SubmitStatus submitStatus = SubmitStatus.builder().sendEmail(true).status("SUCCESS")
                .confirmationNumber("123456").company("1001").userId("USERID").build();
        SubmitPayload submitPayload = SubmitPayload.builder().payload(strategyDataJson()).build();
        submitStatus.setSubmitPayload(submitPayload);
        when(mockSubmitStatusService.findByConfirmationNumber(anyString(), anyString())).thenReturn(submitStatus);
        when(mockRealmDataDao.isCommuterBenefitOffered(any(Long.class))).thenReturn(true);
    }

    @After
    public void tearDown() {
        rulesAndConfigsUtilsMockedStatic.close();
        commonServiceHelperMockedStatic.close();
        benefitCategoriesHelperMockedStatic.close();
        renewalServiceHelperMockedStatic.close();
    }

	@Test
	public void submitNewClientTest() throws SQLException {

		company.setRenewalCompany(false);
		company.setProspectConvertedOnboardingClient(false);
		company.setRiskType(RiskTypeEnum.BANDS);
		String userId = "USERID";
		boolean emailFlag = true;
		String confirmationId = "123456";
		boolean resubmitFlag = false;
		boolean isMultiClientSubmit = false;
		Map<String, Integer> adminCounts = new HashMap<>();

		ArgumentCaptor<Company> companyArgCaptor = ArgumentCaptor.forClass(Company.class);
		ArgumentCaptor<SubmissionInfo> submissionInfoArgCaptor = ArgumentCaptor.forClass(SubmissionInfo.class);

		doNothing().when(mockSubmitService).postSubmit(companyArgCaptor.capture(), submissionInfoArgCaptor.capture());
		when(RulesAndConfigsUtils.findPickChooseWithExceptions(any(Company.class))).thenReturn(false);

		psSubmitDataDaoImpl.submitData(company, strategyData, userId, emailFlag, confirmationId, resubmitFlag,
				isMultiClientSubmit, adminCounts);

		verify(mockDelEeBpg, times(0)).executeUpdate();
		verify(mockSelBssEe, times(0)).getResultList();
		verify(mockInsEeBpg, times(0)).executeUpdate();
		verify(mockInsBenPlanYear, times(1)).executeUpdate();
		verify(mockSelCurrPlans, times(1)).getResultList();
		verify(mockSelLeavePlans, times(1)).getResultList();
		verify(mockSelCurrSavPlans, times(1)).getResultList();
		verify(mockSelCurrSavOptns, times(1)).getResultList();
		verify(mockSelCurrFSAPlans, times(1)).getResultList();
		verify(mockSelCurrFSAOptns, times(1)).getResultList();
		verify(mockSelMaxOptionCost, times(1)).getResultList();
		verify(mockInsSavedOptn, times(0)).executeUpdate();
		verify(mockInsSavedCost, times(0)).executeUpdate();
		verify(mockInsSavedPlan, times(0)).executeUpdate();
		verify(mockDelLvSvPlan, times(1)).executeUpdate();
		verify(mockDelLvSvOptn, times(1)).executeUpdate();
		verify(mockDelLvSvCost, times(1)).executeUpdate();
		verify(mockDelDefnPlan, times(1)).executeUpdate();
		verify(mockDelDefnOptn, times(1)).executeUpdate();
		verify(mockDelDefnCost, times(1)).executeUpdate();
		verify(mockSelClonePlanTypes, times(3)).getResultList();
		verify(mockInsDefnPlan, times(1)).executeUpdate();
		verify(mockInsDefnOptn, times(1)).executeUpdate();
		verify(mockInsDefnCost, times(1)).executeUpdate();
		verify(mockDelFutDefnPgm, times(1)).executeUpdate();
		verify(mockDelFutDefnPlan, times(1)).executeUpdate();
		verify(mockDelFutDefnOptn, times(1)).executeUpdate();
		verify(mockDelFutDefnCost, times(1)).executeUpdate();
		verify(mockDelElgPygrp, times(1)).executeUpdate();
		verify(mockDelElgBnstat, times(1)).executeUpdate();
		verify(mockDelElgEeclas, times(1)).executeUpdate();
		verify(mockDelElgCnfig1, times(2)).executeUpdate();
		verify(mockDelElgRules, times(1)).executeUpdate();
		verify(mockSelCloneElig, times(1)).getSingleResult();
		verify(mockInsEligRules, times(1)).executeUpdate();
		verify(mockInsEligPygrp, times(1)).executeUpdate();
		verify(mockInsEligBnstat, times(1)).executeUpdate();
		verify(mockInsEligEeclas, times(1)).executeUpdate();
		verify(mockUpdEligEeclas, times(0)).executeUpdate();
		verify(mockUpdEligRule, times(2)).executeUpdate();
		verify(mockNativeQuery, times(1)).executeUpdate();
		verify(mockUpdClientOptns, times(1)).executeUpdate();
		verify(mockInsBenPgm, times(2)).executeUpdate();
		verify(mockInsSetupPlan, times(1)).executeUpdate();
		verify(mockInsSetupOptn, times(1)).executeUpdate();
		verify(mockUpdClOpt2a, times(1)).executeUpdate();
		verify(mockSelDfltEligRule, times(1)).getSingleResult();
		verify(mockUpdUseCnfig1, times(1)).executeUpdate();
		verify(mockInsEligCnfig1, times(0)).executeUpdate();
		verify(mockUpdFlgCnfig1, times(0)).executeUpdate();
		verify(mockSelEligCnfg, times(3)).getResultList();
		verify(mockInsElgCfgEfdt, times(3)).executeUpdate();
		verify(mockInsEligCnfgTbl, times(3)).executeUpdate();
		verify(mockDelPrclBnPgm, times(1)).executeUpdate();
		verify(mockInsPrclBnPgm, times(1)).executeUpdate();
		verify(mockDelExcessCrInc, times(1)).executeUpdate();
		verify(mockDelExcessCrTbl, times(1)).executeUpdate();
		verify(mockInsExcessCrInc, times(12)).executeUpdate();
		verify(mockInsExcessCrTbl, times(1)).executeUpdate();
		verify(mockUpdNonSelected, times(1)).executeUpdate();
		verify(mockFixIneligPlans, times(1)).executeUpdate();
		verify(mockSelLifePlans, times(1)).getResultList();
		verify(mockSelDpPlans, times(2)).getResultList();
		verify(mockUpdStdNonSelected, times(1)).executeUpdate();
		verify(mockUpdLtdNonSelected, times(1)).executeUpdate();
		verify(mockDelWaiveOptn, times(1)).executeUpdate();
		verify(mockUpdAddlWaive, times(1)).executeUpdate();
		verify(mockUpdWaive, times(9)).executeUpdate();
		verify(mockSelHdhpHsa, times(2)).getResultList();
		verify(mockSelOptnWaive, times(9)).getSingleResult();
		verify(mockInsCloneOptnWaive, times(9)).executeUpdate();
		verify(mockUpdEligRuleWaitPer, times(1)).executeUpdate();
		verify(mockSelWaitPerEvnt, times(1)).getResultList();
		verify(mockUpdEventRule, times(11)).executeUpdate();
		verify(mockUpdWaiveCvg, times(1)).executeUpdate();
		verify(mockDelPlanCommuter, times(1)).executeUpdate();
		verify(mockDelOptnCommuter, times(1)).executeUpdate();
		verify(mockDelCostCommuter, times(1)).executeUpdate();
		verify(mockUpdCost, times(3)).executeUpdate();
		verify(mockUpdOptn2, times(1)).executeUpdate();
		verify(mockUpdOpt2a, times(1)).executeUpdate();
		verify(mockDelRateData, times(3)).executeUpdate();
		verify(mockSelWaiveKeys, times(1)).getResultList();
		verify(mockMrgWaiverAllow, times(1)).executeUpdate();
		verify(mockMrgWaiveRtTbl, times(1)).executeUpdate();
		verify(mockMrgWaiveRtData, times(1)).executeUpdate();
		verify(mockSelBssRateId, times(9)).getSingleResult();
		verify(mockInsBssRateId, times(9)).executeUpdate();
		verify(mockInsSuppCost, times(1)).executeUpdate();
		verify(mockSelLifeDisbCost, times(1)).getResultList();
		verify(mockSelLifeDisbRateId, times(15)).getResultList();
		verify(mockUpdLifeDisbCost, times(15)).executeUpdate();
		verify(mockDelFutRateData, times(3)).executeUpdate();
		verify(mockDelFutRateTbl, times(3)).executeUpdate();
		verify(mockDelFutPrclPgm, times(1)).executeUpdate();
		verify(mockDelFutPrclEfdt, times(1)).executeUpdate();
		verify(mockSelInactBenProg, times(1)).getResultList();
		verify(mockMrgInactOptn2, times(2)).executeUpdate();
		verify(mockMrgInactPrclPgm, times(2)).executeUpdate();
		verify(mockMrgDefnPgm, times(2)).executeUpdate();
		verify(mockMrgInactDefnPlan, times(2)).executeUpdate();
		verify(mockMrgInactDefnOptn, times(2)).executeUpdate();
		verify(mockDelExactOpt2a, times(2)).executeUpdate();
		verify(mockStmt, times(163)).addBatch();
		verify(bandCodesService, never()).getBandCodeByType(eq("12345"), any(), eq(BSSApplicationConstants.LIFE), eq(BenExchngEnums.TRINET_III));
		verify(mockSubmitService, times(1)).postSubmit(any(Company.class), any(SubmissionInfo.class));
		assertEquals("1001", submissionInfoArgCaptor.getValue().getCompanyCode());
		assertEquals(resubmitFlag, submissionInfoArgCaptor.getValue().isResubmit());
		assertFalse(submissionInfoArgCaptor.getValue().isDefaultSubmit());
		assertFalse(submissionInfoArgCaptor.getValue().isQueuedSubmit());
		assertFalse(submissionInfoArgCaptor.getValue().isPreSubmit());
		assertTrue(submissionInfoArgCaptor.getValue().getEmailInfo().isSendClientEmail());
		assertFalse(submissionInfoArgCaptor.getValue().getEmailInfo().isResendEmail());
		assertEquals(adminCounts, submissionInfoArgCaptor.getValue().getEmailInfo().getBdmCounts());
		assertEquals(confirmationId, submissionInfoArgCaptor.getValue().getSubmitStatusInfo().getConfirmationNumber());
		assertEquals(strategyDataJson(), submissionInfoArgCaptor.getValue().getSubmitStatusInfo().getPayload());
		assertEquals("SUCCESS", submissionInfoArgCaptor.getValue().getSubmitStatusInfo().getSubmitStatus());
		assertEquals(userId, submissionInfoArgCaptor.getValue().getSubmitStatusInfo().getUserId());
		assertEquals(null, submissionInfoArgCaptor.getValue().getSubmitStatusInfo().getSubmitException());
	}
	
	@Test
	public void submitProspectConvertedOnboardngClient() throws SQLException {

		company.setRenewalCompany(false);
		company.setProspectConvertedOnboardingClient(true);
        company.setEligAle(true);
        company.setPlanStartDate("01-Jan-2026");
		String userId = "USERID";
		boolean emailFlag = true;
		String confirmationId = "123456";
		boolean resubmitFlag = false;
		boolean isMultiClientSubmit = false;
		Map<String, Integer> adminCounts = new HashMap<>();

		ArgumentCaptor<Company> companyArgCaptor = ArgumentCaptor.forClass(Company.class);
		ArgumentCaptor<SubmissionInfo> submissionInfoArgCaptor = ArgumentCaptor.forClass(SubmissionInfo.class);

		doNothing().when(mockSubmitService).postSubmit(companyArgCaptor.capture(), submissionInfoArgCaptor.capture());
		when(RulesAndConfigsUtils.findPickChooseWithExceptions(any(Company.class))).thenReturn(false);

		psSubmitDataDaoImpl.submitData(company, strategyData, userId, emailFlag, confirmationId, resubmitFlag,
				isMultiClientSubmit, adminCounts);

		verify(mockDelEeBpg, times(0)).executeUpdate();
		verify(mockSelBssEe, times(0)).getResultList();
		verify(mockInsEeBpg, times(0)).executeUpdate();
		verify(mockInsBenPlanYear, times(1)).executeUpdate();
		verify(mockSelCurrPlans, times(1)).getResultList();
		verify(mockSelLeavePlans, times(1)).getResultList();
		verify(mockSelCurrSavPlans, times(1)).getResultList();
		verify(mockSelCurrSavOptns, times(1)).getResultList();
		verify(mockSelCurrFSAPlans, times(1)).getResultList();
		verify(mockSelCurrFSAOptns, times(1)).getResultList();
		verify(mockSelMaxOptionCost, times(1)).getResultList();
		verify(mockInsSavedOptn, times(0)).executeUpdate();
		verify(mockInsSavedCost, times(0)).executeUpdate();
		verify(mockInsSavedPlan, times(0)).executeUpdate();
		verify(mockDelLvSvPlan, times(1)).executeUpdate();
		verify(mockDelLvSvOptn, times(1)).executeUpdate();
		verify(mockDelLvSvCost, times(1)).executeUpdate();
		verify(mockDelDefnPlan, times(1)).executeUpdate();
		verify(mockDelDefnOptn, times(1)).executeUpdate();
		verify(mockDelDefnCost, times(1)).executeUpdate();
		verify(mockSelClonePlanTypes, times(3)).getResultList();
		verify(mockInsDefnPlan, times(1)).executeUpdate();
		verify(mockInsDefnOptn, times(1)).executeUpdate();
		verify(mockInsDefnCost, times(1)).executeUpdate();
		verify(mockDelFutDefnPgm, times(1)).executeUpdate();
		verify(mockDelFutDefnPlan, times(1)).executeUpdate();
		verify(mockDelFutDefnOptn, times(1)).executeUpdate();
		verify(mockDelFutDefnCost, times(1)).executeUpdate();
		verify(mockDelElgPygrp, times(1)).executeUpdate();
		verify(mockDelElgBnstat, times(1)).executeUpdate();
		verify(mockDelElgEeclas, times(1)).executeUpdate();
		verify(mockDelElgCnfig1, times(2)).executeUpdate();
		verify(mockDelElgRules, times(1)).executeUpdate();
		verify(mockSelCloneElig, times(1)).getSingleResult();
		verify(mockInsEligRules, times(1)).executeUpdate();
		verify(mockInsEligPygrp, times(1)).executeUpdate();
		verify(mockInsEligBnstat, times(1)).executeUpdate();
		verify(mockInsEligEeclas, times(1)).executeUpdate();
		verify(mockUpdEligEeclas, times(0)).executeUpdate();
		verify(mockUpdEligRule, times(2)).executeUpdate();
		verify(mockNativeQuery, times(1)).executeUpdate();
		verify(mockUpdClientOptns, times(2)).executeUpdate();
		verify(mockInsBenPgm, times(2)).executeUpdate();
		verify(mockInsSetupPlan, times(1)).executeUpdate();
		verify(mockInsSetupOptn, times(1)).executeUpdate();
		verify(mockUpdClOpt2a, times(1)).executeUpdate();
		verify(mockSelDfltEligRule, times(1)).getSingleResult();
		verify(mockUpdUseCnfig1, times(1)).executeUpdate();
		verify(mockInsEligCnfig1, times(0)).executeUpdate();
		verify(mockUpdFlgCnfig1, times(0)).executeUpdate();
		verify(mockSelEligCnfg, times(3)).getResultList();
		verify(mockInsElgCfgEfdt, times(3)).executeUpdate();
		verify(mockInsEligCnfgTbl, times(3)).executeUpdate();
		verify(mockDelPrclBnPgm, times(1)).executeUpdate();
		verify(mockInsPrclBnPgm, times(1)).executeUpdate();
		verify(mockDelExcessCrInc, times(1)).executeUpdate();
		verify(mockDelExcessCrTbl, times(1)).executeUpdate();
		verify(mockInsExcessCrInc, times(12)).executeUpdate();
		verify(mockInsExcessCrTbl, times(1)).executeUpdate();
		verify(mockUpdNonSelected, times(1)).executeUpdate();
		verify(mockFixIneligPlans, times(1)).executeUpdate();
		verify(mockSelLifePlans, times(1)).getResultList();
		verify(mockSelDpPlans, times(2)).getResultList();
		verify(mockUpdStdNonSelected, times(1)).executeUpdate();
		verify(mockUpdLtdNonSelected, times(1)).executeUpdate();
		verify(mockDelWaiveOptn, times(1)).executeUpdate();
		verify(mockUpdAddlWaive, times(1)).executeUpdate();
		verify(mockUpdWaive, times(9)).executeUpdate();
		verify(mockSelHdhpHsa, times(2)).getResultList();
		verify(mockSelOptnWaive, times(9)).getSingleResult();
		verify(mockInsCloneOptnWaive, times(9)).executeUpdate();
		verify(mockUpdEligRuleWaitPer, times(1)).executeUpdate();
		verify(mockSelWaitPerEvnt, times(1)).getResultList();
		verify(mockUpdEventRule, times(11)).executeUpdate();
		verify(mockUpdWaiveCvg, times(1)).executeUpdate();
		verify(mockDelPlanCommuter, times(1)).executeUpdate();
		verify(mockDelOptnCommuter, times(1)).executeUpdate();
		verify(mockDelCostCommuter, times(1)).executeUpdate();
		verify(mockUpdCost, times(3)).executeUpdate();
		verify(mockUpdOptn2, times(1)).executeUpdate();
		verify(mockUpdOpt2a, times(1)).executeUpdate();
		verify(mockDelRateData, times(3)).executeUpdate();
		verify(mockSelWaiveKeys, times(1)).getResultList();
		verify(mockMrgWaiverAllow, times(1)).executeUpdate();
		verify(mockMrgWaiveRtTbl, times(1)).executeUpdate();
		verify(mockMrgWaiveRtData, times(1)).executeUpdate();
		verify(mockSelBssRateId, times(9)).getSingleResult();
		verify(mockInsBssRateId, times(9)).executeUpdate();
		verify(mockInsSuppCost, times(1)).executeUpdate();
		verify(mockSelLifeDisbCost, times(1)).getResultList();
		verify(mockSelLifeDisbRateId, times(15)).getResultList();
		verify(mockUpdLifeDisbCost, times(15)).executeUpdate();
		verify(mockDelFutRateData, times(3)).executeUpdate();
		verify(mockDelFutRateTbl, times(3)).executeUpdate();
		verify(mockDelFutPrclPgm, times(1)).executeUpdate();
		verify(mockDelFutPrclEfdt, times(1)).executeUpdate();
		verify(mockSelInactBenProg, times(1)).getResultList();
		verify(mockMrgInactOptn2, times(2)).executeUpdate();
		verify(mockMrgInactPrclPgm, times(2)).executeUpdate();
		verify(mockMrgDefnPgm, times(2)).executeUpdate();
		verify(mockMrgInactDefnPlan, times(2)).executeUpdate();
		verify(mockMrgInactDefnOptn, times(2)).executeUpdate();
		verify(mockDelExactOpt2a, times(2)).executeUpdate();
		verify(mockUpdClientOptnsAleStatus, times(1)).executeUpdate();
		verify(mockStmt, times(163)).addBatch();

		verify(mockSubmitService, times(1)).postSubmit(any(Company.class), any(SubmissionInfo.class));
		assertEquals("1001", submissionInfoArgCaptor.getValue().getCompanyCode());
		assertEquals(resubmitFlag, submissionInfoArgCaptor.getValue().isResubmit());
		assertFalse(submissionInfoArgCaptor.getValue().isDefaultSubmit());
		assertFalse(submissionInfoArgCaptor.getValue().isQueuedSubmit());
		assertFalse(submissionInfoArgCaptor.getValue().isPreSubmit());
		assertTrue(submissionInfoArgCaptor.getValue().getEmailInfo().isSendClientEmail());
		assertFalse(submissionInfoArgCaptor.getValue().getEmailInfo().isResendEmail());
		assertEquals(adminCounts, submissionInfoArgCaptor.getValue().getEmailInfo().getBdmCounts());
		assertEquals(confirmationId, submissionInfoArgCaptor.getValue().getSubmitStatusInfo().getConfirmationNumber());
		assertEquals(strategyDataJson(), submissionInfoArgCaptor.getValue().getSubmitStatusInfo().getPayload());
		assertEquals("SUCCESS", submissionInfoArgCaptor.getValue().getSubmitStatusInfo().getSubmitStatus());
		assertEquals(userId, submissionInfoArgCaptor.getValue().getSubmitStatusInfo().getUserId());
		assertEquals(null, submissionInfoArgCaptor.getValue().getSubmitStatusInfo().getSubmitException());
	}
	
	@Test
	public void submitNewClientTestWithPickChoose() throws SQLException {

		company.setRenewalCompany(false);
		String userId = "USERID";
		boolean emailFlag = true;
		String confirmationId = "123456";
		boolean resubmitFlag = false;
		boolean isMultiClientSubmit = false;
		Map<String, Integer> adminCounts = new HashMap<>();

		ArgumentCaptor<Company> companyArgCaptor = ArgumentCaptor.forClass(Company.class);
		ArgumentCaptor<SubmissionInfo> submissionInfoArgCaptor = ArgumentCaptor.forClass(SubmissionInfo.class);

		doNothing().when(mockSubmitService).postSubmit(companyArgCaptor.capture(), submissionInfoArgCaptor.capture());
		when(RulesAndConfigsUtils.findPickChooseWithExceptions(any(Company.class))).thenReturn(true);

		// Prepare mock return value
		Map<String, Map<String, List<String>>> mockResult = new HashMap<>();
		Map<String, List<String>> regionMap = new HashMap<>();
		regionMap.put("Region1", Arrays.asList("005VN3"));
		mockResult.put("10", regionMap);
		when(mockRealmDataDao.getAutoSelectedPlansByRegion(anySet(), anyLong())).thenReturn(mockResult);
		when(mockRealmPlanYearService.getPreviousRealmPlanYear(any())).thenReturn(company.getRealmPlanYear());

		psSubmitDataDaoImpl.submitData(company, strategyData, userId, emailFlag, confirmationId, resubmitFlag,
				isMultiClientSubmit, adminCounts);

		verify(mockSubmitService, times(1)).postSubmit(any(Company.class), any(SubmissionInfo.class));
		assertEquals("1001", submissionInfoArgCaptor.getValue().getCompanyCode());
		assertEquals(resubmitFlag, submissionInfoArgCaptor.getValue().isResubmit());
		assertFalse(submissionInfoArgCaptor.getValue().isDefaultSubmit());
		assertFalse(submissionInfoArgCaptor.getValue().isQueuedSubmit());
		assertFalse(submissionInfoArgCaptor.getValue().isPreSubmit());
		assertTrue(submissionInfoArgCaptor.getValue().getEmailInfo().isSendClientEmail());
		assertFalse(submissionInfoArgCaptor.getValue().getEmailInfo().isResendEmail());
		assertEquals(adminCounts, submissionInfoArgCaptor.getValue().getEmailInfo().getBdmCounts());
		assertEquals(confirmationId, submissionInfoArgCaptor.getValue().getSubmitStatusInfo().getConfirmationNumber());
		assertEquals(strategyDataJson(), submissionInfoArgCaptor.getValue().getSubmitStatusInfo().getPayload());
		assertEquals("SUCCESS", submissionInfoArgCaptor.getValue().getSubmitStatusInfo().getSubmitStatus());
		assertEquals(userId, submissionInfoArgCaptor.getValue().getSubmitStatusInfo().getUserId());
		assertEquals(null, submissionInfoArgCaptor.getValue().getSubmitStatusInfo().getSubmitException());
		verify(mockRealmDataDao, times(2)).getAutoSelectedPlansByRegion(any(Set.class), anyLong());
	}


	@Test
	public void submitRenewalTest() throws SQLException {

		company.setRenewalCompany(true);
		company.setPlanStartDate("01-JAN-2021");
		String userId = "USERID";
		boolean emailFlag = true;
		String confirmationId = "";
		boolean resubmitFlag = false;
		boolean isMultiClientSubmit = false;
		Map<String, Integer> adminCounts = new HashMap<>();
		
		when(RulesAndConfigsUtils.findPickChooseWithExceptions(any(Company.class))).thenReturn(false);

		psSubmitDataDaoImpl.submitData(company, strategyData, userId, emailFlag, confirmationId, resubmitFlag,
				isMultiClientSubmit, adminCounts);

		verify(mockDelEeBpg, times(1)).executeUpdate();
		verify(mockSelBssEe, times(1)).getResultList();
		verify(mockInsEeBpg, times(4)).executeUpdate();
		verify(mockInsBenPlanYear, times(1)).executeUpdate();
		verify(mockSelCurrPlans, times(1)).getResultList();
		verify(mockSelLeavePlans, times(1)).getResultList();
		verify(mockSelCurrSavPlans, times(1)).getResultList();
		verify(mockSelCurrSavOptns, times(1)).getResultList();
		verify(mockSelCurrFSAPlans, times(1)).getResultList();
		verify(mockSelCurrFSAOptns, times(1)).getResultList();
		verify(mockSelMaxOptionCost, times(1)).getResultList();
		verify(mockInsSavedOptn, times(0)).executeUpdate();
		verify(mockInsSavedCost, times(0)).executeUpdate();
		verify(mockInsSavedPlan, times(0)).executeUpdate();
		verify(mockDelLvSvPlan, times(1)).executeUpdate();
		verify(mockDelLvSvOptn, times(1)).executeUpdate();
		verify(mockDelLvSvCost, times(1)).executeUpdate();
		verify(mockDelDefnPlan, times(1)).executeUpdate();
		verify(mockDelDefnOptn, times(1)).executeUpdate();
		verify(mockDelDefnCost, times(1)).executeUpdate();
		verify(mockSelClonePlanTypes, times(3)).getResultList();
		verify(mockInsDefnPlan, times(1)).executeUpdate();
		verify(mockInsDefnOptn, times(1)).executeUpdate();
		verify(mockInsDefnCost, times(1)).executeUpdate();
		verify(mockDelFutDefnPgm, times(0)).executeUpdate();
		verify(mockDelFutDefnPlan, times(0)).executeUpdate();
		verify(mockDelFutDefnOptn, times(0)).executeUpdate();
		verify(mockDelFutDefnCost, times(0)).executeUpdate();
		verify(mockDelElgPygrp, times(1)).executeUpdate();
		verify(mockDelElgBnstat, times(1)).executeUpdate();
		verify(mockDelElgEeclas, times(1)).executeUpdate();
		verify(mockDelElgCnfig1, times(2)).executeUpdate();
		verify(mockDelElgRules, times(1)).executeUpdate();
		verify(mockSelCloneElig, times(1)).getSingleResult();
		verify(mockInsEligRules, times(1)).executeUpdate();
		verify(mockInsEligPygrp, times(1)).executeUpdate();
		verify(mockInsEligBnstat, times(1)).executeUpdate();
		verify(mockInsEligEeclas, times(1)).executeUpdate();
		verify(mockUpdEligEeclas, times(0)).executeUpdate();
		verify(mockUpdEligRule, times(1)).executeUpdate();
		verify(mockNativeQuery, times(1)).executeUpdate();
		verify(mockUpdClientOptns, times(0)).executeUpdate();
		verify(mockInsBenPgm, times(1)).executeUpdate();
		verify(mockInsSetupPlan, times(0)).executeUpdate();
		verify(mockInsSetupOptn, times(0)).executeUpdate();
		verify(mockUpdClOpt2a, times(1)).executeUpdate();
		verify(mockSelDfltEligRule, times(1)).getSingleResult();
		verify(mockUpdUseCnfig1, times(1)).executeUpdate();
		verify(mockInsEligCnfig1, times(0)).executeUpdate();
		verify(mockUpdFlgCnfig1, times(0)).executeUpdate();
		verify(mockSelEligCnfg, times(3)).getResultList();
		verify(mockInsElgCfgEfdt, times(3)).executeUpdate();
		verify(mockInsEligCnfgTbl, times(3)).executeUpdate();
		verify(mockDelPrclBnPgm, times(1)).executeUpdate();
		verify(mockInsPrclBnPgm, times(1)).executeUpdate();
		verify(mockDelExcessCrInc, times(1)).executeUpdate();
		verify(mockDelExcessCrTbl, times(1)).executeUpdate();
		verify(mockInsExcessCrInc, times(12)).executeUpdate();
		verify(mockInsExcessCrTbl, times(1)).executeUpdate();
		verify(mockUpdNonSelected, times(1)).executeUpdate();
		verify(mockFixIneligPlans, times(1)).executeUpdate();
		verify(mockSelLifePlans, times(1)).getResultList();
		verify(mockSelDpPlans, times(2)).getResultList();
		verify(mockUpdStdNonSelected, times(1)).executeUpdate();
		verify(mockUpdLtdNonSelected, times(1)).executeUpdate();
		verify(mockDelWaiveOptn, times(1)).executeUpdate();
		verify(mockUpdAddlWaive, times(1)).executeUpdate();
		verify(mockUpdWaive, times(9)).executeUpdate();
		verify(mockSelHdhpHsa, times(2)).getResultList();
		verify(mockSelOptnWaive, times(9)).getSingleResult();
		verify(mockInsCloneOptnWaive, times(9)).executeUpdate();
		verify(mockUpdEligRuleWaitPer, times(1)).executeUpdate();
		verify(mockSelWaitPerEvnt, times(1)).getResultList();
		verify(mockUpdEventRule, times(11)).executeUpdate();
		verify(mockUpdWaiveCvg, times(1)).executeUpdate();
		verify(mockDelPlanCommuter, times(1)).executeUpdate();
		verify(mockDelOptnCommuter, times(1)).executeUpdate();
		verify(mockDelCostCommuter, times(1)).executeUpdate();
		verify(mockUpdCost, times(3)).executeUpdate();
		verify(mockUpdOptn2, times(1)).executeUpdate();
		verify(mockUpdOpt2a, times(1)).executeUpdate();
		verify(mockDelRateData, times(3)).executeUpdate();
		verify(mockSelWaiveKeys, times(1)).getResultList();
		verify(mockMrgWaiverAllow, times(1)).executeUpdate();
		verify(mockMrgWaiveRtTbl, times(1)).executeUpdate();
		verify(mockMrgWaiveRtData, times(1)).executeUpdate();
		verify(mockSelBssRateId, times(9)).getSingleResult();
		verify(mockInsBssRateId, times(9)).executeUpdate();
		verify(mockInsSuppCost, times(1)).executeUpdate();
		verify(mockSelLifeDisbCost, times(1)).getResultList();
		verify(mockSelLifeDisbRateId, times(15)).getResultList();
		verify(mockUpdLifeDisbCost, times(15)).executeUpdate();
		verify(mockDelFutRateData, times(0)).executeUpdate();
		verify(mockDelFutRateTbl, times(0)).executeUpdate();
		verify(mockDelFutPrclPgm, times(0)).executeUpdate();
		verify(mockDelFutPrclEfdt, times(0)).executeUpdate();
		verify(mockSelInactBenProg, times(1)).getResultList();
		verify(mockMrgInactOptn2, times(2)).executeUpdate();
		verify(mockMrgInactPrclPgm, times(2)).executeUpdate();
		verify(mockMrgDefnPgm, times(2)).executeUpdate();
		verify(mockMrgInactDefnPlan, times(2)).executeUpdate();
		verify(mockMrgInactDefnOptn, times(2)).executeUpdate();
		verify(mockDelExactOpt2a, times(2)).executeUpdate();
		verify(mockStmt, times(163)).addBatch();

		verify(mockSubmitService, times(1)).postSubmit(any(Company.class), any(SubmissionInfo.class));
	}

	@Test
	public void submitNewClientTest_shouldCallBandCodesService_whenRiskTypeDifferentials() {
		company.setRenewalCompany(false);
		company.setProspectConvertedOnboardingClient(false);
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		company.setBssNaicsCode(12345);
		String userId = "USERID";
		boolean emailFlag = true;
		String confirmationId = "123456";
		boolean resubmitFlag = false;
		boolean isMultiClientSubmit = false;
		Map<String, Integer> adminCounts = new HashMap<>();

		ArgumentCaptor<Company> companyArgCaptor = ArgumentCaptor.forClass(Company.class);
		ArgumentCaptor<SubmissionInfo> submissionInfoArgCaptor = ArgumentCaptor.forClass(SubmissionInfo.class);

		doNothing().when(mockSubmitService).postSubmit(companyArgCaptor.capture(), submissionInfoArgCaptor.capture());
		when(RulesAndConfigsUtils.findPickChooseWithExceptions(any(Company.class))).thenReturn(false);

		psSubmitDataDaoImpl.submitData(company, strategyData, userId, emailFlag, confirmationId, resubmitFlag,
				isMultiClientSubmit, adminCounts);
		// TODO: when setT2ProvCovrgRate is fully set in PsSubmitDataDaoImpl,
		//       change this verification back to times(5) instead of never().
		verify(bandCodesService, never()).getBandCodeByType(eq("12345"), any(), eq(BSSApplicationConstants.LIFE), eq(BenExchngEnums.TRINET_III));
	}

	@Test
	public void submitNewClientTest_shouldCallBandCodesService_whenProspectConvertedOnboardingClientTrue() {
		company.setRenewalCompany(false);
		company.setProspectConvertedOnboardingClient(true);
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		company.setBssNaicsCode(12345);
		String userId = "USERID";
		boolean emailFlag = true;
		String confirmationId = "123456";
		boolean resubmitFlag = false;
		boolean isMultiClientSubmit = false;
		Map<String, Integer> adminCounts = new HashMap<>();

		ArgumentCaptor<Company> companyArgCaptor = ArgumentCaptor.forClass(Company.class);
		ArgumentCaptor<SubmissionInfo> submissionInfoArgCaptor = ArgumentCaptor.forClass(SubmissionInfo.class);

		doNothing().when(mockSubmitService).postSubmit(companyArgCaptor.capture(), submissionInfoArgCaptor.capture());
		when(RulesAndConfigsUtils.findPickChooseWithExceptions(any(Company.class))).thenReturn(false);

		psSubmitDataDaoImpl.submitData(company, strategyData, userId, emailFlag, confirmationId, resubmitFlag,
				isMultiClientSubmit, adminCounts);
		// TODO: when setT2ProvCovrgRate is fully set in PsSubmitDataDaoImpl,
		//       change this verification back to times(5) instead of never().
		verify(bandCodesService, never()).getBandCodeByType(eq("12345"), any(), eq(BSSApplicationConstants.LIFE), eq(BenExchngEnums.TRINET_III));
	}

	@Test
	public void submitNewClientTest_shouldCallBandCodesService_whenProspectCompanyTrue() {
		company.setRenewalCompany(false);
		company.setProspectConvertedOnboardingClient(false);
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		company.setBssNaicsCode(12345);
		company.setProspectCompany(true);
		String userId = "USERID";
		boolean emailFlag = true;
		String confirmationId = "123456";
		boolean resubmitFlag = false;
		boolean isMultiClientSubmit = false;
		Map<String, Integer> adminCounts = new HashMap<>();

		ArgumentCaptor<Company> companyArgCaptor = ArgumentCaptor.forClass(Company.class);
		ArgumentCaptor<SubmissionInfo> submissionInfoArgCaptor = ArgumentCaptor.forClass(SubmissionInfo.class);

		doNothing().when(mockSubmitService).postSubmit(companyArgCaptor.capture(), submissionInfoArgCaptor.capture());
		when(RulesAndConfigsUtils.findPickChooseWithExceptions(any(Company.class))).thenReturn(false);

		psSubmitDataDaoImpl.submitData(company, strategyData, userId, emailFlag, confirmationId, resubmitFlag,
				isMultiClientSubmit, adminCounts);
		// TODO: when setT2ProvCovrgRate is fully set in PsSubmitDataDaoImpl,
		//       change this verification back to times(5) instead of never().
		verify(bandCodesService, never()).getBandCodeByType(eq("12345"), any(), eq(BSSApplicationConstants.LIFE), eq(BenExchngEnums.TRINET_III));
	}

	private void mockQueries() {
		List<Object[]> emptyResult = new ArrayList<>();
		int resultZero = 0;
		int resultOne = 1;
		int resultTen = 10;
		String cloneEligRule = "ELIG";
		String dfltEligRule = "DFLT";

		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.CHK_T2_BEN_PROG_GROUP)).thenReturn(mockHasBenProgGrp);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.DELETE_PS_T2_EE_BPG)).thenReturn(mockDelEeBpg);
		when(bssEM.createNamedQuery(PsSubmitDataDaoImpl.SELECT_XBSS_EMPLOYEE)).thenReturn(mockSelBssEe);
		when(mockSelBssEe.getResultList()).thenReturn(getBssEeQueryResult());
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.INSERT_PS_T2_EE_BPG)).thenReturn(mockInsEeBpg);
		when(mockInsEeBpg.executeUpdate()).thenReturn(resultOne);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.INSERT_COMPANY_BENEFIT_PLAN_YEAR))
				.thenReturn(mockInsBenPlanYear);

		// SavedPlanOptns queries
		when(psEntityManager.createNamedQuery(SavedPlanOptns.GET_CURRENT_PLANS)).thenReturn(mockSelCurrPlans);
		when(mockSelCurrPlans.getResultList()).thenReturn(emptyResult);
		when(psEntityManager.createNamedQuery(SavedPlanOptns.GET_BEN_DEFN_LEAVE_PLANS)).thenReturn(mockSelLeavePlans);
		when(mockSelCurrPlans.getResultList()).thenReturn(emptyResult);
		when(psEntityManager.createNamedQuery(SavedPlanOptns.GET_CURRENT_SAVINGS_PLANS))
				.thenReturn(mockSelCurrSavPlans);
		when(mockSelCurrPlans.getResultList()).thenReturn(emptyResult);
		when(psEntityManager.createNamedQuery(SavedPlanOptns.GET_CURRENT_SAVINGS_OPTNS))
				.thenReturn(mockSelCurrSavOptns);
		when(mockSelCurrPlans.getResultList()).thenReturn(emptyResult);
		when(psEntityManager.createNamedQuery(SavedPlanOptns.GET_CURRENT_FSA_PLANS)).thenReturn(mockSelCurrFSAPlans);
		when(mockSelCurrPlans.getResultList()).thenReturn(emptyResult);
		when(psEntityManager.createNamedQuery(SavedPlanOptns.GET_CURRENT_FSA_OPTNS)).thenReturn(mockSelCurrFSAOptns);
		when(mockSelCurrPlans.getResultList()).thenReturn(emptyResult);
		when(psEntityManager.createNamedQuery(SavedPlanOptns.GET_MAX_OPTION_COST)).thenReturn(mockSelMaxOptionCost);
		when(mockSelCurrPlans.getResultList()).thenReturn(emptyResult);
		when(psEntityManager.createNamedQuery(SavedPlanOptns.INSERT_SAVED_OPTN)).thenReturn(mockInsSavedOptn);
		when(psEntityManager.createNamedQuery(SavedPlanOptns.INSERT_SAVED_COST)).thenReturn(mockInsSavedCost);
		when(psEntityManager.createNamedQuery(SavedPlanOptns.INSERT_SAVED_PLAN)).thenReturn(mockInsSavedPlan);
		when(psEntityManager.createNamedQuery(SavedPlanOptns.CLEAN_LEAVE_SVNGS_PLAN)).thenReturn(mockDelLvSvPlan);
		when(psEntityManager.createNamedQuery(SavedPlanOptns.CLEAN_LEAVE_SVNGS_OPTN)).thenReturn(mockDelLvSvOptn);
		when(psEntityManager.createNamedQuery(SavedPlanOptns.CLEAN_LEAVE_SVNGS_COST)).thenReturn(mockDelLvSvCost);

		// Benefit program service/dao queries
		when(psEntityManager.createNamedQuery(BenefitProgramDaoImpl.CLEAN_PLAN_ALL)).thenReturn(mockDelDefnPlan);
		when(psEntityManager.createNamedQuery(BenefitProgramDaoImpl.CLEAN_OPTN_ALL)).thenReturn(mockDelDefnOptn);
		when(psEntityManager.createNamedQuery(BenefitProgramDaoImpl.CLEAN_COST_ALL)).thenReturn(mockDelDefnCost);
		when(psEntityManager.createNamedQuery(BenefitProgramDaoImpl.GET_CLONE_PLAN_TYPES))
				.thenReturn(mockSelClonePlanTypes);
		when(mockSelClonePlanTypes.getResultList()).thenReturn(Arrays.asList("10", "11", "14"));
		when(psEntityManager.createNamedQuery(BenefitProgramDaoImpl.INSERT_BEN_DEFN_PLAN)).thenReturn(mockInsDefnPlan);
		when(psEntityManager.createNamedQuery(BenefitProgramDaoImpl.INSERT_BEN_DEFN_OPTN)).thenReturn(mockInsDefnOptn);
		when(psEntityManager.createNamedQuery(BenefitProgramDaoImpl.INSERT_BEN_DEFN_COST)).thenReturn(mockInsDefnCost);
		when(psEntityManager.createNamedQuery(BenefitProgramDaoImpl.DELETE_FUTURE_PGM)).thenReturn(mockDelFutDefnPgm);
		when(mockDelFutDefnPgm.executeUpdate()).thenReturn(resultOne);
		when(psEntityManager.createNamedQuery(BenefitProgramDaoImpl.DELETE_FUTURE_PLAN)).thenReturn(mockDelFutDefnPlan);
		when(mockDelFutDefnPlan.executeUpdate()).thenReturn(resultTen);
		when(psEntityManager.createNamedQuery(BenefitProgramDaoImpl.DELETE_FUTURE_OPTN)).thenReturn(mockDelFutDefnOptn);
		when(mockDelFutDefnOptn.executeUpdate()).thenReturn(resultTen);
		when(psEntityManager.createNamedQuery(BenefitProgramDaoImpl.DELETE_FUTURE_COST)).thenReturn(mockDelFutDefnCost);
		when(mockDelFutDefnCost.executeUpdate()).thenReturn(resultTen);

		// Elig rule queries
		when(psEntityManager.createNamedQuery(BenEligRulesDaoImpl.CLEAN_ELIG_PYGRP)).thenReturn(mockDelElgPygrp);
		when(psEntityManager.createNamedQuery(BenEligRulesDaoImpl.CLEAN_ELIG_BNSTAT)).thenReturn(mockDelElgBnstat);
		when(psEntityManager.createNamedQuery(BenEligRulesDaoImpl.CLEAN_ELIG_EECLAS)).thenReturn(mockDelElgEeclas);
		when(psEntityManager.createNamedQuery(BenEligRulesDaoImpl.CLEAN_ELIG_CNFIG1)).thenReturn(mockDelElgCnfig1);
		when(psEntityManager.createNamedQuery(BenEligRulesDaoImpl.CLEAN_ELIG_RULES)).thenReturn(mockDelElgRules);
		when(psEntityManager.createNamedQuery(BenEligRulesImpl.GET_CLONE_ELIG_RULE)).thenReturn(mockSelCloneElig);
		when(mockSelCloneElig.getSingleResult()).thenReturn(cloneEligRule);
		when(psEntityManager.createNamedQuery(BenEligRulesDaoImpl.INSERT_BAS_ELIG_RULES)).thenReturn(mockInsEligRules);
		when(psEntityManager.createNamedQuery(BenEligRulesDaoImpl.INSERT_BAS_ELIG_PYGRP)).thenReturn(mockInsEligPygrp);
		when(psEntityManager.createNamedQuery(BenEligRulesDaoImpl.INSERT_BAS_ELIG_BNSTAT))
				.thenReturn(mockInsEligBnstat);
		when(psEntityManager.createNamedQuery(BenEligRulesDaoImpl.INSERT_BAS_ELIG_EECLAS))
				.thenReturn(mockInsEligEeclas);
		when(psEntityManager.createNamedQuery(BenEligRulesImpl.UPDATE_PGM_ELIG_RULE)).thenReturn(mockUpdEligRule);
		when(psEntityManager.createNamedQuery(BenEligRulesImpl.UPDATE_CLIENT_OPT2A)).thenReturn(mockUpdClOpt2a);
		when(psEntityManager.createNamedQuery(BenEligRulesDaoImpl.GET_DEFAULT_ELIG_RULE))
				.thenReturn(mockSelDfltEligRule);
		when(psEntityManager.createNamedQuery(BenEligRulesDaoImpl.GET_DEFAULT_ELIG_RULE_EFFDT))
				.thenReturn(mockSelDfltEligRule);
		when(mockSelDfltEligRule.getSingleResult()).thenReturn(dfltEligRule);
		when(psEntityManager.createNamedQuery(BenEligRulesDaoImpl.SET_ELIG_USE_CNFIG1)).thenReturn(mockUpdUseCnfig1);
		when(psEntityManager.createNamedQuery(EligConfigDaoImpl.GET_ELIGCNFG_COMPONENT)).thenReturn(mockSelEligCnfg);
		when(psEntityManager.createNamedQuery(EligConfigDaoImpl.INSERT_ELIGCFG_EFDT)).thenReturn(mockInsElgCfgEfdt);
		when(psEntityManager.createNamedQuery(EligConfigDaoImpl.INSERT_ELIGCNFG_TBL)).thenReturn(mockInsEligCnfgTbl);

		when(psEntityManager.createNativeQuery(any(String.class))).thenReturn(mockNativeQuery);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.UPDATE_CLIENT_OPTIONS))
				.thenReturn(mockUpdClientOptns);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.UPDATE_CLOPT_EFFDT_ALE_STATUS))
		.thenReturn(mockUpdClientOptnsAleStatus);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.INSERT_BEN_DEFN_PGM)).thenReturn(mockInsBenPgm);

		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.INSERT_SETUP_BEN_DEFN_PLAN))
				.thenReturn(mockInsSetupPlan);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.INSERT_SETUP_BEN_DEFN_OPTN))
				.thenReturn(mockInsSetupOptn);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.DELETE_BENEFIT_PROGRAM)).thenReturn(mockDelPrclBnPgm);
		when(mockDelPrclBnPgm.executeUpdate()).thenReturn(resultOne);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.INSERT_PRCL_BN_PGM)).thenReturn(mockInsPrclBnPgm);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.DELETE_PS_EXC_CR_CALC_INC))
				.thenReturn(mockDelExcessCrInc);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.DELETE_PS_EXC_CR_CALC_TBL))
				.thenReturn(mockDelExcessCrTbl);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.INSERT_PS_EXC_CR_CALC_INC))
				.thenReturn(mockInsExcessCrInc);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.INSERT_PS_EXC_CR_CALC_TBL))
				.thenReturn(mockInsExcessCrTbl);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.TURN_OFF_NON_SELECTED_BENEFIT_PLANS))
				.thenReturn(mockUpdNonSelected);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.FIX_ELIG_NOT_OFFERED_PLAN_TYPE))
				.thenReturn(mockFixIneligPlans);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.GET_LIFE_BENEFIT_PLANS)).thenReturn(mockSelLifePlans);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.GET_DP_BENEFIT_PLANS)).thenReturn(mockSelDpPlans);
		when(mockSelDpPlans.getResultList()).thenReturn(getDpBenefitPlans());
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.TURN_OFF_STD_NON_SELECTED_BENEFIT_PLANS))
				.thenReturn(mockUpdStdNonSelected);
		when(mockUpdStdNonSelected.executeUpdate()).thenReturn(resultOne);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.TURN_OFF_LTD_NON_SELECTED_BENEFIT_PLANS))
				.thenReturn(mockUpdLtdNonSelected);
		when(mockUpdLtdNonSelected.executeUpdate()).thenReturn(resultOne);

		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.DELETE_BENEFIT_DEFN_OPTN_OF_TYPE_W))
				.thenReturn(mockDelWaiveOptn);
		when(mockDelWaiveOptn.executeUpdate()).thenReturn(resultOne);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.UPDATE_WAIVE_ROW_FOR_ADDITIONAL_BENEFITS))
				.thenReturn(mockUpdAddlWaive);
		when(mockUpdAddlWaive.executeUpdate()).thenReturn(resultOne);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.UPDATE_WAIVE_ROW)).thenReturn(mockUpdWaive);
		when(mockUpdWaive.executeUpdate()).thenReturn(resultOne);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.GET_HDHP_HSA_PLANS)).thenReturn(mockSelHdhpHsa);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.CHECK_PS_BEN_DEFN_OPTN_RECORD_EXISTS))
				.thenReturn(mockSelOptnWaive);
		when(mockSelOptnWaive.getSingleResult()).thenReturn(BigDecimal.ZERO);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.INSERT_PS_BEN_DEFN_OPTN))
				.thenReturn(mockInsCloneOptnWaive);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.UPDATE_ELIG_RULE_FOR_WAIT_PER))
				.thenReturn(mockUpdEligRuleWaitPer);
		when(mockUpdEligRuleWaitPer.executeUpdate()).thenReturn(resultTen);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.WAIT_PER_EVENT_RULE)).thenReturn(mockSelWaitPerEvnt);
		when(mockSelWaitPerEvnt.getResultList()).thenReturn(getWaitPerEventRule());
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.UPDATE_PLAN_EVENT_RULE)).thenReturn(mockUpdEventRule);
		when(mockUpdEventRule.executeUpdate()).thenReturn(resultOne);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.UPDATE_BENEFIT_DEFN_PLAN))
				.thenReturn(mockUpdWaiveCvg);
		when(mockUpdWaiveCvg.executeUpdate()).thenReturn(resultTen);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.DELETE_BENEFIT_DEFN_PLAN_OF_TYPE_A3))
				.thenReturn(mockDelPlanCommuter);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.DELETE_BENEFIT_DEFN_OPTN_OF_TYPE_A3))
				.thenReturn(mockDelOptnCommuter);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.DELETE_BENEFIT_DEFN_COST_OF_TYPE_A3))
				.thenReturn(mockDelCostCommuter);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.UPDATE_BEN_DEFN_OPTN_COST)).thenReturn(mockUpdCost);
		when(mockUpdCost.executeUpdate()).thenReturn(resultTen);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.UPDATE_OPTN2)).thenReturn(mockUpdOptn2);
		when(mockUpdOptn2.executeUpdate()).thenReturn(resultOne);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.UPDATE_OPT2A)).thenReturn(mockUpdOpt2a);
		when(mockUpdOpt2a.executeUpdate()).thenReturn(resultOne);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.DELETE_PS_BN_RATE_DATA)).thenReturn(mockDelRateData);
		when(mockDelRateData.executeUpdate()).thenReturn(resultTen);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.GET_WAIVE_ROW_KEYS)).thenReturn(mockSelWaiveKeys);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.SET_WAIVE_ALLOW_COST)).thenReturn(mockMrgWaiverAllow);
		when(mockMrgWaiverAllow.executeUpdate()).thenReturn(resultTen);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.SET_WAIVE_ALLOW_RATE_TBL))
				.thenReturn(mockMrgWaiveRtTbl);
		when(mockMrgWaiveRtTbl.executeUpdate()).thenReturn(resultTen);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.SET_WAIVE_ALLOW_RATE_DATA))
				.thenReturn(mockMrgWaiveRtData);
		when(mockMrgWaiveRtData.executeUpdate()).thenReturn(resultTen);
		when(bssEM.createNamedQuery(PsSubmitDataDaoImpl.GET_SPECIFIC_GROUP_RATE)).thenReturn(mockSelBssRateId);
		when(bssEM.createNamedQuery(PsSubmitDataDaoImpl.INSERT_NEW_GROUP_RATE)).thenReturn(mockInsBssRateId);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.UPDATE_BEN_SUPPLEMENT_COST))
				.thenReturn(mockInsSuppCost);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.GET_LIFE_DISB_COST_FOR_UPDATE))
				.thenReturn(mockSelLifeDisbCost);
		when(mockSelLifeDisbCost.getResultList()).thenReturn(getLifeDisbCostResult());
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.GET_LIFE_DISB_RATE_ID))
				.thenReturn(mockSelLifeDisbRateId);
		when(mockSelLifeDisbRateId.getResultList()).thenReturn(Arrays.asList("RATELD"));
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.UPDATE_LIFE_DISB_RATE_ID))
				.thenReturn(mockUpdLifeDisbCost);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.DELETE_FUTURE_RATE_DATA))
				.thenReturn(mockDelFutRateData);
		when(mockDelFutRateData.executeUpdate()).thenReturn(resultTen);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.DELETE_FUTURE_RATE_TBL))
				.thenReturn(mockDelFutRateTbl);
		when(mockDelFutRateTbl.executeUpdate()).thenReturn(resultOne);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.DELETE_FUTURE_PRCL_PGM))
				.thenReturn(mockDelFutPrclPgm);
		when(mockDelFutPrclPgm.executeUpdate()).thenReturn(resultOne);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.DELETE_FUTURE_PRCL_EFDT))
				.thenReturn(mockDelFutPrclEfdt);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.DELETE_PS_T2_MEAS_STAB))
		.thenReturn(mockDelMeasurments);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.INSERT_PS_T2_MEAS_STAB))
		.thenReturn(mockInsMeasurments);
		when(mockDelFutPrclEfdt.executeUpdate()).thenReturn(resultOne);
		when(bssEM.createNamedQuery(PsSubmitDataDaoImpl.GET_INACTIVE_BENEFIT_PROGRAMS)).thenReturn(mockSelInactBenProg);
		when(mockSelInactBenProg.getResultList()).thenReturn(getInactBenProg());
		when(psEntityManager.createNamedQuery(BenProgInactivateDaoImpl.UPDATE_BENEFITGROUP_CLIENT_OPTION2))
				.thenReturn(mockMrgInactOptn2);
		when(psEntityManager.createNamedQuery(BenProgInactivateDaoImpl.INACTIVATE_PRCL_BN_PGM))
				.thenReturn(mockMrgInactPrclPgm);
		when(psEntityManager.createNamedQuery(BenProgInactivateDaoImpl.UPDATE_BENEFITGROUP_BEN_DEFN_PROG))
				.thenReturn(mockMrgDefnPgm);
		when(psEntityManager.createNamedQuery(BenProgInactivateDaoImpl.UPDATE_BENEFITGROUP_BEN_DEFN_PLAN))
				.thenReturn(mockMrgInactDefnPlan);
		when(psEntityManager.createNamedQuery(BenProgInactivateDaoImpl.UPDATE_BENEFITGROUP_BEN_DEFN_OPTN_ELIG_RULE))
				.thenReturn(mockMrgInactDefnOptn);
		when(psEntityManager.createNamedQuery(BenProgInactivateDaoImpl.DELETE_EXACT_OPT2A))
				.thenReturn(mockDelExactOpt2a);

	}

	private void setupBatchMocksAndStubs() {
		lenient().when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.INSERT_BEN_PLAN_RATE_DATA)).thenReturn(mockedBatchQuery);
		lenient().when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.INSERT_BEN_PLAN_RATE_DATA_WITH_PAY_IN_RATE)).thenReturn(mockedBatchQuery);
		lenient().when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.CHANGE_COST_RATE)).thenReturn(mockedBatchQuery);
		lenient().when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.INSERT_BN_RATE_TBL)).thenReturn(mockedBatchQuery);
		lenient().when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.INSERT_SIMPLE_BN_RATE_DATA)).thenReturn(mockedBatchQuery);
		when(mockedBatchQuery.unwrap(org.hibernate.query.Query.class)).thenReturn( mockHibernateQuery );
		when(mockHibernateQuery.getQueryString()).thenReturn( MOCK_SQL );
		when(psEntityManager.unwrap( Session.class )).thenReturn( mockSession );
		try {
			when(mockCn.prepareStatement( MOCK_SQL ) ).thenReturn( mockStmt );
			when(mockStmt.executeBatch() ).thenReturn( new int[] { -2 } );
		} catch( SQLException ex ) {
			// need to catch, but exception should not occur during unit tests
		}

		Answer<Work> answer = new Answer<Work>() {
			public Work answer(InvocationOnMock invocation) throws Throwable {
				Work work = invocation.getArgument(0);
				work.execute( mockCn );
				return work;
			}
		};
		Mockito.doAnswer(answer).when(mockSession).doWork( ArgumentMatchers.any( Work.class ) );

	}

	private List<Object[]> getBssEeQueryResult() {
		List<Object[]> result = new ArrayList<>();
		result.add(makeBssEeQueryRow("TNET", "01112220001", "0", "2021-04-01", "BPG001", "A000", "BPG001"));
		result.add(makeBssEeQueryRow("TNET", "01112220002", "0", "2021-04-01", "BPG001", "A000", "BPG001"));
		result.add(makeBssEeQueryRow("TNET", "01112220003", "0", "2021-04-01", "BPG001", "A000", "BPG001"));
		result.add(makeBssEeQueryRow("TNET", "01112220004", "0", "2021-04-01", "BPG001", "A000", "BPG001"));
		return result;
	}

	private Object[] makeBssEeQueryRow(String code, String emplid, String emplRcd, String effdt, String currBenProg,
			String futureEligConfig1, String futureBenProg) {
		Object[] row = new Object[7];
		row[0] = code;
		row[1] = emplid;
		row[2] = new BigDecimal(emplRcd);
		row[3] = java.sql.Date.valueOf(effdt);
		row[4] = currBenProg;
		row[5] = futureEligConfig1;
		row[6] = futureBenProg;
		return row;
	}

	private List<Object[]> getDpBenefitPlans() {
		List<Object[]> result = new ArrayList<>();

		result.add(makeStringsRow("000NHJ", "000NS9"));
		result.add(makeStringsRow("000NHN", "000NSD"));
		result.add(makeStringsRow("000NHO", "000NSE"));
		result.add(makeStringsRow("000NHP", "000NSF"));
		result.add(makeStringsRow("000NHQ", "000NSG"));
		result.add(makeStringsRow("002LYB", "002LYJ"));
		result.add(makeStringsRow("002LYD", "002LYL"));
		result.add(makeStringsRow("003L9D", "003L49"));
		result.add(makeStringsRow("003L9E", "003L4A"));
		result.add(makeStringsRow("003L9F", "003L4B"));
		result.add(makeStringsRow("003L9G", "003L4C"));
		result.add(makeStringsRow("003LQY", "003LRE"));
		result.add(makeStringsRow("003LQZ", "003LRF"));
		result.add(makeStringsRow("0052WA", "0052WI"));
		result.add(makeStringsRow("0052WB", "0052WJ"));
		result.add(makeStringsRow("005VN3", "005VNE"));
		result.add(makeStringsRow("005VN4", "005VNF"));
		result.add(makeStringsRow("005VN9", "005VNK"));

		return result;
	}

	private List<Object[]> getWaitPerEventRule() {
		List<Object[]> result = new ArrayList<>();

		result.add(makeStringsRow("10", "NONE", "MED", "MED"));
		result.add(makeStringsRow("10", "OTHR", "003H", "003H"));
		result.add(makeStringsRow("11", "NONE", "0023", "0023"));
		result.add(makeStringsRow("11", "OTHR", "003D", "003D"));
		result.add(makeStringsRow("14", "NONE", "003K", "003K"));
		result.add(makeStringsRow("14", "OTHR", "003L", "003L"));
		result.add(makeStringsRow("15", "NONE", "000A", "000A"));
		result.add(makeStringsRow("15", "OTHR", "002X", "002X"));
		result.add(makeStringsRow("16", "NONE", "003O", "003O"));
		result.add(makeStringsRow("16", "OTHR", "003M", "003M"));
		result.add(makeStringsRow("17", "NONE", "003N", "003N"));
		result.add(makeStringsRow("17", "OTHR", "003P", "003P"));
		result.add(makeStringsRow("1D", "NONE", "0023", "0023"));
		result.add(makeStringsRow("1D", "OTHR", "003D", "003D"));
		result.add(makeStringsRow("1E", "NONE", "003O", "003O"));
		result.add(makeStringsRow("1E", "OTHR", "003M", "003M"));
		result.add(makeStringsRow("1U", "NONE", "003N", "003N"));
		result.add(makeStringsRow("1U", "OTHR", "003P", "003P"));
		result.add(makeStringsRow("1V", "NONE", "003K", "003K"));
		result.add(makeStringsRow("1V", "OTHR", "003L", "003L"));
		result.add(makeStringsRow("23", "NONE", "GEN", "GEN"));
		result.add(makeStringsRow("23", "OTHR", "003G", "003G"));

		return result;
	}

	private Object[] makeStringsRow(String... rowData) {
		Object[] row = rowData;
		return row;
	}

	private Set<GroupRate> getGroupRateSet() {
		Set<GroupRate> set = new HashSet<>();
		set.add(makeGroupRate("110001", "RATE01", "10"));
		set.add(makeGroupRate("110001", "RATE02", "15"));
		set.add(makeGroupRate("110001", "RATE03", "OTHER"));
		return set;
	}

	private GroupRate makeGroupRate(String groupId, String rateId, String rateType) {
		GroupRatePK pk = new GroupRatePK();
		pk.setGroupId(Long.valueOf(groupId));
		pk.setRateTblId(rateId);
		GroupRate gr = new GroupRate();
		gr.setId(pk);
		gr.setRateIdType(rateType);
		return gr;
	}

	private List<String> getMockPortfolios() {
		List<String> list = Arrays.asList("12", "18", "19", "30");
		return list;
	}

	private BenefitGroup convertStrategyBGToBenefitGroup(StrategyBenefitGroup sbg) {
		BenefitGroup group = new BenefitGroup();
		group.setId(sbg.getId());
		group.setName(sbg.getName());
		group.setType(sbg.getType());
		group.setDefaultGroup(sbg.isDefaultGroup());
		group.setWaitingPeriod(sbg.getWaitingPeriod());
		group.setStatus(sbg.getStatus());
		group.setBenefitProgram(sbg.getBenefitProgram());
		group.setCompanyId(sbg.getCompanyId());
		group.setStrategyId(sbg.getStrategyId());
		group.setPercentChange(sbg.getPercentChange());
		group.setEstimatedTotalCost(sbg.getEstimatedTotalCost());
		group.setHeadcount(sbg.getHeadcount());
		group.setState(sbg.getState());
		group.setBenefitOffers(sbg.getBenefitOffers());
		group.setCoverageLevelHeadCounts(sbg.getCoverageLevelHeadCounts());
		group.setGroupRate(getGroupRateSet());

		group.setRateTblId(null);
		group.setEligRuleId(null);
		group.setEligConfig1(null);
		group.setBenefitGroupStrategy(null);
		group.setSystemCreated(true);
		return group;
	}

	private List<Object[]> getLifeDisbCostResult() {
		List<Object[]> result = new ArrayList<>();
		result.add(makeLifeDisbCostRow("BPG001", "2021-01-01", "23", "8704", "8685", "000OP1"));
		result.add(makeLifeDisbCostRow("BPG001", "2021-01-01", "23", "8705", "8686", "000OP2"));
		result.add(makeLifeDisbCostRow("BPG001", "2021-01-01", "23", "8706", "8687", "000OP3"));
		result.add(makeLifeDisbCostRow("BPG001", "2021-01-01", "23", "8707", "8688", "000OP4"));
		result.add(makeLifeDisbCostRow("BPG001", "2021-01-01", "23", "8708", "8689", "000OP5"));
		result.add(makeLifeDisbCostRow("BPG001", "2021-01-01", "30", "8753", "8731", "001JE9"));
		result.add(makeLifeDisbCostRow("BPG001", "2021-01-01", "30", "8754", "8732", "001JEA"));
		result.add(makeLifeDisbCostRow("BPG001", "2021-01-01", "30", "8755", "8733", "001JEB"));
		result.add(makeLifeDisbCostRow("BPG001", "2021-01-01", "30", "8756", "8734", "001JEC"));
		result.add(makeLifeDisbCostRow("BPG001", "2021-01-01", "30", "8757", "8735", "001JED"));
		result.add(makeLifeDisbCostRow("BPG001", "2021-01-01", "31", "8760", "8738", "000OQ0"));
		result.add(makeLifeDisbCostRow("BPG001", "2021-01-01", "31", "8761", "8739", "000OQ1"));
		result.add(makeLifeDisbCostRow("BPG001", "2021-01-01", "31", "8762", "8740", "000OQ2"));
		result.add(makeLifeDisbCostRow("BPG001", "2021-01-01", "31", "8763", "8741", "000OQ3"));
		result.add(makeLifeDisbCostRow("BPG001", "2021-01-01", "31", "8764", "8742", "000OQ4"));
		return result;
	}

	private Object[] makeLifeDisbCostRow(String benefitProgram, String effdt, String planType, String optionId,
			String costId, String benefitPlan) {
		Object[] row = new Object[6];
		row[0] = benefitProgram;
		row[1] = java.sql.Date.valueOf(effdt);
		row[2] = planType;
		row[3] = new BigDecimal(optionId);
		row[4] = new BigDecimal(costId);
		row[5] = benefitPlan;
		return row;
	}

	private List<Object[]> getInactBenProg() {
		List<Object[]> result = new ArrayList<>();
		result.add(makeStringsRow("JUNK01", "A001"));
		result.add(makeStringsRow("JUNK02", "A002"));
		return result;
	}

	private String companyJson() {
		String json = "{\"id\":1001,\"headcount\":30,\"currentYearTotalCost\":null,\"totalBenefitGroups\":1,\"percentChange\":null,\"totalEmployees\":30,\"description\":\"UnitTesting Inc.\",\"name\":\"UnitTesting Inc.\",\"code\":\"TNET\",\"headQuatersState\":\"CA\",\"updateTime\":null,\"realmPlanYearId\":0,\"benefitProgram\":\"BPG001\",\"realm\":{\"id\":1,\"description\":\"EarthlyRealm\",\"realmType\":\"X\",\"peoid\":\"PAS\",\"verticalCode\":null,\"benExchange\":\"TriNet III\"},\"aleAmount\":null,\"aleAmountHistory\":null,\"headQuatersCity\":null,\"newCompany\":false,\"realmPlanYear\":{\"id\":10,\"realmId\":1,\"oeQuarter\":\"Q1\",\"minFunding\":75,\"mbgNew\":true,\"mbgRenewal\":true,\"aleAmount\":99.99,\"avgSalary\":180000,\"acaFplOpt\":0,\"micrositeUrl\":\"https://trinet.com/microsite\",\"planYearStart\":\"2020-01-01\",\"planYearEnd\":\"2020-12-31\",\"k1Flag\":true,\"cloneProgram\":101},\"actualHeadCount\":0,\"quater\":\"Q4\",\"planEndDate\":null,\"planStartDate\":\"10-OCT-2000\",\"companySetupDate\":\"09-SEP-2000\",\"strategiesHistoryAvailable\":false,\"liveDate\":\"01-OCT-2000\",\"industry\":null,\"pfClient\":9998,\"bandCodes\":{\"kaiserBandCode\":null,\"uhcBandCode\":null,\"aetnaBandCode\":null,\"aetnaHmoBandCode\":null,\"aetnaPpoBandCode\":null,\"bcbsBandCode\":null,\"bcbsNcBandCode\":null,\"kaisCoBandCode\":null,\"bsOfCaBandCode\":null,\"tuftsBandCode\":null,\"lifeBandCode\":\"1\",\"disBandCode\":\"2\",\"empireNYBand\":null,\"bcOfIdBandCode\":null,\"bcbsMNBandCode\":null,\"kaiHawaiiBandCode\":null,\"kaiMidAtlBandCode\":null,\"kaisNwBandCode\":null},\"mbg\":false,\"schedTbl\":null,\"defaultFundingType\":null,\"activeServiceOrder\":false,\"regionalMinimumFundings\":null,\"exclusiveMedPlan\":null,\"defaultMinFundingPct\":0,\"minFundings\":null,\"zipCode\":null,\"emplId\":null,\"sdiStates\":null,\"companyRegions\":null,\"employeeRegions\":null,\"regionsUpdated\":false,\"renewalCompany\":true,\"payrollProcessed\":false,\"transitionPeriod\":false,\"bandCodeUpdated\":false,\"bmguser\":false,\"csauser\":false,\"benCorpAdUser\":false,\"renewalOpen\":false,\"k1Company\":false,\"texasSitus\":false,\"eligAle\":false,\"naicsCode\":0,\"tmtuser\":false}";
		return json;
	}

	private String strategyDataJson() {
		String json = "{\"id\":987654,\"name\":\"Strategy for JUnit Test\",\"type\":\"customized\",\"submitted\":true,\"submitDate\":\"2020-10-21T19:36:59.123-0500\",\"effectiveDate\":\"2021-01-01\",\"endDate\":null,\"comments\":null,\"estimatedTotalCost\":0,\"currentYearTotalCost\":0,\"percentChange\":null,\"totalEmployees\":0,\"headcount\":30,\"totalBudget\":0,\"budgetFactor\":1,\"companyId\":\"TNET\",\"acaFplOpted\":true,\"pkgType\":null,\"costShareType\":\"DFLT\",\"submitStatus\":null,\"canDelete\":false,\"benefitGroups\":[{\"id\":110001,\"name\":\"Staff\",\"type\":\"STD\",\"waitingPeriod\":\"NONE\",\"waitPeriodDescr\":\"Date of hire (DOH)\",\"status\":\"A\",\"benefitProgram\":\"BPG001\",\"companyId\":1001,\"strategyId\":987654,\"strategyGroupId\":2001,\"percentChange\":null,\"estimatedTotalCost\":null,\"headcount\":10,\"benefitOffers\":[{\"type\":\"vision\",\"groupId\":110001,\"description\":\"1V\",\"headcount\":0,\"waiverHeadcount\":0,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":{\"15\":2.09},\"baseFundingRequired\":true,\"planCarriers\":[{\"id\":15,\"name\":\"EyeMed\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"0052V5\",\"parentId\":null,\"regionalCarriers\":[]},{\"id\":6,\"name\":\"VSP\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"002LXU\",\"parentId\":null,\"regionalCarriers\":[]}],\"planPackage\":{\"id\":0,\"templateId\":0,\"fundingModelId\":0,\"name\":null,\"customized\":false,\"employeePaid\":true,\"fundingBasePlan\":null,\"waiverAllowance\":null,\"strategyId\":null,\"fundingType\":null,\"bsuppExcessOption\":null,\"companyId\":null,\"fundingBasePlans\":[],\"planCarrierIds\":[],\"benefitPlans\":[],\"coverageLevelFunding\":{},\"coverageLevelFundingFlatMax\":{},\"headCountPlans\":[],\"bsuppSelectedVolPlanTypes\":[]},\"benefitPlans\":[{\"id\":\"002LYD\",\"planCarrierId\":6,\"name\":\"VSP Vision Plus Optional\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"002LXW\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020924,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"002LYD\",\"headcount\":4,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":10.2,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020925,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"002LYD\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":20.42,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020927,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"002LYD\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":34.91,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020926,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"002LYD\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":21.83,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"0052WA\",\"planCarrierId\":15,\"name\":\"Aetna EyeMed Plus Opt VA\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"0052VB\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020928,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"0052WA\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":12.59,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020929,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"0052WA\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":24,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020931,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"0052WA\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":36.97,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020930,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"0052WA\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":25.15,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"002LYB\",\"planCarrierId\":6,\"name\":\"VSP Vision Optional\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"002LXU\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020932,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"002LYB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":6.62,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020933,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"002LYB\",\"headcount\":1,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":13.23,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020935,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"002LYB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":22.63,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020934,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"002LYB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":14.16,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"0052WB\",\"planCarrierId\":15,\"name\":\"Aetna EyeMed Plus Opt NY\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"0052VC\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020940,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"0052WB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":12.59,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020941,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"0052WB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":24,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020943,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"0052WB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":36.97,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020942,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"0052WB\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":25.15,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]}],\"additionalBenefitOffers\":[]},{\"type\":\"medical\",\"groupId\":110001,\"description\":\"10\",\"headcount\":0,\"waiverHeadcount\":23,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":{\"1\":213.5,\"5\":2636.5,\"12\":165.5},\"baseFundingRequired\":true,\"planCarriers\":[{\"id\":30,\"name\":\"Tufts MA\",\"mandatory\":false,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]},{\"id\":19,\"name\":\"UHC HI\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]},{\"id\":18,\"name\":\"Kaiser HI\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]},{\"id\":12,\"name\":\"Florida Blue\",\"mandatory\":false,\"restricted\":true,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}],\"planPackage\":{\"id\":0,\"templateId\":0,\"fundingModelId\":989691,\"name\":null,\"customized\":true,\"employeePaid\":false,\"fundingBasePlan\":null,\"waiverAllowance\":100,\"strategyId\":987654,\"fundingType\":\"BSUPP\",\"bsuppExcessOption\":2,\"companyId\":null,\"fundingBasePlans\":[],\"planCarrierIds\":[],\"benefitPlans\":[],\"coverageLevelFunding\":{\"employeePlusChild\":200,\"employeePlusSpouse\":200,\"employee\":200,\"employeePlusFamily\":200},\"coverageLevelFundingFlatMax\":{},\"headCountPlans\":[],\"bsuppSelectedVolPlanTypes\":[\"1D\",\"1V\",\"21\",\"25\",\"27\",\"2Y\",\"30\",\"31\"]},\"benefitPlans\":[{\"id\":\"003L9E\",\"planCarrierId\":12,\"name\":\"FL Blue HDHP 3000 FL North\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":true,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-N\"],\"contributions\":[{\"id\":1452020984,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003L9E\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":430,\"employerPercent\":46.51162791,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020985,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003L9E\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":993,\"employerPercent\":20.14098691,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020987,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003L9E\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1302,\"employerPercent\":15.36098311,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020986,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003L9E\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":877,\"employerPercent\":22.80501711,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"000NHN\",\"planCarrierId\":12,\"name\":\"FL Blue PPO 1500 FL Central\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-C\"],\"contributions\":[{\"id\":1452020980,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"000NHN\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":600,\"employerPercent\":33.33333334,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020981,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"000NHN\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1386,\"employerPercent\":14.43001444,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020983,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"000NHN\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1817,\"employerPercent\":11.00715466,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020982,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"000NHN\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1224,\"employerPercent\":16.33986929,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"000NHO\",\"planCarrierId\":12,\"name\":\"FL Blue PPO 1500 FL North\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-N\"],\"contributions\":[{\"id\":1452020988,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"000NHO\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":612,\"employerPercent\":32.67973857,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020989,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"000NHO\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1415,\"employerPercent\":14.13427562,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020991,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"000NHO\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1856,\"employerPercent\":10.77586207,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020990,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"000NHO\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1249,\"employerPercent\":16.01281025,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"003L9D\",\"planCarrierId\":12,\"name\":\"FL Blue HDHP 3000 FL Central\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":true,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-C\"],\"contributions\":[{\"id\":1452020992,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003L9D\",\"headcount\":1,\"hsaHeadcount\":1,\"mirrorHeadCount\":0,\"planCost\":422,\"employerPercent\":47.39336493,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020993,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003L9D\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":975,\"employerPercent\":20.51282052,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020995,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003L9D\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1279,\"employerPercent\":15.63721658,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020994,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003L9D\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":861,\"employerPercent\":23.22880372,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"003L9G\",\"planCarrierId\":12,\"name\":\"FL Blue HDHP 3000 NTL\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":true,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452021000,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003L9G\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":474,\"employerPercent\":42.19409283,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021001,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003L9G\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1095,\"employerPercent\":18.26484019,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021003,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003L9G\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1435,\"employerPercent\":13.93728223,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021002,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003L9G\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":967,\"employerPercent\":20.68252327,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"000NHP\",\"planCarrierId\":12,\"name\":\"FL Blue PPO 1500 FL South\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-S\"],\"contributions\":[{\"id\":1452020996,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"000NHP\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":648,\"employerPercent\":30.86419754,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020997,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"000NHP\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1496,\"employerPercent\":13.36898396,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020999,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"000NHP\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1962,\"employerPercent\":10.19367992,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020998,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"000NHP\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1322,\"employerPercent\":15.12859305,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"000NHQ\",\"planCarrierId\":12,\"name\":\"FL Blue PPO 1500 NTL\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452021004,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"000NHQ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":675,\"employerPercent\":29.62962963,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021005,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"000NHQ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1560,\"employerPercent\":12.82051283,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021007,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"000NHQ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":2046,\"employerPercent\":9.77517107,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021006,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"000NHQ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1377,\"employerPercent\":14.52432825,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"003L9F\",\"planCarrierId\":12,\"name\":\"FL Blue HDHP 3000 FL South\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":true,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-S\"],\"contributions\":[{\"id\":1452021008,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003L9F\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":456,\"employerPercent\":43.85964913,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021009,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003L9F\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1053,\"employerPercent\":18.99335233,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021011,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003L9F\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1382,\"employerPercent\":14.47178003,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021010,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003L9F\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":930,\"employerPercent\":21.50537635,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"000NHJ\",\"planCarrierId\":12,\"name\":\"FL Blue PPO 1000 FL Central\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":false,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":null,\"ppoPlan\":true,\"widelyAvailablePlan\":true,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"FL-C\"],\"contributions\":[{\"id\":1452021012,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"000NHJ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":669,\"employerPercent\":29.89536622,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021013,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"000NHJ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1546,\"employerPercent\":12.93661061,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021015,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"000NHJ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":2028,\"employerPercent\":9.86193294,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021014,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"000NHJ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":1365,\"employerPercent\":14.65201466,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]}],\"additionalBenefitOffers\":[]},{\"type\":\"dental\",\"groupId\":110001,\"description\":\"1D\",\"headcount\":0,\"waiverHeadcount\":0,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":{\"1\":12.09},\"baseFundingRequired\":true,\"planCarriers\":[{\"id\":16,\"name\":\"Delta\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"002LW0\",\"parentId\":null,\"regionalCarriers\":[]},{\"id\":14,\"name\":\"Guardian\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"002LW5\",\"parentId\":null,\"regionalCarriers\":[]},{\"id\":3,\"name\":\"Metlife\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"002LW7\",\"parentId\":null,\"regionalCarriers\":[]},{\"id\":1,\"name\":\"Aetna\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":\"005387\",\"parentId\":null,\"regionalCarriers\":[]}],\"planPackage\":{\"id\":0,\"templateId\":0,\"fundingModelId\":0,\"name\":null,\"customized\":false,\"employeePaid\":true,\"fundingBasePlan\":null,\"waiverAllowance\":null,\"strategyId\":null,\"fundingType\":null,\"bsuppExcessOption\":null,\"companyId\":null,\"fundingBasePlans\":[],\"planCarrierIds\":[],\"benefitPlans\":[],\"coverageLevelFunding\":{},\"coverageLevelFundingFlatMax\":{},\"headCountPlans\":[],\"bsuppSelectedVolPlanTypes\":[]},\"benefitPlans\":[{\"id\":\"005VN3\",\"planCarrierId\":1,\"name\":\"Aetna Dental 0 Opt NV\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"005VMI\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452021160,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"005VN3\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":65.46,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021161,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"005VN3\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":134.26,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021163,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"005VN3\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":209.54,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021162,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"005VN3\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":140.79,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"005VN4\",\"planCarrierId\":1,\"name\":\"Aetna Dental 0 Opt NY\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"005VMJ\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452021140,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"005VN4\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":69.98,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021141,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"005VN4\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":143.55,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021143,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"005VN4\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":224.02,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021142,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"005VN4\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":150.52,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"003LQY\",\"planCarrierId\":16,\"name\":\"Delta Dental 50 Opt NV\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"003LQW\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020828,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003LQY\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":53.36,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020829,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003LQY\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":109.66,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020831,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003LQY\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":170.95,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020830,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003LQY\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":114.86,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"003LQZ\",\"planCarrierId\":16,\"name\":\"Delta Dental 50 Opt VA\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"003LQX\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452020832,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"003LQZ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":53.36,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020833,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"003LQZ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":109.66,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020835,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"003LQZ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":170.95,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452020834,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"003LQZ\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":114.86,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]},{\"id\":\"005VN9\",\"planCarrierId\":16,\"name\":\"Delta Dental 0 Opt VA\",\"estimatedTotalCost\":null,\"annualCap\":null,\"employeePaid\":true,\"highDeductible\":false,\"premium\":false,\"mandatory\":false,\"restrictedState\":false,\"nationalPlan\":false,\"optionalPlans\":\"005VMO\",\"ppoPlan\":false,\"widelyAvailablePlan\":false,\"mandatoryExcluded\":false,\"planCategory\":\"DFLT\",\"strategyId\":null,\"planSelectionId\":0,\"crossRefPlans\":[],\"offeredStates\":[\"All\"],\"contributions\":[{\"id\":1452021184,\"planSelectionId\":0,\"type\":\"employee\",\"benefitPlanId\":\"005VN9\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":69.39,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021185,\"planSelectionId\":0,\"type\":\"employeePlusSpouse\",\"benefitPlanId\":\"005VN9\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":142.61,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021187,\"planSelectionId\":0,\"type\":\"employeePlusFamily\",\"benefitPlanId\":\"005VN9\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":222.31,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"},{\"id\":1452021186,\"planSelectionId\":0,\"type\":\"employeePlusChild\",\"benefitPlanId\":\"005VN9\",\"headcount\":0,\"hsaHeadcount\":0,\"mirrorHeadCount\":0,\"planCost\":149.38,\"employerPercent\":0,\"employeePercent\":null,\"employerContribution\":null,\"employeeContribution\":null,\"overrideType\":\"BASE\"}]}],\"additionalBenefitOffers\":[]},{\"type\":\"additionalBenefit\",\"groupId\":110001,\"description\":null,\"headcount\":30,\"waiverHeadcount\":0,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":null,\"baseFundingRequired\":false,\"planCarriers\":null,\"planPackage\":null,\"benefitPlans\":[],\"additionalBenefitOffers\":[{\"type\":\"LIFE\",\"groupId\":110001,\"description\":\"Life and AD and D\",\"headcount\":30,\"waiverHeadcount\":0,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":null,\"baseFundingRequired\":false,\"additionalBenefitPlans\":[{\"id\":\"000N5P\",\"description\":\"3X Earnings Basic Life & AD&D\",\"region\":null,\"planCost\":14.87,\"annualCap\":1000000,\"planType\":\"23\",\"standAlone\":false,\"offeredGroupType\":null}]},{\"type\":\"DISABILITY\",\"groupId\":110001,\"description\":\"Short & Long Term Disability Plan Options\",\"headcount\":0,\"waiverHeadcount\":0,\"estimatedTotalCost\":null,\"currentYearTotalCost\":null,\"bsuppExcessAmount\":null,\"minFunding\":null,\"baseFundingRequired\":false,\"additionalBenefitPlans\":[{\"id\":\"560\",\"description\":\"STD & LTD Employee Paid\",\"region\":\"FL\",\"planCost\":0,\"monthlyTotalCost\":0,\"annualCap\":null,\"planType\":null,\"standAlone\":false,\"optionPlans\":[{\"id\":\"000N6E\",\"planType\":\"30\",\"planDesc\":\"50% STD Employee Paid\",\"planShortDesc\":\"STD SDI6\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6F\",\"planType\":\"30\",\"planDesc\":\"50% STD Employee Paid\",\"planShortDesc\":\"STD Opt6\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6G\",\"planType\":\"30\",\"planDesc\":\"60% STD Employee Paid\",\"planShortDesc\":\"STD SDI7\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6J\",\"planType\":\"30\",\"planDesc\":\"60% STD Employee Paid\",\"planShortDesc\":\"STD Opt7\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6K\",\"planType\":\"30\",\"planDesc\":\"50% STD Employee Paid\",\"planShortDesc\":\"STD SDI8\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6L\",\"planType\":\"30\",\"planDesc\":\"50% STD Employee Paid\",\"planShortDesc\":\"STD Opt8\",\"planCost\":0,\"planHeadCount\":0,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N6Z\",\"planType\":\"31\",\"planDesc\":\"50% LTD Employee Paid\",\"planShortDesc\":\"LTD Opt6\",\"planCost\":0,\"planHeadCount\":30,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N70\",\"planType\":\"31\",\"planDesc\":\"60% LTD Employee Paid\",\"planShortDesc\":\"LTD Opt7\",\"planCost\":0,\"planHeadCount\":30,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false},{\"id\":\"000N71\",\"planType\":\"31\",\"planDesc\":\"60% LTD Employee Paid\",\"planShortDesc\":\"LTD Opt9\",\"planCost\":0,\"planHeadCount\":30,\"offeredGroupType\":\"STD\",\"taxFree\":false,\"primaryPlan\":false,\"employeePaid\":true,\"sdiPlan\":false}],\"offeredGroupType\":\"STD\"}]}]}],\"coverageLevelHeadCounts\":null,\"isDefault\":true,\"region\":\"All\"}]}";
		return json;
	}

	private String benefitGroupStrategyJson() {
		String json = "{\"id\":2001,\"headcount\":10,\"groupId\":110001,\"strategyId\":987654,\"waitingPeriod\":\"NONE\",\"defaultGroup\":true,\"status\":\"A\",\"benefitGroup\":null,\"strategy\":null}";
		return json;
	}
}
