/**
 * 
 */
package com.trinet.ambis.service.impl;

import java.util.Date;

import com.trinet.ambis.service.StrategyPreLoadService;

/**
 * @author rvutukuri
 *
 */
public class StrategyPreLoadTask implements Runnable {

	private StrategyPreLoadService strategyPreLoadService = new StrategyPreLoadServiceImpl();
	private String peoId;
	private String quarter;
	private Long relamYearId;
	private Date payrollCutOffDate;
	private String emplid;

	public StrategyPreLoadTask(String peoId, String quarter, Long relamYearId, Date payrollCutOffDate, String emplid) {
		this.peoId = peoId;
		this.quarter = quarter;
		this.relamYearId = relamYearId;
		this.payrollCutOffDate = payrollCutOffDate;
		this.emplid = emplid;

	}

	@Override
	public void run() {
		strategyPreLoadService.preLoadBssStrategies(peoId, quarter, relamYearId, payrollCutOffDate, emplid);
	}

	/**
	 * @return the peoId
	 */
	public String getPeoId() {
		return peoId;
	}

	/**
	 * @param peoId
	 *            the peoId to set
	 */
	public void setPeoId(String peoId) {
		this.peoId = peoId;
	}

	/**
	 * @return the quarter
	 */
	public String getQuarter() {
		return quarter;
	}

	/**
	 * @param quarter
	 *            the quarter to set
	 */
	public void setQuarter(String quarter) {
		this.quarter = quarter;
	}

	/**
	 * @return the relamYearId
	 */
	public Long getRelamYearId() {
		return relamYearId;
	}

	/**
	 * @param relamYearId
	 *            the relamYearId to set
	 */
	public void setRelamYearId(Long relamYearId) {
		this.relamYearId = relamYearId;
	}

	/**
	 * @return the payrollCutOffDate
	 */
	public Date getPayrollCutOffDate() {
		return payrollCutOffDate;
	}

	/**
	 * @param payrollCutOffDate
	 *            the payrollCutOffDate to set
	 */
	public void setPayrollCutOffDate(Date payrollCutOffDate) {
		this.payrollCutOffDate = payrollCutOffDate;
	}

}
