package com.trinet.ambis.service.impl.outputs;

import static com.trinet.ambis.helper.PlanCompareHelper.distinctByKey;
import static com.trinet.ambis.helper.PlanCompareHelper.filterMedPlanByAttributes;
import static com.trinet.ambis.helper.PlanCompareHelper.getCvgLvlPlanInfo;
import static com.trinet.ambis.helper.PlanCompareHelper.getPlanAttribute;
import static com.trinet.ambis.helper.PlanCompareHelper.populateMDVAttributeLabels;
import static com.trinet.ambis.helper.PlanCompareHelper.sortPlanAttributes;
import static com.trinet.ambis.helper.PlanCompareHelper.getAgeBandCvgLvlInfo;
import static com.trinet.ambis.helper.PlanCompareHelper.planRateNullCheckAndReplaceDoller;
import static com.trinet.ambis.util.PlanWordWrapUtil.calculateMaxLinesForPlan;
import static com.trinet.ambis.common.PlanAttributeConstants.LIMITS_MAP;
import static com.trinet.ambis.util.PlanWordWrapUtil.getBenTypeName;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.service.impl.HrisPlanAttributeServiceImpl;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.OutputBenefitsTypeEnums;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.rest.controllers.dto.outputs.AttributeDesc;
import com.trinet.ambis.rest.controllers.dto.outputs.BasePlanComparison;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitTypeTotal;
import com.trinet.ambis.rest.controllers.dto.outputs.EeErCvgLvlInfo;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendix;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixBenefitPlanData;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixFilters;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAttribute;
import com.trinet.ambis.rest.controllers.dto.outputs.CvgLvlPlanInfo;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanComparisonAdditonalBenefits;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.RateDetail;
import com.trinet.ambis.service.PlanAvailabilityService;
import com.trinet.ambis.service.PlanCompareService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.ProspectPlanAvailabilityService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.impl.outputs.util.PlanAppendixServiceUtil;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableRequest;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableResponse;
import com.trinet.ambis.service.model.plancompare.Attribute;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.outputs.CarrierLogoService;
import com.trinet.ambis.service.outputs.PlanAppendixService;
import com.trinet.ambis.service.outputs.PlanComparisonService;
import com.trinet.ambis.service.prospect.enums.RateTypeEnum;

