package com.trinet.ambis.service.model;

import java.util.List;

import lombok.Data;

@Data
public class EmployeeAssignmentData {
	private long destinationStrategyGroupId;
	private List<EmployeeSourceData> employeesList;
}
