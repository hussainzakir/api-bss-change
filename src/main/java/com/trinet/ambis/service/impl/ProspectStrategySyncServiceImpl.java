package com.trinet.ambis.service.impl;

import static com.trinet.ambis.common.BSSApplicationConstants.CLIENT_MA_GROUP_NAME;
import static com.trinet.ambis.common.BSSApplicationConstants.DENTAL_PLAN_TYPE;
import static com.trinet.ambis.common.BSSApplicationConstants.EMPTY_PLAN_TYPES;
import static com.trinet.ambis.common.BSSApplicationConstants.MEDICAL_PLAN_TYPE;
import static com.trinet.ambis.common.BSSApplicationConstants.PRIMARY_PLAN_TYPES;
import static com.trinet.ambis.common.BSSApplicationConstants.VISION_PLAN_TYPE;

import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.US;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EePlanAssignmentDao;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.TibRateService;

import java.util.Collections;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.trinet.ambis.service.ProspectCompanyService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.model.GroupData;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CacheObjectLevelEnum;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.dto.CompanyStrategyDetailsDto;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.EmplDefaultPlanAssignmentService;
import com.trinet.ambis.service.EmployeeBenefitGroupService;
import com.trinet.ambis.service.EmployeePlanAssignmentService;
import com.trinet.ambis.service.EmployeeStrategyGroupService;
import com.trinet.ambis.service.ProspectDefaultPlanAssignmentService;
import com.trinet.ambis.service.ProspectDefaultPlanMappingService;
import com.trinet.ambis.service.ProspectStrategySyncService;
import com.trinet.ambis.service.DefaultPlanMappingService;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.model.EmployeeCensusStrategyGroupDetails;
import com.trinet.ambis.service.model.ProspectStrategySyncData;
import com.trinet.ambis.service.model.StrategyGroupDetails;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;


@Service
public class ProspectStrategySyncServiceImpl implements ProspectStrategySyncService {

	private static final Logger logger = LoggerFactory.getLogger(ProspectStrategySyncServiceImpl.class);

	@Autowired
	EmployeeBenefitGroupService employeeBenefitGroupService;

	@Autowired
	EmployeeBenefitGroupDao employeeBenefitGroupDao;

	@Autowired
	EmployeeStrategyGroupService employeeStrategyGroupService;

	@Autowired
	EmployeePlanAssignmentService employeePlanAssignmentService;

	@Autowired
	EmplDefaultPlanAssignmentService emplDefaultPlanAssignmentService;

	@Autowired
	ProspectDefaultPlanMappingService prospectDefaultPlanMappingService;

	@Autowired
	DefaultPlanMappingService planMappingAssignmentService;

	@Autowired
	ProspectDefaultPlanAssignmentService prospectDefaultPlanAssignmentService;

	@Autowired
	CompanyService companyService;

	@Autowired
	ProspectCompanyService prospectCompanyService;

	@Autowired
	StrategyService strategyService;

	@Autowired
	PortfolioRuleDao portfolioRuleDao;
	
	@Autowired
	CacheService cacheService;

	@Autowired
	TibRateService tibRateService;

	@Autowired
	EePlanAssignmentDao eePlanAssignmentDao;

	@Autowired
	PlanSelectionService planSelectionService;

    @Autowired
    BenefitGroupService benefitGroupService;

