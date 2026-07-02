package com.trinet.ambis.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.plancompare.model.BenefitPlanDetailDto;
import com.trinet.ambis.persistence.plancompare.model.MappedPlanDetailDto;
import com.trinet.ambis.service.model.plancompare.BenefitPlan;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;

@Service
public interface PlanCompareService {

	Map<String, Map<String, Set<String>>> findCompanyLevelEnrolledPlans(Company company, List<Long> strategyIds,
			Map<String, BenefitPlan> currentRegionalBasePlanMappings,
			Map<String, BenefitPlan> futureRegionalBasePlanMappings);

	Workbook generateWorkbook(Company company, RealmPlanYear currentPlYr, RealmPlanYear futurePlYr,
			Map<String,Map<String, Set<String>>> plansToCompareByBeneiftType, Map<String, BenefitPlan> currentRegionalBasePlanMappings,
			Map<String, BenefitPlan> futureRegionalBasePlanMappings,
			HttpServletRequest httpRequest);
	
	/** Added for the plan compare changes **/

	/**
	 * Find Benefit plans for current year submitted strategy
	 * 
	 * @param code
	 * @param quarterName
	 * 
	 * @return List<BenefitPlanDetailDto>
	 */
	List<BenefitPlanDetailDto> findSubmittedStrategyPlansBy(String code);
	
	/**
	 * Find All benefit plans for current year submitted strategy
	 * 
	 * @param code
	 * @param quarterName
	 * 
	 * @return List<BenefitPlanDetailDto>
	 */
	List<BenefitPlanDetailDto> findAllFutureYearPlansBy(String code);
	
	/**
	 * 
	 * Find the mapped plans for given realmIds
	 * 
	 * @param code
	 * @return List<MappedPlanDetailDto>
	 */
	List<MappedPlanDetailDto> findMappingBenefitPlansBy(String code);
	
	CompletableFuture<List<BenefitPlanCompare>> getPlanAttributes(Set<String> planIds, Date effectiveDate, String template,
			HttpServletRequest httpRequest);

}