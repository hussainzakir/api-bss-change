package com.trinet.ambis.service.model;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Realm;

@Service
public interface RealmTypeService {

	Realm findById(long RealmId);

	Realm findByQuarter(String oeQuarterId);

}
