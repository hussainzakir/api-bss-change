package com.trinet.ambis.service.model.prospect;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ProspectCensusResponse {

	private String employeeId;
	private String employeeName;
    private String firstName;
    private String lastName;
	private boolean k1;
	private BigDecimal salary;
	private String gender;
	private String state;
	private String zip;
	private String medicalTier;
	private String dentalTier;
	private String visionTier;

	private String dob;

	List<Dependents> dependents;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Dependents {
		private String relation;
		private boolean covgElection;
		private boolean includeInCost;

		private String dob;
	}
}
