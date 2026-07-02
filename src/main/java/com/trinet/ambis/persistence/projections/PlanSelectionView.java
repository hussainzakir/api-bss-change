package com.trinet.ambis.persistence.projections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanSelectionView {

	private String benefitPlan;

	private String planType;

}
