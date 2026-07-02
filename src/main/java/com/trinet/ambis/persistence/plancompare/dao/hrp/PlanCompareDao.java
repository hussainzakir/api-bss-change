package com.trinet.ambis.persistence.plancompare.dao.hrp;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.trinet.ambis.persistence.plancompare.model.BenefitPlanDetailDto;
import com.trinet.ambis.persistence.plancompare.model.MappedPlanDetailDto;
import com.trinet.ambis.persistence.plancompare.model.PlanYearDetailDto;

/**
 * 
 * @author rpittala
 *
 */
@Repository
public interface PlanCompareDao {

	/**
	 * Fetching the plan years for given quarter
	 * 
	 * @param quarterName
	 * @param planYearDate
	 * @param lastXYears
	 * @param nextXYears 
	 * 
	 * @return List<PlanCompareDto>
	 */
	List<PlanYearDetailDto> findPlanYearDetailsBy(String quarterName, String planYearDate, int lastXYears, int nextXYears);
	
	/**
	 * Find the current year plans for the given company code and realm
	 * 
	 * @param companyCode
	 * @param realmYearId
	 * 
	 * @return List<BenefitPlanDetailDto>
	 */
	List<BenefitPlanDetailDto> findSubmittedStrategyPlansBy(String companyCode, String realmYearId);
	
	
	/**
	 * Find all plans for the given company code and realm
	 * 
	 * @param realmYearId
	 * 
	 * @return List<BenefitPlanDetailDto>
	 */
	List<BenefitPlanDetailDto> findAllFutureYearPlansBy (String realmYearId);
	
	/**
	 * Returns the mapping plans for the given futureRealmYearId
	 * 
	 * @param futureRealmYearId
	 * @param currentRealmYearId
	 * @return
	 */
	List<MappedPlanDetailDto> findMappingBenefitPlansBy(String futureRealmYearId, String currentRealmYearId);
	
}
