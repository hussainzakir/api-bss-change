package com.trinet.ambis.service.impl.outputs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.OutputBenefitsTypeEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.rest.controllers.dto.outputs.BasePlanComparison;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitCostSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitTypeEmployeeCostSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.FundingSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputData;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendix;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.outputs.BenefitCostSummaryService;
import com.trinet.ambis.service.outputs.EmployeeCostSummaryService;
import com.trinet.ambis.service.outputs.FundingSummaryService;
import com.trinet.ambis.service.outputs.OutputReportDataService;
import com.trinet.ambis.service.outputs.OutputService;
import com.trinet.ambis.service.outputs.PlanAppendixService;
import com.trinet.ambis.service.outputs.PlanComparisonService;
import com.trinet.ambis.service.outputs.TitlePageService;
import com.trinet.ambis.service.prospect.ProspectPlanService;
import com.trinet.ambis.service.prospect.dto.BenTypeOfferRes;
import com.trinet.ambis.service.prospect.dto.BenefitPlansRes;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

/**
 * @author pallu
 *
 */
@Service
@Log4j2
public class OutputReportDataServiceImpl implements OutputReportDataService {
	
	@Autowired
	private TitlePageService titlePageService;

	@Autowired
	private EmployeeCostSummaryService emplCostSummaryService;

	@Autowired
	private PlanAppendixService planAppendixService;

	@Autowired
	private BenefitCostSummaryService benefitCostSummaryService;

	@Autowired
	private FundingSummaryService fundingSummaryService;

	@Autowired
	private PlanComparisonService planComparisonService;
	
	@Autowired
	private StrategyService strategyService;

	@Autowired
	private ProspectPlanService prospectPlanService;
	
	@Autowired
	private OutputService prospectoutputsService;

	@Override
	public OutputData getData(OutputRequest outputRequest, Company company, HttpServletRequest httpRequest) {
        List<String> includedBenTypes = outputRequest.getBenefitTypes();
		transformToBSSBenefitTypeCode(outputRequest);
		OutputData data = new OutputData();

		//Check what all benefit types offered on TriNet Strategy ID
		populateTriNetStrategyOfferedBenTypesDetails(data, outputRequest);

		data.setCurrStrategyIsBenTypeOffered(getProspectPlansBy().apply(company.getCode()));
		data.setCompanyName(company.getName());
		data.setStrategyName(getStrategyName(company,outputRequest));
        data.setIncludedPlanTypes(includedBenTypes);
		if((outputRequest.getBenefitTypes().contains(BSSApplicationConstants.MEDICAL_PLAN_TYPE))
				&& (data.getTrinetStrategyIsBenTypeOffered().get(ProspectConstants.MEDICAL_PLAN_TYPE_DESC))) {
			if (!CompanyServiceHelper.isTibProspect(company)) {
				data.setPrimaryCarrierName(strategyService.getPrimaryCarrierName(company, outputRequest.getTnStrategyId()));
			} else {
				data.setPrimaryCarrierName("");
			}
		}

		if(company.isContingentPricing())
			data.setIsContingent(BSSApplicationConstants.TRUE);
		data.setGeneratePlanAppendixOnly(generateOnlyPlanAppendixReport(outputRequest));
		if(data.isGeneratePlanAppendixOnly() && AppRulesAndConfigsUtils.isBssOutputPhase2Enabled()){
			String topPriorityBenTypeCode = java.util.stream.Stream.of(
							OutputBenefitsTypeEnums.MEDICAL.getCode(),
							OutputBenefitsTypeEnums.DENTAL.getCode(),
							OutputBenefitsTypeEnums.VISION.getCode(),
							OutputBenefitsTypeEnums.LIFE.getCode(),
							OutputBenefitsTypeEnums.DISABILITY.getCode())
					.filter(outputRequest.getBenefitTypes()::contains)
					.findFirst()
					.orElse(outputRequest.getBenefitTypes().get(0));

			httpRequest.setAttribute(BSSApplicationConstants.PLAN_APPENDIX_FIRST_BEN_TYPE, topPriorityBenTypeCode);
			data.setPlanappendixfirstBentype(topPriorityBenTypeCode);

		}
		List<String> templateNames = new ArrayList<>(outputRequest.getTemplateNames());
		
		CompletableFuture<Map<String, BasePlanComparison>> additionalBenefitPlanCompareDataFuture = templateNames
				.contains(ProspectConstants.PLAN_APPENDIX) || templateNames.contains(ProspectConstants.PLAN_COMPARISON)
						? planComparisonService.getAdditionalBenfitsCompareData(company, outputRequest)
						: null;
		
		CompletableFuture<Void> appendixCf = CompletableFuture
				.runAsync(() -> prepareReportDataForPlanAppendix(data, company, outputRequest, templateNames,
						httpRequest, additionalBenefitPlanCompareDataFuture))
				.exceptionally(ex -> {
					if (ex instanceof InterruptedException) {
						log.error(
								"Thread is interrupted while generating plan appendix report for prospect : {} exception :",
								company.getCode(), ex);
						Thread.currentThread().interrupt();
					} else {
						log.error(
								"Exception occured while generating plan appendix report for prospect : {} exception :",
								company.getCode(), ex);
					}
					return null;
				});

		if(!data.isGeneratePlanAppendixOnly()){
			prepareReportDataWithoutPlanAppendix(data, company, outputRequest, templateNames, httpRequest, additionalBenefitPlanCompareDataFuture);
		}

		appendixCf.join();

		// Set Title page data, obtained from report
		data.setTitlePage(titlePageService.getTitlePageData(outputRequest,company));
		data.setTemplateNames(templateNames);
		// Set if the company is a TIB company
		data.setTibCompany(CompanyServiceHelper.isTibProspect(company));
		data.setEmployeeComparePageBreakEnabled(AppRulesAndConfigsUtils.isEmployeeComparePageBreakEnabled());
		data.setLifeAndDiPageBreakEnabled(AppRulesAndConfigsUtils.isLifeAndDiPageBreakEnabled());
		return data;
	}

