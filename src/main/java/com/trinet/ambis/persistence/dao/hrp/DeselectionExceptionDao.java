package com.trinet.ambis.persistence.dao.hrp;

import java.util.Date;
import java.util.List;


public interface DeselectionExceptionDao {

	/**
	 * Run query to get pick & choose flag including possible exceptions
	 * @param realmYearId
	 * @param companyCode
	 * @param effdt
	 * @return
	 */
	List<Object[]> getPickChooseWithException( long realmYearId, String companyCode, Date effdt );

}