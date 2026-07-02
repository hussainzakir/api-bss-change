package com.trinet.ambis.service;

import com.trinet.ambis.enums.RiskTypeEnum;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.QuarterAndPlanYearDto;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.plancompare.model.PlanYearDetailDto;
import com.trinet.ambis.service.dto.RealmPlanYearDetailsDto;


@Service
public interface RealmPlanYearService {
	
        RealmPlanYear getRealmPlanYear(long Id, String quarter, Date planStartDate);
        RealmPlanYear getMaxRealmPlanYear(long Id, String quarter);
		RealmPlanYear getRealmPlanYearById(long id);
		RealmPlanYear getPreviousRealmPlanYear(String code, long realmPlanYearId);
		
		RealmPlanYear getCurrentRealmPlanYear(long realmId, String quarter);
		RealmPlanYear getNextRealmPlanYear(RealmPlanYear realmPlanYear);
		RealmPlanYear getPreviousRealmPlanYear(RealmPlanYear realmPlanYear);
		RealmPlanYear getLatestRealmPlanYear(long realmId, String quarter, Date converStringToDate);
		RealmPlanYear getRealmForCompanyId(long companyId);
		
		/**
		 * This method returns RealmPlanYear realmIds.
		 * 
		 * @return List<RealmPlanYear> 
		 */
		List<RealmPlanYear> getRealmPlanYearByIds(Set<Long> realmIds);
		
		List<QuarterAndPlanYearDto> getOeQuartersAndPlanYearsInfo();
		
		/**
		 * Find current and future plan years 
		 * 
		 * @param code
		 * @param quarterName
		 * 
		 * @return List<BenefitPlanDetailDto>
		 */
		List<PlanYearDetailDto> findCurrentAndFuturePlanYearsBy(String code, String quarterName);
		
		/**
		 * Gets the current and future realm plan years for the given realm
		 * 
		 * @param realmId
		 * @return
		 */
		List<RealmPlanYearDetailsDto> findByRealmId(long realmId);
		
		/**
		 * Get the realm plan years for the given quarter
		 *
		 * @param benifitStartDate
		 * @param quarter
		 * @return
		 */
		RealmPlanYear findRealmPlanYearBy(Date benifitStartDate, String quarter);

		/*
		 * Get the configured renewal risk type for the latest plan year for a given quarter
		 */
		RiskTypeEnum getRenewalRiskTypeForLatestPlanYearInQuarter(String quarter);
}
