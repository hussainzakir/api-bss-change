package com.trinet.ambis.helper;

import com.trinet.ambis.common.BSSApplicationConstants;

public class PlanOverrideServiceHelper {
	
	private PlanOverrideServiceHelper() {
		throw new IllegalStateException("PlanOverrideServiceHelper class");
	}
	
	/**
	 * This method is used to determine the correct override type for renewal given the current override type.
	 * It assumes a desired default override type of BASE
	 * 
	 * @param currentOverrideType
	 * @return
	 */
	public static String getRenewalPlanOverrideType(String currentOverrideType) {
		return getRenewalPlanOverrideType(currentOverrideType, BSSApplicationConstants.PLAN_OVERRIDE_BASE);
	}
	
	/**
	 * This method is used to determine the correct override type for renewal given the current override type.
	 * The defaultOverrideType is the default override value desired if this is not MNF or FPL
	 * 
	 * 
	 * @param currentOverrideType
	 * @param defaultOverrideType
	 * @return
	 */
	public static String getRenewalPlanOverrideType(String currentOverrideType, String defaultOverrideType) {
		String returnOverrideType = currentOverrideType;
		if (currentOverrideType != null) {
			if (BSSApplicationConstants.PLAN_OVERRIDE_MNF.equals(currentOverrideType)
					|| BSSApplicationConstants.PLAN_OVERRIDE_FPL.equals(currentOverrideType)) {
				returnOverrideType = defaultOverrideType;
			} else if (BSSApplicationConstants.PLAN_OVERRIDE_MNF_FLT.equals(currentOverrideType)
					|| BSSApplicationConstants.PLAN_OVERRIDE_FPL_FLT.equals(currentOverrideType)) {
				returnOverrideType = BSSApplicationConstants.PLAN_OVERRIDE_FLT;
			} else if (BSSApplicationConstants.PLAN_OVERRIDE_MNF_PCT.equals(currentOverrideType)
					|| BSSApplicationConstants.PLAN_OVERRIDE_FPL_PCT.equals(currentOverrideType)) {
				returnOverrideType = BSSApplicationConstants.PLAN_OVERRIDE_PCT;
			}
		}
		return returnOverrideType;
	}
	
	/**
	 * This method is used to determine the correct MNF override type given the current override type.
	 * 
	 * @param currentOverrideType
	 * @return
	 */
	public static String getMNFPlanOverrideType(String currentOverrideType) {
		String returnOverrideType;
		if (BSSApplicationConstants.PLAN_OVERRIDE_FLT.equals(currentOverrideType)) {
			returnOverrideType = BSSApplicationConstants.PLAN_OVERRIDE_MNF_FLT;
		} else if (BSSApplicationConstants.PLAN_OVERRIDE_PCT.equals(currentOverrideType)) {
			returnOverrideType = BSSApplicationConstants.PLAN_OVERRIDE_MNF_PCT;
		} else {
			returnOverrideType = BSSApplicationConstants.PLAN_OVERRIDE_MNF;
		}
		return returnOverrideType;
	}
	
	/**
	 * This method is used to determine the correct FPL override type given the current override type.
	 * 
	 * @param currentOverrideType
	 * @return
	 */
	public static String getFPLPlanOverrideType(String currentOverrideType) {
		String returnOverrideType;
		if (BSSApplicationConstants.PLAN_OVERRIDE_FLT.equals(currentOverrideType)) {
			returnOverrideType = BSSApplicationConstants.PLAN_OVERRIDE_FPL_FLT;
		} else if (BSSApplicationConstants.PLAN_OVERRIDE_PCT.equals(currentOverrideType)) {
			returnOverrideType = BSSApplicationConstants.PLAN_OVERRIDE_FPL_PCT;
		} else {
			returnOverrideType = BSSApplicationConstants.PLAN_OVERRIDE_FPL;
		}
		return returnOverrideType;
	}	

}