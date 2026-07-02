package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.RealmPlyrPlanService;

@Service
public class RealmPlyrPlanServiceImpl implements RealmPlyrPlanService {

    @Autowired 
    XbssRealmPlyrPlanDao xbssRealmPlyrPlanDao;

    @Override
    public List<XbssRealmPlyrPlan> findByBenefitPlanAndPlanTypeAndRealmYearId(String benefitPlan, String planType, BigDecimal realmYearId) {
        return xbssRealmPlyrPlanDao.findByBenefitPlanAndPlanTypeAndRealmYearId(benefitPlan, planType, realmYearId);
    }
    

	@Override
	public List<XbssRealmPlyrPlan> getForRealmPlanYear( long realmYearId ) {
		return xbssRealmPlyrPlanDao.findByRealmYearId( BigDecimal.valueOf( realmYearId ) );
	}

	@Override
	public Map<String,XbssRealmPlyrPlan> getMapForRealmPlanYear( long realmYearId ) {
		List<XbssRealmPlyrPlan> plyrList = xbssRealmPlyrPlanDao.findByRealmYearId( BigDecimal.valueOf( realmYearId ) );
		Map<String,XbssRealmPlyrPlan> map = new HashMap<>();
		for( XbssRealmPlyrPlan p : plyrList ) {
			map.put( p.getBenefitPlan(), p );
		}
		return map;
	}

    @Override
    public XbssRealmPlyrPlan save(XbssRealmPlyrPlan xbssRealmPlyrPlan){
        return xbssRealmPlyrPlanDao.save(xbssRealmPlyrPlan);
    }

	@Override
	public XbssRealmPlyrPlan getCommuterPlanForRealmPlanYear(long realmYearId) {
		XbssRealmPlyrPlan commuterPlan = null;
		List<XbssRealmPlyrPlan> planList = xbssRealmPlyrPlanDao.findByRealmYearIdInAndPlanTypeInOrderByRealmYearId(
				new HashSet<>(Arrays.asList(BigDecimal.valueOf(realmYearId))),
				Arrays.asList(PlanTypesEnum.CMTR.getCode()));
		if (!planList.isEmpty()) {
			commuterPlan = planList.get(0);
		}
		return commuterPlan;
	}

    @Override
    public Map<String, List<XbssRealmPlyrPlan>> getPlanTypePlanMapForRealmPlanYear (long realmYearId, List<String> planTypes) {
    	Map<String, List<XbssRealmPlyrPlan>> returnMap = new HashMap<>();
		List<XbssRealmPlyrPlan> planYearPlanList = xbssRealmPlyrPlanDao.findByRealmYearIdInAndPlanTypeInOrderByRealmYearId( new HashSet<>(Arrays.asList(BigDecimal.valueOf( realmYearId ))), planTypes);
		for (XbssRealmPlyrPlan planYearPlan : planYearPlanList) {
			if (returnMap.containsKey(planYearPlan.getPlanType())) {
				returnMap.get(planYearPlan.getPlanType()).add(planYearPlan);
			}
			else {
				List<XbssRealmPlyrPlan> planList = new ArrayList<>();
				planList.add(planYearPlan);
				returnMap.put(planYearPlan.getPlanType(), planList);
			}
		}
		return returnMap;
    }

}
