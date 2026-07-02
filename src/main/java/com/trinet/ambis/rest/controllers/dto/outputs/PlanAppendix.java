package com.trinet.ambis.rest.controllers.dto.outputs;

import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PlanAppendix{
	
	private List<AttributeDesc> attributeNames;
	private List<PlanAttribute> planAttributes;
	private List<AdditionalBenefitGroup> additionalGroupDetails;
	private List<String> additionalGroupAttributeNames;
	private Integer maxLinesFirstPage;
	private Integer maxLinesSubsequentPages;

}
