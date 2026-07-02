package com.trinet.ambis.service.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class RateUpdateDto implements Serializable {
	private String companyCode;

	private String prospectId;

	private String proposalId;

	@NotBlank
	private String effectiveDate;

	@NotBlank
	private String quarter;

	@NotBlank
	private String rateGroupId;
}