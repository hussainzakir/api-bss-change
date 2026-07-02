package com.trinet.ambis.service.impl;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.trinet.ambis.persistence.dao.hrp.EmployerEmployeePlansMappingDao;
import com.trinet.ambis.service.EmployerEmployeePlansMappingService;
import com.trinet.ambis.service.model.BenefitPlan;

/**
 * @author mpulipaka
 *
 */
public class EmployerEmployeePlansMappingServiceImpl implements EmployerEmployeePlansMappingService {
	@PersistenceContext(unitName = "bis-hrp")
	EntityManager em;

	@Autowired
	EmployerEmployeePlansMappingDao employerEmployeePlansMappingDao;

	private static final Logger logger = LoggerFactory.getLogger(EmployerEmployeePlansMappingServiceImpl.class);

	@Override
	public Map<BenefitPlan, BenefitPlan> getEmployerEmployeePlansMappingByRealmYearId(long realmPlanYear) {
		logger.info("Entering method: getEmployerEmployeePlansMappingByRealmYearId");
		Map<BenefitPlan, BenefitPlan> employeeEmployerPlansMappingMap = employerEmployeePlansMappingDao
				.getEmployerEmployeePlansMappingByRealmYearId(realmPlanYear);
		logger.info("Exiting method: getEmployerEmployeePlansMappingByRealmYearId");
		return employeeEmployerPlansMappingMap;
	}

	@Override
	public boolean isEmployerEmployeePlansMappingByRealmYearIdOffered(long realmPlanYear) {
		logger.info("Entering method: isEmployerEmployeePlansMappingByRealmYearIdOffered");
		boolean employerEmployeePlansMappingByRealmYearIdOffered = false;
		int employerEmployeePlansMappingByRealmYearIdOfferedCount = employerEmployeePlansMappingDao
				.getEmployerEmployeePlansMappingByRealmYearIdOfferedCount(realmPlanYear);
		if (employerEmployeePlansMappingByRealmYearIdOfferedCount > 0) {
			employerEmployeePlansMappingByRealmYearIdOffered = true;
		}
		logger.info("Exiting method: isEmployerEmployeePlansMappingByRealmYearIdOffered");
		return employerEmployeePlansMappingByRealmYearIdOffered;
	}

	@Override
	public Map<String, String> getEeAndErPlanMapping(long realmPlanYear) {
		return employerEmployeePlansMappingDao.getEeAndErPlanMapping(realmPlanYear);
	}

	/**
	 * @param employerEmployeePlansMappingDao
	 *            the employerEmployeePlansMappingDao to set
	 */
	public void setEmployerEmployeePlansMappingDao(EmployerEmployeePlansMappingDao employerEmployeePlansMappingDao) {
		this.employerEmployeePlansMappingDao = employerEmployeePlansMappingDao;
	}
}
