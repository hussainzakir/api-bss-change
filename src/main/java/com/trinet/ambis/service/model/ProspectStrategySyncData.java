package com.trinet.ambis.service.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProspectStrategySyncData {

	private String employeeId;

	private boolean k1;
	
	private String homeState;
	
	private String homePostalCode;
	
	private BigDecimal annualWages;

	private List<EnrolledCvgCode> enrolledCvgCodes;
	
	@JsonProperty(value="isLocationChanged")        
	private boolean isLocationChanged;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class EnrolledCvgCode {

		private String benefitType;

		private String desiredCvgCode;
	}

}