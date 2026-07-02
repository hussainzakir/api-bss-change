/**
 * 
 */
package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import lombok.Data;

/**
 * @author kpamulapati
 *
 */
@Data
public class PlanRateContribution {

	private String type;
	private BigDecimal planCost;
	private BigDecimal previousYearCost;

}
