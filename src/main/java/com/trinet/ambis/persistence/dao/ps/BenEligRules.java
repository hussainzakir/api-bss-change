package com.trinet.ambis.persistence.dao.ps;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;

/**
 * BenEligRules replaces the Oracle PL/SQL stored procedure
 * T2_BENELIGRULES.SetupEligConfig for the purpose of making our BSS submit
 * transaction a single logical unit of work.
 * 
 * @author mikebro
 *
 */
public interface BenEligRules {
	/**
	 * The caller must set the
	 * entity manager to the entity manager of the current transaction.
	 * 
	 * @param em  The PeopleSoft entity manager of the current transaction
	 */
	public void setEntityManager(EntityManager em);

	/**
	 * Performs the setup of the eligibility rules for one benefit program.
	 *
	 * @param group  The current benefit group for which a benefit program will be built
	 * @param groups  The collection of benefit groups in this strategy
	 * @param company  The current company object
	 * @param targetDate  The date for which a benefit program will be built
	 */
	public void setupEligConfig( BenefitGroup group, List<BenefitGroup> groups, Company company, Date targetDate );
	
	public void createBasEligRules( BenefitGroup group, Company company );
	
}
