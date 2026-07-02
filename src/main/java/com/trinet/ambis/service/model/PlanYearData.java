package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class PlanYearData implements Serializable {

	private static final long serialVersionUID = 1L;

	public PlanYearData() {
	}

	private Date effectiveDate;

	private Date endDate;

	private Date nextPlanYearStartDate;

	private Date nextPlanYearEndDate;

}
