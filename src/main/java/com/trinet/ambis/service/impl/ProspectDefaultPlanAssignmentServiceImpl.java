/**
 * 
 */
package com.trinet.ambis.service.impl;

import static com.trinet.ambis.common.BSSApplicationConstants.EMPTY_PLAN_TYPES;
import static com.trinet.ambis.common.BSSApplicationConstants.PRIMARY_PLAN_TYPES;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.DefaultPlanDataDao;
import com.trinet.ambis.persistence.dao.hrp.EePlanAssignmentDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.ProspectDefaultPlanAssignmentService;


/**
 * 
 */
@Service
public class ProspectDefaultPlanAssignmentServiceImpl implements ProspectDefaultPlanAssignmentService {

	@Autowired
	DefaultPlanDataDao defaultPlanDataDao;
	@Autowired
	EePlanAssignmentDao eePlanAssignmentDao;

	@Override
	public void insertStrategyDefaultAssignments(Company company, Set<Long> primaryPortfolioIds,
			Set<Long> altPortfolioIds, long strategyId) {
		eePlanAssignmentDao.deleteEePlanAssignmentBy(Set.of(strategyId));
		defaultPlanDataDao.insertStrategyDefaultAssignmentsBy(company, primaryPortfolioIds, altPortfolioIds,
				strategyId);
	}

	@Override
	@Transactional
	public void assignDefaultPlanBy(Set<String> emplIds, long strategyId,
			Map<Long, Boolean> existingMedStrategyPortfolioMap, Set<String> benTypes) {
		if (CollectionUtils.isNotEmpty(emplIds)) {
			eePlanAssignmentDao.deleteEePlanAssignmentBy(emplIds, strategyId, benTypes);
			List<Long> primaryPortfolioIds = new ArrayList<>();
			List<Long> alternatePortfolioIds = new ArrayList<>();
			populatePrimaryAndAltPortfolioIds(existingMedStrategyPortfolioMap, primaryPortfolioIds,
					alternatePortfolioIds);
			defaultPlanDataDao.insertStrategyDefaultAssignmentsBy(emplIds, strategyId, primaryPortfolioIds,
					alternatePortfolioIds, benTypes);
		}
	}
	

	@Override
	public void assignDefaultPlanBy(Set<Long> strategyIds, Set<Long> groupIds,
			Map<Long, Boolean> existingMedStrategyPortfolioMap, Set<String> benTypes) {
		List<Long> primaryPortfolioIds = new ArrayList<>();
		List<Long> alternatePortfolioIds = new ArrayList<>();
		populatePrimaryAndAltPortfolioIds(existingMedStrategyPortfolioMap, primaryPortfolioIds, alternatePortfolioIds);
		eePlanAssignmentDao.deleteEePlanAssignmentBy(strategyIds, groupIds, benTypes);
		defaultPlanDataDao.insertStrategyDefaultAssignmentsBy(strategyIds, groupIds, primaryPortfolioIds,
				alternatePortfolioIds, benTypes);
	}

	@Override
	public void assignDefaultPlanForMissingEmployees(Set<String> noLocChangeEmployeeIds, long strategyId,
			long companyId, Map<Long, Boolean> existingMedStrategyPortfolioMap, boolean isTibCompany) {
		List<Long> primaryPortfolioIds = new ArrayList<>();
		List<Long> alternatePortfolioIds = new ArrayList<>();
		populatePrimaryAndAltPortfolioIds(existingMedStrategyPortfolioMap, primaryPortfolioIds, alternatePortfolioIds);
		Set<String> planTypes = new HashSet<>(isTibCompany ? EMPTY_PLAN_TYPES : PRIMARY_PLAN_TYPES);

		eePlanAssignmentDao.deleteEePlanAssignmentForWaivedEmployees(noLocChangeEmployeeIds, planTypes);
		Map<String, Set<String>> missingEeplansByBenType = defaultPlanDataDao
				.getMissingEmplPlanAssignmentsBy(noLocChangeEmployeeIds, strategyId, companyId);

		Set<String> missingMedPlanAssignmentsEeIds = missingEeplansByBenType
				.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		Set<String> missingDenPlanAssignmentsEeIds = missingEeplansByBenType
				.get(BSSApplicationConstants.DENTAL_PLAN_TYPE);
		Set<String> missingVisPlanAssignmentsEeIds = missingEeplansByBenType
				.get(BSSApplicationConstants.VISION_PLAN_TYPE);

		insertStrategyDefaultPlanAssignmentsBy(strategyId, missingMedPlanAssignmentsEeIds,
				BSSApplicationConstants.MEDICAL_PLAN_TYPE, primaryPortfolioIds, alternatePortfolioIds);

		insertStrategyDefaultPlanAssignmentsBy(strategyId, missingDenPlanAssignmentsEeIds,
				BSSApplicationConstants.DENTAL_PLAN_TYPE, primaryPortfolioIds, alternatePortfolioIds);

		insertStrategyDefaultPlanAssignmentsBy(strategyId, missingVisPlanAssignmentsEeIds,
				BSSApplicationConstants.VISION_PLAN_TYPE, primaryPortfolioIds, alternatePortfolioIds);
	}

	private void populatePrimaryAndAltPortfolioIds(Map<Long, Boolean> existingMedStrategyPortfolioMap,
			List<Long> primaryPortfolioIds, List<Long> alternatePortfolioIds) {
		if (MapUtils.isNotEmpty(existingMedStrategyPortfolioMap)) {
			for (Entry<Long, Boolean> portfolioEntrySet : existingMedStrategyPortfolioMap.entrySet()) {
				if (portfolioEntrySet.getValue().booleanValue()) {
					primaryPortfolioIds.add(portfolioEntrySet.getKey());
				} else {
					alternatePortfolioIds.add(portfolioEntrySet.getKey());
				}
			}
		}
	}

	private void insertStrategyDefaultPlanAssignmentsBy(long strategyId, Set<String> missingPlanAssignmentsEeIds,
			String benType, List<Long> primaryPortfolioIds, List<Long> alternatePortfolioIds) {
		if (CollectionUtils.isNotEmpty(missingPlanAssignmentsEeIds)) {
			defaultPlanDataDao.insertStrategyDefaultAssignmentsBy(missingPlanAssignmentsEeIds, strategyId,
					primaryPortfolioIds, alternatePortfolioIds, Set.of(benType));
		}
	}

}
