package com.trinet.ambis.persistence.dao.ps.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.ps.SupplementalAuthDao;
import com.trinet.ambis.service.model.SupplementalLtdAuthReponse;
import com.trinet.ambis.util.DaoUtils;

@Repository
public class SupplementalAuthDaoImpl implements SupplementalAuthDao {

	@PersistenceContext(unitName = "bis-sysadm")
	private EntityManager entityManager;

	@Override
	public SupplementalLtdAuthReponse getExecSuppLtdAuthResponse(final String companyCode) {
		SupplementalLtdAuthReponse response = null;
		Query query = entityManager.createNamedQuery("GET_SUPP_LTD_AUTH_RESPONSE");
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		List<Object[]> results = DaoUtils.getResultList(query, "GET_SUPP_LTD_AUTH_RESPONSE");
		if (CollectionUtils.isNotEmpty(results)) {
			Object[] record = results.get(0);
			String userId = (String) record[1];
			Character answer = record[2] == null ? null : ((String) record[2]).charAt(0);
			Date datePosted = (Date) record[3];
			response = SupplementalLtdAuthReponse.builder().answer(answer).authDate(datePosted).authUserId(userId)
					.build();
		}
		return response;
	}

	@Override
	@Transactional(value = "bisSysadmTransactionManager", propagation = Propagation.REQUIRED, rollbackFor = {
			Exception.class })
	public void saveExecSuppLtdAuthResponse(final String companyCode,
			final SupplementalLtdAuthReponse supplementalLtdAuthReponse) {
		final String SQL_NAME = "INSERT_SUPP_LTD_AUTH_RESPONSE";
		Query query = entityManager.createNamedQuery(SQL_NAME);
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		query.setParameter(BSSQueryConstants.EMPL_ID, supplementalLtdAuthReponse.getAuthUserId());
		query.setParameter("approvalFlag", supplementalLtdAuthReponse.getAnswer());
		query.setParameter("datePosted", new Date());
		query.executeUpdate();
	}

}
