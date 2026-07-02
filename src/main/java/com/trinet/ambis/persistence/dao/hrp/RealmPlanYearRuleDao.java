package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.RealmPlanYearRule;

/**
 * @author schaudhari
 *
 */
@Repository
@Transactional(readOnly = true)
public interface RealmPlanYearRuleDao extends JpaRepository<RealmPlanYearRule, Long> {

	/**
	 * Returns List of all {@code RealmPlanYearRule} for given RealmPlanYearId
	 * 
	 * @param realmPlanYearId
	 * @return {@code List<RealmPlanYearRule>}
	 */
	@Query( value="SELECT PRL.* FROM XBSS_REALM_PLYR_RULES PRL, XBSS_REALM_PLAN_YEAR RPY WHERE RPY.ID = ?1 AND PRL.OE_QUARTER = RPY.OE_QUARTER AND RPY.PLAN_YEAR_START BETWEEN PRL.EFFDT AND PRL.ENDDT", nativeQuery=true )
	List<RealmPlanYearRule> findByIdRealmPlanYearId(long realmPlanYearId);

}