/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.NoArgsConstructor;

/**
 * @author kpamulapati
 *
 */
@Embeddable
@NoArgsConstructor
public class StrategyFundingId implements Serializable {

	private static final long serialVersionUID = 1L;
	@Column(name="plan_type")
	private String planType;
	@Column(name="group_id")
	private long groupId;
	@Column(name="strategy_id")
	private long strategyId;
	
	@Column(name="plan_type")
	public String getPlanType() {
		return planType;
	}
	public void setPlanType(String planType) {
		this.planType = planType;
	}
	
	@Column(name="group_id")
	public long getGroupId() {
		return groupId;
	}
	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}
	
	@Column(name="strategy_id")
	public long getStrategyId() {
		return strategyId;
	}
	public void setStrategyId(long strategyId) {
		this.strategyId = strategyId;
	}
	
}
