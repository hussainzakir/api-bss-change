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

import lombok.Data;


@Data
@Entity
@Table(name = "xbss_employee_strategy_group_trnx")
public class EmployeeStrategyGroupTransaction implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator( name = "esgTxSeq", sequenceName = "XESG_TX_SEQ", allocationSize = 1, initialValue = 1 )
	@GeneratedValue( strategy = GenerationType.SEQUENCE, generator = "esgTxSeq" )
	private long id;

	@Column(name = "EMPLID")
	private String emplid;

	@Column(name = "STRATEGY_GROUP_ID")
	private long strategyGroupId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATE_DATE")
	private Date createDate;
}
