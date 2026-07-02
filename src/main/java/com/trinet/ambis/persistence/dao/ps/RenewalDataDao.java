package com.trinet.ambis.persistence.dao.ps;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanMapping;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanHeadCount;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;

public interface RenewalDataDao {
	/**
	 * 
	 * @param company
	 * @param effDate
	 * @return
	 */
	Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> getHeadCountByGroupAndPlan(String company, Date effDate);

	/**
	 * 
	 * @param company
	 * @param effDate
	 * @return
	 */
	Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> getAdditionalPlansHeadCountByGroup(String company,
			Date effDate);

	/**
	 * 
	 * @param pfClient
	 * @param currentPlanYearStartDate
	 * @return
	 */
	List<BenefitGroup> getBenefitPrograms(String pfClient, Date currentPlanYearStartDate);

	/**
	 * This method is for getting selected health benefit plans for the renewal company.
	 * @param benefitProgram
	 * @param currentPlanYearStartDate
	 * @param pfClient
	 * @param CompanyCode
	 * @return
	 */
	Map<String, Map<String, Map<String, BenefitPlan>>> getHealthPlansForRenewalCompany(Date currentPlanYearStartDate, String pfClient, String companyCode);
	/**
	 * This method is for getting selected additional benefit plans for the renewal company.
	 * @param benefitProgram
	 * @param currentPlanYearStartDate
	 * @param pfClient
	 * @param CompanyCode
	 * @return
	 */
	Map<String, Map<String, Map<String, BenefitPlan>>> getAdditionalBenefitPlansForRenewalCompany(
			Date currentPlanYearStartDate, String pfClient, String companyCode);

	/**
	 * 
	 * @param pfClient
	 * @param effDate
	 * @return
	 */
	Map<String, String> getEligRuleIdsByClient(String pfClient, Date effDate);
	/**
	 * 
	 * @param pfClient
	 * @param company
	 * @return
	 */
	Map<String, String> getWaitPeriodByClient(String pfClient, String company, Date effDate);	

	/**
	 *
	 * @param company
	 * @param strategyId
	 * @param effDate
	 * @param realmPlanMapping
	 * @param eeErPlanMapping
	 * @param benefitProgramPlanTypes
	 * @param isVendorMappingOn
	 * @return
	 */
	Map<String, List<BenefitPlanHeadCount>> getPlanHeadCountByGroups(Company company, Long strategyId, Date effDate,
			Map<String, PlanMapping> realmPlanMapping, Map<String, String> eeErPlanMapping,
			Map<String, List<String>> benefitProgramPlanTypes, boolean isVendorMappingOn);

	/**
	 * 
	 * @param company
	 * @param effDate
	 * @return
	 */
	Map<String, Map<String, Map<String, Object>>> getRenewalFundingDetails(String company, Date effDate);

	/**
	 * This method returns the list of ActiveEligibleEECount for given company by
	 * code, effdt and realmYrId. It also identifies in/out state group count for
	 * sdi states
	 * @param company
	 * @param history
	 * @param strategyId
	 * @param realmPlanYear
	 * @return
	 */
	Map<String, ActiveEligibleEECount> getActiveEligibleEECount(Company company, boolean history,
			long strategyId, RealmPlanYear realmPlanYear);
	
	/**
	 * This method returns a map of benefit groups and count of enrolled
	 * employees on Medical, Dental and/or Vision
	 * 
	 * @param company
	 * @param history
	 * @param strategyId
	 * @return
	 */
	Map<String, Integer> getPrimaryEnrolledEECount(Company company, boolean history, long strategyId);
	
	/** 
	 * @param benefitProgram
	 * @param effDate
	 * @return
	 */
	Map<String, String> getRateTableIds(String benefitProgram, Date effDate);
	
	/**
	 * 
	 * @param benefitPrograms
	 * @param effDate
	 * @param includeGroupPlanTypes
	 * @return
	 */
	 List<String> getBsuppVoluntaryPlanTypes(String benefitProgram, Date effDate, boolean includeGroupPlanTypes, List<String> supportedVoluntaryPlanTypes);

	/**
	 * Returns the company's HSA PS setup data as of the passed in effective date. 
	 * @param companyCode
	 * @param effDate
	 * @return
	 */
	StrategyHsaFundingDto getPsHsaFundingDetails(String companyCode, Date effDate);

}
