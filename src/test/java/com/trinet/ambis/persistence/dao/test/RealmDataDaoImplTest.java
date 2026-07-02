package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.dao.hrp.impl.RealmDataDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmCloneProgram;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.planofferings.CarrierData;
import com.trinet.ambis.service.RealmPlanYearRuleConfigService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.FundingType;
import com.trinet.ambis.service.model.ProductQuarters;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;

/**
 * @author rvutukuri
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RealmDataDaoImplTest {
	
	RealmDataDaoImpl realmDataDao = new RealmDataDaoImpl();
	EntityManager entityManager = null;
	EntityManager psEm = null;
	Query mockedQuery = null;
	RealmPlanYearRuleConfigService realmPlanYearRuleConfigServiceMock = null;
	RealmPlanYearService realmPlanYearService = null;

    private MockedStatic<CommonUtils> commonUtilsMockedStatic;
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

    @Before
    public void setup() {
        entityManager = mock(EntityManager.class);
        psEm = mock(EntityManager.class);
        mockedQuery = mock(Query.class);
        realmDataDao.setEntityManager(entityManager);
        realmDataDao.setPsEm(psEm);
        when(mockedQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockedQuery);
        when(realmDataDao.getEntityManager().createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
        when(psEm.createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);

        realmPlanYearRuleConfigServiceMock = mock(RealmPlanYearRuleConfigService.class);
        realmPlanYearService = mock(RealmPlanYearService.class);
        RulesAndConfigsUtils.setRealmPlanYearRuleConfigService(realmPlanYearRuleConfigServiceMock);
        Map<String, String> rulesConfig = new HashMap<>();
        rulesConfig.put("SDI_STATES", "NJ,RI,CA,NY");

        commonUtilsMockedStatic = org.mockito.Mockito.mockStatic(CommonUtils.class);
        rulesAndConfigsUtilsMockedStatic = org.mockito.Mockito.mockStatic(RulesAndConfigsUtils.class);
    }

    @After
    public void tearDown() {
        commonUtilsMockedStatic.close();
        rulesAndConfigsUtilsMockedStatic.close();
    }

	@Test
	public void getCoverageCodesTest() {
		List<Object[]> results = getCoverageCode();
		when(mockedQuery.getResultList()).thenReturn(results);
		List<CoverageLevel> coverageLevels = realmDataDao.getCoverageCodes("10", 4);
		assertEquals(coverageLevels.get(0).getId(), "employee");
	}

	@Test
	public void getBenefitsPlans() {
		long realmPlanYearId = 10;
		Set<String> benefitPlans = new HashSet<String>();
		Collection<List<String>> plans = new ArrayList<List<String>>();
		plans.add(new ArrayList<>());

		when(CommonUtils.getBucketedList(anyList(), anyInt())).thenReturn(plans);
		when(mockedQuery.getResultList()).thenReturn(prepareBenPlansByRegionMockData());

		Map<String, List<String>> actualResult = realmDataDao.getBenefitsPlans(realmPlanYearId, benefitPlans);

		assertEquals(2, actualResult.size());
		assertEquals(1, actualResult.get("002J1L").size());
		assertEquals(3, actualResult.get("002J1M").size());
	}
	
	
	@Test
	public void getSelectedPlansByRegion() {
		long realmPlanYearId = 10;
		Set<String> benefitPlans = new HashSet<String>();
		Collection<List<String>> plans = new ArrayList<List<String>>();
		plans.add(new ArrayList<>());

		when(CommonUtils.getBucketedList(anyList(), anyInt())).thenReturn(plans);
		when(mockedQuery.getResultList()).thenReturn(prepareBenPlansByRegionMockData());

		Map<String, List<String>> actualResult = realmDataDao.getSelectedPlansByRegion(realmPlanYearId, benefitPlans);

		assertEquals(3, actualResult.size());
	}


	@Test
	public void getCoverageCodesByPlanTypes() {
		long realmPlanYearId = 10;
		List<String> planTypes = new ArrayList<String>();

		when(mockedQuery.getResultList()).thenReturn(prepareCovgCodeByAllPlanTypesMockData());

		Map<String, LinkedHashMap<String, Integer>> actualResult = realmDataDao.getCoverageCodesByPlanTypes(planTypes,
				realmPlanYearId);

		assertEquals(3, actualResult.get("medical").size());
		assertEquals(3, actualResult.get("dental").size());
		assertEquals(3, actualResult.get("vision").size());
	}

	@Test
	public void getCoverageCodesDescrByPlanTypes() {
		long realmPlanYearId = 10;
		List<String> planTypes = new ArrayList<String>();

		when(mockedQuery.getResultList()).thenReturn(prepareCovgCodeByAllPlanTypesMockData());

		Map<String, LinkedHashMap<String, String>> actualResult = realmDataDao
				.getCoverageCodesDescrByPlanTypes(planTypes, realmPlanYearId);

		assertEquals(3, actualResult.get("medical").size());
		assertEquals(3, actualResult.get("dental").size());
		assertEquals(3, actualResult.get("vision").size());
	}

	@Test
	public void getCoverageCodesDescByPlanTypes() {
		long realmPlanYearId = 10;
		List<String> planTypes = new ArrayList<String>();

		when(mockedQuery.getResultList()).thenReturn(prepareCovgCodeByAllPlanTypesMockData());

		Map<String, List<CoverageLevel>> actualResult = realmDataDao.getCoverageCodesDescByPlanTypes(planTypes,
				realmPlanYearId);

		assertEquals(3, actualResult.get("medical").size());
		assertEquals(3, actualResult.get("dental").size());
		assertEquals(3, actualResult.get("vision").size());
	}

	@Test
	public void getCompanyLocationStates() {
		List<String> mockData = new ArrayList<String>();
		mockData.add("CA");
		mockData.add("NJ");
		when(mockedQuery.getResultList()).thenReturn(mockData);
		Set<String> locationsOfOperations = realmDataDao.getCompanyLocationStates("G48");
		assertEquals(2, locationsOfOperations.size());
	}

	@Test
	public void getEmployeeHomeStates() {
		List<String> mockData = new ArrayList<String>();
		mockData.add("CA");
		mockData.add("NJ");
		when(mockedQuery.getResultList()).thenReturn(mockData);
		List<String> locationsOfOperations = realmDataDao.getEmployeeHomeStates("G48");
		assertEquals(2, locationsOfOperations.size());
	}

	@Test
	public void getBenefitPlansStates() {
		when(mockedQuery.getResultList()).thenReturn(prepareBenPlanStatesMockData());
		Map<String, List<String>> locationsOfOperations = realmDataDao.getBenefitPlansStates(10);
		assertEquals("HI", locationsOfOperations.get("000TF8").get(0));
		assertEquals("NV", locationsOfOperations.get("0011LH").get(0));
		assertEquals("CA", locationsOfOperations.get("0013HF").get(0));
		assertEquals("CA", locationsOfOperations.get("0013HG").get(0));
	}

	@Ignore
	@Test
	public void getAutoSelectPlansByRealmIdAndPlanTypes() {
		Set<String> outOfRegionPlans = new HashSet<String>();
		long realmYrId = 10L;
		Company company = new Company();
		Realm realm = new Realm();
		company.setRealm(realm);

		when(mockedQuery.getResultList()).thenReturn(prepareCrossRefPlanMockData());

		Map<String, Map<String, Set<String>>> actualResult = realmDataDao
				.getAutoSelectPlansByRealmIdAndPlanTypes(realmYrId, company, outOfRegionPlans);

		assertEquals(2, actualResult.size());
		assertEquals(2, actualResult.get("vision").size());
		assertEquals(2, actualResult.get("dental").size());
	}

	@Test
	public void getCoverageCodeMap() {
		when(mockedQuery.getResultList()).thenReturn(prepareCvrgCodeMockData());
		Map<String, String> actualResult = realmDataDao.getCoverageCodeMap();
		assertEquals("1", actualResult.get("employee"));
		assertEquals("2", actualResult.get("employeePlusSpouse"));
	}

	@Test
	public void getAdditionalBenefitsAllStatePlans() {
		long planYearId = 10;
		Set<String> regions = new HashSet<String>();
		Company company = new Company();

		when(mockedQuery.getResultList()).thenReturn(prepareBenPlansForAddtnalBenefitsMockData());

		Map<String, Set<StateBenefitPlan>> actualResult = realmDataDao.getAdditionalBenefitsAllStatePlans(planYearId,
				regions, company);

		assertEquals(1, actualResult.size());
		assertEquals(2, actualResult.get("23").size());
	}

	@Test
	public void getSelectedBenefitsTest() {
		List<Object[]> results = getSelectedBenefits();
		when(mockedQuery.getResultList()).thenReturn(results);
		Set<String> regions = new TreeSet<String>();
		regions.add("CA");
		regions.add("SF");
		Map<String, Boolean> selectedBenefits = realmDataDao.getSelectedBenefits(2, regions);
		assertEquals(selectedBenefits.get("medical"), true);
	}

	@Test
	public void getRealmCloneProgram() {
		when(mockedQuery.getResultList()).thenReturn(prepareRealmCloneProgData());
		RealmCloneProgram actualResult = realmDataDao.getRealmCloneProgram(21);
		assertEquals( CLONE_QTR, actualResult.getId().getOeQuarter() );
		assertEquals( CLONE_BENPRG, actualResult.getCloneProgram() );
		assertEquals( CLONE_K1BENPRG, actualResult.getCloneK1Program() );
		assertEquals( CLONE_COMPANY, actualResult.getCloneCompany() );
		assertEquals( CLONE_EFFDT, actualResult.getId().getEffdt() );
		assertEquals( CLONE_ENDDT, actualResult.getEnddt() );
	}

	@Test
	public void getRealmCloneProgramWithNull() {
		when( mockedQuery.getResultList() ).thenReturn( prepareRealmCloneProgDataWithNull() );
		RealmCloneProgram actualResult = realmDataDao.getRealmCloneProgram(21);
		assertEquals( CLONE_QTR, actualResult.getId().getOeQuarter() );
		assertEquals( CLONE_BENPRG, actualResult.getCloneProgram() );
		assertEquals( "", actualResult.getCloneK1Program() );
		assertEquals( CLONE_COMPANY, actualResult.getCloneCompany() );
		assertEquals( CLONE_EFFDT, actualResult.getId().getEffdt() );
		assertEquals( null, actualResult.getEnddt() );
	}

	@Test
	public void getPlanVendors() {
		Set<String> plans = new HashSet<String>();

		when(mockedQuery.getResultList()).thenReturn(preparePlanVendorsMockData());
		Map<String, String> actualResult = realmDataDao.getPlanVendors(plans, 10);
		assertEquals("TNPTO", actualResult.get("003S3L"));
		assertEquals("TNPTO", actualResult.get("003S3K"));
	}

	@Test
	public void isCommuterBenefitOffered() {
		when(mockedQuery.getSingleResult()).thenReturn("somestring");
		boolean actualResult = realmDataDao.isCommuterBenefitOffered(10);
		assertEquals(true, actualResult);
	}

	@Test
	public void getLifeSupplementalPlanTypes() {
		List<String> mockedData = new ArrayList<String>();
		mockedData.add("21");
		mockedData.add("25");
		when(mockedQuery.getResultList()).thenReturn(mockedData);
		List<String> actualResult = realmDataDao.getLifeSupplementalPlanTypes(10);
		assertEquals(2, actualResult.size());
		assertEquals("21", actualResult.get(0));
		assertEquals("25", actualResult.get(1));
	}

	@Test
	public void getRealmFundingTypes() {
		when(mockedQuery.getResultList()).thenReturn(prepareRealmFundingTypesMockedData());
		List<FundingType> actualResult = realmDataDao.getRealmFundingTypes(10);
		assertEquals(3, actualResult.size());
		assertEquals("Base Plan Percent", actualResult.get(0).getDescription());
		assertEquals("Covered Person Percent", actualResult.get(1).getDescription());
		assertEquals("Flat", actualResult.get(2).getDescription());
	}

	@Test
	public void getAllProductQuartersTest() {
		when(mockedQuery.getResultList()).thenReturn(prepareAllProductQuartesMockData());

		Map<String, ProductQuarters> actualResult = realmDataDao.getAllProductQuarters();

		assertEquals(2, actualResult.size());
		assertEquals(1, actualResult.get("Trinet IV").getQuarters().size());
		assertEquals(2, actualResult.get("Trinet III").getQuarters().size());
	}

	@Test
	public void getPortfilioDefaultPlans() {
		when(mockedQuery.getResultList()).thenReturn(preparePortfolioDefaultPlansMockedData());
		Map<String, Map<String, String>> actualResult = realmDataDao.getPortfilioDefaultPlans(10);
		assertEquals(2, actualResult.size());
		assertEquals("0038Q4", actualResult.get("11").get("1"));
		assertEquals("002ACW", actualResult.get("11").get("14"));
		assertEquals("0038QI", actualResult.get("14").get("15"));
	}

	@Test
	public void getDisabilityOptionPlans() {
		Long realmPlanYearId = 10L;
		String region = "NJ";
		String benExchange = "TriNet XI";
		
		Set<String> sidStates =  new HashSet<String>();

		when(mockedQuery.getResultList()).thenReturn(prepareDisabilityOptionPlansMockedData());

		Map<String, AdditionalBenefitPlan> actualResult = realmDataDao.getDisabilityOptionPlans(realmPlanYearId, region,
				benExchange, sidStates );

		assertEquals(2, actualResult.size());
		assertEquals("STD Premium & LTD Premium", actualResult.get("150").getDescription());
		assertEquals("STD Enhanced & LTD Premium", actualResult.get("151").getDescription());
	}

	@Test
	public void getRenewalFundingDetailsBSS() {
		when(mockedQuery.getResultList()).thenReturn(prepareRenewalFundingDetailsMockedData());

		Map<String, Map<String, Map<String, Object>>> actualResult = realmDataDao.getRenewalFundingDetailsBSS("G48",
				10);

		assertEquals(2, actualResult.size());
		assertEquals(2, actualResult.get("EF1").size());
		assertEquals(3, actualResult.get("001RS3").size());
	}

	@Test
	public void getADBenefitPlans() {
		Company company = new Company();
		List<String> mockedData = new ArrayList<String>();
		mockedData.add("000SRO");
		mockedData.add("000SRS");
		mockedData.add("000TM9");

		when(mockedQuery.getResultList()).thenReturn(mockedData);

		List<String> actualResult = realmDataDao.getADBenefitPlans(company);

		assertEquals(3, actualResult.size());
		assertTrue(actualResult.contains("000SRS"));
		assertTrue(actualResult.contains("000SRO"));
		assertTrue(actualResult.contains("000TM9"));
	}

	@Test
	public void validateRateTableId() {
		String rateTableId = "0ORPAF";
		String benefitProgram = "UPP";
		List<String> mockedData = new ArrayList<String>();
		mockedData.add("0ORPAF");

		when(mockedQuery.getResultList()).thenReturn(mockedData);

		boolean actualResult = realmDataDao.validateRateTableId(rateTableId, benefitProgram);

		assertFalse(actualResult);

		when(mockedQuery.getResultList()).thenReturn(Collections.emptyList());

		actualResult = realmDataDao.validateRateTableId(rateTableId, benefitProgram);

		assertTrue(actualResult);

		// If no rateTableId is passed, test should return "not valid"
		rateTableId = null;
		actualResult = realmDataDao.validateRateTableId(rateTableId, benefitProgram);
		assertFalse(actualResult);

	}

	@Test
	public void getAutoSelectedMedicalPlansByRegion() {
		Set<String> selectedPlans = new HashSet<String>();
		long realmPlanYearId = 10L;

		when(mockedQuery.getResultList()).thenReturn(prepareAutoSelectedPlanMockData());

		Map<String, Map<String, List<String>>> actualResult = realmDataDao.getAutoSelectedPlansByRegion(selectedPlans,
				realmPlanYearId);

		//assertEquals(2, actualResult.size());
		assertEquals(1, actualResult.get("10").get("CA-N").size());
		assertEquals(1, actualResult.get("11").get("CA-N").size());
	}
	
	@Test
	public void getAutoSelectedMedicalPlansForPassport() {

		Company company = new Company();
		Long strategyId = 1L;
		Set<String> selectedPlans = new HashSet<String>();
		
		when(mockedQuery.getResultList()).thenReturn(prepareAutoSelectedPassportPlanMockData());

		Set<String> actualResult = realmDataDao.getAutoSelectedMedicalPlansForPassport(company, strategyId,
				selectedPlans);
		assertEquals(3, actualResult.size());
	}

	@Test
	public void getRegionForSelectedPlans() {

		Set<String> selectedPlans = new HashSet<String>();
		selectedPlans.add("0013HF");
		long realmPlanYearId = 10L;
		when(mockedQuery.getResultList()).thenReturn(prepareAutoSelectedPlanMockData());

		Map<String, List<String>> actualResult = realmDataDao.getRegionForSelectedPlans(selectedPlans, realmPlanYearId);
		//assertEquals(2, actualResult.size());
		assertEquals(3, actualResult.get("CA-N").size());

	}

	@Test
	public void getSelectedBenefitsExceptVoluntary() {
		Set<String> regions = new HashSet<String>();
		long realmPlanYearId = 10;

		List<String> mockedData = new ArrayList<String>();
		mockedData.add("10");
		mockedData.add("11");

		when(mockedQuery.getResultList()).thenReturn(mockedData);

		List<String> actualResult = realmDataDao.getSelectedBenefitsExceptVoluntary(realmPlanYearId, regions);

		assertEquals(2, actualResult.size());
		assertTrue(actualResult.contains("medical"));
		assertTrue(actualResult.contains("dental"));
	}
	
	@Test
	public void getBSOutOfRegionPlans() {
		Company company = new Company();
		company.setZipCode("29715");
		RealmPlanYear ry = new RealmPlanYear();
		ry.setPlanYearStart(new Date());
		company.setRealmPlanYear(ry);
		List<String> mockedData = Arrays.asList("OOFFAA1", "OOFFAA2");

		when(mockedQuery.getResultList()).thenReturn(mockedData);

		Set<String> actualResult = realmDataDao.getBSOutOfRegionPlans(company);

		assertEquals(2, actualResult.size());
		assertTrue(actualResult.contains("OOFFAA1"));
		assertTrue(actualResult.contains("OOFFAA2"));
	}
	
	@Test
	public void getDisabilityOptionsForRealmPlanYears() {
		Company company = new Company();
		company.setZipCode("29715");
		RealmPlanYear ry = new RealmPlanYear();
		ry.setPlanYearStart(new Date());
		company.setRealmPlanYear(ry);
		

		when(mockedQuery.getResultList()).thenReturn(prepareDisabilityOptionsMockData());
		Map<String, AdditionalBenefitPlan> actualResult = realmDataDao.getDisabilityOptionsForRealmPlanYears(2, 1, "FL", false);

		assertEquals(1, actualResult.size());
		
	}
	
	@Test
	public void getCarrierOutOfRegionPlans() {
		Company company = new Company();
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		company.setRealmPlanYear(realmPlanYear);
		List<String> regions = Arrays.asList("CA", "MA");
		Set<String> medPlanGroups = new HashSet<>();
		Set<String> expectedResult = new HashSet<>(Arrays.asList("CA", "MA"));

		when(mockedQuery.getResultList()).thenReturn(regions);

		Set<String> actualResult = realmDataDao.getCarrierOutOfRegionPlans(company, medPlanGroups);

		assertEquals(expectedResult.size(), actualResult.size());
		assertTrue(actualResult.contains("CA"));
		assertTrue(actualResult.contains("MA"));
	}
	
	@Test
	public void getFundingPlanStatesTest() {

		Company company = new Company();
		company.setCode("TEST");
		company.setRealmPlanYearId(1);
		RealmPlanYear prevRealmPlanYear = new RealmPlanYear();
		prevRealmPlanYear.setPlanYearStart(new Date());

		Set<String> currentRegions = new HashSet<>(Arrays.asList("AL", "AZ"));
		List<String> fundingRegions = Arrays.asList("AL", "WI", "WY");
		Set<String> expectedResult = new HashSet<>(Arrays.asList("AL", "WI", "WY"));

		when(mockedQuery.getResultList()).thenReturn(fundingRegions);
//		when(realmPlanYearService.getPreviousRealmPlanYear(Mockito.any())).thenReturn(prevRealmPlanYear);
		Set<String> actualResult = realmDataDao.getFundingPlanStates(company, currentRegions, prevRealmPlanYear);

		assertEquals(expectedResult.size(), actualResult.size());
		assertTrue(actualResult.contains("AL"));
		assertTrue(actualResult.contains("WI"));
		assertTrue(actualResult.contains("WY"));
	}

	@Test
	public void getMedicalCarrierByTest() {
		String zipCode = "1001";
		long realmPlanYearId = 1;
		String state = "NY";
		String reportCode = "POEX";
		String planType = "10";

		when(mockedQuery.getResultList()).thenReturn(prepareCarriersByHQMockData());

		List<CarrierData> actualResult = realmDataDao.getCarriersBy(zipCode, realmPlanYearId, state,reportCode,planType);
		assertEquals("Aetna", actualResult.get(0).getCarrierName());
		assertEquals("Aig", actualResult.get(1).getCarrierName());
		assertEquals(1, actualResult.get(0).getCarrierId());
		assertEquals(2, actualResult.get(1).getCarrierId());

	}
	
	/**
	 * getMedicalCarrierByTest1 when report code is not passed
	 * 
	 * 
	 * @return
	 */
	
	@Test
	public void getMedicalCarrierByTest1() {
		String zipCode = "1001";
		long realmPlanYearId = 1;
		String state = "NY";
		String planType = "10";

		when(mockedQuery.getResultList()).thenReturn(prepareCarriersByHQMockData());

		List<CarrierData> actualResult = realmDataDao.getCarriersBy(zipCode, realmPlanYearId, state,null,planType);
		assertEquals("Aetna", actualResult.get(0).getCarrierName());
		assertEquals("Aig", actualResult.get(1).getCarrierName());
		assertEquals(1, actualResult.get(0).getCarrierId());
		assertEquals(2, actualResult.get(1).getCarrierId());

	}

	/**
	 * This method is for creating mock data for set of StateBenefitPlans
	 * 
	 * 
	 * @return
	 */
	public Set<StateBenefitPlan> getMockedListOfStateBenefitPlans() {
		StateBenefitPlan statebenefitPlan1 = new StateBenefitPlan();
		statebenefitPlan1.setBenefitPlan("002AHA");
		StateBenefitPlan statebenefitPlan2 = new StateBenefitPlan();
		statebenefitPlan2.setBenefitPlan("002AH9");
		Set<StateBenefitPlan> listOfStateBenefitPlans = new TreeSet<StateBenefitPlan>();
		listOfStateBenefitPlans.add(statebenefitPlan1);
		listOfStateBenefitPlans.add(statebenefitPlan2);
		return listOfStateBenefitPlans;
	}

	/**
	 * This method is for creating mock data for set of PlanSelections
	 * 
	 * 
	 * @return
	 */
	public List<PlanSelection> getMockedListOfPlanSelections() {
		PlanSelection planSelection1 = new PlanSelection();
		planSelection1.setBenefitPlan("002AHA");
		PlanSelection planSelection2 = new PlanSelection();
		planSelection1.setBenefitPlan("002AH9");
		List<PlanSelection> listOfPlanSelections = new ArrayList<PlanSelection>();
		listOfPlanSelections.add(planSelection1);
		listOfPlanSelections.add(planSelection2);
		return listOfPlanSelections;
	}

	/**
	 * This method is for creating mock data for getting benefit plans by regions
	 * query
	 * 
	 * 
	 * @return
	 */
	public List<Object[]> getAllBenefitPlansByRegionsMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] statesForBenefitPlans = new Object[2];
		statesForBenefitPlans[0] = "002AHA";
		statesForBenefitPlans[1] = "CA";
		Object[] statesForBenefitPlans2 = new Object[2];
		statesForBenefitPlans2[0] = "002AH9";
		statesForBenefitPlans2[1] = "CA";
		results.add(statesForBenefitPlans);
		results.add(statesForBenefitPlans2);

		return results;
	}

	/**
	 * This method is for creating coverage codes for planType and realm year id.
	 * 
	 * @return
	 */
	public List<Object[]> getCoverageCode() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] covrg = new Object[2];
		covrg[0] = "employee";
		covrg[1] = "Employee Only";
		Object[] covrg2 = new Object[2];
		covrg2[0] = "employeePlusSpouse";
		covrg2[1] = "Employee + Spouse";
		results.add(covrg);
		results.add(covrg2);
		return results;
	}

	/**
	 * This method is for creating locations of operations for a given Company Code
	 * 
	 * 
	 * @return
	 */
	public List<String> getLocationsOfOperations() {
		List<String> results = new ArrayList<String>();
		results.add("CA");
		results.add("NY");
		return results;
	}

	/**
	 * This method is for setting selected Benefits.
	 * 
	 * @return
	 */
	public List<Object[]> getSelectedBenefits() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] covrg = new Object[2];
		covrg[0] = "10";
		covrg[1] = new BigDecimal(1);
		Object[] covrg2 = new Object[2];
		covrg2[0] = "11";
		covrg2[1] = new BigDecimal(0);
		Object[] covrg3 = new Object[2];
		covrg3[0] = "14";
		covrg3[1] = new BigDecimal(0);
		results.add(covrg);
		results.add(covrg2);
		return results;
	}

	/**
	 * This method is for setting selected Benefits.
	 * 
	 * @return
	 */
	public List<Object[]> getVoluntaryPlans() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] vp = new Object[2];
		vp[0] = "30";
		vp[1] = "xyz123";
		Object[] vp1 = new Object[2];
		vp1[0] = "30";
		vp1[1] = "xyz321";
		Object[] vp2 = new Object[2];
		vp2[0] = "30";
		vp2[1] = "tyr123";
		results.add(vp);
		results.add(vp1);
		results.add(vp2);
		return results;
	}

	private List<Object[]> prepareBenPlansByRegionMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[2];
		r[0] = "002J1L";
		r[1] = "CA";
		results.add(r);
		r = new Object[2];
		r[0] = "002J1M";
		r[1] = "CA";
		results.add(r);
		r = new Object[2];
		r[0] = "002J1M";
		r[1] = "NJ";
		results.add(r);
		r = new Object[2];
		r[0] = "002J1M";
		r[1] = "NY";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareCovgCodeByAllPlanTypesMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[3];
		r[0] = "10";
		r[1] = "all";
		r[2] = "All Levels";
		results.add(r);
		r = new Object[3];
		r[0] = "10";
		r[1] = "employee";
		r[2] = "Employee Only";
		results.add(r);
		r = new Object[3];
		r[0] = "10";
		r[1] = "employeePlusSpouse";
		r[2] = "Employee + Spouse";
		results.add(r);
		r = new Object[3];
		r[0] = "11";
		r[1] = "all";
		r[2] = "All Levels";
		results.add(r);
		r = new Object[3];
		r[0] = "11";
		r[1] = "employee";
		r[2] = "Employee Only";
		results.add(r);
		r = new Object[3];
		r[0] = "11";
		r[1] = "employeePlusSpouse";
		r[2] = "Employee + Spouse";
		results.add(r);
		r = new Object[3];
		r[0] = "14";
		r[1] = "all";
		r[2] = "All Levels";
		results.add(r);
		r = new Object[3];
		r[0] = "14";
		r[1] = "employee";
		r[2] = "Employee Only";
		results.add(r);
		r = new Object[3];
		r[0] = "14";
		r[1] = "employeePlusSpouse";
		r[2] = "Employee + Spouse";
		results.add(r);
		r = new Object[3];
		return results;
	}

	private List<Object[]> prepareBenPlanStatesMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[2];
		r[0] = "000TF8";
		r[1] = "HI";
		results.add(r);
		r = new Object[2];
		r[0] = "0011LH";
		r[1] = "NV";
		results.add(r);
		r = new Object[2];
		r[0] = "0013HF";
		r[1] = "CA";
		results.add(r);
		r = new Object[2];
		r[0] = "0013HG";
		r[1] = "CA";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareCvrgCodeMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[2];
		r[0] = "1";
		r[1] = "employee";
		results.add(r);
		r = new Object[2];
		r[0] = "2";
		r[1] = "employeePlusSpouse";
		results.add(r);
		return results;
	}


	private static final String CLONE_QTR = "QQ";
	private static final String CLONE_BENPRG = "I'm a clone benprog";
	private static final String CLONE_K1BENPRG = "I'm a K1 clone";
	private static final String CLONE_COMPANY = "I'm a clone company";
	private static final java.sql.Date CLONE_EFFDT = java.sql.Date.valueOf( "1970-01-01" );
	private static final java.sql.Date CLONE_ENDDT = java.sql.Date.valueOf( "2099-12-31" );

	private List<Object[]> prepareRealmCloneProgData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[6];
		r[0] = CLONE_QTR;
		r[1] = CLONE_BENPRG;
		r[2] = CLONE_K1BENPRG;
		r[3] = CLONE_COMPANY;
		r[4] = new java.sql.Timestamp( CLONE_EFFDT.getTime() );
		r[5] = new java.sql.Timestamp( CLONE_ENDDT.getTime() );
		results.add(r);
		return results;
	}

	private List<Object[]> prepareRealmCloneProgDataWithNull() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[6];
		r[0] = CLONE_QTR;
		r[1] = CLONE_BENPRG;
		r[2] = null;
		r[3] = CLONE_COMPANY;
		r[4] = new java.sql.Timestamp( CLONE_EFFDT.getTime() );
		r[5] = null;
		results.add(r);
		return results;
	}

	private List<Object[]> preparePlanVendorsMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[2];
		r = new Object[2];
		r[0] = "003S3K";
		r[1] = "TNPTO";
		results.add(r);
		r = new Object[2];
		r[0] = "003S3L";
		r[1] = "TNPTO";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareRealmPlanMappingMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[8];
		r[0] = BigDecimal.valueOf(21);
		r[1] = "002AZ5";
		r[2] = BigDecimal.valueOf(12);
		r[3] = "FL Blue HDHP 2500 Central FL";
		r[4] = "10";
		r[5] = "003TQJ";
		r[6] = BigDecimal.valueOf(12);
		r[7] = "FL Blue HDHP 2700 Central FL";
		results.add(r);
		return results;

	}

	private List<Object[]> prepareRealmFundingTypesMockedData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[3];
		r[0] = "BFPCT";
		r[1] = "Base Plan Percent";
		r[2] = BigDecimal.valueOf(0);
		results.add(r);
		r = new Object[3];
		r[0] = "CFPCT";
		r[1] = "Covered Person Percent";
		r[2] = BigDecimal.valueOf(1);
		results.add(r);
		r = new Object[3];
		r[0] = "FLT";
		r[1] = "Flat";
		r[2] = BigDecimal.valueOf(0);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareAllProductQuartesMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[3];
		r[0] = "Trinet IV";
		r[1] = "8Y";
		results.add(r);
		r = new Object[3];
		r[0] = "Trinet III";
		r[1] = "Q1";
		results.add(r);
		r = new Object[3];
		r[0] = "Trinet III";
		r[1] = "Q2";
		results.add(r);
		return results;
	}

	private List<Object[]> preparePortfolioDefaultPlansMockedData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[3];
		r[0] = "1";
		r[1] = "0038Q4";
		r[2] = "11";
		results.add(r);
		r = new Object[3];
		r[0] = "14";
		r[1] = "002ACW";
		r[2] = "11";
		results.add(r);
		r = new Object[3];
		r[0] = "15";
		r[1] = "0038QI";
		r[2] = "14";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareDisabilityOptionPlansMockedData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[13];
		r[0] = BigDecimal.valueOf(150);
		r[1] = "STD Premium & LTD Premium";
		r[2] = "000TMD";
		r[3] = "30";
		r[4] = "Premium STD";
		r[5] = "STD3";
		r[6] = "NJ";
		r[7] = BigDecimal.valueOf(1);
		r[8] = BigDecimal.valueOf(0);
		r[9] = BigDecimal.valueOf(0);
		r[10] = BigDecimal.valueOf(0);
		r[11] = "STD";
		r[12] = BigDecimal.valueOf(10);
		results.add(r);
		r = new Object[13];
		r[0] = BigDecimal.valueOf(151);
		r[1] = "STD Enhanced & LTD Premium";
		r[2] = "000SRS";
		r[3] = "30";
		r[4] = "Enhanced STD";
		r[5] = "STD2";
		r[6] = "NJ";
		r[7] = BigDecimal.valueOf(1);
		r[8] = BigDecimal.valueOf(1);
		r[9] = BigDecimal.valueOf(0);
		r[10] = BigDecimal.valueOf(0);
		r[11] = "STD";
		r[12] = BigDecimal.valueOf(20);
		results.add(r);
		r = new Object[13];
		r[0] = BigDecimal.valueOf(156);
		r[1] = "LTD Premium";
		r[2] = "002J41";
		r[3] = "31";
		r[4] = "LTD Premium";
		r[5] = "LTDPREM";
		r[6] = "NJ";
		r[7] = "1";
		r[8] = "0";
		r[9] = "1";
		r[10] = BigDecimal.valueOf(0);
		r[11] = "STD";
		r[12] = BigDecimal.valueOf(30);
		return results;
	}

	private List<Object[]> prepareRenewalFundingDetailsMockedData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[10];
		r[0] = "BFPCT";
		r[1] = "10";
		r[2] = "001EKY";
		r[3] = "all";
		r[4] = BigDecimal.valueOf(100);
		r[5] = "001RS3";
		r[6] = "all";
		r[7] = BigDecimal.valueOf(100);
		r[8] = BigDecimal.valueOf(2000);
		r[9] = BigDecimal.valueOf(1);
		results.add(r);
		r = new Object[10];
		r[0] = "BFPCT";
		r[1] = "11";
		r[2] = "000SR7";
		r[3] = "all";
		r[4] = BigDecimal.valueOf(100);
		r[5] = "001RS3";
		r[6] = "all";
		r[7] = BigDecimal.valueOf(100);
		r[8] = null;
		r[9] = BigDecimal.valueOf(1);
		results.add(r);
		r = new Object[10];
		r[0] = "BFPCT";
		r[1] = "14";
		r[2] = "002J24";
		r[3] = "all";
		r[4] = BigDecimal.valueOf(100);
		r[5] = "001RS3";
		r[6] = "all";
		r[7] = BigDecimal.valueOf(100);
		r[8] = null;
		r[9] = BigDecimal.valueOf(1);
		results.add(r);
		r = new Object[10];
		r[0] = "CFPCT";
		r[1] = "10";
		r[2] = null;
		r[3] = "employeePlusSpouse";
		r[4] = BigDecimal.valueOf(100);
		r[5] = "EF1";
		r[6] = "all";
		r[7] = BigDecimal.valueOf(100);
		r[8] = BigDecimal.valueOf(2000);
		r[9] = BigDecimal.valueOf(1);
		results.add(r);
		r = new Object[10];
		r[0] = "CFPCT";
		r[1] = "10";
		r[2] = null;
		r[3] = "employeePlusFamily";
		r[4] = BigDecimal.valueOf(100);
		r[5] = "EF1";
		r[6] = "all";
		r[7] = BigDecimal.valueOf(100);
		r[8] = BigDecimal.valueOf(2000);
		r[9] = BigDecimal.valueOf(1);
		r = new Object[10];
		r[0] = "CFPCT";
		r[1] = "14";
		r[2] = null;
		r[3] = "employee";
		r[4] = BigDecimal.valueOf(100);
		r[5] = "EF1";
		r[6] = "all";
		r[7] = BigDecimal.valueOf(100);
		r[8] = null;
		r[9] = BigDecimal.valueOf(1);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareAutoSelectedPlanMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[3];
		r[0] = "0013HF";
		r[1] = "CA-N";
		r[2] = "10";
		results.add(r);
		r = new Object[3];
		r[0] = "0013HG";
		r[1] = "CA-N";
		r[2] = "11";
		results.add(r);
		r = new Object[3];
		r[0] = "0013HJ";
		r[1] = "CA-N";
		r[2] = "14";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareAutoSelectedPassportPlanMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[1];
		r[0] = "0013HF";
		results.add(r);
		r = new Object[1];
		r[0] = "0013HG";
		results.add(r);
		r = new Object[1];
		r[0] = "0013HJ";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareBenPlansForAddtnalBenefitsMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[10];
		r[0] = "000SR6";
		r[1] = "MetLife Standard";
		r[2] = "11";
		r[3] = "METAM";
		r[4] = null;
		r[5] = BigDecimal.valueOf(10);
		r[6] = BigDecimal.valueOf(1);
		results.add(r);
		r = new Object[10];
		r[0] = "000SRO";
		r[1] = "1X Earnings Basic Life & AD&D";
		r[2] = "23";
		r[3] = "METAM";
		r[4] = null;
		r[5] = BigDecimal.valueOf(10);
		r[6] = null;
		results.add(r);
		r = new Object[10];
		r[0] = "000TM9";
		r[1] = "Basic Life $50,000";
		r[2] = "23";
		r[3] = "METAM";
		r[4] = null;
		r[5] = BigDecimal.valueOf(10);
		r[6] = BigDecimal.valueOf(1);
		results.add(r);
		return results;
	}

	public List<Object[]> prepareVoluntaryPlansMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[10];
		r[0] = "30";
		r[1] = "002A5Z";
		results.add(r);
		r = new Object[10];
		r[0] = "30";
		r[1] = "002A60";
		results.add(r);
		r = new Object[10];
		r[0] = "31";
		r[1] = "002A6B";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareCrossRefPlanMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[10];
		r[0] = "Aetna Vision";
		r[1] = "002J1N";
		r[2] = BigDecimal.valueOf(305);
		r[3] = "vision";
		results.add(r);
		r = new Object[10];
		r[0] = "Aetna Vision";
		r[1] = "002J1N";
		r[2] = BigDecimal.valueOf(305);
		r[3] = "vision";
		results.add(r);
		r = new Object[10];
		r[0] = "Aetna Vision Plus";
		r[1] = "002J1O";
		r[2] = BigDecimal.valueOf(306);
		r[3] = "vision";
		r = new Object[10];
		r[0] = "Aetna Vision Plus";
		r[1] = "002J1O";
		r[2] = BigDecimal.valueOf(306);
		r[3] = "vision";
		results.add(r);
		r = new Object[10];
		r[0] = "Delta Dental Standard";
		r[1] = "002R4P";
		r[2] = BigDecimal.valueOf(303);
		r[3] = "dental";
		results.add(r);
		r = new Object[10];
		r[0] = "Delta Dental Standard";
		r[1] = "002R4P";
		r[2] = BigDecimal.valueOf(303);
		r[3] = "dental";
		results.add(r);
		r = new Object[10];
		r[0] = "Delta Dental Enhanced";
		r[1] = "002R4Q";
		r[2] = BigDecimal.valueOf(301);
		r[3] = "dental";
		results.add(r);
		r = new Object[10];
		r[0] = "Delta Dental Enhanced";
		r[1] = "002R4Q";
		r[2] = BigDecimal.valueOf(301);
		r[3] = "dental";
		results.add(r);
		return results;
	}
	
	private List<Object[]> prepareDisabilityOptionsMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[10];
		r[0] = "OPTION 1";
		r[1] = "OPTPLAN1";
		r[2] = "31";
		r[3] = "OPTION 1 PLAN DESCRIPTION";
		r[4] = BigDecimal.valueOf(1);
		results.add(r);
		
		r = new Object[10];
		r[0] = "OPTION 1";
		r[1] = "OPTPLAN2";
		r[2] = "31";
		r[3] = "OPTION 2 PLAN DESCRIPTION";
		r[4] = BigDecimal.valueOf(0);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareCarriersByHQMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[4];
		r[0] = BigDecimal.valueOf(74);
		r[1] = BigDecimal.valueOf(1);
		r[2] = "Aetna";
		r[3] = BigDecimal.valueOf(10);
		results.add(r);

		r = new Object[4];
		r[0] = BigDecimal.valueOf(34);
		r[1] = BigDecimal.valueOf(2);
		r[2] = "Aig";
		r[3] = BigDecimal.valueOf(11);
		results.add(r);
		return results;
	}

}