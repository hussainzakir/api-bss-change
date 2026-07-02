package com.trinet.ambis.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.IndustryType;
import com.trinet.ambis.enums.US;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.BenefitPlanRate;

public class StrategyUtils {
	
	private static final String OTHERS = "Others";

	private StrategyUtils() {
	}

	/**
	 * This version of <code>findBandCode</code> uses the band locator, derived from the COWEB_BANDNG table
	 * to get a more accurate band code than by using the vendor ID alone.
	 * @param company A Company object with its BandCodes object instantiated and populated
	 * @param benefitPlan A String representing a PeopleSoft benefit plan
	 * @param plyrPlanMap A Map of benefit plan codes to an XbssRealmPlyrPlan object containing a band locator
	 * @return the client's banded value for this benefit plan code
	 */
	public static String findBandCode(Company company, String benefitPlan, Map<String,XbssRealmPlyrPlan> plyrPlanMap ) {
		String bandCode;
		BandCodes bndCds = company.getBandCodes();
		
		// find plan in plyr map
		XbssRealmPlyrPlan props = plyrPlanMap.get( benefitPlan );

		if( props == null || props.getBandLocator() == null ) {
			bandCode = "N";
		} else {
			// Select one of the company band code values depending upon the band locator
			switch( props.getBandLocator() ) {
			case "4":
				bandCode = bndCds.getKaiserBandCode();
				break;
			case "5":
				bandCode = bndCds.getKaisCoBandCode();
				break;
			case "6":
				bandCode = bndCds.getBsOfCaBandCode();
				break;
			case "7":
				bandCode = bndCds.getAetnaBandCode();
				break;
			case "8":
				bandCode = bndCds.getTuftsBandCode();
				break;
			case "9":
				bandCode = bndCds.getKaisNwBandCode();
				break;
			case "A":
				bandCode = bndCds.getBcbsBandCode();
				break;
			case "B":
				bandCode = bndCds.getBcbsNcBandCode();
				break;
			case "C":
				bandCode = bndCds.getUhcBandCode();
				break;
			case "D":
				bandCode = bndCds.getBcOfIdBandCode();
				break;
			case "E":
				bandCode = bndCds.getKaiMidAtlBandCode();
				break;
			case "F":
				bandCode = bndCds.getBcbsMNBandCode();
				break;
			case "G":
				bandCode = bndCds.getKaiHawaiiBandCode();
				break;
			case "H":
				bandCode = StringUtils.isBlank( bndCds.getAetnaHmoBandCode() ) ? bndCds.getAetnaBandCode() : bndCds.getAetnaHmoBandCode();
				break;
			case "I":
				bandCode = StringUtils.isBlank( bndCds.getAetnaPpoBandCode() ) ? bndCds.getAetnaBandCode() : bndCds.getAetnaPpoBandCode();
				break;
			case "J":
				bandCode = bndCds.getEmpireNYBand();
				break;
			case "L":
				bandCode = bndCds.getHarvardBandCode();
				break;
			case "M":
				bandCode = bndCds.getHighmarkBandCode();
				break;
			default:
				bandCode = evaluateLifeDisability( bndCds, props );
			}
		}
		return bandCode;
	}
	

	/** 
	 * This is part of the band code derivation.  This has been split from <code>findBandCode</code>
	 * to prevent SonarQube from becoming too confused.
	 * @return
	 */
	private static String evaluateLifeDisability( BandCodes bandCodes, XbssRealmPlyrPlan plyrPlan ) {
		String bandCode;
		if( BSSApplicationConstants.getDisabilityPlanTypes().contains( plyrPlan.getPlanType() ) ) {
			bandCode = bandCodes.getDisBandCode();
		} else if( BSSApplicationConstants.getLifePlanTypes().contains( plyrPlan.getPlanType() ) ) {
			bandCode = bandCodes.getLifeBandCode();
		} else {
			bandCode = "N";
		}
		return bandCode;
	}

    /**
     * Returns plan costs for a given benefit plan using a list of rates.
     *
     * @param rates List of BenefitPlanRate objects for a single benefit plan
     * @return Map of coverageCode to employer cost
     * @throws IllegalArgumentException if rates have inconsistent planIds
     */
    public static Map<String, BigDecimal> getPlanCost(List<BenefitPlanRate> rates) {
        if (rates == null || rates.isEmpty()) {
            return new HashMap<>();
        }
        // Validate all rates have the same planId and bandcode
        String planId = rates.get(0).getBenefitPlan();
        String bandCode = rates.get(0).getBandCode();
        if (rates.stream().anyMatch(r -> !planId.equals(r.getBenefitPlan()))) {
            throw new IllegalArgumentException("All rates must have the same planId.");
        }
        if (rates.stream().anyMatch(r -> !Objects.equals(bandCode, r.getBandCode()))) {
            throw new IllegalArgumentException("All rates must have the same bandCode.");
        }
        return rates.stream()
                .filter(java.util.Objects::nonNull)
                .map(rate -> Map.entry(CoverageCodesEnums.valueOfId(rate.getCoverageCode()), rate.getEmployerCost()))
                .filter(entry -> entry.getKey() != null)
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

	public static String getRegion(String state, String industry) {
		String derivedState = null;
		IndustryType industryTypeEnum = null;
		for (IndustryType industryType : IndustryType.values()) {
			if (industryType.getType().equals(industry)) {
				industryTypeEnum = industryType;
				break;
			}
		}
		if (IndustryType.IndustryTypeAmbroseSet.contains(industryTypeEnum)) {
			derivedState = getDerivedStateForAmbroseIndustryType(state);
		} else if (null != industryTypeEnum && (IndustryType.IndustryTypeSOISet.contains(industryTypeEnum)
				|| IndustryType.IndustryTypeSOInoMedSet.contains(industryTypeEnum)
				|| IndustryType.IndustryTypePassportSet.contains(industryTypeEnum))) {
			List<String> states = industryTypeEnum.getStates();
			US stateOfTheUNion = US.parse(state);
			if (states.contains(stateOfTheUNion.getANSIabbreviation())) {
				derivedState = stateOfTheUNion.getUnnabreviated();
			} else {
				derivedState = OTHERS;
			}
		} else {
			US stateOfTheUNion = US.parse(state);
			if (state.equals(stateOfTheUNion.getANSIabbreviation())) {
				derivedState = stateOfTheUNion.getUnnabreviated();
			} else {
				derivedState = OTHERS;
			}
		}
		return derivedState;
	}

	private static String getDerivedStateForAmbroseIndustryType(String state) {
		switch (state) {
		case "CA":
			return "California";
		case "NY":
			return "NewYork";
		case "NV":
			return "Nevada";
		case "PR":
			return "PuertoRico";
		case "HI":
			return "Hawaii";
		default:
			return OTHERS;
		}
	}

}
