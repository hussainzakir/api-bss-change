package com.trinet.ambis.service.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAndConfNumberDto {

	private String companyName;

	private String companyCode;

	private String confirmationNumber;

}
