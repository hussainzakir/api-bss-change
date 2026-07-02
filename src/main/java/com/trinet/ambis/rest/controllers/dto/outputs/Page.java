package com.trinet.ambis.rest.controllers.dto.outputs;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonAutoDetect
@Data
@lombok.Generated
@lombok.ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Page {

	private String id;
	private String name;
	private String source;
	private boolean isTemplate;
	
}
