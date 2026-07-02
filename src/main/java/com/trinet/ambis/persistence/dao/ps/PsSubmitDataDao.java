package com.trinet.ambis.persistence.dao.ps;

import java.util.List;
import java.util.Map;

import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.model.PayInRateInfo;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategySummary;

public interface PsSubmitDataDao {
	public int insertBenefitSelectionEffectiveDate( Company company,  StrategySummary strategySummary );
	public int insertBenefitProgramFunding(Company company, BenefitGroup group);
	public int updateSTDBenefitDefitionOption(Company company, BenefitGroup group);
	public int deleteBenefitDefinitionPlanOfTypeA3(BenefitGroup group);
	public int deleteBenefitDefinitionOptionOfTypeA3(BenefitGroup group);
	public int deleteBenefitDefinitionCostOfTypeA3(BenefitGroup group);
	public int updateBenefitDefitionPlan(Company company, List<String> planTypes, BenefitGroup group);
	public int deleteBenefitDefinitionOptionOfTypeW(Company company, List<String> planTypes, BenefitGroup group);
	public int updateBenefitDefinitionOptionCost(Company company, BenefitGroup group);
	public int insertBenefitPlanRateData(Company company, BenefitGroup group, Map<String, XbssRealmPlyrPlan> plyrPlanMap, Map<String, List<PayInRateInfo>> payInRatesMap);
	public int insertBenefitPlanRateTbl(Company company, BenefitGroup group);
	public int insertBenefitDefinitionOption(Company company, BenefitGroup group, List<String> list);
	public int deleteBenefitRateData(Company company, BenefitGroup group);
	public int updateWaiveRow(Company comapny, List<String> list, BenefitGroup group);
	public int updateWaiveRowForAdditionalBenefits(Company company, BenefitGroup group);
	public Map<String, String> getDPPlanMapping(Company company, BenefitGroup group);
	
	
	/**
	 * @param company
	 * @param strategy
	 * @param userId
	 * @param emailFlag
	 * @param confirmationId
	 * @param resubmitFlag
	 * @param isMultiClientSubmit
	 * @param adminCounts
	 */
	public String submitData(Company company, StrategyData strategy, String userId, boolean emailFlag,
			String confirmationId, boolean resubmitFlag, boolean isMultiClientSubmit, Map<String, Integer> adminCounts);
	
	/**
	 * 
	 * @param company
	 * @param newPlanStartDate
	 */
	public void updateBenefitStartDateAndQuarterForPlanYearSync(Company company, String newPlanStartDate);
}
