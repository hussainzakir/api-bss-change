package com.trinet.ambis.service.impl.submit;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.SchedMidYearFundingDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.SchedMidYearFunding;
import com.trinet.ambis.persistence.model.SubmitPayload;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.QueuedSubmitService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.service.submit.ResubmitService;
import com.trinet.ambis.service.submit.SubmitService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.CommonUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ResubmitServiceImpl implements ResubmitService {

	@Autowired
	private StrategyService strategyService;

	@Autowired
	private SubmitService submitService;

	@Autowired
	private QueuedSubmitService queuedSubmitService;

	@Autowired
	private SubmitStatusService submitStatusService;

	@Autowired
	private SchedMidYearFundingDao schedMidYearFundingDao;

	@Autowired
	private CompanyService companyService;

	@Override
	public StrategyData resubmit(String companyCode, boolean sendClientEmail) {
		String userId = BSSSecurityUtils.getAuthenticatedPersonId();
		Company company = companyService.getCompanyDetails(companyCode, false, userId, null);
		
		if (company.isProspectCompany()) {
			throw new BSSApplicationException("Can't perform resubmission for Prospect company");
		}

		SubmitStatus submitStatus = submitStatusService.findLatestSubmitStatusBy(company.getCode());
		// If last submitted strategy is for current plan year and the future plan year
		// is open or in transition period
		if (Long.compare(submitStatus.getRealmYrId(), company.getRealmPlanYearId()) != 0) {
			company = companyService.getCompanyDetails(companyCode, true, userId, null);
		}
		StrategyData strategyDataToSubmit = null;
		if (company.isBandCodeUpdated() || (company.isAcaLargeEmplrStatusUpdated() && !company.isRenewalCompany())) {
			strategyDataToSubmit = findSubmittedStrategy(company);
		} else {
			SubmitPayload payLoad = submitStatus.getSubmitPayload();
			strategyDataToSubmit = CommonServiceHelper.jsonToObject(payLoad.getPayload(), StrategyData.class);
		}

		if (strategyDataToSubmit != null && strategyDataToSubmit.getStrategySummary().isSubmitted()) {
			executeSubmit(company, userId, sendClientEmail, ProcessStatusEnum.RESUBMIT_PROCESS.getProcessName(),
					strategyDataToSubmit);
		}

		return strategyDataToSubmit;
	}

	@Override
	@Async
	public void bandcodeResubmit(String companyCode, Date effDt) {
		try {
			String userId = BSSApplicationConstants.BANDCHANGE_USER_ID;
			Company company = companyService.getCompanyDetails(companyCode, false, userId, null);

			if (isRenewalCompanyForFuturePlanYear(company)
					&& !isPlanStartDtSameAsBandChangeEffDtForRenewalComp(company, effDt)) {
				company = companyService.getCompanyDetails(companyCode, true, userId, null);
			}

			if (!isPlanStartDtSameAsBandChangeEffDtForRenewalComp(company, effDt)
					&& !isBenStartDtSameAsBandChangeEffDtForNewComp(company, effDt)) {
				log.error(
						"Can't perform bandcode resubmission for company: {} because the EffDt: {} is not matching Plan year start dt or company benefit start date: {}",
						company.getCode(), effDt, company.getBenefitStartDate());
				return;
			}

			if (!company.isBandCodeUpdated()) {
				log.error(
						"Bandcode resubmit is not performed for company: {} EffDt : {} because band code is not updated.",
						company.getCode(), effDt);
				return;
			}

			if (isMidYearFundingExistsAfterBandChangeEffDt(company, effDt)) {
				log.error("Can't perform bandcode resubmission because mid year exist for Company: {} after EffDt: {}",
						company.getCode(), effDt);
				return;
			}

			StrategyData strategyDataToSubmit = findSubmittedStrategy(company);
			
			if (strategyDataToSubmit == null) {
				log.error(
						"Can't perform bandcode resubmission because no strategy has been submitted for Company: {} for EffDt: {}",
						company.getCode(), effDt);
				return;
			}

			strategyDataToSubmit.getStrategySummary().setSubmitDate(new Date());
			executeSubmit(company, userId, true, ProcessStatusEnum.BAND_CODE_RESUBMIT_PROCESS.getProcessName(),
					strategyDataToSubmit);
		} catch (Exception e) {
			log.error("Exception occured in bandcodeResubmit: {}", e);
			throw e;
		}
	}

	private StrategyData findSubmittedStrategy(Company company) {
		StrategyData strategyDataToSubmit = null;
		List<StrategyData> availableStrategies = strategyService.getStrategies(company, false, null);
		for (StrategyData strategyData : availableStrategies) {
			if (strategyData.getStrategySummary().isSubmitted()) {
				strategyDataToSubmit = strategyData;
			}
		}
		return strategyDataToSubmit;
	}

	private void executeSubmit(Company company, String userId, boolean sendClientEmail, String processName,
			StrategyData strategyDataToSubmit) {
		if (AppRulesAndConfigsUtils.isSubmitQueuingEnabled()) {
			queuedSubmitService.createSubmitProcess(company, strategyDataToSubmit, processName, sendClientEmail);
		} else {
			submitService.submit(company, strategyDataToSubmit, userId, sendClientEmail, true);
		}
	}

	private boolean isRenewalCompanyForFuturePlanYear(Company company) {
		return company.isRenewalCompany() && (company.isTransitionPeriod() || company.isRenewalOpen());
	}

	private boolean isPlanStartDtSameAsBandChangeEffDtForRenewalComp(Company company, Date effDt) {
		Date benStartDt = CommonUtils.formatStringToDate(company.getBenefitStartDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
		return company.isRenewalCompany() && company.getRealmPlanYear().getPlanYearStart().compareTo(benStartDt) >= 0
				&& company.getRealmPlanYear().getPlanYearStart().compareTo(effDt) == 0;
	}

	private boolean isBenStartDtSameAsBandChangeEffDtForNewComp(Company company, Date effDt) {
		Date benStartDt = CommonUtils.formatStringToDate(company.getBenefitStartDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
		return benStartDt.compareTo(company.getRealmPlanYear().getPlanYearStart()) >= 0
				&& effDt.compareTo(benStartDt) == 0;

	}

	private boolean isMidYearFundingExistsAfterBandChangeEffDt(Company company, Date effDt) {
		boolean result = false;
		List<SchedMidYearFunding> smfs = schedMidYearFundingDao.findByCompanyId(company.getId());
		for (SchedMidYearFunding smf : smfs) {
			if (smf.getMidYearFundingEffDate().compareTo(effDt) > 0) {
				result = true;
				break;
			}
		}
		return result;
	}

}
