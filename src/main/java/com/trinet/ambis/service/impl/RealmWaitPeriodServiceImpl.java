package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.RealmWaitPeriodDao;
import com.trinet.ambis.service.RealmWaitPeriodService;
import com.trinet.ambis.service.model.WaitPeriod;

/**
 * @author rvutukuri
 *
 */
@Service
public class RealmWaitPeriodServiceImpl implements RealmWaitPeriodService {

	@Autowired
	RealmWaitPeriodDao realmWaitPeriodDao;

	/**
	 * This method is for retrieving wait period info for the plan year ID.
	 * 
	 * @param PlanYear Realm plan year Id.
	 * @return list of wait periods
	 */
	@Override
	public List<WaitPeriod> getWaitPeriodsByRelamPlanYear(long planYear) {
		return realmWaitPeriodDao.getWaitPeriodsByRealmPlanYear(planYear);
	}

	@Override
	public Map<String,String> getWaitPeriodDescr() {
		return realmWaitPeriodDao.getWaitPeriodDescriptions();
	}
	
	@Override
	public List<String> getWaitPeriodCodesForRelamPlanYear(long planYearId) {
		List<String> waitPeriodCodes = new ArrayList<>();
		List<WaitPeriod> waitPeriods = getWaitPeriodsByRelamPlanYear(planYearId);
		for (WaitPeriod waitPeriod : waitPeriods) {
			waitPeriodCodes.add(waitPeriod.getId());
		}
		return waitPeriodCodes;
	}

}
