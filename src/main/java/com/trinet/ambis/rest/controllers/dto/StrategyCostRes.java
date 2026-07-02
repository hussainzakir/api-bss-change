package com.trinet.ambis.rest.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyCostRes {

	private List<PlanTypeCost> costSummary;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PlanTypeCost {

		private String benefitType;

		private boolean offered;

		private BigDecimal monthlyErCost;

		private BigDecimal monthlyEeCost;

		private BigDecimal monthlyTotalCost;

	}
}