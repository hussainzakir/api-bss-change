package com.trinet.ambis.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuarterPlanYearDate {
	
	//Master ID
	private Integer id;
	private String yearIndicator;
	
	//@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_PATTERN)
	private String planYearStartDate;
	
	//@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_PATTERN)
	private String planYearEndDate;
	
	private Integer quarterID;
	private String quarterName;
	private Integer displayOrder;
	private Character mainQuarterFlag;
	
	@JsonIgnore
	private int yearHeaderOrder;
	
	@JsonIgnore
	private int planYrsCountInQuarter;
}
