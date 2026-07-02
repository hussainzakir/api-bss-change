package com.trinet.ambis.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.service.FeatureFlagService;
import com.trinet.ambis.service.model.FeatureFlag;

@RunWith(JUnit4.class)
public class FeatureFlagUtilsTest {

	@Mock
	FeatureFlagService featureFlagService;

	private static final String BSS_YEAR_ROUND = "BSS_YEAR_ROUND";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		FeatureFlagUtils.setFeatureFlagService(featureFlagService);
	}

	@Test
	public void retrieveFlagValueBy_On() {
		when(featureFlagService.retrieveFeatureFlags(anyString(), anyLong()))
		.thenReturn(prepareFeatureFlags(true));
		
		boolean result = FeatureFlagUtils.isBssYearAround("G48", 68);

		assertTrue(result);
	}
	
	@Test
	public void retrieveFlagValueBy_Off() {
		when(featureFlagService.retrieveFeatureFlags(anyString(), anyLong()))
		.thenReturn(prepareFeatureFlags(false));
		
		boolean result = FeatureFlagUtils.isBssYearAround("G48", 68);

		assertFalse(result);
	}
	
	@Test
	public void retrieveFlagValueBy_Off_Flag_Not_Configured() {
		when(featureFlagService.retrieveFeatureFlags(anyString(), anyLong()))
		.thenReturn(Arrays.asList());
		
		boolean result = FeatureFlagUtils.isBssYearAround("G48", 68);

		assertFalse(result);
	}

	private List<FeatureFlag> prepareFeatureFlags(boolean value) {
		List<FeatureFlag> flags = new ArrayList<>();
		FeatureFlag flag = new FeatureFlag(BSS_YEAR_ROUND, value);
		flags.add(flag);
		flag = new FeatureFlag("FLAG_1", true);
		flags.add(flag);
		return flags;
	}
}
