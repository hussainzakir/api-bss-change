package com.trinet.ambis.service.model;

import lombok.Data;

@Data
public class PlanEligibilityRequest {

	private String benefitType;
	private String state;
	private String zipCode;

}
