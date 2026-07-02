package com.trinet.ambis.rest.controllers.dto.outputs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CvgLvlPlanInfo {

	private String employeeOnly;
	private String employeeSpouse;
	private String employeeChildren;
	private String family;

}
