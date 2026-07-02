package com.trinet.ambis.rest.controllers.dto.outputs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false)
public class PlanComparison extends BasePlanComparison {
	private List<AttributeDesc> attributeNames;
	private List<CompareCurrentTrinetPlans> comparisons;
}