	private void populateTriNetStrategyOfferedBenTypesDetails(OutputData data, OutputRequest request) {

		List<BenTypeOfferRes> offeredBenTypesResponse = prospectoutputsService.getPlanTypeOfferedDetails(List.of(Long.valueOf(request.getTnStrategyId())), BSSApplicationConstants.ALL_PLAN_TYPES_INCLUD_ADDITIONAL);
		Map<String, Boolean> benTypesOffered = new HashMap<>();
		benTypesOffered.put(ProspectConstants.MEDICAL_PLAN_TYPE_DESC,Boolean.FALSE);
		benTypesOffered.put(ProspectConstants.DENTAL_PLAN_TYPE_DESC,Boolean.FALSE);
		benTypesOffered.put(ProspectConstants.VISION_PLAN_TYPE_DESC,Boolean.FALSE);
		benTypesOffered.put(ProspectConstants.LIFE_ADD_PLAN_TYPE_DESC,Boolean.FALSE);
		benTypesOffered.put(ProspectConstants.DISABILITY_PLAN_TYPE_DESC,Boolean.FALSE);
		data.setTrinetStrategyIsBenTypeOffered(benTypesOffered);

		if(!offeredBenTypesResponse.isEmpty()) {
			BenTypeOfferRes offeredBenTypes = offeredBenTypesResponse.get(0);
			Set<String> benTypes = offeredBenTypes.getOfferTypes();
			data.getTrinetStrategyIsBenTypeOffered().put(ProspectConstants.MEDICAL_PLAN_TYPE_DESC,benTypes.contains(ProspectConstants.MEDICAL_PLAN_TYPE));
			data.getTrinetStrategyIsBenTypeOffered().put(ProspectConstants.DENTAL_PLAN_TYPE_DESC,(benTypes.contains(ProspectConstants.DENTAL_PLAN_TYPE)
					|| benTypes.contains(ProspectConstants.DENTAL_VOL_PLAN_TYPE)));
			data.getTrinetStrategyIsBenTypeOffered().put(ProspectConstants.VISION_PLAN_TYPE_DESC,(benTypes.contains(ProspectConstants.VISION_PLAN_TYPE)
					|| benTypes.contains(ProspectConstants.VISION_VOL_PLAN_TYPE)));
			data.getTrinetStrategyIsBenTypeOffered().put(ProspectConstants.LIFE_ADD_PLAN_TYPE_DESC,benTypes.contains(ProspectConstants.LIFE_ADD_PLAN_TYPE));
			data.getTrinetStrategyIsBenTypeOffered().put(ProspectConstants.DISABILITY_PLAN_TYPE_DESC,(benTypes.contains(ProspectConstants.STD_PLAN_TYPE) || benTypes.contains(ProspectConstants.LTD_PLAN_TYPE)));
		}
	}

