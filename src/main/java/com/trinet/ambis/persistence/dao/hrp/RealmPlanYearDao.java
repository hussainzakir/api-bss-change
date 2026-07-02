package com.trinet.ambis.persistence.dao.hrp;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.RealmPlanYear;

@Repository
@Transactional(readOnly = true)
public interface RealmPlanYearDao extends JpaRepository<RealmPlanYear, Long> {

	List<RealmPlanYear> findAll();
    
	@Query("Select r from RealmPlanYear r where r.id in (Select max(r1.id) from RealmPlanYear r1 where r1.realmId = ?1 and r1.oeQuarter = ?2)")
    RealmPlanYear getMaxRealmPlanYearByRealmIdAndQuarter(long realmId, String quarter);
	
	@Query("Select r from RealmPlanYear r where r.id in (Select max(r1.id) from RealmPlanYear r1 where r1.oeQuarter = ?1)")
    RealmPlanYear getMaxRealmPlanYearByQuarter(String quarter);
    
	@Query("Select r from RealmPlanYear r  where r.realmId = ?1 and r.oeQuarter = ?2 and ?3 between r.planYearStart and r.planYearEnd" )
    RealmPlanYear findByRealmIdAndOeQuarterAndPlanYearStart(long id, String oeQuarter, Date companyPlanStartDate);
  
    RealmPlanYear findById(long id);
    
    @Query("select r from RealmPlanYear r where r.id in  (select max(r1.id) from RealmPlanYear r1 where r1.id < ?1 and r1.realmId = ?2 and r1.oeQuarter = ?3)")
    RealmPlanYear findPreviousRealmPlanYearByRealmIdAndOeQuarter(long realmPlanYearId, long realmId, String quarter);
    
    @Query("Select r from RealmPlanYear r where r.id = (select max(realmPlanYearId) from Company c where c.code = ?1 and c.realmPlanYearId != ?2)")
    RealmPlanYear findPreviousRealmPlanYearByRealmPlanYearId(String code, long realmPlanYearId);
    
	@Query("Select r from RealmPlanYear r  where r.realmId = ?1 and r.oeQuarter = ?2 and trunc(sysdate) between r.planYearStart and r.planYearEnd" )
	RealmPlanYear getCurrentRealmPlanYear(long realmId, String quarter);

	@Query("Select r from RealmPlanYear r where r.id in (Select min(r2.id) from RealmPlanYear r2 where r2.realmId = ?1 and r2.oeQuarter = ?2 and r2.planYearStart > ?3)")
	RealmPlanYear getNextRealmPlanYear(long realmId, String oeQuarter, Date planYearEnd);

	@Query("Select r1 from RealmPlanYear r1 where id in (Select min(id) from RealmPlanYear r  where r.realmId = ?1 and r.oeQuarter = ?2 and ?3 < r.planYearStart)" )
	RealmPlanYear findByLatestRealmIdAndOeQuarterAndPlanYearStart(long realmId, String quarter,Date companyPlanStartDate);

	@Query("Select r1 from RealmPlanYear r1 where id = (select c.realmPlanYearId from Company c where c.id = ?1)" )
	RealmPlanYear findByCompanyId(long companyId);
	
	@Query("select r.benExchange, r.peoid ,rpy.oeQuarter, rpy.planYearStart from RealmPlanYear rpy, Realm r  "
			+ "where rpy.planYearEnd > sysdate and  rpy.realmId = r.id and rpy.id= (select max(id) from RealmPlanYear rl where rl.oeQuarter = rpy.oeQuarter )"
			+ " order by rpy.planYearStart  desc , r.benExchange, rpy.oeQuarter" )
	List<Object[]> getQuartersAndPlanYearsInfo();

	@Query("Select r1 from RealmPlanYear r1 where id in (?1)" )
	List<RealmPlanYear> findBy(Set<Long> realmIds);
	
	/**
	 * Returns the current and future realm plan years for a given realm(exchange)
	 * 
	 * @param realmId
	 * @return
	 */
	@Query("Select rpy from RealmPlanYear rpy where rpy.realmId in (?1) and ( rpy.planYearStart > sysdate or sysdate between rpy.planYearStart and rpy.planYearEnd ) ")
	List<RealmPlanYear> findByRealmId(long realmId);
	
	RealmPlanYear findByOeQuarterAndPlanYearStart(String quarter,Date planYearStart);
	
	/**
	 * Returns the current and future realm plan years for a given realm(exchange)
	 * 
	 * @param quarter
	 * @return
	 */
    @Query("Select rpy from RealmPlanYear rpy where ?1 between rpy.planYearStart and rpy.planYearEnd and rpy.oeQuarter = ?2")
	RealmPlanYear findByOeQuarter(Date benifitStartDate, String quarter); 


}
