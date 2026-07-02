package com.trinet.ambis.helper;

import com.trinet.ambis.enums.CacheObjectTypeEnum;

public class CacheKeyGenerator {

	private static final String CACHE_PRE_KEY = "BSS:";
	private static final String PLAN_RATES_PRE_KEY = CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE.getObjectType().concat(":");
	private static final String BEN_PLANS_PRE_KEY = CacheObjectTypeEnum.BEN_PLANS_OBJECT_TYPE.getObjectType().concat(":");
	private static final String RATES_BEN_PLANS_PRE_KEY = CacheObjectTypeEnum.RATES_BEN_PLANS_OBJECT_TYPE.getObjectType().concat(":");
	private static final String STRATEGY_DATA_OBJECT_TYPE_KEY = CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE.getObjectType().concat(":");
	private static final String OMS_BENEFIT_PRE_KEY = CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES.getObjectType().concat(":");

	private CacheKeyGenerator() {
	}

	public static String generateCacheKey(CacheObjectTypeEnum objectType, String uniqueId) {
		StringBuilder key = new StringBuilder(CACHE_PRE_KEY);
		switch (objectType) {
		case PLAN_RATES_OBJECT_TYPE:
			key.append(PLAN_RATES_PRE_KEY);
			break;
		case BEN_PLANS_OBJECT_TYPE:
			key.append(BEN_PLANS_PRE_KEY);
			break;
		case RATES_BEN_PLANS_OBJECT_TYPE:
			key.append(RATES_BEN_PLANS_PRE_KEY);
			break;
		case STRATEGY_DATA_OBJECT_TYPE:
			key.append(STRATEGY_DATA_OBJECT_TYPE_KEY);
			break;
		case OMS_BENEFIT_PLAN_RATES:
			key.append(OMS_BENEFIT_PRE_KEY);
			break;
		default:
			break;
		}
		key.append(uniqueId);
		return key.toString();
	}

}