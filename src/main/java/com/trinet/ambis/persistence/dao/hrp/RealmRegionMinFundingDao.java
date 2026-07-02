package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.RealmRegionMinFunding;
import com.trinet.ambis.persistence.model.RealmRegionMinFundingPK;

@Repository
@Transactional(readOnly = true)
public interface RealmRegionMinFundingDao extends JpaRepository<RealmRegionMinFunding, RealmRegionMinFundingPK> {

	@Query( value="SELECT FND.* FROM XBSS_REALM_REGION_MIN_FUNDING FND, XBSS_REALM_PLAN_YEAR RPY WHERE RPY.ID = ?1 AND FND.OE_QUARTER = RPY.OE_QUARTER AND RPY.PLAN_YEAR_START BETWEEN FND.EFFDT AND FND.ENDDT", nativeQuery=true )
	List<RealmRegionMinFunding> findByid_realmYearId(long realmYearId);

}
