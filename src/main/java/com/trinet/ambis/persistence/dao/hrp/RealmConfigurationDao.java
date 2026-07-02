package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.RealmConfiguration;
/**
 * @author schaudhari
 *
 */
@Repository
@Transactional(readOnly = true)
public interface RealmConfigurationDao extends JpaRepository<RealmConfiguration, Long> {

	/**
	 * Returns List of RealmConfiguration for given RealmId
	 * @param realmId
	 * @return {@code List<RealmConfiguration>}
	 */
	List<RealmConfiguration> findByIdRealmId(long realmId);
	    
}