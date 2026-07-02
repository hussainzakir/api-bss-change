/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * @author rvutukuri
 *
 */
@Entity
@Table(name = "xbss_strategy_funding_model")
@Getter
@Setter
public class StrategyFundingModel implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "strategyModelSeq", sequenceName = "XBSS_STRATEGY_MODEL_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "strategyModelSeq")
	@Column(name = "id")
	private Long id;
	@Column(name = "funding_type")
	private String fundingType;
	@Column(name = "plan_type")
	private String planType;
	@Column(name = "group_id")
	private Long groupId;
	@Column(name = "strategy_id")
	private Long strategyId;
	@Column(name = "base_benefit_plan")
	private String baseBenefitPlan;
	@Column(name = "name")
	private String name;
	@Column(name = "customized")
	private boolean customized;
	@Column(name = "waiver_allowance")
	private BigDecimal waiverAllowance;
	@Column(name = "BSUPP_EXCESS_OPTION")
	private BigDecimal bsuppExcessOption;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "strategyFundingModel")
	private Set<StrategyFundingDetail> fundingDetails = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "strategyFundingModel")
	private Set<StrategyFundingFlatMax> fundingFlatMax = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "strategyFundingModel")
	private Set<StrategyFundingBasePlanLimits> fundingBasePlanLimits = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "strategyFundingModel")
	private Set<StrategyFundBsuppPlanTypes> fundingBsuppPlanTypes = new HashSet<>();

	public StrategyFundingModel() {

	}

	public StrategyFundingModel(StrategyFundingModel sfm, long groupId) {
		this.id = 0L;
		this.baseBenefitPlan = sfm.baseBenefitPlan;
		this.customized = sfm.isCustomized();
		this.fundingType = sfm.getFundingType();
		this.groupId = groupId;
		this.name = sfm.getName();
		this.planType = sfm.getPlanType();
		this.strategyId = sfm.getStrategyId();
		this.waiverAllowance = sfm.getWaiverAllowance();
		this.bsuppExcessOption = sfm.getBsuppExcessOption();
	}
}
