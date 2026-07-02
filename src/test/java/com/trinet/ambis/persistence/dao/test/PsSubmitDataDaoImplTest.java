package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import com.trinet.ambis.enums.RiskTypeEnum;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.helper.SubmitServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.SchedMidYearFundingDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.ps.BenefitPlanDataDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.dao.ps.impl.PsSubmitDataDaoImpl;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.GroupRate;
import com.trinet.ambis.persistence.model.GroupRatePK;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.model.AdditionalBenefitOffer;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.BenefitOfferSummary;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategySummary;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.ApplicationContextProvider;
import com.trinet.ambis.util.Constants;

@RunWith(MockitoJUnitRunner.class)
public class PsSubmitDataDaoImplTest extends ServiceUnitTest {

	@InjectMocks
	PsSubmitDataDaoImpl psSubmitDataDaoImpl;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	SchedMidYearFundingDao schedMidYearFundingDao;

	@Mock
	CompanyService companyService;

	@Mock
	Connection mockCn;

	@Mock
	PreparedStatement mockStmt;

	@Mock
	Session mockSession;

	@Mock
	PsDao psDao;

	@Mock
	EntityManager bssEm;

	@Mock
	EntityManager psEntityManager;
	
	@Mock
	EntityManagerFactory efBss;
	
	@Mock
	EntityManagerFactory efps;
	
	@Mock
	StrategyService strategyService;
	
	@Mock
	EmailGenService emailGenService;
	
	@Mock
	PsCompanyDao psCompanyDao;
	
	@Mock
	SubmitStatusService submitStatusService;
	
	@Mock
	StrategyDataDao strategyDataDao;
	
	@Mock
	BenefitPlanDataDao benefitPlanDataDao;
	
	@Mock
	BenefitGroupService benefitGroupService;
	
	@Mock
	StrategyGroupService benefitGroupStrategyService;
	
	@Mock
	DataSource dataSource;
	
	@Mock
	private HttpServletRequest request;

	private static final String MOCK_SQL = "MOCK-QUERY";

	Company company = null;
	StrategySummary strategySummary = null;
	BenefitGroup bg = null;
	Query mockedQuery1 = null;
	Query mockedQuery2 = null;
	Query mockedQuery3 = null;
	Query mockedQueryBss = null;
	Query mockedBatchQuery = null;
	org.hibernate.query.Query mockHibernateQuery;
    MockedStatic<SubmitServiceHelper> mockStaticSubmitServiceHelper = null;
    MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils =null;
    MockedStatic<ApplicationContextProvider> mockStaticApplicationContextProvider = null;

    @Before
	public void setup() {
        mockStaticSubmitServiceHelper = Mockito.mockStatic(SubmitServiceHelper.class);
        mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
        mockStaticApplicationContextProvider = Mockito.mockStatic(ApplicationContextProvider.class);

        company = new Company();
		company.setCode("TEST");
		company.setEligAle(true);
		company.setRenewalCompany(true);
		company.setPlanStartDate("01-JAN-2019");
		company.setPfClient("TESTPF");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		Realm realm = new Realm();
		realm.setPeoid("AMB");
		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		company.setRealm(realm);
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(10);
		company.setRealmPlanYear(rpy);
		strategySummary = new StrategySummary();
		strategySummary.setName("teststrategy");
		bg = prepareBenefitGroup();
		mockedQuery1 = mock(Query.class);
		mockedQuery2 = mock(Query.class);
		mockedQuery3 = mock(Query.class);
		mockedQueryBss = mock(Query.class);
		mockedBatchQuery = mock(Query.class);
		mockHibernateQuery = mock(org.hibernate.query.Query.class);
		when(psEntityManager.createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery1);
		when(psEntityManager.getTransaction()).thenReturn(mock(EntityTransaction.class));
	}

    @After
    public void tearDown() {
        if (mockStaticSubmitServiceHelper != null) {
            mockStaticSubmitServiceHelper.close();
        }
        if (mockStaticAppRulesAndConfigsUtils != null) {
            mockStaticAppRulesAndConfigsUtils.close();
        }
        if (mockStaticApplicationContextProvider != null) {
            mockStaticApplicationContextProvider.close();
        }
    }

	@Test
	public void createEntityManager() {
		ApplicationContext context = mock(ApplicationContext.class);

        mockStaticApplicationContextProvider.when(ApplicationContextProvider::getApplicationContext).thenReturn(context);
		when(context.getBean("bisSysadmEntityManagerFactory")).thenReturn(efps);
		when(context.getBean("bisHrpEntityManagerFactory")).thenReturn(efBss);
		when(efps.createEntityManager()).thenReturn(psEntityManager);
		when(efBss.createEntityManager()).thenReturn(bssEm);
		
		when(context.getBean("emailGenService")).thenReturn(emailGenService);
		when(context.getBean("submitStatusService")).thenReturn(submitStatusService);
		when(context.getBean("psCompanyDao")).thenReturn(psCompanyDao);
		when(context.getBean("companyService")).thenReturn(companyService);
		when(context.getBean("strategyService")).thenReturn(strategyService);
		when(context.getBean("realmDataDao")).thenReturn(realmDataDao);
		when(context.getBean("strategyDataDao")).thenReturn(strategyDataDao);
		
		psSubmitDataDaoImpl.createEntityManager();
	}

