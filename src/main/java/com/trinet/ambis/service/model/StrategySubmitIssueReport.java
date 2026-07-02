package com.trinet.ambis.service.model;

import java.util.List;

import lombok.Data;

/**
 * @author hliddle
 */
@Data
public class StrategySubmitIssueReport {

	private String statementUploadStatus;
	private boolean emailSent;
	private String companyCode;
	private String exchange;
	private String oeQuarter;
	private String companyLegalName;
	private String companyName;
	private String submitDateStr;
	private boolean submittedByBdm;
	private List<Bdm> bdms;

	@Data
	public class Bdm {
		private String employeeId;
		private String employeeFirstName;
		private String employeeLastName;
		private String submitter;
	}

}
