/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "XBSS_STRATEGY_DEFAULT_PLAN")
@Data
public class StrategyDefaultPlan implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	private String id;

	@Column(name = "OE_QUARTER")
	private String quarter;
	
	@Column(name = "PLAN_TYPE")
	private String planType;

	@Column(name = "EFFDT")
	private Date effectiveDate;

	@Column(name = "ENDDT")
	private Date endDate;

	@Column(name = "PORTFOLIO_ID")
	private long portfolioId;

	@Column(name = "BASE_BENEFIT_PLAN")
	private String baseBenefitPlan;
}
