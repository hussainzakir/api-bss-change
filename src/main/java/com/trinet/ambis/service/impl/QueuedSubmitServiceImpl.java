package com.trinet.ambis.service.impl;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.SubmitError;
import com.trinet.ambis.persistence.model.SubmitPayload;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.PsSubmitDataService;
import com.trinet.ambis.service.QueuedSubmitService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.email.dto.CompanyAndConfNumberDto;
import com.trinet.ambis.service.email.dto.StrategySubmissionFailureDto;
import com.trinet.ambis.service.email.dto.SubmissionEmailDto;
import com.trinet.ambis.service.email.dto.SupportEmailDto;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.SubmissionInfo;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.service.submit.SubmitService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.CommonUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class QueuedSubmitServiceImpl implements QueuedSubmitService {
	private static final Logger LOGGER = LoggerFactory.getLogger(QueuedSubmitServiceImpl.class);

	@Autowired
	private CompanyService companyService;

	@Autowired
	private StrategyDao strategyDao;

	@Autowired
	private SubmitStatusService submitStatusService;

	@Autowired
	private ProcessStatusService processStatusService;

	@Autowired
	private EmailGenService emailGenService;

	@Autowired
	private SubmitService submitService;

	@Autowired
	private StrategyService strategyService;
	
	@Autowired
	private Executor executor;

	@Override
	@Transactional
	public void createSubmitProcess(final Company company, final StrategyData dto, final String processName,
			final boolean sendEmail) {
		String userId = company.getEmplId();
		String payLoad = CommonServiceHelper.objectToJsonString(dto);
		String confirmationId = CommonServiceHelper.randomAlphanumeric();
		Strategy strategy = strategyDao.findByIdAndCompanyIdAndStatus(dto.getStrategySummary().getId(), company.getId(),
				BSSApplicationConstants.STATUS_ACTIVE);
		// SubmittedBy is only populated when strategy is default submitted.
		strategy.setDefaultSubmit(false);
		strategy.setSubmitDate(dto.getStrategySummary().getSubmitDate());
		strategyDao.saveAndFlush(strategy);

		SubmitPayload submitPayload = SubmitPayload.builder().payload(payLoad).build();
		SubmitStatus submitStatus = SubmitStatus.builder()
				.strategyId(dto.getStrategySummary().getId())
				.status(BSSApplicationConstants.UNPROCESSED).submitPayload(submitPayload)
				.createTime(dto.getStrategySummary().getSubmitDate()).userId(userId).company(company.getCode()).emailSentStatus(false)
				.confirmationNumber(confirmationId).realmYrId(company.getRealmPlanYear().getId())
				.serviceOrder(company.getServiceOrderNumber()).statementUploadStatus(null)
				.updateTime(null).sendEmail(sendEmail).build();
		submitPayload.setSubmitStatus(submitStatus);
		submitStatusService.createUpdateSubmitStatus(submitStatus);
		processStatusService.createSubmitProcess(processName, confirmationId, userId);
		if (!CommonServiceHelper.isResubmit(processName)) {
			SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().queuedSubmit(true)
					.withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo().buildPreSubmit();
			submitService.preSubmit(company, submissionInfo);
		}
	}

	@Override
	@Transactional
	public void createAsyncDefaultSubmitProcess(final String companyCode, String userId){
		CompletableFuture.runAsync(() -> createDefaultSubmitProcess(companyCode, userId), executor)
				.exceptionally(ex -> {
					Exception exc = (ex instanceof Exception) ? (Exception) ex : new Exception(ex);
					CommonUtils.logExceptions(exc, LOGGER, companyCode, userId);

					SupportEmailDto strategySubmissionFailureDto = StrategySubmissionFailureDto.builder()
							.companyCode(companyCode)
							.confirmationNumber(BSSApplicationConstants.NOT_AVAILABLE)
							.userId(userId)
							.sendToBSS(true)
							.build();
					emailGenService.createSupportEmail(strategySubmissionFailureDto);
					return null;
				}).toCompletableFuture();
	}
	
	private void createDefaultSubmitProcess(final String companyCode, String userId) {
		String processName = ProcessStatusEnum.TERMED_CLIENT_DEFAULT_SUBMIT.getProcessName();
		processStatusService.createSubmitProcess(processName, companyCode, userId);
	}

	@Override
	public void startSchedulerSubmitProcess() {
		ProcessStatus processStatus = processStatusService.findNextToProcessSubmit();
		startSubmitProcess(processStatus);
	}

	// ******* DO NOT USE @Transactional ON THIS METHOD. *******
	@Override
	@Async
	public void startAsyncManualSubmitProcess() {
		List<ProcessStatus> processStatuses = processStatusService.findPendingSubmitProcesses();
		for (ProcessStatus processStatus : processStatuses) {
			startSubmitProcess(processStatus);
		}
	}

	private Future<String> startAsyncSubmitProcess(ProcessStatus processStatus) {
		if (processStatus.getProcessName().equals(ProcessStatusEnum.TERMED_CLIENT_DEFAULT_SUBMIT.getProcessName())) {
			return CompletableFuture.supplyAsync(() -> startTermDefaultPsSubmitDataProcess(processStatus).getStatus(),
					executor);
		} else {
			return CompletableFuture.supplyAsync(() -> startPsSubmitDataProcess(processStatus).getStatus(), executor);
		}
	}

	private void startSubmitProcess(ProcessStatus processStatus) {
		if (processStatus != null) {
			Exception exc = null;
			String errorMsg = null;
			Future<String> submitProcessFuture = null;
			updateProcessStatus(processStatus, BSSApplicationConstants.PROCESS_STATUS_INPROGRESS);
			try {
				submitProcessFuture = startAsyncSubmitProcess(processStatus);
				submitProcessFuture.get(30, TimeUnit.MINUTES);
			} catch (TimeoutException e) {
				exc = e;
				errorMsg = "Submit timed out.";

				if (!submitProcessFuture.isCancelled() && !submitProcessFuture.isDone()) {
					submitProcessFuture.cancel(true);
				}
			} catch (BSSApplicationException e) {
				exc = e;
				errorMsg = e.getMessage();
			} catch (InterruptedException | ExecutionException e) {
				exc = e;
				errorMsg = "Unknown exception occured.";
				Thread.currentThread().interrupt();
			} finally {
				Set<String> statuses = new HashSet<>(
						Arrays.asList(BSSApplicationConstants.UNPROCESSED, BSSApplicationConstants.PROCESSING,
								BSSApplicationConstants.ERROR, BSSApplicationConstants.SUCCESS));
				SubmitStatus submitStatus = submitStatusService
						.findByConfirmationNumberAndStatus(processStatus.getProcessIdentiferValue(), statuses);
				if (BSSApplicationConstants.SUCCESS.equals(submitStatus.getStatus())) {
					updateProcessStatus(processStatus, BSSApplicationConstants.PROCESS_STATUS_PROCESSED);
				} else if (exc != null) {
					// This if block is for submit failure handled out side of
					// PSSubmitDataDao#submitData()
					errorMsg = generateErrorMessage(exc, errorMsg);
					updateProcessStatus(processStatus, BSSApplicationConstants.PROCESS_STATUS_FAILED);
					updateSubmitStatus(submitStatus, BSSApplicationConstants.ERROR, errorMsg, exc);
					if (AppRulesAndConfigsUtils.isSnowEmailsEnabled()) {
						List<CompanyAndConfNumberDto> companyAndConfNumberDtos = new ArrayList<>();
						companyAndConfNumberDtos
								.add(CompanyAndConfNumberDto.builder().companyCode(submitStatus.getCompany())
										.companyName(companyService.getCompanyName(submitStatus.getCompany()))
										.confirmationNumber(processStatus.getProcessIdentiferValue()).build());
						LOGGER.error(
								"sendBssSubmissionFailureEmail invoked for company = {} , confirmation number = {}",
								submitStatus.getCompany(), processStatus.getProcessIdentiferValue());
						emailGenService.sendBssSubmissionFailureEmail(
								SubmissionEmailDto.builder().companyAndConfNumberDtos(companyAndConfNumberDtos)
										.userId(processStatus.getUserId()).sendToBssTeam(true).isSingleClient(true)
										.bssProcessType(processStatus.getProcessName()).build());
					} else {
						SupportEmailDto strategySubmissionFailureDto = StrategySubmissionFailureDto.builder()
								.companyCode(submitStatus.getCompany())
								.confirmationNumber(processStatus.getProcessIdentiferValue())
								.userId(processStatus.getUserId())
								.sendToBSS(true)
								.build();
						emailGenService.createSupportEmail(strategySubmissionFailureDto);
					}
					CommonUtils.logExceptions(exc, LOGGER, submitStatus.getCompany(), processStatus.getUserId());
				} else {
					updateProcessStatus(processStatus, BSSApplicationConstants.PROCESS_STATUS_FAILED);
				}
			}
		}
	}

	private SubmitStatus startPsSubmitDataProcess(ProcessStatus processStatus) {
		Company company = null;
		StrategyData strategyData = null;
		SubmitStatus submitStatus = null;
		boolean resubmitFlag = false;
		String confirmationNumber = null;
		String userId = processStatus.getUserId();

		confirmationNumber = processStatus.getProcessIdentiferValue();
		if (CommonServiceHelper.isResubmit(processStatus.getProcessName())) {
			resubmitFlag = true;
		}
		Set<String> statuses = new HashSet<>(
				Arrays.asList(BSSApplicationConstants.UNPROCESSED, BSSApplicationConstants.PROCESSING));
		submitStatus = submitStatusService.findByConfirmationNumberAndStatus(confirmationNumber, statuses);
		if (submitStatus == null) {
			throw new BSSApplicationException(
					new BSSApplicationError(BSSErrorResponseCodes.BSS_SUBMIT_FAILURE_EXCEPTION,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, this.getClass().getName(),
							"No submitStatus record found for confirmation # " + confirmationNumber, null, null));
		}
		if (BSSApplicationConstants.SUCCESS.equals(submitStatus.getStatus())
				|| BSSApplicationConstants.ERROR.equals(submitStatus.getStatus())) {
			return submitStatus;
		}
		submitStatusService.update(BSSApplicationConstants.PROCESSING, submitStatus.getConfirmationNumber(),
				submitStatus.getCompany());

		try {
			String payLoad = submitStatus.getSubmitPayload().getPayload();
			strategyData = CommonServiceHelper.jsonToObject(payLoad, StrategyData.class);
			company = companyService.getCompanyDetails(submitStatus.getCompany(), false, userId, null);
			if (Long.compare(company.getRealmPlanYearId(), submitStatus.getRealmYrId()) != 0) {
				company = companyService.getCompanyDetails(submitStatus.getCompany(), true, userId, null);
			}
		} catch (Exception e) {
			throw new BSSApplicationException(new BSSApplicationError(
					BSSErrorResponseCodes.BSS_SUBMIT_FAILURE_EXCEPTION, BSSHttpStatusConstants.INTERNAL_SERVER_ERROR,
					this.getClass().getName(), "Error occured while parsing the strategy data.", null, null));

		}
		PsSubmitDataService submitDataService = new PsSubmitDataServiceImpl();
		submitDataService.submitData(company, strategyData, userId, submitStatus.getSendEmail(), confirmationNumber,
				resubmitFlag);
		return submitStatus;
	}
	
	private SubmitStatus startTermDefaultPsSubmitDataProcess(ProcessStatus processStatus) {

		Company company = companyService.getCompanyDetails(processStatus.getProcessData(), false, null, null);
		if (!company.isRenewalCompany()) {
			LOGGER.error("Can't run default submit because the company is not recognised as renewal company :{}",
					company.getCode());
			return null;
		}
		long defaultStrategyId = 0;
		List<Strategy> strategies = strategyDao.findByCompanyIdAndStatus(company.getId(),
				BSSApplicationConstants.STATUS_ACTIVE);
		if (strategies.isEmpty()) {
			// creating future strategies.
			strategyService.createFutureStrategies(company, true, false);
			strategies = strategyDao.findByCompanyIdAndStatus(company.getId(), BSSApplicationConstants.STATUS_ACTIVE);
		}
		for (Strategy strategy : strategies) {
			if (BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED.equals(strategy.getType())) {
				defaultStrategyId = strategy.getId();
				strategyDao.updateToSubmitted(defaultStrategyId, true, new Date());
				break;
			}
		}
		StrategyData strategyData = strategyService.getStrategyById(company, defaultStrategyId, true);

		String payLoad = CommonServiceHelper.objectToJsonString(strategyData);
		String confirmationId = processStatus.getProcessIdentiferValue();
		SubmitPayload submitPayload = SubmitPayload.builder().payload(payLoad).build();

		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(strategyData.getStrategySummary().getId())
				.status(BSSApplicationConstants.UNPROCESSED).submitPayload(submitPayload).createTime(new Date())
				.userId(processStatus.getUserId()).company(company.getCode()).emailSentStatus(false)
				.confirmationNumber(confirmationId).realmYrId(company.getRealmPlanYear().getId())
				.serviceOrder(company.getServiceOrderNumber()).statementUploadStatus(null).updateTime(null)
				.sendEmail(false).build();

		submitPayload.setSubmitStatus(submitStatus);

		submitStatusService.createUpdateSubmitStatus(submitStatus);

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().queuedSubmit(true)
				.defaultSubmit(true).withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo()
				.buildPreSubmit();
		submitService.preSubmit(company, submissionInfo);

		PsSubmitDataService submitDataService = new PsSubmitDataServiceImpl();
		submitDataService.submitData(company, strategyData, processStatus.getUserId(), submitStatus.getSendEmail(),
				confirmationId, false);
		return submitStatus;
	}

	private String generateErrorMessage(Exception exc, String errorMsg) {
		String finalErrorMsg = null;
		if (errorMsg != null) {
			finalErrorMsg = errorMsg;
		} else if (StringUtils.isNotEmpty(exc.getMessage())) {
			finalErrorMsg = exc.getMessage();
		} else if (ArrayUtils.isNotEmpty(ExceptionUtils.getRootCauseStackTrace(exc))
				&& ExceptionUtils.getRootCauseStackTrace(exc)[0] != null) {
			finalErrorMsg = ExceptionUtils.getRootCauseStackTrace(exc)[0];
		} else {
			finalErrorMsg = "Unknow exception occured.";
		}
		finalErrorMsg = finalErrorMsg.substring(0, finalErrorMsg.length() > 1024 ? 1024 : finalErrorMsg.length());
		return finalErrorMsg;
	}

	private void updateProcessStatus(ProcessStatus processStatus, String status) {
		processStatus.setProcessStatus(status);
		processStatusService.updateProcessStatus(processStatus);
	}

	private void updateSubmitStatus(SubmitStatus submitStatus, String status, String errorMsg, Exception exc) {
		submitStatus.setStatus(status);
		SubmitError submitError = SubmitError.builder().errorMsg(errorMsg).stackTrace(ExceptionUtils.getStackTrace(exc))
				.submitStatus(submitStatus).build();
		submitStatus.setSubmitError(submitError);
		submitStatus.setUpdateTime(new Date());
		submitStatusService.createUpdateSubmitStatus(submitStatus);
	}
	

}
