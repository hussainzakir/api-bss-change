package com.trinet.ambis.persistence.plancompare.model;

import lombok.Data;

@Data
public class PlanYearDetailDto {

	private String planYear;
	
	// Change the datatype to long
	private String realmYearId;
	
	//Change the data type to date
	private String planYearStartDate;

	//Change the data type to date
	private String planYearEndDate;

}
