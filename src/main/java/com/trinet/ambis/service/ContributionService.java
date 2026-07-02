/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;
import java.util.Map;

import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.PlanContribution;

/**
 * @author kpamulapati
 *
 */
public interface ContributionService {

	public Contribution createUpdate(Contribution contribution);
	public Contribution getById(long id);
	public Map<Long, List<PlanContribution>> getPlanContributions(List<Long> planSelectionId, Map<String, List<BenefitPlanRate>> planRates, boolean contributionRequired);
	public Contribution getByPlanSelectionIdAndName(long planSelectionId, String name);
	public List<Contribution> getContributions(long planSelectionId);
	public Contribution saveContribution(Contribution contribution);
	public void deleteAll(List<Contribution> list);
	void saveAll(List<Contribution> contributionsList);
}
