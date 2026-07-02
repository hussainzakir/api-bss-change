/**
 * 
 */
package com.trinet.ambis.service.model;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.ToString;

/**
 * @author kpamulapati
 *
 */
@ToString
@Data
public class PlanHeadCount {
	private String benefitProgram;
	private List<BenefitPlanHeadCount> benefitPlans;
	private Map<String, Integer> adBenefitPlans;
	boolean hasAdditionalHeadCount;
}
