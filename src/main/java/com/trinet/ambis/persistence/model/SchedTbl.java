package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.NoArgsConstructor;

@Entity
@Table(name = "xbss_sched_tbl")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class SchedTbl implements Serializable {

	private static final long serialVersionUID = 1L;

	@Transient
	@JsonIgnore
	@JsonProperty("id")
	private String ui;
	@JsonUnwrapped
	@EmbeddedId
	private SchedTblId sched;
	
	@Transient
	private boolean payrollProcessed;

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "OPEN_DT")
	private Date openDate;

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "CLOSE_DT")
	private Date closeDate;

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "INTRNL_OPEN_DT")
	private Date internalOpenDate;

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "INTRNL_CLOSE_DT")
	private Date internalCloseDate;

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "EXTN_ENDDT")
	private Date extensionEndDate;

	@JsonIgnore
	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "UPDTDTTM")
	private Date updateTime;

	@JsonIgnore
	@Column(name = "LASTUPDATEDBY")
	private String lastUpdatedBy;

	@EmbeddedId
	public SchedTblId getSched() {
		return sched;
	}

	public void setSched(SchedTblId sched) {
		this.sched = sched;
	}

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "OPEN_DT")
	public Date getOpenDate() {
		return openDate;
	}

	public void setOpenDate(Date opendate) {
		this.openDate = opendate;
	}

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "CLOSE_DT")
	public Date getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Date closedate) {
		this.closeDate = closedate;
	}

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "EXTN_ENDDT")
	public Date getExtensionEndDate() {
		return extensionEndDate;
	}

	public void setExtensionEndDate(Date extensionenddate) {
		this.extensionEndDate = extensionenddate;
	}

	@Transient
	public boolean isPayrollProcessed() {
		return payrollProcessed;
	}

	public void setPayrollProcessed(boolean payrollProcessed) {
		this.payrollProcessed = payrollProcessed;
	}

	@Transient
	@JsonIgnore
	public String getUi() {
		return ui;
	}

	public void setUi(String ui) {
		this.ui = ui;
	}

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "INTRNL_OPEN_DT")
	public Date getInternalOpenDate() {
		return internalOpenDate;
	}

	public void setInternalOpenDate(Date internalOpenDate) {
		this.internalOpenDate = internalOpenDate;
	}

	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "INTRNL_CLOSE_DT")
	public Date getInternalCloseDate() {
		return internalCloseDate;
	}

	public void setInternalCloseDate(Date internalCloseDate) {
		this.internalCloseDate = internalCloseDate;
	}

	@JsonIgnore
	@Temporal(TemporalType.DATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "UPDTDTTM")
	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updatetime) {
		this.updateTime = updatetime;
	}

	@JsonIgnore
	@Column(name = "LASTUPDATEDBY")
	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

}