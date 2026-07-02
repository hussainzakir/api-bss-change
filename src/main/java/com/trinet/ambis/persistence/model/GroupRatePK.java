package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.ToString;

/**
 * The primary key class for the XBSS_GROUP_RATE database table.
 * 
 */
@Embeddable
@ToString
public class GroupRatePK implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "GROUP_ID")
	private long groupId;

	@Column(name = "RATE_TBL_ID")
	private String rateTblId;

	@Column(name = "GROUP_ID")
	public long getGroupId() {
		return this.groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	@Column(name = "RATE_TBL_ID")
	public String getRateTblId() {
		return this.rateTblId;
	}

	public void setRateTblId(String rateTblId) {
		this.rateTblId = rateTblId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof GroupRatePK)) {
			return false;
		}
		GroupRatePK castOther = (GroupRatePK) other;
		return (this.groupId == castOther.groupId) && this.rateTblId.equals(castOther.rateTblId);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + ((int) (this.groupId ^ (this.groupId >>> 32)));
		hash = hash * prime + this.rateTblId.hashCode();

		return hash;
	}

}