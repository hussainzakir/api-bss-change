package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.plancompare.dao.hrp.impl.PlanCompareDaoImpl;
import com.trinet.ambis.persistence.plancompare.model.BenefitPlanDetailDto;
import com.trinet.ambis.persistence.plancompare.model.MappedPlanDetailDto;
import com.trinet.ambis.persistence.plancompare.model.PlanYearDetailDto;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.DaoUtils;

@RunWith(MockitoJUnitRunner.class)
public class PlanCompareDaoImplTest extends ServiceUnitTest{

	PlanCompareDaoImpl planCompareDao;
	
	EntityManager em = null;
	Query mockedQuery = null;
	
	private final String CODE = "001";
	private final String REALM_YEAR_ID = "61";
	private final String PREV_REALM_YEAR_ID = "51";
	private final String CURREN_FUTURE_PLAN_YEAR_DETAILS = "CURREN_FUTURE_PLAN_YEAR_DETAILS";
	private final String CURRENT_YEAR_PLANS = "CURRENT_YEAR_PLANS";
	private final String FUTURE_YEAR_PLANS = "FUTURE_YEAR_PLANS";
	private final String MAPPING_PLANS = "MAPPING_PLANS";

    private MockedStatic<DaoUtils> daoUtilsMockedStatic;

    @Before
    public void setup() {
        planCompareDao = new PlanCompareDaoImpl();
        daoUtilsMockedStatic = org.mockito.Mockito.mockStatic(DaoUtils.class);
        em = mock(EntityManager.class);
        mockedQuery = mock(Query.class);
        when(em.createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
        planCompareDao.setEm(em);
    }

    @After
    public void tearDown() {
        if (daoUtilsMockedStatic != null)
        daoUtilsMockedStatic.close();
    }

	@Test
	public void getCompanyPlanYearsIsNULL() {
		List<Object[]> planYearsStatuses = new ArrayList<>();
		
		when(em.createNamedQuery(CURREN_FUTURE_PLAN_YEAR_DETAILS)).thenReturn(mockedQuery);
		when(DaoUtils.getResultList(mockedQuery, CURREN_FUTURE_PLAN_YEAR_DETAILS)).thenReturn(planYearsStatuses);
		
		List<PlanYearDetailDto> companyPlanYears = planCompareDao.findPlanYearDetailsBy(CODE, REALM_YEAR_ID, 0, 0);

		assertNull(companyPlanYears);
	}
	
	@Test
	public void getCompanyPlanYearsNotNULL() {
		List<Object[]> planYearsStatuses = new ArrayList<>();
		planYearsStatuses.add(getObject().get());
		
		when(em.createNamedQuery(CURREN_FUTURE_PLAN_YEAR_DETAILS)).thenReturn(mockedQuery);
		when(DaoUtils.getResultList(mockedQuery, CURREN_FUTURE_PLAN_YEAR_DETAILS)).thenReturn(planYearsStatuses);
		
		
		List<PlanYearDetailDto> companyPlanYears = planCompareDao.findPlanYearDetailsBy(CODE, REALM_YEAR_ID, 0, 0);
		
		assertNotNull(companyPlanYears);
		assertEquals(1,companyPlanYears.size());
		assertEquals("current",planYearsStatuses.get(0)[0]);
		assertEquals("56",planYearsStatuses.get(0)[1]);
	}
	
	@Test
	public void getYearPlansForCurrentYear() {
		List<Object[]> planYearsStatuses = new ArrayList<>();
		planYearsStatuses.add(getCurrentYearPlansObject().get());
		
		when(em.createNamedQuery(CURRENT_YEAR_PLANS)).thenReturn(mockedQuery);
		when(DaoUtils.getResultList(mockedQuery, CURRENT_YEAR_PLANS)).thenReturn(planYearsStatuses);
		
		List<BenefitPlanDetailDto> companyPlanYears = planCompareDao.findSubmittedStrategyPlansBy(CODE, REALM_YEAR_ID);
		
		assertNotNull(companyPlanYears);
		assertEquals(1,companyPlanYears.size());
		assertEquals("Medical",planYearsStatuses.get(0)[0]);
		assertEquals("PPO",planYearsStatuses.get(0)[1]);
	}
	
	@Test
	public void getYearPlansForFeatureYear() {
		List<Object[]> planYearsStatuses = new ArrayList<>();
		planYearsStatuses.add(getCurrentYearPlansObject().get());
		
		when(em.createNamedQuery(FUTURE_YEAR_PLANS)).thenReturn(mockedQuery);
		when(DaoUtils.getResultList(mockedQuery, FUTURE_YEAR_PLANS)).thenReturn(planYearsStatuses);
		
		List<BenefitPlanDetailDto> companyPlanYears = planCompareDao.findAllFutureYearPlansBy(REALM_YEAR_ID);
		
		assertNotNull(companyPlanYears);
		assertEquals(1, companyPlanYears.size());
		assertEquals("Medical",planYearsStatuses.get(0)[0]);
		assertEquals("PPO",planYearsStatuses.get(0)[1]);
	}
	
	@Test
	public void findMappingBenefitPlansBy() {
		List<Object[]> planYearsStatuses = new ArrayList<>();
		planYearsStatuses.add(getMappedPlansObject().get());
		
		when(em.createNamedQuery(MAPPING_PLANS)).thenReturn(mockedQuery);
		when(DaoUtils.getResultList(mockedQuery, MAPPING_PLANS)).thenReturn(planYearsStatuses);
		
		List<MappedPlanDetailDto> companyPlanYears = planCompareDao.findMappingBenefitPlansBy(REALM_YEAR_ID, PREV_REALM_YEAR_ID);
		
		assertNotNull(companyPlanYears);
		assertEquals(1, companyPlanYears.size());
		assertEquals("Medical",planYearsStatuses.get(0)[0]);
		assertEquals("PPO",planYearsStatuses.get(0)[1]);
	}
	
	private Supplier<Object[]> getObject(){
		return () -> {
			Object[] planCompareObjects = new Object[4];
			planCompareObjects[0] = "current";
			planCompareObjects[1] = "56";
			planCompareObjects[2] = "27-10-2022";
			planCompareObjects[3] = "27-10-2023";
			return planCompareObjects;
		};
	}
	
	private Supplier<Object[]> getCurrentYearPlansObject(){
		return () -> {
			Object[] currentYearPlan = new Object[3];
			currentYearPlan[0] = "Medical";
			currentYearPlan[1] = "PPO";
			currentYearPlan[2] = "Medical";
			return currentYearPlan;
		};
	}
	
	private Supplier<Object[]> getMappedPlansObject(){
		return () -> {
			Object[] currentYearPlan = new Object[4];
			currentYearPlan[0] = "Medical";
			currentYearPlan[1] = "PPO";
			currentYearPlan[2] = "Medical";
			currentYearPlan[3] = "10";
			return currentYearPlan;
		};
	}
}
