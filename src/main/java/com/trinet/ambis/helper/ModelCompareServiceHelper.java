/**
 * 
 */
package com.trinet.ambis.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.StrategyTypesEnums;
import com.trinet.ambis.persistence.model.RealmPlanYear;

/**
 * @author hliddle
 *
 */
public class ModelCompareServiceHelper {

	private ModelCompareServiceHelper() {
		throw new IllegalStateException("ModelCompareServiceHelper class");
	}

	/**
	 * Takes the passed in comma delimited string and parses it. The first
	 * element in the string is assumed to be the current strategy id and the
	 * next elements are put in the list as the future strategy id(s).
	 * 
	 * Returns a map with one entry that is the current strategy id and a list
	 * of future strategy id(s).
	 * 
	 * @param strategyListString
	 * @return Map<Long, List<Long>>
	 */
	public static Map<Long, List<Long>> splitStrategyList(String strategyListString) {

		Map<Long, List<Long>> returnMap = new HashMap<>();

		if (strategyListString != null && !strategyListString.isEmpty()) {
			List<Long> strategyList = new ArrayList<>();
			Long currentStrategyId = null;
			int i = 0;
			for (String s : strategyListString.split(",")) {
				if (i == 0) {
					i = 1;
					currentStrategyId = Long.valueOf(s);
				} else {
					strategyList.add(Long.valueOf(s));
				}
			}
			returnMap.put(currentStrategyId, strategyList);
		}
		return returnMap;
	}

	public static String getStrategyDisplayName(String strategyName, RealmPlanYear realmPlanYear, boolean history,
			boolean isProspect) {
		if (!isProspect) {
			if (history) {
				strategyName = StrategyTypesEnums.F_S.getName();
			}
			if (StrategyTypesEnums.F_S.getName().equals(strategyName)) {
				strategyName = strategyName.concat(BSSApplicationConstants.EMPTY_SPACE)
						.concat(getPlanYearDateRangeText(realmPlanYear));
				if (!history) {
					strategyName = strategyName.concat(BSSApplicationConstants.EMPTY_SPACE)
							.concat(BSSApplicationConstants.STRATEGY_RATE_SUFFIX);
				}
			}
		}
		return strategyName;
	}
	
	/**
	 * 
	 * @param company
	 * @return
	 */
	public static String getPlanYearDateRangeText(RealmPlanYear realmPlanYear) {
		SimpleDateFormat sd = new SimpleDateFormat("MM/dd/yyyy");
		Date startDate = null;
		Date endDate = null;
		startDate = realmPlanYear.getPlanYearStart();
		endDate = realmPlanYear.getPlanYearEnd();
		return sd.format(startDate) + " - " + sd.format(endDate);
	}	

}
