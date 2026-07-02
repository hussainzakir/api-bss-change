package com.trinet.ambis.persistence.dao.ps.impl;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.ps.BenEligRulesDao;
import com.trinet.ambis.util.DaoUtils;;

public class BenEligRulesDaoImpl implements BenEligRulesDao {

	// SQL parameters, names and display constants
	public static final String CLEAN_ELIG_BNSTAT = "CLEAN_ELIG_BNSTAT";
	public static final String CLEAN_ELIG_CNFIG1 = "CLEAN_ELIG_CNFIG1";
	public static final String CLEAN_ELIG_EECLAS = "CLEAN_ELIG_EECLAS";
	public static final String CLEAN_ELIG_PYGRP = "CLEAN_ELIG_PYGRP";
	public static final String CLEAN_ELIG_RULES = "CLEAN_ELIG_RULES";
	public static final String GET_DEFAULT_ELIG_RULE = "GET_DEFAULT_ELIG_RULE";
	public static final String GET_DEFAULT_ELIG_RULE_EFFDT = "GET_DEFAULT_ELIG_RULE_EFFDT";
	public static final String INSERT_BAS_ELIG_BNSTAT = "INSERT_BAS_ELIG_BNSTAT";
	public static final String INSERT_BAS_ELIG_EECLAS = "INSERT_BAS_ELIG_EECLAS";
	public static final String INSERT_BAS_ELIG_PYGRP = "INSERT_BAS_ELIG_PYGRP";
	public static final String INSERT_BAS_ELIG_RULES = "INSERT_BAS_ELIG_RULES";
	public static final String INSERT_ELIG_CNFIG1 = "INSERT_ELIG_CNFIG1";
	public static final String SET_ELIG_FLG_CNFIG1 = "SET_ELIG_FLG_CNFIG1";
	public static final String SET_ELIG_USE_CNFIG1 = "SET_ELIG_USE_CNFIG1";
	public static final String UPDATE_RULES_EECLAS = "UPDATE_RULES_EECLAS";


	@Override
	public String getEligFlgCnfig1Exact( String eligRulesId, String effdtStr, EntityManager em ) {
		String eligFlag = null;
		Query query = em.createNamedQuery(GET_DEFAULT_ELIG_RULE_EFFDT);
		query.setParameter("defaultEligRule", eligRulesId );
		query.setParameter(BSSQueryConstants.EFF_DATE_STR, effdtStr );
		try {
			eligFlag = (String) DaoUtils.getSingleResult(query, GET_DEFAULT_ELIG_RULE_EFFDT);
		} catch( NoResultException nre ) {
			eligFlag = " ";
		}
		return eligFlag;
	}


	@Override
	public String getEligFlgCnfig1AsOf( String eligRulesId, String effdtStr, EntityManager em ) {
		String eligFlag = null;
		Query query = em.createNamedQuery(GET_DEFAULT_ELIG_RULE);
		query.setParameter("defaultEligRule", eligRulesId );
		query.setParameter(BSSQueryConstants.EFF_DATE_STR, effdtStr );
		try {
			eligFlag = (String) DaoUtils.getSingleResult(query, GET_DEFAULT_ELIG_RULE);
		} catch( NoResultException nre ) {
			eligFlag = " ";
		}
		return eligFlag;
	}


