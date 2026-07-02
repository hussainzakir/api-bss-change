package com.trinet.ambis.helper;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.planAvailability.HrisPlanRequest;
import com.trinet.ambis.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProspectPlanAvailabilityServiceHelper {

	private ProspectPlanAvailabilityServiceHelper() {
		throw new IllegalStateException(
				"Utility class " + ProspectPlanAvailabilityServiceHelper.class.getName() + " can not be instantiated.");
	}

	/**
	 * This method creates and returns a PlanAvailableRequest object.
	 *
	 * @param company The company object containing the company information.
	 * @param locations A map of location details where the key is the state and the value is a list of zip codes.
	 * @param benefitType benefit type (medical, dental, vision)
	 * @param numOfEligibleWse number of eligible WSE
	 */
	public static HrisPlanRequest createHrisPlanRequest(
			Company company, Map<String, List<String>> locations, String benefitType, Long numOfEligibleWse) {
		return HrisPlanRequest.builder()
				.benefitsType(benefitType)
				.hqState(company.getHeadQuatersState())
				.hqZipCode(company.getZipCode())
				.effDate(Utils.convertDateFormat(company.getBenefitStartDate(), "dd-MMM-yyyy", "yyyy-MM-dd").get())
				.naicsCode(String.valueOf(company.getNaicsCode()))
				.numOfEligibleWse(numOfEligibleWse)
				.emplLocDetails(locations
						.entrySet()
						.stream()
						.map(entry -> HrisPlanRequest.LocationDetails.builder()
								.homeState(entry.getKey())
								.homeZipCodes(entry.getValue())
								.build())
						.collect(Collectors.toList()))
				.build();
	}
}
