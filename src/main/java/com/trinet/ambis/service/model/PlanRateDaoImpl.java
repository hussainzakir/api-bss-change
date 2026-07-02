package com.trinet.ambis.service.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.trinet.ambis.util.DaoUtils;

public class PlanRateDaoImpl implements PlanRateDao {

	@PersistenceContext(unitName = "bis-hrp")
	EntityManager em;

	@Override
	public Map<String, String> getPortfolios(Date effectiveDate) {
		Map<String, String> idMap = new HashMap<>();
		Query q = em.createNamedQuery("getPortfolios");
		q.setParameter("effectiveDate", effectiveDate);
		List<Object[]> results = DaoUtils.getResultList(q, "getPortfolios");

		for (Object[] r : results) {
			String id = r[0].toString();
			String name = r[1].toString();

			idMap.put(id, name);
		}

		return idMap;
	}

}
