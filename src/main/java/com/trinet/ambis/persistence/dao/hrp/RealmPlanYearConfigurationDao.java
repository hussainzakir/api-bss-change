package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.RealmPlanYearConfiguration;
/**
 * @author schaudhari
 *
 */
@Repository
@Transactional(readOnly = true)
public interface RealmPlanYearConfigurationDao extends JpaRepository<RealmPlanYearConfiguration, Long> {

	/**
	 * Returns List of RealmPlanYearConfiguration for given RealmPlanYearId
	 * @param realmPlanYearId
	 * @return {@code List<RealmPlanYearConfiguration>}
	 */
	@Query( value="SELECT CFG.* FROM XBSS_REALM_PLYR_CONFIGURATIONS CFG, XBSS_REALM_PLAN_YEAR RPY WHERE RPY.ID = ?1 AND CFG.OE_QUARTER = RPY.OE_QUARTER AND RPY.PLAN_YEAR_START BETWEEN CFG.EFFDT AND CFG.ENDDT", nativeQuery=true )
	List<RealmPlanYearConfiguration> findByIdRealmPlanYearId(long realmPlanYearId);

}
