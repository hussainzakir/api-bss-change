package com.trinet.ambis.service.model;

import java.util.Map;

import lombok.Data;

/*
 * @Author mpulipaka
 */
@Data
public class PrimaryBenefitGroupHeadCount {

	private String benefitProgram;

	private Map<String, Map<String, Long>> benefitPlanHeadCount;

}
