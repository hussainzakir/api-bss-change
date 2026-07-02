/**
 * 
 */
package com.trinet.ambis.service.model;

import lombok.Data;

/**
 * @author rvutukuri
 *
 */
@Data
public class AdditionalBenefitPlanHeadCount {
	String benefitPlan;
	String planType;
	boolean primaryPlan;
	boolean employeePaid;
	int planHeadCount;

}
