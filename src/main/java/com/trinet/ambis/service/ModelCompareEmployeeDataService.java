package com.trinet.ambis.service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.EmployeeStrategyData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ModelCompareEmployeeDataService {

    /**
     * This method returns the employee level benefit plan and cost comparison for given strategies.
     *
     * Works for prospect company, prospect to client converted company and renewal companies.
     *
     * For prospect company the current strategy data comes from the prospect api, for prospect to client converted
     * new clients all the strategy data comes from bss api and all the strategies are from the same plan year so no
     * separate logic is required for current vs future strategies.
     *
     * For both prospect and new clients the plan assignments and cost for all the strategies comes from
     * ee plan assignment table unlike renewal client. Renewal clients the data comes from tables populated
     * by nightly census job.
     *
     * In future once the plan assignment feature is available for renewal clients, we will get rid of renewal company
     * logic and use the same logic for new and renewal companies.
     *
     * @param company
     * @param currentStrategyId
     * @param strategyList
     * @return
     */
	List<EmployeeStrategyData> getEmployeeStrategiesPlanCostData(Company company, Long currentStrategyId,
			List<Long> strategyList);
}
