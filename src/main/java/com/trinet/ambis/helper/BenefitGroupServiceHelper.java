package com.trinet.ambis.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyGroupHeadCount;
import com.trinet.ambis.persistence.model.StrategyGroupHeadCountId;
import com.trinet.ambis.service.model.GroupRuleDto;
import com.trinet.ambis.service.model.GroupRuleDto.PlanTypeRule;

/**
 * This class contains all the helper methods to support BenefitGroupService
 * 
 * @author schaudhari
 *
 */
public class BenefitGroupServiceHelper {
	
	private BenefitGroupServiceHelper() {
		throw new IllegalStateException(
				"Utility class " + BenefitGroupServiceHelper.class.getName() + " can not be instantiated.");
	}

	/**
	 * This method contrusts the list of all StrategyGroupHeadCount using given
	 * StrategyBenefitGroup object and bsgId.
	 * 
	 * @param sbg
	 * @param bsgId
	 * @return List<StrategyGroupHeadCount>
	 */
	public static List<StrategyGroupHeadCount> prepareStrategyGroupHeadCountObj(Map<String, Integer> covrgLvlHeadCounts,
			long bsgId) {
		List<StrategyGroupHeadCount> strategyGroupHeadCounts = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : covrgLvlHeadCounts.entrySet()) {
			StrategyGroupHeadCount sghc = new StrategyGroupHeadCount();
			StrategyGroupHeadCountId id = new StrategyGroupHeadCountId();
			id.setCovrgCd(CoverageCodesEnums.valueOfCode(entry.getKey()));
			id.setStrategyGroupId(bsgId);
			sghc.setId(id);
			sghc.setHeadcount(entry.getValue());
			strategyGroupHeadCounts.add(sghc);
		}
		return strategyGroupHeadCounts;
	}

	/**
	 * Returns a map of Plan Type (String) and PlanTypeRule if any rules apply
	 * to the passed in benefit group
	 * 
	 * @param benefitGroup
	 * @param groupRuleDtoList
	 * @return
	 */
	public static Map<String, PlanTypeRule> getBenefitGroupPlanTypeExceptions(BenefitGroup benefitGroup,
			List<GroupRuleDto> groupRuleDtoList) {
		Map<String, PlanTypeRule> benefitGroupExceptions = new HashMap<>();

		for (GroupRuleDto groupRuleDto : groupRuleDtoList) {
			if (groupRuleDto.getGroupType().equals(benefitGroup.getType())
					&& ((groupRuleDto.getState() == null && benefitGroup.getState() == null)
							|| (groupRuleDto.getState() != null && benefitGroup.getState() != null
									&& groupRuleDto.getState().equals(benefitGroup.getState())))) {
				for (PlanTypeRule planTypeRule : groupRuleDto.getRules()) {
					benefitGroupExceptions.put(planTypeRule.getPlanType(), planTypeRule);
				}
			}
		}
		return benefitGroupExceptions;
	}
	
	/**
	 * This method is for constructing the Strategy Benefit Group.
	 * @param benefitGroup
	 * @param strategy
	 * @return
	 */
	public static BenefitGroupStrategy constructStrategyBenefitGroup(BenefitGroup benefitGroup, Strategy strategy,
			String waitPeriod, boolean defaultGroup) {
		BenefitGroupStrategy bsg = new BenefitGroupStrategy();
		bsg.setGroupId(benefitGroup.getId());
		bsg.setBenefitGroup(benefitGroup);
		bsg.setStrategyId(strategy.getId());
		bsg.setStrategy(strategy);
		bsg.setDefaultGroup(defaultGroup);
		bsg.setWaitingPeriod(waitPeriod);
		bsg.setHeadcount(0);
		bsg.setStatus(BSSApplicationConstants.STATUS_ACTIVE);
		return bsg;
	}
	
	public static String generateProspectBenProgram() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
	}
}
