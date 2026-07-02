package com.trinet.ambis.service.model;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewCompany implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private long annualBudget;
	private Map<String, LinkedHashMap<String, Integer>> coverageLevels;
	private Map<String, Integer> headCounts;

}
