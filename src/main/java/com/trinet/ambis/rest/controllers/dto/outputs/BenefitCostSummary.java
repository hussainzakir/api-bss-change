package com.trinet.ambis.rest.controllers.dto.outputs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BenefitCostSummary {

	private boolean noCurrentStrategy;
	private BenefitCostStrategy currentStrategy;
	private BenefitCostStrategy trinetStrategy;
	private List<EnrollmentByPlanType> enrollmentByType;
}