	@Test
	public void insertBenefitSelectionEffectiveDate() {

		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.insertBenefitSelectionEffectiveDate(company, strategySummary);

		assertEquals(1, actualResult);

		/*
		 * When strategy summary name length is > 254
		 */
		strategySummary.setName("teststrategyteststrategyteststrategyteststrategyteststrategyteststrategyteststrategy"
				+ "teststrategyteststrategyteststrategyteststrategyteststrategyteststrategyteststrategyteststrategy"
				+ "teststrategyteststrategyteststrategyteststrategyteststrategyteststrategyteststrategyteststrategy");

		actualResult = psSubmitDataDaoImpl.insertBenefitSelectionEffectiveDate(company, strategySummary);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(2)).executeUpdate();
		verify(mockedQuery2, times(0)).executeUpdate();
		strategySummary.setName("teststrategy");
	}

	@Test
	public void deleteBenefitProgram() {

		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.deleteBenefitProgram(company, bg);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(1)).executeUpdate();
	}

	@Test
	public void deleteBenefitDefinitionCostOfTypeA3() {

		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.deleteBenefitDefinitionCostOfTypeA3(bg);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(1)).executeUpdate();
	}

	@Test
	public void deleteBenefitDefinitionOptionOfTypeA3() {

		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.deleteBenefitDefinitionOptionOfTypeA3(bg);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(1)).executeUpdate();
	}

	@Test
	public void deleteBenefitDefinitionOptionOfTypeW() {

		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.deleteBenefitDefinitionOptionOfTypeW(company, Arrays.asList(BSSApplicationConstants.MEDICAL_PLAN_TYPE), bg);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(1)).executeUpdate();
	}
	
	@Test
	public void insertBenefitProgramFunding() throws Exception {
		
		when(mockedQuery1.executeUpdate()).thenReturn(1);

        mockStaticSubmitServiceHelper
                .when(() -> SubmitServiceHelper.updateMedicalFunding(Mockito.any(Query.class), Mockito.any(PlanPackage.class)))
                .thenAnswer(invocation -> null);

		int actualResult = psSubmitDataDaoImpl.insertBenefitProgramFunding(company, bg);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(2)).executeUpdate();		
		
	}

	@Test
	public void deleteBenefitDefinitionPlanOfTypeA3() {

		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.deleteBenefitDefinitionPlanOfTypeA3(bg);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(1)).executeUpdate();
	}


	@Test
	public void getLtdBenefitPlans() {

		Set<String> actualResult = psSubmitDataDaoImpl.getLtdBenefitPlans(bg);

		assertEquals(1, actualResult.size());
	}

	@Test
	public void insertAutoSelectedBenefitPlanRateData() throws SQLException {

		Map<String, List<Contribution>> benefitPlanContributions = new HashMap<String, List<Contribution>>();
		List<Contribution> contributions = new ArrayList<Contribution>();
		Contribution contribution = new Contribution();
		contribution.setBenefitPlan("MEDPLAN1");
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		contribution.setEmployeeContribution(BigDecimal.valueOf(500));
		contribution.setEmployerContribution(BigDecimal.valueOf(1000));
		contributions.add(contribution);

		contribution = new Contribution();
		contribution.setBenefitPlan("MEDPLAN1");
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
		contribution.setEmployeeContribution(BigDecimal.valueOf(750));
		contribution.setEmployerContribution(BigDecimal.valueOf(1250));
		contributions.add(contribution);
		benefitPlanContributions.put("MEDPLAN1", contributions);

		contribution = new Contribution();
		contribution.setBenefitPlan("MEDPLAN1");
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode());
		contribution.setEmployeeContribution(BigDecimal.valueOf(1000));
		contribution.setEmployerContribution(BigDecimal.valueOf(1500));
		contributions.add(contribution);
		benefitPlanContributions.put("MEDPLAN1", contributions);

		contribution = new Contribution();
		contribution.setBenefitPlan("MEDPLAN1");
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode());
		contribution.setEmployeeContribution(BigDecimal.valueOf(1250));
		contribution.setEmployerContribution(BigDecimal.valueOf(1750));
		contributions.add(contribution);
		benefitPlanContributions.put("MEDPLAN1", contributions);
		
		Map<String, String> autoSelectPlanTypes = new HashMap<String, String>();
		// For this scenario, use BANDS risk type so batchInsertRateDataForBands is used
		company.setRiskType(RiskTypeEnum.BANDS);

		when(psEntityManager.createNamedQuery("GET_DP_BENEFIT_PLANS")).thenReturn(mockedQuery2);

		setupBatchMocksAndStubs();

		when(mockedQuery2.getResultList()).thenReturn(prepareDpBenPlansMockData());
		Map<String, XbssRealmPlyrPlan> plyrPlanMap =  new HashMap<String, XbssRealmPlyrPlan>();
		int actualResult = psSubmitDataDaoImpl.insertAutoSelectedBenefitPlanRateData(company, bg,
				benefitPlanContributions, plyrPlanMap, autoSelectPlanTypes, Collections.emptyMap());

		assertEquals(4, actualResult);
		verify(mockStmt, times(8)).addBatch();
	}


	@Test(expected = BSSBadDataException.class)
	public void insertAutoSelectedBenefitPlanRateDataThrowsBSSApplicationException() throws Exception {

		Map<String, List<Contribution>> benefitPlanContributions = new HashMap<String, List<Contribution>>();
		List<Contribution> contributions = new ArrayList<Contribution>();
		Contribution contribution = new Contribution();
		contribution.setBenefitPlan("MEDPLAN1");
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		contribution.setEmployeeContribution(BigDecimal.valueOf(500));
		contribution.setEmployerContribution(BigDecimal.valueOf(1000));
		contributions.add(contribution);

		contribution = new Contribution();
		contribution.setBenefitPlan("MEDPLAN1");
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		contribution.setEmployeeContribution(null);
		contribution.setEmployerContribution(null);
		contributions.add(contribution);
		benefitPlanContributions.put("MEDPLAN1", contributions);
		
		Map<String, String> autoSelectPlanTypes = new HashMap<String, String>();

		setupBatchMocksAndStubs();
		//when(mockStmt.executeBatch()).thenThrow( new SQLException() );

		Map<String, XbssRealmPlyrPlan> plyrPlanMap =  new HashMap<String, XbssRealmPlyrPlan>();
		psSubmitDataDaoImpl.insertAutoSelectedBenefitPlanRateData(company, bg, benefitPlanContributions, plyrPlanMap, autoSelectPlanTypes, Collections.emptyMap());

	}

	@Test
	public void insertBenefitDefinitionPlanOfTypeA3() {
		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.insertBenefitDefinitionPlanOfTypeA3(company, bg);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(1)).executeUpdate();
	}

	@Test
	public void insertBenefitDefinitionOptionOfTypeA3() {
		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.insertBenefitDefinitionOptionOfTypeA3(company, bg);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(1)).executeUpdate();
	}

	@Test
	public void insertBenefitDefinitionCostOfTypeA3() {
		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.insertBenefitDefinitionCostOfTypeA3(company, bg);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(1)).executeUpdate();
	}

	@Test
	public void insertBenefitPlanRateTbl() throws SQLException {
		GroupRate gr = new GroupRate();
		GroupRatePK id = new GroupRatePK();
		id.setRateTblId("1ABCDE");
		gr.setId(id);
		gr.setRateIdType(Constants.MEDICAL_CODE);  // this one only should be "updated" by the class we are testing
		Set<GroupRate> grs = new HashSet<GroupRate>();
		grs.add(gr);

		gr = new GroupRate();
		id = new GroupRatePK();
		id.setRateTblId("1VWXYZ");
		gr.setId(id);
		gr.setRateIdType("WAIVE");  // this one should not be updated by the tested class
		grs.add(gr);

		bg.setGroupRate(grs);

		setupBatchMocksAndStubs();

		int actualResult = psSubmitDataDaoImpl.insertBenefitPlanRateTbl(company, bg);

		assertEquals(1, actualResult);
		verify(mockStmt, times(1)).addBatch();
	}

	@Test
	public void insertBenefitDefinitionOption() {
		List<String> planTypes = new ArrayList<String>();
		planTypes.add(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		when(mockedQuery1.getSingleResult()).thenReturn(BigDecimal.valueOf(0));
		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.insertBenefitDefinitionOption(company, bg, planTypes);

		assertEquals(1, actualResult);

		/*
		 * When singleResult return value is not 0
		 */
		when(mockedQuery1.getSingleResult()).thenReturn(BigDecimal.valueOf(1));

		actualResult = psSubmitDataDaoImpl.insertBenefitDefinitionOption(company, bg, planTypes);

		assertEquals(0, actualResult);
		verify(mockedQuery1, times(2)).getSingleResult();
		verify(mockedQuery1, times(1)).executeUpdate();
	}

	@Test
	public void deleteBenefitRateData() {
		GroupRate gr = new GroupRate();
		GroupRatePK id = new GroupRatePK();
		id.setRateTblId("rateTblId");
		gr.setId(id);
		Set<GroupRate> grs = new HashSet<GroupRate>();
		grs.add(gr);
		bg.setGroupRate(grs);
		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.deleteBenefitRateData(company, bg);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(1)).executeUpdate();
	}

	@Test
	public void updateWaiveRow() {
		when( mockedQuery1.executeUpdate() ).thenReturn( 1 );
		List<String> planTypes = new ArrayList<String>();
		planTypes.add(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		int actualResult = psSubmitDataDaoImpl.updateWaiveRow(company, planTypes, bg);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(1)).executeUpdate();
	}


	@Test
	public void updateWaiveRowForAdditionalBenefits() {

		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.updateWaiveRowForAdditionalBenefits(company, bg);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(1)).executeUpdate();
	}


	@Test
	public void updateSTDBenefitDefitionOption() {

		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.updateSTDBenefitDefitionOption(company, bg);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(1)).executeUpdate();

	}

	@Test
	public void updateLTDBenefitDefitionOption() {

		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.updateLTDBenefitDefitionOption(bg);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(1)).executeUpdate();

	}

	@Test
	public void updateBenefitDefitionPlan() {

		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.updateBenefitDefitionPlan(company, Arrays.asList(BSSApplicationConstants.MEDICAL_PLAN_TYPE), bg);

		assertEquals(1, actualResult);
		verify(mockedQuery1, times(1)).executeUpdate();

	}

	@Test
	public void updateBenefitDefinitionOptionCost() {

		when(mockedQuery1.executeUpdate()).thenReturn(1);

		int actualResult = psSubmitDataDaoImpl.updateBenefitDefinitionOptionCost(company, bg);

		assertEquals(3, actualResult);
		verify(mockedQuery1, times(3)).executeUpdate();

	}

	@Test
	public void defaultSubmit() throws Exception {
		String userId ="121231231";
		String quarter = "SM";
		Long relamYearId = 10L;
		Date payrollCutOffDate = new Date();
		List<String> companyList = new ArrayList<String>();
		companyList.add("TEST");
		List<StrategyData> strategyData = prepareStrategyData();
        mockStaticAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isSnowEmailsEnabled).thenReturn(true);
		ApplicationContext context = mock(ApplicationContext.class);
		EntityManagerFactory ef = mock(EntityManagerFactory.class);
		EntityManager entityManager = mock(EntityManager.class);
		EntityTransaction transaction = mock(EntityTransaction.class);

        mockStaticApplicationContextProvider.when(ApplicationContextProvider::getApplicationContext).thenReturn(context);
        mockStaticSubmitServiceHelper
                .when(() -> SubmitServiceHelper.updateBenefitGroupData(benefitGroupStrategyService, strategyData.get(0)))
                .thenReturn(Arrays.asList(prepareBenefitGroup()));
		psSubmitDataDaoImpl.defaultSubmit(new ArrayList<String>(), true, userId, relamYearId);
	}

	@Test
	public void updateBenefitStartDateAndQuarterForPlanYearSync() {
		String newPlanStartDate = "2025-10-01";

		when(mockedQuery1.executeUpdate()).thenReturn(1);
		psSubmitDataDaoImpl.updateBenefitStartDateAndQuarterForPlanYearSync(company, newPlanStartDate);
		verify(mockedQuery1, times(2)).executeUpdate();
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

	private void setCompanyPlanStartDate(String planStartDate) throws Exception {
		Field field = PsSubmitDataDaoImpl.class.getDeclaredField("companyPlanStartDate");
		field.setAccessible(true);
		field.set(psSubmitDataDaoImpl, planStartDate);
	}

	private BenefitGroup prepareBenefitGroup() {
		BenefitGroup benefitGroup = new BenefitGroup();

		// Provide the minimum GroupRate entries required by PsSubmitDataDaoImpl when inserting
		// benefit plan rate data (especially for DIFFERENTIALS where null validation is strict).
		Set<GroupRate> groupRates = new HashSet<>();
		GroupRate medicalRate = new GroupRate();
		GroupRatePK medicalRatePk = new GroupRatePK();
		medicalRatePk.setRateTblId("RTBL_MED");
		medicalRate.setId(medicalRatePk);
		medicalRate.setRateIdType(Constants.MEDICAL_CODE); // "10"
		groupRates.add(medicalRate);

		GroupRate otherRate = new GroupRate();
		GroupRatePK otherRatePk = new GroupRatePK();
		otherRatePk.setRateTblId("RTBL_OTH");
		otherRate.setId(otherRatePk);
		otherRate.setRateIdType("OTHER");
		groupRates.add(otherRate);

		benefitGroup.setGroupRate(groupRates);

		List<BenefitOffer> benefitOffers = new ArrayList<BenefitOffer>();
		BenefitOffer benefitOffer = new BenefitOffer();
		BenefitOfferSummary benefitOfferSummary = new BenefitOfferSummary();
		benefitOfferSummary.setType(BSSApplicationConstants.MEDICAL);
		benefitOffer.setSummary(benefitOfferSummary);
		PlanPackage pkg = new PlanPackage();
		benefitOffer.setPlanPackage( pkg );
		List<BenefitPlan> benefitPlans = new ArrayList<BenefitPlan>();
		BenefitPlan benefitPlan = new BenefitPlan();
		benefitPlan.setId("MEDPLAN1");
		benefitPlans.add(benefitPlan);
		benefitOffer.setBenefitPlans(benefitPlans);
		benefitOffers.add(benefitOffer);

		benefitOffer = new BenefitOffer();
		benefitOfferSummary = new BenefitOfferSummary();
		benefitOfferSummary.setType(BSSApplicationConstants.DENTAL);
		benefitOffer.setSummary(benefitOfferSummary);
		benefitOffers.add(benefitOffer);

		List<AdditionalBenefitOffer> additionalBenefitOffers = new ArrayList<AdditionalBenefitOffer>();
		AdditionalBenefitOffer additionalBenefitOffer = new AdditionalBenefitOffer();
		BenefitOfferSummary additionalBenefitOfferSummary = new BenefitOfferSummary();
		additionalBenefitOfferSummary.setType(BSSApplicationConstants.DISABILITY);
		additionalBenefitOffer.setSummary(additionalBenefitOfferSummary);
		List<AdditionalBenefitPlan> additionalBenefitPlans = new ArrayList<AdditionalBenefitPlan>();
		AdditionalBenefitPlan additionalBenefitPlan = new AdditionalBenefitPlan();
		List<DisabilityBenefitOptionPlans> optionPlans = new ArrayList<DisabilityBenefitOptionPlans>();
		DisabilityBenefitOptionPlans optionPlan = new DisabilityBenefitOptionPlans();
		optionPlan.setPlanType(BSSApplicationConstants.LTD_CODE);
		optionPlan.setId("LTDPLAN");
		optionPlans.add(optionPlan);
		additionalBenefitPlan.setOptionPlans(optionPlans);
		additionalBenefitPlans.add(additionalBenefitPlan);
		additionalBenefitOffer.setAdditionalBenefitPlans(additionalBenefitPlans);
		additionalBenefitOffers.add(additionalBenefitOffer);
		benefitOfferSummary = new BenefitOfferSummary();
		benefitOfferSummary.setType(BSSApplicationConstants.ADDITIONAL);
		benefitOffer.setSummary(benefitOfferSummary);
		benefitOffer.setAdditionalBenefitOffers(additionalBenefitOffers);
		benefitOffers.add(benefitOffer);

		benefitGroup.setBenefitOffers(benefitOffers);
		return benefitGroup;
	}
	
	private List<StrategyData> prepareStrategyData() {
		List<StrategyData> strategyDataList = new ArrayList<StrategyData>();
		StrategyData strategyData = new StrategyData();
		StrategySummary strategySummary = new StrategySummary();
		strategySummary.setId(1000L);
		strategyData.setStrategySummary(strategySummary);
		strategyDataList.add(strategyData);
		return strategyDataList;
	}

	private List<Object[]> prepareDpBenPlansMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[2];
		r[0] = "MEDPLAN1";
		r[1] = "DPPLAN1";
		results.add(r);
		r = new Object[2];
		r[0] = "MEDPLAN2";
		r[1] = "DPPLAN2";
		results.add(r);
		r = new Object[2];
		r[0] = "MEDPLAN3";
		r[1] = "DPPLAN3";
		results.add(r);
		return results;
	}

	// Tests for batchInsertRateDataForDifferentials method

	@Test
	public void testInsertBenefitPlanRateDataForDifferentials() throws Exception {
		// Test successful batch insert for DIFFERENTIALS risk type with pay-in rates
		Map<String, List<Contribution>> benefitPlanContributions = new HashMap<>();
		List<Contribution> contributions = new ArrayList<>();

		Contribution contribution = new Contribution();
		contribution.setBenefitPlan("MEDPLAN1");
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		contribution.setEmployeeContribution(BigDecimal.valueOf(500));
		contribution.setEmployerContribution(BigDecimal.valueOf(1000));
		contributions.add(contribution);

		contribution = new Contribution();
		contribution.setBenefitPlan("MEDPLAN1");
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
		contribution.setEmployeeContribution(BigDecimal.valueOf(750));
		contribution.setEmployerContribution(BigDecimal.valueOf(1250));
		contributions.add(contribution);

		benefitPlanContributions.put("MEDPLAN1", contributions);

		Map<String, String> autoSelectPlanTypes = new HashMap<>();
		autoSelectPlanTypes.put("MEDPLAN1", BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		// Set DIFFERENTIALS risk type to trigger batchInsertRateDataForDifferentials
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);

		// Prepare pay-in rates map
		Map<String, List<com.trinet.ambis.service.model.PayInRateInfo>> payInRatesMap = new HashMap<>();
		List<com.trinet.ambis.service.model.PayInRateInfo> payInRates = new ArrayList<>();
		payInRates.add(com.trinet.ambis.service.model.PayInRateInfo.builder()
				.planType("10")
				.coverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode())
				.payInRate(50.0)
				.build());
		payInRates.add(com.trinet.ambis.service.model.PayInRateInfo.builder()
				.planType("10")
				.coverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode())
				.payInRate(100.0)
				.build());
		payInRatesMap.put("MEDPLAN1", payInRates);

		when(psEntityManager.createNamedQuery("GET_DP_BENEFIT_PLANS")).thenReturn(mockedQuery2);
		setupBatchMocksAndStubs();
		// Return empty DP plans list to avoid DP processing (which would require DP coverage pay-in rates)
		when(mockedQuery2.getResultList()).thenReturn(new ArrayList<>());

		// Set the companyPlanStartDate field using reflection
		setCompanyPlanStartDate("01-JAN-2019");

		Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
		int actualResult = psSubmitDataDaoImpl.insertAutoSelectedBenefitPlanRateData(company, bg,
				benefitPlanContributions, plyrPlanMap, autoSelectPlanTypes, payInRatesMap);

		assertEquals(2, actualResult);
		verify(mockStmt, times(2)).addBatch(); // 2 for rate data (no DP plans)
	}

	@Test
	public void testInsertBenefitPlanRateDataForDifferentialsWithMultiplePlans() throws Exception {
		// Test batch insert for multiple plans with DIFFERENTIALS risk type
		Map<String, List<Contribution>> benefitPlanContributions = new HashMap<>();

		// Plan 1
		List<Contribution> contributions1 = new ArrayList<>();
		Contribution contribution1 = new Contribution();
		contribution1.setBenefitPlan("MEDPLAN1");
		contribution1.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		contribution1.setEmployeeContribution(BigDecimal.valueOf(500));
		contribution1.setEmployerContribution(BigDecimal.valueOf(1000));
		contributions1.add(contribution1);
		benefitPlanContributions.put("MEDPLAN1", contributions1);

		// Plan 2
		List<Contribution> contributions2 = new ArrayList<>();
		Contribution contribution2 = new Contribution();
		contribution2.setBenefitPlan("MEDPLAN2");
		contribution2.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		contribution2.setEmployeeContribution(BigDecimal.valueOf(600));
		contribution2.setEmployerContribution(BigDecimal.valueOf(1100));
		contributions2.add(contribution2);
		benefitPlanContributions.put("MEDPLAN2", contributions2);

		Map<String, String> autoSelectPlanTypes = new HashMap<>();
		autoSelectPlanTypes.put("MEDPLAN1", BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		autoSelectPlanTypes.put("MEDPLAN2", BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);

		// Prepare pay-in rates for both plans
		Map<String, List<com.trinet.ambis.service.model.PayInRateInfo>> payInRatesMap = new HashMap<>();
		List<com.trinet.ambis.service.model.PayInRateInfo> payInRates1 = new ArrayList<>();
		payInRates1.add(com.trinet.ambis.service.model.PayInRateInfo.builder()
				.planType("10")
				.coverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode())
				.payInRate(50.0)
				.build());
		payInRatesMap.put("MEDPLAN1", payInRates1);

		List<com.trinet.ambis.service.model.PayInRateInfo> payInRates2 = new ArrayList<>();
		payInRates2.add(com.trinet.ambis.service.model.PayInRateInfo.builder()
				.planType("10")
				.coverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode())
				.payInRate(60.0)
				.build());
		payInRatesMap.put("MEDPLAN2", payInRates2);

		when(psEntityManager.createNamedQuery("GET_DP_BENEFIT_PLANS")).thenReturn(mockedQuery2);
		setupBatchMocksAndStubs();
		// Return empty DP plans list to avoid DP processing (which would require DP coverage pay-in rates)
		when(mockedQuery2.getResultList()).thenReturn(new ArrayList<>());

		// Set the companyPlanStartDate field using reflection
		setCompanyPlanStartDate("01-JAN-2019");

		Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
		int actualResult = psSubmitDataDaoImpl.insertAutoSelectedBenefitPlanRateData(company, bg,
				benefitPlanContributions, plyrPlanMap, autoSelectPlanTypes, payInRatesMap);

		assertEquals(2, actualResult);
		verify(mockStmt, times(2)).addBatch(); // 2 for rate data (no DP plans)
	}

	@Test(expected = BSSApplicationException.class)
	public void testBatchInsertRateDataForDifferentialsThrowsExceptionOnNullRateTblId() throws Exception {
		// Test that null validation works for DIFFERENTIALS - missing rateTblId
		Map<String, List<Contribution>> benefitPlanContributions = new HashMap<>();
		List<Contribution> contributions = new ArrayList<>();

		Contribution contribution = new Contribution();
		contribution.setBenefitPlan("MEDPLAN1");
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		contribution.setEmployeeContribution(BigDecimal.valueOf(500));
		contribution.setEmployerContribution(BigDecimal.valueOf(1000));
		contributions.add(contribution);
		benefitPlanContributions.put("MEDPLAN1", contributions);

		Map<String, String> autoSelectPlanTypes = new HashMap<>();
		autoSelectPlanTypes.put("MEDPLAN1", BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);

		// Prepare pay-in rates
		Map<String, List<com.trinet.ambis.service.model.PayInRateInfo>> payInRatesMap = new HashMap<>();
		List<com.trinet.ambis.service.model.PayInRateInfo> payInRates = new ArrayList<>();
		payInRates.add(com.trinet.ambis.service.model.PayInRateInfo.builder()
				.planType("10")
				.coverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode())
				.payInRate(50.0)
				.build());
		payInRatesMap.put("MEDPLAN1", payInRates);
		setupBatchMocksAndStubs();

		// Set the companyPlanStartDate field using reflection
		setCompanyPlanStartDate("01-JAN-2019");

		// Set up a benefit group with no group rates to cause null rateTblId
		BenefitGroup bgNoRates = prepareBenefitGroup();
		bgNoRates.setGroupRate(new HashSet<>());

		Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
		psSubmitDataDaoImpl.insertAutoSelectedBenefitPlanRateData(company, bgNoRates,
				benefitPlanContributions, plyrPlanMap, autoSelectPlanTypes, payInRatesMap);
	}

	@Test(expected = BSSApplicationException.class)
	public void testBatchInsertRateDataForDifferentialsThrowsSQLException() throws Exception {
		// Test SQL exception handling in batchInsertRateDataForDifferentials
		Map<String, List<Contribution>> benefitPlanContributions = new HashMap<>();
		List<Contribution> contributions = new ArrayList<>();

		Contribution contribution = new Contribution();
		contribution.setBenefitPlan("MEDPLAN1");
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		contribution.setEmployeeContribution(BigDecimal.valueOf(500));
		contribution.setEmployerContribution(BigDecimal.valueOf(1000));
		contributions.add(contribution);
		benefitPlanContributions.put("MEDPLAN1", contributions);

		Map<String, String> autoSelectPlanTypes = new HashMap<>();
		autoSelectPlanTypes.put("MEDPLAN1", BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);

		// Prepare pay-in rates
		Map<String, List<com.trinet.ambis.service.model.PayInRateInfo>> payInRatesMap = new HashMap<>();
		List<com.trinet.ambis.service.model.PayInRateInfo> payInRates = new ArrayList<>();
		payInRates.add(com.trinet.ambis.service.model.PayInRateInfo.builder()
				.planType("10")
				.coverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode())
				.payInRate(50.0)
				.build());
		payInRatesMap.put("MEDPLAN1", payInRates);
		setupBatchMocksAndStubs();

		// Set the companyPlanStartDate field using reflection
		setCompanyPlanStartDate("01-JAN-2019");

		// Mock SQLException on executeBatch
		when(mockStmt.executeBatch()).thenThrow(new SQLException("Database error"));

		Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
		psSubmitDataDaoImpl.insertAutoSelectedBenefitPlanRateData(company, bg,
				benefitPlanContributions, plyrPlanMap, autoSelectPlanTypes, payInRatesMap);
	}

	@Test
	public void testBatchInsertRateDataForDifferentialsWithAllCoverageLevels() throws Exception {
		// Test all coverage levels with DIFFERENTIALS
		Map<String, List<Contribution>> benefitPlanContributions = new HashMap<>();
		List<Contribution> contributions = new ArrayList<>();

		// Employee
		Contribution contribution1 = new Contribution();
		contribution1.setBenefitPlan("MEDPLAN1");
		contribution1.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		contribution1.setEmployeeContribution(BigDecimal.valueOf(500));
		contribution1.setEmployerContribution(BigDecimal.valueOf(1000));
		contributions.add(contribution1);

		// Employee + Spouse
		Contribution contribution2 = new Contribution();
		contribution2.setBenefitPlan("MEDPLAN1");
		contribution2.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
		contribution2.setEmployeeContribution(BigDecimal.valueOf(750));
		contribution2.setEmployerContribution(BigDecimal.valueOf(1250));
		contributions.add(contribution2);

		// Employee + Child
		Contribution contribution3 = new Contribution();
		contribution3.setBenefitPlan("MEDPLAN1");
		contribution3.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode());
		contribution3.setEmployeeContribution(BigDecimal.valueOf(800));
		contribution3.setEmployerContribution(BigDecimal.valueOf(1300));
		contributions.add(contribution3);

		// Family
		Contribution contribution4 = new Contribution();
		contribution4.setBenefitPlan("MEDPLAN1");
		contribution4.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode());
		contribution4.setEmployeeContribution(BigDecimal.valueOf(1000));
		contribution4.setEmployerContribution(BigDecimal.valueOf(1500));
		contributions.add(contribution4);

		benefitPlanContributions.put("MEDPLAN1", contributions);

		Map<String, String> autoSelectPlanTypes = new HashMap<>();
		autoSelectPlanTypes.put("MEDPLAN1", BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);

		// Prepare pay-in rates for all coverage levels
		Map<String, List<com.trinet.ambis.service.model.PayInRateInfo>> payInRatesMap = new HashMap<>();
		List<com.trinet.ambis.service.model.PayInRateInfo> payInRates = new ArrayList<>();
		payInRates.add(com.trinet.ambis.service.model.PayInRateInfo.builder()
				.planType("10")
				.coverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode())
				.payInRate(50.0)
				.build());
		payInRates.add(com.trinet.ambis.service.model.PayInRateInfo.builder()
				.planType("10")
				.coverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode())
				.payInRate(100.0)
				.build());
		payInRates.add(com.trinet.ambis.service.model.PayInRateInfo.builder()
				.planType("10")
				.coverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode())
				.payInRate(125.0)
				.build());
		payInRates.add(com.trinet.ambis.service.model.PayInRateInfo.builder()
				.planType("10")
				.coverageLevel(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode())
				.payInRate(150.0)
				.build());
		payInRatesMap.put("MEDPLAN1", payInRates);

		when(psEntityManager.createNamedQuery("GET_DP_BENEFIT_PLANS")).thenReturn(mockedQuery2);
		setupBatchMocksAndStubs();
		// Return empty DP plans list to avoid DP processing (which would require DP coverage pay-in rates)
		when(mockedQuery2.getResultList()).thenReturn(new ArrayList<>());

		// Set the companyPlanStartDate field using reflection
		setCompanyPlanStartDate("01-JAN-2019");

		Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
		int actualResult = psSubmitDataDaoImpl.insertAutoSelectedBenefitPlanRateData(company, bg,
				benefitPlanContributions, plyrPlanMap, autoSelectPlanTypes, payInRatesMap);

		assertEquals(4, actualResult);
		verify(mockStmt, times(4)).addBatch(); // 4 for rate data (no DP plans)
	}

	@Test
	public void testBatchInsertRateDataForDifferentialsWithDentalAndVision() throws Exception {
		// Test DIFFERENTIALS with dental and vision plans
		Map<String, List<Contribution>> benefitPlanContributions = new HashMap<>();

		// Dental contribution
		List<Contribution> dentalContributions = new ArrayList<>();
		Contribution dentalContribution = new Contribution();
		dentalContribution.setBenefitPlan("DENTALPLAN1");
		dentalContribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		dentalContribution.setEmployeeContribution(BigDecimal.valueOf(50));
		dentalContribution.setEmployerContribution(BigDecimal.valueOf(100));
		dentalContributions.add(dentalContribution);
		benefitPlanContributions.put("DENTALPLAN1", dentalContributions);

		// Vision contribution
		List<Contribution> visionContributions = new ArrayList<>();
		Contribution visionContribution = new Contribution();
		visionContribution.setBenefitPlan("VISIONPLAN1");
		visionContribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		visionContribution.setEmployeeContribution(BigDecimal.valueOf(25));
		visionContribution.setEmployerContribution(BigDecimal.valueOf(75));
		visionContributions.add(visionContribution);
		benefitPlanContributions.put("VISIONPLAN1", visionContributions);

		Map<String, String> autoSelectPlanTypes = new HashMap<>();
		autoSelectPlanTypes.put("DENTALPLAN1", BSSApplicationConstants.DENTAL_PLAN_TYPE);
		autoSelectPlanTypes.put("VISIONPLAN1", BSSApplicationConstants.VISION_PLAN_TYPE);

		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);

		// Prepare pay-in rates
		Map<String, List<com.trinet.ambis.service.model.PayInRateInfo>> payInRatesMap = new HashMap<>();
		List<com.trinet.ambis.service.model.PayInRateInfo> dentalRates = new ArrayList<>();
		dentalRates.add(com.trinet.ambis.service.model.PayInRateInfo.builder()
				.planType("11")
				.coverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode())
				.payInRate(20.0)
				.build());
		payInRatesMap.put("DENTALPLAN1", dentalRates);

		List<com.trinet.ambis.service.model.PayInRateInfo> visionRates = new ArrayList<>();
		visionRates.add(com.trinet.ambis.service.model.PayInRateInfo.builder()
				.planType("12")
				.coverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode())
				.payInRate(10.0)
				.build());
		payInRatesMap.put("VISIONPLAN1", visionRates);

		when(psEntityManager.createNamedQuery("GET_DP_BENEFIT_PLANS")).thenReturn(mockedQuery2);
		setupBatchMocksAndStubs();
		// Return empty DP plans list to avoid DP processing (which would require DP coverage pay-in rates)
		when(mockedQuery2.getResultList()).thenReturn(new ArrayList<>());

		// Set the companyPlanStartDate field using reflection
		setCompanyPlanStartDate("01-JAN-2019");

		Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
		int actualResult = psSubmitDataDaoImpl.insertAutoSelectedBenefitPlanRateData(company, bg,
				benefitPlanContributions, plyrPlanMap, autoSelectPlanTypes, payInRatesMap);

		assertEquals(2, actualResult);
		verify(mockStmt, times(2)).addBatch(); // 2 for rate data (no DP plans)
	}

	@Test
	public void testBatchInsertRateDataForDifferentialsPayInRateLookupLoop() throws Exception {
		// Test to specifically cover the PayInRateInfo loop iteration
		// This test ensures the loop iterates through multiple PayInRateInfo entries
		// to find the one with matching coverage level
		Map<String, List<Contribution>> benefitPlanContributions = new HashMap<>();
		List<Contribution> contributions = new ArrayList<>();

		// Create contribution for coverage level "2" (Employee+Spouse)
		Contribution contribution = new Contribution();
		contribution.setBenefitPlan("MEDPLAN1");
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
		contribution.setEmployeeContribution(BigDecimal.valueOf(750));
		contribution.setEmployerContribution(BigDecimal.valueOf(1250));
		contributions.add(contribution);
		benefitPlanContributions.put("MEDPLAN1", contributions);

		Map<String, String> autoSelectPlanTypes = new HashMap<>();
		autoSelectPlanTypes.put("MEDPLAN1", BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);

		// Prepare pay-in rates with MULTIPLE entries - this tests the loop iteration
		// The loop should iterate through these to find the matching coverage level
		Map<String, List<com.trinet.ambis.service.model.PayInRateInfo>> payInRatesMap = new HashMap<>();
		List<com.trinet.ambis.service.model.PayInRateInfo> payInRates = new ArrayList<>();

		// Add entry for coverage level "1" - should NOT match
		payInRates.add(com.trinet.ambis.service.model.PayInRateInfo.builder()
				.planType("10")
				.coverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode()) // "1"
				.payInRate(50.0)
				.build());

		// Add entry for coverage level "2" - SHOULD match (this is what we're testing)
		payInRates.add(com.trinet.ambis.service.model.PayInRateInfo.builder()
				.planType("10")
				.coverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode()) // "2"
				.payInRate(100.0)
				.build());

		// Add entry for coverage level "C" - should NOT match
		payInRates.add(com.trinet.ambis.service.model.PayInRateInfo.builder()
				.planType("10")
				.coverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode()) // "C"
				.payInRate(125.0)
				.build());

		payInRatesMap.put("MEDPLAN1", payInRates);

		when(psEntityManager.createNamedQuery("GET_DP_BENEFIT_PLANS")).thenReturn(mockedQuery2);
		setupBatchMocksAndStubs();
		// Return empty DP plans list
		when(mockedQuery2.getResultList()).thenReturn(new ArrayList<>());

		// Set the companyPlanStartDate field using reflection
		setCompanyPlanStartDate("01-JAN-2019");

		Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
		int actualResult = psSubmitDataDaoImpl.insertAutoSelectedBenefitPlanRateData(company, bg,
				benefitPlanContributions, plyrPlanMap, autoSelectPlanTypes, payInRatesMap);

		// Verify the method successfully found the matching pay-in rate through loop iteration
		assertEquals(1, actualResult);
		verify(mockStmt, times(1)).addBatch();
	}

	@Test
	public void testGetLifeDisablityRateId_NoBandCodeThrowsException() throws Exception {
		// Test the else block where bandCode is null and BSSApplicationException is thrown
		// This tests the scenario where company has no band codes set for the plan type

		// Setup: Company with no band codes (non-DIFFERENTIALS risk type)
		Company testCompany = new Company();
		testCompany.setCode("TEST_COMPANY");
		testCompany.setQuater("2019Q1");
		testCompany.setPlanStartDate("01-JAN-2019");
		testCompany.setRiskType(RiskTypeEnum.BANDS); // Not DIFFERENTIALS, so it goes to else block
		testCompany.setBandCodes(null); // No band codes set

		// Setup: BenefitPlanRate for LIFE plan type
		BenefitPlanRate benefitPlanRate = new BenefitPlanRate();
		benefitPlanRate.setPlanType(Constants.LIFE_CODE); // "23"
		benefitPlanRate.setBenefitPlan("LIFE_PLAN_1");

		// Mock the query
		Query mockQuery = mock(Query.class);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.GET_LIFE_DISB_RATE_ID))
			.thenReturn(mockQuery);
		when(mockQuery.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
			.thenReturn(mockQuery);

		// Use reflection to invoke the private method getLifeDisablityRateId
		java.lang.reflect.Method method = PsSubmitDataDaoImpl.class
			.getDeclaredMethod("getLifeDisablityRateId", Company.class, BenefitPlanRate.class);
		method.setAccessible(true);

		// When using reflection, the exception is wrapped in InvocationTargetException
		try {
			method.invoke(psSubmitDataDaoImpl, testCompany, benefitPlanRate);
			// If we get here, the test should fail
			throw new AssertionError("Expected BSSApplicationException to be thrown");
		} catch (java.lang.reflect.InvocationTargetException e) {
			// Verify the cause is BSSApplicationException
			assertEquals(BSSApplicationException.class, e.getCause().getClass());
			assertEquals("COMPANY HAS NO BAND CODES SET FOR PLAN TYPE : " + Constants.LIFE_CODE,
					e.getCause().getMessage());
		}
	}

	@Test
	public void testGetLifeDisablityRateId_BandCodesExistButNullLifeBandCode() throws Exception {
		// Test scenario where BandCodes object exists but lifeBandCode is null

		// Setup: Company with band codes but null lifeBandCode
		Company testCompany = new Company();
		testCompany.setCode("TEST_COMPANY");
		testCompany.setQuater("2019Q1");
		testCompany.setPlanStartDate("01-JAN-2019");
		testCompany.setRiskType(RiskTypeEnum.BANDS); // Not DIFFERENTIALS

		BandCodes bandCodes = new BandCodes();
		bandCodes.setLifeBandCode(null); // Explicitly null life band code
		bandCodes.setDisBandCode("DIS123"); // Has disability code but not life code
		testCompany.setBandCodes(bandCodes);

		// Setup: BenefitPlanRate for LIFE plan type
		BenefitPlanRate benefitPlanRate = new BenefitPlanRate();
		benefitPlanRate.setPlanType(Constants.LIFE_CODE); // "23"
		benefitPlanRate.setBenefitPlan("LIFE_PLAN_1");

		// Mock the query
		Query mockQuery = mock(Query.class);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.GET_LIFE_DISB_RATE_ID))
			.thenReturn(mockQuery);
		when(mockQuery.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
			.thenReturn(mockQuery);

		// Use reflection to invoke the private method
		java.lang.reflect.Method method = PsSubmitDataDaoImpl.class
			.getDeclaredMethod("getLifeDisablityRateId", Company.class, BenefitPlanRate.class);
		method.setAccessible(true);

		// When using reflection, the exception is wrapped in InvocationTargetException
		try {
			method.invoke(psSubmitDataDaoImpl, testCompany, benefitPlanRate);
			// If we get here, the test should fail
			throw new AssertionError("Expected BSSApplicationException to be thrown");
		} catch (java.lang.reflect.InvocationTargetException e) {
			// Verify the cause is BSSApplicationException
			assertEquals(BSSApplicationException.class, e.getCause().getClass());
			assertEquals("COMPANY HAS NO BAND CODES SET FOR PLAN TYPE : " + Constants.LIFE_CODE,
					e.getCause().getMessage());
		}
	}

	@Test
	public void testGetLifeDisablityRateId_BandCodesExistButNullDisBandCode() throws Exception {
		// Test scenario where BandCodes object exists but disBandCode is null for LTD/STD

		// Setup: Company with band codes but null disBandCode
		Company testCompany = new Company();
		testCompany.setCode("TEST_COMPANY");
		testCompany.setQuater("2019Q1");
		testCompany.setPlanStartDate("01-JAN-2019");
		testCompany.setRiskType(RiskTypeEnum.BANDS); // Not DIFFERENTIALS

		BandCodes bandCodes = new BandCodes();
		bandCodes.setLifeBandCode("LIFE123"); // Has life code
		bandCodes.setDisBandCode(null); // Explicitly null disability band code
		testCompany.setBandCodes(bandCodes);

		// Setup: BenefitPlanRate for LTD plan type
		BenefitPlanRate benefitPlanRate = new BenefitPlanRate();
		benefitPlanRate.setPlanType(Constants.LTD_CODE); // "31"
		benefitPlanRate.setBenefitPlan("LTD_PLAN_1");

		// Mock the query
		Query mockQuery = mock(Query.class);
		when(psEntityManager.createNamedQuery(PsSubmitDataDaoImpl.GET_LIFE_DISB_RATE_ID))
			.thenReturn(mockQuery);
		when(mockQuery.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
			.thenReturn(mockQuery);

		// Use reflection to invoke the private method
		java.lang.reflect.Method method = PsSubmitDataDaoImpl.class
			.getDeclaredMethod("getLifeDisablityRateId", Company.class, BenefitPlanRate.class);
		method.setAccessible(true);

		// When using reflection, the exception is wrapped in InvocationTargetException
		try {
			method.invoke(psSubmitDataDaoImpl, testCompany, benefitPlanRate);
			// If we get here, the test should fail
			throw new AssertionError("Expected BSSApplicationException to be thrown");
		} catch (java.lang.reflect.InvocationTargetException e) {
			// Verify the cause is BSSApplicationException
			assertEquals(BSSApplicationException.class, e.getCause().getClass());
			assertEquals("COMPANY HAS NO BAND CODES SET FOR PLAN TYPE : " + Constants.LTD_CODE,
					e.getCause().getMessage());
		}
	}

}
