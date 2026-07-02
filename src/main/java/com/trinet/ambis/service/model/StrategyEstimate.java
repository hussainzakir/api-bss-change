/**
 * 
 */
package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;

import lombok.ToString;

/**
 * @author hliddle
 *
 */
@ToString
public class StrategyEstimate implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "STRATEGY_ID")
	private long strategyId;
	@Column(name = "GROUP_ID")
	private long groupId;
	@Column(name = "PLAN_TYPE")
	private String planType;
	@Column(name = "PLAN_SUB_TYPE")
	private String planSubType;
	@Column(name = "ESTIMATE")
	private BigDecimal estimate;

	public StrategyEstimate() {
	}

	@Column(name = "STRATEGY_ID")
	public long getStrategyId() {
		return strategyId;
	}

	public void setStrategyId(long strategyId) {
		this.strategyId = strategyId;
	}

	@Column(name = "GROUP_ID")
	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	@Column(name = "PLAN_TYPE")
	public String getPlanType() {
		return planType;
	}

	public void setPlanType(String planType) {
		this.planType = planType;
	}

	@Column(name = "PLAN_SUB_TYPE")
	public String getPlanSubType() {
		return planSubType;
	}

	public void setPlanSubType(String planSubType) {
		this.planSubType = planSubType;
	}

	@Column(name = "ESTIMATE")
	public BigDecimal getEstimate() {
		return estimate;
	}

	public void setEstimate(BigDecimal estimate) {
		this.estimate = estimate;
	}

}