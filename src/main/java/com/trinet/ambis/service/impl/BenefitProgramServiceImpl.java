package com.trinet.ambis.service.impl;

import java.util.Set;

import javax.persistence.EntityManager;

import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.ps.BenProgInactivateDao;
import com.trinet.ambis.persistence.dao.ps.BenefitProgramDao;
import com.trinet.ambis.persistence.dao.ps.EligConfigDao;
import com.trinet.ambis.persistence.dao.ps.impl.BenProgInactivateDaoImpl;
import com.trinet.ambis.persistence.dao.ps.impl.BenefitProgramDaoImpl;
import com.trinet.ambis.persistence.dao.ps.impl.EligConfigDaoImpl;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmCloneProgram;
import com.trinet.ambis.service.BenefitProgramService;
import com.trinet.ambis.util.ApplicationContextProvider;

public class BenefitProgramServiceImpl implements BenefitProgramService {

	private RealmDataDao realmDataDao;
	private BenefitProgramDao benefitProgramDao;
	private BenProgInactivateDao benProgInactivateDao;
	private EligConfigDao eligConfigDao;
	private EntityManager psEm;
	
	private void rewireContext() {
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		if (context != null) {
			realmDataDao = (RealmDataDao) context.getBean("realmDataDao");
			benProgInactivateDao = new BenProgInactivateDaoImpl();
			eligConfigDao = new EligConfigDaoImpl();
			benefitProgramDao = new BenefitProgramDaoImpl();
			benefitProgramDao.setEntityManager(psEm);
		}
	}

	public BenefitProgramServiceImpl(EntityManager psEm) {
		this.psEm = psEm;
		this.rewireContext();
	}

	// recreate the benefit program rows
	@Override
	public void createBenefitProgram(Company company, BenefitGroup group) {
		/*
		 * After some discussion (March 2018 meeting: Sukhwinder, Aaron A.,
		 * Caller 01 Maria S., MikeBro, Radhika, Raj, Vijendhar) we decided that
		 * the benefit program should be cloned in-full each time. This is
		 * consistent with the way that benefit programs were cloned with the
		 * old Oracle stored procedures.
		 */
		// Use the create-NEW method to clone the entire benefit program
		this.createNewBenefitProgram(company, group);
	}

	@Override
	public void deleteFutureProgram(Company company, BenefitGroup group) {
		// during new client setup, any future-dated benefit program structures
		// should be removed
		if (!company.isRenewalCompany()) {
			benefitProgramDao.deleteFuturePgm(company, group);
			benefitProgramDao.deleteFuturePlan(company, group);
			benefitProgramDao.deleteFutureOptn(company, group);
			benefitProgramDao.deleteFutureCost(company, group);
		}
	}
	
	@Override
	public void deleteBenDefn(Company company, BenefitGroup group, Set<String> nomedNotApplicablePlanTypes) {
		benefitProgramDao.deleteBenDefnPlan(company, group, nomedNotApplicablePlanTypes);
		benefitProgramDao.deleteBenDefnOptn(company, group, nomedNotApplicablePlanTypes);
		benefitProgramDao.deleteBenDefnCost(company, group, nomedNotApplicablePlanTypes);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void updateInactiveBenefitPrograms(Company company, String benefitProgram, String eligConfig1, EntityManager em) {
		benProgInactivateDao.updateClientOption2(company, benefitProgram, em);
		benProgInactivateDao.updateBenefitPrclBenPrgm(company, benefitProgram, em);
		benProgInactivateDao.updateBenDefinitionOptionEligRule(company, benefitProgram, em);
		benProgInactivateDao.updateBenDefinitionPgm(company, benefitProgram, em);
		benProgInactivateDao.deleteClientOpt2a(company, benefitProgram, em);
		eligConfigDao.putEligConfigRow(company.getPfClient(), company.getPlanStartDate(), eligConfig1, "I", "", em);
	}

	/**
	 * Create a whole new benefit program structure for the first time. This
	 * should be used when there is not already a benefit program structure in
	 * PeopleSoft.
	 * 
	 * @param company
	 * @param group
	 */
	private void createNewBenefitProgram(Company company, BenefitGroup group) {
		// determine the clone benefit program
		String cloneProgram = this.determineCloneBenProgram(company, group);

		// call the dao methods to create the benefit program rows
		benefitProgramDao.deleteBenDefnPgm(company, group);
		benefitProgramDao.insertBenDefnPgm(company, group, cloneProgram);
		benefitProgramDao.deleteAllBenDefnPlan(company, group);
		benefitProgramDao.insertAllBenDefnPlan(company, group, cloneProgram);
		benefitProgramDao.deleteAllBenDefnOptn(company, group);
		benefitProgramDao.insertAllBenDefnOptn(company, group, cloneProgram);
		benefitProgramDao.deleteAllBenDefnCost(company, group);
		benefitProgramDao.insertAllBenDefnCost(company, group, cloneProgram);

	}

	/**
	 * Determine the clone benefit program for a given company/group
	 * 
	 * @param company
	 * @param group
	 * @return the clone benefit program, considering whether the group was a
	 *         standard group or a K1 group
	 */
	private String determineCloneBenProgram(Company company, BenefitGroup group) {
		RealmCloneProgram realmClonePgm = realmDataDao.getRealmCloneProgram(company.getRealmPlanYear().getId());
		String cloneProgram = null;
		if ("K1".equals(group.getType())) {
			cloneProgram = realmClonePgm.getCloneK1Program();
		} else {
			cloneProgram = realmClonePgm.getCloneProgram();
		}
		return cloneProgram;
	}
}
