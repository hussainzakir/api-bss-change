/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.GroupHeadCount;
import com.trinet.ambis.service.model.GroupData;
import com.trinet.ambis.service.model.GroupRuleDto;

/**
 * @author kpamulapati
 *
 */
@Service
public interface BenefitGroupService {
	/**
	 * 
	 * @param benefitGroup
	 * @return
	 */
	BenefitGroup saveBenefitGroup(BenefitGroup benefitGroup);

	/**
	 * 
	 * @param id
	 */
	void deleteBenefitGroup(long id);

	/**
	 * 
	 * @param companyId
	 * @param groupId
	 * @return
	 */
	BenefitGroup getBenefitGroupByCompanyIdAndId(long companyId, long groupId);

	/**
	 * 
	 * @param companyId
	 * @param status
	 * @return
	 */
	List<BenefitGroup> getAllBenefitGroups(long companyId, String status);

	/**
	 * 
	 * @param strategyId
	 * @return
	 */
	List<BenefitGroup> getBenefitGroupByStrategy(long strategyId, String status);
	
	/**
	 * 
	 * @param strategyId
	 * @return
	 */
	List<BenefitGroup> getBenefitGroupByStrategy(long strategyId, List<String> status);

	/**
	 * 
	 * @param strategyId
	 * @param groupId
	 * @return
	 */
	BenefitGroup getBenefitGroupsByStrategyIdAndGroupId(long strategyId, long groupId, String status);

	/**
	 * 
	 * @param benefitGroups
	 * @return
	 */
	List<BenefitGroup> saveAll(List<BenefitGroup> benefitGroups);

	/**
	 * 
	 * @param id
	 * @return
	 */
	GroupHeadCount getGroupHeadCount(long id);

	/**
	 * Perform a service to update the name/description of a benefit group
	 * @param groupId
	 * @param newName
	 */
	void updateBenefitGroupName( Long groupId, String newName );
	
	/**
	 * Method to update the name/description of a benefit group
	 * @param groupId
	 * @param newName
	 */
	void updateBenefitGroupName(long strategyId, long groupId, String newName);

	/**
	 * 
	 * @param companyCode
	 * @param groupId
	 * @param companyId
	 * @param waitPeriod
	 * @param defaultFlag
	 * @param realmPlanYearId
	 * @return
	 */
	void updateBenefitGroupMetaData(String companyCode, Long groupId, Long companyId, String waitPeriod,
			boolean defaultFlag, long realmPlanYearId);

	/**
	 * 
	 * @param company
	 * @param benefitGroup
	 */
	void getBenefitGroupMetaData(Company company, BenefitGroup benefitGroup);

	/**
	 * 
	 * @return
	 */
	Map<String, String> generateRateTableId();

	/**
	 * 
	 * @param rateTableIds
	 */
	void generateRateTableIdsNonMedical(Map<String, String> rateTableIds);

	/**
	 * 
	 * @param companyId
	 * @param status
	 * @return
	 */
	Map<String, Integer> getBenefitProgramHeadCount(long companyId, String status);

	/**
	 * 
	 * @param company
	 * @param groupData
	 * @param strategyId
	 * @return
	 */
	long addGroup(Company company, GroupData groupData, long strategyId);

	/**
	 * 
	 * @param strategyGroupId
	 * @param strategyId
	 */
	void deleteGroup(long strategyGroupId, long strategyId);

	/**
	 * Adds mandatory benefit groups to the list of passed in benefitGroups if
	 * they are not already there.
	 * 
	 * @param company
	 * @param benefitGroups
	 * @param waitPeriodMap
	 */
	void addMandatoryBenefitGroups(Company company, List<BenefitGroup> benefitGroups,
			Map<String, String> waitPeriodMap);

	/**
	 * Returns true when the benefit group defined by the passed in groupRuleDto
	 * exists in the benefitGroups list. Returns false if it does not.
	 * 
	 * @param groupRuleDto
	 * @param benefitGroups
	 * @return
	 */
	boolean mandatoryGroupExists(GroupRuleDto groupRuleDto, List<BenefitGroup> benefitGroups);
	
	/**
	 * 
	 * @param group
	 * @return
	 */
	BenefitGroup completeRateTblSet(BenefitGroup group);
	
	/**
	 * This method is for constructing the W2 benefit program
	 * @param company
	 * @param isDefaultGroup
	 * @return
	 */
	BenefitGroup constructW2Group(Company company, boolean isDefaultGroup);
	
	/**
	 * This method is for constructing the K1 benefit program
	 * @param company
	 * @return
	 */
	BenefitGroup constructK1Group(Company company);

    /**
     * This method is for constructing the MA benefit group
     *
     * @param company
     * @return
     */
    BenefitGroup constructMAGroup(Company company);

	/**
	 * Returns a list of benefit programs in the strategy that have employees associated with them
	 * @param companyCode
	 * @param strategyId
	 * @return
	 */
	List<String> getBenefitProgramsForStrategy(String companyCode, Long strategyId);
	
	/**
	 * 
	 * @param companyId
	 * @return
	 */
	List<BenefitGroup> findByCompanyId(long companyId);
	
	/**
	 * Updates the status of BenefitGroup
	 * @param ids
	 * @param status
	 */
	void updateBenefitGroupStatus(BenefitGroup benefitGroup, String status);
	
	/**
	 * Updates the benefit group with the benefit program, ELIG_RULES_ID, RATE_TBL_ID, ELIG_CONFIG1
	 *
	 * @param benefitGroup
	 * @param company
	 * @return updated BenefitGroup
	 */
	BenefitGroup updateGroupWithPSDetails( BenefitGroup benefitGroup, Company company, boolean prospectConversion);

    /**
     * Method to convert the benefit group to K1
     * @param strategyId
     * @param groupId
     * @param companyCode
     */
    void updateBenefitGroupType(long strategyId, long groupId, String companyCode);
}
