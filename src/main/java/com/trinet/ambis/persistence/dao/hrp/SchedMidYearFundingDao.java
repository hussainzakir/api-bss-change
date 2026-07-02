/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.SchedMidYearFunding;

/**
 * @author rvutukuri
 *
 */
@Repository
@Transactional(readOnly = true)
public interface SchedMidYearFundingDao extends JpaRepository<SchedMidYearFunding, Long> {
	/**
	 * 
	 * @param companyId
	 * @return
	 */
	List<SchedMidYearFunding> findByCompanyId(long companyId);

	/**
	 * 
	 * @param companyId
	 * @return
	 */
	@Query("Select m from SchedMidYearFunding m where m.id in (Select m1.id from SchedMidYearFunding m1 where m1.companyId = ?1 and m1.active = 1)")
	SchedMidYearFunding getActiveSchedMidYearFundingByCompanyId(long companyId);

	/**
	 * 
	 * @param companyId
	 * @return
	 */
	@Query("Select m from SchedMidYearFunding m where m.companyId in (Select c.id from Company c, RealmPlanYear r where c.code = ?1 and c.realmPlanYearId = r.id) order by m.midYearFundingEffDate, m.id")
	List<SchedMidYearFunding> findByCompanyCode(String companyCode);

	/**
	 * 
	 * @param companyId
	 * @return
	 */
	@Query("Select m from SchedMidYearFunding m where m.companyId in (Select c.id from Company c, RealmPlanYear r where c.code = ?1 and c.realmPlanYearId = r.id and trunc(sysdate) <= r.planYearEnd) order by m.midYearFundingEffDate, m.id")
	List<SchedMidYearFunding> findByCompanyCodePlanYearEndDate(String companyCode);

}
