/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.util.List;

import lombok.Data;

/**
 * @author rvutukuri
 *
 */
@Data
public class PlanMapping {
	private String oldBenefitPlan;
	private long oldPortfolioId;
	private long newPortfolioId;
	private String newBenefitPlan;
	private List<String> newBenefitPlans;
	private String planType;
}
