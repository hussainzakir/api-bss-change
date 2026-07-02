package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.EePlanAssignmentPK;
import com.trinet.ambis.service.prospect.dto.EmployeeHeadCountRes;

@Repository
@Transactional(readOnly = true)
public interface EePlanAssignmentDao extends JpaRepository<EePlanAssignment, EePlanAssignmentPK> {

	/**
	 * Returns a List of all entities associated with the given
	 * <code>strategyId</code>.
	 * 
	 * @param strategyId
	 * @return List<EePlanAssignment>
	 */
	List<EePlanAssignment> findByEePlanAssignmentPKStrategyId(long strategyId);

	/**
	 * Returns a List of all entities associated with the given
	 * <code>strategyIds</code>.
	 * 
	 * @param strategyIds
	 * @return List<EePlanAssignment>
	 */
	List<EePlanAssignment> findByEePlanAssignmentPKStrategyIdIn(List<Long> strategyIds);

	@Modifying
	@Query(value = "delete from EePlanAssignment epa where epa.id.strategyId in "
			+ "(select s.id from Strategy s where s.companyId in "
			+ "(select cmny.id from Company cmny where cmny.code = :companyCode)) and epa.id.emplId in (:employeeIds)")
	void deleteEePlanAssignment(@Param("employeeIds") List<String> employeeIds,
			@Param("companyCode") String companyCode);

	@Query(value = "select new com.trinet.ambis.service.prospect.dto.EmployeeHeadCountRes(xepa.benefitPlan as benefitPlan, xepa.covrgCD as covrgCD, count(*) as count) from EePlanAssignment xepa where xepa.id.strategyId = :strategyId group  by (xepa.benefitPlan, xepa.covrgCD)")
	List<EmployeeHeadCountRes> findHeadCountByIdStrategyId(@Param("strategyId") long strategyId);

	@Modifying
	@Query(value = "delete from EePlanAssignment epa where epa.id.strategyId in "
			+ "(select s.id from Strategy s where s.companyId in "
			+ "(select cmny.id from Company cmny where cmny.code = :companyCode))")
	void deleteEePlanAssignmentBy(@Param("companyCode") String companyCode);

	@Modifying
	@Query(value = "delete from EePlanAssignment epa where epa.id.emplId in (:employeeIds) and epa.id.strategyId in (:strategyIds)")
	void deleteEePlanAssignmentBy(@Param("employeeIds") Set<String> employeeIds, @Param("strategyIds") Set<Long> strategyIds);
	
	@Modifying
	@Query(value = "delete from EePlanAssignment epa where epa.id.emplId in (:employeeIds) and epa.id.strategyId = :strategyId and epa.id.benefitType in (:benTypes)")
	void deleteEePlanAssignmentBy(@Param("employeeIds") Set<String> employeeIds,
			@Param("strategyId") long strategyId, @Param("benTypes") Set<String> benTypeCodes);

	@Query(value="SELECT XEPA.* FROM XBSS_EE_PLAN_ASSIGNMENT XEPA, XBSS_EMPL_STRATEGY_GROUP_VW XESG WHERE XESG.STRATEGY_ID = XEPA.STRATEGY_ID AND XEPA.EMPLID=XESG.EMPLID and XESG.FUTURE_GROUP_ID = :GROUP_ID and XEPA.STRATEGY_ID=:STRATEGY_ID", nativeQuery = true)
	List<EePlanAssignment> findByStrategyIdGroupId(@Param("STRATEGY_ID") long strategyId, @Param("GROUP_ID") long groupId);

	@Modifying
	@Query(value = "delete from EePlanAssignment epa where epa.id.strategyId in (:strategyIds)")
	void deleteEePlanAssignmentBy(@Param("strategyIds") Set<Long> strategyIds);
	
