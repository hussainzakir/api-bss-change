package com.trinet.ambis.rest.controllers.dto.outputs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeDetails {
	
	private String firstName;
    private String lastName;
    private String state;
    private String coverageCode;
    private String group;

}
