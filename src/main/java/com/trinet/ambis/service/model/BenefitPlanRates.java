package com.trinet.ambis.service.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author kpamulapati
 */
public class BenefitPlanRates {
    
	private String id;
	private String name;
	private List<String> offeredStates;
	private List<PlanRateContribution> contributionRates;
	
    @JsonIgnore
	private String vendorId;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<PlanRateContribution> getContributionRates() {
		return contributionRates;
	}
	
	public void setContributionRates(List<PlanRateContribution> contributionRates) {
		this.contributionRates = contributionRates;
	}
	
	public String getVendorId() {
		return vendorId;
	}
	
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	
	public List<String> getOfferedStates() {
        return offeredStates;
	}
	
    public void setOfferedStates(List<String> offeredStates) {
        this.offeredStates = offeredStates;
    }
}
