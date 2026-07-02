package com.trinet.ambis.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.MinFundExceptionService;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.FundingBasePlan;
import com.trinet.ambis.service.model.MinimumFunding;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.util.Constants;


/**
 * @author rvutukuri
 *
 */
public class BenefitCategoriesHelper {

	// class contains static helper methods and no objects should be created
	private BenefitCategoriesHelper() {}

	/**
	 * This method is for getting all the primary plan carriers as a set for the
	 * Map.
	 * 
	 * @param planCarrierMap
	 * @return
	 */
	public static Set<String> getPlanCarriers(Map<String, Set<PlanCarrier>> planCarrierMap) {
		Set<String> planCarriers = new HashSet<>();
		Set<PlanCarrier> primaryPlanCarriers = new HashSet<>();
		if (planCarrierMap.get(BSSApplicationConstants.MEDICAL) != null) {
			primaryPlanCarriers.addAll(planCarrierMap.get(BSSApplicationConstants.MEDICAL));
		}
		if (planCarrierMap.get(BSSApplicationConstants.DENTAL) != null) {
			primaryPlanCarriers.addAll(planCarrierMap.get(BSSApplicationConstants.DENTAL));
		}
		if (planCarrierMap.get(BSSApplicationConstants.VISION) != null) {
			primaryPlanCarriers.addAll(planCarrierMap.get(BSSApplicationConstants.VISION));
		}
		for (PlanCarrier pc : primaryPlanCarriers) {
			planCarriers.add(String.valueOf(pc.getId()));
		}
		return planCarriers;
	}
	
	/**
	 * 
	 * @param planCarrierMap
	 * @param selectedPortfolioId
	 * @return
	 */
	public static Set<String> getPlanCarriersProspect(Map<String, Set<PlanCarrier>> planCarrierMap,
			long selectedPortfolioId) {
		Set<String> planCarriers = new HashSet<>();
		Set<PlanCarrier> primaryPlanCarriers = new HashSet<>();
		if (planCarrierMap.get(BSSApplicationConstants.MEDICAL) != null) {
			for (PlanCarrier pc : planCarrierMap.get(BSSApplicationConstants.MEDICAL)) {
				if (!pc.isRestricted()) {
					primaryPlanCarriers.add(pc);
				} else {
					if (pc.getId() == selectedPortfolioId) {
						primaryPlanCarriers.add(pc);
					}
				}
			}
		}
		if (planCarrierMap.get(BSSApplicationConstants.DENTAL) != null) {
			primaryPlanCarriers.addAll(planCarrierMap.get(BSSApplicationConstants.DENTAL));
		}
		if (planCarrierMap.get(BSSApplicationConstants.VISION) != null) {
			primaryPlanCarriers.addAll(planCarrierMap.get(BSSApplicationConstants.VISION));
		}
		for (PlanCarrier pc : primaryPlanCarriers) {
			planCarriers.add(String.valueOf(pc.getId()));
		}
		return planCarriers;
	}
	
	/**
	 * 
	 * @param planCarrierMap
	 * @param selectedPortfolioId
	 * @return
	 */
	public static Map<String, Set<Long>>  getSelectedPlanCarriersProspect(Map<String, Set<PlanCarrier>> planCarrierMap,
			long selectedPortfolioId) {
		Map<String, Set<Long>> selectedPlancarriers = new HashMap<>();
		for (String benefitOffer : planCarrierMap.keySet()) {
			Set<Long> benofferCarriers = new HashSet<>();
			if (BSSApplicationConstants.MEDICAL.equals(benefitOffer)) {
				benofferCarriers.add(selectedPortfolioId);
				for (PlanCarrier pc : planCarrierMap.get(benefitOffer)) {
					if (!pc.isRestricted()) {
						if (null != pc.getParentId() && pc.getParentId().stream().filter(Objects::nonNull)
								.map(Long::parseLong).anyMatch(pid -> pid == selectedPortfolioId)) {
							benofferCarriers.add((long) pc.getId());
						}
						if (pc.isMandatory()) {
							benofferCarriers.add((long) pc.getId());
						}
					}
				}
			} else {
				for (PlanCarrier pc : planCarrierMap.get(benefitOffer)) {
					benofferCarriers.add((long) pc.getId());
				}
			}
			selectedPlancarriers.put(PlanTypesEnum.getCode(benefitOffer), benofferCarriers);

		}
		return selectedPlancarriers;
	}


