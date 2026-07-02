package com.trinet.ambis.rest.controllers.dto.planofferings;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.trinet.ambis.rest.controllers.dto.outputs.VariableInstructions;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReportDetails {
	
	@NotEmpty(message = "cmsType cannot be null or empty.")
	@JsonInclude(Include.NON_NULL)
	private String cmsType;
	@NotEmpty(message = "cmsUrl cannot be null or empty.")
	@JsonInclude(Include.NON_NULL)
	private String cmsUrl;
	@NotEmpty(message = "cmsId cannot be null or empty.")
	@JsonInclude(Include.NON_NULL)
	private String template;
	private String mainTemplate;
	private String headerTemplate;
	private String templateEngine;
	private String pdfEngine;
	private String fileName;
	private String storageServiceName;
	private VariableInstructions variableInstructions;
	private String templateContent;

}
