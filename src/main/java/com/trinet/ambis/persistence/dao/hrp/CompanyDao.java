package com.trinet.ambis.persistence.dao.hrp;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.Company;

@Repository
@Transactional(readOnly = true)
public interface CompanyDao extends JpaRepository<Company, Long> {

	List<Company> findAll();

	Company findByCodeAndRealmPlanYearId(String code, long id);

	List<Company> findByRealmPlanYearId(Long realmPlanYearId);

	List<Company> findByCode(String code);

	@Query("select co"
		+  "  from Company co"
		+  "     , RealmPlanYear rpy"
		+  " where co.code = :companyCode"
		+  "   and rpy.id = co.realmPlanYearId "
		+  "   and rpy.realmId = ("
		+  "       select rl.id"
		+  "         from Realm rl"
		+  "        where rl.benExchange = :benExchange )"
		+  "   and rpy.oeQuarter = :oeQuarter"
		+ "    and :benStartDt between rpy.planYearStart and rpy.planYearEnd")
	Company findCompanyBy(@Param("companyCode") String companyCode, @Param("benExchange") String benExchange,
			@Param("oeQuarter") String oeQuarter, @Param("benStartDt") Date benStartDt);

	@Query("select co"
		+  "  from Company co"
		+  "     , Realm rlm"
		+  "     , RealmPlanYear rpy"
		+  " where co.code = :companyCode"
		+  "   and rlm.benExchange = :benExchange"
		+  "   and rpy.realmId = rlm.id"
		+  "   and co.realmPlanYearId = rpy.id")
	List<Company> findCompaniesBy(@Param("companyCode") String companyCode, @Param("benExchange") String benExchange);

	@Query("select co"
			+  " from Company co"
			+  "    where co.code = :companyCode"
			+  "     and co.realmPlanYearId = "
			+  "        (select MAX(co1.realmPlanYearId)"
			+  "        from Company co1"
			+  "        where co1.code = :companyCode)")
	Company findLatestCompanyBy(@Param("companyCode") String companyCode);

	@Query(value =
			  "SELECT CO.CODE, CO.REALM_YEAR_ID, RPY.REALM_ID "
			+ "  FROM XBSS_COMPANY CO "
			+ "     , XBSS_REALM_PLAN_YEAR RPY "
			+ " WHERE CO.ID = :companyId "
			+ "   AND RPY.ID = CO.REALM_YEAR_ID ", nativeQuery = true)
	List<Object[]> getCompanyCodeAndRealm(@Param("companyId") long companyId);
	
	/**
	 * Updates PLYR_CHANGE_SYNC_EXCUTED for a given company.
	 * 
	 * @param companyId
	 * @param plYrChangeSyncExcuted
	 */
	@Modifying
	@Query("update Company C set C.plYrChangeSyncExcuted = :plYrChangeSyncExcuted, C.prospectId = :prospectId, C.strategyAccessed = :strategyAccessed where C.id = :companyId")
	int updateCompanyForPlanYearSync(@Param("companyId") long companyId, @Param("plYrChangeSyncExcuted") Integer plYrChangeSyncExcuted, @Param("prospectId") String prospectId,
			@Param("strategyAccessed") Integer strategyAccessed);
	
	/**
	 * Updates company code for company id of a prospect
	 * @param companyId
	 * @param code
	 */
	@Modifying
	@Query("update Company C set C.code = :code where C.id = :companyId")
	int updatePsCompanyCodeForProspect(@Param("companyId") long companyId,
			@Param("code") String code);
}

