package com.trinet.ambis.service.impl;

import static com.trinet.ambis.common.BSSApplicationConstants.BSS_EXPORT_TEMPLATE;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.PlanCompareConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.helper.PlanCompareExportHelper;
import com.trinet.ambis.helper.PlanCompareHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.plancompare.dao.hrp.PlanCompareDao;
import com.trinet.ambis.persistence.plancompare.model.BenefitPlanDetailDto;
import com.trinet.ambis.persistence.plancompare.model.MappedPlanDetailDto;
import com.trinet.ambis.persistence.plancompare.model.PlanYearDetailDto;
import com.trinet.ambis.service.BenefitsPlanViewService;
import com.trinet.ambis.service.BplService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.PlanCompareService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.model.EmpBenPlanMapping;
import com.trinet.ambis.service.model.plancompare.BenefitPlan;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.util.BSSSecurityUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author schaudhari
 *
 */
@Service
@Slf4j
public class PlanCompareServiceImpl implements PlanCompareService {
	
	private static final Logger logger = LoggerFactory.getLogger(PlanCompareServiceImpl.class);

	@Autowired
	private EmployeeDataDao employeeDataDao;

	@Autowired
	private StrategyDataDao strategyDataDao;

	@Autowired
	private PlanCompareExportHelper planCompareExportHelper;

	@Autowired
	private PlanCompareDao planCompareDao;
	
	@Autowired
	protected RealmPlanYearService realmPlanYearService;
	
	@Autowired
	private CompanyService companyService;
	
	@Autowired
	private BplService bplService;
	
	@Autowired
	private  BenefitsPlanViewService benefitsPlanViewService;

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Map<String, Set<String>>> findCompanyLevelEnrolledPlans(Company company, List<Long> strategyIds,
			Map<String, BenefitPlan> currentRegionalBasePlanMappings,
			Map<String, BenefitPlan> futureRegionalBasePlanMappings) {
		MultiKeyMap empBenPlanMappings = employeeDataDao.getEmpPlanMapping(company.getCode(),
				company.getRealmPlanYear().getId());
		MultiKeyMap emplStrategyBenGroup = strategyDataDao.getEmplStrategyBenGroup(company.getId());
		Map<Long, Map<String, List<String>>> strategyBenGroupPlans = strategyDataDao.getStrategyBenPlans(strategyIds);

		Map<String, Map<Long, Set<String>>> currentPlanMappedToStrategyFuturePlan = new LinkedHashMap<>();
		Map<String, Map<String, Set<String>>> currentFuturePlansByBenefitType = new LinkedHashMap<>();
		empBenPlanMappings.forEach((key, value) -> {
			EmpBenPlanMapping empBenPlanMapping = (EmpBenPlanMapping) value;
			String emplId = ((MultiKey) key).getKeys()[0].toString();
			String benefitType = ((MultiKey) key).getKeys()[1].toString();
			String curBenPlan = empBenPlanMapping.getCurBenPlan();
			String futBenProg = empBenPlanMapping.getCurBenProgram();
			String futBenPlan = empBenPlanMapping.getNextBenPlan();
			List<String> futAltBenPlans = empBenPlanMapping.getAltBenPlans();
			String tmpCurBenPlan = currentRegionalBasePlanMappings.containsKey(curBenPlan)
					? currentRegionalBasePlanMappings.get(curBenPlan).getPlanId()
					: curBenPlan;
			for (long strategyId : strategyIds) {
				String tmpFutBenProg = getFutureBenProgIfChanged(emplStrategyBenGroup, emplId, futBenProg, strategyId);
				String tmpFutBenPlan = null;
				if (strategyBenGroupPlans.get(strategyId).get(tmpFutBenProg).contains(futBenPlan)) {
					tmpFutBenPlan = futBenPlan;
				} else {
                    if (futAltBenPlans != null) {
                        for (String altPlan : futAltBenPlans) {
                            if (strategyBenGroupPlans.get(strategyId).get(tmpFutBenProg).contains(altPlan)) {
                                tmpFutBenPlan = altPlan;
                                break;
                            }
                        }
                    }
				}
				if (!isPlanAvailable(tmpCurBenPlan)) {
					continue;
				}
				tmpFutBenPlan = futureRegionalBasePlanMappings.containsKey(tmpFutBenPlan)
						? futureRegionalBasePlanMappings.get(tmpFutBenPlan).getPlanId()
						: tmpFutBenPlan;
				

				mapCurrentPlansToFuturePlansByStrategyId(currentPlanMappedToStrategyFuturePlan, strategyId,
						tmpCurBenPlan, tmpFutBenPlan);

				if (BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE.equals(benefitType)) {
					benefitType = BSSApplicationConstants.DENTAL_PLAN_TYPE;
				} else if (BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE.equals(benefitType)) {
					benefitType = BSSApplicationConstants.VISION_PLAN_TYPE;
				}
				
				currentFuturePlansByBenefitType.computeIfAbsent(benefitType, k -> new LinkedHashMap<>()).computeIfAbsent(tmpCurBenPlan,
						k -> new LinkedHashSet<>());
			}
		});
		
		return createCurrentAndFuturePlanMapping(currentPlanMappedToStrategyFuturePlan,currentFuturePlansByBenefitType);
		
	}

