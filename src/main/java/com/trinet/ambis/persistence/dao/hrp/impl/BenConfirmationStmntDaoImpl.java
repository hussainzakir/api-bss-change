package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.BenConfirmationStmntDao;
import com.trinet.ambis.service.model.BenConfirmationStatement;
import com.trinet.ambis.util.DaoUtils;

public class BenConfirmationStmntDaoImpl implements BenConfirmationStmntDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(BenConfirmationStmntDaoImpl.class);

	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager em;

	@Override
	public List<BenConfirmationStatement> getBenefitConfirmationStatementsBy(String companyCode) {
		String query = "BEN_CONFIRMATION_STATEMENTS";
		Query q = em.createNamedQuery(query);
		q.setParameter(BSSQueryConstants.COMPANY, companyCode);
		List<Object[]> results = DaoUtils.getResultList(q, query);
		List<BenConfirmationStatement> benConfirmationStatements = new ArrayList<>();
		if (results != null) {
			for (Object[] r : results) {
				BenConfirmationStatement benConfirmationStatement = new BenConfirmationStatement();
				benConfirmationStatement.setStrategyId(((BigDecimal) r[0]).longValue());
				benConfirmationStatement.setRealmYrId(((BigDecimal) r[1]).longValue());
				benConfirmationStatement.setPlanYrStartDate((Date) r[2]);
				benConfirmationStatement.setPlanYrEndDate((Date) r[3]);
				benConfirmationStatement.setEffectiveDate((Date) r[4]);
				benConfirmationStatement.setSubmitUser((String) r[5]);
				benConfirmationStatement.setConfirmationNumber((String) r[6]);
				benConfirmationStatement.setSubmittedDate((Date) r[7]);
				benConfirmationStatements.add(benConfirmationStatement);
			}
		}
		return benConfirmationStatements;

	}
}
