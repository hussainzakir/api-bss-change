package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.trinet.ambis.jpa.converter.BooleanToStringConverter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author rvutukuri
 *
 */
@Entity
@Table(name = "xbss_submit_status")
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class SubmitStatus implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "submitStatusSeq", sequenceName = "XBSS_SUBMIT_STATUS_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "submitStatusSeq")
	private long id;

	@Column(name = "COMPANY")
	private String company;

	@Column(name = "CONFIRMATION_NUMBER")
	private String confirmationNumber;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATE_TIME")
	private Date createTime;

	@Convert(converter = BooleanToStringConverter.class)
	@Column(name = "EMAIL_SENT_STATUS")
	private Boolean emailSentStatus;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "submitStatus")
	private SubmitError submitError;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "submitStatus")
	private SubmitPayload submitPayload;

	@Column(name = "REALM_YEAR_ID")
	private Long realmYrId;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "STRATEGY_ID")
	private long strategyId;

	@Column(name = "USER_ID")
	private String userId;

	@Column(name = "SERVICE_ORDER")
	private String serviceOrder;

	@Column(name = "STATEMENT_UPLOAD_STATUS")
	private String statementUploadStatus;

	@Column(name = "UPDATE_TIME")
	private Date updateTime;

	@Convert(converter = BooleanToStringConverter.class)
	@Column(name = "SEND_EMAIL")
	private Boolean sendEmail;

}