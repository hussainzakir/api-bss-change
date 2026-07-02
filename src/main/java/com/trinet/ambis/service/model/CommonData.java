package com.trinet.ambis.service.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CommonData {

	public CommonData() {
		super();
	}

	@JsonProperty("company")
	private CompanyData companyCommonData;
	@JsonProperty("planYear")
	private PlanYearCommonData planYearCommonData;
	@JsonProperty("currentPlanYear")
	private PlanYearCommonData currentplanYear;
	@JsonProperty("user")
	private UserData userCommonData;
	@JsonProperty("waitPeriods")
	private List<WaitPeriod> waitPeriods;
	@JsonProperty("fundingTypes")
	private List<FundingType> fundingTypes;
	@JsonProperty("eligibleGroups")
	private List<GroupRuleDto> eligibleGroups;


}
