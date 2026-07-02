package com.trinet.ambis.persistence.dao.hrp;

import java.util.Map;

import com.trinet.ambis.service.model.BenefitPlan;

public interface EmployerEmployeePlansMappingDao {

	int getEmployerEmployeePlansMappingByRealmYearIdOfferedCount(long realmPlanYear);

	Map<BenefitPlan, BenefitPlan> getEmployerEmployeePlansMappingByRealmYearId(long realmPlanYear);

	Map<String, String> getEeAndErPlanMapping(long realmPlanYear);

}
