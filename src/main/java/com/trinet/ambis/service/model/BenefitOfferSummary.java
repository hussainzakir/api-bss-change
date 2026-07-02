package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BenefitOfferSummary implements Serializable {

	private static final long serialVersionUID = 1L;

	String type;
	long groupId;
	String description;
	long headcount;
	long waiverHeadcount;
	BigDecimal estimatedTotalCost;
	BigDecimal currentYearTotalCost;
	BigDecimal bsuppExcessAmount;
	private Map<Long, BigDecimal> minFunding;
	boolean baseFundingRequired;
}