	@Override
	public Workbook generateWorkbook(Company company, RealmPlanYear currentPlYr, RealmPlanYear futurePlYr,
			Map<String,Map<String, Set<String>>> plansIdsToCompareByBenefitType, Map<String, BenefitPlan> currentRegionalBasePlanMappings,
			Map<String, BenefitPlan> futureRegionalBasePlanMappings,
			HttpServletRequest httpRequest) {
		Map<String, Set<String>> plansToCompare = new LinkedHashMap<>();
		plansIdsToCompareByBenefitType.values().forEach(innerMap -> innerMap.forEach(plansToCompare::put));
 
		StopWatch stopWatch = new StopWatch("BenefitPlanViewApiCall");
		stopWatch.start();

		CompletableFuture<List<BenefitPlanCompare>> currentPlansResponse = PlanCompareHelper.getCurrentPlansAttributes(
				plansIdsToCompareByBenefitType, currentPlYr.getPlanYearEnd(), BSS_EXPORT_TEMPLATE, httpRequest);

		CompletableFuture<List<BenefitPlanCompare>> futurePlansResponse = PlanCompareHelper.getFuturePlansAttributes(
				plansIdsToCompareByBenefitType, futurePlYr.getPlanYearEnd(), BSS_EXPORT_TEMPLATE, httpRequest);

		CompletableFuture.allOf(currentPlansResponse, futurePlansResponse).join();

		stopWatch.stop();
		log.info("%s finished in %s:", stopWatch.getId(), stopWatch.getTotalTimeMillis());

		Map<String, BenefitPlanCompare> currentYrPlanAttributes;
		Map<String, BenefitPlanCompare> futureYrPlanAttributes;
		try {
			currentYrPlanAttributes = PlanCompareHelper.mapPlanIdToObject(currentPlansResponse.get());
			futureYrPlanAttributes = PlanCompareHelper.mapPlanIdToObject(futurePlansResponse.get());
		} catch (InterruptedException | ExecutionException e) {
			Thread.currentThread().interrupt();
			throw new BSSApplicationException(e,
					new BSSApplicationError("Exception occured while calling plan compare service."));
		}

		Map<String, BenefitPlan> currentYrBasePlans = currentRegionalBasePlanMappings.keySet().stream()
				.map(currentRegionalBasePlanMappings::get).distinct()
				.collect(Collectors.toMap(BenefitPlan::getPlanId, Function.identity()));
		Map<String, BenefitPlan> futureYrBasePlans = futureRegionalBasePlanMappings.keySet().stream()
				.map(futureRegionalBasePlanMappings::get).distinct()
				.collect(Collectors.toMap(BenefitPlan::getPlanId, Function.identity()));
		return planCompareExportHelper.constructWorkbook(plansToCompare, currentYrPlanAttributes,
				futureYrPlanAttributes, currentYrBasePlans, futureYrBasePlans);
	}
	
