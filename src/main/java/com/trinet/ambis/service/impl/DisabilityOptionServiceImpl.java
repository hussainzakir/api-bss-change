/**
 * 
 */
package com.trinet.ambis.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.DisabilityOptionService;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;

/**
 * @author rvutukuri
 *
 */
@Service
public class DisabilityOptionServiceImpl implements DisabilityOptionService {

	@Autowired
	RealmDataDao realmDataDao;

	@Override
	public Set<AdditionalBenefitPlan> getDisabilityOptionsByRealmPlanYear(Company company) {
		Set<AdditionalBenefitPlan> disabilityBenefitOptions = new HashSet<>();
		Map<String, AdditionalBenefitPlan> disabilityOptionsMap = realmDataDao.getDisabilityOptionPlans(
				company.getRealmPlanYearId(), company.getHeadQuatersState(), company.getRealm().getBenExchange(), company.getSdiStates());
		disabilityBenefitOptions.addAll(disabilityOptionsMap.values());
		return disabilityBenefitOptions;
	}

	@Override
	public AdditionalBenefitPlan getDisabilityOptionByPlans(List<String> benefitPlans, Company company,
			boolean isStandAlone) {
		AdditionalBenefitPlan returnPlan = null;
		String selectedOptionId = null;
		if (!benefitPlans.isEmpty()) {
			Map<String, AdditionalBenefitPlan> disabilityOptionsMap = realmDataDao.getDisabilityOptionPlans(
					company.getRealmPlanYearId(), company.getHeadQuatersState(), company.getRealm().getBenExchange(),
					company.getSdiStates());
			Map<String, List<String>> optionBenefitPlanMap = StrategyServiceHelper
					.constructOptionPlansMap(disabilityOptionsMap);
			for (String optionId : optionBenefitPlanMap.keySet()) {
				AdditionalBenefitPlan ab = disabilityOptionsMap.get(optionId);
				List<String> optPlans = optionBenefitPlanMap.get(optionId);
				if ((ab.isStandAlone() == isStandAlone) && optPlans.containsAll(benefitPlans)) {
					selectedOptionId = optionId;
					break;
				}
			}
			returnPlan = disabilityOptionsMap.get(selectedOptionId);
		}
		return returnPlan;
	}

	@Override
	public List<DisabilityBenefitOptionPlans> getDisabilityPlansByOption(String optionId, Company company) {
		Map<String, AdditionalBenefitPlan> disabilityOptionsMap = realmDataDao.getDisabilityOptionPlans(
				company.getRealmPlanYearId(), company.getHeadQuatersState(), company.getRealm().getBenExchange(), company.getSdiStates());
		return disabilityOptionsMap.get(optionId).getOptionPlans();
	}

	@Override
	public String getDisabilitySTDPlanByLTDPlan(String ltdPlan, Long realmPlanYearId, String region,
			String benExchange) {
		String selectedSTD = null;
		Map<String, AdditionalBenefitPlan> disabilityOptionsMap = realmDataDao.getDisabilityOptionPlans(realmPlanYearId,
				region, benExchange, RulesAndConfigsUtils.getSDIStates(realmPlanYearId));
		Map<String, List<String>> optionBenefitPlanMap = StrategyServiceHelper
				.constructOptionPlansMap(disabilityOptionsMap);
		for (String optionId : optionBenefitPlanMap.keySet()) {
			List<String> optionPlans = optionBenefitPlanMap.get(optionId);
			AdditionalBenefitPlan ab = disabilityOptionsMap.get(optionId);
			if (!ab.isStandAlone() && optionPlans.contains(ltdPlan)) {
				for (DisabilityBenefitOptionPlans dbo : ab.getOptionPlans()) {
					if (Constants.STD_CODE.equals(dbo.getPlanType())) {
						selectedSTD = dbo.getId();
						break;
					}
				}
				break;
			}
		}
		return selectedSTD;
	}

}
