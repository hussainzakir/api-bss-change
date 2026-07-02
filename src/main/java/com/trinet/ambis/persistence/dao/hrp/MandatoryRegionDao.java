package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.MandatoryRegion;
import com.trinet.ambis.persistence.model.MandatoryRegionPK;

/**
 * @author schaudhari
 *
 */
@Repository
@Transactional(readOnly = true)
public interface MandatoryRegionDao extends JpaRepository<MandatoryRegion, MandatoryRegionPK> {

	/**
	 * This method returns list of all MandatoryRegion for given realmYearId
	 * 
	 * @param realmYrId
	 * @return List
	 */
	@Query( value="SELECT reg FROM MandatoryRegion reg, RealmPlanYear rpy WHERE rpy.id = ?1 AND reg.id.oeQuarter = rpy.oeQuarter AND rpy.planYearStart BETWEEN reg.id.effdt AND reg.enddt" )
	List<MandatoryRegion> findAllByRealmYrId(long realmYrId); 
}
