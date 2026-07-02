package com.trinet.ambis.persistence.dao.ps.impl;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.enums.ExcessOptionEnum;
import com.trinet.ambis.persistence.dao.ps.BenefitProgramDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.util.DaoUtils;


public class BenefitProgramDaoImpl implements BenefitProgramDao {

	private static final Logger logger = LoggerFactory.getLogger(BenefitProgramDaoImpl.class);
	private static final String LOG_DELETED_ROWS_TEXT = "{} rows deleted.";

	public static final String CHECK_BENEFIT_PROGRAM_EXISTS = "CHECK_BENEFIT_PROGRAM_EXISTS";
	public static final String INSERT_BEN_DEFN_PGM = "INSERT_BEN_DEFN_PGM";
	public static final String CLEAN_BEN_DEFN_PLAN_PLANTYPES = "CLEAN_BEN_DEFN_PLAN_PLANTYPES";
	public static final String INSERT_BEN_DEFN_PLAN = "INSERT_BEN_DEFN_PLAN";
	public static final String CLEAN_OPTN_PLANTYPES = "CLEAN_OPTN_PLANTYPES";
	public static final String INSERT_BEN_DEFN_OPTN = "INSERT_BEN_DEFN_OPTN";
	public static final String DELETE_BEN_DEFN_COST = "DELETE_BEN_DEFN_COST";
	public static final String INSERT_BEN_DEFN_COST = "INSERT_BEN_DEFN_COST";
	public static final String CLEAN_PLAN_ALL = "CLEAN_PLAN_ALL";
	public static final String CLEAN_OPTN_ALL = "CLEAN_OPTN_ALL";
	public static final String CLEAN_COST_ALL = "CLEAN_COST_ALL";
	public static final String GET_CLONE_PLAN_TYPES = "GET_CLONE_PLAN_TYPES";
	public static final String DELETE_FUTURE_PGM = "DELETE_FUTURE_PGM";
	public static final String DELETE_FUTURE_PLAN = "DELETE_FUTURE_PLAN";
	public static final String DELETE_FUTURE_OPTN = "DELETE_FUTURE_OPTN";
	public static final String DELETE_FUTURE_COST = "DELETE_FUTURE_COST";

	private EntityManager em;

	public BenefitProgramDaoImpl() {
		
	}
	
	@Override
	public void setEntityManager( EntityManager em ) {
		this.em = em;
	}

	public boolean isBenProgInPS(Company company, BenefitGroup group) {

		Query query = em.createNamedQuery( CHECK_BENEFIT_PROGRAM_EXISTS );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		BigDecimal count = (BigDecimal) DaoUtils.getSingleResult(query, CHECK_BENEFIT_PROGRAM_EXISTS);
		logger.debug( "Query returned count : {}", count );
		return (count.intValue() > 0);
	}

	public void deleteBenDefnPgm( Company company, BenefitGroup group ) {
		// not required because the insert step is really a MERGE
	}


	public void insertBenDefnPgm( Company company, BenefitGroup group, String cloneProgram ) {
		// insert (merge) a new row to BEN_DEFN_PGM
		Query query = em.createNamedQuery( INSERT_BEN_DEFN_PGM );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( "groupDescr", group.getName() );
		query.setParameter( BSSQueryConstants.PF_CLIENT, company.getPfClient() );

		// set a default value for excess credit option, in case there are no benefit offers
		query.setParameter( BSSQueryConstants.EXCESS_CREDIT_OPTION, ExcessOptionEnum.CASH.getCode());
		
		// if client chose benefit supplement, apply the selected excess credit option
		for (BenefitOffer benefitOffer : group.getBenefitOffers()) {
			if (benefitOffer.getSummary().getType().equals("medical")
					&& BSSApplicationConstants.BSUPP.equals(benefitOffer.getPlanPackage().getFundingType())) {
				query.setParameter( BSSQueryConstants.EXCESS_CREDIT_OPTION,
						ExcessOptionEnum.getCode(benefitOffer.getPlanPackage().getBsuppExcessOption().intValue()));
				break;   // exit for..loop once this flag is found
			}
		}
		// set short descr to company name for Std groups
		// set short descr to CMP K1 for K1 groups
		if( "K1".equals( group.getType() )) {
			query.setParameter( BSSQueryConstants.SHORT_DESCR, company.getCode().trim() + " K1" );
		} else {
			query.setParameter( BSSQueryConstants.SHORT_DESCR, company.getCode().trim() + " " + group.getName().trim() );
		}

		query.setParameter( BSSQueryConstants.CLONE_BEN_PROGRAM, cloneProgram );
		query.setParameter( BSSQueryConstants.PLAN_START_DATE, company.getPlanStartDate() );

		DaoUtils.executeUpdate(query, INSERT_BEN_DEFN_PGM);
	}


