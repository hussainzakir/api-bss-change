package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.PlanPackage;

public interface TemplateFundingDao {
	/**
	 * This method is for getting all the Benefit plans for the Template.
	 * 
	 * @param indType
	 * @param state
	 * @param pkgTypes
	 * @param isTexasSitus
	 * @return set of benefit plans
	 */
	Map<String, Set<BenefitPlan>> getAllTemplateBenefitPlans(Company company);

	/**
	 * 
	 * @param indType
	 * @param state
	 * @param pkgTypes
	 * @param realmYearId
	 * @return
	 */
	Map<String, List<PlanPackage>> getAllTemplateFundingDetails(String indType, String state, List<String> pkgTypes,
			long realmYearId, boolean isTexasSitus);

	/**
	 * 
	 * @param company
	 * @param PkgType
	 * @return
	 */
	Map<String, List<String>> getTemplateHeadCountPlans(Company company, String pkgType);
	
	/**
	 * 
	 * @param company
	 * @param headCountPlans
	 * @return
	 */
	List<String> getAlternateHeadCountPlans(Company company, List<String> headCountPlans);
}
