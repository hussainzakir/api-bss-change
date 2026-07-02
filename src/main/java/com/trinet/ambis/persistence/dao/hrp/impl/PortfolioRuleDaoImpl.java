package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.dto.CompanyStrategyPortfolioDetailsDto;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.DaoUtils;

public class PortfolioRuleDaoImpl implements PortfolioRuleDao {
	
	@PersistenceContext(unitName = "bis-hrp")
    EntityManager em;	
    
	@Override
	public Map<String, Set<PlanCarrier>> getPortfoliosByHqRegion(long realmYearId, String state, String zipCode,
			String exclusiveMedPlan, String effdt, boolean isPickAndChoose) {
	Query q = em.createNamedQuery("PORTFOLIOS_BY_HQ_STATE");
		q.setParameter("realm_year_id", realmYearId);
		q.setParameter(BSSQueryConstants.STATE, state);
		q.setParameter(BSSQueryConstants.PICK_CHOOSE_FLAG, isPickAndChoose ? "1" : "0");
		q.setParameter("effdt", effdt);
		q.setParameter("zip_code", zipCode);
		q.setParameter("exclMedPlan", exclusiveMedPlan);

		List<Object[]> results = DaoUtils.getResultList(q, "PORTFOLIOS_BY_HQ_STATE");
		Map<String, Set<PlanCarrier>> portfolioMap = new HashMap<>();

		Map<Integer, List<Integer>> regionalCarriers = getRegionalCarriers(results);
		for (Object[] r : results) {
			String planType = (String) r[2];
			int id = ((BigDecimal) r[0]).intValue();
			Collection<Set<PlanCarrier>> carrierSets = portfolioMap.values();
			PlanCarrier planCarrier = carrierSets.stream().flatMap(Set::stream)
					.filter(pc -> pc.getId() == id && planType.equals(pc.getPlanType())).findFirst().orElse(null);
            if (planCarrier != null) {
                if (r[4] != null) {
					List<String> parentIds = planCarrier.getParentId() != null ? new ArrayList<>(planCarrier.getParentId())
							: new ArrayList<>();
					parentIds.add(((BigDecimal) r[4]).toString());
					planCarrier.setParentId(parentIds);
                }
            } else {
            	PlanCarrier pc = new PlanCarrier();
    			pc.setId(id);
    			pc.setName((String) r[1]);
    			pc.setPlanType(planType);
    			String rule = (String) r[3];
    			parseRuleString(rule, pc);
    			List<String> parentIds = r[4] != null ? Arrays.asList(((BigDecimal) r[4]).toString()) : null;
    			pc.setParentId(parentIds);
				pc.setPrimaryCarrier(((BigDecimal) r[5]).compareTo(BigDecimal.ZERO) > 0);
    			if (Constants.medicalPlanTypeList.contains(planType)) {
    				if (regionalCarriers.containsKey(pc.getId())) {
    					pc.setRegionalCarriers(regionalCarriers.get(pc.getId()));
    				}
    				portfolioMap.computeIfAbsent(Constants.MEDICAL, k -> new TreeSet<>()).add(pc);
    			} else if (Constants.dentalPlanTypeList.contains(planType)) {
    				portfolioMap.computeIfAbsent(Constants.DENTAL, k -> new TreeSet<>()).add(pc);
    			} else if (Constants.visionPlanTypeList.contains(planType)) {
    				portfolioMap.computeIfAbsent(Constants.VISION, k -> new TreeSet<>()).add(pc);
    			}
            }
		}

		Set<PlanCarrier> medicalCarrierList = portfolioMap.get(Constants.MEDICAL);
		if (CollectionUtils.isNotEmpty(medicalCarrierList)) {
			updateRestrictedCarrier(medicalCarrierList);
			portfolioMap.put(Constants.MEDICAL, medicalCarrierList);
		}
		return portfolioMap;
	}

	
	@Override
	public Map<String, Set<PlanCarrier>> getStrategyPortfolios(long strategyId, long planYearId,
			Map<String, Map<String, String>> defaultPlanMap, String region, boolean isPickChoose ) {
		Query q = em.createNamedQuery("getStrategyPortfolios");
		q.setParameter("strategy_id", strategyId);
		q.setParameter("realm_year_id", planYearId);
		q.setParameter("region", region);
		q.setParameter( "pickChooseFlag", isPickChoose ? "1" : "0" );

		List<Object[]> results = DaoUtils.getResultList(q, "getStrategyPortfolios");
		Map<String, Set<PlanCarrier>> portfolioMap = new HashMap<>();
		Set<PlanCarrier> medicalCarrierList = null;
		Set<PlanCarrier> dentalCarrierList = null;
		Set<PlanCarrier> visionCarrierList = null;
		for (Object[] r : results) {
			PlanCarrier pc = new PlanCarrier();
			int id = ((BigDecimal) r[0]).intValue();
			pc.setId(id);
			pc.setName((String) r[1]);
			String planType = (String) r[2];
			String rule = (String) r[3];
			parseRuleString(rule, pc);
			if (Constants.medicalPlanTypeList.contains(planType)) {
				if (portfolioMap.get(Constants.MEDICAL) != null) {
					medicalCarrierList = portfolioMap.get(Constants.MEDICAL);
					medicalCarrierList.add(pc);
					portfolioMap.put(Constants.MEDICAL, medicalCarrierList);
				} else {
					medicalCarrierList = new TreeSet<>();
					medicalCarrierList.add(pc);
					portfolioMap.put(Constants.MEDICAL, medicalCarrierList);
				}
			} else if (Constants.dentalPlanTypeList.contains(planType)) {
				if (portfolioMap.get(Constants.DENTAL) != null) {
					dentalCarrierList = portfolioMap.get(Constants.DENTAL);
					dentalCarrierList.add(pc);
					portfolioMap.put(Constants.DENTAL, dentalCarrierList);
				} else {
					dentalCarrierList = new TreeSet<>();
					dentalCarrierList.add(pc);
					portfolioMap.put(Constants.DENTAL, dentalCarrierList);
				}
			} else if (Constants.visionPlanTypeList.contains(planType)) {
				if (portfolioMap.get(Constants.VISION) != null) {
					visionCarrierList = portfolioMap.get(Constants.VISION);
					visionCarrierList.add(pc);
					portfolioMap.put(Constants.VISION, visionCarrierList);
				} else {
					visionCarrierList = new TreeSet<>();
					visionCarrierList.add(pc);
					portfolioMap.put(Constants.VISION, visionCarrierList);
				}
			}
		}
		return portfolioMap;
	}
    
