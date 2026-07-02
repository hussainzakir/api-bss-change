package com.trinet.ambis.rest.controllers.dto.outputs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class FundingSummary {
	
	private List<BenefitGroup> benefitGroups;
	private List<String> orderedBenefitTypes;

}
