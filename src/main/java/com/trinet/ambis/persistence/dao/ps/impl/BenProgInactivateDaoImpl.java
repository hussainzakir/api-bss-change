package com.trinet.ambis.persistence.dao.ps.impl;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.ps.BenProgInactivateDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.DaoUtils;

public class BenProgInactivateDaoImpl implements BenProgInactivateDao {
	private static final Logger logger = LoggerFactory.getLogger(BenProgInactivateDaoImpl.class);

	public static final String UPDATE_BENEFITGROUP_CLIENT_OPTION2 = "UPDATE_BENEFITGROUP_CLIENT_OPTION2";
	public static final String INACTIVATE_PRCL_BN_PGM = "INACTIVATE_PRCL_BN_PGM";
	public static final String UPDATE_BENEFITGROUP_BEN_DEFN_PROG = "UPDATE_BENEFITGROUP_BEN_DEFN_PROG";
	public static final String UPDATE_BENEFITGROUP_BEN_DEFN_PLAN = "UPDATE_BENEFITGROUP_BEN_DEFN_PLAN";
	public static final String UPDATE_BENEFITGROUP_BEN_DEFN_OPTN_ELIG_RULE = "UPDATE_BENEFITGROUP_BEN_DEFN_OPTN_ELIG_RULE";
	public static final String DELETE_EXACT_OPT2A = "DELETE_EXACT_OPT2A";


	@Override
	public int updateClientOption2(Company company, String benProg, EntityManager em) {
		Query query = em.createNamedQuery( UPDATE_BENEFITGROUP_CLIENT_OPTION2 );
		query.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
		query.setParameter(BSSQueryConstants.PF_CLIENT, company.getPfClient());
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, benProg );
		query.setParameter(BSSQueryConstants.EFF_DATE, CommonUtils.formatStringToDate(company.getPlanStartDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		int num = DaoUtils.executeUpdate(query, UPDATE_BENEFITGROUP_CLIENT_OPTION2);
		logger.info("NUMBER OF ITEM DELETE FROM CLIENT OPTION2 : {}", num);
		return num;
	}

	@Override
	public int updateBenefitPrclBenPrgm(Company company, String benProg, EntityManager em) {
		Query query = em.createNamedQuery( INACTIVATE_PRCL_BN_PGM );
		query.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
		query.setParameter("peoId", company.getRealm().getPeoid());
		query.setParameter(BSSQueryConstants.PF_CLIENT, company.getPfClient());
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, benProg );
		query.setParameter(BSSQueryConstants.EFF_DATE, CommonUtils.formatStringToDate(company.getPlanStartDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		return DaoUtils.executeUpdate(query, INACTIVATE_PRCL_BN_PGM);
	}

	@Override
	public int updateBenDefinitionPgm(Company company, String benProg, EntityManager em) {
		Query query = em.createNamedQuery( UPDATE_BENEFITGROUP_BEN_DEFN_PROG );
		query.setParameter(BSSQueryConstants.PF_CLIENT, company.getPfClient());
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, benProg );
		query.setParameter(BSSQueryConstants.EFF_DATE, CommonUtils.formatStringToDate(company.getPlanStartDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		return DaoUtils.executeUpdate(query, UPDATE_BENEFITGROUP_BEN_DEFN_PROG);
	}

	@Override
	public int updateBenDefinitionOptionEligRule(Company company, String benProg, EntityManager em) {

		// make sure there is a row in PS_BEN_DEFN_PLAN so there is no orphan
		// record in OPTN
		Query mrgPlan = em.createNamedQuery( UPDATE_BENEFITGROUP_BEN_DEFN_PLAN );
		mrgPlan.setParameter(BSSQueryConstants.BEN_PROGRAM, benProg );
		mrgPlan.setParameter(BSSQueryConstants.EFF_DATE, CommonUtils.formatStringToDate(company.getPlanStartDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		int num = DaoUtils.executeUpdate(mrgPlan, UPDATE_BENEFITGROUP_BEN_DEFN_PLAN);

		// insert/update PS_BEN_DEFN_OPTN to make benefit program ineligible for
		// assignment
		Query query = em.createNamedQuery( UPDATE_BENEFITGROUP_BEN_DEFN_OPTN_ELIG_RULE );
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, benProg );
		query.setParameter(BSSQueryConstants.EFF_DATE, CommonUtils.formatStringToDate(company.getPlanStartDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));

		num += DaoUtils.executeUpdate(query, UPDATE_BENEFITGROUP_BEN_DEFN_OPTN_ELIG_RULE);
		return num;
	}

	@Override
	public int deleteClientOpt2a(Company company, String benProg, EntityManager em) {
		Query delete = em.createNamedQuery( DELETE_EXACT_OPT2A );
		delete.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		delete.setParameter(BSSQueryConstants.PF_CLIENT, company.getPfClient());
		delete.setParameter(BSSQueryConstants.BENEFIT_PROGRAM, benProg );
		delete.setParameter(BSSQueryConstants.EFF_DATE, CommonUtils.formatStringToDate(company.getPlanStartDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		return DaoUtils.executeUpdate(delete, DELETE_EXACT_OPT2A);
	}

}
