package com.trinet.ambis.service.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@JsonAutoDetect
@Getter
@Setter
@lombok.Generated
public class BenefitDocumentRequest {

	private List<Integer> qtrPlanYearDateIds;
	private String documentType;
	private String title;
	
	@JsonProperty(required=false, defaultValue="false")
	private boolean showAttributes;

	// Optional additional attributes
    private Optional<Map<String, String>> attributes = Optional.empty();
    
    private Optional<List<Map<String, String>>> listOfAttributes = Optional.empty();
}