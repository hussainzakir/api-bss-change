package com.trinet.ambis.service.unit;

import static com.trinet.ambis.common.BSSApplicationConstants.MEDICAL_PLAN_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.service.BenefitPlanService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.AdditionalBenefitServiceHelper;
import com.trinet.ambis.helper.BenefitGroupServiceHelper;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeStrategyGroupTransactionDao;
import com.trinet.ambis.persistence.dao.hrp.MandatoryRegionDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyHsaFundingDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.dao.ps.LifeAndDisabilityCalcData;
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyFundingModel;
import com.trinet.ambis.persistence.model.StrategyGroupHeadCount;
import com.trinet.ambis.persistence.model.StrategyRegion;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.persistence.projections.PlanSelectionDetail;
import com.trinet.ambis.persistence.sp.GetNextEligRulesId;
import com.trinet.ambis.persistence.sp.NextBenProgram;
import com.trinet.ambis.persistence.sp.NextRateTblID;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.rest.controllers.dto.StrategyCostRes;
import com.trinet.ambis.service.AdditionalBenefitPlanService;
import com.trinet.ambis.service.BenefitClassService;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.CacheTemplateService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ContributionService;
import com.trinet.ambis.service.DisabilityOptionService;
import com.trinet.ambis.service.EmployeeBenefitGroupService;
import com.trinet.ambis.service.EmployeeDataService;
import com.trinet.ambis.service.EmployeePlanAssignmentService;
import com.trinet.ambis.service.EmployerEmployeePlansMappingService;
import com.trinet.ambis.service.HeadCountService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.PortfolioService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.ProspectDefaultPlanAssignmentService;
import com.trinet.ambis.service.ProspectStrategyService;
import com.trinet.ambis.service.RealTimeSyncService;
import com.trinet.ambis.service.RealmPlanYearRuleService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.StrategyFundingModelService;
import com.trinet.ambis.service.StrategyGroupHeadCountService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategyHistoryService;
import com.trinet.ambis.service.StrategyHsaFundingService;
import com.trinet.ambis.service.StrategyRenewalService;
import com.trinet.ambis.service.StrategySyncService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.impl.StrategyServiceImpl;
import com.trinet.ambis.service.impl.TibRateServiceImpl;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.AdditionalBenefitEmployeeDetails;
import com.trinet.ambis.service.model.AdditionalBenefitOffer;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.BenefitOfferSummary;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.model.PlanTypeDescription;
import com.trinet.ambis.service.model.StrategyBenefitGroup;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;
import com.trinet.ambis.service.model.StrategySummary;
import com.trinet.ambis.service.prospect.ProspectPlanHeadCountService;
import com.trinet.ambis.test.config.TestHelper;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.FeatureFlagUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.StrategyUtils;
import com.trinet.ambis.util.Utils;
import com.trinet.ambis.validator.RequestValidator;

