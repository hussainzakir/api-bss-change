/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
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
@Table(name = "XBSS_STRATG_FUND_BSUPP_PLN_TP")
@Getter
@Setter
public class StrategyFundBsuppPlanTypes implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonUnwrapped
	@EmbeddedId
	StrategyFundBsuppPlanTypeId strategyFundBsuppPlanTypeId;

	@MapsId("strategyFundingId")
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "strategy_funding_id", insertable = false, updatable = false)
	private StrategyFundingModel strategyFundingModel;

}
