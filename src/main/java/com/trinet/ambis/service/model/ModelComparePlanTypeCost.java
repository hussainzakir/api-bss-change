package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import org.apache.commons.lang.WordUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.trinet.ambis.common.BSSApplicationConstants;

import lombok.Data;

/**
 * @author hliddle
 */
@Data
public class ModelComparePlanTypeCost {

	private String planType;
	private BigDecimal cost = new BigDecimal(0);
	private boolean offered;
	@JsonInclude(Include.NON_NULL)
	private Long headcount;
	
	public String getPlanTypeDisplayName() {
		String planTypeDisplayName = this.planType;
		if (BSSApplicationConstants.BSUPP.equals(this.planType)) {
			planTypeDisplayName = "Benefits Supplement";
		}
		else if (BSSApplicationConstants.CMTR.equals(this.planType)) {
				planTypeDisplayName = "Commuter";
			}
		else if (!BSSApplicationConstants.HSA.equals(this.planType)) {
			planTypeDisplayName = WordUtils.capitalizeFully(this.planType.replace("_", " "));
		}
		
		return planTypeDisplayName;
	}
	
}