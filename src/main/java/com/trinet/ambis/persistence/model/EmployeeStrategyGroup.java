package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author hliddle
 */
@Entity
@Table(name = "XBSS_EMPLOYEE_STRATEGY_GROUP")
public class EmployeeStrategyGroup implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "employeeStrategyGroupSeq", sequenceName = "XBSS_EMPLOYEE_STRATEGY_GRP_SEQ", allocationSize = 100, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employeeStrategyGroupSeq")
	private long id;

	@Column(name = "EMPLID")
	private String emplId;

	@Column(name = "STRATEGY_GROUP_ID")
	private long strategyGroupId;

	@Column(name = "EMPLID")
	public String getEmplId() {
		return emplId;
	}

	public void setEmplId(String emplId) {
		this.emplId = emplId;
	}

	@Column(name = "STRATEGY_GROUP_ID")
	public long getStrategyGroupId() {
		return strategyGroupId;
	}

	public void setStrategyGroupId(long strategyGroupId) {
		this.strategyGroupId = strategyGroupId;
	}
}