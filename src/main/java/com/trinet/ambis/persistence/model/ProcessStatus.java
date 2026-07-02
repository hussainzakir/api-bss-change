package com.trinet.ambis.persistence.model;

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

import lombok.Data;

/**
 * @author rvutukuri
 *
 */
@Entity
@Table(name = "xbss_process_status")
@Data
public class ProcessStatus {

	@Id
	@SequenceGenerator(name = "processSeq", sequenceName = "xbss_process_status_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "processSeq")

	private long id;

	@Column(name = "P_NAME")
	private String processName;

	@Column(name = "P_IDENTIFER")
	private String processIdentifer;

	@Column(name = "P_IDENTIFER_VALUE")
	private String processIdentiferValue;

	@Column(name = "P_STATUS_CODE")
	private String processStatus;

	@Column(name = "ERROR_MSG")
	private String errorMessage;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATE_TIME")
	private Date createTime;

	@Column(name = "USER_ID")
	private String userId;

	@Column(name = "EFFDT")
	private Date effDt;
	
	@Column(name = "PROCESS_DATA")
	private String processData;
}
