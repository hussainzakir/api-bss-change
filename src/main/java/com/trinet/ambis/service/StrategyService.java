/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.trinet.ambis.client.DefaultPlanMappingServiceClient.PlanMappingResponse;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.rest.controllers.dto.StrategyCostRes;
import com.trinet.ambis.service.model.BenOfferExceptionDto;
import com.trinet.ambis.service.model.StrategyBudget;
import com.trinet.ambis.service.model.StrategyData;

/**
 * @author kpamulapati
 *
 */
/**
 * @author schaudhari
 *
 */
public interface StrategyService {
	/**
	 * 
	 * @param company
	 * @param history
	 * @param exchange 
	 * 
	 * @return List<StrategyData>
	 */
	List<StrategyData> getStrategies(Company company, boolean history, String exchange);
	/**
	 * 
	 * @param company
	 * @param strategyId
	 * @return
	 */
	StrategyData getStrategyById(Company company, long strategyId, boolean isDefault);
	/**
	 * This method returns the submitted strategies count for given realmYrId.
	 * 
	 * @param companyCode
	 * @param realmYrId
	 * 
	 * @return int
	 */
	int getStrategiesHistoryCount(String companyCode, long realmYrId);

	/**
	 * This method creates or updates the strategy information and returns back the strategy id. 
	 * 
	 * @param dto
	 * @param company
	 * @param updateFlag
	 * 
	 * @return long strategyId
	 */
	long createUpdateStrategy(StrategyData dto, Company company, boolean updateFlag);
	
	/**
	 * 
	 * @param companyId
	 * @return
	 */
	List<Strategy> getAllStrategies(long companyId);
	/**
	 * 
	 * @param strategy
	 * @return
	 */
	Strategy saveStrategy(Strategy strategy);

	/**
	 * 
	 * @param company
	 * @param isDefaultSubmit
	 */
	void createFutureStrategies(Company company, boolean isDefaultSubmit, boolean isPreload);
	
	/**
	 * This method returns  all submitted strategies for given company code.
	 * 
	 * @param code
	 * @return List
	 */
	List<Strategy> getAllSubmittedStrategiesByCompanyCode(String code);
	
	/**
	 * 
	 * @param companyId
	 * @return
	 */
	boolean hasSubmittedStrategy(long companyId);
	
	/**
	 * 
	 * @param bisCompany
	 * @param peoId
	 * @param quarter
	 */
	void preLoadBssStrategies(String peoId, String quarter, String emplid);
	
	/**
	 * 
	 * @param company
	 */
	void preLoadBssStrategies(Company company);
	
	/**
	 * This method will perform cascade delete on strategy object.
	 * 
	 * @param company
	 * @param strategyId
	 */
	void deleteStrategy(Company company, long strategyId);
	
	/**
	 * This method will update the name of the strategy.
	 * 
	 * @param strategyId
	 */
	void updateStrategyName(long strategyId, String strategyName);
	
	/**
	 * 
	 * @param company
	 * @param history
	 * @return
	 */
	List<StrategyData> updateStrategieHistory(Company company, boolean history);
	
	/**
	 * This method removes data from planSelection, strategyFundingModel and
	 * strategyFundingModel tables for given plan type codes and company.
	 * 
	 * @param benOfferExceptionDto
	 * @param planTypeCodes
	 */
	void syncStrategiesForBenOfferException(BenOfferExceptionDto benOfferExceptionDto, Set<String> planTypeCodes);
	
	/**
	 * @param companyCode
	 * @return
	 */
	List<Strategy> findBy(String companyCode );
	
	void updateStrategyBudget(long strategyId, StrategyBudget budget);
	/**
	 * 
	 * @param company
	 */
	void createProspectsTrinetStrategy(Company company, long selectedCarrier, List<PlanMappingResponse> planMappingResponse);
	
	/**
	 * Reset the strategies for the given company code
	 * 
	 * @param companyCode
	 * @param companyId
	 * @param realmPlanYearId
	 * @param strategyIds
	 */
	void resetStrategiesBy(String companyCode, long companyId, long realmPlanYearId, Set<Long> strategyIds);
	
	/**Returns strategy for the given given strategy id 
	 * @param strategyId
	 * @return
	 */
	Optional<Strategy> findById(long strategyId);
	
	/**
	 * This will return the primaryCarrierName, related to medical
	 * 
	 * @param company
	 * @param strategyId
	 * @return primaryCarrierName string
	 */
	String getPrimaryCarrierName(Company company, String tnStrategyId);

	/**
	 * This will return a List of company cost for the company's submitted strategy
	 *
	 * @param company
	 * @param strategyId
	 * @return StrategyCostRes
	 */
	StrategyCostRes getStrategyCostByPlanType(Company company, long strategyId);


	/**
	 * This method will create the tib medical strategy estimate for the given company and strategy ids
	 *
	 * @param company
	 * @param strategyIds
	 * @return
	 */
	void createOmsStrategyEstimate(Company company, Set<Long> strategyIds);
	
	/**
	 * This method will delete the strategies for the given company code
	 * 
	 * @param strategyIds
	 */
	void deleteExistingStrategies(Set<Long> strategyIds);
	
	/**
	 * Returns strategies for companyID and submitted status
	 * @param companyId
	 * @param status
	 * @return
	 */
	List<Strategy> findByCompanyIdAndSubmitted(long companyId, boolean submitted);
	
	/**
	 * Updates status of list of strategy ids
	 * @param startegyIds
	 * @param status
	 * @return
	 */
	void updateStrategiesStatus(List<Long> startegyIds, String status);
	
	/**
	 * Updates the  strategy name and type for a strategy id
	 * @param strategyId
	 */
	void updateSubmittedStrategyDetails(long strategyId);

	/**
	 * Deletes all strategies and strategy-related data for the given company
	 * using an Oracle PL/SQL cursor loop in a single DB round-trip.
	 *
	 * @param companyId the BSS company ID
	 */
	void deleteStrategies(long companyId);
}
