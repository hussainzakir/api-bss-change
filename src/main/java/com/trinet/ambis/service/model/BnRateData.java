package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import lombok.Data;

/**
 * This class defines a model for the PeopleSoft database record PS_BN_RATE_DATA
 * @author mbrothers
 *
 */
@Data
public class BnRateData {

	private String rateTblId;
	private String effdt;
	private String bnRateKey01;
	private String bnRateKey02;
	private String bnRateKey03;
	private BigDecimal bnEmplRate;
	private BigDecimal bnEmplrRate;
	private BigDecimal bnBTaxRate;
	private BigDecimal bnATaxRate;
	private BigDecimal bnNTaxRate;
	private BigDecimal bnTTaxRate;
	private BigDecimal bnPTaxRate;
	private String otherRatesExist;
	private BigDecimal t2ProvCovrgRate;
	private String pfClient;
}

