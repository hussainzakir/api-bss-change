package com.trinet.ambis.persistence.dao.hrp.impl;

import static com.trinet.ambis.util.Constants.additionalPlanTypeList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.common.PlanOfferingReportConstants;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmCloneProgram;
import com.trinet.ambis.persistence.model.RealmCloneProgramId;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.planofferings.CarrierData;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.FundingType;
import com.trinet.ambis.service.model.ProductQuarters;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.DaoUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;

public class RealmDataDaoImpl implements RealmDataDao {
    
	private static final Logger logger = LoggerFactory.getLogger(RealmDataDaoImpl.class);

	public static final String GET_REALM_CLONE_PGM = "getRealmCloneProgram";

	@PersistenceContext(unitName = "bis-hrp")
    EntityManager em;
	@PersistenceContext(unitName = "bis-sysadm")
	EntityManager psEm;

	@Override
	public List<CoverageLevel> getCoverageCodes(String planType, long realmPlanYearId) {
		Query q = em.createNamedQuery("getCoverageCodes");
	    q.setParameter(BSSQueryConstants.PLAN_TYPE, planType);
	    q.setParameter(BSSQueryConstants.PLAN_YEAR_ID, realmPlanYearId);

		List<Object[]> results = DaoUtils.getResultList(q, "getCoverageCodes");
        List<CoverageLevel> list = new ArrayList<>(results.size());

        for (Object[] r : results) {
        	CoverageLevel cc = new CoverageLevel(); 
        	cc.setId((String) r[0]);
        	cc.setName((String) r[1]);
        	list.add(cc);        
        }
        return list;
	}

	@Override
	public Map<String, List<String>> getBenefitsPlans(long realmPlanYearId, Set<String> benefitPlans) {
		Map<String, List<String>> benefitPlansStatesMap = new HashMap<>();
		final Collection<List<String>> bucketedList = CommonUtils.getBucketedList(
				new ArrayList<>(benefitPlans), BSSApplicationConstants.QUERY_IN_CLAUSE_PARTITION_SIZE);
		
		for (List<String> bucket : bucketedList) {
			Query q = em.createNamedQuery("BENEFIT_PLANS");
			q.setParameter("benefitPlans", bucket);
			q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
			List<Object[]> results = DaoUtils.getResultList(q, "BENEFIT_PLANS");
			for (Object[] result : results) {
				String benefitPlan = (String) result[0];
				String state = (String) result[1];
				if (null == benefitPlansStatesMap.get(benefitPlan)) {
					List<String> states = new ArrayList<>();
					states.add(state);
					benefitPlansStatesMap.put(benefitPlan, states);
				} else {
					List<String> states = benefitPlansStatesMap.get(benefitPlan);
					states.add(state);
				}
			}
		}
		return benefitPlansStatesMap;
	}
	
	
	@Override
	public Map<String, List<String>> getSelectedPlansByRegion(long realmPlanYearId, Set<String> selectedPlans) {
		final Collection<List<String>> bucketedList = CommonUtils.getBucketedList(new ArrayList<>(selectedPlans),
				BSSApplicationConstants.QUERY_IN_CLAUSE_PARTITION_SIZE);
		List<String> allRegionPlans = new ArrayList<>();
		Map<String, List<String>> selectedPlansByRegion = new HashMap<>();
		for (List<String> bucket : bucketedList) {
			Query q = em.createNamedQuery("BENEFIT_PLANS_BY_REGION");
			q.setParameter("benefitPlans", bucket);
			q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
			List<Object[]> results = DaoUtils.getResultList(q, "BENEFIT_PLANS_BY_REGION");
			if (null != results && !results.isEmpty()) {
				for (String sBP : selectedPlans) {
					boolean planExists = false;
					for (Object[] r : results) {
						String benefitPlan = (String) r[0];
						if (benefitPlan.equals(sBP)) {
							planExists = true;
						}
					}
					if (!planExists) {
						allRegionPlans.add(sBP);
					}
				}
				if (null != allRegionPlans && !allRegionPlans.isEmpty()) {
					selectedPlansByRegion.put("all", allRegionPlans);
				}
				for (Object[] r : results) {

					String benefitPlan = (String) r[0];
					String region = (String) r[1];

					if (null != selectedPlansByRegion.get(region)) {
						List<String> benefitPlans = selectedPlansByRegion.get(region);
						benefitPlans.add(benefitPlan);
					} else {
						List<String> benefitPlans = new ArrayList<>();
						benefitPlans.add(benefitPlan);
						selectedPlansByRegion.put(region, benefitPlans);
					}
				}
			} else {
				allRegionPlans.addAll(selectedPlans);
				selectedPlansByRegion.put("all", allRegionPlans);
			}

		}
		return selectedPlansByRegion;
	}
	
