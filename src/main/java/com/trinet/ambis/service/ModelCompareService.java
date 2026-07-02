package com.trinet.ambis.service;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.ModelCompareGroupHeadcount;
import com.trinet.ambis.service.model.ModelCompareStrategy;
import com.trinet.ambis.service.model.ModelCompareStrategyCost;
import com.trinet.ambis.service.model.StrategyBenefitPlanHeadCount;

/**
 * @author tallam
 *
 */
@Service
public interface ModelCompareService {
	/**
	 * This method is for getting the strategies list for model compare and including strategy group details based on isExport flag.
	 * 
	 * @param company
	 * @param isExport
	 * @return
	 */
	List<ModelCompareStrategy> getMCStrategies(Company company, boolean isExport);

	/**
	 * Return strategy costs estimates for benefit plans.
	 * 
	 * @param selectedStrategyIDs
	 * @param companyCode
	 * @return List
	 */
	List<ModelCompareStrategyCost> getMCSelectedStrategyCosts(List<Long> selectedStrategyIDs, Company company);

	/**
	 * This method is for getting the funding details for selected strategy.
	 * 
	 * @param strategyId
	 * @param companyCode
	 * @return ModelCompareStrategy
	 */
	ModelCompareStrategy getMCStrategyGroupFunding(long strategyId, Company company);

	/**
	 * This method returns the head scount by plan, strategy and coverage level
	 * for the selected strategies and company
	 * 
	 * @param strategyIds
	 * @param companyCode
	 * @return
	 */
	public List<StrategyBenefitPlanHeadCount> getMCPlanStrategyCoverageHeadcount(List<Long> strategyIds,
			Company company);

	/**
	 * This method gets the headcount and employer cost by group for the
	 * selected strategies
	 * 
	 * @param selectedStrategyIDs
	 *            {@code a List<Long>}
	 * @return {@code List<ModelCompareGroupHeadcount>}
	 */
	List<ModelCompareGroupHeadcount> getMCStrategyHeadcountCostByGroup(List<Long> selectedStrategyIDs, Company company);

	/**
	 * This method returns an excel workbook containing the company and employee
	 * model compare data
	 * 
	 * @param selectedStrategyIDs
	 *            {@code a List<Long>}
	 * @return {@code List<ModelCompareGroupHeadcount>}
	 */
	Workbook getModelCompareExcelWorkbook(Company company, Long currentStrategy, List<Long> strategyList);

	/**
	 * 
	 * @param company
	 * @param currentStrategyId
	 * @param strategyList
	 * @return
	 */
	void getEmployeeStrategiesPlanCostWorkbook(Company company, Long currentStrategy,
			List<Long> strategyList, Map<Long, String> strategyMap, Workbook workbook);

}