/**
 * 
 */
package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * @author rvutukuri
 *
 */
@Entity
@Table(name = "XBSS_STRATEGY_GROUP")
public class BenefitGroupStrategy implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "strategyGroupSeq", sequenceName = "XBSS_STRATEGY_GROUP_SEQ", allocationSize = 1, initialValue = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "strategyGroupSeq")
	private long id;

	@Column(name = "GROUP_ID")
	private long groupId;

	@Column(name = "STRATEGY_ID")
	private long strategyId;

	@Column(name = "WAITING_PERIOD")
	private String waitingPeriod;

	@Column(name = "DEFAULT_GROUP")
	private boolean defaultGroup;

	@Column(name = "HEADCOUNT")
	private long headcount;

	@Column(name = "STATUS")
	private String status;

	@MapsId("groupId")
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "GROUP_ID", insertable = false, updatable = false)
	private BenefitGroup benefitGroup;

	@MapsId("strategyId")
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "STRATEGY_ID", insertable = false, updatable = false)
	private Strategy strategy;

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the groupId
	 */
	public long getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId
	 *            the groupId to set
	 */
	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the strategyId
	 */
	public long getStrategyId() {
		return strategyId;
	}

	/**
	 * @param strategyId
	 *            the strategyId to set
	 */
	public void setStrategyId(long strategyId) {
		this.strategyId = strategyId;
	}

	/**
	 * @return the waitingPeriod
	 */
	public String getWaitingPeriod() {
		return waitingPeriod;
	}

	/**
	 * @param waitingPeriod
	 *            the waitingPeriod to set
	 */
	public void setWaitingPeriod(String waitingPeriod) {
		this.waitingPeriod = waitingPeriod;
	}

	/**
	 * @return the defaultGroup
	 */
	public boolean isDefaultGroup() {
		return defaultGroup;
	}

	/**
	 * @param defaultGroup
	 *            the defaultGroup to set
	 */
	public void setDefaultGroup(boolean defaultGroup) {
		this.defaultGroup = defaultGroup;
	}

	/**
	 * @return the headcount
	 */
	public long getHeadcount() {
		return headcount;
	}

	/**
	 * @param headcount
	 *            the headcount to set
	 */
	public void setHeadcount(long headcount) {
		this.headcount = headcount;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the benefitGroup
	 */
	@MapsId("groupId")
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "GROUP_ID", insertable = false, updatable = false)
	public BenefitGroup getBenefitGroup() {
		return benefitGroup;
	}

	/**
	 * @param benefitGroup
	 *            the benefitGroup to set
	 */
	public void setBenefitGroup(BenefitGroup benefitGroup) {
		this.benefitGroup = benefitGroup;
	}

	/**
	 * @return the strategy
	 */
	@MapsId("strategyId")
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "STRATEGY_ID", insertable = false, updatable = false)
	public Strategy getStrategy() {
		return strategy;
	}

	/**
	 * @param strategy
	 *            the strategy to set
	 */
	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

}
