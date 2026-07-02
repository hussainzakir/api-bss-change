package com.trinet.ambis.service.prospect.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenefitGroupAssignmentRes {

	private String employeeId;

	private String firstName;

	private String lastName;

	private int benefitGroupId;
	
	private String benefitGroupName;

}