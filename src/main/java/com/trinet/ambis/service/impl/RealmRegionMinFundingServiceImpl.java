package com.trinet.ambis.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.RealmRegionMinFundingDao;
import com.trinet.ambis.persistence.model.RealmRegionMinFunding;
import com.trinet.ambis.service.RealmRegionMinFundingService;

@Service
public class RealmRegionMinFundingServiceImpl implements RealmRegionMinFundingService {

    @Autowired
    RealmRegionMinFundingDao realmRegionMinFundingDao;

    @Override
    public List<RealmRegionMinFunding> findAll() {
        return realmRegionMinFundingDao.findAll();
    }

    @Override
    public List<RealmRegionMinFunding> findByid_realmYearId(long realmYearId) {
        return realmRegionMinFundingDao.findByid_realmYearId(realmYearId);
    }
}