@RunWith(MockitoJUnitRunner.class)
public class StrategyServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	StrategyServiceImpl strategyService;

	@Mock
	CompanyService companyService;

	@Mock
	BenefitGroupService benefitGroupService;

	@Mock
	StrategyGroupService strategyGroupService;

	@Mock
	StrategyGroupHeadCountService strategyGroupHeadCountService;

	@Mock
	StrategyHsaFundingService strategyHsaFundingService;

	@Mock
	RealmPlanYearRuleService realmPlanYearRuleService;

	@Mock
	RealmPlanYearService realmPlanYearService;

	@Mock
	StrategyRenewalService strategyRenewalService;

	@Mock
	PlanSelectionService planSelectionService;

	@Mock
	ContributionService contributionService;

	@Mock
	StrategyFundingModelService strategyFundingModelService;

	@Mock
	EmployeeBenefitGroupService employeeBenefitGroupService;

	@Mock
	EmployeeDataService employeeDataService;

	@Mock
	PlanRatesService planRatesService;

	@Mock
	DisabilityOptionService disabilityOptionService;

	@Mock
	SubmitStatusService submitStatusService;

	@Mock
	LifeAndDisabilityCalcData lifeAndDisabilityCalcData;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	StrategyHsaFundingDao strategyHsaFundingDao;

	@Mock
	MandatoryRegionDao mandatoryRegionDao;

	@Mock
	StrategyDataDao strategyDataDao;

	@Mock
	EmployeeStrategyGroupTransactionDao employeeStrategyGroupTransactionDao;

	@Mock
	StrategyDao strategyDao;

	@Mock
	RenewalDataDao renewalDataDao;

	@Mock
	RealmPlanYearDao realmPlanYearDao;
	
	@Mock
	StrategySyncService strategySyncService;

	@Mock
	NextBenProgram nextBenProgram;

	@Mock
	TibRateServiceImpl tibRateService;

	@Mock
	GetNextEligRulesId getNextEligRulesId;

	@Mock
	NextRateTblID nextRateTblID;

	@Mock
	private HttpServletRequest request;

	@Mock
	ProcessStatusService processStatusService;

	@Mock
	BenefitClassService benefitClassService;
	
	@Mock
	RealmPlyrPlanService realmPlyrPlanService;

	@Mock
	BenefitPlanService benefitPlanService;

	@Mock
	PortfolioRuleDao portfolioRuleDao;

	@Mock
	EmployerEmployeePlansMappingService employerEmployeePlansMappingService;

	@Mock
	AdditionalBenefitPlanService additionalBenefitPlanService;
	
	@Mock
	StrategyFundingDataDao strategyFundingDataDao;

	@Mock
	BenefitPlanDao benefitPlanDao;

	@Mock
	XbssRealmPlyrPlanDao realmPlyrPlanDao;

	@Mock
	StrategyHistoryService strategyHistoryService;
	
	@Mock
	HeadCountService headCountService;

	@Mock
	ProspectPlanHeadCountService prospectPlanHeadCountService;

	@Mock
	RealTimeSyncService realTimeSyncService;
	
	@Mock
	ProspectStrategyService prospectStrategyService;
	
	@Mock
	ProspectDefaultPlanAssignmentService prospectDefaultPlanAssignmentService;
	
	@Mock
	EmployeePlanAssignmentService employeePlanAssignmentService;

	@Mock
	CacheTemplateService cacheTemplateService;

	@Mock
	PortfolioService portfolioService;

	@Mock
	HrpDao hrpDao;

	@Captor
	ArgumentCaptor<Strategy> strategyArgCaptor;
	
	private static final int SUMMARY1_HEADCOUNT = 2;
	private static final boolean SUMMARY1_ISACA_FPL_OPTD = true;
	private static final long STRATEGY_ID = 2222;

	private static final long COMPANY_ID = 1111;
	private static final String COMPANY_CODE = "G48";
	private static final long REALM_PLYR_ID = 11;
	private static final long PREV_REALM_PLYR_ID = 10;
	private static final long BEN_GRP_ID3 = 3333;
	private static final long BEN_GRP_ID4 = 4444;
	private static final String MED_PLAN_ID1 = "MEDPLAN1";
	private static final String DEN_PLAN_ID1 = "DENPLAN1";
	private static final String VIS_PLAN_ID1 = "VISPLAN1";
	private static final long MED_PLAN_SELECTION_ID1 = 1111;
	private static final long DEN_PLAN_SELECTION_ID1 = 2222;
	private static final long AD_PLAN_SELECTION_ID1 = 4444;
	private static final long AD_PLAN_SELECTION_ID2 = 5555;
	private static final String AD_BEN_PLAN_ID1 = "ADPLANID1";
	private static final String AD_BEN_PLAN_ID2 = "ADPLANID2";
	private static final String AD_PLAN_TYPE_CMTR = "CMTR";
	private static final String AD_PLAN_TYPE_STD = "STD";
	private static final String DBO_PLAN_ID = "DBOID1";
	private static final String DBO_PLAN_TYPE = "STD";
	private static final Long DBO_PLAN_HC = 2L;
	private static final String LIFE_PLAN_ID1 = "LIFEPLANID1";
	private static final String LTD_PLAN_ID1 = "LTDPLANID1";
	private static final String STD_PLAN_ID1 = "STDPLANID1";
	private static final String STD_PLAN_ID2 = "STDPLANID2";
	private static final String STD_PLAN_ID3 = "STDPLANID3";
	private static final String COMUTER_PLAN_ID = "COMUTERPLANID";
	public static final String token = "AQIC5wM2LY4SfcyaCHj61cbE-i1pQwGjCjDszTcW7LHGX_w.*AAJTSQACMDMAAlNLABIzMTUzNjM2MDg3MDI4MDM4OTYAAlMxAAIwMQ..*";

    private MockedStatic<CompanyServiceHelper> mockStaticCompanyServiceHelper;
    private MockedStatic<BenefitGroupServiceHelper> mockStaticBenefitGroupServiceHelper;
    private MockedStatic<StrategyServiceHelper> mockStaticStrategyServiceHelper;
    private MockedStatic<AdditionalBenefitServiceHelper> mockStaticAdditionalBenefitServiceHelper;
    private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;
    private MockedStatic<RulesAndConfigsUtils> mockStaticRulesAndConfigsUtils;
    private MockedStatic<StrategyUtils> mockStaticStrategyUtils;
    private MockedStatic<RequestValidator> mockStaticRequestValidator;
    private MockedStatic<FeatureFlagUtils> mockStaticFeatureFlagUtils;
    private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;

    @Before
    public void setUp() {
        if (mockStaticCompanyServiceHelper != null) {
            mockStaticCompanyServiceHelper.close();
            mockStaticCompanyServiceHelper = null;
        }
        mockStaticCompanyServiceHelper = Mockito.mockStatic(CompanyServiceHelper.class);

        if (mockStaticBenefitGroupServiceHelper != null) {
            mockStaticBenefitGroupServiceHelper.close();
            mockStaticBenefitGroupServiceHelper = null;
        }
        mockStaticBenefitGroupServiceHelper = Mockito.mockStatic(BenefitGroupServiceHelper.class);

        if (mockStaticStrategyServiceHelper != null) {
            mockStaticStrategyServiceHelper.close();
            mockStaticStrategyServiceHelper = null;
        }
        mockStaticStrategyServiceHelper = Mockito.mockStatic(StrategyServiceHelper.class);

        if (mockStaticAdditionalBenefitServiceHelper != null) {
            mockStaticAdditionalBenefitServiceHelper.close();
            mockStaticAdditionalBenefitServiceHelper = null;
        }
        mockStaticAdditionalBenefitServiceHelper = Mockito.mockStatic(AdditionalBenefitServiceHelper.class);

        if (mockStaticBSSSecurityUtils != null) {
            mockStaticBSSSecurityUtils.close();
            mockStaticBSSSecurityUtils = null;
        }
        mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);

        if (mockStaticRulesAndConfigsUtils != null) {
            mockStaticRulesAndConfigsUtils.close();
            mockStaticRulesAndConfigsUtils = null;
        }
        mockStaticRulesAndConfigsUtils = Mockito.mockStatic(RulesAndConfigsUtils.class);

        if (mockStaticStrategyUtils != null) {
            mockStaticStrategyUtils.close();
            mockStaticStrategyUtils = null;
        }
        mockStaticStrategyUtils = Mockito.mockStatic(StrategyUtils.class);

        if (mockStaticRequestValidator != null) {
            mockStaticRequestValidator.close();
            mockStaticRequestValidator = null;
        }
        mockStaticRequestValidator = Mockito.mockStatic(RequestValidator.class);

        if (mockStaticFeatureFlagUtils != null) {
            mockStaticFeatureFlagUtils.close();
            mockStaticFeatureFlagUtils = null;
        }
        mockStaticFeatureFlagUtils = Mockito.mockStatic(FeatureFlagUtils.class);

        if (mockStaticAppRulesAndConfigsUtils != null) {
            mockStaticAppRulesAndConfigsUtils.close();
            mockStaticAppRulesAndConfigsUtils = null;
        }
        mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
    }

    @After
    public void tearDown() {
        if (mockStaticCompanyServiceHelper != null) {
            mockStaticCompanyServiceHelper.close();
            mockStaticCompanyServiceHelper = null;
        }
        if (mockStaticBenefitGroupServiceHelper != null) {
            mockStaticBenefitGroupServiceHelper.close();
            mockStaticBenefitGroupServiceHelper = null;
        }
        if (mockStaticStrategyServiceHelper != null) {
            mockStaticStrategyServiceHelper.close();
            mockStaticStrategyServiceHelper = null;
        }
        if (mockStaticAdditionalBenefitServiceHelper != null) {
            mockStaticAdditionalBenefitServiceHelper.close();
            mockStaticAdditionalBenefitServiceHelper = null;
        }
        if (mockStaticBSSSecurityUtils != null) {
            mockStaticBSSSecurityUtils.close();
            mockStaticBSSSecurityUtils = null;
        }
        if (mockStaticRulesAndConfigsUtils != null) {
            mockStaticRulesAndConfigsUtils.close();
            mockStaticRulesAndConfigsUtils = null;
        }
        if (mockStaticStrategyUtils != null) {
            mockStaticStrategyUtils.close();
            mockStaticStrategyUtils = null;
        }
        if (mockStaticRequestValidator != null) {
            mockStaticRequestValidator.close();
            mockStaticRequestValidator = null;
        }
        if (mockStaticFeatureFlagUtils != null) {
            mockStaticFeatureFlagUtils.close();
            mockStaticFeatureFlagUtils = null;
        }
        if (mockStaticAppRulesAndConfigsUtils != null) {
            mockStaticAppRulesAndConfigsUtils.close();
            mockStaticAppRulesAndConfigsUtils = null;
        }
    }
	// Strategy update
	@Test
	public void createUpdateStrategyUpdate() {
		StrategyData dto = prepareStrategyData(STRATEGY_ID, Constants.BALANCED_PACKAGE_NAME, true);
		StrategyHsaFundingDto hsaFundingDto = dto.getStrategyHsaFunding();
		Company company = prepareCompany();
		boolean updateFlag = true;
		List<BenefitGroup> benGrps = prepareBenGroups();
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		List<StrategyRegion> strategyRegions = new ArrayList<>();
		StrategyRegion strategyRegion = new StrategyRegion();
		strategyRegion.setId(STRATEGY_ID);
		strategyRegion.setRegion("MA");
		strategyRegions.add(strategyRegion);
		strategy.setStrategyRegions(strategyRegions);
		List<PlanSelection> planSelections = preparePlanSelectionsList();

		final ArgumentCaptor<Strategy> strategyArgCaptor = ArgumentCaptor.forClass(Strategy.class);
		final ArgumentCaptor<BenefitGroupStrategy> benGrpStrategyArgCaptor = ArgumentCaptor
				.forClass(BenefitGroupStrategy.class);
		final ArgumentCaptor<List<PlanSelection>> plansArgCaptor = ArgumentCaptor.forClass(List.class);

		final ArgumentCaptor<Company> companyArgCaptor = ArgumentCaptor.forClass(Company.class);
		final ArgumentCaptor<BenefitPlan> benPlanArgCaptor = ArgumentCaptor.forClass(BenefitPlan.class);
		final ArgumentCaptor<PlanSelection> planSelectionArgCaptor = ArgumentCaptor.forClass(PlanSelection.class);
		final ArgumentCaptor<List> contributionsArgCaptor = ArgumentCaptor.forClass(List.class);
		final ArgumentCaptor<StrategyFundingModel> sfmArgCaptor = ArgumentCaptor.forClass(StrategyFundingModel.class);
		when(companyService.createUpdateCompany(company)).thenReturn(company);
		when(strategyDataDao.updateStrategySubmitFlag(company.getId())).thenReturn(1);
		when(strategyDao.findByCompanyId(company.getId())).thenReturn(Arrays.asList(strategy));

		Mockito.doAnswer(new Answer<Strategy>() {
			public Strategy answer(InvocationOnMock invocation) {
				strategyArgCaptor.getValue().setId(STRATEGY_ID);
				return strategyArgCaptor.getValue();
			}
		}).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());

		when(strategyHsaFundingService.save(hsaFundingDto)).thenReturn(hsaFundingDto);
		when(benefitGroupService.getBenefitGroupByStrategy(STRATEGY_ID, "A")).thenReturn(benGrps);
		when(StrategyServiceHelper.getBenefitGroupById(Mockito.anyList(), Mockito.anyLong()))
				.thenReturn(prepareStrategyBenGrp());
		when(strategyDataDao.deleteAllPlanContributionsByBenefitgroupAndStrategy(BEN_GRP_ID3, STRATEGY_ID))
				.thenReturn(1);
		when(strategyDataDao.deleteAllPlanSelectionsByBenefitgroupAndStrategy(BEN_GRP_ID3, STRATEGY_ID)).thenReturn(1);
		when(strategyDataDao.deleteStrategyFundingsByBenefitgroupAndStrategy(BEN_GRP_ID3, STRATEGY_ID)).thenReturn(1);
		when(planSelectionService.saveAll(plansArgCaptor.capture())).thenReturn(preparePlanSelectionsList());
		when(StrategyServiceHelper.getPlanSelection(planSelections, MED_PLAN_ID1))
				.thenReturn(planSelections.get(0));
		when(StrategyServiceHelper.getPlanSelection(planSelections, DEN_PLAN_ID1))
				.thenReturn(planSelections.get(1));
		when(StrategyServiceHelper.getPlanSelection(planSelections, VIS_PLAN_ID1))
				.thenReturn(planSelections.get(2));
		doNothing().when(StrategyServiceHelper.class);
		StrategyServiceHelper.createUpdateContribution(companyArgCaptor.capture(), benPlanArgCaptor.capture(),
				planSelectionArgCaptor.capture(), contributionsArgCaptor.capture());
		doNothing().when(contributionService).saveAll(any());

		long actualResult = strategyService.createUpdateStrategy(dto, company, updateFlag);

		assertEquals(2222, actualResult);
		assertEquals(STRATEGY_ID, hsaFundingDto.getStrategyId());
		verify(strategyDataDao, times(1)).updateStrategySubmitFlag(COMPANY_ID);
		verify(strategyDataDao, times(1)).updateStrategySubmitFlag(COMPANY_ID);
		verify(strategyDao, times(1)).findByCompanyId(COMPANY_ID);
		verify(strategyDataDao, times(1)).deleteAllPlanContributionsByBenefitgroupAndStrategy(BEN_GRP_ID3, STRATEGY_ID);
		verify(strategyDataDao, times(1)).deleteAllPlanSelectionsByBenefitgroupAndStrategy(BEN_GRP_ID3, STRATEGY_ID);
		verify(strategyDataDao, times(1)).deleteStrategyFundingsByBenefitgroupAndStrategy(BEN_GRP_ID3, STRATEGY_ID);
		verify(employeeBenefitGroupService, times(0)).loadStrategyEmployeeData(Mockito.any(Company.class),
				Mockito.any(Map.class), Mockito.any(Long.class));
		verify(contributionService, times(1)).saveAll(Mockito.anyList());
		assertEquals(2, strategyArgCaptor.getValue().getStrategyRegions().size());
		assertTrue(Arrays.asList("CA", "GA").containsAll(strategyArgCaptor.getValue().getStrategyRegions().stream()
				.map(e -> e.getRegion()).collect(Collectors.toList())));
	}

	// Strategy create new
	@Test
	public void createUpdateStrategyCreateNew() {
		StrategyData dto = prepareStrategyData(STRATEGY_ID, Constants.CONSERVATIVE_PACKAGE_NAME, false);
		StrategyHsaFundingDto hsaFundingDto = dto.getStrategyHsaFunding();
		Company company = prepareCompany();
		boolean updateFlag = false;
		List<BenefitGroup> benGrps = prepareBenGroups1();
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		List<PlanSelection> planSelections = preparePlanSelectionsList();

		final ArgumentCaptor<Strategy> strategyArgCaptor = ArgumentCaptor.forClass(Strategy.class);
		final ArgumentCaptor<BenefitGroupStrategy> benGrpStrategyArgCaptor = ArgumentCaptor
				.forClass(BenefitGroupStrategy.class);
		final ArgumentCaptor<List<PlanSelection>> plansArgCaptor = ArgumentCaptor.forClass(List.class);

		final ArgumentCaptor<Company> companyArgCaptor = ArgumentCaptor.forClass(Company.class);
		final ArgumentCaptor<BenefitPlan> benPlanArgCaptor = ArgumentCaptor.forClass(BenefitPlan.class);
		final ArgumentCaptor<PlanSelection> planSelectionArgCaptor = ArgumentCaptor.forClass(PlanSelection.class);
		final ArgumentCaptor<List> contributionsArgCaptor = ArgumentCaptor.forClass(List.class);
		final ArgumentCaptor<StrategyFundingModel> sfmArgCaptor = ArgumentCaptor.forClass(StrategyFundingModel.class);
		when(companyService.createUpdateCompany(company)).thenReturn(company);
		when(strategyDao.findByCompanyId(company.getId())).thenReturn(Arrays.asList(strategy));

		Mockito.doAnswer(new Answer<Strategy>() {
			public Strategy answer(InvocationOnMock invocation) {
				strategyArgCaptor.getValue().setId(STRATEGY_ID);
				return strategyArgCaptor.getValue();
			}
		}).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
		when(strategyHsaFundingService.save(hsaFundingDto)).thenReturn(hsaFundingDto);
		when(benefitGroupService.getBenefitGroupByStrategy(STRATEGY_ID, BSSApplicationConstants.ACTIVE_PENDING_STATUS)).thenReturn(benGrps);
		when(StrategyServiceHelper.getBenefitGroupByCompare(Mockito.any(List.class),
				Mockito.any(BenefitGroup.class))).thenReturn(prepareStrategyBenGrp());
		when(BenefitGroupServiceHelper.prepareStrategyGroupHeadCountObj(Mockito.anyMap(), Mockito.anyLong()))
				.thenReturn(prepareStrategyGrpHeadCount());
		when(planSelectionService.saveAll(plansArgCaptor.capture())).thenReturn(preparePlanSelectionsList());
		when(StrategyServiceHelper.getPlanSelection(planSelections, MED_PLAN_ID1))
				.thenReturn(planSelections.get(0));
		when(StrategyServiceHelper.getPlanSelection(planSelections, DEN_PLAN_ID1))
				.thenReturn(planSelections.get(1));
		when(StrategyServiceHelper.getPlanSelection(planSelections, VIS_PLAN_ID1))
				.thenReturn(planSelections.get(2));
		doNothing().when(StrategyServiceHelper.class);
		StrategyServiceHelper.createUpdateContribution(companyArgCaptor.capture(), benPlanArgCaptor.capture(),
				planSelectionArgCaptor.capture(), contributionsArgCaptor.capture());
		when(BenefitGroupServiceHelper.prepareStrategyGroupHeadCountObj(Mockito.anyMap(), Mockito.anyLong()))
				.thenReturn(prepareStrategyGrpHeadCount());
		doNothing().when(contributionService).saveAll(any());

		Mockito.doAnswer(new Answer<BenefitGroupStrategy>() {
			public BenefitGroupStrategy answer(InvocationOnMock invocation) {
				benGrpStrategyArgCaptor.getValue().setId(3333);
				return benGrpStrategyArgCaptor.getValue();
			}
		}).when(strategyGroupService).saveBenefitGroupStrategy(benGrpStrategyArgCaptor.capture());

		doNothing().when(strategyFundingModelService).createUpdateFunding(sfmArgCaptor.capture());
		when(StrategyServiceHelper.constructPlanSelection(Mockito.anyLong(), Mockito.anyLong(),
				Mockito.any(BenefitPlan.class), Mockito.anyLong())).thenReturn(new PlanSelection());
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn("0000");

		long actualResult = strategyService.createUpdateStrategy(dto, company, updateFlag);

		assertEquals(2222, actualResult);
		assertEquals(STRATEGY_ID, hsaFundingDto.getStrategyId());
		verify(strategyDataDao, times(0)).updateStrategySubmitFlag(Mockito.anyLong());
		verify(strategyDao, times(1)).findByCompanyId(COMPANY_ID);
		verify(strategyDao, times(0)).findById(Mockito.anyLong());
		verify(strategyHsaFundingService, times(1)).save(Mockito.any(StrategyHsaFundingDto.class));
		verify(strategyDataDao, times(0)).deleteAllPlanContributionsByBenefitgroupAndStrategy(Mockito.anyLong(),
				Mockito.anyLong());
		verify(strategyDataDao, times(0)).deleteAllPlanSelectionsByBenefitgroupAndStrategy(Mockito.anyLong(),
				Mockito.anyLong());
		verify(strategyDataDao, times(0)).deleteStrategyFundingsByBenefitgroupAndStrategy(Mockito.anyLong(),
				Mockito.anyLong());
		verify(employeeBenefitGroupService, times(1)).loadStrategyEmployeeData(Mockito.any(Company.class),
				Mockito.any(Map.class), Mockito.any(Long.class));

		verify(StrategyServiceHelper.class, VerificationModeFactory.times(3));
		StrategyServiceHelper.constructPlanSelection(Mockito.anyLong(), Mockito.anyLong(),
				Mockito.any(BenefitPlan.class), Mockito.anyLong());
		verify(contributionService, times(1)).saveAll(Mockito.anyList());
		verify(strategyFundingModelService, times(1)).createUpdateFunding(Mockito.any(StrategyFundingModel.class));
		assertEquals(3, strategyArgCaptor.getValue().getStrategyRegions().size());
		assertTrue(Arrays.asList("CA", "MA", "GA").containsAll(strategyArgCaptor.getValue().getStrategyRegions().stream()
				.map(e -> e.getRegion()).collect(Collectors.toList())));
	}
	
	// Strategy create new duplicate name
	@Test
	public void createUpdateStrategyDuplicateName() {
		StrategyData dto = prepareStrategyData(STRATEGY_ID, Constants.CONSERVATIVE_PACKAGE_NAME, false);
		dto.getStrategySummary().setName("Strategy 1");
		Strategy strategy1 = new Strategy();
		strategy1.setName("Strategy 1");
		Strategy strategy2 = new Strategy();
		strategy2.setName("Strategy 2");
		Company company = prepareCompany();
		List<Strategy> strategies = new ArrayList<>(List.of(strategy1, strategy2));

		when(strategyDao.findByCompanyId(COMPANY_ID)).thenReturn(strategies);

		Exception e = null;
		try {
			strategyService.createUpdateStrategy(dto, company, false);
		} catch (Exception ex) {
			e = ex;
		}
		assertNotNull("Not duplicate strategy name exception occured", e);
		assertEquals("Duplicate strategy name", e.getLocalizedMessage());
		assertEquals(BSSApplicationException.class, e.getClass());
	}

	@Test
	public void createUpdateStrategy_forTibProspect() {
	    TypeReference<StrategyData> type = new TypeReference<StrategyData>() {
	    };
	    StrategyData dto = TestHelper.readPlanComparisonRequest("/strategySummary/NewStrategySummarySave.json", type)
		    .get();
	    Company company = prepareCompany();
	    company.setProspectCompany(true);
	    Mockito.doAnswer(new Answer<Strategy>() {
		public Strategy answer(InvocationOnMock invocation) {
		    strategyArgCaptor.getValue().setId(STRATEGY_ID);
		    return strategyArgCaptor.getValue();
		}
	    }).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
	    when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class))).thenReturn(
		    prepareBenGroups().get(0));
	    Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
	    existingMedStrategyPortfolioMap.put(Long.valueOf(2), true);
	    existingMedStrategyPortfolioMap.put(Long.valueOf(5), false);

	    Map<Long, Boolean> newMedStrategyPortfolioMap = new HashMap<>();
	    newMedStrategyPortfolioMap.put(Long.valueOf(9), true);
	    newMedStrategyPortfolioMap.put(Long.valueOf(3), false);
	    when(portfolioRuleDao.getMedicalPortfoliosBy(STRATEGY_ID, company.getRealmPlanYearId(),
		    company.getHeadQuatersState())).thenReturn(existingMedStrategyPortfolioMap, newMedStrategyPortfolioMap);

	    //When
	    when(CompanyServiceHelper.isTibProspect(company)).thenReturn(false);
	    strategyService.createUpdateStrategy(dto, company, false);

	    //Assert
	    verify(tibRateService, never()).saveRatesPerStrategy(company, dto.getStrategySummary().getId());
	    verify(employeePlanAssignmentService, never()).copyEePlanAssignmentsFor(2222, STRATEGY_ID, "10");
		verify(employeePlanAssignmentService, never()).copyEePlanAssignmentsFor(2222, STRATEGY_ID, "11");
		verify(employeePlanAssignmentService, never()).copyEePlanAssignmentsFor(2222, STRATEGY_ID, "14");
        verify(planSelectionService, never()).syncOmsMedicalPlanSelections(2222);

	    //When
	    when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);
	    strategyService.createUpdateStrategy(dto, company, false);
	    //Assert
	    verify(tibRateService).saveRatesPerStrategy(company, dto.getStrategySummary().getId());
	    verify(employeePlanAssignmentService, times(1)).copyEePlanAssignmentsFor(2222, STRATEGY_ID, "10");
		verify(employeePlanAssignmentService, times(1)).copyEePlanAssignmentsFor(2222, STRATEGY_ID, "11");
		verify(employeePlanAssignmentService, times(1)).copyEePlanAssignmentsFor(2222, STRATEGY_ID, "14");
        verify(planSelectionService, times(1)).syncOmsMedicalPlanSelections(2222);

	}

	// Create default strategy
	@Test
	public void createUpdateStrategyCreateDefaultStrategy() {
		StrategyData dto = prepareStrategyData(1L, Constants.TOP_QUALITY_NAME, false);
		dto.setBenefitGroups(prepareStrategyBenGroupsBsupp());
		Company company = prepareCompany();
		boolean updateFlag = false;

		Mockito.doAnswer(new Answer<Strategy>() {
			public Strategy answer(InvocationOnMock invocation) {
				strategyArgCaptor.getValue().setId(STRATEGY_ID);
				return strategyArgCaptor.getValue();
			}
		}).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
		when(benefitGroupService.getAllBenefitGroups(COMPANY_ID, "A")).thenReturn(Arrays.asList());
		when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class)))
				.thenReturn(prepareBenGroupsBsupp().get(0));
		when(strategyGroupService.saveBenefitGroupStrategy(Mockito.any(BenefitGroupStrategy.class)))
				.thenReturn(new BenefitGroupStrategy());
		when(RequestValidator.getValidatedGroupName(Mockito.anyString())).thenReturn("GROUP_NAME");
		when(BenefitGroupServiceHelper.prepareStrategyGroupHeadCountObj(Mockito.anyMap(), Mockito.anyLong()))
				.thenReturn(prepareStrategyGrpHeadCount());

		long actualResult = strategyService.createUpdateStrategy(dto, company, updateFlag);
		assertEquals(STRATEGY_ID, actualResult);

	}
	
	// Test submission with service order - Changes to existing Life and/or Disability offering.
	@Test
	public void createUpdateStrategyValidateSubmittable() {
		StrategyData dto = prepareStrategyData(1L, Constants.TOP_QUALITY_NAME, true);
		dto.setBenefitGroups(prepareStrategyBenGroupsAdditionalBenefits());
		Company company = prepareCompany();
		company.setBenefitStartDate("01-FEB-2022");
		company.setPlanStartDate("01-MAY-2023");
		company.setActiveServiceOrder(true);
		boolean updateFlag = false;
		List<Strategy> historyStrategies = prepareHistoryStrategies();

		when(strategyDataDao.getHistoryStrategies(company.getCode(), company.getRealmPlanYear().getId()))
				.thenReturn(historyStrategies);
		when(strategyDataDao.getAdditionalBenefitPlansForStrategy(historyStrategies.get(0).getId(),
				company.getPlanStartDate())).thenReturn(prepareHistoryAdditionalOfferings());
		when(renewalDataDao.getBenefitPrograms(company.getPfClient(),
				Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT)))
						.thenReturn(prepareBenGroups());

		BSSApplicationException e = null;
		try {
			strategyService.createUpdateStrategy(dto, company, updateFlag);
		} catch (BSSApplicationException ex) {
			e = ex;
		}
		assertEquals("Failed Life or Disability Sync", e.getLocalizedMessage());
		assertEquals(BSSErrorResponseCodes.BSS_LIFE_DISABILITY_SYNC, e.getBssError().getCode());

	}
	
	@Test
	public void createUpdateStrategy_testPlanAssignmentForNewStrategy() {
		TypeReference<StrategyData> type = new TypeReference<StrategyData>() {
		};
		StrategyData dto = TestHelper.readPlanComparisonRequest("/strategySummary/NewStrategySummarySave.json", type).get();
		Company company = prepareCompany();
		company.setBenefitStartDate("01-FEB-2022");
		company.setPlanStartDate("01-MAY-2023");
		company.setProspectCompany(true);
		Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
		existingMedStrategyPortfolioMap.put(Long.valueOf(1), true);
		existingMedStrategyPortfolioMap.put(Long.valueOf(5), false);
		
		Mockito.doAnswer(new Answer<Strategy>() {
			public Strategy answer(InvocationOnMock invocation) {
				strategyArgCaptor.getValue().setId(STRATEGY_ID);
				return strategyArgCaptor.getValue();
			}
		}).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
		when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class)))
				.thenReturn(prepareBenGroups().get(0));
		when(portfolioRuleDao.getMedicalPortfoliosBy(STRATEGY_ID, company.getRealmPlanYearId(),
				company.getHeadQuatersState())).thenReturn(existingMedStrategyPortfolioMap);

		strategyService.createUpdateStrategy(dto, company, false);

		verify(prospectDefaultPlanAssignmentService, times(0)).assignDefaultPlanBy(anySet(), anySet(), anyMap(),
				anySet());
		verify(employeePlanAssignmentService, times(1)).copyEePlanAssignmentsFor(2222, STRATEGY_ID, "10");
	}
	
	@Test
	public void createUpdateStrategy_testPlanAssignmentForUpdateStrategyWithOnlyMedCarrierChange() {
		TypeReference<StrategyData> type = new TypeReference<StrategyData>() {
		};
		StrategyData dto = TestHelper.readPlanComparisonRequest("/strategySummary/NewStrategySummarySave.json", type)
				.get();
		Company company = prepareCompany();
		company.setBenefitStartDate("01-FEB-2022");
		company.setPlanStartDate("01-MAY-2023");
		company.setProspectCompany(true);
		Map<String, Set<PlanCarrier>> existingPortfolios = preparePlanCarriers();
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		strategy.setName("Strategy 1");
		List<Strategy> strategies = new ArrayList<>(List.of(strategy));
		List<PlanSelectionDetail> planSelectionDetails = new ArrayList<>();
		long groupId1 = 335718L;
		long groupId2 = 335719L;
		Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
		existingMedStrategyPortfolioMap.put(Long.valueOf(1), true);
		existingMedStrategyPortfolioMap.put(Long.valueOf(18), false);
		Map<Long, Boolean> newMedStrategyPortfolioMap = new HashMap<>();
		newMedStrategyPortfolioMap.put(Long.valueOf(5), true);
		newMedStrategyPortfolioMap.put(Long.valueOf(2), false);

		when(strategyDao.findByCompanyId(COMPANY_ID)).thenReturn(strategies);
		Mockito.doAnswer(new Answer<Strategy>() {
			public Strategy answer(InvocationOnMock invocation) {
				strategyArgCaptor.getValue().setId(STRATEGY_ID);
				return strategyArgCaptor.getValue();
			}
		}).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
		when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class)))
				.thenReturn(prepareBenGroups().get(0));
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("11").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("14").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("11").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("14").build());
		when(planSelectionService.findDistinctPlanTypeBy(Set.of(STRATEGY_ID), Set.of(335718L, 335719L)))
				.thenReturn(planSelectionDetails);
		when(portfolioRuleDao.getMedicalPortfoliosBy(STRATEGY_ID, company.getRealmPlanYearId(),
				company.getHeadQuatersState())).thenReturn(existingMedStrategyPortfolioMap, newMedStrategyPortfolioMap);

		strategyService.createUpdateStrategy(dto, company, true);

		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
				Set.of(groupId1, groupId2), newMedStrategyPortfolioMap, Set.of("10"));
	}

	@Test
	public void createUpdateStrategy_updateStrategyDefaultEEPlanAssignmentForMed_forTibProspect() {
	    assert_updateStrategyDefaultEEPlanAssignmentForMed(true, 0);
	    assert_updateStrategyDefaultEEPlanAssignmentForMed(false, 1);

	}
	
	
    @Test
    public void updateStrategyEEDefaultPlanAssignmentsTest() {
        TypeReference<StrategyData> type = new TypeReference<StrategyData>() {
        };
        StrategyData dto =
                TestHelper.readPlanComparisonRequest("/strategySummary/NewStrategySummarySave.json", type).get();
        Company company = prepareCompany();
        company.setBenefitStartDate("01-FEB-2022");
        company.setPlanStartDate("01-MAY-2023");
        company.setProspectCompany(true);

        Strategy strategy = new Strategy();
        strategy.setId(STRATEGY_ID);
        strategy.setName("Strategy 1");
        List<Strategy> strategies = new ArrayList<>(List.of(strategy));

        Mockito.doAnswer(new Answer<Strategy>() {
            public Strategy answer(InvocationOnMock invocation) {
                strategyArgCaptor.getValue().setId(STRATEGY_ID);
                return strategyArgCaptor.getValue();
            }
        }).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());

        when(strategyDao.findByCompanyId(COMPANY_ID)).thenReturn(strategies);

        when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class)))
                .thenReturn(prepareBenGroups().get(0));

        long groupId1 = 335718L;
        long groupId2 = 335719L;

        Map<Long, Boolean> newMedStrategyPortfolioMap = new HashMap<>();
        newMedStrategyPortfolioMap.put(Long.valueOf(5), true);
        newMedStrategyPortfolioMap.put(Long.valueOf(2), false);

        strategyService.createUpdateStrategy(dto, company, true);

        verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
                Set.of(groupId1, groupId2), null, Set.of("11", "1D"));

    }

	@Test
	public void createUpdateStrategy_testPlanAssignmentForUpdateStrategyWithDenVisERToEEChange() {
		TypeReference<StrategyData> type = new TypeReference<StrategyData>() {
		};
		StrategyData dto = TestHelper.readPlanComparisonRequest("/strategySummary/StrategySummaryUpdate.json", type).get();
		Company company = prepareCompany();
		company.setBenefitStartDate("01-FEB-2022");
		company.setPlanStartDate("01-MAY-2023");
		company.setProspectCompany(true);
		Map<String, Set<PlanCarrier>> existingPortfolios = preparePlanCarriers();
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		strategy.setName("Strategy 1");
		List<Strategy> strategies = new ArrayList<>(List.of(strategy));
		long groupId1 = 335718L;
		long groupId2 = 335719L;
		List<PlanSelectionDetail> planSelectionDetails = new ArrayList<>();
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("1D").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("1V").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("1D").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("1V").build());
		Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
		existingMedStrategyPortfolioMap.put(Long.valueOf(1), true);
		existingMedStrategyPortfolioMap.put(Long.valueOf(2), false);
		
		when(strategyDao.findByCompanyId(COMPANY_ID)).thenReturn(strategies);
		Mockito.doAnswer(new Answer<Strategy>() {
			public Strategy answer(InvocationOnMock invocation) {
				strategyArgCaptor.getValue().setId(STRATEGY_ID);
				return strategyArgCaptor.getValue();
			}
		}).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
		when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class)))
				.thenReturn(prepareBenGroups().get(0));
		when(planSelectionService.findDistinctPlanTypeBy(Set.of(STRATEGY_ID), Set.of(335718L, 335719L)))
				.thenReturn(planSelectionDetails);
		when(portfolioRuleDao.getMedicalPortfoliosBy(STRATEGY_ID, company.getRealmPlanYearId(),
				company.getHeadQuatersState())).thenReturn(existingMedStrategyPortfolioMap);
		
		strategyService.createUpdateStrategy(dto, company, true);

		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
				Set.of(groupId1, groupId2), null, Set.of("11", "1D"));
		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
				Set.of(groupId1, groupId2), null, Set.of("14", "1V"));
	}
	
	@Test
	public void createUpdateStrategy_testPlanAssignmentForUpdateStrategyWithDentalERToEEChange() {
		TypeReference<StrategyData> type = new TypeReference<StrategyData>() {
		};
		StrategyData dto = TestHelper.readPlanComparisonRequest("/strategySummary/StrategySummaryUpdate.json", type).get();
		Company company = prepareCompany();
		company.setBenefitStartDate("01-FEB-2022");
		company.setPlanStartDate("01-MAY-2023");
		company.setProspectCompany(true);
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		strategy.setName("Strategy 1");
		List<Strategy> strategies = new ArrayList<>(List.of(strategy));
		long groupId1 = 335718L;
		long groupId2 = 335719L;
		List<PlanSelectionDetail> planSelectionDetails = new ArrayList<>();
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("1D").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("14").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("1D").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("14").build());
		Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
		existingMedStrategyPortfolioMap.put(Long.valueOf(1), true);
		existingMedStrategyPortfolioMap.put(Long.valueOf(2), false);
		
		when(strategyDao.findByCompanyId(COMPANY_ID)).thenReturn(strategies);
		Mockito.doAnswer(new Answer<Strategy>() {
			public Strategy answer(InvocationOnMock invocation) {
				strategyArgCaptor.getValue().setId(STRATEGY_ID);
				return strategyArgCaptor.getValue();
			}
		}).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
		when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class)))
				.thenReturn(prepareBenGroups().get(0));
		when(planSelectionService.findDistinctPlanTypeBy(Set.of(STRATEGY_ID), Set.of(335718L, 335719L)))
				.thenReturn(planSelectionDetails);
		when(portfolioRuleDao.getMedicalPortfoliosBy(STRATEGY_ID, company.getRealmPlanYearId(),
				company.getHeadQuatersState())).thenReturn(existingMedStrategyPortfolioMap);
		
		strategyService.createUpdateStrategy(dto, company, true);

		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
				Set.of(groupId1, groupId2), null, Set.of("11", "1D"));
	}
	
	@Test
	public void createUpdateStrategy_testPlanAssignmentForUpdateStrategyWithVisionERToEEChange() {
		TypeReference<StrategyData> type = new TypeReference<StrategyData>() {
		};
		StrategyData dto = TestHelper.readPlanComparisonRequest("/strategySummary/StrategySummaryUpdate.json", type).get();
		Company company = prepareCompany();
		company.setBenefitStartDate("01-FEB-2022");
		company.setPlanStartDate("01-MAY-2023");
		company.setProspectCompany(true);
		Map<String, Set<PlanCarrier>> existingPortfolios = preparePlanCarriers();
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		strategy.setName("Strategy 1");
		List<Strategy> strategies = new ArrayList<>(List.of(strategy));
		long groupId1 = 335718L;
		long groupId2 = 335719L;
		List<PlanSelectionDetail> planSelectionDetails = new ArrayList<>();
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("11").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("1V").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("11").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("1V").build());
		Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
		existingMedStrategyPortfolioMap.put(Long.valueOf(1), true);
		existingMedStrategyPortfolioMap.put(Long.valueOf(2), false);
		
		when(strategyDao.findByCompanyId(COMPANY_ID)).thenReturn(strategies);
		Mockito.doAnswer(new Answer<Strategy>() {
			public Strategy answer(InvocationOnMock invocation) {
				strategyArgCaptor.getValue().setId(STRATEGY_ID);
				return strategyArgCaptor.getValue();
			}
		}).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
		when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class)))
				.thenReturn(prepareBenGroups().get(0));
		when(planSelectionService.findDistinctPlanTypeBy(Set.of(STRATEGY_ID), Set.of(335718L, 335719L)))
				.thenReturn(planSelectionDetails);
		when(portfolioRuleDao.getMedicalPortfoliosBy(STRATEGY_ID, company.getRealmPlanYearId(),
				company.getHeadQuatersState())).thenReturn(existingMedStrategyPortfolioMap);
		
		strategyService.createUpdateStrategy(dto, company, true);

		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
				Set.of(groupId1, groupId2), null, Set.of("14", "1V"));
	}
	
	@Test
	public void createUpdateStrategy_testPlanAssignmentForUpdateStrategyWithDenVisERToEEChangeOnlyOneGroup() {
		TypeReference<StrategyData> type = new TypeReference<StrategyData>() {
		};
		StrategyData dto = TestHelper.readPlanComparisonRequest("/strategySummary/StrategySummaryUpdate.json", type).get();
		Company company = prepareCompany();
		company.setBenefitStartDate("01-FEB-2022");
		company.setPlanStartDate("01-MAY-2023");
		company.setProspectCompany(true);
		Map<String, Set<PlanCarrier>> existingPortfolios = preparePlanCarriers();
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		strategy.setName("Strategy 1");
		List<Strategy> strategies = new ArrayList<>(List.of(strategy));
		long groupId1 = 335718L;
		long groupId2 = 335719L;
		List<PlanSelectionDetail> planSelectionDetails = new ArrayList<>();
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("1D").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("1V").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("11").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("14").build());
		Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
		existingMedStrategyPortfolioMap.put(Long.valueOf(1), true);
		existingMedStrategyPortfolioMap.put(Long.valueOf(2), false);
		
		when(strategyDao.findByCompanyId(COMPANY_ID)).thenReturn(strategies);
		Mockito.doAnswer(new Answer<Strategy>() {
			public Strategy answer(InvocationOnMock invocation) {
				strategyArgCaptor.getValue().setId(STRATEGY_ID);
				return strategyArgCaptor.getValue();
			}
		}).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
		when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class)))
				.thenReturn(prepareBenGroups().get(0));
