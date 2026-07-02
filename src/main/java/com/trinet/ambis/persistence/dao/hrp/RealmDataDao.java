package com.trinet.ambis.persistence.dao.hrp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmCloneProgram;
import com.trinet.ambis.persistence.model.RealmPlanMapping;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.planofferings.CarrierData;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.FundingType;
import com.trinet.ambis.service.model.ProductQuarters;
import com.trinet.ambis.service.model.StateBenefitPlan;

public interface RealmDataDao {
	/**
	 * This method is for getting coverage level based on planType and plan
	 * year.
	 * 
	 * @param planType
	 * @param realmPlanYearId
	 * @return
	 */
	List<CoverageLevel> getCoverageCodes(String planType, long realmPlanYearId);

	/**
	 * This method is for getting all benefit plans for Additional benefits
	 * based on plan year and region.
	 * 
	 * @param planYearId
	 * @param regions
	 * @param company
	 * @return
	 */

	Map<String, Set<StateBenefitPlan>> getAdditionalBenefitsAllStatePlans(long planYearId, Set<String> regions,
			Company company);

	/**
	 * This method is for getting coverage codes map based on planType and plan
	 * year.
	 * 
	 * @param planTypes
	 * @param realmPlanYearId
	 * @param commonData
	 * @return
	 */
	Map<String, LinkedHashMap<String, String>> getCoverageCodesDescrByPlanTypes(List<String> planTypes,
			long realmPlanYearId);

	/**
	 * This method is for getting coverage codes map based on planType and plan
	 * year.
	 * 
	 * @param planTypes
	 * @param realmPlanYearId
	 * @return
	 */
	Map<String, List<CoverageLevel>> getCoverageCodesDescByPlanTypes(List<String> planTypes, long realmPlanYearId);

	/**
	 * This method is for getting coverage codes map.
	 * 
	 * @return
	 */
	Map<String, String> getCoverageCodeMap();

	/**
	 * This method is for getting the selected benefits of the company based on
	 * Realm year id and region.
	 * 
	 * @param realmPlanYearId
	 * @param region
	 * @return map of selected Benefits.
	 */
	Map<String, Boolean> getSelectedBenefits(long realmPlanYearId, Set<String> regions);

	/**
	 * 
	 * /**
	 * 
	 * @param planTypes
	 * @param realmPlanYearId
	 * @return
	 */
	Map<String, LinkedHashMap<String, Integer>> getCoverageCodesByPlanTypes(List<String> planTypes,
			long realmPlanYearId);

	/**
	 * This method is for getting auto selected plans based on realm year and
	 * region excluding outOfRegionPlans.

	 * @param planYearId
	 * @param company
	 * @param outOfRegionPlans
	 * @return
	 */
	Map<String, Map<String, Set<String>>> getAutoSelectPlansByRealmIdAndPlanTypes(long planYearId,
			Company company, Set<String> outOfRegionPlans);

	/**
	 * 
	 * @param realmYearId
	 * @return
	 */
	public RealmCloneProgram getRealmCloneProgram(long realmYearId);

	/**
	 * 
	 * @param plans
	 * @param realmYearId
	 *            a long integer representing a BSS realm plan year
	 * @return
	 */
	public Map<String, String> getPlanVendors(Set<String> plans, long realmYearId);

	/**
	 * 
	 * @param realmYearId
	 * @return
	 */
	boolean isCommuterBenefitOffered(long realmYearId);

	/**
	 * 
	 * @param realmPlanYearId
	 * @return
	 */
	List<FundingType> getRealmFundingTypes(long realmPlanYearId);

	List<String> getLifeSupplementalPlanTypes(long realmYearId);

	Map<String, List<String>> getBenefitPlansStates(long planYearId);

	Set<String> getCompanyLocationStates(String companyCode);

	/**
	 * Returns a set of regions for any funding base plans the client has setup in
	 * PS last year or this year.
	 * 
	 * @param company
	 * @param currentRegions
	 * @param realmPlanYear
	 * @return
	 */
	Set<String> getFundingPlanStates(Company company, Set<String> currentRegions,
			RealmPlanYear realmPlanYear);
	
	List<String> getEmployeeHomeStates(String companyCode);

	/**
	 * 
	 * @return
	 */
	Map<String, ProductQuarters> getAllProductQuarters();