	@Override
	public Map<String, LinkedHashMap<String, Integer>> getCoverageCodesByPlanTypes(List<String> planTypes, long realmPlanYearId) {
		Query q = em.createNamedQuery(BSSQueryConstants.GET_COVERAGE_CODES_BY_PLAN_TYPES);
	    q.setParameter(BSSQueryConstants.PLAN_TYPES, planTypes);
	    q.setParameter(BSSQueryConstants.PLAN_YEAR_ID, realmPlanYearId);
	    Map<String, LinkedHashMap<String, Integer>> listOfCoverageCodes = new LinkedHashMap<>();

    	LinkedHashMap<String, Integer> medicalMap =  new LinkedHashMap<>();
    	LinkedHashMap< String, Integer> dentalMap =  new LinkedHashMap<>();
    	LinkedHashMap< String, Integer> visionMap =  new LinkedHashMap<>();

		List<Object[]> results = DaoUtils.getResultList(q, BSSQueryConstants.GET_COVERAGE_CODES_BY_PLAN_TYPES);

        for (Object[] r : results) {
        	
        	if( r[0].equals(Constants.MEDICAL_CODE) ){
        			medicalMap.put((String) r[1], 0); 	
        	}
        	
        	else if ( r[0].equals(Constants.DENTAL_CODE) ) {
        			dentalMap.put((String) r[1], 0); 
        	}
               	
        	else if ( r[0].equals(Constants.VISION_CODE)) {
        			visionMap.put((String) r[1], 0); 
        	}
        }
        if(medicalMap.size()>0) {
        	 listOfCoverageCodes.put(Constants.MEDICAL, medicalMap);
        }
        listOfCoverageCodes.put(Constants.DENTAL, dentalMap);
        listOfCoverageCodes.put(Constants.VISION, visionMap);
        
        return listOfCoverageCodes;
	}

	
	@Override
	public Map<String, LinkedHashMap<String, String>> getCoverageCodesDescrByPlanTypes(List<String> planTypes, long realmPlanYearId) {
		Query q = em.createNamedQuery(BSSQueryConstants.GET_COVERAGE_CODES_BY_PLAN_TYPES);
	    q.setParameter(BSSQueryConstants.PLAN_TYPES, planTypes);
	    q.setParameter(BSSQueryConstants.PLAN_YEAR_ID, realmPlanYearId);
	    Map<String, LinkedHashMap<String, String>> listOfCoverageCodes = new LinkedHashMap<>();

    	LinkedHashMap<String, String> medicalMap =  new LinkedHashMap<>();
    	LinkedHashMap< String, String> dentalMap =  new LinkedHashMap<>();
    	LinkedHashMap< String, String> visionMap =  new LinkedHashMap<>();
    	
		List<Object[]> results = DaoUtils.getResultList(q, BSSQueryConstants.GET_COVERAGE_CODES_BY_PLAN_TYPES);      

        for (Object[] r : results) {
        	
        	if( r[0].equals(Constants.MEDICAL_CODE) ){
            		medicalMap.put((String) r[1], (String) r[2]);  	
        	}
        	
        	else if ( r[0].equals(Constants.DENTAL_CODE) ) {
        			dentalMap.put((String) r[1], (String) r[2]);	
        	}
               	
        	else if ( r[0].equals(Constants.VISION_CODE)) {
        			visionMap.put((String) r[1], (String) r[2]);
        		}
        }
        listOfCoverageCodes.put(Constants.MEDICAL, medicalMap);
        listOfCoverageCodes.put(Constants.DENTAL, dentalMap);
        listOfCoverageCodes.put(Constants.VISION, visionMap);
        
        return listOfCoverageCodes;
	}
	
	@Override
	public Map<String, List<CoverageLevel>> getCoverageCodesDescByPlanTypes(List<String> planTypes, long realmPlanYearId) {
		Query q = em.createNamedQuery(BSSQueryConstants.GET_COVERAGE_CODES_BY_PLAN_TYPES);
		q.setParameter(BSSQueryConstants.PLAN_TYPES, planTypes);
		q.setParameter(BSSQueryConstants.PLAN_YEAR_ID, realmPlanYearId);
		Map<String, List<CoverageLevel>> coverageCodesMap = new HashMap<>();
		List<CoverageLevel> medicalCoverageLevels = new ArrayList<>();
		List<CoverageLevel> dentalCoverageLevels = new ArrayList<>();
		List<CoverageLevel> visionCoverageLevels = new ArrayList<>();
		
		List<Object[]> results = DaoUtils.getResultList(q, BSSQueryConstants.GET_COVERAGE_CODES_BY_PLAN_TYPES);
		for (Object[] r : results) {
			CoverageLevel cl = new CoverageLevel();
			cl.setId((String) r[1]);
			cl.setName((String) r[2]);
			if (r[0].equals(Constants.MEDICAL_CODE)) {
				medicalCoverageLevels.add(cl);
			}
			else if (r[0].equals(Constants.DENTAL_CODE)) {
				dentalCoverageLevels.add(cl);
			}
			else if (r[0].equals(Constants.VISION_CODE)) {
				visionCoverageLevels.add(cl);
			}
		}
		coverageCodesMap.put(Constants.MEDICAL, medicalCoverageLevels);
		coverageCodesMap.put(Constants.DENTAL, dentalCoverageLevels);
		coverageCodesMap.put(Constants.VISION, visionCoverageLevels);
		return coverageCodesMap;
	}

	@Override
	public Map<String, Set<StateBenefitPlan>> getAdditionalBenefitsAllStatePlans(long planYearId, Set<String> regions,
			Company company) {
		List<Object[]> results;
		Query q;
		q = em.createNamedQuery("benefitPlansForAdditionalBenefits");
		q.setParameter(BSSQueryConstants.PLAN_YEAR_ID, planYearId);
		q.setParameter(BSSQueryConstants.PLAN_TYPES, additionalPlanTypeList);
		results = DaoUtils.getResultList(q, "benefitPlansForAdditionalBenefits");
		Map<String, Set<StateBenefitPlan>> map = new HashMap<>();
		for (Object[] r : results) {
			StateBenefitPlan plan = new StateBenefitPlan();
			String benefitPlan = (String) r[0];
			plan.setBenefitPlan(benefitPlan);
			plan.setDescription((String) r[1]);
			if( r[7] == null ) {
				plan.setDisplaySeq( null );
			} else {
				plan.setDisplaySeq( ((BigDecimal) r[7]).longValue() );
			}
			plan.setPlanType((String) r[2]);
			plan.setVendorId((String) r[3]);
			plan.setRealmYearId(((BigDecimal) r[5]).intValue());
			boolean isMandatory = false;
			BigDecimal mandatoryValue = (BigDecimal) r[6];
			if (mandatoryValue != null){
				isMandatory = mandatoryValue.compareTo(BigDecimal.ZERO) > 0;
			}	
			plan.setMandatory(isMandatory);
			String planType = plan.getPlanType();
			if (Constants.additionalPlanTypeList.contains(planType)) {
				logger.debug("ADDITIONAL PLAN TYPE : {}", planType);
				if (map.containsKey(planType)) {
					Set<StateBenefitPlan> plans = map.get(planType);
					plans.add(plan);
					map.put(planType, plans);
				} else {
					Set<StateBenefitPlan> plans = new HashSet<>();
					plans.add(plan);
					map.put(planType, plans);
				}
			}
		}
		return map;
	}
	
