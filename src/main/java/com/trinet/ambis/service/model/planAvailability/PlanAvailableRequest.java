package com.trinet.ambis.service.model.planAvailability;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class PlanAvailableRequest {

	private String cloneBenefitProgram;
	private Date effectiveDate;
	private List<Location> locations;
	private List<String> plans;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Location {

		private String state;
		private String postalCode;

	}

}