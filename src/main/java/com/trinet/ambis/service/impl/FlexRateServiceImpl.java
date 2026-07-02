package com.trinet.ambis.service.impl;

import com.trinet.ambis.aop.BSSCacheable;
import com.trinet.ambis.aop.CacheKey;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.CacheObjectLevelEnum;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.StrategySyncService;
import com.trinet.ambis.service.model.RateUpdateDto;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.util.*;
import com.trinet.ambis.service.FlexRateService;
import com.trinet.ambis.service.model.FlexRateResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * FlexRateServiceImpl
 *
 * Provides mocked FlexRateResponse data until remote FlexRate API endpoint becomes available.
 * Once available, uncomment the client invocation and remove mock builder.
 *
 * Validation rules (enforced in FlexRateServiceImpl):
 *  - Exactly one of companyCode or proposalId must be provided (mutually exclusive)
 *  - effectiveDate must be provided in YYYY-MM-DD and represent a valid calendar date
 */
@Service
@Slf4j
public class FlexRateServiceImpl implements FlexRateService {

	private static final Logger logger = LoggerFactory.getLogger(FlexRateServiceImpl.class);
	private static final String SOURCE = "FlexRateServiceImpl";

    @Autowired
    private FlexRateRestClient flexRateRestClient;

	@Autowired
	private ProcessStatusService processStatusService;

	@Autowired
	private CompanyService companyService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private StrategySyncService strategySyncService;

	@Autowired
	private CompanyDao companyDao;

	@Autowired
	private StrategyService strategyService;

	@Autowired
	private RealmPlyrPlanService realmPlyrPlanService;

	@Autowired
	private StrategyDao strategyDao;

    @Override
    @BSSCacheable(objectType = CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE, ttl = BSSApplicationConstants.TTL_FOR_FLEX_RATES_PLAN_RATES)
    public FlexRateResponse getPlanRatesFromCache(@CacheKey(value = "id") Company company, String effectiveDate) {
        return getPlanRatesWithoutCache(company, effectiveDate);
    }

    @Override
    public FlexRateResponse getPlanRatesWithoutCache(Company company, String effectiveDate) {
        // Perform validation synchronously so exceptions propagate directly (tests expect direct BSSApplicationException)
        log.info("############### Fetching FlexRateResponse from Flex Rate Engine ###############");

        // Calculate companyCode and proposalId based on company type
        String companyCode = company.isProspectCompany() ? null : company.getCode();
        String proposalId = company.isProspectCompany() ? company.getProposalId() : null;
        
        boolean hasCompany = companyCode != null && !companyCode.isEmpty();
        boolean hasProposal = proposalId != null && !proposalId.isEmpty();
        if (hasCompany == hasProposal) {
            throw validationError("Provide either companyCode OR proposalId. Both can't be provided");
        }
        // Validation: effectiveDate in YYYY-MM-DD format
        if (!DateUtils.isIsoDate(effectiveDate)) {
            throw validationError("effectiveDate must be in YYYY-MM-DD format");
        }
        var myBean = new ParameterizedTypeReference<FlexRateResponse>() {};
	    String bundleId = company.getBundleId() == null ? "" : String.valueOf(company.getBundleId());
        FlexRateResponse flexRateResponse = hasCompany
                ? flexRateRestClient.getPlanRatesByCompanyCode(companyCode, effectiveDate,bundleId, myBean)
                : flexRateRestClient.getPlanRatesByProposalId(proposalId, effectiveDate,bundleId, myBean);

		if (flexRateResponse == null) {
			throw new BSSBadDataException(String.format("FlexRateService returned null response for company id: %s, code: %s",
                        company.getId(), company.getCode()));
		}
		if (flexRateResponse.getRateGroupId() == null) {
			throw new BSSBadDataException(String.format("FlexRateService returned null rateGroupId for company id: %s, code: %s",
                        company.getId(), company.getCode()));
		}
		validateAndFilterResponse(company, flexRateResponse);
		return flexRateResponse;
    }

