package com.trinet.ambis.service.model.prospect;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProspectInfoResponse {

	String companyName;
	String hqState;
	List<String> employeeHomeStates;
	String zipCode;
	String benStartDate;
	String primaryNaicsCode;
	@JsonProperty("isK1Company")
	boolean isK1Company;
	String proposalId;
	String expiryDate;
	@JsonProperty("isContingentPricing")
	boolean isContingentPricing;
	String commonOwnerCompanyCode;
	boolean benefitsQuarterException;
	String quarterEffectiveDate;
}