package com.trinet.ambis.persistence.dao.hrp;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;

@Repository
@Transactional(readOnly = true)
public interface XbssRealmPlyrPlanDao extends JpaRepository<XbssRealmPlyrPlan, Long> {

	List<XbssRealmPlyrPlan> findByBenefitPlanAndPlanTypeAndRealmYearId(String benefitPlan, String planType, BigDecimal realmYearId);
	List<XbssRealmPlyrPlan> findByRealmYearId( BigDecimal realmYearId );


    /**
     * This method returns list all mandatory benefit plans for given realmYrId 
     * except for given HQ region 
     * 
     * @param hqRegion
     * @param realmYrId
     * @return List
     */
    @Query(value = "SELECT DISTINCT XRPP.BENEFIT_PLAN " + 
		    		"FROM XBSS_REALM_PLYR_PLAN XRPP, " + 
		    		"XBSS_RL_REGION_PLANS XRRP " + 
		    		"WHERE XRRP.REALM_PLYR_PLAN_ID = XRPP.ID " + 
		    		"AND XRRP.MANDATORY_FLAG       = 1 " + 
		    		"AND XRPP.REALM_YEAR_ID        = ?2 " + 
		    		"MINUS " + 
		    		"SELECT DISTINCT XRPP1.BENEFIT_PLAN " + 
		    		"FROM XBSS_REALM_PLYR_PLAN XRPP1, " + 
		    		"XBSS_RL_REGION_PLANS XRRP1 " + 
		    		"WHERE XRRP1.REALM_PLYR_PLAN_ID = XRPP1.ID " + 
		    		"AND XRRP1.MANDATORY_FLAG       = 1 " + 
		    		"AND XRPP1.REALM_YEAR_ID        = ?2 " + 
		    		"AND XRRP1.REGION               = ?1 ", nativeQuery = true)
	List<String> getAllMandatoryPlansExcludingGivenRegion(String hqRegion, BigDecimal realmYrId);

	/**
	 * This method validates the benefit plan belongs in the XbssRealmPlyrPlan matching the
	 * given strategyId.  Strategy is joined to XbssRealmPlyrPlan through Company.
	 * 
	 * @param realmYeadIds
	 * @param planTypes
	 * @return <b>1</b> means the plan is valid for the indicated strategy/year<br>
	 * <b>0</b> means the plan is not valid
	 */
	@Query(value = "SELECT PYP.BENEFIT_PLAN "
				+  "  FROM XBSS_STRATEGY ST "
				+  "     , XBSS_COMPANY CO "
				+  "     , XBSS_REALM_PLYR_PLAN PYP "
				+  " WHERE ST.ID = ?2 "
				+  "   AND CO.ID = ST.COMPANY_ID "
				+  "   AND PYP.REALM_YEAR_ID = CO.REALM_YEAR_ID "
				+  "   AND PYP.BENEFIT_PLAN IN ?1 ", nativeQuery = true)
	List<String> validatePlanForStrategyYear(Set<String> benefitPlan, long strategyId);

	/**
	 * This method return list of XbssRealmPlyrPlan for given realmYrIds and
	 * planTypes.
	 * 
	 * @param realmYeadIds
	 * @param planTypes
	 * @return
	 */
	List<XbssRealmPlyrPlan> findByRealmYearIdInAndPlanTypeInOrderByRealmYearId(Set<BigDecimal> realmYrIds,
			List<String> planTypes);
	
}
