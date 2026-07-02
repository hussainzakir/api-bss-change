package com.trinet.ambis.persistence.projections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanSelectionDetail {

	private long strategyId;
	
	private long groupId;

	private String planType;

}
