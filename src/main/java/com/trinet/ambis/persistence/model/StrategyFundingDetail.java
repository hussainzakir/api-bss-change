/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Getter;
import lombok.Setter;

/**
 * @author rvutukuri
 *
 */
@Entity
@Table(name = "xbss_strategy_funding_detail")
@Getter
@Setter
public class StrategyFundingDetail implements Serializable {
	private static final long serialVersionUID = 1L;

	@JsonUnwrapped
	@EmbeddedId
	StrategyFundingDetailId sfDetailId;
	@Column(name = "contribution")
	private BigDecimal contribution;

	@MapsId("strategyFundingId")
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "strategy_funding_id", insertable = false, updatable = false)
	private StrategyFundingModel strategyFundingModel;

}
