package com.trinet.ambis.enums;

public enum SelectedBenefitsEnums {
	
	MEDICAL("medical"),
	COMMUTER("cmtr"),
	STD("std");
	
	private final String benefit;
	
	private SelectedBenefitsEnums(String benefit) {
		this.benefit = benefit;	
	}
	  public String getBenefitId() {
	        return benefit;
	   }

}
