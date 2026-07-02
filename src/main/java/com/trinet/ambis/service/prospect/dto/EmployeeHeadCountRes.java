package com.trinet.ambis.service.prospect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeHeadCountRes {
	private String benefitPlan;
	private String covrgCD;
	private Long count;
 
}
