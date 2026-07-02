/**
 * 
 */
package com.trinet.ambis.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author rvutukuri
 *
 */
public class FundingType {
	private String id;
	private String description;
	@JsonIgnore
	private boolean defaultFunding;
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the defaultFunding
	 */
	public boolean isDefaultFunding() {
		return defaultFunding;
	}
	/**
	 * @param defaultFunding the defaultFunding to set
	 */
	public void setDefaultFunding(boolean defaultFunding) {
		this.defaultFunding = defaultFunding;
	}

}
