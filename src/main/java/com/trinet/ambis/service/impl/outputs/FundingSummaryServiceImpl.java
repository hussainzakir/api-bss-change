package com.trinet.ambis.service.impl.outputs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.helper.ModelCompareServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitGroup;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitOffer;
import com.trinet.ambis.rest.controllers.dto.outputs.FundingDetail;
import com.trinet.ambis.rest.controllers.dto.outputs.FundingSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.ModelCompareStrategy;
import com.trinet.ambis.service.outputs.FundingSummaryService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author rterle
 *
 */

@Service
@Slf4j
public class FundingSummaryServiceImpl implements FundingSummaryService{

	@Autowired
	CompanyService companyService;

	@Autowired
	StrategyFundingDataDao strategyFundingDataDao;

	@Override
	@Async
	public CompletableFuture<FundingSummary> getFundingSummaryData(Company company, OutputRequest outputRequest) {
		log.info("******* Generating Funding Summary Report Data *******");
		StopWatch taskWatch = new StopWatch(" Funding Summary Report Data *******");
		taskWatch.start();
		Optional<ModelCompareStrategy> fundingDetailsOptional = getFundingDetailsByStrategyId(company, outputRequest);
		CompletableFuture<FundingSummary> result = CompletableFuture
				.completedFuture(processFundingDetails(fundingDetailsOptional));
		taskWatch.stop();
		log.info(String.format("****** %s finished in :: %s *******", taskWatch.getId(),
				taskWatch.getTotalTimeMillis()));
		return result;
	}

	private Optional<ModelCompareStrategy> getFundingDetailsByStrategyId(Company company, OutputRequest outputRequest) {
		long strategyId = Long.parseLong(outputRequest.getTnStrategyId());
		boolean isProspect = company.isProspectCompany();
		Map<Long, ModelCompareStrategy> strategyMap = strategyFundingDataDao.getFundingDetailsByStrategyId(
				Arrays.asList(strategyId), company, false, company.getRealmPlanYear().getPlanYearEnd());
		ModelCompareStrategy strategyFunding = strategyMap.get(strategyId);
		strategyFunding.setName(ModelCompareServiceHelper.getStrategyDisplayName(strategyFunding.getName(),
				company.getRealmPlanYear(), false, isProspect));

		return Optional.ofNullable(strategyFunding);
	}

	private FundingSummary processFundingDetails(Optional<ModelCompareStrategy> strategyResultOptional) {

		List<String> orderedBenefitTypes = Arrays.asList(StringUtils.capitalize(PlanTypesEnum.MEDICAL.getName()),
				StringUtils.capitalize(PlanTypesEnum.DENTAL.getName()),
				StringUtils.capitalize(PlanTypesEnum.VISION.getName()));
		List<BenefitGroup> benefitGroups = new ArrayList<>();
		// Create an empty funding summary object
		FundingSummary fundingSummaryResult = new FundingSummary(benefitGroups, orderedBenefitTypes);

		// Check if Model Compare Strategy result is available
		if (strategyResultOptional.isPresent()) {
			ModelCompareStrategy strategyResult = strategyResultOptional.get();
			Map<Long, Set<String>> planLevelOverrideMap = strategyFundingDataDao
					.getPlanLevelOverrides(strategyResult.getId());

			// Iterate on group data from strategy funding to set groups in funding summary
			strategyResult.getGroupFundingList().forEach(groupFunding -> {

				BenefitGroup benGroup = new BenefitGroup();
				benGroup.setBenefitGroupName(groupFunding.getName());
				Map<String, BenefitOffer> benefitOffers = new HashMap<>();
				orderedBenefitTypes.forEach(benType -> benefitOffers.put(benType, null));
				benGroup.setBenefitOffers(benefitOffers);
				
				Set<String> planTypes = planLevelOverrideMap.getOrDefault(groupFunding.getId(), Collections.emptySet());
				
				// Iterate on offerTypeFunding Map from each group to set benefitOffers Map in
				// benefit group by plan type
				groupFunding.getOfferTypeFunding().forEach((benType, benefitOfferFunding) -> {

					// Start building benefit offer for each benefit offer funding type
					BenefitOffer benefitOffer = new BenefitOffer();
					benefitOffer.setBenefitType(benefitOfferFunding.getType());

					// If Funding type is "Covered Person Percent"
					if(benefitOfferFunding.getFundingType().equals(BSSApplicationConstants.CFPCT)) {
						benefitOffer.setFundingType(BSSApplicationConstants.CFPCT_DESC);

						// If a plan exists as base fund plan
						if (benefitOfferFunding.getBaseFundPlan() != null) {

							if (benefitOfferFunding.getBaseFundPlan().equals(BSSApplicationConstants.FLAT_MAX)) {
								String planName = "By Amount";
								benefitOffer.setFundingBasePlan(planName);
								// Set funding detail data
								setFundingDetail(benefitOfferFunding.getCoverageLevels(),
										benefitOfferFunding.getCoverageLevelFundingFlatMax(), benefitOffer);
							} else {
								// Get plan name of respective base fund plan
								String planName = benefitOfferFunding.getBenefitPlanDesc();
								benefitOffer.setFundingBasePlan(planName);
								// Set funding detail data
								setFundingDetail(benefitOfferFunding.getCoverageLevels(),
										benefitOfferFunding.getFundingBasePlanLimits(), benefitOffer);
							}
						}
						// If no base plan was selected
						else {
							//Assign base plan name as ""(empty) to display "None" on template
							benefitOffer.setFundingBasePlan("");
							//Set funding detail data
							setFundingDetail(benefitOfferFunding.getCoverageLevels(), benefitOffer);
						}
					}
					// If funding type is flat
					else {

						benefitOffer.setFundingType(BSSApplicationConstants.FLAT_DESC);
						// No base plan exits for flat funding type so assign null to funding base plane (value will be null)
						benefitOffer.setFundingBasePlan(benefitOfferFunding.getBaseFundPlan());
						// Set funding detail data
						setFundingDetailForFlat(benefitOfferFunding.getCoverageLevels(), benefitOffer);

					}
					benefitOffer.setPlanLevelFundingOverride(planTypes.contains(benefitOffer.getBenefitType()));
					benGroup.getBenefitOffers().put(StringUtils.capitalize(benType), benefitOffer);
				});
				fundingSummaryResult.getBenefitGroups().add(benGroup);
			});
		}
		// Check for not offered benefit types and assign respective values
		verifyNonOfferedBenefitTypes(fundingSummaryResult);
		return fundingSummaryResult;
	}

