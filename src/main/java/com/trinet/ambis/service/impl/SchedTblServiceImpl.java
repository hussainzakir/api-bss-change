package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trinet.ambis.service.BrokerNotificationService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.helper.ExceptionServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeStrategyGroupTransactionDao;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.dao.hrp.SchedMidYearFundingDao;
import com.trinet.ambis.persistence.dao.hrp.SchedTblDao;
import com.trinet.ambis.persistence.dao.hrp.SchedTblDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.SchedMidYearFunding;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.service.PersonService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.SchedTblService;
import com.trinet.ambis.service.model.SchedMidYearFundingDto;
import com.trinet.ambis.service.model.SchedTblAdminDto;
import com.trinet.ambis.service.model.SchedTblDto;
import com.trinet.ambis.util.Constants;
import javax.servlet.http.HttpServletRequest;

@Service
public class SchedTblServiceImpl implements SchedTblService {
	private static final Logger logger = LoggerFactory.getLogger(SchedTblServiceImpl.class);

	@Autowired
	SchedTblDao schedTblDao;
	@Autowired
	RealmPlanYearDao realmPlanYearDao;
	@Autowired
	RealmPlanYearService realmPlanYearService;
	@Autowired
	SchedMidYearFundingDao schedMidYearFundingDao;
	@Autowired
	PersonService personService;
	@Autowired
	EmployeeDao employeeDao;
	@Autowired
	SchedTblDataDao schedTblDataDao;	
	@Autowired
	EmployeeBenefitGroupDao employeeBenefitGroupDao;
	@Autowired
	EmployeeStrategyGroupTransactionDao employeeStrategyGroupTransactionDao;
	@Autowired
	BrokerNotificationService brokerNotificationService;

	@Override
	public SchedTbl getScheduleDates(String companyCode, String oeQuarter, Long realmYearId) {
		logger.debug("Entering method : getScheduleDates");
		SchedTbl schedDates = null;
		if (null == realmYearId) {
			RealmPlanYear rpy = realmPlanYearDao.getMaxRealmPlanYearByQuarter(oeQuarter);
			realmYearId = rpy.getId();
		}
		schedDates = schedTblDao.getSecheduleDates(companyCode, realmYearId);
		if (null == schedDates) {
			schedDates = schedTblDao.getSecheduleDates(Constants.DEFAULT_COMPANY_CODE, realmYearId);
		}
		logger.debug("Exiting method : getScheduleDates");
		return schedDates;
	}

	@Override
	public SchedTbl getCalcuatedScheduleDates(String companyCode, String oeQuarter, Long realmYearId) {
		SchedTbl defaultSchedDates = null;
		SchedTbl returnSchedTbl = null;
		
		if (null == realmYearId) {
			RealmPlanYear rpy = realmPlanYearDao.getMaxRealmPlanYearByQuarter(oeQuarter);
			realmYearId = rpy.getId();
		}
		returnSchedTbl = schedTblDao.getSecheduleDates(companyCode, realmYearId);
		defaultSchedDates = schedTblDao.getSecheduleDates(Constants.DEFAULT_COMPANY_CODE, realmYearId);
		if (null == returnSchedTbl) {
			returnSchedTbl = defaultSchedDates;
		} else if (returnSchedTbl.getExtensionEndDate().compareTo(defaultSchedDates.getExtensionEndDate()) < 0) {
			returnSchedTbl.setExtensionEndDate(defaultSchedDates.getExtensionEndDate());
		}
		return returnSchedTbl;
	}

	@Override
	@Transactional
	public SchedTbl createUpdateScheduleDates(HttpServletRequest request, SchedTblDto schedTblDto, String lastUpdatedBy) {
		logger.debug("Entering method : createUpdateScheduleDates");
		if(AppRulesAndConfigsUtils.isBrokerNotificationEnabled()){
			brokerNotificationService.validateAndSendBrokerNotification(request, schedTblDto);
		}
		SchedTbl schedTblEntity = new SchedTbl();
		schedTblDto.setLastUpdatedBy(lastUpdatedBy);
		schedTblDto.setUpdateTime(new Date());
		BeanUtils.copyProperties(schedTblDto, schedTblEntity);
		SchedTbl newSchedTbl = schedTblDao.saveAndFlush(schedTblEntity);
		logger.debug("Exiting method : createUpdateScheduleDates");
		return newSchedTbl;
	}

