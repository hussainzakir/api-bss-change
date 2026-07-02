package com.trinet.ambis.rest.controllers.dto.outputs;

import java.util.List;

import lombok.Data;

@Data
public class PlanComparisionGroupDetails {

	private String groupName;
	private List<List<String>> attributeValues;
	
}
