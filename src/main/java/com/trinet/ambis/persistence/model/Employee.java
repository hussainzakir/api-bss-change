package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import lombok.Data;

/**
 * @author mpulipaka
 */
@Entity
@Table(name = "XBSS_EMPLOYEE")
@Data
public class Employee implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "EMPLID")
	private String emplId;

	@Column(name = "COMPANY")
	private String company;

	@Column(name = "BENEFIT_PROGRAM")
	private String benefitProgram;

	@Column(name = "ELIG_CONFIG1")
	private String eligConfig1;
	
	@Column(name = "UPDATED_BENEFIT_PROGRAM")
	private String updatedBenefitProgram;

	@Column(name = "BENEFIT_GROUP_ID")
	private long benefitGroupId;

	@Column(name = "BENEFIT_GROUP_NAME")
	private String benefitGroupName;

	@Column(name = "EMPL_RCD")
	private long emplRcd;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "EFFDT")
	private Date effdt;

	@Transient
	private String emplName;

	@Transient
	private String department;

	@Transient
	private String location;

	@Transient
	private String jobTitle;

	@Transient
	private long strategyGroupId;

	@Temporal(TemporalType.TIMESTAMP)
	Date updateTime;

	@Column(name = "Realm_Year_Id")
	private long realmYearId;
	
	@Transient
	private boolean isBenefitProgramUpdated;

    @Transient
    private boolean k1;

}