/**
 * 
 */
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
import javax.persistence.Transient;

import lombok.Data;

/**
 * @author rvutukuri
 *
 */
@Data
@Entity
@Table(name = "XBSS_SCHED_MID_YEAR_FUNDING")
public class SchedMidYearFunding implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@SequenceGenerator(name = "midYearSeq", sequenceName = "XBSS_MID_YEAR_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "midYearSeq")
	private long id;

	@Column(name = "COMPANY_ID")
	private long companyId;

	@Column(name = "SERVICE_ORDER_NUM")
	private String serviceOrderNumber;

	@Temporal(TemporalType.DATE)
	@Column(name = "MID_YEAR_EFFDT")
	private Date midYearFundingEffDate;

	@Column(name = "ACTIVE")
	private boolean active;

	@Column(name = "LASTUPDATEDBY")
	private String lastUpdatedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "UPDTDTTM")
	private Date updtdtm;

    @Transient
    private boolean isFuturePlanYear;

    @Transient
    private boolean isFutureExisting;

}
