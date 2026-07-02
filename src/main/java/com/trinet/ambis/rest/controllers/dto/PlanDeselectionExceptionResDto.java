package com.trinet.ambis.rest.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.trinet.ambis.service.model.ExceptionDto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class PlanDeselectionExceptionResDto extends ExceptionDto {

	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private String planType;

	@JsonIgnore
	private boolean active;

	@JsonIgnore
	private String lastUpdatedById;

	@JsonIgnore
	private String lastUpdatedByName;

	@JsonIgnore
	private String createdById;

	@JsonIgnore
	private String createdByName;

}
