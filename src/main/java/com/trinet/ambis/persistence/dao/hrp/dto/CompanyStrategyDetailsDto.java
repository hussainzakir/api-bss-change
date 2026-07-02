package com.trinet.ambis.persistence.dao.hrp.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyStrategyDetailsDto {

	private long companyId;

	private Set<Long> allStrategyIds;

	private long realmPlanYearId;

}
