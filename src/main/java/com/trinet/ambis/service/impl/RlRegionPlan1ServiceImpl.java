package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.RlRegionPlan1Repository;
import com.trinet.ambis.persistence.projections.RlRegionPlan1View;
import com.trinet.ambis.service.RlRegionPlan1Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RlRegionPlan1ServiceImpl implements RlRegionPlan1Service {

	private final RlRegionPlan1Repository rlRegionPlan1Repository;

	@Override
	public Map<String, List<String>> findByRealmPlanYearId(long realmPlanYearId) {
		List<RlRegionPlan1View> rlRegionPlan1Views = rlRegionPlan1Repository
				.findByRealPlanYearId(new BigDecimal(realmPlanYearId));
		return rlRegionPlan1Views.stream().collect(Collectors.groupingBy(plan -> plan.getBenefitPlan(),
				Collectors.mapping(plan -> plan.getRegion(), Collectors.toList())));
	}

}
