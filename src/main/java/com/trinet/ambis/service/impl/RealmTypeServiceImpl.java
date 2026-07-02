package com.trinet.ambis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.RealmTypeDao;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.service.model.RealmTypeService;

@Service
public class RealmTypeServiceImpl implements RealmTypeService {

	@Autowired
	RealmTypeDao realmTypeDao;

	/**
	 * @param realmTypeDao the realmTypeDao to set
	 */
	public void setRealmTypeDao(RealmTypeDao realmTypeDao) {
		this.realmTypeDao = realmTypeDao;
	}

	@Override
	public Realm findByQuarter(String oeQuarterId) {
		return realmTypeDao.findByQuarter(oeQuarterId);
	}

	@Override
	public Realm findById(long realmId) {
		return realmTypeDao.findById(realmId);
	}
}
