package com.trinet.ambis.service.outputs;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes;


public interface PlanComparisionAsyncService {
	
	CompletableFuture<Map<String, XbssRealmPlyrPlan>> realmPlanYearAsync(long realmYearId);
	
	CompletableFuture<Optional<List<EmployeePlansRes>>> prospectEmployeePlansAsync(String prospectId);
	
	CompletableFuture<Map<String, List<BenefitPlanRate>>> getBenefitPlanRatesByAsync(Company company);

}
