package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.trinet.ambis.persistence.model.Realm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.impl.EmployeeBenefitGroupDaoImpl;
import com.trinet.ambis.persistence.dao.ps.LifeAndDisabilityCalcData;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyGroupEmployeePlanRateData;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectBenefitsSummaryTotalsResponse;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.impl.AdditionalBenefitPlanServiceImpl;
import com.trinet.ambis.service.impl.ProspectStrategyIntegrationServiceImpl;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.EmployeeStrategyGroupDetails;
import com.trinet.ambis.service.prospect.impl.ProspectEmployeeServiceImpl;
import com.trinet.ambis.service.prospect.response.CensusRes;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.validator.RequestValidator;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProspectStrategyIntegrationServiceTest extends ServiceUnitTest {

	@InjectMocks
	ProspectStrategyIntegrationServiceImpl prospectStrategyIntegrationService;

	@Mock
	CompanyService companyService;

	@Mock
	StrategyDataDao strategyDataDao;

	@Mock
	StrategyDao strategyDao;

	@Mock
	AdditionalBenefitPlanServiceImpl additionalBenefitPlanService;

	@Mock
	LifeAndDisabilityCalcData lifeAndDisabilityCalcData;

	@Mock
	ProspectEmployeeServiceImpl prospectEmployeeService;

	@Mock
	EmployeeBenefitGroupDaoImpl employeeBenefitGroupDao;

	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;
	private MockedStatic<RulesAndConfigsUtils> mockStaticRulesAndConfigsUtils;

	private static final String EMPLID = "0000000123456";
	private static final Long STRATEGY_ID = Long.valueOf(197547);

	private static final Long GROUP_ID_1 = Long.valueOf(4376);
	private static final String GROUP_NAME_1 = "BENPROG1";

	private static final Long GROUP_ID_2= Long.valueOf(4377);
	private static final String GROUP_NAME_2 = "BENPROG2";

	private static final Long GROUP_ID_3 = Long.valueOf(4378);
	private static final String GROUP_NAME_3 = "BENPROG3";

	@Before
	public void setup() throws ParseException {
		MockitoAnnotations.initMocks(this);
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
		mockStaticRulesAndConfigsUtils = Mockito.mockStatic(RulesAndConfigsUtils.class);
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) mockStaticBSSSecurityUtils.close();
		if (mockStaticRulesAndConfigsUtils != null) mockStaticRulesAndConfigsUtils.close();
	}

	@Test
	public void getBenefitsSummaryTotalsForStrategyTest() {

		String exchangeId = "TNIII";

		Company company = prepareCompany();
		List<StrategyGroupEmployeePlanRateData> summaryList = prepareStrategyEmployeePlanRateData();
		Optional<List<StrategyGroupEmployeePlanRateData>> summaryListOptional = Optional.ofNullable(summaryList);
		Optional<Strategy> strategy = Optional.ofNullable(prepareStrategy());

		when(strategyDataDao.getStrategyGroupPlanCostByPlanType(company, List.of(STRATEGY_ID),
				BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER)).thenReturn(summaryListOptional);
		when(strategyDao.findById(STRATEGY_ID)).thenReturn(strategy);

		// Mocking the behaviour for additonal plans
		when(strategyDataDao.getAdditionalBenefitPlansForStrategyWithSdiInfo(STRATEGY_ID, company.getPlanStartDate(),
				company.getRealmPlanYearId())).thenReturn(prepareDisabilityBenefitOptionPlans());
		when(prospectEmployeeService.getEmployees(company.getCode())).thenReturn(prepareProspectEmployees());
		when(employeeBenefitGroupDao.getEmployeeDetailsByStrategy(STRATEGY_ID)).thenReturn(prepareTrinetEmployeesByEmplId());
		when(RulesAndConfigsUtils.isVendorMappingOn(Mockito.anyLong())).thenReturn(true);
		when(lifeAndDisabilityCalcData.getGroupEmployeeSelections(company, false, STRATEGY_ID, true)).thenReturn(Collections.emptyMap());
		when(additionalBenefitPlanService.calculateAdditionalPlansCostByGroup(Mockito.any(Company.class), Mockito.anyBoolean(), Mockito.anyMap(), Mockito.anyMap())).thenReturn(prepareAdditionalPlansCostByGroup());

		ProspectBenefitsSummaryTotalsResponse response = prospectStrategyIntegrationService
				.getBenefitsSummaryTotalsForStrategy(STRATEGY_ID, company, BenExchngEnums.getByExchangeId(exchangeId));

		assertEquals("Test-Strategy", response.getStrategyName());
		assertEquals("recommended", response.getType());
		assertEquals("12345", response.getBundleId());
		assertEquals("Test Bundle", response.getBundleName());
		assertEquals(6, response.getQuotes().size());

		ProspectBenefitsSummaryTotalsResponse.Quotes quote = response.getQuotes().get(0);
		assertEquals("1", quote.getBenefitType());

		assertEquals(Long.valueOf(2), quote.getTotalCostDetails().getEmployeeCount());
		assertEquals(BigDecimal.valueOf(46.0), quote.getTotalCostDetails().getEmployeeTotalCost());
		assertEquals(BigDecimal.valueOf(246.0), quote.getTotalCostDetails().getEmployerTotalCost());

		assertNotNull(quote.getTotalCostDetails().getMonthlyCostByEmployee());
		assertEquals("23567500", quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getEeIdentifier());
		assertEquals(BigDecimal.valueOf(20.5),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getEmployee());
		assertEquals(BigDecimal.valueOf(130.5),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getEmployer());


		// LIFE
		quote = response.getQuotes().get(3);
		assertEquals("3", quote.getBenefitType());
		assertEquals(2, quote.getPlanDetails().size());
		assertEquals(BigDecimal.valueOf(660), quote.getTotalCostDetails().getTotalAnnualCost());
		assertEquals(BigDecimal.valueOf(0), quote.getTotalCostDetails().getEmployeeTotalCost());
		assertEquals(BigDecimal.valueOf(55), quote.getTotalCostDetails().getEmployerTotalCost());
		assertEquals(Long.valueOf(7), quote.getTotalCostDetails().getEmployeeCount());
		assertNotNull(quote.getTotalCostDetails().getMonthlyCostByEmployee());
		assertEquals("23567431", quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getEeIdentifier());
		assertEquals(BigDecimal.valueOf(0),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getEmployee());
		assertEquals(BigDecimal.valueOf(10),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getEmployer());

		// STD
		quote = response.getQuotes().get(4);
		assertEquals("7", quote.getBenefitType());
		assertEquals(4, quote.getPlanDetails().size());
		assertEquals(BigDecimal.valueOf(1740), quote.getTotalCostDetails().getTotalAnnualCost());
		assertEquals(BigDecimal.valueOf(0), quote.getTotalCostDetails().getEmployeeTotalCost());
		assertEquals(BigDecimal.valueOf(145), quote.getTotalCostDetails().getEmployerTotalCost());
		assertEquals(Long.valueOf(7), quote.getTotalCostDetails().getEmployeeCount());
		assertNotNull(quote.getTotalCostDetails().getMonthlyCostByEmployee());

		assertEquals("23567431", quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getEeIdentifier());
		assertEquals("STD1SDI", quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getPlanId());
		assertEquals(BigDecimal.valueOf(0),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getEmployee());
		assertEquals(BigDecimal.valueOf(30),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getEmployer());

		assertEquals("23567500", quote.getTotalCostDetails().getMonthlyCostByEmployee().get(3).getEeIdentifier());
		assertEquals("STD2", quote.getTotalCostDetails().getMonthlyCostByEmployee().get(3).getPlanId());
		assertEquals(BigDecimal.valueOf(0),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(3).getEmployee());
		assertEquals(BigDecimal.valueOf(10),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(3).getEmployer());

		// LTD
		quote = response.getQuotes().get(5);
		assertEquals("8", quote.getBenefitType());
		assertEquals(2, quote.getPlanDetails().size());
		assertEquals(BigDecimal.valueOf(1080), quote.getTotalCostDetails().getTotalAnnualCost());
		assertEquals(BigDecimal.valueOf(0), quote.getTotalCostDetails().getEmployeeTotalCost());
		assertEquals(BigDecimal.valueOf(90), quote.getTotalCostDetails().getEmployerTotalCost());
		assertEquals(Long.valueOf(7), quote.getTotalCostDetails().getEmployeeCount());
		assertNotNull(quote.getTotalCostDetails().getMonthlyCostByEmployee());
		assertEquals("23567503", quote.getTotalCostDetails().getMonthlyCostByEmployee().get(6).getEeIdentifier());
		assertEquals(BigDecimal.valueOf(0),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(6).getEmployee());
		assertEquals(BigDecimal.valueOf(10),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(6).getEmployer());


	}

	@Test
	public void getBenefitsSummaryTotalsForStrategy_NoPrimaryPlansTest() {

		String exchangeId = "TNIII";

		Company company = prepareCompany();
		Optional<List<StrategyGroupEmployeePlanRateData>> summaryListOptional = Optional.empty();
		Optional<Strategy> strategy = Optional.ofNullable(prepareStrategy());

		when(strategyDataDao.getStrategyGroupPlanCostByPlanType(company, List.of(STRATEGY_ID),
				BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER)).thenReturn(summaryListOptional);
		when(strategyDao.findById(STRATEGY_ID)).thenReturn(strategy);

		// Mocking the behaviour for additonal plans
		when(strategyDataDao.getAdditionalBenefitPlansForStrategyWithSdiInfo(STRATEGY_ID, company.getPlanStartDate(),
				company.getRealmPlanYearId())).thenReturn(prepareDisabilityBenefitOptionPlans());
		when(prospectEmployeeService.getEmployees(company.getCode())).thenReturn(prepareProspectEmployees());
		when(employeeBenefitGroupDao.getEmployeeDetailsByStrategy(STRATEGY_ID)).thenReturn(prepareTrinetEmployeesByEmplId());
		when(RulesAndConfigsUtils.isVendorMappingOn(Mockito.anyLong())).thenReturn(true);
		when(lifeAndDisabilityCalcData.getGroupEmployeeSelections(company, false, STRATEGY_ID, true)).thenReturn(Collections.emptyMap());
		when(additionalBenefitPlanService.calculateAdditionalPlansCostByGroup(Mockito.any(Company.class), Mockito.anyBoolean(), Mockito.anyMap(), Mockito.anyMap())).thenReturn(prepareAdditionalPlansCostByGroup());

		ProspectBenefitsSummaryTotalsResponse response = prospectStrategyIntegrationService
				.getBenefitsSummaryTotalsForStrategy(STRATEGY_ID, company, BenExchngEnums.getByExchangeId(exchangeId));

		assertEquals("Test-Strategy", response.getStrategyName());
		assertEquals("recommended", response.getType());
		assertEquals("12345", response.getBundleId());
		assertEquals("Test Bundle", response.getBundleName());
		assertEquals(3, response.getQuotes().size());

		// LIFE
		ProspectBenefitsSummaryTotalsResponse.Quotes quote = response.getQuotes().get(0);
		assertEquals("3", quote.getBenefitType());
		assertEquals(2, quote.getPlanDetails().size());
		assertEquals(BigDecimal.valueOf(660), quote.getTotalCostDetails().getTotalAnnualCost());
		assertEquals(BigDecimal.valueOf(0), quote.getTotalCostDetails().getEmployeeTotalCost());
		assertEquals(BigDecimal.valueOf(55), quote.getTotalCostDetails().getEmployerTotalCost());
		assertEquals(Long.valueOf(7), quote.getTotalCostDetails().getEmployeeCount());
		assertNotNull(quote.getTotalCostDetails().getMonthlyCostByEmployee());
		assertEquals("23567431", quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getEeIdentifier());
		assertEquals(BigDecimal.valueOf(0),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getEmployee());
		assertEquals(BigDecimal.valueOf(10),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getEmployer());

		// STD
		quote = response.getQuotes().get(1);
		assertEquals("7", quote.getBenefitType());
		assertEquals(4, quote.getPlanDetails().size());
		assertEquals(BigDecimal.valueOf(1740), quote.getTotalCostDetails().getTotalAnnualCost());
		assertEquals(BigDecimal.valueOf(0), quote.getTotalCostDetails().getEmployeeTotalCost());
		assertEquals(BigDecimal.valueOf(145), quote.getTotalCostDetails().getEmployerTotalCost());
		assertEquals(Long.valueOf(7), quote.getTotalCostDetails().getEmployeeCount());
		assertNotNull(quote.getTotalCostDetails().getMonthlyCostByEmployee());

		assertEquals("23567431", quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getEeIdentifier());
		assertEquals("STD1SDI", quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getPlanId());
		assertEquals(BigDecimal.valueOf(0),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getEmployee());
		assertEquals(BigDecimal.valueOf(30),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(0).getEmployer());

		assertEquals("23567500", quote.getTotalCostDetails().getMonthlyCostByEmployee().get(3).getEeIdentifier());
		assertEquals("STD2", quote.getTotalCostDetails().getMonthlyCostByEmployee().get(3).getPlanId());
		assertEquals(BigDecimal.valueOf(0),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(3).getEmployee());
		assertEquals(BigDecimal.valueOf(10),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(3).getEmployer());

		// LTD
		quote = response.getQuotes().get(2);
		assertEquals("8", quote.getBenefitType());
		assertEquals(2, quote.getPlanDetails().size());
		assertEquals(BigDecimal.valueOf(1080), quote.getTotalCostDetails().getTotalAnnualCost());
		assertEquals(BigDecimal.valueOf(0), quote.getTotalCostDetails().getEmployeeTotalCost());
		assertEquals(BigDecimal.valueOf(90), quote.getTotalCostDetails().getEmployerTotalCost());
		assertEquals(Long.valueOf(7), quote.getTotalCostDetails().getEmployeeCount());
		assertNotNull(quote.getTotalCostDetails().getMonthlyCostByEmployee());
		assertEquals("23567503", quote.getTotalCostDetails().getMonthlyCostByEmployee().get(6).getEeIdentifier());
		assertEquals(BigDecimal.valueOf(0),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(6).getEmployee());
		assertEquals(BigDecimal.valueOf(10),
				quote.getTotalCostDetails().getMonthlyCostByEmployee().get(6).getEmployer());


	}

	@Test
	public void getBenefitsSummaryTotalsForStrategy_NullBundleIdAndNameTest() {

		String exchangeId = "TNIII";

		Company company = prepareCompanyWithNullBundleIdAndName();
		List<StrategyGroupEmployeePlanRateData> summaryList = prepareStrategyEmployeePlanRateData();
		Optional<List<StrategyGroupEmployeePlanRateData>> summaryListOptional = Optional.ofNullable(summaryList);
		Optional<Strategy> strategy = Optional.ofNullable(prepareStrategy());

		when(strategyDataDao.getStrategyGroupPlanCostByPlanType(company, List.of(STRATEGY_ID),
				BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER)).thenReturn(summaryListOptional);
		when(strategyDao.findById(STRATEGY_ID)).thenReturn(strategy);

		// Mocking the behaviour for additonal plans
		when(strategyDataDao.getAdditionalBenefitPlansForStrategyWithSdiInfo(STRATEGY_ID, company.getPlanStartDate(),
				company.getRealmPlanYearId())).thenReturn(prepareDisabilityBenefitOptionPlans());
		when(prospectEmployeeService.getEmployees(company.getCode())).thenReturn(prepareProspectEmployees());
		when(employeeBenefitGroupDao.getEmployeeDetailsByStrategy(STRATEGY_ID)).thenReturn(prepareTrinetEmployeesByEmplId());
		when(RulesAndConfigsUtils.isVendorMappingOn(Mockito.anyLong())).thenReturn(true);
		when(lifeAndDisabilityCalcData.getGroupEmployeeSelections(company, false, STRATEGY_ID, true)).thenReturn(Collections.emptyMap());
		when(additionalBenefitPlanService.calculateAdditionalPlansCostByGroup(Mockito.any(Company.class), Mockito.anyBoolean(), Mockito.anyMap(), Mockito.anyMap())).thenReturn(prepareAdditionalPlansCostByGroup());

		ProspectBenefitsSummaryTotalsResponse response = prospectStrategyIntegrationService
				.getBenefitsSummaryTotalsForStrategy(STRATEGY_ID, company, BenExchngEnums.getByExchangeId(exchangeId));

		assertEquals("Test-Strategy", response.getStrategyName());
		assertEquals("recommended", response.getType());
		assertEquals(null, response.getBundleId());
		   assertEquals("", response.getBundleName());
		assertEquals(6, response.getQuotes().size());
	}

	@Test
	public void getBenefitsSummaryTotalsForStrategy_NullBundleIdOnlyTest() {

		String exchangeId = "TNIII";

		Company company = prepareCompanyWithNullBundleIdOnly();
		List<StrategyGroupEmployeePlanRateData> summaryList = prepareStrategyEmployeePlanRateData();
		Optional<List<StrategyGroupEmployeePlanRateData>> summaryListOptional = Optional.ofNullable(summaryList);
		Optional<Strategy> strategy = Optional.ofNullable(prepareStrategy());

		when(strategyDataDao.getStrategyGroupPlanCostByPlanType(company, List.of(STRATEGY_ID),
				BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER)).thenReturn(summaryListOptional);
		when(strategyDao.findById(STRATEGY_ID)).thenReturn(strategy);

		// Mocking the behaviour for additonal plans
		when(strategyDataDao.getAdditionalBenefitPlansForStrategyWithSdiInfo(STRATEGY_ID, company.getPlanStartDate(),
				company.getRealmPlanYearId())).thenReturn(prepareDisabilityBenefitOptionPlans());
		when(prospectEmployeeService.getEmployees(company.getCode())).thenReturn(prepareProspectEmployees());
		when(employeeBenefitGroupDao.getEmployeeDetailsByStrategy(STRATEGY_ID)).thenReturn(prepareTrinetEmployeesByEmplId());
		when(RulesAndConfigsUtils.isVendorMappingOn(Mockito.anyLong())).thenReturn(true);
		when(lifeAndDisabilityCalcData.getGroupEmployeeSelections(company, false, STRATEGY_ID, true)).thenReturn(Collections.emptyMap());
		when(additionalBenefitPlanService.calculateAdditionalPlansCostByGroup(Mockito.any(Company.class), Mockito.anyBoolean(), Mockito.anyMap(), Mockito.anyMap())).thenReturn(prepareAdditionalPlansCostByGroup());

		ProspectBenefitsSummaryTotalsResponse response = prospectStrategyIntegrationService
				.getBenefitsSummaryTotalsForStrategy(STRATEGY_ID, company, BenExchngEnums.getByExchangeId(exchangeId));

		assertEquals("Test-Strategy", response.getStrategyName());
		assertEquals("recommended", response.getType());
		assertEquals(null, response.getBundleId());
		assertEquals("Test Bundle", response.getBundleName());
		assertEquals(6, response.getQuotes().size());
	}

	@Test
	public void getBenefitsSummaryTotalsForStrategy_NullBundleNameOnlyTest() {

		String exchangeId = "TNIII";

		Company company = prepareCompanyWithNullBundleNameOnly();
		List<StrategyGroupEmployeePlanRateData> summaryList = prepareStrategyEmployeePlanRateData();
		Optional<List<StrategyGroupEmployeePlanRateData>> summaryListOptional = Optional.ofNullable(summaryList);
		Optional<Strategy> strategy = Optional.ofNullable(prepareStrategy());

		when(strategyDataDao.getStrategyGroupPlanCostByPlanType(company, List.of(STRATEGY_ID),
				BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER)).thenReturn(summaryListOptional);
		when(strategyDao.findById(STRATEGY_ID)).thenReturn(strategy);

		// Mocking the behaviour for additonal plans
		when(strategyDataDao.getAdditionalBenefitPlansForStrategyWithSdiInfo(STRATEGY_ID, company.getPlanStartDate(),
				company.getRealmPlanYearId())).thenReturn(prepareDisabilityBenefitOptionPlans());
		when(prospectEmployeeService.getEmployees(company.getCode())).thenReturn(prepareProspectEmployees());
		when(employeeBenefitGroupDao.getEmployeeDetailsByStrategy(STRATEGY_ID)).thenReturn(prepareTrinetEmployeesByEmplId());
		when(RulesAndConfigsUtils.isVendorMappingOn(Mockito.anyLong())).thenReturn(true);
		when(lifeAndDisabilityCalcData.getGroupEmployeeSelections(company, false, STRATEGY_ID, true)).thenReturn(Collections.emptyMap());
		when(additionalBenefitPlanService.calculateAdditionalPlansCostByGroup(Mockito.any(Company.class), Mockito.anyBoolean(), Mockito.anyMap(), Mockito.anyMap())).thenReturn(prepareAdditionalPlansCostByGroup());

		ProspectBenefitsSummaryTotalsResponse response = prospectStrategyIntegrationService
				.getBenefitsSummaryTotalsForStrategy(STRATEGY_ID, company, BenExchngEnums.getByExchangeId(exchangeId));

		assertEquals("Test-Strategy", response.getStrategyName());
		assertEquals("recommended", response.getType());
		assertEquals("12345", response.getBundleId());
		   assertEquals("", response.getBundleName());
		assertEquals(6, response.getQuotes().size());
	}


	private Company prepareCompany() {
		Company company = new Company();
		company.setCode("6PR");
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(1234L);
		company.setRealmPlanYear(rpy);
		company.setRealmPlanYearId(2002L);
		company.setHeadQuatersState("CA");
		company.setZipCode("22787");
		company.setExclusiveMedPlan("DFLT");
		company.setPlanStartDate("01/01/2020");
		company.setRenewalCompany(true);
		company.setSdiStates(Set.of("CA", "NY"));
		company.setBundleId(12345L);
		company.setBundleName("Test Bundle");
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		company.setRealm(realm);

		return company;

	}

	private Company prepareCompanyWithNullBundleIdAndName() {
		Company company = new Company();
		company.setCode("6PR");
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(1234L);
		company.setRealmPlanYear(rpy);
		company.setRealmPlanYearId(2002L);
		company.setHeadQuatersState("CA");
		company.setZipCode("22787");
		company.setExclusiveMedPlan("DFLT");
		company.setPlanStartDate("01/01/2020");
		company.setRenewalCompany(true);
		company.setSdiStates(Set.of("CA", "NY"));
		company.setBundleId(null);
		company.setBundleName(null);
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		company.setRealm(realm);

		return company;
	}

	private Company prepareCompanyWithNullBundleIdOnly() {
		Company company = new Company();
		company.setCode("6PR");
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(1234L);
		company.setRealmPlanYear(rpy);
		company.setRealmPlanYearId(2002L);
		company.setHeadQuatersState("CA");
		company.setZipCode("22787");
		company.setExclusiveMedPlan("DFLT");
		company.setPlanStartDate("01/01/2020");
		company.setRenewalCompany(true);
		company.setSdiStates(Set.of("CA", "NY"));
		company.setBundleId(null);
		company.setBundleName("Test Bundle");
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		company.setRealm(realm);

		return company;
	}

	private Company prepareCompanyWithNullBundleNameOnly() {
		Company company = new Company();
		company.setCode("6PR");
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(1234L);
		company.setRealmPlanYear(rpy);
		company.setRealmPlanYearId(2002L);
		company.setHeadQuatersState("CA");
		company.setZipCode("22787");
		company.setExclusiveMedPlan("DFLT");
		company.setPlanStartDate("01/01/2020");
		company.setRenewalCompany(true);
		company.setSdiStates(Set.of("CA", "NY"));
		company.setBundleId(12345L);
		company.setBundleName(null);
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		company.setRealm(realm);

		return company;
	}

	private Strategy prepareStrategy() {

		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		strategy.setName("Test-Strategy");
		strategy.setType(BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED);
		return strategy;
	}

	private List<StrategyGroupEmployeePlanRateData> prepareStrategyEmployeePlanRateData() {
		List<StrategyGroupEmployeePlanRateData> summaryList = new ArrayList<>();
		StrategyGroupEmployeePlanRateData planRateData = StrategyGroupEmployeePlanRateData.builder().emplId("23567431")
				.strategyId(new BigDecimal(STRATEGY_ID).longValue()).groupId(new BigDecimal(GROUP_ID_1).longValue())
				.groupName(GROUP_NAME_1).planType("10").benefitPlan("000SR7").planName("MetLife Enhanced")
				.coverageCode("C").eeRate(new BigDecimal(18.25)).erRate(new BigDecimal(120.25)).carrier("Kaiser")
				.build();
		summaryList.add(planRateData);

		StrategyGroupEmployeePlanRateData planRateData1 = StrategyGroupEmployeePlanRateData.builder().emplId("23567432")
				.strategyId(new BigDecimal(STRATEGY_ID).longValue()).groupId(new BigDecimal(GROUP_ID_2).longValue())
				.groupName(GROUP_NAME_2).planType("10").benefitPlan("000SR7").planName("MetLife Enhanced")
				.coverageCode("1").eeRate(new BigDecimal(20.25)).erRate(new BigDecimal(120.25)).carrier("Kaiser")
				.build();
		summaryList.add(planRateData1);

		StrategyGroupEmployeePlanRateData planRateData2 = StrategyGroupEmployeePlanRateData.builder().emplId("23567433")
				.strategyId(new BigDecimal(STRATEGY_ID).longValue()).groupId(new BigDecimal(GROUP_ID_3).longValue())
				.groupName(GROUP_NAME_3).planType("10").benefitPlan("000SR7").planName("MetLife Enhanced")
				.coverageCode("2").eeRate(new BigDecimal(25.50)).erRate(new BigDecimal(110.50)).carrier("Aetna")
				.build();
		summaryList.add(planRateData2);

		StrategyGroupEmployeePlanRateData planRateData3 = StrategyGroupEmployeePlanRateData.builder().emplId("23567500")
				.strategyId(new BigDecimal(STRATEGY_ID).longValue()).groupId(new BigDecimal(GROUP_ID_3).longValue())
				.groupName(GROUP_NAME_3).planType("11").benefitPlan("000SR7").planName("MetLife Enhanced")
				.coverageCode("2").eeRate(new BigDecimal(20.50)).erRate(new BigDecimal(130.50)).carrier("Aetna")
				.build();
		summaryList.add(planRateData3);

		StrategyGroupEmployeePlanRateData planRateData4 = StrategyGroupEmployeePlanRateData.builder().emplId("23567501")
				.strategyId(new BigDecimal(STRATEGY_ID).longValue()).groupId(new BigDecimal(GROUP_ID_3).longValue())
				.groupName(GROUP_NAME_3).planType("11").benefitPlan("000SR7").planName("MetLife Enhanced")
				.coverageCode("2").eeRate(new BigDecimal(25.50)).erRate(new BigDecimal(115.50)).carrier("").build();
		summaryList.add(planRateData4);

		StrategyGroupEmployeePlanRateData planRateData5 = StrategyGroupEmployeePlanRateData.builder().emplId("23567502")
				.strategyId(new BigDecimal(STRATEGY_ID).longValue()).groupId(new BigDecimal(GROUP_ID_3).longValue())
				.groupName(GROUP_NAME_3).planType("14").benefitPlan("000SR7").planName("MetLife Enhanced")
				.coverageCode("2").eeRate(new BigDecimal(21.50)).erRate(new BigDecimal(120.50)).build();
		summaryList.add(planRateData5);

		StrategyGroupEmployeePlanRateData planRateData6 = StrategyGroupEmployeePlanRateData.builder().emplId("23567503")
				.strategyId(new BigDecimal(STRATEGY_ID).longValue()).groupId(new BigDecimal(GROUP_ID_3).longValue())
				.groupName(GROUP_NAME_3).planType("14").benefitPlan("000SR7").planName("MetLife Enhanced")
				.coverageCode("2").eeRate(new BigDecimal(27.50)).erRate(new BigDecimal(110.50)).build();
		summaryList.add(planRateData6);

		return summaryList;
	}

	private Map<Long, List<DisabilityBenefitOptionPlans>> prepareDisabilityBenefitOptionPlans() {
		Map<Long, List<DisabilityBenefitOptionPlans>> additionalBenefitPlansByGroup = new HashMap<>();

		//Group 1
		List<DisabilityBenefitOptionPlans> disabilityBenefitOptionPlans = new ArrayList<>();
		DisabilityBenefitOptionPlans disabilityBenefitOptionPlan = new DisabilityBenefitOptionPlans();
		disabilityBenefitOptionPlan.setPlanType(BSSApplicationConstants.LIFE_CODE);
		disabilityBenefitOptionPlan.setId("LIFE1");
		disabilityBenefitOptionPlan.setPlanDesc("Basic Life 1");
		disabilityBenefitOptionPlan.setCarrierName("Metlife");
		disabilityBenefitOptionPlan.setSdiPlan(false);
		disabilityBenefitOptionPlans.add(disabilityBenefitOptionPlan);

		disabilityBenefitOptionPlan = new DisabilityBenefitOptionPlans();
		disabilityBenefitOptionPlan.setPlanType(BSSApplicationConstants.STD_CODE);
		disabilityBenefitOptionPlan.setId("STD1");
		disabilityBenefitOptionPlan.setPlanDesc("STD Plan 1");
		disabilityBenefitOptionPlan.setCarrierName("Aetna");
		disabilityBenefitOptionPlan.setSdiPlan(false);
		disabilityBenefitOptionPlans.add(disabilityBenefitOptionPlan);

		disabilityBenefitOptionPlan = new DisabilityBenefitOptionPlans();
		disabilityBenefitOptionPlan.setPlanType(BSSApplicationConstants.STD_CODE);
		disabilityBenefitOptionPlan.setId("STD1SDI");
		disabilityBenefitOptionPlan.setPlanDesc("STD SDI Plan 1");
		disabilityBenefitOptionPlan.setCarrierName("Aetna");
		disabilityBenefitOptionPlan.setSdiPlan(true);
		disabilityBenefitOptionPlans.add(disabilityBenefitOptionPlan);

		disabilityBenefitOptionPlan = new DisabilityBenefitOptionPlans();
		disabilityBenefitOptionPlan.setPlanType(BSSApplicationConstants.LTD_CODE);
		disabilityBenefitOptionPlan.setId("LTD1");
		disabilityBenefitOptionPlan.setPlanDesc("LTD Plan 1");
		disabilityBenefitOptionPlan.setCarrierName("Aetna");
		disabilityBenefitOptionPlan.setSdiPlan(false);
		disabilityBenefitOptionPlans.add(disabilityBenefitOptionPlan);
		additionalBenefitPlansByGroup.put(GROUP_ID_1, disabilityBenefitOptionPlans);

		//Group 2
		disabilityBenefitOptionPlans = new ArrayList<>();
		disabilityBenefitOptionPlan = new DisabilityBenefitOptionPlans();
		disabilityBenefitOptionPlan.setPlanType(BSSApplicationConstants.LIFE_CODE);
		disabilityBenefitOptionPlan.setId("LIFE2");
		disabilityBenefitOptionPlan.setPlanDesc("Basic Life 2");
		disabilityBenefitOptionPlan.setCarrierName("Metlife");
		disabilityBenefitOptionPlan.setSdiPlan(false);
		disabilityBenefitOptionPlans.add(disabilityBenefitOptionPlan);

		disabilityBenefitOptionPlan = new DisabilityBenefitOptionPlans();
		disabilityBenefitOptionPlan.setPlanType(BSSApplicationConstants.STD_CODE);
		disabilityBenefitOptionPlan.setId("STD2");
		disabilityBenefitOptionPlan.setPlanDesc("STD Plan 2");
		disabilityBenefitOptionPlan.setCarrierName("Aetna");
		disabilityBenefitOptionPlan.setSdiPlan(false);
		disabilityBenefitOptionPlans.add(disabilityBenefitOptionPlan);

		disabilityBenefitOptionPlan = new DisabilityBenefitOptionPlans();
		disabilityBenefitOptionPlan.setPlanType(BSSApplicationConstants.STD_CODE);
		disabilityBenefitOptionPlan.setId("STD2SDI");
		disabilityBenefitOptionPlan.setPlanDesc("STD SDI Plan 2");
		disabilityBenefitOptionPlan.setCarrierName("Aetna");
		disabilityBenefitOptionPlan.setSdiPlan(true);
		disabilityBenefitOptionPlans.add(disabilityBenefitOptionPlan);

		disabilityBenefitOptionPlan = new DisabilityBenefitOptionPlans();
		disabilityBenefitOptionPlan.setPlanType(BSSApplicationConstants.LTD_CODE);
		disabilityBenefitOptionPlan.setId("LTD2");
		disabilityBenefitOptionPlan.setPlanDesc("LTD Plan 2");
		disabilityBenefitOptionPlan.setCarrierName("Aetna");
		disabilityBenefitOptionPlan.setSdiPlan(false);
		disabilityBenefitOptionPlans.add(disabilityBenefitOptionPlan);
		additionalBenefitPlansByGroup.put(GROUP_ID_2, disabilityBenefitOptionPlans);

		// Group 3 - Same as group 2
		additionalBenefitPlansByGroup.put(GROUP_ID_3, disabilityBenefitOptionPlans);

		return additionalBenefitPlansByGroup;
	}

	private List<CensusRes> prepareProspectEmployees() {
		List<CensusRes> prospectEmployees = new ArrayList<>();
		CensusRes censusRes = CensusRes.builder().employeeId("23567431").homeState("CA").build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567432").homeState("CA").build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567433").homeState("NY").build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567500").homeState("FL").build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567501").homeState("FL").build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567502").homeState("FL").build();
		prospectEmployees.add(censusRes);

		censusRes = CensusRes.builder().employeeId("23567503").homeState("FL").build();
		prospectEmployees.add(censusRes);

		return prospectEmployees;
	}

	private	Map<String, EmployeeStrategyGroupDetails> prepareTrinetEmployeesByEmplId() {
		Map<String, EmployeeStrategyGroupDetails> trinetEmployeesByEmplId = new HashMap<>();
		EmployeeStrategyGroupDetails employeeStrategyGroupDetails = new EmployeeStrategyGroupDetails();
		employeeStrategyGroupDetails.setFutureGroupId(GROUP_ID_1);
		employeeStrategyGroupDetails.setFutureBenefitProgram(GROUP_NAME_1);
		trinetEmployeesByEmplId.put("23567431", employeeStrategyGroupDetails);

		employeeStrategyGroupDetails = new EmployeeStrategyGroupDetails();
		employeeStrategyGroupDetails.setFutureGroupId(GROUP_ID_2);
		employeeStrategyGroupDetails.setFutureBenefitProgram(GROUP_NAME_2);
		trinetEmployeesByEmplId.put("23567432", employeeStrategyGroupDetails);

		employeeStrategyGroupDetails = new EmployeeStrategyGroupDetails();
		employeeStrategyGroupDetails.setFutureGroupId(GROUP_ID_3);
		employeeStrategyGroupDetails.setFutureBenefitProgram(GROUP_NAME_3);
		trinetEmployeesByEmplId.put("23567433", employeeStrategyGroupDetails);
		trinetEmployeesByEmplId.put("23567500", employeeStrategyGroupDetails);
		trinetEmployeesByEmplId.put("23567501", employeeStrategyGroupDetails);
		trinetEmployeesByEmplId.put("23567502", employeeStrategyGroupDetails);
		trinetEmployeesByEmplId.put("23567503", employeeStrategyGroupDetails);

		return trinetEmployeesByEmplId;
	}

	private Map<String, Map<String, BigDecimal>> prepareAdditionalPlansCostByGroup() {
		Map<String, Map<String, BigDecimal>> additionalPlansCostByGroup = new HashMap<>();
		Map<String, BigDecimal> additionalPlansCost = new HashMap<>();
		additionalPlansCost.put("LIFE1", BigDecimal.valueOf(10));
		additionalPlansCost.put("STD1", BigDecimal.valueOf(15));
		additionalPlansCost.put("STD1SDI", BigDecimal.valueOf(30));
		additionalPlansCost.put("LTD1", BigDecimal.valueOf(20));
		additionalPlansCostByGroup.put(GROUP_NAME_1, additionalPlansCost);

		additionalPlansCost = new HashMap<>();
		additionalPlansCost.put("LIFE2", BigDecimal.valueOf(20));
		additionalPlansCost.put("STD2", BigDecimal.valueOf(30));
		additionalPlansCost.put("STD2SDI", BigDecimal.valueOf(60));
		additionalPlansCost.put("LTD2", BigDecimal.valueOf(20));
		additionalPlansCostByGroup.put(GROUP_NAME_2, additionalPlansCost);

		additionalPlansCost = new HashMap<>();
		additionalPlansCost.put("LIFE2", BigDecimal.valueOf(5));
		additionalPlansCost.put("STD2", BigDecimal.valueOf(10));
		additionalPlansCost.put("STD2SDI", BigDecimal.valueOf(15));
		additionalPlansCost.put("LTD2", BigDecimal.valueOf(10));
		additionalPlansCostByGroup.put(GROUP_NAME_3, additionalPlansCost);

		return additionalPlansCostByGroup;
	}
}
