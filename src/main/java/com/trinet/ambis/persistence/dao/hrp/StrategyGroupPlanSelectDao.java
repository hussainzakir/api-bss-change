package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.projections.PlanSelectionDetail;
import com.trinet.ambis.persistence.projections.PlanSelectionView;
import com.trinet.ambis.persistence.projections.StrategySitusDetail;

/**
 * 
 * @author schaudhari
 *
 */

@Repository
@Transactional(readOnly = true)
public interface StrategyGroupPlanSelectDao extends JpaRepository<PlanSelection, Long> {
	/**
	 * Returns list of all PlanSelection objects for given strategyId and groupId
	 * 
	 * @param strategyId
	 * @param groupId    return List<PlanSelection>
	 */
	public List<PlanSelection> findByStrategyIdAndGroupId(long strategyId, long groupId);

	/**
	 * Returns list of all PlanSelection objects for given strategyId and planType
	 *
	 * @param strategyId
	 * @param planType    return List<PlanSelection>
	 */
	public List<PlanSelection> findByStrategyIdAndPlanType(long strategyId, String  planType);

	/**
	 * Returns list of all PlanSelection objects for given strategyId and planTypes
	 *
	 * @param strategyId
	 * @param planTypes    return List<PlanSelection>
	 */
	public List<PlanSelection> findByStrategyIdAndPlanTypeIn(long strategyId, List<String> planTypes);

	/**
	 * Returns a list of all distinct (combination of GroupID + PlanType)
	 * PlanSelection objects for given strategyId
	 * 
	 * @param strategyId
	 * @return List<PlanSelection>
	 */
	public List<PlanSelection> findDistinctGroupIdPlanTypeByStrategyId(long strategyId);

	/**
	 * Returns list of all PlanSelection objects for given groupId
	 * 
	 * @param groupId
	 * @return List<PlanSelection>
	 */
	@Transactional
	public List<PlanSelection> findByGroupId(long groupId);

	/**
	 * Returns a list of all distinct PlanSelection objects for given strategyIds
	 * 
	 * @param strategyIds
	 * @return List<PlanSelection>
	 */
	@Query("select new com.trinet.ambis.persistence.projections.PlanSelectionView( ps.benefitPlan, ps.planType) from PlanSelection ps where ps.strategyId in (:strategyIds) group by ps.benefitPlan, ps.planType ")
	public List<PlanSelectionView> findDistinctBenefitPlanPlanTypeByStrategyIdIn(
			@Param("strategyIds") List<Long> strategyIds);

	@Query(value="SELECT DISTINCT "
				+ "      DECODE( RBPM.PLANTYPE, '10','medical' "
				+ "                           , '11','dental' "
				+ "                           , '1D','dental' "
				+ "                           , '14','vision' "
				+ "                           , '1V','vision' "
				+ "                           , null ) BEN_TYPE "
				+ "    , RBPM.BASE_PLAN_ID "
				+ "    , RBPM.BASE_PLAN_DESCR "
				+ " FROM XBSS_STRATEGY_GROUP_PLANSELECT PSL "
				+ "    , XBSS_STRATEGY ST "
				+ "    , XBSS_COMPANY CO "
				+ "    , XBSS_REALM_PLAN_YEAR RPY "
				+ "    , XBSS_REGIONAL_BASE_PLAN_MAPPING RBPM "
				+ "WHERE PSL.STRATEGY_ID = :strategyId "
				+ "  AND PSL.GROUP_ID = :groupId "
				+ "  AND PSL.PLAN_TYPE IN ('10', '11', '1D', '14', '1V') "
				+ "  AND ST.ID = PSL.STRATEGY_ID "
				+ "  AND CO.ID = ST.COMPANY_ID "
				+ "  AND RPY.ID = CO.REALM_YEAR_ID "
				+ "  AND RBPM.REGIONAL_PLAN_ID = PSL.BENEFIT_PLAN "
				+ "  AND RBPM.OE_QUARTER = RPY.OE_QUARTER "
				+ "  AND RBPM.REALM_ID = RPY.REALM_ID "
				+ "  AND RPY.PLAN_YEAR_START BETWEEN RBPM.EFFDT AND RBPM.ENDDT", nativeQuery = true)
	List<Object[]> getBasePlans(@Param("strategyId") long strategyId, @Param("groupId") long groupId);

	/**
	 * Returns distinct plan type by strategy and group
	 * 
	 * @param strategyIds
	 * @param groupIds
	 * @return
	 */
	@Query("select new com.trinet.ambis.persistence.projections.PlanSelectionDetail( ps.strategyId, ps.groupId, ps.planType) "
			+ "from PlanSelection ps "
			+ "where ps.strategyId in (:strategyIds) "
			+ "and ps.groupId in (:groupIds) "
			+ "group by ps.strategyId, ps.groupId, ps.planType ")
	public List<PlanSelectionDetail> findDistinctPlanTypeBy(@Param("strategyIds") Set<Long> strategyIds,
			@Param("groupIds") Set<Long> groupIds);
	
	@Transactional
	public List<PlanSelection> findByStrategyIdAndGroupIdAndPlanType(long strategyId, long groupId, String planType);
	
	
	/**
	 * Returns situs detailsfor strategies 
	 * 
	 * @param strategyIds
	 * @param realmPlanYearId  DECODE(UPPER(OP.T2_PRODUCT_LINE), 'DFTX', 'true' , 'false') 
	 * @return
	 */
	@Query(value = "SELECT strategyId, DECODE( UPPER(SITUS) , 'TX', 'true' , 'false' ) isTexasSitus "
			+ " FROM (SELECT DISTINCT STRATEGY_ID strategyId, XBSS_REALM_PLYR_PLAN.SITUS SITUS "
			+ " FROM XBSS_STRATEGY_GROUP_PLANSELECT, XBSS_REALM_PLYR_PLAN "
			+ " WHERE XBSS_STRATEGY_GROUP_PLANSELECT.BENEFIT_PLAN = XBSS_REALM_PLYR_PLAN.BENEFIT_PLAN AND XBSS_REALM_PLYR_PLAN.REALM_YEAR_ID = :realmPlanYearId "
			+ " AND XBSS_STRATEGY_GROUP_PLANSELECT.STRATEGY_ID IN :strategyIds "
			+ " AND XBSS_REALM_PLYR_PLAN.PORTFOLIO_ID in (1, 33) "
			+ " AND XBSS_REALM_PLYR_PLAN.PLAN_TYPE = '10' "
			+ " AND XBSS_REALM_PLYR_PLAN.SITUS IS NOT NULL ) ", nativeQuery = true)
	public List<StrategySitusDetail> getStrategiesSitus(@Param("strategyIds") List<Long> strategyIds ,@Param("realmPlanYearId") long realmPlanYearId );
	
	/**
	 * Returns list of all PlanSelection objects for given strategyId and groupId
	 * 
	 * @param strategyId
	 * @param groupId    return List<PlanSelection>
	 */
	@Transactional
	public List<PlanSelection> findByStrategyId(long strategyId);

}
