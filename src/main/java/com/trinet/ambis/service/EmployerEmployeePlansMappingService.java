package com.trinet.ambis.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.EmployerEmployeePlansMappingDao;
import com.trinet.ambis.service.model.BenefitPlan;

@Service
public interface EmployerEmployeePlansMappingService {

	/**
	 * This method is for retrieving employee and employer plans mapping for the
	 * given realm plan year ID.
	 * 
	 * @param PlanYear
	 *            Realm plan year Id.
	 * @return list of wait periods
	 */
	public Map<BenefitPlan, BenefitPlan> getEmployerEmployeePlansMappingByRealmYearId(long realmPlanYear);

	/**
	 * This method is for retrieving employee and employer plans mapping for the
	 * given realm plan year ID.
	 * 
	 * @param PlanYear
	 *            Realm plan year Id.
	 * @return list of wait periods
	 */
	public boolean isEmployerEmployeePlansMappingByRealmYearIdOffered(long realmPlanYear);
	/**
	 * 
	 * @param employerEmployeePlansMappingDao
	 */
	public void setEmployerEmployeePlansMappingDao(EmployerEmployeePlansMappingDao employerEmployeePlansMappingDao);
	/**
	 * 
	 * @param realmPlanYear
	 * @return
	 */
	Map<String, String> getEeAndErPlanMapping(long realmPlanYear);
}
