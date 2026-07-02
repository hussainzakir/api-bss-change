package com.trinet.ambis.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanRateDto {

	private String planId;

	private String rateTypeCode;

	private Map<String, BigDecimal> tieredCost;

	private Integer carrierAgeLimit;

}
