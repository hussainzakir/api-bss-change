package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The compound primary key class for the XBSS_EE_PLAN_ASSIGNMENT table.
 */
@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EePlanAssignmentPK implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "STRATEGY_ID")
	private long strategyId;

	@Column(name = "EMPLID")
	private String emplId;

	@Column(name = "BENEFIT_TYPE")
	private String benefitType;
}