//		when(portfolioRuleDao.getStrategyPortfolios(anyLong(), anyLong(), anyMap(),
//				anyString(), anyBoolean())).thenReturn(existingPortfolios);
		when(planSelectionService.findDistinctPlanTypeBy(Set.of(STRATEGY_ID), Set.of(335718L, 335719L)))
				.thenReturn(planSelectionDetails);
		when(portfolioRuleDao.getMedicalPortfoliosBy(STRATEGY_ID, company.getRealmPlanYearId(),
				company.getHeadQuatersState())).thenReturn(existingMedStrategyPortfolioMap);
		
		strategyService.createUpdateStrategy(dto, company, true);

		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
				Set.of(groupId1), null, Set.of("11", "1D"));
		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
				Set.of(groupId1), null, Set.of("14", "1V"));
	}
	
	@Test
	public void createUpdateStrategy_testPlanAssignmentForUpdateStrategyWithDenEEToEROneGroupVisEEToERAnotherGroup() {
		TypeReference<StrategyData> type = new TypeReference<StrategyData>() {
		};
		StrategyData dto = TestHelper.readPlanComparisonRequest("/strategySummary/StrategySummaryUpdate.json", type).get();
		Company company = prepareCompany();
		company.setBenefitStartDate("01-FEB-2022");
		company.setPlanStartDate("01-MAY-2023");
		company.setProspectCompany(true);
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		strategy.setName("Strategy 1");
		List<Strategy> strategies = new ArrayList<>(List.of(strategy));
		long groupId1 = 335718L;
		long groupId2 = 335719L;
		List<PlanSelectionDetail> planSelectionDetails = new ArrayList<>();
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("1D").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("14").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("11").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("1V").build());
		Map<Long, Boolean> existingMedStrategyPortfolioMap = null;
		
		when(strategyDao.findByCompanyId(COMPANY_ID)).thenReturn(strategies);
		Mockito.doAnswer(new Answer<Strategy>() {
			public Strategy answer(InvocationOnMock invocation) {
				strategyArgCaptor.getValue().setId(STRATEGY_ID);
				return strategyArgCaptor.getValue();
			}
		}).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
		when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class)))
				.thenReturn(prepareBenGroups().get(0));
		when(planSelectionService.findDistinctPlanTypeBy(Set.of(STRATEGY_ID), Set.of(335718L, 335719L)))
				.thenReturn(planSelectionDetails);
		
		strategyService.createUpdateStrategy(dto, company, true);

		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
				Set.of(groupId1), existingMedStrategyPortfolioMap, Set.of("11", "1D"));
		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
				Set.of(groupId2), existingMedStrategyPortfolioMap, Set.of("14", "1V"));
	}

//	TODO
//	@Test
	public void createUpdateStrategy_testPlanAssignmentForUpdateStrategyWithDenVisERToEEBothGroup() {
		TypeReference<StrategyData> type = new TypeReference<StrategyData>() {
		};
		StrategyData dto = TestHelper.readPlanComparisonRequest("/strategySummary/StrategySummaryUpdateERToEE.json", type).get();
		Company company = prepareCompany();
		company.setBenefitStartDate("01-FEB-2022");
		company.setPlanStartDate("01-MAY-2023");
		company.setProspectCompany(true);
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		strategy.setName("Strategy 1");
		List<Strategy> strategies = new ArrayList<>(List.of(strategy));
		long groupId1 = 335718L;
		long groupId2 = 335719L;
		List<PlanSelectionDetail> planSelectionDetails = new ArrayList<>();
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("11").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("14").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("11").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("14").build());
		Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
		existingMedStrategyPortfolioMap.put(Long.valueOf(1), true);
		existingMedStrategyPortfolioMap.put(Long.valueOf(2), false);
		
		when(strategyDao.findByCompanyId(COMPANY_ID)).thenReturn(strategies);
		Mockito.doAnswer(new Answer<Strategy>() {
			public Strategy answer(InvocationOnMock invocation) {
				strategyArgCaptor.getValue().setId(STRATEGY_ID);
				return strategyArgCaptor.getValue();
			}
		}).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
		when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class)))
				.thenReturn(prepareBenGroups().get(0));
		when(planSelectionService.findDistinctPlanTypeBy(Set.of(STRATEGY_ID), Set.of(335718L, 335719L)))
				.thenReturn(planSelectionDetails);
		when(portfolioRuleDao.getMedicalPortfoliosBy(STRATEGY_ID, company.getRealmPlanYearId(),
				company.getHeadQuatersState())).thenReturn(existingMedStrategyPortfolioMap);
		
		strategyService.createUpdateStrategy(dto, company, true);

		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
				Set.of(groupId1, groupId2), null, Set.of("11", "1D"));
		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
				Set.of(groupId1, groupId2), null, Set.of("14", "1V"));
	}
	
