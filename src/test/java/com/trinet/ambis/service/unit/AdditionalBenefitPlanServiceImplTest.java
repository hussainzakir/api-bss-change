package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

import com.trinet.ambis.util.AppRulesAndConfigsUtils;
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

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.helper.AdditionalBenefitServiceHelper;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.MandatoryRegionDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupDataDao;
import com.trinet.ambis.persistence.dao.ps.LifeAndDisabilityCalcData;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.outputs.AdditionalBenefitGroup;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanComparisonAdditonalBenefits;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.DisabilityOptionService;
import com.trinet.ambis.service.HeadCountService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.impl.AdditionalBenefitPlanServiceImpl;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.AdditionalBenefitEmployeeDetails;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.AdditionalBenefitPlanRates;
import com.trinet.ambis.service.model.AdditionalBenefitsCategoryOffer;
import com.trinet.ambis.service.model.AdditionalPlanRate;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.EmployeeStrategyGroupDetails;
import com.trinet.ambis.service.model.FormulaDefinition;
import com.trinet.ambis.service.model.FormulaProperties;
import com.trinet.ambis.service.model.PlanTypeDescription;
import com.trinet.ambis.service.model.RateProperties;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.service.model.output.AdditionalBenefitPlanDto;
import com.trinet.ambis.service.prospect.ProspectEmployeeService;
import com.trinet.ambis.service.prospect.response.CensusRes;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.Utils;


@RunWith(MockitoJUnitRunner.class)
public class AdditionalBenefitPlanServiceImplTest extends ServiceUnitTest {

	private static final long REALM_PLYR_ID = 21;
	private static final String COMPANY_CODE = "G48";
	private static final String CLONE_PROG = "ABCD1234";
	private static final String DIS_BAND_CODE = "DISBANDCODE";
	private static final String LIFE_BAND_CODE = "LIFEBANDCODE";
	private static final String QUARTER = BenExchngEnums.TRINET_IV.getBenExchng();
	private static final String PLAN_START_DATE_STR = "01-JAN-2019";
	private static final Date EFF_DATE = Utils.convertStringToDate(PLAN_START_DATE_STR, Constants.DATE_FORMAT);
	private static final String LIFE_PLAN_ID = "L1111";
	private static final String LIFE_PLAN_ID2 = "L2222";
	private static final String CMTR_PLAN_ID = "C1111";
	private static final String STD_PLAN_ID = "S1111";
	private static final String STD_PLAN_ID2 = "S2222";
	private static final String STD_PLAN_ID3 = "S3333";
	private static final String LTD_PLAN_ID = "LTD1111";
	private static final String LTD_PLAN_ID2 = "LTD2222";
	private static final Set<String> LTD_PLANS = new HashSet<String>(Arrays.asList(LTD_PLAN_ID, LTD_PLAN_ID2));
	private static final Set<String> STD_PLANS = new HashSet<String>(Arrays.asList("STD1111", "STD2222", STD_PLAN_ID, STD_PLAN_ID2, STD_PLAN_ID3));
	private static final Set<String> LIFE_PLANS = new HashSet<String>(Arrays.asList("LIF1111", "LIF2222", LIFE_PLAN_ID, LIFE_PLAN_ID2));
	private static final String DISABILITY_BUNDLE_ID_1 = "1";
	private static final String DISABILITY_BUNDLE_NAME_1 = "Disability Bundle 1";
	private static final String DISABILITY_BUNDLE_ID_2 = "2";
	private static final String DISABILITY_BUNDLE_NAME_2 = "Disability Bundle 2";

	private static final Date EFFDT = Utils.convertStringToDate(PLAN_START_DATE_STR, Constants.DATE_FORMAT);

	private static final Long GROUP_ID = 1234L;
	private static final String BEN_PROGRAM = "benprog";
	private static final String BEN_GRP = "Test Group";

	private static final Long GROUP_ID1 = 4568L;
	private static final String BEN_PROGRAM1 = "benprog1";
	private static final String BEN_GRP1 = "Test Group 1";

	private static final Long GROUP_ID2 = 2345L;
	private static final String BEN_PROGRAM2 = "benprog2";
	private static final String BEN_GRP2 = "Test Group 2";

	private static final Long GROUP_ID3 = 3456L;
	private static final String BEN_PROGRAM3 = "benprog3";
	private static final String BEN_GRP3 = "Test Group 3";

	private Company company = null;

	/*
	 * @Rule public PowerMockRule rule = new PowerMockRule();
	 */

	@InjectMocks
	AdditionalBenefitPlanServiceImpl additionalBenefitPlanService;
	
	@Mock
	DisabilityOptionService disabilityOptionService;

	@Mock
	BenefitGroupService benefitGroupService;

	@Mock
	StrategyDataDao strategyDataDao;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	PsCompanyDao psCompanyDao;

	@Mock
	RenewalDataDao renewalDataDao;
	
	@Mock
	LifeAndDisabilityCalcData lifeAndDisabilityCalcData;

	@Mock
	MandatoryRegionDao mandatoryRegionDao;
	
	@Mock
	BenefitOfferExceptionService benOfferExceptionService;
	
	@Mock
	HeadCountService headCountService;
	
	@Mock
	RealmPlanYearService realmPlanYearService;

	@Mock
	StrategyGroupDataDao strategyGroupDataDao;

	@Mock
	ProspectEmployeeService prospectEmployeeService;

	@Mock
	EmployeeBenefitGroupDao employeeBenefitGroupDao;

    private static MockedStatic<AdditionalBenefitServiceHelper> additionalBenefitServiceHelperMockedStatic;
    private static MockedStatic<StrategyServiceHelper> strategyServiceHelperMockedStatic;
    private static MockedStatic<Utils> utilsMockedStatic;
    private static MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;
	private static MockedStatic<AppRulesAndConfigsUtils> appRulesAndConfigsUtilsMockedStatic;

    @Before
    public void setUp() {
        if (additionalBenefitServiceHelperMockedStatic != null) {
            additionalBenefitServiceHelperMockedStatic.close();
            additionalBenefitServiceHelperMockedStatic = null;
        }
        if (strategyServiceHelperMockedStatic != null) {
            strategyServiceHelperMockedStatic.close();
            strategyServiceHelperMockedStatic = null;
        }
        if (utilsMockedStatic != null) {
            utilsMockedStatic.close();
            utilsMockedStatic = null;
        }
        if (rulesAndConfigsUtilsMockedStatic != null) {
            rulesAndConfigsUtilsMockedStatic.close();
            rulesAndConfigsUtilsMockedStatic = null;
        }
		if (appRulesAndConfigsUtilsMockedStatic != null) {
			appRulesAndConfigsUtilsMockedStatic.close();
			appRulesAndConfigsUtilsMockedStatic = null;
		}
        additionalBenefitServiceHelperMockedStatic = Mockito.mockStatic(AdditionalBenefitServiceHelper.class);
        strategyServiceHelperMockedStatic = Mockito.mockStatic(StrategyServiceHelper.class);
        utilsMockedStatic = Mockito.mockStatic(Utils.class);
        rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
		appRulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(AppRulesAndConfigsUtils.class);

        prepareMocks();
    }