	@Override
	@Transactional
	public void handleCensusChangeEvent(List<ProspectStrategySyncData> prospectStrategySyncData, String companyCode) {
		cacheService.invalidateCache(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES.getObjectType(),
				CacheObjectLevelEnum.COMPANY.getObjectLevel(), companyCode);
		Map<String, List<EmployeeCensusStrategyGroupDetails>> employeeStrategyGroupDetails = employeeBenefitGroupDao
				.getEmployeeStrategyGroupDetails(companyCode);
		Map<Long, List<EmployeeCensusStrategyGroupDetails>> strategyGroupDetails = employeeBenefitGroupDao
				.getStartegyGroupByCompanyAndStrategy(companyCode).stream()
				.collect(Collectors.groupingBy(EmployeeCensusStrategyGroupDetails::getStrategyId));
		Set<String> employeeIds = new HashSet<>();
		Map<String, Set<StrategyGroupDetails>> employeeStrategyGroups = new HashMap<>();
		if (MapUtils.isNotEmpty(employeeStrategyGroupDetails)) {
			prospectStrategySyncData.stream().forEach(employee -> {
				String employeeId = employee.getEmployeeId();
                if (ifEmplTypeGroupMismatchFound(employeeStrategyGroupDetails, employee) ||
                        ifEmployeeHomeStateMismatchFound(employeeStrategyGroupDetails,employee)) {
					Set<StrategyGroupDetails> strategyGroupDetailsSet = new HashSet<>();
					Set<Long> strategyIds = strategyGroupDetails.keySet();
					strategyIds.stream().forEach(strategyId -> {
						long strategyGroupId = 0;
                        boolean isMAGroupPresent = strategyGroupDetails.get(strategyId).stream()
                                .anyMatch(strategyGroupDetail-> CLIENT_MA_GROUP_NAME.equals(strategyGroupDetail.getGroupDesc()));
						if (employee.isK1()) {
							strategyGroupId = getStrategyGroupIdBy(strategyGroupDetails, strategyId,
									BSSApplicationConstants.K1_GROUP_TYPE);
                        } else if(isMAGroupPresent && US.MASSACHUSETTS.getANSIabbreviation().equals(employee.getHomeState())){
                            strategyGroupId = getStrategyGroupIdForMAGroup(strategyGroupDetails, strategyId);
                        } else {
							strategyGroupId = getStrategyGroupIdBy(strategyGroupDetails, strategyId,
									BSSApplicationConstants.STD);
						}
						StrategyGroupDetails newStrategyGroupDetails = new StrategyGroupDetails();
						newStrategyGroupDetails.setStrategyGroupId(strategyGroupId);
						strategyGroupDetailsSet.add(newStrategyGroupDetails);
					});
					if (CollectionUtils.isNotEmpty(strategyGroupDetailsSet)) {
						employeeStrategyGroups.put(employeeId, strategyGroupDetailsSet);
					}
				}
				employeeIds.add(employeeId);
			});
            syncStrategyData(prospectStrategySyncData, companyCode, employeeStrategyGroups,getCompanies(companyCode));
		}
	}

	@Override
	@Transactional
	public void handleCensusDeleteEvent(List<String> employeeIds, String companyCode) {
		employeeStrategyGroupService.deleteEmployeeStrategyGroups(employeeIds, companyCode);
		employeePlanAssignmentService.deleteEmployeePlanAssignment(employeeIds, companyCode);
		emplDefaultPlanAssignmentService.deleteEmplDefaultPlanAssignment(employeeIds, companyCode);
		if (!CompanyServiceHelper.isClientCompanyPattern(companyCode)) {
			deleteOmsPlanSelections(companyCode, employeeIds);
			cacheService.invalidateCache(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES.getObjectType(),
					CacheObjectLevelEnum.COMPANY.getObjectLevel(), companyCode);
		}
		if (CompanyServiceHelper.isClientCompanyPattern(companyCode)) {
			Company company = companyService.getCompanyDetails(companyCode);
			cacheService.invalidateStrategyDataCache(company);
		}

	}

