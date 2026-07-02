package com.trinet.ambis.service.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author schaudhari
 *
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class BenOfferExceptionDto extends ExceptionDto {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean offered;

	private String originDept;
	private String originationDeptName;
}