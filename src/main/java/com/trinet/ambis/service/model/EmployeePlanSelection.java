/**
 * 
 */
package com.trinet.ambis.service.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * @author rvutukuri
 *
 */
@Data
public class EmployeePlanSelection {

	private String employeeId;
	private String benefitPlan;
	private Map<String, Map<String, Long>> coveragecodes = new HashMap<>();

}
