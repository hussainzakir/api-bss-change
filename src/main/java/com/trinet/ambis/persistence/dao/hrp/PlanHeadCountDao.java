package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.service.model.CoverageLevelHeadCount;

@Repository
@Transactional(readOnly = true)
public interface PlanHeadCountDao {
	/**
	 * This method is for getting the head counts for a company from BSS.
	 * @param companyCode
	 * @param previousRealmPlanYearId
	 * @param ss
	 * @return
	 */
	Map<String, Map<String, List<CoverageLevelHeadCount>>> getLastYearPlansAndHeadCount(String companyCode,
			Long previousRealmPlanYearId, Set<Long> ss);
}
