/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.trinet.ambis.persistence.dao.hrp.CommonDataDao;
import com.trinet.ambis.service.model.SelectItem;
import com.trinet.ambis.util.DaoUtils;

/**
 * @author rvutukuri
 *
 */
public class CommonDataDaoImpl implements CommonDataDao {

	private static final String BSUPP_VOL_PLAN_TYPES = "BSUPP_VOL_PLAN_TYPES";

	@PersistenceContext(unitName = "bis-hrp")
	EntityManager em;

	@Override
	public List<SelectItem> getBsuppExcessOptions() {
		Query q = em.createNamedQuery("BSUPP_EXCESS_OPTIONS");
		List<SelectItem> list = new ArrayList<>();
		List<Object[]> results = DaoUtils.getResultList(q, "BSUPP_EXCESS_OPTIONS");
		for (Object[] r : results) {
			SelectItem exOption = new SelectItem();
			exOption.setId(((BigDecimal) r[0]).toString());
			exOption.setDescription((String) r[1]);
			list.add(exOption);
		}
		return list;
	}

	@Override
	public List<SelectItem> getBsuppVolPlanTypes(long realmId) {
		Query q = em.createNamedQuery(BSUPP_VOL_PLAN_TYPES);
		q.setParameter("realmId", realmId);
		List<SelectItem> list = new ArrayList<>();
		List<Object[]> results = DaoUtils.getResultList(q, BSUPP_VOL_PLAN_TYPES);
		for (Object[] r : results) {
			SelectItem exOption = new SelectItem();
			exOption.setId((String) r[0]);
			exOption.setDescription((String) r[1]);
			list.add(exOption);
		}
		return list;
	}

	@Override
	public List<String> getBsuppVolBenPlanTypes(long realmId) {
		Query q = em.createNamedQuery(BSUPP_VOL_PLAN_TYPES);
		q.setParameter("realmId", realmId);
		List<String> list = new ArrayList<>();
		List<Object[]> results = DaoUtils.getResultList(q, BSUPP_VOL_PLAN_TYPES);
		for (Object[] r : results) {
			list.add((String) r[0]);
		}
		return list;
	}
}
