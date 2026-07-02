package com.trinet.ambis.rest.controllers.dto.prospect;

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
public class ProspectBenefitsSummaryTotalsResponse {

	String exchange;
	String strategyName;
	String type;
	String proposalId;
	String bundleId;
	String bundleName;
	List<Quotes> quotes;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Quotes {

		private String benefitType;

		private List<PlanDetails> planDetails;

		private TotalCostDetails totalCostDetails;

	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PlanDetails {

		private String planId;

		private String name;

		private String carrierName;

	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class TotalCostDetails {

		private BigDecimal totalAnnualCost;

		private BigDecimal employeeTotalCost;

		private BigDecimal employerTotalCost;

		private Long employeeCount;

		private List<MonthlyCostByEmployee> monthlyCostByEmployee;

	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class MonthlyCostByEmployee {

		private String eeIdentifier;

		private String planId;

		private BigDecimal employee;

		private BigDecimal employer;

		private BigDecimal total;

	}
}
