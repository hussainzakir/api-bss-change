/**
 * 
 */
package com.trinet.ambis.service.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author kpamulapati
 *
 */
public class PlanRateDetails {
	
	private String planYearType;
	@JsonInclude(JsonInclude.Include.NON_NULL) 
	private List<PlanRateDetail> medical;
	@JsonInclude(JsonInclude.Include.NON_NULL) 
	private List<PlanRateDetail> dental;
	@JsonInclude(JsonInclude.Include.NON_NULL) 
	private List<PlanRateDetail> vision;
	
	public String getPlanYearType() {
		return planYearType;
	}
	public void setPlanYearType(String planYearType) {
		this.planYearType = planYearType;
	}
	public List<PlanRateDetail> getMedical() {
		return medical;
	}
	public void setMedical(List<PlanRateDetail> medical) {
		this.medical = medical;
	}
	public List<PlanRateDetail> getDental() {
		return dental;
	}
	public void setDental(List<PlanRateDetail> dental) {
		this.dental = dental;
	}
	public List<PlanRateDetail> getVision() {
		return vision;
	}
	public void setVision(List<PlanRateDetail> vision) {
		this.vision = vision;
	}
}
