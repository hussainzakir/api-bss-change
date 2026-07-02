package com.trinet.ambis.service.impl;

import com.trinet.ambis.client.DefaultPlanMappingServiceClient.PlanMappingResponse;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.BSSRateType;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.AdditionalBenefitServiceHelper;
import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.BenefitGroupServiceHelper;
import com.trinet.ambis.helper.CacheKeyGenerator;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.helper.RenewalServiceHelper;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeStrategyGroupTransactionDao;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.hrp.PlanSelectionDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyBasePlanLimitsDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingModelDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyHsaFundingDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.dao.ps.LifeAndDisabilityCalcData;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.GroupRate;
import com.trinet.ambis.persistence.model.GroupRatePK;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyFundBsuppPlanTypeId;
import com.trinet.ambis.persistence.model.StrategyFundBsuppPlanTypes;
import com.trinet.ambis.persistence.model.StrategyFundingBasePlanLimits;
import com.trinet.ambis.persistence.model.StrategyFundingDetail;
import com.trinet.ambis.persistence.model.StrategyFundingDetailId;
import com.trinet.ambis.persistence.model.StrategyFundingFlatMax;
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
import com.trinet.ambis.service.BenefitPlanService;
import com.trinet.ambis.service.CacheTemplateService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ContributionService;
import com.trinet.ambis.service.DisabilityOptionService;
import com.trinet.ambis.service.EmployeeBenefitGroupService;
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
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.StrategyFundingModelService;
import com.trinet.ambis.service.StrategyGroupHeadCountService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategyHistoryService;
import com.trinet.ambis.service.StrategyHsaFundingService;
import com.trinet.ambis.service.StrategyRenewalService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.StrategySyncService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.AdditionalBenefitEmployeeDetails;
import com.trinet.ambis.service.model.AdditionalBenefitOffer;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BenOfferExceptionDto;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.BenefitOfferSummary;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.model.PlanTypeDescription;
import com.trinet.ambis.service.model.StrategyBenefitGroup;
import com.trinet.ambis.service.model.StrategyBudget;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategyEstimate;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;
import com.trinet.ambis.service.model.StrategySummary;
import com.trinet.ambis.service.prospect.ProspectPlanHeadCountService;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.FeatureFlagUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.StrategyUtils;
import com.trinet.ambis.util.Utils;
import com.trinet.ambis.validator.RequestValidator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.trinet.ambis.util.Constants.LIFE_CMTR_PLANS_DESC;

/**
 * @author kpamulapati
 */
@Service
public class StrategyServiceImpl implements StrategyService {
	private static final Logger logger = LoggerFactory.getLogger(StrategyServiceImpl.class);
	private static final String CLASS_NAME = StrategyServiceImpl.class.toString();

	@Autowired
	private AdditionalBenefitPlanService additionalBenefitPlanService;
	@Autowired
	private BenefitClassService benefitClassService;
	@Autowired
	private BenefitGroupService benefitGroupService;
	@Autowired
	private StrategyGroupService strategyGroupService;
	@Autowired
	private StrategyGroupHeadCountService strategyGroupHeadCountService;
	@Autowired
	private CompanyService companyService;
	@Autowired
	private ContributionService contributionService;
	@Autowired
	private DisabilityOptionService disabilityOptionService;
	@Autowired
	private EmployeeBenefitGroupService employeeBenefitGroupService;
	@Autowired 
	private EmployerEmployeePlansMappingService employerEmployeePlansMappingService;
	@Autowired
	private HeadCountService headCountService;
	@Autowired
	private PlanSelectionService planSelectionService;
	@Autowired
	private ProcessStatusService processStatusService;
	@Autowired
	private RealmPlanYearService realmPlanYearService;
	@Autowired
	private RealmPlyrPlanService realmPlyrPlanService;
	@Autowired
	private StrategyHistoryService strategyHistoryService;
	@Autowired
	private StrategyRenewalService strategyRenewalService;
	@Autowired
	private SubmitStatusService submitStatusService;
	@Autowired
	private BenefitPlanDao benefitPlanDao;
	@Autowired
	private GetNextEligRulesId spGetNextEligRulesId;
	@Autowired
	private LifeAndDisabilityCalcData lifeAndDisabilityCalcData;
	@Autowired
	private NextBenProgram nextBenProgram;
	@Autowired
	private NextRateTblID nextRateTblID;
	@Autowired
	private PortfolioRuleDao portfolioRuleDao;
	@Autowired
	private PsCompanyDao psCompanyDao;
	@Autowired
	private RealmDataDao realmDataDao;	
	@Autowired
	private RealmPlanYearDao realmPlanYearDao;
	@Autowired
	private RenewalDataDao renewalDataDao;
	@Autowired
	private StrategyDao strategyDao;
	@Autowired
	private StrategyDataDao strategyDataDao;
	@Autowired
	private EmployeeStrategyGroupTransactionDao employeeStrategyGroupTransactionDao;
	@Autowired
	private StrategyFundingDataDao strategyFundingDataDao;
	@Autowired
	private StrategyFundingModelService strategyFundingModelService;
	@Autowired
	private StrategyHsaFundingDao strategyHsaFundingDao;
	@Autowired
	private StrategyFundingModelDao strategyFundingModelDao;
	@Autowired
	private StrategyHsaFundingService strategyHsaFundingService;
	@Autowired
	private XbssRealmPlyrPlanDao realmPlyrPlanDao;
	@Autowired
	private PlanSelectionDao planSelectionDao;
	@Autowired
	private PlanRatesService planRatesService;
	@Autowired
	private StrategySyncService strategySyncService;
	@Autowired
	private StrategyBasePlanLimitsDao strategyBasePlanLimitsDao;
	@Autowired
	private RealTimeSyncService realTimeSyncService;
	@Autowired
	ProspectPlanHeadCountService prospectPlanHeadCountService;
	@Autowired
	private ProspectStrategyService prospectStrategyService;
	@Autowired
	ProspectDefaultPlanAssignmentService prospectDefaultPlanAssignmentService;
	@Autowired
	EmployeePlanAssignmentService employeePlanAssignmentService;
	@Autowired
	private CacheTemplateService cacheTemplateService;
	@Autowired
	private Executor executor;
	@Autowired
	PortfolioService portfolioService;
	@Autowired
	TibRateServiceImpl tibRateService;
	@Autowired
	private BenefitPlanService benefitPlanService;
	@Autowired
	private HrpDao hrpDao;

