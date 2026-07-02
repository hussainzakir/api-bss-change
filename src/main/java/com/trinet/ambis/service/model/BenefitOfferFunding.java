package com.trinet.ambis.service.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

/**
 * @author tallam
 */
@Data
public class BenefitOfferFunding {

	private String type;
	
	private boolean offered;
	
	private boolean employeePaid;
	
	private String fundingType;
	
	private String benefitPlanDesc;
	
	private String baseFundPlan;
	
	private BigDecimal waiverAllowance;
	
	private List<CoverageLevel> coverageLevels;
	
	private List<CoverageLevel> coverageLevelFundingFlatMax;
	
	private List<CoverageLevel> fundingBasePlanLimits;
	
	private ModelCompareBenSuppExcessOption excessOption;	

	@JsonInclude(Include.NON_EMPTY)
	private Map<String, BigDecimal> limitFunding;

}