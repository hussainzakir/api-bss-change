/**
 * 
 */
package com.trinet.ambis.service.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author hliddle
 *
 */
public class ModelCompareGroupHeadcount {
	private String benefitProgram;
	private String groupDescr;

	@JsonIgnore
	private Map<Long, LinkedList<ModelComparePlanTypeCost>> strategyHeadcountMap;

	public String getBenefitProgram() {
		return benefitProgram;
	}

	public void setBenefitProgram(String benefitProgram) {
		this.benefitProgram = benefitProgram;
	}

	public String getGroupDescr() {
		return groupDescr;
	}

	public void setGroupDescr(String groupDescr) {
		this.groupDescr = groupDescr;
	}

	public List<ModelCompareStrategyHeadcount> getStrategyHeadcount() {
		List<ModelCompareStrategyHeadcount> strategyHeadcount = new LinkedList<>();
		for (Map.Entry<Long, LinkedList<ModelComparePlanTypeCost>> entry : strategyHeadcountMap.entrySet()) {
			ModelCompareStrategyHeadcount mcStrategyHeadcount = new ModelCompareStrategyHeadcount();
			mcStrategyHeadcount.setStrategyId(entry.getKey());
			mcStrategyHeadcount.setStrategyHeadcount(entry.getValue());
			strategyHeadcount.add(mcStrategyHeadcount);
		}
		return strategyHeadcount;
	}

	public Map<Long, LinkedList<ModelComparePlanTypeCost>> getStrategyHeadcountMap() {
		return strategyHeadcountMap;
	}

	public void setStrategyHeadcountMap(Map<Long, LinkedList<ModelComparePlanTypeCost>> strategyHeadcountMap) {
		this.strategyHeadcountMap = strategyHeadcountMap;
	}

}