package com.trinet.ambis.rest.controllers.dto.outputs;

import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OutputRequest {
	
	private List<String> templateNames;
	private List<String> benefitTypes;
	private PlanAppendixFilters planAppendixFilters;
	private String tnStrategyId;
	
}