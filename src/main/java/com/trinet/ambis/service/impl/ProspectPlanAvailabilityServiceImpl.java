package com.trinet.ambis.service.impl;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.helper.ProspectPlanAvailabilityServiceHelper;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.HrisPlanAvailabilityService;
import com.trinet.ambis.service.PlanAvailabilityService;
import com.trinet.ambis.service.ProspectCensusService;
import com.trinet.ambis.service.ProspectPlanAvailabilityService;
import com.trinet.ambis.service.model.planAvailability.HrisPlanRequest;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableRequest;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableResponse;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ProspectPlanAvailabilityServiceImpl implements ProspectPlanAvailabilityService {

	@Autowired
	ProspectCensusService prospectCensusService;
	@Autowired
	PlanAvailabilityService planAvailabilityService;
	@Autowired
	HrisPlanAvailabilityService hrisPlanAvailabilityService;

	@Override
	public List<PlanAvailableResponse> getProspectEmployeePlanAvailability(Company company, List<String> plans) {

		List<ProspectCensusResponse> census = prospectCensusService.getProspectCensus(company.getCode());

		Map<String, Set<String>> locations = census.stream()
				.collect(Collectors.groupingBy(ProspectCensusResponse::getState,
						Collectors.mapping(ProspectCensusResponse::getZip,
								Collectors.toSet())));

		PlanAvailableRequest planAvailableRequest = createPlanAvailableRequest(company, locations, plans);

		CompletableFuture<List<PlanAvailableResponse>> availablePlansCompletableFuture = planAvailabilityService.getBenefitPlanAvailability(planAvailableRequest);

		return availablePlansCompletableFuture.join();
	}

	@Override
	public Map<String, List<String>> getProspectEmployeePlansByPlanType(Company company, List<String> plans) {
		List<PlanAvailableResponse> availablePlansByZip = getProspectEmployeePlanAvailability(company, plans);

		Map<String, List<String>> plansByPlanType = new HashMap<>();
		for (PlanAvailableResponse planAvailableResponse : availablePlansByZip) {
			planAvailableResponse.getPlansByBenType().forEach(benTypePlans -> {
				if (plansByPlanType.containsKey(benTypePlans.getBenType())) {
					plansByPlanType.get(benTypePlans.getBenType()).addAll(benTypePlans.getPlanIds());
				} else {
					plansByPlanType.put(benTypePlans.getBenType(), new ArrayList<>(benTypePlans.getPlanIds()));
				}
			});
		}

		// Filter out duplicate plans
		plansByPlanType.forEach((benType, planIds) -> {
			Set<String> uniquePlanIds = new HashSet<>(planIds);
			plansByPlanType.put(benType, new ArrayList<>(uniquePlanIds));
		});

		return plansByPlanType;

	}

	private PlanAvailableRequest createPlanAvailableRequest(Company company, Map<String, Set<String>> locations,
			List<String> plans) {
		List<PlanAvailableRequest.Location> locationList = new ArrayList<>();
		locations.forEach((state, postalCodes) -> postalCodes.forEach(postalCode -> {
			PlanAvailableRequest.Location location = PlanAvailableRequest.Location.builder().state(state)
					.postalCode(postalCode).build();
			locationList.add(location);
		}));

		PlanAvailableRequest planAvailableRequest = new PlanAvailableRequest();
		planAvailableRequest.setCloneBenefitProgram(company.getRealmPlanYear().getCloneProgram());
		planAvailableRequest.setEffectiveDate(company.getRealmPlanYear().getPlanYearEnd());
		planAvailableRequest.setLocations(locationList);
		planAvailableRequest.setPlans(plans);
		return planAvailableRequest;
	}

	/**
	 * Temporary method to support existing code paths.
	 * Delegates to getProspectEmployeeHrisPlanAvailability(String, String) using a default benefitType 'medical'.
	 * TODO: remove this method after migrating all usages to getProspectEmployeeHrisPlanAvailability(String, String)
	 */
	@Override
	public List<HrisPlanResponse> getProspectEmployeeHrisPlanAvailability(Company company) {
		return getProspectEmployeeHrisPlanAvailability(company, BSSApplicationConstants.MEDICAL);
	}

	@Override
	public List<HrisPlanResponse> getProspectEmployeeHrisPlanAvailability(Company company, String benefitType) {
		if (!BSSApplicationConstants.PRIMARY_PLAN_TYPE_NAMES.contains(benefitType)) {
			throw new IllegalArgumentException("The benefitType must be a primary plan type name");
		}
		if (company.getNaicsCode() == null && !Objects.equals(benefitType, BSSApplicationConstants.MEDICAL)) {
			throw new IllegalArgumentException("Company naicsCode is required");
		}

		List<ProspectCensusResponse> census = prospectCensusService.getProspectCensus(company.getCode());
		// Get a list of unique states and unique zip codes from the census
		Map<String, List<String>> locations = census.stream()
				.collect(Collectors.groupingBy(
						ProspectCensusResponse::getState,
						Collectors.mapping(ProspectCensusResponse::getZip, Collectors.collectingAndThen(
								Collectors.toSet(),
								ArrayList::new
						))
				));

		Long numOfEligibleWse = census.stream().filter(Objects::nonNull).distinct().count();
		HrisPlanRequest hrisPlanRequest = ProspectPlanAvailabilityServiceHelper.createHrisPlanRequest(
				company, locations, benefitType, numOfEligibleWse);

		return hrisPlanAvailabilityService.getBenefitPlanAvailability(hrisPlanRequest, String.valueOf(company.getId()) + ':' + benefitType);
	}

}