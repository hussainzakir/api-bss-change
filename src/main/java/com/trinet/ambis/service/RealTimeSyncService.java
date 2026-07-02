package com.trinet.ambis.service;

import java.util.Optional;

import com.trinet.ambis.enums.RealTimeSyncServiceStatusEnum;
import com.trinet.ambis.persistence.model.Company;

public interface RealTimeSyncService {

	RealTimeSyncServiceStatusEnum eventDrivenSync(Optional<String> companyCode);

	void onDemandSync(Company company) ;

}
