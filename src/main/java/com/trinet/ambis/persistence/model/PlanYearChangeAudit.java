package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "XBSS_PLAN_YEAR_CHANGE_AUDIT")
public class PlanYearChangeAudit implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@SequenceGenerator(name = "auditSeq", sequenceName = "XBSS_PLR_CHANGE_AUDIT_SEQ", allocationSize = 1,initialValue = 1)
	@GeneratedValue(strategy = GenerationType.IDENTITY,generator = "auditSeq")
	@Column(name = "ID")
	private Long id;

	@Column(name = "COMPANY_CODE", nullable = false, length = 255)
	private String companyCode;

	@Column(name = "OLD_BENEFIT_START_DATE", nullable = false)
	private LocalDate oldBenefitStartDate;

	@Column(name = "OLD_QUARTER", nullable = false, length = 10)
	private String oldQuarter;

	@Column(name = "NEW_BENEFIT_START_DATE", nullable = false)
	private LocalDate newBenefitStartDate;

	@Column(name = "NEW_QUARTER", nullable = false, length = 10)
	private String newQuarter;

	@Column(name = "SERVICE_ORDER_NUM", nullable = false, length = 255)
	private String serviceOrderNum;

	@Column(name = "QUARTER_EXCEPTION", length = 1)
	private String quarterException;

	@Column(name = "COMMON_OWNER_COMPANY", length = 10)
	private String commonOwnerCompany;

	@Column(name = "CHANGED_BY", nullable = false, length = 255)
	private String changedBy;

	@Column(name = "CHANGE_TIMESTAMP", nullable = false)
	private LocalDate changeTimestamp;
}
