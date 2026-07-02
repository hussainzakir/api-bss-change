package com.trinet.ambis.persistence.dao.hrp.dto;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CensusHcSyncEventDto {

	private String companyCode;

	private Set<Long> processStatusIds;

	private boolean isTermedCompany;

}
