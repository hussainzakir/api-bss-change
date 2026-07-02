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
public class PlanRateDetail {

	private int id;
	private String name;
	private List<BenefitPlanRates> benefitPlanRates;

}
