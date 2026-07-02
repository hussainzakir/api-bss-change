package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * The persistent class for the XBSS_GROUP_RATE database table.
 * 
 */
@Entity
@Table(name = "XBSS_GROUP_RATE")
public class GroupRate implements Serializable {
	private static final long serialVersionUID = 1L;
	@JsonUnwrapped
	@EmbeddedId
	private GroupRatePK id;

	@Column(name = "RATE_ID_TYPE")
	private String rateIdType;

	@MapsId("groupId")
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "GROUP_ID", insertable = false, updatable = false)
	private BenefitGroup benefitGroup;

	/**
	 * @return the id
	 */
	public GroupRatePK getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(GroupRatePK id) {
		this.id = id;
	}

	/**
	 * @return the rateIdType
	 */
	public String getRateIdType() {
		return rateIdType;
	}

	/**
	 * @param rateIdType
	 *            the rateIdType to set
	 */
	public void setRateIdType(String rateIdType) {
		this.rateIdType = rateIdType;
	}

	/**
	 * @return the benefitGroup
	 */
	public BenefitGroup getBenefitGroup() {
		return benefitGroup;
	}

	/**
	 * @param benefitGroup
	 *            the benefitGroup to set
	 */
	public void setBenefitGroup(BenefitGroup benefitGroup) {
		this.benefitGroup = benefitGroup;
	}

}