	private OutputData prepareReportDataWithoutPlanAppendix(OutputData data, Company company,
			OutputRequest outputRequest, List<String> templateNames, HttpServletRequest httpRequest,CompletableFuture<Map<String, BasePlanComparison>> additionalBenefitPlanCompareDataFuture) {

		CompletableFuture<BenefitTypeEmployeeCostSummary> employeeLvlCostSummaryFuture = emplCostSummaryService
				.getCostSummaryData(data, company, outputRequest);

		CompletableFuture<FundingSummary> fundingSummaryFuture = fundingSummaryService.getFundingSummaryData(company,
				outputRequest);

		CompletableFuture<Map<String, BasePlanComparison>> planComparisonDataFuture = null;

		if (templateNames.contains(ProspectConstants.PLAN_COMPARISON)) {
			planComparisonDataFuture = planComparisonService.getPlanComparisonData(company, outputRequest, httpRequest);
		}

		BenefitTypeEmployeeCostSummary employeeLvlCostSummary = extractEmployeeLvlCostSummaryFromFutureObj(company,
				employeeLvlCostSummaryFuture);

		CompletableFuture<BenefitCostSummary> benefitCostSummaryFuture = benefitCostSummaryService
				.getBenefitCostSummaryData(employeeLvlCostSummary, outputRequest.getTnStrategyId(), company.getCode(), outputRequest.getBenefitTypes());

		BenefitCostSummary benefitCostSummary = extractBenefitsCostSummaryFromFutureObj(company,
				benefitCostSummaryFuture);

		Map<String, BasePlanComparison> planComparisonData = extractPlanComparisonDataFromFutureObj(company,
				planComparisonDataFuture);
		Map<String, BasePlanComparison> additionalBenefitsPlanComparisonData = extractPlanComparisonDataFromFutureObj(company,
				additionalBenefitPlanCompareDataFuture);
		
		if (planComparisonData != null) {
	        planComparisonData.putAll(additionalBenefitsPlanComparisonData);
	    } else {
	        planComparisonData = additionalBenefitsPlanComparisonData;
	    }

		FundingSummary fundingSummary = extractFundingSummaryFromFutureObj(company, fundingSummaryFuture);

		if (null != employeeLvlCostSummary) {
			filterEmployeeCostSummaryBySelection(employeeLvlCostSummary, outputRequest);
			data.setEmployeeCostSummary(employeeLvlCostSummary);
		}

		if (Objects.nonNull(planComparisonData) && !planComparisonData.isEmpty()) {
			data.setPlanComparison(planComparisonData);
		} else {
			templateNames.removeIf(type -> type.equalsIgnoreCase(ProspectConstants.PLAN_COMPARISON));
		}

		data.setBenefitCostSummary(benefitCostSummary);
		data.setFundingSummary(fundingSummary);
		return data;
	}

	private BenefitTypeEmployeeCostSummary extractEmployeeLvlCostSummaryFromFutureObj(Company company,
			CompletableFuture<BenefitTypeEmployeeCostSummary> employeeLvlCostSummaryFuture) {
		BenefitTypeEmployeeCostSummary employeeLvlCostSummary = null;
		try {
			employeeLvlCostSummary = employeeLvlCostSummaryFuture.get();
		} catch (InterruptedException e) {
			log.error(
					"Thread is interrupted while generating employee cost summary report for prospect : {} exception : ",
					company.getCode(), e);
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			log.error("Exception occured while generating employee cost summary report for prospect : {} exception : ",
					company.getCode(), e);
		}
		return employeeLvlCostSummary;
	}

	private BenefitCostSummary extractBenefitsCostSummaryFromFutureObj(Company company,
			CompletableFuture<BenefitCostSummary> benefitCostSummaryFuture) {
		BenefitCostSummary benefitCostSummary = null;
		try {
			benefitCostSummary = benefitCostSummaryFuture.get();
		} catch (InterruptedException e) {
			log.error(
					"Thread is interrupted while generating benefit cost summary report for prospect : {} exception : ",
					company.getCode(), e);
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			log.error("Exception occured while generating benefit cost summary report for prospect : {} exception : ",
					company.getCode(), e);
		}
		return benefitCostSummary;
	}

	private Map<String, BasePlanComparison> extractPlanComparisonDataFromFutureObj(Company company,
			CompletableFuture<Map<String, BasePlanComparison>> planComparisonDataFuture) {
		Map<String, BasePlanComparison> planComparisonData = null;
		if (planComparisonDataFuture != null) {
			try {
				planComparisonData = planComparisonDataFuture.get();
			} catch (InterruptedException e) {
				log.error(
						"Thread is interrupted while generating plan comparison report for prospect : {} exception : ",
						company.getCode(), e);
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				log.error("Exception occured while generating plan comparison report for prospect : {} exception : ",
						company.getCode(), e);
			}
		}
		return planComparisonData;
	}

	private FundingSummary extractFundingSummaryFromFutureObj(Company company,
			CompletableFuture<FundingSummary> fundingSummaryFuture) {
		FundingSummary fundingSummary = null;
		try {
			fundingSummary = fundingSummaryFuture.get();
		} catch (InterruptedException e) {
			log.error(
					"Thread is interrupted while generating benefit funding summary report for prospect : {} exception : ",
					company.getCode(), e);
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			log.error(
					"Exception occured while generating benefit funding summary report for prospect : {} exception : ",
					company.getCode(), e);
		}
		return fundingSummary;
	}

