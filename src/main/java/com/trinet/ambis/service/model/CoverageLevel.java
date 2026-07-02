/**
 * 
 */
package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import com.trinet.ambis.enums.CoverageCodesEnums;

/**
 * @author khinton
 *
 */
public class CoverageLevel {
	private String id;
	private String name;
	private BigDecimal contribution;

	public CoverageLevel() {

	}

	public CoverageLevel(String id, String name, BigDecimal contribution) {
		this.id = id;
		this.name = name;
		this.contribution = contribution;
	}

	public CoverageLevel(CoverageCodesEnums coverageCodesEnums) {
		this.id = coverageCodesEnums.getId();
		this.name = coverageCodesEnums.getName();
	}

	public CoverageLevel(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the contribution
	 */
	public BigDecimal getContribution() {
		return contribution;
	}

	/**
	 * @param contribution
	 *            the contribution to set
	 */
	public void setContribution(BigDecimal contribution) {
		this.contribution = contribution;
	}
}
