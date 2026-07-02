package com.trinet.ambis.service.model;

import java.util.Set;

public class BenefitOfferPackage implements Comparable<BenefitOfferPackage>{
	    private String type;

	    public String getType() {
	        return type;
	    }

	    public void setType(String type) {
	        this.type = type;
	    }
	    
	    private Set<Long> planCarrierIds;

	   
	    public Set<Long> getPlanCarrierIds() {
	        return planCarrierIds;
	    }
	    
	    public void setPlanCarrierIds(Set<Long> planCarrierIds) {
	        this.planCarrierIds = planCarrierIds;
	    }

	    private long planPackageId;

	    public long getPlanPackageId() {
	        return planPackageId;
	    }

	   
	    public void setPlanPackageId(long planPackageId) {
	        this.planPackageId = planPackageId;
	    }

	@Override
	public int compareTo( BenefitOfferPackage o ) {
		if( this.getType() == null ) {
			return -1;
		} else if( o == null || o.getType() == null ) {
			return +1;
		} else {
			return this.getType().compareTo( o.getType() );
		}
	}
}
