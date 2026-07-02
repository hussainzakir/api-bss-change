package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.trinet.ambis.util.JsonDateDeserializer;
import com.trinet.ambis.util.JsonDateSerializer;
import com.trinet.ambis.util.JsonDateTimeDeserializer;
import com.trinet.ambis.util.JsonDateTimeSerializer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrategySummary implements Serializable {

	private static final long serialVersionUID = 1L;

	Long id;
	String name;
	String type;
	boolean submitted;
	Date submitDate;
	private Date effectiveDate;
	private Date endDate;
	String comments;
	BigDecimal estimatedTotalCost;
	BigDecimal currentYearTotalCost;
	BigDecimal percentChange;
	int totalEmployees;
	int headcount;
	BigDecimal totalBudget;
	int budgetFactor;
	private String companyId;
	private boolean acaFplOpted;
	private String pkgType;
	private String costShareType;
	private String submitStatus;
	private boolean isProspectCurrentStrategy;
	@Getter
	@Setter
	private boolean canDelete;
	@Getter
	@Setter
	List<String> filterRegions;

	@JsonIgnore
	List<String> medicalCarriers = new ArrayList<>();

	@JsonIgnore
	List<String> dentalCarriers = new ArrayList<>();

	@JsonIgnore
	List<String> visionCarriers = new ArrayList<>();

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
	 * @return the type
	 */
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
	 * @return the submitted
	 */
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
	@JsonSerialize(using = JsonDateTimeSerializer.class)
	public Date getSubmitDate() {
		return submitDate;
	}

	/**
	 * @param submitDate
	 *            the submitDate to set
	 */
	@JsonDeserialize(using = JsonDateTimeDeserializer.class)
	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate;
	}

	/**
	 * @return the effectiveDate
	 */
	@JsonSerialize(using = JsonDateSerializer.class)
	public Date getEffectiveDate() {
		return effectiveDate;
	}

	/**
	 * @param effectiveDate
	 *            the effectiveDate to set
	 */
	@JsonDeserialize(using = JsonDateDeserializer.class)
	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	/**
	 * @return the endDate
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the comments
	 */
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
	 * @return the estimatedTotalCost
	 */
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
	 * @return the totalEmployees
	 */
	public int getTotalEmployees() {
		return totalEmployees;
	}

	/**
	 * @param totalEmployees
	 *            the totalEmployees to set
	 */
	public void setTotalEmployees(int totalEmployees) {
		this.totalEmployees = totalEmployees;
	}

	/**
	 * @return the headcount
	 */
	public int getHeadcount() {
		return headcount;
	}

	/**
	 * @param headcount
	 *            the headcount to set
	 */
	public void setHeadcount(int headcount) {
		this.headcount = headcount;
	}

	/**
	 * @return the totalBudget
	 */
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
	 * @return the companyId
	 */
	public String getCompanyId() {
		return companyId;
	}

	/**
	 * @param companyId
	 *            the companyId to set
	 */
	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	/**
	 * @return the acaFplOpted
	 */
	public boolean isAcaFplOpted() {
		return acaFplOpted;
	}

	/**
	 * @param acaFplOpted
	 *            the acaFplOpted to set
	 */
	public void setAcaFplOpted(boolean acaFplOpted) {
		this.acaFplOpted = acaFplOpted;
	}

	/**
	 * @return the pkgType
	 */
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
	 * @return the costShareType
	 */
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
	 * @return the medicalCarriers
	 */
	public List<String> getMedicalCarriers() {
		return medicalCarriers;
	}

	/**
	 * @param medicalCarriers
	 *            the medicalCarriers to set
	 */
	public void setMedicalCarriers(List<String> medicalCarriers) {
		this.medicalCarriers = medicalCarriers;
	}

	/**
	 * @return the dentalCarriers
	 */
	public List<String> getDentalCarriers() {
		return dentalCarriers;
	}

	/**
	 * @param dentalCarriers
	 *            the dentalCarriers to set
	 */
	public void setDentalCarriers(List<String> dentalCarriers) {
		this.dentalCarriers = dentalCarriers;
	}

	/**
	 * @return the visionCarriers
	 */
	public List<String> getVisionCarriers() {
		return visionCarriers;
	}

	/**
	 * @param visionCarriers
	 *            the visionCarriers to set
	 */
	public void setVisionCarriers(List<String> visionCarriers) {
		this.visionCarriers = visionCarriers;
	}

	public String getSubmitStatus() {
		return submitStatus;
	}

	public void setSubmitStatus(String submitStatus) {
		this.submitStatus = submitStatus;
	}

	public boolean isProspectCurrentStrategy() {
		return isProspectCurrentStrategy;
	}

	public void setProspectCurrentStrategy(boolean isProspectCurrentStrategy) {
		this.isProspectCurrentStrategy = isProspectCurrentStrategy;
	}

}
