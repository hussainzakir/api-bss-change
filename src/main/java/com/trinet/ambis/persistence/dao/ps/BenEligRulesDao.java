package com.trinet.ambis.persistence.dao.ps;

import javax.persistence.EntityManager;

/**
 * An interface to encapsulate all the SQL actions against any of the PS_BAS_ELIG... tables in PeopleSoft
 * @author mbrothers
 *
 */
public interface BenEligRulesDao {


	public String getEligFlgCnfig1Exact( String eligRulesId, String effdtStr, EntityManager em );
	public String getEligFlgCnfig1AsOf( String eligRulesId, String effdtStr, EntityManager em );


	public void setEligUseCnfig1( String eligRulesId, String effdtStr, String yesNo, EntityManager em );
	public void setEligFlgCnfig1( String eligRulesId, String effdtStr, String eligFlag, EntityManager em );
	public void setEligFlgEeclas( String eligRulesId, String effdtStr, String flgEeClass, EntityManager em );


	public void cleanEligRules( String eligRulesId, String effdtStr, EntityManager em );
	public void cleanEligPygrp( String eligRulesId, String effdtStr, EntityManager em );
	public void cleanEligBnstat( String eligRulesId, String effdtStr, EntityManager em );
	public void cleanEligEeclas( String eligRulesId, String effdtStr, EntityManager em );
	public void cleanEligCnfig1( String eligRulesId, String effdtStr, EntityManager em );


	public void insertBasEligRules( String eligRulesId, String effdtStr, String description, String pfClient, String cloneEligRule, EntityManager em );
	public void insertBasEligPygrp( String eligRulesId, String effdtStr, String companyCode, String cloneEligRule, EntityManager em );
	public void insertBasEligBnstat( String eligRulesId, String effdtStr, String cloneEligRule, EntityManager em );
	public int  insertBasEligEeclas( String eligRulesId, String effdtStr, String cloneEligRule, EntityManager em );
	public void insertBasEligCnfig1( String eligRulesId, String effdtStr, String eligConfig1, EntityManager em );

}
