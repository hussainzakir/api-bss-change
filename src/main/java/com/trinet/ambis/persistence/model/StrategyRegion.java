/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author schaudhari
 *
 */

@Entity
@Table(name = "xbss_strategy_region")
@Data
public class StrategyRegion implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public StrategyRegion() {
		super();
	}

	public StrategyRegion(String region, Strategy strategy) {
		super();
		this.region = region;
		this.strategy = strategy;
	}

	@Id
	@SequenceGenerator(name = "strategyRegionSeq", sequenceName = "XBSS_STRATEGY_REGION_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "strategyRegionSeq")
	private long id;

	@Column(name = "REGION", nullable = false)
	private String region;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "strategy_id", nullable = false)
	private Strategy strategy;

}