	@Override
	public List<SchedMidYearFundingDto> createUpdateMidYearDetails(SchedMidYearFundingDto smyfDto, Company company,
			boolean updateFlag) {
		logger.debug("Entering method : createUpdateMidYearDetails");
		smyfDto.setUpdtdtm(new Date());
		List<SchedMidYearFunding> schedMidYearFundingList = schedMidYearFundingDao.findByCompanyCode(company.getCode());
		ExceptionServiceHelper.validateMidYearFundingRequestData(smyfDto.getCompanyCode(), smyfDto.getServiceOrderNumber());

		boolean isExternalRenewalOpen = CompanyServiceHelper.isExternalRenewalOpen(company);
		if (smyfDto.isActive()) {
			if (company.isRenewalCompany() && !isExternalRenewalOpen) {
				//delete transactions
				employeeStrategyGroupTransactionDao.deleteByCompanyAndYear(company.getCode(), company.getRealmPlanYearId());
			}
			for (SchedMidYearFunding smyf1 : schedMidYearFundingList) {
				if (smyf1.isActive()) {
					smyf1.setActive(false);
				}
			}
		}
		if (updateFlag) {
			for (SchedMidYearFunding smyf1 : schedMidYearFundingList) {
				if (smyfDto.getId() == smyf1.getId()) {
					smyf1.setActive(smyfDto.isActive());
					smyf1.setMidYearFundingEffDate(smyfDto.getMidYearFundingEffDate());
					smyf1.setServiceOrderNumber(smyfDto.getServiceOrderNumber());
					smyf1.setLastUpdatedBy(smyfDto.getLastUpdatedBy());
					smyf1.setUpdtdtm(smyfDto.getUpdtdtm());
				}
			}
		} else {
			SchedMidYearFunding schedMidYearFundingEntity = new SchedMidYearFunding();
			BeanUtils.copyProperties(smyfDto, schedMidYearFundingEntity);
			schedMidYearFundingList.add(schedMidYearFundingEntity);
		}
		schedMidYearFundingDao.saveAll(schedMidYearFundingList);
		logger.debug("Exiting method : createUpdateMidYearDetails");
		return getMidYearDetails(company.getCode());
	}

	@Override
	public List<SchedMidYearFundingDto> getMidYearDetails(String companyCode) {
		logger.debug("Entering method : getMidYearDetails");
		List<SchedMidYearFunding> midYearFundingDetails = schedMidYearFundingDao.findByCompanyCodePlanYearEndDate(companyCode);
		List<SchedMidYearFundingDto> smyfDtoList = prepareSchedMidYearFundingDTO(companyCode, midYearFundingDetails);
		logger.debug("Exiting method : getMidYearDetails");
		return smyfDtoList;
	}
	
	@Override
	public List<SchedTblAdminDto> getSchedTblAdminDates(String companyCode, String quarter) {
		return schedTblDataDao.getSchedTblAdminDates(companyCode, quarter);
	}

	@Override
	public void validateRequest(SchedTblDto schedTblDto) {
		String company = schedTblDto.getSched().getCompany();
		// From ui DEFAULT is passed for exchange/quarter level
		if (!"DEFAULT".equals(company)) {
			ExceptionServiceHelper.validateCompanyCode(company);
		}
		ExceptionServiceHelper.validateOeQuarter(schedTblDto.getOeQuarter());
	}

	private List<SchedMidYearFundingDto> prepareSchedMidYearFundingDTO(String companyCode,
			List<SchedMidYearFunding> updatedSchedMidYearFundingList) {
		List<SchedMidYearFundingDto> updatedSmyfDtoList = new ArrayList<>();
		for (SchedMidYearFunding smyfEntity : updatedSchedMidYearFundingList) {
			SchedMidYearFundingDto updatedSmyfDto = new SchedMidYearFundingDto();
			updatedSmyfDto.setCompanyCode(companyCode);
			RealmPlanYear realmPlanYear = realmPlanYearService.getRealmForCompanyId(smyfEntity.getCompanyId());
			updatedSmyfDto.setPlanYearStartDate(realmPlanYear.getPlanYearStart());
			updatedSmyfDto.setPlanYearEndDate(realmPlanYear.getPlanYearEnd());
			updatedSmyfDto
					.setLastUpdatedByName(personService.getPersonFirstAndLastName(smyfEntity.getLastUpdatedBy()));
			updatedSmyfDto.setUpdatedTime(smyfEntity.getUpdtdtm());
			BeanUtils.copyProperties(smyfEntity, updatedSmyfDto);
			updatedSmyfDtoList.add(updatedSmyfDto);
		}
		return updatedSmyfDtoList;
	}
}