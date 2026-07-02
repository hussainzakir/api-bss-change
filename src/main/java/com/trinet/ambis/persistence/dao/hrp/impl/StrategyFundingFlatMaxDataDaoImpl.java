package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingFlatMaxDataDao;
import com.trinet.ambis.util.DaoUtils;

public class StrategyFundingFlatMaxDataDaoImpl implements StrategyFundingFlatMaxDataDao {

	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager em;

	@Override
	public void updateStrategyFundingFlatMax(long strategyId, long groupId, String planType, String coverageLevel, BigDecimal contribution) {
		Query query = em.createNamedQuery("UPDATE_STRATEGY_FUNDING_FLTMAX");
		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		query.setParameter(BSSQueryConstants.GROUP_ID, groupId);
		query.setParameter(BSSQueryConstants.PLAN_TYPE, planType);
		query.setParameter("coverageLevel", coverageLevel);
		query.setParameter("contribution", contribution);
		DaoUtils.executeUpdate(query, "UPDATE_STRATEGY_FUNDING_FLTMAX");
	}

}