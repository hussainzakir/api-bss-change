package com.trinet.ambis.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.PlanCompareConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.plancompare.model.PlanYearDetailDto;
import com.trinet.ambis.rest.controllers.StrategyController;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto;
import com.trinet.ambis.service.model.plancompare.BenefitPlan;
import com.trinet.ambis.service.prospect.ProspectPlanCompareService;
import com.trinet.ambis.util.BSSSecurityUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PlanCompareFacade {
	
	@Autowired
	private CompanyService companyService;

	@Autowired
	private PlanCompareService planCompareService;

	@Autowired
	private BenefitPlanService benefitPlanService;

	@Autowired
	private RealmPlanYearService realmPlanYearService;
	
	@Autowired
	private ProspectPlanCompareService prospectPlanCompareService;

	@Autowired
	private StrategyDao strategyDao;

	public Workbook generateEnrolledPlanCompareReport(Company company, List<Long> strategyIds,
			HttpServletRequest httpRequest) {
		List<Strategy> strategies = strategyDao.findAllById(strategyIds);
		RealmPlanYear currentPlYr = realmPlanYearService.getRealmForCompanyId(strategies.get(0).getCompanyId());
		RealmPlanYear futurePlYr = realmPlanYearService.getRealmForCompanyId(strategies.get(1).getCompanyId());

		Map<String, BenefitPlan> currentRegionalBasePlanMappings = benefitPlanService
				.getRegionalBasePlanMapping(currentPlYr);
		Map<String, BenefitPlan> futureRegionalBasePlanMappings = benefitPlanService
				.getRegionalBasePlanMapping(futurePlYr);

		StopWatch taskWatch = new StopWatch("findCompanyLevelEnrolledPlans");
		taskWatch.start();
		strategyIds.remove(0);
		Map<String,Map<String, Set<String>>> plansToCompareByBenefitType = planCompareService.findCompanyLevelEnrolledPlans(company, strategyIds,
				currentRegionalBasePlanMappings, futureRegionalBasePlanMappings);
		taskWatch.stop();
		log.info(String.format("%s finished in :: %s", taskWatch.getId(), taskWatch.getTotalTimeMillis()));

		return planCompareService.generateWorkbook(company, currentPlYr, futurePlYr, plansToCompareByBenefitType,
				currentRegionalBasePlanMappings, futureRegionalBasePlanMappings, httpRequest);
	}
	
	public Workbook generatePlanCompareReport(Company company, Map<String, Map<String, Set<String>>> comparePlans,
			HttpServletRequest httpRequest) {
		RealmPlanYear currentPlYr = null;
		RealmPlanYear futurePlYr = null;
		List<PlanYearDetailDto> planYears = realmPlanYearService.findCurrentAndFuturePlanYearsBy(company.getCode(),company.getQuater());
		if(!CollectionUtils.isEmpty(planYears)) {
			Optional<PlanYearDetailDto> currentYearPlan = planYears.stream().filter(hasCurrentOrFutureYear(PlanCompareConstants.CURRENT.getAction())).findAny();
			Optional<PlanYearDetailDto> futureYearPlan = planYears.stream().filter(hasCurrentOrFutureYear(PlanCompareConstants.FUTURE.getAction())).findAny();
			if(!currentYearPlan.isPresent()) {
				throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_PLAN_COMPARE_EXCEPTION_ERROR,
						BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, StrategyController.class.getName(), "Current year plans not found",
						null, null));
			}
			currentPlYr = realmPlanYearService.getRealmPlanYearById(Long.valueOf(currentYearPlan.get().getRealmYearId())) ;
			futurePlYr = futureYearPlan.isPresent() ? realmPlanYearService.getRealmPlanYearById(Long.valueOf(futureYearPlan.get().getRealmYearId())) : currentPlYr;
		}
		Map<String, BenefitPlan> currentRegionalBasePlanMappings = benefitPlanService
				.getRegionalBasePlanMapping(currentPlYr);
		Map<String, BenefitPlan> futureRegionalBasePlanMappings = benefitPlanService
				.getRegionalBasePlanMapping(futurePlYr);


		return planCompareService.generateWorkbook(company, currentPlYr, futurePlYr, comparePlans,
				currentRegionalBasePlanMappings, futureRegionalBasePlanMappings, httpRequest);
	}
	
	public List<PlanCompareDetailDto> getPlanCompareDetails(String companyCode, String exchange,
			List<Long> trinetStrategyIds, HttpServletRequest httpRequest) {
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchange));
		return prospectPlanCompareService.getPlanCompareDetails(company, trinetStrategyIds, httpRequest);
	}
	
	private Predicate<PlanYearDetailDto> hasCurrentOrFutureYear(String yearType){
		return plan -> plan.getPlanYear().equalsIgnoreCase(yearType);
	}

}