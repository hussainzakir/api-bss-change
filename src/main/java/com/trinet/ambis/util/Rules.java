package com.trinet.ambis.util;

import java.math.BigDecimal;
import java.util.Set;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.MinFundExceptionService;
import com.trinet.ambis.service.model.MinFundExceptionDto;
import com.trinet.ambis.service.model.MinimumFunding;

public class Rules {
	
	private Rules() {
		throw new IllegalStateException(
				"Utility class " + Rules.class.getName() + " can not be instantiated.");
	}

    public static BigDecimal getAnnualCap(String psPlanId) {
        BigDecimal annualCap = BigDecimal.ZERO;
        if (psPlanId != null) {
	        if (psPlanId.equals("000TM9"))
	            annualCap = BigDecimal.valueOf(50000);
	        else if (psPlanId.equals("000SRO"))
	            annualCap = BigDecimal.valueOf(200000);
	        else if (psPlanId.equals("000TMA"))
	            annualCap = BigDecimal.valueOf(400000);
	        else if (psPlanId.equals("000TMB"))
	            annualCap = BigDecimal.valueOf(750000);
            else if ( psPlanId.equals("000SRT") || psPlanId.equals("000TMF"))
	            annualCap = BigDecimal.valueOf(250000);
        }
        return annualCap;
    }
    
    /**
     * This method overrides the minimum funding PCT for TNIII having HQ in FL and TX.
     * @param company
     */
	public static void overrideMiniumFunding(Company company) {
		if (BenExchngEnums.TRINET_III.getBenExchng().equals(company.getRealm().getBenExchange())
				&& BSSApplicationConstants.MIN_FUNDING_OVERRIDE_STATES_PAS.contains(company.getHeadQuatersState())) {
			company.setDefaultMinFundingPct(BSSApplicationConstants.MIN_FUNDING_OVERRIDE_PCT_PAS.longValue());
			for (MinimumFunding minFunding : company.getMinFundings()) {
				minFunding.setMinFundType(MinFundExceptionService.PERCENT);
				minFunding.setMinFundValue(BSSApplicationConstants.MIN_FUNDING_OVERRIDE_PCT_PAS);
			}
		}
	}

	public static void overrideExceptionMiniumFunding(Company company, Set<MinFundExceptionDto> minFundExceptions) {
		for (MinFundExceptionDto minFundExceptionDto : minFundExceptions) {
			MinimumFunding minFundException = new MinimumFunding(
					PlanTypesEnum.getName(minFundExceptionDto.getPlanType()), minFundExceptionDto.getMinFundType(),
					minFundExceptionDto.getMinFundValue(), true);
			company.getMinFundings().remove(minFundException);
			company.getMinFundings().add(minFundException);
		}
	}
}
