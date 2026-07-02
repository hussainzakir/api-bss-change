package com.trinet.ambis.service.model;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * @author hliddle
 */
@Data
public class PlanRatesExportData {
	
	private String currentStartDate;
	private String currentEndDate;
	private String futureStartDate;
	private String futureEndDate;
	private Map<String, List<HealthPlanRatesExportPlan>> healthPlanData;
	private Map<String, List<AdditionalPlanOptionsExport>> additionalPlanData;

}
