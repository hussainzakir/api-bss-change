package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.Realm;
@Repository
@Transactional(readOnly = true)
public interface RealmTypeDao extends JpaRepository<Realm, Long> {

	List<Realm> findAll();

	@Query("SELECT r FROM Realm r WHERE r.id IN (SELECT DISTINCT ry.realmId FROM RealmPlanYear ry WHERE ry.oeQuarter =?1)")
	Realm findByQuarter(String oeQuarterId);

	Realm findById(long id);
}