	/**
	 * 
	 * @param realmPlanYearId
	 * @return
	 */
	Map<String, Map<String, String>> getPortfilioDefaultPlans(long realmPlanYearId);

	/**
	 * 
	 * @param realmPlanYearId
	 * @param region
	 * @param benExchange
	 * @return
	 */
	Map<String, AdditionalBenefitPlan> getDisabilityOptionPlans(Long realmPlanYearId, String region, String benExchange,
			Set<String> sdiStates);

	/**
	 * getRenewalFundingDetailsBSS
	 * 
	 * @param company
	 * @param realmPlanYearId
	 * @return
	 */
	Map<String, Map<String, Map<String, Object>>> getRenewalFundingDetailsBSS(String company, long realmPlanYearId);

	/**
	 * 
	 * @param company
	 * @return
	 */
	List<String> getADBenefitPlans(Company company);

	/**
	 * 
	 * @param rateTableId
	 * @return
	 */
	boolean validateRateTableId(String rateTableId, String benefitProgram);

	/**
	 * 
	 * @param selectedPlans
	 * @param realmPlanYearId
	 * @return
	 */
	Map<String, Map<String, List<String>>> getAutoSelectedPlansByRegion(Set<String> selectedPlans, long realmPlanYearId);

	/**
	 * 
	 * @param selectedPlans
	 * @param realmPlanYearId
	 * @return
	 */
	Map<String, List<String>> getRegionForSelectedPlans(Set<String> selectedPlans, long realmPlanYearId);

	/**
	 * 
	 * @param realmPlanYearId
	 * @param benefitPlans
	 * @return
	 */
	Map<String, List<String>> getBenefitsPlans(long realmPlanYearId, Set<String> benefitPlans);

	/**
	 * 
	 * @param realmPlanYearId
	 * @param regions
	 * @return
	 */
	List<String> getSelectedBenefitsExceptVoluntary(long realmPlanYearId, Set<String> regions);

	/**
	 * @param company
	 * @param strategyId
	 * @param outOfRegionBSPlans
	 * @return
	 */
	Set<String> getAutoSelectedMedicalPlansForPassport(Company company, Long strategyId,
			Set<String> outOfRegionBSPlans);

	/**
	 * This method returns a set of all non applicable BS plans for given
	 * company. Company's HQ zip code is used to identify whether the company HQ
	 * is located in Northern CA or Southern CA and realm planYearStartDate to
	 * pick correct quarter plans.
	 * 
	 * Ex. This method will return all the Northern BS plans if company zip-code
	 * falls under Southern CA and vice versa.
	 * 
	 * @param company
	 * @return Set
	 */
	Set<String> getBSOutOfRegionPlans(Company company);

	/**
	 * This method returns a map of disability option names and plan option sets
	 * 
	 * @param realmPlanYearId
	 * @param prevRealmPlanYearId
	 * @param region
	 * @param includeK1
	 * @return Map
	 */
	Map<String, AdditionalBenefitPlan> getDisabilityOptionsForRealmPlanYears(long realmPlanYearId,
			long prevRealmPlanYearId, String region, boolean includeK1);

	/**
	 * This method returns a set of all non applicable plans for given carrier
	 * and company. Company's HQ zip code, planYearStartDate, quarter
	 * medPlanGroup (carrier) and is used to identify non applicable plans.
	 * 
	 * @param company
	 * @param medPlanGroups
	 * @return
	 */
	Set<String> getCarrierOutOfRegionPlans(Company company, Set<String> medPlanGroups);

	/**
	 * 
	 * @param strategyId
	 * @return
	 */
	Map<String, Map<String, Map<String, Object>>> getStrategyFundingDetails(long strategyId);

	/**
	 * 
	 * @param realmPlanYearId
	 * @param selectedPlans
	 * @param locations
	 * @return
	 */
	Map<String, List<String>> getSelectedPlansByRegion(long realmPlanYearId, Set<String> selectedPlans);

	/**
	 *
	 * @param zipCode
	 * @param realmPlanYearId
	 * @param state
	 * @param reportCode
	 * @param planType
	 * @return
	 */
	List<CarrierData> getCarriersBy(String zipCode, long realmPlanYearId, String state, String reportCode, String planType);



}
