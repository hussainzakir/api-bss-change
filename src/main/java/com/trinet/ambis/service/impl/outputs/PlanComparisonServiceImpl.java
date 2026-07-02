package com.trinet.ambis.service.impl.outputs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.RateDetail;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.TierRate;
import com.trinet.ambis.service.*;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse.RateDetails;
import com.trinet.ambis.service.outputs.*;
import com.trinet.ambis.service.prospect.enums.RateTypeEnum;
import com.trinet.ambis.util.FileUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.OutputBenefitsTypeEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.rest.controllers.dto.outputs.BasePlanComparison;
import com.trinet.ambis.rest.controllers.dto.outputs.CurrentTrinetPlans;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanComparisonAdditonalBenefits;
import com.trinet.ambis.service.AdditionalBenefitPlanService;
import com.trinet.ambis.service.BenefitsPlanViewService;
import com.trinet.ambis.service.HrisPlanAttributeService;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.outputs.PlanCompareBuilder;
import com.trinet.ambis.service.outputs.PlanComparisionAsyncService;
import com.trinet.ambis.service.outputs.PlanComparisonService;
import com.trinet.ambis.service.outputs.PlanPopulator;
import com.trinet.ambis.service.outputs.PopulatorBuilder;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes;

import lombok.extern.log4j.Log4j2;

/**
 * @author ramesh
 *
 *         Get Plan Comparison data
 */
@Service
@Log4j2
public class PlanComparisonServiceImpl implements PlanComparisonService {

	@Autowired
	private PlanPopulator planPopulator;

	@Autowired
	private EmployeePlanAssignmentService eePlanAssignmentService;

	@Autowired
	private ProspectPlanAvailabilityService prospectPlanAvailabilityService;

	@Autowired
	private PlanComparisionAsyncService planComparisionAsyncService;
	
	@Autowired
    private AdditionalBenefitPlanService additionalBenefitPlanService;

	@Autowired
	private CarrierLogoService carrierLogoService;

	@Autowired
	private FileUtils fileUtils;
	
	@Autowired
	private BenefitsPlanViewService benefitsPlanViewService;

	@Autowired
	private HrisPlanAttributeService hrisPlanAttributeService;
	
	@Autowired
	private PlanCompareService planCompareService;

	@Value("${carrier.logo.file}")
	private String logoFile;
	
	@Override
	@Async
	public CompletableFuture<Map<String, BasePlanComparison>> getPlanComparisonData(Company company,
			OutputRequest prospectRequest, HttpServletRequest httpRequest) {
		log.info("******* Generating Plan Comparison Report Data *******");
		StopWatch taskWatch = new StopWatch("Plan Comparison");
		taskWatch.start();
		Map<String, BasePlanComparison> planComparisonMap = new LinkedHashMap<>();
		getMDVBenefitsCompareData(company, prospectRequest, httpRequest, planComparisonMap);
		taskWatch.stop();
		log.info(String.format("%s finished in :: %s", taskWatch.getId(), taskWatch.getTotalTimeMillis()));
		return CompletableFuture.completedFuture(planComparisonMap);
	}

