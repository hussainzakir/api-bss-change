package com.trinet.ambis.service.prospect.service;

import com.trinet.ambis.service.prospect.dto.EmployeeCostRes;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ProspectEmployeeCostService {

   /**
    * This method returns the benefit plan cost of prospect employee by plan type
    *
    * @param prospectId
    * @param benefitTypes
    * @return CompletableFuture<OptionalList<BenefitsDetailsRes>>>
    */
   Optional<List<EmployeeCostRes>> getProspectEmployeeCostByType(String prospectId, List<String> benefitTypes);

}
