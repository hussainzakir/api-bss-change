package com.trinet.ambis.service.prospect.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class BenTypeOfferRes {

	private long strategyId;
	private Set<String> offerTypes;

}
