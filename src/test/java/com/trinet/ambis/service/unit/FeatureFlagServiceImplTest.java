package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.persistence.dao.hrp.FeatureFlagDao;
import com.trinet.ambis.service.impl.FeatureFlagServiceImpl;
import com.trinet.ambis.service.model.FeatureFlag;

@RunWith(MockitoJUnitRunner.class)
public class FeatureFlagServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	private FeatureFlagServiceImpl featureFlagService;

	@Mock
	private FeatureFlagDao featureFlagDao;

	private final String COMPANY_CODE = "G48";

	@Test
	public void retrieveFeatureFlags_noData() {
		when(featureFlagDao.retrieveFeatureFlags(COMPANY_CODE, 68)).thenReturn(Arrays.asList());

		List<FeatureFlag> actualResult = featureFlagService.retrieveFeatureFlags(COMPANY_CODE, 68);

		assertEquals(0, actualResult.size());
	}

	@Test
	public void retrieveFeatureFlags_withData() {
		FeatureFlag ff1 = new FeatureFlag("FEATURE_KEY_1", true);
		FeatureFlag ff2 = new FeatureFlag("FEATURE_KEY_2", false);

		when(featureFlagDao.retrieveFeatureFlags(COMPANY_CODE, 68)).thenReturn(Arrays.asList(ff1, ff2));

		List<FeatureFlag> actualResult = featureFlagService.retrieveFeatureFlags(COMPANY_CODE, 68);

		assertEquals(2, actualResult.size());
		assertEquals("FEATURE_KEY_1", actualResult.get(0).getKey());
		assertEquals(true, actualResult.get(0).isValue());

		assertEquals("FEATURE_KEY_2", actualResult.get(1).getKey());
		assertEquals(false, actualResult.get(1).isValue());
	}

}