    @Override
    public Set<String> getCompanyLocationStates(String companyCode) {
        
        Query q = em.createNamedQuery("COMPANY_LOCATION_STATES");
        q.setParameter(BSSQueryConstants.COMPANY, companyCode);

        List<String> statesList = DaoUtils.getResultStringList(q, "COMPANY_LOCATION_STATES");
        Set<String> states = new TreeSet<>();
        
        states.addAll(statesList);
        return states;
    }
	
    @Override
	public Set<String> getFundingPlanStates(Company company, Set<String> currentRegions,
			RealmPlanYear realmPlanYear) {

		Query q = em.createNamedQuery("FUNDING_PLAN_STATES");
		q.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		q.setParameter(BSSQueryConstants.EFF_DATE, realmPlanYear.getPlanYearEnd());
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYear.getId());
		q.setParameter(BSSQueryConstants.REGIONS, currentRegions);

		List<String> statesList = DaoUtils.getResultStringList(q, "FUNDING_PLAN_STATES");
		Set<String> states = new TreeSet<>();

		states.addAll(statesList);
		return states;
	}
	
    @Override
    public List<String> getEmployeeHomeStates(String companyCode) {
        
        Query q = psEm.createNamedQuery("EMPLOYEE_HOME_STATES");
        q.setParameter(BSSQueryConstants.COMPANY, companyCode);
        
        return DaoUtils.getResultStringList(q, "EMPLOYEE_HOME_STATES");
    }
    
	@Override
    public Map<String, List<String>> getBenefitPlansStates(long realmPlanYearId) {
	    
        Query q = em.createNamedQuery("getStatesForPlan");
        q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
        Map<String, List<String>> benefitPlansStatesMap = Maps.newHashMap();
        List<Object[]> results = DaoUtils.getResultList(q, "getStatesForPlan");
        
        for (Object[] result : results) {
            
            String benefitPlan = (String) result[0];
            String state = (String) result[1];
            if (benefitPlansStatesMap.get(benefitPlan) == null){
                List<String> states = new ArrayList<>();
                states.add(state); 
                benefitPlansStatesMap.put(benefitPlan, states);
            }
            else {
                List<String> states = benefitPlansStatesMap.get(benefitPlan);
                states.add(state); 
            }
        }
        return benefitPlansStatesMap;
    }

	public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    public EntityManager getEntityManager() {
        return this.em;
    }
    
	@Override
	public Map<String, Map<String, Set<String>>> getAutoSelectPlansByRealmIdAndPlanTypes(long planYearId,
			Company company, Set<String> outOfRegionPlans) {
		List<Object[]> results;
		String sqlName = "getCrossRefPlansByRealmYearIdAndRegionAndPlanTypes";
		Query q = em.createNamedQuery(sqlName);
		q.setParameter(BSSQueryConstants.REALM_YEAR_ID, planYearId);
		results = DaoUtils.getResultList(q, sqlName);
			
		Map<String, Map<String, Set<String>>> planTypeMap = new HashMap<>();
		for (Object[] r : results) {
			String groupName = (String) r[0];
			String benefitPlan = (String) r[1];
			String planType = (String) r[3];
			if(CollectionUtils.isNotEmpty(outOfRegionPlans) && outOfRegionPlans.contains(benefitPlan)) {
				continue;
			}
			Map<String, Set<String>> autoSelectMap = planTypeMap.get(planType);
			if (autoSelectMap != null) {
				Set<String> planList = autoSelectMap.get(groupName);
				if (planList != null) {
					planList.add(benefitPlan);
				} else {
					Set<String> plList = new HashSet<>();
					plList.add(benefitPlan);
					autoSelectMap.put(groupName, plList);
				}
			} else {
				Map<String, Set<String>> autoSelectMapNew = new HashMap<>();
				Set<String> planListNew = new HashSet<>();
				planListNew.add(benefitPlan);
				autoSelectMapNew.put(groupName, planListNew);
				planTypeMap.put(planType, autoSelectMapNew);
			}

		}

		Map<String, Map<String, Set<String>>> typeMap = new HashMap<>();
		for (Object[] r : results) {
			String groupName = (String) r[0];
			String benefitPlan = (String) r[1];
			String planType = (String) r[3];
			Map<String, Set<String>> autoMap = planTypeMap.get(planType);
			Set<String> selectMap = autoMap.get(groupName);
			Set<String> finalMap = new HashSet<>();

			finalMap.addAll(selectMap);
			finalMap.remove(benefitPlan);
			Map<String, Set<String>> map = typeMap.get(planType);
			if (map != null) {
				map.put(benefitPlan, finalMap);
			} else {
				Map<String, Set<String>> mapNew = new HashMap<>();
				mapNew.put(benefitPlan, finalMap);
				typeMap.put(planType, mapNew);
			}
		}

		return typeMap;
	}

	 @Override
	public Map<String, String> getCoverageCodeMap() {
		Query q = em.createNamedQuery("getAllCoverageCodes");
		Map<String, String> map = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, "getAllCoverageCodes");

		for (Object[] r : results) {
			map.put((String) r[1], (String) r[0]);
		}
		return map;
	}

	 @Override
	public Map<String, Boolean> getSelectedBenefits(long realmPlanYearId, Set<String> regions) {
		Query q = em.createNamedQuery("getRealmPlanTypeRules");
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		q.setParameter(BSSQueryConstants.REGIONS, regions);
		Map<String, Boolean> map = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, "getRealmPlanTypeRules");
		for (Object[] r : results) {
			boolean manditory = false;
			if (1 == ((BigDecimal) r[1]).intValue()) {
				manditory = true;
			}
			if (null == map.get(PlanTypesEnum.getName((String) r[0]))) {
				map.put(PlanTypesEnum.getName((String) r[0]), manditory);
			}
		}
		return map;
	}
	
	@Override
	public RealmCloneProgram getRealmCloneProgram( long realmYearId ) {
		Query q = em.createNamedQuery( GET_REALM_CLONE_PGM );
		q.setParameter( BSSQueryConstants.REALM_YEAR_ID, realmYearId );
		List<Object[]> results = DaoUtils.getResultList( q, GET_REALM_CLONE_PGM );

		RealmCloneProgram cloneProgram = null;
		for( Object[] r : results ) {
			String oeQuarter = nullSafeString( r[0] );
			String cloneBenpgm = nullSafeString( r[1] );
			String cloneK1Benpgm = nullSafeString( r[2] );
			String cloneCompany = nullSafeString( r[3] );
			java.sql.Date effdt = nullSafeDate( r[4] );
			java.sql.Date enddt = nullSafeDate( r[5] );

			cloneProgram = new RealmCloneProgram();
			RealmCloneProgramId clonePgmId = new RealmCloneProgramId( oeQuarter, effdt );
			cloneProgram.setId( clonePgmId );
			cloneProgram.setCloneProgram( cloneBenpgm );
			cloneProgram.setCloneK1Program( cloneK1Benpgm );
			cloneProgram.setCloneCompany( cloneCompany );
			cloneProgram.setEnddt( enddt );
		}
		return cloneProgram;
	}

	private String nullSafeString( Object object ) {
		if( object == null )
			return "";
		else
			return object.toString();
	}

	private java.sql.Date nullSafeDate( Object object ) {
		if( object == null )
			return null;
		else
			return new java.sql.Date( ((java.sql.Timestamp) object).getTime() );
	}

	
	@Override
	public Map<String, String> getPlanVendors(Set<String> plans, long realmYearId) {
		Query query = em.createNamedQuery("getPlanVendors");

		query.setParameter("list", plans);
		query.setParameter(BSSQueryConstants.REALM_YEAR_ID, realmYearId);

		Map<String, String> planVendorMap = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(query, "getPlanVendors");
		for (Object[] r : results) {
			planVendorMap.put((String) r[0], (String) r[1]);
		}
		return planVendorMap;
	}

	@Override
	public boolean isCommuterBenefitOffered(long realmYearId) {
		Query q = em.createNamedQuery("getCommuterBenefit");
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmYearId);
		
		try {
			String commuter =  (String) DaoUtils.getSingleResult(q, "getCommuterBenefit");
			logger.info("COMMUTER BENEFIT IS OFFERED : {}", commuter);
			return true;
		} catch (NoResultException e) {
			logger.info("COMMUTER BENEFIT IS NOT OFFERED");
			return false;
		}
	}


	@Override
	public List<String> getLifeSupplementalPlanTypes(long realmYearId) {
		Query q = em.createNamedQuery("getSupplementalLifePlanTypes");
		q.setParameter(BSSQueryConstants.REALM_YEAR_ID, realmYearId);
		
		List<String> lifePlanTypes = new ArrayList<>();
		List<String> results = DaoUtils.getResultStringList(q, "getSupplementalLifePlanTypes");
		for (String planType : results) {
			lifePlanTypes.add(planType);
		}
		return lifePlanTypes;
	}
		
	@Override
	public List<FundingType> getRealmFundingTypes(long realmPlanYearId) {
		Query q = em.createNamedQuery("getRealmFundingTypes");
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		List<FundingType> fundingTypes = new ArrayList<>();
		List<Object[]> results = DaoUtils.getResultList(q, "getRealmFundingTypes");
		for (Object[] r : results) {
			FundingType ft = new FundingType();
			ft.setId((String) r[0]);
			ft.setDescription((String) r[1]);
			if(((BigDecimal) r[2]).longValue() == 1){
				ft.setDefaultFunding(true);
			}else{
				ft.setDefaultFunding(false);
			}
			fundingTypes.add(ft);
		}
		return fundingTypes;
	}

	@Override
	public Map<String, ProductQuarters> getAllProductQuarters() {
		Query q = em.createNamedQuery("getAllProductQuarters");
		Map<String, ProductQuarters> productQuarters = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, "getAllProductQuarters");
		for (Object[] r : results) {
			String product = (String) r[0];
			String quarter = (String) r[1];
			ProductQuarters pq = null;
			if (productQuarters.containsKey(product)) {
				pq = productQuarters.get(product);
				pq.getQuarters().add(quarter);
			} else {
				pq= new ProductQuarters();
				pq.setProduct(product);
				pq.setQuarters(new ArrayList<>());
				pq.getQuarters().add(quarter);
			}
			productQuarters.put(product, pq);
		}
		return productQuarters;
	}
	
	@Override
	public Map<String, Map<String, String>> getPortfilioDefaultPlans(long realmPlanYearId) {
		Query q = em.createNamedQuery("realmPortfolioDefaultPlans");
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		Map<String, Map<String, String>> planTypeportfolioMap = new HashMap<>();

		List<Object[]> results = DaoUtils.getResultList(q, "realmPortfolioDefaultPlans");
		for (Object[] r : results) {
			Map<String, String> portfolioMap = planTypeportfolioMap.get((String) r[2]);
			if (null == portfolioMap) {
				portfolioMap = new HashMap<>();
			}
			portfolioMap.put( r[0].toString(), (String) r[1]);
			planTypeportfolioMap.put((String) r[2], portfolioMap);
		}
		return planTypeportfolioMap;
	}
	
	@Override
	public Map<String, AdditionalBenefitPlan> getDisabilityOptionPlans(Long realmPlanYearId, String region,
			String benExchange, Set<String> sdiStates) {
		Query q = em.createNamedQuery("disabilityOptionPlansByRealm");
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		q.setParameter("region", region);
		Map<String, AdditionalBenefitPlan> disabilityBenefitOptionsMap = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, "disabilityOptionPlansByRealm");
		boolean isDisabledBundledOn = RulesAndConfigsUtils.isDisabledBundledOn(realmPlanYearId);
		
		for (Object[] r : results) {
			DisabilityBenefitOptionPlans optPlan = new DisabilityBenefitOptionPlans();
			optPlan.setId((String) r[2]);
			optPlan.setPlanType((String) r[3]);
			optPlan.setPlanDesc((String) r[4]);
			optPlan.setPlanShortDesc((String) r[5]);
			BigDecimal primaryPlan = (BigDecimal) r[7];
			BigDecimal empPaid = (BigDecimal) r[8];
			BigDecimal taxfree = (BigDecimal) r[10];
			optPlan.setOfferedGroupType((String) r[11]);
			if (BigDecimal.ONE.equals(empPaid)) {
				optPlan.setEmployeePaid(true);
			} else {
				optPlan.setEmployeePaid(false);
			}
			if (BigDecimal.ONE.equals(taxfree)) {
				optPlan.setTaxFree(true);
			} else {
				optPlan.setTaxFree(false);
			}
			if(optPlan.getPlanShortDesc().toUpperCase().contains("SDI")) {
				optPlan.setSdiPlan(true);
			}
			// need to clean up this code to do this check right.
			if (sdiStates.contains(region) && Constants.STD_CODE.equals(optPlan.getPlanType())
					&& isDisabledBundledOn && !optPlan.isEmployeePaid()) {
				if (BigDecimal.ONE.equals(primaryPlan)) {
					optPlan.setPrimaryPlan(false);
				} else {
					optPlan.setPrimaryPlan(true);
				}
			} else {
				if (BigDecimal.ONE.equals(primaryPlan)) {
					optPlan.setPrimaryPlan(true);
				} else {
					optPlan.setPrimaryPlan(false);
				}
			}
			String optionId = ((BigDecimal) r[0]).toString();
			AdditionalBenefitPlan opt = disabilityBenefitOptionsMap.get(optionId);
			if (null != opt) {
				opt.getOptionPlans().add(optPlan);
			} else {
				opt = new AdditionalBenefitPlan();
				List<DisabilityBenefitOptionPlans> optionPlans = new ArrayList<>();
				opt.setId(optionId);
				opt.setDescription((String) r[1]);
				BigDecimal isStandAlone = (BigDecimal) r[9];
				if (BigDecimal.ONE.equals(isStandAlone)) {
					opt.setStandAlone(true);
				} else {
					opt.setStandAlone(false);
				}
				opt.setRegion(region);
				optionPlans.add(optPlan);
				opt.setOptionPlans(optionPlans);
				opt.setOfferedGroupType((String) r[11]);
				if( r[12] == null ) {
					opt.setDisplaySeq( null );
				} else {
					opt.setDisplaySeq( ((BigDecimal) r[12]).longValue() );
				}
				disabilityBenefitOptionsMap.put(opt.getId(), opt);
			}
			if (optPlan.isEmployeePaid()) {
				opt.setEmployeePaidOption(true);
			}
			if (optPlan.isTaxFree()) {
				opt.setTaxFreeOption(true);
			}
		}
		return disabilityBenefitOptionsMap;
	}
	
	
	@Override
	public Map<String, Map<String, Map<String, Object>>> getStrategyFundingDetails(long strategyId) {
		Query q = em.createNamedQuery("STRATEGY_FUNDING_DETAILS");
		q.setParameter("strategyId", strategyId);
		Map<String, Map<String, Map<String, Object>>> groupRenewalFundingMap = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, "STRATEGY_FUNDING_DETAILS");
		for (Object[] r : results) {
			Map<String, Map<String, Object>> planTypeFunding = null;
			if (null != groupRenewalFundingMap.get((String) r[5])) {
				planTypeFunding = groupRenewalFundingMap.get((String) r[5]);
				Map<String, Object> coverageLevelFunding = null;
				if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains((String) r[1])) {
					coverageLevelFunding = planTypeFunding.get(BSSApplicationConstants.DENTAL_PLAN_TYPE);
				} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains((String) r[1])) {
					coverageLevelFunding = planTypeFunding.get(BSSApplicationConstants.VISION_PLAN_TYPE);
				} else {
					coverageLevelFunding = planTypeFunding.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
				}

				if (null != coverageLevelFunding) {
					if (null == coverageLevelFunding.get((String) r[3])) {
						coverageLevelFunding.put((String) r[3], (BigDecimal) r[4]);
					}
					if (BSSApplicationConstants.FLAT_MAX.equals((String) r[2])) {
						coverageLevelFunding.put((String) r[6] + BSSApplicationConstants.LIMIT, (BigDecimal) r[7]);
					}
				} else {
					coverageLevelFunding = new HashMap<>();
					coverageLevelFunding.put(BSSApplicationConstants.PRIMARY_PLAN_TYPE, ((String) r[1]));
					if (null != r[2]) {
						coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, ((String) r[2]));
					}
					if (BSSApplicationConstants.BFPCT.equals(r[0])) {
						coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_CVG, (String) r[3]);
						coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PCT, (BigDecimal) r[4]);
					} else {
						if (null == coverageLevelFunding.get((String) r[3])) {
							coverageLevelFunding.put((String) r[3], (BigDecimal) r[4]);
						}
					}
					if (null != r[2] && BSSApplicationConstants.FLAT_MAX.equals((String) r[2])) {
						coverageLevelFunding.put((String) r[6] + BSSApplicationConstants.LIMIT, (BigDecimal) r[7]);
					}
					coverageLevelFunding.put(BSSApplicationConstants.WAIVER_ALLOWANCE, (BigDecimal) r[8]);
					coverageLevelFunding.put(BSSApplicationConstants.BSUPP_EXCESS_OPTION, (BigDecimal) r[9]);
					coverageLevelFunding.put(BSSApplicationConstants.FUNDING_MODEL_ID, (BigDecimal) r[10]);
					coverageLevelFunding.put(BSSApplicationConstants.CUSTOMIZED, (BigDecimal) r[11]);
					coverageLevelFunding.put(BSSApplicationConstants.FUNDING_PKG_TYPE, (String) r[12]);
					coverageLevelFunding.put(BSSApplicationConstants.FUNDING_TYPE, ((String) r[0]));
					if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains((String) r[1])) {
						planTypeFunding.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, coverageLevelFunding);
					} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains((String) r[1])) {
						planTypeFunding.put(BSSApplicationConstants.VISION_PLAN_TYPE, coverageLevelFunding);
					} else {
						planTypeFunding.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, coverageLevelFunding);
					}
				}
			} else {
				planTypeFunding = new HashMap<>();
				Map<String, Object> coverageLevelFunding = new HashMap<>();
				coverageLevelFunding.put(BSSApplicationConstants.PRIMARY_PLAN_TYPE, ((String) r[1]));
				coverageLevelFunding.put(BSSApplicationConstants.FUNDING_TYPE, ((String) r[0]));
				coverageLevelFunding.put(BSSApplicationConstants.WAIVER_ALLOWANCE, (BigDecimal) r[8]);
				coverageLevelFunding.put(BSSApplicationConstants.BSUPP_EXCESS_OPTION, (BigDecimal) r[9]);
				coverageLevelFunding.put(BSSApplicationConstants.FUNDING_MODEL_ID, (BigDecimal) r[10]);
				coverageLevelFunding.put(BSSApplicationConstants.CUSTOMIZED, (BigDecimal) r[11]);
				coverageLevelFunding.put(BSSApplicationConstants.FUNDING_PKG_TYPE, (String) r[12]);
				if (BSSApplicationConstants.BFPCT.equals(r[0])) {
					coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, ((String) r[2]));
					coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_CVG, (String) r[3]);
					coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PCT, (BigDecimal) r[4]);
				} else {
					if (null != r[2]) {
						coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, ((String) r[2]));
					}
					if (null == coverageLevelFunding.get((String) r[3])) {
						coverageLevelFunding.put((String) r[3], (BigDecimal) r[4]);
					}
				}
				if (null != r[2] && BSSApplicationConstants.FLAT_MAX.equals((String) r[2])) {
					coverageLevelFunding.put((String) r[6] + BSSApplicationConstants.LIMIT, (BigDecimal) r[7]);
				}
				if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains((String) r[1])) {
					planTypeFunding.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, coverageLevelFunding);
				} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains((String) r[1])) {
					planTypeFunding.put(BSSApplicationConstants.VISION_PLAN_TYPE, coverageLevelFunding);
				} else {
					planTypeFunding.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, coverageLevelFunding);
				}
				groupRenewalFundingMap.put((String) r[5], planTypeFunding);
			}
		}
		return groupRenewalFundingMap;
	}
	
	@Override
	public Map<String, Map<String, Map<String, Object>>> getRenewalFundingDetailsBSS(String company,
			long realmPlanYearId) {
		Query q = em.createNamedQuery("getRenewalFundingDetailsBSS");
		q.setParameter("company", company);
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		Map<String, Map<String, Map<String, Object>>> groupRenewalFundingMap = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, "getRenewalFundingDetailsBSS");
		for (Object[] r : results) {
			Map<String, Map<String, Object>> planTypeFunding = null;
			if (null != groupRenewalFundingMap.get((String) r[5])) {
				planTypeFunding = groupRenewalFundingMap.get((String) r[5]);
				Map<String, Object> coverageLevelFunding = null;
				if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains((String) r[1])) {
					coverageLevelFunding = planTypeFunding.get(BSSApplicationConstants.DENTAL_PLAN_TYPE);
				} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains((String) r[1])) {
					coverageLevelFunding = planTypeFunding.get(BSSApplicationConstants.VISION_PLAN_TYPE);
				} else {
					coverageLevelFunding = planTypeFunding.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
				}

				if (null != coverageLevelFunding) {
					if (null == coverageLevelFunding.get((String) r[3])) {
						coverageLevelFunding.put((String) r[3], (BigDecimal) r[4]);
					}
					if (BSSApplicationConstants.FLAT_MAX.equals((String) r[2])) {
						coverageLevelFunding.put((String) r[6] + BSSApplicationConstants.LIMIT, (BigDecimal) r[7]);
					}
				} else {
					coverageLevelFunding = new HashMap<>();
					coverageLevelFunding.put(BSSApplicationConstants.PRIMARY_PLAN_TYPE, ((String) r[1]));
					if (BSSApplicationConstants.BFPCT.equals(r[0])) {
						coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, ((String) r[2]));
						coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_CVG, (String) r[3]);
						coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PCT, (BigDecimal) r[4]);
					} else {
						if (null != r[2]) {
							coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, ((String) r[2]));
						}
						if (null == coverageLevelFunding.get((String) r[3])) {
							coverageLevelFunding.put((String) r[3], (BigDecimal) r[4]);
						}
					}
					if (null != r[2] && BSSApplicationConstants.FLAT_MAX.equals((String) r[2])) {
						coverageLevelFunding.put((String) r[6] + BSSApplicationConstants.LIMIT, (BigDecimal) r[7]);
					}
					coverageLevelFunding.put(BSSApplicationConstants.WAIVER_ALLOWANCE, (BigDecimal) r[8]);
					coverageLevelFunding.put(BSSApplicationConstants.BSUPP_EXCESS_OPTION, (BigDecimal) r[9]);
					coverageLevelFunding.put(BSSApplicationConstants.FUNDING_TYPE, ((String) r[0]));
					if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains((String) r[1])) {
						planTypeFunding.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, coverageLevelFunding);
					} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains((String) r[1])) {
						planTypeFunding.put(BSSApplicationConstants.VISION_PLAN_TYPE, coverageLevelFunding);
					} else {
						planTypeFunding.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, coverageLevelFunding);
					}
				}
			} else {
				planTypeFunding = new HashMap<>();
				Map<String, Object> coverageLevelFunding = new HashMap<>();
				coverageLevelFunding.put(BSSApplicationConstants.PRIMARY_PLAN_TYPE, ((String) r[1]));
				coverageLevelFunding.put(BSSApplicationConstants.FUNDING_TYPE, ((String) r[0]));
				coverageLevelFunding.put(BSSApplicationConstants.WAIVER_ALLOWANCE, (BigDecimal) r[8]);
				coverageLevelFunding.put(BSSApplicationConstants.BSUPP_EXCESS_OPTION, (BigDecimal) r[9]);
				if (BSSApplicationConstants.BFPCT.equals(r[0])) {
					coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, ((String) r[2]));
					coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_CVG, (String) r[3]);
					coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PCT, (BigDecimal) r[4]);
				} else {
					if (null != r[2]) {
						coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, ((String) r[2]));
					}
					if (null == coverageLevelFunding.get((String) r[3])) {
						coverageLevelFunding.put((String) r[3], (BigDecimal) r[4]);
					}
				}
				if (null != r[2] && BSSApplicationConstants.FLAT_MAX.equals((String) r[2])) {
					coverageLevelFunding.put((String) r[6] + BSSApplicationConstants.LIMIT, (BigDecimal) r[7]);
				}
				if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains((String) r[1])) {
					planTypeFunding.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, coverageLevelFunding);
				} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains((String) r[1])) {
					planTypeFunding.put(BSSApplicationConstants.VISION_PLAN_TYPE, coverageLevelFunding);
				} else {
					planTypeFunding.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, coverageLevelFunding);
				}
				groupRenewalFundingMap.put((String) r[5], planTypeFunding);
			}
		}
		return groupRenewalFundingMap;
	}
	
	@Override
	public List<String> getADBenefitPlans(Company company) {
		Query q = em.createNamedQuery("ADDITIONAL_PLANS_BY_REGION");
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, company.getRealmPlanYearId());
		q.setParameter("region", company.getHeadQuatersState());
		try {
			return DaoUtils.getResultStringList(q, "ADDITIONAL_PLANS_BY_REGION");
		} catch (NoResultException e) {
			logger.error("No additional plans found");
		}
		return null;
	}
	
	@Override
	public boolean validateRateTableId(String rateTableId, String benefitProgram) {
		boolean isValidRateTableId = false;
		
		if( StringUtils.isEmpty( rateTableId )) {
			isValidRateTableId = false;
		} else {
			Query q = em.createNamedQuery("RATE_TABLE_ID_VALIDATION");
			q.setParameter("rateTableId", rateTableId);
			q.setParameter(BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram);
			try {
				List<String> rateTableIds = DaoUtils.getResultStringList(q, "RATE_TABLE_ID_VALIDATION");
				
				if (null != rateTableIds && !rateTableIds.isEmpty()) {
					isValidRateTableId = false;
				} else {
					isValidRateTableId = true;
				}

			} catch (NoResultException e) {
				isValidRateTableId = true;
			}
		}
		return isValidRateTableId;
	}
	
	@Override
	public Map<String, Map<String, List<String>>> getAutoSelectedPlansByRegion(Set<String> selectedPlans,
			long realmPlanYearId) {
		Query q = em.createNamedQuery("AUTO_SELECTED_BY_REGION");
		q.setParameter("selectedPlans", selectedPlans);
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		List<Object[]> results = DaoUtils.getResultList(q, "AUTO_SELECTED_BY_REGION");
		Map<String, Map<String, List<String>>> planTypeAutoSelectPlansByRegion = new HashMap<>();
		for (Object[] r : results) {
			String benefitPlan = (String) r[0];
			String region = (String) r[1];
			String planType = (String) r[2];
			if (null != planTypeAutoSelectPlansByRegion.get(planType)) {
				Map<String, List<String>> autoSelectPlansByRegion = planTypeAutoSelectPlansByRegion.get(planType);
				if (null != autoSelectPlansByRegion.get(region)) {
					List<String> benefitPlans = autoSelectPlansByRegion.get(region);
					benefitPlans.add(benefitPlan);
				} else {
					List<String> benefitPlans = new ArrayList<>();
					benefitPlans.add(benefitPlan);
					autoSelectPlansByRegion.put(region, benefitPlans);
				}
			} else {
				Map<String, List<String>> autoSelectPlansByRegion = new HashMap<>();
				List<String> benefitPlans = new ArrayList<>();
				benefitPlans.add(benefitPlan);
				autoSelectPlansByRegion.put(region, benefitPlans);
				planTypeAutoSelectPlansByRegion.put(planType, autoSelectPlansByRegion);
			}
		}
		return planTypeAutoSelectPlansByRegion;
	}

	@Override
	public Set<String> getAutoSelectedMedicalPlansForPassport(Company company, Long strategyId, Set<String> outOfRegionPlans) {
		if(CollectionUtils.isEmpty(outOfRegionPlans)) {
			// Need to pass some random string as a benPlan since oracle IN clause throws exception for empty list.
			outOfRegionPlans = new HashSet<>(1);
			outOfRegionPlans.add(BSSQueryConstants.PLAN_TO_EXCLUDE);
		}
		Query q = em.createNamedQuery("AUTO_SELECT_PLANS_PASSPORT");
		q.setParameter("STRATEGY_ID", strategyId);
		q.setParameter("REALM_YEAR_ID", company.getRealmPlanYearId());
		q.setParameter("OUT_OF_REGION_PLANS", outOfRegionPlans);
		if (company.isTexasSitus()) {
			q.setParameter(BSSQueryConstants.SITUS, BSSApplicationConstants.SITUS_TX);
		} else {
			q.setParameter(BSSQueryConstants.SITUS, BSSApplicationConstants.SITUS_FL);
		}
		Set<String> benefitPlans = new HashSet<>();
		List<String> autoSelectPlans = DaoUtils.getResultStringList(q, "AUTO_SELECT_PLANS_PASSPORT");
		benefitPlans.addAll(autoSelectPlans);
		return benefitPlans;
	}
	
	@Override
	public Map<String, List<String>> getRegionForSelectedPlans(Set<String> selectedPlans, long realmPlanYearId) {
		Query q = em.createNamedQuery("SELECTED_MEDICAL_PLANS_BY_REGION");
		
		// create a temporary version of the plan set and add a dummy value in case of empty sets
		Set<String> tempSelectPlans = new HashSet<>( selectedPlans );
		tempSelectPlans.add( " " );
		q.setParameter("selectedPlans", tempSelectPlans);

		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		Map<String, List<String>> autoSelectPlansByRegion = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, "SELECTED_MEDICAL_PLANS_BY_REGION");
		List<String> allRegionPlans = new ArrayList<>();
		if (null != results && !results.isEmpty()) {
			for (String sBP : selectedPlans) {
				boolean planExists = false;
				for (Object[] r : results) {
					String benefitPlan = (String) r[0];
					if (benefitPlan.equals(sBP)) {
						planExists = true;
					}
				}
				if (!planExists) {
					allRegionPlans.add(sBP);
				}
			}
			if (null != allRegionPlans && !allRegionPlans.isEmpty()) {
				autoSelectPlansByRegion.put("all", allRegionPlans);
			}
			for (Object[] r : results) {

				String benefitPlan = (String) r[0];
				String region = (String) r[1];

				if (null != autoSelectPlansByRegion.get(region)) {
					List<String> benefitPlans = autoSelectPlansByRegion.get(region);
					benefitPlans.add(benefitPlan);
				} else {
					List<String> benefitPlans = new ArrayList<>();
					benefitPlans.add(benefitPlan);
					autoSelectPlansByRegion.put(region, benefitPlans);
				}
			}
		} else {

			allRegionPlans.addAll(selectedPlans);
			autoSelectPlansByRegion.put("all", allRegionPlans);
		}
		return autoSelectPlansByRegion;
	}
	
	@Override
	public List<String> getSelectedBenefitsExceptVoluntary(long realmPlanYearId, Set<String> regions) {
		Query q = em.createNamedQuery("getRealmPlanTypeRulesExceptVoluntary");
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		q.setParameter(BSSQueryConstants.REGIONS, regions);
		List<String> benefits = new ArrayList<>();
		List<String> results = DaoUtils.getResultStringList(q, "getRealmPlanTypeRulesExceptVoluntary");
		for (String r : results) {
			String planTypeDesc = PlanTypesEnum.getName(r);
			if(!benefits.contains(planTypeDesc)) {
				benefits.add(planTypeDesc);
			}
		}
		return benefits;
	}

	@Override
	public Set<String> getBSOutOfRegionPlans(Company company) {
		Query q = psEm.createNamedQuery("BSCA_OUT_OF_REGION_PLANS");
		q.setParameter("zipCode", company.getZipCode());
		q.setParameter("effdt", company.getRealmPlanYear().getPlanYearStart());
		List<String> results = DaoUtils.getResultStringList(q, "BSCA_OUT_OF_REGION_PLANS");
		Set<String> benPlans = null;
		if(CollectionUtils.isNotEmpty(results)) {
			benPlans = new HashSet<>(results);
		}
		return benPlans;
	}
	
	@Override
	public Map<String, AdditionalBenefitPlan> getDisabilityOptionsForRealmPlanYears(long realmPlanYearId,
			long prevRealmPlanYearId, String region, boolean includeK1) {
		Query q = em.createNamedQuery("DISABILITY_OPTIONS_FOR_REALM_PLAN_YEARS");
		q.setParameter(BSSQueryConstants.PREVIOUS_REALM_PLAN_YEAR_ID, prevRealmPlanYearId);
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		q.setParameter(BSSQueryConstants.REGIONS, region);
		q.setParameter("groupTypeExclude", (includeK1 ? " " : BSSApplicationConstants.K1_GROUP_TYPE));
		Map<String, AdditionalBenefitPlan> disabilityBenefitOptionsMap = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, "DISABILITY_OPTIONS_FOR_REALM_PLAN_YEARS");
		for (Object[] r : results) {
			DisabilityBenefitOptionPlans optPlan = new DisabilityBenefitOptionPlans();
			String optionName = (String) r[0];
			optPlan.setId((String) r[1]);
			optPlan.setPlanType((String) r[2]);
			optPlan.setPlanDesc((String) r[3]);
			optPlan.setSdiPlan(BigDecimal.ONE.equals((BigDecimal) r[4]));
			optPlan.setOfferedGroupType((String) r[6]);
			if (disabilityBenefitOptionsMap.containsKey(optionName)) {
				AdditionalBenefitPlan opt = disabilityBenefitOptionsMap.get(optionName);
				opt.getOptionPlans().add(optPlan);
			} else {
				AdditionalBenefitPlan opt = new AdditionalBenefitPlan();
				List<DisabilityBenefitOptionPlans> optionPlans = new ArrayList<>();
				opt.setDescription((String) r[0]);
				if( r[7] == null ) {
					opt.setDisplaySeq( null );
				} else {
					opt.setDisplaySeq( ((BigDecimal) r[7]).longValue() );
				}
				opt.setRegion(region);
				optionPlans.add(optPlan);
				opt.setOptionPlans(optionPlans);
				disabilityBenefitOptionsMap.put(optionName, opt);
			}
		}
		return disabilityBenefitOptionsMap;
	}

	@Override
	public Set<String> getCarrierOutOfRegionPlans(Company company, Set<String> medPlanGroups) {
		Query q = psEm.createNamedQuery("CARRIER_OUT_OF_REGION_PLANS");
		q.setParameter(BSSQueryConstants.ZIP_CODE, company.getZipCode());
		q.setParameter(BSSQueryConstants.EFF_DATE, company.getRealmPlanYear().getPlanYearStart());
		q.setParameter(BSSQueryConstants.MED_PLAN_GRPS, medPlanGroups);
		q.setParameter(BSSQueryConstants.OE_QUARTER, company.getQuater());
		List<String> results = DaoUtils.getResultStringList(q, "CARRIER_OUT_OF_REGION_PLANS");
		Set<String> outOfRegionPlans = null;
		if (CollectionUtils.isNotEmpty(results)) {
			outOfRegionPlans = new HashSet<>(results);
		}
		return outOfRegionPlans;
	}

	@Override
	public List<CarrierData> getCarriersBy(String zipCode, long realmPlanYearId, String state, String reportCode, String planType) {
		if(Objects.nonNull(reportCode) && PlanOfferingReportConstants.REPORT_CODE_EXCHANGE.equalsIgnoreCase(reportCode)) {
			final String queryName = "MED_CARRIERS_BY_EXCHANGE";
			Query q = em.createNamedQuery(queryName);
			q.setParameter(BSSQueryConstants.PLAN_TYPE, planType);
			q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
			List<Object[]> results = DaoUtils.getResultList(q, queryName);
			return results.stream().map(result -> new CarrierData(((BigDecimal) result[1]).longValue(), result[2].toString())).collect(Collectors.toList());
		} else {
			final String queryName = "MED_CARRIERS_BY_HQ";
			Query q = em.createNamedQuery(queryName);
			q.setParameter(BSSQueryConstants.ZIP_CODE, zipCode);
			q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
			q.setParameter(BSSQueryConstants.STATE, state);
			List<Object[]> results = DaoUtils.getResultList(q, queryName);
			return results.stream().map(result -> new CarrierData(((BigDecimal) result[1]).longValue(), result[2].toString())).collect(Collectors.toList());
		}
		
	}

	/**
	 * @param psEm the psEm to set
	 */
	public void setPsEm(EntityManager psEm) {
		this.psEm = psEm;
	}
}