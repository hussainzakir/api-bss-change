/**
 * 
 */
package com.trinet.ambis.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.EmployeeSelectionDao;
import com.trinet.ambis.persistence.dao.hrp.HeadCountDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioHeadCountDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupDataDao;
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.HeadCountService;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.MappedHeadCount;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.util.RulesAndConfigsUtils;

@Service
public class HeadCountServiceImpl implements HeadCountService {

	@Autowired
	private EmployeeSelectionDao employeeSelectionDao;
	@Autowired
	private RenewalDataDao renewalDataDao;
	@Autowired
	private PortfolioHeadCountDataDao portfolioHeadCountDataDao;
	@Autowired
	private HeadCountDao headCountDao;
	@Autowired
	private StrategyGroupDataDao strategyGroupDataDao;
	@Autowired
	private StrategyDataDao strategyDataDao;

	@Override
	public Map<String, Integer> getEmployeeHeadcountByBenefitGroup(Company company, Long realmPlanYearId,
			Date effDate) {

		boolean isVendorMappingOn = RulesAndConfigsUtils.isVendorMappingOn(company.getRealmPlanYear().getId());
		Map<String, Integer> groupHeadCountMap = null;
		if (isVendorMappingOn) {
			groupHeadCountMap = headCountDao.getEmployeeCountByBenefitGroup(company.getCode(), realmPlanYearId);
		} else {
			groupHeadCountMap = employeeSelectionDao.getEmployeesByBG(company, effDate);
		}
		return groupHeadCountMap;
	}

	@Override
	public Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> getHeadCountByGroupAndPlan(Company company,
			Long realmPlanYearId, Date effDate, boolean getMappedPlans) {

		boolean isVendorMappingOn = RulesAndConfigsUtils.isVendorMappingOn(company.getRealmPlanYear().getId());
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupCovrgHeadCountMap = null;
		if (isVendorMappingOn) {
			groupCovrgHeadCountMap = headCountDao.getPlanCoverageLevelHeadCountByGroup(company.getCode(),
					realmPlanYearId, getMappedPlans);
		} else {
			groupCovrgHeadCountMap = renewalDataDao.getHeadCountByGroupAndPlan(company.getCode(), effDate);
		}
		return groupCovrgHeadCountMap;
	}

	@Override
	public Map<String, Map<String, Map<String, Long>>> getMirrorPlanHeadCounts(Company company, long realmPlanYearId) {

		return portfolioHeadCountDataDao.getMirrorPlanHeadCounts(company.getCode(), realmPlanYearId);
	}
	
	@Override
	public Map<String, Long> getWaiverHeadCountByBenefitProgram(Company company, long strategyId, long realmPlanYearId, boolean history) {
		
		boolean isVendorMappingOn = RulesAndConfigsUtils.isVendorMappingOn(company.getRealmPlanYear().getId());
		Map<String, Long> waiverHeadcount = null;
		if (isVendorMappingOn) {
			waiverHeadcount = headCountDao.getWaiverHeadCountByBenefitProgram( company, strategyId, history);
		} else {
			waiverHeadcount = strategyGroupDataDao.getStrategyWaiverHeadCount(company, strategyId, history);
		}
		return waiverHeadcount;
	}
	
	@Override
	public Map<String, Integer> getPrimaryHeadCountByBenefitProgram(Company company, long strategyId, boolean history) {
		
		boolean isVendorMappingOn = RulesAndConfigsUtils.isVendorMappingOn(company.getRealmPlanYear().getId());
		Map<String, Integer> primaryHeadcount = null;
		if (isVendorMappingOn) {
			primaryHeadcount = headCountDao.geEnrolledHeadCountByBenefitProgram(company, strategyId, history);
		} else {
			primaryHeadcount = renewalDataDao.getPrimaryEnrolledEECount(company, history, strategyId);
		}
		return primaryHeadcount;
	}
	
	@Override
	public Map<Long, Long> getStrategyBenefitGroupHeadCount(Company company, long strategyId) {
		return strategyDataDao.getStrategyBenefitGroupHeadCountsFromCensus(strategyId);
	}
	
	@Override
	public Map<String, ActiveEligibleEECount> getEligibleEmployeeCount(Company company, long strategyId, RealmPlanYear realmPlanYear, boolean history) {
		
		boolean isVendorMappingOn = RulesAndConfigsUtils.isVendorMappingOn(company.getRealmPlanYear().getId());
		Map<String, ActiveEligibleEECount> eligibleHeadCount = null;
		if (isVendorMappingOn) {
			eligibleHeadCount = headCountDao.getEligibleEmployeeCount(company, strategyId, history);
		} else {
			eligibleHeadCount = renewalDataDao.getActiveEligibleEECount(company, history, strategyId, realmPlanYear);	
		}
		return eligibleHeadCount;
	}
	
	

	@Override
	public List<MappedHeadCount> getMappedHeadCounts(String companyCode, long realmPlanYearId) {

		return headCountDao.getMappedHeadCount(companyCode, realmPlanYearId);
	}

}