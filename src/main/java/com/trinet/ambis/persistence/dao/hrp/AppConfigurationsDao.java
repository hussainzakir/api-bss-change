package com.trinet.ambis.persistence.dao.hrp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.AppConfigurations;

/**
 * @author hliddle
 *
 */
@Repository
@Transactional(readOnly = true)
public interface AppConfigurationsDao extends JpaRepository<AppConfigurations, String> {

}