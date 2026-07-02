package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTimeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.RateUnitsEnums;
import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.helper.PlanRatesExportHelper;
import com.trinet.ambis.persistence.dao.hrp.PlanMappingDao;
import com.trinet.ambis.persistence.dao.hrp.PlanRatesDataDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.AdditionalBenefitPlanService;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.FlexRateService;
import com.trinet.ambis.service.HeadCountService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.PortfolioService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.AdditionalPlanOptionPlanExport;
import com.trinet.ambis.service.model.AdditionalPlanOptionsExport;
import com.trinet.ambis.service.model.AdditionalPlanRate;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.BenefitPlanRateMapper;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.FlexRateResponse;
import com.trinet.ambis.service.model.FlexRateResponseMapper;
import com.trinet.ambis.service.model.HealthPlanRates;
import com.trinet.ambis.service.model.HealthPlanRatesExportPlan;
import com.trinet.ambis.service.model.MappedHeadCount;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.service.model.PlanRate;
import com.trinet.ambis.service.model.PlanRatesExportData;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.StrategyUtils;
import com.trinet.ambis.util.Utils;

@Service
public class PlanRatesServiceImpl implements PlanRatesService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PlanRatesServiceImpl.class);

    // DP-only tier codes (5, 6, 7, 8)
    private static final Set<String> DP_ONLY_TIERS = new HashSet<>(Arrays.asList(
            CoverageCodesEnums.COV_EMPLOYEE_PLUS_DP.getCode(),
            CoverageCodesEnums.COV_EMPLOYEE_PLUS_DP_CHILD.getCode(),
            CoverageCodesEnums.COV_EMPLOYEE_PLUS_DP_ADULT_CHILD.getCode(),
            CoverageCodesEnums.COV_EMPLOYEE_PLUS_TWO_DP_ADULT.getCode()
    ));

	@Autowired
	private CompanyService companyService;

	@Autowired
	private AdditionalBenefitPlanService additionalBenefitPlanService;
	
	@Autowired
	HeadCountService headCountService;

	@Autowired
	private RealmPlyrPlanService realmPlyrPlanService;
	
	@Autowired
	BenefitOfferExceptionService benOfferExceptionService;
	
	@Autowired
	private PortfolioService portfolioService;
	
	@Autowired
	private PlanRatesDataDao planRatesDataDao;

	@Autowired
	private RealmDataDao realmDataDao;
	
	@Autowired
	private PlanMappingDao planMappingDao;

	@Autowired
	private FlexRateService flexRateService;

	@Override
	public PlanRatesExportData getPlanRatesExportData(Company futureCompany) {
		
		PlanRatesExportData planRatesExportData = new PlanRatesExportData();
		planRatesExportData.setFutureStartDate(Utils.convertDateToString(futureCompany.getRealmPlanYear().getPlanYearStart()));
		planRatesExportData.setFutureEndDate(Utils.convertDateToString(futureCompany.getRealmPlanYear().getPlanYearEnd()));

		// Get history company if this is a renewal client and plan year is in the future
		Company currentCompany = null;
		if (futureCompany.isRenewalCompany() && DateTimeComparator.getDateOnlyInstance()
				.compare(null, futureCompany.getRealmPlanYear().getPlanYearStart()) < 0) {
			currentCompany = companyService.getCompanyDetails(futureCompany.getCode(), true,
					futureCompany.getEmplId(), null);
			currentCompany.setRenewalCompany(futureCompany.isRenewalCompany());
			planRatesExportData.setCurrentStartDate(Utils.convertDateToString(currentCompany.getRealmPlanYear().getPlanYearStart()));
			planRatesExportData.setCurrentEndDate(Utils.convertDateToString(currentCompany.getRealmPlanYear().getPlanYearEnd()));
		}
		
		planRatesExportData.setHealthPlanData(getHealthPlanRatesByPlanTypeForExport(futureCompany, currentCompany));
		planRatesExportData.setAdditionalPlanData(getAdditionalPlanRates(futureCompany, currentCompany));

		return planRatesExportData;
	}
  
    @Override
    public Workbook getPlanRatesExcelWorkbook(Company company, PlanRatesExportData planRatesExportData, String hiddenColumns) {
        
        Workbook workbook = new XSSFWorkbook();
        PlanRatesExportHelper.constructPlanRatesWorkbook(company, planRatesExportData, workbook, hiddenColumns);

        return workbook;
    }


    @Override
    public Map<String, List<BenefitPlanRate>> getBenefitPlanRatesBy(Company company){
        return getBenefitPlanRatesBy(company, true);
    }

    /**
     * Retrieves benefit plan rates for the given company as a map of plan name to BenefitPlanRates.
     * Determines band codes, matches rates by coverage level.
     */
    @Override
    public Map<String, List<BenefitPlanRate>> getBenefitPlanRatesBy(Company company,boolean includeDpRates) {
		LOGGER.info("Band codes for company id :: {} -->> {} ", company.getId(), company.getBandCodes());

		// Check if DIFFERENTIALS risk type - use FlexRate service
		if (company.getRiskType() == RiskTypeEnum.DIFFERENTIALS) {
			LOGGER.info("Using DIFFERENTIALS path for company id :: {}", company.getId());
			Map<String, List<BenefitPlanRate>> result = getPlanRatesByPlanId(company);
			// Filter DP rates if needed
			if (!includeDpRates) {
				LOGGER.info("Filtering DP-only tiers for DIFFERENTIALS company id :: {}", company.getId());
				result = filterDpRatesFromMap(result);
			}
			return result;
		}

        Map<String,XbssRealmPlyrPlan> plyrPlanMap = realmPlyrPlanService.getMapForRealmPlanYear(company.getRealmPlanYear().getId());
        Map<String, List<BenefitPlanRate>> ratesByPlanId = planRatesDataDao.getBenefitPlanRatesBy(company);

        Map<String, List<BenefitPlanRate>> filteredRatesMap = new HashMap<>();
        for (Entry<String, List<BenefitPlanRate>> entry : ratesByPlanId.entrySet()) {
            String plan = entry.getKey();
            List<BenefitPlanRate> rates = entry.getValue();
            String bandCode = StrategyUtils.findBandCode( company, plan, plyrPlanMap );

            // Collect rates matching bandCode
            List<BenefitPlanRate> rateList = rates.stream()
                    .filter(rate -> rate.getBandCode().equalsIgnoreCase(bandCode))
                    .collect(Collectors.toList());
            // If no match, collect rates with bandCode "N"
            if (rateList.isEmpty()) {
                rateList = rates.stream()
                        .filter(rate -> rate.getBandCode().equalsIgnoreCase("N"))
                        .collect(Collectors.toList());
            }

            filteredRatesMap.put(plan, rateList);
        }

        return filteredRatesMap;
    }

    /**
     * Retrieves benefit plan rates for DIFFERENTIALS risk type companies using FlexRate service.
     * Calls FlexRateService.getPlanRatesFromCache and maps the results to a Map by planId.
     *
     * @param company the company to retrieve rates for
     * @return Map of planId to list of BenefitPlanRate
     */
    public Map<String, List<BenefitPlanRate>> getPlanRatesByPlanId(Company company) {
        try {
            LOGGER.info("Retrieving flex rates for company id: {}, code: {}, prospectId: {}",
                    company.getId(), company.getCode(), company.getProspectId());

            // Determine effective date from plan start date
            String effectiveDate = CommonUtils.formatDate(company.getPlanStartDate(),
                    BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY, BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD);
			LOGGER.info("Effective date for company id: {}, code: {}, set to: {}", company.getId(), company.getCode(), effectiveDate);
            if (effectiveDate == null || effectiveDate.isEmpty()) {
                LOGGER.warn("No effective date found for company id: {}, code: {}, returning empty map",
                        company.getId(), company.getCode());
                return Collections.emptyMap();
            }

            // Call FlexRateService with company object and effectiveDate
            FlexRateResponse response = flexRateService.getPlanRatesFromCache(
                    company,
                    effectiveDate
            );

            // Convert FlexRateResponse to List<BenefitPlanRate>
            List<BenefitPlanRate> allRates = FlexRateResponseMapper.toBenefitPlanRates(response, company.getCode());

            // Group by planId (benefitPlan field)
            Map<String, List<BenefitPlanRate>> ratesByPlanId = allRates.stream()
                    .filter(rate -> rate.getBenefitPlan() != null)
                    .collect(Collectors.groupingBy(BenefitPlanRate::getBenefitPlan));

            LOGGER.info("Retrieved {} plan rates for company id: {}", ratesByPlanId.size(), company.getId());
            return ratesByPlanId;

        } catch (Exception e) {
            LOGGER.error("Error retrieving flex rates for company id: {}, code: {}, error: {}",
                    company.getId(), company.getCode(), e.getMessage(), e);
			String causeMessage = e.getCause() != null ? e.getCause().getMessage() : "No additional cause available";
            throw new BSSBadDataException(String.format("Error retrieving flex rates for company id: %s, code: %s: %s",
					company.getId(), company.getCode(), causeMessage));
        }
    }

    /**
     * Filters out DP-only tiers from the rate map.
     * When includeDpRates=false, we exclude DP tiers (5,6,7,8) and keep only regular tiers (1,2,C,4).
     *
     * @param ratesByPlanId map of plan id to rates
     * @return filtered map with DP tiers excluded
     */
    private Map<String, List<BenefitPlanRate>> filterDpRatesFromMap(Map<String, List<BenefitPlanRate>> ratesByPlanId) {
        if (ratesByPlanId == null || ratesByPlanId.isEmpty()) {
            return ratesByPlanId;
        }

        Map<String, List<BenefitPlanRate>> filteredMap = new HashMap<>();
        for (Entry<String, List<BenefitPlanRate>> entry : ratesByPlanId.entrySet()) {
            List<BenefitPlanRate> filteredRates = entry.getValue().stream()
                    .filter(rate -> rate.getCoverageCode() != null)
                    // Keep only rates that are NOT in DP_ONLY_TIERS (i.e., exclude DP tiers)
                    .filter(rate -> !DP_ONLY_TIERS.contains(rate.getCoverageCode().trim().toUpperCase(Locale.ROOT)))
                    .collect(Collectors.toList());

            if (!filteredRates.isEmpty()) {
                filteredMap.put(entry.getKey(), filteredRates);
            }
        }
        return filteredMap;
    }

    /**
     * Retrieves benefit plan rates for the given company as a map of plan type to PlanRates.
     * Determines band codes, matches rates by coverage level.
     */
    @Override
    public Map<String, List<PlanRate>> getBenefitPlanRatesByBenefitType(Company company) {
        Map<String, List<BenefitPlanRate>> planRatesByPlanId = getBenefitPlanRatesBy(company);

        Map<String, List<PlanRate>> planRatesByBenefitType = new HashMap<>();

        for (Entry<String, List<BenefitPlanRate>> entry : planRatesByPlanId.entrySet()) {
            List<BenefitPlanRate> rates = entry.getValue();

            String planType = rates.isEmpty() ? null : rates.get(0).getPlanType();
            PlanRate planRate = BenefitPlanRateMapper.toPlanRate(rates);
            planRatesByBenefitType.computeIfAbsent(planType, k -> new ArrayList<>()).add(planRate);
        }

        return planRatesByBenefitType;
    }

    /**
	 * Returns a map of planType with a list of plans and rates offered in the
	 * current or future company in an flat object used to export the data
	 * 
	 * @param futureCompany
	 * @param currentCompany
	 * @return
	 */
	private Map<String, List<HealthPlanRatesExportPlan>> getHealthPlanRatesByPlanTypeForExport(Company futureCompany,
			Company currentCompany) {

		Map<String, List<HealthPlanRatesExportPlan>> returnMap = new HashMap<>();

		Map<String, List<HealthPlanRates>> healthPlanRatesMap = getHealthPlanRatesByPlanType(futureCompany,
				currentCompany);
		
		benOfferExceptionService.applyException(futureCompany, healthPlanRatesMap);

		for (Entry<String, List<HealthPlanRates>> healthPlanRatesEntry : healthPlanRatesMap.entrySet()) {
			String planType = healthPlanRatesEntry.getKey();
			List<HealthPlanRatesExportPlan> benefitPlanRatesExportList = new ArrayList<>();
			for (HealthPlanRates healthPlanRates : healthPlanRatesEntry.getValue()) {
				benefitPlanRatesExportList.add(convertHealthPlanRatesToHealthPlanRatesExportPlan(healthPlanRates));
			}
			returnMap.put(planType, benefitPlanRatesExportList);
		}

		return returnMap;
	}

	/**
	 * Returns a map of planType with a list of plans and rates offered in the
	 * current or future company
	 * 
	 * @param futureCompany
	 * @param currentCompany
	 * @return
	 */
	private Map<String, List<HealthPlanRates>> getHealthPlanRatesByPlanType(Company futureCompany,
			Company currentCompany) {

		Map<String, List<HealthPlanRates>> returnMap;

		boolean planRateMappingFlag = RulesAndConfigsUtils.isPlanRateMappingEnabled(futureCompany.getRealmPlanYearId());

		List<HealthPlanRates> combinedPlans = getCombinedHealthPlans(futureCompany, currentCompany,
				planRateMappingFlag);

		Map<String, Map<String, BigDecimal>> futureRates = getHealthPlanRatesForCompany(futureCompany, combinedPlans,
				false);
		Map<String, Map<String, BigDecimal>> currentRates = new HashMap<>();
		if (currentCompany != null) {
			currentRates = getHealthPlanRatesForCompany(currentCompany, combinedPlans, true);
		}

		Map<String, Map<String, Map<String, Long>>> mappedPlanHeadCount = new HashMap<>();
		Map<String, Map<String, Long>> currentPlanHeadcount = new HashMap<>();
		Map<String, Map<String, Long>> futurePlanHeadcount = new HashMap<>();
		
		if (currentCompany != null) {
			if (planRateMappingFlag) {
				mappedPlanHeadCount = getMappedHealthPlanHeadcount(futureCompany);
			} else {
				currentPlanHeadcount = getUnmappedHealthPlanHeadcount(currentCompany, true);
			}
		}
		else {
			futurePlanHeadcount = getUnmappedHealthPlanHeadcount(futureCompany, false);
		}

		Map<String, Set<String>> planStateMap = planRatesDataDao.getBenefitPlanStates(
				futureCompany.getRealmPlanYearId(),
				currentCompany == null ? futureCompany.getRealmPlanYearId() : currentCompany.getRealmPlanYearId());

		if (planRateMappingFlag && currentCompany != null) {
			returnMap = createHealthPlanRatesByPlanTypeMapped(combinedPlans, planStateMap, futureRates, currentRates,
					mappedPlanHeadCount);
		} else {
			returnMap = createHealthPlanRatesByPlanTypeUnmapped(combinedPlans, planStateMap, futureRates, currentRates,
					currentCompany == null ? futurePlanHeadcount : currentPlanHeadcount);
		}

		for (List<HealthPlanRates> healthPlanRates : returnMap.values()) {
			Collections.sort(healthPlanRates);
		}
		
		return returnMap;
	}

	/**
	 * Returns a map of benefitPlan and plan object for the plans offered in the
	 * current or future company
	 * 
	 * @param futurePlanMap
	 * @param currentPlanMap
	 * @return
	 */
	private List<HealthPlanRates> getCombinedHealthPlans(Company futureCompany, Company currentCompany,
			boolean planRateMappingFlag) {

		List<HealthPlanRates> returnPlans;

		if (planRateMappingFlag) {
			returnPlans = getCombinedHealthPlansMapped(futureCompany, currentCompany);
		} else {
			returnPlans = getCombinedHealthPlansUnmapped(futureCompany, currentCompany);
		}
		return returnPlans;
	}

	/**
	 * Returns a map of benefitPlan and plan object for the plans offered by the
	 * passed in company
	 * 
	 * @param company
	 * @return
	 */
	private Map<String, StateBenefitPlan> getHealthPlans(Company company) {
		
		Map<String, StateBenefitPlan> returnMap = new HashMap<>();

		Set<String> medicalPlansPortfoliosList = new HashSet<>();
		Set<String> dentalPlansPortfoliosList = new HashSet<>();
		Set<String> visionPlansPortfoliosList = new HashSet<>();

		Map<String, Set<PlanCarrier>> planCarrierMap = portfolioService.findPrimaryPlanCarriers(company);
		Set<String> primaryPlanCarriers = BenefitCategoriesHelper.getPlanCarriers(planCarrierMap);
		Set<String> outOfRegionPlans = CommonServiceHelper.getOutOfRegionPlansToExclude(company, primaryPlanCarriers,
				realmDataDao);
		
		if (planCarrierMap.get(Constants.MEDICAL) != null) {
			for (PlanCarrier pc : planCarrierMap.get(Constants.MEDICAL)) {
				medicalPlansPortfoliosList.add(String.valueOf(pc.getId()));
			}
		}
		if (planCarrierMap.get(Constants.DENTAL) != null) {
			for (PlanCarrier pc : planCarrierMap.get(Constants.DENTAL)) {
				dentalPlansPortfoliosList.add(String.valueOf(pc.getId()));
			}
		}
		if (planCarrierMap.get(Constants.VISION) != null) {
			for (PlanCarrier pc : planCarrierMap.get(Constants.VISION)) {
				visionPlansPortfoliosList.add(String.valueOf(pc.getId()));
			}
		}
		
		Map<String, StateBenefitPlan> medicalPlanMap = planRatesDataDao.getBenefitPlans(company.getRealmPlanYearId(),
				medicalPlansPortfoliosList, company, outOfRegionPlans,
				new HashSet<>(Arrays.asList(BSSApplicationConstants.MEDICAL_PLAN_TYPE)));

		Map<String, StateBenefitPlan> dentalPlanMap = planRatesDataDao.getBenefitPlans(company.getRealmPlanYearId(),
				dentalPlansPortfoliosList, company, outOfRegionPlans,
				new HashSet<>(BSSApplicationConstants.DENTAL_PLAN_TYPES));

		Map<String, StateBenefitPlan> visionPlanMap = planRatesDataDao.getBenefitPlans(company.getRealmPlanYearId(),
				visionPlansPortfoliosList, company, outOfRegionPlans,
				new HashSet<>(BSSApplicationConstants.VISION_PLAN_TYPES));

		returnMap.putAll(medicalPlanMap);
		returnMap.putAll(dentalPlanMap);
		returnMap.putAll(visionPlanMap);

		return returnMap;
	}
	
	private List<HealthPlanRates> getCombinedHealthPlansUnmapped(Company futureCompany, Company currentCompany) {

		List<HealthPlanRates> returnPlans = new ArrayList<>();
		
		Map<String, StateBenefitPlan> futurePlanMap = getHealthPlans(futureCompany);
		Map<String, StateBenefitPlan> currentPlanMap = new HashMap<>();

		if (currentCompany != null) {
			currentPlanMap = getHealthPlans(currentCompany);
		}

		for (StateBenefitPlan stateBenefitPlan : futurePlanMap.values()) {
			String planId = stateBenefitPlan.getBenefitPlan();
			String basePlanType = Utils.getGenericPlanType(stateBenefitPlan.getPlanType());
			String currentPlanId = null;
			String currentPlanName = null;
			String offeredYearsFlag = "F";
			if (currentPlanMap.containsKey(planId)) {
				currentPlanId = planId;
				currentPlanName = currentPlanMap.get(currentPlanId).getDescription();
				offeredYearsFlag = "B";
			}
			returnPlans.add(buildHealthPlanRates(currentPlanId, planId, currentPlanName, stateBenefitPlan.getDescription(),
					stateBenefitPlan.getPlanType(), basePlanType, offeredYearsFlag, stateBenefitPlan.getVendorId(),
					stateBenefitPlan.getPortfolioId()));
		}

		for (StateBenefitPlan stateBenefitPlan : currentPlanMap.values()) {
			String planId = stateBenefitPlan.getBenefitPlan();
			String basePlanType = Utils.getGenericPlanType(stateBenefitPlan.getPlanType());
			if (!futurePlanMap.containsKey(planId)) {
				returnPlans.add(buildHealthPlanRates(planId, null, stateBenefitPlan.getDescription(), stateBenefitPlan.getDescription(),
						stateBenefitPlan.getPlanType(), basePlanType, "C", stateBenefitPlan.getVendorId(),
						stateBenefitPlan.getPortfolioId()));				
			}
		}

		return returnPlans;	
		
	}
	
	private List<HealthPlanRates> getCombinedHealthPlansMapped(Company futureCompany, Company currentCompany) {

		List<HealthPlanRates> returnPlans = new ArrayList<>();
		LOGGER.info("Fetching plan mappings from database for company: {}, realmPlanYearId: {}", 
				futureCompany.getCode(), futureCompany.getRealmPlanYearId());
		Map<String, List<String>> realmPlanMappings = planMappingDao.getPlanMappingsAsSimpleMap(futureCompany, null);
		List<String> currentPlansIncluded = new ArrayList<>();
		List<String> futurePlansMapped = new ArrayList<>();
		Map<String, StateBenefitPlan> futurePlanMap = getHealthPlans(futureCompany);
		Map<String, StateBenefitPlan> currentPlanMap = new HashMap<>();

		if (currentCompany != null) {
			currentPlanMap = getHealthPlans(currentCompany);
		}
		
		for (StateBenefitPlan stateBenefitPlan : currentPlanMap.values()) {
			String planId = stateBenefitPlan.getBenefitPlan();
			String basePlanType = Utils.getGenericPlanType(stateBenefitPlan.getPlanType());
			if (realmPlanMappings.containsKey(planId)) {
				for (String newPlanId : realmPlanMappings.get(planId)) {
					if (futurePlanMap.containsKey(newPlanId)) {
						currentPlansIncluded.add(planId);
						futurePlansMapped.add(newPlanId);
						returnPlans.add(buildHealthPlanRates(planId, newPlanId, stateBenefitPlan.getDescription(),
								futurePlanMap.get(newPlanId).getDescription(), stateBenefitPlan.getPlanType(),
								basePlanType, "B", stateBenefitPlan.getVendorId(), stateBenefitPlan.getPortfolioId()));
					}
				}
			}
		}
		
		// Loop back through current plans to get any plans that were not mapped and add
		// as offeredYearsFlag = C (Current)
		addUnmappedCurrentPlans(returnPlans, currentPlansIncluded, currentPlanMap);

		addUnmappedFuturePlans(returnPlans, futurePlansMapped, futurePlanMap);		
		return returnPlans;
	}

	private void addUnmappedCurrentPlans(List<HealthPlanRates> returnPlans, List<String> currentPlansIncluded,
			Map<String, StateBenefitPlan> currentPlanMap) {
		for (StateBenefitPlan stateBenefitPlan : currentPlanMap.values()) {
			String planId = stateBenefitPlan.getBenefitPlan();
			String basePlanType = Utils.getGenericPlanType(stateBenefitPlan.getPlanType());
			if (!currentPlansIncluded.contains(planId)) {
				returnPlans.add(buildHealthPlanRates(planId, null, stateBenefitPlan.getDescription(), null,
						stateBenefitPlan.getPlanType(), basePlanType, "C", stateBenefitPlan.getVendorId(),
						stateBenefitPlan.getPortfolioId()));
			}
		}
	}
	
	private void addUnmappedFuturePlans(List<HealthPlanRates> returnPlans, List<String> futurePlansMapped,
			Map<String, StateBenefitPlan> futurePlanMap) {
		for (StateBenefitPlan stateBenefitPlan : futurePlanMap.values()) {
			String planId = stateBenefitPlan.getBenefitPlan();
			String basePlanType = Utils.getGenericPlanType(stateBenefitPlan.getPlanType());
			if (!futurePlansMapped.contains(planId)) {
				returnPlans.add(buildHealthPlanRates(null, planId, null, stateBenefitPlan.getDescription(),
						stateBenefitPlan.getPlanType(), basePlanType, "F", stateBenefitPlan.getVendorId(),
						stateBenefitPlan.getPortfolioId()));

			}
		}
	}
	
	private Map<String, List<HealthPlanRates>> createHealthPlanRatesByPlanTypeUnmapped(List<HealthPlanRates> combinedPlans, Map<String, Set<String>> planStateMap,
			Map<String, Map<String, BigDecimal>> futureRates, Map<String, Map<String, BigDecimal>> currentRates, Map<String, Map<String, Long>> currentPlanHeadcount) {
		Map<String, List<HealthPlanRates>> returnRates = new HashMap<>();
		for (HealthPlanRates healthPlanRates : combinedPlans) {
			String basePlanType = healthPlanRates.getBasePlanType();
			String planId = healthPlanRates.getCurrentId() == null ? healthPlanRates.getFutureId() : healthPlanRates.getCurrentId();
			if (planStateMap.containsKey(planId)) {
				healthPlanRates.setOfferedStates(new ArrayList<>(planStateMap.get(planId)));
			}
			if (!("F").equals(healthPlanRates.getOfferedYearsFlag())) {
				healthPlanRates.setCurrentRates(currentRates.get(planId));
			}
			if (!("C").equals(healthPlanRates.getOfferedYearsFlag())) {
				healthPlanRates.setFutureRates(futureRates.get(planId));
			}
			healthPlanRates.setCurrentHeadCount(currentPlanHeadcount.get(planId));
			healthPlanRates.setFutureHeadCount(currentPlanHeadcount.get(planId));
			updateReturnRates(returnRates, healthPlanRates, basePlanType);
		}

		return returnRates;
	}
	
	private Map<String, List<HealthPlanRates>> createHealthPlanRatesByPlanTypeMapped(List<HealthPlanRates> combinedPlans, Map<String, Set<String>> planStateMap,
			Map<String, Map<String, BigDecimal>> futureRates, Map<String, Map<String, BigDecimal>> currentRates,
			Map<String, Map<String, Map<String, Long>>> mappedPlanHeadCount) {
		Map<String, List<HealthPlanRates>> returnRates = new HashMap<>();
		
		Map<String, HealthPlanRates> combinedPlansByCurrentPlanId = new HashMap<>();
		
		Map<String, Map<String, Map<String, Long>>> localMappedPlanHeadCount = new HashMap<>(mappedPlanHeadCount);
		for (HealthPlanRates healthPlanRates : combinedPlans) {

			String basePlanType = healthPlanRates.getBasePlanType();
			if (healthPlanRates.getCurrentId() != null) {
				combinedPlansByCurrentPlanId.put(healthPlanRates.getCurrentId(), healthPlanRates);
			}

			buildHealthPlanRates(planStateMap, futureRates, currentRates, localMappedPlanHeadCount, healthPlanRates);

			updateReturnRates(returnRates, healthPlanRates, basePlanType);
		}
		
		// Add records for plans were we shouldn't have enrollment but do
		for (Entry<String, Map<String, Map<String, Long>>> headCountEntry : localMappedPlanHeadCount.entrySet()) {
			for (Entry<String, Map<String, Long>> futureHeadCountEntry : headCountEntry.getValue().entrySet()) {
				if (futureHeadCountEntry.getKey() == null) {
					String currentId = headCountEntry.getKey();

					HealthPlanRates healthPlanRates = combinedPlansByCurrentPlanId.get(currentId);
					HealthPlanRates invalidCurrentPlan = buildHealthPlanRates(healthPlanRates.getCurrentId(), null, healthPlanRates.getCurrentPlanName(), null,
							healthPlanRates.getPlanType(), healthPlanRates.getBasePlanType(), BSSApplicationConstants.INVALID_PLAN_ENROLLMENT, healthPlanRates.getVendorId(),
							healthPlanRates.getPortfolioId());
					
					if (planStateMap.containsKey(healthPlanRates.getCurrentId())) {
						invalidCurrentPlan.setOfferedStates(new ArrayList<>(planStateMap.get(healthPlanRates.getCurrentId())));
					}

					invalidCurrentPlan.setCurrentRates(currentRates.get(invalidCurrentPlan.getCurrentId()));
					invalidCurrentPlan.setCurrentHeadCount(futureHeadCountEntry.getValue());
					
					updateReturnRates(returnRates, invalidCurrentPlan, invalidCurrentPlan.getBasePlanType());
					
				}
			}
		}
		
		return returnRates;
	}

	private void updateReturnRates(Map<String, List<HealthPlanRates>> returnRates, HealthPlanRates healthPlanRates,
			String basePlanType) {
		if (returnRates.containsKey(basePlanType)) {
			returnRates.get(basePlanType).add(healthPlanRates);
		} else {
			returnRates.put(basePlanType, new ArrayList<>(Arrays.asList(healthPlanRates)));
		}
	}

	private void buildHealthPlanRates(Map<String, Set<String>> planStateMap,
			Map<String, Map<String, BigDecimal>> futureRates, Map<String, Map<String, BigDecimal>> currentRates,
			Map<String, Map<String, Map<String, Long>>> localMappedPlanHeadCount, HealthPlanRates healthPlanRates) {
		if (planStateMap.containsKey(healthPlanRates.getFutureId())) {
			healthPlanRates.setOfferedStates(new ArrayList<>(planStateMap.get(healthPlanRates.getFutureId())));
		} else if (planStateMap.containsKey(healthPlanRates.getCurrentId())) {
			healthPlanRates.setOfferedStates(new ArrayList<>(planStateMap.get(healthPlanRates.getCurrentId())));
		}

		if (!("F").equals(healthPlanRates.getOfferedYearsFlag())) {
			healthPlanRates.setCurrentRates(currentRates.get(healthPlanRates.getCurrentId()));
		}
		if (!("C").equals(healthPlanRates.getOfferedYearsFlag())) {
			healthPlanRates.setFutureRates(futureRates.get(healthPlanRates.getFutureId()));
		}
		if (localMappedPlanHeadCount.containsKey(healthPlanRates.getCurrentId())) {
			Map<String, Map<String, Long>> futureMap = localMappedPlanHeadCount.get(healthPlanRates.getCurrentId());
			// When a plan is only offering in the Current year, apply headcount to it,
			// regardless of the plan it is mapping to in the future year. Plans will only
			// have one entry in the futureMap so there is not a risk for overwriting the data.
			if (("C").equals(healthPlanRates.getOfferedYearsFlag())) {
				for (Entry<String, Map<String, Long>> futureMapEntry : futureMap.entrySet()) {
					healthPlanRates.setCurrentHeadCount(futureMapEntry.getValue());
					futureMap.remove(futureMapEntry.getKey());					
				}
			}
			else if (futureMap.containsKey(healthPlanRates.getFutureId())) {
				healthPlanRates.setCurrentHeadCount(futureMap.get(healthPlanRates.getFutureId()));
				if (healthPlanRates.getFutureId() != null) {
					healthPlanRates.setFutureHeadCount(futureMap.get(healthPlanRates.getFutureId()));
				}
				futureMap.remove(healthPlanRates.getFutureId());					
			}
		}
	}

	/**
	 * Returns a map of benefitPlan and coverage level/rates for the passed in
	 * company
	 * 
	 * @param company
	 * @param planMap
	 * @return
	 */
	private Map<String, Map<String, BigDecimal>> getHealthPlanRatesForCompany(Company company,
			List<HealthPlanRates> plans, boolean isHistory) {

		Map<String, Map<String, BigDecimal>> returnPlanRates = new HashMap<>();
		Map<String, List<BenefitPlanRate>> planRates = getBenefitPlanRatesBy(company);

		for (HealthPlanRates healthPlanRates : plans) {
			String planId = isHistory ? healthPlanRates.getCurrentId() : healthPlanRates.getFutureId();
			Map<String, BigDecimal> coverageLevelRateMap = new HashMap<>();
			// If the plan is in the planRate map, get the band code and
			// rate
			if (planRates.containsKey(planId)) {
				List<BenefitPlanRate> planRateList = planRates.get(planId);
				for (BenefitPlanRate planRate : planRateList) {
					coverageLevelRateMap.put(planRate.getCoverageCode(), planRate.getEmployerCost());
				}
				returnPlanRates.put(planId, coverageLevelRateMap);
			}
		}

		return returnPlanRates;
	}

	/**
	 * Returns a map of benefitPlan and coverage level/total headcount for the passed
	 * in company
	 * 
	 * This includes both base and mirror plan enrollment
	 * 
	 * @param company
	 * @return
	 */
	private Map<String, Map<String, Long>> getUnmappedHealthPlanHeadcount(Company company, boolean isHistory) {

		Map<String, Map<String, Long>> returnMap = new HashMap<>();

		MultiKeyMap planHeadcountMap = getHealthPlanHeadcount(company, isHistory);

		MapIterator itr = planHeadcountMap.mapIterator();
		while (itr.hasNext()) {
			itr.next();
			MultiKey mk = (MultiKey) itr.getKey();
			String planId = (String) mk.getKey(0);
			String covrgCode = (String) mk.getKey(1);
			Long headCount = (Long) itr.getValue();
			if (returnMap.containsKey(planId)) {
				returnMap.get(planId).put(covrgCode, headCount);
			} else {
				Map<String, Long> coverageLevelHeadcountMap = new HashMap<>();
				coverageLevelHeadcountMap.put(covrgCode, headCount);
				returnMap.put(planId, coverageLevelHeadcountMap);
			}
		}
		return returnMap;
	}

	/**

	 * Returns a MultiKeyMap of benefitPlan, coverage level and base plan headcount for the passed
	 * in company
	 * 
	 * @param company
	 * @return
	 */
	private MultiKeyMap getHealthPlanHeadcount(Company company, boolean isHistory) {

		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupCovrgHeadCountMap = headCountService
				.getHeadCountByGroupAndPlan(company, company.getRealmPlanYear().getId(),
						company.getRealmPlanYear().getPlanYearEnd(), !isHistory);

		// Covert data to MultiKeyMap to flatten it by plan rather than group
		MultiKeyMap planHeadcountMap = new MultiKeyMap();
		for (Map<String, List<PlanCoverageLevelHeadCount>> planCoverageLevelHeadcountMap : groupCovrgHeadCountMap
				.values()) {
			for (Entry<String, List<PlanCoverageLevelHeadCount>> planCoverageLevelHeadCountEntry : planCoverageLevelHeadcountMap
					.entrySet()) {
				String planId = planCoverageLevelHeadCountEntry.getKey();
				for (PlanCoverageLevelHeadCount planCoverageLevelHeadCount : planCoverageLevelHeadCountEntry
						.getValue()) {
					String covrgCode = planCoverageLevelHeadCount.getCovrgCode();
					int headCount = planCoverageLevelHeadCount.getHeadCount();
					Long totalHeadcount = Long.valueOf(headCount);
					if (planHeadcountMap.containsKey(planId, covrgCode)) {
						totalHeadcount = (Long) planHeadcountMap.get(planId, covrgCode) + headCount;
					}
					planHeadcountMap.put(planId, covrgCode, totalHeadcount);
				}

			}
		}

		return planHeadcountMap;

	}
	
	private Map<String, Map<String, Map<String, Long>>> getMappedHealthPlanHeadcount(Company company) {
		
		Map<String, Map<String, Map<String, Long>>> returnValues = new HashMap<>();

		List<MappedHeadCount> mappedHeadCounts =  headCountService
				.getMappedHeadCounts(company.getCode(), company.getRealmPlanYear().getId());
		
		for (MappedHeadCount mappedHeadCount : mappedHeadCounts) {
			Map<String, Map<String, Long>> futureMap = new HashMap<>();
			Map<String, Long> coverageMap = new HashMap<>();
			if (returnValues.containsKey(mappedHeadCount.getCurrentBenefitPlanId())) {
				futureMap = returnValues.get(mappedHeadCount.getCurrentBenefitPlanId());
				if (futureMap.containsKey(mappedHeadCount.getFutureBenefitPlanId())) {
					coverageMap = futureMap.get(mappedHeadCount.getFutureBenefitPlanId());
				}
				coverageMap.put(mappedHeadCount.getCoverageCode(), Long.valueOf(mappedHeadCount.getHeadCount()));
				futureMap.put(mappedHeadCount.getFutureBenefitPlanId(), coverageMap);
			}
			else {
				coverageMap.put(mappedHeadCount.getCoverageCode(), Long.valueOf(mappedHeadCount.getHeadCount()));
				futureMap.put(mappedHeadCount.getFutureBenefitPlanId(), coverageMap);
				returnValues.put(mappedHeadCount.getCurrentBenefitPlanId(), futureMap);
			}
			
		}
		
		return returnValues;
	}

	/**
	 * Returns a map of planType and list of Disability and Life/ADD plans
	 * offered in the current or future company
	 * 
	 * @param futureCompany
	 * @param currentCompany
	 * @return
	 */
	private Map<String, List<AdditionalPlanOptionsExport>> getAdditionalPlanRates(Company futureCompany,
			Company currentCompany) {

		Map<String, List<AdditionalPlanOptionsExport>> returnMap = new HashMap<>();
		List<AdditionalPlanOptionsExport> planList;

		Map<String, AdditionalBenefitPlan> disabilityPlanMap = getDisabilityPlanList(futureCompany, currentCompany);
		Set<AdditionalPlanOptionsExport> lifeAddPlanSet = getCombinedLifeAddPlans(futureCompany, currentCompany);
		Map<String, Set<String>> optionPlanSet = getOptionPlanMap(disabilityPlanMap, lifeAddPlanSet);

		Map<String, AdditionalPlanRate> futureAdditionalBenefitRates = additionalBenefitPlanService
				.getAdditionalPlansRate(futureCompany, false, optionPlanSet);
		
		Map<String, AdditionalPlanRate> currentAdditionalBenefitRates = new HashMap<>();
		if (currentCompany != null) {
			currentAdditionalBenefitRates = additionalBenefitPlanService
				.getAdditionalPlansRate(currentCompany, true, optionPlanSet);
		}

		planList = calculateDisabilityPlanRates(disabilityPlanMap, futureAdditionalBenefitRates,
				currentAdditionalBenefitRates,
				RulesAndConfigsUtils.isDisabledBundledOn(futureCompany.getRealmPlanYearId()),
				futureCompany.getRealmPlanYearId(), futureCompany.getRealm().getBenExchange());

		Collections.sort(planList);
		returnMap.put(BSSApplicationConstants.DISABILITY, planList);

		planList = calculateLifeAddPlanRates(lifeAddPlanSet, futureAdditionalBenefitRates,
				currentAdditionalBenefitRates);
		Collections.sort(planList);
		returnMap.put(BSSApplicationConstants.LIFE, planList);

		benOfferExceptionService.applyException(futureCompany, returnMap);

		return returnMap;
	}

	/**
	 * Returns a map of benefitPlan and plan object for the Disability plans
	 * offered in the current or future company
	 * 
	 * @param futureCompany
	 * @param currentCompany
	 * @return
	 */
	private Map<String, AdditionalBenefitPlan> getDisabilityPlanList(Company futureCompany, Company currentCompany) {

		return realmDataDao.getDisabilityOptionsForRealmPlanYears(futureCompany.getRealmPlanYearId(),
				currentCompany == null ? futureCompany.getRealmPlanYearId()
						: currentCompany.getRealmPlanYearId(),
				futureCompany.getHeadQuatersState(), false);
	}

	/**
	 * Returns a set of Life/ADD plans and Disability and Life/ADD plan offered
	 * in the current or future company
	 * 
	 * @param futureCompany
	 * @param currentCompany
	 * @return
	 */
	private Set<AdditionalPlanOptionsExport> getCombinedLifeAddPlans(Company futureCompany, Company currentCompany) {

		Set<AdditionalPlanOptionsExport> returnSet = new HashSet<>();

		Map<String, StateBenefitPlan> futurePlanMap = planRatesDataDao.getBenefitPlans(
				futureCompany.getRealmPlanYearId(), new HashSet<>(), futureCompany, new HashSet<>(),
				new HashSet<>(Arrays.asList(BSSApplicationConstants.LIFE_CODE)));

		Map<String, StateBenefitPlan> currentPlanMap = new HashMap<>();
		
		if (currentCompany != null) {
			currentPlanMap = planRatesDataDao.getBenefitPlans(
				currentCompany.getRealmPlanYearId(), new HashSet<>(), futureCompany, new HashSet<>(),
				new HashSet<>(Arrays.asList(BSSApplicationConstants.LIFE_CODE)));
		}

		for (StateBenefitPlan stateBenefitPlan : futurePlanMap.values()) {
			String planId = stateBenefitPlan.getBenefitPlan();

			AdditionalPlanOptionsExport additionalPlanOptionsExport = new AdditionalPlanOptionsExport();
			additionalPlanOptionsExport.setId(planId);
			additionalPlanOptionsExport.setName(stateBenefitPlan.getDescription());
			additionalPlanOptionsExport.setPlanType(stateBenefitPlan.getPlanType());
			if (currentPlanMap.containsKey(planId)) {
				additionalPlanOptionsExport.setOfferedYearsFlag("B");
			} else {
				additionalPlanOptionsExport.setOfferedYearsFlag("F");
			}
			returnSet.add(additionalPlanOptionsExport);
		}

		for (StateBenefitPlan stateBenefitPlan : currentPlanMap.values()) {
			String planId = stateBenefitPlan.getBenefitPlan();

			if (!futurePlanMap.containsKey(planId)) {
				AdditionalPlanOptionsExport additionalPlanOptionsExport = new AdditionalPlanOptionsExport();
				additionalPlanOptionsExport.setId(planId);
				additionalPlanOptionsExport.setName(stateBenefitPlan.getDescription());
				additionalPlanOptionsExport.setPlanType(stateBenefitPlan.getPlanType());
				additionalPlanOptionsExport.setOfferedYearsFlag("C");
				returnSet.add(additionalPlanOptionsExport);
			}
		}

		return returnSet;
	}

	/**
	 * Returns a planType and Disability and Life/ADD plan ids included in the
	 * passed in parameters
	 * 
	 * @param disabilityPlanMap
	 * @param lifeAddPlanSet
	 * @return
	 */
	private Map<String, Set<String>> getOptionPlanMap(Map<String, AdditionalBenefitPlan> disabilityPlanMap,
			Set<AdditionalPlanOptionsExport> lifeAddPlanSet) {
		Map<String, Set<String>> returnMap = new HashMap<>();
		for (AdditionalBenefitPlan additionalBenefitPlan : disabilityPlanMap.values()) {
			for (DisabilityBenefitOptionPlans disabilityBenefitOptionPlans : additionalBenefitPlan.getOptionPlans()) {
				String planType = disabilityBenefitOptionPlans.getPlanType();
				String planId = disabilityBenefitOptionPlans.getId();
				if (returnMap.containsKey(planType)) {
					returnMap.get(planType).add(planId);
				} else {
					returnMap.put(planType, (new HashSet<>(Arrays.asList(planId))));
				}
			}
		}
		for (AdditionalPlanOptionsExport additionalPlanOptionsExport : lifeAddPlanSet) {
			String planType = additionalPlanOptionsExport.getPlanType();
			String planId = additionalPlanOptionsExport.getId();
			if (returnMap.containsKey(planType)) {
				returnMap.get(planType).add(planId);
			} else {
				returnMap.put(planType, (new HashSet<>(Arrays.asList(planId))));
			}
		}
		return returnMap;
	}

	
	/**
	 * Converts a HealthPlanRates object to a HealthPlanRatesExportPlan object
	 * 
	 * @param benefitPlanRatesByYear
	 * @return
	 */
	private HealthPlanRatesExportPlan convertHealthPlanRatesToHealthPlanRatesExportPlan(
			HealthPlanRates benefitPlanRatesByYear) {

		HealthPlanRatesExportPlan planRatesExportPlan = new HealthPlanRatesExportPlan();
		planRatesExportPlan.setCurrentId(benefitPlanRatesByYear.getCurrentId());
		planRatesExportPlan.setFutureId(benefitPlanRatesByYear.getFutureId());
		planRatesExportPlan.setCurrentName(benefitPlanRatesByYear.getCurrentPlanName());
		planRatesExportPlan.setFutureName(benefitPlanRatesByYear.getFuturePlanName());
		planRatesExportPlan.setPlanType(benefitPlanRatesByYear.getPlanType());
		planRatesExportPlan.setHasHeadcount(false);
		planRatesExportPlan.setOfferedYearsFlag(benefitPlanRatesByYear.getOfferedYearsFlag());
		planRatesExportPlan.setOfferedStates(benefitPlanRatesByYear.getOfferedStates());

		if (benefitPlanRatesByYear.getCurrentRates() != null) {
			planRatesExportPlan.setEmployeeOnlyCurrentCost(
					benefitPlanRatesByYear.getCurrentRates().get(CoverageCodesEnums.COV_EMPLOYEE.getCode()));
			planRatesExportPlan.setEmployeeSpouseCurrentCost(benefitPlanRatesByYear.getCurrentRates()
					.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode()));
			planRatesExportPlan.setEmployeeChildCurrentCost(
					benefitPlanRatesByYear.getCurrentRates().get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode()));
			planRatesExportPlan.setEmployeeFamilyCurrentCost(
					benefitPlanRatesByYear.getCurrentRates().get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode()));
		}

		if (benefitPlanRatesByYear.getFutureRates() != null) {
			planRatesExportPlan.setEmployeeOnlyFutureCost(
					benefitPlanRatesByYear.getFutureRates().get(CoverageCodesEnums.COV_EMPLOYEE.getCode()));
			planRatesExportPlan.setEmployeeSpouseFutureCost(
					benefitPlanRatesByYear.getFutureRates().get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode()));
			planRatesExportPlan.setEmployeeChildFutureCost(
					benefitPlanRatesByYear.getFutureRates().get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode()));
			planRatesExportPlan.setEmployeeFamilyFutureCost(
					benefitPlanRatesByYear.getFutureRates().get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode()));
		}

		populateCurrentHeadcount(planRatesExportPlan, benefitPlanRatesByYear);
		populateFutureHeadcount(planRatesExportPlan, benefitPlanRatesByYear);
		
		return planRatesExportPlan;
	}
	
	private void populateCurrentHeadcount(HealthPlanRatesExportPlan planRatesExportPlan,
			HealthPlanRates benefitPlanRatesByYear) {

		planRatesExportPlan.setEmployeeOnlyCurrentHeadcount(0L);
		planRatesExportPlan.setEmployeeSpouseCurrentHeadcount(0L);
		planRatesExportPlan.setEmployeeChildCurrentHeadcount(0L);
		planRatesExportPlan.setEmployeeFamilyCurrentHeadcount(0L);

		if (benefitPlanRatesByYear.getCurrentHeadCount() != null) {
			if (benefitPlanRatesByYear.getCurrentHeadCount().containsKey(CoverageCodesEnums.COV_EMPLOYEE.getCode())) {
				planRatesExportPlan.setEmployeeOnlyCurrentHeadcount(
						benefitPlanRatesByYear.getCurrentHeadCount().get(CoverageCodesEnums.COV_EMPLOYEE.getCode()));
				planRatesExportPlan.setHasHeadcount(true);
			}
			if (benefitPlanRatesByYear.getCurrentHeadCount()
					.containsKey(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode())) {
				planRatesExportPlan.setEmployeeSpouseCurrentHeadcount(benefitPlanRatesByYear.getCurrentHeadCount()
						.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode()));
				planRatesExportPlan.setHasHeadcount(true);
			}
			if (benefitPlanRatesByYear.getCurrentHeadCount()
					.containsKey(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode())) {
				planRatesExportPlan.setEmployeeChildCurrentHeadcount(benefitPlanRatesByYear.getCurrentHeadCount()
						.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode()));
				planRatesExportPlan.setHasHeadcount(true);
			}
			if (benefitPlanRatesByYear.getCurrentHeadCount()
					.containsKey(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode())) {
				planRatesExportPlan.setEmployeeFamilyCurrentHeadcount(benefitPlanRatesByYear.getCurrentHeadCount()
						.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode()));
				planRatesExportPlan.setHasHeadcount(true);
			}
		}
	}
	
	private void populateFutureHeadcount(HealthPlanRatesExportPlan planRatesExportPlan,
			HealthPlanRates benefitPlanRatesByYear) {

		planRatesExportPlan.setEmployeeOnlyFutureHeadcount(0L);
		planRatesExportPlan.setEmployeeSpouseFutureHeadcount(0L);
		planRatesExportPlan.setEmployeeChildFutureHeadcount(0L);
		planRatesExportPlan.setEmployeeFamilyFutureHeadcount(0L);

		if (benefitPlanRatesByYear.getFutureHeadCount() != null) {
			if (benefitPlanRatesByYear.getFutureHeadCount().containsKey(CoverageCodesEnums.COV_EMPLOYEE.getCode())) {
				planRatesExportPlan.setEmployeeOnlyFutureHeadcount(
						benefitPlanRatesByYear.getFutureHeadCount().get(CoverageCodesEnums.COV_EMPLOYEE.getCode()));
				planRatesExportPlan.setHasHeadcount(true);
			}
			if (benefitPlanRatesByYear.getFutureHeadCount()
					.containsKey(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode())) {
				planRatesExportPlan.setEmployeeSpouseFutureHeadcount(benefitPlanRatesByYear.getFutureHeadCount()
						.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode()));
				planRatesExportPlan.setHasHeadcount(true);
			}
			if (benefitPlanRatesByYear.getFutureHeadCount()
					.containsKey(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode())) {
				planRatesExportPlan.setEmployeeChildFutureHeadcount(benefitPlanRatesByYear.getFutureHeadCount()
						.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode()));
				planRatesExportPlan.setHasHeadcount(true);
			}
			if (benefitPlanRatesByYear.getFutureHeadCount()
					.containsKey(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode())) {
				planRatesExportPlan.setEmployeeFamilyFutureHeadcount(benefitPlanRatesByYear.getFutureHeadCount()
						.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode()));
				planRatesExportPlan.setHasHeadcount(true);
			}
		}
	}	

	/**
	 * Calculates the disability costs given the plans and rates
	 * 
	 * @param disabilityPlanMap
	 * @param futureAdditionalBenefitRates
	 * @param currentAdditionalBenefitRates
	 * @param bundledExchange
	 * @param realmPlanYrId
	 * @return
	 */
	private List<AdditionalPlanOptionsExport> calculateDisabilityPlanRates(
			Map<String, AdditionalBenefitPlan> disabilityPlanMap,
			Map<String, AdditionalPlanRate> futureAdditionalBenefitRates,
			Map<String, AdditionalPlanRate> currentAdditionalBenefitRates, boolean bundledExchange,
			long realmPlanYrId, String benefitExchange) {

		List<String> sdiStates = new ArrayList<>();
		sdiStates.addAll(RulesAndConfigsUtils.getSDIStates(realmPlanYrId));
		Collections.sort(sdiStates);
		String stdSdiStateText = String.join(", ", sdiStates);	
		String stdNonSdiStateText = "Other States not in " + stdSdiStateText;
		String allStates = "All States";

		List<AdditionalPlanOptionsExport> planList = new ArrayList<>();

		for (Entry<String, AdditionalBenefitPlan> disabilityPlanMapEntry : disabilityPlanMap.entrySet()) {
			AdditionalPlanOptionsExport additionalPlanOptionsExport = new AdditionalPlanOptionsExport();
			additionalPlanOptionsExport.setName(disabilityPlanMapEntry.getKey());
			additionalPlanOptionsExport.setOptionPlans(new ArrayList<>());
			AdditionalBenefitPlan additionalBenefitPlan = disabilityPlanMapEntry.getValue();

			for (DisabilityBenefitOptionPlans optionPlan : additionalBenefitPlan.getOptionPlans()) {
				String planTypeName = null;
				String offeredStatesText = allStates;
				if ((BSSApplicationConstants.STD_CODE).equals(optionPlan.getPlanType()) && bundledExchange) {
					// For TN IV, SDI indicator is used in the opposite way to indicate Other states
					offeredStatesText = getOfferedStatesTextForSTD(benefitExchange, stdSdiStateText, stdNonSdiStateText,
							allStates, optionPlan, offeredStatesText, realmPlanYrId);
				} 
				
				planTypeName = getplanTypeName(optionPlan, planTypeName);

				AdditionalPlanOptionPlanExport additionalPlanOptionPlanExport = new AdditionalPlanOptionPlanExport();
				additionalPlanOptionPlanExport.setName(optionPlan.getPlanDesc());
				additionalPlanOptionPlanExport.setPlanType(planTypeName);
				additionalPlanOptionPlanExport.setSdiPlan(optionPlan.isSdiPlan());
				additionalPlanOptionPlanExport.setOfferedStatesString(offeredStatesText);

				AdditionalPlanRate currentRate = currentAdditionalBenefitRates.get(optionPlan.getId());
				if( currentRate == null ) {
					additionalPlanOptionPlanExport.setCurrentUnit( null );
					additionalPlanOptionPlanExport.setCurrentCost( null );
				} else {
					additionalPlanOptionPlanExport.setCurrentUnit(RateUnitsEnums.valueOfDescription( currentRate.getUnit() ));
					additionalPlanOptionPlanExport.setCurrentCost( currentRate.getRate() );
				}

				additionalPlanOptionPlanExport.setFutureUnit(RateUnitsEnums
						.valueOfDescription(futureAdditionalBenefitRates.get(optionPlan.getId()).getUnit()));
				additionalPlanOptionPlanExport
						.setFutureCost(futureAdditionalBenefitRates.get(optionPlan.getId()).getRate());
				additionalPlanOptionsExport.getOptionPlans().add(additionalPlanOptionPlanExport);
			}
			Collections.sort(additionalPlanOptionsExport.getOptionPlans());
			planList.add(additionalPlanOptionsExport);
		}
		return planList;
	}

	private String getOfferedStatesTextForSTD(String benefitExchange, String stdSdiStateText, String stdNonSdiStateText,
			String allStates, DisabilityBenefitOptionPlans optionPlan, String offeredStatesText, long realmYearId) {
		String offeredText;
		if (BenExchngEnums.TRINET_IV.getBenExchng().equals(benefitExchange) && realmYearId < 86) {
			offeredText = optionPlan.isSdiPlan() ? stdSdiStateText : allStates;
		} else {
			offeredText = optionPlan.isSdiPlan() ? stdSdiStateText : stdNonSdiStateText;
		}

		return offeredText;
	}

	private String getplanTypeName(DisabilityBenefitOptionPlans optionPlan, String planTypeName) {
		if ((BSSApplicationConstants.STD_CODE).equals(optionPlan.getPlanType())) {
			planTypeName = BSSApplicationConstants.STD;
		}
		else if ((BSSApplicationConstants.LTD_CODE).equals(optionPlan.getPlanType())) {
			planTypeName = BSSApplicationConstants.LTD;
			
		} else if ((BSSApplicationConstants.LIFE_CODE).equals(optionPlan.getPlanType())) {
			planTypeName = BSSApplicationConstants.LIFE;
		}
		return planTypeName;
	}

	/**
	 * Calculates the life costs given the plans and rates
	 * 
	 * @param lifeAddPlanSet
	 * @param futureAdditionalBenefitRates
	 * @param currentAdditionalBenefitRates
	 * @return
	 */
	private List<AdditionalPlanOptionsExport> calculateLifeAddPlanRates(Set<AdditionalPlanOptionsExport> lifeAddPlanSet,
			Map<String, AdditionalPlanRate> futureAdditionalBenefitRates,
			Map<String, AdditionalPlanRate> currentAdditionalBenefitRates) {

		List<AdditionalPlanOptionsExport> planList = new ArrayList<>();

		for (AdditionalPlanOptionsExport additionalPlanOptionsExport : lifeAddPlanSet) {

			if (!("F").equals(additionalPlanOptionsExport.getOfferedYearsFlag())) {
				additionalPlanOptionsExport.setCurrentUnit(RateUnitsEnums.valueOfDescription(
						currentAdditionalBenefitRates.get(additionalPlanOptionsExport.getId()).getUnit()));
				additionalPlanOptionsExport.setCurrentCost(
						currentAdditionalBenefitRates.get(additionalPlanOptionsExport.getId()).getRate());
			}
			if (!("C").equals(additionalPlanOptionsExport.getOfferedYearsFlag())) {
				additionalPlanOptionsExport.setFutureUnit(RateUnitsEnums.valueOfDescription(
						futureAdditionalBenefitRates.get(additionalPlanOptionsExport.getId()).getUnit()));
				additionalPlanOptionsExport
						.setFutureCost(futureAdditionalBenefitRates.get(additionalPlanOptionsExport.getId()).getRate());
			}
			planList.add(additionalPlanOptionsExport);
		}
		return planList;
	}
	
	private HealthPlanRates buildHealthPlanRates(String currentId, String futureId, String currentPlanName, String futurePlanName,
			String planType, String basePlanType, String offeredYearsFlag, String vendorId,
			long portfolioId) {

		String planNameForSort = futurePlanName;
		if ("C".equals(offeredYearsFlag)) {
			planNameForSort = currentPlanName;
		}
		
		HealthPlanRates healthPlanRates = new HealthPlanRates();
		healthPlanRates.setCurrentId(currentId);
		healthPlanRates.setFutureId(futureId);
		healthPlanRates.setCurrentPlanName(currentPlanName);
		healthPlanRates.setFuturePlanName(futurePlanName);
		healthPlanRates.setPlanNameForSort(planNameForSort);
		healthPlanRates.setPlanType(planType);
		healthPlanRates.setBasePlanType(basePlanType);
		healthPlanRates.setOfferedYearsFlag(offeredYearsFlag);
		healthPlanRates.setVendorId(vendorId);
		healthPlanRates.setPortfolioId(portfolioId);
		return healthPlanRates;
	}

}