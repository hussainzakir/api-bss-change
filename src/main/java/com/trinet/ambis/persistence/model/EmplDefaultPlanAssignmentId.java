package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author schaudhari
 *
 */
@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmplDefaultPlanAssignmentId implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "COMPANY_ID")
	@NotNull
	private long companyId;
	
	@Column(name = "EMPLID")
	@NotNull
	private String emplId;
	
	@Column(name = "PLAN_TYPE")
	@NotNull
	private String planType;
	
	@Column(name = "PORTFOLIO_ID")
	@NotNull
	private int portfolioId;
	
	@Column(name = "BENEFIT_PLAN")
	@NotNull
	private String benefitPlanId;
	
	@Column(name = "COVRG_CD")
	@NotNull
	private String coverageCode;

}
