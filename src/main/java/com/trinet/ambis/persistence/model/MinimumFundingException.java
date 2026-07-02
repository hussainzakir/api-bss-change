/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.math.BigDecimal;
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

/**
 * @author schaudhari
 *
 */
@Data
@Entity
@Table(name = "XBSS_MIN_FUND_EXCEPTION")
public class MinimumFundingException implements Serializable, IExceptionDto {

	private static final long serialVersionUID = 1L;

	

	@Id
	@SequenceGenerator(name = "minFundExceptionSeq", sequenceName = "XBSS_MIN_FUND_EXCEPTION_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "minFundExceptionSeq")
	private long id;

	@Column(name = "COMPANY")
	private String companyCode;

	@NotNull
	@Column(name = "PLAN_TYPE")
	private String planType;

	@Column(name = "MIN_FUND_VALUE_TYPE")
	private String minFundType;

	@Column(name = "MIN_FUND_VALUE")
	private BigDecimal minFundValue;

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
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATE_TIME")
	private Date createTime;

	@NotNull
	@Column(name = "CREATED_BY")
	private String createdById;

	@Column(name = "LASTUPDATEDBY")
	private String lastUpdatedById;

	@NotNull
	@Column(name = "REALM_ID")
	private long realmId;

	@NotNull
	@Column(name = "QUARTER")
	private String quarter;

}
