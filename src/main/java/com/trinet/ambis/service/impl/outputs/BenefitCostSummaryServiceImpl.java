package com.trinet.ambis.service.impl.outputs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StopWatch;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.enums.BenefitsCostEnum;
import com.trinet.ambis.enums.OutputBenefitsTypeEnums;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitCostStrategy;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitCostSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitTypeCost;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitTypeEmployeeCostSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitTypeTotal;
import com.trinet.ambis.rest.controllers.dto.outputs.EmployeeCostSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.EnrollmentByPlanType;
import com.trinet.ambis.rest.controllers.dto.prospect.BenefitsDetailsResponse;
import com.trinet.ambis.rest.controllers.dto.prospect.BenefitsDetailsResponse.BenefitType;
import com.trinet.ambis.service.outputs.BenefitCostSummaryService;
import com.trinet.ambis.service.prospect.ProspectPlanService;
import com.trinet.ambis.service.prospect.enums.BenefitTypeEnum;
import com.trinet.ambis.util.ProspectServiceRestClient;
import com.trinet.domain.common.ReturnResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * @author rterle
 *
 */

@Service
@Slf4j
public class BenefitCostSummaryServiceImpl implements BenefitCostSummaryService {

	@Autowired
	private StrategyDataDao strategyDataDao;

	@Autowired
	private ProspectPlanService prospectPlanService;

	@Autowired
	ProspectServiceRestClient prospectServiceRestClient;
	
