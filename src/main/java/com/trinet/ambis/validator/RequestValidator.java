package com.trinet.ambis.validator;

import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;

public class RequestValidator {

	private RequestValidator() {
	}
	
	public static final String getValidatedStrategyName(String strategyName) {
		strategyName = strategyName.trim();
		if (!isValidCharacterSet(strategyName)) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_STRATEGY_SAVE_FAILED,
					BSSHttpStatusConstants.BAD_REQUEST, "", "Invalid characters", null, null));
		}
		int strategyNameMaxLength = AppRulesAndConfigsUtils.getStrategyNameMaxLength();
		if (strategyName.length() > strategyNameMaxLength) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_STRATEGY_SAVE_FAILED,
					BSSHttpStatusConstants.BAD_REQUEST, "", "Name too long", null, null));
		}
		return strategyName;
	}

	public static final String getValidatedGroupName(String groupName) {
		groupName = groupName.trim();
		if (!isValidCharacterSet(groupName)) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_GROUP_SAVE_FAILED,
					BSSHttpStatusConstants.BAD_REQUEST, "", "Invalid characters", null, null));
		}
		int groupNameMaxLength = AppRulesAndConfigsUtils.getGroupNameMaxLength();
		if (groupName.length() > groupNameMaxLength) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_GROUP_SAVE_FAILED,
					BSSHttpStatusConstants.BAD_REQUEST, "", "Name too long", null, null));
		}
		return groupName;
	}

	public static final boolean isValidCharacterSet(String string) {
		boolean results = false;
		String allowedCharacterRegExp = AppRulesAndConfigsUtils.getAllowedCharacterRegExp();
		if (string.matches(allowedCharacterRegExp)) {
			results = true;
		}
		return results;
	}

}
