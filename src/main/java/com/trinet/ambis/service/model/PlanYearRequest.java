package com.trinet.ambis.service.model;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanYearRequest {

	@NotNull
	private String companyCode;

	@NotNull
	private String benefitStartDate;

	@NotNull
	private String quarter;

	@NotNull
	private String serviceOrderNumber;

	private String commonOwnerCompany;

	private Boolean quarterException;
}
