package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "XBSS_EE_PLAN_ASSIGNMENT")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EePlanAssignment implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonUnwrapped
	@EmbeddedId
	private EePlanAssignmentPK eePlanAssignmentPK;

	@Column(name = "BENEFIT_PLAN")
	private String benefitPlan;

	@Column(name = "PORTFOLIO_ID")
	private long portfolioId;
	
	@Column(name = "COVRG_CD")
	private String covrgCD;

	@Column(name = "EE_RATE")
	private BigDecimal eeRate;

	@Column(name = "ER_RATE")
	private BigDecimal erRate;

}