/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Data;
import lombok.ToString;

/**
 * @author kpamulapati
 *
 */
@Entity
@Table(name = "xbss_strategy_group_planselect")
@ToString
@Data
public class PlanSelection {

	@Id
	@SequenceGenerator(name = "planSelectionSeq", sequenceName = "XBSS_STRATGRPPLNSELECT_SEQ", allocationSize = 128, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "planSelectionSeq")
	private long id;
	@Column(name = "STRATEGY_ID")
	private long strategyId;
	@Column(name = "GROUP_ID")
	private long groupId;
	@Column(name = "PLAN_TYPE")
	private String planType;
	@Column(name = "BENEFIT_PLAN")
	private String benefitPlan;
	@Column(name = "HEADCOUNT")
	private long headCount;
	@Column(name = "PPO")
	private boolean ppoPlan;
	@Transient
	private String crossPlan;
	@Transient
	private long planCarrierId;
	@Transient
	private String vendor;
	@Transient
	private String name;
	@Transient
	private String planCategory;
	@Transient
	private boolean highDeductiblePlan;
	@Transient
	private boolean isSdiPlan;
	@Transient
	private boolean isEmployeePaid;
	@Transient
	private List<String> listOfStates;
	@Transient
	private List<Contribution> contributions;

	public PlanSelection() {
		
	}
	
	public PlanSelection(PlanSelection ps, long groupId) {
		super();
		this.strategyId = ps.getStrategyId();
		this.groupId = groupId;
		this.planType = ps.getPlanType();
		this.benefitPlan = ps.getBenefitPlan();
		this.name = ps.getName();
		this.ppoPlan = ps.isPpoPlan();
		this.highDeductiblePlan = ps.isHighDeductiblePlan();
		this.headCount = 0;
		this.id = 0;
	}

}