//	TODO
//	@Test
	public void createUpdateStrategy_testPlanAssignmentForUpdateStrategyWithDenERToEEOneGroupVisERToEEAnotherGroup() {
		TypeReference<StrategyData> type = new TypeReference<StrategyData>() {
		};
		StrategyData dto = TestHelper.readPlanComparisonRequest("/strategySummary/StrategySummaryUpdateERToEEOneGrpEEToERAnother.json", type).get();
		Company company = prepareCompany();
		company.setBenefitStartDate("01-FEB-2022");
		company.setPlanStartDate("01-MAY-2023");
		company.setProspectCompany(true);
		Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
		existingMedStrategyPortfolioMap.put(Long.valueOf(1), true);
		existingMedStrategyPortfolioMap.put(Long.valueOf(2), false);
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		strategy.setName("Strategy 1");
		List<Strategy> strategies = new ArrayList<>(List.of(strategy));
		long groupId1 = 335718L;
		long groupId2 = 335719L;
		List<PlanSelectionDetail> planSelectionDetails = new ArrayList<>();
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("11").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("1D").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("11").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("14").build());
		
		when(strategyDao.findByCompanyId(COMPANY_ID)).thenReturn(strategies);
		Mockito.doAnswer(new Answer<Strategy>() {
			public Strategy answer(InvocationOnMock invocation) {
				strategyArgCaptor.getValue().setId(STRATEGY_ID);
				return strategyArgCaptor.getValue();
			}
		}).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
		when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class)))
				.thenReturn(prepareBenGroups().get(0));
		when(planSelectionService.findDistinctPlanTypeBy(Set.of(STRATEGY_ID), Set.of(335718L, 335719L)))
				.thenReturn(planSelectionDetails);
		when(portfolioRuleDao.getMedicalPortfoliosBy(STRATEGY_ID, company.getRealmPlanYearId(),
				company.getHeadQuatersState())).thenReturn(existingMedStrategyPortfolioMap);
		
		strategyService.createUpdateStrategy(dto, company, true);

		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
				Set.of(groupId1), null, Set.of("11", "1D"));
		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
				Set.of(groupId2), null, Set.of("14", "1V"));
	}
	
	private Map<String, Set<PlanCarrier>> preparePlanCarriers() {
		Map<String, Set<PlanCarrier>> planCarriers = new HashMap<>();
		PlanCarrier planCarrier = new PlanCarrier();
		planCarrier.setId(1);
		planCarrier.setName("Aetna HDHP 3000");
		PlanCarrier planCarrier1 = new PlanCarrier();
		planCarrier1.setId(2);
		planCarrier1.setName("Blue Shield of California");
		planCarriers.put(Constants.MEDICAL, Set.of(planCarrier, planCarrier1));
		planCarriers.put(Constants.DENTAL, Set.of());
		planCarriers.put(Constants.VISION, Set.of());
		return planCarriers;
	}

	@Test
	public void getStrategiesRenewalStrategyNotCached() {
		Company company = prepareCompany();
		company.setRenewalCompany(true);
		company.getRealmPlanYear().setMbgRenewal(true);
		boolean history = false;
		List<Strategy> strategies = new ArrayList<>();
		Strategy strategy = prepareStrategy();
		strategies.add(strategy);
		RealmPlanYear prevRealmPlYr = new RealmPlanYear();
		prevRealmPlYr.setId(PREV_REALM_PLYR_ID);
	
		when(strategyDao.findByCompanyIdAndStatus(COMPANY_ID, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(strategies);
		doNothing().when(strategySyncService).syncStrategyData(company, null);
		when(realmPlanYearService.getPreviousRealmPlanYear(COMPANY_CODE, REALM_PLYR_ID)).thenReturn(prevRealmPlYr);
		when(strategyDataDao.getPlanTypeDescriptions(REALM_PLYR_ID)).thenReturn(preparePlanTypeDescriptions());
		when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(preparePlanRates());
		when(RulesAndConfigsUtils.getMinFundingType(REALM_PLYR_ID)).thenReturn("HQ");
		when(strategyGroupService.getBenefitGroupStrategy(STRATEGY_ID, "A")).thenReturn(prepareBenGrpStrategy());
		when(headCountService.getEligibleEmployeeCount(Mockito.any(Company.class), Mockito.anyLong(),
				Mockito.any(RealmPlanYear.class), Mockito.anyBoolean())).thenReturn(prepareActiveEligEECount());
		when(StrategyServiceHelper.constructBenefitGroup(Mockito.anyLong(), Mockito.any()))
				.thenReturn(prepareStrategyBenGrp());
		when(headCountService.getWaiverHeadCountByBenefitProgram(Mockito.any(Company.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean()))
				.thenReturn(prepareStrategyWaiverHeadCount());
	
		when(StrategyServiceHelper.getPlanSelectionIds(Mockito.anyLong(), Mockito.anyMap()))
				.thenReturn(preparePlanSelectionIds());
		when(StrategyServiceHelper.constructADPlanSelection(STRATEGY_ID, BEN_GRP_ID3, DBO_PLAN_ID,
				DBO_PLAN_TYPE, DBO_PLAN_HC)).thenReturn(new PlanSelection());
		when(StrategyUtils.getPlanCost(Mockito.anyList())).thenReturn(preparePlanCosts());
		when(FeatureFlagUtils.isBssYearAround(COMPANY_CODE, REALM_PLYR_ID)).thenReturn(true);
		when( RulesAndConfigsUtils.findPickChooseWithExceptions( eq( company )) ).thenReturn( false );
		when(AppRulesAndConfigsUtils.isStrategyCacheEnabled()).thenReturn(true);
		when(cacheTemplateService.storeInCache(anyString(), any(StrategyData.class), anyString())).thenReturn(true);

		// TODO Uncomment this out and fix the failing test on jenkins.
		List<StrategyData> actualResult = strategyService.getStrategies(company, history, null);
		verify(strategyDao, times(1)).findByCompanyIdAndStatus(COMPANY_ID, BSSApplicationConstants.STATUS_ACTIVE);
		verify(strategySyncService, times(1)).syncStrategyData(company, null);
		verify(cacheTemplateService, times(1)).retrieveFromCache("BSS:STRATEGY_DATA:"+STRATEGY_ID, StrategyData.class);
		assertEquals(1, actualResult.get(0).getStrategySummary().getBudgetFactor());
		assertFalse(actualResult.get(0).isCached());
		verify(cacheTemplateService, times(1)).storeInCache("BSS:STRATEGY_DATA:" + STRATEGY_ID, actualResult.get(0), "240");
	}

	@Test
	public void getStrategiesRenewalStrategyCached() {
		Company company = prepareCompany();
		company.setRenewalCompany(true);
		company.getRealmPlanYear().setMbgRenewal(true);
		boolean history = false;
		List<Strategy> strategies = new ArrayList<>();
		Strategy strategy = prepareStrategy();
		strategies.add(strategy);
		RealmPlanYear prevRealmPlYr = new RealmPlanYear();
		prevRealmPlYr.setId(PREV_REALM_PLYR_ID);
		StrategyData cacheData = new StrategyData();
		StrategySummary summary = new StrategySummary();
		summary.setId(1111111L);
		cacheData.setStrategySummary(summary);
		cacheData.getStrategySummary().setSubmitted(true);
		cacheData.getStrategySummary().setSubmitDate(new Date());
		cacheData.getStrategySummary().setCanDelete(false);

		when(strategyDao.findByCompanyIdAndStatus(COMPANY_ID, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(strategies);
		doNothing().when(strategySyncService).syncStrategyData(company, null);
		when(realmPlanYearService.getPreviousRealmPlanYear(COMPANY_CODE, REALM_PLYR_ID)).thenReturn(prevRealmPlYr);
		when(strategyDataDao.getPlanTypeDescriptions(REALM_PLYR_ID)).thenReturn(preparePlanTypeDescriptions());
		when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(preparePlanRates());
		when(RulesAndConfigsUtils.getMinFundingType(REALM_PLYR_ID)).thenReturn("HQ");
		when(StrategyServiceHelper.constructBenefitGroup(Mockito.anyLong(), Mockito.any()))
				.thenReturn(prepareStrategyBenGrp());

		when(StrategyServiceHelper.getPlanSelectionIds(Mockito.anyLong(), Mockito.anyMap()))
				.thenReturn(preparePlanSelectionIds());
		when(StrategyServiceHelper.constructADPlanSelection(STRATEGY_ID, BEN_GRP_ID3, DBO_PLAN_ID,
				DBO_PLAN_TYPE, DBO_PLAN_HC)).thenReturn(new PlanSelection());
		when(StrategyUtils.getPlanCost(Mockito.anyList())).thenReturn(preparePlanCosts());
		when(FeatureFlagUtils.isBssYearAround(COMPANY_CODE, REALM_PLYR_ID)).thenReturn(true);
		when( RulesAndConfigsUtils.findPickChooseWithExceptions( eq( company )) ).thenReturn( false );
		when(AppRulesAndConfigsUtils.isStrategyCacheEnabled()).thenReturn(true);
		when(cacheTemplateService.retrieveFromCache("BSS:STRATEGY_DATA:"+STRATEGY_ID, StrategyData.class)).thenReturn(cacheData);
		when(StrategyServiceHelper.isStrategyDeletable(any(), any(), anyBoolean())).thenReturn(true);

		// TODO Uncomment this out and fix the failing test on jenkins.
		List<StrategyData> actualResult = strategyService.getStrategies(company, history, null);
		verify(strategyDao, times(1)).findByCompanyIdAndStatus(COMPANY_ID, BSSApplicationConstants.STATUS_ACTIVE);
		verify(strategySyncService, times(1)).syncStrategyData(company, null);
		verify(cacheTemplateService, times(1)).retrieveFromCache("BSS:STRATEGY_DATA:"+STRATEGY_ID, StrategyData.class);
		verify(cacheTemplateService, times(0)).storeInCache("BSS:STRATEGY_DATA:" + STRATEGY_ID, actualResult.get(0), "240");
		assertEquals(1111111L, actualResult.get(0).getStrategySummary().getId().longValue());
		assertFalse(actualResult.get(0).getStrategySummary().isSubmitted());
		assertNull(actualResult.get(0).getStrategySummary().getSubmitDate());
		assertTrue(actualResult.get(0).isCached());
		assertTrue(actualResult.get(0).getStrategySummary().isCanDelete());
	}

	@Test
	public void getStrategyById() {
		Company company = prepareCompany();
		company.setRenewalCompany(true);
		Set<String> locations = new HashSet<>();
		locations.add("MA");
		List<String> medicalPlans = new ArrayList<>();
		medicalPlans.add("AA1111");
		Map<String, AdditionalBenefitEmployeeDetails> employeeSelection = new HashMap<>();
		employeeSelection.put("key", new AdditionalBenefitEmployeeDetails());

		when(StrategyServiceHelper.getLocations(company)).thenReturn(locations);
		when(StrategyServiceHelper.getBenefitPlanList(Mockito.anyMap())).thenReturn(medicalPlans);
		when(
				StrategyServiceHelper.constructBenefitGroup(Mockito.anyLong(), Mockito.any(BenefitGroupStrategy.class)))
				.thenReturn(prepareStrategyBenGrp());
		when(AdditionalBenefitServiceHelper.getADBPlanListMapByType(mock(List.class)))
				.thenReturn(new HashMap<String, Set<String>>());
		when(StrategyServiceHelper.constructADPlanSelection(STRATEGY_ID, BEN_GRP_ID3, AD_BEN_PLAN_ID1,
				AD_PLAN_TYPE_CMTR, 2)).thenReturn(prepareADPlanSelection());
		when(StrategyServiceHelper.constructADPlanSelection(STRATEGY_ID, BEN_GRP_ID3, AD_BEN_PLAN_ID2,
				AD_PLAN_TYPE_STD, 2)).thenReturn(prepareADPlanSelection());

	}

	@Test
	public void getStrategyByIdForProspect() { // ss
		Company company = prepareCompany();
		company.setCode("P1");
		company.setProspectCompany(true);

		Strategy strategy = prepareStrategy();
		strategy.setName("Custom Strategy");
		strategy.setType("custom");

		Set<String> locations = new HashSet<>();
		locations.add("MA");
		List<String> medicalPlans = new ArrayList<>();
		medicalPlans.add("AA1111");
		Map<String, AdditionalBenefitEmployeeDetails> employeeSelection = new HashMap<>();
		employeeSelection.put("key", new AdditionalBenefitEmployeeDetails());
		Map<String, Map<String, String>> defaultPlanMap = new HashMap<>();
		Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
		Map<String, Map<String, Set<String>>> autoSelectPlansByType = new HashMap<>();
		Map<String, Set<String>> eecFundingMap = new HashMap<>();

		when(StrategyServiceHelper.getLocations(company)).thenReturn(locations);
		when(prospectPlanHeadCountService.getProspectEligibleEmployeeCount(Mockito.any(Company.class),
				Mockito.anyLong())).thenReturn(prepareActiveEligEECount());
		when(StrategyServiceHelper.getBenefitPlanList(Mockito.anyMap())).thenReturn(medicalPlans);

		when(strategyDao.findByIdAndCompanyIdAndStatus(STRATEGY_ID, COMPANY_ID, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(strategy);
		when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(preparePlanRates());
		when(strategyDataDao.getPlanTypeDescriptions(REALM_PLYR_ID)).thenReturn(preparePlanTypeDescriptions());
		when(strategyGroupService.getBenefitGroupStrategy(STRATEGY_ID, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(prepareBenGrpStrategy());
		when(strategyDataDao.getAdditionalBenefitPlanEstCost(REALM_PLYR_ID)).thenReturn(preparePlanEstCost());
		when(realmPlyrPlanService.getMapForRealmPlanYear(REALM_PLYR_ID)).thenReturn(prepareRealmPlanMap());
		when(realmDataDao.getPortfilioDefaultPlans(REALM_PLYR_ID)).thenReturn(defaultPlanMap);
		when(RulesAndConfigsUtils.findPickChooseWithExceptions(eq(company))).thenReturn(false);
		when(strategyHsaFundingService.findById(strategy.getId())).thenReturn(new StrategyHsaFundingDto());

		when(
				StrategyServiceHelper.constructBenefitGroup(Mockito.anyLong(), Mockito.any(BenefitGroupStrategy.class)))
				.thenReturn(prepareStrategyBenGrp());
		when(AdditionalBenefitServiceHelper.getADBPlanListMapByType(mock(List.class)))
				.thenReturn(new HashMap<String, Set<String>>());
		when(StrategyServiceHelper.constructADPlanSelection(STRATEGY_ID, BEN_GRP_ID3, AD_BEN_PLAN_ID1,
				AD_PLAN_TYPE_CMTR, 2)).thenReturn(prepareADPlanSelection());
		when(StrategyServiceHelper.constructADPlanSelection(STRATEGY_ID, BEN_GRP_ID3, AD_BEN_PLAN_ID2,
				AD_PLAN_TYPE_STD, 2)).thenReturn(prepareADPlanSelection());

		StrategyData actualResult = strategyService.getStrategyById(company, STRATEGY_ID, false);
		assertNotNull(actualResult);
		assertNotNull(actualResult.getStrategySummary());
		assertEquals(Long.valueOf(2222), actualResult.getStrategySummary().getId());
		assertEquals("Custom Strategy", actualResult.getStrategySummary().getName());
		assertEquals("custom", actualResult.getStrategySummary().getType());

	}

	@Test
	public void getStrategyByIdForProspectTibTest() {
		Company company = prepareCompany();
		company.setCode("P1");
		company.setProspectCompany(true);

		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_OMS.getBenExchng());
		company.setRealm(realm);

		Strategy strategy = prepareStrategy();
		strategy.setName("Custom Strategy");
		strategy.setType("custom");

		Set<String> locations = new HashSet<>();
		locations.add("MA");
		List<String> medicalPlans = new ArrayList<>();
		medicalPlans.add("AA1111");
		Map<String, AdditionalBenefitEmployeeDetails> employeeSelection = new HashMap<>();
		employeeSelection.put("key", new AdditionalBenefitEmployeeDetails());
		Map<String, Map<String, String>> defaultPlanMap = new HashMap<>();
		Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
		Map<String, Map<String, Set<String>>> autoSelectPlansByType = new HashMap<>();
		Map<String, Set<String>> eecFundingMap = new HashMap<>();

		when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);
		when(StrategyServiceHelper.getLocations(company)).thenReturn(locations);
		when(prospectPlanHeadCountService.getProspectEligibleEmployeeCount(Mockito.any(Company.class),
				Mockito.anyLong())).thenReturn(prepareActiveEligEECount());
		when(StrategyServiceHelper.getBenefitPlanList(Mockito.anyMap())).thenReturn(medicalPlans);
		when(portfolioService.getOmsPlanCarriersForStrategyIdAndPlanType(STRATEGY_ID, MEDICAL_PLAN_TYPE)).thenReturn(new HashSet<>());
		when(strategyDao.findByIdAndCompanyIdAndStatus(STRATEGY_ID, COMPANY_ID, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(strategy);
		when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(preparePlanRates());
		when(strategyDataDao.getPlanTypeDescriptions(REALM_PLYR_ID)).thenReturn(preparePlanTypeDescriptions());
		when(strategyGroupService.getBenefitGroupStrategy(STRATEGY_ID, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(prepareBenGrpStrategy());
		when(strategyDataDao.getAdditionalBenefitPlanEstCost(REALM_PLYR_ID)).thenReturn(preparePlanEstCost());
		when(realmPlyrPlanService.getMapForRealmPlanYear(REALM_PLYR_ID)).thenReturn(prepareRealmPlanMap());
		when(realmDataDao.getPortfilioDefaultPlans(REALM_PLYR_ID)).thenReturn(defaultPlanMap);
		when(RulesAndConfigsUtils.findPickChooseWithExceptions(eq(company))).thenReturn(false);
		when(strategyHsaFundingService.findById(strategy.getId())).thenReturn(new StrategyHsaFundingDto());
		when(strategyDataDao.getStrategyGroupEstimateByPlanType(STRATEGY_ID, BEN_GRP_ID3)).thenReturn(prepareEstimate());

		when(
						StrategyServiceHelper.constructBenefitGroup(Mockito.anyLong(), Mockito.any(BenefitGroupStrategy.class)))
				.thenReturn(prepareStrategyBenGrp());
		when(AdditionalBenefitServiceHelper.getADBPlanListMapByType(mock(List.class)))
				.thenReturn(new HashMap<String, Set<String>>());
		when(StrategyServiceHelper.constructADPlanSelection(STRATEGY_ID, BEN_GRP_ID3, AD_BEN_PLAN_ID1,
				AD_PLAN_TYPE_CMTR, 2)).thenReturn(prepareADPlanSelection());
		when(StrategyServiceHelper.constructADPlanSelection(STRATEGY_ID, BEN_GRP_ID3, AD_BEN_PLAN_ID2,
				AD_PLAN_TYPE_STD, 2)).thenReturn(prepareADPlanSelection());

		StrategyData actualResult = strategyService.getStrategyById(company, STRATEGY_ID, false);
		assertNotNull(actualResult);
		assertNotNull(actualResult.getStrategySummary());
		assertEquals(Long.valueOf(2222), actualResult.getStrategySummary().getId());
		assertEquals("Custom Strategy", actualResult.getStrategySummary().getName());
		assertEquals("custom", actualResult.getStrategySummary().getType());
		assertEquals(BigDecimal.valueOf(100).setScale(2), actualResult.getBenefitGroups().get(0).getBenefitOffers().get(0).getSummary().getEstimatedTotalCost().setScale(2));
		verify(strategyDataDao, times(3)).getStrategyGroupEstimateByPlanType(STRATEGY_ID, BEN_GRP_ID3);
		verify(portfolioService, times(1)).getOmsPlanCarriersForStrategyIdAndPlanType(STRATEGY_ID, MEDICAL_PLAN_TYPE);

	}

	@Test
	public void getStrategyByIdForProspectPlanRate() {
        // Common setup for both tests
        Company company = prepareCompany();
        company.setCode("P1");
        company.setProspectCompany(true);

        Strategy strategy = prepareStrategy();
        strategy.setName("Custom Strategy");
        strategy.setType("custom");

        Set<String> locations = new HashSet<>();
        locations.add("MA");
        List<String> medicalPlans = new ArrayList<>();
        medicalPlans.add("AA1111");
        Map<String, AdditionalBenefitEmployeeDetails> employeeSelection = new HashMap<>();
        employeeSelection.put("key", new AdditionalBenefitEmployeeDetails());
        Map<String, Map<String, String>> defaultPlanMap = new HashMap<>();
        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
        Map<String, Map<String, Set<String>>> autoSelectPlansByType = new HashMap<>();
        Map<String, Set<String>> eecFundingMap = new HashMap<>();
        // Plan Rates
        Map<String, List<BenefitPlanRate>> benefitPlanRates = new HashMap<>();
        List<BenefitPlanRate> rateList = new ArrayList<>();
        String dataStream;
        dataStream = "{\"benefitPlan\":\"MEDPLAN1\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"1\",\"employerCost\":100,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        dataStream = "{\"benefitPlan\":\"MEDPLAN1\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"2\",\"employerCost\":200,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        dataStream = "{\"benefitPlan\":\"MEDPLAN1\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"C\",\"employerCost\":300,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        dataStream = "{\"benefitPlan\":\"MEDPLAN1\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"4\",\"employerCost\":400,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        benefitPlanRates.put("MEDPLAN1", rateList );
        // Plan Selections
        PlanSelection planSelection = new PlanSelection();
        planSelection.setId(MED_PLAN_SELECTION_ID1);
        planSelection.setBenefitPlan(MED_PLAN_ID1);
        planSelection.setPlanType(MEDICAL_PLAN_TYPE);
        planSelection.setListOfStates(Arrays.asList("CA", "MA"));
        List<PlanSelection> planSelections = Arrays.asList(planSelection);
        Map<Long, List<PlanSelection>> groupPlansMap = new HashMap<>();
        groupPlansMap.put(BEN_GRP_ID3, planSelections);
        Map<String, Map<Long, List<PlanSelection>>> strategyGroupPlansSelections = new HashMap<>();
        strategyGroupPlansSelections.put(MED_PLAN_ID1, groupPlansMap);

        when(StrategyServiceHelper.getLocations(company)).thenReturn(locations);
        when(prospectPlanHeadCountService.getProspectEligibleEmployeeCount(Mockito.any(Company.class),
                Mockito.anyLong())).thenReturn(prepareActiveEligEECount());
        when(StrategyServiceHelper.getBenefitPlanList(Mockito.anyMap())).thenReturn(medicalPlans);
        when(strategyDao.findByIdAndCompanyIdAndStatus(STRATEGY_ID, COMPANY_ID, BSSApplicationConstants.STATUS_ACTIVE))
                .thenReturn(strategy);
        when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(benefitPlanRates);
        when(strategyDataDao.getPlanTypeDescriptions(REALM_PLYR_ID)).thenReturn(preparePlanTypeDescriptions());
        when(strategyGroupService.getBenefitGroupStrategy(STRATEGY_ID, BSSApplicationConstants.STATUS_ACTIVE))
                .thenReturn(prepareBenGrpStrategy());
        when(strategyDataDao.getAdditionalBenefitPlanEstCost(REALM_PLYR_ID)).thenReturn(preparePlanEstCost());
        when(realmPlyrPlanService.getMapForRealmPlanYear(REALM_PLYR_ID)).thenReturn(prepareRealmPlanMap());
        when(realmDataDao.getPortfilioDefaultPlans(REALM_PLYR_ID)).thenReturn(defaultPlanMap);
        when(RulesAndConfigsUtils.findPickChooseWithExceptions(eq(company))).thenReturn(false);
        when(strategyHsaFundingService.findById(strategy.getId())).thenReturn(new StrategyHsaFundingDto());
        when(StrategyServiceHelper.constructBenefitGroup(Mockito.anyLong(), Mockito.any(BenefitGroupStrategy.class)))
                .thenReturn(prepareStrategyBenGrp());
        when(AdditionalBenefitServiceHelper.getADBPlanListMapByType(mock(List.class)))
                .thenReturn(new HashMap<String, Set<String>>());
        when(StrategyServiceHelper.constructADPlanSelection(STRATEGY_ID, BEN_GRP_ID3, AD_BEN_PLAN_ID1,
                AD_PLAN_TYPE_CMTR, 2)).thenReturn(prepareADPlanSelection());
        when(StrategyServiceHelper.constructADPlanSelection(STRATEGY_ID, BEN_GRP_ID3, AD_BEN_PLAN_ID2,
                AD_PLAN_TYPE_STD, 2)).thenReturn(prepareADPlanSelection());
        when(strategyDataDao.getPlansSelectionsByCompany(
                company.getCode(), company.getRealmPlanYearId(), company.getPlanStartDate()))
                .thenReturn(Map.of(STRATEGY_ID, strategyGroupPlansSelections));
        when(StrategyServiceHelper.getPlanSelectionIds(Mockito.anyLong(), Mockito.anyMap()))
                .thenReturn(List.of(MED_PLAN_SELECTION_ID1));

        StrategyData actualResult = strategyService.getStrategyById(company, STRATEGY_ID, false);

        assertNotNull(actualResult);
        assertNotNull(actualResult.getStrategySummary());
        assertEquals(Long.valueOf(2222), actualResult.getStrategySummary().getId());
        assertEquals("Custom Strategy", actualResult.getStrategySummary().getName());
        assertEquals("custom", actualResult.getStrategySummary().getType());

		mockStaticStrategyUtils.verify(() -> StrategyUtils.getPlanCost(any()), times(1));
    }

    @Test
	public void createFutureStrategies() {
		Company company = prepareCompany();
		boolean isDefaultSubmit = true;
		boolean isPreload = false;
		when(realmPlanYearService.getPreviousRealmPlanYear(company.getRealmPlanYear()))
				.thenReturn(prepareRealmPlanYear());
		when(companyService.getCompanyDetails(COMPANY_CODE, true, null, null)).thenReturn(prepareCompany());
		when(strategyDao.findByCompanyIdAndStatus(COMPANY_ID, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(Collections.<Strategy>emptyList());
		when(strategyDataDao.getCurrentStrategy(COMPANY_CODE, REALM_PLYR_ID)).thenReturn(null);

		strategyService.createFutureStrategies(company, isDefaultSubmit, isPreload);

		verify(strategyRenewalService).createFutureStrategies(Mockito.any(Company.class), Mockito.anyBoolean(),
				Mockito.any(RealmPlanYear.class), Mockito.anyBoolean(), Mockito.anyMap(), Mockito.anyMap(),
				Mockito.anyBoolean());
		verify(renewalDataDao).getHealthPlansForRenewalCompany(Mockito.any(Date.class), Mockito.anyString(),
				Mockito.anyString());
		verify(strategyDao).findByCompanyIdAndStatus(COMPANY_ID, BSSApplicationConstants.STATUS_ACTIVE);

		reset(strategyDao, companyService, strategyDataDao, renewalDataDao, strategyRenewalService,
				employeeStrategyGroupTransactionDao);

		List<Strategy> strategies = new ArrayList<>();
		strategies.add(prepareStrategy());

		when(strategyDao.findByCompanyIdAndStatus(COMPANY_ID, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(strategies);
		when(strategyDataDao.getCurrentStrategy(COMPANY_CODE, REALM_PLYR_ID)).thenReturn(null);

		strategyService.createFutureStrategies(company, isDefaultSubmit, isPreload);

		verify(strategyRenewalService).createFutureStrategies(Mockito.any(Company.class), Mockito.anyBoolean(),
				Mockito.any(RealmPlanYear.class), Mockito.anyBoolean(), Mockito.anyMap(), Mockito.anyMap(),
				Mockito.anyBoolean());
		verify(renewalDataDao).getHealthPlansForRenewalCompany(Mockito.any(Date.class), Mockito.anyString(),
				Mockito.anyString());
		verify(strategyDao).findByCompanyIdAndStatus(COMPANY_ID, BSSApplicationConstants.STATUS_ACTIVE);
	}

	@Test
	public void createProspectsTrinetStrategy_passesPlanMappingResponse() {
		Company company = prepareCompany();
		List<com.trinet.ambis.client.DefaultPlanMappingServiceClient.PlanMappingResponse> planMappingResponse =
				List.of(new com.trinet.ambis.client.DefaultPlanMappingServiceClient.PlanMappingResponse());
		com.trinet.ambis.persistence.model.ProcessStatus processStatus = new com.trinet.ambis.persistence.model.ProcessStatus();

		when(strategyDao.findByCompanyIdAndStatus(company.getId(), BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(Collections.emptyList());
		when(processStatusService.createStrategyProcess(company)).thenReturn(processStatus);

		strategyService.createProspectsTrinetStrategy(company, 1L, planMappingResponse);

		verify(prospectStrategyService, times(1))
				.createDefaultTrinetStrategy(company, 1L, planMappingResponse);
		verify(processStatusService, times(1)).updateProcessStatus(processStatus);
	}

	@Test
	public void getAllStrategies() {
		List<Strategy> strategies = new ArrayList<>();
		when(strategyDao.findByCompanyIdAndStatus(COMPANY_ID, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(strategies);
		List<Strategy> actualResult = strategyService.getAllStrategies(COMPANY_ID);
		assertEquals(0, actualResult.size());
	}

	@Test
	public void preLoadBssStrategies() {
		String peoId = "PAS";
		String quarter = "Q2";
		String emplId = "00002222276";

		when(realmPlanYearDao.getMaxRealmPlanYearByQuarter(quarter)).thenReturn(prepareRealmPlanYear());

		strategyService.preLoadBssStrategies(peoId, quarter, emplId);
	}

	@Test
	public void preLoadBssStrategiesCompany() {
		Company company = prepareCompany();

		strategyService.preLoadBssStrategies(company);
	}

	@Test
	public void deleteStrategyCanBeDeleted() {
		Set<Long> strategyIds = new HashSet<>();
		strategyIds.add(STRATEGY_ID);
		Strategy strategy = prepareStrategy();
		Set<Long> pendingStrategyIds = Collections.emptySet();
		Company comp = new Company();
		
		when(strategyDao.getReferenceById(STRATEGY_ID)).thenReturn(strategy);
		when(StrategyServiceHelper.isStrategyDeletable(strategy, pendingStrategyIds, false)).thenReturn(true);

		strategyService.deleteStrategy(comp, STRATEGY_ID);

		verify(strategyDataDao, times(1)).deleteEmployeeStrategyGroup(strategyIds);
		verify(strategyDataDao, times(1)).deleteStrategyGroup(strategyIds);
		verify(strategyDataDao, times(1)).deleteStrategyGroupCovHeadCount(strategyIds);
		verify(strategyDataDao, times(1)).deleteAllPlanContributionsByStrategy(strategyIds);
		verify(strategyDataDao, times(1)).deleteStrategyFundDetailByStrategy(strategyIds);
		verify(strategyDataDao, times(1)).deleteStrategyFlatMaxByStrategy(strategyIds);
		verify(strategyDataDao, times(1)).deleteStrategyFundModelByStrategy(strategyIds);
		verify(strategyDataDao, times(1)).deleteStrategyEstimateList(strategyIds);
		verify(strategyDataDao, times(1)).deleteStrategyById(strategyIds);
	}

	@Test
	public void deleteStrategyCanNotBeDeleted() {
		Set<Long> strategyIds = new HashSet<>();
		strategyIds.add(STRATEGY_ID);
		Strategy strategy = prepareStrategy();
		Set<Long> submitPendingStrategyIds = new HashSet<>(Arrays.asList(STRATEGY_ID));
		Company comp = new Company();
		Set<String> SUBMIT_PENDING_STATUS = Collections.unmodifiableSet(new HashSet<String>(
				Arrays.asList(BSSApplicationConstants.PROCESSING, BSSApplicationConstants.UNPROCESSED)));
		List<SubmitStatus> pendingSubmitStatuses = new ArrayList<>();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID).build();
		pendingSubmitStatuses.add(submitStatus);
		
		when(strategyDao.getReferenceById(STRATEGY_ID)).thenReturn(strategy);
		when(submitStatusService.findByCompanyAndPlanYearIdAndStatuses(comp,
				SUBMIT_PENDING_STATUS)).thenReturn(pendingSubmitStatuses);
		when(StrategyServiceHelper.isStrategyDeletable(strategy, submitPendingStrategyIds, false)).thenReturn(false);

		strategyService.deleteStrategy(comp, STRATEGY_ID);

		verify(StrategyServiceHelper.class, VerificationModeFactory.times(1));
		StrategyServiceHelper.isStrategyDeletable(strategy, submitPendingStrategyIds, false);
		
		verify(strategyDataDao, times(0)).deleteEmployeeStrategyGroup(strategyIds);
		verify(strategyDataDao, times(0)).deleteStrategyGroup(strategyIds);
		verify(strategyDataDao, times(0)).deleteStrategyGroupCovHeadCount(strategyIds);
		verify(strategyDataDao, times(0)).deleteAllPlanContributionsByStrategy(strategyIds);
		verify(strategyDataDao, times(0)).deleteStrategyFundDetailByStrategy(strategyIds);
		verify(strategyDataDao, times(0)).deleteStrategyFlatMaxByStrategy(strategyIds);
		verify(strategyDataDao, times(0)).deleteStrategyFundModelByStrategy(strategyIds);
		verify(strategyDataDao, times(0)).deleteStrategyEstimateList(strategyIds);
		verify(strategyDataDao, times(0)).deleteStrategyById(strategyIds);
	}
	
	@Test
	public void updateStrategyNameCanBeEdited() {
		String strategyName = "UPDATED STRATEGY NAME";
		Strategy strategy = prepareStrategy();
		when(strategyDao.getReferenceById(STRATEGY_ID)).thenReturn(strategy);
		when(StrategyServiceHelper.isStrategyNameEditable(strategy, false)).thenReturn(true);

		strategyService.updateStrategyName(STRATEGY_ID, strategyName);

		verify(strategyDao, times(1)).updateStrategyName(STRATEGY_ID, strategyName);
	}	
	
	@Test
	public void updateStrategyNameCanNotBeEdited() {
		
		String strategyName = "UPDATED STRATEGY NAME";
		Strategy strategy = prepareStrategy();
		when(strategyDao.getReferenceById(STRATEGY_ID)).thenReturn(strategy);
		when(StrategyServiceHelper.isStrategyNameEditable(strategy, false)).thenReturn(false);

		strategyService.updateStrategyName(STRATEGY_ID, strategyName);		
		
		verify(strategyDao, times(0)).updateStrategyName(STRATEGY_ID, strategyName);
	}
	

	@Test
	public void getStrategiesHistoryCount() {
		when(strategyDataDao.getStrategiesHistoryCount(COMPANY_CODE, REALM_PLYR_ID)).thenReturn(1);

		int actual = strategyService.getStrategiesHistoryCount(COMPANY_CODE, REALM_PLYR_ID);

		assertEquals(1, actual);
	}

	@Test
	public void getAllSubmittedStrategiesByCompanyCode() {
		List<Strategy> strategies = new ArrayList<>();
		strategies.add(prepareStrategy());

		when(strategyDao.findSubmittedStrategiesByCompanyCode(COMPANY_CODE)).thenReturn(strategies);

		List<Strategy> actual = strategyService.getAllSubmittedStrategiesByCompanyCode(COMPANY_CODE);

		assertEquals(strategies, actual);
	}

	@Test
	public void hasSubmittedStrategy() {
		when(strategyDataDao.getSubmittedStrategiesCount(COMPANY_ID)).thenReturn(1);

		boolean actual = strategyService.hasSubmittedStrategy(COMPANY_ID);

		assertEquals(true, actual);
	}

	@Test
	public void getStrategiesForClientTest() {
		Company company = prepareCompany();
		company.setRenewalCompany(true);
		company.getRealmPlanYear().setMbgRenewal(true);

		boolean history = false;
		List<Strategy> strategies = new ArrayList<>();
		Strategy strategy = prepareStrategy();
		strategies.add(strategy);
		RealmPlanYear prevRealmPlYr = new RealmPlanYear();
		prevRealmPlYr.setId(PREV_REALM_PLYR_ID);

		when(strategyDao.findByCompanyIdAndStatus(COMPANY_ID, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(strategies);
		doNothing().when(strategySyncService).syncStrategyData(company, null);
		when(realmPlanYearService.getPreviousRealmPlanYear(COMPANY_CODE, REALM_PLYR_ID)).thenReturn(prevRealmPlYr);
		when(strategyDataDao.getPlanTypeDescriptions(REALM_PLYR_ID)).thenReturn(preparePlanTypeDescriptions());
		when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(preparePlanRates());
		when(RulesAndConfigsUtils.getMinFundingType(REALM_PLYR_ID)).thenReturn("HQ");
		when(strategyGroupService.getBenefitGroupStrategy(STRATEGY_ID, "A")).thenReturn(prepareBenGrpStrategy());
		when(headCountService.getEligibleEmployeeCount(Mockito.any(Company.class), Mockito.anyLong(),
				Mockito.any(RealmPlanYear.class), Mockito.anyBoolean())).thenReturn(prepareActiveEligEECount());
		when(StrategyServiceHelper.constructBenefitGroup(Mockito.anyLong(), Mockito.any()))
				.thenReturn(prepareStrategyBenGrp());
		when(headCountService.getWaiverHeadCountByBenefitProgram(Mockito.any(Company.class), Mockito.anyLong(),
				Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(prepareStrategyWaiverHeadCount());

		when(StrategyServiceHelper.getPlanSelectionIds(Mockito.anyLong(), Mockito.anyMap()))
				.thenReturn(preparePlanSelectionIds());
		when(StrategyServiceHelper.constructADPlanSelection(STRATEGY_ID, BEN_GRP_ID3, DBO_PLAN_ID,
				DBO_PLAN_TYPE, DBO_PLAN_HC)).thenReturn(new PlanSelection());
		when(StrategyUtils.getPlanCost(Mockito.anyList()))
				.thenReturn(preparePlanCosts());
		when(FeatureFlagUtils.isBssYearAround(COMPANY_CODE, REALM_PLYR_ID)).thenReturn(true);
		when(RulesAndConfigsUtils.findPickChooseWithExceptions(eq(company))).thenReturn(false);
		when(CompanyServiceHelper.isClientCompanyPattern(company.getCode())).thenReturn(true);
		List<StrategyData> actualResult = strategyService.getStrategies(company, history, null);

		assertNotNull(actualResult);
		assertNotNull(actualResult.get(0).getStrategySummary());
		assertEquals(Long.valueOf(2222), actualResult.get(0).getStrategySummary().getId());
		assertEquals("recommended", actualResult.get(0).getStrategySummary().getType());
		assertEquals(30, actualResult.get(0).getStrategySummary().getHeadcount());
		assertFalse(actualResult.get(0).getStrategySummary().isSubmitted());
		assertEquals(1, actualResult.get(0).getStrategySummary().getBudgetFactor());
		assertEquals(COMPANY_CODE, actualResult.get(0).getStrategySummary().getCompanyId());
		List<StrategyBenefitGroup> benefitGroups = actualResult.get(0).getBenefitGroups();

		assertEquals(3333, benefitGroups.get(0).getId());
		assertEquals("Group 1", benefitGroups.get(0).getName());
		assertEquals("STD", benefitGroups.get(0).getType());
		assertEquals("EF1", benefitGroups.get(0).getBenefitProgram());
		assertEquals(1111, benefitGroups.get(0).getCompanyId());
		assertEquals(2222, benefitGroups.get(0).getStrategyId());
		assertEquals(1111, benefitGroups.get(0).getStrategyGroupId());
		assertEquals(BigDecimal.valueOf(40000), benefitGroups.get(0).getEstimatedTotalCost());
		assertEquals(30, benefitGroups.get(0).getHeadcount());

		// verify
		verify(strategyDao, times(1)).findByCompanyIdAndStatus(COMPANY_ID, BSSApplicationConstants.STATUS_ACTIVE);
		verify(strategyDataDao, times(0)).getHistoryStrategies(anyString(), anyLong());
		verify(prospectStrategyService, times(0)).getProspectCurrentStrategy(anyString());
	}

	@Test
	public void getStrategiesForProspectTest() {
		Company company = prepareCompany();
		company.setCode("P1");
		company.setProspectCompany(true);
		StrategyData prospectStrategy = prepareProspectStrategy();
		when(strategyDataDao.getHistoryStrategies(company.getCode(), company.getRealmPlanYear().getId()))
				.thenReturn(new ArrayList<>());
		when(prospectStrategyService.getProspectCurrentStrategy("P1")).thenReturn(prospectStrategy);
		List<StrategyData> actualResult = strategyService.getStrategies(company, true, "TNIII");
		assertNotNull(actualResult);
		assertNotNull(actualResult.get(0).getStrategySummary());
		assertEquals(Long.valueOf(0), actualResult.get(0).getStrategySummary().getId());
		assertEquals("Prospect Current Strategy", actualResult.get(0).getStrategySummary().getName());
		assertEquals("prospect", actualResult.get(0).getStrategySummary().getType());
		assertEquals(BigDecimal.valueOf(1329.78), actualResult.get(0).getStrategySummary().getEstimatedTotalCost());
		assertEquals(29, actualResult.get(0).getStrategySummary().getHeadcount());
		assertEquals(BigDecimal.ZERO, actualResult.get(0).getStrategySummary().getTotalBudget());
		assertEquals(1, actualResult.get(0).getStrategySummary().getBudgetFactor());
		assertEquals("SFDC_PROSPECT_ID", actualResult.get(0).getStrategySummary().getCompanyId());
		assertNotNull(actualResult.get(0).getStrategyHsaFunding());
		assertEquals(0L, actualResult.get(0).getStrategyHsaFunding().getStrategyId());
		assertEquals(Integer.valueOf(0), actualResult.get(0).getStrategyHsaFunding().getOptionId());
		assertFalse(actualResult.get(0).getStrategyHsaFunding().isCustomLevel());
		List<StrategyBenefitGroup> benefitGroups = actualResult.get(0).getBenefitGroups();
		assertBenefitGroup(benefitGroups, prospectStrategy);
		verify(strategyDataDao, times(1)).getHistoryStrategies(company.getCode(), company.getRealmPlanYear().getId());
		verify(prospectStrategyService, times(1)).getProspectCurrentStrategy("P1");
	}
	
	@Test
	public void getStrategiesForProspectTest1() {
		Company company = prepareCompany();
		company.setCode("P1");
		company.setProspectCompany(true);

		List<Strategy> strategies = new ArrayList<>();
		Strategy strategy = prepareStrategy();
		strategy.setName("Custom Strategy");
		strategy.setType("custom");
		strategies.add(strategy);
		RealmPlanYear prevRealmPlYr = new RealmPlanYear();
		prevRealmPlYr.setId(PREV_REALM_PLYR_ID);
		Map<String, Map<String, String>> defaultPlanMap = new HashMap<>();
		Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
		Map<String, Map<String, Set<String>>> autoSelectPlansByType = new HashMap<>();
		Map<String, Set<String>> eecFundingMap = new HashMap<>();

		when(strategyDataDao.getHistoryStrategies(company.getCode(), company.getRealmPlanYear().getId()))
				.thenReturn(strategies);
		when(strategyDataDao.getPlanTypeDescriptions(REALM_PLYR_ID)).thenReturn(preparePlanTypeDescriptions());
		when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(preparePlanRates());
		when(RulesAndConfigsUtils.getMinFundingType(REALM_PLYR_ID)).thenReturn("HQ");
		when(strategyGroupService.getBenefitGroupStrategy(STRATEGY_ID, "A")).thenReturn(prepareBenGrpStrategy());

		when(strategyDataDao.getAdditionalBenefitPlanEstCost(REALM_PLYR_ID)).thenReturn(preparePlanEstCost());
		when(prospectPlanHeadCountService.getProspectEligibleEmployeeCount(Mockito.any(Company.class),
				Mockito.anyLong())).thenReturn(prepareActiveEligEECount());
		when(realmPlyrPlanService.getMapForRealmPlanYear(REALM_PLYR_ID)).thenReturn(prepareRealmPlanMap());
		when(realmDataDao.getPortfilioDefaultPlans(REALM_PLYR_ID)).thenReturn(defaultPlanMap);
		doNothing().when(strategyDataDao).insertStrategyEstimate(Mockito.anyMap());
		when(StrategyServiceHelper.constructADPlanSelection(STRATEGY_ID, BEN_GRP_ID3, DBO_PLAN_ID,
				DBO_PLAN_TYPE, DBO_PLAN_HC)).thenReturn(new PlanSelection());
		when(StrategyUtils.getPlanCost(Mockito.anyList()))
				.thenReturn(preparePlanCosts());
		when(FeatureFlagUtils.isBssYearAround(COMPANY_CODE, REALM_PLYR_ID)).thenReturn(true);
		when(RulesAndConfigsUtils.findPickChooseWithExceptions(eq(company))).thenReturn(false);
		when(StrategyServiceHelper.constructBenefitGroup(Mockito.anyLong(), Mockito.any()))
				.thenReturn(prepareStrategyBenGrp());
		when(strategyFundingDataDao.getEecFunding(company.getCode(), REALM_PLYR_ID)).thenReturn(eecFundingMap);

		List<StrategyData> actualResult = strategyService.getStrategies(company, true, "TNIII");

		assertNotNull(actualResult);
		assertNotNull(actualResult.get(0).getStrategySummary());
		assertEquals(Long.valueOf(2222), actualResult.get(0).getStrategySummary().getId());
		assertEquals("Custom Strategy", actualResult.get(0).getStrategySummary().getName());
		assertEquals("custom", actualResult.get(0).getStrategySummary().getType());

		verify(strategyDataDao, times(1)).getHistoryStrategies(company.getCode(), company.getRealmPlanYear().getId());
		verify(prospectStrategyService, times(1)).getProspectCurrentStrategy("P1");
	}

	/**
	 * given company code, company id , realm plan year id and strategy ids</br>
	 * when resetStrategiesBy method is called </br>
	 * then call reset methods</br>
	 **/
	@Test
	public void resetStrategiesByTest1() {
		// given
		// data
		String companyCode = "B5NP1PC1";
		long companyId = 129059L;
		long realmPlanYearId = 64;
		Set<Long> strategyIds = Set.of(1L, 2L, 3L);
		// method mocks
		doNothing().when(employeeStrategyGroupTransactionDao).deleteByStrategyIds(strategyIds);
		doNothing().when(strategyDataDao).deleteEmployeeStrategyGroup(strategyIds);
		doNothing().when(strategyDataDao).deleteStrategyGroupCovHeadCount(strategyIds);
		doNothing().when(strategyDataDao).deleteStrategyGroup(strategyIds);
		when(strategyDataDao.deleteAllPlanContributionsByStrategy(strategyIds)).thenReturn(1);
		when(strategyDataDao.deleteAllPlanSelectionsByStrategy(strategyIds)).thenReturn(1);
		doNothing().when(strategyDataDao).deleteStrategyFundDetailByStrategy(strategyIds);
		doNothing().when(strategyDataDao).deleteStrategyFlatMaxByStrategy(strategyIds);
		when(strategyDataDao.deleteStrategyFundModelByStrategy(strategyIds)).thenReturn(1);
		doNothing().when(strategyDataDao).deleteStrategyEstimateList(strategyIds);
		when(strategyDataDao.deleteEePlanAssignmentsByStrategyIds(strategyIds)).thenReturn(1);
		doNothing().when(strategyDataDao).deleteStrategyById(strategyIds);
		doNothing().when(strategyDataDao).deleteGroupCovHeadCount(companyId);
		doNothing().when(strategyDataDao).deleteGroupRate(companyId);
		doNothing().when(strategyDataDao).deleteGroupByCompanyId(companyId);
		when(strategyDataDao.deleteEeDefaultPlanAssignmentsByCompanyId(companyId)).thenReturn(1);
		doNothing().when(strategyDataDao).deleteEmployees(companyCode, realmPlanYearId);
		// when
		strategyService.resetStrategiesBy(companyCode, companyId, realmPlanYearId, strategyIds);
		// then
		// verify
		verify(employeeStrategyGroupTransactionDao, times(1)).deleteByStrategyIds(strategyIds);
		verify(strategyDataDao, times(1)).deleteEmployeeStrategyGroup(strategyIds);
		verify(strategyDataDao, times(1)).deleteStrategyGroupCovHeadCount(strategyIds);
		verify(strategyDataDao, times(1)).deleteStrategyGroup(strategyIds);
		verify(strategyDataDao, times(1)).deleteAllPlanContributionsByStrategy(strategyIds);
		verify(strategyDataDao, times(1)).deleteAllPlanSelectionsByStrategy(strategyIds);
		verify(strategyDataDao, times(1)).deleteStrategyFundDetailByStrategy(strategyIds);
		verify(strategyDataDao, times(1)).deleteStrategyFlatMaxByStrategy(strategyIds);
		verify(strategyDataDao, times(1)).deleteStrategyFundModelByStrategy(strategyIds);
		verify(strategyDataDao, times(1)).deleteStrategyEstimateList(strategyIds);
		verify(strategyDataDao, times(1)).deleteEePlanAssignmentsByStrategyIds(strategyIds);
		verify(strategyDataDao, times(1)).deleteStrategyById(strategyIds);
		verify(strategyDataDao, times(1)).deleteGroupCovHeadCount(companyId);
		verify(strategyDataDao, times(1)).deleteGroupRate(companyId);
		verify(strategyDataDao, times(1)).deleteGroupByCompanyId(companyId);
		verify(strategyDataDao, times(1)).deleteEeDefaultPlanAssignmentsByCompanyId(companyId);
		verify(strategyDataDao, times(1)).deleteEmployees(companyCode, realmPlanYearId);
	}

	/**
	 * given company code, company id , realm plan year id and empty strategy
	 * ids</br>
	 * when resetStrategiesBy method is called </br>
	 * then call reset methods</br>
	 **/
	@Test
	public void resetStrategiesByTest2() {
		// given
		// data
		String companyCode = "B5NP1PC1";
		long companyId = 129059L;
		long realmPlanYearId = 64;
		Set<Long> strategyIds = Collections.emptySet();
		// method mocks
		doNothing().when(strategyDataDao).deleteGroupCovHeadCount(companyId);
		doNothing().when(strategyDataDao).deleteGroupRate(companyId);
		doNothing().when(strategyDataDao).deleteGroupByCompanyId(companyId);
		when(strategyDataDao.deleteEeDefaultPlanAssignmentsByCompanyId(companyId)).thenReturn(1);
		doNothing().when(strategyDataDao).deleteEmployees(companyCode, realmPlanYearId);
		// when
		strategyService.resetStrategiesBy(companyCode, companyId, realmPlanYearId, strategyIds);
		// then
		// verify
		verify(employeeStrategyGroupTransactionDao, times(0)).deleteByStrategyIds(strategyIds);
		verify(strategyDataDao, times(0)).deleteEmployeeStrategyGroup(strategyIds);
		verify(strategyDataDao, times(0)).deleteStrategyGroupCovHeadCount(strategyIds);
		verify(strategyDataDao, times(0)).deleteStrategyGroup(strategyIds);
		verify(strategyDataDao, times(0)).deleteAllPlanContributionsByStrategy(strategyIds);
		verify(strategyDataDao, times(0)).deleteAllPlanSelectionsByStrategy(strategyIds);
		verify(strategyDataDao, times(0)).deleteStrategyFundDetailByStrategy(strategyIds);
		verify(strategyDataDao, times(0)).deleteStrategyFlatMaxByStrategy(strategyIds);
		verify(strategyDataDao, times(0)).deleteStrategyFundModelByStrategy(strategyIds);
		verify(strategyDataDao, times(0)).deleteStrategyEstimateList(strategyIds);
		verify(strategyDataDao, times(0)).deleteEePlanAssignmentsByStrategyIds(strategyIds);
		verify(strategyDataDao, times(0)).deleteStrategyById(strategyIds);
		verify(strategyDataDao, times(1)).deleteGroupCovHeadCount(companyId);
		verify(strategyDataDao, times(1)).deleteGroupRate(companyId);
		verify(strategyDataDao, times(1)).deleteGroupByCompanyId(companyId);
		verify(strategyDataDao, times(1)).deleteEeDefaultPlanAssignmentsByCompanyId(companyId);
		verify(strategyDataDao, times(1)).deleteEmployees(companyCode, realmPlanYearId);
	}

	@Test
	/*
	 * Given, a request to get the strategy cost by plan type
	 * When, there are no submitted strategies for the company
	 * Then, return an empty StrategyCostRes
	 */
	public void getStrategyCostByPlanType1() {
		Company company = prepareCompany();
		StrategyCostRes actual = strategyService.getStrategyCostByPlanType(company, STRATEGY_ID);
		assertNotNull(actual);
		assertTrue(actual.getCostSummary().isEmpty());
	}

	@Test
	/*
	 * Given, a request to get the strategy cost by plan type
	 * When, there are more than one submitted strategies for the company
	 *       and the client offers all plan types in the realm
	 * Then, get the strategy with the last submitted date and return a valid StrategyCostRes
	 */
	public void getStrategyCostByPlanType2() {
		Company company = prepareCompany();

		when(strategyDataDao.getHealthCostsByPlanType(STRATEGY_ID))
				.thenReturn(prepareHealthCostsByPlanType(true));
		when(strategyDataDao.getAdditionalBenefitCostsByPlanType(STRATEGY_ID))
				.thenReturn(prepareAdditionalBenefitCostsByPlanType(true));
		when(strategyDataDao.getRealmPlanTypes(company.getRealmPlanYear().getId()))
				.thenReturn(prepareRealmPlanTypes(true));

		StrategyCostRes actual = strategyService.getStrategyCostByPlanType(company, STRATEGY_ID);
		assertNotNull(actual);
		assertEquals(5, actual.getCostSummary().size());
		for (StrategyCostRes.PlanTypeCost entry : actual.getCostSummary()) {
			switch (entry.getBenefitType()) {
			case "medical":
				assertEquals(BigDecimal.valueOf(300), entry.getMonthlyTotalCost());
				assertTrue(entry.isOffered());
				break;
			case "dental":
				assertEquals(BigDecimal.valueOf(60), entry.getMonthlyTotalCost());
				assertTrue(entry.isOffered());
				break;
			case "vision":
				assertEquals(BigDecimal.valueOf(30), entry.getMonthlyTotalCost());
				assertTrue(entry.isOffered());
				break;
			case "LIFE":
				assertEquals(BigDecimal.valueOf(100), entry.getMonthlyTotalCost());
				assertTrue(entry.isOffered());
				break;
			case "DISABILITY":
				assertEquals(BigDecimal.valueOf(20), entry.getMonthlyTotalCost());
				assertTrue(entry.isOffered());
				break;
			default:
				break;
			}
		}
		verify(strategyDataDao, times(1)).getHealthCostsByPlanType(STRATEGY_ID);
		verify(strategyDataDao, times(1)).getAdditionalBenefitCostsByPlanType(STRATEGY_ID);
	}

	@Test
    public void updateStrategieHistoryTest() {
         Company company = prepareCompany();
         company.setCode("P1");
         company.setProspectCompany(true);
         List<Strategy> historyStrategies = prepareHistoryStrategies();
         StrategyData prospectStrategy = prepareProspectStrategy();

         when(strategyDataDao.getHistoryStrategies(company.getCode(), company.getRealmPlanYear().getId()))
                  .thenReturn(historyStrategies);
        
         when(strategyDataDao.getHistoryStrategies(company.getCode(), company.getRealmPlanYear().getId()))
                  .thenReturn(new ArrayList<>());
         when(prospectStrategyService.getProspectCurrentStrategy("P1")).thenReturn(prospectStrategy);

         List<StrategyData> startegies = strategyService.updateStrategieHistory(company, true);
         assertNotNull(startegies);
    }
	
	@Test
	/*
	 * Given, a request to get the strategy cost by plan type
	 * When, there are more than one submitted strategies for the company
	 *       and the client offers all plan types in the realm except LIFE
	 *       and the realm does not include medical plan type
	 * Then, get the strategy with the last submitted date and return a valid StrategyCostRes
	 */
	public void getStrategyCostByPlanType3() {
		Company company = prepareCompany();

		when(strategyDataDao.getHealthCostsByPlanType(STRATEGY_ID))
				.thenReturn(prepareHealthCostsByPlanType(false));
		when(strategyDataDao.getAdditionalBenefitCostsByPlanType(STRATEGY_ID))
				.thenReturn(prepareAdditionalBenefitCostsByPlanType(false));
		when(strategyDataDao.getRealmPlanTypes(company.getRealmPlanYear().getId()))
				.thenReturn(prepareRealmPlanTypes(false));

		StrategyCostRes actual = strategyService.getStrategyCostByPlanType(company, STRATEGY_ID);
		assertNotNull(actual);
		assertEquals(4, actual.getCostSummary().size());

		for (StrategyCostRes.PlanTypeCost entry : actual.getCostSummary()) {
			switch (entry.getBenefitType()) {
				case "medical":
				case "LIFE":
					assertEquals(BigDecimal.valueOf(0), entry.getMonthlyTotalCost());
					assertFalse(entry.isOffered());
					break;
				case "dental":
					assertEquals(BigDecimal.valueOf(60), entry.getMonthlyTotalCost());
					assertTrue(entry.isOffered());
					break;
				case "vision":
					assertEquals(BigDecimal.valueOf(30), entry.getMonthlyTotalCost());
					assertTrue(entry.isOffered());
					break;
				case "DISABILITY":
					assertEquals(BigDecimal.valueOf(20), entry.getMonthlyTotalCost());
					assertTrue(entry.isOffered());
					break;
				default:
					break;
			}
		}
		verify(strategyDataDao, times(1)).getHealthCostsByPlanType(STRATEGY_ID);
		verify(strategyDataDao, times(1)).getAdditionalBenefitCostsByPlanType(STRATEGY_ID);
	}

	@Test
	/*
	 * Given, a request to create a strategy estimate for TIB Medical plans
	 * When, the company is a TIB prospect company
	 * Then, the strategy estimate is created successfully
	 */
	public void createOmsStrategyEstimateTest1() {
		Set<Long> strategies = new HashSet<>(Arrays.asList(STRATEGY_ID));
		Company company = prepareCompany();

		when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);
		when(strategyDataDao.deleteStrategyEstimateForPlanTypes(strategies, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER)).thenReturn(1);
		when(strategyDataDao.insertStrategyEstimateForOmsPlanTypes(strategies, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER)).thenReturn(1);

		strategyService.createOmsStrategyEstimate(company, strategies);
		verify(strategyDataDao, times(1)).deleteStrategyEstimateForPlanTypes(strategies, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
		verify(strategyDataDao, times(1)).insertStrategyEstimateForOmsPlanTypes(strategies, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
	}

	@Test
	/*
	 * Given, a request to create a strategy estimate for TIB Medical plans
	 * When, the company is NOT a TIB prospect company
	 * Then, the strategy estimate is created successfully
	 */
	public void createOmsStrategyEstimateTest2() {
		Set<Long> strategies = new HashSet<>(Arrays.asList(STRATEGY_ID));
		Company company = prepareCompany();

		when(CompanyServiceHelper.isTibProspect(company)).thenReturn(false);

		strategyService.createOmsStrategyEstimate(company, strategies);
		verify(strategyDataDao, times(0)).deleteStrategyEstimateForPlanTypes(strategies, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
		verify(strategyDataDao, times(0)).insertStrategyEstimateForOmsPlanTypes(strategies, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
	}
	
	@Test
	public void createUpdateStrategy_testPlanAssignmentForCustomStrategyWithOnlyMedCarrierChange() {
		TypeReference<StrategyData> type = new TypeReference<StrategyData>() {
		};
		StrategyData dto = TestHelper.readPlanComparisonRequest("/strategySummary/NewStrategySummarySave.json", type)
				.get();
		Company company = prepareCompany();
		company.setBenefitStartDate("01-FEB-2022");
		company.setPlanStartDate("01-MAY-2023");
		company.setProspectCompany(true);
		Map<String, Set<PlanCarrier>> existingPortfolios = preparePlanCarriers();
		Strategy strategy = new Strategy();
		strategy.setId(1234l);
		strategy.setName("Custom_Strategy");
		List<Strategy> strategies = new ArrayList<>(List.of(strategy));
		List<PlanSelectionDetail> planSelectionDetails = new ArrayList<>();
		long groupId1 = 335718L;
		long groupId2 = 335719L;
		Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
		existingMedStrategyPortfolioMap.put(Long.valueOf(1), true);
		existingMedStrategyPortfolioMap.put(Long.valueOf(18), false);
		Map<Long, Boolean> newMedStrategyPortfolioMap = new HashMap<>();
		newMedStrategyPortfolioMap.put(Long.valueOf(5), true);
		newMedStrategyPortfolioMap.put(Long.valueOf(2), false);

		when(strategyDao.findByCompanyId(COMPANY_ID)).thenReturn(strategies);
		Mockito.doAnswer(new Answer<Strategy>() {
			public Strategy answer(InvocationOnMock invocation) {
				strategyArgCaptor.getValue().setId(1234l);
				return strategyArgCaptor.getValue();
			}
		}).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
		when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class)))
				.thenReturn(prepareBenGroups().get(0));
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("11").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("14").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("11").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("14").build());
		when(portfolioRuleDao.getMedicalPortfoliosBy(1234l, company.getRealmPlanYearId(),
				company.getHeadQuatersState())).thenReturn(existingMedStrategyPortfolioMap);

		strategyService.createUpdateStrategy(dto, company, false);

		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(eq(Set.of(1234l)), any(), any(),
				eq(Set.of("10")));
	}

	@Test
	public void createUpdateStrategy_testPlanAssignmentForCustomStrategyWithDentalERToEEChange() {
		TypeReference<StrategyData> type = new TypeReference<StrategyData>() {
		};
		StrategyData dto = TestHelper.readPlanComparisonRequest("/strategySummary/StrategySummaryUpdate.json", type)
				.get();
		Company company = prepareCompany();
		company.setBenefitStartDate("01-FEB-2022");
		company.setPlanStartDate("01-MAY-2023");
		company.setProspectCompany(true);
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		strategy.setName("Strategy 1");
		List<Strategy> strategies = new ArrayList<>(List.of(strategy));
		long groupId1 = 335718L;
		long groupId2 = 335719L;
		List<PlanSelectionDetail> planSelectionDetails = new ArrayList<>();
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("1D").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("14").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("1D").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("14").build());
		Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
		existingMedStrategyPortfolioMap.put(Long.valueOf(1), true);
		existingMedStrategyPortfolioMap.put(Long.valueOf(2), false);

		when(strategyDao.findByCompanyId(COMPANY_ID)).thenReturn(strategies);
		Mockito.doAnswer(new Answer<Strategy>() {
			public Strategy answer(InvocationOnMock invocation) {
				strategyArgCaptor.getValue().setId(STRATEGY_ID);
				return strategyArgCaptor.getValue();
			}
		}).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
		when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class)))
				.thenReturn(prepareBenGroups().get(0));
		when(planSelectionService.findDistinctPlanTypeBy(Set.of(STRATEGY_ID), Set.of(335718L, 335719L)))
				.thenReturn(planSelectionDetails);
		when(portfolioRuleDao.getMedicalPortfoliosBy(STRATEGY_ID, company.getRealmPlanYearId(),
				company.getHeadQuatersState())).thenReturn(existingMedStrategyPortfolioMap);

		strategyService.createUpdateStrategy(dto, company, false);

		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
				Set.of(groupId1, groupId2), null, Set.of("11", "1D"));
	}

	@Test
	public void createUpdateStrategy_testPlanAssignmentForCustomStrategyWithVisionERToEEChange() {
		TypeReference<StrategyData> type = new TypeReference<StrategyData>() {
		};
		StrategyData dto = TestHelper.readPlanComparisonRequest("/strategySummary/StrategySummaryUpdate.json", type)
				.get();
		Company company = prepareCompany();
		company.setBenefitStartDate("01-FEB-2022");
		company.setPlanStartDate("01-MAY-2023");
		company.setProspectCompany(true);
		Map<String, Set<PlanCarrier>> existingPortfolios = preparePlanCarriers();
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		strategy.setName("Strategy 1");
		List<Strategy> strategies = new ArrayList<>(List.of(strategy));
		long groupId1 = 335718L;
		long groupId2 = 335719L;
		List<PlanSelectionDetail> planSelectionDetails = new ArrayList<>();
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("11").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("1V").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("11").build());
		planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("1V").build());
		Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
		existingMedStrategyPortfolioMap.put(Long.valueOf(1), true);
		existingMedStrategyPortfolioMap.put(Long.valueOf(2), false);

		when(strategyDao.findByCompanyId(COMPANY_ID)).thenReturn(strategies);
		Mockito.doAnswer(new Answer<Strategy>() {
			public Strategy answer(InvocationOnMock invocation) {
				strategyArgCaptor.getValue().setId(STRATEGY_ID);
				return strategyArgCaptor.getValue();
			}
		}).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
		when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class)))
				.thenReturn(prepareBenGroups().get(0));
		when(planSelectionService.findDistinctPlanTypeBy(Set.of(STRATEGY_ID), Set.of(335718L, 335719L)))
				.thenReturn(planSelectionDetails);
		when(portfolioRuleDao.getMedicalPortfoliosBy(STRATEGY_ID, company.getRealmPlanYearId(),
				company.getHeadQuatersState())).thenReturn(existingMedStrategyPortfolioMap);

		strategyService.createUpdateStrategy(dto, company, false);

		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of(STRATEGY_ID),
				Set.of(groupId1, groupId2), null, Set.of("14", "1V"));
	}

	@Test
	public void findByCompanyIdAndSubmittedTest() {

		long companyId = 111;
		List<Strategy> strategiesList = prepareStrategiesData();

		when(strategyDao.findByCompanyIdAndSubmitted(companyId, false)).thenReturn(strategiesList);

		List<Strategy> result = strategyService.findByCompanyIdAndSubmitted(companyId, false);

		assertNotNull(result);
		assertEquals(2, result.size());
		verify(strategyDao, times(1)).findByCompanyIdAndSubmitted(companyId, false);

	}

	@Test
	public void updateStrategiesStatusTest() {

		List<Long> strategyIds = List.of(111L, 222L);

		strategyService.updateStrategiesStatus(strategyIds, BSSApplicationConstants.STATUS_IN_ACTIVE);

		verify(strategyDao, times(1)).updateStrategiesStatus(strategyIds, BSSApplicationConstants.STATUS_IN_ACTIVE);

	}

	@Test
	public void updateClientStrategyTypeSuccessTest() {
		Strategy strategy = new Strategy();
		when(strategyDao.findById(anyLong())).thenReturn(strategy);

		strategyService.updateSubmittedStrategyDetails(1L);

		verify(strategyDao, times(1)).findById(1L);
		verify(strategyDao, times(1)).save(strategy);
	}

	private List<Strategy> prepareStrategiesData() {
		Strategy strategy1 = new Strategy();
		strategy1.setName("Strategy 1");
		strategy1.setId(111L);
		Strategy strategy2 = new Strategy();
		strategy2.setName("Strategy 2");
		strategy2.setId(222L);

		List<Strategy> strategies = new ArrayList<>(List.of(strategy1, strategy2));
		return strategies;

	}

	private Map<Long, List<PlanContribution>> prepareConstributionsMap() {
		// TODO Auto-generated method stub
		return null;
	}

	private List<Long> preparePlanSelectionIds() {
		// TODO Auto-generated method stub
		return null;
	}

	private Map<String, List<BenefitPlanRate>> preparePlanRates() {
		// TODO Auto-generated method stub
		return null;
	}

	private Map<String, ActiveEligibleEECount> prepareActiveEligEECount() {
		Map<String, ActiveEligibleEECount> waiverHC = new HashMap<>();
		ActiveEligibleEECount aeh = new ActiveEligibleEECount();
		aeh.setPrimaryHeadCount(2);
		aeh.setSecondaryHeadCount(4);
		aeh.setTotalHeadCount(0);
		aeh.setBenProg("EF1");
		waiverHC.put("EF1", aeh);
		return waiverHC;
	}

	private Map<String, Long> prepareStrategyWaiverHeadCount() {
		Map<String, Long> waiverHC = new HashMap<>();
		waiverHC.put("EF1", 20L);
		return waiverHC;
	}

	private Map<String, PlanTypeDescription> preparePlanTypeDescriptions() {
		Map<String, PlanTypeDescription> planTypeDescriptions = new HashMap<>();
		PlanTypeDescription pd = new PlanTypeDescription();
		pd.setPlanType("10");
		pd.setDescription("medical");
		planTypeDescriptions.put("10", pd);
		pd = new PlanTypeDescription();
		pd.setPlanType("23");
		pd.setDescription("Life");
		planTypeDescriptions.put("23", pd);
		pd = new PlanTypeDescription();
		pd.setPlanType("A3");
		pd.setDescription("Commuter");
		planTypeDescriptions.put("23", pd);
		pd = new PlanTypeDescription();
		pd.setPlanType("23");
		pd.setDescription("Life");
		planTypeDescriptions.put("23", pd);
		pd = new PlanTypeDescription();
		pd.setPlanType("30");
		pd.setDescription("STD");
		planTypeDescriptions.put("30", pd);
		return planTypeDescriptions;
	}

	private StrategyData prepareStrategyData(long strategyId, String packageType, boolean submitted) {
		StrategyData data = new StrategyData();
		StrategySummary strategySummary = prepareSummaryData(packageType);
		strategySummary.setId(strategyId);
		if (submitted) {
			strategySummary.setSubmitted(submitted);
			strategySummary.setSubmitDate(new Date());
			strategySummary.setSubmitStatus("SUCCESS");
		} else {
			strategySummary.setSubmitted(false);
			strategySummary.setSubmitStatus(null);
		}
		strategySummary.setFilterRegions(Arrays.asList("CA", "MA", "GA"));
		List<StrategyBenefitGroup> benefitGroups = new ArrayList<>();
		benefitGroups.add(prepareStrategyBenGrp());
		data.setStrategySummary(strategySummary);
		data.setBenefitGroups(benefitGroups);
		data.setStrategyHsaFunding(prepareStrategyHsaFundingDto());
		return data;
	}

	private StrategySummary prepareSummaryData(String packageType) {
		StrategySummary summary = new StrategySummary();
		summary.setId(STRATEGY_ID);
		summary.setHeadcount(SUMMARY1_HEADCOUNT);
		summary.setAcaFplOpted(SUMMARY1_ISACA_FPL_OPTD);
		summary.setSubmitted(true);
		summary.setName("Test Strategy 1");
		summary.setSubmitDate(new Date());
		summary.setEstimatedTotalCost(BigDecimal.valueOf(100));
		summary.setCurrentYearTotalCost(BigDecimal.valueOf(1000));
		summary.setHeadcount(2);
		summary.setPkgType(packageType);
		return summary;
	}

	private StrategyBenefitGroup prepareStrategyBenGrp() {
		StrategyBenefitGroup benGrp = new StrategyBenefitGroup();
		benGrp.setId(BEN_GRP_ID3);
		benGrp.setDefaultGroup(false);
		benGrp.setBenefitProgram("EF1");
		benGrp.setCompanyId(COMPANY_ID);
		benGrp.setEstimatedTotalCost(BigDecimal.valueOf(40000));
		benGrp.setHasVolDental(true);
		benGrp.setHasVolVision(true);
		benGrp.setHeadcount(30L);
		benGrp.setName("Group 1");
		benGrp.setRegion("N");
		benGrp.setState("MA");
		benGrp.setStatus("A");
		benGrp.setStrategyGroupId(1111L);
		benGrp.setStrategyId(STRATEGY_ID);
		benGrp.setType("STD");
		benGrp.setWaitingPeriod("FDOH");
		benGrp.setWaitPeriodDescr("First Day of Hire");
		Map<String, Integer> coverageLevelHeadCounts = new HashMap<>();
		coverageLevelHeadCounts.put("employee", 1);
		coverageLevelHeadCounts.put("employeePlusSpouse", 1);
		benGrp.setCoverageLevelHeadCounts(coverageLevelHeadCounts);
		benGrp.setBenefitOffers(prepareBenOffers());
		return benGrp;
	}

	private Company prepareCompany() {
		Company comp = new Company();
		comp.setId(COMPANY_ID);
		comp.setCode(COMPANY_CODE);
		comp.setRenewalCompany(false);
		comp.setPfClient("PFCLIENT");
		comp.setPlanStartDate("01-Jan-2019");
		comp.setHeadQuatersState("CA");
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setPlanYearEnd(java.sql.Date.valueOf("2018-12-31"));
		realmPlanYear.setPlanYearStart(java.sql.Date.valueOf("2019-01-01"));
		realmPlanYear.setId(REALM_PLYR_ID);
		comp.setRealmPlanYear(realmPlanYear);
		comp.setRealmPlanYearId(REALM_PLYR_ID);
		Set<String> companyRegions = new HashSet<>();
		companyRegions.add("MA");
		comp.setCompanyRegions(companyRegions);
		List<String> employeeRegions = new ArrayList<>();
		employeeRegions.add("CT");
		comp.setEmployeeRegions(employeeRegions);
		Realm realm = new Realm();
		realm.setBenExchange("TriNetIII");
		comp.setRealm(realm);
		return comp;
	}

	private List<BenefitGroup> prepareBenGroups() {
		List<BenefitGroup> benGrps = new ArrayList<>();
		BenefitGroup benGrp = new BenefitGroup();
		benGrp.setId(BEN_GRP_ID3);
		benGrp.setBenefitOffers(prepareBenOffers());
		BenefitGroupStrategy bgs = new BenefitGroupStrategy();

		benGrp.getBenefitGroupStrategy().add(bgs);
		benGrps.add(benGrp);
		benGrp = new BenefitGroup();
		benGrp.setId(BEN_GRP_ID4);
		benGrp.getBenefitGroupStrategy().add(bgs);
		benGrp.setBenefitOffers(Collections.emptyList());
		benGrps.add(benGrp);

		return benGrps;
	}
	private List<BenefitGroup> prepareBenGroups1() {
		List<BenefitGroup> benGrps = new ArrayList<>();
		BenefitGroup benGrp = new BenefitGroup();
		benGrp.setId(BEN_GRP_ID3);
		benGrp.setBenefitOffers(prepareBenOffers());
		BenefitGroupStrategy bgs = new BenefitGroupStrategy();
		benGrp.getBenefitGroupStrategy().add(bgs);
		benGrps.add(benGrp);

		return benGrps;
	}

	private List<BenefitGroup> prepareBenGroupsBsupp() {
		List<BenefitGroup> benGrps = new ArrayList<>();
		BenefitGroup benGrp = new BenefitGroup();
		benGrp.setId(BEN_GRP_ID3);
		benGrp.setBenefitOffers(prepareBenOffersBsupp());
		benGrps.add(benGrp);
		return benGrps;
	}

	private List<StrategyBenefitGroup> prepareStrategyBenGroupsBsupp() {
		List<StrategyBenefitGroup> benGrps = new ArrayList<>();
		StrategyBenefitGroup benGrp = new StrategyBenefitGroup();
		benGrp.setId(BEN_GRP_ID3);
		benGrp.setBenefitOffers(prepareBenOffersBsupp());
		benGrp.setName("GROUP_NAME");
		benGrps.add(benGrp);
		return benGrps;
	}

	private List<BenefitOffer> prepareBenOffersBsupp() {
		List<BenefitOffer> benOffers = new ArrayList<>();
		BenefitOffer benOffer = new BenefitOffer();
		benOffer.setSummary(prepareBenOfferSummary("medical"));
		benOffer.setBenefitPlans(Collections.emptyList());
		benOffer.setPlanPackage(preparePlanPckg("BSUPP", ""));
		benOffers.add(benOffer);
		return benOffers;
	}

	private List<StrategyGroupHeadCount> prepareStrategyGrpHeadCount() {
		List<StrategyGroupHeadCount> strategyGroupHeadCounts = new ArrayList<>();
		StrategyGroupHeadCount sghc = new StrategyGroupHeadCount();
		strategyGroupHeadCounts.add(sghc);
		return strategyGroupHeadCounts;
	}

	private List<BenefitOffer> prepareBenOffers() {
		List<BenefitOffer> benOffers = new ArrayList<>();
		BenefitOffer benOffer = new BenefitOffer();
		benOffer.setSummary(prepareBenOfferSummary("medical"));
		benOffer.setBenefitPlans(prepareBenefitPlans(MED_PLAN_ID1, false, true, false, "medical"));
		benOffer.setPlanPackage(preparePlanPckg("BFPCT", "FLTMAX"));
		benOffers.add(benOffer);
		benOffer = new BenefitOffer();
		benOffer.setSummary(prepareBenOfferSummary("dental"));
		benOffer.setBenefitPlans(prepareBenefitPlans(DEN_PLAN_ID1, true, false, false, "dental"));
		benOffer.setPlanPackage(preparePlanPckg("BFPCT", ""));
		benOffers.add(benOffer);
		benOffer = new BenefitOffer();
		benOffer.setSummary(prepareBenOfferSummary("vision"));
		benOffer.setBenefitPlans(prepareBenefitPlans(VIS_PLAN_ID1, true, false, false, "vision"));
		benOffer.setPlanPackage(preparePlanPckg("CFPCT", ""));
		benOffers.add(benOffer);
		benOffer = new BenefitOffer();
		benOffer.setSummary(prepareBenOfferSummary("additionalBenefit"));
		benOffer.setBenefitPlans(prepareBenefitPlans(AD_BEN_PLAN_ID1, false, false, false, "additionalBenefit"));
		benOffer.setPlanPackage(null);
		benOffer.setAdditionalBenefitOffers(prepareAdditionalBenOffers());
		benOffers.add(benOffer);
		return benOffers;
	}

	private List<AdditionalBenefitOffer> prepareAdditionalBenOffers() {
		List<AdditionalBenefitOffer> additionalBenefitOffers = new ArrayList<>();
		AdditionalBenefitOffer additionalBenefitOffer = new AdditionalBenefitOffer();
		BenefitOfferSummary benefitOfferSummary = new BenefitOfferSummary();
		additionalBenefitOffer
				.setAdditionalBenefitPlans(prepareAdditionalBenefitPlans(AD_BEN_PLAN_ID1, AD_PLAN_TYPE_CMTR));
		benefitOfferSummary.setType(AD_PLAN_TYPE_CMTR);
		benefitOfferSummary.setHeadcount(2);
		additionalBenefitOffer.setSummary(benefitOfferSummary);
		additionalBenefitOffers.add(additionalBenefitOffer);
		additionalBenefitOffer = new AdditionalBenefitOffer();
		additionalBenefitOffer
				.setAdditionalBenefitPlans(prepareAdditionalBenefitPlans(AD_BEN_PLAN_ID2, AD_PLAN_TYPE_STD));
		benefitOfferSummary = new BenefitOfferSummary();
		benefitOfferSummary.setType(AD_PLAN_TYPE_STD);
		benefitOfferSummary.setHeadcount(2);
		additionalBenefitOffer.setSummary(benefitOfferSummary);
		additionalBenefitOffers.add(additionalBenefitOffer);
		return additionalBenefitOffers;
	}

	private List<AdditionalBenefitPlan> prepareAdditionalBenefitPlans(String planId, String planType) {
		List<AdditionalBenefitPlan> list = new ArrayList<>();
		AdditionalBenefitPlan plan = new AdditionalBenefitPlan();
		plan.setId(planId);
		plan.setPlanType(planType);
		List<DisabilityBenefitOptionPlans> optionPlans = new ArrayList<>();
		DisabilityBenefitOptionPlans optionPlan = new DisabilityBenefitOptionPlans();
		optionPlan.setPlanHeadCount(DBO_PLAN_HC);
		optionPlan.setId(DBO_PLAN_ID);
		optionPlan.setPlanType(DBO_PLAN_TYPE);
		optionPlans.add(optionPlan);
		plan.setOptionPlans(optionPlans);
		list.add(plan);
		return list;
	}

	private BenefitOfferSummary prepareBenOfferSummary(String type) {
		BenefitOfferSummary summary = new BenefitOfferSummary();
		summary.setType(type);
		return summary;
	}

	private List<BenefitPlan> prepareBenefitPlans(String id, boolean empPaid, boolean ppoPlan, boolean highDeductible,
			String planType) {
		List<BenefitPlan> plans = new ArrayList<>();
		BenefitPlan benPlan = new BenefitPlan();
		benPlan.setId(id);
		benPlan.setEmployeePaid(empPaid);
		benPlan.setPpoPlan(ppoPlan);
		benPlan.setWidelyAvailablePlan(ppoPlan);
		benPlan.setHighDeductible(highDeductible);
		benPlan.setPlanType(planType);
		plans.add(benPlan);
		return plans;
	}

	private PlanPackage preparePlanPckg(String fundingType, String fundingBasePlan) {
		PlanPackage planPckg = new PlanPackage();
		planPckg.setFundingType(fundingType);
		Map<String, BigDecimal> coverageLevelFunding = new HashMap<>();
		coverageLevelFunding.put("employee", BigDecimal.valueOf(100));
		coverageLevelFunding.put("employeePlusSpouse", BigDecimal.valueOf(80));
		coverageLevelFunding.put("employeePlusChild", BigDecimal.valueOf(90));
		coverageLevelFunding.put("employeePlusFamily", BigDecimal.valueOf(80));
		planPckg.setCoverageLevelFunding(coverageLevelFunding);
		if ("FLTMAX".equals(fundingBasePlan)) {
			planPckg.setFundingBasePlan("FLTMAX");
			Map<String, BigDecimal> coverageLevelFundingFltMx = new HashMap<>();
			coverageLevelFundingFltMx.put("employee", BigDecimal.valueOf(500));
			coverageLevelFundingFltMx.put("employeePlusSpouse", BigDecimal.valueOf(600));
			coverageLevelFundingFltMx.put("employeePlusChild", BigDecimal.valueOf(650));
			coverageLevelFundingFltMx.put("employeePlusFamily", BigDecimal.valueOf(700));
			planPckg.setCoverageLevelFundingFlatMax(coverageLevelFundingFltMx);
		} else if ("BSUPP".equals(fundingType)) {
			List<String> bsuppSelectedVolPlanTypes = new ArrayList<>();
			bsuppSelectedVolPlanTypes.add("30");
			bsuppSelectedVolPlanTypes.add("31");
			planPckg.setBsuppSelectedVolPlanTypes(bsuppSelectedVolPlanTypes);
		}
		return planPckg;
	}

	private RealmPlanYear prepareRealmPlanYear() {
		RealmPlanYear realmYr = new RealmPlanYear();
		realmYr.setId(10);
		realmYr.setPlanYearEnd(new Date());
		return realmYr;
	}

	private Strategy prepareStrategy() {
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		strategy.setType(BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED);
		strategy.setSubmitted(false);
		strategy.setSubmitDate(null);
		return strategy;
	}

	private Map<Long, Map<String, Map<Long, List<PlanSelection>>>> preparePlanSelections() {
		Map<Long, Map<String, Map<Long, List<PlanSelection>>>> map = new HashMap<>();
		Map<String, Map<Long, List<PlanSelection>>> planSelectionsMap = new HashMap<>();
		Map<Long, List<PlanSelection>> adtnlPlanSelections = new HashMap<>();
		adtnlPlanSelections.put(BEN_GRP_ID3, preparePlanSelectionsList());
		planSelectionsMap.put("additionalBenefit", adtnlPlanSelections);
		Map<Long, List<PlanSelection>> medicalPlanSelections = new HashMap<>();
		medicalPlanSelections.put(BEN_GRP_ID3, preparePlanSelectionsList());
		planSelectionsMap.put("medical", medicalPlanSelections);
		map.put(STRATEGY_ID, planSelectionsMap);
		return map;
	}

	private List<PlanSelection> preparePlanSelectionsList() {
		List<PlanSelection> planSelections = new ArrayList<>();
		PlanSelection planSelection = new PlanSelection();
		planSelection.setId(MED_PLAN_SELECTION_ID1);
		planSelection.setBenefitPlan(MED_PLAN_ID1);
		planSelection.setListOfStates(Arrays.asList("CA", "MA"));
		planSelections.add(planSelection);
		planSelection = new PlanSelection();
		planSelection.setId(DEN_PLAN_SELECTION_ID1);
		planSelection.setListOfStates(Arrays.asList("CA", "MA"));
		planSelection.setBenefitPlan(DEN_PLAN_ID1);
		planSelections.add(planSelection);
		planSelection = new PlanSelection();
		planSelection.setId(AD_PLAN_SELECTION_ID1);
		planSelection.setBenefitPlan("Life");
		planSelection.setPlanType("23");
		planSelection.setListOfStates(Arrays.asList("CA", "MA"));
		planSelections.add(planSelection);
		planSelection = new PlanSelection();
		planSelection.setId(AD_PLAN_SELECTION_ID2);
		planSelection.setBenefitPlan("Commuter");
		planSelection.setPlanType("A2");
		planSelection.setListOfStates(Arrays.asList("CA", "MA"));
		planSelections.add(planSelection);
		return planSelections;
	}

	private PlanSelection prepareADPlanSelection() {
		return new PlanSelection();
	}

	private List<BenefitGroupStrategy> prepareBenGrpStrategy() {
		List<BenefitGroupStrategy> list = new ArrayList<>();
		BenefitGroupStrategy bgs = new BenefitGroupStrategy();
		bgs.setBenefitGroup(prepareBenGroups().get(0));
		list.add(bgs);
		return list;
	}

	private StrategyHsaFundingDto prepareStrategyHsaFundingDto() {
		return new StrategyHsaFundingDto();
	}

	private Map<String, BigDecimal> preparePlanCosts() {
		Map<String, BigDecimal> costs = new HashMap<>();
		costs.put(MED_PLAN_ID1, BigDecimal.valueOf(200.50));
		costs.put(DEN_PLAN_ID1, BigDecimal.valueOf(20.75));
		return costs;
	}
	
	private List<Strategy> prepareHistoryStrategies() {
		List<Strategy> strategies = new ArrayList<>();
		Strategy historyStrategy = new Strategy();
		
		historyStrategy.setId(Long.valueOf(111));
		
		strategies.add(historyStrategy);
		return strategies;
	}
	
	private Map<Long, List<AdditionalBenefitPlan>> prepareHistoryAdditionalOfferings() {
		Map<Long, List<AdditionalBenefitPlan>> offerings = new HashMap<>();
		
		List<AdditionalBenefitPlan> additionalBenefitPlans = new ArrayList<>();
		
		AdditionalBenefitPlan additionalBenefitPlan = new AdditionalBenefitPlan();
		additionalBenefitPlan.setPlanType(PlanTypesEnum.LIFE.getCode());
		additionalBenefitPlan.setId(LIFE_PLAN_ID1);
		additionalBenefitPlans.add(additionalBenefitPlan);
		
		additionalBenefitPlan = new AdditionalBenefitPlan();
		additionalBenefitPlan.setPlanType(PlanTypesEnum.LTD.getCode());
		additionalBenefitPlan.setId(LTD_PLAN_ID1);
		additionalBenefitPlans.add(additionalBenefitPlan);
		
		additionalBenefitPlan = new AdditionalBenefitPlan();
		additionalBenefitPlan.setPlanType(PlanTypesEnum.STD.getCode());
		additionalBenefitPlan.setId(STD_PLAN_ID1);
		additionalBenefitPlans.add(additionalBenefitPlan);
		
		additionalBenefitPlan = new AdditionalBenefitPlan();
		additionalBenefitPlan.setPlanType(PlanTypesEnum.STD.getCode());
		additionalBenefitPlan.setId(STD_PLAN_ID2);
		additionalBenefitPlans.add(additionalBenefitPlan);
		
		additionalBenefitPlan = new AdditionalBenefitPlan();
		additionalBenefitPlan.setPlanType(PlanTypesEnum.CMTR.getCode());
		additionalBenefitPlan.setId(COMUTER_PLAN_ID);
		additionalBenefitPlans.add(additionalBenefitPlan);
		
		offerings.put(BEN_GRP_ID3, additionalBenefitPlans);
		return offerings;
	}
	
	private List<StrategyBenefitGroup> prepareStrategyBenGroupsAdditionalBenefits() {

		List<StrategyBenefitGroup> benGrps = new ArrayList<>();
		
		StrategyBenefitGroup benGrp = new StrategyBenefitGroup();
		benGrp.setId(BEN_GRP_ID3);
		benGrp.setBenefitOffers(prepareBenefitOffersAdditionalBenefits());
		benGrp.setName("GROUP_3333");
		benGrps.add(benGrp);

		benGrp = new StrategyBenefitGroup();
		benGrp.setId(BEN_GRP_ID4);
		benGrp.setBenefitOffers(prepareBenefitOffersAdditionalBenefits());
		benGrp.setName("GROUP_4444");
		benGrps.add(benGrp);
		
		return benGrps;
	}

	private List<BenefitOffer> prepareBenefitOffersAdditionalBenefits() {
		List<BenefitOffer> benOffers = new ArrayList<>();
		BenefitOffer benOffer = new BenefitOffer();
		benOffer.setSummary(prepareBenOfferSummary(BSSApplicationConstants.ADDITIONAL));
		benOffer.setBenefitPlans(Collections.emptyList());
		benOffer.setPlanPackage(null);
		benOffer.setAdditionalBenefitOffers(prepareAdditionalBenefitOffers());
		benOffers.add(benOffer);
		return benOffers;
	}
	
	private List<AdditionalBenefitOffer> prepareAdditionalBenefitOffers() {
		List<AdditionalBenefitOffer> additionalBenefitOffers = new ArrayList<>();
		additionalBenefitOffers.add(prepareAdditionalBenefitOffer());
		return additionalBenefitOffers;
	}
	
	private AdditionalBenefitOffer prepareAdditionalBenefitOffer() {
		AdditionalBenefitOffer additionalBenefitOffer = new AdditionalBenefitOffer();
		additionalBenefitOffer.setSummary(new BenefitOfferSummary());
		additionalBenefitOffer.getSummary().setType(BSSApplicationConstants.LIFE);
		additionalBenefitOffer.setAdditionalBenefitPlans(prepareUpdatedAdditionalBenefitPlans());
		return additionalBenefitOffer;
	}
	
	private List<AdditionalBenefitPlan> prepareUpdatedAdditionalBenefitPlans() {
		
		List<AdditionalBenefitPlan> additionalBenefitPlans = new ArrayList<>();
		
		AdditionalBenefitPlan additionalBenefitPlan = new AdditionalBenefitPlan();
		additionalBenefitPlan.setPlanType(PlanTypesEnum.LIFE.getCode());
		additionalBenefitPlan.setId(LIFE_PLAN_ID1);
		additionalBenefitPlans.add(additionalBenefitPlan);
		
		additionalBenefitPlan = new AdditionalBenefitPlan();
		additionalBenefitPlan.setPlanType(PlanTypesEnum.LTD.getCode());
		additionalBenefitPlan.setId(LTD_PLAN_ID1);
		additionalBenefitPlans.add(additionalBenefitPlan);
		
		additionalBenefitPlan = new AdditionalBenefitPlan();
		additionalBenefitPlan.setPlanType(PlanTypesEnum.STD.getCode());
		additionalBenefitPlan.setId(STD_PLAN_ID1);
		additionalBenefitPlans.add(additionalBenefitPlan);
		
		additionalBenefitPlan = new AdditionalBenefitPlan();
		additionalBenefitPlan.setPlanType(PlanTypesEnum.STD.getCode());
		additionalBenefitPlan.setId(STD_PLAN_ID3);
		additionalBenefitPlans.add(additionalBenefitPlan);

		return additionalBenefitPlans;
	}	
		
	private StrategyData prepareProspectStrategy() {
		String companyId = "SFDC_PROSPECT_ID";
		StrategyData strategyData = new StrategyData();
		StrategySummary strategySummary = new StrategySummary();
		strategySummary.setId(ProspectConstants.PROSPECT_STRATEGY_ID);
		strategySummary.setName(ProspectConstants.PROSPECT_STRATEGY_NAME);
		strategySummary.setType(ProspectConstants.PROSPECT);
		strategySummary.setEstimatedTotalCost(BigDecimal.valueOf(1329.78));
		strategySummary.setHeadcount(29);
		strategySummary.setTotalBudget(BigDecimal.ZERO);
		strategySummary.setBudgetFactor(1);
		strategySummary.setCompanyId(companyId);
		strategyData.setStrategySummary(strategySummary);
		strategyData.setBenefitGroups(prepareBenefitGroup());
		StrategyHsaFundingDto strategyHsaFundingDto = new StrategyHsaFundingDto();
		strategyHsaFundingDto.setOptionId(0);
		strategyData.setStrategyHsaFunding(strategyHsaFundingDto);
		return strategyData;
	}

	private List<StrategyBenefitGroup> prepareBenefitGroup() {
		List<StrategyBenefitGroup> strategyBenefitGroupList = new ArrayList<>();
		StrategyBenefitGroup strategyBenefitGroup = new StrategyBenefitGroup();
		strategyBenefitGroup.setId(111);
		strategyBenefitGroup.setName("PermEmp");
		strategyBenefitGroup.setType("K1");
		strategyBenefitGroup.setStatus("A");
		strategyBenefitGroup.setBenefitProgram(null);
		strategyBenefitGroup.setCompanyId(101);
		strategyBenefitGroup.setStrategyId(0L);
		strategyBenefitGroup.setStrategyGroupId(0L);
		strategyBenefitGroup.setEstimatedTotalCost(BigDecimal.valueOf(2394.95));
		strategyBenefitGroup.setHeadcount(10);
		strategyBenefitGroup.setBenefitOffers(prepareBenefitOffer());
		strategyBenefitGroupList.add(strategyBenefitGroup);
		return strategyBenefitGroupList;
	}

	private List<BenefitOffer> prepareBenefitOffer() {
		List<BenefitOffer> benefitOffers = new ArrayList<>();
		BenefitOffer benefitOffer = new BenefitOffer();
		BenefitOfferSummary benefitOfferSummary = new BenefitOfferSummary();
		benefitOfferSummary.setType("vision");
		benefitOfferSummary.setGroupId(111);
		benefitOfferSummary.setDescription("14");
		benefitOfferSummary.setEstimatedTotalCost(BigDecimal.valueOf(2394.90));
		benefitOfferSummary.setBaseFundingRequired(false);
		benefitOffer.setSummary(benefitOfferSummary);

		Set<PlanCarrier> planCarrierSet = new LinkedHashSet<>();
		PlanCarrier planCarrier = new PlanCarrier();
		planCarrier.setId(1);
		planCarrier.setName("Aetna HDHP 3000");
		PlanCarrier planCarrier1 = new PlanCarrier();
		planCarrier1.setId(2);
		planCarrier1.setName("Blue Shield of California");
		planCarrierSet.add(planCarrier);
		benefitOffer.setPlanCarriers(planCarrierSet);

		PlanPackage planPackage = new PlanPackage();
		planPackage.setFundingModelId(0L);
		planPackage.setName("renewal");
		planPackage.setCustomized(true);
		planPackage.setFundingType("PCT");
		Map<String, BigDecimal> covgLevelFunding = new HashMap<>();
		covgLevelFunding.put("employeePlusChild", BigDecimal.valueOf(60));
		covgLevelFunding.put("employeePlusSpouse", BigDecimal.valueOf(80));
		covgLevelFunding.put("employee", BigDecimal.valueOf(100));
		covgLevelFunding.put("employeePlusFamily", BigDecimal.valueOf(70));
		planPackage.setCoverageLevelFunding(covgLevelFunding);
		benefitOffer.setPlanPackage(planPackage);
		benefitOffers.add(benefitOffer);

		return benefitOffers;
	}

	private static void assertBenefitGroup(List<StrategyBenefitGroup> benefitGroups, StrategyData prospectStrategy) {
		assertEquals(111, benefitGroups.get(0).getId());
		assertEquals("PermEmp", benefitGroups.get(0).getName());
		assertEquals("K1", benefitGroups.get(0).getType());
		assertEquals("A", benefitGroups.get(0).getStatus());
		assertNull(benefitGroups.get(0).getBenefitProgram());
		assertEquals(101, benefitGroups.get(0).getCompanyId());
		assertEquals(0L, benefitGroups.get(0).getStrategyId());
		assertEquals(0L, benefitGroups.get(0).getStrategyGroupId());
		assertEquals(BigDecimal.valueOf(2394.95), benefitGroups.get(0).getEstimatedTotalCost());
		assertEquals(10, benefitGroups.get(0).getHeadcount());

		List<BenefitOffer> benefitOffers = benefitGroups.get(0).getBenefitOffers();
		assertEquals("vision", benefitOffers.get(0).getSummary().getType());
		assertEquals(111, benefitOffers.get(0).getSummary().getGroupId());
		assertEquals("14", benefitOffers.get(0).getSummary().getDescription());
		assertEquals(BigDecimal.valueOf(2394.90), benefitOffers.get(0).getSummary().getEstimatedTotalCost());
		assertFalse(benefitOffers.get(0).getSummary().isBaseFundingRequired());

		Set<PlanCarrier> expectedplanCarrierSet = prospectStrategy.getBenefitGroups().get(0).getBenefitOffers().get(0)
				.getPlanCarriers();
		assertEquals(expectedplanCarrierSet, benefitOffers.get(0).getPlanCarriers());

		assertEquals(0, benefitOffers.get(0).getPlanPackage().getFundingModelId());
		assertEquals("renewal", benefitOffers.get(0).getPlanPackage().getName());
		assertTrue(benefitOffers.get(0).getPlanPackage().isCustomized());
		assertEquals("PCT", benefitOffers.get(0).getPlanPackage().getFundingType());
		assertEquals(BigDecimal.valueOf(60),
				benefitOffers.get(0).getPlanPackage().getCoverageLevelFunding().get("employeePlusChild"));
		assertEquals(BigDecimal.valueOf(80),
				benefitOffers.get(0).getPlanPackage().getCoverageLevelFunding().get("employeePlusSpouse"));
		assertEquals(BigDecimal.valueOf(100),
				benefitOffers.get(0).getPlanPackage().getCoverageLevelFunding().get("employee"));
		assertEquals(BigDecimal.valueOf(70),
				benefitOffers.get(0).getPlanPackage().getCoverageLevelFunding().get("employeePlusFamily"));
	}

	private Map<String, XbssRealmPlyrPlan> prepareRealmPlanMap() {
		Map<String, XbssRealmPlyrPlan> rpps = new HashMap<>();

		XbssRealmPlyrPlan rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("MEDPLAN1");
		rpp.setPlanType(MEDICAL_PLAN_TYPE);
		rpps.put("MEDPLAN1", rpp);
		return rpps;
	}

	private Map<String, BigDecimal> preparePlanEstCost() {
		Map<String, BigDecimal> map = new HashMap<>();
		map.put("000SRP", BigDecimal.valueOf(450000));
		return map;
	}

	private List<Object[]> prepareHealthCostsByPlanType(boolean includeMedical) {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[3];
		if (includeMedical) {
			r[0] = "medical";
			r[1] = new BigDecimal(100);
			r[2] = new BigDecimal(200);
			results.add(r);
			r = new Object[3];
		}
		r[0] = "dental";
		r[1] = new BigDecimal(20);
		r[2] = new BigDecimal(40);
		results.add(r);
		r = new Object[3];
		r[0] = "vision";
		r[1] = new BigDecimal(10);
		r[2] = new BigDecimal(20);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareAdditionalBenefitCostsByPlanType(boolean includeLife) {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[2];
		if (includeLife) {
			r[0] = "LIFE";
			r[1] = new BigDecimal(100);
			results.add(r);
			r = new Object[2];
		}
		r[0] = "DISABILITY";
		r[1] = new BigDecimal(20);
		results.add(r);
		return results;
	}

	private Set<String> prepareRealmPlanTypes(boolean includeMedical) {
		Set<String> realmPlanTypes = new HashSet<>();
		if (includeMedical) {
			realmPlanTypes.add("10");
		}
		realmPlanTypes.add("11");
		realmPlanTypes.add("1D");
		realmPlanTypes.add("14");
		realmPlanTypes.add("1V");
		realmPlanTypes.add("23");
		realmPlanTypes.add("30");
		realmPlanTypes.add("31");
		return realmPlanTypes;
	}

	private Map<String, BigDecimal> prepareEstimate() {
		Map<String, BigDecimal> estimate = new HashMap<>();
		estimate.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, BigDecimal.valueOf(100));
		estimate.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, BigDecimal.valueOf(20));
		estimate.put(BSSApplicationConstants.VISION_PLAN_TYPE, BigDecimal.valueOf(10));
		return estimate;
	}

	private void assert_updateStrategyDefaultEEPlanAssignmentForMed(boolean isTibProspect, int noOfTimesUpdatedCalled) {
	    // Given
	    TypeReference<StrategyData> type = new TypeReference<StrategyData>() {
	    };
	    StrategyData dto = TestHelper.readPlanComparisonRequest("/strategySummary/NewStrategySummarySave.json", type)
		    .get();
	    Company company = prepareCompany();
	    company.setBenefitStartDate("01-FEB-2022");
	    company.setPlanStartDate("01-MAY-2023");
	    company.setProspectCompany(true);
	    Map<String, Set<PlanCarrier>> existingPortfolios = preparePlanCarriers();
	    Strategy strategy = new Strategy();
	    strategy.setId(STRATEGY_ID);
	    strategy.setName("Strategy 1");
	    List<Strategy> strategies = new ArrayList<>(List.of(strategy));
	    List<PlanSelectionDetail> planSelectionDetails = new ArrayList<>();
	    long groupId1 = 335718L;
	    long groupId2 = 335719L;
	    Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
	    existingMedStrategyPortfolioMap.put(Long.valueOf(1), true);
	    existingMedStrategyPortfolioMap.put(Long.valueOf(18), false);
	    Map<Long, Boolean> newMedStrategyPortfolioMap = new HashMap<>();
	    newMedStrategyPortfolioMap.put(Long.valueOf(5), true);
	    newMedStrategyPortfolioMap.put(Long.valueOf(2), false);

	    when(strategyDao.findByCompanyId(COMPANY_ID)).thenReturn(strategies);
	    Mockito.doAnswer(new Answer<Strategy>() {
		public Strategy answer(InvocationOnMock invocation) {
		    strategyArgCaptor.getValue().setId(STRATEGY_ID);
		    return strategyArgCaptor.getValue();
		}
	    }).when(strategyDao).saveAndFlush(strategyArgCaptor.capture());
	    when(benefitGroupService.saveBenefitGroup(Mockito.any(BenefitGroup.class))).thenReturn(
		    prepareBenGroups().get(0));
	    when(portfolioRuleDao.getStrategyPortfolios(anyLong(), anyLong(), anyMap(), anyString(),
		    anyBoolean())).thenReturn(existingPortfolios);
	    planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("11").build());
	    planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId1).planType("14").build());
	    planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("11").build());
	    planSelectionDetails.add(PlanSelectionDetail.builder().groupId(groupId2).planType("14").build());
	    when(planSelectionService.findDistinctPlanTypeBy(Set.of(STRATEGY_ID), Set.of(335718L, 335719L))).thenReturn(
		    planSelectionDetails);
	    when(portfolioRuleDao.getMedicalPortfoliosBy(STRATEGY_ID, company.getRealmPlanYearId(),
		    company.getHeadQuatersState())).thenReturn(existingMedStrategyPortfolioMap, newMedStrategyPortfolioMap);
	    when(CompanyServiceHelper.isTibProspect(company)).thenReturn(isTibProspect);

	    // When
	    strategyService.createUpdateStrategy(dto, company, true);

	    //Then
	    verify(prospectDefaultPlanAssignmentService, times(noOfTimesUpdatedCalled)).assignDefaultPlanBy(
		    Set.of(STRATEGY_ID), Set.of(groupId1, groupId2), newMedStrategyPortfolioMap, Set.of("10"));

	}



/*


		List<String> benefitPlans = new ArrayList<>();

		List<AdditionalBenefitPlan> additionalOfferings = offering.stream()
				.flatMap(benefitOffer -> benefitOffer.getAdditionalBenefitOffers().stream())
				.flatMap(additionalOffering -> additionalOffering.getAdditionalBenefitPlans().stream())
				.collect(Collectors.toList());

		for (AdditionalBenefitPlan additionalPlan : additionalOfferings) {
			if (additionalPlan.getOptionPlans() != null) {
				for (DisabilityBenefitOptionPlans optionPlan : additionalPlan.getOptionPlans()) {
					benefitPlans.add(optionPlan.getId());
				}
			} else {
				benefitPlans.add(additionalPlan.getId());
			}
		}
		Collections.sort(benefitPlans);
		return benefitPlans;
	
 */
		

	// -----------------------------------------------------------------------
	// deleteStrategies tests
	// -----------------------------------------------------------------------

	@Test
	public void testDeleteStrategies_delegatesToHrpDao() {
		// given
		long companyId = 99001L;
		doNothing().when(hrpDao).deleteStrategiesByCompanyId(companyId);

		// when
		strategyService.deleteStrategies(companyId);

		// then
		verify(hrpDao).deleteStrategiesByCompanyId(eq(companyId));
	}

	@Test
	public void testDeleteStrategies_noStrategiesExist_completesSuccessfully() {
		// given — company with no strategies; DAO succeeds silently
		long companyId = 99001L;
		doNothing().when(hrpDao).deleteStrategiesByCompanyId(companyId);

		// when / then — must not throw
		strategyService.deleteStrategies(companyId);

		verify(hrpDao).deleteStrategiesByCompanyId(companyId);
	}

	@Test
	public void testDeleteStrategies_whenHrpDaoFails_exceptionPropagated() {
		// given
		long companyId = 99001L;
		RuntimeException cause = new RuntimeException("PL/SQL execution failed");
		doThrow(cause).when(hrpDao).deleteStrategiesByCompanyId(companyId);

		// when
		try {
			strategyService.deleteStrategies(companyId);
			fail("Expected RuntimeException to propagate");
		} catch (RuntimeException ex) {
			assertEquals(cause, ex);
		}

		verify(hrpDao).deleteStrategiesByCompanyId(companyId);
	}

	@Test
	public void testDeleteStrategies_usesExactCompanyId() {
		// given
		long specificCompanyId = 555999L;
		doNothing().when(hrpDao).deleteStrategiesByCompanyId(specificCompanyId);

		// when
		strategyService.deleteStrategies(specificCompanyId);

		// then — no mapping or transformation of the id
		verify(hrpDao).deleteStrategiesByCompanyId(specificCompanyId);
	}

}