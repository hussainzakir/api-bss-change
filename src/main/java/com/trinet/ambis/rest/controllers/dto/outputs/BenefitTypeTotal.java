package com.trinet.ambis.rest.controllers.dto.outputs;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BenefitTypeTotal {
	
	 private BigDecimal erAmount;
	 private BigDecimal eeAmount;
	 private String planName;
	 private BigDecimal total;

}
