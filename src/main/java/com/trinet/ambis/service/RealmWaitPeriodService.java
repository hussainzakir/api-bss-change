/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;
import java.util.Map;

import com.trinet.ambis.service.model.WaitPeriod;

/**
 * @author rvutukuri
 *
 */
public interface RealmWaitPeriodService {
	/**
	 * This method is for retrieving wait periods for the realm plan year ID.
	 * @param PlanYear Realm plan year Id.
	 * @return list of wait periods
	 */
	public List<WaitPeriod> getWaitPeriodsByRelamPlanYear(long planYear);

	Map<String,String> getWaitPeriodDescr();
	
	List<String> getWaitPeriodCodesForRelamPlanYear(long planYearId);

}
