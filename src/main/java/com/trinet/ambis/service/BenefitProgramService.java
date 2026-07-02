package com.trinet.ambis.service;

import java.util.Set;

import javax.persistence.EntityManager;

import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;

public interface BenefitProgramService {

	/**
	 * Create or replace a benefit program structure in PeopleSoft.
	 * 
	 * @param company
	 * @param group
	 */
	public void createBenefitProgram(Company company, BenefitGroup group);

	/**
	 * During new client setup, the application may have needed to create
	 * future-dated benefit program rows. Once these are no longer needed, they
	 * should be deleted.
	 * 
	 * @param company
	 * @param group
	 */
	public void deleteFutureProgram(Company company, BenefitGroup group);

	/**
	 * Performs all the steps required to inactivate a benefit program in PeopleSoft (without physically deleting the data)
	 * @param company the current Company object
	 * @param benefitGroup a benefit program code
	 * @param benefitClass the ELIG_CONFIG1 value corresponding to the benefit program code
	 * @param em the current PeopleSoft EntityManager object
	 */
	void updateInactiveBenefitPrograms(Company company, String benefitGroup, String benefitClass, EntityManager em);

	/**
	 * Deletes any future-dated benefit defn plan, option, cost during new client setup
	 * 
	 * @param company
	 * @param group
	 * @param nomedNotApplicablePlanTypes
	 */
	void deleteBenDefn(Company company, BenefitGroup group, Set<String> nomedNotApplicablePlanTypes);

}
