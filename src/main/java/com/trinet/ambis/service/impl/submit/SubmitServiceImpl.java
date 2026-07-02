package com.trinet.ambis.service.impl.submit;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.SubmitError;
import com.trinet.ambis.persistence.model.SubmitPayload;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.FlexRateService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.service.email.AleService;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.email.EmailService;
import com.trinet.ambis.service.email.dto.CompanyAndConfNumberDto;
import com.trinet.ambis.service.email.dto.StrategySubmissionFailureDto;
import com.trinet.ambis.service.email.dto.SubmissionEmailDto;
import com.trinet.ambis.service.email.dto.SupportEmailDto;
import com.trinet.ambis.service.impl.DefaultSubmitExecutorService;
import com.trinet.ambis.service.impl.SubmiDataExecutorService;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.SubmissionInfo;
import com.trinet.ambis.service.model.SubmitStatusInfo;
import com.trinet.ambis.service.submit.SubmitService;
import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class SubmitServiceImpl implements SubmitService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubmitServiceImpl.class);

	@Autowired
	private EmailService emailService;

	@Autowired
	private EmailGenService emailGenService;

	@Autowired
	private SubmitStatusService submitStatusService;

	@Autowired
	private StrategyDao strategyDao;

	@Autowired
	private RealmPlanYearDao realmPlanYearDao;

	@Autowired
	private PsDao psDao;

	@Autowired
	private Executor executor;
	
	@Autowired
	private CompanyService companyService;

	@Autowired
	private ProcessStatusService processStatusService;

	@Autowired
	private FlexRateService flexRateService;

	@Autowired
	private AleService aleService;
	
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	@Override
	public CompletableFuture<Void> preSubmit(Company company, SubmissionInfo submissionInfo) {
		return CompletableFuture.runAsync(() -> {
			uploadStatementAndSendConfirmation(company, submissionInfo);
			updateSubmitStatus(submissionInfo);
			if (!submissionInfo.isDefaultSubmit()) {
				sendSupportEmailIfClientEmailFailed(submissionInfo);
			}
			if(company.isAleUpdatedNewClient() && !CompanyServiceHelper.isTNXIExchange(company)) {
				aleService.sendConfirmationEmail(company, submissionInfo);
				companyService.updateAleUpdatedFlag(company, 0);
			}
		}, executor).toCompletableFuture().exceptionally(ex -> {
			Exception exc = new Exception(ex);
			CommonUtils.logExceptions(exc, LOGGER, company.getCode(), company.getEmplId());
			return null;
		});
	}

	@Override
	public void postSubmit(Company company, SubmissionInfo submissionInfo) {
		if (submissionInfo.isResubmit()) {
			if (BSSApplicationConstants.SUCCESS.equals(submissionInfo.getSubmitStatusInfo().getSubmitStatus())) {
				uploadStatementAndSendConfirmation(company, submissionInfo);
			} else {
				submissionInfo.getSubmitStatusInfo().setStatementUploadStatus(BSSApplicationConstants.ERROR);
				submissionInfo.getEmailInfo().setClientEmailSent(false);
			}
		}
		updateSubmitStatus(submissionInfo);
		CompletableFuture.runAsync(() -> {
			if (submissionInfo.isResubmit() && submissionInfo.getEmailInfo().isSendClientEmail()) {
				sendSupportEmailIfClientEmailFailed(submissionInfo);
			}
			if (BSSApplicationConstants.ERROR.equals(submissionInfo.getSubmitStatusInfo().getSubmitStatus())) {
				if (AppRulesAndConfigsUtils.isSnowEmailsEnabled() && !submissionInfo.isDefaultSubmit()) {
					List<CompanyAndConfNumberDto> companyAndConfNumberDtos = new ArrayList<>();
					String confirmationNumber = submissionInfo.getSubmitStatusInfo().getConfirmationNumber();
					companyAndConfNumberDtos.add(CompanyAndConfNumberDto.builder().companyCode(company.getCode())
							.companyName(company.getName()).confirmationNumber(confirmationNumber).build());
					LOGGER.error("sendBssSubmissionFailureEmail invoked for company = {} , confirmation number = {}",
							company.getCode(), confirmationNumber);
					ProcessStatus processStatus = processStatusService.findByConfirmationNumber(confirmationNumber);
					emailGenService.sendBssSubmissionFailureEmail(
							SubmissionEmailDto.builder().companyAndConfNumberDtos(companyAndConfNumberDtos)
									.userId(submissionInfo.getSubmitStatusInfo().getUserId()).sendToBssTeam(false)
									.isSingleClient(true).bssProcessType(processStatus.getProcessName()).build());
				} else {
					SupportEmailDto strategySubmissionFailureDto = StrategySubmissionFailureDto.builder()
							.companyCode(company.getCode())
							.confirmationNumber(submissionInfo.getSubmitStatusInfo().getConfirmationNumber())
							.userId(submissionInfo.getSubmitStatusInfo().getUserId())
							.sendToBSS(false)
							.build();
					emailGenService.createSupportEmail(strategySubmissionFailureDto);
				}
			}
		}, executor).exceptionally(ex -> {
			Exception exc = new Exception(ex);
			CommonUtils.logExceptions(exc, LOGGER, company.getCode(), company.getEmplId());
			return null;
		}).toCompletableFuture();
	}

	@Override
	public void submit(Company company, StrategyData strategyData, String userId, boolean sendClientEmail,
			boolean isResubmit) {
		long startTime = System.currentTimeMillis();

		String payLoad = CommonServiceHelper.objectToJsonString(strategyData);
		String confirmationId = CommonServiceHelper.randomAlphanumeric();
		Strategy strategy = strategyDao.findByIdAndCompanyIdAndStatus(strategyData.getStrategySummary().getId(),
				company.getId(), BSSApplicationConstants.STATUS_ACTIVE);
		strategy.setSubmitted(true);
		strategy.setDefaultSubmit(false);
		strategy.setSubmitDate(strategyData.getStrategySummary().getSubmitDate());
		strategyDao.saveAndFlush(strategy);
		SubmitPayload submitPayload = SubmitPayload.builder().payload(payLoad).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(strategyData.getStrategySummary().getId())
				.status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
				.createTime(strategyData.getStrategySummary().getSubmitDate()).userId(userId).company(company.getCode())
				.emailSentStatus(false).confirmationNumber(confirmationId).realmYrId(company.getRealmPlanYear().getId())
				.serviceOrder(company.getServiceOrderNumber()).statementUploadStatus(null).updateTime(null)
				.sendEmail(sendClientEmail).build();
		submitPayload.setSubmitStatus(submitStatus);
		submitStatusService.createUpdateSubmitStatus(submitStatus);
		if (!isResubmit) {
			SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().queuedSubmit(false)
					.resubmit(isResubmit).withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo()
					.buildPreSubmit();
			preSubmit(company, submissionInfo);
		}
		SubmiDataExecutorService executeService = new SubmiDataExecutorService(company, strategyData, userId,
				sendClientEmail, confirmationId, isResubmit);
		try {
			executeService.start();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error("Exception occured in method: submit({}). Reason: {}", company.getCode(), e.getMessage());
		} catch (Exception ex) {
			throw new BSSApplicationException(ex,
					new BSSApplicationError(BSSErrorResponseCodes.BSS_STRATEGY_COMPANY_SUBMIT_FAIL,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, SubmitServiceImpl.class.toString(), "SUBMIT FAILED",
							null, null));
		}
		long endTime = System.currentTimeMillis();
		LOGGER.info("LOADING DATA TO PS TOOK : {} ms", (endTime - startTime));
	}

	@Override
	public void defaultSubmit(String companyCode, String quarter, String userId) {
		List<String> companies = new ArrayList<>();
		boolean isSingleClientSubmit = false;
		long realmPlanYearId = 0L;
		Company company = null;
		if (Constants.DEFAULT_COMPANY_CODE.equalsIgnoreCase(companyCode)) {
			Date payrollCutOffDate = new DateTime().minusDays(14).toDate();
			RealmPlanYear rpy = realmPlanYearDao.getMaxRealmPlanYearByQuarter(quarter);
			realmPlanYearId = rpy.getId();
			companies = psDao.getUnsubmittedClients(quarter, rpy.getId(), payrollCutOffDate, BSSQueryConstants.TERM_STATUS_ACTIVE);
		} else {
			isSingleClientSubmit = true;
			companies.add(companyCode);
			company = companyService.getCompanyDetails(companyCode, false, userId, null);
			if (!company.isActiveServiceOrder()) {
				realmPlanYearId = company.getRealmPlanYearId();
			}
		}
		DefaultSubmitExecutorService executeService = new DefaultSubmitExecutorService(companies, isSingleClientSubmit,
				userId, realmPlanYearId);
		try {
			executeService.start();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error("Exception occured in method: defaultSubmit({}, {}, {}). Reason: {}", companyCode, quarter,
					userId, e.getMessage());
		} catch (Exception ex) {
			throw new BSSApplicationException(ex,
					new BSSApplicationError(BSSErrorResponseCodes.BSS_STRATEGY_DEFAULT_SUBMIT_FAIL,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, SubmitServiceImpl.class.toString(),
							"DEFAULT SUBMIT FAILED", null, null));
		}
	}

	@Override
	public void defaultSubmitTermedClients(String companyCode, String quarter) {
		if (!Constants.DEFAULT_COMPANY_CODE.equalsIgnoreCase(companyCode)) {
			return;
		}
		RealmPlanYear rpy = realmPlanYearDao.getMaxRealmPlanYearByQuarter(quarter);
		RiskTypeEnum riskType = RulesAndConfigsUtils.getRenewalRiskType(rpy.getId());
		if (riskType != RiskTypeEnum.DIFFERENTIALS) {
			LOGGER.info("defaultSubmitTermedClients: skipping, riskType={} is not DIFFERENTIALS for quarter={}", riskType, quarter);
			return;
		}
		Set<String> qualifiedTermedCompanies = discoverQualifiedTermedCompanies(quarter, rpy);
		if (qualifiedTermedCompanies.isEmpty()) {
			LOGGER.info("defaultSubmitTermedClients: no termed companies found for quarter={}", quarter);
			return;
		}
		LOGGER.info("defaultSubmitTermedClients: discovered {} termed companies for quarter={}", 
				qualifiedTermedCompanies.size(), quarter);
		submitTermedCompaniesSequentially(quarter, qualifiedTermedCompanies);
	}

	private Set<String> discoverQualifiedTermedCompanies(String quarter, RealmPlanYear rpy) {
		String planYearStartDate = new SimpleDateFormat(BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD)
				.format(rpy.getPlanYearStart());
		Set<String> flexRateCompanySet = toOrderedSet(flexRateService.getClientsWithRates(quarter, planYearStartDate));
		Date payrollCutOffDate = new DateTime().minusDays(14).toDate();
		Set<String> psTermedCompanySet = toOrderedSet(
				psDao.getUnsubmittedClients(quarter, rpy.getId(), payrollCutOffDate, BSSQueryConstants.TERM_STATUS_TERMED));
		psTermedCompanySet.retainAll(flexRateCompanySet);
		return psTermedCompanySet;
	}

	private void submitTermedCompaniesSequentially(String quarter, Set<String> qualifiedTermedCompanies) {
		String termedUserId = ProcessStatusEnum.TERMED_CLIENT_DEFAULT_SUBMIT.getProcessName();
		int submittedCount = 0;
		int failedCount = 0;
		for (String termedCompanyCode : qualifiedTermedCompanies) {
			try {
				defaultSubmit(termedCompanyCode, quarter, termedUserId);
				submittedCount++;
			} catch (Exception ex) {
				failedCount++;
				LOGGER.error("Termed default submit failed for companyCode={} quarter={}", termedCompanyCode, quarter, ex);
			}
		}
		LOGGER.info("submitTermedCompaniesSequentially: quarter={}, submitted={}, failed={}", 
				quarter, submittedCount, failedCount);
	}

	private Set<String> toOrderedSet(List<String> companies) {
		return new LinkedHashSet<>(companies == null ? Collections.emptyList() : companies);
	}

	private void uploadStatementAndSendConfirmation(Company company, SubmissionInfo submissionInfo) {
		try {
			emailService.uploadStatementAndSendConfirmation(company, submissionInfo);
		} catch (Exception e) {
			CommonUtils.logExceptions(e, LOGGER, company.getCode(), submissionInfo.getSubmitStatusInfo().getUserId());
		}
	}

	private void updateSubmitStatus(SubmissionInfo submissionInfo) {
		SubmitStatusInfo submitStatusInfo = submissionInfo.getSubmitStatusInfo();

		if (submissionInfo.isResubmit()) {
			submitStatusService.update(submissionInfo.getSubmitStatusInfo().getStatementUploadStatus(),
					submissionInfo.getEmailInfo().isClientEmailSent(),
					submissionInfo.getSubmitStatusInfo().getConfirmationNumber(), submissionInfo.getCompanyCode());
		} else if (submissionInfo.isPreSubmit()) {
			submitStatusService.update(submissionInfo.getSubmitStatusInfo().getStatementUploadStatus(),
					submissionInfo.getEmailInfo().isClientEmailSent(), submitStatusInfo.getConfirmationNumber(),
					submissionInfo.getCompanyCode());
		}
		if (!submissionInfo.isPreSubmit()) {
			setErrorMessage(submissionInfo);
			String status = submitStatusInfo.getSubmitException() == null ? BSSApplicationConstants.SUCCESS
					: BSSApplicationConstants.ERROR;
			submitStatusService.update(status, new Date(),
					submitStatusInfo.getConfirmationNumber(), submissionInfo.getCompanyCode());
			if (status.equals(BSSApplicationConstants.ERROR)) {
				SubmitStatus submitStatus = submitStatusService.findByConfirmationNumber(
						submissionInfo.getCompanyCode(), submitStatusInfo.getConfirmationNumber());
				SubmitError submitError = SubmitError.builder().errorMsg(submitStatusInfo.getErrorMessage())
						.stackTrace(ExceptionUtils.getStackTrace(submitStatusInfo.getSubmitException()))
						.submitStatus(submitStatus).submitStatusId(submitStatus.getId()).build();
				submitStatus.setSubmitError(submitError);
				submitStatusService.createUpdateSubmitStatus(submitStatus);
			}
		}
	}

	private void sendSupportEmailIfClientEmailFailed(SubmissionInfo submissionInfo) {
		if (!submissionInfo.getEmailInfo().isClientEmailSent()) {
			emailGenService.generateSubmissionIssueReport(submissionInfo.getCompanyCode(),
					submissionInfo.getSubmitStatusInfo().getUserId());
		}
	}

	private void setErrorMessage(SubmissionInfo submissionInfo) {
		try {
			String errorMessage = StringUtils.EMPTY;
			Exception exception = submissionInfo.getSubmitStatusInfo().getSubmitException();
			if (exception != null) {
				if (StringUtils.isNotEmpty(exception.getMessage())) {
					errorMessage = exception.getMessage();
				} else if (ArrayUtils.isNotEmpty(ExceptionUtils.getRootCauseStackTrace(exception))
						&& ExceptionUtils.getRootCauseStackTrace(exception)[0] != null) {
					errorMessage = ExceptionUtils.getRootCauseStackTrace(exception)[0];
				} else {
					CommonUtils.logExceptions(exception, LOGGER, submissionInfo.getCompanyCode(),
							submissionInfo.getSubmitStatusInfo().getUserId());
					errorMessage = "Encountered NullPointer Exception";
				}
				errorMessage = errorMessage.substring(0, errorMessage.length() > 1024 ? 1024 : errorMessage.length());
				LOGGER.info("SUBMIT ERROR MESSAGE {}", errorMessage);
			}
			submissionInfo.getSubmitStatusInfo().setErrorMessage(errorMessage);
		} catch (Exception e) {
			submissionInfo.getSubmitStatusInfo().setErrorMessage("Error occured while generating error message.");
			CommonUtils.logExceptions(e, LOGGER, submissionInfo.getCompanyCode(),
					submissionInfo.getSubmitStatusInfo().getUserId());
		}
	}
	
}
