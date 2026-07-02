package com.trinet.ambis.persistence.dao.hrp.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.BSSStatusDetailsDao;
import com.trinet.ambis.persistence.model.BSSStatusDetailsDto;
import com.trinet.ambis.util.DaoUtils;

public class BSSStatusDetailsDaoImpl implements BSSStatusDetailsDao {

	private static final Logger logger = LoggerFactory.getLogger(BSSStatusDetailsDaoImpl.class);

	private static final String BSS_STATUS_DETAILS = "BSS_STATUS_DETAILS";

	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager em;

	public void setEm(EntityManager em) {
		this.em = em;
	}

	@Override
	public BSSStatusDetailsDto getSubmitedStatus(String companyCode) {
		Query query = em.createNamedQuery(BSS_STATUS_DETAILS);
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		List<Object[]> bssStatuses = DaoUtils.getResultList(query, BSS_STATUS_DETAILS);
		BSSStatusDetailsDto bssStatusDetails = new BSSStatusDetailsDto();
		if (!CollectionUtils.isEmpty(bssStatuses)) {
			Object[] res = bssStatuses.get(0);
			bssStatusDetails.setBssStarted(isContainNullValue(res[0]));
			bssStatusDetails.setBssSubmitted(isContainNullValue(res[1]));
		}
		logger.info("BSS_STATUS DETAILS : {}", bssStatusDetails);

		return bssStatusDetails;
	}

	private boolean isContainNullValue(Object object) {
		return object != null;
	}
}