	@Override
	@Transactional
	public void handleCensusAddEvent(List<ProspectStrategySyncData> prospectStrategySyncData, String companyCode) {

        List<EmployeeCensusStrategyGroupDetails> existingStrategyGroups = employeeBenefitGroupDao
                .getStartegyGroupByCompanyAndStrategy(companyCode);
        if (CollectionUtils.isNotEmpty(existingStrategyGroups)) {

            List<Company> companies = getCompanies(companyCode);
            boolean enableDefaultMAGroupCreation = AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled();
            if(enableDefaultMAGroupCreation && checkIfW2MAEmployeeExists(prospectStrategySyncData, companyCode))
            {
                boolean maGroupMissing = existingStrategyGroups.stream()
                        .noneMatch(group -> BSSApplicationConstants.CLIENT_MA_GROUP_NAME.equals(group.getGroupDesc()));
                if (maGroupMissing) {
                    createW2MAGroupForExistingStrategies(companies);
                    // Refresh strategy groups after creating new MA group
                    existingStrategyGroups = employeeBenefitGroupDao.getStartegyGroupByCompanyAndStrategy(companyCode);
                }
            }
            Map<String, List<EmployeeCensusStrategyGroupDetails>> existingGroupTypeToStrategyGroupDetailsMap = createGroupTypeDescToStrategyGroupIds(
                    existingStrategyGroups);
            Map<String, Set<StrategyGroupDetails>> employeeStrategyGroups = new HashMap<>();
            prospectStrategySyncData.stream().forEach(prospectEmployee -> {
                String groupType = null;
                if (CompanyServiceHelper.isClientCompanyPattern(companyCode)) {
                    groupType = BSSApplicationConstants.STD_GROUP_TYPE;
                } else {
                    groupType = assignGroupToProspectCensus(prospectEmployee);
                }
                String employeeId = prospectEmployee.getEmployeeId();
                if (existingGroupTypeToStrategyGroupDetailsMap.containsKey(groupType)) {
                    List<EmployeeCensusStrategyGroupDetails> existingStrategyDetailsList = existingGroupTypeToStrategyGroupDetailsMap
                            .get(groupType);
                    for (EmployeeCensusStrategyGroupDetails strategy : existingStrategyDetailsList) {
                        StrategyGroupDetails strategyGroupDetails = new StrategyGroupDetails();
                        strategyGroupDetails.setStrategyGroupId(strategy.getStrategyGroupId());
                        employeeStrategyGroups.putIfAbsent(employeeId, new HashSet<>());
                        employeeStrategyGroups.get(employeeId).add(strategyGroupDetails);
                    }
                }
            });
            syncStrategyData(prospectStrategySyncData, companyCode, employeeStrategyGroups,companies);
        }
        cacheService.invalidateCache(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES.getObjectType(),
                CacheObjectLevelEnum.COMPANY.getObjectLevel(), companyCode);

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void strategySyncOnHQLocationChange(String companyCode) {
		Map<Long, CompanyStrategyDetailsDto> companyStrategyDetailsDtos = companyService
				.getCompanyStrategyDetails(companyCode);
		companyStrategyDetailsDtos.forEach((companyId, companyStrategyDetailsDto) -> {
			if (CollectionUtils.isNotEmpty(companyStrategyDetailsDto.getAllStrategyIds())) {
				Company company = prospectCompanyService.getProspectCompanyDetails(companyCode,
						companyStrategyDetailsDto.getRealmPlanYearId());
				if (null != company && !CompanyServiceHelper.isTNXIExchange(company)) {
					strategyService.resetStrategiesBy(companyCode, companyId,
							companyStrategyDetailsDto.getRealmPlanYearId(),
							companyStrategyDetailsDto.getAllStrategyIds());
				}
			}
		});
		cacheService.invalidateCache(CacheObjectTypeEnum.ALL.getObjectType(),
				CacheObjectLevelEnum.COMPANY.getObjectLevel(), companyCode);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void resetStrategiesBy(Company company, long realmPlanYearId) {
		String companyCode = company.getCode();
		List<Strategy> allStrategies = strategyService.getAllStrategies(company.getId());
		if (CollectionUtils.isNotEmpty(allStrategies)) {
			Set<Long> strategyIds = allStrategies.stream().map(Strategy::getId).collect(Collectors.toSet());
			strategyService.resetStrategiesBy(companyCode, company.getId(), realmPlanYearId, strategyIds);
		}
		cacheService.invalidateCache(CacheObjectTypeEnum.ALL.getObjectType(),
				CacheObjectLevelEnum.COMPANY.getObjectLevel(), companyCode);
	}

	@Override
	@Transactional
	public void rateSyncOnDependentChange(List<String> employeeIds, String companyCode) {
		//Get the list of BSS companies associated with the given company code
		List<Company> bssCompanies = companyService.getXbssCompaniesByCode(companyCode);

		Set<String> employeeIdSet = employeeIds.stream().collect(Collectors.toSet());
		bssCompanies.forEach(bssCompany -> {
			Company company = prospectCompanyService.getProspectCompanyDetails(companyCode, bssCompany.getRealmPlanYearId());
			if (CompanyServiceHelper.isTibProspect(company)) {
				// If the company is a TIB prospect, sync the rates for the employees on dependent change
				List<Strategy> strategies = strategyService.getAllStrategies(company.getId());
				if (CollectionUtils.isNotEmpty(strategies)) {
					strategies.forEach(strategy -> tibRateService.saveRatesPerEmployeeIds(company, strategy.getId(),
							employeeIdSet));
				}
			} else {
				logger.error("TIB Rate Sync is not applicable for company: {}", companyCode);
			}
		});
	}

    @Override
    @Transactional
    public void rateSyncOnCensusDependentChange(List<ProspectCensusResponse> prospectCensusResponseList, String companyCode) {
        //Get the list of BSS companies associated with the given company code
        List<Company> bssCompanies = companyService.getXbssCompaniesByCode(companyCode);
        bssCompanies.forEach(bssCompany -> {
            Company company = prospectCompanyService.getProspectCompanyDetails(companyCode, bssCompany.getRealmPlanYearId());
            if (CompanyServiceHelper.isTibProspect(company)) {
                // If the company is a TIB prospect, sync the rates for the employees on dependent change
                List<Strategy> strategies = strategyService.getAllStrategies(company.getId());
                if (CollectionUtils.isNotEmpty(strategies)) {
                    strategies.forEach(strategy -> tibRateService.saveRatesPerEmployee(company, strategy.getId(), prospectCensusResponseList));
                }
            } else {
                logger.error("TIB Rate Sync is not applicable for company: {}", companyCode);
            }
        });
    }

	private void syncStrategyData(List<ProspectStrategySyncData> prospectStrategySyncData, String companyCode,
                                   Map<String, Set<StrategyGroupDetails>> employeeStrategyGroups,List<Company> companies) {
		updateEmployeeStrategyGroups(employeeStrategyGroups);
		Set<String> locChangeEmployeeIds = getEmlpIdsForLocationChange(prospectStrategySyncData);
		Set<String> noLocChangeEmployeeIds = getNoLocationChangeEmplIds(prospectStrategySyncData);
		Set<String> employeeIds = getAllEmplIds(prospectStrategySyncData);
		deleteEmplDefaultPlanMapping(SetUtils.union(locChangeEmployeeIds, noLocChangeEmployeeIds));
        List<ProspectCensusResponse> prospectCensusResponse = getProspectCensusDetails(prospectStrategySyncData);
		
		companies.forEach(company -> {
			updateEmplDefaultPlanMapping(company, prospectCensusResponse, employeeIds);
			updateEmplPlanAssignments(company, locChangeEmployeeIds);
			updateEmplPlanAssignmentsForMissingEmployees(company, noLocChangeEmployeeIds);
			updateEmplPlanAssignmentCvgCode(company, noLocChangeEmployeeIds, prospectCensusResponse);
			updateEmplPlanAssignmentRates(company, employeeIds);
			cacheService.invalidateStrategyDataCache(company);
		});
	}

	private Set<String> getEmlpIdsForLocationChange(List<ProspectStrategySyncData> prospectStrategySyncData) {
		return prospectStrategySyncData.stream().filter(ProspectStrategySyncData::isLocationChanged)
				.map(ProspectStrategySyncData::getEmployeeId).collect(Collectors.toSet());
	}
	
	private Set<String> getNoLocationChangeEmplIds(List<ProspectStrategySyncData> prospectStrategySyncData) {
		return prospectStrategySyncData.stream().filter(data -> !data.isLocationChanged())
				.map(ProspectStrategySyncData::getEmployeeId).collect(Collectors.toSet());
	}

	private Set<String> getAllEmplIds(List<ProspectStrategySyncData> prospectStrategySyncData) {
		return prospectStrategySyncData.stream().map(ProspectStrategySyncData::getEmployeeId).collect(Collectors.toSet());
	}

	private boolean ifEmplTypeGroupMismatchFound(
			Map<String, List<EmployeeCensusStrategyGroupDetails>> employeeStrategyGroupDetails,
			ProspectStrategySyncData employee) {
		return (employee.isK1() && employeeStrategyGroupDetails.get(employee.getEmployeeId()).stream()
				.anyMatch(emp -> !BSSApplicationConstants.K1_GROUP_TYPE.equals(emp.getGroupType())))
				|| (!employee.isK1() && employeeStrategyGroupDetails.get(employee.getEmployeeId()).stream()
						.anyMatch(emp -> BSSApplicationConstants.K1_GROUP_TYPE.equals(emp.getGroupType())));
	}

	private long getStrategyGroupIdBy(Map<Long, List<EmployeeCensusStrategyGroupDetails>> strategyGroupDetails,
			Long strategyId, String groupType) {
		long strategyGroupId;
		strategyGroupId = strategyGroupDetails.get(strategyId).stream()
				.filter(strategyGroupDetail -> groupType.equals(strategyGroupDetail.getGroupType()))
				.mapToLong(EmployeeCensusStrategyGroupDetails::getStrategyGroupId).findAny().getAsLong();
		return strategyGroupId;
	}

	private void updateEmployeeStrategyGroups(Map<String, Set<StrategyGroupDetails>> employeeStrategyGroups) {
		Set<String> employeeIds = employeeStrategyGroups.keySet();
		if (CollectionUtils.isNotEmpty(employeeIds)) {
			employeeBenefitGroupDao.deleteEmployeeStrategyGroups(employeeIds);
			logger.info("Deleted employee strategy groups for {} employees", employeeIds.size());
			employeeBenefitGroupService.insertNewEmployeeStrategyGroups(employeeStrategyGroups);
			logger.info("Added employee strategy groups for {} employees", employeeStrategyGroups.keySet().size());
		}
	}

	private void updateEmplDefaultPlanMapping(Company company, List<ProspectCensusResponse> prospectCensusResponse, Set<String> employeeIds) {
		if (CollectionUtils.isNotEmpty(prospectCensusResponse)) {
			List<Strategy> strategies = strategyService.getAllStrategies(company.getId());
			// BNFT-51157 - If no strategies are available for the company, skip creating records in the xbss_ee_default_plan_assignment table.
			if (Objects.nonNull(strategies) && !strategies.isEmpty()) {
				boolean planMappingServiceEnabled =
						RulesAndConfigsUtils.isPlanMappingServiceEnabled(company.getRealmPlanYear().getId());
				if (planMappingServiceEnabled && company.isProspectCompany()) {
					planMappingAssignmentService.callPlanMappingService(company, employeeIds);
				} else {
					prospectDefaultPlanMappingService.createCensusDefaultRegionalPlanMapping(company, prospectCensusResponse);
				}
			}
		}
	}

	private List<ProspectCensusResponse> getProspectCensusDetails(
			List<ProspectStrategySyncData> prospectStrategySyncData) {
		return prospectStrategySyncData.stream().map(prospectData -> {
			ProspectCensusResponse response = new ProspectCensusResponse();
			response.setEmployeeId(prospectData.getEmployeeId());
			response.setState(prospectData.getHomeState());
			response.setZip(prospectData.getHomePostalCode());
			prospectData.getEnrolledCvgCodes().stream().forEach(ecc -> {
				if (MEDICAL_PLAN_TYPE.equals(ecc.getBenefitType())) {
					response.setMedicalTier(ecc.getDesiredCvgCode());
				}
				if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(ecc.getBenefitType())) {
					response.setDentalTier(ecc.getDesiredCvgCode());
				}
				if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(ecc.getBenefitType())) {
					response.setVisionTier(ecc.getDesiredCvgCode());
				}
			});
			return response;
		}).collect(Collectors.toList());
	}

