package com.trinet.ambis.persistence.dao.hrp;

import com.trinet.ambis.persistence.model.BSSStatusDetailsDto;

public interface BSSStatusDetailsDao {

	/**
	 * get submitted strategies status and bssstarted status
	 * 
	 * @param company
	 * 
	 */
	BSSStatusDetailsDto getSubmitedStatus(String companyCode);

}