	@Override
	public Map<String, RateDetail> getOMSPlanRatesByPlan(Company company, Map<String, Set<String>> planIdsByBenType) {
		Map<String, RateDetail> omsPlanRatesByPlan = new HashMap<>();

		for (Map.Entry<String, Set<String>> entry : planIdsByBenType.entrySet()) {
			String benType = PlanTypesEnum.getName(entry.getKey());
			Set<String> planIds = entry.getValue();

			// Fetch TIB plan rates for the company
			Map<String, HrisPlanResponse> tibPlanRatesMap = prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, benType)
					.stream()
					.collect(Collectors.toMap(tibPlan -> String.valueOf(tibPlan.getPlanId()), tibPlan -> tibPlan));

			if (!tibPlanRatesMap.isEmpty() && !CollectionUtils.isEmpty(planIds)) {
				planIds.stream()
						.filter(tibPlanRatesMap::containsKey)
						.forEach(planId -> {
							HrisPlanResponse tibPlan = tibPlanRatesMap.get(planId);
							String rateType = tibPlan.getRateDetails().getRateType();
							RateDetail rateDetail = RateDetail.builder()
									.rateType(Optional.ofNullable(tibPlan.getRateDetails())
											.map(RateDetails::getRateType)
											.orElse(null))
									.tierRates(Optional.ofNullable(tibPlan.getRateDetails())
											.map(RateDetails::getRatesByZip)
											.filter(ratesByZip -> !ratesByZip.isEmpty())
											.map(ratesByZips -> ratesByZips.get(0).getRates().stream() // Consider only the first list of rates
													.map(rate -> TierRate.builder()
															.cvgTierCode(RateTypeEnum.FOUR_TIER.getCode().equals(rateType) ?  CoverageCodesEnums.valueOfId(rate.getTierCode()) : rate.getTierCode())
															.cost(BigDecimal.valueOf(rate.getRate()))
															.build())
													.collect(Collectors.toList()))
											.orElse(Collections.emptyList()))
									.build();
							omsPlanRatesByPlan.put(planId, rateDetail);
						});
			}
		}
		return omsPlanRatesByPlan;
	}

	private void getMDVBenefitsCompareData(Company company, OutputRequest prospectRequest,
			HttpServletRequest httpRequest, Map<String, BasePlanComparison> planComparisonMap) {
		List<String> mDVBenefitTypeCodes = prospectRequest.getBenefitTypes().stream()
				.filter(key -> !key.equalsIgnoreCase(OutputBenefitsTypeEnums.LIFE.getCode()))
				.filter(key -> !key.equalsIgnoreCase(OutputBenefitsTypeEnums.DISABILITY.getCode()))
				.collect(Collectors.toList());

		if (CollectionUtils.isNotEmpty(mDVBenefitTypeCodes)) {
			Map<String, BasePlanComparison> mdvPlans = getBenefitTypePlanComparison(company, prospectRequest,
					mDVBenefitTypeCodes, httpRequest).get();
			planComparisonMap.putAll(mdvPlans);
		}
	}

	private Supplier<Map<String, BasePlanComparison>> getBenefitTypePlanComparison(Company company,
			OutputRequest prospectRequest, List<String> benefitTypeCodes, HttpServletRequest httpRequest) {
		return () -> {
			try {
				CompletableFuture<Map<String, XbssRealmPlyrPlan>> plyrPlanMapAsync = planComparisionAsyncService.realmPlanYearAsync(company.getRealmPlanYear().getId());
				CompletableFuture<Optional<List<EmployeePlansRes>>> prospectEmployeePlansAsync = planComparisionAsyncService.prospectEmployeePlansAsync(company.getCode());
				CompletableFuture<Map<String, List<BenefitPlanRate>>> trinetPlanRatesAsync = planComparisionAsyncService.getBenefitPlanRatesByAsync(company);
				Map<String, RateDetail> omsPlanRatesByPlan = new HashMap<>();
				// If the company is a TIB prospect and the benefit type includes medical, dental or vision, fetch TIB plan rates
				if(CompanyServiceHelper.isTibProspect(company) && OutputBenefitsTypeEnums.hasMDVBenefitType(benefitTypeCodes)) {
					// Fetch all distinct plans by strategy
					List<EePlanAssignment> eePlanAssignments = eePlanAssignmentService.getEmployeePlanAssigmentBy(List.of(Long.valueOf(prospectRequest.getTnStrategyId())));
					Map<String, Set<String>> planIdsByBenType = eePlanAssignments.stream()
							.collect(Collectors.groupingBy(
									eePlan -> String.valueOf(eePlan.getEePlanAssignmentPK().getBenefitType()),
									Collectors.mapping(
											eePlan -> String.valueOf(eePlan.getBenefitPlan()),
											Collectors.toSet()
									)
							));
					omsPlanRatesByPlan = getOMSPlanRatesByPlan(company, planIdsByBenType);
				}
				CompletableFuture.allOf(plyrPlanMapAsync, prospectEmployeePlansAsync, trinetPlanRatesAsync).join();
				Map<List<String>, String> carrierLogoIdMap = carrierLogoService.getCarrierLogoIdMap();

				if(plyrPlanMapAsync.isDone() && prospectEmployeePlansAsync.isDone() && trinetPlanRatesAsync.isDone()) {
					
					CurrentTrinetPlans plans = CurrentTrinetPlans.builder()
							.company(company)
							.requestBenefitTypes(benefitTypeCodes)
							.strategyId(prospectRequest.getTnStrategyId())
							.prospectEmployees(prospectEmployeePlansAsync.get())
							.plyrPlanMap(plyrPlanMapAsync.get())
							.trinetPlanRatesByCompany(trinetPlanRatesAsync.get())
							.tibPlanRates(omsPlanRatesByPlan)
							.planComparison(new HashMap<>())
							.httpRequest(httpRequest)
							.cmsLogoDetailMap(carrierLogoIdMap)
							.build();
					prepareCurrentPlanPopulator(builder()).populate(plans);
					prepareTrinetPlanPopulator(builder()).populate(plans);
					preparePlanComparison().populate(plans);
					return plans.getPlanComparison();
				}
			}catch (InterruptedException | ExecutionException ex) {
				log.error("Exception in populator buider...{} " , ex);
				Thread.currentThread().interrupt();
			}catch (Exception ex) {
				log.error("Exception in populator buider...{} " , ex);
				Thread.currentThread().interrupt();
			}
			return Collections.emptyMap();
		};
	}
	
	private PopulatorBuilder prepareCurrentPlanPopulator(PopulatorBuilder builder) {
		return builder.with(planPopulator().populateCurrentPlan())
				.with(planPopulator().populateCurrentPlanRate())
				.with(planPopulator().populateCurrentPlanHeadCount());
	}
	
	private PopulatorBuilder prepareTrinetPlanPopulator(PopulatorBuilder builder) {
		return builder.with(planPopulator().populateTrinetPlan())
				.with(planPopulator().populateTrinetPlanRate());
	}
	
	private PopulatorBuilder preparePlanComparison() {
		return builder().with(planBuilder().buildPlanAttributeLabels())
						.with(planBuilder().buildCurrentAndTrinetPlanAttributeValues())
						.with(planBuilder().buildCurrentPlanRate())
						.with(planBuilder().buildCurrentPlanHeadCount())
						.with(planBuilder().buildTrinetPlanRate())
						.with(planBuilder().buildTrinetPlanHeadCount())
						.with(planBuilder().buildPlanComparisonPagination());
	}
	
	private PlanPopulator planPopulator() {
		return planPopulator;
	}
	
	private PopulatorBuilder builder() {
		return PopulatorBuilder.create(PopulatorBuilderImpl::new);
	}
	
	private PlanCompareBuilder planBuilder() {
		return new PlanCompareBuilderImpl();
	}
	
	@Override
	@Async
	public CompletableFuture<Map<String, BasePlanComparison>> getAdditionalBenfitsCompareData(Company company, OutputRequest prospectRequest) {
		log.info("******* Generating Additional Benefits Plan Comparison Report Data *******");
		StopWatch taskWatch = new StopWatch("Additional Benefits Plan Comparison");
		taskWatch.start();
		Map<String, BasePlanComparison> planComparisonMap = new LinkedHashMap<>();
		if(!prospectRequest.getBenefitTypes().isEmpty() && (prospectRequest.getBenefitTypes().contains(OutputBenefitsTypeEnums.LIFE.getCode()) 
				|| prospectRequest.getBenefitTypes().contains(OutputBenefitsTypeEnums.DISABILITY.getCode()))) {
			
			List<PlanComparisonAdditonalBenefits> adCompareDetails = additionalBenefitPlanService
					.getAdditionalBenefitsCompareInformation(company, Long.valueOf(prospectRequest.getTnStrategyId()), prospectRequest.getTemplateNames());

			List<String> benefitTypeCodes = prospectRequest.getBenefitTypes().stream()
					.filter(key -> !key.equalsIgnoreCase(OutputBenefitsTypeEnums.MEDICAL.getCode()))
					.filter(key -> !key.equalsIgnoreCase(OutputBenefitsTypeEnums.DENTAL.getCode()))
					.filter(key -> !key.equalsIgnoreCase(OutputBenefitsTypeEnums.VISION.getCode()))
					.collect(Collectors.toList());
			
			for (var entry : adCompareDetails) {
				if(entry.getBenefitType().equalsIgnoreCase(OutputBenefitsTypeEnums.LIFE.toString()) && benefitTypeCodes.contains(OutputBenefitsTypeEnums.LIFE.getCode())) {
					planComparisonMap.put(BSSApplicationConstants.LIFE_CODE, entry);
				} else if(entry.getBenefitType().equalsIgnoreCase(OutputBenefitsTypeEnums.DISABILITY.toString()) && benefitTypeCodes.contains(OutputBenefitsTypeEnums.DISABILITY.getCode())) {
					planComparisonMap.put(BSSApplicationConstants.STD_CODE, entry);
				}
			}
		}
		taskWatch.stop();
		log.info(String.format("%s finished in :: %s", taskWatch.getId(), taskWatch.getTotalTimeMillis()));
		return CompletableFuture.completedFuture(planComparisonMap);
	}

	@Override
	public BasePlanComparison getPlanCompareAssignmentDetails(String prospectPlanId, String trinetPlanId,
			String benefitType, HttpServletRequest httpRequest, Company company) {

		boolean tibProspectForMDV = BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER.contains(benefitType) &&
				CompanyServiceHelper.isTibProspect(company);

		CurrentTrinetPlans currentTrinetPlans = CurrentTrinetPlans.builder().company(null)
				.requestBenefitTypes(List.of(benefitType)).planComparison(new HashMap<>()).httpRequest(httpRequest)
				.build();

		Set<String> planIds = tibProspectForMDV ? Set.of(prospectPlanId)
				: Stream.of(prospectPlanId, trinetPlanId).collect(Collectors.toSet());
		Set<String> validPlanIds = planIds.stream()
				.filter(planId -> planId != null && !planId.equals("0") && !planId.equalsIgnoreCase("null"))
				.collect(Collectors.toSet());
		List<BenefitPlanCompare> benefitPlanCompares = new ArrayList<>();
		try {
			// Do not call benefitsPlanViewService if there is no valid planId
			if (!validPlanIds.isEmpty()) {
				benefitPlanCompares
						.addAll(new ArrayList<>(planCompareService
								.getPlanAttributes(validPlanIds, company.getRealmPlanYear().getPlanYearEnd(),
										BSSApplicationConstants.databaseTemplatesMap.get(benefitType), httpRequest)
								.get()));
			}
			planIds.stream().filter(planId -> !validPlanIds.contains(planId))
					.forEach(planId -> benefitPlanCompares.add(BenefitPlanCompare.builder().planId(planId).build()));

		} catch (ExecutionException | InterruptedException ex) {
			log.error("Error while getting plan details form BenefitsPlanViewService ...");
			Thread.currentThread().interrupt();
			return null;
		}

		try {
			if (tibProspectForMDV) {
				// Do not call hrisPlanAttributeService if there is no valid planId
				if (trinetPlanId != null && !trinetPlanId.equals("0") && !trinetPlanId.equals("null")) {
					benefitPlanCompares.addAll(new ArrayList<>(hrisPlanAttributeService.getPlanAttributesByBenefitType(Set.of(trinetPlanId), PlanTypesEnum.getName(benefitType)).get()));
				} else {
					benefitPlanCompares.add(BenefitPlanCompare.builder().planId(trinetPlanId).build());
				}

			}
		} catch (ExecutionException | InterruptedException ex) {
			log.error("Error while getting plan details form HrisPlanAttributeService ...");
			Thread.currentThread().interrupt();
			return null;
		}

		// Assigning prospect plan from plan view service to setBenefitTypeCurrentPlansAttributes
		Map<String, List<BenefitPlanCompare>> benefitTypeCurrentPlansAttributes = new HashMap<>();
		Optional<BenefitPlanCompare> prospectBenefitPlanCompareOptional = benefitPlanCompares.stream()
				.filter(plan -> plan.getPlanId().equals(prospectPlanId)).findFirst();
		if (prospectBenefitPlanCompareOptional.isPresent()) {
			benefitTypeCurrentPlansAttributes.put(benefitType, List.of(prospectBenefitPlanCompareOptional.get()));
			currentTrinetPlans.setBenefitTypeCurrentPlansAttributes(benefitTypeCurrentPlansAttributes);
		}
		
		// Assigning trinet plan from plan view service to setBenefitTypeTrinetPlansAttributes
		Map<String, List<BenefitPlanCompare>> benefitTypeTrinetPlansAttributes = new HashMap<>();
		Optional<BenefitPlanCompare> trinetBenefitPlanCompareOptional = benefitPlanCompares.stream()
				.filter(plan -> plan.getPlanId().equals(trinetPlanId)).findFirst();
		if (trinetBenefitPlanCompareOptional.isPresent()) {
			benefitTypeTrinetPlansAttributes.put(benefitType, List.of(trinetBenefitPlanCompareOptional.get()));
			currentTrinetPlans.setBenefitTypeTrinetPlansAttributes(benefitTypeTrinetPlansAttributes);

		} 
		// creating CurrentTrinetPlansMapping of (BenefitType, (CurrentPlanId, List of trinetPlanIds))
		Map<String, Map<String, List<String>>> currentTrinetPlansMapping = Map.of(benefitType,
				Map.of(prospectPlanId, List.of(trinetPlanId)));
		currentTrinetPlans.setCurrentTrinetPlansMapping(currentTrinetPlansMapping);

		builder().with(planBuilder().buildPlanAttributeLabels())
				.with(planBuilder().buildCurrentAndTrinetPlanAttributeValues()).populate(currentTrinetPlans);

		return currentTrinetPlans.getPlanComparison().get(benefitType);

	}
}