	@Override
	public Map<Long, CompanyStrategyPortfolioDetailsDto> getCompanyStrategyPortfolioIds(List<Long> companyIds) {
		Query q = em.createNamedQuery("getCompanyStrategyPortfolioIds");
		q.setParameter("companyIds", companyIds);
		List<Object[]> results = DaoUtils.getResultList(q, "getCompanyStrategyPortfolioIds");
		return results.stream().map(result -> {
			String allStategyIdsResult = (String) result[2];
			Set<Long> allStategyIds = StringUtils.isNotEmpty(allStategyIdsResult)
					? Stream.of((allStategyIdsResult).split(",")).map(Long::valueOf).collect(Collectors.toSet())
					: Collections.emptySet();
			String defaultStrategyPortfolioIdsResult = (String) result[3];
			List<Long> defaultStrategyPortfolioIds = StringUtils.isNotEmpty(defaultStrategyPortfolioIdsResult)
					? Stream.of((defaultStrategyPortfolioIdsResult).split(",")).map(Long::valueOf)
							.collect(Collectors.toList())
					: Collections.emptyList();
			return CompanyStrategyPortfolioDetailsDto.builder().companyId(((BigDecimal) result[0]).longValue())
					.defaultStrategyId(Long.parseLong((String) result[1])).allStrategyIds(allStategyIds)
					.defaultStrategyPortfolioIds(defaultStrategyPortfolioIds)
					.realmPlanYearId(Long.parseLong((String) result[4])).realmId(((BigDecimal) result[5]).longValue())
					.build();
		}).collect(Collectors.toMap(CompanyStrategyPortfolioDetailsDto::getCompanyId, Function.identity()));
	}

	@Override
	public Map<Long, Boolean> getMedicalPortfoliosBy(long strategyId, long planYearId, String region) {
		String query = "findStrategyPortfoliosForMedical";
		Query q = em.createNamedQuery(query);
		q.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, planYearId);
		q.setParameter(BSSQueryConstants.STATE, region);

		List<Object[]> results = DaoUtils.getResultList(q, query);
		Map<Long, Boolean> portfolioIds = new HashMap<>();
		for (Object[] r : results) {
			long id = ((BigDecimal) r[0]).intValue();
			BigDecimal primaryValue = (BigDecimal) r[1];
			boolean isPrimary = primaryValue.compareTo(BigDecimal.ZERO) > 0 ? Boolean.TRUE : Boolean.FALSE;
			portfolioIds.put(id, isPrimary);
		}
		return portfolioIds;
	}
	
	private Map<Integer, List<Integer>> getRegionalCarriers(
			List<Object[]> results) {
		Map<Integer, List<Integer>> regionalCarriers = new HashMap<>();
		for (Object[] r : results) {
			int id = ((BigDecimal) r[0]).intValue();
			if (r[4] != null) {
				int parentId = ((BigDecimal) r[4]).intValue();
				if (regionalCarriers.get(parentId) == null) {
					List<Integer> rc = new ArrayList<>();
					rc.add(id);
					regionalCarriers.put(parentId, rc);					
				} else {
					List<Integer> rc = regionalCarriers.get(parentId);
					rc.add(id);					
				}
			}
		}
		return regionalCarriers;
	}

	private void parseRuleString(String ruleString, PlanCarrier pc) {
		if (ruleString != null) {
			int mandotoryString = ruleString.lastIndexOf("Mandatory");
			int restrictedString = ruleString.lastIndexOf("Restricted");
			if (mandotoryString == 0) {
				String mandatoryFlag = ruleString.substring(mandotoryString + 10, mandotoryString + 11);
				pc.setMandatory(mandatoryFlag.equals(Constants.TRUE));
			}else{
				pc.setMandatory(false);
			}
			if (restrictedString == 0) {
				String restrictedFlag = ruleString.substring(restrictedString + 11, restrictedString + 12);
				pc.setRestricted(restrictedFlag.equals(Constants.TRUE));
			}else{
				pc.setRestricted(false);
			}
		} else {
			pc.setMandatory(false);
			pc.setRestricted(false);
		}
	}

	private void updateRestrictedCarrier(Set<PlanCarrier> medicalCarrierList) {
		List<PlanCarrier> restrictedMedicalPlanCarrier = new ArrayList<>();

		for (PlanCarrier pc : medicalCarrierList) {
			if (pc.isRestricted()) {
				restrictedMedicalPlanCarrier.add(pc);
			}
		}
		if (restrictedMedicalPlanCarrier.size() == 1) {
			for (PlanCarrier pc : medicalCarrierList) {
				if (pc.isRestricted()) {
					pc.setMandatory(true);
					pc.setRestricted(false);

				}
			}
		}
	}

	public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    public EntityManager getEntityManager() {
        return this.em;
    }
    
}