    @After
    public void tearDown() {
        if (additionalBenefitServiceHelperMockedStatic != null) {
            additionalBenefitServiceHelperMockedStatic.close();
            additionalBenefitServiceHelperMockedStatic = null;
        }
        if (strategyServiceHelperMockedStatic != null) {
            strategyServiceHelperMockedStatic.close();
            strategyServiceHelperMockedStatic = null;
        }
        if (utilsMockedStatic != null) {
            utilsMockedStatic.close();
            utilsMockedStatic = null;
        }
        if (rulesAndConfigsUtilsMockedStatic != null) {
            rulesAndConfigsUtilsMockedStatic.close();
            rulesAndConfigsUtilsMockedStatic = null;
        }
		if (appRulesAndConfigsUtilsMockedStatic != null) {
			appRulesAndConfigsUtilsMockedStatic.close();
			appRulesAndConfigsUtilsMockedStatic = null;
		}
    }
	private void prepareMocks() {
		company = prepareCompany();
		Set<String> locations = new HashSet<String>();
		Map<String, Set<StateBenefitPlan>> aDBenefitsAllStatePlans = prepareADBenefitsAllStatePlans();

		Mockito.when(AdditionalBenefitServiceHelper.getADBPlanListMapByType(aDBenefitsAllStatePlans))
				.thenReturn(prepareAdbPlanMap());

		Mockito.when(AdditionalBenefitServiceHelper.getADBPlanListMapByType(Mockito.anyList()))
				.thenReturn(prepareAdbPlanMap());

		Mockito.when(AdditionalBenefitServiceHelper.getAdditionalBenefitPlanListMapByType(Mockito.anyList()))
				.thenReturn(prepareAdbPlanMap());

		Mockito.when(Utils.convertStringToDate(PLAN_START_DATE_STR, Constants.DATE_FORMAT)).thenReturn(EFF_DATE);

		when(realmDataDao.getAdditionalBenefitsAllStatePlans(REALM_PLYR_ID, locations, company))
				.thenReturn(aDBenefitsAllStatePlans);
		when(strategyDataDao.getAdditionalBenefitPlanEstCost(REALM_PLYR_ID)).thenReturn(preparePlanEstCostMap());
		when(strategyDataDao.getPlanTypeDescriptions(REALM_PLYR_ID)).thenReturn(preparePlanTypeDescMap());
		Set<String> hqStates = new HashSet<>();
		hqStates.add("MA");
		Mockito.when(StrategyServiceHelper.getHqStateCity(company)).thenReturn(hqStates);
		when(realmDataDao.getSelectedBenefits(REALM_PLYR_ID, hqStates)).thenReturn(prepareSelectedBenefits());

		when(lifeAndDisabilityCalcData.getFormulaProperties(LTD_PLANS, EFF_DATE, BSSQueryConstants.DISABILITY_CVG_FORMULA_PROPERTIES))
				.thenReturn(prepareLtdFormulaProps());
		when(lifeAndDisabilityCalcData.getFormulaProperties(STD_PLANS, EFF_DATE, BSSQueryConstants.DISABILITY_CVG_FORMULA_PROPERTIES))
				.thenReturn(prepareStdFormulaProps());
		when(lifeAndDisabilityCalcData.getFormulaProperties(LIFE_PLANS, EFF_DATE, BSSQueryConstants.LIFE_CVG_FORMULA_PROPERTIES))
				.thenReturn(prepareLifeFormulaProps());

		when(lifeAndDisabilityCalcData.getRateProperties(CLONE_PROG, EFF_DATE, LTD_PLANS, DIS_BAND_CODE, QUARTER))
				.thenReturn(prepareLtdRateProps());
		when(lifeAndDisabilityCalcData.getRateProperties(CLONE_PROG, EFF_DATE, STD_PLANS, DIS_BAND_CODE, QUARTER))
				.thenReturn(prepareStdRateProps());
		when(lifeAndDisabilityCalcData.getRateProperties(CLONE_PROG, EFF_DATE, LIFE_PLANS, LIFE_BAND_CODE, QUARTER))
				.thenReturn(prepareLifeRateProps());
		when(disabilityOptionService.getDisabilityOptionsByRealmPlanYear(company)).thenReturn(prepareDisabilityPlans());
		when(lifeAndDisabilityCalcData.getPlanRates(Mockito.anySet(), Mockito.any(Date.class))).thenReturn(preparePlanRates());

		when(lifeAndDisabilityCalcData.getFormulaDefinition(Mockito.anyString(), Mockito.any(Date.class)))
				.thenReturn(prepareFormulaDef());

		when(headCountService.getEligibleEmployeeCount(Mockito.any(Company.class), Mockito.anyLong(),
				Mockito.any(RealmPlanYear.class), Mockito.anyBoolean())).thenReturn(prepareEligibleHeadCount());
		when(realmPlanYearService.getPreviousRealmPlanYear(Mockito.anyString(), Mockito.anyLong()))
				.thenReturn(preparePrevRealmPlanYear());
	}

	@Test
	public void getADBPlanCostByGroup() {
		long strategyId = 1111;

		List<AdditionalBenefitPlanRates> actualResult = additionalBenefitPlanService.getADBPlanCostByGroup(company,
				strategyId);

		assertEquals(1, actualResult.size());
		assertEquals("STD", actualResult.get(0).getGroupType());
		assertEquals(3, actualResult.get(0).getAdditionalBenefitOffers().size());
	}

