package com.trinet.ambis.common;

import java.util.EnumSet;
import java.util.Set;

/**
 * An enumeration of the different types of RATE_TBL_ID that are used by the BSS application.
 * Certain rate types (but not all) have additional parameters that can be used to match the
 * rate ID to other related fields.
 * @author mbrothers
 *
 */
public enum BSSRateType {
   
	MEDICAL( "10" ),
	DP_MEDICAL( "15" ),
	OTHER( "OTHER" ),
	WAIVER_ALLOWANCE( "WAIVE" ),
	BEN_SUPP_EE( "BSUP1", "1", "BS_EE" ),
	BEN_SUPP_SP( "BSUP2", "2", "BS_SP" ),
	BEN_SUPP_DEP( "BSUPC", "C", "BS_DEP" ),
	BEN_SUPP_FAM( "BSUP4", "4", "BS_FAM" ),
	BEN_SUPP_NQ_ADULT( "BSUP5", "5", "BS_NQ5" ),
	BEN_SUPP_NQ_CHILD( "BSUP6", "6", "BS_NQ6" ),
	BEN_SUPP_NQ_ADULT_CHILD( "BSUP7", "7", "BS_NQ7" ),
	BEN_SUPP_FAM_NQ_ADULT( "BSUP8", "8", "BS_NQ8" ),
	BEN_SUPP_OTHER( "BSOTH", null, "BS_OTH" );

	private final String rateIdType;
	private final String covrgCd;
	private final String rateDescr;

	BSSRateType( String rateIdType, String covrgCd, String rateDescr ) {
		this.rateIdType = rateIdType;
		this.covrgCd = covrgCd;
		this.rateDescr = rateDescr;
	}

	BSSRateType( String rateIdType ) {
		this( rateIdType, null, null );
	}

	public String rateIdType() {
		return this.rateIdType;
	}

	public String covrgCd() {
		return this.covrgCd;
	}

	public String rateDescr() {
		return this.rateDescr;
	}

	private static final Set<BSSRateType> HealthSet = EnumSet.of(MEDICAL, DP_MEDICAL, OTHER);

	/**
	 * A set of rate types that are used for health benefits.  This inclues medical, dental, vision,
	 * and the corresponding Domestic Partner coverages.
	 */
	public static Set<BSSRateType> getHealthRateSet() {
		return HealthSet;
	}

	private static final Set<BSSRateType> BenefitSupplementSet = EnumSet.of(BEN_SUPP_EE, BEN_SUPP_SP, BEN_SUPP_DEP,
			BEN_SUPP_FAM, BEN_SUPP_NQ_ADULT, BEN_SUPP_NQ_CHILD, BEN_SUPP_NQ_ADULT_CHILD, BEN_SUPP_FAM_NQ_ADULT);

	/**
	 * A special set of rate types defining the set used for benefit supplement
	 * funding rates. This set can be used by a looping construct to take some
	 * action on each rate type.
	 */
	public static Set<BSSRateType> getBenefitSupplementSet() {
		return BenefitSupplementSet;
	}
}