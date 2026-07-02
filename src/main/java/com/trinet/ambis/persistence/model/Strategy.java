/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author kpamulapati
 *
 */

@Entity
@Table(name = "xbss_strategy")
@Data
@NoArgsConstructor
public class Strategy {
	@Id
	@SequenceGenerator(name = "strategySeq", sequenceName = "XBSS_STRATEGY_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "strategySeq")
	Long id;

	@Column(name = "NAME")
	private String name;

	@Column(name = "company_id")
	private long companyId;

	@Column(name = "PKG")
	private String pkgType;

	@Column(name = "TYPE")
	String type;

	@Column(name = "COST_SHARE_TYPE")
	private String costShareType;

	@Column(name = "TOTALBUDGET")
	BigDecimal totalBudget;

	@Column(name = "BUDGET_FACTOR")
	private int budgetFactor;

	@Column(name = "ESTIMATEDTOTALCOST")
	BigDecimal estimatedTotalCost;

	@Column(name = "CURRENTTOTALCOST")
	private BigDecimal currentYearTotalCost;

	@Column(name = "PERCENTCHANGE")
	private BigDecimal percentChange;

	@Column(name = "COMMENTS")
	private String comments;

	@Column(name = "SUBMITTED")
	boolean submitted;

	@Temporal(TemporalType.TIMESTAMP)
	private Date submitDate;
	
	@Setter
	@Getter
	@Column(name = "CREATED_BY")
	private String createdBy;
	
	@Setter
	@Getter
	@Column(name = "DEFAULT_SUBMIT")
	private boolean defaultSubmit;
	
	@Setter
	@Getter
	@Column(name = "UPDATED_BY")
	private String updatedBy;
	
	@Setter
	@Getter
	@Column(name = "UPDATETIME")
	private Date updateTime;
	
	@Setter
	@Getter
	@Column(name = "STATUS")
	private String status;
	
	
	@Setter
	@Getter
	@Column(name = "ACA_FPL_OPTED")
	private int acaFplOpted;

	@Transient
	private Long headCount;
	@Transient
	private Long realmPlanYearId;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "strategy")
	private Set<BenefitGroupStrategy> benefitGroupStrategy = new HashSet<>();
	
	@OneToMany(mappedBy = "strategy", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<StrategyRegion> strategyRegions;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	@Column(name = "NAME")
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the companyId
	 */
	@Column(name = "COMPANY_ID")
	public long getCompanyId() {
		return companyId;
	}

	/**
	 * @param companyId
	 *            the companyId to set
	 */
	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}

	/**
	 * @return the pkgType
	 */
	@Column(name = "PKG")
	public String getPkgType() {
		return pkgType;
	}

	/**
	 * @param pkgType
	 *            the pkgType to set
	 */
	public void setPkgType(String pkgType) {
		this.pkgType = pkgType;
	}

	/**
	 * @return the type
	 */
	@Column(name = "TYPE")
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the costShareType
	 */
	@Column(name = "COST_SHARE_TYPE")
	public String getCostShareType() {
		return costShareType;
	}

	/**
	 * @param costShareType
	 *            the costShareType to set
	 */
	public void setCostShareType(String costShareType) {
		this.costShareType = costShareType;
	}

	/**
	 * @return the totalBudget
	 */
	@Column(name = "TOTALBUDGET")
	public BigDecimal getTotalBudget() {
		return totalBudget;
	}

	/**
	 * @param totalBudget
	 *            the totalBudget to set
	 */
	public void setTotalBudget(BigDecimal totalBudget) {
		this.totalBudget = totalBudget;
	}

	/**
	 * @return the budgetFactor
	 */
	public int getBudgetFactor() {
		return budgetFactor;
	}

	/**
	 * @param budgetFactor
	 *            the budgetFactor to set
	 */

	public void setBudgetFactor(int budgetFactor) {
		this.budgetFactor = budgetFactor;
	}

	/**
	 * @return the estimatedTotalCost
	 */
	@Column(name = "ESTIMATEDTOTALCOST")
	public BigDecimal getEstimatedTotalCost() {
		return estimatedTotalCost;
	}

	/**
	 * @param estimatedTotalCost
	 *            the estimatedTotalCost to set
	 */
	public void setEstimatedTotalCost(BigDecimal estimatedTotalCost) {
		this.estimatedTotalCost = estimatedTotalCost;
	}

	/**
	 * @return the currentYearTotalCost
	 */
	@Column(name = "CURRENTTOTALCOST")
	public BigDecimal getCurrentYearTotalCost() {
		return currentYearTotalCost;
	}

	/**
	 * @param currentYearTotalCost
	 *            the currentYearTotalCost to set
	 */
	public void setCurrentYearTotalCost(BigDecimal currentYearTotalCost) {
		this.currentYearTotalCost = currentYearTotalCost;
	}

	/**
	 * @return the percentChange
	 */
	@Column(name = "PERCENTCHANGE")
	public BigDecimal getPercentChange() {
		return percentChange;
	}

	/**
	 * @param percentChange
	 *            the percentChange to set
	 */
	public void setPercentChange(BigDecimal percentChange) {
		this.percentChange = percentChange;
	}

	/**
	 * @return the comments
	 */
	@Column(name = "COMMENTS")
	public String getComments() {
		return comments;
	}

	/**
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * @return the submitted
	 */
	@Column(name = "SUBMITTED")
	public boolean isSubmitted() {
		return submitted;
	}

	/**
	 * @param submitted
	 *            the submitted to set
	 */
	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}

	/**
	 * @return the submitDate
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "SUBMITDATE")
	public Date getSubmitDate() {
		return submitDate;
	}

	/**
	 * @param submitDate
	 *            the submitDate to set
	 */
	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate;
	}

	/**
	 * @return the headCount
	 */
	public Long getHeadCount() {
		return headCount;
	}

	/**
	 * @param headCount
	 *            the headCount to set
	 */
	public void setHeadCount(Long headCount) {
		this.headCount = headCount;
	}

	/**
	 * @return the realmPlanYearId
	 */
	public Long getRealmPlanYearId() {
		return realmPlanYearId;
	}

	/**
	 * @param realmPlanYearId
	 *            the realmPlanYearId to set
	 */
	public void setRealmPlanYearId(Long realmPlanYearId) {
		this.realmPlanYearId = realmPlanYearId;
	}

	/**
	 * @return the benefitGroupStrategy
	 */
	public Set<BenefitGroupStrategy> getBenefitGroupStrategy() {
		return benefitGroupStrategy;
	}

	/**
	 * @param benefitGroupStrategy
	 *            the benefitGroupStrategy to set
	 */
	public void setBenefitGroupStrategy(Set<BenefitGroupStrategy> benefitGroupStrategy) {
		this.benefitGroupStrategy = benefitGroupStrategy;
	}

	public List<StrategyRegion> getStrategyRegions() {
		return strategyRegions;
	}

	public void setStrategyRegions(List<StrategyRegion> strategyRegions) {
		this.strategyRegions = strategyRegions;
	}
}
