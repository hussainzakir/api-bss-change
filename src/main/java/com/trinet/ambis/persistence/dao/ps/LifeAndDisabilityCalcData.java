package com.trinet.ambis.persistence.dao.ps;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.AdditionalBenefitEmployeeDetails;
import com.trinet.ambis.service.model.AdditionalPlanRate;
import com.trinet.ambis.service.model.FormulaDefinition;
import com.trinet.ambis.service.model.FormulaProperties;
import com.trinet.ambis.service.model.RateProperties;

public interface LifeAndDisabilityCalcData {
	/**
	 * 
	 * @param cloneBenProg
	 * @param effDt
	 * @param planList
	 * @param bandCode
	 * @param quarter
	 * @return
	 */
	Map<String, RateProperties> getRateProperties(String cloneBenProg, Date effDt, Set<String> planList,
			String bandCode, String quarter);

	/**
	 * 
	 * @param planList
	 * @param effDt
	 * @param queryStr
	 * @return
	 */
	Map<String, FormulaProperties> getFormulaProperties(Set<String> planList, Date effDt, String queryStr);

	/**
	 * 
	 * @param formulaID
	 * @param formulaEffDt
	 * @return
	 */
	List<FormulaDefinition> getFormulaDefinition(String formulaID, Date formulaEffDt);

	/**
	 * 
	 * @param rateIds
	 * @param effDt
	 * @return
	 */
	Map<String, List<AdditionalPlanRate>> getPlanRates(Set<String> rateIds, Date effDt);

	/**
	 * 
	 * @param company
	 * @param life_query
	 * @param disabilityquery
	 * @param history
	 * @return
	 */
	Map<String, AdditionalBenefitEmployeeDetails> getGroupEmployeeSelections(Company company, boolean history,
			long strategyId, boolean isVendorMappingOn);

}
