package com.trinet.ambis.service.model.planAvailability;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
public class PlanAvailableResponse {

	private String postal;
	private List<BenTypePlan> plansByBenType;

	@Data
	@Builder(toBuilder = true)
	@AllArgsConstructor
	@NoArgsConstructor
	@JsonIgnoreProperties
	public static class BenTypePlan {

		private String benType;
		private List<String> planIds;

	}

}