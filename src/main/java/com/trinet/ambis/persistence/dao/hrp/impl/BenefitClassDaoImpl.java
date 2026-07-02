package com.trinet.ambis.persistence.dao.hrp.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.BenefitClassDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.util.DaoUtils;

public class BenefitClassDaoImpl implements BenefitClassDao {
	private static final Logger logger = LoggerFactory.getLogger(BenefitClassDaoImpl.class);

	@PersistenceContext( unitName = "bis-sysadm" )
	EntityManager em;
	
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}
	public EntityManager getEntityManager() {
		return this.em;
	}

	
	@Override
	public String getEligClass(Company company, BenefitGroup group) {

		Query query = em.createNamedQuery("SELECT_EXISTING_ELIG_CONFIG1");

		query.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		query.setParameter(BSSQueryConstants.EFF_DT, company.getRealmPlanYear().getPlanYearEnd());
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());

		String eligClass;
		try {
			eligClass = (String) DaoUtils.getSingleResult(query, "SELECT_EXISTING_ELIG_CONFIG1");
		} catch (Exception e) {
			eligClass = "";
		}
		logger.info("Query returned elig class: {}", eligClass);

		return eligClass;
	}


	@Override
	public Map<String,String> getBenProgramBenClassMappings( Company company ) {
		Query query = em.createNamedQuery("SELECT_BEN_PROGRAM_CLASS_CODES");
		query.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		query.setParameter(BSSQueryConstants.EFF_DT, company.getRealmPlanYear().getPlanYearEnd());
		List<Object[]> result = DaoUtils.getResultList(query, "SELECT_BEN_PROGRAM_CLASS_CODES");

		Map<String,String> programClassMap = new HashMap<>();
		for( Object[] row : result ) {
			programClassMap.put( (String) row[0], (String) row[1] );
		}

		return programClassMap;
	}

}
