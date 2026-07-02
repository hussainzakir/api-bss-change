package com.trinet.ambis.persistence.model;

import java.io.Serializable;

public class RealmPlanMappingId implements Serializable {

	private static final long serialVersionUID = 1L;

	private long realmYearId;

	private String plan;

	public long getRealmYearId() {
		return realmYearId;
	}

	public void setRealmYearId(long realmYearId) {
		this.realmYearId = realmYearId;
	}

	public String getPlan() {
		return plan;
	}

	public void setPlan(String plan) {
		this.plan = plan;
	}

}
