package com.trinet.ambis.persistence.dao.ps;

import javax.persistence.EntityManager;

import com.trinet.ambis.persistence.model.Company;

public interface BenProgInactivateDao {
	/**
	 * 
	 * @param company
	 * @param group
	 * @param em
	 * @return
	 */
	int updateClientOption2(Company company, String benProg, EntityManager em);

	/**
	 * 
	 * @param company
	 * @param group
	 * @param em
	 * @return
	 */
	int updateBenefitPrclBenPrgm(Company company, String benProg, EntityManager em);

	/**
	 * 
	 * @param company
	 * @param group
	 * @param em
	 * @return
	 */
	int updateBenDefinitionPgm(Company company, String benProg, EntityManager em);

	/**
	 * 
	 * @param company
	 * @param group
	 * @param em
	 * @return
	 */
	int updateBenDefinitionOptionEligRule(Company company, String benProg, EntityManager em);

	/**
	 * 
	 * @param company
	 * @param group
	 * @param em
	 * @return
	 */
	int deleteClientOpt2a(Company company, String benProg, EntityManager em);
}
