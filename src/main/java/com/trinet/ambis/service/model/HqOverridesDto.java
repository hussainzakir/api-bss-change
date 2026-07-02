package com.trinet.ambis.service.model;

import lombok.Data;

@Data
public class HqOverridesDto {

	String companyCode;
	String companyName;
	String termDate;
	String oeQuarter;
	Long realmYearId;
	String planYearStart;
	String planYearEnd;
	String state;
	String zip;
	String overrideHqState;
	String overrideHqZip;
	boolean canEdit;
	boolean canDelete;
	boolean canCopy;
	boolean hasStrategies;
	Long nextRealmYearId;
	String nextYearPlanYearStart;
	String nextYearPlanYearEnd;
}
