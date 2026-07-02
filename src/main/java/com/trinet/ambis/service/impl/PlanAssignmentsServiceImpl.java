package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.trinet.ambis.enums.PlanTypesEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.DefaultPlanDataDao;
import com.trinet.ambis.persistence.dao.hrp.EePlanAssignmentDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupPlanSelectDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.EePlanAssignmentPK;
import com.trinet.ambis.persistence.model.EligiblePlanData;
import com.trinet.ambis.rest.controllers.dto.prospect.CreatePlanAssignmentsRequest;
import com.trinet.ambis.rest.controllers.dto.prospect.PlanAssignmentsResponse;
import com.trinet.ambis.rest.controllers.dto.prospect.PlanAssignmentsResponse.PlanAssignmentItem;
import com.trinet.ambis.service.PlanAssignmentsService;
import com.trinet.ambis.service.PlanAvailabilityService;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.ProspectCensusService;
import com.trinet.ambis.service.ProspectPlanAvailabilityService;
import com.trinet.ambis.service.TibRateService;
import com.trinet.ambis.service.dto.BasePlansResDto;
import com.trinet.ambis.service.model.planAvailability.EligibleEmployeePlanResponse;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableRequest;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableResponse;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.service.prospect.dto.EmployeeCostRes;
import com.trinet.ambis.service.prospect.service.ProspectEmployeeCostService;
import com.trinet.ambis.util.BssCoreServiceClient;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.Utils;

@Service
public class PlanAssignmentsServiceImpl implements PlanAssignmentsService {

	@Autowired
	EePlanAssignmentDao eePlanAssignmentDao;

	@Autowired
	ProspectCensusService prospectCensusService;

	@Autowired
	TibRateService tibRateService;

	@Autowired
	XbssRealmPlyrPlanDao realmPlyrPlanDao;

	@Autowired
	DefaultPlanDataDao defaultPlanDataDao;

	@Autowired
	EmployeeBenefitGroupDao employeeBenefitGroupDao;

	@Autowired
	ProspectEmployeeCostService prospectEmployeeCostService;

	@Autowired
	StrategyGroupPlanSelectDao strategyGroupPlanSelectDao;

	@Autowired
	PlanAvailabilityService planAvailabilityService;

	@Autowired
	BenefitPlanDao benefitPlanDao;
	
	@Autowired
	ProspectPlanAvailabilityService prospectPlanAvailabilityService;

	@Autowired
	PlanSelectionService planSelectionService;
	
	@Autowired
	BssCoreServiceClient bssCoreServiceClient;