	@Override
	public void setEligUseCnfig1( String eligRulesId, String effdtStr, String yesNo, EntityManager em ) {
		// This method must not update any ELIG_RULES_ID that represents "inactive"
		if( BSSApplicationConstants.ELIG_INACTIVE.contains( eligRulesId ) ) {
			return;
		}
		Query query = em.createNamedQuery(SET_ELIG_USE_CNFIG1);
		query.setParameter( BSSQueryConstants.ELIG_RULES_ID, eligRulesId );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtStr );
		query.setParameter( "yesNoFlag", yesNo );
		DaoUtils.executeUpdate(query, SET_ELIG_USE_CNFIG1);
	}


	@Override
	public void insertBasEligCnfig1( String eligRulesId, String effdtStr, String eligConfig1, EntityManager em ) {
		// This method must not update any ELIG_RULES_ID that represents "inactive"
		if( BSSApplicationConstants.ELIG_INACTIVE.contains( eligRulesId ) ) {
			return;
		}
		Query query = em.createNamedQuery(INSERT_ELIG_CNFIG1);
		query.setParameter(BSSQueryConstants.ELIG_RULES_ID, eligRulesId );
		query.setParameter(BSSQueryConstants.EFF_DATE_STR, effdtStr );
		query.setParameter(BSSQueryConstants.ELIG_CONFIG_1, eligConfig1 );
		DaoUtils.executeUpdate(query, INSERT_ELIG_CNFIG1);
	}


	@Override
	public void setEligFlgCnfig1( String eligRulesId, String effdtStr, String eligFlag, EntityManager em ) {
		// This method must not update any ELIG_RULES_ID that represents "inactive"
		if( BSSApplicationConstants.ELIG_INACTIVE.contains( eligRulesId ) ) {
			return;
		}
		Query query = em.createNamedQuery(SET_ELIG_FLG_CNFIG1);
		query.setParameter(BSSQueryConstants.ELIG_RULES_ID, eligRulesId );
		query.setParameter(BSSQueryConstants.EFF_DATE_STR, effdtStr );
		query.setParameter("inelgEligFlag", eligFlag );
		DaoUtils.executeUpdate(query, SET_ELIG_FLG_CNFIG1);
	}


	@Override
	public void cleanEligRules( String eligRulesId, String effdtStr, EntityManager em ) {
		// This method must not update any ELIG_RULES_ID that represents "inactive"
		if( BSSApplicationConstants.ELIG_INACTIVE.contains( eligRulesId ) ) {
			return;
		}
		Query query = em.createNamedQuery(CLEAN_ELIG_RULES);
		query.setParameter( BSSQueryConstants.ELIG_RULES_ID, eligRulesId );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtStr );
		DaoUtils.executeUpdate(query, CLEAN_ELIG_RULES);
	}

	@Override
	public void cleanEligPygrp( String eligRulesId, String effdtStr, EntityManager em ) {
		// This method must not update any ELIG_RULES_ID that represents "inactive"
		if( BSSApplicationConstants.ELIG_INACTIVE.contains( eligRulesId ) ) {
			return;
		}
		Query query = em.createNamedQuery(CLEAN_ELIG_PYGRP);
		query.setParameter( BSSQueryConstants.ELIG_RULES_ID, eligRulesId );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtStr );
		DaoUtils.executeUpdate(query, CLEAN_ELIG_PYGRP);
	}

	@Override
	public void cleanEligBnstat( String eligRulesId, String effdtStr, EntityManager em ) {
		// This method must not update any ELIG_RULES_ID that represents "inactive"
		if( BSSApplicationConstants.ELIG_INACTIVE.contains( eligRulesId ) ) {
			return;
		}
		Query query = em.createNamedQuery(CLEAN_ELIG_BNSTAT);
		query.setParameter( BSSQueryConstants.ELIG_RULES_ID, eligRulesId );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtStr );
		DaoUtils.executeUpdate(query, CLEAN_ELIG_BNSTAT);
	}

	@Override
	public void cleanEligEeclas( String eligRulesId, String effdtStr, EntityManager em ) {
		// This method must not update any ELIG_RULES_ID that represents "inactive"
		if( BSSApplicationConstants.ELIG_INACTIVE.contains( eligRulesId ) ) {
			return;
		}
		Query query = em.createNamedQuery(CLEAN_ELIG_EECLAS);
		query.setParameter( BSSQueryConstants.ELIG_RULES_ID, eligRulesId );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtStr );
		DaoUtils.executeUpdate(query, CLEAN_ELIG_EECLAS);
	}

	@Override
	public void cleanEligCnfig1( String eligRulesId, String effdtStr, EntityManager em ) {
		// This method must not update any ELIG_RULES_ID that represents "inactive"
		if( BSSApplicationConstants.ELIG_INACTIVE.contains( eligRulesId ) ) {
			return;
		}
		Query query = em.createNamedQuery(CLEAN_ELIG_CNFIG1);
		query.setParameter( BSSQueryConstants.ELIG_RULES_ID, eligRulesId );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtStr );
		DaoUtils.executeUpdate(query, CLEAN_ELIG_CNFIG1);
	}


	@Override
	public void insertBasEligRules( String eligRulesId, String effdtStr, String description, String pfClient, String cloneEligRule, EntityManager em ) {
		// This method must not update any ELIG_RULES_ID that represents "inactive"
		if( BSSApplicationConstants.ELIG_INACTIVE.contains( eligRulesId ) ) {
			return;
		}
		Query query = em.createNamedQuery(INSERT_BAS_ELIG_RULES);
		query.setParameter(BSSQueryConstants.ELIG_RULES_ID, eligRulesId );
		query.setParameter(BSSQueryConstants.EFF_DATE_STR, effdtStr );
		query.setParameter("descr", description );
		query.setParameter(BSSQueryConstants.PF_CLIENT, pfClient );
		query.setParameter(BSSQueryConstants.CLONE_ELIG_RULE, cloneEligRule );
		DaoUtils.executeUpdate(query, INSERT_BAS_ELIG_RULES);
	}


	@Override
	public void insertBasEligPygrp( String eligRulesId, String effdtStr, String companyCode, String cloneEligRule, EntityManager em ) {
		// This method must not update any ELIG_RULES_ID that represents "inactive"
		if( BSSApplicationConstants.ELIG_INACTIVE.contains( eligRulesId ) ) {
			return;
		}
		Query query = em.createNamedQuery(INSERT_BAS_ELIG_PYGRP);
		query.setParameter(BSSQueryConstants.ELIG_RULES_ID, eligRulesId );
		query.setParameter(BSSQueryConstants.EFF_DATE_STR, effdtStr );
		query.setParameter(BSSQueryConstants.COMPANY, companyCode );
		query.setParameter(BSSQueryConstants.CLONE_ELIG_RULE, cloneEligRule );
		DaoUtils.executeUpdate(query, INSERT_BAS_ELIG_PYGRP);
	}


	@Override
	public void insertBasEligBnstat( String eligRulesId, String effdtStr, String cloneEligRule, EntityManager em ) {
		// This method must not update any ELIG_RULES_ID that represents "inactive"
		if( BSSApplicationConstants.ELIG_INACTIVE.contains( eligRulesId ) ) {
			return;
		}
		Query query = em.createNamedQuery(INSERT_BAS_ELIG_BNSTAT);
		query.setParameter(BSSQueryConstants.ELIG_RULES_ID, eligRulesId );
		query.setParameter(BSSQueryConstants.EFF_DATE_STR, effdtStr );
		query.setParameter(BSSQueryConstants.CLONE_ELIG_RULE, cloneEligRule );
		DaoUtils.executeUpdate(query, INSERT_BAS_ELIG_BNSTAT);
	}


	@Override
	public int insertBasEligEeclas( String eligRulesId, String effdtStr, String cloneEligRule, EntityManager em ) {
		// This method must not update any ELIG_RULES_ID that represents "inactive"
		if( BSSApplicationConstants.ELIG_INACTIVE.contains( eligRulesId ) ) {
			return 0;
		}
		Query query = em.createNamedQuery(INSERT_BAS_ELIG_EECLAS);
		query.setParameter(BSSQueryConstants.ELIG_RULES_ID, eligRulesId );
		query.setParameter(BSSQueryConstants.EFF_DATE_STR, effdtStr );
		query.setParameter(BSSQueryConstants.CLONE_ELIG_RULE, cloneEligRule );
		int rows = DaoUtils.executeUpdate(query, INSERT_BAS_ELIG_EECLAS);
		return rows;
	}


	@Override
	public void setEligFlgEeclas( String eligRulesId, String effdtStr, String flgEeClass, EntityManager em ) {
		// This method must not update any ELIG_RULES_ID that represents "inactive"
		if( BSSApplicationConstants.ELIG_INACTIVE.contains( eligRulesId ) ) {
			return;
		}
		Query query = em.createNamedQuery(UPDATE_RULES_EECLAS);
		query.setParameter(BSSQueryConstants.ELIG_RULES_ID, eligRulesId );
		query.setParameter(BSSQueryConstants.EFF_DATE_STR, effdtStr );
		query.setParameter("flgEeClass", flgEeClass );
		DaoUtils.executeUpdate(query, UPDATE_RULES_EECLAS);
	}

}
