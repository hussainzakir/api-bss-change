package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Map;

import com.trinet.ambis.service.model.WaitPeriod;

/**
 * @author rvutukuri
 *
 */
public interface RealmWaitPeriodDao {
	/**
	 * This method is for retrieving wait periods for the realm plan year ID.
	 * @param PlanYear plan year of a Realm.
	 * @return list of wait periods for a plan year.
	 */
	public List<WaitPeriod> getWaitPeriodsByRealmPlanYear(long PlanYear);
	
	Map<String,String> getWaitPeriodDescriptions();

}
