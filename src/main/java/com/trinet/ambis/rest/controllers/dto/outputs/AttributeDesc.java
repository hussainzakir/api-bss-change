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
public class AttributeDesc {

	private Integer id;
	private String type;// category,attribute
	private String desc;
	private String name;
	private List<AttributeDesc> children;
	   
}
