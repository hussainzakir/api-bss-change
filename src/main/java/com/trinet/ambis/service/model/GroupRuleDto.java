package com.trinet.ambis.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GroupRuleDto implements Comparable<GroupRuleDto> {

	@JsonIgnore
	private long id;
	@JsonProperty("type")
	private String groupType;
	@JsonIgnore
	private String state;
	private boolean mandatory;
	private String flowType;
	private boolean allowMultiplies;
	private boolean appliesToNoMed;
	private String groupName;
	private int sortOrder;
	@JsonIgnore
	private Date effDate;
	@JsonIgnore
	private Date expDate;
	
	private List<PlanTypeRule> rules = new ArrayList<>();

	/**
	 * This inner class contains the properties required for the plan type rules
	 * @author hliddle
	 *
	 */
	@Data
	public class PlanTypeRule {
		private String planType;
		private String ruleName;
	}

	@JsonProperty("region")
	public String getRegion() {
		return this.state == null ? "All" : this.state;
	}
	
	public void setRegion(String region) {
		this.state = ("All").equals(region) ? null : region;
	}

	@Override
	public int compareTo(GroupRuleDto o) {
		return Integer.valueOf(this.getSortOrder()).compareTo(Integer.valueOf(o.getSortOrder()));
	}	

}
