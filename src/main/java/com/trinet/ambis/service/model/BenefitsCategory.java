/**
 * 
 */
package com.trinet.ambis.service.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author khinton
 *
 */
@ToString
@Data
public class BenefitsCategory {
	@JsonInclude(Include.NON_EMPTY)
	private Set<PlanCarrier> planCarriers;
	@JsonInclude(Include.NON_EMPTY)
	private Set<PlanPackage> planPackages;
	@JsonInclude(Include.NON_EMPTY)
	private Set<BenefitPlan> benefitPlans;
	@JsonInclude(Include.NON_EMPTY)
	@JsonProperty("headcountPlans")
	private Set<BenefitPlan> headcountPlans;
	@JsonInclude(Include.NON_EMPTY)
	private List<AdditionalBenefitsCategoryOffer> additionalBenefitOffers;
	@JsonInclude(Include.NON_EMPTY)
	private List<CoverageLevel> coverageLevels;
	@Setter
	@Getter
	private Map<Long, BigDecimal> minFunding;
	@JsonIgnore
	private List<String> plansMissingRates;
	@JsonInclude(Include.NON_EMPTY)
	private List<SelectItem> bsuppExcessOptions = new ArrayList<>();
	@JsonInclude(Include.NON_EMPTY)
	private List<SelectItem> bsuppVoluntaryPlanTypes = new ArrayList<>();
}
