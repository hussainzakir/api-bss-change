package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author schaudhari
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper=true)
public class MinFundExceptionDto extends ExceptionDto {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String minFundType;
	private BigDecimal minFundValue;

}
