package com.trinet.ambis.service.model;

import java.util.List;

import lombok.Data;

/**
 * @author hliddle
 */
@Data
public class ModelCompareBenSuppExcessOption {

	private Long optionId;
	
	private String optionCode;
	
	private String optionName;
	
	private List<PlanTypeDescription> excessVoluntaryPlanTypes;
	

}