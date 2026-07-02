package com.trinet.ambis.persistence.dao.ps.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.ps.BenEligRules;
import com.trinet.ambis.persistence.dao.ps.BenEligRulesDao;
import com.trinet.ambis.persistence.dao.ps.EligConfigDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.DaoUtils;

public class BenEligRulesImpl implements BenEligRules {
	
	private static final Logger logger = LoggerFactory.getLogger(BenEligRulesImpl.class);
	
	private EntityManager psEntityManager;
	
	/**
	 * The effective date of this submission.  For new clients, this is the earliest date
	 * for which setup data is required.  For renewals, this is the renewal effective date.
	 */
	private String effdtStr;

	EligConfigDao eligConfigDao = new EligConfigDaoImpl();
	BenEligRulesDao benEligRulesDao = new BenEligRulesDaoImpl();

	// SQL parameters, names and display constants
	public static final String GET_CLONE_ELIG_RULE = "GET_CLONE_ELIG_RULE";
	public static final String UPDATE_CLIENT_OPT2A = "UPDATE_CLIENT_OPT2A";
	public static final String UPDATE_PGM_ELIG_RULE = "UPDATE_PGM_ELIG_RULE";

	private static final int DESCR_LENGTH_MAX = 30;


	public void setEntityManager( EntityManager em ) {
		this.psEntityManager = em;
	}
	
	// This setter is required for junit test.
	public void setEligConfigDao(EligConfigDao eligConfigDao) {
		this.eligConfigDao = eligConfigDao;
	}

	public void setupEligConfig( BenefitGroup group, List<BenefitGroup> groups, Company company, Date targetDate ) {
		this.chooseEffectiveDate( company );
		
		String grpClass = null;
		if( groups.size() > 1 ) {
			grpClass = "BCL";
		} else {
			grpClass = "ONE";
		}
		
		Query query = this.psEntityManager.createNamedQuery(UPDATE_CLIENT_OPT2A);
		query.setParameter("groupDescr", group.getName());
		String eligConfigOne = grpClass.equals("ONE") ? " " : group.getEligConfig1();
		query.setParameter(BSSQueryConstants.ELIG_CONFIG_1, eligConfigOne);
		query.setParameter("isDflt", ( group.isDefaultGroup() ? "Y" : "N" ));
		query.setParameter("waitingPeriod", group.getWaitingPeriod());
		query.setParameter("groupClassification", grpClass);
		query.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		query.setParameter(BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram());
		query.setParameter(BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate());

		DaoUtils.executeUpdate(query, UPDATE_CLIENT_OPT2A);
		
		// update the ELIGCNFG PeopleSoft component with any changes to ELIG_CONFIG1 values
		int descrLength = Math.min( DESCR_LENGTH_MAX, group.getName().length() );
		String descr = group.getName().substring( 0, descrLength );
		eligConfigDao.putEligConfigRow( company.getPfClient(), this.effdtStr, group.getEligConfig1(), "A", descr, psEntityManager );
		
		
		// begin setup elig/inelig rules
		
		BenefitGroup defaultGroup = null;
		String eligFlag = " ";
		// Identify the default benefit group
		for( BenefitGroup g : groups ) {
			if( g.isDefaultGroup() ) {
				defaultGroup = g;
				break;
			}
		}

		if(null != defaultGroup){
			if( company.isRenewalCompany() ) {
				eligFlag = this.benEligRulesDao.getEligFlgCnfig1Exact( defaultGroup.getEligRuleId(), this.effdtStr, psEntityManager );
			} else {
				eligFlag = this.benEligRulesDao.getEligFlgCnfig1AsOf( defaultGroup.getEligRuleId(), this.effdtStr, psEntityManager );
			}

			if( BSSApplicationConstants.EMPTY_SPACE.equals( eligFlag ) ) {
				/* No row was found for the default group's elig rule ID so it must be created */
				this.createBasEligRules( defaultGroup, company );
			}
		}

		if( group.isDefaultGroup() ) {
			/* If there is only one benefit group in the strategy, cancel the ELIG_CONFIG1 rules */
			if( groups.size() == 1 ) {
				this.benEligRulesDao.cleanEligCnfig1( group.getEligRuleId(), this.effdtStr, psEntityManager );
				this.benEligRulesDao.setEligUseCnfig1( group.getEligRuleId(), this.effdtStr, "N", psEntityManager );
			}
		} else {
			/* This is not the default benefit group.  Both the default elig rule and this
			 * group's elig rule must be updated.
			 * First, update the default benefit group's eligibility rule.  Turn on ELIG_CONFIG1 criteria
			 * and add this benefit class to the list of ELIG_CONFIG1 values ineligible for the default rule.
			 */
			if (null != defaultGroup) {
				this.benEligRulesDao.setEligUseCnfig1( defaultGroup.getEligRuleId(), this.effdtStr, "Y", psEntityManager );
				this.benEligRulesDao.insertBasEligCnfig1( defaultGroup.getEligRuleId(), this.effdtStr, group.getEligConfig1(), psEntityManager );
				
				// set the Ineligible flag on the default benefit group's elig rule
				if( ! "I".equals(eligFlag) ) {
					this.benEligRulesDao.setEligFlgCnfig1( defaultGroup.getEligRuleId(), this.effdtStr, "I", psEntityManager );
				}
			}

			/* Next, update the eligibility rule for the current benefit group.  Set the benefit class
			 * value as ELIG_CONFIG1 and set the rule flag to "E" for eligible
			 */
			this.benEligRulesDao.insertBasEligCnfig1( group.getEligRuleId(), this.effdtStr, group.getEligConfig1(), psEntityManager );
			this.benEligRulesDao.setEligFlgCnfig1( group.getEligRuleId(), this.effdtStr, "E", psEntityManager );
		}
	}


