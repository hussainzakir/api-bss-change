package com.trinet.ambis.persistence.dao.hrp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.AppRules;

/**
 * @author schaudhari
 *
 */
@Repository
@Transactional(readOnly = true)
public interface AppRulesDao extends JpaRepository<AppRules, String> {

}