package com.trinet.ambis.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.service.StrategyService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.enums.CacheObjectLevelEnum;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.CacheKeyGenerator;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.BenefitGroupController;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.CacheTemplateService;

@Service
public class CacheServiceImpl implements CacheService {

	@Autowired
	private CacheTemplateService cacheTemplateService;

	@Autowired
	private StrategyService strategyService;

	@Autowired
	CompanyDao companyDao;

	@Autowired
	CompanyDataDao companyDataDao;

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheServiceImpl.class);

	@Override
	public boolean invalidateCache(String objectName, String level, String value) {
		validateRequest(objectName, level, value);
		
		if (CacheObjectTypeEnum.BEN_PLANS_OBJECT_TYPE.getObjectType().equals(objectName)) {
			invalidateBenPlansCacheUsingCompCode(level, value);
		} else if (CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE.getObjectType().equals(objectName)) {
			invalidatePlanRatesCacheUsingCompCode(level, value);
		} else if (CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE.getObjectType().equals(objectName)) {
			invalidateStrategyDataCache(level,value);
		} else if (CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES.getObjectType().equals(objectName)) {
			invalidateOmsBenefitPlanRatesCacheUsingCompCode(level, value);
		} else if (CacheObjectTypeEnum.BASIC_COMPANY_DETAILS.getObjectType().equals(objectName)) {
			invalidateCompanyData(level, value);
		} 
		else if (CacheObjectTypeEnum.ALL.getObjectType().equals(objectName)) {
			invalidateApplicationCache(level, value);
		}
		return true;
	}

	@Override
	public void invalidateOutofDateCache(Company company) {
		invalidateBenPlanAndPlanRates(company);
		if (company.isAleUpdatedNewClient() || company.isAcaLargeEmplrStatusUpdated())
			invalidateStrategyDataCache(company);
	}

	@Override
	public void invalidateStrategyDataCache(Company company) {
		invalidateStrategyDataCache(CacheObjectLevelEnum.COMPANY.getObjectLevel(), company.getCode());
	}

	private void invalidateBenPlanAndPlanRates(Company company) {
		if (company.isBandCodeUpdated() || company.isRegionsUpdated() || company.isPlanYearChanged()) {
			Set<Long> ids = new HashSet<>();
			ids.add(company.getId());
			invalidateBenPlansCacheUsingCompIds(ids);
			invalidatePlanRatesCacheUsingCompIds(ids);
		}
	}

	private void invalidateBenPlansCacheUsingCompCode(String level, String value) {
		Set<Long> ids = getCompanyIds(level, value);
		invalidateBenPlansCacheUsingCompIds(ids);
	}

	private void invalidateBenPlansCacheUsingCompIds(Set<Long> ids) {
		Set<String> keys = new HashSet<>();
		keys.addAll(generateKeys(CacheObjectTypeEnum.BEN_PLANS_OBJECT_TYPE, ids));
		deleteFromCache(keys);
	}

	private void invalidatePlanRatesCacheUsingCompCode(String level, String value) {
		Set<Long> ids = getCompanyIds(level, value);
		invalidatePlanRatesCacheUsingCompIds(ids);
	}

	private void invalidateOmsBenefitPlanRatesCacheUsingCompCode(String level, String value) {
		Set<Long> ids = getCompanyIds(level, value);
		invalidateOmsBenefitPlanRatesCacheUsingCompIds(ids);
	}

	private void invalidatePlanRatesCacheUsingCompIds(Set<Long> ids) {
		Set<String> keys = new HashSet<>();
		keys.addAll(generateKeys(CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE, ids));
		deleteFromCache(keys);
	}

	private void invalidateOmsBenefitPlanRatesCacheUsingCompIds(Set<Long> ids) {
		// Cache key format for OMS: CacheObjectType + ":" + id + ":" + benefitType
		Set<String> keys = ids.stream()
				.flatMap(id -> BSSApplicationConstants.PRIMARY_PLAN_TYPE_NAMES.stream().map(
						benefitType -> CacheKeyGenerator.generateCacheKey(
								CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES,
								String.join(":", String.valueOf(id), benefitType)
						)))
				.collect(Collectors.toSet());
		deleteFromCache(keys);
	}

	private void invalidateApplicationCache(String level, String value) {
		invalidateBenPlansCacheUsingCompCode(level, value);
		invalidatePlanRatesCacheUsingCompCode(level, value);
		invalidateOmsBenefitPlanRatesCacheUsingCompCode(level, value);
		invalidateStrategyDataCache(level, value);
		invalidateCompanyData(level, value);
	}
	
	private void invalidateStrategyDataCache(String objectLevel, String value) {
		Set<Long> companyIds = getCompanyIds(objectLevel, value);
		Set<Long> strategyIds = getStrategyIds(objectLevel, value, companyIds);
		Set<String> keys = generateKeys(CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE, strategyIds);
		deleteFromCache(keys);
	}

	private Set<Long> getStrategyIds(String objectLevel, String value, Set<Long> companyIds) {
		Set<Long> strategyIds = new HashSet<>();
		if (CacheObjectLevelEnum.STRATEGY.getObjectLevel().equals(objectLevel)) {
			strategyIds.add(Long.parseLong(value));
		}
		if (CollectionUtils.isNotEmpty(companyIds)) {
			companyIds.forEach(companyId -> {
				List<Strategy> strategies = strategyService.getAllStrategies(companyId);
				if (CollectionUtils.isNotEmpty(strategies)) {
					strategyIds.addAll(strategies.stream().map(Strategy::getId).collect(Collectors.toSet()));
				}
			});
		}
		return strategyIds;
	}
	
	private void invalidateCompanyData(String objectLevel, String value) {
		Set<String> keys = new HashSet<>();
		Set<String> companyCodes = getCompanyCodes(objectLevel, value);
		for (String companyCode : companyCodes) {
			keys.add(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.BASIC_COMPANY_DETAILS, companyCode));
		}
		deleteFromCache(keys);
	}
	
	private Set<String> getCompanyCodes(String objectLevel, String value) {
		Set<String> companyCodes = new HashSet<>();
		List<Company> companies = null;
		if (CacheObjectLevelEnum.REALM_PLAN_YEAR.getObjectLevel().equals(objectLevel)) {
			companies = companyDao.findByRealmPlanYearId(Long.valueOf(value));
			if (CollectionUtils.isNotEmpty(companies)) {
				for (Company company : companies) {
					companyCodes.add(company.getCode());
				}
			}
		} else if (CacheObjectLevelEnum.COMPANY.getObjectLevel().equals(objectLevel)) {
			companyCodes.add(value);
		}
		return companyCodes;
	}

	private Set<Long> getCompanyIds(String objectName, String value) {
		List<Company> companies = null;
		Set<Long> ids = null;

		if (CacheObjectLevelEnum.REALM_PLAN_YEAR.getObjectLevel().equals(objectName)) {
			companies = companyDao.findByRealmPlanYearId(Long.valueOf(value));
		} else if (CacheObjectLevelEnum.COMPANY.getObjectLevel().equals(objectName)) {
			companies = companyDao.findByCode(value);
		}
		if (CollectionUtils.isNotEmpty(companies)) {
			ids = new HashSet<>(companies.size());
			for (Company company : companies) {
				ids.add(company.getId());
			}
		} else {
			ids = new HashSet<>(1);
		}
		return ids;
	}

	private Set<String> generateKeys(CacheObjectTypeEnum objectType, Set<Long> ids) {
		Set<String> keys = new HashSet<>();
		for (Long id : ids) {
			keys.add(CacheKeyGenerator.generateCacheKey(objectType, String.valueOf(id)));
		}
		return keys;
	}

	private boolean deleteFromCache(Set<String> keysToDelete) {
		boolean result = false;
		if (CollectionUtils.isNotEmpty(keysToDelete)) {
			LOGGER.info("Deleting from cache :: {} ", keysToDelete);
			result = cacheTemplateService.deleteFromCache(keysToDelete);
		}
		return result;
	}

	private void validateRequest(String objectName, String level, String value) {
		if (!Arrays.asList(CacheObjectTypeEnum.values()).stream().map(CacheObjectTypeEnum::getObjectType)
				.collect(Collectors.toSet()).contains(objectName)) {
			throwException(String.format("objectName is invalid : %s", objectName));
		}
		if (!Arrays.asList(CacheObjectLevelEnum.values()).stream().map(CacheObjectLevelEnum::getObjectLevel)
				.collect(Collectors.toSet()).contains(level)) {
			throwException(String.format("level is invalid : %s", level));
		}
		if ((CacheObjectLevelEnum.COMPANY.getObjectLevel().equals(level)
				|| CacheObjectLevelEnum.REALM_PLAN_YEAR.getObjectLevel().equals(level)
				|| CacheObjectLevelEnum.STRATEGY.getObjectLevel().equals(level)) && StringUtils.isEmpty(value)) {
			throwException(String.format("value is required when Level is %s", level));
		}
		if ((CacheObjectLevelEnum.STRATEGY.getObjectLevel().equals(level)
				&& !CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE.getObjectType().equals(objectName))) {
			throwException(String.format("STRATEGY level is only support for STRATEGY_DATA object type"));
		}
	}

	private void throwException(String errorMsg) {
		throw new BSSApplicationException(new Throwable(),
				new BSSApplicationError(BSSErrorResponseCodes.BSS_UNHANDLED_EXCEPTION,
						BSSHttpStatusConstants.INTERNAL_SERVER_ERROR,
						BenefitGroupController.class.getName(), errorMsg, null, null));
	}

}