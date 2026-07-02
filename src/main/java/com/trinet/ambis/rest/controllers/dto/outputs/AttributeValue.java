package com.trinet.ambis.rest.controllers.dto.outputs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AttributeValue {
	
	private String type;// category,attribute
	private String value;
	private List<AttributeValue> children;

}
