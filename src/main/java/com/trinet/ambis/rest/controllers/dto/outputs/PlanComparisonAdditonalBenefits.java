package com.trinet.ambis.rest.controllers.dto.outputs;

import java.util.List;

import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PlanComparisonAdditonalBenefits extends BasePlanComparison {
	@Transient
	String benefitType;
	List<String> attributeNames;
	List<AdditionalBenefitGroup> selectedGroupDetails;
	List<AdditionalBenefitGroup> availableGroupDetails;
}
