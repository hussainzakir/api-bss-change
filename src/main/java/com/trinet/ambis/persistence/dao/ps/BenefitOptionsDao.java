package com.trinet.ambis.persistence.dao.ps;

import javax.persistence.EntityManager;

import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;

public interface BenefitOptionsDao {

	public void setEntityManager( EntityManager em );

	/**
	 * Ensure there is a benefit options parent record for this combination of keys.
	 * @param company
	 * @return the number of rows inserted
	 */
	public int insertOptnEfdt( Company company );


	////////////////////// PS_T2_CLIENT_OPTN2

	/**
	 * Insert a new CLIENT_OPTN2 record by copying from the prior effective date for the
	 * same combination of company and benefit program.
	 * Do not insert a row if one already exists.
	 * <p>For DIFFERENTIALS clients, {@code lifeBandCode} and {@code disBandCode} override the
	 * copied {@code T2_LIFE_BAND_CD} and {@code T2_DIS_BAND_CD} columns.
	 * Both must be non-null/non-blank for DIFFERENTIALS clients.</p>
	 * @param company      the target company
	 * @param group        the benefit group
	 * @param lifeBandCode life insurance band code (required for DIFFERENTIALS clients, ignored otherwise)
	 * @param disBandCode  disability band code (required for DIFFERENTIALS clients, ignored otherwise)
	 * @return the number of rows inserted
	 */
	public int insertOptn2( Company company, BenefitGroup group, String lifeBandCode, String disBandCode );


	/**
	 * Call this method with an HSA strategy object to update the Client Benefit Options (CLIENT_OPTN2)
	 * with the new HSA funding strategy
	 * @param company the Company to update.  The planStartDate from this object will be the effective
	 * date of this update action.
	 * @param group the BenefitGroup to update.  HSA funding is by company but the options in PeopleSoft
	 * are by benefit program, so both keys are required.
	 * @param hsa the HSA options chosen by the client
	 * @return the number of rows updated
	 */
	public int populateHsaParameters( Company company, BenefitGroup group, StrategyHsaFundingDto hsa );


	////////////////////// PS_T2_CLIENT_OPT2A

	/**
	 * Insert a new CLIENT_OPT2A record by copying from the prior effective date for the
	 * same combination of company and benefit program.
	 * Do not insert a row if one already exists
	 * @param company
	 * @param group
	 * @return the number of rows inserted
	 */
	public int insertOpt2A( Company company, BenefitGroup group );

	/**
	 * Insert a new CLIENT_OPT2A record by copying from the current effective row for
	 * the same company and some other benefit program.  This increases the likelihood
	 * of keeping accurate options.  The default benefit program flag is set to "N"
	 * Do not insert a row if one already exists
	 * @param company
	 * @param group
	 * @return the number of rows inserted
	 */
	public int insertOpt2AFromBenProg( Company company, BenefitGroup group );

	/**
	 * Create a new CLIENT_OPT2A record by inserting skeleton values.
	 * The true values will be set later during the submit process.
	 * Do not insert a row if one already exists
	 * @param company
	 * @param group
	 * @return the number of rows inserted
	 */
	public int insertOpt2ASkeleton( Company company, BenefitGroup group );


	////////////////////// PS_T2_CLIENT_OPTN3

	/**
	 * Insert new CLIENT_OPTN3 records by copying from the prior effective date for the
	 * same combination of company and benefit program.
	 * Do not insert a row if it already exists.
	 * <p>For DIFFERENTIALS clients, {@code lifeBandCode} and {@code disBandCode} are applied
	 * to plan types LIKE '2%' and LIKE '3%' respectively, overriding the source row values.
	 * Both must be non-null/non-blank for DIFFERENTIALS clients.</p>
	 * @param company      the target company
	 * @param group        the benefit group
	 * @param lifeBandCode life insurance band code (required for DIFFERENTIALS clients)
	 * @param disBandCode  disability band code (required for DIFFERENTIALS clients)
	 * @return the number of rows inserted
	 */
	public int insertOptn3( Company company, BenefitGroup group, String lifeBandCode, String disBandCode );

