package com.trinet.ambis.service.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasePlansResDto implements Serializable {

	private static final long serialVersionUID = 1L;
	private String benType;
	private List<Plan> plans;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Plan implements Serializable {
		private static final long serialVersionUID = 1L;
		private String planId;
		private String planName;
	}

}
