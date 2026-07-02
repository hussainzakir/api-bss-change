package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

/**
 * @author pallu
 *
 */
@Data
@Builder
public class StrategyGroupEmployeePlanRateData implements Serializable {

	private static final long serialVersionUID = 1L;

	private String emplId;
	private long strategyId;
	private long groupId;
	private String groupName;
	private String planType;
	private String benefitPlan;
	private String planName;
	private String coverageCode;
	private BigDecimal erRate;
	private BigDecimal eeRate;
	private String carrier;

}