	/**
	 * 
	 * @param planCarrierMap
	 * @return
	 */
	public static Set<String> getMedicalPlanCarriers(Map<String, Set<PlanCarrier>> planCarrierMap) {
		Set<String> medicalPlanCarriers = new HashSet<>();
		Set<PlanCarrier> primaryPlanCarriers = planCarrierMap.get(BSSApplicationConstants.MEDICAL);
		if (CollectionUtils.isNotEmpty(primaryPlanCarriers)) {
			for (PlanCarrier pc : primaryPlanCarriers) {
				medicalPlanCarriers.add(String.valueOf(pc.getId()));
			}
		}
		return medicalPlanCarriers;
	}

	/**
	 * For each of the medical, dental, and vision offer types, find and return a map of the
	 * mandatory carriers (portfolios).
	 * @param planCarrierMap
	 * @return
	 */
	public static Map<String, Set<Long>> getMandatoryPlanCarriers(Map<String, Set<PlanCarrier>> planCarrierMap) {
		Map<String, Set<Long>> mandatoryPlanCarriersByPlanType = new HashMap<>();

		Set<String> mdvSet = new HashSet<>( Arrays.asList(
						BSSApplicationConstants.MEDICAL,
						BSSApplicationConstants.DENTAL,
						BSSApplicationConstants.VISION ) );

		for( Map.Entry<String,Set<PlanCarrier>> planCarrierEntry : planCarrierMap.entrySet() ) {
			// If the entry offer type is one of the MDV types, AND the mapped Set is not null
			// then go get the mandatory carrier Set
			if( mdvSet.contains( planCarrierEntry.getKey() ) && planCarrierEntry.getValue() != null ) {
				Set<Long> pcs = captureMandatoryCarrierIds( planCarrierEntry.getValue() );
				mandatoryPlanCarriersByPlanType.put( planCarrierEntry.getKey(), pcs );
			}
		}
		return mandatoryPlanCarriersByPlanType;
	}

	private static Set<Long> captureMandatoryCarrierIds( Set<PlanCarrier> planCarrierSet ) {
		Set<Long> planCarrierIdSet = new HashSet<>();
		for( PlanCarrier pc : planCarrierSet ) {
			if( pc.isMandatory() ) {
				planCarrierIdSet.add( (long) pc.getId() );
			}
		}
		return planCarrierIdSet;
	}


	/**
	 * This method is for getting all the primary plan carriers as a set for the
	 * Map.
	 * 
	 * @param planCarrierMap
	 * @return
	 */
	public static Map<String, Set<String>> getPlanCarriersByPlanType(Map<String, Set<PlanCarrier>> planCarrierMap) {
		Map<String, Set<String>> planCarrierPlanTypeMap = new HashMap<>();

		for ( Map.Entry<String,Set<PlanCarrier>> planCarrierEntry : planCarrierMap.entrySet() ) {
			Set<String> offerCarrier = new HashSet<>();
			for( PlanCarrier pc : planCarrierEntry.getValue() ) {
				offerCarrier.add( String.valueOf( pc.getId() ));
			}
			planCarrierPlanTypeMap.put( planCarrierEntry.getKey(), offerCarrier );
		}
		return planCarrierPlanTypeMap;
	}

	/**
	 * 
	 * @param allBenefitStatePlansMap
	 * @return
	 */
	public static Set<String> getAllBenefitPlans(Map<String, Set<StateBenefitPlan>> primaryPlanMap) {
		Set<String> benefitPlans = new HashSet<>();
		for( Map.Entry<String, Set<StateBenefitPlan>> primaryPlanEntry : primaryPlanMap.entrySet() ) { 
			for( StateBenefitPlan sbp : primaryPlanEntry.getValue() ) {
				benefitPlans.add( sbp.getBenefitPlan() );
			}
		}
		return benefitPlans;
	}

	/**
	 * 
	 * @param primaryPlanMap
	 * @return
	 */
	public static List<String> getAllMedicalPlanIds(Map<String, Set<StateBenefitPlan>> primaryPlanMap) {
		Set<StateBenefitPlan> medicalPlans = primaryPlanMap.get(BSSApplicationConstants.MEDICAL);
		List<String> medicalPlanIds = new ArrayList<>();
		if (null != medicalPlans) {
			for (StateBenefitPlan sbp : medicalPlans) {
				medicalPlanIds.add(sbp.getBenefitPlan());
			}
		}
		return medicalPlanIds;
	}