	@Override
	public List<PlanAssignmentsResponse> getPlanAssignments(long strategyId, long groupId, Company company) {
		String companyCode = company.getCode();
		boolean isTibProspect = CompanyServiceHelper.isTibProspect(company);
		List<ProspectCensusResponse> census = getCensusForCompany(company);
		List<EePlanAssignment> currAssgns = eePlanAssignmentDao.findByStrategyIdGroupId(strategyId, groupId);
		List<String> employeesByStrategyGroup = employeeBenefitGroupDao.getEmployeeDetailsByStrategyAndGroup(strategyId,
				groupId);
		Map<String, List<EePlanAssignment>> currAssignmentsByEmployee = currAssgns.stream()
				.collect(Collectors.groupingBy(assignment -> assignment.getEePlanAssignmentPK().getEmplId()));
		Optional<List<EmployeeCostRes>> prospectEmployeeCost = Optional.empty();
		if (!company.isProspectConvertedClient()) {
			prospectEmployeeCost = prospectEmployeeCostService.getProspectEmployeeCostByType(companyCode,
					BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
		}
		Map<String, List<PlanAssignmentItem>> prospectPlansAssignments = convertToPlanAssignment(prospectEmployeeCost);
		return census.stream().filter(ee -> employeesByStrategyGroup.contains(ee.getEmployeeId())).map(ee -> {
			List<EePlanAssignment> planAssignments = getPlanAssignments(currAssignmentsByEmployee, ee.getEmployeeId());
			return PlanAssignmentsResponse.builder().employeeId(ee.getEmployeeId()).employeeName(ee.getEmployeeName())
					.homeState(ee.getState()).homeZipCode(ee.getZip()).medicalCvgCd(ee.getMedicalTier())
					.dentalCvgCd(ee.getDentalTier()).visionCvgCd(ee.getVisionTier())
					.planAssignment(getPlanAssignmentItems(planAssignments, isTibProspect))
					.prospectPlanAssignment(prospectPlansAssignments.get(ee.getEmployeeId())).build();
		}).collect(Collectors.toList());
	}

	private List<String> getEligibleRegionalPlans(long strategyId, long groupId, String state, String zipCode,
			String benefitType, Company company, String cvgTierCode) {
		if (CoverageCodesEnums.getByCoverageCode(cvgTierCode) == null) {
			throw new BSSApplicationException(new BSSApplicationError(
					BSSErrorResponseCodes.BSS_INVALID_COVERGAE_TIER_CODE, BSSHttpStatusConstants.BAD_REQUEST, "",
					"Invalid coverage tier code.", null, null));
		}
		Map<String, EligiblePlanData> eligiblePlansMap = benefitPlanDao.getBenefitPlansAndCarriersBy(strategyId,
				groupId, benefitType, company.getRealmPlanYear().getPlanYearStart(), cvgTierCode);
		List<String> planList = eligiblePlansMap.keySet().stream().collect(Collectors.toList());
		
		PlanAvailableRequest planAvailabilityReq = new PlanAvailableRequest();
		planAvailabilityReq.setCloneBenefitProgram(company.getRealmPlanYear().getCloneProgram());
		planAvailabilityReq
				.setEffectiveDate(Utils.convertStringToDate(company.getBenefitStartDate(), Constants.DATE_FORMAT));
		List<PlanAvailableRequest.Location> locationList = new ArrayList<>();
		PlanAvailableRequest.Location location = new PlanAvailableRequest.Location();
		location.setState(state);
		location.setPostalCode(zipCode);
		locationList.add(location);
		planAvailabilityReq.setLocations(locationList);
		planAvailabilityReq.setPlans(planList);

		CompletableFuture<List<PlanAvailableResponse>> availablePlansCompletableFuture = planAvailabilityService
				.getBenefitPlanAvailability(planAvailabilityReq);
		List<PlanAvailableResponse> availablePlansByZip = availablePlansCompletableFuture.join();
		List<String> plans = new ArrayList<>();
		for (PlanAvailableResponse planAvailableResponse : availablePlansByZip) {
			planAvailableResponse.getPlansByBenType().forEach(benTypePlans -> {
				plans.addAll(benTypePlans.getPlanIds());
			});
		}
		
		plans.sort(Comparator
				.comparing(planId -> eligiblePlansMap.get(planId).getCarrier(),
						Comparator.nullsLast(Comparator.naturalOrder()))
				.thenComparing(planId -> eligiblePlansMap.get(planId).getPlanCost(),
				Comparator.nullsLast(Comparator.naturalOrder())));

		return plans;
	}

	@Override
	public void createPlanAssignments(long strategyId, Company company,
			List<CreatePlanAssignmentsRequest> assignments) {
		List<String> companyEmployeeIds = new ArrayList<>();
		List<ProspectCensusResponse> census = getCensusForCompany(company);
		census.stream().forEach(ee -> {
			companyEmployeeIds.add(ee.getEmployeeId());
		});

		assignments.stream().forEach(asgn -> {
			validateInputEmployeeIds(assignments, companyEmployeeIds);
			validateInputBenefitPlans(assignments, strategyId, CompanyServiceHelper.isTibProspect(company));
		});
		List<EePlanAssignment> entities = transformToEntityList(assignments, strategyId);
		eePlanAssignmentDao.saveAllAndFlush(entities);
		processOmsPlanAssignments(assignments, strategyId, company);
	}
	
    @Override
    public List<EligibleEmployeePlanResponse> getProspectEmployeesEligibleForPlan(long strategyId, long groupId,
            Company company, String basePlanId, String benefitType) {
        final boolean isTibProspect = CompanyServiceHelper.isTibProspect(company);

        List<ProspectCensusResponse> prospectEmployees = getCensusForCompany(company);
        List<String> employeesByStrategyGroup =
                employeeBenefitGroupDao.getEmployeeDetailsByStrategyAndGroup(strategyId, groupId);
        List<ProspectCensusResponse> prospectEmployeesInGroup = prospectEmployees.stream()
                .filter(prospectEmpl -> employeesByStrategyGroup.contains(prospectEmpl.getEmployeeId())).collect(Collectors.toList());

        if (isTibProspect) {
            return getProspectEmployeesEligibleForHrisPlan(company, basePlanId, prospectEmployeesInGroup, benefitType);
        }

        return getProspectEmployeesEligibleForBasePlan(strategyId, groupId, basePlanId, company,
                prospectEmployeesInGroup);
    }
	
    private List<EligibleEmployeePlanResponse> getProspectEmployeesEligibleForHrisPlan(
			Company company, String basePlanId, List<ProspectCensusResponse> prospectEmployeesInGroup, String benefitType) {
        List<HrisPlanResponse> hrisPlans = prospectPlanAvailabilityService
                .getProspectEmployeeHrisPlanAvailability(company, PlanTypesEnum.getName(benefitType))
                .stream()
                .filter(plan -> basePlanId.equals(String.valueOf(plan.getPlanId())))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(hrisPlans) || CollectionUtils.isEmpty(prospectEmployeesInGroup)) {
            return Collections.emptyList();
        }
        // @formatter:off
		return prospectEmployeesInGroup.stream()
	            .map(employee -> mapToEligibleEmployeePlan(employee, hrisPlans))
	            .filter(Optional::isPresent)
	            .map(Optional::get)
	            .collect(Collectors.toList());
		// @formatter:on
    }

