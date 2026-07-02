/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;
import java.util.Map;

import com.trinet.ambis.service.model.PlanRate;
import org.apache.poi.ss.usermodel.Workbook;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.PlanRatesExportData;

/**
 * @author hliddle
 *
 */

public interface PlanRatesService {

	/**
	 * Returns a PlanRatesExportData object that includes all plan and rate
	 * information for the passed in company's current and future plan years
	 * 
	 * @param futureCompany
	 * @return
	 */
	PlanRatesExportData getPlanRatesExportData(Company futureCompany);

    Workbook getPlanRatesExcelWorkbook(Company company, PlanRatesExportData planRatesExportData, String hiddenColumns);

	/**
	 * This method returns a map of coverage level rates for all the benefit plans
	 * mapped to benefitPlan for given company.
	 *
	 * @param company
	 * @return
	 */
	Map<String, List<BenefitPlanRate>> getBenefitPlanRatesBy(Company company);
    
    /**
     * Returns benefit plan rates for a company, with optional DP tier filtering.
     *
     * @param company company context
     * @param includeDpRates true returns all tiers (1,2,C,4,5,6,7,8), false excludes DP tiers (returns only 1,2,C,4)
     * @return map of plan id to list of rates
     */
    Map<String, List<BenefitPlanRate>> getBenefitPlanRatesBy(Company company, boolean includeDpRates);
    /**
     * Retrieves benefit plan rates for the given company as a map of plan type to PlanRates.
     * Determines band codes, matches rates by coverage level.
     *
     * @param company
     * @return
     */
    Map<String, List<PlanRate>> getBenefitPlanRatesByBenefitType(Company company);

}
