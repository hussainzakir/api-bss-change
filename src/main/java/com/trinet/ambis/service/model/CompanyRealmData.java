package com.trinet.ambis.service.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class CompanyRealmData {

	private String recordType;

	private Long companyId;

	private String code;

	private Long realmYearId;

	private String product;

	private String oeQuarter;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date planYearStartDate;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date planYearEndDate;	

	private boolean renewalCompany;

}
