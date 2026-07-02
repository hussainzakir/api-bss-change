package com.trinet.ambis.persistence.dao.hrp;

import java.util.Optional;

import com.trinet.ambis.persistence.dao.hrp.dto.CensusHcSyncEventDto;

public interface RealTimeSyncDao {

	/**
	 * Returns census hc sync events in FIFO order when Optional.empty() is passed<br>
	 * Events are returned for a single company<br>
	 * Then updates status to I ie., in progress status <br>
	 * 
	 * @return Returns CensusHcSyncEventDto with company and its process status ids
	 *         <br>
	 *         Returns Optional.empty() if no events
	 */
	Optional<CensusHcSyncEventDto> findNewCensusHcSyncEventAndUpdateToInProgress(Optional<String> companyCode);

}
