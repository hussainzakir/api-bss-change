package com.trinet.ambis.service.prospect.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BenefitPlansRes {

	private String benefitTypeCode;

	private String benefitType;

	private List<BenefitPlan> benefitPlans;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@JsonPropertyOrder({ "benefitPlanId", "benefitPlanName", "carrier", "attributes" })
	public static class BenefitPlan {

		private int benefitPlanId;

		private String benefitPlanName;

		private String carrier;

		private List<Attribute> attributes;

		@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
		private List<TierRates> tierRates;

		@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
		private List<AgeBandedRates> ageBandedRates;

	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Attribute {

		private Integer id;

		private String displayName;

		private String value;

		private int displayOrder;

	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class TierRates {

		@JsonProperty("cvgTierCode")
		private String cvgCode;

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
