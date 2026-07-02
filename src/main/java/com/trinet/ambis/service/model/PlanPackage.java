package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.ToString;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PlanPackage implements Comparable<PlanPackage>, Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private long templateId;
	private long fundingModelId;
	private String name;
	private boolean customized;
	private boolean employeePaid;
	private String fundingBasePlan;
	private String fundingBasePlanName;
	private BigDecimal waiverAllowance;
	@JsonIgnore
	private BigDecimal companyContributionPercent;
	@JsonIgnore
	private String coverageLevel;
	private Long strategyId;
	private String fundingType;
	private BigDecimal bsuppExcessOption;
	@JsonIgnore
	private String planType;
	private Long companyId;
	@JsonIgnore
	private List<String> fundingBasePlanList;
	private List<FundingBasePlan> fundingBasePlans = new ArrayList<>();
	private List<Long> planCarrierIds = new ArrayList<>();
	private List<String> benefitPlans = new ArrayList<>();
	private Map<String, BigDecimal> coverageLevelFunding = new HashMap<>();
	private Map<String, BigDecimal> coverageLevelFundingFlatMax = new HashMap<>();
	private Map<String, BigDecimal> coverageLevelBasePlanLimits = new HashMap<>();
	private List<String> headCountPlans = new ArrayList<>();
	private List<String> bsuppSelectedVolPlanTypes = new ArrayList<>();

	@Override
	public int compareTo(PlanPackage pkg) {
		if (this.getName() == null) {
			return -1;
		} else if (pkg == null || pkg.getName() == null) {
			return +1;
		} else {
			return getName().compareTo(pkg.getName());
		}
	}

}
