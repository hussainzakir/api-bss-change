/**
 * 
 */
package com.trinet.ambis.service.model.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdditionalBenefitPlanDto {

	private boolean isSelected;
	private String planType;
	private String benefitPlan;
	private String name;
	private boolean isSdiPlan;
	private boolean isEmployeePaid;
	private String bundleId;
	private String bundleName;

}
