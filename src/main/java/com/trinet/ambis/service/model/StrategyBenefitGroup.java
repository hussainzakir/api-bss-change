/**
 * 
 */
package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * @author rvutukuri
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrategyBenefitGroup implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private String name;
	private String type;
	@JsonProperty("isDefault")
	private boolean defaultGroup;
	private String waitingPeriod;
	private String waitPeriodDescr;
	private String status;
	private String benefitProgram;
	private long companyId;
	private long strategyId;
	private long strategyGroupId;
	private BigDecimal percentChange;
	private BigDecimal estimatedTotalCost;
	private long headcount;
	@JsonIgnore
	private String state;
	private List<BenefitOffer> benefitOffers = new ArrayList<>();
	private Map<String, Integer> coverageLevelHeadCounts;
	@JsonIgnore
	private boolean hasVolDental;
	@JsonIgnore
	private boolean hasVolVision;
	
	@JsonProperty("region")
	public String getRegion() {
		return this.state == null ? "All" : this.state;
	}
	
	public void setRegion(String region) {
		this.state = ("All").equals(region) ? null : region;
	}	
}
