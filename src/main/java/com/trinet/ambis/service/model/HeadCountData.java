package com.trinet.ambis.service.model;

import java.util.Map;

import lombok.Data;

/**
 * 
 * @author akaparaboyna
 *
 */
@Data
public class HeadCountData {
	private long strategyGroupId;
	private Map<String, Integer> covrgHeadCountMap;
}
