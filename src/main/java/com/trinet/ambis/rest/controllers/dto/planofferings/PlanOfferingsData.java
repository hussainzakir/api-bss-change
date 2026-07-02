package com.trinet.ambis.rest.controllers.dto.planofferings;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendix;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanOfferingsData {
	
	private String reportCode;
	private String loggedInUser;
    private String companyName;
	private String exchange;
	private String quarter;
	private String bundleName;
	private String planYearStartDate;
	private String planYearEndDate;
	private String hqState;
	private String hqZipCode;
	private List<String> regions;
	private Map<String, PlanAppendix> planOfferings;
	private String wseState;
	private String wseZipCode;
	private List<Carrier> carriers;
	private List<String> benefitTypes;
	private boolean allRegions;

}
