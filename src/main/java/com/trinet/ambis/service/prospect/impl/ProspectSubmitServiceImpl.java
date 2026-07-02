package com.trinet.ambis.service.prospect.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.enums.CacheObjectLevelEnum;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.service.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.SubmitPayload;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.outputs.OutputService;
import com.trinet.ambis.service.prospect.ProspectSubmitService;
import com.trinet.ambis.service.prospect.SfdcClientService;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.DateUtils;
import com.trinet.ambis.util.FileUtils;
import com.trinet.security.exception.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProspectSubmitServiceImpl implements ProspectSubmitService {

	private final OutputService outputService;

	private final CompanyService companyService;

	private final ProspectStrategyIntegrationService prospectStrategyIntegrationService;

	private final SubmitStatusService submitStatusService;

	private final StrategyService strategyService;

	private final FileUtils fileUtils;

	private final SfdcClientService sfdcClientService;

	private final CacheService cacheService;

	private static final String BENEFITS_PROPOSAL = "Benefits Proposal";
	private static final String PLAN_APPENDIX = "Plan Appendix";
	private static final String FILE_EXTENSION_PDF = ".pdf";
	private static final String FILE_NAME_SPLITS_SEPARATOR = "_";

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void submit(String companyCode, long strategyId, String exchangeId, HttpServletRequest httpRequest) {
		Path employeeCostAndPlanComparisonReportPath = null;
		Path planAppendixReportPath = null;
		try {
			String emplId = BSSSecurityUtils.getAuthenticatedEmplId(httpRequest);
			BenExchngEnums benExchngEnums = BenExchngEnums.getByExchangeId(exchangeId);
			Company company = companyService.getCompanyDetails(companyCode, false, emplId, benExchngEnums);
			String companyName = FileUtils.removeSpecialCharacters(company.getName());
			String date = DateUtils.getCurrentDate();
			CompletableFuture<Path> employeeCostAndPlanComparisonReportPathFuture = CompletableFuture
					.completedFuture(fileUtils.writeToTempFile(
							outputService.generateEmployeeCostAndPlanComparisonReport(company, strategyId, httpRequest),
							FILE_EXTENSION_PDF, FILE_NAME_SPLITS_SEPARATOR, companyName, BENEFITS_PROPOSAL,
							date));
			CompletableFuture<Path> planAppendixReportPathFuture = CompletableFuture.completedFuture(
					fileUtils.writeToTempFile(outputService.generatePlanAppendixReport(company, strategyId, httpRequest),
							FILE_EXTENSION_PDF, FILE_NAME_SPLITS_SEPARATOR, companyName, PLAN_APPENDIX, date));
			CompletableFuture<String> quoteSummaryFuture = CompletableFuture
					.completedFuture(CommonServiceHelper.objectToJsonString(prospectStrategyIntegrationService
							.getBenefitsSummaryTotalsForStrategy(strategyId, company, benExchngEnums)));
			CompletableFuture.allOf(employeeCostAndPlanComparisonReportPathFuture, planAppendixReportPathFuture,
					quoteSummaryFuture).join();
			employeeCostAndPlanComparisonReportPath = employeeCostAndPlanComparisonReportPathFuture.get();
			planAppendixReportPath = planAppendixReportPathFuture.get();
			String quoteSummary = quoteSummaryFuture.get();
			MultiValueMap<String, Object> bodyMap = buildBodyMap(employeeCostAndPlanComparisonReportPath.toFile(),
					planAppendixReportPath.toFile(), quoteSummary);
			sfdcClientService.sendProposal(bodyMap);
			postSubmit(strategyId, company, emplId, quoteSummary);
		} catch (InterruptedException ie) {
			log.error("InterruptedException occured generating output file for submission: ", ie);
			Thread.currentThread().interrupt();
			throw new BSSApplicationException(ie,
					new BSSApplicationError("InterruptedException occured generating output file for submission"));
		} catch (Exception e) {
			throw new BSSApplicationException(e, new BSSApplicationError(e instanceof NotFoundException ? e.getMessage()
					: "Exception occured generating output file for submission"));
		} finally {
			fileUtils.deleteFiles(employeeCostAndPlanComparisonReportPath, planAppendixReportPath);
		}
	}

	private void postSubmit(long strategyId, Company company, String emplId, String payload) {
		Date currentDateTime = new Date();
		updateSubmitStatus(strategyId, company, emplId, payload, currentDateTime);
		List<Strategy> strategies = strategyService.findBy(company.getCode());
		markExistingStrategiesToUnsubmitted(strategies);
		markCurrentStrategyToSubmitted(strategies, strategyId, currentDateTime);
	}

	private void updateSubmitStatus(long strategyId, Company company, String emplId, String payload, Date currentDateTime) {
		SubmitPayload submitPayload = SubmitPayload.builder().payload(payload).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(strategyId)
				.status(BSSApplicationConstants.SUCCESS).submitError(null).submitPayload(submitPayload).createTime(currentDateTime)
				.userId(emplId).company(company.getCode()).emailSentStatus(Boolean.FALSE)
				.confirmationNumber(CommonServiceHelper.randomAlphanumeric()).realmYrId(company.getRealmPlanYearId())
				.serviceOrder(company.getServiceOrderNumber()).statementUploadStatus(null).updateTime(currentDateTime)
				.sendEmail(false).build();
		submitPayload.setSubmitStatus(submitStatus);
		submitStatusService.createUpdateSubmitStatus(submitStatus);
	}

	private void markExistingStrategiesToUnsubmitted(List<Strategy> strategies) {
		strategies.stream().filter(Strategy::isSubmitted).forEach(strategy -> {
			strategy.setSubmitted(Boolean.FALSE);
			strategy.setSubmitDate(null);
			strategyService.saveStrategy(strategy);
		});
	}

	private void markCurrentStrategyToSubmitted(List<Strategy> strategies, long strategyId, Date currentDateTime) {
		strategies.stream().filter(strategy -> strategy.getId() == strategyId).findFirst().ifPresent(strategy -> {
			strategy.setSubmitted(Boolean.TRUE);
			strategy.setSubmitDate(currentDateTime);
			strategyService.saveStrategy(strategy);
		});
	}

	private MultiValueMap<String, Object> buildBodyMap(File employeeCostAndPlanComparisonReportFile,
			File planAppendixReportFile, String quoteSummary) {
		MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
		bodyMap.add("i_attachment", new FileSystemResource(employeeCostAndPlanComparisonReportFile.getPath()));
		bodyMap.add("i_attachment_apndx", new FileSystemResource(planAppendixReportFile.getPath()));
		bodyMap.add("i_quoteSummary", quoteSummary);
		return bodyMap;
	}
}