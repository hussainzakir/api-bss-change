package com.trinet.ambis.service.impl.outputs;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.outputs.PlanComparisionAsyncService;
import com.trinet.ambis.service.prospect.ProspectEmployeeService;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes;

@Service
public class PlanComparisionAsyncServiceImpl implements PlanComparisionAsyncService {
	
	@Autowired
	private RealmPlyrPlanService realmPlyrPlanService;
	
	@Autowired
	private ProspectEmployeeService prospectEmployeeService;
	
	@Autowired
	private PlanRatesService planRatesService;

	@Override
	public CompletableFuture<Map<String, XbssRealmPlyrPlan>> realmPlanYearAsync(long realmYearId) {
		return CompletableFuture.supplyAsync(() -> realmPlyrPlanService.getMapForRealmPlanYear(realmYearId));
	}

	@Override
	public CompletableFuture<Optional<List<EmployeePlansRes>>> prospectEmployeePlansAsync(String prospectId) {
		return CompletableFuture.supplyAsync(() ->prospectEmployeeService.getEmployeePlans(prospectId));
	}

	@Override
	public CompletableFuture<Map<String, List<BenefitPlanRate>>> getBenefitPlanRatesByAsync(Company company) {
		return CompletableFuture.supplyAsync(() -> planRatesService.getBenefitPlanRatesBy(company));
	}

}
