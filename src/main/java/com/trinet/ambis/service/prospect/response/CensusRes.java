package com.trinet.ambis.service.prospect.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CensusRes {

	private String employeeId;

	private String employeeName;

	private boolean k1;

	private Integer age;

	@JsonProperty("salary")
	private Double annualWages;

	private String gender;

	@JsonProperty("state")
	private String homeState;

	@JsonProperty("zip")
	private String homePostalCode;

	private String medicalTier;

	private String dentalTier;

	private String visionTier;

}
