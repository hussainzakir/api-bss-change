package com.trinet.ambis.service.model;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.trinet.ambis.util.JsonDateSerializer;

import lombok.ToString;

@ToString
public class PlanYearCommonData {
	public PlanYearCommonData() {
	}

	public PlanYearCommonData(Date effectiveDate, Date endDate) {
		this.effectiveDate = effectiveDate;
		this.endDate = endDate;
	
	}

	private Date effectiveDate;

	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	
	private Date endDate;

	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
}