	private void updateEmplPlanAssignments(Company company, Set<String> locChangeEmployeeIds) {
		if(CollectionUtils.isNotEmpty(locChangeEmployeeIds)) {
			List<Strategy> strategies = strategyService.getAllStrategies(company.getId());
			Set<String> benTypes = new HashSet<>(
					CompanyServiceHelper.isTibProspect(company) ? EMPTY_PLAN_TYPES
							: PRIMARY_PLAN_TYPES);
			strategies.forEach(strategy -> {
				Map<Long, Boolean> existingMedStrategyPortfolioMap = portfolioRuleDao.getMedicalPortfoliosBy(
						strategy.getId(), company.getRealmPlanYearId(), company.getHeadQuatersState());
				prospectDefaultPlanAssignmentService.assignDefaultPlanBy(locChangeEmployeeIds, strategy.getId(),
						existingMedStrategyPortfolioMap, benTypes);
			});
		}
	}

	private void updateEmplPlanAssignmentsForMissingEmployees(Company company, Set<String> noLocChangeEmployeeIds) {
		if (CollectionUtils.isNotEmpty(noLocChangeEmployeeIds)) {
			List<Strategy> strategies = strategyService.getAllStrategies(company.getId());
			strategies.forEach(strategy -> {
				Map<Long, Boolean> existingMedStrategyPortfolioMap = portfolioRuleDao.getMedicalPortfoliosBy(
						strategy.getId(), company.getRealmPlanYearId(), company.getHeadQuatersState());
				prospectDefaultPlanAssignmentService.assignDefaultPlanForMissingEmployees(noLocChangeEmployeeIds,
						strategy.getId(), company.getId(), existingMedStrategyPortfolioMap,
						CompanyServiceHelper.isTibProspect(company));
			});
		}
	}