import com.trinet.ambis.util.StrategyUtils;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.FileUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.trinet.ambis.service.dto.CarrierAssetDto;
import com.trinet.ambis.service.dto.CmsLogoDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @author pallu
 *
 *         Get Plan Appendix data
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class PlanAppendixServiceImpl implements PlanAppendixService {

	private final HrpDao hrpDao;

	private final PlanSelectionService planSelectionService;

	private final RealmPlyrPlanService realmPlyrPlanService;

	private final PlanRatesService planRatesService;

	private final ProspectPlanAvailabilityService prospectPlanAvailabilityService;

	private final CarrierLogoService carrierLogoService;

	private final FileUtils fileUtils;

	private final PlanAvailabilityService planAvailabilityService;
	
	private final HrisPlanAttributeServiceImpl hrisPlanAttributeService;
	
	private final StrategyDataDao strategyDataDao;
	
	private final PlanComparisonService planComparisonService;
	
	private final PlanCompareService planCompareService;


	@Value("${carrier.logo.file}")
	private String logoFile;

	@Value("${carrier.logo.v2.file}")
	private String logoV2File;

	@Override
	public Map<String, PlanAppendix> getPlanAppendixData(Company company, OutputRequest prospectRequest,
			HttpServletRequest httpRequest,CompletableFuture<Map<String, BasePlanComparison>> additionalBenefitPlanCompareDataFuture) {
		return getPlanAppendixDataValues(company, prospectRequest, httpRequest,additionalBenefitPlanCompareDataFuture);
	}

	/**
	 * This method will retrieve the Appendix data for Medical, Dental and Vision
	 * reports
	 * 
	 * @param company
	 * @param prospectRequest
	 * @return
	 */
	private Map<String, PlanAppendix> getPlanAppendixDataValues(Company company, OutputRequest prospectRequest,
			HttpServletRequest httpRequest,CompletableFuture<Map<String, BasePlanComparison>> additionalBenefitPlanCompareDataFuture) {
		Map<String, PlanAppendix> planAppendixMap = new LinkedHashMap<>();

		Map<String, List<PlanAppendixBenefitPlanData>> planAppendixDataByBenType = getPlanAppendixDataByBenType(company, prospectRequest);

		Map<String, XbssRealmPlyrPlan> plyrPlanMap = realmPlyrPlanService
				.getMapForRealmPlanYear(company.getRealmPlanYear().getId());
		
		Map<String, List<BenefitPlanRate>> fetchedBenefitPlanRates = null ;
		Map<String, EeErCvgLvlInfo> eeErCvgLvlInfoMapForGroupId = new HashMap<>();
		if (prospectRequest.getPlanAppendixFilters().getGroupId() != null) {
			prepareEeErCoverageLevelInfoForGroupId(prospectRequest, company, eeErCvgLvlInfoMapForGroupId);
			updatePlanAppendixDataByGroup(planAppendixDataByBenType, eeErCvgLvlInfoMapForGroupId);
		} else {
			fetchedBenefitPlanRates = planRatesService.getBenefitPlanRatesBy(company);
		}
		//Create a final reference to benefitPlanRates for later use
		Map<String, List<BenefitPlanRate>> benefitPlanRates = fetchedBenefitPlanRates;

		Map<String, List<BenefitPlanCompare>> planAttributes = new HashMap<>();

		Map<String, RateDetail> omsPlanRatesByPlan;
		if (CompanyServiceHelper.isTibProspect(company)) {
			Map<String, Set<String>> omsPlanIdsByBenType = planAppendixDataByBenType.entrySet().stream()
					.filter(entry -> OutputBenefitsTypeEnums.isMDVBenefitType(entry.getKey()))
					.collect(Collectors.toMap(
							Map.Entry::getKey,
							entry -> entry.getValue().stream()
									.map(PlanAppendixBenefitPlanData::getBenefitPlan)
									.collect(Collectors.toSet())
					));

			omsPlanRatesByPlan = planComparisonService.getOMSPlanRatesByPlan(company, omsPlanIdsByBenType);
		} else {
			omsPlanRatesByPlan = new HashMap<>();
		}

		Map<List<String>, String> phase2CarrierLogoMap = new HashMap<>();
		if (AppRulesAndConfigsUtils.isBssOutputPhase2Enabled()) {
			CmsLogoDto cmsLogoDto = carrierLogoService.fetchCarrierLogos();
			CarrierAssetDto carrierAssetDto = carrierLogoService.fetchCarrierLogos(logoV2File, new TypeReference<CarrierAssetDto>() {}, fileUtils::readJsonData);
			if (carrierAssetDto != null && cmsLogoDto != null && cmsLogoDto.getAssets() != null) {
				Map<String, List<String>> carrierUidMap = carrierAssetDto.getAssestDetails().stream()
						.distinct()
						.collect(Collectors.toMap(CarrierAssetDto.AssestDetails::getUid, CarrierAssetDto.AssestDetails::getCarrierNames));

				carrierUidMap.forEach((key, val) -> {
					Optional<CmsLogoDto.LogoDto> logoDetails = cmsLogoDto.getAssets().stream().filter(cms -> cms.getUid().equals(key)).findAny();
					if (logoDetails.isPresent()) {
						phase2CarrierLogoMap.put(val, logoDetails.get().getUrl());
					}
				});
			}
		}

        for (Map.Entry<String, List<PlanAppendixBenefitPlanData>> benefitPlanEntry : planAppendixDataByBenType
				.entrySet()) {
			String benType = benefitPlanEntry.getKey();
			boolean includeBenType = isIncludeBenefitType(benType, planAppendixDataByBenType.keySet());

			if (!includeBenType) {
				continue;
			}
			boolean isMDV = OutputBenefitsTypeEnums.isMDVBenefitType(benType);
			boolean isMedical = OutputBenefitsTypeEnums.isMedicalBenefitType(benType);
			Set<String> planIds = planAppendixDataByBenType.entrySet().stream()
					.filter(entry -> entry.getKey().equals(benType))
					.flatMap(entry -> entry.getValue().stream().map(PlanAppendixBenefitPlanData::getBenefitPlan))
					.collect(Collectors.toSet());
			try {
			    	List<BenefitPlanCompare> benefitPlanCompares;
				if (CompanyServiceHelper.isTibProspect(company) && isMDV){
				    benefitPlanCompares = hrisPlanAttributeService.getPlanAttributesByBenefitType(planIds, PlanTypesEnum.getName(benType)).get();
				}
				else{
					benefitPlanCompares = planCompareService
							.getPlanAttributes(planIds, company.getRealmPlanYear().getPlanYearEnd(),
									BSSApplicationConstants.databaseTemplatesMap.get(benType), httpRequest)
							.get();
					if(BSSApplicationConstants.NATIONAL.equals(company.getRateType())){
						// Update description from planAppendixDataByBenType for non-TIB companies
						Map<String, String> planDescriptionMap = benefitPlanEntry.getValue().stream()
								.filter(p -> p.getBenefitPlan() != null && p.getDescription() != null)
								.filter(distinctByKey(PlanAppendixBenefitPlanData::getBenefitPlan))
								.collect(Collectors.toMap(PlanAppendixBenefitPlanData::getBenefitPlan, PlanAppendixBenefitPlanData::getDescription));
						benefitPlanCompares.forEach(bpc -> {
							String description = planDescriptionMap.get(bpc.getPlanId());
							if (description != null) {
								bpc.setName(description);
							}
						});
					}
				}
				List<BenefitPlanCompare> benefitPlans = benefitPlanCompares.stream()
						.filter(plan -> Objects.nonNull(plan.getTemplate()))
						.filter(plan -> !plan.getTemplate().isEmpty()).collect(Collectors.toList());
				
				planAttributes.put(benType, benefitPlans);
			} catch (InterruptedException | ExecutionException e) {
				Thread.currentThread().interrupt();
				throw new BSSApplicationException(e, new BSSApplicationError(
						"Error occured while calling plan view API for generating the plan appendix report"));
			}

			List<AttributeDesc> attributeLabels = populateMDVAttributeLabels(benType, planAttributes);
			PlanAppendix planAppendix = planAppendixMap.get(benType);
			if (planAppendixMap.containsKey(benType)) {
				if (planAppendixMap.get(benType).getAttributeNames().isEmpty()) {
					planAppendix.setAttributeNames(attributeLabels);
				}
			} else {
				if (Objects.nonNull(attributeLabels)) {
					planAppendix = new PlanAppendix();
					planAppendix.setAttributeNames(attributeLabels);

					List<PlanAttribute> allAttributes = new LinkedList<>();
					List<BenefitPlanCompare> compPlans = planAttributes.get(benType);
					Map<String, String> planRegionMap = benefitPlanEntry.getValue().stream()
							.filter(distinctByKey(PlanAppendixBenefitPlanData::getBenefitPlan))
							.filter(region -> Objects.nonNull(region.getRegion()))
							.collect(Collectors.toMap(PlanAppendixBenefitPlanData::getBenefitPlan,
									PlanAppendixBenefitPlanData::getRegion));
					compPlans.stream().forEach(plan -> {
						String carrierLogoUrl;
						if (AppRulesAndConfigsUtils.isBssOutputPhase2Enabled()) {
							Optional<Map.Entry<List<String>, String>> carrierLogo = phase2CarrierLogoMap.entrySet().stream().filter(entry -> entry.getKey().contains(plan.getCarrier())).findAny();
							carrierLogoUrl = carrierLogo.map(Map.Entry::getValue).orElse(null);
						} else {
							carrierLogoUrl = carrierLogoService.fetchLogoUrl().apply(plan.getCarrier());
						}
						plan.setCarrierLogoUrl(carrierLogoUrl);
						Optional<Attribute> primaryCareVisitAttribute = Optional.empty();
						PlanAppendixFilters planAppendixFilters = prospectRequest.getPlanAppendixFilters();
						if (Objects.nonNull(planAppendixFilters)
								&& OutputBenefitsTypeEnums.MEDICAL.getCode().equalsIgnoreCase(benType)) {
							primaryCareVisitAttribute = getCopayAttributes.apply(plan);
						}
						if ((primaryCareVisitAttribute.isEmpty() || checkMinAndMaxValue
                                .test(primaryCareVisitAttribute.get().getValue(), planAppendixFilters))
								&& (isMedical && PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(plan,
										planAppendixFilters))
								|| !isMedical) {
							PlanAttribute planAttribute = getPlanAttribute(plan, true);
							planAttribute.setRegion(planRegionMap.get(plan.getPlanId()));
							if(prospectRequest.getPlanAppendixFilters().getGroupId() != null) {
								planAttribute.setEeErPlanRates(eeErCvgLvlInfoMapForGroupId.get(plan.getPlanId()));
							} else {
								if(CompanyServiceHelper.isTibProspect(company) && isMDV &&  !omsPlanRatesByPlan.isEmpty()) {
									RateDetail rateDetail =  omsPlanRatesByPlan.get(plan.getPlanId());
									planAttribute.setPlanRates(getCvgLvlFromTIBPlanRates(rateDetail));
								} else {
                                    Map<String, BigDecimal> planCostMap;
									planCostMap = StrategyUtils.getPlanCost(benefitPlanRates.get(plan.getPlanId()));
									Map<String, String> costWithCvg = planCostMap.entrySet().stream()
											.collect(Collectors.toMap(cvg -> CoverageCodesEnums.valueOfCode(cvg.getKey()),
													cvg -> String.valueOf(cvg.getValue())));
									planAttribute.setPlanRates(getCvgLvlPlanInfo(costWithCvg));
								}

							}
							allAttributes.add(planAttribute);
						}
					});
					boolean isIncludeCarrierSorting = prospectRequest.getPlanAppendixFilters().isIncludeCarrierSorting();

					if(AppRulesAndConfigsUtils.isBssOutputPhase2Enabled()) {
						List<PlanAttribute> filteredAttributes = filterMedPlanByAttributes(benType).apply(prospectRequest, allAttributes);
						List<PlanAttribute> sortedAttributes = sortPlanAttributeByCarrierRegionAndCost(filteredAttributes);
						if(AppRulesAndConfigsUtils.isMdvPageBreakEnabled()) {
							int maxLinesFirstPage = BSSApplicationConstants.PLAN_APPENDIX_MAX_LINES_FIRST_PAGE;
							int maxLinesSubsequentPages = BSSApplicationConstants.PLAN_APPENDIX_MAX_LINES_SUBSEQUENT_PAGES;
							boolean isGeneratePlanAppendixOnly = !CollectionUtils.isEmpty(prospectRequest.getTemplateNames())
									&& prospectRequest.getTemplateNames().size() == 1
									&& prospectRequest.getTemplateNames().contains(ProspectConstants.PLAN_APPENDIX);

							if (isGeneratePlanAppendixOnly){
								String firstBenType = (String) httpRequest.getAttribute(BSSApplicationConstants.PLAN_APPENDIX_FIRST_BEN_TYPE);
								if(firstBenType != null && firstBenType.equals(benType)) {
								maxLinesFirstPage = maxLinesFirstPage - BSSApplicationConstants.PLAN_APPENDIX_HEADER_LINES;
							    }
							}

							planAppendix.setMaxLinesFirstPage(maxLinesFirstPage);
							planAppendix.setMaxLinesSubsequentPages(maxLinesSubsequentPages);
							int currentLines = 0;
							boolean isFirstPage = true;
							String currentCarrierName = null;
							String benTypeName = getBenTypeName(benType);
							String key = ProspectConstants.PLAN_APPENDIX+"_"+ benTypeName;
							Map<String, Double> limits = LIMITS_MAP.getOrDefault(key.toUpperCase(), java.util.Collections.emptyMap());
							for (int i = 0; i < sortedAttributes.size(); i++) {
							PlanAttribute plan = sortedAttributes.get(i);
							int planLines = calculateMaxLinesForPlan(plan, attributeLabels, limits, benTypeName);
							int carrierLines = 0;
							if (!Objects.equals(plan.getCarrierName(), currentCarrierName)) {
									carrierLines = 1;
								}
								int currentMaxLines = isFirstPage ? maxLinesFirstPage : maxLinesSubsequentPages;
								if (currentLines > 0 && currentLines + planLines + carrierLines > currentMaxLines) {
									plan.setPageBreak(true);
									currentLines = planLines + carrierLines;
									isFirstPage = false;
								} else {
									currentLines += planLines + carrierLines;
								}
								currentCarrierName = plan.getCarrierName();
							}
						}
						planAppendix.setPlanAttributes(sortedAttributes);
					}
					else{
						planAppendix.setPlanAttributes(filterMedPlanByAttributes(benType).andThen(sortPlanAttributes(isIncludeCarrierSorting))
								.apply(prospectRequest, allAttributes));
					}
				}
			}

			planAppendixMap.put(benType, planAppendix);
		}
		
		updatePlanAppendixWithAdditionalBenefitsCompareData(company, prospectRequest, additionalBenefitPlanCompareDataFuture,
				planAppendixMap);

		return planAppendixMap;
	}

	private void updatePlanAppendixWithAdditionalBenefitsCompareData(Company company, OutputRequest prospectRequest,
			CompletableFuture<Map<String, BasePlanComparison>> additionalBenefitPlanCompareDataFuture,
			Map<String, PlanAppendix> planAppendixMap) {
		Map<String, BasePlanComparison> additionalBenefitsPlanComparisonMap = extractPlanComparisonDataFromFutureObj(company,
				additionalBenefitPlanCompareDataFuture);
		
		if (Objects.nonNull(additionalBenefitsPlanComparisonMap) && !additionalBenefitsPlanComparisonMap.isEmpty()) {
			List<String> lifeAndDisabilityCodes = getAdditionalBenefitCodes(prospectRequest);
			if (lifeAndDisabilityCodes != null && !lifeAndDisabilityCodes.isEmpty()) {
				for (String code : lifeAndDisabilityCodes) {
					PlanComparisonAdditonalBenefits benefitPlanComparision = (PlanComparisonAdditonalBenefits) additionalBenefitsPlanComparisonMap
							.get(code);
					if (benefitPlanComparision != null) {
						PlanAppendix planAppendix = new PlanAppendix();
						planAppendix.setAdditionalGroupAttributeNames(benefitPlanComparision.getAttributeNames());
						planAppendix.setAdditionalGroupDetails(benefitPlanComparision.getAvailableGroupDetails());
						planAppendixMap.put(code, planAppendix);
					}
				}
			}
		}
	}

	private List<String> getAdditionalBenefitCodes(OutputRequest prospectRequest) {
		return prospectRequest.getBenefitTypes().stream()
				.filter(reqBenType -> (reqBenType.equals(OutputBenefitsTypeEnums.LIFE.getCode()))
						|| (reqBenType.equals(OutputBenefitsTypeEnums.DISABILITY.getCode())))
				.collect(Collectors.toList());
	}
	
	private Map<String, BasePlanComparison> extractPlanComparisonDataFromFutureObj(Company company,
			CompletableFuture<Map<String, BasePlanComparison>> additionalBenefitPlanCompareDataFuture) {
		Map<String, BasePlanComparison> planComparisonData = null;
		if (additionalBenefitPlanCompareDataFuture != null) {
			try {
				planComparisonData = additionalBenefitPlanCompareDataFuture.get();
			} catch (InterruptedException e) {
				log.error(
						"Thread is interrupted while generating additional benefits plan comparison report for prospect : {} exception : ",
						company.getCode(), e);
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				log.error("Exception occured while generating additional benefits plan comparison report for prospect : {} exception : ",
						company.getCode(), e);
			}
		}
		return planComparisonData;
	}

	private List<PlanAppendixBenefitPlanData> getPlansForOms(String planType, Company company,
															 OutputRequest prospectRequest) {
		List<PlanAppendixBenefitPlanData> planAppendixData = new ArrayList<>();
		if (prospectRequest.getBenefitTypes().contains(planType) && CompanyServiceHelper.isTibProspect(company)) {
			long strategyId = Long.parseLong(prospectRequest.getTnStrategyId());
			List<PlanSelection> planSections = planSelectionService.findByStrategyIdAndPlanType(strategyId, planType);
			for (PlanSelection planSelection : planSections) {
				PlanAppendixBenefitPlanData planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
				planAppendixBenefitPlanData.setPlanType(planSelection.getPlanType());
				planAppendixBenefitPlanData.setDescription("");
				planAppendixBenefitPlanData.setBenefitPlan(planSelection.getBenefitPlan());
				planAppendixBenefitPlanData.setRegion("");
				planAppendixData.add(planAppendixBenefitPlanData);
			}
		}
		return planAppendixData;
	}

	private Map<String, List<PlanAppendixBenefitPlanData>> getPlanAppendixDataByBenType(Company company,
			OutputRequest prospectRequest) {
		Map<String, List<PlanAppendixBenefitPlanData>> planAppendixDataByBenType = new HashMap<>();
		
		List<String> mdvBenTypeCodes = prospectRequest.getBenefitTypes().stream()
				.filter(reqBenType -> (!(reqBenType.equals(OutputBenefitsTypeEnums.LIFE.getCode()))
						&& !(reqBenType.equals(OutputBenefitsTypeEnums.DISABILITY.getCode()))))
				.collect(Collectors.toList());
		
		if (prospectRequest.getPlanAppendixFilters().isIncludeOnlyEeLocationPlans()) {
			prospectRequest.getPlanAppendixFilters().setRegions(company.getEmployeeRegions());
		}
		
		if (CollectionUtils.isNotEmpty(prospectRequest.getPlanAppendixFilters().getRegions())) {
			boolean includeSubRegions = prospectRequest.getPlanAppendixFilters().isIncludeRegionsFlag() || prospectRequest.getPlanAppendixFilters().isIncludeOnlyEeLocationPlans();
			planAppendixDataByBenType = planSelectionService.findAppendixReportBenefitPlansBy(company, prospectRequest.getTnStrategyId(),
					prospectRequest.getPlanAppendixFilters().getRegions(), mdvBenTypeCodes, !includeSubRegions); // Will send 'false' value to filterSubregions indicator when EE/ER feature flag is removed from UI
		}

		if (prospectRequest.getPlanAppendixFilters().isIncludeOnlyEeLocationPlans()) {
			List<String> planIds = planAppendixDataByBenType.entrySet().stream()
					.flatMap(entry -> entry.getValue().stream().map(PlanAppendixBenefitPlanData::getBenefitPlan))
					.distinct().collect(Collectors.toList());
			Map<String, List<String>> employeeEligiblePlans = prospectPlanAvailabilityService
					.getProspectEmployeePlansByPlanType(company, planIds);
			planAppendixDataByBenType.entrySet().stream()
					.filter(entry -> employeeEligiblePlans.containsKey(entry.getKey()))
					.forEach(entry -> entry.getValue().removeIf(
							plan -> !employeeEligiblePlans.get(entry.getKey()).contains(plan.getBenefitPlan())));
		}

		if (CollectionUtils.isNotEmpty(prospectRequest.getPlanAppendixFilters().getZipCodes())) {
			Map<String, List<PlanAppendixBenefitPlanData>> planAppendixDataForZipCodeStates = getPlanAppendexDataForZipCodes(company, prospectRequest, mdvBenTypeCodes);
			
			if(!planAppendixDataForZipCodeStates.isEmpty()) {
				if(planAppendixDataByBenType.isEmpty()) {
					planAppendixDataByBenType.putAll(planAppendixDataForZipCodeStates);
				} else {
					for(Map.Entry<String, List<PlanAppendixBenefitPlanData>> entry: planAppendixDataForZipCodeStates.entrySet()) {
						planAppendixDataByBenType.merge(entry.getKey(), new ArrayList<>(entry.getValue()), (existingList, newList) -> {existingList.addAll(newList); return existingList;});
					}
				}
			}
		}
		for (String planType : Arrays.asList(BSSApplicationConstants.MEDICAL_PLAN_TYPE,
			BSSApplicationConstants.DENTAL_PLAN_TYPE, BSSApplicationConstants.VISION_PLAN_TYPE)) {
			List<PlanAppendixBenefitPlanData> plansForOms = getPlansForOms(planType, company, prospectRequest);
			if (!plansForOms.isEmpty()) {
				planAppendixDataByBenType.computeIfAbsent(planType, k -> new ArrayList<>()).addAll(plansForOms);
			}
	    }
		
		return planAppendixDataByBenType;
	}
	
	private void prepareEeErCoverageLevelInfoForGroupId(OutputRequest prospectRequest, Company company, Map<String, EeErCvgLvlInfo> eeErCvgLvlInfoMap) {
		String groupId = prospectRequest.getPlanAppendixFilters().getGroupId();

		List<Object[]> planContributions = strategyDataDao.getPlanContributionsByStrategyId(company,Long.parseLong(prospectRequest.getTnStrategyId()), true);
		if (planContributions != null && !planContributions.isEmpty()) {
			planContributions.stream()
					.filter(planContribution -> isMatchingGroupId(planContribution, groupId))
					.forEach(planContribution -> prepareEeErCvgLvlInfoMap(eeErCvgLvlInfoMap, planContribution));
		}
	}
	
	private boolean isMatchingGroupId(Object[] planContribution, String groupId) {
	    return ((BigDecimal) planContribution[7]).longValue() == Long.parseLong(groupId);
	}

	private void prepareEeErCvgLvlInfoMap(Map<String, EeErCvgLvlInfo> cvgLvlInfoMap, Object[] planContribution) {
		String benefitPlanId = (String) planContribution[8];

		EeErCvgLvlInfo covLvlInfo = cvgLvlInfoMap.computeIfAbsent(benefitPlanId, k -> new EeErCvgLvlInfo());

		String coverageLevel = (String) planContribution[2];
		BigDecimal empRate = ((BigDecimal) planContribution[5]);
		BigDecimal emplrRate = ((BigDecimal) planContribution[6]);

		BenefitTypeTotal benefitTypeTotal = createBenefitTypeTotal(empRate, emplrRate);

		if (CoverageCodesEnums.COV_EMPLOYEE.getCode().equals(coverageLevel)) {
			covLvlInfo.setEmployeeOnly(benefitTypeTotal);
		} else if (CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode().equals(coverageLevel)) {
			covLvlInfo.setEmployeeSpouse(benefitTypeTotal);
		} else if (CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode().equals(coverageLevel)) {
			covLvlInfo.setEmployeeChildren(benefitTypeTotal);
		} else if (CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode().equals(coverageLevel)) {
			covLvlInfo.setFamily(benefitTypeTotal);
		}
		
	}

	private BenefitTypeTotal createBenefitTypeTotal(BigDecimal empRate, BigDecimal emplrRate) {
	    BigDecimal total = empRate.add(emplrRate).setScale(2, RoundingMode.HALF_UP);
	    return BenefitTypeTotal.builder()
	            .eeAmount(empRate.setScale(2, RoundingMode.HALF_UP))
	            .erAmount(emplrRate.setScale(2, RoundingMode.HALF_UP))
	            .total(total)
	            .build();
	}
	
	private void updatePlanAppendixDataByGroup(
			Map<String, List<PlanAppendixBenefitPlanData>> planAppendixDataByBenType,
			Map<String, EeErCvgLvlInfo> eeErCvgLvlInfoMapForGroupId) {
		Set<String> benefitPlansByGroupId = eeErCvgLvlInfoMapForGroupId != null ? eeErCvgLvlInfoMapForGroupId.keySet() : new HashSet<>();
		
		//Remove entries from existing planAppendixDataByBenType map when no matching benefit plans are in eeErCvgLvlInfoMapForGroupId map.
		planAppendixDataByBenType.entrySet().removeIf(planType -> {
			List<PlanAppendixBenefitPlanData> planAppendixBenefitPlanDataByGroupId = planType.getValue().stream()
					.filter(planData -> benefitPlansByGroupId.contains(planData.getBenefitPlan())).collect(Collectors.toList());
			if (planAppendixBenefitPlanDataByGroupId.isEmpty()) {
				return true;
			} else {
				planType.setValue(planAppendixBenefitPlanDataByGroupId);
				return false;
			}
		});
	}

	private Function<BenefitPlanCompare, Optional<Attribute>> getCopayAttributes = plan -> plan.getTemplate().stream()
			.flatMap(template -> template.getChildren().stream()).filter(child -> child.getId() == 11)
			.filter(child -> child.getValue() != null && child.getValue().startsWith("$") && !child.getValue().contains("ded")
					&& !child.getValue().contains("%"))
			.findAny();

	private BiPredicate<String, BigDecimal> isValueGTCopay = (dbValue,
			reqCopayValue) -> new BigDecimal(dbValue).compareTo(reqCopayValue) >= 0;

	private BiPredicate<String, BigDecimal> isValueLTCopay = (dbValue,
			reqCopayValue) -> new BigDecimal(dbValue).compareTo(reqCopayValue) <= 0;

	private BiPredicate<String, PlanAppendixFilters> checkMinAndMaxValue = (value, planAdxFilter) -> (Objects
			.isNull(planAdxFilter.getCopayMin()) && Objects.isNull(planAdxFilter.getCopayMax()))
			|| ((Objects.nonNull(planAdxFilter.getCopayMin()) && Objects.nonNull(planAdxFilter.getCopayMax())
					&& (isValueGTCopay.test(value.substring(1, value.length()), planAdxFilter.getCopayMin())
							&& isValueLTCopay.test(value.substring(1, value.length()), planAdxFilter.getCopayMax())))
					|| (Objects.nonNull(planAdxFilter.getCopayMin())
							&& planAdxFilter.getCopayMin().compareTo(BigDecimal.ZERO) >= 0
							&& Objects.isNull(planAdxFilter.getCopayMax())
							&& isValueGTCopay.test(value.substring(1, value.length()), planAdxFilter.getCopayMin()))
					|| (Objects.nonNull(planAdxFilter.getCopayMax())
							&& planAdxFilter.getCopayMax().compareTo(BigDecimal.ZERO) >= 0
							&& Objects.isNull(planAdxFilter.getCopayMin())
							&& isValueLTCopay.test(value.substring(1, value.length()), planAdxFilter.getCopayMax())));

	/*
	 * When the benefit type is Voluntary Dental Plan or Voluntary Vision Plan, then
	 * check if the Group Dental Plan or Group Vision Plan is included in the
	 * appendix. If so, then exclude the Voluntary Dental Plan or Voluntary Vision
	 * Plan from the appendix.
	 */
	private boolean isIncludeBenefitType(String benefitType, Set<String> benTypesInAppendix) {
		boolean includeBenefitType = true;
		if (BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE.equals(benefitType)
				&& benTypesInAppendix.contains(BSSApplicationConstants.DENTAL_PLAN_TYPE)) {
			includeBenefitType = false;
		} else if (BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE.equals(benefitType)
				&& benTypesInAppendix.contains(BSSApplicationConstants.VISION_PLAN_TYPE)) {
			includeBenefitType = false;
		}
		return includeBenefitType;
	}
	
	private Map<String, List<PlanAppendixBenefitPlanData>> getPlanAppendexDataForZipCodes(Company company, OutputRequest prospectRequest, List<String> mdvBenTypeCodes) {
		Map<String, List<PlanAppendixBenefitPlanData>> planAppendixDataForZipCodeStates = new HashMap<>();
		Map<String, String> zipAndstates = getZipCodesAndStatesBy(prospectRequest.getPlanAppendixFilters().getZipCodes());
		if (zipAndstates != null && !zipAndstates.isEmpty()) {
			planAppendixDataForZipCodeStates = planSelectionService.findAppendixReportBenefitPlansBy(company,
					prospectRequest.getTnStrategyId(), zipAndstates.values().stream().collect(Collectors.toList()),
					mdvBenTypeCodes, false);
			List<String> zipAndStatePlanIds = getPlanIdsForZipAndStates(company, zipAndstates, planAppendixDataForZipCodeStates);

			planAppendixDataForZipCodeStates.entrySet().removeIf(planType -> {
				List<PlanAppendixBenefitPlanData> zipCodeBenefitPlans = planType.getValue().stream()
						.filter(planData -> zipAndStatePlanIds.contains(planData.getBenefitPlan())).collect(Collectors.toList());
				if (zipCodeBenefitPlans.isEmpty()) {
					return true;
				} else {
					planType.setValue(zipCodeBenefitPlans);
					return false;
				}
			});
		}
		return planAppendixDataForZipCodeStates;
	}
	
	private Map<String, String> getZipCodesAndStatesBy(List<String> zipCodes) {
		return hrpDao.getZipCodesAndStatesBy(zipCodes);
	}

	private List<String> getPlanIdsForZipAndStates(Company company, Map<String, String> zipAndstates, Map<String, List<PlanAppendixBenefitPlanData>> planAppendixDataForZipCodeStates) {

		List<String> planIds = new ArrayList<>();

		PlanAvailableRequest planAvailabilityReq = createPlanAvailabilityRequest(company, zipAndstates,
				planAppendixDataForZipCodeStates);

		CompletableFuture<List<PlanAvailableResponse>> availablePlansCompletableFuture = planAvailabilityService
				.getBenefitPlanAvailability(planAvailabilityReq);
		List<PlanAvailableResponse> availablePlansByZip = availablePlansCompletableFuture.join();

		for (PlanAvailableResponse planAvailableResponse : availablePlansByZip) {
			planAvailableResponse.getPlansByBenType()
					.forEach(benTypePlans -> planIds.addAll(benTypePlans.getPlanIds()));
		}

		return planIds;
	}

	private PlanAvailableRequest createPlanAvailabilityRequest(Company company, Map<String, String> zipAndstates, Map<String, List<PlanAppendixBenefitPlanData>> planAppendixDataForZipCodeStates) {
		List<PlanAppendixBenefitPlanData> planAppendixBenefitPlans = planAppendixDataForZipCodeStates.values().stream()
				.flatMap(List::stream).collect(Collectors.toList());
		List<String> benefitPlans = planAppendixBenefitPlans.stream().map(PlanAppendixBenefitPlanData::getBenefitPlan)
				.collect(Collectors.toList());
		
		PlanAvailableRequest planAvailabilityReq = new PlanAvailableRequest();
		planAvailabilityReq.setCloneBenefitProgram(company.getRealmPlanYear().getCloneProgram());
		planAvailabilityReq.setEffectiveDate(company.getRealmPlanYear().getPlanYearStart());
		List<PlanAvailableRequest.Location> locationList = new ArrayList<>();
		zipAndstates.forEach((key, value) -> {
			PlanAvailableRequest.Location location = new PlanAvailableRequest.Location();
			location.setPostalCode(key);
			location.setState(value);
			locationList.add(location);
		});

		planAvailabilityReq.setLocations(locationList);
		planAvailabilityReq.setPlans(benefitPlans);

		return planAvailabilityReq;
	}

	private CvgLvlPlanInfo getCvgLvlFromTIBPlanRates(RateDetail rateDetail){
		CvgLvlPlanInfo  cvgLvlPlanInfo = null;
		if (Objects.nonNull(rateDetail) && Objects.nonNull(rateDetail.getRateType()) && rateDetail.getRateType().equals(RateTypeEnum.FOUR_TIER.getCode())) {
			final Map<String, String> mapOfTierRateToCost = rateDetail.getTierRates().stream()
					.filter(tierRate -> Objects.nonNull(tierRate.getCvgTierCode()))
					.filter(tierRate -> Objects.nonNull(tierRate.getCost()))
					.collect(Collectors.toMap(tierRate ->(CoverageCodesEnums.valueOfCode(tierRate.getCvgTierCode())),
							tierRate -> String.valueOf(tierRate.getCost())));
			cvgLvlPlanInfo = getCvgLvlPlanInfo(mapOfTierRateToCost);
		} else if (Objects.nonNull(rateDetail) && Objects.nonNull(rateDetail.getRateType()) && rateDetail.getRateType().equals(RateTypeEnum.AGE_BANDED.getCode())) {
			cvgLvlPlanInfo = getAgeBandCvgLvlInfo();
		}
		return cvgLvlPlanInfo;
	}

	private List<PlanAttribute> sortPlanAttributeByCarrierRegionAndCost(List<PlanAttribute> attributes) {
		if (CollectionUtils.isEmpty(attributes)) {
			return attributes;
		}
		return attributes.stream()
				.sorted(Comparator
						.comparing(PlanAttribute::getCarrierName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
						.thenComparing(PlanAttribute::getRegion, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
						.thenComparing(planRateNullCheckAndReplaceDoller(), Comparator.nullsFirst(Comparator.naturalOrder())))
				.collect(Collectors.toList());
	}
}