	/**
	 * This method is for constructing the default plan package for UI use for
	 * Renewal client.
	 * 
	 * @param company
	 * @param mapOfCoverageLevels
	 * @return
	 */
	public static Map<String, List<PlanPackage>> getDefaultPlanPackage(Company company,
			Map<String, List<CoverageLevel>> mapOfCoverageLevels) {
		Map<String, List<PlanPackage>> map = new HashMap<>();

		// Loop to prepare Medical, Dental and Vision plan package.
		for (String planTypeCode : BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER) {
			MinimumFunding minFunding = CommonServiceHelper
					.extractMinFundingDetails(PlanTypesEnum.getName(planTypeCode), company);
			BigDecimal minFundingValue = minFunding.getMinFundValue();

			PlanPackage mPkg = new PlanPackage();
			Map<String, BigDecimal> coverageLevelFunding = new HashMap<>();

			for( CoverageLevel cv : mapOfCoverageLevels.get( PlanTypesEnum.getName(planTypeCode) )) {
				evaluateFundingTypes( company, cv, minFundingValue, coverageLevelFunding );
			}

			mPkg.setName("Default");
			mPkg.setFundingType(company.getDefaultFundingType());
			mPkg.setPlanType(planTypeCode);
			mPkg.setCoverageLevelFunding(coverageLevelFunding);

			List<PlanPackage> packageList = new ArrayList<>();
			packageList.add( mPkg );
			map.put( PlanTypesEnum.getName( planTypeCode ), packageList );
		}
		return map;
	}

	/**
	 * Depending on the funding type, populate the coverage-level funding map with the minimum funding level
	 * @param company
	 * @param level
	 * @param minimumFunding
	 * @param fundingMap
	 */
	private static void evaluateFundingTypes( Company company, CoverageLevel level, BigDecimal minimumFunding,
			Map<String,BigDecimal> fundingMap ) {
		if( BSSApplicationConstants.CFPCT.equals( company.getDefaultFundingType() )) {
			if ( ! BSSApplicationConstants.CVG_CODE_ALL.equals( level.getId() )) {
				if( Constants.EMPLOYEE.equals( level.getId() )) {
					fundingMap.put( level.getId(), minimumFunding );
				} else {
					fundingMap.put( level.getId(), BigDecimal.ZERO );
				}
			}
		} else {
			if (Constants.EMPLOYEE.equals( level.getId())) {
				fundingMap.put( level.getId(), minimumFunding );
			}
		}
	}


	/**
	 * 
	 * @param company
	 * @param planPackagesMap
	 * @param planPackagePlans
	 * @param autoSelectPlans
	 * @param primaryPlanMap
	 * @param erEEPlansMapping
	 */
	public static void updatePlanPackageNewClients(Map<String, List<PlanPackage>> planPackagesMap,
			Map<String, Set<BenefitPlan>> planPackagePlans, Map<String, Map<String, Set<String>>> autoSelectPlans,
			Map<String, Set<StateBenefitPlan>> primaryPlanMap, Map<String, String> erEEPlansMapping) {

		for (String offerType : primaryPlanMap.keySet()) {

			updatePlanPackage( planPackagesMap.get(offerType), planPackagePlans, offerType );

			crossReferencePlansForHeadcount( offerType, planPackagesMap, erEEPlansMapping );

			for (PlanPackage planPackage : planPackagesMap.get(offerType)) {
				planPackage.getHeadCountPlans().addAll(planPackage.getBenefitPlans());
			}

			Map<String, Set<String>> autoSelectPlansByType = autoSelectPlans.get(offerType);
			if( MapUtils.isNotEmpty( autoSelectPlansByType ) ) {
				gatherAutoSelectCrossRefPlans( planPackagesMap.get(offerType), autoSelectPlansByType );
			}
		}
	}

	private static void crossReferencePlansForHeadcount( String offerType, Map<String,List<PlanPackage>> planPackageMap,
			Map<String,String> erEePlansMap ) {
		if( offerType.equals( BSSApplicationConstants.DENTAL )
				|| offerType.equals( BSSApplicationConstants.VISION ) ) {
			for( PlanPackage pkg : planPackageMap.get(offerType) ) {
				addCrossRefTemplatePlans( erEePlansMap, pkg );
			}
		}
	}

