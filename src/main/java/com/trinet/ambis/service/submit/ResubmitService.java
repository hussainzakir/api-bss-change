package com.trinet.ambis.service.submit;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.trinet.ambis.service.model.StrategyData;

@Service
public interface ResubmitService {

	/**
	 * Perform resubmit
	 * 
	 * @param companyCode
	 * @param sendClientEmail
	 * @return
	 */
	StrategyData resubmit(String companyCode, boolean sendClientEmail);

	/**
	 * Perform re-submission for band code change. 1. The effDt must be equal to
	 * plan year start date for renewal company or benefit start date for new
	 * company. 2. Re-submission will be performed only if the prior submitted
	 * strategy exits for plan year matching effDt. 3. There should not be any
	 * service order after effDt. 4. Band code should be updated for given effDt.
	 * 
	 * @param companyCode
	 * @param effDt
	 */
	void bandcodeResubmit(String companyCode, Date effDt);
}
