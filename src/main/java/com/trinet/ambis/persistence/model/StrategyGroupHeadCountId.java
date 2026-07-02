package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.ToString;

/**
 * This class represents the composite key for StrategyGroupHeadCount entity.
 * 
 * @author schaudhari
 *
 */
@Embeddable
@ToString
public class StrategyGroupHeadCountId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "STRATEGY_GROUP_ID", nullable = false)
	private long strategyGroupId;
	@Column(name = "COVRG_CD", nullable = false)
	private String covrgCd;

	public long getStrategyGroupId() {
		return strategyGroupId;
	}

	public void setStrategyGroupId(long strategyGroupId) {
		this.strategyGroupId = strategyGroupId;
	}

	public String getCovrgCd() {
		return covrgCd;
	}

	public void setCovrgCd(String covrgCd) {
		this.covrgCd = covrgCd;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((covrgCd == null) ? 0 : covrgCd.hashCode());
		result = prime * result + (int) (strategyGroupId ^ (strategyGroupId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StrategyGroupHeadCountId other = (StrategyGroupHeadCountId) obj;
		if (covrgCd == null) {
			if (other.covrgCd != null)
				return false;
		} else if (!covrgCd.equals(other.covrgCd))
			return false;
		if (strategyGroupId != other.strategyGroupId)
			return false;
		return true;
	}

}