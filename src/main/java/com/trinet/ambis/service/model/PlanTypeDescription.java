package com.trinet.ambis.service.model;

import lombok.Data;

@Data
public class PlanTypeDescription {
	private String planType;
	private String description;
	private String type;

	public PlanTypeDescription() {
		super();
	}

}
