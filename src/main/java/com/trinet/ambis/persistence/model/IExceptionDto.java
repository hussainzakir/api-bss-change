/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.util.Date;

/**
 * @author schaudhari
 *
 */
public interface IExceptionDto {

	long getId();
	
	Date getStartDate();

	Date getEndDate();
}
