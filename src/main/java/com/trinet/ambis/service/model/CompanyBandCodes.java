/**
 * 
 */
package com.trinet.ambis.service.model;

/**
 * @author rvutukuri
 *
 */

public class CompanyBandCodes {

	private Long companyId;

	private String bandCodeType;

	private String bandCodeValue;

	/**
	 * 
	 * @param companyId
	 * @param bandCodeType
	 * @param bandCodeValue
	 */
	public CompanyBandCodes(long companyId, String bandCodeType, String bandCodeValue) {
		this.companyId = companyId;
		this.bandCodeType = bandCodeType;
		this.bandCodeValue = bandCodeValue;
	}

	/**
	 * 
	 */
	public CompanyBandCodes() {

	}

	/**
	 * @return the companyId
	 */
	public Long getCompanyId() {
		return companyId;
	}

	/**
	 * @param companyId
	 *            the companyId to set
	 */
	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	/**
	 * @return the bandCodeType
	 */
	public String getBandCodeType() {
		return bandCodeType;
	}

	/**
	 * @param bandCodeType
	 *            the bandCodeType to set
	 */
	public void setBandCodeType(String bandCodeType) {
		this.bandCodeType = bandCodeType;
	}

	/**
	 * @return the bandCodeValue
	 */
	public String getBandCodeValue() {
		return bandCodeValue;
	}

	/**
	 * @param bandCodeValue
	 *            the bandCodeValue to set
	 */
	public void setBandCodeValue(String bandCodeValue) {
		this.bandCodeValue = bandCodeValue;
	}
}
