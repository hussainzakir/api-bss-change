package com.trinet.ambis.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BSSProcessTypeToSNFeatureMappingTest {

	@Test
	public void getSubmitEnumTest() {
		// when
		BSSProcessTypeToSNFeatureMapping bSSProcessTypeToSNFeatureMapping = BSSProcessTypeToSNFeatureMapping
				.get(BSSProcessTypeToSNFeatureMapping.SUBMIT.getBssProcessType());
		// then
		assertNotNull(bSSProcessTypeToSNFeatureMapping);
		assertEquals(BSSProcessTypeToSNFeatureMapping.SUBMIT.getBssProcessType(),
				bSSProcessTypeToSNFeatureMapping.getBssProcessType());
		assertEquals(BSSProcessTypeToSNFeatureMapping.SUBMIT.getServiceNowFeatureName(),
				bSSProcessTypeToSNFeatureMapping.getServiceNowFeatureName());
	}

	@Test
	public void getReSubmitEnumTest() {
		// when
		BSSProcessTypeToSNFeatureMapping bSSProcessTypeToSNFeatureMapping = BSSProcessTypeToSNFeatureMapping
				.get(BSSProcessTypeToSNFeatureMapping.RESUBMIT.getBssProcessType());
		// then
		assertNotNull(bSSProcessTypeToSNFeatureMapping);
		assertEquals(BSSProcessTypeToSNFeatureMapping.RESUBMIT.getBssProcessType(),
				bSSProcessTypeToSNFeatureMapping.getBssProcessType());
		assertEquals(BSSProcessTypeToSNFeatureMapping.RESUBMIT.getServiceNowFeatureName(),
				bSSProcessTypeToSNFeatureMapping.getServiceNowFeatureName());
	}

	@Test
	public void getDefaultSubmitEnumTest() {
		// when
		BSSProcessTypeToSNFeatureMapping bSSProcessTypeToSNFeatureMapping = BSSProcessTypeToSNFeatureMapping
				.get(BSSProcessTypeToSNFeatureMapping.DEFAULT_SUBMIT.getBssProcessType());
		// then
		assertNotNull(bSSProcessTypeToSNFeatureMapping);
		assertEquals(BSSProcessTypeToSNFeatureMapping.DEFAULT_SUBMIT.getBssProcessType(),
				bSSProcessTypeToSNFeatureMapping.getBssProcessType());
		assertEquals(BSSProcessTypeToSNFeatureMapping.DEFAULT_SUBMIT.getServiceNowFeatureName(),
				bSSProcessTypeToSNFeatureMapping.getServiceNowFeatureName());
	}

	@Test
	public void getBandCodReSubmitEnumTest() {
		// when
		BSSProcessTypeToSNFeatureMapping bSSProcessTypeToSNFeatureMapping = BSSProcessTypeToSNFeatureMapping
				.get(BSSProcessTypeToSNFeatureMapping.BANDCODE_RESUBMIT.getBssProcessType());
		// then
		assertNotNull(bSSProcessTypeToSNFeatureMapping);
		assertEquals(BSSProcessTypeToSNFeatureMapping.BANDCODE_RESUBMIT.getBssProcessType(),
				bSSProcessTypeToSNFeatureMapping.getBssProcessType());
		assertEquals(BSSProcessTypeToSNFeatureMapping.BANDCODE_RESUBMIT.getServiceNowFeatureName(),
				bSSProcessTypeToSNFeatureMapping.getServiceNowFeatureName());
	}

	@Test
	public void getTermDefaultEnumTest() {
		// when
		BSSProcessTypeToSNFeatureMapping bSSProcessTypeToSNFeatureMapping = BSSProcessTypeToSNFeatureMapping
				.get(BSSProcessTypeToSNFeatureMapping.TERM_DEFAULT.getBssProcessType());
		// then
		assertNotNull(bSSProcessTypeToSNFeatureMapping);
		assertEquals(BSSProcessTypeToSNFeatureMapping.TERM_DEFAULT.getBssProcessType(),
				bSSProcessTypeToSNFeatureMapping.getBssProcessType());
		assertEquals(BSSProcessTypeToSNFeatureMapping.TERM_DEFAULT.getServiceNowFeatureName(),
				bSSProcessTypeToSNFeatureMapping.getServiceNowFeatureName());
	}

	@Test
	public void getUnknownEnumTest() {
		// when
		BSSProcessTypeToSNFeatureMapping bSSProcessTypeToSNFeatureMapping = BSSProcessTypeToSNFeatureMapping
				.get(BSSProcessTypeToSNFeatureMapping.UNKNOWN.getBssProcessType());
		// then
		assertNotNull(bSSProcessTypeToSNFeatureMapping);
		assertEquals(BSSProcessTypeToSNFeatureMapping.UNKNOWN.getBssProcessType(),
				bSSProcessTypeToSNFeatureMapping.getBssProcessType());
		assertEquals(BSSProcessTypeToSNFeatureMapping.UNKNOWN.getServiceNowFeatureName(),
				bSSProcessTypeToSNFeatureMapping.getServiceNowFeatureName());
	}

	@Test
	public void getInvalidEnumTest() {
		// when
		BSSProcessTypeToSNFeatureMapping bSSProcessTypeToSNFeatureMapping = BSSProcessTypeToSNFeatureMapping
				.get("InValid");
		// then
		assertNotNull(bSSProcessTypeToSNFeatureMapping);
		assertEquals(BSSProcessTypeToSNFeatureMapping.UNKNOWN.getBssProcessType(),
				bSSProcessTypeToSNFeatureMapping.getBssProcessType());
		assertEquals(BSSProcessTypeToSNFeatureMapping.UNKNOWN.getServiceNowFeatureName(),
				bSSProcessTypeToSNFeatureMapping.getServiceNowFeatureName());
	}

}
