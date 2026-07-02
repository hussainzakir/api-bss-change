package com.trinet.ambis.service.model.plancompare;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Attribute {

	private String type;
	private String name;
	private int displayOrder;
	private boolean display;
	private String displayName;
	private String value;
	private String dataType;
	private String transformedValue;
	
	private List<Attribute> children;
	
	private int id;

}
