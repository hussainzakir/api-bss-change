/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.dao.hrp.PlanHeadCountDao;
import com.trinet.ambis.service.model.CoverageLevelHeadCount;
import com.trinet.ambis.util.DaoUtils;

/**
 * @author kpamulapati
 */

public class PlanHeadCountDaoImpl implements PlanHeadCountDao {
	@PersistenceContext(unitName = "bis-hrp")
	EntityManager em;

	@Override
	public Map<String, Map<String, List<CoverageLevelHeadCount>>> getLastYearPlansAndHeadCount(String companyCode,
			Long previousRealmPlanYearId, Set<Long> submitStatus) {
		Query q = constructQuery(companyCode, previousRealmPlanYearId, submitStatus);

		List<Object[]> results = DaoUtils.getResultList(q, "getLastYearSubmittedPlansAndHeadCount");
		Map<String, Map<String, List<CoverageLevelHeadCount>>> map = new HashMap<>();

		if (results != null) {
			for (Object[] row : results) {
				CoverageLevelHeadCount headCount = createHeadCountFromRow(row);
				String benefitPlan = (String) row[1];
				String groupName = (String) row[5];

				map.computeIfAbsent(groupName, k -> new HashMap<>())
						.computeIfAbsent(benefitPlan, k -> new ArrayList<>()).add(headCount);
			}
		}
		return map;
	}

	private CoverageLevelHeadCount createHeadCountFromRow(Object[] row) {
		CoverageLevelHeadCount headCount = new CoverageLevelHeadCount();
		headCount.setHeadCount(((BigDecimal) row[3]).intValue());
		headCount.setCoverageLevel(CoverageCodesEnums.valueOfId((String) row[2]));
		headCount.setBenefitProgram((String) row[5]);
		return headCount;
	}

	private Query constructQuery(String companyCode, Long previousRealmPlanYearId, Set<Long> submitStatus) {
		Query query = em.createNamedQuery("getLastYearSubmittedPlansAndHeadCount");
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, previousRealmPlanYearId);
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		query.setParameter("submitStatus", submitStatus);
		return query;
	}

	/**
	 * @param em the em to set
	 */
	public void setEm(EntityManager em) {
		this.em = em;
	}
}
