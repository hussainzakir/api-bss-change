package com.trinet.ambis.persistence.dao.hrp;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenefitPlan;

public interface EmployeeSelectionDao {

	/**
	 * 
	 * @param planYears
	 * @return
	 */
	Map<String, BenefitPlan> getRealmPlanYearBenefitPlans(List<Long> planYears);

	/**
	 * 
	 * @param company
	 * @param date
	 * @return
	 */
	Map<String, Integer> getEmployeesByBG(Company company, Date date);
}
