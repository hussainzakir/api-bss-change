package com.trinet.ambis.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;

@Service
public interface RealmPlyrPlanService {

	List<XbssRealmPlyrPlan> findByBenefitPlanAndPlanTypeAndRealmYearId(String benefitPlan, String planType,
			BigDecimal realmYearId);

	List<XbssRealmPlyrPlan> getForRealmPlanYear(long realmYearId);

	Map<String, XbssRealmPlyrPlan> getMapForRealmPlanYear(long realmYearId);

	XbssRealmPlyrPlan save(XbssRealmPlyrPlan xbssRealmPlyrPlan);

	/**
	 * This method returns the commuter XbssRealmPlyrPlan for the passed in realmYearId
	 * 
	 * @param realmYearId
	 * @return
	 */
	XbssRealmPlyrPlan getCommuterPlanForRealmPlanYear( long realmYearId );

	/**
	 * Returns a map XbssRealmPlyrPlans by planType for the passed in
	 * realmYearId and planTypes
	 * 
	 * @param realmYearId
	 * @param planTypes
	 * @return
	 */
	Map<String, List<XbssRealmPlyrPlan>> getPlanTypePlanMapForRealmPlanYear(long realmYearId, List<String> planTypes);

}
