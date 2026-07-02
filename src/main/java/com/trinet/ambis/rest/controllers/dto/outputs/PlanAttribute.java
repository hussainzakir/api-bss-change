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
public class PlanAttribute {
	
	private String planName;
	private String planId;
	private String carrierName;
	private String carrierLogoUrl;
	private String planType;
	private String region;
	private List<AttributeValue>  attributeValues;
	private CvgLvlPlanInfo planRates;
	private CvgLvlPlanInfo headCount;
	private boolean hsa;
	private EeErCvgLvlInfo eeErPlanRates; 
	private boolean pageBreak;
	private boolean wordWrap;

}
