package com.trinet.ambis.persistence.dao.ps;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.trinet.ambis.service.model.BenDefnOptnHSA;

/**
 * Public interface for HSA plan setup data actions
 * @author mbrothers
 *
 */
public interface HSAPlansDao {

	/**
	 * Pass the PeopleSoft EntityManager currently in-use.  This provides a database connection to 
	 * the SQL actions and makes these actions part of the overall database transaction
	 * @param em
	 */
	public void setEntityManager( EntityManager em );

	/**
	 * Returns a list of objects representing BEN_DEFN_OPTN records.  These are all the HSA
	 * plans that are part of the benefit program as of an effective date.  All plans are 
	 * returned, regardless of eligibility.
	 * @param benefitProgram
	 * @param effdt
	 * @return a List of BEN_DEFN_OPTN records in the form of BenDefnOptnHSA objects
	 */
	public List<BenDefnOptnHSA> getAllHSAPlans( String benefitProgram, java.sql.Date effdt );

	/**
	 * Get a Map of active medical plans to the corresponding eligibility rule.
	 * The rule should be applied to the corresponding custom HSA plan.
	 * Select data for the given benefit program and effective date. 
	 * @param benefitProgram
	 * @param effdt
	 * @return a Map of medical plan to elig rule
	 */
	public Map<String,String> getActiveMedPlans( String benefitProgram, java.sql.Date effdt );

	/**
	 * Update the benefit plan definition.  A row will be inserted/updated for the new 
	 * effective date only if data has changed since the last row.
	 * @param cloneBenefitPlan
	 * @param newBenefitPlan
	 * @param pfClient
	 * @param effdt
	 * @return the number of rows inserted/updated
	 */
	public int updateBenefPlanTable( String cloneBenefitPlan, String newBenefitPlan,
			String pfClient, java.sql.Date effdt );

	/**
	 * Update the FSA benefit plan attributes.  (HSA plan attributes are stored on the 
	 * FSA benefit plan table.<BR>
	 * A row will be inserted/updated for the new effective date only if data has changed
	 * since the last row.
	 * @param cloneBenefitPlan
	 * @param newBenefitPlan
	 * @param effdt
	 * @return the number of rows inserted/updated
	 */
	public int updateFSABenefTable( String cloneBenefitPlan, String newBenefitPlan, java.sql.Date effdt );

	/**
	 * To ensure clean submits and resubmits, this method deletes rows that might have previously
	 * been inserted for this HSA plan but using another medical plan as key.  The method 
	 * <code>insertHSAContribLmt</code> should next be called to replace the rows deleted.
	 * @param newBenefitPlan
	 * @param effdt
	 * @return the number of rows deleted
	 */
	public int deleteHSAContribLmt( String newBenefitPlan, java.sql.Date effdt );

	/**
	 * Inserts the rows which specify the company contribution amounts for an HSA plan.
	 * @param cloneBenefitPlan
	 * @param newBenefitPlan
	 * @param eeContrib
	 * @param famContrib
	 * @param effdt
	 * @return the number of rows inserted
	 */
	public int insertHSAContribLmt( String cloneBenefitPlan, String newBenefitPlan, BigDecimal eeContrib,
			BigDecimal famContrib, java.sql.Date effdt );

	/**
	 * Inserts rows into a table used to calculate the total HSA amount for limit testing.
	 * All HSA plans are in this table for any given effective date.  This method inserts for
	 * the current row (by effective date) and any future-dated rows.  It will insert only
	 * if the rows do not exist.  No update is necessary for matching rows.<BR>
	 * The structure of this table is not sustainable, but redesigning this table and its 
	 * usage is outside the scope of the BSS HSA feature.
	 * @param newBenefitPlan
	 * @param effdt
	 * @return
	 */
	public int updateLimitIncludeTable( String newBenefitPlan, java.sql.Date effdt );

	/**
	 * This method should be used to update the PS_BEN_DEFN_OPTN rows after completing
	 * HSA plan setup and resequencing.  Because of the resequencing, all rows will likely
	 * be changed, so will update all the rows based on each unique key.
	 * @param optns the collection of OPTN rows created by the HSA setup process
	 * @return the total number of rows updated
	 */
	public int updateHSAOptns( List<BenDefnOptnHSA> optns );
	
}
