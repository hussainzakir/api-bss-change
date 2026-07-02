package com.trinet.ambis.service.prospect.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data object to handle the benefit plans
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenefitsPlansRatesRes {

	private String benefitTypeCode;

	private String benefitPlanId;

	private List<TierRates> tierRates;

	private List<AgeBandedRates> ageBandedRates;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class TierRates {

		private String cvgTierCode;

		private BigDecimal cost;

	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class AgeBandedRates {

		private String ageBandCode;

		private BigDecimal cost;

	}

}