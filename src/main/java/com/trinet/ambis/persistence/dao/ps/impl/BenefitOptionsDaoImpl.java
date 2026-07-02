package com.trinet.ambis.persistence.dao.ps.impl;

import java.text.DecimalFormat;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.trinet.ambis.enums.RiskTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.persistence.dao.ps.BenefitOptionsDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.DaoUtils;

public class BenefitOptionsDaoImpl implements BenefitOptionsDao {

	private static final Logger logger = LoggerFactory.getLogger(BenefitOptionsDaoImpl.class);

	private EntityManager em;

	private static final String LOG_DELETED_ROWS_TEXT = "{} rows deleted.";

	@Override
	public void setEntityManager( EntityManager em ) {
		this.em = em;
	}


	public int insertOptnEfdt( Company company ) {

		// insert (merge) a row into the benefit options parent record
		Query query = em.createNamedQuery("INSERT_OPTN_EFDT");
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );

		return DaoUtils.executeUpdate(query, "INSERT_OPTN_EFDT");
	}


	public int insertOptn2( Company company, BenefitGroup group, String lifeBandCode, String disBandCode ) {

		String sqlName = "INSERT_OPTN2";
		RiskTypeEnum riskType = company.getRiskType() != null ? company.getRiskType() : RiskTypeEnum.BANDS;
		validateDifferentialsBandCodes( riskType, lifeBandCode, disBandCode, sqlName );

		// Copy a row from a prior row for the same company and ben prog.
		// A row will not be inserted if one is already present for the combination
		// of company, effdt, and benefit program.
		Query query = em.createNamedQuery( sqlName );
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.RISK_TYPE, riskType.name() );
		query.setParameter( BSSQueryConstants.LIFE_BAND_CODE, lifeBandCode );
		query.setParameter( BSSQueryConstants.DIS_BAND_CODE, disBandCode );
		if ( RiskTypeEnum.DIFFERENTIALS == riskType ) {
			logger.info( "INSERT_OPTN2 applying DIFFERENTIALS band codes: lifeBandCode={}, disBandCode={}, company={}, benefitProgram={}",
					lifeBandCode, disBandCode, company.getCode(), group.getBenefitProgram() );
		}

		return DaoUtils.executeUpdate( query, sqlName );
	}


	public int populateHsaParameters( Company company, BenefitGroup group, StrategyHsaFundingDto hsa ) {

		String sqlName = "UPDATE_OPTN2_HSA";
		// update the Client Benefit Options with the HSA funding selections
		Query query = em.createNamedQuery( sqlName );

		if( null == hsa ) {
			// if no funding passed, create a dummy HSA object
			hsa = new StrategyHsaFundingDto();
		}

		// keys
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.PF_CLIENT, company.getPfClient() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );

		// update data elements
		query.setParameter( BSSQueryConstants.ER_HSA_LVL, CommonUtils.validateParameter( hsa.getOptionId() ).toString() );
		query.setParameter( BSSQueryConstants.HSA_CNTB_FRQ, CommonUtils.validateParameter( hsa.getContributionFrequency() ) );
		query.setParameter( BSSQueryConstants.ER_HSA_MON_EE, CommonUtils.validateParameter( hsa.getMonthlyEeAmount() ) );
		query.setParameter( BSSQueryConstants.ER_HSA_MON_FAM, CommonUtils.validateParameter( hsa.getMonthlyFamilyAmount() ) );

		query.setParameter( BSSQueryConstants.HSA_FRNT_FRQ, CommonUtils.validateParameter( hsa.getLumpSumFrequency() ) );
		if( null != hsa.getLumpSumFrequency() && BSSApplicationConstants.HSA_QUARTERLY.equals( hsa.getLumpSumFrequency() ) ) {
			query.setParameter( BSSQueryConstants.ER_HSA_FRT_EE, CommonUtils.validateParameter( hsa.getQuarterlyEeAmount() ) );
			query.setParameter( BSSQueryConstants.ER_HSA_FRT_FAM, CommonUtils.validateParameter( hsa.getQuarterlyFamilyAmount() ) );
		} else {
			query.setParameter( BSSQueryConstants.ER_HSA_FRT_EE, CommonUtils.validateParameter( hsa.getAnnualEeAmount() ) );
			query.setParameter( BSSQueryConstants.ER_HSA_FRT_FAM, CommonUtils.validateParameter( hsa.getAnnualFamilyAmount() ) );
		}

		// define a format that preserves the leading zero
		DecimalFormat format = new DecimalFormat( "00" );
		query.setParameter( BSSQueryConstants.HSA_FRT_PAYOUT, CommonUtils.formatMonth( hsa.getAnnualMonth(), format ) );
		query.setParameter( BSSQueryConstants.HSA_FRTMNTH_Q1, CommonUtils.formatMonth( hsa.getQ1Month(), format ) );
		query.setParameter( BSSQueryConstants.HSA_FRTMNTH_Q2, CommonUtils.formatMonth( hsa.getQ2Month(), format ) );
		query.setParameter( BSSQueryConstants.HSA_FRTMNTH_Q3, CommonUtils.formatMonth( hsa.getQ3Month(), format ) );
		query.setParameter( BSSQueryConstants.HSA_FRTMNTH_Q4, CommonUtils.formatMonth( hsa.getQ4Month(), format ) );

		DaoUtils.displayParameters( query, sqlName );

		return query.executeUpdate();
	}


	public int insertOpt2A( Company company, BenefitGroup group ) {

		// Copy a row from a prior row for the same company and ben prog.
	    // A row will not be inserted if one is already present for the combination
	    // of company, effdt, and benefit program.
		Query query = em.createNamedQuery("INSERT_OPT2A");
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );

		return DaoUtils.executeUpdate(query, "INSERT_OPT2A");
	}


	public int insertOpt2AFromBenProg( Company company, BenefitGroup group ) {

		// Copy a row from a prior row for the same company using any ben prog the
		// query can find.
	    // A row will not be inserted if one is already present for the combination
	    // of company, effdt, and benefit program.
		String sqlName = "INSERT_OPT2A_FROM_BENPROG";
		Query query = em.createNamedQuery( sqlName );
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );

		return DaoUtils.executeUpdate(query, sqlName);
	}


	public int insertOpt2ASkeleton( Company company, BenefitGroup group ) {

		// Insert a row using initial values.  The true values will be updated later in the process.
	    // A row will not be inserted if one is already present for the combination
	    // of company, effdt, and benefit program.
		Query query = em.createNamedQuery("INSERT_OPT2A_FROM_SKELETON");
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.PF_CLIENT, company.getPfClient() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );

		return DaoUtils.executeUpdate(query, "INSERT_OPT2A_FROM_SKELETON");
	}


	public int setDefaultBenProg( Company company, BenefitGroup group ) {

		// Copy a row from a prior row for the same company and ben prog.
	    // A row will not be inserted if one is already present for the combination
	    // of company, effdt, and benefit program.
		Query query = em.createNamedQuery("SET_DEFAULT_BENEFIT_PROGRAM");
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );

		return DaoUtils.executeUpdate(query, "SET_DEFAULT_BENEFIT_PROGRAM");
	}


	public int insertOptn3( Company company, BenefitGroup group, String lifeBandCode, String disBandCode ) {

		// Copy a row from a prior row for the same company and ben prog.
	    // A row will not be inserted if one is already present for the combination
	    // of company, effdt, and benefit program.
		RiskTypeEnum riskType = company.getRiskType() != null ? company.getRiskType() : RiskTypeEnum.BANDS;
		validateDifferentialsBandCodes(riskType, lifeBandCode, disBandCode, "INSERT_OPTN3");
		Query query = em.createNamedQuery("INSERT_OPTN3");
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.RISK_TYPE, riskType.name());
		query.setParameter( BSSQueryConstants.LIFE_BAND_CODE, lifeBandCode );
		query.setParameter( BSSQueryConstants.DIS_BAND_CODE, disBandCode );
		if ( RiskTypeEnum.DIFFERENTIALS == riskType ) {
			logger.info( "INSERT_OPTN3 applying DIFFERENTIALS band codes: lifeBandCode={}, disBandCode={}, company={}, benefitProgram={}",
					lifeBandCode, disBandCode, company.getCode(), group.getBenefitProgram() );
		}

		return DaoUtils.executeUpdate(query, "INSERT_OPTN3");
	}


	public int insertOptn3FromBenProg( Company company, BenefitGroup group, String lifeBandCode, String disBandCode ) {

		// Copy a row from a prior row for the same company and ben prog.
	    // A row will not be inserted if one is already present for the combination
	    // of company, effdt, and benefit program.
		RiskTypeEnum riskType = company.getRiskType() != null ? company.getRiskType() : RiskTypeEnum.BANDS;
		validateDifferentialsBandCodes(riskType, lifeBandCode, disBandCode, "INSERT_OPTN3_FROM_BENPROG");

		Query query = em.createNamedQuery("INSERT_OPTN3_FROM_BENPROG");
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.RISK_TYPE, riskType.name());
		query.setParameter( BSSQueryConstants.LIFE_BAND_CODE, lifeBandCode );
		query.setParameter( BSSQueryConstants.DIS_BAND_CODE, disBandCode );
		if ( RiskTypeEnum.DIFFERENTIALS == riskType ) {
			logger.info( "INSERT_OPTN3_FROM_BENPROG applying DIFFERENTIALS band codes: lifeBandCode={}, disBandCode={}, company={}, benefitProgram={}",
					lifeBandCode, disBandCode, company.getCode(), group.getBenefitProgram() );
		}

		return DaoUtils.executeUpdate(query, "INSERT_OPTN3_FROM_BENPROG");
	}


	public int insertOptn3FromClone( Company company, BenefitGroup group, String cloneProgram, String cloneCompany, String lifeBandCode, String disBandCode ) {

		// Copy a row from the clone company and clone benefit program.
	    // A row will not be inserted if one is already present for the combination
	    // of company, effdt, and benefit program.
		RiskTypeEnum riskType = company.getRiskType() != null ? company.getRiskType() : RiskTypeEnum.BANDS;
		validateDifferentialsBandCodes(riskType, lifeBandCode, disBandCode, "INSERT_OPTN3_FROM_CLONE");

		Query query = em.createNamedQuery("INSERT_OPTN3_FROM_CLONE");
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.CLONE_COMPANY, cloneCompany );
		query.setParameter( BSSQueryConstants.CLONE_PROGRAM, cloneProgram );
		query.setParameter( BSSQueryConstants.RISK_TYPE, riskType.name());
		query.setParameter( BSSQueryConstants.LIFE_BAND_CODE, lifeBandCode );
		query.setParameter( BSSQueryConstants.DIS_BAND_CODE, disBandCode );
		if ( RiskTypeEnum.DIFFERENTIALS == riskType ) {
			logger.info( "INSERT_OPTN3_FROM_CLONE applying DIFFERENTIALS band codes: lifeBandCode={}, disBandCode={}, company={}, benefitProgram={}",
					lifeBandCode, disBandCode, company.getCode(), group.getBenefitProgram() );
		}

		return DaoUtils.executeUpdate(query, "INSERT_OPTN3_FROM_CLONE");
	}


	public int resetOptn3Bands( Company company, BenefitGroup group, String lifeBandCode, String disBandCode ) {
		RiskTypeEnum riskType = company.getRiskType() != null ? company.getRiskType() : RiskTypeEnum.BANDS;
		validateDifferentialsBandCodes(riskType, lifeBandCode, disBandCode, "RESET_OPTN3_BANDS");
		// Set the correct band codes for each plan in CLIENT_OPTN3
		Query query = em.createNamedQuery("RESET_OPTN3_BANDS");
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		query.setParameter( BSSQueryConstants.REALM_YEAR_ID, company.getRealmPlanYearId() );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.RISK_TYPE, riskType.name());
		query.setParameter( BSSQueryConstants.LIFE_BAND_CODE, lifeBandCode );
		query.setParameter( BSSQueryConstants.DIS_BAND_CODE, disBandCode );
		if ( RiskTypeEnum.DIFFERENTIALS == riskType ) {
			logger.info( "RESET_OPTN3_BANDS applying DIFFERENTIALS band codes: lifeBandCode={}, disBandCode={}, company={}, benefitProgram={}",
					lifeBandCode, disBandCode, company.getCode(), group.getBenefitProgram() );
		}

		return DaoUtils.executeUpdate(query, "RESET_OPTN3_BANDS");
	}


	/**
	 * Validates that life and disability band codes are present when the risk type is DIFFERENTIALS.
	 * Throws {@link BSSApplicationException} if either code is null or blank for a DIFFERENTIALS client.
	 *
	 * @param riskType     the effective risk type for this company
	 * @param lifeBandCode life insurance band code
	 * @param disBandCode  disability band code
	 * @param queryName    the calling query name, included in the error for traceability
	 */
	private void validateDifferentialsBandCodes( RiskTypeEnum riskType, String lifeBandCode, String disBandCode, String queryName ) {
		if ( RiskTypeEnum.DIFFERENTIALS != riskType ) {
			return;
		}
		if ( lifeBandCode == null || lifeBandCode.trim().isEmpty() ) {
			BSSApplicationError errorData = new BSSApplicationError(
					BSSErrorResponseCodes.BSS_MISSING_DIFFERENTIALS_BAND_CODES,
					BSSHttpStatusConstants.INTERNAL_SERVER_ERROR,
					this.getClass().getName(),
					"Life band code is required for DIFFERENTIALS clients but was not provided.",
					queryName, null );
			throw new BSSApplicationException( errorData );
		}
		if ( disBandCode == null || disBandCode.trim().isEmpty() ) {
			BSSApplicationError errorData = new BSSApplicationError(
					BSSErrorResponseCodes.BSS_MISSING_DIFFERENTIALS_BAND_CODES,
					BSSHttpStatusConstants.INTERNAL_SERVER_ERROR,
					this.getClass().getName(),
					"Disability band code is required for DIFFERENTIALS clients but was not provided.",
					queryName, null );
			throw new BSSApplicationException( errorData );
		}
	}


	public void deleteFutureEfdt(Company company, BenefitGroup group) {
		// delete the rows from PS_T2_CL_OPTN_EFDT for this company, and FUTURE to plan start date
		// The SQL statement should be careful not to delete this record if child records are still
		// present.  We don't want to create orphan records.
		Query query = em.createNamedQuery("DELETE_FUTURE_EFDT");
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.PF_CLIENT, company.getPfClient() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		int num = DaoUtils.executeUpdate(query, "DELETE_FUTURE_EFDT");
		logger.info( LOG_DELETED_ROWS_TEXT, num );
	}

	public void deleteFutureOptn2(Company company, BenefitGroup group) {
		// delete the rows from PS_T2_CLIENT_OPTN2 for this company, benefit program and FUTURE to plan start date
		Query query = em.createNamedQuery("DELETE_FUTURE_OPTN2");
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.PF_CLIENT, company.getPfClient() );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		int num = DaoUtils.executeUpdate(query, "DELETE_FUTURE_OPTN2");
		logger.info( LOG_DELETED_ROWS_TEXT, num );
	}

	public void deleteFutureOpt2A(Company company, BenefitGroup group) {
		// delete the rows from PS_T2_CLIENT_OPT2A for this company, benefit program and FUTURE to plan start date
		Query query = em.createNamedQuery("DELETE_FUTURE_OPT2A");
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.PF_CLIENT, company.getPfClient() );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		int num = DaoUtils.executeUpdate(query, "DELETE_FUTURE_OPT2A");
		logger.info( LOG_DELETED_ROWS_TEXT, num );
	}

	public void deleteFutureOptn3(Company company, BenefitGroup group) {
		// delete the rows from PS_T2_CLIENT_OPTN3 for this company, benefit program and FUTURE to plan start date
		Query query = em.createNamedQuery("DELETE_FUTURE_OPTN3");
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.PF_CLIENT, company.getPfClient() );
		query.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		int num = DaoUtils.executeUpdate(query, "DELETE_FUTURE_OPTN3");
		logger.info( LOG_DELETED_ROWS_TEXT, num );
	}


	public String getCloneCompany( Company company ) {
		String sqlName = "GET_CLONE_COMPANY";
		Query query = em.createNamedQuery(sqlName);
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		Map<String, Object> queryMap = DaoUtils.generateQueryMap(query);

		String cloneCompany;
		try {
			cloneCompany = (String) DaoUtils.getSingleResult(query, sqlName);
		} catch( NoResultException nre ) {
			BSSApplicationError errorData = new BSSApplicationError( BSSErrorResponseCodes.BSS_NO_CLONE_COMPANY
					, BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, this.getClass().getName()
					, "No clone company found for this transaction. Check Exchange/PF Corp/PEO Id are valid.", sqlName, queryMap );
			throw new BSSApplicationException(nre, errorData);
		}
		return cloneCompany; 
	}

}
