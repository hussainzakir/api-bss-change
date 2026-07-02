package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "xbss_realm_plan_year")
public class RealmPlanYear implements Serializable {

	private static final long serialVersionUID = 1L;

	public RealmPlanYear(long id, long realmId, String oeQuarter, long minFunding, boolean mbgNew, boolean mbgRenewal,
			Date planYearStart, Date planYearEnd, String cloneProgram, BigDecimal aleAmount, BigDecimal avgSalary,
			int acaFplOpt, String micrositeUrl, boolean k1Flag) {
		super();
		this.id = id;
		this.realmId = realmId;
		this.oeQuarter = oeQuarter;
		this.minFunding = minFunding;
		this.mbgNew = mbgNew;
		this.mbgRenewal = mbgRenewal;
		this.planYearStart = planYearStart;
		this.planYearEnd = planYearEnd;
		this.cloneProgram = cloneProgram;
		this.aleAmount = aleAmount;
		this.avgSalary = avgSalary;
		this.acaFplOpt = acaFplOpt;
		this.micrositeUrl = micrositeUrl;
		this.k1Flag = k1Flag;
	}

	public RealmPlanYear() {
		super();
	}

	@Id
	@Column(name = "ID")
	private long id;
	@Column(name = "REALM_ID")
	private long realmId;
	@Column(name = "OE_QUARTER")
	private String oeQuarter;
	@Column(name = "MIN_FUNDING")
	private long minFunding;
	@Column(name = "MBG_NEW")
	private boolean mbgNew;
	@Column(name = "MBG_RENEWAL")
	private boolean mbgRenewal;
	@Column(name = "ACA_FPL_AMOUNT")
	private BigDecimal aleAmount;
	@Column(name = "AVG_SALARY")
	private BigDecimal avgSalary;
	@Column(name = "ACA_FPL_OPT")
	private int acaFplOpt;
	@Column(name = "MICROSITE_URL")
    private String micrositeUrl;
	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "America/Los_Angeles")
	@Column(name = "PLAN_YEAR_START")
	private Date planYearStart;
	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "America/Los_Angeles")
	@Column(name = "PLAN_YEAR_END")
	private Date planYearEnd;
	@Column(name = "K1_FLAG")
	private boolean k1Flag;
	@Transient
	private String cloneProgram;


	@Column(name = "ID")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "REALM_ID")
	public long getRealmId() {
		return realmId;
	}

	public void setRealmId(long realmId) {
		this.realmId = realmId;
	}

	@Column(name = "OE_QUARTER")
	public String getOeQuarter() {
		return oeQuarter;
	}

	public void setOeQuarter(String oeQuarter) {
		this.oeQuarter = oeQuarter;
	}

	@Column(name = "MIN_FUNDING")
	public long getMinFunding() {
		return minFunding;
	}

	public void setMinFunding(long minFunding) {
		this.minFunding = minFunding;
	}

	@Column(name = "MBG_NEW")
	public boolean isMbgNew() {
		return mbgNew;
	}

	public void setMbgNew(boolean mbgNew) {
		this.mbgNew = mbgNew;
	}

	@Column(name = "MBG_RENEWAL")
	public boolean isMbgRenewal() {
		return mbgRenewal;
	}

	public void setMbgRenewal(boolean mbgRenewal) {
		this.mbgRenewal = mbgRenewal;
	}

	@Column(name = "ACA_FPL_AMOUNT")
	public BigDecimal getAleAmount() {
		return aleAmount;
	}

	public void setAleAmount(BigDecimal aleAmount) {
		this.aleAmount = aleAmount;
	}

	@Column(name = "AVG_SALARY")
	public BigDecimal getAvgSalary() {
		return avgSalary;
	}

	public void setAvgSalary(BigDecimal avgSalary) {
		this.avgSalary = avgSalary;
	}

	@Column(name = "ACA_FPL_OPT")
	public int getAcaFplOpt() {
		return acaFplOpt;
	}

	public void setAcaFplOpt(int acaFplOpt) {
		this.acaFplOpt = acaFplOpt;
	}
	
	@Column(name = "MICROSITE_URL")
	public String getMicrositeUrl() {
        return micrositeUrl;
    }

    public void setMicrositeUrl(String micrositeUrl) {
        this.micrositeUrl = micrositeUrl;
    }

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "America/Los_Angeles")
	@Column(name = "PLAN_YEAR_START")
	public Date getPlanYearStart() {
		return planYearStart;
	}

	public void setPlanYearStart(Date planYearStart) {
		this.planYearStart = planYearStart;
	}

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "America/Los_Angeles")
	@Column(name = "PLAN_YEAR_END")
	public Date getPlanYearEnd() {
		return planYearEnd;
	}

	public void setPlanYearEnd(Date planYearEnd) {
		this.planYearEnd = planYearEnd;
	}

	@Transient
	public String getCloneProgram() {
		return cloneProgram;
	}

	public void setCloneProgram(String cloneProgram) {
		this.cloneProgram = cloneProgram;
	}

	/**
	 * @return the k1Flag
	 */
	@Column(name = "K1_FLAG")
	public boolean isK1Flag() {
		return k1Flag;
	}

	/**
	 * @param k1Flag the k1Flag to set
	 */
	public void setK1Flag(boolean k1Flag) {
		this.k1Flag = k1Flag;
	}


}
