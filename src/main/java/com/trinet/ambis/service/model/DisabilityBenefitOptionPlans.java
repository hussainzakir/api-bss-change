/**
 * 
 */
package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * @author rvutukuri
 *
 */
@Data
public class DisabilityBenefitOptionPlans implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String planType;
	private String planDesc;
	private String planShortDesc;
	private boolean isSdiPlan;
	private boolean isPrimaryPlan;
	private boolean isEmployeePaid;
	private boolean isTaxFree;
	private BigDecimal planCost;
	private Long planHeadCount;
	private String offeredGroupType;
	private String carrierName;
	
	public DisabilityBenefitOptionPlans() {
		super();
	}
	
	public DisabilityBenefitOptionPlans(DisabilityBenefitOptionPlans original) {
		this.id = original.id;
		this.planType = original.planType;
		this.planDesc = original.planDesc;
		this.planShortDesc = original.planShortDesc;
		this.isSdiPlan = original.isSdiPlan;
		this.isPrimaryPlan = original.isPrimaryPlan;
		this.isEmployeePaid = original.isEmployeePaid;
		this.isTaxFree = original.isTaxFree;
		this.planCost = original.planCost;
		this.planHeadCount = original.planHeadCount;
		this.offeredGroupType = original.offeredGroupType;
		this.carrierName = original.carrierName;
	}
}
