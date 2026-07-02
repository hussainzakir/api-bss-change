package com.trinet.ambis.service.model;

import lombok.Data;

/**
 * @author jshuali
 * 
 *         Use by Freemarker to generate the confirmation email.
 *
 */
@Data
public class ContributionPlan {

	private String planName;
	private String coverageCode;
	private String companyPercent;
	private String companyCost;
	private String employeePercent;
	private String employeeCost;
}
