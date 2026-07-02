package com.trinet.ambis.rest.controllers.dto.planofferings;

import java.util.List;

import lombok.Data;

@Data
public class PlanOfferingsRequest {

	private String reportCode;
	private String companyCode;
	private String exchange;
	private String quarter;
	private String planYearStartDate;
	private String planYearEndDate;
	private Long bundleId;
	private String hqState;
	private String hqZipCode;
	private String state;
	private String zipCode;
	private List<Carrier> carriers;
	private List<String> regions;
	private List<String> benefitTypes;
	private boolean allRegions;

}
