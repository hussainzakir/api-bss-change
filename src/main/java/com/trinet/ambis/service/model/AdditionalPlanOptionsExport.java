package com.trinet.ambis.service.model;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * @author hliddle
 */
@Data
public class AdditionalPlanOptionsExport implements Comparable<AdditionalPlanOptionsExport> {

	private String id;
	private String name;
	private String planType;
	private String offeredYearsFlag;
	private List<String> offeredStates;
	private String currentUnit;
	private String futureUnit;
	private BigDecimal currentCost;
	private BigDecimal futureCost;
	private List<AdditionalPlanOptionPlanExport> optionPlans;

	@Override
	public int compareTo(AdditionalPlanOptionsExport o) {
        return this.getName().compareTo(o.getName());
	}
}
