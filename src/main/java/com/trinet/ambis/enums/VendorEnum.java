package com.trinet.ambis.enums;

/**
 * @author schaudhari
 */
public enum VendorEnum {

	AETNA("1", "AETNA", "Aetna"), 
	UHC("5", "UHC", "UHC"), 
	BCBSMN("22", "BCBSMN", "BCBS MN"),
	BCBSNC("23", "BCBSNC", "BCBS NC"),
	BCBSCA("11", "BLUE", "Blue Shield of CA"),
	BCBSFL("12", "BCBSFL", "Florida Blue"),
	KAISER("2", "KAISER", "Kaiser"),
	EMPIRENY("29", "EMPIRENY", "Empire NY");
	
	private String portfolioId;
	private String vendorId;
	private String desc;

	VendorEnum(String portfolioId, String vendorId, String desc) {
		this.portfolioId = portfolioId;
		this.vendorId = vendorId;
		this.desc = desc;
	}

	public String getVendorId() {
		return this.vendorId;
	}

	public String getDesc() {
		return this.desc;
	}
	
	public String getPortfolioId() {
		return this.portfolioId;
	}
	
	public static String getPortfolioIdFromVendorId(String vendorId) {
		for (VendorEnum value : values()) {
			if (value.vendorId.equals(vendorId)) {
				return value.portfolioId;
			}
		}
		throw new IllegalArgumentException("No enum const " + VendorEnum.class + "@vendorId." + vendorId);
	}
	
	
}
