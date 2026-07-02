package com.trinet.ambis.rest.controllers.dto.outputs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BenefitTypeCost {

	 private String benType; 
	 private BenefitTypeTotal benTypeTotal;
	 private boolean displayOnReport = true;
	
}
