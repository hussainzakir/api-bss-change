package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.EmployeeStrategyGroup;

/**
 * @author hliddle
 *
 */
@Repository
@Transactional(readOnly = true)
public interface EmployeeStrategyGroupDao extends JpaRepository<EmployeeStrategyGroup, Long> {

	@Modifying
	@Query(value = "Delete from EmployeeStrategyGroup esg where strategyGroupId in"
			+ "(select bgs.id  from BenefitGroupStrategy bgs where bgs.strategyId in"
			+ "(select s.id from Strategy s where s.companyId in"
			+ "(select cmny.id from Company cmny where cmny.code = :companyCode))) and  esg.emplId in (:employeeIds) ")
	void deleteEmployeeStrategyGroups(@Param("employeeIds") List<String> employeeIds,
			@Param("companyCode") String companyCode);

	@Modifying
	@Transactional
	@Query(value = "UPDATE xbss_employee_STRATEGY_GROUP SET strategy_group_id = :defaultStrategyGroupId"
			+ " WHERE strategy_group_id IN (:k1StrategyGroupIds)", nativeQuery = true)
	void updateEmployeesToDefaultStrategyGroup(@Param("k1StrategyGroupIds") List<Long> k1StrategyGroupIds,
			@Param("defaultStrategyGroupId") Long defaultStrategyGroupId);
}
