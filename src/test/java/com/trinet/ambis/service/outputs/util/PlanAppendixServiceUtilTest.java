package com.trinet.ambis.service.outputs.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixFilters;
import com.trinet.ambis.service.impl.outputs.util.PlanAppendixServiceUtil;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.test.config.TestHelper;
import com.trinet.ambis.util.RangeUtils;

@RunWith(MockitoJUnitRunner.class)
public class PlanAppendixServiceUtilTest extends ServiceUnitTest {

    private MockedStatic<RangeUtils> mockStaticRangeUtils;

    @Before
    public void setUp() {
        mockStaticRangeUtils = Mockito.mockStatic(RangeUtils.class);
    }

    @After
    public void tearDown() {
        if (mockStaticRangeUtils != null) mockStaticRangeUtils.close();
    }

	/**
	 * given benefit plan contains single deductible attribute and deductible min is
	 * not provided , deductible max is not provided in the plan appendix filter
	 * <br>
	 * when isSingleINDeductibleFilterApplicable method is called<br>
	 * then return true (include plan in plan appendix report)
	 * 
	 */
	@Test
	public void isSingleINDeductibleFilterApplicableTest1() {
		// given
		// data
		BenefitPlanCompare plan = prepareBenefitPlanCompare(
				"/PlanAppendixServiceUtil/isSingleINDeductibleFilterApplicableTest1.json");
		PlanAppendixFilters planAppendixFilters = preparePlanAppendixFilters();
		// method mocks
		when(RangeUtils.isInRange(any(), any(), any())).thenReturn(Boolean.TRUE);
		// when
		boolean actualResult = PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(plan, planAppendixFilters);
		// then
		// assertions
		assertTrue(actualResult);
		// verify
		verify(RangeUtils.class);
		RangeUtils.isInRange(any(), any(), any());
	}

	/**
	 * given benefit plan do not contains single deductible attribute <br>
	 * when isSingleINDeductibleFilterApplicable method is called<br>
	 * then return false (do not include plan in plan appendix report)
	 * 
	 */
	@Test
	public void isSingleINDeductibleFilterApplicableTest2() {
		// given
		// data
		BenefitPlanCompare plan = prepareBenefitPlanCompare(
				"/PlanAppendixServiceUtil/isSingleINDeductibleFilterApplicableTest2.json");
		PlanAppendixFilters planAppendixFilters = preparePlanAppendixFilters();
		// method mocks
		when(RangeUtils.isInRange(any(), any(), any())).thenReturn(Boolean.TRUE);
		// when
		boolean actualResult = PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(plan, planAppendixFilters);
		// then
		// assertions
		assertFalse(actualResult);
		// verify
		verify(RangeUtils.class, times(0));
		RangeUtils.isInRange(any(), any(), any());
	}

	/**
	 * given benefit plan contains single deductible attribute and deductible min is
	 * provided , deductible max is provided in the plan appendix filter and
	 * isInRange return false <br>
	 * when isSingleINDeductibleFilterApplicable method is called<br>
	 * then return false (do not include plan in plan appendix report)
	 * 
	 */
	@Test
	public void isSingleINDeductibleFilterApplicableTest3() {
		// given
		// data
		BenefitPlanCompare plan = prepareBenefitPlanCompare(
				"/PlanAppendixServiceUtil/isSingleINDeductibleFilterApplicableTest1.json");
		PlanAppendixFilters planAppendixFilters = preparePlanAppendixFilters();
		// method mocks
		when(RangeUtils.isInRange(any(), any(), any())).thenReturn(Boolean.FALSE);
		// when
		boolean actualResult = PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(plan, planAppendixFilters);
		// then
		// assertions
		assertFalse(actualResult);
		// verify
		verify(RangeUtils.class);
		RangeUtils.isInRange(any(), any(), any());
	}

	private BenefitPlanCompare prepareBenefitPlanCompare(String filePath) {
		return TestHelper.readPlanComparisonRequest(filePath, new TypeReference<BenefitPlanCompare>() {
		}).get();
	}

	private PlanAppendixFilters preparePlanAppendixFilters() {
		return PlanAppendixFilters.builder().build();
	}

}