	@Modifying
	@Query(nativeQuery = true, value = "DELETE FROM XBSS_EE_PLAN_ASSIGNMENT xepa WHERE xepa.emplId in "
            + "( SELECT xesg.emplId FROM XBSS_EMPL_STRATEGY_GROUP_VW xesg  "
            + "WHERE xesg.STRATEGY_ID in (:strategyIds) AND xesg.FUTURE_GROUP_ID in (:groupIds)) "
            + "AND xepa.STRATEGY_ID in (:strategyIds) "
            + "AND xepa.BENEFIT_TYPE in (:benTypes)")
	void deleteEePlanAssignmentBy(@Param("strategyIds") Set<Long> strategyIds, @Param("groupIds") Set<Long> groupIds,
			@Param("benTypes") Set<String> benTypeCodes);

	@Modifying
	@Query(nativeQuery = true, value = "insert into XBSS_EE_PLAN_ASSIGNMENT(STRATEGY_ID, EMPLID, BENEFIT_PLAN, BENEFIT_TYPE, COVRG_CD, PORTFOLIO_ID, EE_RATE, ER_RATE) SELECT "
			+ " :targetStrategyId, xepa.EMPLID, xepa.BENEFIT_PLAN, xepa.BENEFIT_TYPE, xepa.COVRG_CD, xepa.PORTFOLIO_ID, xepa.EE_RATE, xepa.ER_RATE FROM XBSS_EE_PLAN_ASSIGNMENT xepa "
			+ " WHERE STRATEGY_ID = :sourceStrategyId")
	void copyEePlanAssignmentsFor(@Param("sourceStrategyId") long sourceStrategyId,
			@Param("targetStrategyId") long targetStrategyId);
	
	@Modifying
	@Query(nativeQuery = true, value = "UPDATE xbss_ee_plan_assignment eepa "
			+ "SET eepa.covrg_cd = ("
			+ "    SELECT DISTINCT eedpa.covrg_cd "
			+ "    FROM xbss_ee_default_plan_assignment eedpa "
			+ "      WHERE eepa.emplid = eedpa.emplid "
			+ "      AND eepa.benefit_type = eedpa.plan_type) "
			+ "WHERE eepa.emplid IN ( :employeeIds )")
	void updateEePlanAssignmentCvgCode(@Param("employeeIds") Set<String> employeeIds);
	
	/**
	 * This method identifies all the employees that have waived benefit for a
	 * particular bentype and delete ee plan assignments. It uses
	 * xbss_ee_default_plan_assignment table to identify the records that has
	 * missing default plan assignments but record in ee plan assignment.
	 * 
	 * @param emplIds
	 */
	@Modifying
	@Query(nativeQuery = true, value = "DELETE FROM xbss_ee_plan_assignment eepa WHERE NOT EXISTS ( "
			+ "    SELECT * FROM xbss_ee_default_plan_assignment eedpa WHERE eepa.emplid = eedpa.emplid "
			+ "    AND eepa.BENEFIT_TYPE  = eedpa.PLAN_TYPE AND eepa.emplid in (:emplIds) " + " ) "
			+ " AND eepa.emplid IN (:emplIds) AND eepa.BENEFIT_TYPE IN (:planTypes)")
	void deleteEePlanAssignmentForWaivedEmployees(@Param("emplIds") Set<String> emplIds, @Param("planTypes") Set<String> planTypes);
	
	@Modifying
	@Query(nativeQuery = true, value = "insert into XBSS_EE_PLAN_ASSIGNMENT(STRATEGY_ID, EMPLID, BENEFIT_PLAN, BENEFIT_TYPE, COVRG_CD, PORTFOLIO_ID, EE_RATE, ER_RATE) SELECT "
			+ " :targetStrategyId, xepa.EMPLID, xepa.BENEFIT_PLAN, xepa.BENEFIT_TYPE, xepa.COVRG_CD, xepa.PORTFOLIO_ID, xepa.EE_RATE, xepa.ER_RATE FROM XBSS_EE_PLAN_ASSIGNMENT xepa "
			+ " WHERE STRATEGY_ID = :sourceStrategyId AND BENEFIT_TYPE = :benefitType")
	void copyEePlanAssignmentsFor(@Param("sourceStrategyId") long sourceStrategyId,
			@Param("targetStrategyId") long targetStrategyId, @Param("benefitType") String benefitType);
}