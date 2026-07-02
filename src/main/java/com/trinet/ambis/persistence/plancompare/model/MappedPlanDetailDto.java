package com.trinet.ambis.persistence.plancompare.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MappedPlanDetailDto extends BenefitPlanDetailDto {

	private String parentId;
	
}
