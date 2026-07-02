package com.trinet.ambis.service;

import java.util.List;

import com.trinet.ambis.persistence.model.RealmRegionMinFunding;

public interface RealmRegionMinFundingService {

    List<RealmRegionMinFunding> findAll();
    
    List<RealmRegionMinFunding> findByid_realmYearId(long realmYearId);

}
