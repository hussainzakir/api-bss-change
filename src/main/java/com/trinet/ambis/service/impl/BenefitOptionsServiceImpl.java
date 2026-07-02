package com.trinet.ambis.service.impl;

import javax.persistence.EntityManager;

import com.trinet.ambis.enums.RiskTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.ps.BenefitOptionsDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmCloneProgram;
import com.trinet.ambis.service.BenefitOptionsService;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;
import com.trinet.ambis.util.ApplicationContextProvider;

public class BenefitOptionsServiceImpl implements BenefitOptionsService {

	private static final Logger logger = LoggerFactory.getLogger(BenefitOptionsServiceImpl.class);

	private BenefitOptionsDao benefitOptionsDao;
	private RealmDataDao  realmDataDao;
	private EntityManager psEm;

	private void rewireContext() {
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		if (context != null) {
			realmDataDao = (RealmDataDao) context.getBean("realmDataDao");
			benefitOptionsDao = (BenefitOptionsDao) context.getBean("benefitOptionsDao");
			benefitOptionsDao.setEntityManager(psEm);
		}
	}

	public BenefitOptionsServiceImpl(EntityManager psEm) {
		this.psEm = psEm;
		this.rewireContext();
	}

	/**
	 * When a client is granted an exception and is not offering medical plans, HSA plans are also not available.
	 * Clear all HSA option parameters so that conflicting information is not displayed in PeopleSoft.
	 * @param company
	 * @param group
	 */
	public void clearHSAOptions( Company company, BenefitGroup group ) {
		int count = benefitOptionsDao.populateHsaParameters( company, group, null );
	}

	public void createClientBenefitOptions( Company company, BenefitGroup group, StrategyHsaFundingDto hsaOptions ) {

		// determine the clone benefit program
		RealmCloneProgram realmClonePgm = realmDataDao.getRealmCloneProgram( company.getRealmPlanYear().getId() );
		String cloneProgram;
		// K1 clones are not available for Client Benefit Options so we always use the standard clone here
		cloneProgram = realmClonePgm.getCloneProgram();

		// determine the PeopleSoft clone company
		String cloneCompany = benefitOptionsDao.getCloneCompany( company );

		logger.info( "Clone company:" + cloneCompany );
		logger.info( "Clone program:" + cloneProgram );

		benefitOptionsDao.insertOptnEfdt( company );

		{	// insert CLIENT_OPTN2 records
			// If OPTN2 rows could not be created, data must be corrected before submit can resume
			String lifeBandCode = company.getBandCodes().getLifeBandCode();
			String disBandCode  = company.getBandCodes().getDisBandCode();
			int count = benefitOptionsDao.insertOptn2( company, group, lifeBandCode, disBandCode );

			if( count == 0 ) {
				throw new BSSApplicationException(
						new BSSApplicationError( "ERR_BSS_CREATE_OPTN2", BSSHttpStatusConstants.INTERNAL_SERVER_ERROR,
								this.getClass().getName(),
								"Could not create Client Benefit Options; please review PS_T2_CLIENT_OPTN2 and consult engineering if needed.",
								"", null ) );
			}

			// after OPTN2 rows were created, update the HSA funding options
			count = benefitOptionsDao.populateHsaParameters( company, group, hsaOptions );
		}



		{   // insert CLIENT_OPT2A records until some rows have been inserted
			int count = benefitOptionsDao.insertOpt2A( company, group );

			if( count == 0 ) {
				count = benefitOptionsDao.insertOpt2AFromBenProg( company, group );
			}

			if( count == 0 ) {
				count = benefitOptionsDao.insertOpt2ASkeleton( company, group );
			}
		}

		{ // insert CLIENT_OPTN3 records until some rows have been inserted
			String lifeBandCode = company.getBandCodes().getLifeBandCode();
			String disBandCode  = company.getBandCodes().getDisBandCode();

			int count = benefitOptionsDao.insertOptn3( company, group, lifeBandCode, disBandCode );

			if( count == 0 ) {
				count = benefitOptionsDao.insertOptn3FromBenProg( company, group, lifeBandCode, disBandCode );
			}

			if( count == 0 ) {
				benefitOptionsDao.insertOptn3FromClone( company, group, cloneProgram, cloneCompany, lifeBandCode, disBandCode );
			}
			benefitOptionsDao.resetOptn3Bands( company, group, lifeBandCode, disBandCode );
		}

		// if this is the default group, set the default flag
		if( group.isDefaultGroup() ) {
			benefitOptionsDao.setDefaultBenProg( company, group );
		}

	}


	// I think we can come up with a better way of creating the OPTN3 records after client benefit selections
	// have been applied to the benefit program.  This method will eventually be used for that creation.
	public void regenerateOptn3(Company company, BenefitGroup group) {
		// not implemented
	}


	public void deleteFutureOptions(Company company, BenefitGroup group) {
		// during new client setup, any future-dated client benefit options should be removed
		if( ! company.isRenewalCompany() ) {
			benefitOptionsDao.deleteFutureOptn2( company, group );
			benefitOptionsDao.deleteFutureOpt2A( company, group );
			benefitOptionsDao.deleteFutureOptn3( company, group );
			benefitOptionsDao.deleteFutureEfdt( company, group );
		}
	}


}
