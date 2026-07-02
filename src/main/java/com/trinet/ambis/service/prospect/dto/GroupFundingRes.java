package com.trinet.ambis.service.prospect.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GroupFundingRes {

	@EqualsAndHashCode.Include
	private String benefitType;
	private String fundingType;
	private List<CvgCodeValue> cvgCodeValues;
	private FundingCapDetails fundingCapDetails;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CvgCodeValue {

		private String cvgCode;
		private BigDecimal value;

	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class FundingCapDetails {

		private String capType;
		private Integer capPlanId;
		private String capPlanName;
		private List<CvgCodeValue> cvgCodeValues;
	}

}
