package com.trinet.ambis.service;

import java.util.List;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.service.model.SchedMidYearFundingDto;
import com.trinet.ambis.service.model.SchedTblAdminDto;
import com.trinet.ambis.service.model.SchedTblDto;

import javax.servlet.http.HttpServletRequest;

public interface SchedTblService {
	/**
	 * 
	 * @param companyCode
	 * @param quarter
	 * @param realmYearId
	 * @return
	 */
	SchedTbl getScheduleDates(String companyCode, String quarter, Long realmYearId);

	/**
	 * This method pull the client and default schedule table data. If the client
	 * has a schedule, update it with the greater of the client and default
	 * extension end dates If not, return the default schedule
	 * 
	 * @param companyCode
	 * @param oeQuarter
	 * @param realmYearId
	 * @return
	 */
	SchedTbl getCalcuatedScheduleDates(String companyCode, String oeQuarter, Long realmYearId);

	/**
	 * 
	 * @param schedTblDto
	 * @return
	 */
	SchedTbl createUpdateScheduleDates(HttpServletRequest request, SchedTblDto schedTblDto, String lastUpdatedBy);

	/**
	 * @param smyf
	 * @param company
	 * @param updateFlag
	 * @return
	 */
	List<SchedMidYearFundingDto> createUpdateMidYearDetails(SchedMidYearFundingDto smyf, Company company,
			boolean updateFlag);

	/**
	 * @param company
	 * @return
	 */
	List<SchedMidYearFundingDto> getMidYearDetails(String companyCode);

	/**
	 * Returns a list of the passed in company's current and future schedule table
	 * information.
	 * 
	 * @param companyCode
	 * @param quarter
	 * @return
	 */
	List<SchedTblAdminDto> getSchedTblAdminDates(String companyCode, String quarter);

	/**
	 * Validates schedule dates post api request. <br>
	 * Currently company code and oe quarter are validated. <br>
	 * 
	 * @param schedTblDto
	 */
	void validateRequest(SchedTblDto schedTblDto);

}