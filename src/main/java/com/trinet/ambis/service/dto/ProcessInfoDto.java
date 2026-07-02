package com.trinet.ambis.service.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessInfoDto implements Serializable {

	private static final long serialVersionUID = -1817456323378644924L;

	private String processName;
	
	private Long exchangeId;
	
	private Long oldRealmPlanYear;

	private Long oldCompanyId;

}
