package com.trinet.ambis.service.model;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * @author hliddle
 */
@Data
public class HealthPlanRates implements Comparable<HealthPlanRates> {

	private String currentId;
	private String futureId;
	private String currentPlanName;
	private String futurePlanName;
	private String planNameForSort;
	private String planType;
	private String basePlanType;
	private String offeredYearsFlag;
	private List<String> offeredStates;
	private Map<String, BigDecimal> currentRates;
	private Map<String, BigDecimal> futureRates;
	private Map<String, Long> currentHeadCount;
	private Map<String, Long> futureHeadCount;

	@JsonIgnore
	private String vendorId;

	@JsonIgnore
	private long portfolioId;

	@Override
	public int compareTo(HealthPlanRates o) {
		return healthPlanRatesComparator.compare(this, o);
	}

	private static Comparator<String> nullSafeStringComparator = Comparator.nullsFirst(String::compareToIgnoreCase);

	private static Comparator<HealthPlanRates> healthPlanRatesComparator = Comparator
			.comparing(HealthPlanRates::getPlanNameForSort, nullSafeStringComparator);

}
