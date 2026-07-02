package com.trinet.ambis.rest.controllers.dto.outputs;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class BenefitGroup {

	private String benefitGroupName;
	private Map<String, BenefitOffer> benefitOffers;
}
