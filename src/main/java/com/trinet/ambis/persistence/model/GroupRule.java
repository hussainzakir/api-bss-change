package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

/**
 * The persistent class for the XBSS_GROUP_RATE database table.
 * 
 */
@Data
@Entity
@Table(name = "XBSS_GROUP_RULE")
public class GroupRule implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	private long id;

	@Column(name = "GROUP_TYPE")
	private String groupType;

	@Column(name = "STATE")
	private String state;

	@Column(name = "PLAN_TYPE")
	private String planType;

	@Column(name = "MANDATORY")
	private boolean mandatory;

	@Column(name = "FLOW_TYPE")
	private String flowType;

	@Column(name = "ALLOW_MULTIPLES")
	private boolean allowMultiples;

	@Column(name = "APPLIES_TO_NOMED")
	private boolean appliesToNoMed;

	@Column(name = "DEFAULT_GROUP_NAME")
	private String defaultGroupName;

	@Column(name = "RULE_NAME")
	private String ruleName;
	
	@Column(name = "SORT_ORDER")
	private int sortOrder;

	@Temporal(TemporalType.DATE)
	@Column(name = "EFFDT")
	private Date effDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "EXPDT")
	private Date expDate;

}
