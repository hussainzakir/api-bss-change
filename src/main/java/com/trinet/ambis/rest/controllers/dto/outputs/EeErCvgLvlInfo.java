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
public class EeErCvgLvlInfo {
	private BenefitTypeTotal employeeOnly;
	private BenefitTypeTotal employeeSpouse;
	private BenefitTypeTotal employeeChildren;
	private BenefitTypeTotal family;
}