	private static void gatherAutoSelectCrossRefPlans( List<PlanPackage> planPackages, Map<String,Set<String>> autoSelectPlansMap ) {
		for( PlanPackage planPackage : planPackages ) {
			Set<String> autoSelectPlansInPackage = new HashSet<>();
			for( String benefitPlan : planPackage.getBenefitPlans() ) {
				Set<String> crossRefPlans = autoSelectPlansMap.get( benefitPlan );
				if( CollectionUtils.isNotEmpty( crossRefPlans ) ) {
					for( String crossRefPlan : crossRefPlans ) {
						if( ! planPackage.getBenefitPlans().contains(crossRefPlan) ) {
							autoSelectPlansInPackage.add(crossRefPlan);
						}
					}
				}
			}
			planPackage.getBenefitPlans().addAll(autoSelectPlansInPackage);
		}
	}


	/**
	 * This method is for updating the plan package with funding base plan, Plan
	 * carriers and setting employee paid for dental and vision plan packages.
	 * 
	 * @param planPackages
	 * @param plans
	 * @param type
	 */
	private static void updatePlanPackage(List<PlanPackage> planPackages, Map<String, Set<BenefitPlan>> plans,
			String type) {
		if( planPackages == null ) {
			return;
		}

		for( PlanPackage planPackage : planPackages ) {
			// Get Benefit plans for this package
			Set<BenefitPlan> benefitPlans = plans.get( planPackage.getName() );
			planPackage.setTemplateId( planPackage.getId() );
			List<String> fundingBasePlans = planPackage.getFundingBasePlanList();
			if( CollectionUtils.isNotEmpty( fundingBasePlans )) {
				for( String plan : fundingBasePlans ) {
					captureFundingBasePlans( benefitPlans, plan, planPackage );
				}
			}

			// Plan Carrier Ids
			List<Long> planCarrierIds = new ArrayList<>();
			planPackage.setPlanCarrierIds(planCarrierIds);
			List<String> benefitPlanIds = new ArrayList<>();
			for (FundingBasePlan basePlan : planPackage.getFundingBasePlans()) {
				planCarrierIds.add(basePlan.getPlanCarrierId());
			}
			if( CollectionUtils.isNotEmpty( benefitPlans )) {
				gatherMDVBenefitPlanIds( benefitPlans, type, planPackage, benefitPlanIds );
			}

			planPackage.setBenefitPlans(benefitPlanIds);
		}
	}

	private static void captureFundingBasePlans( Set<BenefitPlan> benefitPlans, String basePlan, PlanPackage pkg ) {
		if( CollectionUtils.isNotEmpty( benefitPlans ) ) {
			for( BenefitPlan benefitPlan : benefitPlans ) {
				if( benefitPlan.getName().equals( basePlan ) ) {
					FundingBasePlan fundingBasePlan = new FundingBasePlan();
					fundingBasePlan.setFundingBasePlan( basePlan );
					fundingBasePlan.setPlanCarrierId( benefitPlan.getPlanCarrierId() );
					if ( ! pkg.getFundingBasePlans().contains( fundingBasePlan ) ) {
						pkg.getFundingBasePlans().add( fundingBasePlan );
					}
				}
			}
		}
	}

