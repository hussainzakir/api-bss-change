package com.trinet.ambis.persistence.dao.hrp.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.RealmWaitPeriodDao;
import com.trinet.ambis.service.model.WaitPeriod;
import com.trinet.ambis.util.DaoUtils;

public class RealmWaitPeriodDaoImpl implements RealmWaitPeriodDao {
	@PersistenceContext(unitName = "bis-hrp")
	EntityManager em;

	public static final String GET_WAITPERIOD_DESCR = "getWaitPeriodCodesDescr";

	/**
	 * This method is for retrieving wait periods for the realm plan year ID.
	 * @param PlanYear realm plan year id.
	 * @return list of wait periods for a plan year.
	 */
	@Override
	public List<WaitPeriod> getWaitPeriodsByRealmPlanYear(long planYear) {
		Query q = em.createNamedQuery("getWaitPeriodCodes");
		q.setParameter(BSSQueryConstants.PLAN_YEAR_ID, planYear);
		List<WaitPeriod> list = new ArrayList<>();
		List<Object[]> results = DaoUtils.getResultList(q, "getWaitPeriodCodes");
		for (Object[] r : results) {
			WaitPeriod wp = new WaitPeriod();
			wp.setId((String) r[0]);
			wp.setDescription((String) r[1]);
			list.add(wp);
		}
		return list;
	}
	
	@Override
	public Map<String,String> getWaitPeriodDescriptions() {
		Map<String,String> waitPdMap = new HashMap<>();
		Query q = em.createNamedQuery( GET_WAITPERIOD_DESCR );
		List<Object[]> results = DaoUtils.getResultList(q, GET_WAITPERIOD_DESCR );
		for( Object[] r : results ) {
			waitPdMap.put( (String) r[0], (String) r[1] );
		}
		return waitPdMap;
	}

	/**
	 * setter for entity manager
	 * @param em
	 */
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}
	/**
	 * getter for entity manager.
	 * @return
	 */
	public EntityManager getEntityManager() {
		return this.em;
	}
	
	
}
