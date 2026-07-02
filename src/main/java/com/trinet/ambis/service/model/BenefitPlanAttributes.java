package com.trinet.ambis.service.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@IdClass(BenefitPlanAttributes.Key.class)
public class BenefitPlanAttributes {

	@Id
	private String planId;

	private String planName;
	
	private String benefitType;
	
	private String carrier;

	@Id
	private String attributeName;
	
	private String displayName;
	
	private String attributeValue;
	
	private String dataType;
	
	private String transformedValue;

	@Getter
	@Setter
	@EqualsAndHashCode
	public static class Key implements Serializable {
		private static final long serialVersionUID = 4787385305378735134L;
		private String planId;
		private String attributeName;
	}
	
}


