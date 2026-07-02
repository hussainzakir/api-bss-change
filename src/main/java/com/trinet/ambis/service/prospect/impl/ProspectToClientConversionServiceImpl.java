package com.trinet.ambis.service.prospect.impl;


import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Bundle;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectToClientConversionRequest;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectToClientConversionResponse;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.BenefitsBundleService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.email.dto.ClientConversionFailureEmailDto;
import com.trinet.ambis.service.email.dto.SupportEmailDto;
import com.trinet.ambis.service.prospect.ProspectToClientConversionService;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.Utils;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.service.EmployeeStrategyGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProspectToClientConversionServiceImpl implements ProspectToClientConversionService {

	private static final Logger logger = LoggerFactory.getLogger(ProspectToClientConversionServiceImpl.class);

	@Autowired
	CompanyService companyService;

	@Autowired
	RealmPlanYearService realmPlanYearService;

	@Autowired
	StrategyService strategyService;

	@Autowired
	BenefitGroupService benefitGroupService;
	
	@Autowired
	StrategyGroupService strategyGroupService;

	@Autowired
	EmailGenService emailGenService;

    @Autowired
    private Configuration configuration;
	
	@Autowired
	EmployeeStrategyGroupService employeeStrategyGroupService;

    @Autowired
    StrategyDataDao strategyDataDao;

	@Autowired
	BenefitsBundleService benefitsBundleService;

	@Transactional
	@Override
    public ProspectToClientConversionResponse processProspectToClientConversion(ProspectToClientConversionRequest request) {
		String psCompanyCode = request.getCompanyCode();
		String prospectId = request.getProspectId();
		String streamEventId = request.getStreamEventId();
		Company prospectCompany = null;
        try {
			Company psCompany = companyService.getPsCompanyDetails(psCompanyCode);

			prospectCompany = findByProspectIdOrPsCompanyCode(psCompany, prospectId);

			if (prospectCompany == null) {
                throw new BSSBadDataException(String.format(
					"BSS proposal was submitted for different exchange and quarter than company is onboarded on. " +
					"Onboarding exchange: %s, quarter: %s, but no matching BSS company found for prospect ID: %s", 
					psCompany.getRealm().getBenExchange(), psCompany.getQuater(), prospectId));
			}

            validateRiskType(request, prospectCompany, prospectId, psCompanyCode);
            validateBundleId(request, prospectCompany, prospectId, psCompanyCode);

			if(prospectCompany.isLargeDealProspect()){
				updateCompanyCodeForBundle(prospectId, psCompanyCode);
			}

			updateStrategiesAndGroupsForClientCompany(prospectCompany, psCompany, prospectId);

            updatePsCompanyCodeForProspect(prospectCompany, psCompanyCode);

			return ProspectToClientConversionResponse.builder()
                    .k1(psCompany.isK1Company())
                    .bssCompanyId(prospectCompany.getId())
                    .build();
		} catch (Exception ex) {
            String errorMessage = "Error message not available";
            if(ex instanceof BSSBadDataException) {
                errorMessage = ex.getMessage();
            }
            if (streamEventId == null || streamEventId.trim().isEmpty()) {
                streamEventId = "";
            }
            String emailBody = buildEmailBody(psCompanyCode, prospectId, streamEventId, errorMessage);
            SupportEmailDto supportEmailDto = ClientConversionFailureEmailDto.builder()
                    .companyCode(psCompanyCode)
                    .emailBody(emailBody)
                    .sendToBSS(true)
                    .build();
            emailGenService.createSupportEmail(supportEmailDto);
			throw ex;
		}
	}

	private void validateRiskType(ProspectToClientConversionRequest request, Company prospectCompany, String prospectId,
			String psCompanyCode) {
		String requestRiskType = !StringUtils.isBlank(request.getRiskType()) ? request.getRiskType()
				: com.trinet.ambis.enums.RiskTypeEnum.BANDS.name();
		String companyRiskType = prospectCompany.getRiskType() != null ? prospectCompany.getRiskType().name()
				: com.trinet.ambis.enums.RiskTypeEnum.BANDS.name();
		if (!requestRiskType.equalsIgnoreCase(companyRiskType)) {
			throw new BSSBadDataException(String.format(
					"Prospect to Client conversion request was submitted with a different riskType than proposed. "
							+ "ProspectId: %s, CompanyCode: %s",
					prospectId, psCompanyCode));
		}
	}

	private void validateBundleId(ProspectToClientConversionRequest request, Company prospectCompany, String prospectId,
			String psCompanyCode) {
		String requestBundleId = StringUtils.isBlank(request.getBundleId()) ? null : request.getBundleId();
		String companyBundleId = prospectCompany.getBundleId() != null ? String.valueOf(prospectCompany.getBundleId()) : null;

		if (!Objects.equals(requestBundleId, companyBundleId)) {
			throw new BSSBadDataException(String.format(
					"Prospect to Client conversion request was submitted with a different bundle than proposed. "
							+ "ProspectId: %s, CompanyCode: %s",
					prospectId, psCompanyCode));
		}
	}

	private void updateCompanyCodeForBundle(String prospectId, String psCompanyCode) {

		// Try lookup by prospectId, otherwise by psCompanyCode
		Bundle bundle = Optional.ofNullable(benefitsBundleService.getBundleByCompanyCode(prospectId))
				.orElseGet(() -> benefitsBundleService.getBundleByCompanyCode(psCompanyCode));

		if (bundle == null) {
			throw new BSSBadDataException(
					String.format("No bundle data found for prospectId: %s or companyCode: %s",
							prospectId, psCompanyCode));
		}

		// Only update if the company code is different
		if (!psCompanyCode.equalsIgnoreCase(bundle.getCompanyCode())) {
			bundle.setCompanyCode(psCompanyCode);
			benefitsBundleService.save(bundle);
		}
	}

	private String buildEmailBody(String psCompanyCode, String prospectId, String streamEventId, String errorMessage) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("companyCode", psCompanyCode);
        parameters.put("prospectId", prospectId);
        parameters.put("streamEventId", streamEventId);
        parameters.put("errorMessage", errorMessage);

        return transform(configuration, parameters, "prospectToClientConversion.ftl");
    }

    private String transform(Configuration configuration, Map<String, Object> parameters, String templateLocation) {
        StringWriter dataModel = new StringWriter();

        try {
            Template template = configuration.getTemplate(templateLocation,
                    StandardCharsets.UTF_8.name());
            template.process(parameters, dataModel);
        } catch (TemplateException | IOException e) {
            log.error("Error processing FreeMarker template: {}", e.getMessage(), e);
            throw new RuntimeException(
                    "Error generating email body for Prospect-to-Client conversion", e);
        }

        return dataModel.toString();
    }

	private Company findByProspectIdOrPsCompanyCode(Company psCompany, String prospectId) {

		RealmPlanYear realmPlanYear = realmPlanYearService.getRealmPlanYear(psCompany.getRealm().getId(),
				psCompany.getQuater(), Utils.convertStringToDate(psCompany.getPlanStartDate(), Constants.DATE_FORMAT));
		psCompany.setRealmPlanYear(realmPlanYear);
		Company prospectCompany = companyService.findCompanyBy(prospectId, realmPlanYear.getId());
		if (prospectCompany != null) {
			return prospectCompany;
		}

		// If not found by prospectId, find by psCompany code
		return companyService.findCompanyBy(psCompany.getCode(), realmPlanYear.getId());
	}

	private void updatePsCompanyCodeForProspect(Company prospectCompany, String psCompanyCode) {
		companyService.updatePsCompanyCodeForProspect(prospectCompany.getId(), psCompanyCode);
	}

	private void updateStrategiesAndGroupsForClientCompany(Company prospectCompany, Company psCompany, String prospectId) {
		List<Strategy> prospectStrategies = strategyService.findBy(prospectId);

        prospectCompany.setQuater(psCompany.getQuater());
		// Find and validate submitted strategy
		Strategy submittedStrategy = findAndValidateSubmittedStrategy(prospectStrategies, prospectCompany);
		long submittedStrategyId = submittedStrategy.getId();
		
		// Invalidate unsubmitted strategies
		deleteUnsubmittedStrategiesAndGroups(prospectCompany, prospectStrategies, submittedStrategyId);
		
		// Handle K1 group cleanup for non-K1 companies
		handleK1GroupCleanupForNonK1Companies(psCompany, submittedStrategyId);
		
		// Update benefit groups with PS company details
		updateBenefitGroupsForPSCompany(submittedStrategyId, psCompany);
		
		// Finalize submitted strategy
		strategyService.updateSubmittedStrategyDetails(submittedStrategyId);
	}
	
	/**
	 * Finds and validates the submitted strategy for the onboarding company.
	 * Ensures there is exactly one submitted strategy for the company.
	 */
	private Strategy findAndValidateSubmittedStrategy(List<Strategy> prospectStrategies, Company prospectCompany) {
		List<Strategy> allSubmittedStrategies = findAllSubmittedStrategies(prospectStrategies);
		
		if (CollectionUtils.isEmpty(allSubmittedStrategies)) {
			throw new BSSBadDataException(String.format(
				"No submitted strategy found for prospect. Company code: %s, quarter: %s. " +
				"Ensure at least one strategy has been submitted before attempting client conversion.", 
				prospectCompany.getCode(), prospectCompany.getQuater()));
		}
		
		List<Strategy> submittedStrategiesForOnboardingCompany = filterStrategiesForCompany(allSubmittedStrategies, prospectCompany.getId());
		
		if (CollectionUtils.isEmpty(submittedStrategiesForOnboardingCompany)) {
			throw new BSSBadDataException(String.format(
				"Strategy submitted for different company than the one being onboarded. " +
				"Company being onboarded: %s (quarter: %s), but no submitted strategy found for this company. " +
				"Verify that the strategy was submitted for the correct company and quarter.", 
				prospectCompany.getCode(), prospectCompany.getQuater()));
		}
		
		if (submittedStrategiesForOnboardingCompany.size() > 1) {
			throw new BSSBadDataException(String.format(
				"Multiple submitted strategies found for the same company. " +
				"Company code: %s, quarter: %s, number of strategies: %d. " +
				"Only one strategy should be submitted per company per quarter. Please review and ensure only one strategy is marked as submitted.", 
				prospectCompany.getCode(), prospectCompany.getQuater(), submittedStrategiesForOnboardingCompany.size()));
		}
		
		return submittedStrategiesForOnboardingCompany.get(0);
	}
	
	/**
	 * Filters strategies to find only submitted ones.
	 */
	private List<Strategy> findAllSubmittedStrategies(List<Strategy> strategies) {
		return strategies.stream()
			.filter(strategy -> Objects.nonNull(strategy.getSubmitDate()) && strategy.isSubmitted())
			.collect(Collectors.toList());
	}
	
	/**
	 * Filters strategies to find only those belonging to the specified company.
	 */
	private List<Strategy> filterStrategiesForCompany(List<Strategy> allSubmittedStrategies, Long companyId) {
		return allSubmittedStrategies.stream()
			.filter(strategy -> Objects.equals(strategy.getCompanyId(), companyId))
			.collect(Collectors.toList());
	}
	
	/**
	 * This method deletes all the strategies that are not submitted.
     * Also, it deletes the groups that are not in the submitted strategy.
	 */
	private void deleteUnsubmittedStrategiesAndGroups(Company prospectCompany, List<Strategy> prospectStrategies, long submittedStrategyId) {
		Set<Long> strategiesToDelete = prospectStrategies.stream()
			.map(Strategy::getId)
			.filter(strategyId -> !Objects.equals(strategyId, submittedStrategyId))
			.collect(Collectors.toSet());
		
		if (!strategiesToDelete.isEmpty()) {
            List<BenefitGroupStrategy> strategyGroups = strategyGroupService.findBy(prospectCompany.getCode());
			strategyService.deleteExistingStrategies(strategiesToDelete);
            deleteGroupsForUnsubmittedStrategies(strategyGroups, submittedStrategyId);
        }
	}

    /**
	 * Handles K1 group cleanup for non-K1 companies.
	 * For non-K1 companies, K1 groups should be deleted and their employees moved to default groups.
	 */
	private void handleK1GroupCleanupForNonK1Companies(Company psCompany, long submittedStrategyId) {
		if (!psCompany.isK1Company()) {
			updateK1GroupStatusToDeletedFor(submittedStrategyId);
		}
	}

	private void updateK1GroupStatusToDeletedFor(long submittedStrategyId) {

		List<BenefitGroup> benefitGroups = benefitGroupService.getBenefitGroupByStrategy(submittedStrategyId,
				BSSApplicationConstants.STATUS_ACTIVE);

		BenefitGroup k1BenefitGroups = benefitGroups.stream().filter(BenefitGroup::isK1Group).findAny()
				.orElse(null);
		if (k1BenefitGroups != null) {
			moveEmployeesOfK1GroupsToDefaultGroup(submittedStrategyId, benefitGroups, k1BenefitGroups);
			benefitGroupService.updateBenefitGroupStatus(k1BenefitGroups, BSSApplicationConstants.STATUS_DELETED);
            BenefitGroupStrategy k1BenefitGroupStrategy = k1BenefitGroups.getBenefitGroupStrategy().stream()
                    .filter(bgs -> bgs.getStrategyId() == submittedStrategyId)
                    .findFirst().orElse(null);
			strategyGroupService.updateBenefitGroupStrategyStatus(k1BenefitGroupStrategy,
					BSSApplicationConstants.STATUS_DELETED);
		}

	}
	
	private void moveEmployeesOfK1GroupsToDefaultGroup(long submittedStrategyId, List<BenefitGroup> benefitGroups,
			BenefitGroup k1BenefitGroups) {
		Long defaultStrategyGroupId = benefitGroups.stream().flatMap(bg -> bg.getBenefitGroupStrategy().stream())
				.filter(bgs -> bgs.getStrategyId() == submittedStrategyId && bgs.isDefaultGroup()).findFirst()
				.map(BenefitGroupStrategy::getId).orElseThrow(() -> new BSSApplicationException(new BSSApplicationError(
						String.format("No default strategy group found for strategy ID: %d. " +
						"Cannot move K1 employees to default group during client conversion. " +
						"Verify that a default benefit group strategy exists for this strategy.", submittedStrategyId))));

		List<Long> k1StrategyGroupIds = k1BenefitGroups.getBenefitGroupStrategy().stream()
				.filter(bgs -> bgs.getStrategyId() == submittedStrategyId).map(BenefitGroupStrategy::getId)
				.collect(Collectors.toList());
		employeeStrategyGroupService.updateEmployeesToDefaultStrategyGroup(k1StrategyGroupIds, defaultStrategyGroupId);
	}
	
	private void updateBenefitGroupsForPSCompany(long submittedStrategyId, Company psCompany){
        List<BenefitGroup> benefitGroups = benefitGroupService.getBenefitGroupByStrategy(submittedStrategyId,
                List.of(BSSApplicationConstants.STATUS_ACTIVE, BSSApplicationConstants.PENDING_STATUS, BSSApplicationConstants.STATUS_DELETED));
        benefitGroups.stream().filter(Objects::nonNull).forEach(
				benefitGroup -> {
					benefitGroupService.updateGroupWithPSDetails(benefitGroup, psCompany, true);
					benefitGroupService.saveBenefitGroup(benefitGroup);
				});
	}

    private void deleteGroupsForUnsubmittedStrategies(List<BenefitGroupStrategy> strategyGroups, long submittedStrategyId) {
        Set<Long> allGroupIds = strategyGroups.stream().map(BenefitGroupStrategy::getGroupId).collect(Collectors.toSet());
        Set<Long> submittedStrategyGroupIds = strategyGroups.stream()
				.filter(sg -> sg.getStrategyId() == submittedStrategyId)
				.map(BenefitGroupStrategy::getGroupId).collect(Collectors.toSet());
        allGroupIds.removeAll(submittedStrategyGroupIds);
        if(CollectionUtils.isNotEmpty(allGroupIds)) {
            strategyDataDao.deleteGroupCovHeadCountByGroupIds(allGroupIds);
            strategyDataDao.deleteGroupRateByGroupIds(allGroupIds);
            strategyDataDao.deleteGroupByIds(allGroupIds);
        }
    }

}
