/**
 * 
 */
package com.trinet.ambis.service.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author rvutukuri
 *
 */
public class StrategyCoverageLevelHeadcount {

	private Long strategyId;
	private boolean offered;

	@JsonInclude(Include.NON_EMPTY)
	private Map<String, Long> coverageHeadcount;

	/**
	 * @return the strategyId
	 */
	public Long getStrategyId() {
		return strategyId;
	}

	/**
	 * @param strategyId
	 *            the strategyId to set
	 */
	public void setStrategyId(Long strategyId) {
		this.strategyId = strategyId;
	}

	/**
	 * @return the offered
	 */
	public boolean isOffered() {
		return offered;
	}

	/**
	 * @param offered
	 *            the offered to set
	 */
	public void setOffered(boolean offered) {
		this.offered = offered;
	}

	/**
	 * @return the coverageHeadcount
	 */
	public Map<String, Long> getCoverageHeadcount() {
		return coverageHeadcount;
	}

	/**
	 * @param coverageHeadcount
	 *            the coverageHeadcount to set
	 */
	public void setCoverageHeadcount(Map<String, Long> coverageHeadcount) {
		this.coverageHeadcount = coverageHeadcount;
	}
}
