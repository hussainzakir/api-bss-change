package com.trinet.ambis.service.email.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmissionEmailDto {

	@Builder.Default
	private List<CompanyAndConfNumberDto> companyAndConfNumberDtos = new ArrayList<>();

	private String oeQuarter;
	
	private String userId;

	private boolean sendToBssTeam;

	private boolean isSingleClient;
	
	private String bssProcessType;

}
