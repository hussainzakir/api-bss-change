package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import com.trinet.ambis.service.model.SchedTblAdminDto;

public interface SchedTblDataDao {
	
	/**
	 * Returns a list of the passed in company's current and future schedule table information.
	 * 
	 * @param companyCode
	 * @param quarter
	 * @return
	 */
	List<SchedTblAdminDto> getSchedTblAdminDates(String companyCode, String quarter);

}