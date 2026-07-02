package com.trinet.ambis.persistence.dao.ps;

import java.util.Set;

import javax.persistence.EntityManager;

import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;

public interface BenefitProgramDao {

	/**
	 * Explicitly set the EntityManager for this object
	 * @param em
	 */
	public void setEntityManager( EntityManager em );

	/**
	 * Delete any current PS_BEN_DEFN_PGM row for this benefit program and company plan start date.
	 * @param company
	 * @param group
	 */
	public void deleteBenDefnPgm( Company company, BenefitGroup group );

	/**
	 * Insert a new PS_BEN_DEFN_PGM row using the clone program for this benefit program and
	 * company plan start date.
	 * @param company
	 * @param group
	 * @param cloneProgram
	 */
	public void insertBenDefnPgm( Company company, BenefitGroup group, String cloneProgram );

	/**
	 * Delete rows from PS_BEN_DEFN_PLAN for this benefit program, company plan start date, and list of plan types.
	 * @param company
	 * @param group
	 * @param planTypes
	 */
	public void deleteBenDefnPlan( Company company, BenefitGroup group, Set<String> planTypes );

	/**
	 * Insert new PS_BEN_DEFN_PLAN rows using the clone program for this benefit program,
	 * company plan start date, and list of plan types.
	 * @param company
	 * @param group
	 * @param cloneProgram
	 * @param planTypes
	 */
	public void insertBenDefnPlan( Company company, BenefitGroup group, String cloneProgram, Set<String> planTypes );

	/**
	 * Delete rows from PS_BEN_DEFN_OPTN for this benefit program, company plan start date, and list of plan types.
	 * @param company
	 * @param group
	 * @param planTypes
	 */
	public void deleteBenDefnOptn( Company company, BenefitGroup group, Set<String> planTypes );

	/**
	 * Insert new PS_BEN_DEFN_OPTN rows using the clone program for this benefit program,
	 * company plan start date, and list of plan types.
	 * @param company
	 * @param group
	 * @param cloneProgram
	 * @param planTypes
	 */
	public void insertBenDefnOptn( Company company, BenefitGroup group, String cloneProgram, Set<String> planTypes );

	/**
	 * Delete rows from PS_BEN_DEFN_COST for this benefit program, company plan start date, and list of plan types.
	 * @param company
	 * @param group
	 * @param planTypes
	 */
	public void deleteBenDefnCost( Company company, BenefitGroup group, Set<String> planTypes );

	/**
	 * Insert new PS_BEN_DEFN_COST rows using the clone program for this benefit program,
	 * company plan start date, and list of plan types.
	 * @param company
	 * @param group
	 * @param cloneProgram
	 * @param planTypes
	 */
	public void insertBenDefnCost( Company company, BenefitGroup group, String cloneProgram, Set<String> planTypes );




	/**
	 * Delete all rows from PS_BEN_DEFN_PLAN for this benefit program and company plan start date.
	 * @param company
	 * @param group
	 */
	public void deleteAllBenDefnPlan( Company company, BenefitGroup group );

	/**
	 * Insert new PS_BEN_DEFN_PLAN rows using the clone program for this benefit program and company plan start date.
	 * @param company
	 * @param group
	 * @param cloneProgram
	 */
	public void insertAllBenDefnPlan( Company company, BenefitGroup group, String cloneProgram );

	/**
	 * Delete all rows from PS_BEN_DEFN_OPTN for this benefit program and company plan start date.
	 * @param company
	 * @param group
	 */
	public void deleteAllBenDefnOptn( Company company, BenefitGroup group );

	/**
	 * Insert new PS_BEN_DEFN_OPTN rows using the clone program for this benefit program and company plan start date.
	 * @param company
	 * @param group
	 * @param cloneProgram
	 */
	public void insertAllBenDefnOptn( Company company, BenefitGroup group, String cloneProgram );

	/**
	 * Delete all rows from PS_BEN_DEFN_COST for this benefit program, company plan start date.
	 * @param company
	 * @param group
	 */
	public void deleteAllBenDefnCost( Company company, BenefitGroup group );

	/**
	 * Insert new PS_BEN_DEFN_COST rows using the clone program for this benefit program and company plan start date.
	 * @param company
	 * @param group
	 * @param cloneProgram
	 */
	public void insertAllBenDefnCost( Company company, BenefitGroup group, String cloneProgram );


	
	/**********  delete future-dated rows  **********/
	
	/**
	 * Delete PS_BEN_DEFN_PGM rows for this benefit program where effective date is future relative to
	 * company plan start date.
	 * @param company
	 * @param group
	 */
	public void deleteFuturePgm( Company company, BenefitGroup group );

	/**
	 * Delete PS_BEN_DEFN_PLAN rows for this benefit program where effective date is future relative to
	 * company plan start date.
	 * @param company
	 * @param group
	 */
	public void deleteFuturePlan( Company company, BenefitGroup group );

	/**
	 * Delete PS_BEN_DEFN_OPTN rows for this benefit program where effective date is future relative to
	 * company plan start date.
	 * @param company
	 * @param group
	 */
	public void deleteFutureOptn( Company company, BenefitGroup group );

	/**
	 * Delete PS_BEN_DEFN_COST rows for this benefit program where effective date is future relative to
	 * company plan start date.
	 * @param company
	 * @param group
	 */
	public void deleteFutureCost( Company company, BenefitGroup group );


	
}