	private static void verifyNonOfferedBenefitTypes(FundingSummary fundingSummaryResult) {
		fundingSummaryResult.getBenefitGroups()
				.forEach(benefitGroup -> benefitGroup.getBenefitOffers().forEach((benType, benefitOffer) -> {
					if (benefitOffer == null) {
						FundingDetail employeePlanValue = new FundingDetail(ProspectConstants.NOT_AVAILABLE,
								ProspectConstants.EMPLOYEE_FUNDING_DESC);
						FundingDetail employeeSpousePlanValue = new FundingDetail(ProspectConstants.NOT_AVAILABLE,
								ProspectConstants.EMPLOYEE_SPOUSE_FUNDING_DESC);
						FundingDetail employeeChildrenPlanValue = new FundingDetail(ProspectConstants.NOT_AVAILABLE,
								ProspectConstants.EMPLOYEE_CHILDREN_FUNDING_DESC);
						FundingDetail familyPlanValue = new FundingDetail(ProspectConstants.NOT_AVAILABLE,
								ProspectConstants.FAMILY_FUNDING_DESC);

						benefitOffer = BenefitOffer.builder().benefitType(benType)
								.fundingType(ProspectConstants.NOT_OFFERED).fundingBasePlan(null)
								.employeePlanValue(employeePlanValue).employeeSpousePlanValue(employeeSpousePlanValue)
								.employeeChildrenPlanValue(employeeChildrenPlanValue).familyPlanValue(familyPlanValue)
								.build();
					}
				}));
	}
	
