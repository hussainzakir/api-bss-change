package com.trinet.ambis.service.model.plancompare;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BenefitPlan {
	@EqualsAndHashCode.Include
	String planId;
	String descr;
}