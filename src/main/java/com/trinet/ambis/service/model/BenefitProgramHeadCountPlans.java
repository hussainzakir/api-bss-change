/**
 * 
 */
package com.trinet.ambis.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * @author rvutukuri
 *
 */
@Data
public class BenefitProgramHeadCountPlans {
	String benefitProgram;
	List<HeadCountBenefitPlan> benefitPlans = new ArrayList<>();
	private Map<String, Integer> adBenefitPlans;
	boolean hasAdditionalHeadCount;
}
