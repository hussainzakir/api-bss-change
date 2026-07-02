package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.util.BssCoreServiceClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.MultiKeyMap;
import org.joda.time.DateTimeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDataDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeStrategyGroupDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeStrategyGroupTransactionDao;
import com.trinet.ambis.persistence.dao.hrp.EmployerEmployeePlansMappingDao;
import com.trinet.ambis.persistence.dao.hrp.PlanMappingDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDataDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Employee;
import com.trinet.ambis.persistence.model.EmployeeStrategyGroup;
import com.trinet.ambis.persistence.model.EmployeeStrategyGroupTransaction;
import com.trinet.ambis.persistence.model.PlanMapping;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.EmployeeBenefitGroupService;
import com.trinet.ambis.service.EmployeeDataService;
import com.trinet.ambis.service.ModelCompareEmployeeDataService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.model.BenefitOfferFunding;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRateData;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.EmpBenPlanMapping;
import com.trinet.ambis.service.model.EmployeeAssignmentData;
import com.trinet.ambis.service.model.EmployeeBenefitGroup;
import com.trinet.ambis.service.model.EmployeeData;
import com.trinet.ambis.service.model.EmployeeSourceData;
import com.trinet.ambis.service.model.EmployeeStrategyData;
import com.trinet.ambis.service.model.EmployeeStrategyPlanData;
import com.trinet.ambis.service.model.GroupFunding;
import com.trinet.ambis.service.model.ModelCompareBenSuppExcessOption;
import com.trinet.ambis.service.model.ModelCompareStrategy;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanTypeDescription;
import com.trinet.ambis.service.model.StrategyGroupDetails;
import com.trinet.ambis.service.model.StrategyGroupPlanRateData;
import com.trinet.ambis.service.prospect.ProspectEmployeeService;
import com.trinet.ambis.service.prospect.ProspectGroupAssignmentService;
import com.trinet.ambis.service.prospect.response.CensusRes;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.Utils;

/**
 * Following Service is used to the details of Employees from PeopleSoft Note:
 * This is different from EmployeeServiceImpl which is used to get the employee
 * data stored in BSS
 */

@Service
public class EmployeeDataServiceImpl implements EmployeeDataService {

	@Autowired
	EmployeeBenefitGroupService employeeBenefitGroupService;

	@Autowired
	RealmPlanYearService realmPlanYearService;

	@Autowired
	ProspectGroupAssignmentService prospectGroupAssignmentService;

	@Autowired
	ProspectEmployeeService prospectEmployeeService;
	
	@Autowired
	EmployeeDataDao employeeDataDao;

	@Autowired
	EmployeeDao employeeDao;

	@Autowired
	EmployeeStrategyGroupDao employeeStrategyGroupDao;

	@Autowired
	EmployeeStrategyGroupTransactionDao employeeStrategyGroupTransactionDao;

	@Autowired
	XbssRealmPlyrPlanDao xbssRealmPlyrPlanDao;
	
	@Autowired
	StrategyDao strategyDao;
	
	@Autowired
	StrategyDataDao strategyDataDao;
	
	@Autowired
	StrategyFundingDataDao strategyFundingDataDao;

	@Autowired
	RealmDataDao realmDataDao;

	@Autowired
	EmployerEmployeePlansMappingDao employerEmployeePlansMappingDao;

	@Autowired
	EmployeeBenefitGroupDao employeeBenefitGroupDao;

	@Autowired
	PortfolioRuleDao portfolioRuleDao;
	
	@Autowired
	PlanMappingDao planMappingDao;

	@Autowired
    ModelCompareEmployeeDataService modelCompareEmployeeDataService;

	@Autowired
	StrategyService strategyService;

    @Autowired
    BssCoreServiceClient bssCoreServiceClient;

	private static final Logger logger = LoggerFactory.getLogger(EmployeeDataService.class);
	private static final String NO_COVERAGE_CODE = "0";

	@Override
	public boolean loadEmployeeData(Company company, Map<String, BenefitGroup> mapOfBenefitProgram,
			Map<String, Set<Long>> strategyGroupBenefitProgramMap) {
		Set<Employee> employees = new HashSet<>();
		Map<String, Employee> psEmployees = employeeDataDao.getEmployeesByCompany(company);
		Set<EmployeeStrategyGroup> employeeStrategyGroups = new HashSet<>();
		for (Entry<String, Employee> psEmployee : psEmployees.entrySet()) {
			String psftBenefitProgramName = psEmployee.getValue().getBenefitProgram();
			psEmployee.getValue().setBenefitProgram(psftBenefitProgramName);
			psEmployee.getValue().setUpdateTime(new Date());
			BenefitGroup benefitGroup = mapOfBenefitProgram.get(psftBenefitProgramName);
			logger.info("employees benefit program : {}:{}", psEmployee.getValue().getEmplId(), psftBenefitProgramName);
			logger.info("benefitgroupid in create future strategies flow {}", benefitGroup.getId());
			psEmployee.getValue().setBenefitGroupId(benefitGroup.getId());
			psEmployee.getValue().setBenefitGroupName(benefitGroup.getName());
			if (benefitGroup.getEligConfig1() != null) {
				psEmployee.getValue().setEligConfig1(benefitGroup.getEligConfig1());
			}
			employees.add(psEmployee.getValue());
			Set<Long> strategyGroups = strategyGroupBenefitProgramMap.get(benefitGroup.getBenefitProgram());
			for (long strategyGroupId : strategyGroups) {
				EmployeeStrategyGroup employeeStrategyGroup = new EmployeeStrategyGroup();
				employeeStrategyGroup.setEmplId(psEmployee.getValue().getEmplId());
				employeeStrategyGroup.setStrategyGroupId(strategyGroupId);
				employeeStrategyGroups.add(employeeStrategyGroup);
			}
		}
		// Persist the employee data - First into XBSS_EMPLOYEE then into
		// XBSS_EMPLOYEE_STRATEGY_GROUP
		if (!employees.isEmpty()) {
			logger.info("Number of employees to be Saved are {}", employees.size());
			employeeDao.saveAll(employees);
			employeeStrategyGroupDao.saveAll(employeeStrategyGroups);
		}
		return false;
	}

