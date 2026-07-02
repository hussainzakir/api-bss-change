package com.trinet.ambis.service;

import javax.persistence.EntityManager;

import com.trinet.ambis.persistence.dao.ps.HSAPlansDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.sp.GetNextBenefitPlan;
import com.trinet.ambis.service.impl.HSAPlanMapping;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;

public interface HSAPlanService {

	/**
	 * Setup HSA benefit plans for clients that choose a custom HSA option.  This method will setup
	 * the benefit program structure, the plan attributes, and the limit tables for all the HSA plans.
	 * @param company
	 * @param group
	 * @param hsaOptions
	 * @param em BSS entity manager
	 */
	public void setupHSABenefitPlans( Company company, BenefitGroup group, StrategyHsaFundingDto hsaOptions,
			EntityManager em );
	
	/** 
	 * This allows the calling method to set an implementation for the HSA Plans DAO.
	 * This setter provides flexibility and enables automated unit testing of this class.
	 * @param dao an instance of the dao class to create HSA plans
	 */
	public void setHSAPlansDao( HSAPlansDao dao );

	/** 
	 * This allows the calling method to set an implementation for the stored procedure object.
	 * This setter provides flexibility and enables automated unit testing of this class.
	 * @param sp an instance of the class to create the next BENEFIT_PLAN code from PeopleSoft
	 */
	public void setNextBenPlanSP( GetNextBenefitPlan sp );

	/**
	 * This allows the calling method to set the HSA plan map object.
	 * This setter provides flexibility and enables automated unit testing of the class. 
	 * @param hsaMap
	 */
	public void setHSAPlanMapping( HSAPlanMapping hsaMap );


}