	private static void gatherMDVBenefitPlanIds( Set<BenefitPlan> plans, String offerType, PlanPackage pkg, List<String> benefitPlanIdList ) {
		if( offerType == null ) {
			return;
		}
		for( BenefitPlan plan : plans ) {
			switch( offerType ) {
			case BSSApplicationConstants.MEDICAL:
				if( BSSApplicationConstants.MEDICAL_PLAN_TYPES.contains( plan.getPlanType() )) {
					benefitPlanIdList.add( plan.getName() );
				}
				break;
			case BSSApplicationConstants.DENTAL:
				if( BSSApplicationConstants.DENTAL_PLAN_TYPES.contains( plan.getPlanType() )) {
					benefitPlanIdList.add( plan.getName() );
					identifyEmployeePaidPackage( pkg, plan.getPlanType() );
				}
				break;
			case BSSApplicationConstants.VISION:
				if( BSSApplicationConstants.VISION_PLAN_TYPES.contains(plan.getPlanType() )) {
					benefitPlanIdList.add( plan.getName() );
					identifyEmployeePaidPackage( pkg, plan.getPlanType() );
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * If this plan type is identified as one of the employee-paid plan types, 
	 * set the employee-paid in the PlanPackage object.
	 * @param pkg
	 * @param planType
	 */
	private static void identifyEmployeePaidPackage( PlanPackage pkg, String planType ) {
		if( BSSApplicationConstants.VOLUNTARY_PLAN_TYPES.contains( planType )) {
			pkg.setEmployeePaid( true );
		}
	}


	/**
	 * This method is for getting the corresponding plans for head count plans.
	 * 
	 * @param employerEmployeePlansMapping
	 * @param planPackage
	 */
	public static void addCrossRefTemplatePlans(Map<String, String> erEEPlansMapping, PlanPackage planPackage) {
		Set<String> crossHeadCountPlans = new HashSet<>();
		for (String bp : planPackage.getBenefitPlans()) {
			if (null != erEEPlansMapping.get(bp)) {
				crossHeadCountPlans.add(erEEPlansMapping.get(bp));
			}
		}
		if ( CollectionUtils.isNotEmpty( crossHeadCountPlans ) ) {
			crossHeadCountPlans.addAll(planPackage.getBenefitPlans());
			List<String> finalList = new ArrayList<>();
			finalList.addAll(crossHeadCountPlans);
			planPackage.setBenefitPlans(finalList);
		}
	}

	/**
	 * 
	 * @param company
	 * @param planCarrierMap
	 * @param isRenewalCompany
	 */
	public static void updatePlanCarrierExclusivity(Company company, Map<String, Set<PlanCarrier>> planCarrierMap,
			boolean isRenewalCompany) {
		Set<PlanCarrier> medicalPlanCarriers = planCarrierMap.get(BSSApplicationConstants.MEDICAL);
		Set<PlanCarrier> notRequiredCarriers = new HashSet<>();
		if (isRenewalCompany
				&& BSSApplicationConstants.COMPANY_HEAD_QUARTERS_FL.equalsIgnoreCase(company.getHeadQuatersState())
				&& BSSApplicationConstants.T2_EXCL_MED_PLAN_BCBSFL.equals(company.getExclusiveMedPlan())
				&& null != medicalPlanCarriers && !medicalPlanCarriers.isEmpty() && !company.isOnboardingCompany()) {
			for (PlanCarrier pc : medicalPlanCarriers) {
				if (BSSApplicationConstants.AETNA_PORTFOLIO_INT == pc.getId()
						|| (null != pc.getParentId() && pc.getParentId().stream().filter(Objects::nonNull)
								.anyMatch(BSSApplicationConstants.AETNA_PORTFOLIO::equals))) {
					notRequiredCarriers.add(pc);
				} else {
					pc.setMandatory(true);
					pc.setRestricted(false);
				}
			}
			medicalPlanCarriers.removeAll(notRequiredCarriers);
			planCarrierMap.put(BSSApplicationConstants.MEDICAL, medicalPlanCarriers);
		}
	}

	/**
	 * 
	 * @param planPackages
	 * @param benefitPlanIds
	 */
	public static void updatePlanPackagesPickAndChoose(Set<PlanPackage> planPackages, List<String> benefitPlanIds) {
		for (PlanPackage pk : planPackages) {
			pk.setBenefitPlans(benefitPlanIds);
		}
	}

	public static Map<Long, BigDecimal> calculateCarrierMinFunding(Company company, String benefitOfferType,
			List<CarrierMinimumFunding> lowestCostPlanPerCarrier) {
		Map<Long, BigDecimal> minFunding = new HashMap<>();
		for (CarrierMinimumFunding carrierMinimumFunding : lowestCostPlanPerCarrier) {
			if (benefitOfferType.equals(PlanTypesEnum.getName(carrierMinimumFunding.getPlanType()))) {
				MinimumFunding minFund = CommonServiceHelper.extractMinFundingDetails(benefitOfferType, company);
				if (MinFundExceptionService.FLAT.equals(minFund.getMinFundType())) {
					minFunding.put(carrierMinimumFunding.getCarrierId(), minFund.getMinFundValue());
				} else {
					BigDecimal minFundingPercent = minFund.getMinFundValue();
					minFundingPercent = minFundingPercent.divide(new BigDecimal(100));
					BigDecimal minFundingAmt = carrierMinimumFunding.getMinimumFundingAmt().multiply(minFundingPercent)
							.setScale(2, RoundingMode.HALF_UP);
					minFunding.put(carrierMinimumFunding.getCarrierId(), minFundingAmt);
				}
			}
		}
		return minFunding;
	}
}