	@Override
	public boolean processRateUpdateEvent(RateUpdateDto dto) {
		String companyCode = dto.getCompanyCode();
		String prospectId = dto.getProspectId();
		String proposalId = dto.getProposalId();
		String newRateGroupId = dto.getRateGroupId();

		// Validate that either companyCode OR (prospectId AND proposalId) are provided
		boolean hasCompanyCode = companyCode != null && !companyCode.isEmpty();
		boolean hasProspectAndProposal = (prospectId != null && !prospectId.isEmpty()) 
				&& (proposalId != null && !proposalId.isEmpty());

		if (!hasCompanyCode && !hasProspectAndProposal) {
			throw validationError("Provide either companyCode OR both prospectId and proposalId");
		}

		String companyIdentifier = hasCompanyCode ? companyCode : prospectId;
		if (pendingQuarterChangeUpdate(companyIdentifier)){
			return true;
		}
		Company company = companyService.findCompanyByQuarterAndEffDate(
				Utils.convertStringToDate(dto.getEffectiveDate(),
						BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD), dto.getQuarter(),companyIdentifier);

		if (company == null) {
			logger.error("Company not found for code: {}", companyIdentifier);
			return false;
		}

		if (!RiskTypeEnum.DIFFERENTIALS.equals(company.getRiskType())) {
			logger.error("Company riskType is not DIFFERENTIALS (found: {}) for company id: {}", company.getRiskType(), company.getId());
			return false;
		}
		String previousRateGroupId = company.getRateGroupId();
		if (Objects.equals(previousRateGroupId, newRateGroupId)) {
			return false;
		}
		return handleBandUpdateProcess(company);
	}

	private boolean pendingQuarterChangeUpdate(String companyIdentifier) {
		// Check for quarter-change condition: client company with pending QUARTER_CHANGE
		List<ProcessStatus> quarterChangeRecords = processStatusService.findPendingQuarterChangeProcesses(companyIdentifier);
		if (!CollectionUtils.isEmpty(quarterChangeRecords)) {
			processStatusService.createStrategySyncProcess(
					companyIdentifier,
					quarterChangeRecords.get(0).getProcessData(),
					ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName(),
					ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getIdentifierName());
			return true;
		}
		return false;
	}

	/**
	 * Handles the existing band update process creation logic.
	 *
	 * @param company the company to create the band update process for
	 * @return true if the process was created successfully
	 */
	private boolean handleBandUpdateProcess(Company company) {
		try {
			List<Strategy> existingStrategies = strategyDao.findByCompanyIdAndStatus(company.getId(), BSSApplicationConstants.STATUS_ACTIVE);
			if (existingStrategies != null && !existingStrategies.isEmpty()) {
				processStatusService.createBandUpdateProcess(
						company.getRealmPlanYear().getRealmId(),
						company.getCode(),
						company.getId()
				);
				return true;
			}
			logger.error("No Strategies are available for company {}", company.getCode());
			return false;
		} catch (Exception e) {
			logger.error("Failed to create band update process for company [{}] (id: {}): {}", company.getCode(), company.getId(), e.getMessage(), e);
			String errorMessage = String.format("Failed to create band update process for company [%s] (id: %s): %s", company.getCode(), company.getId(), e.getMessage());
			throw new BSSBadDataException(errorMessage);
		}
	}

	@Override
	@Transactional
	public void processRateGroupUpdate(Company company, String rateGroupId) {
		// Invalidate caches for plan rates
		cacheService.invalidateCache(
				CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE.getObjectType(),
				CacheObjectLevelEnum.COMPANY.getObjectLevel(),
				company.getCode());

		// Only perform the rest of the logic if the company has strategies
		List<Strategy> strategies = strategyService.getAllStrategies(company.getId());

		if (strategies.isEmpty()) {
			logger.info("No active strategies found for company code: {}. Skipping strategy sync.", company.getCode());
			return;
		}
		cacheService.invalidateStrategyDataCache(company);

		// Mark company as having updated rates to trigger contribution recalculation
		company.setRatesUpdated(true);
		// Set the new rate group id on the entity
		company.setRateGroupId(rateGroupId);

		// Sync strategies for the company
		strategySyncService.syncStrategyData(company, null);

		// Persist RATE_GROUP_ID on XBSS_COMPANY
		companyDao.saveAndFlush(company);

		company.setRatesUpdated(false);
	}

