package com.trinet.ambis.service.impl;

import static com.trinet.ambis.util.Constants.voluntaryPlanTypeList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.service.BenefitPlanService;
import com.trinet.ambis.service.ProspectPlanAvailabilityService;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.CommonDataDao;
import com.trinet.ambis.persistence.dao.hrp.EmployerEmployeePlansMappingDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.TemplateFundingDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.BenefitCategoriesService;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.PortfolioService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.BenefitsCategories;
import com.trinet.ambis.service.model.BenefitsCategory;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.FundingType;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.model.SelectItem;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.StrategyUtils;
import com.trinet.ambis.util.Utils;

/**
 * @author rvutukuri
 *
 */
@Service
public class BenefitCategoriesServiceImpl implements BenefitCategoriesService {
	
	@Autowired
	private RealmPlyrPlanService realmPlyrPlanService;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private PlanRatesService planRatesService;
	@Autowired
	private BenefitOfferExceptionService benOfferExceptionService;
	@Autowired
	private RealmDataDao realmDataDao;
	@Autowired
	private PsCompanyDao psCompanyDao;
	@Autowired
	private TemplateFundingDao templateFundingDao;
	@Autowired
	private EmployerEmployeePlansMappingDao employerEmployeePlansMappingDao;
	@Autowired
	private XbssRealmPlyrPlanDao realmPlyrPlanDao;
	@Autowired
	private CommonDataDao commonDataDao;
	@Autowired
	private BenefitPlanDao benefitPlanDao;
	@Autowired
	private ProspectPlanAvailabilityService prospectPlanAvailabilityService;
	@Autowired
	private BenefitPlanService benefitPlanService;

	@Override
	public BenefitsCategories constructBenefitsCategories(Company company) {
		Map<String, List<CoverageLevel>> coverageLevelsMap = realmDataDao
				.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes, company.getRealmPlanYearId());

		// Get plan/properties mapping for band locator/band code lookup
		Map<String,XbssRealmPlyrPlan> plyrPlanMap = realmPlyrPlanService.getMapForRealmPlanYear( company.getRealmPlanYear().getId() );
		
		// getting ER/EE mappings
		Map<String, String> erEEPlansMapping = employerEmployeePlansMappingDao
				.getEeAndErPlanMapping(company.getRealmPlanYearId());

		// excluding IS mandated plans from minimum funding calculations
		List<String> mandatoryPlansToExclude = realmPlyrPlanDao.getAllMandatoryPlansExcludingGivenRegion(
				company.getHeadQuatersState(), BigDecimal.valueOf(company.getRealmPlanYearId()));

		// applicable regions.
		Map<String, Set<PlanCarrier>> planCarrierMap = portfolioService.findPrimaryPlanCarriers(company);

		Set<String> primaryPlanCarriers = BenefitCategoriesHelper.getPlanCarriers(planCarrierMap);
		Set<String> outOfRegionPlans = CommonServiceHelper.getOutOfRegionPlansToExclude(company, primaryPlanCarriers,
				realmDataDao);

		Map<String, Set<StateBenefitPlan>> primaryPlanMap = benefitPlanDao.getAllPrimaryBenefitPlans(primaryPlanCarriers, company, outOfRegionPlans);
		Set<String> medicalPlans = BenefitCategoriesHelper.getAllBenefitPlans(primaryPlanMap);
		Set<String> widelyAvailablePlanSet = benefitPlanDao.getWidelyAvailablePlans(medicalPlans, company.getRealmPlanYearId());
		
		Map<String, List<String>> benefitPlansStatesMap = null;
		if (CollectionUtils.isNotEmpty(medicalPlans)) {
			benefitPlansStatesMap = realmDataDao.getBenefitsPlans(company.getRealmPlanYearId(), medicalPlans);
		}

		// Get all PlanRates for realmYearId
		Map<String, List<BenefitPlanRate>> planRates = planRatesService.getBenefitPlanRatesBy(company);

		Map<String, List<PlanPackage>> planPackagesMap = null;
		Map<String, Set<BenefitPlan>> planPackagePlans = null;
		
		// Get all autoselectionPlan mappings
		Map<String, Map<String, Set<String>>> autoSelectPlans = realmDataDao
				.getAutoSelectPlansByRealmIdAndPlanTypes(company.getRealmPlanYearId(), company,
						outOfRegionPlans);

		boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions( company );

