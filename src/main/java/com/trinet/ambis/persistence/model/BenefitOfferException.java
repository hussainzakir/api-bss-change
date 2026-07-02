package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
@Entity
@Table(name = "XBSS_BEN_OFFER_EXCEPTION")
public class BenefitOfferException implements Serializable, IExceptionDto {
	private static final long serialVersionUID = 1L;


	@Id
	@SequenceGenerator(name = "benefitOfferExceptionSeq", sequenceName = "XBSS_BEN_OFFER_EXCEPTION_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "benefitOfferExceptionSeq")
	private long id;

	@Column(name = "COMPANY")
	private String companyCode;

	@Column(name = "REALM_ID")
	private long realmId;

	@NotNull
	@Column(name = "QUARTER")
	private String quarter;

	@NotNull
	@Column(name = "PLAN_TYPE")
	private String planType;

	@Column(name = "OFFERED")
	private boolean offered;

	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "START_DATE")
	private Date startDate;

	@NotNull
	@Temporal(TemporalType.DATE)
	@Column(name = "END_DATE")
	private Date endDate;

	@Column(name = "ACTIVE")
	private boolean active;

	@NotNull
	@Column(name = "APPROVER")
	private String approverId;

	@NotNull
	@Column(name = "ORIGINATION")
	private String originDept;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATE_TIME")
	private Date createTime;

	@NotNull
	@Column(name = "CREATED_BY")
	private String createdById;

	@Column(name = "LASTUPDATEDBY")
	private String lastUpdatedById;

}