	@Override
	public CompletableFuture<List<BenefitPlanCompare>> getPlanAttributes(Set<String> planIds, Date effectiveDate,
			String template, HttpServletRequest httpRequest) {
		CompletableFuture<List<BenefitPlanCompare>> benefitPlanCompares;
		benefitPlanCompares = bplService.getBPLAttributes(planIds, effectiveDate, template, httpRequest);

		return benefitPlanCompares;
	}

	private String getFutureBenProgIfChanged(MultiKeyMap emplStrategyBenGroup, String emplId, String futBenProg,
			long strategyId) {
		if (emplStrategyBenGroup.containsKey(emplId, strategyId)) {
			futBenProg = ((BenefitGroup) emplStrategyBenGroup.get(emplId, strategyId)).getBenefitProgram();
		}
		return futBenProg;
	}

	/**
	 * This method creates map of current plan mapped to map of strategy id and
	 * future plans. This data-structures helps to club the future plans together by
	 * strategy when multiple employees has same current plans.
	 * 
	 */
	// @formatter:off
	/**
	 * Example: 
	 * Employee 1 = CurrentStrategy=Aetna PPO 1000, FutureStrategy1=Aetna PPO 1000 , FutureStrategy2=Empire PPO 1000
	 * Employee 2 = CurrentStrategy=Aetna PPO 1000, FutureStrategy1=Aetna PPO 1500, FutureStrategy2=Empire PPO 1500
	 * 
	 * Comparison result on UI:
	 * 
	 * Aetna PPO 1000 | Aetna PPO 1000 | Aetna PPO 1500 | Empire PPO 1000 | Empire PPO 1500
	 * 
	 */		
	// @formatter:on
	private void mapCurrentPlansToFuturePlansByStrategyId(
			Map<String, Map<Long, Set<String>>> currentPlanMappedToStrategyFuturePlan, long strategyId,
			String tmpCurBenPlan, String tmpFutBenPlan) {
		if (currentPlanMappedToStrategyFuturePlan.get(tmpCurBenPlan) == null) {
			Map<Long, Set<String>> strategyFuturePlans = new LinkedHashMap<>();
			Set<String> tmpFutBenPlans = new LinkedHashSet<>();
			tmpFutBenPlans.add(tmpFutBenPlan);
			strategyFuturePlans.put(strategyId, tmpFutBenPlans);
			currentPlanMappedToStrategyFuturePlan.put(tmpCurBenPlan, strategyFuturePlans);
		} else {
			if (currentPlanMappedToStrategyFuturePlan.get(tmpCurBenPlan).get(strategyId) == null) {
				Set<String> tmpFutBenPlans = new LinkedHashSet<>();
				tmpFutBenPlans.add(tmpFutBenPlan);
				currentPlanMappedToStrategyFuturePlan.get(tmpCurBenPlan).put(strategyId, tmpFutBenPlans);
			} else {
				currentPlanMappedToStrategyFuturePlan.get(tmpCurBenPlan).get(strategyId).add(tmpFutBenPlan);
			}
		}
	}

	private Map<String, Map<String, Set<String>>> createCurrentAndFuturePlanMapping(
			Map<String, Map<Long, Set<String>>> curPlanMappedToStrategyFutPlans,
			Map<String, Map<String, Set<String>>> currentFuturePlansByBenefitType) {
		for (Map.Entry<String, Map<Long, Set<String>>> curPlanMappedToStrategyFutPlan : curPlanMappedToStrategyFutPlans
				.entrySet()) {
			String currentPlan = curPlanMappedToStrategyFutPlan.getKey();
			Map<Long, Set<String>> strategyFuturePlans = curPlanMappedToStrategyFutPlan.getValue();
			
			for (Set<String> futurePlans : strategyFuturePlans.values()) {
				for (Map.Entry<String, Map<String, Set<String>>> benefitTypeEntry : currentFuturePlansByBenefitType.entrySet()) {
					Map<String, Set<String>> currentPlans = benefitTypeEntry.getValue();
					if (currentPlans.containsKey(currentPlan)) {
						if (currentPlans.get(currentPlan).isEmpty()) {
							currentPlans.put(currentPlan, new LinkedHashSet<>(futurePlans));
						} else {
							currentPlans.get(currentPlan).addAll(futurePlans);
						}
					}
				}
			}
		}
		return currentFuturePlansByBenefitType;
	}
	
	

