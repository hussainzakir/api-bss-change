package com.trinet.ambis.rest.controllers.dto.outputs;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BenefitTypeEmployeeCostSummary {

	private Map<String, Map<String, List<EmployeeCostSummary>>> emplCostSummaryByBenGroup;
	
}
