package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.MappedHeadCount;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;

@Repository
@Transactional(readOnly = true)
public interface HeadCountDao {

	Map<String, Integer> getEmployeeCountByBenefitGroup(String companyCode, Long prevRealmPlanYearId);

	Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> getPlanCoverageLevelHeadCountByGroup(String companyCode,
			Long realmPlanYearId, boolean isHeadCountMapped);

	Map<String, Long> getWaiverHeadCountByBenefitProgram(Company company, long strategyId, boolean history);

	Map<String, Integer> geEnrolledHeadCountByBenefitProgram(Company company, long strategyId, boolean history);

	Map<String, ActiveEligibleEECount> getEligibleEmployeeCount(Company company, long strategyId,
			boolean history);
	
	List<MappedHeadCount> getMappedHeadCount(String companyCode, long realmPlanYearId);
}