	@Override
	@Transactional
	public void syncRateGroupWhenUpdated(Company company) {
		String effectiveDate = CommonUtils.formatDate(company.getPlanStartDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY, BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD);
		FlexRateResponse response = getPlanRatesWithoutCache(company, effectiveDate);
		String latestRateGroupId = response.getRateGroupId();
		if (latestRateGroupId != null && !latestRateGroupId.equals(company.getRateGroupId())) {
			processRateGroupUpdate(company, latestRateGroupId);
		}
	}


	@Override
	public List<String> getClientsWithRates(String quarter, String planYearStartDate) {
		return flexRateRestClient.getClientsWithRates(quarter, planYearStartDate);
	}

    private BSSApplicationException validationError(String msg) {
        return new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.REQUEST_VALIDATION_ERROR, 400, SOURCE, msg, "", null));
    }

	/**
	 * Filters the plans in the FlexRateResponse to only include those that are part of the realm plan year for the company.
	 * Then ensures that all M/D/V plans are returned associated with the realm plan year are included in the response, throwing an exception if any are missing.
	 *
	 * @param company The company for which to filter the plans
	 * @param flexRateResponse The original FlexRateResponse containing all plans
	 */
	private void validateAndFilterResponse(Company company, FlexRateResponse flexRateResponse) {

		List<XbssRealmPlyrPlan> allPlans = realmPlyrPlanService.getForRealmPlanYear(company.getRealmPlanYear().getId());
		List<XbssRealmPlyrPlan> mdvPlans = allPlans.stream()
		        .filter(plan -> BSSApplicationConstants.PRIMARY_PLAN_TYPES.contains(plan.getPlanType()))
		        .collect(Collectors.toList());

		List<String> planYearPlanIds = mdvPlans.stream()
				.map(XbssRealmPlyrPlan::getBenefitPlan)
				.collect(Collectors.toList());

		if (flexRateResponse.getPlansByBenefitType() != null) {
			List<FlexRateResponse.PlanByBenefitType> filtered = flexRateResponse.getPlansByBenefitType().stream()
					.map(plansByBenefitType -> {
						List<FlexRateResponse.PlansByPlanType> filteredPlansByPlanType = plansByBenefitType.getPlansByPlanType().stream()
								.map(plansByPlanType -> {
									List<com.trinet.ambis.service.model.PlanRate> filteredPlans = plansByPlanType.getPlans().stream()
											.filter(planRate -> planYearPlanIds.contains(planRate.getRegionalPlanId()))
											.collect(Collectors.toList());
									plansByPlanType.setPlans(filteredPlans);
									return plansByPlanType;
								})
								.collect(Collectors.toList());
						plansByBenefitType.setPlansByPlanType(filteredPlansByPlanType);
						return plansByBenefitType;
					})
					.collect(Collectors.toList());
			flexRateResponse.setPlansByBenefitType(filtered);
		}

		// Ensure that all of the plans in planYearPlanIds are represented in the response
		List<String> missingPlans = planYearPlanIds.stream()
				.filter(planId -> flexRateResponse.getPlansByBenefitType().stream()
						.flatMap(plansByBenefitType -> plansByBenefitType.getPlansByPlanType().stream())
						.flatMap(plansByPlanType -> plansByPlanType.getPlans().stream())
						.noneMatch(planRate -> planRate.getRegionalPlanId().equals(planId)))
				.collect(Collectors.toList());

		if (!missingPlans.isEmpty()) {
			String errorMessage = String.format(
					"The following plans are configured for realm plan year %s but were missing from the Flex Rate Response for company %s: %s",
					company.getRealmPlanYear().getId(),
					company.getCode(),
					String.join(", ", missingPlans));
			if (errorMessage.length() > 2000) {
				errorMessage = errorMessage.substring(0, 2000) + "...(truncated)";
			}
			logger.error(errorMessage);
			throw new BSSBadDataException(errorMessage);
		}

	}

}
