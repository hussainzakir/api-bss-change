package com.trinet.ambis.rest.controllers.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BundlePlanResponse {

	private List<BundleDto> bundles;

	@Data
	@Builder
	public static class BundleDto {
		private Long id;
		private String name;
		private String type;
		private List<String> benefitPlanIds;
	}
}