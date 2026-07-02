/**
 * 
 */
package com.trinet.ambis.service.model;

import java.util.List;

import lombok.Data;

/**
 * @author kpamulapati
 *
 */
@Data
public class BenefitPlanHeadCount {
	private String benefitPlan;
	private String planType;
	private List<CoverageLevelHeadCount> coverageLevelHeadCount;

}
