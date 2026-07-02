package com.trinet.ambis.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.service.model.AdditionalBenefitOffer;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.BenefitOfferSummary;
import com.trinet.ambis.service.model.BenefitPlan;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class SubmitUtilTest {

	String medicalPlan = "MEDPLAN";
	String dentalPlan = "DENTALPLAN";
	String visionPlan = "VISIONPLAN";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void findBandCode() {

		String actualResult;

		actualResult = SubmitUtil.getCoverageCode(CoverageCodesEnums.COV_EMPLOYEE_PLUS_ONE.getId());
		assertEquals(CoverageCodesEnums.COV_EMPLOYEE_PLUS_ONE.getCode(), actualResult);

		actualResult = SubmitUtil.getCoverageCode(CoverageCodesEnums.COV_EMPLOYEE.getId());
		assertEquals(CoverageCodesEnums.COV_EMPLOYEE.getCode(), actualResult);

		actualResult = SubmitUtil.getCoverageCode(CoverageCodesEnums.COV_FAMILY.getId());
		assertEquals(CoverageCodesEnums.COV_FAMILY.getCode(), actualResult);

		actualResult = SubmitUtil.getCoverageCode(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId());
		assertEquals(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode(), actualResult);

		actualResult = SubmitUtil.getCoverageCode(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId());
		assertEquals(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode(), actualResult);

		actualResult = SubmitUtil.getCoverageCode(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId());
		assertEquals(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode(), actualResult);

		actualResult = SubmitUtil.getCoverageCode(CoverageCodesEnums.COV_EMPLOYEE_PLUS_ONE.getId());
		assertEquals(CoverageCodesEnums.COV_EMPLOYEE_PLUS_ONE.getCode(), actualResult);

		actualResult = SubmitUtil.getCoverageCode(CoverageCodesEnums.COV_ALL.getId());
		assertEquals("Z", actualResult);

		actualResult = SubmitUtil.getCoverageCode("INVALID");
		assertNull(actualResult);
	}

	@Test
	public void getSelectedBenefitPlans() {

		BenefitOffer benefitOffer;
		BenefitOfferSummary summary;
		BenefitPlan benefitPlan;
		AdditionalBenefitOffer additionalBenefitOffer;
		Set<String> actualResult;

		// Medical
		benefitOffer = new BenefitOffer();
		summary = new BenefitOfferSummary();
		summary.setType(BSSApplicationConstants.MEDICAL);
		benefitPlan = new BenefitPlan();
		benefitPlan.setId(medicalPlan);
		benefitOffer.setSummary(summary);
		benefitOffer.setBenefitPlans(Arrays.asList(benefitPlan));
		actualResult = SubmitUtil.getSelectedBenefitPlans(benefitOffer);
		assertEquals(1, actualResult.size());
		assertEquals(medicalPlan, actualResult.iterator().next());

		// Dental
		benefitOffer = new BenefitOffer();
		summary = new BenefitOfferSummary();
		summary.setType(BSSApplicationConstants.DENTAL);
		benefitPlan = new BenefitPlan();
		benefitPlan.setId(dentalPlan);
		benefitOffer.setSummary(summary);
		benefitOffer.setBenefitPlans(Arrays.asList(benefitPlan));
		actualResult = SubmitUtil.getSelectedBenefitPlans(benefitOffer);
		assertEquals(1, actualResult.size());
		assertEquals(dentalPlan, actualResult.iterator().next());

		// Vision
		benefitOffer = new BenefitOffer();
		summary = new BenefitOfferSummary();
		summary.setType(BSSApplicationConstants.VISION);
		benefitPlan = new BenefitPlan();
		benefitPlan.setId(visionPlan);
		benefitOffer.setSummary(summary);
		benefitOffer.setBenefitPlans(Arrays.asList(benefitPlan));
		actualResult = SubmitUtil.getSelectedBenefitPlans(benefitOffer);
		assertEquals(1, actualResult.size());
		assertEquals(visionPlan, actualResult.iterator().next());

		// ADDITIONAL
		benefitOffer = new BenefitOffer();
		summary = new BenefitOfferSummary();
		summary.setType(BSSApplicationConstants.ADDITIONAL);
		additionalBenefitOffer = new AdditionalBenefitOffer();
		benefitOffer.setSummary(summary);
		benefitOffer.setAdditionalBenefitOffers(Arrays.asList(additionalBenefitOffer));
		actualResult = SubmitUtil.getSelectedBenefitPlans(benefitOffer);
		assertTrue(actualResult.isEmpty());
	}

	@Test
	public void isEmployeePaid() {

		BenefitOffer benefitOffer;
		BenefitPlan benefitPlan;
		boolean actualResult;

		// Empty benefitPlans
		benefitOffer = new BenefitOffer();
		actualResult = SubmitUtil.isEmployeePaid(benefitOffer);
		assertFalse(actualResult);

		// Populated benefitPlans
		benefitOffer = new BenefitOffer();
		benefitPlan = new BenefitPlan();
		benefitPlan.setEmployeePaid(true);
		benefitOffer.setBenefitPlans(Arrays.asList(benefitPlan));
		actualResult = SubmitUtil.isEmployeePaid(benefitOffer);
		assertTrue(actualResult);
	}
	
	@Test(expected = InvocationTargetException.class)
	public void privateConstructorTest()
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<?> constructor = SubmitUtil.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		constructor.newInstance();
	}
}
