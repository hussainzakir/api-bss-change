
package com.trinet.ambis.persistence.dao.hrp;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.PlanSelection;

@Repository
@Transactional(readOnly = true)
public interface PlanSelectionDao extends JpaRepository<PlanSelection, Long> {
	/**
	 * 
	 * @param strategyId
	 * @param benefitGroupId
	 * @param benefitPlan
	 * @return
	 */
	PlanSelection getByStrategyIdAndGroupIdAndBenefitPlan(long strategyId, long benefitGroupId, String benefitPlan);


	/**
	 * This method deletes PlanSelection for given strategy ids and plan type codes.
	 * 
	 * @param strategyIds
	 * @param planType
	 */
	void deleteByStrategyIdInAndPlanTypeIn(Set<Long> strategyIds, Set<String> planType);
}
