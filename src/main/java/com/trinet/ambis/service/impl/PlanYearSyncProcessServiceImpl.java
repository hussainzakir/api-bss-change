package com.trinet.ambis.service.impl;

import java.time.LocalDate;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CacheObjectLevelEnum;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.persistence.dao.hrp.PlanYearChangeAuditDao;
import com.trinet.ambis.persistence.dao.ps.PsSubmitDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanYearChangeAudit;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.PlanYearRequest;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.PlanYearSyncProcessService;
import com.trinet.ambis.service.dto.ProcessInfoDto;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.JsonConverterUtils;
import com.trinet.ambis.util.Utils;

@Service
public class PlanYearSyncProcessServiceImpl implements PlanYearSyncProcessService {
	private static final Logger logger = LoggerFactory.getLogger(PlanYearSyncProcessServiceImpl.class);
	@Autowired
	private CompanyService companyService;

	@Autowired
	private RealmPlanYearService realmPlanYearService;

	@Autowired
	private ProcessStatusService processStatusService;

	@Autowired
	private PlanYearChangeAuditDao planYearChangeAuditDao;
	
	@Autowired
	private PsSubmitDataDao psSubmitDataDao;
	
	@Autowired
	private CacheService cacheService;

	@Override
	@Transactional
	public void updatePlanYearSyncProcessStatus(final PlanYearRequest planYearRequest) {
		logger.info("updatePlanYearSyncProcessStatus() PlanYearSyncProcessServiceImpl: {}",
				planYearRequest.getCompanyCode());
		String companyCode = planYearRequest.getCompanyCode();
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), null);

		savePlanYearChangeAuditDetails(planYearRequest, company);
		updatePSBenefitStartDateAndQuarter(planYearRequest, company);
		
		cacheService.invalidateCache(CacheObjectTypeEnum.BASIC_COMPANY_DETAILS.getObjectType(),
				CacheObjectLevelEnum.COMPANY.getObjectLevel(), companyCode);
		
		boolean isPlanYearChanged = isPlanYearChanged(company, planYearRequest);

		if (isPlanYearChanged) {
			ProcessInfoDto processInfoDto = ProcessInfoDto.builder()
					.processName(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName())
					.oldRealmPlanYear(company.getRealmPlanYearId()).oldCompanyId(company.getId()).build();
			processStatusService.createStrategySyncProcess(companyCode,
					JsonConverterUtils.convertObjectToJson(processInfoDto),
					ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName(),
					ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getIdentifierName());
			logger.info("end of updatePlanYearSyncProcessStatus() PlanYearSyncProcessServiceImpl: {}",
					processInfoDto.getProcessName());
		}
	}

	private void savePlanYearChangeAuditDetails(PlanYearRequest planYearRequest, Company company) {
		logger.info("savePlanYearChangeAuditDetails() PlanYearSyncProcessServiceImpl CommonOwnerCompany: {}",
				planYearRequest.getCommonOwnerCompany());
		LocalDate oldBenefitStartDate = Utils.convertStringToLocalDate(
				Utils.convertDateFormat(company.getBenefitStartDate(), BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY,
						BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY).get(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
		LocalDate newBenefitStartDate = Utils.convertStringToLocalDate(
				Utils.convertDateFormat(planYearRequest.getBenefitStartDate(),
						BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD,
						BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY).get(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);

		PlanYearChangeAudit audit = new PlanYearChangeAudit();
		audit.setChangedBy(BSSSecurityUtils.getAuthenticatedPersonId());
		audit.setCompanyCode(planYearRequest.getCompanyCode());
		audit.setCommonOwnerCompany(planYearRequest.getCommonOwnerCompany());
		audit.setNewBenefitStartDate(newBenefitStartDate);
		audit.setOldBenefitStartDate(oldBenefitStartDate);
		audit.setNewQuarter(planYearRequest.getQuarter());
		audit.setServiceOrderNum(planYearRequest.getServiceOrderNumber());
		audit.setQuarterException(planYearRequest.getQuarterException() == Boolean.TRUE ? Constants.YES : Constants.NO);
		audit.setOldQuarter(company.getQuater());
		audit.setChangeTimestamp(LocalDate.now());
		planYearChangeAuditDao.save(audit);
	}

	private void updatePSBenefitStartDateAndQuarter(final PlanYearRequest planYearRequest, Company company) {
		String newPlanStartDate = Utils.convertDateFormat(planYearRequest.getBenefitStartDate(),
				BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD, BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY).get();
		company.setQuater(planYearRequest.getQuarter());
		company.setBenefitsQuarterException(planYearRequest.getQuarterException());
		company.setCommonOwnerCompanyCode(planYearRequest.getCommonOwnerCompany());
		psSubmitDataDao.updateBenefitStartDateAndQuarterForPlanYearSync(company, newPlanStartDate);
	}
	
	private boolean isPlanYearChanged(Company company, PlanYearRequest planYearRequest) {
		Date benefitStartDate = Utils.convertStringToDate(Utils.convertDateFormat(planYearRequest.getBenefitStartDate(),
				BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD, BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY).get(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
		RealmPlanYear newRealmPlanYear = realmPlanYearService.findRealmPlanYearBy(benefitStartDate,
				planYearRequest.getQuarter());

		return newRealmPlanYear != null && newRealmPlanYear.getId() != company.getRealmPlanYear().getId();
	}
}
