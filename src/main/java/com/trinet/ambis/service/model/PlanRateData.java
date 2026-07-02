package com.trinet.ambis.service.model;

import java.util.Map;

import lombok.Data;

@Data
public class PlanRateData {
	private String planType;
	private Map<String, Map<String, BenefitPlanRatesData>> mapRates;

}