	@Test
	public void getADBPlanCostByGroup_1() {
		long strategyId = 1111;
		company.setRenewalCompany(true);

		when(benefitGroupService.getBenefitGroupByStrategy(strategyId, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(prepareBenefitGroups());
		
		Mockito.when(RulesAndConfigsUtils.isVendorMappingOn(Mockito.anyLong())).thenReturn(true);
		
		List<AdditionalBenefitPlanRates> actualResult = additionalBenefitPlanService.getADBPlanCostByGroup(company,
				strategyId);

		assertEquals(3, actualResult.size());

		for (AdditionalBenefitPlanRates additionalBenefitPlanRates : actualResult) {
			assertTrue(Arrays.asList(1111L, 2222L, 3333L).contains(additionalBenefitPlanRates.getGroupId()));
			if (Long.valueOf(1111).equals(additionalBenefitPlanRates.getGroupId())) {
				for (AdditionalBenefitsCategoryOffer benOffers : additionalBenefitPlanRates
						.getAdditionalBenefitOffers()) {
					assertTrue(Arrays.asList(BSSApplicationConstants.DISABILITY, "life", BSSApplicationConstants.CMTR)
							.contains(benOffers.getSummary().getType()));
					if (BSSApplicationConstants.DISABILITY.equals(benOffers.getSummary().getType())) {
						assertEquals(2, benOffers.getAdditionalBenefitPlans().size());
						for (AdditionalBenefitPlan benPlan : benOffers.getAdditionalBenefitPlans()) {
							assertTrue(Arrays.asList("1111", "3333").contains(benPlan.getId()));
							if ("1111".equals(benPlan.getId())) {
								assertEquals(2, benPlan.getOptionPlans().size());
								for (DisabilityBenefitOptionPlans plan : benPlan.getOptionPlans()) {
									assertTrue(Arrays.asList("L1111", "S1111").contains(plan.getId()));
								}
							} else {
								assertEquals(1, benPlan.getOptionPlans().size());
								for (DisabilityBenefitOptionPlans plan : benPlan.getOptionPlans()) {
									assertTrue(Arrays.asList("S3333").contains(plan.getId()));
								}
							}
						}
					}
				}
			} else if (Long.valueOf(2222).equals(additionalBenefitPlanRates.getGroupId())) {
				for (AdditionalBenefitsCategoryOffer benOffers : additionalBenefitPlanRates
						.getAdditionalBenefitOffers()) {
					assertTrue(Arrays.asList(BSSApplicationConstants.DISABILITY, "life", BSSApplicationConstants.CMTR)
							.contains(benOffers.getSummary().getType()));
					if (BSSApplicationConstants.DISABILITY.equals(benOffers.getSummary().getType())) {
						assertEquals(2, benOffers.getAdditionalBenefitPlans().size());
						for (AdditionalBenefitPlan benPlan : benOffers.getAdditionalBenefitPlans()) {
							assertTrue(Arrays.asList("2222", "3333").contains(benPlan.getId()));
							if ("2222".equals(benPlan.getId())) {
								assertEquals(1, benPlan.getOptionPlans().size());
								for (DisabilityBenefitOptionPlans plan : benPlan.getOptionPlans()) {
									assertTrue(Arrays.asList("S2222").contains(plan.getId()));
								}
							} else {
								assertEquals(1, benPlan.getOptionPlans().size());
								for (DisabilityBenefitOptionPlans plan : benPlan.getOptionPlans()) {
									assertTrue(Arrays.asList("S3333").contains(plan.getId()));
								}
							}
						}
					}
				}
			}
		}
	}

	@Test
	public void getAdditionalPlansRate() {

		Map<String, Set<String>> adbPlanMap = prepareAdbPlanMap();

		Map<String, AdditionalPlanRate> actualResult = additionalBenefitPlanService.getAdditionalPlansRate(company,
				false, adbPlanMap);
		assertEquals(7, actualResult.size());
	}

	@Test
	public void getAdditionalBenefitsCompareInformation() {
		long strategyId = 1111;
		List<String> templates = Arrays.asList(ProspectConstants.PLAN_APPENDIX, ProspectConstants.PLAN_COMPARISON);

		when(benefitGroupService.getBenefitGroupByStrategy(strategyId, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(prepareBenefitGroups());
		when(strategyGroupDataDao.getAdditionalBenPlanSelections(strategyId, company.getRealmPlanYearId()))
				.thenReturn(prepareBeneProgPlanSelection());
		when(lifeAndDisabilityCalcData.getGroupEmployeeSelections(company, false, strategyId, true))
				.thenReturn(prepareEmployeeSelection());
		when(prospectEmployeeService.getEmployees(company.getCode())).thenReturn(prepareProspectEmployees());
		when(employeeBenefitGroupDao.getEmployeeDetailsByStrategy(strategyId)).thenReturn(prepareTrinetEmployeesByEmplId());
		when(benefitGroupService.getBenefitProgramsForStrategy(company.getCode(), strategyId)).thenReturn(List.of(BEN_PROGRAM, BEN_PROGRAM1, BEN_PROGRAM2));
		appRulesAndConfigsUtilsMockedStatic
				.when(AppRulesAndConfigsUtils::isLifeAndDiPageBreakEnabled)
				.thenReturn(false);
		List<PlanComparisonAdditonalBenefits> actualResult = additionalBenefitPlanService.getAdditionalBenefitsCompareInformation(company, strategyId, templates);
		assertEquals(2, actualResult.size());
		PlanComparisonAdditonalBenefits lifePlanCompare = null;
		PlanComparisonAdditonalBenefits disabilityPlanCompare = null;
		AdditionalBenefitGroup selectedGroupDetails = null;
		AdditionalBenefitGroup selectedGroupOneDetails = null;
		AdditionalBenefitGroup selectedGroupTwoDetails = null;
		AdditionalBenefitGroup availableGroupDetails = null;
		AdditionalBenefitGroup availableGroupOneDetails = null;
		AdditionalBenefitGroup availableGroupTwoDetails = null;

		if (actualResult.get(0).getBenefitType().equals(BSSApplicationConstants.LIFE)) {
			lifePlanCompare = actualResult.get(0);
			disabilityPlanCompare = actualResult.get(1);
		} else {
			lifePlanCompare = actualResult.get(1);
			disabilityPlanCompare = actualResult.get(0);
		}

		selectedGroupDetails = lifePlanCompare.getSelectedGroupDetails().stream().filter(group -> group.getGroupName().equals(BEN_GRP)).findFirst().get();
		selectedGroupOneDetails = lifePlanCompare.getSelectedGroupDetails().stream().filter(group -> group.getGroupName().equals(BEN_GRP1)).findFirst().get();
		selectedGroupTwoDetails = lifePlanCompare.getSelectedGroupDetails().stream().filter(group -> group.getGroupName().equals(BEN_GRP2)).findFirst().get();

		availableGroupDetails = lifePlanCompare.getAvailableGroupDetails().stream().filter(group -> group.getGroupName().equals(BEN_GRP)).findFirst().get();
		availableGroupOneDetails = lifePlanCompare.getAvailableGroupDetails().stream().filter(group -> group.getGroupName().equals(BEN_GRP1)).findFirst().get();
		availableGroupTwoDetails = lifePlanCompare.getAvailableGroupDetails().stream().filter(group -> group.getGroupName().equals(BEN_GRP2)).findFirst().get();

		assertEquals(6, lifePlanCompare.getAttributeNames().size());
		assertEquals(3, lifePlanCompare.getSelectedGroupDetails().size());
		assertEquals(3, lifePlanCompare.getAvailableGroupDetails().size());
		assertEquals(1, selectedGroupDetails.getPlans().get(0).getAttributeValues().size());
		assertEquals("$300.00", selectedGroupDetails.getPlans().get(0).getAttributeValues().get(0).get(5));
		assertEquals(1, selectedGroupOneDetails.getPlans().get(0).getAttributeValues().size());
		assertEquals("$400.00", selectedGroupOneDetails.getPlans().get(0).getAttributeValues().get(0).get(5));
		assertEquals(0, selectedGroupTwoDetails.getPlans().get(0).getAttributeValues().size());

		assertEquals(2, availableGroupDetails.getPlans().get(0).getAttributeValues().size());
		assertEquals("$300.00", availableGroupDetails.getPlans().get(0).getAttributeValues().get(0).get(5));
		assertEquals(2, availableGroupOneDetails.getPlans().get(0).getAttributeValues().size());
		assertEquals("$400.00", availableGroupOneDetails.getPlans().get(0).getAttributeValues().get(0).get(5));
		assertEquals(2, availableGroupTwoDetails.getPlans().get(0).getAttributeValues().size());
		assertEquals("$400.00", availableGroupTwoDetails.getPlans().get(0).getAttributeValues().get(0).get(5));

		selectedGroupDetails = disabilityPlanCompare.getSelectedGroupDetails().stream().filter(group -> group.getGroupName().equals(BEN_GRP)).findFirst().get();
		selectedGroupOneDetails = disabilityPlanCompare.getSelectedGroupDetails().stream().filter(group -> group.getGroupName().equals(BEN_GRP1)).findFirst().get();
		selectedGroupTwoDetails = disabilityPlanCompare.getSelectedGroupDetails().stream().filter(group -> group.getGroupName().equals(BEN_GRP2)).findFirst().get();

		availableGroupDetails = disabilityPlanCompare.getAvailableGroupDetails().stream().filter(group -> group.getGroupName().equals(BEN_GRP)).findFirst().get();
		availableGroupOneDetails = disabilityPlanCompare.getAvailableGroupDetails().stream().filter(group -> group.getGroupName().equals(BEN_GRP1)).findFirst().get();
		availableGroupTwoDetails = disabilityPlanCompare.getAvailableGroupDetails().stream().filter(group -> group.getGroupName().equals(BEN_GRP2)).findFirst().get();

		assertEquals(8, disabilityPlanCompare.getAttributeNames().size());
		assertEquals(3, disabilityPlanCompare.getSelectedGroupDetails().size());
		assertEquals("Disability Bundle 1", selectedGroupDetails.getPlans().get(0).getBundleName());
		assertEquals("$37,499.01", selectedGroupDetails.getPlans().get(0).getTotalCost());
		assertEquals(3, selectedGroupDetails.getPlans().get(0).getAttributeValues().size());
		assertEquals("$100 of covered payroll", selectedGroupDetails.getPlans().get(0).getAttributeValues().get(0).get(4));
		assertEquals("$17,499.00", selectedGroupDetails.getPlans().get(0).getAttributeValues().get(0).get(6));
		assertEquals("$17,499.00", selectedGroupDetails.getPlans().get(0).getAttributeValues().get(0).get(7));
		assertEquals("$1,000 of covered payroll", selectedGroupDetails.getPlans().get(0).getAttributeValues().get(1).get(4));
		assertEquals("$17,499.00", selectedGroupDetails.getPlans().get(0).getAttributeValues().get(1).get(6));
		assertEquals("$0.00", selectedGroupDetails.getPlans().get(0).getAttributeValues().get(1).get(7));
		assertEquals(0, selectedGroupOneDetails.getPlans().get(0).getAttributeValues().size());
		assertEquals(0, selectedGroupTwoDetails.getPlans().get(0).getAttributeValues().size());

		assertEquals("Disability Bundle 1", availableGroupDetails.getPlans().get(0).getBundleName());
		assertEquals("$37,499.01", availableGroupDetails.getPlans().get(0).getTotalCost());
		assertEquals(3, availableGroupDetails.getPlans().get(0).getAttributeValues().size());
		assertEquals("$100 of covered payroll", availableGroupDetails.getPlans().get(0).getAttributeValues().get(0).get(4));
		assertEquals("$17,499.00", availableGroupDetails.getPlans().get(0).getAttributeValues().get(0).get(6));
		assertEquals("$1,000 of covered payroll", availableGroupDetails.getPlans().get(0).getAttributeValues().get(1).get(4));
		assertEquals("$17,499.00", availableGroupDetails.getPlans().get(0).getAttributeValues().get(1).get(6));

		assertEquals("Disability Bundle 2", availableGroupOneDetails.getPlans().get(0).getBundleName());
		assertEquals("$0.00", availableGroupOneDetails.getPlans().get(0).getTotalCost());
		assertEquals(0, availableGroupOneDetails.getPlans().get(0).getAttributeValues().size());

        assertEquals("Disability Bundle 2", availableGroupTwoDetails.getPlans().get(0).getBundleName());
        assertEquals("$0.00", availableGroupTwoDetails.getPlans().get(0).getTotalCost());
        assertEquals(0, availableGroupTwoDetails.getPlans().get(0).getAttributeValues().size());

	}
	
	@Test
	public void getAdditionalBenefitsCompareInformationForPCCTest() {
		long strategyId = 1111;
		List<String> templates = Arrays.asList(ProspectConstants.PLAN_COMPARISON);

		when(benefitGroupService.getBenefitGroupByStrategy(strategyId, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(prepareBenefitGroups());
		when(strategyGroupDataDao.getAdditionalBenPlanSelections(strategyId, company.getRealmPlanYearId()))
				.thenReturn(prepareBeneProgPlanSelection());
		when(lifeAndDisabilityCalcData.getGroupEmployeeSelections(company, false, strategyId, true))
				.thenReturn(prepareEmployeeSelection());
		when(prospectEmployeeService.getEmployees(company.getCode())).thenReturn(prepareProspectEmployees());
		when(employeeBenefitGroupDao.getEmployeeDetailsByStrategy(strategyId)).thenReturn(prepareTrinetEmployeesByEmplId());
		when(benefitGroupService.getBenefitProgramsForStrategy(company.getCode(), strategyId)).thenReturn(List.of(BEN_PROGRAM, BEN_PROGRAM1, BEN_PROGRAM2));
		appRulesAndConfigsUtilsMockedStatic
				.when(AppRulesAndConfigsUtils::isLifeAndDiPageBreakEnabled)
				.thenReturn(false);
		List<PlanComparisonAdditonalBenefits> actualResult = additionalBenefitPlanService.getAdditionalBenefitsCompareInformation(company, strategyId, templates);
		assertEquals(2, actualResult.size());
		PlanComparisonAdditonalBenefits lifePlanCompare = null;
		PlanComparisonAdditonalBenefits disabilityPlanCompare = null;

		if (actualResult.get(0).getBenefitType().equals(BSSApplicationConstants.LIFE)) {
			lifePlanCompare = actualResult.get(0);
			disabilityPlanCompare = actualResult.get(1);
		} else {
			lifePlanCompare = actualResult.get(1);
			disabilityPlanCompare = actualResult.get(0);
		}
		
		assertEquals(3, lifePlanCompare.getSelectedGroupDetails().size());
		assertEquals("3", lifePlanCompare.getSelectedGroupDetails().get(2).getPlans().get(0).getAttributeValues().get(0).get(3));
		assertEquals("$300.00", lifePlanCompare.getSelectedGroupDetails().get(2).getPlans().get(0).getAttributeValues().get(0).get(5));
		assertEquals(null, lifePlanCompare.getAvailableGroupDetails());
		assertEquals(3, disabilityPlanCompare.getSelectedGroupDetails().size());
		assertEquals(null, disabilityPlanCompare.getAvailableGroupDetails());
	}

	@Test
	public void getAdditionalBenefitsCompareInformationForPCCWithPageBreakEnabledTest() throws NoSuchFieldException, IllegalAccessException {
		long strategyId = 1111;
		List<String> templates = Arrays.asList(ProspectConstants.PLAN_COMPARISON);

		when(benefitGroupService.getBenefitGroupByStrategy(strategyId, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(prepareBenefitGroups());
		when(strategyGroupDataDao.getAdditionalBenPlanSelections(strategyId, company.getRealmPlanYearId()))
				.thenReturn(prepareBeneProgPlanSelection());
		when(lifeAndDisabilityCalcData.getGroupEmployeeSelections(company, false, strategyId, true))
				.thenReturn(prepareEmployeeSelection());
		when(prospectEmployeeService.getEmployees(company.getCode())).thenReturn(prepareProspectEmployees());
		when(employeeBenefitGroupDao.getEmployeeDetailsByStrategy(strategyId)).thenReturn(prepareTrinetEmployeesByEmplId());
		when(benefitGroupService.getBenefitProgramsForStrategy(company.getCode(), strategyId)).thenReturn(List.of(BEN_PROGRAM, BEN_PROGRAM1, BEN_PROGRAM2));
		appRulesAndConfigsUtilsMockedStatic
				.when(AppRulesAndConfigsUtils::isLifeAndDiPageBreakEnabled)
				.thenReturn(true);
		List<PlanComparisonAdditonalBenefits> actualResult = additionalBenefitPlanService.getAdditionalBenefitsCompareInformation(company, strategyId, templates);
		assertEquals(2, actualResult.size());
		PlanComparisonAdditonalBenefits lifePlanCompare = null;
		PlanComparisonAdditonalBenefits disabilityPlanCompare = null;

		if (actualResult.get(0).getBenefitType().equals(BSSApplicationConstants.LIFE)) {
			lifePlanCompare = actualResult.get(0);
			disabilityPlanCompare = actualResult.get(1);
		} else {
			lifePlanCompare = actualResult.get(1);
			disabilityPlanCompare = actualResult.get(0);
		}

		assertEquals(3, lifePlanCompare.getSelectedGroupDetails().size());
		assertEquals("3", lifePlanCompare.getSelectedGroupDetails().get(2).getPlans().get(0).getAttributeValues().get(0).get(3));
		assertEquals("$300.00", lifePlanCompare.getSelectedGroupDetails().get(2).getPlans().get(0).getAttributeValues().get(0).get(5));
        assertNull(lifePlanCompare.getAvailableGroupDetails());
		assertEquals(3, disabilityPlanCompare.getSelectedGroupDetails().size());
        assertNull(disabilityPlanCompare.getAvailableGroupDetails());
	}
	
	@Test
	public void getAdditionalBenefitsCompareInformationForAPXTest() {
		long strategyId = 1111;
		List<String> templates = Arrays.asList(ProspectConstants.PLAN_APPENDIX);

		when(benefitGroupService.getBenefitGroupByStrategy(strategyId, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(prepareBenefitGroups());
		when(strategyGroupDataDao.getAdditionalBenPlanSelections(strategyId, company.getRealmPlanYearId()))
				.thenReturn(prepareBeneProgPlanSelection());
		when(lifeAndDisabilityCalcData.getGroupEmployeeSelections(company, false, strategyId, true))
				.thenReturn(prepareEmployeeSelection());
		when(prospectEmployeeService.getEmployees(company.getCode())).thenReturn(prepareProspectEmployees());
		when(employeeBenefitGroupDao.getEmployeeDetailsByStrategy(strategyId)).thenReturn(prepareTrinetEmployeesByEmplId());
		when(benefitGroupService.getBenefitProgramsForStrategy(company.getCode(), strategyId)).thenReturn(List.of(BEN_PROGRAM, BEN_PROGRAM1, BEN_PROGRAM2));
		appRulesAndConfigsUtilsMockedStatic
				.when(AppRulesAndConfigsUtils::isLifeAndDiPageBreakEnabled)
				.thenReturn(false);
		List<PlanComparisonAdditonalBenefits> actualResult = additionalBenefitPlanService.getAdditionalBenefitsCompareInformation(company, strategyId, templates);
		assertEquals(2, actualResult.size());
		PlanComparisonAdditonalBenefits lifePlanCompare = null;
		PlanComparisonAdditonalBenefits disabilityPlanCompare = null;

		if (actualResult.get(0).getBenefitType().equals(BSSApplicationConstants.LIFE)) {
			lifePlanCompare = actualResult.get(0);
			disabilityPlanCompare = actualResult.get(1);
		} else {
			lifePlanCompare = actualResult.get(1);
			disabilityPlanCompare = actualResult.get(0);
		}
		
		assertNull(lifePlanCompare.getSelectedGroupDetails());
		assertNull(disabilityPlanCompare.getSelectedGroupDetails());
		assertEquals(3, lifePlanCompare.getAvailableGroupDetails().size());
		assertEquals(3, disabilityPlanCompare.getAvailableGroupDetails().size());
	}

	private Map<String, AdditionalBenefitEmployeeDetails> prepareEmployeeSelection() {
		Map<String, AdditionalBenefitEmployeeDetails> empSelections = new HashMap<String, AdditionalBenefitEmployeeDetails>();
		AdditionalBenefitEmployeeDetails adBenEmpDetails = new AdditionalBenefitEmployeeDetails();
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID, "EMP1111", BigDecimal.valueOf(100000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID2, "EMP1111", BigDecimal.valueOf(101000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID, "EMP2222", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID2, "EMP2222", BigDecimal.valueOf(91000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID, "EMP1111", BigDecimal.valueOf(100000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID2, "EMP1111", BigDecimal.valueOf(101000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID, "EMP2222", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID2, "EMP2222", BigDecimal.valueOf(91000) );
		empSelections.put(BEN_GRP, adBenEmpDetails);

		adBenEmpDetails = new AdditionalBenefitEmployeeDetails();
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID, "23567431", BigDecimal.valueOf(70000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID2, "23567431", BigDecimal.valueOf(70000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID3, "23567431", BigDecimal.valueOf(70000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID, "23567431", BigDecimal.valueOf(70000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID2, "23567431", BigDecimal.valueOf(70000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID, "23567431", BigDecimal.valueOf(70000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID2, "23567431", BigDecimal.valueOf(70000) );

		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID, "23567432", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID2, "23567432", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID3, "23567432", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID, "23567432", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID2, "23567432", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID, "23567432", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID2, "23567432", BigDecimal.valueOf(80000) );

		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID, "23567433", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID2, "23567433", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID3, "23567433", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID, "23567433", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID2, "23567433", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID, "23567433", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID2, "23567433", BigDecimal.valueOf(90000) );

		empSelections.put(BEN_PROGRAM, adBenEmpDetails);

		adBenEmpDetails = new AdditionalBenefitEmployeeDetails();
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID, "23567500", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID2, "23567500", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID3, "23567500", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID, "23567500", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID2, "23567500", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID, "23567500", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID2, "23567500", BigDecimal.valueOf(80000) );

		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID, "23567501", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID2, "23567501", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID3, "23567501", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID, "23567501", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID2, "23567501", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID, "23567501", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID2, "23567501", BigDecimal.valueOf(90000) );

		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID, "23567502", BigDecimal.valueOf(100000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID2, "23567502", BigDecimal.valueOf(100000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID3, "23567502", BigDecimal.valueOf(100000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID, "23567502", BigDecimal.valueOf(100000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID2, "23567502", BigDecimal.valueOf(100000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID, "23567502", BigDecimal.valueOf(100000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID2, "23567502", BigDecimal.valueOf(100000) );

		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID, "23567503", BigDecimal.valueOf(110000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID2, "23567503", BigDecimal.valueOf(110000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID3, "23567503", BigDecimal.valueOf(110000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID, "23567503", BigDecimal.valueOf(110000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID2, "23567503", BigDecimal.valueOf(110000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID, "23567503", BigDecimal.valueOf(110000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID2, "23567503", BigDecimal.valueOf(110000) );

		empSelections.put(BEN_PROGRAM1, adBenEmpDetails);

		adBenEmpDetails = new AdditionalBenefitEmployeeDetails();
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID, "23567600", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID2, "23567600", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID3, "23567600", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID, "23567600", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID2, "23567600", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID, "23567600", BigDecimal.valueOf(80000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID2, "23567600", BigDecimal.valueOf(80000) );

		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID, "23567601", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID2, "23567601", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID3, "23567601", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID, "23567601", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID2, "23567601", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID, "23567601", BigDecimal.valueOf(90000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID2, "23567601", BigDecimal.valueOf(90000) );

		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID, "23567602", BigDecimal.valueOf(100000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID2, "23567602", BigDecimal.valueOf(100000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID3, "23567602", BigDecimal.valueOf(100000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID, "23567602", BigDecimal.valueOf(100000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID2, "23567602", BigDecimal.valueOf(100000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID, "23567602", BigDecimal.valueOf(100000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID2, "23567602", BigDecimal.valueOf(100000) );

		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID, "23567603", BigDecimal.valueOf(110000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID2, "23567603", BigDecimal.valueOf(110000) );
		adBenEmpDetails.addPlanEmployeeAndRate( STD_PLAN_ID3, "23567603", BigDecimal.valueOf(110000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID, "23567603", BigDecimal.valueOf(110000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LTD_PLAN_ID2, "23567603", BigDecimal.valueOf(110000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID, "23567603", BigDecimal.valueOf(110000) );
		adBenEmpDetails.addPlanEmployeeAndRate( LIFE_PLAN_ID2, "23567603", BigDecimal.valueOf(110000) );

		empSelections.put(BEN_PROGRAM2, adBenEmpDetails);

		return empSelections;
	}

	private List<BenefitGroup> prepareBenefitGroups() {
		List<BenefitGroup> benGrps = new ArrayList<BenefitGroup>();
		BenefitGroup benGrp = new BenefitGroup();
		benGrp.setId(1111);
		benGrp.setBenefitProgram(BEN_PROGRAM);
		benGrp.setName(BEN_GRP);
		benGrp.setType("K1");
		benGrps.add(benGrp);

		benGrp = new BenefitGroup();
		benGrp.setId(2222);
		benGrp.setBenefitProgram(BEN_PROGRAM1);
		benGrp.setName(BEN_GRP1);
		benGrp.setType("STD");
		benGrps.add(benGrp);

		benGrp = new BenefitGroup();
		benGrp.setId(3333);
		benGrp.setBenefitProgram(BEN_PROGRAM2);
		benGrp.setName(BEN_GRP2);
		benGrp.setType("STD");
		benGrps.add(benGrp);
		return benGrps;
	}

	private Map<String, List<AdditionalPlanRate>> preparePlanRates() {
		Map<String, List<AdditionalPlanRate>> map = new HashMap<String, List<AdditionalPlanRate>>();
		List<AdditionalPlanRate> adPlanRates = new ArrayList<AdditionalPlanRate>();
		AdditionalPlanRate adPlanRate = new AdditionalPlanRate();
		adPlanRate.setRate(BigDecimal.valueOf(100));
		adPlanRates.add(adPlanRate);
		map.put("RT1111", adPlanRates);
		map.put("RT2222", adPlanRates);
		return map;
	}

	private Set<AdditionalBenefitPlan> prepareDisabilityPlans() {
		Set<AdditionalBenefitPlan> disabilityPlans = new HashSet<AdditionalBenefitPlan>();

		AdditionalBenefitPlan adBenPlan = new AdditionalBenefitPlan();
		adBenPlan.setId("1111");
		List<DisabilityBenefitOptionPlans> optionPlans = new ArrayList<DisabilityBenefitOptionPlans>();
		DisabilityBenefitOptionPlans dbOptionPlan = new DisabilityBenefitOptionPlans();
		dbOptionPlan.setPrimaryPlan(true);
		dbOptionPlan.setId(LIFE_PLAN_ID);
		dbOptionPlan.setOfferedGroupType("K1");
		optionPlans.add(dbOptionPlan);
		dbOptionPlan = new DisabilityBenefitOptionPlans();
		dbOptionPlan.setPrimaryPlan(true);
		dbOptionPlan.setId(STD_PLAN_ID);
		dbOptionPlan.setOfferedGroupType("K1");
		optionPlans.add(dbOptionPlan);
		adBenPlan.setOptionPlans(optionPlans);
		adBenPlan.setOfferedGroupType("K1");

		disabilityPlans.add(adBenPlan);

		adBenPlan = new AdditionalBenefitPlan();
		adBenPlan.setId("2222");
		optionPlans = new ArrayList<DisabilityBenefitOptionPlans>();

		dbOptionPlan = new DisabilityBenefitOptionPlans();
		dbOptionPlan.setPrimaryPlan(true);
		dbOptionPlan.setId(STD_PLAN_ID2);
		dbOptionPlan.setOfferedGroupType("STD");
		optionPlans.add(dbOptionPlan);
		adBenPlan.setOptionPlans(optionPlans);
		adBenPlan.setOfferedGroupType("STD");

		disabilityPlans.add(adBenPlan);

		adBenPlan = new AdditionalBenefitPlan();
		adBenPlan.setId("3333");
		optionPlans = new ArrayList<DisabilityBenefitOptionPlans>();
		dbOptionPlan = new DisabilityBenefitOptionPlans();
		dbOptionPlan.setPrimaryPlan(true);
		dbOptionPlan.setId(STD_PLAN_ID3);
		dbOptionPlan.setOfferedGroupType("ALL");
		optionPlans.add(dbOptionPlan);
		adBenPlan.setOptionPlans(optionPlans);
		adBenPlan.setOfferedGroupType("ALL");

		disabilityPlans.add(adBenPlan);

		return disabilityPlans;
	}

	private Map<String, Set<StateBenefitPlan>> prepareADBenefitsAllStatePlans() {
		Map<String, Set<StateBenefitPlan>> map = new HashMap<String, Set<StateBenefitPlan>>();
		Set<StateBenefitPlan> benPlans = new HashSet<StateBenefitPlan>();
		benPlans.add(prepareStateBenPlan(Constants.LIFE_CODE, LIFE_PLAN_ID));
		map.put(Constants.LIFE_CODE, benPlans);
		benPlans = new HashSet<StateBenefitPlan>();
		benPlans.add(prepareStateBenPlan(Constants.STD_CODE, STD_PLAN_ID));
		map.put(Constants.STD_CODE, benPlans);
		benPlans = new HashSet<StateBenefitPlan>();
		benPlans.add(prepareStateBenPlan(Constants.COMMUTER_CODE, CMTR_PLAN_ID));
		map.put(Constants.COMMUTER_CODE, benPlans);
		return map;
	}

	private StateBenefitPlan prepareStateBenPlan(String planType, String benefitPlan) {
		StateBenefitPlan stateBenPlan = new StateBenefitPlan();
		stateBenPlan.setPlanType(planType);
		stateBenPlan.setBenefitPlan(benefitPlan);
		return stateBenPlan;
	}

	private Map<String, RateProperties> prepareLifeRateProps() {
		Map<String, RateProperties> rateProps = new HashMap<String, RateProperties>();
		RateProperties rateProp = new RateProperties();
		rateProp.setRateTblID("RT2222");
		rateProp.setRatePerUnit("NONE");
		rateProp.setRateType(2);
		rateProps.put(LIFE_PLAN_ID, rateProp);
		rateProps.put(LIFE_PLAN_ID2, rateProp);
		return rateProps;
	}

	private Map<String, RateProperties> prepareStdRateProps() {
		Map<String, RateProperties> map = new HashMap<String, RateProperties>();
		RateProperties rp = new RateProperties();
		rp.setRateTblID("RT2222");
		rp.setRateType(2);
		rp.setRatePerUnit("PHUN");
		rp.setBenefitPlan(STD_PLAN_ID);
		map.put(STD_PLAN_ID, rp);
		rp = new RateProperties();
		rp.setRateTblID("RT2222");
		rp.setRateType(1);
		rp.setRatePerUnit("PTHO");
		rp.setBenefitPlan(STD_PLAN_ID2);
		map.put(STD_PLAN_ID2, rp);
		rp = new RateProperties();
		rp.setRateTblID("RT2222");
		rp.setRateType(1);
		rp.setRatePerUnit("PTHO");
		rp.setBenefitPlan(STD_PLAN_ID3);
		map.put(STD_PLAN_ID3, rp);
		return map;
	}

	private Map<String, RateProperties> prepareLtdRateProps() {
		Map<String, RateProperties> map = new HashMap<String, RateProperties>();
		RateProperties rp = new RateProperties();
		rp.setRateTblID("RT2222");
		rp.setRateType(2);
		rp.setRatePerUnit("PHUN");
		map.put(LTD_PLAN_ID, rp);
		map.put(LTD_PLAN_ID2, rp);
		return map;
	}

	private Map<String, FormulaProperties> prepareLifeFormulaProps() {
		Map<String, FormulaProperties> map = new HashMap<String, FormulaProperties>();
		FormulaProperties fp = new FormulaProperties();
		fp.setFormulaID("LF1111");
		fp.setFormulaEffDt(EFFDT);
		map.put(LIFE_PLAN_ID, fp);
		map.put(LIFE_PLAN_ID2, fp);
		return map;
	}

	private Map<String, FormulaProperties> prepareStdFormulaProps() {
		Map<String, FormulaProperties> map = new HashMap<String, FormulaProperties>();
		FormulaProperties fp = new FormulaProperties();
		fp.setFormulaID("SF1111");
		fp.setFormulaEffDt(EFFDT);
		fp.setMaxBenefitBase(BigDecimal.valueOf(70000));
		map.put(STD_PLAN_ID, fp);
		fp = new FormulaProperties();
		fp.setFormulaID("SF2222");
		fp.setFormulaEffDt(EFFDT);
		fp.setMaxBenefitBase(BigDecimal.valueOf(70000));
		map.put(STD_PLAN_ID2, fp);
		fp = new FormulaProperties();
		fp.setFormulaID("SF3333");
		fp.setFormulaEffDt(EFFDT);
		fp.setMaxBenefitBase(BigDecimal.valueOf(70000));
		map.put(STD_PLAN_ID3, fp);
		return map;
	}

	private Map<String, FormulaProperties> prepareLtdFormulaProps() {
		Map<String, FormulaProperties> map = new HashMap<String, FormulaProperties>();
		FormulaProperties fp = new FormulaProperties();
		fp.setFormulaID("LTD1111");
		fp.setFormulaEffDt(EFFDT);
		map.put(LTD_PLAN_ID, fp);
		map.put(LTD_PLAN_ID2, fp);
		return map;
	}

	private Map<String, Boolean> prepareSelectedBenefits() {
		Map<String, Boolean> selectedBenefits = new HashMap<String, Boolean>();
		selectedBenefits.put(Constants.CMTR, true);
		selectedBenefits.put(Constants.DISABILITY, true);
		selectedBenefits.put(Constants.LIFE, true);
		return selectedBenefits;
	}

	private Map<String, PlanTypeDescription> preparePlanTypeDescMap() {
		Map<String, PlanTypeDescription> map = new HashMap<String, PlanTypeDescription>();
		PlanTypeDescription planTypeDesc = new PlanTypeDescription();
		planTypeDesc.setType(Constants.DISABILITY);
		map.put(Constants.DISABILITY, planTypeDesc);
		planTypeDesc = new PlanTypeDescription();
		planTypeDesc.setType(Constants.CMTR);
		map.put(Constants.COMMUTER_CODE, planTypeDesc);
		planTypeDesc = new PlanTypeDescription();
		planTypeDesc.setType(Constants.LIFE);
		map.put(Constants.LIFE_CODE, planTypeDesc);
		return map;
	}

	private Map<String, BigDecimal> preparePlanEstCostMap() {
		Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
		map.put(LIFE_PLAN_ID, BigDecimal.valueOf(10));
		map.put(CMTR_PLAN_ID, BigDecimal.valueOf(20));
		return map;
	}

	private Map<String, Set<String>> prepareAdbPlanMap() {
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();
		map.put(Constants.LTD_CODE, LTD_PLANS);
		map.put(Constants.STD_CODE, STD_PLANS);
		map.put(Constants.LIFE_CODE, LIFE_PLANS);
		return map;
	}

	private Company prepareCompany() {
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setPlanStartDate("01-JAN-2019");
		company.setHeadQuatersState("MA");
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(REALM_PLYR_ID);
		realmPlanYear.setCloneProgram(CLONE_PROG);
		realmPlanYear.setAvgSalary(BigDecimal.valueOf(80000));
		realmPlanYear.setPlanYearStart(EFFDT);
		company.setRealmPlanYear(realmPlanYear);
		Realm realm = new Realm();
		realm.setBenExchange(QUARTER);
		company.setRealm(realm);
		BandCodes bandCodes = new BandCodes();
		bandCodes.setDisBandCode(DIS_BAND_CODE);
		bandCodes.setLifeBandCode(LIFE_BAND_CODE);
		company.setBandCodes(bandCodes);
		company.setRenewalCompany(false);
		company.setPlanStartDate(PLAN_START_DATE_STR);
		company.setQuater(QUARTER);
		company.setSdiStates(Set.of("CA", "NY"));
		return company;
	}
	
	private Map<String, ActiveEligibleEECount> prepareEligibleHeadCount() {
		Map<String, ActiveEligibleEECount> returnMap = new HashMap<>();
		ActiveEligibleEECount eeCount = new ActiveEligibleEECount();
		eeCount.setBenProg(BEN_PROGRAM);
		eeCount.setPrimaryHeadCount(2);
		eeCount.setSecondaryHeadCount(4);
		eeCount.setTotalHeadCount(6);
		returnMap.put(BEN_PROGRAM, eeCount);
		return returnMap;
	}
	
	private RealmPlanYear preparePrevRealmPlanYear() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1);
		return realmPlanYear;
	}

	private Map<String, List<AdditionalBenefitPlanDto>> prepareBeneProgPlanSelection() {
		Map<String, List<AdditionalBenefitPlanDto>> beneProgPlanSelection = new HashMap<>();
		List<AdditionalBenefitPlanDto> planSelections = new ArrayList<>();
		AdditionalBenefitPlanDto planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.LIFE_CODE);
		planSelection.setBenefitPlan(LIFE_PLAN_ID);
		planSelection.setName("Life Plan 1");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(true);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.STD_CODE);
		planSelection.setBenefitPlan(STD_PLAN_ID);
		planSelection.setName("STD Plan 1 - Not SDI");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(true);
		planSelection.setBundleId(DISABILITY_BUNDLE_ID_1);
		planSelection.setBundleName(DISABILITY_BUNDLE_NAME_1);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.STD_CODE);
		planSelection.setBenefitPlan(STD_PLAN_ID2);
		planSelection.setName("STD Plan 2 - SDI");
		planSelection.setSdiPlan(true);
		planSelection.setSelected(true);
		planSelection.setBundleId(DISABILITY_BUNDLE_ID_1);
		planSelection.setBundleName(DISABILITY_BUNDLE_NAME_1);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.LTD_CODE);
		planSelection.setBenefitPlan(LTD_PLAN_ID);
		planSelection.setName("LTD Plan 1");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(true);
		planSelection.setBundleId(DISABILITY_BUNDLE_ID_1);
		planSelection.setBundleName(DISABILITY_BUNDLE_NAME_1);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.LIFE_CODE);
		planSelection.setBenefitPlan(LIFE_PLAN_ID2);
		planSelection.setName("Life Plan 2");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(false);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.STD_CODE);
		planSelection.setBenefitPlan(STD_PLAN_ID);
		planSelection.setName("STD Plan 1 - Not SDI");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(false);
		planSelection.setEmployeePaid(true);
		planSelection.setBundleId(DISABILITY_BUNDLE_ID_2);
		planSelection.setBundleName(DISABILITY_BUNDLE_NAME_2);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.LTD_CODE);
		planSelection.setBenefitPlan(LTD_PLAN_ID2);
		planSelection.setName("LTD Plan 2");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(false);
		planSelection.setEmployeePaid(true);
		planSelection.setBundleId(DISABILITY_BUNDLE_ID_2);
		planSelection.setBundleName(DISABILITY_BUNDLE_NAME_2);
		planSelections.add(planSelection);

		beneProgPlanSelection.put(BEN_PROGRAM, planSelections);

		planSelections = new ArrayList<>();
		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.LIFE_CODE);
		planSelection.setBenefitPlan(LIFE_PLAN_ID2);
		planSelection.setName("Life Plan 2");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(true);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.STD_CODE);
		planSelection.setBenefitPlan(STD_PLAN_ID);
		planSelection.setName("STD Plan 1 - Not SDI");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(true);
		planSelection.setEmployeePaid(true);
		planSelection.setBundleId(DISABILITY_BUNDLE_ID_2);
		planSelection.setBundleName(DISABILITY_BUNDLE_NAME_2);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.LTD_CODE);
		planSelection.setBenefitPlan(LTD_PLAN_ID2);
		planSelection.setName("LTD Plan 2");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(true);
		planSelection.setEmployeePaid(true);
		planSelection.setBundleId(DISABILITY_BUNDLE_ID_2);
		planSelection.setBundleName(DISABILITY_BUNDLE_NAME_2);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.LIFE_CODE);
		planSelection.setBenefitPlan(LIFE_PLAN_ID);
		planSelection.setName("Life Plan 1");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(false);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.STD_CODE);
		planSelection.setBenefitPlan(STD_PLAN_ID);
		planSelection.setName("STD Plan 1 - Not SDI");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(false);
		planSelection.setEmployeePaid(true);
		planSelection.setBundleId(DISABILITY_BUNDLE_ID_1);
		planSelection.setBundleName(DISABILITY_BUNDLE_NAME_1);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.STD_CODE);
		planSelection.setBenefitPlan(STD_PLAN_ID2);
		planSelection.setName("STD Plan 2 - SDI");
		planSelection.setSdiPlan(true);
		planSelection.setSelected(false);
		planSelection.setBundleId(DISABILITY_BUNDLE_ID_1);
		planSelection.setBundleName(DISABILITY_BUNDLE_NAME_1);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.LTD_CODE);
		planSelection.setBenefitPlan(LTD_PLAN_ID);
		planSelection.setName("LTD Plan 1");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(false);
		planSelection.setBundleId(DISABILITY_BUNDLE_ID_1);
		planSelection.setBundleName(DISABILITY_BUNDLE_NAME_1);
		planSelections.add(planSelection);

		beneProgPlanSelection.put(BEN_PROGRAM1, planSelections);

		planSelections = new ArrayList<>();
		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.LIFE_CODE);
		planSelection.setBenefitPlan(LIFE_PLAN_ID2);
		planSelection.setName("Life Plan 2");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(false);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.STD_CODE);
		planSelection.setBenefitPlan(STD_PLAN_ID);
		planSelection.setName("STD Plan 1 - Not SDI");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(false);
		planSelection.setEmployeePaid(true);
		planSelection.setBundleId(DISABILITY_BUNDLE_ID_2);
		planSelection.setBundleName(DISABILITY_BUNDLE_NAME_2);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.LTD_CODE);
		planSelection.setBenefitPlan(LTD_PLAN_ID2);
		planSelection.setName("LTD Plan 2");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(false);
		planSelection.setEmployeePaid(true);
		planSelection.setBundleId(DISABILITY_BUNDLE_ID_2);
		planSelection.setBundleName(DISABILITY_BUNDLE_NAME_2);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.LIFE_CODE);
		planSelection.setBenefitPlan(LIFE_PLAN_ID);
		planSelection.setName("Life Plan 1");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(false);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.STD_CODE);
		planSelection.setBenefitPlan(STD_PLAN_ID);
		planSelection.setName("STD Plan 1 - Not SDI");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(false);
		planSelection.setEmployeePaid(true);
		planSelection.setBundleId(DISABILITY_BUNDLE_ID_1);
		planSelection.setBundleName(DISABILITY_BUNDLE_NAME_1);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.STD_CODE);
		planSelection.setBenefitPlan(STD_PLAN_ID2);
		planSelection.setName("STD Plan 2 - SDI");
		planSelection.setSdiPlan(true);
		planSelection.setSelected(false);
		planSelection.setBundleId(DISABILITY_BUNDLE_ID_1);
		planSelection.setBundleName(DISABILITY_BUNDLE_NAME_1);
		planSelections.add(planSelection);

		planSelection = new AdditionalBenefitPlanDto();
		planSelection.setPlanType(BSSApplicationConstants.LTD_CODE);
		planSelection.setBenefitPlan(LTD_PLAN_ID);
		planSelection.setName("LTD Plan 1");
		planSelection.setSdiPlan(false);
		planSelection.setSelected(false);
		planSelection.setBundleId(DISABILITY_BUNDLE_ID_1);
		planSelection.setBundleName(DISABILITY_BUNDLE_NAME_1);
		planSelections.add(planSelection);

		beneProgPlanSelection.put(BEN_PROGRAM2, planSelections);
		beneProgPlanSelection.put(BEN_PROGRAM3, planSelections);

		return beneProgPlanSelection;
	}

	private List<CensusRes> prepareProspectEmployees() {
		List<CensusRes> prospectEmployees = new ArrayList<>();
		CensusRes censusRes = CensusRes.builder().employeeId("23567431").homeState("CA").annualWages(50000.0).build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567432").homeState("CA").annualWages(60000.0).build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567433").homeState("NY").annualWages(70000.0).build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567500").homeState("FL").annualWages(80000.0).build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567501").homeState("FL").annualWages(90000.0).build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567502").homeState("FL").annualWages(100000.0).build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567503").homeState("FL").annualWages(110000.0).build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567600").homeState("FL").annualWages(80000.0).build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567601").homeState("FL").annualWages(90000.0).build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567602").homeState("FL").annualWages(100000.0).build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567603").homeState("FL").annualWages(110000.0).build();
		prospectEmployees.add(censusRes);

		return prospectEmployees;
	}

	private	Map<String, EmployeeStrategyGroupDetails> prepareTrinetEmployeesByEmplId() {
		Map<String, EmployeeStrategyGroupDetails> trinetEmployeesByEmplId = new HashMap<>();
		EmployeeStrategyGroupDetails employeeStrategyGroupDetails = new EmployeeStrategyGroupDetails();
		employeeStrategyGroupDetails.setFutureGroupId(GROUP_ID);
		employeeStrategyGroupDetails.setFutureBenefitProgram(BEN_PROGRAM);
		trinetEmployeesByEmplId.put("23567431", employeeStrategyGroupDetails);
		trinetEmployeesByEmplId.put("23567432", employeeStrategyGroupDetails);
		trinetEmployeesByEmplId.put("23567433", employeeStrategyGroupDetails);

		employeeStrategyGroupDetails = new EmployeeStrategyGroupDetails();
		employeeStrategyGroupDetails.setFutureGroupId(GROUP_ID1);
		employeeStrategyGroupDetails.setFutureBenefitProgram(BEN_PROGRAM1);
		trinetEmployeesByEmplId.put("23567500", employeeStrategyGroupDetails);
		trinetEmployeesByEmplId.put("23567501", employeeStrategyGroupDetails);
		trinetEmployeesByEmplId.put("23567502", employeeStrategyGroupDetails);
		trinetEmployeesByEmplId.put("23567503", employeeStrategyGroupDetails);

		employeeStrategyGroupDetails = new EmployeeStrategyGroupDetails();
		employeeStrategyGroupDetails.setFutureGroupId(GROUP_ID2);
		employeeStrategyGroupDetails.setFutureBenefitProgram(BEN_PROGRAM2);
		trinetEmployeesByEmplId.put("23567600", employeeStrategyGroupDetails);
		trinetEmployeesByEmplId.put("23567601", employeeStrategyGroupDetails);
		trinetEmployeesByEmplId.put("23567602", employeeStrategyGroupDetails);
		trinetEmployeesByEmplId.put("23567603", employeeStrategyGroupDetails);

		return trinetEmployeesByEmplId;
	}

	private List<FormulaDefinition> prepareFormulaDef() {
		List<FormulaDefinition> formulaDef = new ArrayList<>();

		FormulaDefinition formulaDefinition = new FormulaDefinition();
		formulaDefinition.setBenOperand("(");
		formulaDefinition.setBnEntryTyp(" ");
		formulaDefinition.setBnValue(BigDecimal.ZERO);
		formulaDefinition.setRoundUpAmt(BigDecimal.ZERO);
		formulaDefinition.setRoundTo(BigDecimal.ZERO);
		formulaDef.add(formulaDefinition);

		formulaDefinition = new FormulaDefinition();
		formulaDefinition.setBenOperand(" ");
		formulaDefinition.setBnEntryTyp("BASE");
		formulaDefinition.setBnValue(BigDecimal.ZERO);
		formulaDefinition.setRoundUpAmt(BigDecimal.ZERO);
		formulaDefinition.setRoundTo(BigDecimal.ZERO);
		formulaDef.add(formulaDefinition);

		formulaDefinition = new FormulaDefinition();
		formulaDefinition.setBenOperand("/");
		formulaDefinition.setBnEntryTyp("CNST");
		formulaDefinition.setBnValue(BigDecimal.valueOf(12));
		formulaDefinition.setRoundUpAmt(BigDecimal.ZERO);
		formulaDefinition.setRoundTo(BigDecimal.ZERO);
		formulaDef.add(formulaDefinition);

		formulaDefinition = new FormulaDefinition();
		formulaDefinition.setBenOperand(")");
		formulaDefinition.setBnEntryTyp(" ");
		formulaDefinition.setBnValue(BigDecimal.ZERO);
		formulaDefinition.setRoundUpAmt(BigDecimal.ZERO);
		formulaDefinition.setRoundTo(BigDecimal.ZERO);
		formulaDef.add(formulaDefinition);

		return formulaDef;
	}
}
