/**
 * 
 */
package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.enums.StrategyTypesEnums;
import com.trinet.ambis.helper.ModelCompareExportHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupPlanSelectDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyHsaFundingDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyHsaFunding;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.DisabilityOptionService;
import com.trinet.ambis.service.EmployeeDataService;
import com.trinet.ambis.service.ProspectStrategyService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.impl.ModelCompareServiceImpl;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.BenefitOfferFunding;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.GroupFunding;
import com.trinet.ambis.service.model.ModelComparePlanTypeCost;
import com.trinet.ambis.service.model.ModelCompareStrategy;
import com.trinet.ambis.service.model.ModelCompareStrategyCost;
import com.trinet.ambis.service.model.StrategyGroupDetails;
import com.trinet.ambis.util.Constants;

/**
 * @author hliddle
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ModelCompareServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	ModelCompareServiceImpl modelCompareService;
	
	@Mock
	StrategyDao strategyDao;

	@Mock
	StrategyDataDao strategyDataDao;

	@Mock
	StrategyFundingDataDao strategyFundingDataDao;
	
	@Mock
	StrategyHsaFundingDao strategyHsaFundingDao;

	@Mock
	RealmPlanYearService realmPlanYearService;

	@Mock
	CompanyService companyService;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	StrategyGroupPlanSelectDao strategyGroupPlanSelectDao;

	@Mock
	EmployeeDataService employeeDataService;
	
	@Mock
	DisabilityOptionService disabilityOptionService;

	@Mock
	ProspectStrategyService prospectStrategyService;
	
	@Mock
	EmployeeBenefitGroupDao employeeBenefitGroupDao;

	private MockedStatic<ModelCompareExportHelper> mockStaticModelCompareExportHelper;

	private static final String PLAN_START_DATE = "10-JAN-2018";
	private static final String QUARTER = "IV";
	private static final String HEAD_QTR_STATE = "FL";
	private static final long COMPANY_ID = 9999;
	private static final String COMPANY_NAME = "Trinet Group";
	private static final String COMPANY_CODE = "5R9";
	private static final String PROSPECT_COMPANY_CODE = "PROSPECT";
	private static final String KAISER_BAND_CODE = "KBC";
	private static final boolean IS_PAYROLL_PROCESSED_TRUE = true;
	private static final boolean TRANSITION_PERIOD = true;
	private static final Long STRATEGY_1 = 1000L;
	private static final Long STRATEGY_2 = 1001L;
	private static final Long STRATEGY_3 = 1002L;

	@Before
	public void setup() {
		if (mockStaticModelCompareExportHelper == null) {
			mockStaticModelCompareExportHelper = Mockito.mockStatic(ModelCompareExportHelper.class);
		}
	}

	@After
	public void tearDown() {
		if (mockStaticModelCompareExportHelper != null) {
			mockStaticModelCompareExportHelper.close();
			mockStaticModelCompareExportHelper = null;
		}
	}

	@Test
	public void getMCStrategiesForNewCompanyTest() {

		List<ModelCompareStrategy> currentStrategies;
		List<ModelCompareStrategy> actualMCList;
		Company company = prepareCompany(COMPANY_ID, COMPANY_NAME, COMPANY_CODE, PLAN_START_DATE, QUARTER,
				IS_PAYROLL_PROCESSED_TRUE, TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);

		/*
		 * GIVEN Test when we have one submitted strategy and one not submitted
		 */
		currentStrategies = new ArrayList<>();
		currentStrategies
				.add(prepareModelCompareStrategy(1000, "TEST_SUBMITTED_STRATEGY", true, true));
		currentStrategies
				.add(prepareModelCompareStrategy(1001, "TEST_NOT_SUBMITTED_STRATEGY", false, false));
		Mockito.when(strategyDataDao.getModelCompareStrategies(company.getId())).thenReturn(currentStrategies);

		// when
		actualMCList = modelCompareService.getMCStrategies(company,false);

		// then
		assertEquals(2, actualMCList.size());
		for (ModelCompareStrategy mcs : actualMCList) {
			if (1000 == mcs.getId()) {
				assertTrue("isActiveStrategy should be true", mcs.isActiveStrategy());
			} else {
				assertFalse("isActiveStrategy should be false", mcs.isActiveStrategy());
			}
		}

		/*
		 * GIVEN Test when we have two strategies and neither is submitted - The
		 * "selected" strategy should be set as active
		 */
		currentStrategies = new ArrayList<>();
		currentStrategies.add(
				prepareModelCompareStrategy(1000, "TEST_NOT_SUBMITTED_STRATEGY", false, false));
		currentStrategies.add(prepareModelCompareStrategy(1001, "TEST_NOT_SUBMITTED_SELECTED_STRATEGY", false, true));
		Mockito.when(strategyDataDao.getModelCompareStrategies(company.getId())).thenReturn(currentStrategies);

		// when
		actualMCList = modelCompareService.getMCStrategies(company,false);

		// then
		assertEquals(2, actualMCList.size());
		for (ModelCompareStrategy mcs : actualMCList) {
			if (1000 == mcs.getId()) {
				assertFalse("isActiveStrategy should be false", mcs.isActiveStrategy());
			} else {
				assertTrue("isActiveStrategy should be true", mcs.isActiveStrategy());
			}
		}

		/*
		 * GIVEN Test when we have two strategies and neither is submitted or
		 * "selected"
		 */
		currentStrategies = new ArrayList<>();
		currentStrategies.add(
				prepareModelCompareStrategy(1000, "TEST_NOT_SUBMITTED_STRATEGY", false, false));
		currentStrategies
				.add(prepareModelCompareStrategy(1001, "TEST_NOT_SUBMITTED_STRATEGY_2", false, false));
		Mockito.when(strategyDataDao.getModelCompareStrategies(company.getId())).thenReturn(currentStrategies);

		// when
		actualMCList = modelCompareService.getMCStrategies(company,false);

		// then
		assertEquals(2, actualMCList.size());
		for (ModelCompareStrategy mcs : actualMCList) {
			assertFalse("isActiveStrategy should be false", mcs.isActiveStrategy());
		}
	}

	@Test
	public void getMCStrategiesForRenewalCompanyTest() {

		long prevRealmPlanYear = 4;
		long currentRealmPlanYear = 9;

		List<ModelCompareStrategy> strategies;
		List<ModelCompareStrategy> actualMCList;
		RealmPlanYear prevRealmYear = new RealmPlanYear();
		prevRealmYear.setId(prevRealmPlanYear);

		Company company = prepareCompany(COMPANY_ID, COMPANY_NAME, COMPANY_CODE, PLAN_START_DATE, QUARTER,
				IS_PAYROLL_PROCESSED_TRUE, TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		company.setRenewalCompany(true);
		company.setRealmPlanYearId(currentRealmPlanYear);

		/*
		 * GIVEN Test the previousModel should be set as active. All others
		 * should not be active
		 */
		strategies = new ArrayList<>();
		strategies.add(prepareModelCompareStrategy(0, "TEST_PREV_YEAR_STRATEGY", true,
				true));
		strategies
				.add(prepareModelCompareStrategy(1000, "TEST_SUBMITTED_STRATEGY", true, false));
		strategies
				.add(prepareModelCompareStrategy(1001, "TEST_NOT_SUBMITTED_STRATEGY", false, false));
		
		Mockito.when(strategyDataDao.getModelCompareStrategies(company.getId())).thenReturn(strategies);

		// when
		actualMCList = modelCompareService.getMCStrategies(company,false);

		// then
		assertEquals(3, actualMCList.size());
		for (ModelCompareStrategy mcs : actualMCList) {
			if (0 == mcs.getId()) {
				assertTrue("isActiveStrategy should be true", mcs.isActiveStrategy());
			} else {
				assertFalse("isActiveStrategy should be false", mcs.isActiveStrategy());
			}
		}
	}

	@Test
	public void getMCStrategiesForProspectTest() {

		long prevRealmPlanYear = 4;
		long currentRealmPlanYear = 9;

		List<ModelCompareStrategy> strategies = null;
		List<ModelCompareStrategy> actualMCList = null;

		Company company = prepareCompany(COMPANY_ID, COMPANY_NAME, "PROSPECT", PLAN_START_DATE, QUARTER,
				IS_PAYROLL_PROCESSED_TRUE, TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		company.setRenewalCompany(true);
		company.setRealmPlanYearId(currentRealmPlanYear);
		company.setProspectCompany(true);

		/*
		 * GIVEN Test the previousModel should be set as active. All others
		 * should not be active
		 */
		strategies = new ArrayList<>();
		strategies
				.add(prepareModelCompareStrategy(1000, StrategyTypesEnums.F_S.getName(), true, false));
		strategies
				.add(prepareModelCompareStrategy(1001, "Custom Strategy", false, false));

		Mockito.when(strategyDataDao.getModelCompareStrategies(company.getId())).thenReturn(strategies);

		// when
		actualMCList = modelCompareService.getMCStrategies(company,false);

		// then
		assertEquals(3, actualMCList.size());
		verify(realmPlanYearService, times(0)).getPreviousRealmPlanYear(any(String.class), any(Long.class));

		for (ModelCompareStrategy mcs : actualMCList) {
			if (0 == mcs.getId()) {
				assertEquals("Prospect strategy has the incorrect name", ProspectConstants.PROSPECT_STRATEGY_NAME,
						mcs.getName());
			}
		}
	}

	/* For Prospect Trinet Stratergies */
	@Test
	public void getMCStrategiesForProspectTest1() {
		long currentRealmPlanYear = 9;

		List<ModelCompareStrategy> strategies = null;
		List<ModelCompareStrategy> actualMCList = null;

		Company company = prepareCompany(COMPANY_ID, COMPANY_NAME, "PROSPECT", PLAN_START_DATE, QUARTER,
				IS_PAYROLL_PROCESSED_TRUE, TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		company.setRenewalCompany(true);
		company.setRealmPlanYearId(currentRealmPlanYear);
		company.setProspectCompany(true);

		/*
		 * GIVEN Test the previousModel should be set as active. All others should not
		 * be active
		 */
		strategies = new ArrayList<>();
		strategies.add(prepareModelCompareStrategy(1000, StrategyTypesEnums.F_S.getName(), true, false));
		strategies.add(prepareModelCompareStrategy(1001, "Custom Strategy", false, false));

		Mockito.when(strategyDataDao.getModelCompareStrategies(company.getId())).thenReturn(strategies);

		// when
		actualMCList = modelCompareService.getMCStrategies(company,false);

		// then
		assertEquals(3, actualMCList.size());
		assertEquals(ProspectConstants.PROSPECT_STRATEGY_NAME, actualMCList.get(0).getName());
		assertEquals("Current Strategy", actualMCList.get(1).getName());
		assertEquals("Custom Strategy", actualMCList.get(2).getName());
		verify(realmPlanYearService, times(0)).getPreviousRealmPlanYear(any(String.class), any(Long.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getMCStrategyGroupFundingTest() {

		long strategyId = 1000;
		String companyCode = "G48";
		int realmPlanYearId = 1234;

		ModelCompareStrategy actualStrategy;

		Company company = new Company();
		company.setId(COMPANY_ID);
		company.setCode(companyCode);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(realmPlanYearId);
		realmPlanYear.setPlanYearEnd(new Date());
		company.setRealmPlanYear(realmPlanYear);
		
		Strategy strategy = new Strategy();
		strategy.setCompanyId(COMPANY_ID);
		
		AdditionalBenefitPlan adPlan = new AdditionalBenefitPlan();
		adPlan.setDescription("AD PLAN");

		/*
		 * GIVEN
		 */

		List<PlanSelection> planSelections = new ArrayList<>();
		planSelections.add(preparePlanSelection(37351, Constants.MEDICAL_CODE));
		planSelections.add(preparePlanSelection(37351, Constants.VISION_CODE));
		planSelections.add(preparePlanSelection(37351, Constants.DENTAL_CODE));
		planSelections.add(preparePlanSelection(37352, Constants.MEDICAL_CODE));
		
		ModelCompareStrategy modelCompareStrategy = new ModelCompareStrategy();
		List<Object[]> data = new ArrayList<>();
		Object[] obj = { 37351, "Group 1", "1st of month on/after DOH", Constants.MEDICAL_CODE, null, null,
				"employee", new BigDecimal(100), "Strategy 1", "Employee" };
		data.add(obj);
		Object[] obj1 = { 37351, "Group 1", "1st of month on/after DOH", Constants.DENTAL_CODE, null, null,
				"employeePlusSpouse", new BigDecimal(200), "Strategy 1", "Employee + Spouse" };
		data.add(obj1);
		Object[] obj2 = { 37352, "Group 2", "1st of month on/after DOH", null, null, null, null, null,
				"Strategy 1", null };
		data.add(obj2);
		setStrategyDetails(modelCompareStrategy, data);
		Map<Long, ModelCompareStrategy> modelCompareStrategyMap = new HashMap<>();
		modelCompareStrategyMap.put(strategyId, modelCompareStrategy);

		when(strategyDao.findById(strategyId)).thenReturn(strategy);

		when(strategyFundingDataDao.getFundingDetailsByStrategyId(Arrays.asList(strategyId), company, false, company.getRealmPlanYear().getPlanYearEnd())).thenReturn(modelCompareStrategyMap);
		when(strategyHsaFundingDao.findByStrategyId(strategyId)).thenReturn(prepareStrategyHsaFunding(strategyId));
		when(strategyGroupPlanSelectDao.findDistinctGroupIdPlanTypeByStrategyId(strategyId)).thenReturn(planSelections);
		
//		when(strategyDataDao.getAdditionalBenefitPlansForStrategy(Mockito.anyLong(), Mockito.anyString())).thenReturn(prepareStrategyGroupAdditionalOfferingMap());
		
//		when(disabilityOptionService.getDisabilityOptionByPlans(ArgumentMatchers.anyList(), ArgumentMatchers.any(Company.class), ArgumentMatchers.anyBoolean())).thenReturn(adPlan);

//		when(companyService.getCompanyDetails(companyCode)).thenReturn(company);

		when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes, realmPlanYearId))
				.thenReturn(prepareCoverageLevelMap());

		// when
		actualStrategy = modelCompareService.getMCStrategyGroupFunding(strategyId, company);

		// then
		assertEquals(2, actualStrategy.getGroupFundingList().size());
		assertEquals("Strategy 1", actualStrategy.getName());
		assertEquals("Group 1", actualStrategy.getGroupFundingList().get(0).getName());
		assertEquals("Group 2", actualStrategy.getGroupFundingList().get(1).getName());
		assertEquals(3, actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().size());
		assertEquals(3, actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().size());
		assertTrue(actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().keySet()
				.containsAll(Arrays.asList(Constants.MEDICAL, Constants.DENTAL, Constants.VISION)));
		assertTrue(actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().keySet()
				.containsAll(Arrays.asList(Constants.MEDICAL, Constants.DENTAL, Constants.VISION)));
		assertTrue(actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().get(Constants.MEDICAL).isOffered());
		assertTrue(actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().get(Constants.DENTAL).isOffered());
		assertTrue(actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().get(Constants.VISION).isOffered());
		assertTrue(actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().get(Constants.MEDICAL).isOffered());
		assertFalse(actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().get(Constants.DENTAL).isOffered());
		assertFalse(actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().get(Constants.VISION).isOffered());
		assertEquals(1, actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().get(Constants.MEDICAL)
				.getCoverageLevels().size());
		assertEquals(1, actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().get(Constants.DENTAL)
				.getCoverageLevels().size());
		assertEquals(2, actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().get(Constants.VISION)
				.getCoverageLevels().size());
		assertEquals(new BigDecimal(100), actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding()
				.get(Constants.MEDICAL).getCoverageLevels().get(0).getContribution());
		assertEquals(new BigDecimal(200), actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding()
				.get(Constants.DENTAL).getCoverageLevels().get(0).getContribution());
		assertEquals(2, actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().get(Constants.MEDICAL)
				.getCoverageLevels().size());
		assertEquals(2, actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().get(Constants.DENTAL)
				.getCoverageLevels().size());
		assertEquals(2, actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().get(Constants.VISION)
				.getCoverageLevels().size());
		assertFalse(actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().get(Constants.MEDICAL)
				.getCoverageLevels().contains("all"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getMCStrategyGroupFundingForProspectTest() {

		long strategyId = 2222;
		String companyCode = "PROSPECT-COMPANY";
		int realmPlanYearId = 1234;

		ModelCompareStrategy actualStrategy;

		Company company = new Company();
		company.setId(COMPANY_ID);
		company.setCode(companyCode);
		company.setProspectCompany(true);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(realmPlanYearId);
		realmPlanYear.setPlanYearEnd(new Date());
		company.setRealmPlanYear(realmPlanYear);

		Strategy strategy = new Strategy();
		strategy.setCompanyId(COMPANY_ID);

		AdditionalBenefitPlan adPlan = new AdditionalBenefitPlan();
		adPlan.setDescription("AD PLAN");

		/*
		 * GIVEN
		 */

		List<PlanSelection> planSelections = new ArrayList<>();
		planSelections.add(preparePlanSelection(37351, Constants.MEDICAL_CODE));
		planSelections.add(preparePlanSelection(37351, Constants.VISION_CODE));
		planSelections.add(preparePlanSelection(37351, Constants.DENTAL_CODE));
		planSelections.add(preparePlanSelection(37352, Constants.MEDICAL_CODE));

		ModelCompareStrategy modelCompareStrategy = new ModelCompareStrategy();
		List<Object[]> data = new ArrayList<>();
		Object[] obj = { 37351, "Group 1", "1st of month on/after DOH", Constants.MEDICAL_CODE, null, null, "employee",
				new BigDecimal(100), "Custom Strategy", "Employee" };
		data.add(obj);
		Object[] obj1 = { 37351, "Group 1", "1st of month on/after DOH", Constants.DENTAL_CODE, null, null,
				"employeePlusSpouse", new BigDecimal(200), "Custom Strategy", "Employee + Spouse" };
		data.add(obj1);
		Object[] obj2 = { 37352, "Group 2", "1st of month on/after DOH", null, null, null, null, null,
				"Custom Strategy", null };
		data.add(obj2);
		setStrategyDetails(modelCompareStrategy, data);
		Map<Long, ModelCompareStrategy> modelCompareStrategyMap = new HashMap<>();
		modelCompareStrategyMap.put(strategyId, modelCompareStrategy);

		when(strategyDao.findById(strategyId)).thenReturn(strategy);

		when(strategyFundingDataDao.getFundingDetailsByStrategyId(Arrays.asList(strategyId), company, false,
				company.getRealmPlanYear().getPlanYearEnd())).thenReturn(modelCompareStrategyMap);
		when(strategyHsaFundingDao.findByStrategyId(strategyId)).thenReturn(prepareStrategyHsaFunding(strategyId));
		when(strategyGroupPlanSelectDao.findDistinctGroupIdPlanTypeByStrategyId(strategyId)).thenReturn(planSelections);

		when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes, realmPlanYearId))
				.thenReturn(prepareCoverageLevelMap());

		// when
		actualStrategy = modelCompareService.getMCStrategyGroupFunding(strategyId, company);

		// then
		assertEquals(2, actualStrategy.getGroupFundingList().size());
		assertEquals("Custom Strategy", actualStrategy.getName());
		assertEquals("Group 1", actualStrategy.getGroupFundingList().get(0).getName());
		assertEquals("Group 2", actualStrategy.getGroupFundingList().get(1).getName());
		assertEquals(3, actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().size());
		assertEquals(3, actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().size());
		assertTrue(actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().keySet()
				.containsAll(Arrays.asList(Constants.MEDICAL, Constants.DENTAL, Constants.VISION)));
		assertTrue(actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().keySet()
				.containsAll(Arrays.asList(Constants.MEDICAL, Constants.DENTAL, Constants.VISION)));
		assertTrue(actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().get(Constants.MEDICAL).isOffered());
		assertTrue(actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().get(Constants.DENTAL).isOffered());
		assertTrue(actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().get(Constants.VISION).isOffered());
		assertTrue(actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().get(Constants.MEDICAL).isOffered());
		assertFalse(actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().get(Constants.DENTAL).isOffered());
		assertFalse(actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().get(Constants.VISION).isOffered());
		assertEquals(1, actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().get(Constants.MEDICAL)
				.getCoverageLevels().size());
		assertEquals(1, actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().get(Constants.DENTAL)
				.getCoverageLevels().size());
		assertEquals(2, actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding().get(Constants.VISION)
				.getCoverageLevels().size());
		assertEquals(new BigDecimal(100), actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding()
				.get(Constants.MEDICAL).getCoverageLevels().get(0).getContribution());
		assertEquals(new BigDecimal(200), actualStrategy.getGroupFundingList().get(0).getOfferTypeFunding()
				.get(Constants.DENTAL).getCoverageLevels().get(0).getContribution());
		assertEquals(2, actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().get(Constants.MEDICAL)
				.getCoverageLevels().size());
		assertEquals(2, actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().get(Constants.DENTAL)
				.getCoverageLevels().size());
		assertEquals(2, actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().get(Constants.VISION)
				.getCoverageLevels().size());
		assertFalse(actualStrategy.getGroupFundingList().get(1).getOfferTypeFunding().get(Constants.MEDICAL)
				.getCoverageLevels().contains("all"));
	}

	@Test
	public void getMCSelectedStrategyCostsTest() {

		Long realmYrId = 10L;
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setRealmPlanYearId(realmYrId);
		company.setHeadQuatersState("MA");
		company.setHeadQuatersCity("Boston");
		List<Long> strategyIds = Arrays.asList(STRATEGY_1, STRATEGY_2, STRATEGY_3);
		Map<Long, List<ModelComparePlanTypeCost>> mockStrategyPlanTypeCosts = new HashMap<>();
		List<ModelCompareStrategyCost> actualStrategyPlanTypeCosts;

		ArgumentCaptor<Long> realmYrIdCaptor = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Set> regionsCaptor = ArgumentCaptor.forClass(Set.class);
		/*
		 * GIVEN
		 */

		// TEST CASE 1 - ############# When Commuter is offered
		// ###################

		List<String> offeredBenefits = Arrays.asList("medical", "dental", "vision", "LIFE", "DISABILITY", "CMTR");

		mockStrategyPlanTypeCosts.put(STRATEGY_1, prepareModelComparePlanTypeCosts(STRATEGY_1));
		mockStrategyPlanTypeCosts.put(STRATEGY_2, prepareModelComparePlanTypeCosts(STRATEGY_2));
		mockStrategyPlanTypeCosts.put(STRATEGY_3, prepareModelComparePlanTypeCosts(STRATEGY_3));

		// mock data for the strategy - benefit groups
		Map<Long, List<BenefitGroup>> mockStrategyGroups = prepareStrategyGroups();

		Mockito.when(strategyDataDao.getGroupsByStrategy(strategyIds)).thenReturn(mockStrategyGroups);
		Mockito.when(strategyDataDao.getStrategiesCost(strategyIds)).thenReturn(mockStrategyPlanTypeCosts);
		Mockito.when(
				realmDataDao.getSelectedBenefitsExceptVoluntary(realmYrIdCaptor.capture(), regionsCaptor.capture()))
				.thenReturn(offeredBenefits);

		// when - 3 different strategies setup in our test
		actualStrategyPlanTypeCosts = modelCompareService.getMCSelectedStrategyCosts(strategyIds, company);

		ModelCompareStrategyCost strategyCost = null;

		/*
		 * get costs for strategy1 Then test the return values
		 */
		for (ModelCompareStrategyCost mcsc : actualStrategyPlanTypeCosts) {
			if (mcsc.getStrategyId() == STRATEGY_1) {
				strategyCost = mcsc;
			}
		}

		assertEquals(2, regionsCaptor.getValue().size());
		assertTrue(regionsCaptor.getValue().contains("MA"));
		assertTrue(regionsCaptor.getValue().contains("Boston"));
		assertEquals(realmYrId, realmYrIdCaptor.getValue());
		assertEquals(2, strategyCost.getBenefitGroups().size());
		assertEquals("Group One", strategyCost.getBenefitGroups().get(0).getName());
		assertEquals(7, strategyCost.getPlanTypeCosts().size()); 
		
		for (ModelComparePlanTypeCost r : strategyCost.getPlanTypeCosts()) {
			if (PlanTypesEnum.MEDICAL.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(10000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.DENTAL.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(5000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.VISION.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(2500), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.STD.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(15000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.LIFE.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(20000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.CMTR.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(20000), r.getCost());
				assertTrue(r.isOffered());
			}
		}

		/*
		 * get costs for strategy2 Then test the return values
		 */
		for (ModelCompareStrategyCost mcsc : actualStrategyPlanTypeCosts) {
			if (mcsc.getStrategyId() == STRATEGY_2) {
				strategyCost = mcsc;
			}
		}

		assertEquals(7, strategyCost.getPlanTypeCosts().size());
		for (ModelComparePlanTypeCost r : strategyCost.getPlanTypeCosts()) {
			if (PlanTypesEnum.MEDICAL.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(30000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.DENTAL.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(4000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.VISION.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(5000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.STD.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(0), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.LIFE.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(0), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.CMTR.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(0), r.getCost());
				assertTrue(r.isOffered());
			}
		}

		/*
		 * get costs for strategy3 Then test the return values
		 */
		for (ModelCompareStrategyCost mcsc : actualStrategyPlanTypeCosts) {
			if (mcsc.getStrategyId() == STRATEGY_3) {
				strategyCost = mcsc;
			}
		}
		assertEquals(7, actualStrategyPlanTypeCosts.get(1).getPlanTypeCosts().size()); 
		for (ModelComparePlanTypeCost r : strategyCost.getPlanTypeCosts()) {
			if (PlanTypesEnum.MEDICAL.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(0), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.DENTAL.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(0), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.VISION.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(0), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.STD.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(4000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.LIFE.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(2000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.CMTR.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(0), r.getCost());
				assertTrue(r.isOffered());
			}
		}

		// TEST CASE 2 - ############# When Commuter is not offered
		// ###################

		// Given
		offeredBenefits = Arrays.asList("medical", "dental", "vision", "LIFE", "DISABILITY");
		strategyIds = Arrays.asList(STRATEGY_2);

		mockStrategyPlanTypeCosts.clear();
		mockStrategyPlanTypeCosts.put(STRATEGY_2, prepareModelComparePlanTypeCosts(STRATEGY_2));

		Mockito.when(strategyDataDao.getGroupsByStrategy(strategyIds)).thenReturn(mockStrategyGroups);
		Mockito.when(strategyDataDao.getStrategiesCost(strategyIds)).thenReturn(mockStrategyPlanTypeCosts);
		Mockito.when(
				realmDataDao.getSelectedBenefitsExceptVoluntary(realmYrIdCaptor.capture(), regionsCaptor.capture()))
				.thenReturn(offeredBenefits);

		// when - 3 different strategies setup in our test
		actualStrategyPlanTypeCosts = modelCompareService.getMCSelectedStrategyCosts(strategyIds, company);

		/*
		 * get costs for strategy2 Then the return plans should not contain CMTR
		 * benefit.
		 */
		strategyCost = actualStrategyPlanTypeCosts.get(0);

		assertEquals(1, actualStrategyPlanTypeCosts.size());
		assertEquals(6, strategyCost.getPlanTypeCosts().size());
		for (ModelComparePlanTypeCost r : strategyCost.getPlanTypeCosts()) {
			if (PlanTypesEnum.MEDICAL.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(30000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.DENTAL.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(4000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.VISION.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(5000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.STD.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(0), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.LIFE.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(0), r.getCost());
				assertTrue(r.isOffered());
			}
		}
	}

	@Test
	public void getMCSelectedStrategyCostsProspectTest() {

		Long realmYrId = 10L;
		Company company = new Company();
		company.setCode(PROSPECT_COMPANY_CODE);
		company.setRealmPlanYearId(realmYrId);
		company.setHeadQuatersState("MA");
		company.setHeadQuatersCity("Boston");
		List<Long> strategyIds = new ArrayList<>();
		strategyIds.add(ProspectConstants.PROSPECT_STRATEGY_ID);
		strategyIds.add(STRATEGY_1);
		Map<Long, List<ModelComparePlanTypeCost>> mockStrategyPlanTypeCosts = new HashMap<>();
		List<ModelCompareStrategyCost> actualStrategyPlanTypeCosts;

		ArgumentCaptor<Long> realmYrIdCaptor = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Set> regionsCaptor = ArgumentCaptor.forClass(Set.class);
		/*
		 * GIVEN
		 */

		// TEST CASE 1 - ############# When Commuter is offered
		// ###################

		List<String> offeredBenefits = Arrays.asList("medical", "dental", "vision", "LIFE", "DISABILITY", "CMTR");

		mockStrategyPlanTypeCosts.put(STRATEGY_1, prepareModelComparePlanTypeCosts(STRATEGY_1));
		mockStrategyPlanTypeCosts.put(STRATEGY_2, prepareModelComparePlanTypeCosts(STRATEGY_2));

		// mock data for the strategy - benefit groups
		Map<Long, List<BenefitGroup>> mockStrategyGroups = prepareStrategyGroups();

		Mockito.when(strategyDataDao.getGroupsByStrategy(strategyIds)).thenReturn(mockStrategyGroups);
		Mockito.when(strategyDataDao.getStrategiesCost(strategyIds)).thenReturn(mockStrategyPlanTypeCosts);
		Mockito.when(
						realmDataDao.getSelectedBenefitsExceptVoluntary(realmYrIdCaptor.capture(), regionsCaptor.capture()))
				.thenReturn(offeredBenefits);

		// when - 3 different strategies setup in our test
		actualStrategyPlanTypeCosts = modelCompareService.getMCSelectedStrategyCosts(strategyIds, company);

		ModelCompareStrategyCost strategyCost = null;

		/*
		 * get costs for strategy1 Then test the return values
		 */
		for (ModelCompareStrategyCost mcsc : actualStrategyPlanTypeCosts) {
			if (mcsc.getStrategyId() == STRATEGY_1) {
				strategyCost = mcsc;
			}
		}

		assertEquals(2, regionsCaptor.getValue().size());
		assertTrue(regionsCaptor.getValue().contains("MA"));
		assertTrue(regionsCaptor.getValue().contains("Boston"));
		assertEquals(realmYrId, realmYrIdCaptor.getValue());
		assertEquals(2, strategyCost.getBenefitGroups().size());
		assertEquals("Group One", strategyCost.getBenefitGroups().get(0).getName());
		assertEquals(7, strategyCost.getPlanTypeCosts().size());

		for (ModelComparePlanTypeCost r : strategyCost.getPlanTypeCosts()) {
			if (PlanTypesEnum.MEDICAL.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(10000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.DENTAL.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(5000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.VISION.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(2500), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.STD.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(15000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.LIFE.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(20000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.CMTR.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(20000), r.getCost());
				assertTrue(r.isOffered());
			}
		}

		/*
		 * get costs for strategy2 Then test the return values
		 */
		for (ModelCompareStrategyCost mcsc : actualStrategyPlanTypeCosts) {
			if (mcsc.getStrategyId() == STRATEGY_2) {
				strategyCost = mcsc;
			}
		}

		assertEquals(7, strategyCost.getPlanTypeCosts().size());
		for (ModelComparePlanTypeCost r : strategyCost.getPlanTypeCosts()) {
			if (PlanTypesEnum.MEDICAL.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(30000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.DENTAL.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(4000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.VISION.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(5000), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.STD.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(0), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.LIFE.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(0), r.getCost());
				assertTrue(r.isOffered());
			} else if (PlanTypesEnum.CMTR.getName().equals(r.getPlanType())) {
				assertEquals(BigDecimal.valueOf(0), r.getCost());
				assertTrue(r.isOffered());
			}
		}
	}


	@Test
	public void getModelCompareExcelWorkbook() throws Exception {
		
		Company company = new Company();
		company.setId(1L);
		company.setCode(COMPANY_CODE);
		company.setRenewalCompany(false);
		company.setRealmPlanYearId(21);
		company.setHeadQuatersState("MA");
		company.setHeadQuatersCity("Boston");

		Workbook workbook = new XSSFWorkbook();
		List<Long> strategyIdList = Arrays.asList(1L, 2L, 3L);
		
		ModelCompareStrategy modelCompareStrategy1 = prepareModelCompareStrategy(1000, "CURRENT STRATEGY", false, true);
		ModelCompareStrategy modelCompareStrategy2 = prepareModelCompareStrategy(1001, "FUTURE STRATEGY", false, false);
		
		List<ModelCompareStrategy> strategies = Arrays.asList(modelCompareStrategy1, modelCompareStrategy2);
		
		when(strategyDataDao.getModelCompareStrategies(company.getId())).thenReturn(strategies);

		modelCompareService.getModelCompareExcelWorkbook(company, 1000L, Arrays.asList(1001L, 1002L));
		assertEquals(0, workbook.getNumberOfSheets());
		verify(employeeBenefitGroupDao, times(0)).getStrategyGroupDetailsForCompany(any(Company.class));
		
	}
	
	@Test
	public void getEmployeeStrategiesPlanCostWorkbook() throws Exception {

		Company company = new Company();
		company.setId(1L);
		company.setRenewalCompany(false);
		company.setRealmPlanYearId(21);
		company.setHeadQuatersState("MA");
		company.setHeadQuatersCity("Boston");

		Workbook workbook = new XSSFWorkbook();

		when(employeeDataService.getEmployeeStrategiesPlanCostData(company, 1L, Arrays.asList(2L, 3L))).thenReturn(null);
		
		modelCompareService.getEmployeeStrategiesPlanCostWorkbook(company, 1L, Arrays.asList(2L, 3L), prepareStrategyMap(), workbook);
		assertEquals(0, workbook.getNumberOfSheets());

	}	
	
	
	/// ********************************SETUP********************//

	private Company prepareCompany(long companyId, String companyName, String companyCode, String planStartDate,
			String quarter, boolean isPayrollProcessed, boolean transitionPeriod, String kaiserBandCode,
			String headQtrState) {

		Company cmp = new Company();
		cmp.setId(companyId);
		cmp.setName(companyName);
		cmp.setCode(companyCode);
		cmp.setPlanStartDate(planStartDate);
		cmp.setQuater(quarter);
		cmp.setPayrollProcessed(isPayrollProcessed);
		cmp.setTransitionPeriod(transitionPeriod);
		BandCodes bandCodes = new BandCodes();
		bandCodes.setKaiserBandCode(kaiserBandCode);
		cmp.setHeadQuatersState(headQtrState);
		cmp.setBandCodes(bandCodes);
		return cmp;
	}

	private ModelCompareStrategy prepareModelCompareStrategy(long id, String name, boolean isSubmitted,
			boolean isActiveStrategy) {

		ModelCompareStrategy mcs = new ModelCompareStrategy();
		mcs.setId(id);
		mcs.setName(name);
		mcs.setSubmitted(isSubmitted);
		mcs.setActiveStrategy(isActiveStrategy);
		return mcs;
	}

	private Map<Long, List<BenefitGroup>> prepareStrategyGroups () {
		Map<Long, List<BenefitGroup>> mockStrategyGroups = new HashMap<>();
		List<BenefitGroup> groupList = new ArrayList<>();
		groupList.add(prepareBenefitGroup(30754L, "Group One"));
		groupList.add(prepareBenefitGroup(30753L, "Group Two"));
		mockStrategyGroups.put(STRATEGY_1, groupList);
		mockStrategyGroups.put(STRATEGY_2, groupList);

		groupList = new ArrayList<>();
		groupList.add(prepareBenefitGroup(30750L, "Group Three"));
		mockStrategyGroups.put(STRATEGY_3, groupList);

		return mockStrategyGroups;
	}

	private BenefitGroup prepareBenefitGroup(Long id, String name) {
		BenefitGroup bg = new BenefitGroup();
		bg.setId(id);
		bg.setName(name);
		return bg;
	}

	private List<ModelComparePlanTypeCost> prepareModelComparePlanTypeCosts(Long strategyId) {
		List<ModelComparePlanTypeCost> mockPlanTypeCosts = new ArrayList<>();
		if (strategyId.equals(STRATEGY_1)) {
			mockPlanTypeCosts
					.add(prepareModelComparePlanTypeCost(PlanTypesEnum.MEDICAL.getName(), BigDecimal.valueOf(10000), true));
			mockPlanTypeCosts
					.add(prepareModelComparePlanTypeCost(PlanTypesEnum.DENTAL.getName(), BigDecimal.valueOf(5000), true));
			mockPlanTypeCosts
					.add(prepareModelComparePlanTypeCost(PlanTypesEnum.VISION.getName(), BigDecimal.valueOf(2500), true));
			mockPlanTypeCosts
					.add(prepareModelComparePlanTypeCost(PlanTypesEnum.STD.getName(), BigDecimal.valueOf(15000), true));
			mockPlanTypeCosts
					.add(prepareModelComparePlanTypeCost(PlanTypesEnum.LIFE.getName(), BigDecimal.valueOf(20000), true));
			mockPlanTypeCosts
					.add(prepareModelComparePlanTypeCost(PlanTypesEnum.CMTR.getName(), BigDecimal.valueOf(20000), true));
		}
		else if (strategyId.equals(STRATEGY_2)) {
			mockPlanTypeCosts
					.add(prepareModelComparePlanTypeCost(PlanTypesEnum.MEDICAL.getName(), BigDecimal.valueOf(30000), true));
			mockPlanTypeCosts
					.add(prepareModelComparePlanTypeCost(PlanTypesEnum.DENTAL.getName(), BigDecimal.valueOf(4000), true));
			mockPlanTypeCosts
					.add(prepareModelComparePlanTypeCost(PlanTypesEnum.VISION.getName(), BigDecimal.valueOf(5000), true));
		}
		else if (strategyId.equals(STRATEGY_3)) {
			mockPlanTypeCosts
					.add(prepareModelComparePlanTypeCost(PlanTypesEnum.STD.getName(), BigDecimal.valueOf(4000), true));
			mockPlanTypeCosts
					.add(prepareModelComparePlanTypeCost(PlanTypesEnum.LIFE.getName(), BigDecimal.valueOf(2000), true));
		}
		else if (strategyId.equals(ProspectConstants.PROSPECT_STRATEGY_ID)) {
			mockPlanTypeCosts
					.add(prepareModelComparePlanTypeCost(PlanTypesEnum.MEDICAL.getName(), BigDecimal.valueOf(10000), true));
			mockPlanTypeCosts
					.add(prepareModelComparePlanTypeCost(PlanTypesEnum.DENTAL.getName(), BigDecimal.valueOf(5000), true));
			mockPlanTypeCosts
					.add(prepareModelComparePlanTypeCost(PlanTypesEnum.VISION.getName(), BigDecimal.valueOf(2500), true));
		}

		return mockPlanTypeCosts;

	}

	private ModelCompareStrategyCost prepareProspectModelCompareStrategyCost () {
		ModelCompareStrategyCost modelCompareStrategyCost = new ModelCompareStrategyCost();
		modelCompareStrategyCost.setStrategyId(ProspectConstants.PROSPECT_STRATEGY_ID);
		modelCompareStrategyCost.setBenefitGroups(Arrays.asList(prepareBenefitGroup(30754L, "Group One")));
		modelCompareStrategyCost.setPlanTypeCosts(prepareModelComparePlanTypeCosts(ProspectConstants.PROSPECT_STRATEGY_ID));
		return modelCompareStrategyCost;
	}

	private ModelComparePlanTypeCost prepareModelComparePlanTypeCost(String planType, BigDecimal cost,
			boolean offered) {
		ModelComparePlanTypeCost mcptc = new ModelComparePlanTypeCost();
		mcptc.setPlanType(planType);
		mcptc.setCost(cost);
		mcptc.setOffered(offered);
		return mcptc;
	}

	private void setStrategyDetails(ModelCompareStrategy strategy, List<Object[]> obj) {
		List<GroupFunding> groupFundingList = new ArrayList<>();
		Map<Long, GroupFunding> groupFundingMap = new HashMap<>();
		String strategyName = null;
		for (Object[] r : obj) {
			BigDecimal groupId = new BigDecimal(String.valueOf(r[0]));
			String groupDesc = (String) r[1];
			String waitTime = (String) r[2];
			String planType = r[3] == null ? null : String.valueOf(r[3]);
			String fundingType = (String) r[4];
			String baseFundPlan = (String) r[5];
			String coverageId = (String) r[6];
			BigDecimal contribution = r[7] == null ? null : new BigDecimal(String.valueOf(r[7]));
			strategyName = (String) r[8];
			String cvgLvlDesc = (String) r[9];

			if (Constants.dentalPlanTypeList.contains(planType)) {
				planType = Constants.DENTAL;
			} else if (Constants.visionPlanTypeList.contains(planType)) {
				planType = Constants.VISION;
			} else if (Constants.medicalPlanTypeList.contains(planType)) {
				planType = Constants.MEDICAL;
			}
			CoverageLevel cvgLvl = new CoverageLevel(cvgLvlDesc, coverageId, contribution);
			if (null != groupFundingMap.get(groupId.longValue())) {
				GroupFunding gf = groupFundingMap.get((groupId).longValue());
				if (null != gf.getOfferTypeFunding().get(planType)) {
					gf.getOfferTypeFunding().get(planType).getCoverageLevels().add(cvgLvl);
				} else if (planType != null) {
					BenefitOfferFunding bof = new BenefitOfferFunding();
					bof.setBaseFundPlan(baseFundPlan);
					bof.setFundingType(fundingType);
					bof.setType(planType);
					bof.setOffered(true);
					bof.setCoverageLevels(new ArrayList<CoverageLevel>());
					if (coverageId != null) {
						bof.getCoverageLevels().add(cvgLvl);
					}
					gf.getOfferTypeFunding().put(bof.getType(), bof);
				}
			} else {
				GroupFunding gf = new GroupFunding();
				gf.setId((groupId).longValue());
				gf.setName(groupDesc);
				gf.setWaitTime(waitTime);
				if (planType != null) {
					BenefitOfferFunding bof = new BenefitOfferFunding();
					bof.setBaseFundPlan(baseFundPlan);
					bof.setFundingType(fundingType);
					bof.setType(planType);
					bof.setOffered(true);
					bof.setCoverageLevels(new ArrayList<CoverageLevel>());
					if (coverageId != null) {
						bof.getCoverageLevels().add(cvgLvl);
					}
					gf.getOfferTypeFunding().put(bof.getType(), bof);
				}
				groupFundingMap.put(gf.getId(), gf);
			}
		}
		groupFundingList.addAll(groupFundingMap.values());
		strategy.setName(strategyName);
		strategy.setGroupFundingList(groupFundingList);
	}

	private PlanSelection preparePlanSelection(long groupId, String planType) {
		PlanSelection planSelection = new PlanSelection();
		planSelection.setGroupId(groupId);
		planSelection.setPlanType(planType);
		return planSelection;
	}

	private Map<String, List<CoverageLevel>> prepareCoverageLevelMap() {
		Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
		List<CoverageLevel> coverageLevels = new ArrayList<>();
		CoverageLevel coverageLevel1 = new CoverageLevel(CoverageCodesEnums.COV_ALL);
		CoverageLevel coverageLevel2 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE);
		CoverageLevel coverageLevel3 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD);
		coverageLevels.addAll(Arrays.asList(coverageLevel1, coverageLevel2, coverageLevel3));

		mapOfCoverageLevels.put(Constants.MEDICAL, coverageLevels);
		mapOfCoverageLevels.put(Constants.DENTAL, coverageLevels);
		mapOfCoverageLevels.put(Constants.VISION, coverageLevels);
		return mapOfCoverageLevels;
	}	
	
	private Map<Long, String> prepareStrategyMap() {
		Map<Long, String> strategyMap = new HashMap<>();
		strategyMap.put(1000L, "CURRENT STRATEGY");
		strategyMap.put(1001L, "FUTURE STRATEGY");
		return strategyMap;
	}
	
	private Map<Long, List<AdditionalBenefitPlan>> prepareStrategyGroupAdditionalOfferingMap() {

		Map<Long, List<AdditionalBenefitPlan>> strategyGroupAdditionalOfferingMap = new HashMap<>();
		List<AdditionalBenefitPlan> group1AdditionalBenefitPlanList = new ArrayList<>();
		AdditionalBenefitPlan additionalBenefitPlan = new AdditionalBenefitPlan();
		additionalBenefitPlan.setPlanType(BSSApplicationConstants.STD_CODE);
		additionalBenefitPlan.setDescription("STD PLAN 1");
		additionalBenefitPlan.setId("STDPLAN1");
		group1AdditionalBenefitPlanList.add(additionalBenefitPlan);

		additionalBenefitPlan = new AdditionalBenefitPlan();
		additionalBenefitPlan.setPlanType(BSSApplicationConstants.LIFE_CODE);
		additionalBenefitPlan.setDescription("LIFE PLAN 1");
		additionalBenefitPlan.setId("LIFEPLAN1");
		group1AdditionalBenefitPlanList.add(additionalBenefitPlan);

		strategyGroupAdditionalOfferingMap.put(37351L, group1AdditionalBenefitPlanList);
		
		List<AdditionalBenefitPlan> group2AdditionalBenefitPlanList = new ArrayList<>();
		group2AdditionalBenefitPlanList.addAll(group1AdditionalBenefitPlanList);
		additionalBenefitPlan = new AdditionalBenefitPlan();
		additionalBenefitPlan.setPlanType(BSSApplicationConstants.LTD_CODE);
		additionalBenefitPlan.setDescription("LTD PLAN 1");
		additionalBenefitPlan.setId("LTDPLAN1");
		group2AdditionalBenefitPlanList.add(additionalBenefitPlan);

		strategyGroupAdditionalOfferingMap.put(37352L, group2AdditionalBenefitPlanList);

		return strategyGroupAdditionalOfferingMap;
	}
	
	private StrategyHsaFunding prepareStrategyHsaFunding(long strategyId) {
		StrategyHsaFunding strategyHsaFunding = new StrategyHsaFunding();
		strategyHsaFunding.setStrategyId(strategyId);
		strategyHsaFunding.setOptionId(7);
		strategyHsaFunding.setMonthlyEeAmount(BigDecimal.valueOf(200));
		strategyHsaFunding.setMonthlyFamilyAmount(BigDecimal.valueOf(200));
		strategyHsaFunding.setContributionFrequency(BSSApplicationConstants.HSA_MONTHLY);
		strategyHsaFunding.setAnnualEeAmount(BigDecimal.valueOf(1200));
		strategyHsaFunding.setAnnualFamilyAmount(BigDecimal.valueOf(2400));
		strategyHsaFunding.setAnnualMonth(1);
		strategyHsaFunding.setLumpSumFrequency(BSSApplicationConstants.HSA_ANNUAL);		
		return strategyHsaFunding;
	}
	
	@Test
	public void getMCStrategiesWithGroupDetails() {

		long currentRealmPlanYear = 9;

		List<ModelCompareStrategy> strategies = null;
		List<ModelCompareStrategy> actualMCList = null;
		Map<String, Set<StrategyGroupDetails>> benefitProgramStrategyGroups = getStrategyGroupsMap();

		Company company = prepareCompany(COMPANY_ID, COMPANY_NAME, "PROSPECT", PLAN_START_DATE, QUARTER,
				IS_PAYROLL_PROCESSED_TRUE, TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		company.setRenewalCompany(true);
		company.setRealmPlanYearId(currentRealmPlanYear);
		company.setProspectCompany(true);

		strategies = new ArrayList<>();
		strategies.add(prepareModelCompareStrategy(1000, StrategyTypesEnums.F_S.getName(), true, false));
		strategies.add(prepareModelCompareStrategy(1001, "Custom Strategy", false, false));
		
		Mockito.when(strategyDataDao.getModelCompareStrategies(company.getId())).thenReturn(strategies);
		Mockito.when(employeeBenefitGroupDao.getStrategyGroupDetailsForCompany(company)).thenReturn(benefitProgramStrategyGroups);

		// when
		actualMCList = modelCompareService.getMCStrategies(company,false);

		// then
		assertEquals(3, actualMCList.size());
		verify(employeeBenefitGroupDao, times(1)).getStrategyGroupDetailsForCompany(any(Company.class));

		for (ModelCompareStrategy mcs : actualMCList) {
			if (1000 == mcs.getId()) {
				assertEquals(2, mcs.getGroups().size());
			}
			if (1001 == mcs.getId()) {
				assertEquals(1, mcs.getGroups().size());
			}
		}
	}

	private Map<String, Set<StrategyGroupDetails>> getStrategyGroupsMap() {
		Map<String, Set<StrategyGroupDetails>> benefitProgramStrategyGroups = new HashMap<>();
		Set<StrategyGroupDetails> strategyGroupDetailsSet = new HashSet<>();
		StrategyGroupDetails strategyGroupDetails =  new StrategyGroupDetails();
		strategyGroupDetails.setDefaultGroup(true);
		strategyGroupDetails.setStrategyId(1000L);
		strategyGroupDetails.setGroupName("K1");
		strategyGroupDetails.setGroupId(12345L);
		strategyGroupDetails.setStatus("A");
		strategyGroupDetails.setBenefitProgram("test1");
		strategyGroupDetailsSet.add(strategyGroupDetails);
		
		strategyGroupDetails =  new StrategyGroupDetails();
		strategyGroupDetails.setDefaultGroup(true);
		strategyGroupDetails.setStrategyId(1000L);
		strategyGroupDetails.setGroupName("W2");
		strategyGroupDetails.setGroupId(12346L);
		strategyGroupDetails.setStatus("A");
		strategyGroupDetailsSet.add(strategyGroupDetails);
		strategyGroupDetails.setBenefitProgram("test1");
		benefitProgramStrategyGroups.put("test1", strategyGroupDetailsSet);
		
		strategyGroupDetailsSet = new HashSet<>();
		
		strategyGroupDetails =  new StrategyGroupDetails();
		strategyGroupDetails.setDefaultGroup(true);
		strategyGroupDetails.setStrategyId(1001L);
		strategyGroupDetails.setGroupName("K1");
		strategyGroupDetails.setGroupId(12345L);
		strategyGroupDetails.setStatus("A");
		strategyGroupDetails.setBenefitProgram("test2");
		strategyGroupDetailsSet.add(strategyGroupDetails);
		
		strategyGroupDetails =  new StrategyGroupDetails();
		strategyGroupDetails.setDefaultGroup(true);
		strategyGroupDetails.setStrategyId(1001L);
		strategyGroupDetails.setGroupName("K1");
		strategyGroupDetails.setGroupId(12346L);
		strategyGroupDetails.setStatus("D");
		strategyGroupDetails.setBenefitProgram("test2");
		
		benefitProgramStrategyGroups.put("test2", strategyGroupDetailsSet);
		
		return benefitProgramStrategyGroups;
	}
}

