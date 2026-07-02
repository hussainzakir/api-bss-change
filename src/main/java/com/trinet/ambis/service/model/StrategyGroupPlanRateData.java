/**
 * 
 */
package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * @author hliddle
 *
 */
@Data
public class StrategyGroupPlanRateData implements Serializable {

	private static final long serialVersionUID = 1L;

	private long strategyId;
	private long groupId;
	private String planType;
	private String benefitPlan;
	private String description;
	private String coverageLevel;
	private String coverageLevelName;
	private BigDecimal erContribPercent;
	private BigDecimal erRate;
	private BigDecimal eeRate;
	private String mapReason;
	private boolean mirrorPlanFlag;

}