	public void deleteBenDefnPlan( Company company, BenefitGroup group, Set<String> planTypes ) {

		// delete the rows from BEN_DEFN_PLAN for the plan types managed by BSS
		Query query = em.createNamedQuery( CLEAN_BEN_DEFN_PLAN_PLANTYPES );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.PLAN_TYPES, planTypes );

		DaoUtils.executeUpdate(query, CLEAN_BEN_DEFN_PLAN_PLANTYPES);
	}


	public void insertBenDefnPlan( Company company, BenefitGroup group, String cloneProgram, Set<String> planTypes ) {

		// insert new rows from the clone to BEN_DEFN_PLAN for the plan types managed by BSS
		Query query = em.createNamedQuery( INSERT_BEN_DEFN_PLAN );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.CLONE_BEN_PROGRAM, cloneProgram );
		query.setParameter( BSSQueryConstants.PLAN_TYPES, planTypes );
		query.setParameter( BSSQueryConstants.PLAN_START_DATE, company.getPlanStartDate() );

		DaoUtils.executeUpdate(query, INSERT_BEN_DEFN_PLAN);
	}


	public void deleteBenDefnOptn( Company company, BenefitGroup group, Set<String> planTypes ) {

		// delete the rows from BEN_DEFN_OPTN for the plan types managed by BSS
		Query query = em.createNamedQuery( CLEAN_OPTN_PLANTYPES );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.PLAN_TYPES, planTypes );

		DaoUtils.executeUpdate(query, CLEAN_OPTN_PLANTYPES);
	}


	public void insertBenDefnOptn( Company company, BenefitGroup group, String cloneProgram, Set<String> planTypes ) {

		// insert new rows from the clone to BEN_DEFN_OPTN for the plan types managed by BSS
		Query query = em.createNamedQuery( INSERT_BEN_DEFN_OPTN );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.CLONE_BEN_PROGRAM, cloneProgram );
		query.setParameter( BSSQueryConstants.PLAN_TYPES, planTypes );
		query.setParameter( BSSQueryConstants.PLAN_START_DATE, company.getPlanStartDate() );

		DaoUtils.executeUpdate(query, INSERT_BEN_DEFN_OPTN);
	}


	public void deleteBenDefnCost( Company company, BenefitGroup group, Set<String> planTypes ) {

		// delete the rows from BEN_DEFN_COST for the plan types managed by BSS
		Query query = em.createNamedQuery( DELETE_BEN_DEFN_COST );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.PLAN_TYPES, planTypes );

		DaoUtils.executeUpdate(query, DELETE_BEN_DEFN_COST);
	}


	public void insertBenDefnCost( Company company, BenefitGroup group, String cloneProgram, Set<String> planTypes  ) {

		// insert new rows from the clone to BEN_DEFN_COST for the plan types managed by BSS
		Query query = em.createNamedQuery( INSERT_BEN_DEFN_COST );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.CLONE_BEN_PROGRAM, cloneProgram );
		query.setParameter( BSSQueryConstants.PLAN_TYPES, planTypes );
		query.setParameter( BSSQueryConstants.PLAN_START_DATE, company.getPlanStartDate() );

		DaoUtils.executeUpdate(query, INSERT_BEN_DEFN_COST);
	}


	public void deleteAllBenDefnPlan(Company company, BenefitGroup group) {

		// delete the rows from BEN_DEFN_PLAN for this benefit program and plan start date
		Query query = em.createNamedQuery( CLEAN_PLAN_ALL );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );

		DaoUtils.executeUpdate(query, CLEAN_PLAN_ALL);
	}


	public void insertAllBenDefnPlan(Company company, BenefitGroup group, String cloneProgram) {
		// get all the plans types and then go insert them from the clone
		Set<String> planTypes = this.getPlanTypes( cloneProgram, company.getPlanStartDate() );
		this.insertBenDefnPlan( company, group, cloneProgram, planTypes );
	}


	public void deleteAllBenDefnOptn(Company company, BenefitGroup group) {

		// delete the rows from BEN_DEFN_OPTN for this benefit program and plan start date
		Query query = em.createNamedQuery( CLEAN_OPTN_ALL );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );

		DaoUtils.executeUpdate(query, CLEAN_OPTN_ALL);
	}


	public void insertAllBenDefnOptn(Company company, BenefitGroup group, String cloneProgram) {
		// get all the plans types and then go insert them from the clone
		Set<String> planTypes = this.getPlanTypes( cloneProgram, company.getPlanStartDate() );
		this.insertBenDefnOptn( company, group, cloneProgram, planTypes );
	}


	public void deleteAllBenDefnCost(Company company, BenefitGroup group) {

		// delete the rows from BEN_DEFN_COST for this benefit program and plan start date
		Query query = em.createNamedQuery( CLEAN_COST_ALL );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );

		DaoUtils.executeUpdate(query, CLEAN_COST_ALL);
	}


	public void insertAllBenDefnCost(Company company, BenefitGroup group, String cloneProgram) {
		// get all the plans types and then go insert them from the clone
		Set<String> planTypes = this.getPlanTypes( cloneProgram, company.getPlanStartDate() );
		this.insertBenDefnCost( company, group, cloneProgram, planTypes );
	}


	private Set<String> getPlanTypes( String cloneBenProg, String effdtStr ) {

		Query planTypeQuery = em.createNamedQuery( GET_CLONE_PLAN_TYPES );
		planTypeQuery.setParameter( "cloneBenefitProgram", cloneBenProg );
		planTypeQuery.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtStr );
		
		return new HashSet<>(DaoUtils.getResultStringList(planTypeQuery, GET_CLONE_PLAN_TYPES));
	}


	public void deleteFuturePgm(Company company, BenefitGroup group) {
		// delete the rows from BEN_DEFN_PGM for this benefit program and FUTURE to plan start date
		Query query = em.createNamedQuery( DELETE_FUTURE_PGM );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );

		int num = DaoUtils.executeUpdate(query, DELETE_FUTURE_PGM);
		logger.info( LOG_DELETED_ROWS_TEXT, num );
	}


	public void deleteFuturePlan(Company company, BenefitGroup group) {
		// delete the rows from BEN_DEFN_PLAN for this benefit program and FUTURE to plan start date
		Query query = em.createNamedQuery( DELETE_FUTURE_PLAN );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		
		int num = DaoUtils.executeUpdate(query, DELETE_FUTURE_PLAN);
		logger.info( LOG_DELETED_ROWS_TEXT, num );
	}


	public void deleteFutureOptn(Company company, BenefitGroup group) {
		// delete the rows from BEN_DEFN_OPTN for this benefit program and FUTURE to plan start date
		Query query = em.createNamedQuery( DELETE_FUTURE_OPTN );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );

		int num = DaoUtils.executeUpdate(query, DELETE_FUTURE_OPTN);
		logger.info( LOG_DELETED_ROWS_TEXT, num );
	}


	public void deleteFutureCost(Company company, BenefitGroup group) {
		// delete the rows from BEN_DEFN_COST for this benefit program and FUTURE to plan start date
		Query query = em.createNamedQuery( DELETE_FUTURE_COST );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );

		int num = DaoUtils.executeUpdate(query, DELETE_FUTURE_COST);
		logger.info( LOG_DELETED_ROWS_TEXT, num );
	}

}