		if (!company.isProspectCompany()) {
			setDefaultFunding(company);
			planPackagesMap = BenefitCategoriesHelper.getDefaultPlanPackage(company, coverageLevelsMap);
		}
		
		List<CarrierMinimumFunding> lowestCostPlanPerCarrier = benefitPlanService.getLowestCostPlanPerCarrier(company);
		
		BenefitsCategories benefitsCategories = new BenefitsCategories();
		Map<String, Set<String>> primaryPlanCarriersMap = BenefitCategoriesHelper
				.getPlanCarriersByPlanType(planCarrierMap);
		Map<String, Boolean> benOfferExceptions = benOfferExceptionService.findApplicableBy(company);
		for (String offerType : primaryPlanMap.keySet()) {
			if (Boolean.TRUE.equals(CommonUtils.isBenOfferExceptionAvailable(benOfferExceptions, offerType))) {
				continue;
			}
			if (MapUtils.isNotEmpty(primaryPlanCarriersMap)) {
				StrategyServiceHelper.updateBenfitPlanRegions(primaryPlanMap.get(offerType), benefitPlansStatesMap);
			}

			BenefitsCategory offerCategory = new BenefitsCategory();
			List<String> benefitPlanIds = new ArrayList<>();
			// constructing the offer type.
			constructBenefitsCategory(company, primaryPlanMap.get(offerType), planRates, plyrPlanMap,
					coverageLevelsMap.get(offerType), autoSelectPlans.get(offerType), widelyAvailablePlanSet,
					mandatoryPlansToExclude, offerType, erEEPlansMapping, offerCategory,
					primaryPlanCarriersMap.get(offerType), planCarrierMap.get(offerType), benefitPlanIds, lowestCostPlanPerCarrier);

			// adding the plan packages
			Set<PlanPackage> planPackages = new HashSet<>();
			if(planPackagesMap!=null && !planPackagesMap.isEmpty())
				planPackages.addAll(planPackagesMap.get(offerType));

			// updating the plan packages for pick and choose
			if (!company.isRenewalCompany() && ! isPickChoose ) {
				BenefitCategoriesHelper.updatePlanPackagesPickAndChoose(planPackages, benefitPlanIds);
			}
			// adding offer type data to the benefit categories object.
			offerCategory.setPlanPackages(planPackages);
			if (BSSApplicationConstants.MEDICAL.equals(offerType)) {
				List<SelectItem> getBsuppExcessOptions = commonDataDao.getBsuppExcessOptions();
				List<SelectItem> bsuppVoluntaryPlanTypes = commonDataDao
						.getBsuppVolPlanTypes(company.getRealm().getId());
				if (CollectionUtils.isNotEmpty(getBsuppExcessOptions)) {
					offerCategory.getBsuppExcessOptions().addAll(getBsuppExcessOptions);
				}
				if (CollectionUtils.isNotEmpty(bsuppVoluntaryPlanTypes)) {
					offerCategory.getBsuppVoluntaryPlanTypes().addAll(bsuppVoluntaryPlanTypes);
				}
				benefitsCategories.setMedical(offerCategory);
			}
			if (BSSApplicationConstants.DENTAL.equals(offerType)) {
				benefitsCategories.setDental(offerCategory);
			}
			if (BSSApplicationConstants.VISION.equals(offerType)) {
				benefitsCategories.setVision(offerCategory);
			}
		}

		if (CompanyServiceHelper.isTibProspect(company)) {
			BenefitsCategory benefitsCategoryMedical = constructOmsBenefitsCategory(company, coverageLevelsMap.get(BSSApplicationConstants.MEDICAL), BSSApplicationConstants.MEDICAL);
			benefitsCategories.setMedical(benefitsCategoryMedical);
			BenefitsCategory benefitsCategoryDental = constructOmsBenefitsCategory(company, coverageLevelsMap.get(BSSApplicationConstants.DENTAL), BSSApplicationConstants.DENTAL);
			benefitsCategories.setDental(benefitsCategoryDental);
			BenefitsCategory benefitsCategoryVision = constructOmsBenefitsCategory(company, coverageLevelsMap.get(BSSApplicationConstants.VISION), BSSApplicationConstants.VISION);
			benefitsCategories.setVision(benefitsCategoryVision);
		}

