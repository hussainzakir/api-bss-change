package com.trinet.ambis.persistence.projections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RlRegionPlan1View {

	private String benefitPlan;

	private String region;

}
