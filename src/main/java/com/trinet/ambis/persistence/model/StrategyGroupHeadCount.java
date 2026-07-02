package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.ToString;

/**
 * This class is JPA entity for XBSS_STRATEGY_GROUP_COV_HC table.
 * 
 * @author schaudhari
 *
 */
@Entity
@Table(name = "XBSS_STRATEGY_GROUP_COV_HC")
@ToString
public class StrategyGroupHeadCount implements Serializable {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private StrategyGroupHeadCountId id;
	@Column(name = "HEADCOUNT", nullable = false)
	private long headcount;

	public StrategyGroupHeadCountId getId() {
		return id;
	}

	public void setId(StrategyGroupHeadCountId id) {
		this.id = id;
	}

	public long getHeadcount() {
		return headcount;
	}

	public void setHeadcount(long headcount) {
		this.headcount = headcount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (headcount ^ (headcount >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		StrategyGroupHeadCount other = (StrategyGroupHeadCount) obj;
		if (headcount != other.headcount)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
