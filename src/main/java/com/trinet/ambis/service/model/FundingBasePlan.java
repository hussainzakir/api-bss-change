package com.trinet.ambis.service.model;

import java.io.Serializable;

public class FundingBasePlan implements Serializable {

    private static final long serialVersionUID = 1L;
	
	Long planCarrierId;
	String fundingBasePlan;

	public Long getPlanCarrierId() {
		return planCarrierId;
	}
	public void setPlanCarrierId(Long planCarrierId) {
		this.planCarrierId = planCarrierId;
	}
	public String getFundingBasePlan() {
		return fundingBasePlan;
	}
	public void setFundingBasePlan(String fundingBasePlan) {
		this.fundingBasePlan = fundingBasePlan;
	}
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fundingBasePlan == null) ? 0 : fundingBasePlan.hashCode());
        result = prime * result + ((planCarrierId == null) ? 0 : planCarrierId.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FundingBasePlan other = (FundingBasePlan) obj;
        if (fundingBasePlan == null) {
            if (other.fundingBasePlan != null)
                return false;
        } else if (!fundingBasePlan.equals(other.fundingBasePlan))
            return false;
        if (planCarrierId == null) {
            if (other.planCarrierId != null)
                return false;
        } else if (!planCarrierId.equals(other.planCarrierId))
            return false;
        return true;
    }
	
	
}
