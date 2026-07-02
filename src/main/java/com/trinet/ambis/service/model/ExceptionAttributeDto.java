package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ExceptionAttributeDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long exceptionId;
	private String exceptionName;

	@JsonIgnore
	private Map<Long, Map<Long, AttributeDto>> tempAttributes;
	private List<AttributeDto> attributes;

}
