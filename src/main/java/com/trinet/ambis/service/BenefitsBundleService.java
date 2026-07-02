package com.trinet.ambis.service;


import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.trinet.ambis.persistence.model.Bundle;
import com.trinet.ambis.rest.controllers.dto.BundlePlanResponse;
import com.trinet.ambis.rest.controllers.dto.BundleSelectionDetailsRequest;
import com.trinet.ambis.service.model.BenefitsBundleDto;
import com.trinet.ambis.service.model.BundleSelectionDetailsDto;
import com.trinet.ambis.rest.controllers.dto.BundlePlansDto;


public interface BenefitsBundleService {
    /**
     * @param oeQuarter
     * @param effectiveDate
     * @return
     */
    List<BenefitsBundleDto> getBundleDetails(String oeQuarter, String effectiveDate);
    
    Bundle getBundleById(long id);

    String getCustomBundleCreatedStatus(String companyCode, String type);

    /**
     * @param companyCode
     * @return
     */
    Bundle getBundleByCompanyCode(String companyCode);

    /**
     * @param bundle
     */
    void save(Bundle bundle);

    /**
     * Retrieves benefit plans for a given company scoped to general availability bundles and the full exchange.
     * <p>
     * If {@code bundleIds} is provided and non-empty, only the plans belonging to those bundles are returned.
     * If {@code bundleIds} is null or empty, all GA bundle plans specific to company are returned.
     *
     * @param companyCode the company code used to resolve the exchange context
     * @param exchangeId  the OE quarter / exchange identifier
     * @param bundleIds   optional set of bundle IDs to filter plans; pass null or empty to return all exchange plans
     * @return list of {@link BundlePlansDto} containing plan details, each entry including the bundle ID
     *         (if applicable) and its associated plans
     */
    List<BundlePlansDto> getBundleAndExchangePlans(String companyCode, String exchangeId, Set<Long> bundleIds);

    /**
     * Gets bundle selection details for a company
     * @param request the bundle selection details request containing companyCode, exchanges, quarter, and effectiveDate
     * @return list of BundleSelectionDetailsDto
     */
    List<BundleSelectionDetailsDto> getBundleSelectionDetails(BundleSelectionDetailsRequest request);


    /**
     * Retrieves bundle information along with associated regional plan IDs for a given effective date and quarter.
     *
     * @param effectiveDate the effective date
     * @param quarter       the OE quarter (e.g. Q1, Q2, Q3, Q4)
     * @return BundlePlanResponse containing bundles with their plan IDs
     */
    BundlePlanResponse getBundlesByEffectiveDateAndQuarter(LocalDate effectiveDate, String quarter);
}
