/**
 *
 */
package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.persistence.dao.hrp.DefaultPlanDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.EmplDefaultPlanAssignmentService;
import com.trinet.ambis.service.PlanAvailabilityService;
import com.trinet.ambis.service.ProspectCensusService;
import com.trinet.ambis.service.ProspectDefaultPlanMappingService;
import com.trinet.ambis.service.dto.EmplDefaultPlanAssignmentDto;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableRequest;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableResponse;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.util.BssCoreServiceClient;

import jakarta.transaction.Transactional;

/**
 *
 */
@Service
public class ProspectDefaultPlanMappingServiceImpl implements ProspectDefaultPlanMappingService {

	@Autowired
	ProspectCensusService prospectCensusService;
	@Autowired
	DefaultPlanDataDao defaultPlanDataDao;
	@Autowired
	EmplDefaultPlanAssignmentService emplDefaultPlanAssignmentService;
	@Autowired
	PlanAvailabilityService planAvailabilityService;
	@Autowired
	RealmDataDao realmDataDao;
	@Autowired
	BssCoreServiceClient bssCoreServiceClient;

	private static final Logger logger = LoggerFactory.getLogger(ProspectDefaultPlanMappingServiceImpl.class);

	@Override
	public void createCensusDefaultRegionalPlanMapping(Company company) {
		List<ProspectCensusResponse> census = company.isProspectCompany()
				? prospectCensusService.getProspectCensus(company.getCode())
				: bssCoreServiceClient.getCensusByCompanyCode(company.getCode());

		emplDefaultPlanAssignmentService.deleteEmplDefaultPlanAssignments(company.getId());
		saveDefaultRegionalPlanMapping(company, census);
	}

	@Override
	@Transactional
	public void createCensusDefaultRegionalPlanMapping(Company company, List<ProspectCensusResponse> census) {
		saveDefaultRegionalPlanMapping(company, census);
	}

	private void saveDefaultRegionalPlanMapping(Company company, List<ProspectCensusResponse> census) {

		Map<String, Map<String, Long>> regionalDefaultPlansByPlanType = defaultPlanDataDao.getRegionalDefaultPlansByPlanType(company);
		if (regionalDefaultPlansByPlanType.containsKey(BSSApplicationConstants.MEDICAL_PLAN_TYPE)) {
			Set<String> medicalPlanCarriers = regionalDefaultPlansByPlanType.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE)
					.values().stream().map(Object::toString)
					.collect(Collectors.toSet());
			Set<String> outOfRegionPlans = CommonServiceHelper.getOutOfRegionPlansToExclude(company, medicalPlanCarriers,
					realmDataDao);
			if (CollectionUtils.isNotEmpty(outOfRegionPlans)) {
				regionalDefaultPlansByPlanType.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE).keySet().removeAll(outOfRegionPlans);
			}
		}
		Map<String, Long> regionalDefaultPlansByPlan = regionalDefaultPlansByPlanType.values().stream()
				.flatMap(m -> m.entrySet().stream())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		Map<String, Set<String>> locations = census.stream()
				.collect(Collectors.groupingBy(ProspectCensusResponse::getState,
						Collectors.mapping(ProspectCensusResponse::getZip,
								Collectors.toSet())));

		PlanAvailableRequest planAvailableRequest = createPlanAvailableRequest(company, locations, regionalDefaultPlansByPlan);

		CompletableFuture<List<PlanAvailableResponse>> availablePlansCompletableFuture = planAvailabilityService.getBenefitPlanAvailability(planAvailableRequest);

		List<PlanAvailableResponse> availablePlansByZip = availablePlansCompletableFuture.join();

		List<EmplDefaultPlanAssignmentDto> emplDefaultPlanAssignmentDtos = createEmpPlanDefaultAssignmentDtos(company,
				census, availablePlansByZip, regionalDefaultPlansByPlan);
		emplDefaultPlanAssignmentService.saveAll(emplDefaultPlanAssignmentDtos);
	}

	private PlanAvailableRequest createPlanAvailableRequest(Company company, Map<String, Set<String>> locations,
	                                                        Map<String, Long> regionalDefaultPlansByPlan) {
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
		planAvailableRequest.setPlans(new ArrayList<>(regionalDefaultPlansByPlan.keySet()));
		return planAvailableRequest;
	}

	private List<EmplDefaultPlanAssignmentDto> createEmpPlanDefaultAssignmentDtos(Company company,
	                                                                              List<ProspectCensusResponse> census,
	                                                                              List<PlanAvailableResponse> availablePlansByZip,
	                                                                              Map<String, Long> regionalDefaultPlansByPlan) {
		List<EmplDefaultPlanAssignmentDto> emplDefaultPlanAssignmentDtos = new ArrayList<>();
		for (ProspectCensusResponse pcs : census) {
			List<PlanAvailableResponse.BenTypePlan> plansByBenTypeList = new ArrayList<>();
			Optional<PlanAvailableResponse> plansForZip = availablePlansByZip.stream()
					.filter(value -> value.getPostal().equals(pcs.getZip())).findFirst();
			if (plansForZip.isPresent()) {
				plansByBenTypeList =  plansForZip.get().getPlansByBenType();
			} else {
				logger.error("Zip Code {} not found in plan availability for prospect {}", pcs.getZip(), company.getCode());
			}

			for (PlanAvailableResponse.BenTypePlan plansByBenType : plansByBenTypeList) {
				Set<Integer> portfolioIdsAssigned = new HashSet<>();
				int portfolioId = 0;
				String planId = null;
				boolean planFound = false;
				String planType = plansByBenType.getBenType();
				for (String benPlanId : plansByBenType.getPlanIds()) {
					if (regionalDefaultPlansByPlan.containsKey(benPlanId)) {
						portfolioId = regionalDefaultPlansByPlan.get(benPlanId).intValue();
						planId = benPlanId;
						if (!portfolioIdsAssigned.contains(portfolioId)) {
							portfolioIdsAssigned.add(portfolioId);
							populateResultList(emplDefaultPlanAssignmentDtos, company, pcs, planType, portfolioId,
									planId);
							planFound = true;
						}
					}
				}
				if(!planFound) {
					logger.error("No default plan found for plan type {} zip code {} employee {} prospect {}", planType, pcs.getZip(), pcs.getEmployeeId(), company.getCode());
				}
			}

		}
		return emplDefaultPlanAssignmentDtos;
	}

	private void populateResultList(List<EmplDefaultPlanAssignmentDto> emplDefaultPlanAssignmentDtos, Company company,
	                                ProspectCensusResponse pcs, String planType, int portfolioId, String planId) {
		EmplDefaultPlanAssignmentDto dto = new EmplDefaultPlanAssignmentDto();
		dto.setEmplId(pcs.getEmployeeId());
		dto.setCompanyId(company.getId());
		dto.setBenefitPlanId(planId);
		dto.setPlanType(planType);
		dto.setPortfolioId(portfolioId);
		if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(planType)) {
			dto.setCoverageCode(pcs.getMedicalTier());
		} else if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(planType)) {
			dto.setCoverageCode(pcs.getDentalTier());
		} else {
			dto.setCoverageCode(pcs.getVisionTier());
		}
		if (!ProspectConstants.WAVED_COVERAGE.equalsIgnoreCase(dto.getCoverageCode())) {
			emplDefaultPlanAssignmentDtos.add(dto);
		}
	}


}