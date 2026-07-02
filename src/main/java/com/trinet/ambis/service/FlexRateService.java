package com.trinet.ambis.service;

import com.trinet.ambis.aop.CacheKey;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.FlexRateResponse;
import com.trinet.ambis.service.model.RateUpdateDto;

import java.util.List;

/**
 * Service facade for retrieving FlexRate plan rates.
 * Initially returns mocked data until remote FlexRate endpoint is available.
 */
public interface FlexRateService {

    /**
     * Retrieve plan rates for either a company or a prospect proposal.
     * CompanyCode and proposalId are calculated internally based on the company information.
     *
     * Validation:
     *  - effectiveDate must be a valid date in format YYYY-MM-DD.
     *
     * @param company company to retrieve rates for
     * @param effectiveDate target effective date (YYYY-MM-DD)
     * @return FlexRateResponse containing plan rates.
     */
    FlexRateResponse getPlanRatesFromCache(@CacheKey(value = "id") Company company, String effectiveDate);

    /**
     * Retrieves plan rates directly from the source, bypassing cache.
     * Calculates companyCode and proposalId internally based on the company information.
     *
     * @param company        Company to retrieve rates for.
     * @param effectiveDate  Effective date in YYYY-MM-DD format.
     * @return FlexRateResponse containing plan rates.
     */
    FlexRateResponse getPlanRatesWithoutCache(Company company, String effectiveDate);

	/**
	 * Processes a rate update event for a company and creates a band update process if needed.
	 *
	 * @param dto RateUpdateDto containing update info
	 * @return true if the rate group was changed, false if unchanged
	 */
	boolean processRateUpdateEvent(RateUpdateDto dto);

	/**
	 * Processes a rate group update for the given company.
	 * Steps:
	 * <ol>
	 *   <li>Invalidate plan rates cache at company level.</li>
	 *   <li>Invalidate strategy data cache for the company.</li>
	 *   <li>Mark the company state to trigger rate/contribution recalculation.</li>
	 *   <li>Sync strategies for the company.</li>
	 *   <li>Persist the new rate group id on {@code XBSS_COMPANY}.</li>
	 * </ol>
	 *
	 * @param company the company context; must include a valid {@code id} and {@code code}
	 * @param rateGroupId the new rate group identifier to persist and apply
	 */
	void processRateGroupUpdate(Company company, String rateGroupId);

	/**
	 * Synchronizes the rate group for the specified company.
	 * This method triggers the necessary processes to ensure that
	 * the company's rate group is updated in downstream systems.
	 *
	 * @param company the company whose rate group needs to be synchronized
	 */
	void syncRateGroupWhenUpdated(Company company);

	/**
	 * Fetches company codes from FlexRate that have rates for the given quarter and planYearStartDate.
	 */
	List<String> getClientsWithRates(String quarter, String planYearStartDate);
}

