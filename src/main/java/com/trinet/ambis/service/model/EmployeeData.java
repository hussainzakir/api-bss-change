package com.trinet.ambis.service.model;

import java.util.Date;

import lombok.Data;

/**
 * @author mpulipaka
 */
@Data
public class EmployeeData {

	private String emplId;
	private String emplName;
	private String department;
	private String location;
	private String jobTitle;
	private String benefitProgram;
	private String benefitGroupName;
	private long emplRcd;
	private long benefitGroupId;
	private long strategyGroupId;
	private Date effdt;
    private boolean k1;

}