	private static final Set<String> SUBMIT_PENDING_STATUS = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(BSSApplicationConstants.PROCESSING, BSSApplicationConstants.UNPROCESSED)));
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public long createUpdateStrategy(StrategyData dto, Company company, boolean updateFlag) {
		
		if (dto.getStrategySummary().isSubmitted() && !isStrategySubmittable(dto, company)) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_LIFE_DISABILITY_SYNC,
					BSSHttpStatusConstants.BAD_REQUEST, "", "Failed Life or Disability Sync", null, null));
		}

		long sourceStrategyId = dto.getStrategySummary().getId();

		// updating company details
		CompanyServiceHelper.saveStrategyUpdateCompany(dto.getStrategySummary().getHeadcount(), company);
		companyService.createUpdateCompany(company);

		// if submit is true updating the status of all other strategies
		if (dto.getStrategySummary().isSubmitted()) {
			strategyDataDao.updateStrategySubmitFlag(company.getId());
		}

		// Save or update strategy
		Strategy strategy = saveStrategy(dto, company, updateFlag);
		
		// Save or update StrategyHsaFunding
		saveStrategyHsaFunding(dto, strategy);

		long companyId = company.getId();
		long newStrategyId = strategy.getId();
		List<StrategyBenefitGroup> dtoGroups = dto.getBenefitGroups();

		List<BenefitGroup> benefitGroups = getBenefitGroupsForStrategy(dto, updateFlag, companyId, newStrategyId);
		
		Map<Long, Set<String>> existingBenTypeByGroupId = null;
		Map<Long, Boolean> existingMedStrategyPortfolioMap = null;
		if ((company.isProspectCompany() || company.isProspectConvertedOnboardingClient())
				&& updateFlag) {
			existingBenTypeByGroupId = findExistingDentalAndVisionBenTypesByGroup(dto);
			existingMedStrategyPortfolioMap = portfolioRuleDao.getMedicalPortfoliosBy(newStrategyId,
					company.getRealmPlanYearId(), company.getHeadQuatersState());
		}
		if (benefitGroups.isEmpty()) {
			createBenefitsGroupsAndPlans(company, updateFlag, strategy, companyId, newStrategyId, dtoGroups);
		} else {
			updateBenefitGroups(dto, company, updateFlag, strategy, newStrategyId, dtoGroups, benefitGroups);
		}
		if (company.isProspectCompany() || company.isProspectConvertedOnboardingClient()) {
			if(!updateFlag) {
				existingMedStrategyPortfolioMap = portfolioRuleDao.getMedicalPortfoliosBy(newStrategyId,
						company.getRealmPlanYearId(), company.getHeadQuatersState());
			}
			insertStrategyEEDefaultPlanAssignments(company, dto, updateFlag, newStrategyId, existingBenTypeByGroupId,
					existingMedStrategyPortfolioMap);
		}
		if (CompanyServiceHelper.isTibProspect(company)) {
			tibRateService.saveRatesPerStrategy(company, newStrategyId);
            planSelectionService.syncOmsMedicalPlanSelections(newStrategyId);
		}
		return newStrategyId;
	}

	private void createBenefitsGroupsAndPlans(Company company, boolean updateFlag, Strategy strategy, long companyId,
			long newStrategyId, List<StrategyBenefitGroup> dtoGroups) {
		List<Contribution> contributionsToSave = new ArrayList<>();
		for (StrategyBenefitGroup benefitGroup : dtoGroups) {
			BenefitGroup bg = new BenefitGroup();
			bg.setId(0);
			bg.setName(RequestValidator.getValidatedGroupName(benefitGroup.getName()));
			bg.setStatus(BSSApplicationConstants.STATUS_ACTIVE);
			bg.setCompanyId(companyId);
			bg.setType(benefitGroup.getType());
			bg.setState(benefitGroup.getState());
			if (benefitGroup.isDefaultGroup()) {
				bg.setBenefitProgram(company.getBenefitProgram());
				bg.setDefaultGroup(true);
			} else {
				bg.setBenefitProgram(nextBenProgram.execute());
				bg.setDefaultGroup(false);
			}
			bg.setEligRuleId(spGetNextEligRulesId.execute());
			// make sure the complete set of rate IDs is present
			this.completeRateTblSet(bg);
			// setting ELIG_CONFIG1 values
			bg.setEligConfig1(benefitClassService.generateClassCode(company, bg));
			BenefitGroup createdBenefitGroup = benefitGroupService.saveBenefitGroup(bg);
			BenefitGroupStrategy bsg = new BenefitGroupStrategy();
			bsg.setGroupId(createdBenefitGroup.getId());
			bsg.setBenefitGroup(createdBenefitGroup);
			bsg.setStrategyId(newStrategyId);
			bsg.setStrategy(strategy);
			bsg.setDefaultGroup(benefitGroup.isDefaultGroup());
			bsg.setWaitingPeriod(benefitGroup.getWaitingPeriod());
			bsg.setHeadcount(benefitGroup.getHeadcount());
			bsg.setStatus(BSSApplicationConstants.STATUS_ACTIVE);
			bsg = strategyGroupService.saveBenefitGroupStrategy(bsg);
			if (!(company.isRenewalCompany() || company.isProspectCompany() || company.isProspectConvertedClient())) {
				List<StrategyGroupHeadCount> strategyGrpHeadCnts = BenefitGroupServiceHelper
						.prepareStrategyGroupHeadCountObj(benefitGroup.getCoverageLevelHeadCounts(), bsg.getId());
				for (StrategyGroupHeadCount sghc : strategyGrpHeadCnts) {
					strategyGroupHeadCountService.saveStrategyGroupHeadCount(sghc);
				}
			}
			createdBenefitGroup.setBenefitOffers(benefitGroup.getBenefitOffers());
			// Save plans and Funding
			createUpdatePlans(company, newStrategyId, createdBenefitGroup, contributionsToSave, updateFlag);
		}
		contributionService.saveAll(contributionsToSave);
	}
	
	private void updateBenefitGroups(StrategyData dto, Company company, boolean updateFlag, Strategy strategy,
			long newStrategyId, List<StrategyBenefitGroup> dtoGroups, List<BenefitGroup> benefitGroups) {
		Map<String, Long> benefitProgramStrategyGroupId = new HashMap<>();
		StrategyBenefitGroup dtoGroup = null;
		List<Contribution> contributionsToSave = new ArrayList<>();
		for (BenefitGroup benefitGroup : benefitGroups) {
			BenefitGroupStrategy newStrategyGroup = new BenefitGroupStrategy();
			if (!updateFlag) {
				dtoGroup = handleBenefitGroupCreation(company, strategy, newStrategyId, dtoGroups,
						benefitProgramStrategyGroupId, benefitGroup, newStrategyGroup);
			} else {
				dtoGroup = StrategyServiceHelper.getBenefitGroupById(dtoGroups, benefitGroup.getId());
			}
			if (null != dtoGroup) {
				benefitGroup.setBenefitOffers(dtoGroup.getBenefitOffers());
				createUpdatePlans(company, newStrategyId, benefitGroup, contributionsToSave, updateFlag);
			}
		}
		contributionService.saveAll(contributionsToSave);
		if (!benefitProgramStrategyGroupId.isEmpty()) {
			employeeBenefitGroupService.loadStrategyEmployeeData(company, benefitProgramStrategyGroupId,
					dto.getStrategySummary().getId());
		}
	}

	private StrategyBenefitGroup handleBenefitGroupCreation(Company company, Strategy strategy, long newStrategyId,
			List<StrategyBenefitGroup> dtoGroups, Map<String, Long> benefitProgramStrategyGroupId,
			BenefitGroup benefitGroup, BenefitGroupStrategy newStrategyGroup) {
		for (BenefitGroupStrategy benefitGroupStrategy : benefitGroup.getBenefitGroupStrategy()) {
			newStrategyGroup.setGroupId(benefitGroup.getId());
			newStrategyGroup.setBenefitGroup(benefitGroup);
			newStrategyGroup.setStrategyId(newStrategyId);
			newStrategyGroup.setStrategy(strategy);
			newStrategyGroup.setDefaultGroup(benefitGroupStrategy.isDefaultGroup());
			newStrategyGroup.setWaitingPeriod(benefitGroupStrategy.getWaitingPeriod());
			newStrategyGroup.setHeadcount(benefitGroupStrategy.getHeadcount());
			newStrategyGroup.setStatus(benefitGroupStrategy.getStatus());
			newStrategyGroup = strategyGroupService.saveBenefitGroupStrategy(newStrategyGroup);
			benefitProgramStrategyGroupId.put(benefitGroup.getBenefitProgram(), newStrategyGroup.getId());
		}
		StrategyBenefitGroup dtoGroup = StrategyServiceHelper.getBenefitGroupByCompare(dtoGroups, benefitGroup);

		boolean isEligibleCompany = company.isProspectConvertedClient() || company.isRenewalCompany()
				|| company.isProspectCompany();

		if (null != dtoGroup && !isEligibleCompany) {
			List<StrategyGroupHeadCount> strategyGroupHcs = BenefitGroupServiceHelper
					.prepareStrategyGroupHeadCountObj(dtoGroup.getCoverageLevelHeadCounts(), newStrategyGroup.getId());

			if (strategyGroupHcs != null && !strategyGroupHcs.isEmpty()) {
				strategyGroupHcs.forEach(strategyGroupHeadCountService::saveStrategyGroupHeadCount);
			}
		}
		return dtoGroup;
	}

	private List<BenefitGroup> getBenefitGroupsForStrategy(StrategyData dto, boolean updateFlag, long companyId,
			long newStrategyId) {
		List<BenefitGroup> benefitGroups = null;
		if (BSSApplicationConstants.NEW_COMPANY_DEFAULT_STRATEGIES.contains(dto.getStrategySummary().getId())) {
			benefitGroups = benefitGroupService.getAllBenefitGroups(companyId, BSSApplicationConstants.STATUS_ACTIVE);
		} else {
			if (updateFlag) {
				benefitGroups = benefitGroupService.getBenefitGroupByStrategy(newStrategyId,
						BSSApplicationConstants.STATUS_ACTIVE);
			} else {
				benefitGroups = benefitGroupService.getBenefitGroupByStrategy(dto.getStrategySummary().getId(),
						BSSApplicationConstants.ACTIVE_PENDING_STATUS);
			}
		}
		return benefitGroups;
	}

	private void createUpdatePlans(Company company, long strategyId, BenefitGroup benefitGroup, List<Contribution> contributionsToSave, boolean updateFlag) {
		long benefitGroupId = benefitGroup.getId();
		logger.info("CREATING BENEFIT PLANS");
		if (updateFlag) {
			deleteStrategyData(strategyId, benefitGroup.getId());
		}
		List<BenefitOffer> benefitOffers = benefitGroup.getBenefitOffers();
		for (BenefitOffer benefitOffer : benefitOffers) {
			List<BenefitPlan> benefitPlans = benefitOffer.getBenefitPlans();
			BenefitOfferSummary benefitOfferSummary = benefitOffer.getSummary();
			String planType = PlanTypesEnum.getCode(benefitOfferSummary.getType());
			// DISABILITY
			if (planType.equals(PlanTypesEnum.ADDITIONAL.getCode())) {
				createUpdateADPlans(strategyId, benefitOffer, benefitGroupId);
			}
			// MEDICAL, DENTAL & VISION
			else {
				createMDVPlans(company, strategyId, benefitGroupId, benefitOffer, benefitPlans, contributionsToSave, planType);
			}
		}
	}

	/**
	 * This method is for creating AD plan selections in BSS.
	 *
	 * @param strategyId
	 * @param benefitOffer
	 * @param benefitGroupId
	 */
	private void createUpdateADPlans(long strategyId, BenefitOffer benefitOffer, Long benefitGroupId) {
		List<AdditionalBenefitOffer> addBenefitOffers = benefitOffer.getAdditionalBenefitOffers();
		List<PlanSelection> addlPlanSelections = new ArrayList<>();
		for (AdditionalBenefitOffer addBenefitOffer : addBenefitOffers) {
			long headCount = addBenefitOffer.getSummary().getHeadcount();
			if (LIFE_CMTR_PLANS_DESC.contains(addBenefitOffer.getSummary().getType())) {
				processLifeCmtrPlans(strategyId, benefitGroupId, addlPlanSelections, addBenefitOffer, headCount);
			} else {
				processNonLifeNonCmtrPlans(strategyId, benefitGroupId, addlPlanSelections, addBenefitOffer);
			}
		}
		if (!addlPlanSelections.isEmpty()) {
			planSelectionService.saveAll(addlPlanSelections);
		}
	}

	private void processLifeCmtrPlans(long strategyId, Long benefitGroupId, List<PlanSelection> addlPlanSelections,
			AdditionalBenefitOffer addBenefitOffer, long headCount) {
		List<AdditionalBenefitPlan> additionalBenefitPlans = addBenefitOffer.getAdditionalBenefitPlans();
		for (AdditionalBenefitPlan adPlan : additionalBenefitPlans) {
			// need to check the head count logic here
			addlPlanSelections.add(StrategyServiceHelper.constructADPlanSelection(strategyId, benefitGroupId,
					adPlan.getId(), adPlan.getPlanType(), headCount));
		}
	}
	
	private void processNonLifeNonCmtrPlans(long strategyId, Long benefitGroupId,
			List<PlanSelection> addlPlanSelections, AdditionalBenefitOffer addBenefitOffer) {
		long headCount;
		List<AdditionalBenefitPlan> additionalBenefitPlans = addBenefitOffer.getAdditionalBenefitPlans();
		for (AdditionalBenefitPlan adPlan : additionalBenefitPlans) {
			if (adPlan.getOptionPlans() != null) {
				for (DisabilityBenefitOptionPlans dboPlan : adPlan.getOptionPlans()) {
					headCount = dboPlan.getPlanHeadCount() != null ? dboPlan.getPlanHeadCount() : 0L;
					addlPlanSelections.add(StrategyServiceHelper.constructADPlanSelection(strategyId,
							benefitGroupId, dboPlan.getId(), dboPlan.getPlanType(), headCount));
				}
			}
		}
	}
	
	private void createMDVPlans(Company company, long strategyId, long benefitGroupId, BenefitOffer benefitOffer,
			List<BenefitPlan> benefitPlans, List<Contribution> contributionsToSave, String planType) {
		List<Contribution> contributionsList = new ArrayList<>();
		List<PlanSelection> planList = new ArrayList<>();
		Map<String, BenefitPlan> updatedBenefitPlansMap = new HashMap<>();
		for (BenefitPlan benefitPlan : benefitPlans) {
			long headCount = 0;
			boolean employeePaid = benefitPlan.isEmployeePaid();
			if (employeePaid) {
				if (planType.equals(PlanTypesEnum.DENTAL.getCode())) {
					planType = PlanTypesEnum.DENTAL_VOLUNTARY.getCode();
				} else if (planType.equals(PlanTypesEnum.VISION.getCode())) {
					planType = PlanTypesEnum.VISION_VOLUNTARY.getCode();
				}
			}
			benefitPlan.setPlanType(planType);
			updatedBenefitPlansMap.put(benefitPlan.getId(), benefitPlan);
			planList.add(StrategyServiceHelper.constructPlanSelection(strategyId, benefitGroupId, benefitPlan,
					headCount));
		}
		List<PlanSelection> planSelections = null;
		if (!planList.isEmpty()) {
			planSelections = planSelectionService.saveAll(planList);
		}
		for (Map.Entry<String, BenefitPlan> entry : updatedBenefitPlansMap.entrySet()) {
			String benefitPlanId = entry.getKey();
			BenefitPlan benefitPlan = updatedBenefitPlansMap.get(benefitPlanId);
			PlanSelection planSelection = StrategyServiceHelper.getPlanSelection(planSelections,
					benefitPlan.getId());
			StrategyServiceHelper.createUpdateContribution(company, benefitPlan, planSelection,
					contributionsList);
		}
		contributionsToSave.addAll(contributionsList);
//		contributionService.saveAll(contributionsList);
		createUpdateFunding(strategyId, benefitGroupId, benefitOffer, planType);
	}

	/**
	 * This method is for deleting the existing strategy data on Update. all
	 * plans and contributions and add new selected and plans contributions in
	 * case of updating existing strategy
	 * @param strategyId
	 * @param benefitGroupId
	 */
	private void deleteStrategyData(long strategyId, long benefitGroupId) {
		strategyDataDao.deleteAllPlanContributionsByBenefitgroupAndStrategy(benefitGroupId, strategyId);
		strategyDataDao.deleteAllPlanSelectionsByBenefitgroupAndStrategy(benefitGroupId, strategyId);
		strategyDataDao.deleteStrategyFundingsByBenefitgroupAndStrategy(benefitGroupId, strategyId);
	}
	
	/**
	 * This method is for saving or updating the StrategyFundingModel.
	 * @param strategyId
	 * @param benefitGroupId
	 * @param benefitOffer
	 * @param planType
	 */
	private void createUpdateFunding(long strategyId, long benefitGroupId, BenefitOffer benefitOffer, String planType) {
		long start = System.currentTimeMillis();
		PlanPackage planPackage = benefitOffer.getPlanPackage();
		if (planPackage != null && null != planPackage.getFundingType()
				&& StringUtils.isNotEmpty(planPackage.getFundingType())
				&& BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER.contains(planType)) {
			StrategyFundingModel sfm = new StrategyFundingModel();
			constructFunding(strategyId, benefitGroupId, planType, planPackage, sfm);
			strategyFundingModelService.createUpdateFunding(sfm);
			long end = System.currentTimeMillis();
			logger.error("strategyFundingModelService.createUpdateFunding() Template took {} ms", (end - start));
		}
	}

	
	/**
	 * This method is for creating StrategyFundingModel object for save or update.
	 * @param strategyId
	 * @param benefitGroupId
	 * @param planType
	 * @param planPackage
	 * @param strategyFundingModel
	 */
	private void constructFunding(long strategyId, long benefitGroupId, String planType, PlanPackage planPackage,
			StrategyFundingModel strategyFundingModel) {
		if (planPackage != null) {
			strategyFundingModel.setFundingType(planPackage.getFundingType());
			strategyFundingModel.setPlanType(planType);
			strategyFundingModel.setGroupId(benefitGroupId);
			strategyFundingModel.setStrategyId(strategyId);
			String fundingPlan = planPackage.getFundingBasePlan();
			if (fundingPlan != null && !BSSApplicationConstants.FLAT.equals(planPackage.getFundingType())) {
				strategyFundingModel.setBaseBenefitPlan(fundingPlan);
			}
			strategyFundingModel.setName(planPackage.getName());
			strategyFundingModel.setCustomized(planPackage.isCustomized());
			strategyFundingModel.setWaiverAllowance(planPackage.getWaiverAllowance());
			strategyFundingModel.setBsuppExcessOption(planPackage.getBsuppExcessOption());
			Set<StrategyFundingDetail> fundingDetails = new java.util.HashSet<>();
			Set<StrategyFundingFlatMax> fundingFlatMax = new java.util.HashSet<>();
			Set<StrategyFundBsuppPlanTypes> bsuppVolPlanTypes = new java.util.HashSet<>();
			Set<StrategyFundingBasePlanLimits> fundingBasePlanLimits = new java.util.HashSet<>();

			// funding detail
			for (String key : planPackage.getCoverageLevelFunding().keySet()) {
				StrategyFundingDetail sfd = new StrategyFundingDetail();
				StrategyFundingDetailId sfdId = new StrategyFundingDetailId();
				sfdId.setCoverageId(key);
				sfd.setSfDetailId(sfdId);
				sfd.setContribution(planPackage.getCoverageLevelFunding().get(key));
				sfd.setStrategyFundingModel(strategyFundingModel);
				fundingDetails.add(sfd);
			}

			// BSUPP changes
			processBsuppVolPlanTypes(planPackage, strategyFundingModel, bsuppVolPlanTypes);

			// funding flat max
			processFundingFlatMax(planPackage, strategyFundingModel, fundingPlan, fundingFlatMax);

			// funding Base Plan Limits
			processFundingBasePlanLimits(planPackage, strategyFundingModel, fundingBasePlanLimits);
			if (!fundingDetails.isEmpty()) {
				strategyFundingModel.setFundingDetails(fundingDetails);
			}
		}
	}

	private void processBsuppVolPlanTypes(PlanPackage planPackage, StrategyFundingModel strategyFundingModel,
			Set<StrategyFundBsuppPlanTypes> bsuppVolPlanTypes) {
		if (CollectionUtils.isNotEmpty(planPackage.getBsuppSelectedVolPlanTypes())) {
			for (String bsupPlanType : planPackage.getBsuppSelectedVolPlanTypes()) {
				StrategyFundBsuppPlanTypes sfbpt = new StrategyFundBsuppPlanTypes();
				StrategyFundBsuppPlanTypeId id = new StrategyFundBsuppPlanTypeId();
				id.setPlanType(bsupPlanType);
				sfbpt.setStrategyFundBsuppPlanTypeId(id);
				sfbpt.setStrategyFundingModel(strategyFundingModel);
				bsuppVolPlanTypes.add(sfbpt);
			}
			if (CollectionUtils.isNotEmpty(bsuppVolPlanTypes)) {
				strategyFundingModel.setFundingBsuppPlanTypes(bsuppVolPlanTypes);
			}
		}
	}
	
	private void processFundingFlatMax(PlanPackage planPackage, StrategyFundingModel strategyFundingModel,
			String fundingPlan, Set<StrategyFundingFlatMax> fundingFlatMax) {
		if (BSSApplicationConstants.FLAT_MAX.equals(fundingPlan)) {
			for (String key : planPackage.getCoverageLevelFundingFlatMax().keySet()) {
				StrategyFundingFlatMax sffm = new StrategyFundingFlatMax();
				StrategyFundingDetailId sfdId = new StrategyFundingDetailId();
				sfdId.setCoverageId(key);
				sffm.setSfDetailId(sfdId);
				sffm.setContribution(planPackage.getCoverageLevelFundingFlatMax().get(key));
				sffm.setStrategyFundingModel(strategyFundingModel);
				fundingFlatMax.add(sffm);
			}
			if (!fundingFlatMax.isEmpty()) {
				strategyFundingModel.setFundingFlatMax(fundingFlatMax);
			}
		}
	}
	
	private void processFundingBasePlanLimits(PlanPackage planPackage, StrategyFundingModel strategyFundingModel,
			Set<StrategyFundingBasePlanLimits> fundingBasePlanLimits) {
		if (MapUtils.isNotEmpty(planPackage.getCoverageLevelBasePlanLimits())) {
			for (String key : planPackage.getCoverageLevelBasePlanLimits().keySet()) {
				StrategyFundingBasePlanLimits sfd = new StrategyFundingBasePlanLimits();
				StrategyFundingDetailId sfdId = new StrategyFundingDetailId();
				sfdId.setCoverageId(key);
				sfd.setSfDetailId(sfdId);
				sfd.setContribution(planPackage.getCoverageLevelBasePlanLimits().get(key));
				sfd.setStrategyFundingModel(strategyFundingModel);
				fundingBasePlanLimits.add(sfd);
			}
			if (!fundingBasePlanLimits.isEmpty()) {
				strategyFundingModel.setFundingBasePlanLimits(fundingBasePlanLimits);
			}
		}
	}

	private Strategy saveStrategy(StrategyData dto, Company company, boolean updateFlag) {
		StrategySummary summary = dto.getStrategySummary();
		List<Strategy> strategies = strategyDao.findByCompanyId(company.getId());
		Strategy strategy = null;
		if (updateFlag) {
			strategy = strategies.stream().filter(obj -> summary.getId().equals(obj.getId())).findFirst()
					.orElseThrow(() -> {
						throw new BSSApplicationException(new BSSApplicationError(
								BSSErrorResponseCodes.BSS_STRATEGY_SAVE_FAILED, BSSHttpStatusConstants.BAD_REQUEST, "",
								"No strategy found for update", null, null));
					});
		} else {
			strategy = new Strategy();
		}
		List<String> strategyNames = strategies.stream().map(Strategy::getName).collect(Collectors.toList());
		if (!updateFlag && strategyNames.contains(summary.getName())) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_STRATEGY_SAVE_FAILED,
					BSSHttpStatusConstants.BAD_REQUEST, "", "Duplicate strategy name", null, null));
		}
		constructStrategy(company, summary, strategy, updateFlag);
		strategy = saveStrategy(strategy);
		return strategy;
	}

	private void constructStrategy(Company company, StrategySummary summary, Strategy strategy, boolean updateFlg) {
		strategy.setComments(summary.getComments());
		strategy.setType(summary.getType());
		strategy.setSubmitted(summary.isSubmitted());
		if (summary.isSubmitted()) {
			strategy.setSubmitDate(new Date());
		}
		strategy.setEstimatedTotalCost(summary.getEstimatedTotalCost());
		strategy.setCurrentYearTotalCost(summary.getCurrentYearTotalCost());
		strategy.setCompanyId(company.getId());
		strategy.setTotalBudget(summary.getTotalBudget());
		strategy.setBudgetFactor(summary.getBudgetFactor());
		strategy.setHeadCount(Long.valueOf(summary.getHeadcount()));
		strategy.setCostShareType(summary.getCostShareType());
		strategy.setStatus(BSSApplicationConstants.STATUS_ACTIVE);
		int acaFplOpted = summary.isAcaFplOpted() ? BSSApplicationConstants.ACA_FPL_OPTED_IN
				: BSSApplicationConstants.ACA_FPL_OPTED_OUT;
		strategy.setAcaFplOpted(acaFplOpted);
		
		updatePkgType(summary, strategy);
		
		if (updateFlg) {
			strategy.setUpdatedBy(BSSSecurityUtils.getAuthenticatedPersonId());
		} else {
			strategy.setName(summary.getName());
			strategy.setCreatedBy(BSSSecurityUtils.getAuthenticatedPersonId());
		}
		List<StrategyRegion> newStrategyRegions = new ArrayList<>();
		List<String> existingStrategyRegions = Collections.emptyList();
		if (CollectionUtils.isNotEmpty(strategy.getStrategyRegions())) {
			existingStrategyRegions = strategy.getStrategyRegions().stream().map(e -> e.getRegion())
					.collect(Collectors.toList());
		}
		if (CollectionUtils.isNotEmpty(summary.getFilterRegions())) {
			for (String region : summary.getFilterRegions()) {
				if (!existingStrategyRegions.contains(region)) {
					StrategyRegion strategyRegion = new StrategyRegion(region, strategy);
					newStrategyRegions.add(strategyRegion);
				}
			}

		}
		strategy.setStrategyRegions(newStrategyRegions);
	}

	private void updatePkgType(StrategySummary summary, Strategy strategy) {
		if (summary.getPkgType() != null && summary.getPkgType().equals(Constants.BALANCED_PACKAGE_NAME)) {
			strategy.setPkgType(Constants.BALANCED_ID);
		} else if (summary.getPkgType() != null && summary.getPkgType().equals(Constants.CONSERVATIVE_PACKAGE_NAME)) {
			strategy.setPkgType(Constants.CONSERVATIVE_ID);
		} else if (summary.getPkgType() != null && summary.getPkgType().equals(Constants.TOP_QUALITY_NAME)) {
			strategy.setPkgType(Constants.TOP_QUALITY_ID);
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public Strategy saveStrategy(Strategy strategy) {
		return strategyDao.saveAndFlush(strategy);
	}
	
	private StrategyHsaFundingDto saveStrategyHsaFunding(StrategyData dto, Strategy strategy) {
		StrategyHsaFundingDto existingStrategyHsaFundingDto = dto.getStrategyHsaFunding();
		StrategyHsaFundingDto newStrategyHsaFundingDto = null;
		if (existingStrategyHsaFundingDto != null) {
			existingStrategyHsaFundingDto.setStrategyId(strategy.getId().longValue());
			newStrategyHsaFundingDto = strategyHsaFundingService.save(existingStrategyHsaFundingDto);
		}
		return newStrategyHsaFundingDto;
	}

	private void insertStrategyEEDefaultPlanAssignments(Company company, StrategyData dto, boolean updateFlag,
			long newStrategyId, Map<Long, Set<String>> existingBenTypeByGroupId,
			Map<Long, Boolean> existingMedStrategyPortfolioMap) {
		if (updateFlag) {
		    if (!CompanyServiceHelper.isTibProspect(company)) {
				updateStrategyDefaultEEPlanAssignmentForMed(company, dto, existingMedStrategyPortfolioMap);
				updateStrategyDefaultEEPlanAssignmentForDenVis(dto, existingBenTypeByGroupId, 0l, updateFlag);
		    }
		} else {
			if (existingBenTypeByGroupId == null)
				existingBenTypeByGroupId = findExistingDentalAndVisionBenTypesByGroup(dto);
			long sourceStrategyId = dto.getStrategySummary().getId();
			Map<Long, Boolean> sourceStrategyMedPortfolioMap = portfolioRuleDao.getMedicalPortfoliosBy(sourceStrategyId,
					company.getRealmPlanYearId(), company.getHeadQuatersState());
			// For Medical,
			if (sourceStrategyMedPortfolioMap.keySet().containsAll(existingMedStrategyPortfolioMap.keySet()) || CompanyServiceHelper.isTibProspect(company)) {
			    // if the carriers are the same OR the company is TIB, Copy existing Medical Plans
			    employeePlanAssignmentService.copyEePlanAssignmentsFor(sourceStrategyId, newStrategyId,
				    BSSApplicationConstants.MEDICAL_PLAN_TYPE);
			} else {
			    // else: Carrier is Changed
			    prospectDefaultPlanAssignmentService.assignDefaultPlanBy(Set.of(newStrategyId), Set.of(),
				    existingMedStrategyPortfolioMap, Set.of(BSSApplicationConstants.MEDICAL_PLAN_TYPE));

			}
			// For dental & Vision
			if(CompanyServiceHelper.isTibProspect(company)) {
				employeePlanAssignmentService.copyEePlanAssignmentsFor(sourceStrategyId, newStrategyId,
						BSSApplicationConstants.DENTAL_PLAN_TYPE);
				employeePlanAssignmentService.copyEePlanAssignmentsFor(sourceStrategyId, newStrategyId,
						BSSApplicationConstants.VISION_PLAN_TYPE);
			}
			else {
				updateStrategyDefaultEEPlanAssignmentForDenVis(dto, existingBenTypeByGroupId, newStrategyId, updateFlag);
			}
		}
	}

	private void updateStrategyDefaultEEPlanAssignmentForMed(Company company, StrategyData dto,
			Map<Long, Boolean> existingMedStrategyPortfolioMap) {
		long strategyId = dto.getStrategySummary().getId();
		Map<Long, Boolean> newMedicalPortfolioIds = portfolioRuleDao.getMedicalPortfoliosBy(strategyId,
				company.getRealmPlanYearId(), company.getHeadQuatersState());
		if (CollectionUtils.isNotEmpty(newMedicalPortfolioIds.keySet())) {
			Set<Long> groupIds = dto.getBenefitGroups().stream().map(StrategyBenefitGroup::getId)
					.collect(Collectors.toSet());
			if (!existingMedStrategyPortfolioMap.keySet().containsAll(newMedicalPortfolioIds.keySet())) {
				prospectDefaultPlanAssignmentService.assignDefaultPlanBy(Set.of(strategyId), groupIds,
						newMedicalPortfolioIds, Set.of(BSSApplicationConstants.MEDICAL_PLAN_TYPE));
			}
		}
	}

	private void updateStrategyDefaultEEPlanAssignmentForDenVis(StrategyData dto,
			Map<Long, Set<String>> existingBenTypeByGroupId, long newStrategyId, boolean updateFlag) {
		Long strategyId = dto.getStrategySummary().getId();
		Long existingStrategyId = strategyId;
		Map<Long, Set<String>> newBenTypeByGroupId = findNewDentalAndVisionBenTypesByGroup(dto);
		Set<Long> groupIdsSyncAssignmentForDental = new HashSet<>();
		Set<Long> groupIdsSyncAssignmentForVision = new HashSet<>();

		newBenTypeByGroupId.keySet().forEach(groupId -> {
			Set<String> newBenTypes = new HashSet<>(newBenTypeByGroupId.getOrDefault(groupId, Collections.emptySet()));
			newBenTypes.removeAll(existingBenTypeByGroupId.getOrDefault(groupId, Collections.emptySet()));
			if (CollectionUtils.isNotEmpty(newBenTypes)
					&& (newBenTypes.contains(BSSApplicationConstants.DENTAL_PLAN_TYPE)
							|| newBenTypes.contains(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE))) {
				groupIdsSyncAssignmentForDental.add(groupId);
			}
			if (CollectionUtils.isNotEmpty(newBenTypes)
					&& (newBenTypes.contains(BSSApplicationConstants.VISION_PLAN_TYPE)
							|| newBenTypes.contains(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE))) {
				groupIdsSyncAssignmentForVision.add(groupId);
			}
		});
		if (!updateFlag) {
			strategyId = newStrategyId;
		}
		if (CollectionUtils.isNotEmpty(groupIdsSyncAssignmentForDental)) {
			prospectDefaultPlanAssignmentService.assignDefaultPlanBy(Set.of(strategyId),
					groupIdsSyncAssignmentForDental, null,
					BSSApplicationConstants.DENTAL_PLAN_TYPES.stream().collect(Collectors.toSet()));
		} else if (!updateFlag) {
			employeePlanAssignmentService.copyEePlanAssignmentsFor(existingStrategyId, newStrategyId,
					BSSApplicationConstants.DENTAL_PLAN_TYPE);
		}
		if (CollectionUtils.isNotEmpty(groupIdsSyncAssignmentForVision)) {
			prospectDefaultPlanAssignmentService.assignDefaultPlanBy(Set.of(strategyId),
					groupIdsSyncAssignmentForVision, null,
					BSSApplicationConstants.VISION_PLAN_TYPES.stream().collect(Collectors.toSet()));
		} else if (!updateFlag) {
			employeePlanAssignmentService.copyEePlanAssignmentsFor(existingStrategyId, newStrategyId,
					BSSApplicationConstants.VISION_PLAN_TYPE);
		}
	}

	private Map<Long, Set<String>> findNewDentalAndVisionBenTypesByGroup(StrategyData dto) {
		Map<Long, Set<String>> newBenTypeByGroupId = new HashMap<>();
		dto.getBenefitGroups().forEach(benGroup -> benGroup.getBenefitOffers().forEach(benOffer -> {
			if (BSSApplicationConstants.DENTAL.equalsIgnoreCase(benOffer.getSummary().getType())
					|| BSSApplicationConstants.VISION.equalsIgnoreCase(benOffer.getSummary().getType())) {
				benOffer.getBenefitPlans().forEach(benPlan -> {
					newBenTypeByGroupId.putIfAbsent(benGroup.getId(), new HashSet<>());
					newBenTypeByGroupId.get(benGroup.getId()).add(benPlan.getPlanType());
				});
			}
		}));
		return newBenTypeByGroupId;
	}
	
	private Map<Long, Set<String>> findExistingDentalAndVisionBenTypesByGroup(StrategyData dto) {
		Map<Long, Set<String>> benTypeByGroupId = new HashMap<>();
		Map<Long, List<PlanSelectionDetail>> planSelectionByGroupId = getCurrentPlanSelectionDetailsByGroup(dto);
		planSelectionByGroupId.keySet().stream().forEach(groupId -> {
			Set<String> existingBenTypes = new HashSet<>();
			List<PlanSelectionDetail> planSelectionDetails = planSelectionByGroupId.get(groupId);
			planSelectionDetails.forEach(planSelectionDetail -> {
				String benType = planSelectionDetail.getPlanType();
				if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(benType)
						|| BSSApplicationConstants.VISION_PLAN_TYPES.contains(benType)) {
					existingBenTypes.add(benType);
				}
			});
			benTypeByGroupId.put(groupId, existingBenTypes);
		});
		return benTypeByGroupId;
	}

	private Map<Long, List<PlanSelectionDetail>> getCurrentPlanSelectionDetailsByGroup(StrategyData dto) {
		Set<Long> groupIds = dto.getBenefitGroups().stream().map(StrategyBenefitGroup::getId)
				.collect(Collectors.toSet());
		List<PlanSelectionDetail> planSelectionDetails = planSelectionService
				.findDistinctPlanTypeBy(Set.of(dto.getStrategySummary().getId()), groupIds);
		return planSelectionDetails.stream().collect(Collectors.groupingBy(PlanSelectionDetail::getGroupId,
				Collectors.collectingAndThen(Collectors.toList(), Function.identity())));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { Exception.class })
	public List<StrategyData> getStrategies(Company company, boolean history, String exchange) {
		return constructStrategiesData(company, history);
	}
	
	private List<StrategyData> constructStrategiesData(Company company, boolean history) {
		String companyCode = company.getCode();
		long companyId = company.getId();
		RealmPlanYear realmPlanYear = company.getRealmPlanYear();
		ProcessStatus processStatus = null;
		List<Strategy> strategies = null;
		if (history) {
			strategies = strategyDataDao.getHistoryStrategies(companyCode, realmPlanYear.getId());
		} else {
			strategies = getActiveStrategies(company, companyCode, companyId, processStatus);
		}
		logger.info("NUMBER OF STRATEGIES : {}", strategies.size());
		List<StrategyData> strategiesData = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(strategies)) {
			strategiesData = getStategiesData(company, history, realmPlanYear, strategies,false);
		}
		if (company.isProspectCompany()) {
			strategiesData.add(prospectStrategyService.getProspectCurrentStrategy(company.getCode()));
		}
		return strategiesData;
	}

	private List<Strategy> getActiveStrategies(Company company, String companyCode, long companyId,
			ProcessStatus processStatus) {
		List<Strategy> strategies = new ArrayList<>();
		if (company.isProspectCompany()) {
			processStatus = processStatusService.findLastRecordByCompanyAndEvent(companyId,
					ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName());
			if (processStatus != null && BSSApplicationConstants.PROCESS_STATUS_FAILED.equals(processStatus.getProcessStatus())) {
				company.setBandCodeUpdated(true);
			}
		}
		if (FeatureFlagUtils.isBssYearAround(companyCode, company.getRealmPlanYear().getId())
				|| !company.isRenewalCompany()
				|| (company.isRenewalCompany() && (company.isRenewalOpen() || company.isTransitionPeriod()))) {
			strategies = strategyDao.findByCompanyIdAndStatus(companyId, BSSApplicationConstants.STATUS_ACTIVE);
		} else {
			Strategy cStrategy = strategyDao.findByCompanyIdAndSubmitted(companyId, true).get(0);
			strategies.add(cStrategy);
		}
		// doing strategy/census sync to update the head counts, plans and rates
		syncStrategyData(company, null);

		if (company.isProspectCompany() && processStatus != null && BSSApplicationConstants.PROCESS_STATUS_FAILED.equals(processStatus.getProcessStatus())) {
			processStatusService.updateProcessStatus(Collections.singleton(processStatus.getId()),
					processStatus);
		}
		return strategies;
	}

	private void syncStrategyData(Company company, Long strategyId) {
		if (AppRulesAndConfigsUtils.isOnDemandSyncEnabled()) {
			realTimeSyncService.onDemandSync(company);
		} else {
			strategySyncService.syncStrategyData(company, strategyId);
		}
	}
	
	@Override
	public List<StrategyData> updateStrategieHistory(Company company, boolean history) {
		Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap = renewalDataDao
				.getHealthPlansForRenewalCompany(company.getRealmPlanYear().getPlanYearEnd(), company.getPfClient(),
						company.getCode());
		Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap = renewalDataDao
				.getAdditionalBenefitPlansForRenewalCompany(company.getRealmPlanYear().getPlanYearEnd(),
						company.getPfClient(), company.getCode());
		strategyHistoryService.createHistoryStrategyFromPS(company, bgsHealthPlansMap, bgsADPlansMap);
		return constructStrategiesData(company, history);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { Exception.class })
	public void createProspectsTrinetStrategy(Company company, long selectedCarrier,
			List<PlanMappingResponse> planMappingResponse) {
		List<Strategy> strategies = null;
		ProcessStatus ps = null;
		boolean exceptionOccurred = false;
		strategies = new ArrayList<>();
		strategies = strategyDao.findByCompanyIdAndStatus(company.getId(), BSSApplicationConstants.STATUS_ACTIVE);
		try {
			if (strategies.isEmpty()) {
				ps = processStatusService.createStrategyProcess(company);
				prospectStrategyService.createDefaultTrinetStrategy(company, selectedCarrier, planMappingResponse);
			}
		} catch (Exception e) {
			exceptionOccurred = true;
			throw new BSSApplicationException(e,
					new BSSApplicationError(BSSErrorResponseCodes.BSS_STRATEGY_CREATE_FAIL,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, StrategyServiceImpl.class.getName(),
							"PROSPECT STRATEGY CREATION FAILED", null, null));
		} finally {
			if (null != ps) {
				if (exceptionOccurred) {
					ps.setProcessStatus(BSSApplicationConstants.PROCESS_STATUS_FAILED);
				} else {
					ps.setProcessStatus(BSSApplicationConstants.PROCESS_STATUS_PROCESSED);
				}
				processStatusService.updateProcessStatus(ps);
			}
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { Exception.class })
	public void createFutureStrategies(Company company, boolean isDefaultSubmit, boolean isPreload) {
		if (company.isRenewalOpen() || company.isTransitionPeriod() || isDefaultSubmit) {
			List<Strategy> strategies = null;
			ProcessStatus ps = null;
			boolean exceptionOccurred = false;
			strategies = new ArrayList<>();
			RealmPlanYear prevRealmPlanYear = realmPlanYearService.getPreviousRealmPlanYear(company.getRealmPlanYear());
			strategies = strategyDao.findByCompanyIdAndStatus(company.getId(), BSSApplicationConstants.STATUS_ACTIVE);
			boolean isMigratedCompany = false;
			try {
				if (strategies.isEmpty()) {
					ps = processStatusService.createStrategyProcess(company);
					Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap = renewalDataDao
							.getHealthPlansForRenewalCompany(prevRealmPlanYear.getPlanYearEnd(), company.getPfClient(),
									company.getCode());
					Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap = renewalDataDao
							.getAdditionalBenefitPlansForRenewalCompany(prevRealmPlanYear.getPlanYearEnd(),
									company.getPfClient(), company.getCode());
					Company historyCompany = companyService.getCompanyDetails(company.getCode(), true,
							company.getEmplId(), null);
					strategyHistoryService.createHistoryStrategyFromPS(historyCompany, bgsHealthPlansMap,
							bgsADPlansMap);
					Strategy currentStrategy = strategyDataDao.getCurrentStrategy(company.getCode(),
							company.getRealmPlanYear().getId());
					isMigratedCompany = (null == currentStrategy);
					
					logger.info("Creating Current Strategy from PS data");
					logger.info("Creating Future Strategies for companies not in BSS");
					strategyRenewalService.createFutureStrategies(company, isMigratedCompany,
							company.getRealmPlanYear(), isDefaultSubmit, bgsHealthPlansMap, bgsADPlansMap, isPreload);
				} else if (isDefaultSubmit) {
					ps = processStatusService.createStrategyProcess(company);
					resetCompanyStrategies(company, strategies.stream().map(e -> e.getId()).collect(Collectors.toSet()));
					Strategy currentStrategy = strategyDataDao.getCurrentStrategy(company.getCode(),
							company.getRealmPlanYear().getId());
					
					isMigratedCompany = (null == currentStrategy);
					
					Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap = renewalDataDao
							.getHealthPlansForRenewalCompany(company.getRealmPlanYear().getPlanYearEnd(),
									company.getPfClient(), company.getCode());
					Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap = renewalDataDao
							.getAdditionalBenefitPlansForRenewalCompany(prevRealmPlanYear.getPlanYearEnd(),
									company.getPfClient(), company.getCode());
					
					logger.info("DefaultSubmit: Creating Future Strategies for companies not in BSS");
					strategyRenewalService.createFutureStrategies(company, isMigratedCompany,
							company.getRealmPlanYear(), isDefaultSubmit, bgsHealthPlansMap, bgsADPlansMap, isPreload);
				}
			} catch (Exception e) {
				exceptionOccurred = true;
				throw new BSSApplicationException(e,
						new BSSApplicationError(BSSErrorResponseCodes.BSS_STRATEGY_CREATE_FAIL,
								BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, StrategyServiceImpl.class.getName(),
								"STRATEGY CREATION FAILED", null, null));
			} finally {
				if (null != ps) {
					if (exceptionOccurred) {
						ps.setProcessStatus(BSSApplicationConstants.PROCESS_STATUS_FAILED);
					} else {
						ps.setProcessStatus(BSSApplicationConstants.PROCESS_STATUS_PROCESSED);
					}
					processStatusService.updateProcessStatus(ps);
				}
			}
		}
	}

	private List<StrategyData> getStategiesData(Company company, boolean history, RealmPlanYear realmPlanYear,
			List<Strategy> strategies, boolean isDefault) {
		List<StrategyData> strategyDataList = new ArrayList<>();
		StrategyData strategyData = null;
		List<BenefitGroupStrategy> benefitGroupStrategies = null;
		List<StrategyBenefitGroup> strategyBenefitGroup = null;
		Map<String, Integer> groupHeadCountMap = null;
		Map<String, Map<String, Map<String, Long>>> mirrorPlanHc =new HashMap<>();
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupCovrgHeadCountMap = null;
		logger.info(" company Realm Year ID : {}", company.getRealmPlanYearId());
		
		RealmPlanYear prevRealmYear = getPrevRealmYear(company, history);
		
		Map<String, ActiveEligibleEECount> activeEligibleEmplCount = null;
		Map<String, Long> waiverAllowanceHeadCount = null;
		if (prevRealmYear != null && history) {
				groupHeadCountMap = headCountService.getEmployeeHeadcountByBenefitGroup(company, prevRealmYear.getId(), prevRealmYear.getPlanYearEnd());
				groupCovrgHeadCountMap = headCountService.getHeadCountByGroupAndPlan(company, prevRealmYear.getId(), prevRealmYear.getPlanYearEnd(), false);
				mirrorPlanHc = headCountService.getMirrorPlanHeadCounts(company, prevRealmYear.getId());
		}
		
		Map<String, PlanTypeDescription> planTypeDescMap = strategyDataDao
				.getPlanTypeDescriptions(realmPlanYear.getId());
		
		Map<String, BigDecimal> planEstCostMap = strategyDataDao.getAdditionalBenefitPlanEstCost(realmPlanYear.getId());
		
		Map<String, List<BenefitPlanRate>> planRates = planRatesService.getBenefitPlanRatesBy(company);
		
		Map<Long, Map<String, Map<Long, List<PlanSelection>>>> strategyGroupPlansSelections = strategyDataDao
				.getPlansSelectionsByCompany(company.getCode(), company.getRealmPlanYearId(), company.getPlanStartDate());

		// get REALM_PLYR_PLAN data mapped by benefit plan code (for banding/rates)
		Map<String,XbssRealmPlyrPlan> plyrPlanMap = realmPlyrPlanService.getMapForRealmPlanYear( company.getRealmPlanYear().getId() );

		// Set the carrierId -> minimum funding map to benefitOffers so that UI can use
		// it for min funding calculation.
		List<CarrierMinimumFunding> lowestCostPlanPerCarrier = benefitPlanService.getLowestCostPlanPerCarrier(company);

		Map<String, Map<String, String>> defaultPlanMap = realmDataDao
				.getPortfilioDefaultPlans(company.getRealmPlanYearId());
		// Get all portfolios by Region for realmYearId
		boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions( company );
		Map<String, Set<PlanCarrier>> planCarrierMap = portfolioRuleDao.getPortfoliosByHqRegion(
				company.getRealmPlanYearId(), company.getHeadQuatersState(), company.getZipCode(),
				company.getExclusiveMedPlan(), company.getPlanStartDate(), isPickChoose );

		Set<String> primaryPlanCarriers = BenefitCategoriesHelper.getPlanCarriers(planCarrierMap);
		Set<String> outOfRegionPlans = CommonServiceHelper.getOutOfRegionPlansToExclude(company, primaryPlanCarriers,
				realmDataDao);
		
		Map<String, Map<String, Set<String>>> autoSelectPlansByType = realmDataDao
				.getAutoSelectPlansByRealmIdAndPlanTypes(company.getRealmPlanYearId(), company,
						outOfRegionPlans);
		Set<Long> submitPendingStrategyIds = getSubmitPendingStrategyIdsIfNotHistory(company, history, strategies);
		
		Map<String, Set<String>> eecFundingMap = null;
		
		if (prevRealmYear != null) {
			eecFundingMap = strategyFundingDataDao.getEecFunding(company.getCode(), prevRealmYear.getId());
		}
		
		boolean resetBudgetFactor = isResetBudgetFactorRequired(company);

		boolean isStrategyCacheEnabled = AppRulesAndConfigsUtils.isStrategyCacheEnabled();
		for (Strategy strategy : strategies) {
			//Check in the cache
			String cacheKey = null;
			Object cachedStrategy = null;
			if (isStrategyCacheEnabled) {
				cacheKey = CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE,
						String.valueOf(strategy.getId()));
				cachedStrategy = cacheTemplateService.retrieveFromCache(cacheKey, StrategyData.class);
			}

			//If not in the cache - then compute
			if(ObjectUtils.isEmpty(cachedStrategy)) {
				if (resetBudgetFactor) {
					strategy.setBudgetFactor(1);
				}

				strategyData = getStrategySummaries(strategy, company, prevRealmYear, history);
				long strategyId = getStrategyId(strategyData);

				if (company.isProspectCompany()
						|| company.isProspectConvertedOnboardingClient()) {
					activeEligibleEmplCount = prospectPlanHeadCountService.getProspectEligibleEmployeeCount(company, strategyId);
				} else if (prevRealmYear != null) {
					activeEligibleEmplCount = headCountService.getEligibleEmployeeCount(company, strategyId, prevRealmYear, history);
					waiverAllowanceHeadCount = headCountService.getWaiverHeadCountByBenefitProgram(company, strategyId, prevRealmYear.getId(), history);
				}

				benefitGroupStrategies = strategyGroupService.getBenefitGroupStrategy(strategyId, BSSApplicationConstants.STATUS_ACTIVE);

				strategyBenefitGroup = constructBenefitOffers(benefitGroupStrategies, strategyId, company, prevRealmYear, planRates, plyrPlanMap, planTypeDescMap, planEstCostMap, groupHeadCountMap, history, strategyGroupPlansSelections, defaultPlanMap, autoSelectPlansByType, groupCovrgHeadCountMap, activeEligibleEmplCount, waiverAllowanceHeadCount, lowestCostPlanPerCarrier, mirrorPlanHc, eecFundingMap);

				updateStrategyHeadcount(strategyData, strategyBenefitGroup);

				if (null != strategyData) {
					strategyData.setStrategyHsaFunding(strategyHsaFundingService.findById(strategy.getId()));
				}
				// put computed strategy in the cache
				if (isStrategyCacheEnabled) {
					cacheTemplateService.storeInCache(cacheKey, strategyData, BSSApplicationConstants.TTL_FOR_STRATEGY_CACHE);
				}
			} else {
				//If in the cache - then use from the cache
				if (isStrategyCacheEnabled) {
					try {
						strategyData = (StrategyData) cachedStrategy;
						strategyData.setCached(true);
					} catch (Exception e) {
						cacheTemplateService.deleteFromCache(Set.of(cacheKey));
					}
				}
			}
			updateStrategySummaryFields(company, history, isDefault, strategyData, submitPendingStrategyIds, strategy);

			strategyDataList.add(strategyData);
		}
		
		// Calculate and store the client strategy cost estimate
		if(null != strategyDataList && !strategyDataList.isEmpty()){
			createStrategyEstimate(strategyDataList, company);
		}
		logger.info("This is the END *********************");
		return strategyDataList;
	}

	private long getStrategyId(StrategyData strategyData) {
		long strategyId = 0;
		if (null != strategyData && null != strategyData.getStrategySummary()) {
			strategyId = strategyData.getStrategySummary().getId();
		}
		return strategyId;
	}

	private RealmPlanYear getPrevRealmYear(Company company, boolean history) {
		RealmPlanYear prevRealmYear = null;
		if (history) {
			prevRealmYear = company.getRealmPlanYear();
		} else {
			prevRealmYear = realmPlanYearService.getPreviousRealmPlanYear(company.getCode(),
					company.getRealmPlanYear().getId());
		}
		return prevRealmYear;
	}

	private void updateStrategyHeadcount(StrategyData strategyData, List<StrategyBenefitGroup> strategyBenefitGroup) {
		int count = 0;
		for (StrategyBenefitGroup benefitGroup : strategyBenefitGroup) {
			count += benefitGroup.getHeadcount();
		}
		if (null != strategyData && null != strategyData.getStrategySummary()) {
			strategyData.getStrategySummary().setHeadcount(count);
			strategyData.setBenefitGroups(strategyBenefitGroup);
		}
	}

	private Set<Long> getSubmitPendingStrategyIdsIfNotHistory(Company company, boolean history,
			List<Strategy> strategies) {
		Set<Long> submitPendingStrategyIds = null;
		if (!CollectionUtils.isEmpty(strategies) && !history) {
			submitPendingStrategyIds = findSubmitPendingStrategyIds(company);
		}
		return submitPendingStrategyIds;
	}

	private boolean isResetBudgetFactorRequired(Company company) {
		boolean resetBudgetFactor = false;
		if (DateTimeComparator.getDateOnlyInstance().compare(company.getRealmPlanYear().getPlanYearStart(), null) < 0) {
			resetBudgetFactor = true;
		}
		return resetBudgetFactor;
	}
	
	private void updateStrategySummaryFields(Company company, boolean history, boolean isDefault,
			StrategyData strategyData, Set<Long> submitPendingStrategyIds, Strategy strategy) {
		strategyData.getStrategySummary().setSubmitted(strategy.isSubmitted());
		strategyData.getStrategySummary().setSubmitDate(strategy.getSubmitDate());
		if (isDefault || BSSApplicationConstants.BANDCHANGE_USER_ID.equals(company.getEmplId())
				|| ProcessStatusEnum.TERMED_CLIENT_DEFAULT_SUBMIT.getProcessName().equals(company.getEmplId())) {
			strategyData.getStrategySummary().setCanDelete(false);
		} else {
			strategyData.getStrategySummary().setCanDelete(
					StrategyServiceHelper.isStrategyDeletable(strategy, submitPendingStrategyIds, history));
		}
	}

	/**
	 * 
	 * @param strategyDataList
	 */
	private void createStrategyEstimate(List<StrategyData> strategyDataList, Company company) {
		// Get a list of estimates by strategy, group and plan type
		Map<Long, List<StrategyEstimate>> strategyEstimateMap = StrategyServiceHelper
				.calcStrategyEstimate(strategyDataList, company);
		// Delete any current data from the database
		Set<Long> strategyIds = strategyEstimateMap.keySet();
		if (CollectionUtils.isNotEmpty(strategyIds)) {
			strategyDataDao.deleteStrategyEstimateList(strategyIds);
		}
		// Save the new data in the database
		strategyDataDao.insertStrategyEstimate(strategyEstimateMap);
		createOmsStrategyEstimate(company, strategyIds);
	}

	@Override
	public void createOmsStrategyEstimate(Company company, Set<Long> strategyIds) {
		if (CompanyServiceHelper.isTibProspect(company)) {
			// Delete any current data from the database
			strategyDataDao.deleteStrategyEstimateForPlanTypes(strategyIds, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
			// Save the new data in the database
			strategyDataDao.insertStrategyEstimateForOmsPlanTypes(strategyIds, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
		}
	}

	private StrategyData getStrategySummaries(Strategy strategy, Company company, RealmPlanYear prevRealmPlanYear,
			boolean history) {
		StrategyData strategyData = null;
		if (strategy != null) {
			strategyData = new StrategyData();

			StrategySummary strategySummary = new StrategySummary();
			strategySummary.setId(strategy.getId());
			strategySummary.setComments(strategy.getComments());

			setEffectiveAndEndDate(company, prevRealmPlanYear, history, strategySummary);

			strategySummary.setName(strategy.getName());
			strategySummary.setType(strategy.getType());
			strategySummary.setEstimatedTotalCost(strategy.getEstimatedTotalCost());
			strategySummary.setCurrentYearTotalCost(strategy.getCurrentYearTotalCost());
			strategySummary.setTotalBudget(strategy.getTotalBudget());
			strategySummary.setBudgetFactor(strategy.getBudgetFactor());
			strategySummary.setCompanyId(company.getCode());
			strategySummary.setCostShareType(strategy.getCostShareType());
			boolean acaFplOpted = strategy.getAcaFplOpted() == 1 ? Boolean.TRUE : Boolean.FALSE;
			strategySummary.setAcaFplOpted(acaFplOpted);
			updateStrategySummaryPkgType(strategy, strategySummary);
			if (company.isRenewalCompany()) {
			   strategySummary.setHeadcount(company.getActualHeadCount());
			}
			else 
				strategySummary.setHeadcount(company.getHeadcount());
			
			if(CollectionUtils.isNotEmpty(strategy.getStrategyRegions())) {
				strategySummary.setFilterRegions(
						strategy.getStrategyRegions().stream().map(e -> e.getRegion()).collect(Collectors.toList()));				
			}
			strategyData.setStrategySummary(strategySummary);
		}
		return strategyData;
	}

	private void setEffectiveAndEndDate(Company company, RealmPlanYear prevRealmPlanYear, boolean history,
			StrategySummary strategySummary) {
		if(history) {
			strategySummary.setEffectiveDate(prevRealmPlanYear.getPlanYearStart());
			Date endDate = prevRealmPlanYear.getPlanYearEnd();
		    if (endDate != null) {
				strategySummary.setEndDate(endDate);
			}
		}
		else {
			strategySummary.setEffectiveDate(CommonUtils.formatStringToDate(company.getPlanStartDate(),
					BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		}
	}
		

	private void updateStrategySummaryPkgType(Strategy strategy, StrategySummary strategySummary) {
		if (strategy.getPkgType() != null) {
			if (strategy.getPkgType().equals(Constants.BALANCED_ID)) {
				strategySummary.setPkgType(Constants.BALANCED_PACKAGE_NAME);
			}
			else if(strategy.getPkgType().equals(Constants.CONSERVATIVE_ID)) {
				strategySummary.setPkgType(Constants.CONSERVATIVE_PACKAGE_NAME);
			}
			else if(strategy.getPkgType().equals(Constants.TOP_QUALITY_ID)) {
				strategySummary.setPkgType(Constants.TOP_QUALITY_NAME);
			}
		}
	}
	
	private List<StrategyBenefitGroup> constructBenefitOffers(List<BenefitGroupStrategy> benefitGroupStrategies,
			long strategyId, Company company, RealmPlanYear prevRealmPlanYear,
			Map<String, List<BenefitPlanRate>> planRates, Map<String, XbssRealmPlyrPlan> plyrPlanMap,
			Map<String, PlanTypeDescription> planTypeDescMap, Map<String, BigDecimal> planEstCostMap,
			Map<String, Integer> groupHeadCountMap, boolean history,
			Map<Long, Map<String, Map<Long, List<PlanSelection>>>> strategyGroupPlansSelections,
			Map<String, Map<String, String>> defaultPlanMap,
			Map<String, Map<String, Set<String>>> autoSelectPlansByType,
			Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupCovrgHeadCountMap,
			Map<String, ActiveEligibleEECount> activeEligibleEmplCount, Map<String, Long> waiverAllowanceHeadCount,
			List<CarrierMinimumFunding> lowestCostPlanPerCarrier,
			Map<String, Map<String, Map<String, Long>>> mirrorPlanHc, Map<String, Set<String>> eecFundingMap) {
		long realmPlanYearId = 0;

		Map<BenefitPlan, BenefitPlan> employerEmployeePlansMapping = employerEmployeePlansMappingService
				.getEmployerEmployeePlansMappingByRealmYearId(company.getRealmPlanYearId());
		realmPlanYearId = company.getRealmPlanYearId();

		boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions( company );
		Map<String, Set<PlanCarrier>> portfolioMap = portfolioRuleDao.getStrategyPortfolios(strategyId, realmPlanYearId,
				defaultPlanMap, company.getHeadQuatersState(), isPickChoose );
		Map<String, Map<Long, List<PlanSelection>>> benefitOfferPlans = strategyGroupPlansSelections.get(strategyId);
		List<PlanSelection> allGroupsAddPlanSelections = getAdditionalPlanSelections(benefitOfferPlans);
		Map<String, Map<String, BigDecimal>> groupPlanCostMap = calculateLifeAndDisabilityPlansCost(company,
				allGroupsAddPlanSelections, history, benefitGroupStrategies, strategyId);
		
		Map<String, Map<String, Map<String, Object>>> strategyGroupFundingDetails = realmDataDao.getStrategyFundingDetails(strategyId);
		List<CompletableFuture<StrategyBenefitGroup>> futures = new ArrayList<>();
		List<StrategyBenefitGroup> sbgs = new ArrayList<>();
		for (BenefitGroupStrategy bgs : benefitGroupStrategies) {
			if (AppRulesAndConfigsUtils.isStrategyThreadEnabled()) {
				futures.add(CompletableFuture.supplyAsync(() -> {
					return contructBenefitOffer(strategyId, company, prevRealmPlanYear, planRates, plyrPlanMap,
							planTypeDescMap, planEstCostMap, groupHeadCountMap, history, autoSelectPlansByType,
							groupCovrgHeadCountMap, activeEligibleEmplCount, waiverAllowanceHeadCount,
							lowestCostPlanPerCarrier, mirrorPlanHc, eecFundingMap, bgs, benefitOfferPlans,
							strategyGroupFundingDetails, portfolioMap, groupPlanCostMap, employerEmployeePlansMapping);
				}, executor));
			} else {
				StrategyBenefitGroup sbg = contructBenefitOffer(strategyId, company, prevRealmPlanYear, planRates, plyrPlanMap,
						planTypeDescMap, planEstCostMap, groupHeadCountMap, history, autoSelectPlansByType,
						groupCovrgHeadCountMap, activeEligibleEmplCount, waiverAllowanceHeadCount,
						lowestCostPlanPerCarrier, mirrorPlanHc, eecFundingMap, bgs, benefitOfferPlans,
						strategyGroupFundingDetails, portfolioMap, groupPlanCostMap, employerEmployeePlansMapping);
				sbgs.add(sbg);
			}
		}
		if(AppRulesAndConfigsUtils.isStrategyThreadEnabled()) {
			sbgs = futures.stream()
					.map(CompletableFuture::join)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		}
		return sbgs;
	}

	private StrategyBenefitGroup contructBenefitOffer(
            long strategyId, Company company, RealmPlanYear prevRealmPlanYear, Map<String, List<BenefitPlanRate>> planRates,
            Map<String, XbssRealmPlyrPlan> plyrPlanMap, Map<String, PlanTypeDescription> planTypeDescMap,
            Map<String, BigDecimal> planEstCostMap, Map<String, Integer> groupHeadCountMap, boolean history,
            Map<String, Map<String, Set<String>>> autoSelectPlansByType, Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupCovrgHeadCountMap,
            Map<String, ActiveEligibleEECount> activeEligibleEmplCount, Map<String, Long> waiverAllowanceHeadCount,
            List<CarrierMinimumFunding> lowestCostPlanPerCarrier, Map<String, Map<String, Map<String, Long>>> mirrorPlanHc,
            Map<String, Set<String>> eecFundingMap, BenefitGroupStrategy bgs, Map<String, Map<Long, List<PlanSelection>>> benefitOfferPlans,
            Map<String, Map<String, Map<String, Object>>> strategyGroupFundingDetails, Map<String, Set<PlanCarrier>> portfolioMap,
            Map<String, Map<String, BigDecimal>> groupPlanCostMap, Map<BenefitPlan, BenefitPlan> employerEmployeePlansMapping) {

			Map<String, List<PlanCoverageLevelHeadCount>> primaryPlanHeadCountMap = null;
			StrategyBenefitGroup strategyBenefitGroup = StrategyServiceHelper.constructBenefitGroup(strategyId, bgs);
			long medicalWaiverHeadCount = 0;
			if(null != waiverAllowanceHeadCount && !waiverAllowanceHeadCount.isEmpty() && null != waiverAllowanceHeadCount.get(strategyBenefitGroup.getBenefitProgram())){
				medicalWaiverHeadCount = waiverAllowanceHeadCount.get(strategyBenefitGroup.getBenefitProgram());
			}
			if (!company.isRenewalCompany()) {
				List<StrategyGroupHeadCount> strategyGrpHeadCounts = strategyGroupHeadCountService
						.findStrategyGroupHeadCountBy(strategyBenefitGroup.getStrategyGroupId());
				if (null != strategyGrpHeadCounts && !strategyGrpHeadCounts.isEmpty()) {
					strategyBenefitGroup.setCoverageLevelHeadCounts(
							StrategyServiceHelper.prepareCoverageLevelHeadCounts(strategyGrpHeadCounts));
				}
			}
			long groupId = strategyBenefitGroup.getId();
			if (prevRealmPlanYear != null) {
				String benefitProgram = strategyBenefitGroup.getBenefitProgram();
				if (history) {
					if (groupHeadCountMap != null && groupHeadCountMap.get(benefitProgram) != null) {
						strategyBenefitGroup.setHeadcount(groupHeadCountMap.get(benefitProgram));
					} else {
						strategyBenefitGroup.setHeadcount(0);
					}
				}
				if (null != groupCovrgHeadCountMap) {
					primaryPlanHeadCountMap = groupCovrgHeadCountMap.get(benefitProgram);
				}
			}

			List<Long> planIdList = null;
			if (null != benefitOfferPlans) {
				planIdList = StrategyServiceHelper.getPlanSelectionIds(groupId, benefitOfferPlans);
			}
			// Passing history flag to get the dollar amounts for contributions.
			Map<Long, List<PlanContribution>> constributionsMap = new HashMap<>();
			if (null != planIdList && !planIdList.isEmpty()) {
				if (company.isBandCodeUpdated() || history) {
					constributionsMap = contributionService.getPlanContributions(planIdList, planRates, true);
				} else {
					constributionsMap = contributionService.getPlanContributions(planIdList, planRates, false);
				}
			}

			Map<String, PlanPackage> planPackageMap = StrategyServiceHelper.constructPlanPackages(
					strategyGroupFundingDetails.get(strategyBenefitGroup.getBenefitProgram()), company, planRates,
					plyrPlanMap, realmDataDao, strategyId, strategyFundingDataDao);

			if (MapUtils.isNotEmpty(planPackageMap)) {
				List<StrategyFundingBasePlanLimits> sfbplList = new ArrayList<>();
				for (Map.Entry<String, PlanPackage> planPackageEntry : planPackageMap.entrySet()) {
					PlanPackage pkg = planPackageEntry.getValue();
					if (null != pkg && MapUtils.isNotEmpty(pkg.getCoverageLevelBasePlanLimits())) {
						Map<String, BigDecimal> basePlanLimits = pkg.getCoverageLevelBasePlanLimits();
						StrategyFundingModel sfm = strategyFundingModelDao.getReferenceById(pkg.getFundingModelId());
						for (Map.Entry<String, BigDecimal> entry : basePlanLimits.entrySet()) {
							StrategyFundingBasePlanLimits sfbpl = new StrategyFundingBasePlanLimits();
							StrategyFundingDetailId sfDetailId = new StrategyFundingDetailId();
							sfDetailId.setStrategyFundingId(pkg.getFundingModelId());
							sfDetailId.setCoverageId(entry.getKey());
							sfbpl.setSfDetailId(sfDetailId);
							sfbpl.setContribution(entry.getValue());
							sfbpl.setStrategyFundingModel(sfm);
							sfbplList.add(sfbpl);
						}
					}
				}
				if (CollectionUtils.isNotEmpty(sfbplList)) {
					strategyBasePlanLimitsDao.saveAll(sfbplList);
				}
			}

			List<BenefitOffer> benefitOffers = new ArrayList<>();

			if (CompanyServiceHelper.isTibProspect(company)) {
				// Create a map of code and name for Medical, Dental and Vision from PlanTypesEnum
				List<PlanTypesEnum> planTypes = new ArrayList<>();
				planTypes.add(PlanTypesEnum.MEDICAL);
				planTypes.add(PlanTypesEnum.DENTAL);
				planTypes.add(PlanTypesEnum.VISION);

				for (PlanTypesEnum planTypeEnum : planTypes) {
					BenefitOffer benefitOffer = new BenefitOffer();
					benefitOffer.setPlanCarriers(portfolioService.getOmsPlanCarriersForStrategyIdAndPlanType(strategyId, planTypeEnum.getCode()));
					Map.Entry<String, Map<Long, List<PlanSelection>>> entry = Map.entry(planTypeEnum.getName(), new HashMap<>());
					boolean baseFundingRequired = getBaseFundingRequired(
							strategyBenefitGroup.getBenefitProgram(), planTypeEnum.getName(), eecFundingMap);
					constructPrimaryBenefitOffers(company, planRates, plyrPlanMap, strategyId, groupId, constributionsMap,
							benefitOffers, planPackageMap, entry, benefitOffer, new ArrayList<>(), autoSelectPlansByType,
							history, employerEmployeePlansMapping,
							primaryPlanHeadCountMap, medicalWaiverHeadCount,
							lowestCostPlanPerCarrier, baseFundingRequired,
							mirrorPlanHc.get(strategyBenefitGroup.getBenefitProgram()));
				}
			}

			if (null != benefitOfferPlans) {
				for (Map.Entry<String, Map<Long, List<PlanSelection>>> entry : benefitOfferPlans.entrySet()) {
					BenefitOffer benefitOffer = new BenefitOffer();
					benefitOffer.setPlanCarriers(portfolioMap.get(entry.getKey()));
					List<PlanSelection> planList = entry.getValue().get(groupId);
					if (null != planList && !planList.isEmpty()) {
						if (PlanTypesEnum.ADDITIONAL.getName().equals(entry.getKey())) {
							constructAdditionalBenefitOffer(company, prevRealmPlanYear,
									planTypeDescMap, planEstCostMap, strategyBenefitGroup, groupId, benefitOffers,
									entry, benefitOffer, planList, activeEligibleEmplCount,
									groupPlanCostMap.get(strategyBenefitGroup.getBenefitProgram()));
						} else {
							boolean baseFundingRequired = getBaseFundingRequired(
									strategyBenefitGroup.getBenefitProgram(), entry.getKey(), eecFundingMap);
							constructPrimaryBenefitOffers(company, planRates, plyrPlanMap, strategyId, groupId, constributionsMap,
									benefitOffers, planPackageMap, entry, benefitOffer, planList, autoSelectPlansByType,
									history, employerEmployeePlansMapping,
									primaryPlanHeadCountMap, medicalWaiverHeadCount,
									lowestCostPlanPerCarrier, baseFundingRequired,
									mirrorPlanHc.get(strategyBenefitGroup.getBenefitProgram()));
						}
					}
				}
			}
			strategyBenefitGroup.setBenefitOffers(benefitOffers);
			return strategyBenefitGroup;
	}

	private List<PlanSelection> getAdditionalPlanSelections(
			Map<String, Map<Long, List<PlanSelection>>> benefitOfferPlans) {
		Map<Long, List<PlanSelection>> additionalPlanSelections = null;
		if (MapUtils.isNotEmpty(benefitOfferPlans)) {
			additionalPlanSelections = benefitOfferPlans.get(BSSApplicationConstants.ADDITIONAL);
		}
		
		List<PlanSelection> allGroupsAddPlanSelections = new ArrayList<>();
		if (null != additionalPlanSelections && !additionalPlanSelections.isEmpty()) {
			for (Map.Entry<Long, List<PlanSelection>> entry: additionalPlanSelections.entrySet()) {
				allGroupsAddPlanSelections.addAll(entry.getValue());
			}
		}
		return allGroupsAddPlanSelections;
	}
	/**
	 * This method is for constructing primary Benefit Offer.
	 * @param company
	 * @param planRates
	 * @param plyrPlanMap
	 * @param strategyId
	 * @param groupId
	 * @param contributionsMap
	 * @param benefitOffers
	 * @param pkgMap
	 * @param entry
	 * @param benefitOffer
	 * @param planList
	 * @param autoSelectPlansByType
	 * @param history
	 * @param employerEmployeePlansMapping
	 * @param primaryPlanHeadCountMap
	 * @param waiverHeadCount
	 * @param lowestCostPlanPerCarrier
	 * @param baseFundingRequired
	 * @param benefitPlanMirrorHc
	 */
	private void constructPrimaryBenefitOffers(Company company, Map<String, List<BenefitPlanRate>> planRates,
			Map<String, XbssRealmPlyrPlan> plyrPlanMap, long strategyId, long groupId,
			Map<Long, List<PlanContribution>> contributionsMap, List<BenefitOffer> benefitOffers,
			Map<String, PlanPackage> pkgMap, Map.Entry<String, Map<Long, List<PlanSelection>>> entry,
			BenefitOffer benefitOffer, List<PlanSelection> planList,
			Map<String, Map<String, Set<String>>> autoSelectPlansByType, boolean history,
			Map<BenefitPlan, BenefitPlan> employerEmployeePlansMapping,
			Map<String, List<PlanCoverageLevelHeadCount>> primaryPlanHeadCountMap, long waiverHeadCount,
			List<CarrierMinimumFunding> lowestCostPlanPerCarrier,
			boolean baseFundingRequired, Map<String, Map<String, Long>> benefitPlanMirrorHc) {
		Map<String, BenefitPlan> benefitPlansMap = new HashMap<>();
		Map<String, Set<String>> autoSelectPlans = new HashMap<>();
		Map<String, Set<String>> medicalAutoSelectPlans = null;
		Map<String, Set<String>> dentalAutoSelectPlans = null;
		Map<String, Set<String>> visionAutoSelectPlans = null;
		boolean isEmployeePaid = false;
		String offerType = entry.getKey();
		if (autoSelectPlansByType != null) {
			medicalAutoSelectPlans = autoSelectPlansByType.get(Constants.MEDICAL);
			dentalAutoSelectPlans = autoSelectPlansByType.get(Constants.DENTAL);
			visionAutoSelectPlans = autoSelectPlansByType.get(Constants.VISION);
		}
		BenefitOfferSummary benefitOfferSummary = new BenefitOfferSummary();
		benefitOfferSummary.setGroupId(groupId);
		benefitOfferSummary.setType(offerType);
		benefitOfferSummary.setBaseFundingRequired(baseFundingRequired);
		benefitOfferSummary.setEstimatedTotalCost(getEstimatedTotalCost(company, entry.getKey(), strategyId, groupId));

		if (CollectionUtils.isNotEmpty(lowestCostPlanPerCarrier)) {
			Map<Long, BigDecimal> minFunding = BenefitCategoriesHelper.calculateCarrierMinFunding(company,
					offerType, lowestCostPlanPerCarrier);
			benefitOfferSummary.setMinFunding(minFunding);
		}
		
		benefitOffer.setSummary(benefitOfferSummary);
		if (entry.getKey().equalsIgnoreCase(Constants.MEDICAL)) {
			if (autoSelectPlans != null) {
				autoSelectPlans = medicalAutoSelectPlans;
			}
			benefitOfferSummary.setWaiverHeadcount(waiverHeadCount);
		} else if (entry.getKey().equalsIgnoreCase(Constants.DENTAL)) {
			if (autoSelectPlans != null) {
				autoSelectPlans = dentalAutoSelectPlans;
			}
		} else if (entry.getKey().equalsIgnoreCase(Constants.VISION) && autoSelectPlans != null) {
			autoSelectPlans = visionAutoSelectPlans;
		}
		Set<String> planSelections = new HashSet<>();
		Map<String, PlanSelection> mapOfPlanselections = new HashMap<>();
		for (PlanSelection planSelection : planList) {
			planSelections.add(planSelection.getBenefitPlan());
			mapOfPlanselections.put(planSelection.getBenefitPlan(), planSelection);
		}
		Set<String> widelyAvailablePlanSet = benefitPlanDao.getWidelyAvailablePlans(planSelections, company.getRealmPlanYearId());
		Map<String, List<String>> benefitPlansStatesMap = realmDataDao
				.getBenefitsPlans(company.getRealmPlanYearId(), planSelections);
		StrategyServiceHelper.updatePlanSelectionRegions(planList, benefitPlansStatesMap);
		List<Contribution> updatedhistoryContributions = new ArrayList<>();

		// Get exclusion plan list to use lowest cost client selected plan not mandated plans
		// unless client HQ is in mandated State.
		List<String> mandatoryPlansToExclude = realmPlyrPlanDao.getAllMandatoryPlansExcludingGivenRegion(
				company.getHeadQuatersState(), BigDecimal.valueOf(company.getRealmPlanYearId()));
		for (PlanSelection plan : planList) {
			BenefitPlan benefitPlanNew = new BenefitPlan();
			benefitPlanNew.setId(plan.getBenefitPlan());
			Set<String> crossRefPlans = new HashSet<>();
			if (autoSelectPlans != null && autoSelectPlans.containsKey(plan.getBenefitPlan())) {
				crossRefPlans = autoSelectPlans.get(plan.getBenefitPlan());
			}
			benefitPlanNew.setName(plan.getName());
			benefitPlanNew.setPlanCarrierId(plan.getPlanCarrierId());
			benefitPlanNew.setCrossRefPlans(crossRefPlans);
			benefitPlanNew.setPlanType(plan.getPlanType());
			benefitPlanNew.setPlanCategory(plan.getPlanCategory());
			benefitPlanNew.setHighDeductible(plan.isHighDeductiblePlan());
			if(BSSApplicationConstants.FPL.equals(plan.getPlanCategory())) {
				benefitPlanNew.setMandatory(true);
			}

			benefitPlanNew.setPpoPlan(widelyAvailablePlanSet.contains(plan.getBenefitPlan()));
			benefitPlanNew.setWidelyAvailablePlan(widelyAvailablePlanSet.contains(plan.getBenefitPlan()));
			
			Collections.sort(plan.getListOfStates());
			benefitPlanNew.setOfferedStates(plan.getListOfStates());
			benefitPlanNew.setMandatoryExcluded(mandatoryPlansToExclude.contains(plan.getBenefitPlan()));
			if (Constants.voluntaryPlanTypeList.contains(benefitPlanNew.getPlanType())) {
				benefitPlanNew.setEmployeePaid(true);
				isEmployeePaid = true;
			}

            Map<String, BigDecimal> costMap = StrategyUtils.getPlanCost(planRates.get(plan.getBenefitPlan()));
			if (Constants.primaryPlanTypeList.contains(benefitPlanNew.getPlanType())) {
				benefitOfferSummary.setDescription(benefitPlanNew.getPlanType());
				List<PlanContribution> conList = new ArrayList<>();
				List<PlanContribution> planContributions = contributionsMap.get(plan.getId());
				if (null != planContributions && !planContributions.isEmpty()) {
					for (PlanContribution contribution : planContributions) {
						if (history) {
							int headCount = 0;
							int hsaHeadCount = 0;
							int mirrorHeadCount = 0;
							List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();
							if (null != primaryPlanHeadCountMap
									&& null != primaryPlanHeadCountMap.get(benefitPlanNew.getId())) {
								headCountList.addAll(primaryPlanHeadCountMap.get(benefitPlanNew.getId()));
								if (headCountList != null && !headCountList.isEmpty()) {
									headCount = RenewalServiceHelper.getCovrgHeadCount(headCountList,
											CoverageCodesEnums.valueOfCode(contribution.getType()));
									hsaHeadCount = RenewalServiceHelper.getCovrgHsaHeadCount(headCountList,
											CoverageCodesEnums.valueOfCode(contribution.getType()));
								}
							}
							if (null != benefitPlanMirrorHc
									&& null != benefitPlanMirrorHc.get(benefitPlanNew.getId())) {
								mirrorHeadCount = RenewalServiceHelper.getMirrorPlanCvgLevelHeadCount(
										benefitPlanMirrorHc.get(benefitPlanNew.getId()),
										CoverageCodesEnums.valueOfCode(contribution.getType()));
							}
							if (headCount != contribution.getHeadcount()
									|| hsaHeadCount != contribution.getHsaHeadcount()
									|| mirrorHeadCount != contribution.getMirrorHeadCount()) {
								contribution.setHeadcount(headCount);
								contribution.setHsaHeadcount(hsaHeadCount);
								contribution.setMirrorHeadCount(mirrorHeadCount);
								Contribution cb = StrategyServiceHelper.constructContribution(contribution);
								cb.setPlanSelectionId(plan.getId());
								updatedhistoryContributions.add(cb);
							}
						}
						if (history || company.isBandCodeUpdated()) {
							// If history not using plan rates, just use the
							// contribution we have in BSS DB
							if (contribution.getEmployeeContribution() != null
									&& contribution.getEmployerContribution() != null) {
								contribution.setPlanCost(contribution.getEmployeeContribution()
										.add(contribution.getEmployerContribution()));
							} else {
								contribution.setPlanCost(new BigDecimal(0));
							}
						} else {
							BigDecimal planCost = costMap.get(contribution.getType().trim());
							if (planCost != null) {
								contribution.setPlanCost(costMap.get(contribution.getType().trim()));
							} else {
								logger.info(
										"######  NO PLAN COST DATA FOR PLAN {} PLAN TYPE : {}  COVERAGE CODE : {}", benefitPlanNew.getId(),plan.getPlanType(),contribution.getType());
							}
						}
						contribution.setBenefitPlanId(plan.getBenefitPlan());
						conList.add(contribution);
						logger.info("PLAN COST :{}   BENEFIT PLAN : {}  PLAN TYPE : {}" , contribution.getPlanCost(),benefitPlanNew.getId(),plan.getPlanType());
					}
				} else {
					logger.info("PLAN_CONTRIBUTIONS IS NULL FOR PLAN ID : {}\t PLAN : {}\t PORTFOLIO ID : {}\t PLAN TYPE : {}"
							, plan.getId(),plan.getBenefitPlan(),plan.getPlanCarrierId(),plan.getPlanType());
				}
				benefitPlanNew.setContributions(conList);
			}
			benefitPlanNew = setEmployeeEmployerOptionalPlan(benefitPlanNew, employerEmployeePlansMapping);
			benefitPlansMap.put(benefitPlanNew.getId(), benefitPlanNew);
		}
		// saving the updated history contributions
		if (!updatedhistoryContributions.isEmpty()) {
			contributionService.saveAll(updatedhistoryContributions);
		}

		List<BenefitPlan> benefitPlansList = new ArrayList<>();
		benefitPlansList.addAll(benefitPlansMap.values());
		benefitOffer.setBenefitPlans(benefitPlansList);
		PlanPackage planPackage = null;
		if (pkgMap.get(entry.getKey()) != null) {
			planPackage = pkgMap.get(entry.getKey());
		} else {
			planPackage = new PlanPackage();
		}
		planPackage.setEmployeePaid(isEmployeePaid);
		benefitOffer.setPlanPackage(planPackage);
		benefitOffers.add(benefitOffer);
	}

	private BenefitPlan setEmployeeEmployerOptionalPlan(
			BenefitPlan benefitPlanNew,
			Map<BenefitPlan, BenefitPlan> employerEmployeePlansMapping) {
		if(benefitPlanNew.isEmployeePaid() && employerEmployeePlansMapping.containsKey(benefitPlanNew)) {
    		if( employerEmployeePlansMapping.get(benefitPlanNew)!=null) {
    			benefitPlanNew.setOptionalPlans(employerEmployeePlansMapping.get(benefitPlanNew).getId());
    		}	
    	}
		else if( employerEmployeePlansMapping.containsKey(benefitPlanNew) && employerEmployeePlansMapping.get(benefitPlanNew)!=null) {
			benefitPlanNew.setOptionalPlans(employerEmployeePlansMapping.get(benefitPlanNew).getId());
	}
		return benefitPlanNew;
	}
	
	private void constructAdditionalBenefitOffer(Company company, RealmPlanYear prevRealmPlanYear,
			Map<String, PlanTypeDescription> planTypeDescMap, Map<String, BigDecimal> planEstCostMap,
			StrategyBenefitGroup benefitGroup, long groupId, List<BenefitOffer> benefitOffers,
			Map.Entry<String, Map<Long, List<PlanSelection>>> entry, BenefitOffer benefitOffer,
			List<PlanSelection> planSelectionList,
			Map<String, ActiveEligibleEECount> activeEligibleEmplCount, Map<String, BigDecimal> planCostMap) {
		Map<String, AdditionalBenefitOffer> adBenefitOfferMap = new HashMap<>();
		List<String> adPlans = new ArrayList<>();
		Map<String, PlanSelection> adPlanMap = new HashMap<>();
		boolean isStdPlanAvailable = false;
		boolean isLtdPlanAvailable = false;
		boolean isStandAlone = false;
		ActiveEligibleEECount activeHeadCount = null;
		if (MapUtils.isNotEmpty(activeEligibleEmplCount)) {
			activeHeadCount = activeEligibleEmplCount.get(benefitGroup.getBenefitProgram());
		}
		for (PlanSelection ps : planSelectionList) {
			if (Constants.LIFE_CMTR_PLANS.contains(ps.getPlanType())) {
				AdditionalBenefitPlan ab = new AdditionalBenefitPlan();
				ab.setId(ps.getBenefitPlan());
				ab.setPlanType(ps.getPlanType());
				ab.setDescription(ps.getName());
				if (null != activeHeadCount) {
					if (activeHeadCount.getTotalHeadCount() == 0) {
						ps.setHeadCount(Long.valueOf(activeHeadCount.getPrimaryHeadCount()) + Long.valueOf(activeHeadCount.getSecondaryHeadCount()));
					} else {
						ps.setHeadCount(activeHeadCount.getTotalHeadCount());
					}
					planSelectionService.createUpdatePlanSelection(ps);
				}
				if (Constants.COMMUTER_CODE.equals(ps.getPlanType())) {
					ab.setPlanCost(planEstCostMap.get(ps.getBenefitPlan()));
				} else {
					if (planCostMap != null && planCostMap.get(ps.getBenefitPlan()) != null) {
						ab.setPlanCost(planCostMap.get(ps.getBenefitPlan()));
					}
					ab.setAnnualCap(planEstCostMap.get(ps.getBenefitPlan()));
				}
				if (null != adBenefitOfferMap.get(ps.getPlanType())) {
					adBenefitOfferMap.get(ps.getPlanType()).getAdditionalBenefitPlans().add(ab);
				} else {
					AdditionalBenefitOffer additionalBenefitOffer = new AdditionalBenefitOffer();
					BenefitOfferSummary summary = new BenefitOfferSummary();
					summary.setDescription(planTypeDescMap.get(ps.getPlanType()).getDescription());
					summary.setType(PlanTypesEnum.getName(ps.getPlanType()));
					summary.setGroupId(groupId);
					long headCount = ps.getHeadCount();
					if (headCount != 0) {
						summary.setHeadcount(headCount);
					} else {
						summary.setHeadcount(benefitGroup.getHeadcount());
					}
					additionalBenefitOffer.setSummary(summary);
					additionalBenefitOffer.getAdditionalBenefitPlans().add(ab);
					adBenefitOfferMap.put(ps.getPlanType(), additionalBenefitOffer);
				}
			} else {
				adPlans.add(ps.getBenefitPlan());
				adPlanMap.put(ps.getBenefitPlan(), ps);
				if (Constants.STD_CODE.equals(ps.getPlanType())) {
					isStdPlanAvailable = true;
				}
				if (Constants.LTD_CODE.equals(ps.getPlanType())) {
					isLtdPlanAvailable = true;
				}
			}
		}
		benefitOffer.getAdditionalBenefitOffers().addAll(adBenefitOfferMap.values());
		if (null != adPlans && !adPlans.isEmpty()) {
			// converting plans to options for STD & LTD
			if (isStdPlanAvailable && isLtdPlanAvailable) {
				isStandAlone = false;
			} else {
				isStandAlone = true;
			}
			AdditionalBenefitPlan adPlan = disabilityOptionService.getDisabilityOptionByPlans(adPlans, company,
					isStandAlone);
			if (null != adPlan) {
				AdditionalBenefitOffer additionalBenefitOffer = new AdditionalBenefitOffer();
				BenefitOfferSummary summary = new BenefitOfferSummary();
				summary.setDescription("Short & Long Term Disability Plan Options");
				summary.setType("DISABILITY");
				summary.setGroupId(groupId);
				if (null == prevRealmPlanYear) {
					summary.setHeadcount(benefitGroup.getHeadcount());
				}
				populatePlanHeadCount(adPlanMap, activeHeadCount, adPlan);
				// calculating the total cost of the options.
				AdditionalBenefitServiceHelper.populatePlanOptionsCost(planCostMap, activeHeadCount, adPlan, company);
				
				additionalBenefitOffer.setSummary(summary);
				additionalBenefitOffer.getAdditionalBenefitPlans().add(adPlan);
				benefitOffer.getAdditionalBenefitOffers().add(additionalBenefitOffer);
			}
		}
		BenefitOfferSummary benefitOfferSummary = new BenefitOfferSummary();
		benefitOfferSummary.setType(entry.getKey());
		benefitOfferSummary.setGroupId(groupId);
		if (null != activeHeadCount) {
			if (activeHeadCount.getTotalHeadCount() == 0) {
				benefitOfferSummary.setHeadcount(
						(long)activeHeadCount.getPrimaryHeadCount() + (long)activeHeadCount.getSecondaryHeadCount());
			} else {
				benefitOfferSummary.setHeadcount((long)activeHeadCount.getTotalHeadCount());
			}
		} else {
			benefitOfferSummary.setHeadcount(benefitGroup.getHeadcount());
		}
		benefitOffer.setSummary(benefitOfferSummary);
		benefitOffers.add(benefitOffer);
	}

	/**
	 * 
	 * @param company
	 * @param adPlanSelections
	 * @param history
	 * @param benefitGroupStrategies
	 * @param strategyId
	 * @return map from Benefit Program to Benefit Plan to monthly cost
	 */
	private Map<String, Map<String, BigDecimal>> calculateLifeAndDisabilityPlansCost(Company company,
			List<PlanSelection> adPlanSelections, boolean history, List<BenefitGroupStrategy> benefitGroupStrategies,
			long strategyId) {

		boolean isVendorMappingOn = RulesAndConfigsUtils.isVendorMappingOn(company.getRealmPlanYear().getId());

		Map<String, Set<String>> adbPlanMap = AdditionalBenefitServiceHelper.getADBPlanListMapByType(adPlanSelections);

		Map<String, AdditionalBenefitEmployeeDetails> employeeSelection = lifeAndDisabilityCalcData
				.getGroupEmployeeSelections(company, history, strategyId, isVendorMappingOn);

		Map<String, BigDecimal> basePlanCostMap = additionalBenefitPlanService.calculateAdditionalPlansCost(company,
				history, adbPlanMap);

		Map<String, Map<String, BigDecimal>> groupPlanCostMap = new HashMap<>();

		boolean shouldCalculateGroupCosts = company.isRenewalCompany() || company.isProspectCompany()
				|| company.isProspectConvertedClient();

		if (shouldCalculateGroupCosts && employeeSelection != null && !employeeSelection.isEmpty()) {
			groupPlanCostMap = additionalBenefitPlanService.calculateAdditionalPlansCostByGroup(company, history,
					adbPlanMap, employeeSelection);
		}

		// Fill in missing benefit programs with base plan costs
		for (BenefitGroupStrategy strategy : benefitGroupStrategies) {
			String benefitProgram = strategy.getBenefitGroup().getBenefitProgram();
			groupPlanCostMap.computeIfAbsent(benefitProgram, k -> basePlanCostMap);
		}

		return groupPlanCostMap;
	}
	
	@Override
	public StrategyData getStrategyById(Company company, long strategyId, boolean isDefault) {
		RealmPlanYear realmPlanYear = company.getRealmPlanYear();
		Strategy strategy = strategyDao.findByIdAndCompanyIdAndStatus(strategyId, company.getId(), BSSApplicationConstants.STATUS_ACTIVE);
		if(!isDefault) {
			syncStrategyData(company, strategy.getId());
		}
		List<StrategyData> strategiesData = getStategiesData(company, false, realmPlanYear, List.of(strategy),isDefault);
		return strategiesData.get(0);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void preLoadBssStrategies(String peoId, String quarter, String emplId) {
		long startTime = System.currentTimeMillis();
		RealmPlanYear rpy = realmPlanYearDao.getMaxRealmPlanYearByQuarter(quarter);
		DateTime dtOrg = new DateTime();
		DateTime payrollDateTime = null;
		payrollDateTime = dtOrg.minusDays(14);
		StrategyPreLoadExecutorService executeService = new StrategyPreLoadExecutorService(peoId, quarter, rpy.getId(),
				payrollDateTime.toDate(), emplId);
		try {
			executeService.start();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("Exception occurred in method: preLoadBssStrategies({}, {}, {}). Reason: {}", peoId, quarter,
					emplId, e.getMessage());
		} catch (Exception ex) {
			throw new BSSApplicationException(ex,
					new BSSApplicationError(BSSErrorResponseCodes.BSS_STRATEGY_PRE_LOAD_FAIL,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, CLASS_NAME, "PRE LOAD STRATEGIES FAILED", null, null));
		}
		long endTime = System.currentTimeMillis();
		logger.info("submitDefaultStrategy(): LOADING DATA TO PS TOOK : {} ms", (endTime - startTime));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void preLoadBssStrategies(Company company) {
		long startTime = System.currentTimeMillis();
		StrategyPreLoadClientExecutorService executeService = new StrategyPreLoadClientExecutorService(company);
		try {
			executeService.start();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("Exception occurred in method: preLoadBssStrategies({}). Reason: {}", company.getCode(),
					e.getMessage());
		} catch (Exception ex) {
			throw new BSSApplicationException(ex,
					new BSSApplicationError(BSSErrorResponseCodes.BSS_STRATEGY_PRE_LOAD_FAIL,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, CLASS_NAME, "PRE LOAD CLIENT STRATEGIES FAILED", null,
							null));
		}
		long endTime = System.currentTimeMillis();
		logger.info("preLoadBssStrategies(): PRE LOADING THE COMPANY TOOK : {} ms", (endTime - startTime));
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteStrategy(Company company, long strategyId) {
		Strategy strategy = strategyDao.getReferenceById(strategyId);
		Set<Long> submitPendingStrategyIds = findSubmitPendingStrategyIds(company);
		boolean canDelete = StrategyServiceHelper.isStrategyDeletable(strategy, submitPendingStrategyIds, false);
		if (canDelete) {
			deleteExistingStrategies(new HashSet<Long>(Arrays.asList(strategyId)));
		} else {
			logger.info("Strategy {} ID: {} cannot be deleted", strategy.getName(), strategyId);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void updateStrategyName(long strategyId, String strategyName) {
		Strategy strategy = strategyDao.getReferenceById(strategyId);
		boolean canDelete = StrategyServiceHelper.isStrategyNameEditable(strategy, false);
		if (canDelete) {
			strategyDao.updateStrategyName(strategyId, strategyName);
			String cacheKey = CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE,
					String.valueOf(strategy.getId()));
			cacheTemplateService.deleteFromCache(Set.of(cacheKey));
		} else {
			logger.info("Strategy {} ID: {} cannot be updated", strategy.getName(), strategyId);
		}
	}
	
	@Override
	public int getStrategiesHistoryCount(String companyCode, long realmYrId) {
		return strategyDataDao.getStrategiesHistoryCount(companyCode, realmYrId);
	}
	
	@Override
	public List<Strategy> getAllSubmittedStrategiesByCompanyCode(String code) {
		return strategyDao.findSubmittedStrategiesByCompanyCode(code);
	}
	
	@Override
	public boolean hasSubmittedStrategy(long companyId) {
		int submittedCount = strategyDataDao.getSubmittedStrategiesCount(companyId);
		return (submittedCount > 0);
	}
	
	@Override
	@Transactional
	public void syncStrategiesForBenOfferException(BenOfferExceptionDto dto,
			Set<String> planTypeCodes) {
		boolean isHistory = false;
		Company company = companyService.getCompanyDetails(dto.getCompanyCode(), isHistory,
				BSSSecurityUtils.getAuthenticatedPersonId(), null);
		Date planStartDate = Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT);
		boolean isPlanStartDateInRange = CommonUtils.checkIfDateIsInRangeInclusive(planStartDate, dto.getStartDate(),
				dto.getEndDate());
		if (isPlanStartDateInRange) {
			List<Strategy> strategies = getAllStrategies(company.getId());
			Set<Long> strategyIds = strategies.stream().map(Strategy::getId).collect(Collectors.toSet());
			planSelectionDao.deleteByStrategyIdInAndPlanTypeIn(strategyIds, planTypeCodes);
			strategyFundingModelDao.deleteByStrategyIdInAndPlanTypeIn(strategyIds, planTypeCodes);
			if (planTypeCodes.contains(PlanTypesEnum.MEDICAL.getCode())) {
				strategyHsaFundingDao.deleteByStrategyIdIn(strategyIds);
			}
		}
	}
	
	@Override
	public List<Strategy> findBy(String companyCode) {
		return strategyDao.findBy(companyCode);
	}
	
	@Override
	public void resetStrategiesBy(String companyCode, long companyId, long realmPlanYearId, Set<Long> strategyIds) {
		resetCompanyStrategies(companyCode, companyId, realmPlanYearId, strategyIds);
	}
	
	public void setCompanyService(CompanyService companyService) {
		this.companyService = companyService;
	}
	
	public void setStrategyDataDao(StrategyDataDao strategyDataDao){
		this.strategyDataDao = strategyDataDao;
	}
	
	public void setStrategyDao(StrategyDao strategyDao){
		this.strategyDao = strategyDao;
	}
	
	public void setNextBenProgram(NextBenProgram nextBenProgram){
		this.nextBenProgram = nextBenProgram;
	}
	public void setNextRateTblID(NextRateTblID nextRateTblID){
		this.nextRateTblID = nextRateTblID;
	}
	public void setSpGetNextEligRulesId(GetNextEligRulesId spGetNextEligRulesId){
		this.spGetNextEligRulesId = spGetNextEligRulesId;
	}
	public void setBenefitGroupService(BenefitGroupService benefitGroupService){
		this.benefitGroupService = benefitGroupService;
	}
	public void setPlanSelectionService(PlanSelectionService planSelectionService){
		this.planSelectionService = planSelectionService;
	}
	public void setStrategyFundingModelService(StrategyFundingModelService strategyFundingModelService){
		this.strategyFundingModelService = strategyFundingModelService;
	}
	public void setRealmDataDao(RealmDataDao realmDataDao){
		this.realmDataDao = realmDataDao;
	}
	public void setRealmPlanYearService(RealmPlanYearService realmPlanYearService) {
		this.realmPlanYearService = realmPlanYearService;
	}
	public void setPortfolioRuleDao(PortfolioRuleDao portfolioRuleDao) {
		this.portfolioRuleDao =portfolioRuleDao;		
	}
	public void setContributionService(ContributionService contributionService) {
		this.contributionService =contributionService;		
	}

	@Override
	public List<Strategy> getAllStrategies(long companyId) {
		return strategyDao.findByCompanyIdAndStatus(companyId, BSSApplicationConstants.STATUS_ACTIVE);
	}
	
	@Override
	public Optional<Strategy> findById(long strategyId) {
		return Optional.of(strategyDao.findById(strategyId));
	}

	@Override
	public List<Strategy> findByCompanyIdAndSubmitted(long companyId, boolean submitted) {
		return strategyDao.findByCompanyIdAndSubmitted(companyId, submitted);
	}

	@Override
	public void updateStrategiesStatus(List<Long> strategyIds, String status) {
		strategyDao.updateStrategiesStatus(strategyIds, status);
	}

	@Override
	public void updateSubmittedStrategyDetails(long strategyId) {
		Strategy strategy = strategyDao.findById(strategyId);
        strategy.setName(BSSApplicationConstants.PROPOSED_STRATEGY_NAME);
        strategy.setType(BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED);
        strategy.setSubmitDate(null);
        strategy.setSubmitted(false);
        strategy.setAcaFplOpted(1);
		strategyDao.save(strategy);
	}

	/**
	 * This methods ensures that all the expected values for RATE_TBL_ID are
	 * present in the BenefitGroup and if any are missing, new values are
	 * allocated and added to the BenefitGroup
	 * @param group
	 * @return the same group, after modifying the GroupRate collection if needed
	 */
	private BenefitGroup completeRateTblSet(BenefitGroup group) {
		if (group.getGroupRate().isEmpty()) {
			GroupRate medicalRate = new GroupRate();
			medicalRate.setRateIdType( BSSRateType.MEDICAL.rateIdType() );
			GroupRatePK gpk = new GroupRatePK();
			gpk.setRateTblId(nextRateTblID.execute());
			medicalRate.setId(gpk);
			medicalRate.setBenefitGroup(group);
			group.getGroupRate().add(medicalRate);

			GroupRate medicalDpRate = new GroupRate();
			medicalDpRate.setRateIdType( BSSRateType.DP_MEDICAL.rateIdType() );
			GroupRatePK gpkDp = new GroupRatePK();
			gpkDp.setRateTblId(nextRateTblID.execute());
			medicalDpRate.setId(gpkDp);
			medicalDpRate.setBenefitGroup(group);
			group.getGroupRate().add(medicalDpRate);

			GroupRate otherRate = new GroupRate();
			otherRate.setRateIdType( BSSRateType.OTHER.rateIdType() );
			GroupRatePK gpkOther = new GroupRatePK();
			gpkOther.setRateTblId(nextRateTblID.execute());
			otherRate.setId(gpkOther);
			otherRate.setBenefitGroup(group);
			group.getGroupRate().add(otherRate);
		}
		return group;
	}
	
	private void resetCompanyStrategies(Company company, Set<Long> strategyIds) {
		deleteExistingStrategies(strategyIds);
		strategyDataDao.deleteGroupCovHeadCount(company.getId());
		strategyDataDao.deleteGroupRate(company.getId());
		strategyDataDao.deleteGroupByCompanyId(company.getId());
		strategyDataDao.deleteEmployees(company.getCode(), company.getRealmPlanYear().getId());
	}
	
	private void resetCompanyStrategies(String companyCode, long companyId, long realmPlanYearId,
			Set<Long> strategyIds) {
		deleteExistingStrategies(strategyIds);
		strategyDataDao.deleteGroupCovHeadCount(companyId);
		strategyDataDao.deleteGroupRate(companyId);
		strategyDataDao.deleteGroupByCompanyId(companyId);
		strategyDataDao.deleteEeDefaultPlanAssignmentsByCompanyId(companyId);
		strategyDataDao.deleteEmployees(companyCode, realmPlanYearId);
	}
	
	@Override
	public void deleteExistingStrategies(Set<Long> strategyIds) {
		if (!strategyIds.isEmpty()) {
			employeeStrategyGroupTransactionDao.deleteByStrategyIds(strategyIds);
			strategyDataDao.deleteEmployeeStrategyGroup(strategyIds);
			strategyDataDao.deleteStrategyGroupCovHeadCount(strategyIds);
			strategyDataDao.deleteStrategyGroup(strategyIds);
			strategyDataDao.deleteAllPlanContributionsByStrategy(strategyIds);
			strategyDataDao.deleteAllPlanSelectionsByStrategy(strategyIds);
			strategyDataDao.deleteStrategyFundDetailByStrategy(strategyIds);
			strategyDataDao.deleteStrategyFlatMaxByStrategy(strategyIds);
			strategyDataDao.deleteStrategyFundModelByStrategy(strategyIds);
			strategyDataDao.deleteStrategyEstimateList(strategyIds);
			strategyDataDao.deleteEePlanAssignmentsByStrategyIds(strategyIds);
			strategyDataDao.deleteStrategyById(strategyIds);
		}
	}
	
	private void populatePlanHeadCount(Map<String, PlanSelection> adPlanMap, ActiveEligibleEECount activeHeadCount,
			AdditionalBenefitPlan adPlan) {
		for (DisabilityBenefitOptionPlans plan : adPlan.getOptionPlans()) {
			if (null != activeHeadCount) {
				if (BSSApplicationConstants.STD_CODE.equals(plan.getPlanType())) {
					updateHeadCountForSTDPlan(activeHeadCount, plan);
				} else {
					updateHeadCountForNonSTDPlan(activeHeadCount, plan);
				}
			} else {
				if (null != adPlanMap.get(plan.getId())) {
					PlanSelection ps = adPlanMap.get(plan.getId());
					plan.setPlanHeadCount(ps.getHeadCount());
				}
			}
		}
	}

	private void updateHeadCountForSTDPlan(ActiveEligibleEECount activeHeadCount, DisabilityBenefitOptionPlans plan) {
		if (plan.isPrimaryPlan()) {
			if (activeHeadCount.getTotalHeadCount() == 0) {
				plan.setPlanHeadCount(Long.valueOf(activeHeadCount.getPrimaryHeadCount()));
			} else {
				plan.setPlanHeadCount(Long.valueOf(activeHeadCount.getTotalHeadCount()));
			}
		} else {
			if (activeHeadCount.getTotalHeadCount() == 0) {
				plan.setPlanHeadCount(Long.valueOf(activeHeadCount.getSecondaryHeadCount()));
			} else {
				plan.setPlanHeadCount(Long.valueOf(activeHeadCount.getTotalHeadCount()));
			}
		}
	}
	
	private void updateHeadCountForNonSTDPlan(ActiveEligibleEECount activeHeadCount, DisabilityBenefitOptionPlans plan) {
		if (activeHeadCount.getTotalHeadCount() == 0) {
			plan.setPlanHeadCount(Long.valueOf(activeHeadCount.getPrimaryHeadCount())
					+ Long.valueOf(activeHeadCount.getSecondaryHeadCount()));
		} else {
			plan.setPlanHeadCount(Long.valueOf(activeHeadCount.getTotalHeadCount()));
		}
	}
	
	private boolean getBaseFundingRequired(String benefitProgram, String planTypeName,
			Map<String, Set<String>> eecFundingMap) {
		boolean baseFundingRequired = true;
		if (eecFundingMap != null && eecFundingMap.containsKey(benefitProgram)
				&& eecFundingMap.get(benefitProgram).contains(PlanTypesEnum.getCode(planTypeName))) {
			baseFundingRequired = false;
		}
		return baseFundingRequired;
	}
	
	private Set<Long> findSubmitPendingStrategyIds(Company company) {
		List<SubmitStatus> submitStatuses = submitStatusService.findByCompanyAndPlanYearIdAndStatuses(company,
				SUBMIT_PENDING_STATUS);
		return submitStatuses.stream().map(SubmitStatus::getStrategyId).collect(Collectors.toSet());
	}
	
	public void updateStrategyBudget(long strategyId, StrategyBudget budget) {
		strategyDao.updateStrategyBudget(strategyId, budget.getBudget(), Integer.parseInt(budget.getBudgetFactor()));
	}
	
	/**
	 * Verify if the passed in strategy data is submittable.
	 * 
	 * If the company has an active service order (which by definition means the
	 * user is a CDCM/TMT user) and the effective date of the strategy does not
	 * equal the company's benefit start date or the current plan year start date,
	 * get the current year's submitted strategy.
	 * 
	 * If there is a previously submitted strategy, the updated strategy is not
	 * submittable if there are any plan changes to the LTD/STD or Disability plan
	 * offering.
	 * 
	 * 
	 * @param dto
	 * @param company
	 * @return boolean value indicating if the strategy is submittable
	 */
	private boolean isStrategySubmittable(StrategyData dto, Company company) {

		boolean isSubmittable = true;

		if (company.isActiveServiceOrder()
				&& !((Utils.convertStringToDate(company.getBenefitStartDate(), Constants.DATE_FORMAT))
						.equals(Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT)))
				&& !(company.getRealmPlanYear().getPlanYearStart()
						.equals(Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT)))) {
			isSubmittable = validateLifeAndDisabilityChanges(dto, company);
		}

		return isSubmittable;
	}
	
	private boolean validateLifeAndDisabilityChanges(StrategyData dto, Company company) {

		// Get current active benefit programs from PS
		List<String> benefitPrograms = new ArrayList<>();
		renewalDataDao
				.getBenefitPrograms(company.getPfClient(),
						Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT))
				.stream().forEach(group -> benefitPrograms.add(group.getBenefitProgram()));

		// Get currently submitted strategy
		List<Strategy> strategies = strategyDataDao.getHistoryStrategies(company.getCode(),
				company.getRealmPlanYear().getId());

		if (!strategies.isEmpty()) {
			// Get Additional Benefit information for previously submitted strategy
			Map<Long, List<AdditionalBenefitPlan>> strategyGroupAdditionalOfferingMap = strategyDataDao
					.getAdditionalBenefitPlansForStrategy(strategies.get(0).getId(), company.getPlanStartDate());

			for (StrategyBenefitGroup groupData : dto.getBenefitGroups()) {

				// If the group exists in the currently submitted strategy and it is not a K1,
				// test for differences
				// Else proceed
				if (benefitPrograms.contains(groupData.getBenefitProgram())
						&& !(BSSApplicationConstants.K1_GROUP_TYPE.equals(groupData.getType()))) {
					List<String> currentPlans = getAdditionalBenefitPlansFromGroup(
							strategyGroupAdditionalOfferingMap.get(groupData.getId()));
					List<String> submittedPlans = getAdditionalBenefitPlansFromBenefitOffers(
							groupData.getBenefitOffers());

					if (!currentPlans.equals(submittedPlans)) {
						return false;
					}
				}

			}
		}
		return true;
	}

	private List<String> getAdditionalBenefitPlansFromGroup(List<AdditionalBenefitPlan> offering) {
		List<String> benefitPlans = new ArrayList<>();
		if (offering != null) {
			for (AdditionalBenefitPlan additionalBenefitPlan : offering) {
				if (BSSApplicationConstants.getLifePlanTypes().contains(additionalBenefitPlan.getPlanType())
						|| BSSApplicationConstants.getDisabilityPlanTypes()
								.contains(additionalBenefitPlan.getPlanType())) {
					benefitPlans.add(additionalBenefitPlan.getId());
				}
			}
			Collections.sort(benefitPlans);
		}
		return benefitPlans;
	}

	private List<String> getAdditionalBenefitPlansFromBenefitOffers(List<BenefitOffer> offering) {
		List<String> benefitPlans = new ArrayList<>();
		if (offering != null) {
			List<AdditionalBenefitPlan> additionalOfferings = offering.stream()
					.flatMap(benefitOffer -> benefitOffer.getAdditionalBenefitOffers().stream())
					.flatMap(additionalOffering -> additionalOffering.getAdditionalBenefitPlans().stream())
					.collect(Collectors.toList());

			for (AdditionalBenefitPlan additionalPlan : additionalOfferings) {
				if (additionalPlan.getOptionPlans() != null) {
					additionalPlan.getOptionPlans().stream()
				    .filter(optionPlan -> isLifeOrDisabilityPlan(optionPlan.getPlanType()))
				    .map(DisabilityBenefitOptionPlans::getId)
				    .forEach(benefitPlans::add);
				} else if (isLifeOrDisabilityPlan(additionalPlan.getPlanType())) {
					benefitPlans.add(additionalPlan.getId());
				}
			}

			Collections.sort(benefitPlans);
		}
		return benefitPlans;
	}
	
	private boolean isLifeOrDisabilityPlan(String planType) {
	    return BSSApplicationConstants.getLifePlanTypes().contains(planType) ||
	           BSSApplicationConstants.getDisabilityPlanTypes().contains(planType);
	}

	@Override
	public String getPrimaryCarrierName(Company company, String tnStrategyId) {
		return strategyDataDao.getPrimaryCarrierName(company, tnStrategyId);
	}

	@Override
	public StrategyCostRes getStrategyCostByPlanType(Company company, long strategyId) {
		StrategyCostRes strategyCosts = new StrategyCostRes();
		strategyCosts.setCostSummary(new ArrayList<>());
		strategyCosts.getCostSummary().addAll(getHealthCostsByPlanType(strategyId));
		strategyCosts.getCostSummary().addAll(getAdditionalBenefitCostsByPlanType(strategyId));
		populateMissingPlanTypes(strategyCosts, company.getRealmPlanYear().getId());

		return strategyCosts;
	}

	private List<StrategyCostRes.PlanTypeCost> getHealthCostsByPlanType(Long strategyId) {
		List<Object[]> strategyCosts = strategyDataDao.getHealthCostsByPlanType(strategyId);
		List<StrategyCostRes.PlanTypeCost> planCosts = new ArrayList<>();
		for (Object[] strategyCost : strategyCosts) {
			planCosts.add(
					createPlanTypeCost((String) strategyCost[0],true, (BigDecimal) strategyCost[1], (BigDecimal) strategyCost[2]));
		}
		return planCosts;
	}

	private List<StrategyCostRes.PlanTypeCost> getAdditionalBenefitCostsByPlanType(Long strategyId) {
		List<Object[]> strategyCosts = strategyDataDao.getAdditionalBenefitCostsByPlanType(strategyId);
		List<StrategyCostRes.PlanTypeCost> planCosts = new ArrayList<>();
		for (Object[] strategyCost : strategyCosts) {
			planCosts.add(
					createPlanTypeCost((String) strategyCost[0],true, BigDecimal.ZERO, (BigDecimal) strategyCost[1]));
		}
		return planCosts;
	}


	private void populateMissingPlanTypes(StrategyCostRes strategyCosts, long realmPlanYearId) {
		Set<String> planTypesToInclude = getBasePlanTypeNamesOfferedInRealmPlanYear(realmPlanYearId);
		planTypesToInclude.forEach(planType -> {
			if (!strategyCosts.getCostSummary().stream().anyMatch(cost -> cost.getBenefitType().equals(planType))) {
				strategyCosts.getCostSummary().add(createPlanTypeCost(planType, false, BigDecimal.ZERO, BigDecimal.ZERO));
			}
		});
	}

	private StrategyCostRes.PlanTypeCost createPlanTypeCost(String benefitType, boolean offered, BigDecimal monthlyEeCost, BigDecimal monthlyErCost) {
		return StrategyCostRes.PlanTypeCost.builder()
				.benefitType(benefitType)
				.offered(offered)
				.monthlyEeCost(monthlyEeCost)
				.monthlyErCost(monthlyErCost)
				.monthlyTotalCost(monthlyEeCost.add(monthlyErCost))
				.build();
	}

	private Set<String> getBasePlanTypeNamesOfferedInRealmPlanYear(long realmPlanYearId) {
		Set<String> planTypesInRealmPlanYear =  strategyDataDao.getRealmPlanTypes(realmPlanYearId);
		Set<String> planTypes = new HashSet<>();
		for (String planType : planTypesInRealmPlanYear) {
			if (BSSApplicationConstants.MEDICAL_PLAN_TYPES.contains(planType)) {
				planTypes.add(BSSApplicationConstants.MEDICAL);
			} else if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(planType)) {
				planTypes.add(BSSApplicationConstants.DENTAL);
			} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(planType)) {
				planTypes.add(BSSApplicationConstants.VISION);
			} else if (BSSApplicationConstants.DISABILITY_PLAN_TYPES.contains(planType)) {
				planTypes.add(BSSApplicationConstants.DISABILITY);
			} else if (BSSApplicationConstants.LIFE_CODE.equals(planType)) {
				planTypes.add(BSSApplicationConstants.LIFE);
			}
		}
		return planTypes;
	}

	private BigDecimal getEstimatedTotalCost(Company company, String planType, long strategyId, long groupId) {
		BigDecimal estimatedTotalCost = null;
		if (CompanyServiceHelper.isTibProspect(company)) {
			Map<String, BigDecimal> strategyEstimateResults = strategyDataDao.getStrategyGroupEstimateByPlanType(strategyId, groupId);
			if (strategyEstimateResults != null && !strategyEstimateResults.isEmpty()) {
				estimatedTotalCost = strategyEstimateResults.get(PlanTypesEnum.getCode(planType));
			}
		}
		return estimatedTotalCost;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteStrategies(long companyId) {
		logger.info("deleteStrategies() - deleting all strategies for companyId: {}", companyId);
		hrpDao.deleteStrategiesByCompanyId(companyId);
		logger.info("deleteStrategies() - completed for companyId: {}", companyId);
	}

}
