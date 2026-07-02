/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;

/**
 * @author rvutukuri
 *
 */
@Embeddable
@Getter
@Setter
public class StrategyFundingDetailId implements Serializable {

	private static final long serialVersionUID = 1L;
	@Column(name = "strategy_funding_id")
	private Long strategyFundingId;
	@Column(name = "coverage_id")
	private String coverageId;

}
