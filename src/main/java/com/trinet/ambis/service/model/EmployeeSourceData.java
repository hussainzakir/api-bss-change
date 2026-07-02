package com.trinet.ambis.service.model;

import java.util.List;

import lombok.Data;

@Data
public class EmployeeSourceData {
	private List<String> employees;
	private long sourceStrategyGroupId;
}
