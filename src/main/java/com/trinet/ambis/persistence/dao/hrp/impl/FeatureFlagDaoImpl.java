package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.FeatureFlagDao;
import com.trinet.ambis.service.model.FeatureFlag;
import com.trinet.ambis.util.DaoUtils;

public class FeatureFlagDaoImpl implements FeatureFlagDao {

	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager em;

	@Override
	public List<FeatureFlag> retrieveFeatureFlags(String companyCode, long realmYrId) {
		Query query = em.createNamedQuery("FEATURE_ON_OFF_FLAG");
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		query.setParameter(BSSQueryConstants.PLAN_YEAR_ID, realmYrId);

		List<Object[]> results = DaoUtils.getResultList(query, "FEATURE_ON_OFF_FLAG");
		List<FeatureFlag> flags = new ArrayList<>(results.size());
		for (Object[] r : results) {
			String key = (String) r[0];
			boolean value = BigDecimal.ZERO.compareTo((BigDecimal) r[1]) == 0 ? Boolean.FALSE : Boolean.TRUE;
			FeatureFlag flag = new FeatureFlag(key, value);
			flags.add(flag);
		}
		return flags;
	}

	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

}
