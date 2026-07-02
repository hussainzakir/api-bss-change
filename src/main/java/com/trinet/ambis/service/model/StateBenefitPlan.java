package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class StateBenefitPlan implements Serializable {

	private static final long serialVersionUID = 1L;

	String benefitPlan;
	String description;
	String planType;
	String vendorId;
	String crossRefPlanId;
	long portfolioId;
	int realmYearId;
	boolean isMandatory;
	String planCategory;
	private List<String> offeredStates;
	@JsonIgnore
	boolean isTexasSitus;
	Long displaySeq;

}