	/** <p>Choose an effective date, formatted as a string.</p>
	 *  <p>For new clients, this is the earliest date that elig
	 *  rules will be setup for this company.  For renewals, this will be the renewal effective date.</p>
	 *
	 * @param company
	 */
	private void chooseEffectiveDate( Company company ) {
		if( company.isRenewalCompany() ) {
			this.effdtStr = company.getPlanStartDate();
		} else {
			Date effectiveDate;
			Date setupDate = CommonUtils.formatStringToDate(company.getCompanySetupDate(),
					BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
			Date liveDate = CommonUtils.formatStringToDate(company.getLiveDate(),
					BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
			effectiveDate = CommonUtils.chooseLesserDate( setupDate, liveDate );
			this.effdtStr = CommonUtils.formatDateToString( effectiveDate,
					BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY );
		}
	}

	public void createBasEligRules( BenefitGroup group, Company company ) {
		this.chooseEffectiveDate( company );
		this.cleanEligRules( group );
		this.insertBasEligRules( group, company );
		this.updateBasEligRules( group, company );

	}

	private void cleanEligRules( BenefitGroup group ) {
		this.benEligRulesDao.cleanEligPygrp( group.getEligRuleId(), this.effdtStr, psEntityManager );
		this.benEligRulesDao.cleanEligBnstat( group.getEligRuleId(), this.effdtStr, psEntityManager );
		this.benEligRulesDao.cleanEligEeclas( group.getEligRuleId(), this.effdtStr, psEntityManager );
		this.benEligRulesDao.cleanEligCnfig1( group.getEligRuleId(), this.effdtStr, psEntityManager );
		this.benEligRulesDao.cleanEligRules( group.getEligRuleId(), this.effdtStr, psEntityManager );
	}

	private void insertBasEligRules( BenefitGroup group, Company company ) {
		Query query1 = this.psEntityManager.createNamedQuery(GET_CLONE_ELIG_RULE);
		query1.setParameter("peoId", company.getRealm().getPeoid() );
		String cloneEligRule = (String) DaoUtils.getSingleResult(query1, GET_CLONE_ELIG_RULE);
		logger.info( "Result: cloneEligRule: {}:", cloneEligRule );

		this.benEligRulesDao.insertBasEligRules( group.getEligRuleId(), this.effdtStr, group.getName(), company.getPfClient(), cloneEligRule, psEntityManager );
		this.benEligRulesDao.insertBasEligPygrp( group.getEligRuleId(), this.effdtStr, company.getCode(), cloneEligRule, psEntityManager );
		this.benEligRulesDao.insertBasEligBnstat( group.getEligRuleId(), this.effdtStr, cloneEligRule, psEntityManager );
		int numEEClas = this.benEligRulesDao.insertBasEligEeclas( group.getEligRuleId(), this.effdtStr, cloneEligRule, psEntityManager );

		/* if this eligibility rule contained EECLAS details, we are going to assume these
		 * details are a list of EMPL_CLASS values that indicate K1 and enforce eligibility
		 */
		if( numEEClas > 0 ) {
			String flgEeClass;
			if( "K1".equals( group.getType() ) ) {
				flgEeClass = "E";
			} else {
				flgEeClass = "I";
			}

			// set the EMPL_CLASS flags correctly for this benefit group (K1 or Standard group)
			this.benEligRulesDao.setEligFlgEeclas( group.getEligRuleId(), this.effdtStr, flgEeClass, psEntityManager );
		}

	}

	/*  Update the plan type 01 record in the benefit program structure with this benefit
	 *  group's eligibility rule.  Update both the company setup date and the start date.
	 */
	private void updateBasEligRules( BenefitGroup group, Company company ) {
		Query query1 = this.psEntityManager.createNamedQuery(UPDATE_PGM_ELIG_RULE);
		query1.setParameter(BSSQueryConstants.ELIG_RULES_ID, group.getEligRuleId() );
		query1.setParameter(BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query1.setParameter(BSSQueryConstants.EFF_DATE_STR, this.effdtStr );
		DaoUtils.executeUpdate(query1, UPDATE_PGM_ELIG_RULE);

		// new client setup, also update the plan start date instance
		if( ! company.isRenewalCompany() ) {
			query1.setParameter(BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
			DaoUtils.executeUpdate(query1, UPDATE_PGM_ELIG_RULE);
		}
	}
}
