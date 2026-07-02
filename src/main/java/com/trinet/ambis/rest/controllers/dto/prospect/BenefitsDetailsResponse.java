package com.trinet.ambis.rest.controllers.dto.prospect;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BenefitsDetailsResponse {

	private int groupId;

	private String groupName;

    private String groupType;

	private int headCount;

	private BigDecimal monthlyTotal;

	private List<BenefitType> benefitTypes;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class BenefitType {

		private String benefitTypeCode;

		private BigDecimal monthlyTotal;

		private List<String> planCarriers;

		private FundingDetails fundingDetails;
		
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class FundingDetails {

		private String fundingType;

		private Map<String, BigDecimal> cvgCodeValues;
		
		private FundingCapDetails fundingCapDetails;

	}
	
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class FundingCapDetails {

		private String capType;

		private String capPlanId;

		private String capPlanName;

		private Map<String, BigDecimal> cvgCodeValues;

	}

}
