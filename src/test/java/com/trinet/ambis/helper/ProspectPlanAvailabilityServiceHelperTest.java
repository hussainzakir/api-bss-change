package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.planAvailability.HrisPlanRequest;

@RunWith(MockitoJUnitRunner.class)
public class ProspectPlanAvailabilityServiceHelperTest {

	@Test
	public void createHrisPlanRequestTest() {

		Company company = new Company();
		company.setHeadQuatersState("NJ");
		company.setZipCode("07060");
		company.setBenefitStartDate("01-JAN-2025");
		Map<String, List<String>> locations = prepareLocations();

		HrisPlanRequest actualResult = ProspectPlanAvailabilityServiceHelper.createHrisPlanRequest(
				company, locations, "medical", 10L);

		assertNotNull(actualResult);
		assertEquals("NJ", actualResult.getHqState());
		assertEquals("07060", actualResult.getHqZipCode());
		assertEquals("2025-01-01", actualResult.getEffDate());
		assertEquals(2, actualResult.getEmplLocDetails().size());
		assertEquals("NJ", actualResult.getEmplLocDetails().get(0).getHomeState());
		assertEquals(1, actualResult.getEmplLocDetails().get(0).getHomeZipCodes().size());
		assertEquals("CA", actualResult.getEmplLocDetails().get(1).getHomeState());
		assertEquals(2,  actualResult.getEmplLocDetails().get(1).getHomeZipCodes().size());
	}

	private Map<String, List<String>> prepareLocations() {
		Map<String, List<String>> locations = new HashMap<>();
		List<String> zips = new ArrayList<>();
		zips.add("07060");
		locations.put("NJ", zips);

		zips = new ArrayList<>();
		zips.add("90210");
		zips.add("90211");
		locations.put("CA", zips);

		return locations;
	}

}