		return benefitsCategories;

	}

	/**
	 * 
	 * @param company
	 * @param stateBenefitPlans
	 * @param planRates
	 * @param coverageLevels
	 * @param autoSelectPlans
	 * @param ppoPlanMap
	 * @param mandatoryPlansToExclude
	 * @param benefitOfferType
	 * @param erEEPlansMapping
	 * @param offerCategory
	 */
	private void constructBenefitsCategory(Company company, Set<StateBenefitPlan> stateBenefitPlans,
			Map<String, List<BenefitPlanRate>> planRates, Map<String,XbssRealmPlyrPlan> plyrPlanMap, List<CoverageLevel> coverageLevels,
			Map<String, Set<String>> autoSelectPlans, Set<String> widelyAvailablePlanSet,
			List<String> mandatoryPlansToExclude, String benefitOfferType, Map<String, String> erEEPlansMapping,
			BenefitsCategory offerCategory, Set<String> planCarriers, Set<PlanCarrier> planCarriersSet,
			List<String> benefitPlanIds, List<CarrierMinimumFunding> lowestCostPlanPerCarrier) {
		Set<BenefitPlan> benefitPlansToAdd = new TreeSet<>();
		List<String> missingRates = new ArrayList<>();
		for (StateBenefitPlan statePlan : stateBenefitPlans) {
			if (planCarriers.contains(String.valueOf(statePlan.getPortfolioId()))) {
				BenefitPlan bp = createBenefitPlan(statePlan, planRates, plyrPlanMap, coverageLevels, autoSelectPlans,
						widelyAvailablePlanSet, missingRates, mandatoryPlansToExclude, erEEPlansMapping );
				benefitPlansToAdd.add(bp);
				benefitPlanIds.add(bp.getId());
			}
		}
		offerCategory.setPlanCarriers(planCarriersSet);
		offerCategory.setCoverageLevels(coverageLevels);
		offerCategory.setBenefitPlans(benefitPlansToAdd);
		
		// Set the planCarrier -> minimum funding map to offerCategory.So that UI can
		// use it for min funding calculation.
		if (CollectionUtils.isNotEmpty(lowestCostPlanPerCarrier)) {
			Map<Long, BigDecimal> minFunding = BenefitCategoriesHelper.calculateCarrierMinFunding(company,
					benefitOfferType, lowestCostPlanPerCarrier);
			offerCategory.setMinFunding(minFunding);
		}
	}

	/**
	 * 
	 * @param statePlan
	 * @param company
	 * @param planRates
	 * @param coverageLevels
	 * @param autoSelectPlans
	 * @param ppoPlanMap
	 * @param planRatePlans
	 * @param mandatoryPlansToExclude
	 * @param benefitOfferType
	 * @param erEEPlansMapping
	 * @return
	 */
	private BenefitPlan createBenefitPlan(StateBenefitPlan statePlan,
			Map<String, List<BenefitPlanRate>> planRates, Map<String,XbssRealmPlyrPlan> plyrPlanMap, List<CoverageLevel> coverageLevels,
			Map<String, Set<String>> autoSelectPlans, Set<String> widelyAvailablePlanSet, List<String> planRatePlans,
			List<String> mandatoryPlansToExclude, Map<String, String> erEEPlansMapping ) {
		BenefitPlan plan = new BenefitPlan();
		plan.setPlanType(statePlan.getPlanType());
		plan.setName(statePlan.getDescription());
		plan.setId(statePlan.getBenefitPlan());
		plan.setHighDeductible(false);
		if (plyrPlanMap.containsKey(plan.getId())) {
			plan.setHighDeductible(plyrPlanMap.get(plan.getId()).isHighDeductible());
		}
		plan.setPremium(Utils.isPremium(plan.getId()));
		plan.setEmployeePaid(voluntaryPlanTypeList.contains(plan.getPlanType()));
		plan.setMandatory(statePlan.isMandatory());
		if ( BSSApplicationConstants.FPL.equals( statePlan.getPlanCategory() )) {
			plan.setMandatory( true );
		}
		plan.setPlanCarrierId(statePlan.getPortfolioId());
		plan.setPlanCategory(statePlan.getPlanCategory());
		if (null != statePlan.getOfferedStates()) {
			Collections.sort(statePlan.getOfferedStates());
			plan.setOfferedStates(statePlan.getOfferedStates());
		} else {
			List<String> getOfferedStates = new ArrayList<>();
			getOfferedStates.add("All");
			plan.setOfferedStates(getOfferedStates);
		}
		plan.setMandatoryExcluded(mandatoryPlansToExclude.contains(statePlan.getBenefitPlan()));
		if (autoSelectPlans != null && autoSelectPlans.containsKey(statePlan.getBenefitPlan())) {
			plan.setCrossRefPlans(autoSelectPlans.get(statePlan.getBenefitPlan()));
		}
		plan.setPpoPlan(widelyAvailablePlanSet.contains(plan.getId()));
		plan.setWidelyAvailablePlan(widelyAvailablePlanSet.contains(plan.getId()));
		
		if (null != erEEPlansMapping && null != erEEPlansMapping.get(plan.getId())) {
			plan.setOptionalPlans(erEEPlansMapping.get(plan.getId()));
		}
		// creating contributions
		List<PlanContribution> contributions = createContributions(statePlan.getBenefitPlan(),
				planRates, coverageLevels, planRatePlans);
		plan.setContributions(contributions);

		return plan;
	}

	/**
	 * 
	 * @param benefitPlanId
	 * @param planType
	 * @param company
	 * @param planRate
	 * @param vendorId
	 * @param coverageLevels
	 * @param planRatePlans
	 * @return
	 */
	private List<PlanContribution> createContributions(String benefitPlanId,
			Map<String, List<BenefitPlanRate>> planRate, List<CoverageLevel> coverageLevels,
			List<String> planRatePlans ) {
		List<PlanContribution> planContributions = new ArrayList<>();

        Map<String, BigDecimal> planCostMap;
        planCostMap = StrategyUtils.getPlanCost(planRate.get(benefitPlanId));
		for (CoverageLevel cc : coverageLevels) {
			if (cc.getId().equals("all")) {
				continue;
			}
			PlanContribution planContribution = new PlanContribution();
			planContribution.setType(cc.getId());
			planContribution.setHeadcount(0);
			planContribution.setHsaHeadcount(0);
			planContribution.setBenefitPlanId(benefitPlanId);
			BigDecimal planCost = planCostMap.get(cc.getId());
			if (planCost != null) {
				planContribution.setPlanCost(planCost);
			} else {
				planRatePlans.add(benefitPlanId);
			}
			planContribution.setBenefitPlanId(benefitPlanId);
			planContribution.setOverrideType("BASE");
			planContributions.add(planContribution);
		}
		return planContributions;
	}

	/**
	 * 
	 * @param company
	 */
	private void setDefaultFunding(Company company) {
		List<FundingType> fundingTypes = realmDataDao.getRealmFundingTypes(company.getRealmPlanYearId());
		for (FundingType ft : fundingTypes) {
			if (ft.isDefaultFunding()) {
				company.setDefaultFundingType(ft.getId());
			}
		}
	}

	private BenefitsCategory constructOmsBenefitsCategory(Company company, List<CoverageLevel> coverageLevels, String benefitType) {
		BenefitsCategory benefitsCategory = new BenefitsCategory();
		List<HrisPlanResponse> availablePlans = prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, benefitType);

		benefitsCategory.setCoverageLevels(coverageLevels);
		benefitsCategory.setPlanCarriers(constructAllTibCarriers(availablePlans));
		benefitsCategory.setBenefitPlans(constructAllOmsPlans(availablePlans, benefitType));
		return benefitsCategory;
	}

	private Set<PlanCarrier> constructAllTibCarriers(List<HrisPlanResponse> availablePlans) {
		Map<Integer, PlanCarrier> planCarriers = availablePlans.stream()
				.collect(Collectors.toMap(HrisPlanResponse::getCarrierId,
						planResponse -> new PlanCarrier(planResponse.getCarrierId(), planResponse.getCarrierName(), null),
						(existing, replacement) -> existing));

		return planCarriers.values().stream().collect(Collectors.toSet());
	}

	private Set<BenefitPlan> constructAllOmsPlans(List<HrisPlanResponse> availablePlans, String benefitType) {
		Set<BenefitPlan> benefitPlans = new HashSet<>();
		for (HrisPlanResponse planResponse : availablePlans) {
			BenefitPlan benefitPlan = new BenefitPlan();
			benefitPlan.setId(String.valueOf(planResponse.getPlanId()));
			benefitPlan.setPlanType(benefitType);
			benefitPlan.setPlanCarrierId((long) planResponse.getCarrierId());
			benefitPlan.setName(planResponse.getPlanName());
			benefitPlans.add(benefitPlan);
		}
		return benefitPlans;
	}
}
