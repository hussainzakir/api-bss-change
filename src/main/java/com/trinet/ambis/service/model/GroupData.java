package com.trinet.ambis.service.model;

import lombok.Data;

/**
 * 
 * @author rvutukuri
 *
 */

@Data
public class GroupData {
	private long sourceStrategyGroupId;
	private String destGroupName;
	private String waitPeriod;
}
