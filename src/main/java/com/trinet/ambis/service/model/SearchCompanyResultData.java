package com.trinet.ambis.service.model;

import org.apache.commons.lang.builder.HashCodeBuilder;

import lombok.Data;

/*
 *  Class to encapsulate the results obtained from SearchCompanyController
 * */
@Data
public class SearchCompanyResultData {

	private String companyCode;
	private String companyName;

	public SearchCompanyResultData() {

	}

	public SearchCompanyResultData(String companyCode, String companyName) {
		this.companyCode = companyCode;
		this.companyName = companyName;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		SearchCompanyResultData searchCompanyResult = (SearchCompanyResultData) obj;
		return ((this.companyCode == searchCompanyResult.companyCode)
				&& (this.companyName == searchCompanyResult.companyName));
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(companyCode).append(companyName).toHashCode();
	}

}
