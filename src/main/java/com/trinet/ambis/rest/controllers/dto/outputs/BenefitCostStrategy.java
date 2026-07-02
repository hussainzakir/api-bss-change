package com.trinet.ambis.rest.controllers.dto.outputs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BenefitCostStrategy {

	 private String strategyName;
	 private List<BenefitTypeCost> benCosts;
	 private BenefitTypeTotal annualTotal;
	 private BenefitTypeTotal monthlyTotal;
	 
}