	private boolean isPlanAvailable(String benPlan) {
		return StringUtils.isNotEmpty(benPlan) && !"Waive".equalsIgnoreCase(benPlan);
	}

	public Map<String,String> findFundPeriodOrTransitionPeriodOpen(String code) {
		Company company = companyService.getCompanyDetails(code, false, BSSSecurityUtils.getAuthenticatedPersonId(), null);
		List<PlanYearDetailDto> planYearDetails = realmPlanYearService.findCurrentAndFuturePlanYearsBy(company.getCode(), company.getQuater());
		Map<String,String> realmYearIds = new HashMap<>();
		String currentYear = PlanCompareConstants.CURRENT.getAction();
		String futureYear = PlanCompareConstants.FUTURE.getAction();
		if((company.isRenewalOpen() || company.isTransitionPeriod()) &&  hasFuturePlanExist().test(planYearDetails)) {
			realmYearIds.put(currentYear, getRealmId(currentYear).apply(planYearDetails));
			realmYearIds.put(futureYear, getRealmId(futureYear).apply(planYearDetails));
		}else {
			realmYearIds.put(currentYear, getRealmId(currentYear).apply(planYearDetails));
			realmYearIds.put(futureYear, getRealmId(currentYear).apply(planYearDetails));
		}
		return realmYearIds;
	}
	
	@Override
	public List<BenefitPlanDetailDto> findSubmittedStrategyPlansBy(String code) {
		Map<String, String> planYearDetailMap = findFundPeriodOrTransitionPeriodOpen(code);
		String currentRealmYearId = planYearDetailMap.get(PlanCompareConstants.CURRENT.getAction());
		logger.info("Current year plans for company code: {} and realmPlYrId {} ", code, currentRealmYearId);
		return planCompareDao.findSubmittedStrategyPlansBy(code, currentRealmYearId);
	}
	
	@Override
	public List<BenefitPlanDetailDto> findAllFutureYearPlansBy(String code) {
		Map<String, String> planYearDetailMap = findFundPeriodOrTransitionPeriodOpen(code);
		String futureRealmYearId = planYearDetailMap.get(PlanCompareConstants.FUTURE.getAction());
		logger.info("Current year plans for company code: {} and realmPlYrId {} ", code, futureRealmYearId);
		return planCompareDao.findAllFutureYearPlansBy(futureRealmYearId);
	}
	
	@Override
	public  List<MappedPlanDetailDto> findMappingBenefitPlansBy(String code) {
		Map<String, String> planYearDetailMap = findFundPeriodOrTransitionPeriodOpen(code);
		String futureRealmYearId = planYearDetailMap.get(PlanCompareConstants.FUTURE.getAction());
		String currentRealmYearId = planYearDetailMap.get(PlanCompareConstants.CURRENT.getAction());
		logger.info("Mapping plans: futureRealmYearId {} currentRealmYearId {} ", futureRealmYearId,
				currentRealmYearId);
		return planCompareDao.findMappingBenefitPlansBy(futureRealmYearId, currentRealmYearId);
	}

    private Predicate<List<PlanYearDetailDto>> hasFuturePlanExist(){
		return planYearDetails -> planYearDetails.stream().anyMatch(hasCurrentOrFuturYear(PlanCompareConstants.FUTURE.getAction()));
	}
	
	/**
	 * 
	 * @param yearType
	 * @return realmId
	 * 
	 */
	private Function<List<PlanYearDetailDto>,String> getRealmId(String yearType){
		return planYears -> {
			Optional<PlanYearDetailDto> futurePlanYear = planYears.stream().filter(hasCurrentOrFuturYear(yearType)).findFirst();
			return futurePlanYear.isPresent() ? futurePlanYear.get().getRealmYearId() : null;
		};
	}
	
	/**
	 * 
	 * @param yearType
	 * @return Predicate<PlanYearDetailDto>
	 */
	private Predicate<PlanYearDetailDto> hasCurrentOrFuturYear(String yearType){
		return plan -> plan.getPlanYear().equalsIgnoreCase(yearType);
	}

}