    private Optional<EligibleEmployeePlanResponse> mapToEligibleEmployeePlan(ProspectCensusResponse employee, List<HrisPlanResponse> hrisPlans) {
     // @formatter:off
		return hrisPlans.stream()
	            .filter(plan -> isEligibleForPlan(employee, plan))
	            .findFirst()
	            .map(plan -> EligibleEmployeePlanResponse.builder()
	                    .employeeId(employee.getEmployeeId())
	                    .planId(String.valueOf(plan.getPlanId()))
	                    .build());
	// @formatter:on
    }

    private boolean isEligibleForPlan(ProspectCensusResponse employee, HrisPlanResponse plan) {
        if (plan.getRateDetails() == null || plan.getRateDetails().getRatesByZip() == null) {
            return false;
        }

        String employeeZip = employee.getZip();
        if (employeeZip == null) {
            return false;
        }
        // @formatter:off
	    return plan.getRateDetails()
	            .getRatesByZip()
	            .stream()
	            .filter(rate -> rate.getZips() != null)
	            .anyMatch(rate -> rate.getZips().contains(employeeZip));
	    // @formatter:on
    }

    private List<EligibleEmployeePlanResponse> getProspectEmployeesEligibleForBasePlan(long strategyId, long groupId,
            String basePlanId, Company company, List<ProspectCensusResponse> prospectEmployeesInGroup) {
        List<EligibleEmployeePlanResponse> returnData = new ArrayList<>();

        List<String> regionalPlans = benefitPlanDao.getSelectedRegionalPlansForBasePlan(strategyId, groupId,
                company.getRealmPlanYearId(), basePlanId);

        Map<String, Set<String>> locations =
                prospectEmployeesInGroup.stream().collect(Collectors.groupingBy(ProspectCensusResponse::getState,
                        Collectors.mapping(ProspectCensusResponse::getZip, Collectors.toSet())));

        PlanAvailableRequest planAvailableRequest = createPlanAvailableRequest(company, locations, regionalPlans);

        CompletableFuture<List<PlanAvailableResponse>> availablePlansCompletableFuture =
                planAvailabilityService.getBenefitPlanAvailability(planAvailableRequest);

        List<PlanAvailableResponse> availablePlansByZip = availablePlansCompletableFuture.join();

        Map<String, List<PlanAvailableResponse>> plansGroupedByZip =
                availablePlansByZip.stream().collect(Collectors.groupingBy(PlanAvailableResponse::getPostal));

        for (ProspectCensusResponse prospectEmployee : prospectEmployeesInGroup) {
            List<PlanAvailableResponse> plansForEmplZip =
                    plansGroupedByZip.getOrDefault(prospectEmployee.getZip(), Collections.emptyList());

            List<String> plans = plansForEmplZip.stream().flatMap(
                    plan -> Optional.ofNullable(plan.getPlansByBenType()).orElse(Collections.emptyList()).stream())
                    .flatMap(benTypePlans -> Optional.ofNullable(benTypePlans.getPlanIds())
                            .orElse(Collections.emptyList()).stream())
                    .collect(Collectors.toList());
         // @formatter:off
            if (!plans.isEmpty()) {
                returnData.add(
                    EligibleEmployeePlanResponse.builder()
                        .employeeId(prospectEmployee.getEmployeeId())
                        .planId(plans.get(0)) 
                        .build());
            }
         // @formatter:on
        }

        return returnData;

    }
    
