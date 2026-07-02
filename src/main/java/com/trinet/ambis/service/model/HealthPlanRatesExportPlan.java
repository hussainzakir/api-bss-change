package com.trinet.ambis.service.model;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * @author hliddle
 */
@Data
public class HealthPlanRatesExportPlan {

	private String currentId;
	private String futureId;
	private String currentName;
	private String futureName;
	private String planType;
	private boolean hasHeadcount;
	private String offeredYearsFlag;
	private List<String> offeredStates;
	private BigDecimal employeeOnlyCurrentCost;
	private BigDecimal employeeSpouseCurrentCost;
	private BigDecimal employeeChildCurrentCost;
	private BigDecimal employeeFamilyCurrentCost;
	private BigDecimal employeeOnlyFutureCost;
	private BigDecimal employeeSpouseFutureCost;
	private BigDecimal employeeChildFutureCost;
	private BigDecimal employeeFamilyFutureCost;
	private Long employeeOnlyCurrentHeadcount;
	private Long employeeSpouseCurrentHeadcount;
	private Long employeeChildCurrentHeadcount;
	private Long employeeFamilyCurrentHeadcount;
	private Long employeeOnlyFutureHeadcount;
	private Long employeeSpouseFutureHeadcount;
	private Long employeeChildFutureHeadcount;
	private Long employeeFamilyFutureHeadcount;

}
