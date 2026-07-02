package com.trinet.ambis.persistence.dao.hrp.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.DeselectionExceptionDao;
import com.trinet.ambis.util.DaoUtils;

@Repository
public class DeselectionExceptionDaoImpl implements DeselectionExceptionDao {

	public static final String PICK_CHOOSE_FLAG_WITH_EXCEPTION = "PICK_CHOOSE_FLAG_WITH_EXCEPTION";

	@PersistenceContext(unitName = "bis-hrp")
	EntityManager em;

	public void setEntityManager(EntityManager em) {
		this.em = em;
	}


	@Override
	public List<Object[]> getPickChooseWithException( long realmYearId, String companyCode, Date effdt ) {

		Query query = em.createNamedQuery( PICK_CHOOSE_FLAG_WITH_EXCEPTION );
		query.setParameter( BSSQueryConstants.REALM_YEAR_ID, realmYearId );
		query.setParameter( BSSQueryConstants.COMPANY_CODE, companyCode );
		query.setParameter( BSSQueryConstants.EFF_DT, new java.sql.Date( effdt.getTime() ) );
		return DaoUtils.getResultList( query, PICK_CHOOSE_FLAG_WITH_EXCEPTION );
	}


}
