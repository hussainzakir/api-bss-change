/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import org.springframework.stereotype.Repository;

import com.trinet.ambis.persistence.model.SchedMidYearFunding;

@Repository 
public interface SchedMidYearFundingDataDao {

	/**
	 * @param companyCode
	 * @return
	 */
	SchedMidYearFunding getMidYearFundingScheduleForCompany(String companyCode);

}
