package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
public class SchedTblId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "COMPANY", nullable = false)
	private String company;
	@Column(name = "realm_year_id")
	private long realmYearId;

	@Column(name = "realm_year_id")
	public long getRealmYearId() {
		return realmYearId;
	}

	public void setRealmYearId(long realmYearId) {
		this.realmYearId = realmYearId;
	}

	@Column(name = "COMPANY", nullable = false)
	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

}