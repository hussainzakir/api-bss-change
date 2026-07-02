package com.trinet.ambis.rest.controllers.dto.plancompare;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanCompareDetailDto {

	private String benefitType;

	private List<PlanCompareData> planCompareData;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PlanCompareData {

		private BenPlanDetail currentPlan;

		private List<BenPlanDetail> futurePlans;

	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class BenPlanDetail {

		private String planId;

		private String planName;

		private List<Attribute> attributes;

		private List<RateDetail> rates;

	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Attribute {

		private Integer id;

		private String name;

		private String value;

	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class RateDetail {

		private List<String> regionCode;

		private String rateType;

		private List<TierRate> tierRates;

	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class TierRate {

		private String cvgTierCode;

		private BigDecimal cost;

	}

}
