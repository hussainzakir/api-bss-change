/**
 * 
 */
package com.trinet.ambis.service.impl;

import java.util.List;
import java.util.Map;

import com.trinet.ambis.persistence.dao.hrp.impl.ContributionDataDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.ContributionDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.service.ContributionService;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.PlanContribution;

/**
 * @author kpamulapati
 *
 */

@Service
public class ContributionServiceImpl implements ContributionService {

	@Autowired
	ContributionDao contributionDao;

	@Autowired
	ContributionDataDaoImpl contributionDataDao;

	@Autowired
	StrategyDataDao strategyDataDao;

	@Override
	public Contribution createUpdate(Contribution contribution) {
		return contributionDao.saveAndFlush(contribution);
	}

	@Override
	public void saveAll(List<Contribution> contributionsList) {
		contributionDataDao.saveContributionData(contributionsList);
	}

	@Override
	public Contribution getById(long id) {
		return contributionDao.findById(id);
	}

	@Override
	public Map<Long, List<PlanContribution>> getPlanContributions(List<Long> planSelectionIds,
			Map<String, List<BenefitPlanRate>> planRates, boolean contributionRequired) {
		return strategyDataDao.getByPlanSelectionId(planSelectionIds, planRates, contributionRequired);
	}

	@Override
	public Contribution getByPlanSelectionIdAndName(long planSelectionId, String coverageLevel) {
		return contributionDao.findByPlanSelectionIdAndCoverageLevel(planSelectionId, coverageLevel);
	}

	@Override
	public List<Contribution> getContributions(long planSelectionId) {
		return contributionDao.findByPlanSelectionId(planSelectionId);
	}

	@Override
	public Contribution saveContribution(Contribution contribution) {
		return contributionDao.saveAndFlush(contribution);
	}

	@Override
	public void deleteAll(List<Contribution> list) {
		contributionDao.deleteAllInBatch(list);
	}
}
