/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.StrategyDefaultPlan;

/**
 * 
 */
@Repository
@Transactional(readOnly = true)
public interface StrategyDefaultPlanDao extends JpaRepository<StrategyDefaultPlan, Long> {

	List<StrategyDefaultPlan> findByQuarterAndPlanTypeIn(String oeQuarter, List<String> planTypes);

	/**
	 * Returns default plans matching the given quarter and plan types whose
	 * effective date range covers the supplied plan year start date
	 * (EFFDT <= planYearStart AND ENDDT >= planYearStart).
	 */
	@Query("SELECT s FROM StrategyDefaultPlan s WHERE s.quarter = :quarter AND s.planType IN :planTypes AND s.effectiveDate <= :planYearStart AND s.endDate >= :planYearStart")
	List<StrategyDefaultPlan> findBy(
			@Param("quarter") String quarter,
			@Param("planTypes") List<String> planTypes,
			@Param("planYearStart") Date planYearStart);

}
