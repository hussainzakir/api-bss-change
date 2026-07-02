package com.trinet.ambis.service.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CompanyHQData extends CompanyRealmData {
	
	private String companyName;
	private String state;
	private String companyHq;
	private boolean hasStrategies;
	private String zip;
	

}
