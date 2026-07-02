package com.trinet.ambis.persistence.dao.hrp.dto;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyStrategyPortfolioDetailsDto {

	private long companyId;

	private long defaultStrategyId;

	private Set<Long> allStrategyIds;

	private List<Long> defaultStrategyPortfolioIds;

	private long realmPlanYearId;
	
	private long realmId;

}
