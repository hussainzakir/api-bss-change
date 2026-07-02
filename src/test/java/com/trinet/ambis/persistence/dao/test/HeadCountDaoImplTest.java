package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.dao.hrp.impl.HeadCountDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.util.RulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class HeadCountDaoImplTest {

	@InjectMocks
	HeadCountDaoImpl headCountDao;

	@Mock
	EntityManager em = null;

	@Mock
	Query mockedQuery = null;

	private Long strategyId = 1L;
	private long realmYearId = 0;
	Map<String, Map<String, Set<Long>>> benefitGroupPlanTypePortfolio = new HashMap<>();

    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

    @Before
    public void setup() {
        mockedQuery = mock(Query.class);
        when(em.createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);

        rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
        rulesAndConfigsUtilsMockedStatic.when(() -> RulesAndConfigsUtils.getSDIStates(Mockito.anyLong()))
                .thenReturn(new HashSet<>(Arrays.asList("NJ", "RI", "CA", "HI")));
    }

    @After
    public void tearDown() {
        rulesAndConfigsUtilsMockedStatic.close();
    }

	@Test
	public void getEmployeeCountByBenefitGroup() {
		List<Object[]> mockedResult = prepareEmployeeCountMockData(false);
		when(mockedQuery.getResultList()).thenReturn(mockedResult);
		Map<String, Integer> actualResult = headCountDao.getEmployeeCountByBenefitGroup("CODE", realmYearId);
		assertEquals(3, actualResult.size());
	}

	@Test
	public void getPlanCoverageLevelHeadCountByGroup() {
		List<Object[]> mockedResult = preparePlanCoverageLevelCountMockData();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> actualResult = headCountDao
				.getPlanCoverageLevelHeadCountByGroup("CODE", realmYearId, false);
		assertEquals(2, actualResult.size());
		assertEquals(2, actualResult.get("PROGRAM_1").size());
		assertEquals(1, actualResult.get("PROGRAM_2").size());
	}
	
	
	@Test
	public void getPlanCoverageLevelHeadCountByGroupMapped() {
		List<Object[]> mockedResult = preparePlanCoverageLevelCountMockData();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> actualResult = headCountDao
				.getPlanCoverageLevelHeadCountByGroup("CODE", realmYearId, true);
		assertEquals(2, actualResult.size());
		assertEquals(2, actualResult.get("PROGRAM_1").size());
		assertEquals(1, actualResult.get("PROGRAM_2").size());
	}

	@Test
	public void getWaiverHeadCountByBenefitProgram() {
		Company company = new Company();
		company.setCode( "CODE" );
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId( 1L );
		company.setRealmPlanYear( rpy );
		boolean history = true;
		List<Object[]> mockedResult = prepareEmployeeCountMockData(history);
		when(mockedQuery.getResultList()).thenReturn(mockedResult);
		Map<String, Long> actualResult = headCountDao.getWaiverHeadCountByBenefitProgram( company, strategyId,
				history);
		assertEquals(2, actualResult.size());

		history = false;
		mockedResult = prepareEmployeeCountMockData(history);
		when(mockedQuery.getResultList()).thenReturn(mockedResult);
		actualResult = headCountDao.getWaiverHeadCountByBenefitProgram(company, strategyId, history);
		assertEquals(3, actualResult.size());
	}
	
	@Test
	public void getEligibleEmployeeCount() {
		List<Object[]> mockedResult = prepareEligibleEmployeeCountMockData();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);
       
		Company company = prepareCompany("TriNet III", "CA");
		boolean history = true;
		RealmPlanYear rlm = prepareRealmPlanYear();
		 when(RulesAndConfigsUtils.isDisabledBundledOn(rlm.getId())).thenReturn(true);
		Map<String, ActiveEligibleEECount> actualResult = headCountDao.getEligibleEmployeeCount(company, strategyId,
				history);
		assertEquals(2, actualResult.size());
		assertEquals(4, actualResult.get("PROGRAM_1").getPrimaryHeadCount());
		assertEquals(6, actualResult.get("PROGRAM_1").getSecondaryHeadCount());
		assertEquals(3, actualResult.get("PROGRAM_2").getPrimaryHeadCount());
		assertEquals(7, actualResult.get("PROGRAM_2").getSecondaryHeadCount());

		company = prepareCompany("TriNet III", "FL");
		rlm = prepareRealmPlanYear();
		when(RulesAndConfigsUtils.isDisabledBundledOn(rlm.getId())).thenReturn(true);
		history = true;
		actualResult = headCountDao.getEligibleEmployeeCount(company, strategyId,
				history);
		assertEquals(2, actualResult.size());
		assertEquals(6, actualResult.get("PROGRAM_1").getPrimaryHeadCount());
		assertEquals(4, actualResult.get("PROGRAM_1").getSecondaryHeadCount());
		assertEquals(7, actualResult.get("PROGRAM_2").getPrimaryHeadCount());
		assertEquals(3, actualResult.get("PROGRAM_2").getSecondaryHeadCount());

		history = false;
		company = prepareCompany("NOT_BUNDLED", "CA");
		company.setHeadQuatersState("FL");
		rlm = prepareRealmPlanYear();
		when(RulesAndConfigsUtils.isDisabledBundledOn(rlm.getId())).thenReturn(false);
		actualResult = headCountDao.getEligibleEmployeeCount(company, strategyId,
				history);
		assertEquals(2, actualResult.size());
		assertEquals(0, actualResult.get("PROGRAM_1").getPrimaryHeadCount());
		assertEquals(0, actualResult.get("PROGRAM_1").getSecondaryHeadCount());
		assertEquals(10, actualResult.get("PROGRAM_1").getTotalHeadCount());
		assertEquals(0, actualResult.get("PROGRAM_2").getPrimaryHeadCount());
		assertEquals(0, actualResult.get("PROGRAM_2").getSecondaryHeadCount());
		assertEquals(10, actualResult.get("PROGRAM_2").getTotalHeadCount());

		history = false;
		company = prepareCompany("NOT_BUNDLED", "FL");
		company.setHeadQuatersState("FL");
		rlm = prepareRealmPlanYear();
		when(RulesAndConfigsUtils.isDisabledBundledOn(rlm.getId())).thenReturn(false);
		actualResult = headCountDao.getEligibleEmployeeCount(company, strategyId,
				history);
		assertEquals(2, actualResult.size());
		assertEquals(0, actualResult.get("PROGRAM_1").getPrimaryHeadCount());
		assertEquals(0, actualResult.get("PROGRAM_1").getSecondaryHeadCount());
		assertEquals(10, actualResult.get("PROGRAM_1").getTotalHeadCount());
		assertEquals(0, actualResult.get("PROGRAM_2").getPrimaryHeadCount());
		assertEquals(0, actualResult.get("PROGRAM_2").getSecondaryHeadCount());
		assertEquals(10, actualResult.get("PROGRAM_2").getTotalHeadCount());
	}
	

	@Test
	public void geEnrolledHeadCountByBenefitProgram() {
		Company company = new Company();
		company.setCode("CODE");
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId( 1L );
		company.setRealmPlanYear( rpy );
		boolean history = true;
		List<Object[]> mockedResult = prepareEmployeeCountMockData(history);
		when(mockedQuery.getResultList()).thenReturn(mockedResult);
		Map<String, Integer> actualResult = headCountDao.geEnrolledHeadCountByBenefitProgram(company, strategyId,
				history);
		assertEquals(2, actualResult.size());

		history = false;
		mockedResult = prepareEmployeeCountMockData(history);
		when(mockedQuery.getResultList()).thenReturn(mockedResult);
		actualResult = headCountDao.geEnrolledHeadCountByBenefitProgram(company, strategyId, history);
		assertEquals(3, actualResult.size());
	}

	private List<Object[]> prepareEmployeeCountMockData(boolean history) {
		List<Object[]> data = new ArrayList<>();
		Object[] r = new Object[2];
		r[0] = "PROGRAM_1";
		r[1] = new BigDecimal(21);
		data.add(r);

		r = new Object[2];
		r[0] = "PROGRAM_2";
		r[1] = new BigDecimal(5);
		data.add(r);

		if (!history) {
			r = new Object[2];
			r[0] = "PROGRAM_3";
			r[1] = new BigDecimal(7);
			data.add(r);
		}
		return data;
	}

	private List<Object[]> preparePlanCoverageLevelCountMockData() {
		List<Object[]> data = new ArrayList<>();
		Object[] r = new Object[6];
		r[0] = "PROGRAM_1";
		r[1] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
		r[2] = "MEDICAL_PLAN_1";
		r[3] = CoverageCodesEnums.COV_EMPLOYEE.getCode();
		r[4] = new BigDecimal(5);
		r[5] = new BigDecimal(1);
		data.add(r);

		r = new Object[6];
		r[0] = "PROGRAM_1";
		r[1] = BSSApplicationConstants.DENTAL_PLAN_TYPE;
		r[2] = "DENTAL_PLAN_1";
		r[3] = CoverageCodesEnums.COV_EMPLOYEE.getCode();
		r[4] = new BigDecimal(7);
		r[5] = new BigDecimal(0);
		data.add(r);

		r = new Object[6];
		r[0] = "PROGRAM_1";
		r[1] = BSSApplicationConstants.DENTAL_PLAN_TYPE;
		r[2] = "DENTAL_PLAN_1";
		r[3] = CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode();
		r[4] = new BigDecimal(2);
		r[5] = new BigDecimal(0);
		data.add(r);

		r = new Object[6];
		r[0] = "PROGRAM_2";
		r[1] = BSSApplicationConstants.DENTAL_PLAN_TYPE;
		r[2] = "MEDICAL_PLAN_2";
		r[3] = CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode();
		r[4] = new BigDecimal(21);
		r[5] = new BigDecimal(1);
		data.add(r);

		return data;
	}

	private List<Object[]> prepareEligibleEmployeeCountMockData() {
		List<Object[]> data = new ArrayList<>();
		Object[] r = new Object[3];
		r[0] = "PROGRAM_1";
		r[1] = "IN";
		r[2] = new BigDecimal(4);
		data.add(r);
		
		r = new Object[3];
		r[0] = "PROGRAM_1";
		r[1] = "OUT";
		r[2] = new BigDecimal(6);
		data.add(r);
		
		r = new Object[3];
		r[0] = "PROGRAM_2";
		r[1] = "IN";
		r[2] = new BigDecimal(3);
		data.add(r);
		
		r = new Object[3];
		r[0] = "PROGRAM_2";
		r[1] = "OUT";
		r[2] = new BigDecimal(7);
		data.add(r);

		return data;
	}	
	private Company prepareCompany(String exchange, String hqState) {
		Company company = new Company();
		company.setHeadQuatersState(hqState);
		company.setRealmPlanYear( prepareRealmPlanYear() );
		company.setRealmPlanYearId( company.getRealmPlanYear().getId() );
		
		Realm realm = new Realm();
		realm.setBenExchange(exchange);
		company.setRealm(realm);
		
		return company;
	}
	
	private RealmPlanYear prepareRealmPlanYear() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(realmYearId);
		realmPlanYear.setPlanYearStart(new Date());
		realmPlanYear.setPlanYearEnd(new Date());
		return realmPlanYear;
	}

}
