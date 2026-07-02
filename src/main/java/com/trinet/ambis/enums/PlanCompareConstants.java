package com.trinet.ambis.enums;

public enum PlanCompareConstants {
	
	CURRENT("current"),
	FUTURE("future");
	
	private String action;
	
	PlanCompareConstants(String action) {
		this.action = action;
	}

	public String getAction(){
		return action;
	}
	
}