	private static final BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12);

	@Override
	@Async
	public CompletableFuture<BenefitCostSummary> getBenefitCostSummaryData(
			BenefitTypeEmployeeCostSummary benefitTypeEmployeeCostSummary, String trinetStrategyId, String prospectId, List<String> requestedBenfitPlanList) {
		log.info("******* Generating Benefit Cost Summary Report Data *******");
		StopWatch taskWatch = new StopWatch(" Benefit Cost Summary Report Data *******");
		taskWatch.start();
		CompletableFuture<BenefitCostSummary> result = CompletableFuture.completedFuture(
				buildBenefitGroupCostSummaryData(benefitTypeEmployeeCostSummary, trinetStrategyId, prospectId,requestedBenfitPlanList));
		taskWatch.stop();
		log.info(String.format("****** %s finished in :: %s *******", taskWatch.getId(),
				taskWatch.getTotalTimeMillis()));
		return result;
	}

	private BenefitCostSummary getEmptyBenefitGroupCostSummary() {
		BenefitCostSummary emptyBenefitCostSummary = new BenefitCostSummary();
		List<BenefitTypeCost> currentBenCosts = new ArrayList<>();
		List<BenefitTypeCost> trinetBenCosts = new ArrayList<>();
		List<EnrollmentByPlanType> enrollmentByType = new ArrayList<>();
		emptyBenefitCostSummary.setEnrollmentByType(enrollmentByType);
		buildBenefitTypeCost(currentBenCosts, trinetBenCosts);

		BenefitCostStrategy currentStrategy = new BenefitCostStrategy();
		currentStrategy.setBenCosts(currentBenCosts);
		emptyBenefitCostSummary.setCurrentStrategy(currentStrategy);

		BenefitCostStrategy trinetStrategy = new BenefitCostStrategy();
		trinetStrategy.setBenCosts(trinetBenCosts);
		emptyBenefitCostSummary.setTrinetStrategy(trinetStrategy);
		return emptyBenefitCostSummary;
	}

	private void buildBenefitTypeCost(List<BenefitTypeCost> currentBenCosts, List<BenefitTypeCost> trinetBenCosts) {
		Stream.of(OutputBenefitsTypeEnums.values()).forEach(benTypeEnum -> {
			BenefitTypeCost currentBenTypeCost = new BenefitTypeCost();
			currentBenTypeCost.setBenType(benTypeEnum.getDisplayName());
			BenefitTypeTotal currentBenTypeTotal = new BenefitTypeTotal();
			currentBenTypeCost.setBenTypeTotal(currentBenTypeTotal);
			currentBenCosts.add(currentBenTypeCost);
			if (!BSSApplicationConstants.getCheckPlanTypes().contains(benTypeEnum.getCode())) {
				BenefitTypeCost trinetBenTypeCost = new BenefitTypeCost();
				trinetBenTypeCost.setBenType(benTypeEnum.getDisplayName());
				BenefitTypeTotal trinetBenTypeTotal = new BenefitTypeTotal();
				trinetBenTypeCost.setBenTypeTotal(trinetBenTypeTotal);
				trinetBenCosts.add(trinetBenTypeCost);
			}
		});
	}

	private BenefitCostSummary buildBenefitGroupCostSummaryData(BenefitTypeEmployeeCostSummary benefitTypeEmployeeCostSummary, String trinetStrategyId, String prospectId, List<String> requestedBenfitPlanList) {
		BenefitCostSummary benefitCostSummary = getEmptyBenefitGroupCostSummary();
		//Check if there are any current benefit plans at all
		boolean noCurrentData = prospectPlanService.getBenefitPlansBy(prospectId).isEmpty();
		benefitCostSummary.setNoCurrentStrategy(noCurrentData);

		if (benefitTypeEmployeeCostSummary != null
				&& benefitTypeEmployeeCostSummary.getEmplCostSummaryByBenGroup() != null
				&& !benefitTypeEmployeeCostSummary.getEmplCostSummaryByBenGroup().isEmpty()) {

			Map<String, Map<String, List<EmployeeCostSummary>>> employeeCostSummary = benefitTypeEmployeeCostSummary.getEmplCostSummaryByBenGroup();

			Map<String, List<EmployeeCostSummary>> costByBenType = employeeCostSummary.entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().values().stream()
							.flatMap(List::stream).collect(Collectors.toList())));

			//Get the employee count based on their tier including waived
			getEnrollmentCountByBenType(benefitCostSummary, costByBenType, requestedBenfitPlanList);

			//Remove list of waived employees that we kept to calculate Enrollment Count by type, before calculating the total benefits cost summary
			costByBenType.replaceAll((benType, employeeCostSummaries) -> employeeCostSummaries.stream()
					.filter(entry -> !entry.getEmployee().getCoverageCode().equalsIgnoreCase(ProspectConstants.WAVED_COVERAGE))
					.collect(Collectors.toList()));
			for (Map.Entry<String, List<EmployeeCostSummary>> employeeCostSummaryEntry : costByBenType.entrySet()) {
				String benType = OutputBenefitsTypeEnums.getDisplayNameByCode(employeeCostSummaryEntry.getKey());
				List<EmployeeCostSummary> costSummaryList = employeeCostSummaryEntry.getValue();
				if (!noCurrentData) {
					// Get Monthly ER Cost and Monthly EE Cost and Monthly Total Cost of Current
					// strategy for each plan type
					BigDecimal currentStrategyMonthlyERCost = costSummaryList.stream()
							.map(costSummary -> costSummary.getCurrentPlan().getErAmount())
							.reduce(BigDecimal.ZERO, BigDecimal::add);
					currentStrategyMonthlyERCost = currentStrategyMonthlyERCost.setScale(2);

					BigDecimal currentStrategyMonthlyEECost = costSummaryList.stream()
							.map(costSummary -> costSummary.getCurrentPlan().getEeAmount())
							.reduce(BigDecimal.ZERO, BigDecimal::add);
					currentStrategyMonthlyEECost = currentStrategyMonthlyEECost.setScale(2);

					BigDecimal currentStrategyMonthlyTotal = currentStrategyMonthlyERCost.add(currentStrategyMonthlyEECost).setScale(2);

					// Set respective data to currentStrategy of BenefitGroupCostSummary result for
					// each plan type
					Optional<BenefitTypeCost> currentBenefitTypeCostOptional = benefitCostSummary.getCurrentStrategy()
							.getBenCosts().stream().filter(entry -> entry.getBenType().equals(benType)).findFirst();
					if (currentBenefitTypeCostOptional.isPresent()) {
						BenefitTypeCost currentBenefitTypeCost = currentBenefitTypeCostOptional.get();
						BenefitTypeTotal benTypeTotal = BenefitTypeTotal.builder()
								.erAmount(currentStrategyMonthlyERCost)
								.eeAmount(currentStrategyMonthlyEECost)
								.total(currentStrategyMonthlyTotal)
								.build();
						currentBenefitTypeCost.setBenTypeTotal(benTypeTotal);
						currentBenefitTypeCost.setDisplayOnReport(isBenefitPlanRequested.test(transformToBSSBenefitTypeDisplayNames(requestedBenfitPlanList), benType));
					}
				}
				// Get Monthly ER Cost and Monthly EE Cost and Monthly Total Cost of trinet
				// strategy for each plan type
				BigDecimal triNetStrategyMonthlyERCost = costSummaryList.stream()
                        .filter(costSummary -> costSummary.getTriNetPlan() != null)
                        .map(costSummary -> costSummary.getTriNetPlan().getErAmount())
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				triNetStrategyMonthlyERCost = triNetStrategyMonthlyERCost.setScale(2);

				BigDecimal triNetStrategyMonthlyEECost = costSummaryList.stream()
                        .filter(costSummary -> costSummary.getTriNetPlan() != null)
						.map(costSummary -> costSummary.getTriNetPlan().getEeAmount())
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				triNetStrategyMonthlyEECost = triNetStrategyMonthlyEECost.setScale(2);

				BigDecimal triNetStrategyMonthlyTotal = triNetStrategyMonthlyERCost.add(triNetStrategyMonthlyEECost).setScale(2);

				// Set respective data to trinet strategy of BenefitGroupCostSummary result for
				// each plan type
				Optional<BenefitTypeCost> triNetBenefitTypeCostOptional = benefitCostSummary.getTrinetStrategy()
						.getBenCosts().stream().filter(entry -> entry.getBenType().equals(benType)).findFirst();
				if (triNetBenefitTypeCostOptional.isPresent()) {
					BenefitTypeCost triNetBenefitTypeCost = triNetBenefitTypeCostOptional.get();
					BenefitTypeTotal benefitTypeTotal = BenefitTypeTotal.builder()
							.erAmount(triNetStrategyMonthlyERCost)
							.eeAmount(triNetStrategyMonthlyEECost)
							.total(triNetStrategyMonthlyTotal)
							.build();
					triNetBenefitTypeCost.setBenTypeTotal(benefitTypeTotal);
					triNetBenefitTypeCost.setDisplayOnReport(isBenefitPlanRequested.test(transformToBSSBenefitTypeDisplayNames(requestedBenfitPlanList), benType));
				}
			}
		}
		// Prepare Life AD&D, Disability for current Strategy
		if (requestedBenfitPlanList.contains(BSSApplicationConstants.STD_CODE)
				|| requestedBenfitPlanList.contains(BSSApplicationConstants.LIFE_CODE)) {
			setAdditionalBenCostSummary(benefitCostSummary.getCurrentStrategy().getBenCosts(), prospectId);
		}
		//updating displayOnReport for Life AD&D, Disability for Current Strategy
		if (!requestedBenfitPlanList.contains(BSSApplicationConstants.LIFE_CODE)) {
			updateDisplayOnReportForCurrentStrategy(benefitCostSummary, BSSApplicationConstants.LIFE_CODE);
		}
		if (!requestedBenfitPlanList.contains(BSSApplicationConstants.STD_CODE)) {
			updateDisplayOnReportForCurrentStrategy(benefitCostSummary, BSSApplicationConstants.STD_CODE);
		}
		
		// Prepare Life AD&D, Disability, HSA and Waiver allowance for TriNet Strategy
		populateAddBenCostSummary(benefitCostSummary.getTrinetStrategy(), trinetStrategyId, requestedBenfitPlanList);
		orderBenefitTypes(benefitCostSummary.getTrinetStrategy().getBenCosts());
		buildBenefitGroupStrategyTotals(benefitCostSummary, noCurrentData);
		return benefitCostSummary;
	}

	private static void getEnrollmentCountByBenType(BenefitCostSummary benefitCostSummary, Map<String, List<EmployeeCostSummary>> costSummaryByBenType, List<String> benefitTypesList) {
		costSummaryByBenType.forEach((benType, costSummaryList) -> {
			long waivedCount = costSummaryList.stream().filter(employeeCostSummary -> employeeCostSummary.getEmployee().getCoverageCode().equals(ProspectConstants.WAVED_COVERAGE)).count();
			long employeeCvgCount = costSummaryList.stream().filter(employeeCostSummary -> employeeCostSummary.getEmployee().getCoverageCode().equals(ProspectConstants.EMPLOYEE)).count();
			long employeeSpouseCvgCount = costSummaryList.stream().filter(employeeCostSummary -> employeeCostSummary.getEmployee().getCoverageCode().equals(ProspectConstants.EMPLOYEE_PLUS_SPOUSE)).count();
			long employeeChildCvgCount = costSummaryList.stream().filter(employeeCostSummary -> employeeCostSummary.getEmployee().getCoverageCode().equals(ProspectConstants.EMPLOYEE_PLUS_CHILD)).count();
			long employeeFamilyCvgCount = costSummaryList.stream().filter(employeeCostSummary -> employeeCostSummary.getEmployee().getCoverageCode().equals(ProspectConstants.EMPLOYEE_PLUS_FAMILY)).count();
			long total = waivedCount + employeeCvgCount + employeeSpouseCvgCount + employeeChildCvgCount + employeeFamilyCvgCount;

			EnrollmentByPlanType enrollmentByPlanType = new EnrollmentByPlanType();
			enrollmentByPlanType.setBenType(BenefitTypeEnum.getBcrBenTypeDescFromBenTypeCode(benType));
			enrollmentByPlanType.setWaivedTier(String.valueOf(waivedCount));
			enrollmentByPlanType.setEeTier(String.valueOf(employeeCvgCount));
			enrollmentByPlanType.setEeSpouseTier(String.valueOf(employeeSpouseCvgCount));
			enrollmentByPlanType.setEeChildTier(String.valueOf(employeeChildCvgCount));
			enrollmentByPlanType.setEeFamilyTier(String.valueOf(employeeFamilyCvgCount));
			enrollmentByPlanType.setTotal(String.valueOf(total));
			enrollmentByPlanType.setDisplayOnReport(benefitTypesList.contains(benType));
			benefitCostSummary.getEnrollmentByType().add(enrollmentByPlanType);
		});
		//Sorting them in the order of Medical, Dental, Vision
		benefitCostSummary.getEnrollmentByType().sort(Comparator.comparing(type -> ProspectConstants.orderedBenTypes.get(type.getBenType())));
	}

	private static void buildBenefitGroupStrategyTotals(BenefitCostSummary benefitCostSummary, boolean noCurrentData) {

		if(!noCurrentData){
			// Get Monthly Totals of Current Strategy
			BigDecimal currentStrategyERCostMonthlyTotal = benefitCostSummary.getCurrentStrategy().getBenCosts()
					.stream().filter(benCost->benCost.isDisplayOnReport()).filter(benCost -> benCost.getBenTypeTotal().getErAmount() != null)
					.map(benCost -> benCost.getBenTypeTotal().getErAmount()).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
			BigDecimal currentStrategyEECostMonthlyTotal = benefitCostSummary.getCurrentStrategy().getBenCosts()
					.stream().filter(benCost->benCost.isDisplayOnReport()).filter(benCost -> benCost.getBenTypeTotal().getEeAmount() != null)
					.map(benCost -> benCost.getBenTypeTotal().getEeAmount()).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
			BigDecimal currentStrategyTotalMonthlyTotal = benefitCostSummary.getCurrentStrategy().getBenCosts()
					.stream().filter(benCost->benCost.isDisplayOnReport()).filter(benCost -> benCost.getBenTypeTotal().getTotal() != null)
					.map(benCost -> benCost.getBenTypeTotal().getTotal()).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);

			// Set Monthly Totals of Current Strategy
			BenefitTypeTotal currentStrategyMonthlyTotal = BenefitTypeTotal.builder()
					.erAmount(currentStrategyERCostMonthlyTotal)
					.eeAmount(currentStrategyEECostMonthlyTotal)
					.total(currentStrategyTotalMonthlyTotal)
					.build();
			benefitCostSummary.getCurrentStrategy().setMonthlyTotal(currentStrategyMonthlyTotal);

			// Set Annual Totals of Current Strategy
			BenefitTypeTotal currentStrategyAnnualTotal = BenefitTypeTotal.builder()
					.erAmount(currentStrategyERCostMonthlyTotal.multiply(MONTHS_IN_YEAR))
					.eeAmount(currentStrategyEECostMonthlyTotal.multiply(MONTHS_IN_YEAR))
					.total(currentStrategyTotalMonthlyTotal.multiply(MONTHS_IN_YEAR))
					.build();
			benefitCostSummary.getCurrentStrategy().setAnnualTotal(currentStrategyAnnualTotal);
		}

		// Get Monthly Totals of TriNet Strategy
		BigDecimal triNetStrategyERCostMonthlyTotal = benefitCostSummary.getTrinetStrategy().getBenCosts().stream()
				.filter(benCost -> (benCost.isDisplayOnReport()) && benCost.getBenTypeTotal().getErAmount() != null)
				.map(benCost -> benCost.getBenTypeTotal().getErAmount()).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
		BigDecimal triNetStrategyEECostMonthlyTotal = benefitCostSummary.getTrinetStrategy().getBenCosts().stream()
				.filter(benCost -> (benCost.isDisplayOnReport()) && benCost.getBenTypeTotal().getEeAmount() != null)
				.map(benCost -> benCost.getBenTypeTotal().getEeAmount()).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
		BigDecimal triNetStrategyTotalMonthlyTotal = benefitCostSummary.getTrinetStrategy().getBenCosts().stream()
				.filter(benCost -> (benCost.isDisplayOnReport()) && benCost.getBenTypeTotal().getTotal() != null)
				.map(benCost -> benCost.getBenTypeTotal().getTotal()).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);

		// Set Monthly Totals of TriNet Strategy
		BenefitTypeTotal triNetStrategyMonthlyTotal = BenefitTypeTotal.builder()
				.erAmount(triNetStrategyERCostMonthlyTotal)
				.eeAmount(triNetStrategyEECostMonthlyTotal)
				.total(triNetStrategyTotalMonthlyTotal)
				.build();
		benefitCostSummary.getTrinetStrategy().setMonthlyTotal(triNetStrategyMonthlyTotal);

		// Set Annual Totals of TriNet Strategy
		BenefitTypeTotal triNetStrategyAnnualTotal = BenefitTypeTotal.builder()
				.erAmount(triNetStrategyERCostMonthlyTotal.multiply(MONTHS_IN_YEAR))
				.eeAmount(triNetStrategyEECostMonthlyTotal.multiply(MONTHS_IN_YEAR))
				.total(triNetStrategyTotalMonthlyTotal.multiply(MONTHS_IN_YEAR))
				.build();
		benefitCostSummary.getTrinetStrategy().setAnnualTotal(triNetStrategyAnnualTotal);
	}

	/**
	 * This method is to get the Life,Disability, HSA and Waver allowance of TriNet
	 * Strategy
	 * 
	 * @param trinetStrategy
	 * @param trinetStrategyId
	 * @return
	 */
	private void populateAddBenCostSummary(BenefitCostStrategy trinetStrategy, String trinetStrategyId, List<String> requestedBenefitPlans) {
		List<Object[]> results = strategyDataDao.getBenefitCostSummary(trinetStrategyId);
		boolean hasHsaFunding = true;
		boolean hasWaiverStrategyFunding = true;
		List<Long> strategyIds = List.of(Long.valueOf(trinetStrategyId));
		List<Object[]> hsaFundingResults = strategyDataDao.getStrategyHsaFunding(strategyIds);
		List<Object[]> strategyWaiverFundingResults = strategyDataDao.getStrategyFundingByStrategy(strategyIds);

		List<Object[]> hsaFundingRes = hsaFundingResults.stream().filter(
				hsaFunding -> null != hsaFunding[1] && BigDecimal.ZERO.compareTo((BigDecimal) hsaFunding[1]) != 0)
				.collect(Collectors.toList());
		if (hsaFundingRes.isEmpty())
			hasHsaFunding = false;
		List<Object[]> waiverResults = strategyWaiverFundingResults.stream()
				.filter(waiverFunding -> null != waiverFunding[8]).collect(Collectors.toList());
		if (waiverResults.isEmpty())
			hasWaiverStrategyFunding = false;
		trinetStrategy.setStrategyName(BSSApplicationConstants.TRINET_STRATEGY);

		List<BenefitTypeCost> benCosts = trinetStrategy.getBenCosts();
		String disabilityPlanTypeName = null;
		BigDecimal disabilityCost = BigDecimal.valueOf(0.00);

		for (Object[] r : results) {

			String planType = (String) r[0];
			String planSubType = (String) r[1];
			BigDecimal cost = (BigDecimal) r[2];

			String planTypeName = null;
			if (planSubType == null && BSSApplicationConstants.getCheckPlanTypes().contains(planType)) {
				planTypeName = BenefitsCostEnum.getName(planType);

			} else if (null != planSubType) {
				planTypeName = BenefitsCostEnum.getName(planSubType);
			}

			if (null != planTypeName && BSSApplicationConstants.getDisabilityPlanTypes().contains(planType)) {

				disabilityCost = disabilityCost.add(cost.setScale(2));
				disabilityPlanTypeName = planTypeName;
			}

			if (null != planTypeName && (BSSApplicationConstants.getPlanTypeNames().contains(planTypeName))) {

				if(BenefitsCostEnum.HSA.getName().equalsIgnoreCase(planTypeName))
				     benCosts.add(prepareBenefitTypeCost(planTypeName, cost.setScale(2), hasHsaFunding ));
				if(BenefitsCostEnum.WA.getName().equalsIgnoreCase(planTypeName))
					 benCosts.add(prepareBenefitTypeCost(planTypeName, cost.setScale(2), hasWaiverStrategyFunding ));
				if(!BenefitsCostEnum.HSA.getName().equalsIgnoreCase(planTypeName) && !BenefitsCostEnum.WA.getName().equalsIgnoreCase(planTypeName))
				     benCosts.add(prepareBenefitTypeCost(planTypeName, cost.setScale(2), isBenefitPlanRequested.test(transformToBSSBenefitTypeDisplayNames(requestedBenefitPlans), planTypeName)));

			}
		}

		if (null != disabilityPlanTypeName) {
			benCosts.add(prepareBenefitTypeCost(disabilityPlanTypeName, disabilityCost, isBenefitPlanRequested.test(transformToBSSBenefitTypeDisplayNames(requestedBenefitPlans), disabilityPlanTypeName)));
		}
	}


	/**
	 * This method orders the benefit type costs as required
	 *
	 * @param benCosts
	 * @return
	 */
	private static void orderBenefitTypes(List<BenefitTypeCost> benCosts) {

		Map<String, BenefitTypeCost> costByBenType = benCosts.stream().collect(Collectors.toMap(
				BenefitTypeCost::getBenType,
				benefitTypeCost -> benefitTypeCost
		));

		List<String> orderedPlanTypes = List.of(OutputBenefitsTypeEnums.MEDICAL.getDisplayName(),
				OutputBenefitsTypeEnums.DENTAL.getDisplayName(),
				OutputBenefitsTypeEnums.VISION.getDisplayName(),
				OutputBenefitsTypeEnums.LIFE.getDisplayName(),
				OutputBenefitsTypeEnums.DISABILITY.getDisplayName(),
				BenefitsCostEnum.HSA.getName(),
				BenefitsCostEnum.WA.getName());

		List<BenefitTypeCost> orderedBenCosts = new ArrayList<>();
		orderedPlanTypes.forEach(planType -> {
			BenefitTypeCost benefitTypeCost = costByBenType.get(planType);
			if (benefitTypeCost != null) {
				orderedBenCosts.add(benefitTypeCost);
			}
		});
		benCosts.clear();
		benCosts.addAll(orderedBenCosts);
	}

	/**
	 * This method is to prepare the BenefitTypeCost object
	 * 
	 * @param planTypeName
	 * @param cost
	 * @return
	 */
	private BenefitTypeCost prepareBenefitTypeCost(String planTypeName, BigDecimal cost, boolean displayOnReport) {
		BenefitTypeCost benefitTypeCost = new BenefitTypeCost();
		benefitTypeCost.setBenType(planTypeName);
		BenefitTypeTotal benefitTypeTotal = BenefitTypeTotal.builder().erAmount(cost).eeAmount(BigDecimal.valueOf(0.00).setScale(2)).total(cost).build();
		benefitTypeCost.setBenTypeTotal(benefitTypeTotal);
		benefitTypeCost.setDisplayOnReport(displayOnReport);
		return benefitTypeCost;
	}
	
	private BiPredicate<List<String>, String> isBenefitPlanRequested = (requestedBenfitPlanList, benfitPlan) -> requestedBenfitPlanList.contains(benfitPlan);
	 
	/**
	 * This method is to Transform to BSS BenefitType codes to Benefit Display Names
	 *  */
	private List<String> transformToBSSBenefitTypeDisplayNames(List<String> benTypeCodes) {
		if (benTypeCodes.contains(BSSApplicationConstants.DENTAL_PLAN_TYPE)) {
			benTypeCodes.remove(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);
		}
		if (benTypeCodes.contains(BSSApplicationConstants.VISION_PLAN_TYPE)) {
			benTypeCodes.remove(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE);
		}
		return benTypeCodes.stream().map(OutputBenefitsTypeEnums::getDisplayNameByCode).collect(Collectors.toList());
	}
	
	private void updateDisplayOnReportForCurrentStrategy(BenefitCostSummary benefitCostSummary, String benType) {
		Optional<BenefitTypeCost> currentBenefitTypeCostOptional = benefitCostSummary.getCurrentStrategy()
				.getBenCosts().stream().filter(entry -> entry.getBenType().equals(OutputBenefitsTypeEnums.getDisplayNameByCode(benType))).findFirst();
		if (currentBenefitTypeCostOptional.isPresent()) {
			BenefitTypeCost currentBenefitTypeCost = currentBenefitTypeCostOptional.get();
			currentBenefitTypeCost.setDisplayOnReport(false);
		}
	}

	private void setAdditionalBenCostSummary(List<BenefitTypeCost> benefitTypeCostsList, String prospectId) {
		List<BenefitsDetailsResponse> benefitsDetailsResponse = getProspectBenefitDetails(prospectId);

		Map<String, String> benefitTypeCodeMap = Map.of(
				BSSApplicationConstants.STD_CODE, OutputBenefitsTypeEnums.DISABILITY.getDisplayName(), 
				BSSApplicationConstants.LTD_CODE, OutputBenefitsTypeEnums.DISABILITY.getDisplayName(), 
				BSSApplicationConstants.LIFE_CODE, OutputBenefitsTypeEnums.LIFE.getDisplayName());

		Map<String, BigDecimal> benefitsDetails = benefitsDetailsResponse.stream()
				.flatMap(ben -> ben.getBenefitTypes().stream())
				.filter(Objects::nonNull)
				.collect(Collectors.groupingBy(ben -> benefitTypeCodeMap.get(ben.getBenefitTypeCode()),
						Collectors.reducing(BigDecimal.ZERO, BenefitType::getMonthlyTotal, BigDecimal::add)));

		benefitTypeCostsList.stream()
				.filter(ben -> ben.getBenType().equalsIgnoreCase(OutputBenefitsTypeEnums.LIFE.getDisplayName())
						|| ben.getBenType().equalsIgnoreCase(OutputBenefitsTypeEnums.DISABILITY.getDisplayName()))
				.forEach(ben -> Optional.ofNullable(benefitsDetails.get(ben.getBenType())).ifPresent(value -> {
					ben.getBenTypeTotal().setErAmount(value);
					ben.getBenTypeTotal().setTotal(value);
				}));
	}
	
	@SuppressWarnings("unchecked")
	public List<BenefitsDetailsResponse> getProspectBenefitDetails(String companyCode) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.put(ProspectConstants.PROSPECT_ID_REQ_PARAM, List.of(companyCode));
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(ProspectConstants.BENEFITS_TYPES_PATH_PARAM, String.join(",", BSSApplicationConstants.ADDITIONAL_PLAN_TYPES));

		ParameterizedTypeReference<ReturnResponse<List<BenefitsDetailsResponse>>> prospectDetailsBean = new ParameterizedTypeReference<>() {
		};
		ProspectApiRequest<List<BenefitsDetailsResponse>> prospectApiGetRequest = ProspectApiRequest.<List<BenefitsDetailsResponse>>builder()
				.method(HttpMethod.GET)
				.uri(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_STRATEGY_URI)).queryParams(queryParams)
				.pathParams(pathParams).parameterizedTypeReference(prospectDetailsBean).build();

		return prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiGetRequest);
	}

}