	@Override
	public List<String> getEligiblePlans(long strategyId, long groupId, String state, String zipCode,
			String benefitType, Company company, String cvgTierCode) {
		final boolean isTibProspect = CompanyServiceHelper.isTibProspect(company);

		if (isTibProspect) {
            return getOmsPlanIds(company, benefitType, zipCode);
		}

		return getEligibleRegionalPlans(strategyId, groupId, state, zipCode, benefitType, company, cvgTierCode);
	}

    private List<String> getOmsPlanIds(Company company, String benefitType, String zipCode) {
	    // create a sortable list of HrisPlanResponse objects
		List<HrisPlanResponse> hrisPlans = new ArrayList<>(prospectPlanAvailabilityService
	            .getProspectEmployeeHrisPlanAvailability(company, PlanTypesEnum.getName(benefitType)));

        // filter plans based on the zip code
        hrisPlans = hrisPlans.stream()
                .filter(response -> response.getRateDetails() != null &&
                        response.getRateDetails().getRatesByZip() != null &&
                        response.getRateDetails().getRatesByZip().stream()
                                .anyMatch(ratesByZip -> ratesByZip.getZips() != null &&
                                        ratesByZip.getZips().contains(zipCode)))
                .collect(Collectors.toList());

	    hrisPlans.sort(Comparator.comparing(HrisPlanResponse::getCarrierName).thenComparing(HrisPlanResponse::getPlanName));

	    return Optional.of(hrisPlans)
	            .orElse(Collections.emptyList())
	            .stream()
	            .map(HrisPlanResponse::getPlanId)
	            .map(String::valueOf)
	            .distinct()
	            .collect(Collectors.toList());
	}

	private PlanAvailableRequest createPlanAvailableRequest(Company company, Map<String, Set<String>> locations, List<String> regionalPlans) {
		List<PlanAvailableRequest.Location> locationList = new ArrayList<>();
		locations.forEach((state, postalCodes) -> {
			postalCodes.forEach(postalCode -> {
				PlanAvailableRequest.Location location = PlanAvailableRequest.Location.builder().state(state).postalCode(postalCode).build();
				locationList.add(location);
			});
		});

		PlanAvailableRequest planAvailableRequest = new PlanAvailableRequest();
		planAvailableRequest.setCloneBenefitProgram(company.getRealmPlanYear().getCloneProgram());
		planAvailableRequest.setEffectiveDate(company.getRealmPlanYear().getPlanYearEnd());
		planAvailableRequest.setLocations(locationList);
		planAvailableRequest.setPlans(regionalPlans);
		return planAvailableRequest;
	}

	@Override
	public List<BasePlansResDto> getBasePlans(long strategyId, long groupId, Company company) {
		Map<String,List<BasePlansResDto.Plan>> map = getBasePlansByBenType(strategyId, groupId, company);

		List<BasePlansResDto> basePlans = new ArrayList<>();
		for( Map.Entry<String,List<BasePlansResDto.Plan>> entry : map.entrySet() ) {
			basePlans.add( BasePlansResDto.builder()
					.benType( entry.getKey() )
					.plans( entry.getValue() )
					.build() );
		}

		return basePlans;
	}

	@Override
	public BigDecimal getOmsPlanRateByEmployee(Company company, String employeeId, String planId, String covgLevelCode, String benefitType) {
		return tibRateService.getRateForEmployee(company, employeeId, planId, covgLevelCode, benefitType);
	}

