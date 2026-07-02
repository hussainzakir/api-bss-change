/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

/**
 * @author rvutukuri
 *
 */
@Embeddable
@Data
public class StrategyFundBsuppPlanTypeId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "strategy_funding_id")
	private Long strategyFundingId;
	@Column(name = "plan_type")
	private String planType;

	@Column(name = "strategy_funding_id")
	public Long getStrategyFundingId() {
		return strategyFundingId;
	}
}
