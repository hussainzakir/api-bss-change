package com.trinet.ambis.rest.controllers.dto.outputs;

import java.util.List;

import lombok.Data;

@Data
public class AdditionalBenefitGroup {
	private String groupName;
	private List<AdditionalBenefitGroupPlans> plans;
}
