package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.ps.impl.RenewalDataDaoImpl;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanMapping;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanHeadCount;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class RenewalDataDaoImplTest {

	RenewalDataDaoImpl renewalDataDao;
	
	@Mock
	EntityManager entityManager;
	
	@Mock
	Query mockedQuery;
	
	private static final long REALM_PLYR_ID = 1111L;
	private RealmPlanYear prevRealmPlanYear;

    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

    @Before
    public void setup() {
        renewalDataDao = new RenewalDataDaoImpl();
        renewalDataDao.setEntityManager(entityManager);

        when(mockedQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockedQuery);
        when(renewalDataDao.getEntityManager().createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);

        rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
        rulesAndConfigsUtilsMockedStatic.when(() -> RulesAndConfigsUtils.getSDIStates(REALM_PLYR_ID))
                .thenReturn(new HashSet<>(Arrays.asList("NJ", "RI", "CA", "HI")));

        prevRealmPlanYear = new RealmPlanYear();
        prevRealmPlanYear.setId(2);
    }

    @After
    public void tearDown() {
        rulesAndConfigsUtilsMockedStatic.close();
    }

	@Test
	public void getHeadCountByGroupAndPlanTest() {
		String company = "G48";
		Date effDate = new Date();

		List<Object[]> mockedResult = prepareHeadCountMockData();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);

		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> result = renewalDataDao
				.getHeadCountByGroupAndPlan(company, effDate);

		assertEquals(2, result.size());
		assertEquals("001EKY", result.get("EF1").get("001EKY").get(0).getBenefitPlan());
		assertEquals("1", result.get("EF1").get("001EKY").get(0).getCovrgCode());
		assertEquals("EF1", result.get("EF1").get("001EKY").get(0).getGroupName());
		assertEquals(1, result.get("EF1").get("001EKY").get(0).getHeadCount());
		assertEquals("10", result.get("EF1").get("001EKY").get(0).getPlanType());

		assertEquals("001EKY", result.get("EF1").get("001EKY").get(1).getBenefitPlan());
		assertEquals("2", result.get("EF1").get("001EKY").get(1).getCovrgCode());
		assertEquals("EF1", result.get("EF1").get("001EKY").get(1).getGroupName());
		assertEquals(1, result.get("EF1").get("001EKY").get(1).getHeadCount());
		assertEquals("10", result.get("EF1").get("001EKY").get(1).getPlanType());

		assertEquals("002J24", result.get("UPP").get("002J24").get(0).getBenefitPlan());
		assertEquals("1", result.get("UPP").get("002J24").get(0).getCovrgCode());
		assertEquals("UPP", result.get("UPP").get("002J24").get(0).getGroupName());
		assertEquals(20, result.get("UPP").get("002J24").get(0).getHeadCount());
		assertEquals("14", result.get("UPP").get("002J24").get(0).getPlanType());
	}

	@Test
	public void getPlanHeadCountByGroupsTest() {
		
		Query mockedEeQuery = mock(Query.class);
		Query mockedQ = mock(Query.class);
		renewalDataDao.setEntityManager( this.entityManager );
		renewalDataDao.setHrpEntityManager( this.entityManager );

		when( renewalDataDao.getHrpEntityManager().createNamedQuery( "MDV_GROUP_HEADCOUNTS_UPD_BSSEE" )).thenReturn( mockedEeQuery );
		when( mockedEeQuery.setParameter( Mockito.anyString(), Mockito.any() )).thenReturn( mockedEeQuery );
		
		when( renewalDataDao.getEntityManager().createNamedQuery( "MDV_GROUP_HEADCOUNTS_UPDATED" )).thenReturn( mockedQ );
		when( mockedQ.setParameter( Mockito.anyString(), Mockito.any() )).thenReturn( mockedQ );

		when( renewalDataDao.getEntityManager().createNamedQuery( "MDV_ENROLLED_CENSUS_HEADCOUNTS" )).thenReturn( mockedQ );
		when( mockedQ.setParameter( Mockito.anyString(), Mockito.any() )).thenReturn( mockedQ );
		
		Long strategyId = 0L;
		Company company = new Company();
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId( 0L );
		company.setRealmPlanYearId( rpy.getId() );
		company.setRealmPlanYear( rpy );

		Date effDate = new Date();
		Map<String, PlanMapping> realmPlanMapping = new HashMap<String, PlanMapping>();
		Map<String, String> eeErPlanMapping = new HashMap<String, String>();
		eeErPlanMapping.put("001EKY", "001EFF");
		eeErPlanMapping.put("001EKT", "001EFG");
		Map<String, List<String>> benefitProgramPlanTypes = new HashMap<String, List<String>>();
		List<String> plans = new ArrayList<String>();
		plans.add("11");
		plans.add("14");
		plans.add("23");
		benefitProgramPlanTypes.put("UPP", plans);
		benefitProgramPlanTypes.put("EF1", plans);

		when( mockedEeQuery.getResultList() ).thenReturn( bssEmployeeMockData() );
		when( mockedQ.getResultList() ).thenReturn( groupHeadcountUpdatedMockData() );

		Map<String, List<BenefitPlanHeadCount>> actualResult = renewalDataDao.getPlanHeadCountByGroups(company, strategyId, effDate,
				realmPlanMapping, eeErPlanMapping, benefitProgramPlanTypes, false);

		// there should be two benefit programs
		assertEquals(2, actualResult.size());
		// the UPP benefit program should have three plans
		assertEquals(3, actualResult.get("UPP").size());
		// the second headcount of the second plan should be 2
		assertEquals(2, actualResult.get("UPP").get( 1 ).getCoverageLevelHeadCount().get( 1 ).getHeadCount() );

		actualResult = renewalDataDao.getPlanHeadCountByGroups(company, strategyId, effDate,
				realmPlanMapping, eeErPlanMapping, benefitProgramPlanTypes, true);

		// there should be two benefit programs
		assertEquals(2, actualResult.size());
		// the UPP benefit program should have three plans
		assertEquals(3, actualResult.get("UPP").size());
		// the second headcount of the second plan should be 2
		assertEquals(2, actualResult.get("UPP").get( 1 ).getCoverageLevelHeadCount().get( 1 ).getHeadCount() );

	}

	@Test
	public void getAdditionalPlansHeadCountByGroupTest() {
		String company = "G48";
		Date effDate = new Date();

		List<Object[]> mockedResult = prepareHeadCountByGroupsMockData();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);

		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> results = renewalDataDao
				.getAdditionalPlansHeadCountByGroup(company, effDate);

		assertEquals(2, results.size());
		assertEquals("002J40", results.get("EF1").get("002J40").get(0).getBenefitPlan());
		assertEquals("31", results.get("EF1").get("002J40").get(0).getPlanType());
		assertEquals("EF1", results.get("EF1").get("002J40").get(0).getGroupName());
		assertEquals(4, results.get("EF1").get("002J40").get(0).getHeadCount());

		assertEquals("000WAB", results.get("EF1").get("000WAB").get(0).getBenefitPlan());
		assertEquals("30", results.get("EF1").get("000WAB").get(0).getPlanType());
		assertEquals("EF1", results.get("EF1").get("000WAB").get(0).getGroupName());
		assertEquals(2, results.get("EF1").get("000WAB").get(0).getHeadCount());

		assertEquals("002KH3", results.get("UPP").get("002KH3").get(0).getBenefitPlan());
		assertEquals("27", results.get("UPP").get("002KH3").get(0).getPlanType());
		assertEquals("UPP", results.get("UPP").get("002KH3").get(0).getGroupName());
		assertEquals(1, results.get("UPP").get("002KH3").get(0).getHeadCount());

		assertEquals("002J3S", results.get("UPP").get("002J3S").get(0).getBenefitPlan());
		assertEquals("21", results.get("UPP").get("002J3S").get(0).getPlanType());
		assertEquals("UPP", results.get("UPP").get("002J3S").get(0).getGroupName());
		assertEquals(5, results.get("UPP").get("002J3S").get(0).getHeadCount());
	}

	@Test
	public void getBenefitProgramsTest() {
		String pfClient = "0488";
		Date currentPlanYearStartDate = new Date();

		List<Object[]> mockedResult = prepapreBenefitPrograms();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);

		List<BenefitGroup> results = renewalDataDao.getBenefitPrograms(pfClient, currentPlanYearStartDate);

		assertEquals(1, results.size());
		assertEquals("36E", results.get(0).getBenefitProgram());
		assertEquals("Compliance & Ethics Learn  PGM", results.get(0).getName());
		assertEquals("STD", results.get(0).getType());
	}

	@Test
	public void getHealthPlansForRenewalCompanyTest() {
		Date currentPlanYearStartDate = new Date();
		String pfClient = "";
		String CompanyCode = "G48";

		when(mockedQuery.getResultList()).thenReturn(prepareHealthProgramsAndPlansMockData());

		Map<String, Map<String, Map<String, BenefitPlan>>> actualResult = renewalDataDao
				.getHealthPlansForRenewalCompany(currentPlanYearStartDate, pfClient, CompanyCode);

		assertEquals(1, actualResult.size());
		assertEquals(1, actualResult.get("BENPROG1").get("10").size());
		assertEquals(1, actualResult.get("BENPROG1").get("11").size());
		assertEquals(1, actualResult.get("BENPROG1").get("14").size());
	}

	@Test
	public void getAdditionalBenefitPlansForRenewalCompanyTest() {
		Date currentPlanYearStartDate = new Date();
		String pfClient = "0488";
		String CompanyCode = "G48";

		List<Object[]> mockedResult = prepapreAdditionalBenefitPlans();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);

		Map<String, Map<String, Map<String, BenefitPlan>>> results = renewalDataDao
				.getAdditionalBenefitPlansForRenewalCompany(currentPlanYearStartDate, pfClient, CompanyCode);

		assertEquals(3, results.size());

		assertEquals(2, results.get("EF1").size());
		assertEquals(2, results.get("EF1").get("23").size());
		assertEquals(1, results.get("EF1").get("30").size());
		assertEquals(BigDecimal.valueOf(100), results.get("EF1").get("30").get("BENPLAN1").getEstimatedTotalCost());
		assertEquals(BigDecimal.valueOf(75), results.get("EF1").get("23").get("BENPLAN2").getEstimatedTotalCost());
		assertEquals(BigDecimal.valueOf(80), results.get("EF1").get("23").get("BENPLAN3").getEstimatedTotalCost());

		assertEquals(1, results.get("EF2").size());
		assertEquals(1, results.get("EF2").get("30").size());
		assertEquals(BigDecimal.valueOf(150), results.get("EF2").get("30").get("BENPLAN1").getEstimatedTotalCost());

		assertEquals(1, results.get("UPP").size());
		assertEquals(1, results.get("UPP").get("31").size());
		assertEquals(BigDecimal.valueOf(400), results.get("UPP").get("31").get("BENPLAN2").getEstimatedTotalCost());
	}

	@Test
	public void getActiveEligibleEECountTest() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setPlanYearStart(new Date());

		Company comp = new Company();
		comp.setRealmPlanYearId(REALM_PLYR_ID);
		comp.setRealmPlanYear(realmPlanYear);
		Realm realm = new Realm();
		realm.setBenExchange("TriNet II");
		comp.setRealm(realm);
		comp.setHeadQuatersState("FL");
		boolean history = false;
		List<Object[]> mockedResult = createActiveEligibleEECount();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);
		 when(RulesAndConfigsUtils.isDisabledBundledOn(prevRealmPlanYear.getId())).thenReturn(true);
		Map<String, ActiveEligibleEECount> results = renewalDataDao.getActiveEligibleEECount(comp, history, 0L,
				prevRealmPlanYear);
		
		assertEquals(3, results.size());
		assertEquals(3, results.get("001A0J").getPrimaryHeadCount());
		assertEquals(0, results.get("001A0J").getSecondaryHeadCount());
		assertEquals(0, results.get("001A0J").getTotalHeadCount());

		assertEquals(1, results.get("001A0K").getPrimaryHeadCount());
		assertEquals(0, results.get("001A0K").getSecondaryHeadCount());
		assertEquals(0, results.get("001A0K").getTotalHeadCount());

		assertEquals(1, results.get("001A0L").getPrimaryHeadCount());
		assertEquals(2, results.get("001A0L").getSecondaryHeadCount());
		assertEquals(0, results.get("001A0L").getTotalHeadCount());
		
		comp.setHeadQuatersState("NJ");
		results = renewalDataDao.getActiveEligibleEECount(comp, history, 0L, prevRealmPlanYear);
		
		assertEquals(3, results.size());
		assertEquals(0, results.get("001A0J").getPrimaryHeadCount());
		assertEquals(3, results.get("001A0J").getSecondaryHeadCount());
		assertEquals(0, results.get("001A0J").getTotalHeadCount());

		assertEquals(0, results.get("001A0K").getPrimaryHeadCount());
		assertEquals(1, results.get("001A0K").getSecondaryHeadCount());
		assertEquals(0, results.get("001A0K").getTotalHeadCount());

		assertEquals(2, results.get("001A0L").getPrimaryHeadCount());
		assertEquals(1, results.get("001A0L").getSecondaryHeadCount());
		assertEquals(0, results.get("001A0L").getTotalHeadCount());
		
		history = true;
		results = renewalDataDao.getActiveEligibleEECount(comp, history, 0L, prevRealmPlanYear);
		
		assertEquals(3, results.size());
		assertEquals(0, results.get("001A0J").getPrimaryHeadCount());
		assertEquals(3, results.get("001A0J").getSecondaryHeadCount());
		assertEquals(0, results.get("001A0J").getTotalHeadCount());

		assertEquals(0, results.get("001A0K").getPrimaryHeadCount());
		assertEquals(1, results.get("001A0K").getSecondaryHeadCount());
		assertEquals(0, results.get("001A0K").getTotalHeadCount());

		assertEquals(2, results.get("001A0L").getPrimaryHeadCount());
		assertEquals(1, results.get("001A0L").getSecondaryHeadCount());
		assertEquals(0, results.get("001A0L").getTotalHeadCount());
	}
	
	@Test
	public void getPrimaryEnrolledEECount() {
		Company comp = new Company();
		comp.setRealmPlanYearId(REALM_PLYR_ID);
		Realm realm = new Realm();
		realm.setBenExchange("TriNet II");
		comp.setRealm(realm);
		comp.setHeadQuatersState("FL");
		List<Object[]> mockedResult = prepareActiveEnrolledEECount();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);

		Map<String, Integer> results = renewalDataDao.getPrimaryEnrolledEECount(comp, true, 0L);
		assertEquals(2, results.size());
		assertEquals(Integer.valueOf(3), results.get("PROGRAM1"));
		assertEquals(Integer.valueOf(1), results.get("PROGRAM2"));
		
		results = renewalDataDao.getPrimaryEnrolledEECount(comp, false, 0L);
		assertEquals(2, results.size());
		assertEquals(Integer.valueOf(3), results.get("PROGRAM1"));
		assertEquals(Integer.valueOf(1), results.get("PROGRAM2"));
	}	

	@Test
	public void getEligRuleIdsByClient() {
		when(mockedQuery.getResultList()).thenReturn(prepareEligRuleIdsMockData());

		Map<String, String> actualResults = renewalDataDao.getEligRuleIdsByClient("9DDE", new Date());

		assertEquals(1, actualResults.size());
	}

	@Test
	public void getWaitPeriodByClient() {
		when(mockedQuery.getResultList()).thenReturn(prepareWaitPeriodMockData());

		Map<String, String> actualResults = renewalDataDao.getWaitPeriodByClient("9DDE", "G48", new Date());

		assertEquals(2, actualResults.size());
	}

	@Test
	public void getRenewalFundingDetails() {
		String company = "G48";
		Date effDate = new Date();

		when(mockedQuery.getResultList()).thenReturn(prepareRenewalFundingDetailsMockData());

		Map<String, Map<String, Map<String, Object>>> actualResult = renewalDataDao.getRenewalFundingDetails(company,
				effDate);

		assertEquals(2, actualResult.size());
		assertEquals(3, actualResult.get("EF1").size());
		assertEquals(3, actualResult.get("UPP").size());
	}

	@Test
	public void getRateTableIds() {
		when(mockedQuery.getResultList()).thenReturn(prepareRateTblIdMockData());

		Map<String, String> actualResults = renewalDataDao.getRateTableIds("PAS", new Date());

		assertEquals(2, actualResults.size());
	}

	@Test
	public void getBsuppVoluntaryPlanTypes() {
		when(mockedQuery.getResultList()).thenReturn(prepareBsuppVoluntaryPlanTypesMockData("PROGRAM1"));
		List<String> supportedVolTypes = new ArrayList<String>();
		
		supportedVolTypes.add(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);
		supportedVolTypes.add(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE);
		supportedVolTypes.add(BSSApplicationConstants.DENTAL_PLAN_TYPE);
		supportedVolTypes.add(BSSApplicationConstants.VISION_PLAN_TYPE);
		supportedVolTypes.add(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		List<String> actualResults = renewalDataDao.getBsuppVoluntaryPlanTypes("PROGRAM1", new Date(), true, supportedVolTypes);

		assertEquals(5, actualResults.size());
		assertEquals(BSSApplicationConstants.DENTAL_PLAN_TYPE, actualResults.get(1));
		assertEquals(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE, actualResults.get(2));
		assertEquals(BSSApplicationConstants.VISION_PLAN_TYPE, actualResults.get(3));
		assertEquals(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE, actualResults.get(4));

		actualResults = renewalDataDao.getBsuppVoluntaryPlanTypes("PROGRAM1", new Date(), false, supportedVolTypes);

		assertEquals(3, actualResults.size());
		assertEquals(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE, actualResults.get(1));
		assertEquals(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE, actualResults.get(2));

		when(mockedQuery.getResultList()).thenReturn(prepareBsuppVoluntaryPlanTypesMockData("PROGRAM2"));
		
		actualResults = renewalDataDao.getBsuppVoluntaryPlanTypes("PROGRAM2", new Date(), true, supportedVolTypes);

		assertEquals(2, actualResults.size());
		assertEquals(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE, actualResults.get(1));

		actualResults = renewalDataDao.getBsuppVoluntaryPlanTypes("PROGRAM2", new Date(), false, supportedVolTypes);

		assertEquals(2, actualResults.size());
		assertEquals(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE, actualResults.get(1));
	}
	
	@Test
	public void getPsHsaFundingDetails() {
		when(mockedQuery.getResultList()).thenReturn(prepareHsaFundingDetailsMockData("MONTHLY"));
		
		StrategyHsaFundingDto actualResults = renewalDataDao.getPsHsaFundingDetails(" ", new Date());

		assertEquals(BSSApplicationConstants.HSA_MONTHLY, actualResults.getContributionFrequency());
		assertEquals(new BigDecimal(50), actualResults.getMonthlyEeAmount());
		
		when(mockedQuery.getResultList()).thenReturn(prepareHsaFundingDetailsMockData("QUARTERLY"));
		
		actualResults = renewalDataDao.getPsHsaFundingDetails(" ", new Date());

		assertEquals(BSSApplicationConstants.HSA_QUARTERLY, actualResults.getLumpSumFrequency());
		assertEquals(new BigDecimal(500), actualResults.getQuarterlyEeAmount());
		
		when(mockedQuery.getResultList()).thenReturn(prepareHsaFundingDetailsMockData("ANNUAL"));
		
		actualResults = renewalDataDao.getPsHsaFundingDetails(" ", new Date());

		assertEquals(BSSApplicationConstants.HSA_ANNUAL, actualResults.getLumpSumFrequency());
		assertEquals(new BigDecimal(2000), actualResults.getAnnualEeAmount());
		
		when(mockedQuery.getResultList()).thenReturn(prepareHsaFundingDetailsMockData("NONE"));
		
		actualResults = renewalDataDao.getPsHsaFundingDetails(" ", new Date());

		assertEquals(null, actualResults);
	}

	private List<Object[]> prepareHeadCountMockData() {
		List<Object[]> data = new ArrayList<Object[]>();

		Object[] r = new Object[6];
		r[0] = "EF1";
		r[1] = "10";
		r[2] = "001EKY";
		r[3] = "1";
		r[4] = new BigDecimal(1);
		r[5] = BigDecimal.ZERO;
		data.add(r);
		r = new Object[6];
		r[0] = "EF1";
		r[1] = "10";
		r[2] = "001EKY";
		r[3] = "2";
		r[4] = new BigDecimal(1);
		r[5] = new BigDecimal(1);
		data.add(r);
		r = new Object[6];
		r[0] = "EF1";
		r[1] = "16";
		r[2] = "001EKY";
		r[3] = "5";
		r[4] = new BigDecimal(1);
		r[5] = BigDecimal.ZERO;
		data.add(r);
		r = new Object[6];
		r[0] = "UPP";
		r[1] = "14";
		r[2] = "002J24";
		r[3] = "1";
		r[4] = new BigDecimal(20);
		r[5] = BigDecimal.ZERO;
		data.add(r);
		return data;
	}

	private List<Object[]> prepareHeadCountByGroupsMockData() {
		List<Object[]> data = new ArrayList<Object[]>();

		Object[] r = new Object[5];
		r[0] = "EF1";
		r[1] = "30";
		r[2] = "000WAB";
		r[3] = new BigDecimal(2);
		data.add(r);
		r = new Object[5];
		r[0] = "EF1";
		r[1] = "31";
		r[2] = "002J40";
		r[3] = new BigDecimal(4);
		data.add(r);
		r = new Object[5];
		r[0] = "UPP";
		r[1] = "21";
		r[2] = "002J3S";
		r[3] = new BigDecimal(5);
		data.add(r);
		r = new Object[5];
		r[0] = "UPP";
		r[1] = "27";
		r[2] = "002KH3";
		r[3] = new BigDecimal(1);
		data.add(r);
		return data;
	}

	private List<Object[]> createActiveEligibleEECount() {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r = new Object[3];
		r[0] = "001A0J";
		r[1] = "OUT";
		r[2] = new BigDecimal(3);
		data.add(r);
		r = new Object[3];
		r[0] = "001A0K";
		r[1] = "OUT";
		r[2] = new BigDecimal(1);
		data.add(r);
		r = new Object[3];
		r[0] = "001A0L";
		r[1] = "OUT";
		r[2] = new BigDecimal(1);
		data.add(r);
		r = new Object[3];
		r[0] = "001A0L";
		r[1] = "IN";
		r[2] = new BigDecimal(2);
		data.add(r);
		return data;
	}

	private List<Object[]> prepareActiveEnrolledEECount() {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r = new Object[2];
		r[0] = "PROGRAM1";
		r[1] = new BigDecimal(3);
		data.add(r);
		r = new Object[2];
		r[0] = "PROGRAM2";
		r[1] = new BigDecimal(1);
		data.add(r);		
		return data;
	}

	private List<Object[]> prepapreBenefitPrograms() {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r = new Object[4];
		r[0] = "36E";
		r[1] = "Compliance & Ethics Learn  PGM";
		r[2] = "STD";
		r[3] = null;
		data.add(r);
		return data;
	}

	private List<Object[]> prepareHealthProgramsAndPlansMockData() {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r = new Object[8];
		r[0] = "BENPROG1";
		r[1] = "001EKY";
		r[2] = new Date();
		r[3] = "10";
		r[4] = "UHCAM";
		r[5] = "1";
		r[6] = BigDecimal.valueOf(865.04);
		r[7] = BigDecimal.valueOf(865.04);
		data.add(r);
		r = new Object[8];
		r[0] = "BENPROG1";
		r[1] = "000SR7";
		r[2] = new Date();
		r[3] = "11";
		r[4] = "METAM";
		r[5] = "C";
		r[6] = BigDecimal.valueOf(104.64);
		r[7] = BigDecimal.valueOf(865.04);
		data.add(r);
		r = new Object[8];
		r[0] = "BENPROG1";
		r[1] = "002J24";
		r[2] = new Date();
		r[3] = "14";
		r[4] = "VSPAM";
		r[5] = "2";
		r[6] = BigDecimal.valueOf(13.08);
		r[7] = BigDecimal.valueOf(865.04);
		data.add(r);
		r = new Object[8];
		r[0] = "BENPROG1";
		r[1] = "002J24";
		r[2] = new Date();
		r[3] = "14";
		r[4] = "VSPAM";
		r[5] = "1";
		r[6] = BigDecimal.valueOf(14.08);
		r[7] = BigDecimal.valueOf(890.04);
		data.add(r);
		return data;
	}

	private List<Object[]> prepapreAdditionalBenefitPlans() {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r = new Object[6];
		r[0] = "EF1";
		r[1] = "BENPLAN1";
		r[2] = new Date();
		r[3] = Constants.STD_CODE;
		r[4] = new BigDecimal(0);
		r[5] = new BigDecimal(100);
		data.add(r);
		r = new Object[6];
		r[0] = "EF1";
		r[1] = "BENPLAN2";
		r[2] = new Date();
		r[3] = Constants.LIFE_CODE;
		r[4] = new BigDecimal(50);
		r[5] = new BigDecimal(75);
		data.add(r);
		r = new Object[6];
		r[0] = "EF1";
		r[1] = "BENPLAN3";
		r[2] = new Date();
		r[3] = Constants.LIFE_CODE;
		r[4] = new BigDecimal(50);
		r[5] = new BigDecimal(80);
		data.add(r);
		r = new Object[6];
		r[0] = "UPP";
		r[1] = "BENPLAN2";
		r[2] = new Date();
		r[3] = Constants.LTD_CODE;
		r[4] = new BigDecimal(100);
		r[5] = new BigDecimal(400);
		data.add(r);
		r = new Object[6];
		r[0] = "EF2";
		r[1] = "BENPLAN1";
		r[2] = new Date();
		r[3] = Constants.STD_CODE;
		r[4] = new BigDecimal(200);
		r[5] = new BigDecimal(150);
		data.add(r);
		return data;
	}

	private List<Object[]> prepareEligRuleIdsMockData() {
		List<Object[]> data = new ArrayList<Object[]>();

		Object[] r = new Object[5];
		r[0] = "3FCY";
		r[1] = "4YV";
		data.add(r);
		return data;
	}

	private List<Object[]> prepareWaitPeriodMockData() {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r = new Object[2];
		r[0] = "F30D";
		r[1] = "001RS3";
		data.add(r);
		r = new Object[2];
		r[0] = "FDOH";
		r[1] = "EF1";
		data.add(r);
		return data;
	}

	private List<Object[]> prepareRenewalFundingDetailsMockData() {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r = new Object[47];
		r[0] = "G48";
		r[1] = "9ABK";
		r[2] = "UPP";
		r[3] = new Date();
		r[4] = "A";
		r[5] = BigDecimal.valueOf(0);
		r[6] = BigDecimal.valueOf(0);
		r[7] = BigDecimal.valueOf(0);
		r[8] = BigDecimal.valueOf(0);
		r[9] = "BFPCT";
		r[10] = BigDecimal.valueOf(100);  // waiver allowance
		r[11] = "001EKY";
		r[12] = BigDecimal.valueOf(100);
		r[13] = "Z";
		r[14] = BigDecimal.valueOf(0);
		r[15] = BigDecimal.valueOf(0);
		r[16] = BigDecimal.valueOf(0);
		r[17] = BigDecimal.valueOf(0);
		r[18] = BigDecimal.valueOf(0); // flat max ee
		r[19] = BigDecimal.valueOf(0); // flat max sp
		r[20] = BigDecimal.valueOf(0); // flat max ch
		r[21] = BigDecimal.valueOf(0); // flat max fam
		r[22] = "BFPCT";
		r[23] = "000SR7";
		r[24] = BigDecimal.valueOf(100);
		r[25] = "Z";
		r[26] = BigDecimal.valueOf(0);
		r[27] = BigDecimal.valueOf(0);
		r[28] = BigDecimal.valueOf(0);
		r[29] = BigDecimal.valueOf(0);
		r[30] = BigDecimal.valueOf(0); // flat max ee
		r[31] = BigDecimal.valueOf(0); // flat max sp
		r[32] = BigDecimal.valueOf(0); // flat max ch
		r[33] = BigDecimal.valueOf(0); // flat max fam
		r[34] = "BFPCT";
		r[35] = "002J24";
		r[36] = BigDecimal.valueOf(100);
		r[37] = "Z";
		r[38] = BigDecimal.valueOf(0);
		r[39] = BigDecimal.valueOf(0);
		r[40] = BigDecimal.valueOf(0);
		r[41] = BigDecimal.valueOf(0);
		r[42] = BigDecimal.valueOf(0); // flat max ee
		r[43] = BigDecimal.valueOf(0); // flat max sp
		r[44] = BigDecimal.valueOf(0); // flat max ch
		r[45] = BigDecimal.valueOf(0); // flat max fam
		r[46] = "F";
		data.add(r);
		r = new Object[47];
		r[0] = "G48";
		r[1] = "9ABK";
		r[2] = "EF1";
		r[3] = new Date();
		r[4] = "A";
		r[5] = BigDecimal.valueOf(0);
		r[6] = BigDecimal.valueOf(0);
		r[7] = BigDecimal.valueOf(0);
		r[8] = BigDecimal.valueOf(0);
		r[9] = "FLAT";
		r[10] = BigDecimal.valueOf(100);
		r[11] = "001EKY";
		r[12] = BigDecimal.valueOf(100);
		r[13] = "Z";
		r[14] = BigDecimal.valueOf(0);
		r[15] = BigDecimal.valueOf(0);
		r[16] = BigDecimal.valueOf(0);
		r[17] = BigDecimal.valueOf(0);
		r[18] = BigDecimal.valueOf(0);
		r[19] = BigDecimal.valueOf(0);
		r[20] = BigDecimal.valueOf(0);
		r[21] = BigDecimal.valueOf(0);
		r[22] = "FLAT";
		r[23] = "000SR7";
		r[24] = BigDecimal.valueOf(100);
		r[25] = "Z";
		r[26] = BigDecimal.valueOf(0);
		r[27] = BigDecimal.valueOf(0);
		r[28] = BigDecimal.valueOf(0);
		r[29] = BigDecimal.valueOf(0);
		r[30] = BigDecimal.valueOf(0);
		r[31] = BigDecimal.valueOf(0);
		r[32] = BigDecimal.valueOf(0);
		r[33] = BigDecimal.valueOf(0);
		r[34] = "FLAT";
		r[35] = "000SRB";
		r[36] = BigDecimal.valueOf(100);
		r[37] = "Z";
		r[38] = BigDecimal.valueOf(0);
		r[39] = BigDecimal.valueOf(0);
		r[40] = BigDecimal.valueOf(0);
		r[41] = BigDecimal.valueOf(0);
		r[42] = BigDecimal.valueOf(0);
		r[43] = BigDecimal.valueOf(0);
		r[44] = BigDecimal.valueOf(0);
		r[45] = BigDecimal.valueOf(0);
		r[46] = "F";
		data.add(r);
		r = new Object[47];
		r[0] = "G48";
		r[1] = "9ABK";
		r[2] = "EF1";
		r[3] = new Date();
		r[4] = "A";
		r[5] = BigDecimal.valueOf(0);
		r[6] = BigDecimal.valueOf(0);
		r[7] = BigDecimal.valueOf(0);
		r[8] = BigDecimal.valueOf(0);
		r[9] = "CFPCT";
		r[10] = BigDecimal.valueOf(100);
		r[11] = "001EKY";
		r[12] = BigDecimal.valueOf(100);
		r[13] = "Z";
		r[14] = BigDecimal.valueOf(0);
		r[15] = BigDecimal.valueOf(0);
		r[16] = BigDecimal.valueOf(0);
		r[17] = BigDecimal.valueOf(0);
		r[18] = BigDecimal.valueOf(0);
		r[19] = BigDecimal.valueOf(0);
		r[20] = BigDecimal.valueOf(0);
		r[21] = BigDecimal.valueOf(0);
		r[22] = "CFPCT";
		r[23] = "000SR8";
		r[24] = BigDecimal.valueOf(100);
		r[25] = "Z";
		r[26] = BigDecimal.valueOf(0);
		r[27] = BigDecimal.valueOf(0);
		r[28] = BigDecimal.valueOf(0);
		r[29] = BigDecimal.valueOf(0);
		r[30] = BigDecimal.valueOf(0);
		r[31] = BigDecimal.valueOf(0);
		r[32] = BigDecimal.valueOf(0);
		r[33] = BigDecimal.valueOf(0);
		r[34] = "CFPCT";
		r[35] = "000SR8";
		r[36] = BigDecimal.valueOf(100);
		r[37] = "Z";
		r[38] = BigDecimal.valueOf(0);
		r[39] = BigDecimal.valueOf(0);
		r[40] = BigDecimal.valueOf(0);
		r[41] = BigDecimal.valueOf(0);
		r[42] = BigDecimal.valueOf(0);
		r[43] = BigDecimal.valueOf(0);
		r[44] = BigDecimal.valueOf(0);
		r[45] = BigDecimal.valueOf(0);
		r[46] = "F";
		data.add(r);
		r = new Object[47];
		r[0] = "G48";
		r[1] = "9ABK";
		r[2] = "UPP";
		r[3] = new Date();
		r[4] = "A";
		r[5] = BigDecimal.valueOf(0);
		r[6] = BigDecimal.valueOf(0);
		r[7] = BigDecimal.valueOf(0);
		r[8] = BigDecimal.valueOf(0);
		r[9] = "BFPCT";
		r[10] = BigDecimal.valueOf(100);  // waiver allowance
		r[11] = "FLTMAX";
		r[12] = BigDecimal.valueOf(100);
		r[13] = "Z";
		r[14] = BigDecimal.valueOf(0);
		r[15] = BigDecimal.valueOf(0);
		r[16] = BigDecimal.valueOf(0);
		r[17] = BigDecimal.valueOf(0);
		r[18] = BigDecimal.valueOf(0); // flat max ee
		r[19] = BigDecimal.valueOf(0); // flat max sp
		r[20] = BigDecimal.valueOf(0); // flat max ch
		r[21] = BigDecimal.valueOf(0); // flat max fam
		r[22] = "BFPCT";
		r[23] = "FLTMAX";
		r[24] = BigDecimal.valueOf(100);
		r[25] = "Z";
		r[26] = BigDecimal.valueOf(0);
		r[27] = BigDecimal.valueOf(0);
		r[28] = BigDecimal.valueOf(0);
		r[29] = BigDecimal.valueOf(0);
		r[30] = BigDecimal.valueOf(0); // flat max ee
		r[31] = BigDecimal.valueOf(0); // flat max sp
		r[32] = BigDecimal.valueOf(0); // flat max ch
		r[33] = BigDecimal.valueOf(0); // flat max fam
		r[34] = "BFPCT";
		r[35] = "FLTMAX";
		r[36] = BigDecimal.valueOf(100);
		r[37] = "Z";
		r[38] = BigDecimal.valueOf(0);
		r[39] = BigDecimal.valueOf(0);
		r[40] = BigDecimal.valueOf(0);
		r[41] = BigDecimal.valueOf(0);
		r[42] = BigDecimal.valueOf(0); // flat max ee
		r[43] = BigDecimal.valueOf(0); // flat max sp
		r[44] = BigDecimal.valueOf(0); // flat max ch
		r[45] = BigDecimal.valueOf(0); // flat max fam
		r[46] = "F";
		data.add(r);
		r = new Object[47];
		r[0] = "G48";
		r[1] = "9ABK";
		r[2] = "UPP";
		r[3] = new Date();
		r[4] = "A";
		r[5] = BigDecimal.valueOf(0);
		r[6] = BigDecimal.valueOf(0);
		r[7] = BigDecimal.valueOf(0);
		r[8] = BigDecimal.valueOf(0);
		r[9] = "BFPCT";
		r[10] = BigDecimal.valueOf(100);  // waiver allowance
		r[11] = "FLTMAX";
		r[12] = BigDecimal.valueOf(100);
		r[13] = "1";
		r[14] = BigDecimal.valueOf(0);
		r[15] = BigDecimal.valueOf(0);
		r[16] = BigDecimal.valueOf(0);
		r[17] = BigDecimal.valueOf(0);
		r[18] = BigDecimal.valueOf(0); // flat max ee
		r[19] = BigDecimal.valueOf(0); // flat max sp
		r[20] = BigDecimal.valueOf(0); // flat max ch
		r[21] = BigDecimal.valueOf(0); // flat max fam
		r[22] = "BFPCT";
		r[23] = "FLTMAX";
		r[24] = BigDecimal.valueOf(100);
		r[25] = "1";
		r[26] = BigDecimal.valueOf(0);
		r[27] = BigDecimal.valueOf(0);
		r[28] = BigDecimal.valueOf(0);
		r[29] = BigDecimal.valueOf(0);
		r[30] = BigDecimal.valueOf(0); // flat max ee
		r[31] = BigDecimal.valueOf(0); // flat max sp
		r[32] = BigDecimal.valueOf(0); // flat max ch
		r[33] = BigDecimal.valueOf(0); // flat max fam
		r[34] = "BFPCT";
		r[35] = "FLTMAX";
		r[36] = BigDecimal.valueOf(100);
		r[37] = "1";
		r[38] = BigDecimal.valueOf(0);
		r[39] = BigDecimal.valueOf(0);
		r[40] = BigDecimal.valueOf(0);
		r[41] = BigDecimal.valueOf(0);
		r[42] = BigDecimal.valueOf(0); // flat max ee
		r[43] = BigDecimal.valueOf(0); // flat max sp
		r[44] = BigDecimal.valueOf(0); // flat max ch
		r[45] = BigDecimal.valueOf(0); // flat max fam
		r[46] = "F";
		data.add(r);
		r = new Object[47];
		r[0] = "G48";
		r[1] = "9ABK";
		r[2] = "UPP";
		r[3] = new Date();
		r[4] = "A";
		r[5] = BigDecimal.valueOf(0);
		r[6] = BigDecimal.valueOf(0);
		r[7] = BigDecimal.valueOf(0);
		r[8] = BigDecimal.valueOf(0);
		r[9] = "BSUPP";
		r[10] = BigDecimal.valueOf(100);  // waiver allowance
		r[11] = null;
		r[12] = BigDecimal.valueOf(100);
		r[13] = "1";
		r[14] = BigDecimal.valueOf(0);
		r[15] = BigDecimal.valueOf(0);
		r[16] = BigDecimal.valueOf(0);
		r[17] = BigDecimal.valueOf(0);
		r[18] = BigDecimal.valueOf(0); // flat max ee
		r[19] = BigDecimal.valueOf(0); // flat max sp
		r[20] = BigDecimal.valueOf(0); // flat max ch
		r[21] = BigDecimal.valueOf(0); // flat max fam
		r[22] = "BFPCT";
		r[23] = "FLTMAX";
		r[24] = BigDecimal.valueOf(100);
		r[25] = "1";
		r[26] = BigDecimal.valueOf(0);
		r[27] = BigDecimal.valueOf(0);
		r[28] = BigDecimal.valueOf(0);
		r[29] = BigDecimal.valueOf(0);
		r[30] = BigDecimal.valueOf(0); // flat max ee
		r[31] = BigDecimal.valueOf(0); // flat max sp
		r[32] = BigDecimal.valueOf(0); // flat max ch
		r[33] = BigDecimal.valueOf(0); // flat max fam
		r[34] = "BFPCT";
		r[35] = "FLTMAX";
		r[36] = BigDecimal.valueOf(100);
		r[37] = "1";
		r[38] = BigDecimal.valueOf(0);
		r[39] = BigDecimal.valueOf(0);
		r[40] = BigDecimal.valueOf(0);
		r[41] = BigDecimal.valueOf(0);
		r[42] = BigDecimal.valueOf(0); // flat max ee
		r[43] = BigDecimal.valueOf(0); // flat max sp
		r[44] = BigDecimal.valueOf(0); // flat max ch
		r[45] = BigDecimal.valueOf(0); // flat max fam
		r[46] = "F";
		data.add(r);
		return data;
	}

	private List<Object[]> prepareRateTblIdMockData() {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r = new Object[3];
		r[0] = "10";
		r[1] = "0ORPFX";
		r[2] = "16";
		data.add(r);
		r = new Object[3];
		r[0] = "15";
		r[1] = "0ORPFX";
		r[2] = "16";
		data.add(r);
		return data;
	}

	private List<Object[]> groupHeadcountUpdatedMockData() {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r = new Object[7];
		r[0] = "00001415472";
		r[1] = BigDecimal.valueOf( 1L );
		r[2] = "UPP";
		r[3] = "10";
		r[4] = "001EKY";
		r[5] = "4";
		r[6] = BigDecimal.valueOf( 1L );
		data.add(r);
		
		r = new Object[7];
		r[0] = "00001415472";
		r[1] = BigDecimal.valueOf( 1L );
		r[2] = "UPP";
		r[3] = "11";
		r[4] = "000SR7";
		r[5] = "4";
		r[6] = BigDecimal.ZERO;
		data.add(r);
		
		r = new Object[7];
		r[0] = "00001415472";
		r[1] = BigDecimal.valueOf( 1L );
		r[2] = "UPP";
		r[3] = "14";
		r[4] = "002J24";
		r[5] = "4";
		r[6] = BigDecimal.ZERO;
		data.add(r);

		r = new Object[7];
		r[0] = "00001529897";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "EF1";
		r[3] = "10";
		r[4] = "001EKY";
		r[5] = "2";
		r[6] = BigDecimal.ZERO;
		data.add(r);

		r = new Object[7];
		r[0] = "00001529897";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "EF1";
		r[3] = "11";
		r[4] = "000SR7";
		r[5] = "2";
		r[6] = BigDecimal.ZERO;
		data.add(r);

		r = new Object[7];
		r[0] = "00001529897";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "EF1";
		r[3] = "14";
		r[4] = "002J24";
		r[5] = "2";
		r[6] = BigDecimal.ZERO;
		data.add(r);

		r = new Object[7];
		r[0] = "00001529898";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "EF1";
		r[3] = "10";
		r[4] = "001EKY";
		r[5] = "2";
		r[6] = BigDecimal.ZERO;
		data.add(r);

		r = new Object[7];
		r[0] = "00001529898";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "EF1";
		r[3] = "11";
		r[4] = "000SR7";
		r[5] = "2";
		r[6] = BigDecimal.ZERO;
		data.add(r);

		r = new Object[7];
		r[0] = "00001529898";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "EF1";
		r[3] = "14";
		r[4] = "002J24";
		r[5] = "2";
		r[6] = BigDecimal.ZERO;
		data.add(r);

		r = new Object[7];
		r[0] = "00001529902";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "UPP";
		r[3] = "10";
		r[4] = "001EKY";
		r[5] = "4";
		r[6] = BigDecimal.valueOf( 1L );
		data.add(r);

		r = new Object[7];
		r[0] = "00001529902";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "UPP";
		r[3] = "11";
		r[4] = "000SR7";
		r[5] = "4";
		r[6] = BigDecimal.ZERO;
		data.add(r);

		r = new Object[7];
		r[0] = "00001529902";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "UPP";
		r[3] = "14";
		r[4] = "002J24";
		r[5] = "4";
		r[6] = BigDecimal.ZERO;
		data.add(r);

		r = new Object[7];
		r[0] = "00001529923";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "UPP";
		r[3] = "10";
		r[4] = "001EKY";
		r[5] = "2";
		r[6] = BigDecimal.ZERO;
		data.add(r);

		r = new Object[7];
		r[0] = "00001529923";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "UPP";
		r[3] = "11";
		r[4] = "000SR7";
		r[5] = "2";
		r[6] = BigDecimal.ZERO;
		data.add(r);

		r = new Object[7];
		r[0] = "00001529923";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "UPP";
		r[3] = "14";
		r[4] = "002J24";
		r[5] = "2";
		r[6] = BigDecimal.ZERO;
		data.add(r);

		return data;
	}

	private List<Object[]> bssEmployeeMockData() {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r = new Object[3];
		r[0] = "00001529902";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "UPP";
		data.add(r);

		r = new Object[3];
		r[0] = "00001529897";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "EF1";
		data.add(r);

		r = new Object[3];
		r[0] = "00001415472";
		r[1] = BigDecimal.valueOf( 1L );
		r[2] = "UPP";
		data.add(r);

		r = new Object[3];
		r[0] = "00001529923";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "UPP";
		data.add(r);

		r = new Object[3];
		r[0] = "00001529898";
		r[1] = BigDecimal.valueOf( 0L );
		r[2] = "EF1";
		data.add(r);

		return data;
	}
	
	private List<Object[]> prepareBsuppVoluntaryPlanTypesMockData(String benefitProgram) {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r;
		
		if ("PROGRAM1".equals(benefitProgram)) {
			r = new Object[2];
			r[0] = "PROGRAM1";
			r[1] = "10";
			data.add(r);

			r = new Object[2];
			r[0] = "PROGRAM1";
			r[1] = "11";
			data.add(r);

			r = new Object[2];
			r[0] = "PROGRAM1";
			r[1] = "14";
			data.add(r);
		}
		else if ("PROGRAM2".equals(benefitProgram)) {
			r = new Object[2];
			r[0] = "PROGRAM2";
			r[1] = "10";
			data.add(r);

			r = new Object[2];
			r[0] = "PROGRAM2";
			r[1] = "1V";
			data.add(r);
		}

		return data;
	}
	
	private List<Object[]> prepareHsaFundingDetailsMockData(String contributionTestType) {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r;
		
		if ("MONTHLY".equals(contributionTestType)) {
			r = new Object[13];
			r[0] = "Y";
			r[1] = "5";
			r[2] = " ";
			r[3] = BigDecimal.ZERO;
			r[4] = BigDecimal.ZERO;
			r[5] = " ";
			r[6] = " ";
			r[7] = " ";
			r[8] = " ";
			r[9] = " ";
			r[10] = new BigDecimal(50);
			r[11] = new BigDecimal(100);
			r[12] = BSSApplicationConstants.HSA_MONTHLY;
			data.add(r);
		}
		else if ("QUARTERLY".equals(contributionTestType)) {
			r = new Object[13];
			r[0] = "Y";
			r[1] = "6";
			r[2] = BSSApplicationConstants.HSA_QUARTERLY;
			r[3] = new BigDecimal(500);
			r[4] = new BigDecimal(1000);
			r[5] = " ";
			r[6] = "1";
			r[7] = "4";
			r[8] = "7";
			r[9] = "10";
			r[10] = BigDecimal.ZERO;
			r[11] = BigDecimal.ZERO;
			r[12] = " ";
			data.add(r);
		}
		else if ("ANNUAL".equals(contributionTestType)) {
			r = new Object[13];
			r[0] = "Y";
			r[1] = "6";
			r[2] = BSSApplicationConstants.HSA_ANNUAL;
			r[3] = new BigDecimal(2000);
			r[4] = new BigDecimal(4000);
			r[5] = "3";
			r[6] = " ";
			r[7] = " ";
			r[8] = " ";
			r[9] = " ";
			r[10] = BigDecimal.ZERO;
			r[11] = BigDecimal.ZERO;
			r[12] = " ";
			data.add(r);
		}
		else if ("NONE".equals(contributionTestType)) {

		}

		return data;
	}

}