package com.trinet.ambis.rest.controllers.dto.prospect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeGroupAssignmentDto {

	private String employeeId;

	private int benefitGroupId;

}
