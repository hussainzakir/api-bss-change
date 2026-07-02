package com.trinet.ambis.rest.controllers.dto.outputs;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanAppendixFilters {

	private boolean includeOnlyEeLocationPlans;
	private List<String> zipCodes;
	private List<String> regions;
	private List<String> medicalPlanCategories;
	private String hsa;
	private BigDecimal deductibleMin;
	private BigDecimal deductibleMax;
	private BigDecimal copayMin;
	private BigDecimal copayMax;
	private String groupId;
	private String groupName;
	private boolean includeCarrierSorting;
	//Need to remove includeRegionsFlag when EE/ER feature flag is removed from UI
	private boolean includeRegionsFlag;
}
