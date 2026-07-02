package com.trinet.ambis.persistence.model;

import java.io.Serializable;

public class RealmPlanMapping implements Serializable {
	private static final long serialVersionUID = 1L;

	RealmPlanMappingId planMappingId;
	
	private long oldPortfolioId;

	private String oldPlanDesc;

	private String planType;

	private String newPlan;

	private long newPortfolioId;

	private String newPlanDesc;

	public RealmPlanMappingId getPlanMappingId() {
		return planMappingId;
	}

	public void setPlanMappingId(RealmPlanMappingId planMappingId) {
		this.planMappingId = planMappingId;
	}

	public long getOldPortfolioId() {
		return oldPortfolioId;
	}

	public void setOldPortfolioId(long oldPortfolioId) {
		this.oldPortfolioId = oldPortfolioId;
	}

	public String getOldPlanDesc() {
		return oldPlanDesc;
	}

	public void setOldPlanDesc(String oldPlanDesc) {
		this.oldPlanDesc = oldPlanDesc;
	}

	public String getPlanType() {
		return planType;
	}

	public void setPlanType(String planType) {
		this.planType = planType;
	}

	public String getNewPlan() {
		return newPlan;
	}

	public void setNewPlan(String newPlan) {
		this.newPlan = newPlan;
	}

	public long getNewPortfolioId() {
		return newPortfolioId;
	}

	public void setNewPortfolioId(long newPortfolioId) {
		this.newPortfolioId = newPortfolioId;
	}

	public String getNewPlanDesc() {
		return newPlanDesc;
	}

	public void setNewPlanDesc(String newPlanDesc) {
		this.newPlanDesc = newPlanDesc;
	}

}