	/**
	 * Insert new CLIENT_OPTN3 records by copying from the current effective row for
	 * the same company and some other benefit program.  This increases the likelihood
	 * of keeping accurate options.
	 * Do not insert a row if it already exists.
	 * <p>For DIFFERENTIALS clients, {@code lifeBandCode} and {@code disBandCode} are applied
	 * to plan types LIKE '2%' and LIKE '3%' respectively.
	 * Both must be non-null/non-blank for DIFFERENTIALS clients.</p>
	 * @param company      the target company
	 * @param group        the benefit group
	 * @param lifeBandCode life insurance band code (required for DIFFERENTIALS clients)
	 * @param disBandCode  disability band code (required for DIFFERENTIALS clients)
	 * @return the number of rows inserted
	 */
	public int insertOptn3FromBenProg( Company company, BenefitGroup group, String lifeBandCode, String disBandCode );

	/**
	 * Insert new CLIENT_OPTN3 records by copying from the clone company and clone benefit program.
	 * This is a last resort and may not have valid options.
	 * Do not insert a row if one already exists.
	 * <p>For DIFFERENTIALS clients, {@code lifeBandCode} and {@code disBandCode} override the
	 * source clone rows' band codes for plan types LIKE '2%' and LIKE '3%' respectively.
	 * Both must be non-null/non-blank for DIFFERENTIALS clients.</p>
	 * @param company      the target company
	 * @param group        the benefit group
	 * @param cloneProgram the benefit program to clone from
	 * @param cloneCompany the company to clone from
	 * @param lifeBandCode life insurance band code (required for DIFFERENTIALS clients)
	 * @param disBandCode  disability band code (required for DIFFERENTIALS clients)
	 * @return the number of rows inserted
	 */
	public int insertOptn3FromClone( Company company, BenefitGroup group, String cloneProgram, String cloneCompany, String lifeBandCode, String disBandCode );

	/**
	 * Analyze the plans in CLIENT_OPTN3 and apply the correct band code to each row.
	 * <p>For DIFFERENTIALS clients, {@code lifeBandCode} is applied to plan types LIKE '2%'
	 * and {@code disBandCode} is applied to plan types LIKE '3%', bypassing the carrier-specific
	 * lookup from OPTN2. Both must be non-null/non-blank for DIFFERENTIALS clients.</p>
	 * @param company      the target company
	 * @param group        the benefit group
	 * @param lifeBandCode life insurance band code (required for DIFFERENTIALS clients)
	 * @param disBandCode  disability band code (required for DIFFERENTIALS clients)
	 * @return the number of rows updated
	 */
	public int resetOptn3Bands( Company company, BenefitGroup group, String lifeBandCode, String disBandCode );

	
	
	////////////////////// delete future-dated rows
	
	/**
	 * Delete PS_T2_CL_OPTN_EFDT rows for this client where effective date is future relative to
	 * company plan start date and there are no child rows for the effective date.
	 * @param company
	 * @param group
	 */
	public void deleteFutureEfdt( Company company, BenefitGroup group );

	/**
	 * Delete PS_T2_CLIENT_OPTN2 rows for this client and benefit program where effective date is 
	 * future relative to company plan start date.
	 * @param company
	 * @param group
	 */
	public void deleteFutureOptn2( Company company, BenefitGroup group );

	/**
	 * Delete PS_T2_CLIENT_OPT2A rows for this client and benefit program where effective date is 
	 * future relative to company plan start date.
	 * @param company
	 * @param group
	 */
	public void deleteFutureOpt2A( Company company, BenefitGroup group );

	/**
	 * Delete PS_T2_CLIENT_OPTN3 rows for this client and benefit program where effective date is 
	 * future relative to company plan start date.
	 * @param company
	 * @param group
	 */
	public void deleteFutureOptn3( Company company, BenefitGroup group );


	////////////////////// other tools

	/**
	 * Set the flag indicating this is the default benefit program.
	 * @param company
	 * @param group
	 * @return the number of rows inserted
	 */
	public int setDefaultBenProg( Company company, BenefitGroup group );

	/**
	 * Get the clone company from PeopleSoft for this company.  This is the company
	 * code from which other setup data can be copied.
	 * @param company
	 * @return the PeopleSoft clone company
	 */
	public String getCloneCompany( Company company );

}
