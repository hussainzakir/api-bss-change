package com.trinet.ambis.rest.controllers.dto.outputs;

import lombok.Data;

import java.util.List;

@Data
public class AdditionalBenefitGroupPlans {
	private String bundleName;
	private boolean pageBreak;
	private String totalCost;
	private List<List<String>> attributeValues;
}