	public static void setFundingDetail(List<CoverageLevel> coverageLevels, List<CoverageLevel> fundingBasePlanLimits, BenefitOffer benefitOffer ) {
		// Iterate on list of coverage levels from benefit offer funding to set respective Funding detail object
		coverageLevels.forEach(coverageLevel -> {

			StringBuilder sb = new StringBuilder();
			String fundingDetailValue = fundingBasePlanLimits.stream()
					.filter(entry -> entry.getId().equals(coverageLevel.getId()))
					.findFirst()
					.map(entry -> {
						sb.append(coverageLevel.getContribution().toString()).append("%").append(" up to $").append(entry.getContribution().setScale(2));
						return sb.toString();
					}).orElse(sb.append(coverageLevel.getContribution().toString()).append("%").toString());

			if (coverageLevel.getId().equals(CoverageCodesEnums.COV_EMPLOYEE.getId())) {
				// Build funding detail object for each repective type
				FundingDetail fundingDetail = FundingDetail.builder()
						.label(ProspectConstants.EMPLOYEE_FUNDING_DESC)
						.value(fundingDetailValue).build();
				benefitOffer.setEmployeePlanValue(fundingDetail);
			} else if (coverageLevel.getId().equals(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId())) {
				// Build funding detail object for each repective type
				FundingDetail fundingDetail = FundingDetail.builder()
						.label(ProspectConstants.EMPLOYEE_SPOUSE_FUNDING_DESC)
						.value(fundingDetailValue).build();
				benefitOffer.setEmployeeSpousePlanValue(fundingDetail);
			} else if (coverageLevel.getId().equals(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId())) {
				// Build funding detail object for each repective type
				FundingDetail fundingDetail = FundingDetail.builder()
						.label(ProspectConstants.EMPLOYEE_CHILDREN_FUNDING_DESC)
						.value(fundingDetailValue).build();
				benefitOffer.setEmployeeChildrenPlanValue(fundingDetail);
			} else if (coverageLevel.getId().equals(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId())) {
				// Build funding detail object for each repective type
				FundingDetail fundingDetail = FundingDetail.builder()
						.label(ProspectConstants.FAMILY_FUNDING_DESC)
						.value(fundingDetailValue).build();
				benefitOffer.setFamilyPlanValue(fundingDetail);
			}
		});
	}

	public static void setFundingDetail(List<CoverageLevel> coverageLevels, BenefitOffer benefitOffer ) {
		// Iterate on list of coverage levels from benefit offer funding to set respective Funding detail object
		coverageLevels.forEach(coverageLevel -> {

			StringBuilder sb = new StringBuilder();
			String fundingDetailValue = sb.append(coverageLevel.getContribution().toString()).append("%").toString();

			if (coverageLevel.getId().equals(CoverageCodesEnums.COV_EMPLOYEE.getId())) {
				// Build funding detail object for each respective type
				FundingDetail fundingDetail = FundingDetail.builder()
						.label(ProspectConstants.EMPLOYEE_FUNDING_DESC)
						.value(fundingDetailValue).build();
				benefitOffer.setEmployeePlanValue(fundingDetail);
			} else if (coverageLevel.getId().equals(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId())) {
				// Build funding detail object for each respective type
				FundingDetail fundingDetail = FundingDetail.builder()
						.label(ProspectConstants.EMPLOYEE_SPOUSE_FUNDING_DESC)
						.value(fundingDetailValue).build();
				benefitOffer.setEmployeeSpousePlanValue(fundingDetail);
			} else if (coverageLevel.getId().equals(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId())) {
				// Build funding detail object for each respective type
				FundingDetail fundingDetail = FundingDetail.builder()
						.label(ProspectConstants.EMPLOYEE_CHILDREN_FUNDING_DESC)
						.value(fundingDetailValue).build();
				benefitOffer.setEmployeeChildrenPlanValue(fundingDetail);
			} else if (coverageLevel.getId().equals(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId())) {
				// Build funding detail object for each respective type
				FundingDetail fundingDetail = FundingDetail.builder()
						.label(ProspectConstants.FAMILY_FUNDING_DESC)
						.value(fundingDetailValue).build();
				benefitOffer.setFamilyPlanValue(fundingDetail);
			}
		});
	}

	public static void setFundingDetailForFlat(List<CoverageLevel> coverageLevels, BenefitOffer benefitOffer ) {
		// Iterate on list of coverage levels from benefit offer funding to set respective Funding detail object
		coverageLevels.forEach(coverageLevel -> {

			StringBuilder sb = new StringBuilder();
			String fundingDetailLabel = CoverageCodesEnums.nameFromId(coverageLevel.getId());
			String fundingDetailValue = sb.append("$").append(coverageLevel.getContribution().setScale(2)).toString();

			// Build funding detail object for each repective type
			FundingDetail fundingDetail = FundingDetail.builder()
					.label(fundingDetailLabel)
					.value(fundingDetailValue).build();

			if (coverageLevel.getId().equals(CoverageCodesEnums.COV_EMPLOYEE.getId())) {
				fundingDetail.setLabel(ProspectConstants.EMPLOYEE_FUNDING_DESC);
				benefitOffer.setEmployeePlanValue(fundingDetail);
			} else if (coverageLevel.getId().equals(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId())) {
				benefitOffer.setEmployeeSpousePlanValue(fundingDetail);
			} else if (coverageLevel.getId().equals(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId())) {
				benefitOffer.setEmployeeChildrenPlanValue(fundingDetail);
			} else if (coverageLevel.getId().equals(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId())) {
				benefitOffer.setFamilyPlanValue(fundingDetail);
			}
		});
	}

}
