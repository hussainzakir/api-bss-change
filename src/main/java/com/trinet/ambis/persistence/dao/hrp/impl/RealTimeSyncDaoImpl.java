package com.trinet.ambis.persistence.dao.hrp.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.RealTimeSyncDao;
import com.trinet.ambis.persistence.dao.hrp.dto.CensusHcSyncEventDto;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.service.ProcessStatusService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RealTimeSyncDaoImpl implements RealTimeSyncDao {

	private final ProcessStatusService processStatusService;

	/**
	 * Returns census hc sync event in FIFO order when Optional.empty() is passed as argument<br>
	 * Events are returned for a single company<br>
	 * Then updates status to I ie., in progress status <br>
	 * 
	 * @return Returns CensusHcSyncEventDto with company and its process status ids
	 *         <br>
	 *         Returns Optional.empty() if no events
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<CensusHcSyncEventDto> findNewCensusHcSyncEventAndUpdateToInProgress(Optional<String> companyCode) {
		List<ProcessStatus> events = companyCode.isPresent()
				? processStatusService.findNewCenusHcSyncEvent(companyCode.get())
				: processStatusService.findNewCenusHcSyncEvent();
		if (CollectionUtils.isEmpty(events)) {
			return Optional.empty();
		}
		events.forEach(event -> event.setProcessStatus(BSSApplicationConstants.PROCESS_STATUS_INPROGRESS));
		return Optional.of(CensusHcSyncEventDto.builder()
				.processStatusIds(events.stream().map(ProcessStatus::getId).collect(Collectors.toSet()))
				.companyCode(events.get(0).getProcessIdentiferValue()).build());
	}

}