	private void filterEmployeeCostSummaryBySelection(BenefitTypeEmployeeCostSummary employeeLvlCostSummary, OutputRequest outputRequest) {
		employeeLvlCostSummary.getEmplCostSummaryByBenGroup().keySet().removeIf(benefitType-> !outputRequest.getBenefitTypes().contains(benefitType));

		//Remove employees with waived coverage from each group for each benefit type as we do not show them on the Employee Cost Summary report
		employeeLvlCostSummary.getEmplCostSummaryByBenGroup().replaceAll((benType, emplCostByGroup) -> {
			emplCostByGroup.replaceAll((group, emplCostSummaryList) -> emplCostSummaryList.stream()
					.filter(emplCostSummary -> !emplCostSummary.getEmployee().getCoverageCode().equals(ProspectConstants.WAVED_COVERAGE)).collect(Collectors.toList()));
			return emplCostByGroup;
		});
	}

	private void prepareReportDataForPlanAppendix(OutputData data, Company company, OutputRequest outputRequest,
			List<String> templateNames, HttpServletRequest httpRequest, CompletableFuture<Map<String, BasePlanComparison>> additionalBenefitPlanCompareDataFuture) {

		Map<String, PlanAppendix> planAppendixData = null;

		// If Plan Appendix is selected, then get the plan appendix report data
		if (templateNames.contains(ProspectConstants.PLAN_APPENDIX)) {
			log.info("******* Generating Plan Appendix Report Data *******");
			StopWatch taskWatch = new StopWatch(" Plan Appendix Report Data *******");
			taskWatch.start();
			planAppendixData = planAppendixService.getPlanAppendixData(company, outputRequest, httpRequest,additionalBenefitPlanCompareDataFuture);
			taskWatch.stop();
			log.info(String.format("****** %s finished in :: %s *******", taskWatch.getId(),
					taskWatch.getTotalTimeMillis()));

			if (Objects.nonNull(planAppendixData) && !planAppendixData.isEmpty()) {
				data.setPlanAppendix(planAppendixData);
			} else {
				templateNames.removeIf(type -> type.equalsIgnoreCase(ProspectConstants.PLAN_APPENDIX));
			}
		}
	}

	private void transformToBSSBenefitTypeCode(OutputRequest outputRequest) {
		List<String> bSSBenefitTypeCodes = outputRequest.getBenefitTypes().stream()
				.map(OutputBenefitsTypeEnums::getBenTypeCodeByName).collect(Collectors.toList());
		if (bSSBenefitTypeCodes.contains(BSSApplicationConstants.DENTAL_PLAN_TYPE)) {
			bSSBenefitTypeCodes.add(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);
		}
		if (bSSBenefitTypeCodes.contains(BSSApplicationConstants.VISION_PLAN_TYPE)) {
			bSSBenefitTypeCodes.add(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE);
		}
		outputRequest.setBenefitTypes(bSSBenefitTypeCodes);
	}
	
	private String getStrategyName(Company company, OutputRequest outputRequest) {
		List<Strategy> allStrategies = strategyService.getAllStrategies(company.getId());
		Long strategyId =  Long.valueOf(outputRequest.getTnStrategyId());
		Optional<Strategy> strategy = allStrategies.stream().filter(strgy -> strgy.getId().equals(strategyId)).findAny();
		return strategy.isPresent() ? strategy.get().getName() :  null;
	}

	private boolean generateOnlyPlanAppendixReport(OutputRequest outputRequest){
		return notEmptyFilter().and(hasOnlyApxFilter()).test(outputRequest.getTemplateNames());
	}

	private Predicate<List<String>> notEmptyFilter(){
		return templateNames -> !CollectionUtils.isEmpty(templateNames);
	}

	private Predicate<List<String>> hasOnlyApxFilter(){
		return templateNames -> (templateNames.size() ==  1 && templateNames.contains(ProspectConstants.PLAN_APPENDIX));
	}
	
	private Function<String, Map<String, Boolean>> getProspectPlansBy() {
		return prospectId -> {
			Map<String, List<BenefitPlansRes>> prospectBenPlans = prospectPlanService.getBenefitPlansBy(prospectId)
					.stream().collect(Collectors.groupingBy(BenefitPlansRes::getBenefitTypeCode));
			return BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER.stream().collect(
					Collectors.toMap(benType -> benType, benType -> Objects.nonNull(prospectBenPlans.get(benType))));
		};
	}
}