	private void validateInputEmployeeIds(List<CreatePlanAssignmentsRequest> assignments, List<String> validIds) {
		assignments.stream().forEach(assgn -> {
			if (!validIds.contains(assgn.getEmployeeId())) {
				throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_INVALID_EMPLOYEE_ID,
						BSSHttpStatusConstants.BAD_REQUEST, "",
						String.format("Invalid EmployeeId '%s'", assgn.getEmployeeId()), null, null));
			}
		});
	}

	private void validateInputBenefitPlans(List<CreatePlanAssignmentsRequest> assignments, long strategyId, boolean isTibProspect) {
		Set<String> plans = new HashSet<>();
		List<String> validPlans = new ArrayList<>();
		for (CreatePlanAssignmentsRequest req : assignments) {
			if ((isTibProspect &&
					BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER.contains(req.getBenefitType()))) {
				validPlans.add(req.getBenefitPlanId());
			} else {
				plans.add(req.getBenefitPlanId());
			}
		}
		validPlans.addAll(realmPlyrPlanDao.validatePlanForStrategyYear(plans, strategyId));
		assignments.stream().forEach(assgn -> {
			if (!validPlans.contains(assgn.getBenefitPlanId()))
				throw new BSSApplicationException(new BSSApplicationError(
						BSSErrorResponseCodes.BSS_INVALID_BENEFIT_PLAN, BSSHttpStatusConstants.BAD_REQUEST, "",
						String.format("Benefit plan '%s' not valid for indicated strategy %s", assgn.getBenefitPlanId(),
								strategyId),
						null, null));
		});
	}

	private List<EePlanAssignment> transformToEntityList(List<CreatePlanAssignmentsRequest> assignments,
			long strategyId) {
		List<EePlanAssignment> list = new ArrayList<>();
		assignments.stream().forEach(assgn -> {
			EePlanAssignment eeAsgn = new EePlanAssignment();
			EePlanAssignmentPK pk = new EePlanAssignmentPK();
			pk.setStrategyId(strategyId);
			pk.setEmplId(assgn.getEmployeeId());
			pk.setBenefitType(assgn.getBenefitType());
			eeAsgn.setCovrgCD(assgn.getCoverageCode());
			eeAsgn.setPortfolioId(assgn.getPortfolioId());
			eeAsgn.setBenefitPlan(assgn.getBenefitPlanId());
			eeAsgn.setEePlanAssignmentPK(pk);
			list.add(eeAsgn);
		});
		return list;
	}

	private List<EePlanAssignment> getPlanAssignments(Map<String, List<EePlanAssignment>> currAssignmentsByEmployee,
			String employeeId) {
		return currAssignmentsByEmployee.containsKey(employeeId) ? currAssignmentsByEmployee.get(employeeId)
				: Collections.emptyList();
	}

	private List<PlanAssignmentItem> getPlanAssignmentItems(List<EePlanAssignment> planAssignments,
		boolean isTibProspect) {

	    return planAssignments.stream().map(assignment -> {
		PlanAssignmentItem planAssignmentItem = new PlanAssignmentItem();
		planAssignmentItem.setBenefitPlanId(assignment.getBenefitPlan());
		String benefitType = assignment.getEePlanAssignmentPK().getBenefitType();
		planAssignmentItem.setBenefitType(convertToBenTypeString(benefitType));
		// set total cost if required
		if (isTibProspect && (benefitType.equals(BSSApplicationConstants.MEDICAL_PLAN_TYPE) ||
				benefitType.equals(BSSApplicationConstants.DENTAL_PLAN_TYPE) ||
				benefitType.equals(BSSApplicationConstants.VISION_PLAN_TYPE))) {
			BigDecimal totalCost = null;
			if (assignment.getErRate() != null && assignment.getEeRate() != null) {
				totalCost = assignment.getErRate().add(assignment.getEeRate());
			} else if (assignment.getErRate() != null) {
				totalCost = assignment.getErRate();
			} else if (assignment.getEeRate() != null) {
				totalCost = assignment.getEeRate();
			}
			planAssignmentItem.setTotalCost(totalCost);
		}

		return planAssignmentItem;
	    }).collect(Collectors.toList());
	}

	private Map<String, List<PlanAssignmentItem>> convertToPlanAssignment(
			Optional<List<EmployeeCostRes>> prospectData) {
		Map<String, List<PlanAssignmentItem>> employeePlanAssginemntMap = new HashMap<>();
		if (prospectData.isPresent()) {
			for (EmployeeCostRes employeeCostRes : prospectData.get()) {
				String benefitTypeCode = employeeCostRes.getBenefitTypeCode();
				for (EmployeeCostRes.EmployeePlanContribution employeePlanContribution : employeeCostRes
						.getEmployeePlanContribution()) {
					PlanAssignmentItem pa = new PlanAssignmentItem();
					pa.setBenefitPlanId(employeePlanContribution.getPlanContribution().getBenefitPlanId() + "");
					pa.setBplPlanId(employeePlanContribution.getPlanContribution().getBplPlanId());
					pa.setBenefitType(convertToBenTypeString(benefitTypeCode));
					pa.setBenefitPlanName(employeePlanContribution.getPlanContribution().getBenefitPlanName());
					pa.setTotalCost(employeePlanContribution.getPlanContribution().getTotalCost());
					List<PlanAssignmentItem> prospectPlanAssignment = employeePlanAssginemntMap
							.get(employeePlanContribution.getEmployeeId());
					if (null == prospectPlanAssignment) {
						prospectPlanAssignment = new ArrayList<>();
					}
					prospectPlanAssignment.add(pa);
					employeePlanAssginemntMap.put(employeePlanContribution.getEmployeeId(), prospectPlanAssignment);
				}
			}
		}
		return employeePlanAssginemntMap;
	}

	private String convertToBenTypeString(String benefitType) {
		if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(benefitType)) {
			benefitType = BSSApplicationConstants.MEDICAL;
		} else if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(benefitType)) {
			benefitType = BSSApplicationConstants.DENTAL;
		} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(benefitType)) {
			benefitType = BSSApplicationConstants.VISION;
		}
		return benefitType;
	}

	private void processOmsPlanAssignments(List<CreatePlanAssignmentsRequest> assignments, long strategyId, Company company) {
		if (CompanyServiceHelper.isTibProspect(company)) {
			Map<String, Set<String>> employeeIds = assignments.stream()
					.filter(assignment -> BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER.contains(
							assignment.getBenefitType()))
					.collect(Collectors.groupingBy(CreatePlanAssignmentsRequest::getBenefitType,
							Collectors.mapping(CreatePlanAssignmentsRequest::getEmployeeId,
									Collectors.toSet())));

			BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER.forEach(type -> {
				Set<String> ids = employeeIds.get(type);
				if (!CollectionUtils.isEmpty(ids)) {
					tibRateService.saveRatesPerEmployeeIds(company, strategyId, ids, List.of(type));
				}
			});
			planSelectionService.syncOmsMedicalPlanSelections(strategyId);
		}
	}

	private Map<String,List<BasePlansResDto.Plan>> getBasePlansByBenType(long strategyId, long groupId, Company company) {
		// Get base plans and group by benType
		Map<String,List<BasePlansResDto.Plan>> map = new HashMap<>();
		List<Object[]> result = strategyGroupPlanSelectDao.getBasePlans(strategyId, groupId);

		for( Object[] row : result ) {
			String benType = row[0].toString();
			BasePlansResDto.Plan plan = BasePlansResDto.Plan.builder()
					.planId( row[1].toString() )
					.planName( row[2].toString() )
					.build();
			if( map.containsKey( benType ) ) {
				map.get( benType ).add( plan );
			} else {
				map.put( benType, new ArrayList<>( Arrays.asList( plan ) ));
			}
		}

		if (CompanyServiceHelper.isTibProspect(company)) {
			addOmsBasePlansToBasePlansMap(company, map, BSSApplicationConstants.MEDICAL);
			addOmsBasePlansToBasePlansMap(company, map, BSSApplicationConstants.DENTAL);
			addOmsBasePlansToBasePlansMap(company, map, BSSApplicationConstants.VISION);
		}

		return map;
	}

	private void addOmsBasePlansToBasePlansMap(Company company, Map<String, List<BasePlansResDto.Plan>> map, String benefitType) {
		if (!CompanyServiceHelper.isTibProspect(company)) {
			return;
		}
		List<HrisPlanResponse> hrisPlans = prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, benefitType);
		map.put(benefitType, hrisPlans.stream()
				.collect(Collectors.toMap(
						HrisPlanResponse::getPlanId,
						hrisPlan -> BasePlansResDto.Plan.builder()
								.planId(String.valueOf(hrisPlan.getPlanId()))
								.planName(hrisPlan.getCarrierName() + " " + hrisPlan.getPlanName())
								.build(),
						(p1, p2) -> p1,    // drop duplicates
						LinkedHashMap::new))
				.values().stream()
				.sorted(Comparator.comparing(BasePlansResDto.Plan::getPlanName))
				.collect(Collectors.toList()));
	}
	
	private List<ProspectCensusResponse> getCensusForCompany(Company company) {
		String companyCode = company.getCode();
		return company.isProspectConvertedClient() ? bssCoreServiceClient.getCensusByCompanyCode(companyCode)
				: prospectCensusService.getProspectCensus(companyCode);

	}

}