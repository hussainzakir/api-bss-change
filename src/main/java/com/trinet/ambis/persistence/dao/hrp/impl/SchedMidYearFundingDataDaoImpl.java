/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.SchedMidYearFundingDataDao;
import com.trinet.ambis.persistence.model.SchedMidYearFunding;
import com.trinet.ambis.util.DaoUtils;

public class SchedMidYearFundingDataDaoImpl implements SchedMidYearFundingDataDao {

	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager em;

	@Override
	public SchedMidYearFunding getMidYearFundingScheduleForCompany(String companyCode) {
		
		SchedMidYearFunding schedMidYearFunding = null;
		Query query = em.createNamedQuery("GET_COMPANY_MID_YEAR_FUNDING_SCHEDULE");
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		List<Object[]> results = DaoUtils.getResultList(query, "GET_COMPANY_MID_YEAR_FUNDING_SCHEDULE");
     
        for (Object[] r : results) {
        	schedMidYearFunding = new SchedMidYearFunding();
        	schedMidYearFunding.setFuturePlanYear(((BigDecimal) r[0]).equals(BigDecimal.ONE));
        	schedMidYearFunding.setFutureExisting(((BigDecimal) r[1]).equals(BigDecimal.ONE));
        	schedMidYearFunding.setId(((BigDecimal) r[2]).longValue());
        	schedMidYearFunding.setCompanyId(((BigDecimal) r[3]).longValue());
        	schedMidYearFunding.setServiceOrderNumber((String) r[4]);
        	schedMidYearFunding.setMidYearFundingEffDate((Date) r[5]);
        	schedMidYearFunding.setActive((BigDecimal) r[6] == new BigDecimal(0) ? Boolean.FALSE : Boolean.TRUE);        	
        	schedMidYearFunding.setLastUpdatedBy((String) r[7]);
        	schedMidYearFunding.setUpdtdtm((Date) r[8]);
        }

		return schedMidYearFunding;
	}
}
