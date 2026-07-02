package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AttributeDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long attributeId;
	private String attributeName;
	@JsonIgnore
	private Map<Long, List<AttributeValueDto>> tempAttributeValues;
	private List<AttributeValueDto> values;

}