	@Override
	public Set<EmployeeData> getEmployeesData(Company company, long strategyId) {
		Set<EmployeeData> employeesData = new HashSet<>();
		if (company.isProspectCompany()) {
			if (strategyId == ProspectConstants.PROSPECT_STRATEGY_ID) {
				employeesData = prospectGroupAssignmentService.getEmployeeGroupAssignments(company.getCode());
			} else {
				employeesData = getEmployeeDataForProspectTnStrategy(strategyId, company.getCode());
			}
		} else {
			employeesData = getEmployeeDataForClientStrategy(strategyId, company);
		}
		return employeesData;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void updateEmployeeAssignment(Company company, EmployeeAssignmentData employeeAssignmentData,
			long strategyId) {
		List<EmployeeSourceData> employeeSourceData = employeeAssignmentData.getEmployeesList();
		if (company.isProspectCompany() && strategyId == ProspectConstants.PROSPECT_STRATEGY_ID) {
			prospectGroupAssignmentService.updateEmployeeGroupAssignment(strategyId, employeeAssignmentData);
		} else {
			List<EmployeeStrategyGroupTransaction> newAssignments = new ArrayList<>();
			for (EmployeeSourceData esd : employeeSourceData) {
				for( String emplid : esd.getEmployees() ) {
					EmployeeStrategyGroupTransaction txn = new EmployeeStrategyGroupTransaction();
					txn.setEmplid(emplid);
					txn.setStrategyGroupId(employeeAssignmentData.getDestinationStrategyGroupId());
					txn.setCreateDate(new Date());
					newAssignments.add(txn);
				}
			}
			employeeStrategyGroupTransactionDao.saveAll(newAssignments);
			strategyService.createOmsStrategyEstimate(company, Set.of(strategyId));
		}
	}

    @Override
	public void employeeDataSync(Company company) {
		if (!company.isProspectCompany()) {
			Map<String, Employee> psEmployees = employeeDataDao.getEmployeesByCompany(company);
			employeeBenefitGroupDao.deleteEmployeeStrategyGroups(company);
			Map<String, Set<StrategyGroupDetails>> benefitProgramStrategyGroups = employeeBenefitGroupDao
					.getStrategyGroupDetailsForCompany(company);
			Map<String, Set<StrategyGroupDetails>> addEmployeeStrategyGroups = new HashMap<>();
			for (Employee psEmployee : psEmployees.values()) {
				String benefitProgram = psEmployee.getBenefitProgram();
				Set<StrategyGroupDetails> strategyGroups = benefitProgramStrategyGroups.get(benefitProgram);
				if (CollectionUtils.isEmpty(strategyGroups)) {
					String errorMessage = String.format( "Employee sync failed due to missing or inactive benefit program. Company: %s,Employee: %s, Benefit Program: %s", company.getCode(), psEmployee.getEmplId(), benefitProgram);
				throw new BSSApplicationException(new RuntimeException(), new BSSApplicationError("", BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, this.getClass().getName(), errorMessage, null, null));
				}
				psEmployee.setUpdateTime(new Date());
				psEmployee.setEligConfig1(strategyGroups.iterator().next().getEligConfig1());
				addEmployeeStrategyGroups.put(psEmployee.getEmplId(), strategyGroups);
			}
			employeeBenefitGroupService.insertNewEmployeeStrategyGroups(addEmployeeStrategyGroups);
		}
	}

	@Override
	public List<EmployeeStrategyData> getEmployeeStrategiesPlanCostData(Company company, Long currentStrategyId,
			List<Long> strategyList) {

		// If the company is a renewal company, get the data from PeopleSoft data
		// otherwise, get the current data from the prospect DB.
		if (company.isRenewalCompany()) {
			return getEmployeeStrategiesPlanCostDataForRenewalClient(company, currentStrategyId, strategyList);
		} else {
			return modelCompareEmployeeDataService.getEmployeeStrategiesPlanCostData(company, currentStrategyId,
					strategyList);
		}
	}

    /**
     * This method will be removed once the EE plan assignment feature is available for renewal companies.
     * Will transition to modelCompareEmployeeDataService#getEmployeeStrategiesPlanCostData
     *
     * @param company
     * @param currentStrategyId
     * @param strategyList
     * @return
     */
	private List<EmployeeStrategyData> getEmployeeStrategiesPlanCostDataForRenewalClient(Company company, Long currentStrategyId,
                                                                                         List<Long> strategyList) {

		// employees with new benefit group for a given strategy
		MultiKeyMap emplStrategyBenGroupMap = strategyDataDao.getEmplStrategyBenGroup(company.getId());

		// Employees on a mirror plan
		MultiKeyMap mirrorPlanEnrollees = employeeDataDao.getMirrorPlanEnrolledEmployees(company);

		// list to combine current strategy and strategies for upcoming plan year
		List<Long> allStrategyList = new ArrayList<>();

		allStrategyList.add(currentStrategyId);
		allStrategyList.addAll(strategyList);

		// get the plan cost data for the given company for strategy parameters
		MultiKeyMap strategyGroupPlanCostMap = strategyDataDao.getGroupStrategyPlanCost(company,
				allStrategyList);

		RealmPlanYear prevRealmYear = realmPlanYearService.getPreviousRealmPlanYear(company.getCode(),
				company.getRealmPlanYearId());

		Strategy strategy = null;

		// get the company's funding details by strategy and group
		Optional<Strategy> strategyData = strategyDao.findById(currentStrategyId);
		if (strategyData.isPresent()) {
			strategy = strategyData.get();
		}

		Map<Long, ModelCompareStrategy> strategyGroupFundingDetailsMap = new HashMap<>();

		// Get Medical Dental Vision details
		if (strategy != null && company.getId() == strategy.getCompanyId()) {
			strategyGroupFundingDetailsMap.putAll(strategyFundingDataDao.getFundingDetailsByStrategyId(
					Arrays.asList(currentStrategyId), company, true, company.getRealmPlanYear().getPlanYearEnd()));
		} else {
			strategyGroupFundingDetailsMap.putAll(strategyFundingDataDao.getFundingDetailsByStrategyId(
					Arrays.asList(currentStrategyId), company, true, prevRealmYear.getPlanYearEnd()));
		}

		strategyGroupFundingDetailsMap.putAll(strategyFundingDataDao.getFundingDetailsByStrategyId(strategyList,
				company, true, company.getRealmPlanYear().getPlanYearEnd()));
		MultiKeyMap fundingTypeData = getFundingTypeData(strategyGroupFundingDetailsMap);
		MultiKeyMap waiverAllowanceData = getWaiverAllowanceData(strategyGroupFundingDetailsMap);
		MultiKeyMap benefitSupplementData = getBenefitSupplementData(strategyGroupFundingDetailsMap);
		Map<Long,Map<String,GroupFunding>> strategyGroupMap = mapStrategiesToGroups( strategyGroupFundingDetailsMap );


		// a map by strategy, benefitProgram and planType to determine what type
		// of plans the client offers for a given benefit program and strategy
		MultiKeyMap strategyProgramPlanTypeOfferingMap = strategyDataDao
				.getStrategyProgramPlantypeOfferings(allStrategyList, BSSApplicationConstants.PRIMARY_PLAN_TYPES);

		// Create the return map of employees, strategies and plans and add the
		// current cost data from PeopleSoft as the current strategy
		List<Object[]> currentEmployeePlanData = getCurrentEmployeePlanData(company, currentStrategyId);

		RealmPlanYear previousRealmPlanYear = realmPlanYearService.getPreviousRealmPlanYear(company.getRealmPlanYear());
		List<EmployeeStrategyData> employeeStrategyPlanData = processEmployeeStrategyPlanData(company, currentStrategyId,
				previousRealmPlanYear, currentEmployeePlanData, strategyGroupMap );

		// sort the employee list (by emplName) before we add the new strategies
		// below
		Collections.sort(employeeStrategyPlanData);

		if (!(employeeStrategyPlanData.isEmpty())) {

			updateEmplCurrentCosts(employeeStrategyPlanData, strategyGroupPlanCostMap, waiverAllowanceData, mirrorPlanEnrollees);

			setupEmplNewStrategies(company, strategyList, emplStrategyBenGroupMap, employeeStrategyPlanData,
					strategyGroupPlanCostMap, strategyProgramPlanTypeOfferingMap, fundingTypeData, waiverAllowanceData,
					strategyGroupMap );

			// make sure each employee has a medical dental and vision plan, if
			// employee hasn't elected that plan type and client offers that
			// plan type
			expandEmployeeStrategyPlans(employeeStrategyPlanData, strategyProgramPlanTypeOfferingMap);

			// Update employee/client costs based on the client's benefit supplement setup
			updateBsuppEmplRates(employeeStrategyPlanData, benefitSupplementData);

		}

		return employeeStrategyPlanData;
	}

	private Map<Long,Map<String,GroupFunding>> mapStrategiesToGroups( Map<Long, ModelCompareStrategy> strategyGroupFundingDetailsMap ) {
		Map<Long,Map<String,GroupFunding>> strategyGroupMap = new HashMap<>();
		for( Map.Entry<Long,ModelCompareStrategy> entry : strategyGroupFundingDetailsMap.entrySet() ) {
			strategyGroupMap.put( entry.getKey(), new HashMap<>() );
			for( GroupFunding group : entry.getValue().getGroupFundingList() ) {
				strategyGroupMap.get( entry.getKey() ).put( group.getBenefitProgram(), group );
			}
		}
		return strategyGroupMap;
	}


	public void setupEmplNewStrategies(Company company, List<Long> strategyList,
			MultiKeyMap emplStrategyBenGroupMap, List<EmployeeStrategyData> employeeStrategyPlanData,
			MultiKeyMap strategyGroupPlanCostMap, MultiKeyMap strategyProgramPlanTypeOfferingMap,
			MultiKeyMap fundingTypeData, MultiKeyMap waiverAllowanceData,
			Map<Long,Map<String,GroupFunding>> strategyGroupMap ) {

		List<EmployeeStrategyPlanData> empStrategyPlanList;

		// This is the current strategy with the existing costs
		EmployeeStrategyPlanData curEmpStrategyData = null;
		// each strategy has a benefit group id and a set of plans
		EmployeeStrategyPlanData newEmpStrategyData = null;
		// mapped data for one strategy, ben group and plan
		StrategyGroupPlanRateData strategyGroupPlanRateData = null;

		// one plan with rates
		BenefitPlanRateData empPlanAndRates = null;
		long realmPlanYearId = company.getRealmPlanYearId();

		Map<String, Map<String, String>> defaultPlanMap = realmDataDao
				.getPortfilioDefaultPlans(company.getRealmPlanYearId());

		boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions( company );

		Map<String, Set<PlanCarrier>> planCarrierMap = portfolioRuleDao.getPortfoliosByHqRegion(
				company.getRealmPlanYearId(), company.getHeadQuatersState(), company.getZipCode(),
				company.getExclusiveMedPlan(), company.getPlanStartDate(), isPickChoose );

		// applying exclusivity rules for portfolios
		BenefitCategoriesHelper.updatePlanCarrierExclusivity(company, planCarrierMap, company.isRenewalCompany());

		Set<String> primaryPlanCarriers = BenefitCategoriesHelper.getPlanCarriers(planCarrierMap);
		Set<String> outOfRegionPlans = CommonServiceHelper.getOutOfRegionPlansToExclude(company, primaryPlanCarriers,
				realmDataDao);
			
		Map<String, PlanMapping> realmPlanMapping = planMappingDao.getPrimaryPlanMappings(company, outOfRegionPlans);

		// The default plan for a given realm plan year and portfolio id
		Map<String, Map<String, String>> strategyPortfolioDefaultPlans = realmDataDao
				.getPortfilioDefaultPlans(realmPlanYearId);

		// map for employer to employee paid plans -- Needed for dental and
		// vision
		Map<BenefitPlan, BenefitPlan> erEePlanMapping = employerEmployeePlansMappingDao
				.getEmployerEmployeePlansMappingByRealmYearId(realmPlanYearId);	

		// This map is passed to and from the getMappedStrategyPlan method to
		// keep up with the plan mapping so that we do not have to go through
		// the logic for a strategy, group, plan combination multiple times
		MultiKeyMap strategyGroupPlanMapping = new MultiKeyMap();

		MultiKeyMap empBenPlanMappings = employeeDataDao.getEmpPlanMapping(company.getCode(), company.getRealmPlanYear().getId());
		
		boolean isVendorMappingOn = RulesAndConfigsUtils.isVendorMappingOn(realmPlanYearId);
		
		/**
		 * At this point each employee has one strategy which has his current
		 * plan data for each employee and current plan Get the plan in the new
		 * strategy it might be original plan, a mapped plan or a default plan
		 * Get cost for that plan
		 **/
		// each employee
		for (EmployeeStrategyData employeeStrategyData : employeeStrategyPlanData) {
			// all the strategies for one employee
			empStrategyPlanList = employeeStrategyData.getStrategyDetails();
			// the 1st strategy for this employee with his current plan data
			curEmpStrategyData = employeeStrategyData.getStrategyDetails().get(0);
			if (fundingTypeData.containsKey(curEmpStrategyData.getStrategyId(), curEmpStrategyData.getBenefitProgram())) {
				curEmpStrategyData.setFundingType((String) fundingTypeData.get(curEmpStrategyData.getStrategyId(), curEmpStrategyData.getBenefitProgram()));
			}
			Long currentGroupId = curEmpStrategyData.getGroupId();
			String currentGroupName = curEmpStrategyData.getGroupName();

			/**
			 * For each employee Based on the employee's current plans: For each
			 * new strategy: Create and setup the strategy object Get the
			 * current or mapped (if needed) plan and the plan costs Add the
			 * plan to the strategy's plan list
			 **/
			for (Long strategyId : strategyList) {

				// setup this strategy in the employee's strategy list
				newEmpStrategyData = new EmployeeStrategyPlanData();
				newEmpStrategyData.setStrategyId(strategyId);

				/****
				 * see if employee has a new benefit group for this strategy
				 ****/
				BenefitGroup empGroup = (BenefitGroup) emplStrategyBenGroupMap.get(employeeStrategyData.getEmplId(), strategyId);

				// mapped or current group id, name and benefitProgram
				Long groupId = null;
				String groupName = null;
				String benefitProgram = null;
				if( empGroup != null ) {
					benefitProgram = empGroup.getBenefitProgram();
					groupId = empGroup.getId();
					groupName = empGroup.getName();
				} else {
					benefitProgram = curEmpStrategyData.getBenefitProgram();
					/* set the following default values, but then lookup the strategyId/BenefitProgram for
					 * a possible better value in case the benefit group name was changed */
					groupId = currentGroupId;
					groupName = currentGroupName;
					Map<String,GroupFunding> map = strategyGroupMap.get( strategyId );
					if( map != null ) {
						GroupFunding group = map.get( benefitProgram );
						if( group != null ) {
							groupId = group.getId();
							groupName = group.getName();
						}
					}
				}

				/**** benefit program mapping ****/
				newEmpStrategyData.setGroupId(groupId);
				newEmpStrategyData.setGroupName(groupName);
				newEmpStrategyData.setBenefitProgram(benefitProgram);
				if (fundingTypeData.containsKey(strategyId, benefitProgram)) {
					newEmpStrategyData.setFundingType((String) fundingTypeData.get(strategyId, benefitProgram));
				}

				// add new strategy to the map of the strategies for this
				// employee
				empStrategyPlanList.add(newEmpStrategyData);

				/**
				 * for every plan in the employee's original strategy setup a
				 * corresponding plan/cost object for this new strategy
				 **/
				for (BenefitPlanRateData empPlanRate : curEmpStrategyData.getBenefitPlans()) {
					empPlanAndRates = null;

					// For enrolled employees - get new
					// strategyGroupPlanRateData with plan and costs
					// based on current plan and the new strategy
					if (("E").equals(empPlanRate.getCoverageElect())) {
						if (isVendorMappingOn) {
							String planType = clientOfferedPlanType(strategyId, benefitProgram,
									empPlanRate.getPlanType(), strategyProgramPlanTypeOfferingMap);
							strategyGroupPlanRateData = getMappedStrategyPlan(strategyId.longValue(),
									groupId.longValue(), planType, empPlanRate.getCoverageLevel(),
									employeeStrategyData.getEmplId(), strategyGroupPlanCostMap, empBenPlanMappings);
						} else {
							strategyGroupPlanRateData = getMappedStrategyPlan(strategyId.longValue(),
									groupId.longValue(), empPlanRate.getPlanType(), empPlanRate.getPlanId(),
									empPlanRate.getCoverageLevel(), empPlanRate.getPortfolioId().toString(),
									strategyGroupPlanMapping, realmPlanMapping, strategyGroupPlanCostMap,
									strategyPortfolioDefaultPlans, erEePlanMapping);
						}
						if (strategyGroupPlanRateData != null) {
							// we found a plan for the new strategy,
							// benefitPlan,
							// planType, planName, coverageLevel, eeRate,
							// erRate,
							// potfolioId

							if (("TriNet").equals(strategyGroupPlanRateData.getMapReason())) {
								empPlanRate.setMappingFlag(strategyGroupPlanRateData.getMapReason());
								strategyGroupPlanRateData.setMapReason(null);
							}

							empPlanAndRates = new BenefitPlanRateData(strategyGroupPlanRateData.getBenefitPlan(),
									strategyGroupPlanRateData.getPlanType(), strategyGroupPlanRateData.getDescription(),
									strategyGroupPlanRateData.getCoverageLevel(),
									strategyGroupPlanRateData.getCoverageLevelName(),
									strategyGroupPlanRateData.getEeRate(), strategyGroupPlanRateData.getErRate(),
									empPlanRate.getPortfolioId(), empPlanRate.getCoverageElect(),
									strategyGroupPlanRateData.getMapReason(), strategyGroupPlanRateData.isMirrorPlanFlag(), true);
						} else {
							/**
							 * no plan found for employee in new strategy and
							 * benefit group This could be due to 1. The current
							 * plan couldn't be mapped to a plan in the new
							 * strategy 2. The client no longer offers this plan
							 * type
							 *
							 * Create a placeholder plan if client offers this
							 * plan type and a matching new plan just couldn't
							 * be found Otherwise don't add that plan type to
							 * the employee strategy plan list
							 **/

							// clientPlanType will not be null if client offers
							// this
							// type of plan for this strategy and benefit
							// program
							String clientPlanType = clientOfferedPlanType(strategyId, benefitProgram,
									empPlanRate.getPlanType(), strategyProgramPlanTypeOfferingMap);

							if (clientPlanType != null) {

								empPlanAndRates = new BenefitPlanRateData(null, clientPlanType, null, null, null, null,
										null, null, empPlanRate.getCoverageElect(), "Unknown", false, true);

							}
						}
						if (empPlanAndRates != null) {
							newEmpStrategyData.getBenefitPlans().add(empPlanAndRates);
						}
					}

					// For waived employees, check for Medical waiverAllowance
					// in future strategies.
					else if (("W").equals(empPlanRate.getCoverageElect())) {
						boolean createEmptyPlanRate = true;
						if ((BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(empPlanRate.getPlanType()))
								&& waiverAllowanceData.containsKey(strategyId, benefitProgram)) {
							BigDecimal waiverAllowance = (BigDecimal) waiverAllowanceData.get(strategyId, benefitProgram);
							if (waiverAllowance.compareTo(BigDecimal.ZERO) > 0) {
								empPlanAndRates = new BenefitPlanRateData(empPlanRate.getPlanType(), BigDecimal.ZERO,
										waiverAllowance,
										empPlanRate.getCoverageElect(), null, false, true);
								createEmptyPlanRate = false;
							}

						}
						if (createEmptyPlanRate) {
							empPlanAndRates = new BenefitPlanRateData(empPlanRate.getPlanType(), null, null,
									empPlanRate.getCoverageElect(), null, false, true);
						}

						newEmpStrategyData.getBenefitPlans().add(empPlanAndRates);
					}

				} // for each employee plan
			} // for each strategy
		} // each employee
	}

	/**
	 * This method takes the data in a ModelCompareStrategy object and
	 * simplifies it into a MultiKeyMap of strategyId, groupId and fundingType
	 * 
	 * @param strategyGroupFundingDetailsMap
	 * @return
	 */
	private MultiKeyMap getFundingTypeData(Map<Long, ModelCompareStrategy> strategyGroupFundingDetailsMap) {
		MultiKeyMap returnMap = new MultiKeyMap();
		for (Entry<Long, ModelCompareStrategy> modelCompareMapEntry : strategyGroupFundingDetailsMap.entrySet()) {
			Long strategyId = modelCompareMapEntry.getKey();
			ModelCompareStrategy modelCompareStrategy = modelCompareMapEntry.getValue();
			for (GroupFunding groupFunding : modelCompareStrategy.getGroupFundingList()) {
				String benefitProgram = groupFunding.getBenefitProgram();
				if (groupFunding.getOfferTypeFunding().containsKey(BSSApplicationConstants.MEDICAL)) {
					String fundingType = groupFunding.getOfferTypeFunding().get(BSSApplicationConstants.MEDICAL)
							.getFundingType();
					returnMap.put(strategyId, benefitProgram, fundingType);
				}
			}
		}
		return returnMap;
	}
	
	/**
	 * This method takes the data in a ModelCompareStrategy object and
	 * simplifies it into a MultiKeyMap of strategyId, groupId and
	 * waiverAllowance
	 * 
	 * @param strategyGroupFundingDetailsMap
	 * @return
	 */
	private MultiKeyMap getWaiverAllowanceData(Map<Long, ModelCompareStrategy> strategyGroupFundingDetailsMap) {
		MultiKeyMap returnMap = new MultiKeyMap();
		for (Entry<Long, ModelCompareStrategy> modelCompareMapEntry : strategyGroupFundingDetailsMap.entrySet()) {
			Long strategyId = modelCompareMapEntry.getKey();
			ModelCompareStrategy modelCompareStrategy = modelCompareMapEntry.getValue();
			for (GroupFunding groupFunding : modelCompareStrategy.getGroupFundingList()) {
				String benefitProgram = groupFunding.getBenefitProgram();
				Map<String, BenefitOfferFunding> offerFundingMap = groupFunding.getOfferTypeFunding();
				for (Entry<String, BenefitOfferFunding> offerFundingEntry : offerFundingMap.entrySet()) {
					if (BSSApplicationConstants.MEDICAL.equals(offerFundingEntry.getKey())) {
						BigDecimal waiverAllowance = offerFundingEntry.getValue().getWaiverAllowance();
						if (waiverAllowance != null) {
							returnMap.put(strategyId, benefitProgram, waiverAllowance);
						}
					}
				}
			}
		}
		return returnMap;
	}
	
	/**
	 * This method takes the data in a ModelCompareStrategy object and
	 * simplifies it into a MultiKeyMap of strategyId, benefitProgram and
	 * BenefitOfferFunding for Medical offerings that are BSUPP funding type
	 * 
	 * @param strategyGroupFundingDetailsMap
	 * @return
	 */
	protected MultiKeyMap getBenefitSupplementData(Map<Long, ModelCompareStrategy> strategyGroupFundingDetailsMap) {
		MultiKeyMap returnMap = new MultiKeyMap();
		for (Entry<Long, ModelCompareStrategy> modelCompareMapEntry : strategyGroupFundingDetailsMap.entrySet()) {
			Long strategyId = modelCompareMapEntry.getKey();
			ModelCompareStrategy modelCompareStrategy = modelCompareMapEntry.getValue();
			for (GroupFunding groupFunding : modelCompareStrategy.getGroupFundingList()) {
				String benefitProgram = groupFunding.getBenefitProgram();
				Map<String, BenefitOfferFunding> offerFundingMap = groupFunding.getOfferTypeFunding();
				for (Entry<String, BenefitOfferFunding> offerFundingEntry : offerFundingMap.entrySet()) {
					if ((BSSApplicationConstants.MEDICAL.equals(offerFundingEntry.getKey()))
							&& (BSSApplicationConstants.BSUPP.equals(offerFundingEntry.getValue().getFundingType()))) {
						returnMap.put(strategyId, benefitProgram, offerFundingEntry.getValue());
					}
				}
			}
		}
		return returnMap;
	}
	
	private void updateEmplCurrentCosts(List<EmployeeStrategyData> employeeStrategyPlanData,
			MultiKeyMap strategyGroupPlanCostMap, MultiKeyMap waiverAllowanceData, MultiKeyMap mirrorPlanEnrollees) {
		
		for (EmployeeStrategyData employeeStrategyData : employeeStrategyPlanData) {
			String emplId = employeeStrategyData.getEmplId();
			for (EmployeeStrategyPlanData empStrategyPlanData : employeeStrategyData.getStrategyDetails()) {
				
				Long strategyId = empStrategyPlanData.getStrategyId();
				Long groupId = empStrategyPlanData.getGroupId();

				for (BenefitPlanRateData benefitPlanRateData : empStrategyPlanData.getBenefitPlans()) {
					String planType = benefitPlanRateData.getPlanType();
					String benefitPlan = benefitPlanRateData.getPlanId();
					String coverageLevel = benefitPlanRateData.getCoverageLevel();
					String benefitProgram = empStrategyPlanData.getBenefitProgram();
					
					// If this is a mirror plan enrollee, set the flag and employee/employer rate
					if (mirrorPlanEnrollees.containsKey(emplId, planType)) {
						BigDecimal fplRate = (BigDecimal) mirrorPlanEnrollees.get(emplId, planType);
						BigDecimal eeCost = ((StrategyGroupPlanRateData) strategyGroupPlanCostMap.get(strategyId,
								groupId, planType, benefitPlan, coverageLevel)).getEeRate();
						BigDecimal erCost = ((StrategyGroupPlanRateData) strategyGroupPlanCostMap.get(strategyId,
								groupId, planType, benefitPlan, coverageLevel)).getErRate();
						erCost = erCost.add(eeCost).subtract(fplRate);
						eeCost = fplRate;
						benefitPlanRateData.setEmployeeContribution(eeCost);
						benefitPlanRateData.setEmployerContribution(erCost);
						benefitPlanRateData.setMirrorPlanFlag(true);
					}
					else if (strategyGroupPlanCostMap.containsKey(strategyId, groupId, planType, benefitPlan,
							coverageLevel)) {
						BigDecimal eeCost = ((StrategyGroupPlanRateData) strategyGroupPlanCostMap.get(strategyId,
								groupId, planType, benefitPlan, coverageLevel)).getEeRate();
						BigDecimal erCost = ((StrategyGroupPlanRateData) strategyGroupPlanCostMap.get(strategyId,
								groupId, planType, benefitPlan, coverageLevel)).getErRate();
						benefitPlanRateData.setEmployeeContribution(eeCost);
						benefitPlanRateData.setEmployerContribution(erCost);
					}
					else if (("W").equals(benefitPlanRateData.getCoverageElect())) {
						if ((BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(benefitPlanRateData.getPlanType()))
								&& waiverAllowanceData.containsKey(strategyId, benefitProgram)) {
							BigDecimal waiverAllowance = (BigDecimal) waiverAllowanceData.get(strategyId, benefitProgram);
							if (waiverAllowance.compareTo(BigDecimal.ZERO) > 0) {
								BigDecimal eeCost = BigDecimal.ZERO;
								BigDecimal erCost = waiverAllowance;
								benefitPlanRateData.setEmployeeContribution(eeCost);
								benefitPlanRateData.setEmployerContribution(erCost);
							}

						}
					}					
				}
			}
		}
	}
	
	public void expandEmployeeStrategyPlans(List<EmployeeStrategyData> employeeStrategyPlanData,
			MultiKeyMap strategyProgramPlanTypeOfferingMap) {
		/*
		 * Make sure every employee has a medical, dental and vision plan if the
		 * client offers that type of plan Missing plans will be inserted as
		 * waived coverage if client offers that plan type Also reordering plans
		 * in employee strategy plan list medical, dental then vision
		 */
		BenefitPlanRateData empPlanAndRates = null; // one plan with rates
		Long strategyId;
		String benefitProgram;
		String planType;
		String genericPlanType;
		String[] plnTypeCode = BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER.toArray(new String[0]);
		BenefitPlanRateData[] empPlans = new BenefitPlanRateData[3];
		
		// each employee in our return data set
		for (EmployeeStrategyData employeeStrategyData : employeeStrategyPlanData) {

			// for each employee strategy
			for (EmployeeStrategyPlanData curEmpStrategyData : employeeStrategyData.getStrategyDetails()) {
				for (int p = 0; p < plnTypeCode.length; p++) {
					empPlans[p] = null; // reset the employee plans
				}
				
				// loop thru the employee's plans and find any medical/dental
				// and/or vision plans
				for (BenefitPlanRateData benPlanData : curEmpStrategyData.getBenefitPlans()) {
					if (benPlanData.getPlanType() != null) {
						genericPlanType = Utils.getGenericPlanType(benPlanData.getPlanType());
						if (BSSApplicationConstants.MEDICAL.equals(genericPlanType)) {
							empPlans[BSSApplicationConstants.MEDICAL_SORT] = benPlanData;
						} else if (BSSApplicationConstants.DENTAL.equals(genericPlanType)) {
							empPlans[BSSApplicationConstants.DENTAL_SORT] = benPlanData;
						} else if (BSSApplicationConstants.VISION.equals(genericPlanType)) {
							empPlans[BSSApplicationConstants.VISION_SORT] = benPlanData;
						}
					}
				}

				strategyId = curEmpStrategyData.getStrategyId();
				benefitProgram = curEmpStrategyData.getBenefitProgram();

				// setup the plans in this employee strategy list in M/D/V order
				curEmpStrategyData.getBenefitPlans().clear();

				for (int i = 0; i < plnTypeCode.length; i++) {

					if (empPlans[i] != null) {
						empPlanAndRates = empPlans[i]; // existing plan

					} else {

						boolean offered = false;
						planType = plnTypeCode[i];
						if (strategyProgramPlanTypeOfferingMap.containsKey(strategyId, benefitProgram,
								plnTypeCode[i])) {
							offered = true;
							planType = (String) strategyProgramPlanTypeOfferingMap.get(strategyId, benefitProgram,
									plnTypeCode[i]);
						}

						// Add a not enrolled plan
						empPlanAndRates = new BenefitPlanRateData(null, planType, null, null, null, null, null, null,
								"N", null, false, offered);
					}
					if (empPlanAndRates != null) {
						// add found or waived plan to plan list
						curEmpStrategyData.getBenefitPlans().add(empPlanAndRates);
					}
				} // plan types

			} // employee strategies

		} // loop thru all employees

	}

	/**
	 * This method updates the employer and employee cost for dental and/or
	 * vision based on the client's benefit supplement setup. This assumes that
	 * the employee's plans are in Medical, Dental and then Vision order.
	 * 
	 * @param employeeStrategyPlanData
	 * @param benefitSupplementData
	 */
	protected void updateBsuppEmplRates(List<EmployeeStrategyData> employeeStrategyPlanData,
			MultiKeyMap benefitSupplementData) {

		for (EmployeeStrategyData employeeStrategyData : employeeStrategyPlanData) {
			for (EmployeeStrategyPlanData employeeDetails : employeeStrategyData.getStrategyDetails()) {
				Long strategyId = employeeDetails.getStrategyId();
				String benefitProgram = employeeDetails.getBenefitProgram();
				BigDecimal bsuppRemainder = BigDecimal.ZERO;
				if (benefitSupplementData.containsKey(strategyId, benefitProgram)) {
					BenefitOfferFunding benefitOfferFunding = (BenefitOfferFunding) benefitSupplementData
							.get(strategyId, benefitProgram);

					if (benefitOfferFunding != null) {
						List<String> planTypeList = getBsuppPlanTypes(benefitOfferFunding.getExcessOption());
						for (BenefitPlanRateData employeePlans : employeeDetails.getBenefitPlans()) {
							if (employeePlans.getCoverageLevel() != null
									&& !NO_COVERAGE_CODE.equals(employeePlans.getCoverageLevel())
									&& planTypeList.contains(employeePlans.getPlanType())) {
								if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(employeePlans.getPlanType())) {
									bsuppRemainder = getMedicalBsuppCoverageAmount(benefitOfferFunding,
											CoverageCodesEnums.valueOfId(employeePlans.getCoverageLevel()))
													.subtract(employeePlans.getEmployerContribution());
								} else {
									if (bsuppRemainder.compareTo(BigDecimal.ZERO) > 0) {
										BigDecimal currentEmployeeContribution = employeePlans
												.getEmployeeContribution();
										BigDecimal updatedEmployeeContribution;
										BigDecimal updatedEmployerContribution;

										if (bsuppRemainder.compareTo(currentEmployeeContribution) >= 0) {
											updatedEmployeeContribution = BigDecimal.ZERO;
											updatedEmployerContribution = currentEmployeeContribution;
											bsuppRemainder = bsuppRemainder.subtract(currentEmployeeContribution);
										} else {
											updatedEmployeeContribution = currentEmployeeContribution
													.subtract(bsuppRemainder);
											updatedEmployerContribution = bsuppRemainder;
											bsuppRemainder = BigDecimal.ZERO;
										}
										employeePlans.setEmployeeContribution(updatedEmployeeContribution);
										employeePlans.setEmployerContribution(updatedEmployerContribution);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	private List<String> getBsuppPlanTypes(ModelCompareBenSuppExcessOption modelCompareBenSuppExcessOption) {
		List<String> planTypeList = new ArrayList<>();
		planTypeList.add(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		if (modelCompareBenSuppExcessOption.getOptionId().equals(1L)) {
			planTypeList.add(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);
			planTypeList.add(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE);
		}
		else if (modelCompareBenSuppExcessOption.getOptionId().equals(2L)) {
			for (PlanTypeDescription planTypeDescription : modelCompareBenSuppExcessOption.getExcessVoluntaryPlanTypes()) {
				planTypeList.add(planTypeDescription.getPlanType());
			}
		}
		return planTypeList;
	}
	
	private BigDecimal getMedicalBsuppCoverageAmount(BenefitOfferFunding benefitOfferFunding, String coverageLevelId) {
		BigDecimal bsuppCoverageAmount = BigDecimal.ZERO;
		for (CoverageLevel coverageLevel : benefitOfferFunding.getCoverageLevels()) {
			if (coverageLevelId.equals(coverageLevel.getId())) {
				bsuppCoverageAmount = coverageLevel.getContribution();
				break;
			}
		}
		return bsuppCoverageAmount;
	}

	/**
	 * This method returns the mapped plan. 
	 *	1. It will check if plan is available in strategy.
	 *  2. If not found in (#1) it will check in realm mapped plan 
	 *  3. If not found in (#2) it will check in vendor switching mapped plan.
	 * 
	 * @param strategyId
	 * @param groupId
	 * @param planType
	 * @param coverageLevel
	 * @param emplId
	 * @param strategyGroupPlanCostMap
	 * @param empBenPlanMappings
	 * @return
	 */
	private StrategyGroupPlanRateData getMappedStrategyPlan(long strategyId, long groupId, String planType,
			String coverageLevel, String emplId, MultiKeyMap strategyGroupPlanCostMap,
			MultiKeyMap empBenPlanMappings) {
		StrategyGroupPlanRateData strategyGroupPlanRateData = null;
		if(null == empBenPlanMappings || null == strategyGroupPlanCostMap || strategyGroupPlanCostMap.isEmpty()) {
			return strategyGroupPlanRateData;
		}
		EmpBenPlanMapping empBenPlanMapping = (EmpBenPlanMapping) empBenPlanMappings.get(emplId, planType);
		if (null == empBenPlanMapping) {
			String altPlanType = planType;
			if (BSSApplicationConstants.DENTAL_PLAN_TYPE.equalsIgnoreCase(planType)) {
				altPlanType = BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE;
			} else if (BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE.equalsIgnoreCase(planType)) {
				altPlanType = BSSApplicationConstants.DENTAL_PLAN_TYPE;
			}
			if (BSSApplicationConstants.VISION_PLAN_TYPE.equals(planType)) {
				altPlanType = BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE;
			} else if (BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE.equals(planType)) {
				altPlanType = BSSApplicationConstants.VISION_PLAN_TYPE;
			}
			empBenPlanMapping = (EmpBenPlanMapping) empBenPlanMappings.get(emplId, altPlanType);
		}
		if (null != empBenPlanMapping) {
			if (empBenPlanMapping.getNextBenPlan() != null &&
                    strategyGroupPlanCostMap.containsKey(strategyId, groupId, planType,
					    empBenPlanMapping.getNextBenPlan(), coverageLevel)) {
				strategyGroupPlanRateData = (StrategyGroupPlanRateData) strategyGroupPlanCostMap.get(strategyId, groupId, planType,
						empBenPlanMapping.getNextBenPlan(), coverageLevel);
				if (!empBenPlanMapping.getCurBenPlan().equals(empBenPlanMapping.getNextBenPlan())) {
					strategyGroupPlanRateData.setMapReason(BSSApplicationConstants.MAP_REASON_TRINET);
				}
			} else {
                for (String altPlan : empBenPlanMapping.getAltBenPlans()) {
                    if (strategyGroupPlanCostMap.containsKey(strategyId, groupId, planType,
                            altPlan, coverageLevel)) {
                        strategyGroupPlanRateData = (StrategyGroupPlanRateData) strategyGroupPlanCostMap.get(strategyId, groupId, planType,
                                altPlan, coverageLevel);
                        if (!empBenPlanMapping.getCurBenPlan().equals(altPlan)) {
                            strategyGroupPlanRateData.setMapReason(BSSApplicationConstants.MAP_REASON_CLIENT);
                        }
                        break;
                    }
                }
            }
		}
		return strategyGroupPlanRateData;
	}

	public StrategyGroupPlanRateData getMappedStrategyPlan(long strategyId, long groupId, String planType, String plan,
			String coverageLevel, String portfolioId, MultiKeyMap strategyGroupPlanMapping,
			Map<String, PlanMapping> realmPlanMapping, MultiKeyMap strategyGroupPlanCostMap,
			Map<String, Map<String, String>> strategyPortfolioDefaultPlans,
			Map<BenefitPlan, BenefitPlan> erEePlanMapping) {

		final int PLAN_INDEX = 0;
		final int PLAN_TYPE_INDEX = 1;
		final int MAP_REASON_INDEX = 2;

		boolean foundPlan = false;
		String realmMappedPlan = "";
		// Array of strings for plan, planType, and mapReason
		String[] returnPlanAndType = { "", "", null };
		
		// If the strategy cost map is null or empty or if
		// strategyGroupPlanMapping is null , return null as this is an
		// exception
		if (strategyGroupPlanCostMap == null || strategyGroupPlanCostMap.isEmpty()
				|| strategyGroupPlanMapping == null) {
			return null;
		}

		// First, see if this is already in the plan map
		if (strategyGroupPlanMapping.containsKey(strategyId, groupId, planType, plan)) {
			returnPlanAndType = (String[]) strategyGroupPlanMapping.get(strategyId, groupId, planType, plan);
			foundPlan = true;
		}

		// Second, see if this plan is in the client's strategy for this group
		if (!foundPlan && strategyGroupPlanCostMap.containsKey(strategyId, groupId, planType, plan, coverageLevel)) {

			returnPlanAndType[PLAN_INDEX] = plan;
			returnPlanAndType[PLAN_TYPE_INDEX] = planType;
			strategyGroupPlanMapping.put(strategyId, groupId, planType, plan, returnPlanAndType);
			foundPlan = true;
		}

		// Third, see if this plan is in the realmPlanMapping
		// We are keeping the realPlamMapping plan if there is one for future
		// use in mapping
		if (!foundPlan && realmPlanMapping != null && realmPlanMapping.containsKey(plan)) {

			realmMappedPlan = realmPlanMapping.get(plan).getNewBenefitPlan();
			if (strategyGroupPlanCostMap.containsKey(strategyId, groupId, planType, realmMappedPlan, coverageLevel)) {
				returnPlanAndType[PLAN_INDEX] = realmMappedPlan;
				returnPlanAndType[PLAN_TYPE_INDEX] = planType;
				returnPlanAndType[MAP_REASON_INDEX] = BSSApplicationConstants.MAP_REASON_TRINET;
				strategyGroupPlanMapping.put(strategyId, groupId, planType, plan, returnPlanAndType);
				foundPlan = true;
			}

		}
		
		// Fifth, see if the plan is in the default mapping for the plan's
		// current portfolio
		if (!foundPlan && portfolioId != null && strategyPortfolioDefaultPlans != null
				&& strategyPortfolioDefaultPlans.containsKey(planType)
				&& strategyPortfolioDefaultPlans.get(planType).containsKey(portfolioId)
				&& strategyGroupPlanCostMap.containsKey(strategyId, groupId, planType,
						strategyPortfolioDefaultPlans.get(planType).get(portfolioId), coverageLevel)) {

			returnPlanAndType[PLAN_INDEX] = strategyPortfolioDefaultPlans.get(planType).get(portfolioId);
			returnPlanAndType[PLAN_TYPE_INDEX] = planType;
			returnPlanAndType[MAP_REASON_INDEX] = BSSApplicationConstants.MAP_REASON_CLIENT;
			strategyGroupPlanMapping.put(strategyId, groupId, planType, plan, returnPlanAndType);
			foundPlan = true;
		}

		// Sixth, see if the plan is in the default mapping for any portfolio
		// for this planType
		if (!foundPlan && strategyPortfolioDefaultPlans != null
				&& strategyPortfolioDefaultPlans.containsKey(planType)) {

			for (Map.Entry<String, String> entry : strategyPortfolioDefaultPlans.get(planType).entrySet()) {
				returnPlanAndType[PLAN_INDEX] = entry.getValue();
				returnPlanAndType[PLAN_TYPE_INDEX] = planType;
				returnPlanAndType[MAP_REASON_INDEX] = BSSApplicationConstants.MAP_REASON_CLIENT;
				if (strategyGroupPlanCostMap.containsKey(strategyId, groupId, planType, returnPlanAndType[PLAN_INDEX],
						coverageLevel)) {
					strategyGroupPlanMapping.put(strategyId, groupId, planType, plan, returnPlanAndType);
					foundPlan = true;
					break;
				} else {
					returnPlanAndType[PLAN_INDEX] = "";
					returnPlanAndType[PLAN_TYPE_INDEX] = "";
					returnPlanAndType[MAP_REASON_INDEX] = "";
				}
			}
		}

		// Seventh, see if the plan is in the EE to ER mapping in either direction
		// - The map includes both directions already
		if (!foundPlan && erEePlanMapping != null) {

			for (Map.Entry<BenefitPlan, BenefitPlan> erEeEntry : erEePlanMapping.entrySet()) {
				BenefitPlan oldBenefitPlan = erEeEntry.getKey();
				BenefitPlan newBenefitPlan = erEeEntry.getValue();
				if (plan.equals(oldBenefitPlan.getId()) || realmMappedPlan.equals(oldBenefitPlan.getId())) {
					String mapPlanId = newBenefitPlan.getId();
					String mapPlanType = newBenefitPlan.getPlanType();
					// If this mapped plan is in the cost map, use this plan
					if (strategyGroupPlanCostMap.containsKey(strategyId, groupId, mapPlanType, mapPlanId,
							coverageLevel)) {
						returnPlanAndType[PLAN_INDEX] = mapPlanId;
						returnPlanAndType[PLAN_TYPE_INDEX] = mapPlanType;
						returnPlanAndType[MAP_REASON_INDEX] = BSSApplicationConstants.MAP_REASON_CLIENT;
						strategyGroupPlanMapping.put(strategyId, groupId, planType, plan, returnPlanAndType);
						foundPlan = true;
						break;
					}
					// If not, get the default plan for mapped plan's planType,
					// then go back to EE to ER mapping to get the plan
					else if (strategyPortfolioDefaultPlans != null) {
						Map<String, String> defaultPlans = null;
						if (strategyPortfolioDefaultPlans.containsKey(planType)) {
							defaultPlans = strategyPortfolioDefaultPlans.get(planType);
						} else if (strategyPortfolioDefaultPlans.containsKey(mapPlanType)) {
							defaultPlans = strategyPortfolioDefaultPlans.get(mapPlanType);
						}
						if (defaultPlans != null) {
							for (Map.Entry<String, String> entry : defaultPlans.entrySet()) {
								String defaultPlan = entry.getValue();

								// Check first if defaultPlan is in the costMap
								if (strategyGroupPlanCostMap.containsKey(strategyId, groupId, mapPlanType, defaultPlan,
										coverageLevel)) {
									returnPlanAndType[PLAN_INDEX] = defaultPlan;
									returnPlanAndType[PLAN_TYPE_INDEX] = mapPlanType;
									returnPlanAndType[MAP_REASON_INDEX] = BSSApplicationConstants.MAP_REASON_CLIENT;
									strategyGroupPlanMapping.put(strategyId, groupId, planType, plan,
											returnPlanAndType);
									foundPlan = true;
									break;
								}

								// Otherwise, see if the er->ee mapping of the
								// defaultPlan is in the costMap
								for (Map.Entry<BenefitPlan, BenefitPlan> erEeLoopEntry : erEePlanMapping.entrySet()) {
									BenefitPlan oldLoopBenefitPlan = erEeLoopEntry.getKey();
									BenefitPlan newLoopBenefitPlan = erEeLoopEntry.getValue();
									if (defaultPlan.equals(oldLoopBenefitPlan.getId())) {
										String loopPlanId = newLoopBenefitPlan.getId();
										String loopPlanType = newLoopBenefitPlan.getPlanType();
										if (strategyGroupPlanCostMap.containsKey(strategyId, groupId, loopPlanType,
												loopPlanId, coverageLevel)) {
											returnPlanAndType[PLAN_INDEX] = loopPlanId;
											returnPlanAndType[PLAN_TYPE_INDEX] = loopPlanType;
											returnPlanAndType[MAP_REASON_INDEX] = BSSApplicationConstants.MAP_REASON_CLIENT;
											strategyGroupPlanMapping.put(strategyId, groupId, planType, plan,
													returnPlanAndType);
											foundPlan = true;
											break;
										}
									}
								}
								if (foundPlan) {
									break;
								}
							}
						}
						if (foundPlan) {
							break;
						}
					}
					// Otherwise, reset return values
					else {
						returnPlanAndType[PLAN_INDEX] = "";
						returnPlanAndType[PLAN_TYPE_INDEX] = "";
						returnPlanAndType[MAP_REASON_INDEX] = "";
					}
				}
			}
		}

		// If we found the plan and it is not empty
		if (foundPlan && !("").equals(returnPlanAndType[PLAN_INDEX])
				&& !("").equals(returnPlanAndType[PLAN_TYPE_INDEX])) {
			StrategyGroupPlanRateData returnData = (StrategyGroupPlanRateData) strategyGroupPlanCostMap.get(strategyId,
					groupId, returnPlanAndType[PLAN_TYPE_INDEX], returnPlanAndType[PLAN_INDEX], coverageLevel);
			returnData.setMapReason(returnPlanAndType[MAP_REASON_INDEX]);
			return returnData;
		}
		// Otherwise, if we haven't found a plan, put the empty array in the
		// mapping so we do not perform the logic again
		// Return null
		else {
			if (!foundPlan) {
				strategyGroupPlanMapping.put(strategyId, groupId, planType, plan, returnPlanAndType);
			}
			return null;
		}
	}

	/**
	 * This method returns a specific plan type the client offers for this
	 * strategy/group combination based on the passed in plan type.
	 * 
	 * The strategyProgramPlanTypeOfferingMap parameter is a map of strategyId,
	 * benefitProgram, genericPlanType, planType
	 * 
	 * @param strategyId
	 * @param benefitProgram
	 * @param planType
	 * @param strategyProgramPlanTypeOfferingMap
	 * @return String of planType
	 */
	private String clientOfferedPlanType(Long strategyId, String benefitProgram, String planType,
			MultiKeyMap strategyProgramPlanTypeOfferingMap) {

		String genericPlanType = Utils.getGenericPlanTypeCode(planType);
		return (String) strategyProgramPlanTypeOfferingMap.get(strategyId, benefitProgram, genericPlanType);

	}
	
	/**
	 * Create the return map of employees, strategies and plans for the current strategy
	 * @param company
	 * @param currentStrategyId
	 * @return
	 */
	private List<Object[]> getCurrentEmployeePlanData(Company company, Long currentStrategyId) {
		List<Object[]> employeeStrategyPlanData;
		boolean isVendorMappingOn = RulesAndConfigsUtils.isVendorMappingOn(company.getRealmPlanYear().getId());
		if (isVendorMappingOn) {
			Date effDt = company.getRealmPlanYear().getPlanYearEnd();
			if (DateTimeComparator.getDateOnlyInstance().compare(company.getRealmPlanYear().getPlanYearStart(),
					null) > 0) {
				RealmPlanYear prevRealmYear = realmPlanYearService.getPreviousRealmPlanYear(company.getCode(),
						company.getRealmPlanYearId());
				effDt = prevRealmYear.getPlanYearEnd();
			}
			employeeStrategyPlanData = employeeDataDao.getEmployeeCensusStrategyPlanData(company, currentStrategyId,
					effDt);
		} else {
			employeeStrategyPlanData = employeeDataDao.getEmployeeStrategyPlanData(company, currentStrategyId);
		}
		return employeeStrategyPlanData;
	}

	/*
	 * This method has been transplanted here from the EmployeeDataDaoImpl class.  This method is really a service
	 * method and requires additional data from this service class to properly complete its task.
	 */
	private List<EmployeeStrategyData> processEmployeeStrategyPlanData(Company company, Long currentStrategyId,
			RealmPlanYear previousRealmPlanYear, List<Object[]> data, Map<Long,Map<String,GroupFunding>> strategyGroupMap ) {

		Set<BigDecimal> realmPlanYrIds = new HashSet<>(Arrays.asList(BigDecimal.valueOf(company.getRealmPlanYearId()),
				BigDecimal.valueOf(previousRealmPlanYear.getId())));
		List<XbssRealmPlyrPlan> realmPlyrPlans = xbssRealmPlyrPlanDao.findByRealmYearIdInAndPlanTypeInOrderByRealmYearId(
				realmPlanYrIds, BSSApplicationConstants.PRIMARY_PLAN_TYPES);
		Map<String, EmployeeBenefitGroup> benefitGroupMap = employeeBenefitGroupDao.getBenefitProgramDetails(company);

		Map<String, Long> planPortfolioMap = mapPlansToPortfolioId( realmPlyrPlans );

		// this is the main map with all the employee and plan cost data for each client strategy
		Map<String, EmployeeStrategyData> empStrategyPlanMap = new HashMap<>();
		// Basic employee data and a set of strategies
		EmployeeStrategyData empStrategyData;
		// plan data for one strategy
		EmployeeStrategyPlanData empStrategyPlanData;

		if (!CollectionUtils.isEmpty(data)) {
			for (Object[] empPlan : data) {
				String emplId = empPlan[0].toString();
				String emplFirstName = empPlan[1].toString().trim();
				String emplMiddleName = empPlan[2].toString().trim();
				String emplLastName = empPlan[3].toString().trim();
				String deptId = empPlan[4].toString();
				String deptDescr = empPlan[5].toString();
				String location = empPlan[6].toString();
				String locationDescription = empPlan[7].toString();
				String benefitProgram = empPlan[8].toString();
				String planType = (String) empPlan[9];
				String benefitPlan = (String) empPlan[10];
				String planName = (String) empPlan[11];
				String cvrgCode = (String) empPlan[12];
				String coverageElect = empPlan[13].toString();

				Object[] group = lookupGroupByStrategyId( strategyGroupMap, currentStrategyId, benefitProgram );
				Long currentGroupId = benefitGroupMap.get(benefitProgram).getBenefitGroupId();
				String currentGroupName = (String) group[1];

				// If this employee is not in the main map, add it
				if (!empStrategyPlanMap.containsKey(emplId)) {
					// create object that holds all employee/strategy/plan data
					empStrategyData = new EmployeeStrategyData();
					empStrategyData.setEmplId(emplId);
					empStrategyData.setEmplFirstName(emplFirstName);
					empStrategyData.setEmplMiddleName(emplMiddleName);
					empStrategyData.setEmplLastName(emplLastName);
					empStrategyData.setDeptId(deptId);
					empStrategyData.setDeptName(deptDescr);
					empStrategyData.setLocationId(location);
					empStrategyData.setLocationName(locationDescription);
					empStrategyData.setCurrentGroupId( new BigDecimal(currentGroupId) );
					empStrategyData.setCurrentGroupName(currentGroupName);
					empStrategyPlanMap.put(emplId, empStrategyData);

					empStrategyPlanData = new EmployeeStrategyPlanData();
					empStrategyPlanData.setStrategyId(currentStrategyId);
					empStrategyPlanData.setGroupId(currentGroupId);
					empStrategyPlanData.setGroupName(currentGroupName);
					empStrategyPlanData.setBenefitProgram(benefitProgram);
					empStrategyData.getStrategyDetails().add(empStrategyPlanData);

				} else {
					empStrategyData = empStrategyPlanMap.get(emplId);
					empStrategyPlanData = empStrategyData.getStrategyDetails().get(0);
				}
				// object with plan and rates
				if (planName != null || ("W").equals(coverageElect)) {
					String coverageLevelName = determineCoverageLevelName( cvrgCode );
					// the plan info with rates
					BenefitPlanRateData empPlanAndRates = new BenefitPlanRateData(benefitPlan, planType, planName, cvrgCode,
							coverageLevelName, null, null, planPortfolioMap.get(benefitPlan), coverageElect, null,
							false, true);
					empStrategyPlanData.getBenefitPlans().add(empPlanAndRates);
				}
			}
		}
		return new ArrayList<>(empStrategyPlanMap.values());
	}

	private Map<String,Long> mapPlansToPortfolioId( List<XbssRealmPlyrPlan> plyrPlans ) {
		Map<String,Long> planPortfolioMap = new HashMap<>();
			for( XbssRealmPlyrPlan xbssRealmPlyrPlan : plyrPlans ) {
				planPortfolioMap.put(xbssRealmPlyrPlan.getBenefitPlan(), xbssRealmPlyrPlan.getPortfolioId().longValue());
			}
		return planPortfolioMap;
	}

	private String determineCoverageLevelName( String covrgCd ) {
		String coverageLevelName;
		if( covrgCd == null || "0".equals(covrgCd) ) {
			coverageLevelName = null;
		} else {
			coverageLevelName = CoverageCodesEnums.valueOfName(covrgCd);
		}
		return coverageLevelName;
	}

	private Object[] lookupGroupByStrategyId( Map<Long,Map<String,GroupFunding>> strategyGroupMap,
			Long currentStrategyId, String benefitProgram ) {
		Object[] groupDetails = new Object[2];
		Map<String,GroupFunding> map = strategyGroupMap.get( currentStrategyId );
		if( map != null ) {
			GroupFunding group = map.get( benefitProgram );
			if( group != null ) {
				groupDetails[0] = group.getId();
				groupDetails[1] = group.getName();
			}
		}
		return groupDetails;
	}

	private Set<EmployeeData> getEmployeeDataForProspectTnStrategy(long strategyId, String companyCode) {
		Set<Employee> employees = employeeDataDao.getEmployeeGroupDetailsByStrategy(strategyId);
		List<CensusRes> censusResponse = prospectEmployeeService.getEmployees(companyCode);
		Map<String, String> employeeIdToNameMap = censusResponse.stream()
				.collect(Collectors.toMap(CensusRes::getEmployeeId, CensusRes::getEmployeeName));
		Set<EmployeeData> employeeDataSet = new HashSet<>();
		employees.stream().forEach(employee -> {
			EmployeeData employeeData = new EmployeeData();
			employeeData.setEmplId(employee.getEmplId());
			employeeData.setEmplName(employeeIdToNameMap.get(employee.getEmplId()));
			employeeData.setBenefitProgram(employee.getUpdatedBenefitProgram());
			employeeData.setBenefitGroupName(employee.getBenefitGroupName());
			employeeData.setBenefitGroupId(employee.getBenefitGroupId());
			employeeData.setStrategyGroupId(employee.getStrategyGroupId());
			employeeDataSet.add(employeeData);
		});
		return employeeDataSet;
	}
	
	private Set<EmployeeData> getEmployeeDataForClientStrategy(long strategyId, Company company) {
		Set<EmployeeData> employeesData = new HashSet<>();
        Map<String, Employee> employeeDetails = null;
        if (company.isProspectConvertedOnboardingClient()) {
            employeeDetails = bssCoreServiceClient.getCensusByCode(company.getCode());
        } else {
            employeeDetails = employeeDataDao.getEmployeesByCompany(company);
        }
		Set<Employee> bssEmployees = employeeDataDao.getEmployeeGroupDetailsByStrategy(strategyId);
		for (Employee bssEmployee : bssEmployees) {
			EmployeeData employeeData = new EmployeeData();
			employeeData.setBenefitGroupId(bssEmployee.getBenefitGroupId());
			employeeData.setBenefitGroupName(bssEmployee.getBenefitGroupName());
			employeeData.setBenefitProgram(bssEmployee.getUpdatedBenefitProgram());
			employeeData.setEmplId(bssEmployee.getEmplId());
			employeeData.setEmplRcd(bssEmployee.getEmplRcd());
			employeeData.setStrategyGroupId(bssEmployee.getStrategyGroupId());
			Employee employee = employeeDetails.get(bssEmployee.getEmplId());

			if (null != employee) {
				employeeData.setDepartment(employee.getDepartment());
				employeeData.setEmplName(employee.getEmplName());
				employeeData.setJobTitle(employee.getJobTitle());
				employeeData.setLocation(employee.getLocation());
                employeeData.setK1(employee.isK1());
				employeesData.add(employeeData);
			}
		}
		return employeesData;
	}

}