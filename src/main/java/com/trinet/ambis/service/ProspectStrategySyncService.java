package com.trinet.ambis.service;

import java.util.List;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.ProspectStrategySyncData;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;

public interface ProspectStrategySyncService {

	/**
	 * Updates Trinet strategy for the k1 status change of the employee
	 * 
	 * @param prospectStrategySyncData
	 * @param companyCode
	 */
	void handleCensusChangeEvent(List<ProspectStrategySyncData> prospectStrategySyncData, String companyCode);

	/**
	 * Delete employees in the employeeIds List
	 * and associated with passed in company code to sync Trinet Strategy
	 *
	 * @param employeeIds
	 * @param companyCode
	 */
	void handleCensusDeleteEvent(List<String> employeeIds, String companyCode);
	
	/**
	 * This method resets strategies based on state and location change
	 * 
	 * @param hqState
	 * @param zipCode
	 * @param companyCode
	 */
	void strategySyncOnHQLocationChange(String companyCode);

	void handleCensusAddEvent(List<ProspectStrategySyncData> prospectStrategySyncData, String companyCode);
	
	/**
	 * This method resets strategies based on bundleId change
	 *
	 * @param companyCode
	 * @param realmPlanYearId
	 */
	void resetStrategiesBy(Company companyCode, long realmPlanYearId);

	/**
	 * This method recalculates TIB employee strategy rates on dependent change event
	 *
	 * @param employeeIds
	 * @param companyCode
	 */
	void rateSyncOnDependentChange(List<String> employeeIds, String companyCode);

    /**
     * This method recalculates TIB employee strategy rates on census dependent change event
     *
     * @param prospectCensusResponseList
     * @param companyCode
     */
    void rateSyncOnCensusDependentChange(List<ProspectCensusResponse> prospectCensusResponseList, String companyCode);

}