    private void updateEmplPlanAssignmentCvgCode(Company company, Set<String> noLocChangeEmployeeIds,
                                                 List<ProspectCensusResponse> prospectCensusResponse) {
        if (CollectionUtils.isNotEmpty(noLocChangeEmployeeIds)) {
            if (!CompanyServiceHelper.isTibProspect(company)) {
                employeePlanAssignmentService.updateEePlanAssignmentCvgCode(noLocChangeEmployeeIds);
            }
            updateEmplPlanAssignmentCvgCodeForOms(company, noLocChangeEmployeeIds, prospectCensusResponse);
        }
    }

	private void updateEmplPlanAssignmentCvgCodeForOms(Company company,
			Set<String> noLocChangeEmployeeIds,
			List<ProspectCensusResponse> prospectCensusResponse) {
		if (CompanyServiceHelper.isTibProspect(company) && CollectionUtils.isNotEmpty(
				prospectCensusResponse)) {
			List<Strategy> strategies = strategyService.getAllStrategies(company.getId());

			if (CollectionUtils.isNotEmpty(strategies)) {
                List<EePlanAssignment> employeePlanAssignmentsToInsert = new ArrayList<>();
                List<EePlanAssignment> employeePlanAssignmentsToDelete = new ArrayList<>();

				// Build map of empId to medical,dental and vision tier
				Map<String, Map<String, String>> empIdToTierMap = getEmpIdToTierMap(
						prospectCensusResponse);
				strategies.forEach(strategy -> {
					List<EePlanAssignment> employeePlanAssignments = employeePlanAssignmentService.getEmployeePlanAssigmentBy(
							List.of(strategy.getId()));

					employeePlanAssignments = employeePlanAssignments.stream()
							.filter(assignment -> noLocChangeEmployeeIds.contains(
									assignment.getEePlanAssignmentPK().getEmplId()))
							.map(assignment -> setCovrgCD(assignment, empIdToTierMap))
							.filter(Objects::nonNull)
							.collect(Collectors.toList());

                    // If the coverage is now waived, delete the assignment
                    List<EePlanAssignment> employeeAssignmentsWaived = employeePlanAssignments.stream()
                            .filter(assignment -> ProspectConstants.WAVED_COVERAGE.equalsIgnoreCase(assignment.getCovrgCD()))
                            .collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(employeeAssignmentsWaived)) {
                        employeePlanAssignmentsToDelete.addAll(employeeAssignmentsWaived);
                        employeePlanAssignments.removeAll(employeeAssignmentsWaived);
                    }
                    employeePlanAssignmentsToInsert.addAll(employeePlanAssignments);

				});

                if (CollectionUtils.isNotEmpty(employeePlanAssignmentsToDelete)) {
                    eePlanAssignmentDao.deleteAll(employeePlanAssignmentsToDelete);
                }
                if(CollectionUtils.isNotEmpty(employeePlanAssignmentsToInsert)) {
                    eePlanAssignmentDao.saveAll(employeePlanAssignmentsToInsert);
                }
			}

		}
	}

	private Map<String, Map<String, String>> getEmpIdToTierMap(
			List<ProspectCensusResponse> prospectCensusResponse) {
		return prospectCensusResponse.stream()
				.filter(prospect -> prospect.getEmployeeId() != null)
				.collect(Collectors.toMap(ProspectCensusResponse::getEmployeeId,
						response -> {
							Map<String, String> tierMap = new HashMap<>();
							if (response.getMedicalTier() != null) {
								tierMap.put(MEDICAL_PLAN_TYPE, response.getMedicalTier());
							}
							if (response.getDentalTier() != null) {
								tierMap.put(DENTAL_PLAN_TYPE, response.getDentalTier());
							}
							if (response.getVisionTier() != null) {
								tierMap.put(VISION_PLAN_TYPE, response.getVisionTier());
							}
							return tierMap;
						}));
	}

	private EePlanAssignment setCovrgCD(EePlanAssignment assignment,
			Map<String, Map<String, String>> empIdToTierMap) {
		Map<String, String> tierMap = empIdToTierMap.get(
				assignment.getEePlanAssignmentPK().getEmplId());
		if (MapUtils.isNotEmpty(tierMap)) {
			String benefitType = assignment.getEePlanAssignmentPK().getBenefitType();
			String covrgCode = tierMap.get(benefitType);
			if (StringUtils.isNotBlank(covrgCode)) {
				assignment.setCovrgCD(covrgCode);
				return assignment;
			}
		}
		return null;
	}

	private void updateEmplPlanAssignmentRates(Company company, Set<String> employeeIds) {
		if(CollectionUtils.isNotEmpty(employeeIds) && CompanyServiceHelper.isTibProspect(company)) {
			List<Strategy> strategies = strategyService.getAllStrategies(company.getId());
			if (CollectionUtils.isNotEmpty(strategies)) {
				strategies.forEach(strategy ->
						tibRateService.saveRatesPerEmployeeIds(company, strategy.getId(),
								employeeIds));
			}
		}
	}

	private void deleteOmsPlanSelections(String companyCode, List<String> employeeIds) {
		if(CollectionUtils.isNotEmpty(employeeIds)) {
			List<Company> bssCompanies = companyService.getXbssCompaniesByCode(companyCode);
			bssCompanies.forEach(bssCompany -> {
				Company company = prospectCompanyService.getProspectCompanyDetails(companyCode, bssCompany.getRealmPlanYearId());
				if(CompanyServiceHelper.isTibProspect(company)) {
					List<Strategy> strategies = strategyService.getAllStrategies(company.getId());
					strategies.forEach(
							strategy -> planSelectionService.syncOmsMedicalPlanSelections(strategy.getId()));
					strategyService.createOmsStrategyEstimate(company, strategies.stream().map(Strategy::getId).collect(Collectors.toSet()));
				}
			});
		}
	}
	
	private void deleteEmplDefaultPlanMapping(Set<String> locChangeEmployeeIds) {
		if (CollectionUtils.isNotEmpty(locChangeEmployeeIds)) {
			emplDefaultPlanAssignmentService.deleteEmplDefaultPlanAssignments(locChangeEmployeeIds);
		}
	}

    private boolean checkIfW2MAEmployeeExists(List<ProspectStrategySyncData> prospectStrategySyncData,
                                              String companyCode) {
        boolean emplWithHomeStateMAExists = prospectStrategySyncData.stream()
                .anyMatch(census -> !census.isK1() && US.MASSACHUSETTS.getANSIabbreviation().equals(census.getHomeState()));
        if (!CompanyServiceHelper.isClientCompanyPattern(companyCode) && emplWithHomeStateMAExists) {
            return true;
        }
        return false;
    }

    private void createW2MAGroupForExistingStrategies(List<Company> companies) {
        if (CollectionUtils.isNotEmpty(companies)) {
            companies.forEach(company -> {
                List<Strategy> allStrategies = strategyService.getAllStrategies(company.getId());
                allStrategies.forEach(strategy -> {
                    BenefitGroupStrategy defaultBenGroupStrategy = strategy.getBenefitGroupStrategy().stream()
                            .filter(BenefitGroupStrategy::isDefaultGroup).findAny()
                            .orElseThrow(() -> new BSSApplicationException(new BSSApplicationError(
                                    "No default strategy group found for strategyId: " + strategy.getId())));
                    GroupData groupData = new GroupData();
                    groupData.setDestGroupName(BSSApplicationConstants.CLIENT_MA_GROUP_NAME);
                    groupData.setWaitPeriod(defaultBenGroupStrategy.getWaitingPeriod());
                    groupData.setSourceStrategyGroupId(defaultBenGroupStrategy.getId());
                    benefitGroupService.addGroup(company, groupData, strategy.getId());
                });
            });
        }
    }

    private Map<String, List<EmployeeCensusStrategyGroupDetails>> createGroupTypeDescToStrategyGroupIds(
            List<EmployeeCensusStrategyGroupDetails> existingStrategyGroups) {
        Map<String, List<EmployeeCensusStrategyGroupDetails>> map = Optional.ofNullable(existingStrategyGroups)
                .orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(ecsgd -> {
                    if (BSSApplicationConstants.K1_GROUP_TYPE.equals(ecsgd.getGroupType())) {
                        return BSSApplicationConstants.K1_GROUP_TYPE;
                    } else if (BSSApplicationConstants.CLIENT_MA_GROUP_NAME.equals(ecsgd.getGroupDesc())) {
                        return BSSApplicationConstants.CLIENT_MA_GROUP_NAME;
                    } else {
                        return BSSApplicationConstants.STD_GROUP_TYPE;
                    }
                }));
        return map;
    }

    private List<Company> getCompanies(String companyCode) {
        List<Company> companies = new ArrayList<>();
        if (CompanyServiceHelper.isClientCompanyPattern(companyCode)) {
            Company company = companyService.getCompanyDetails(companyCode);
            companies.add(company);
        } else {
            List<Company> bssCompanies = companyService.getXbssCompaniesByCode(companyCode);
            bssCompanies.forEach(bssCompany -> {
                Company company = prospectCompanyService.getProspectCompanyDetails(companyCode,
                        bssCompany.getRealmPlanYearId());
                companies.add(company);
            });
        }
        return companies;
    }

    private String assignGroupToProspectCensus(ProspectStrategySyncData census) {
        return census.isK1() ? BSSApplicationConstants.K1_GROUP_TYPE
                : (AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()
                && US.MASSACHUSETTS.getANSIabbreviation().equals(census.getHomeState()))
                ? BSSApplicationConstants.CLIENT_MA_GROUP_NAME
                : BSSApplicationConstants.STD_GROUP_TYPE;
    }

    private boolean ifEmployeeHomeStateMismatchFound(Map<String, List<EmployeeCensusStrategyGroupDetails>> employeeStrategyGroupDetails,
                                                     ProspectStrategySyncData employee){
        if (!AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()) {
            return false;
        }
        String newHomeState = employee.getHomeState();
        String stateNameMA = US.MASSACHUSETTS.getANSIabbreviation();
        return (!employee.isK1() &&
                !stateNameMA.equals(newHomeState) && employeeStrategyGroupDetails.get(employee.getEmployeeId()).stream()
                .anyMatch(emp -> CLIENT_MA_GROUP_NAME.equals(emp.getGroupDesc())))
                || (!employee.isK1() &&
                stateNameMA.equals(newHomeState) && employeeStrategyGroupDetails.get(employee.getEmployeeId()).stream()
                .anyMatch(emp -> !CLIENT_MA_GROUP_NAME.equals(emp.getGroupDesc())));

    }


    private long getStrategyGroupIdForMAGroup(Map<Long, List<EmployeeCensusStrategyGroupDetails>> strategyGroupDetails,
                                              Long strategyId) {
        return strategyGroupDetails.get(strategyId).stream()
                .filter(strategyGroupDetail -> CLIENT_MA_GROUP_NAME.equals(strategyGroupDetail.getGroupDesc()))
                .mapToLong(EmployeeCensusStrategyGroupDetails::getStrategyGroupId).findAny().getAsLong();

    }

}