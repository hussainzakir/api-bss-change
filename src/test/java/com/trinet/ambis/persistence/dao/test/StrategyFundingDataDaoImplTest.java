package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.ExcessOptionEnum;
import com.trinet.ambis.persistence.dao.hrp.impl.StrategyFundingDataDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.ModelCompareStrategy;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class StrategyFundingDataDaoImplTest {

	@InjectMocks
	StrategyFundingDataDaoImpl strategyFundingDataDaoImpl;

	@Mock
	EntityManager em;

	@Mock
	EntityManager entityManager;

	private Company company = new Company();

	private Query mockedQuery = null;
	private Query mockedQuery1 = null;
	private Query mockedQuery2 = null;
	private Query mockedQuery3 = null;
	
	Long companyId = 1L;

	Long strategy1 = 37318L;
	Long strategy2 = 37319L;
	
	Long group1 = 1000L;
	Long group2 = 2000L;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockedQuery = mock(Query.class);
		mockedQuery1 = mock(Query.class);
		mockedQuery2 = mock(Query.class);
		mockedQuery3 = mock(Query.class);
		when(em.createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
		when(entityManager.createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
		company.setId(companyId);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setPlanYearEnd(new Date());
		company.setCode("XYZ");
		company.setRealmPlanYear(realmPlanYear);
	}

	@Test
	public void getBsuppVolPlanTypes() {
		when(mockedQuery.getResultList()).thenReturn(prepareBsuppVolPlanTypesData());
		List<String> actualResult = strategyFundingDataDaoImpl.getBsuppStrategyFundVolPlanTypes(2L);
		assertEquals(2, actualResult.size());
	}

	@Test
	public void getFundingDetailsByStrategyIdTest() {
		Company company = new Company();
		RealmPlanYear rpy = new RealmPlanYear();
		company.setRealmPlanYear(rpy);
		Realm realm = new Realm();
		realm.setId(1);
		company.setRealm(realm);

		when(em.createNamedQuery("getMCFundingDetailsByStrategyId")).thenReturn(mockedQuery);
		when(em.createNamedQuery("MC_STRATEGY_FLAT_MAX_CONTRIBUTIONS")).thenReturn(mockedQuery1);		
		when(em.createNamedQuery("MC_STRATEGY_BP_LIMITS")).thenReturn(mockedQuery2);	
		when(em.createNamedQuery("STRATEGY_FUNDING_BEN_SUPP_EXCESS_PLAN_TYPES")).thenReturn(mockedQuery3);	

		when(mockedQuery.getResultList()).thenReturn(prepareMCFundingDetailsMockData());
		when(mockedQuery1.getResultList()).thenReturn(prepareStrategyFlatMaxMockData());
		when(mockedQuery2.getResultList()).thenReturn(prepareStrategyFlatMaxMockData());
		when(mockedQuery3.getResultList()).thenReturn(prepareModelCompareBenSuppExcessOption());

		Map<Long, ModelCompareStrategy> actualResults = strategyFundingDataDaoImpl.getFundingDetailsByStrategyId(Arrays.asList(strategy1), company, false, new Date());

		verify(mockedQuery, times(1)).getResultList();
		assertEquals("Future Benefits Solution", actualResults.get(strategy1).getName());
		assertEquals(4, actualResults.get(strategy1).getGroupFundingList().get(0).getOfferTypeFunding().get(BSSApplicationConstants.MEDICAL).getCoverageLevelFundingFlatMax().size());
		assertEquals(Long.valueOf(ExcessOptionEnum.FORFEIT.getType()), actualResults.get(strategy1).getGroupFundingList().get(1).getOfferTypeFunding().get(BSSApplicationConstants.MEDICAL).getExcessOption().getOptionId());
		assertEquals(3, actualResults.get(strategy1).getGroupFundingList().get(1).getOfferTypeFunding().get(BSSApplicationConstants.MEDICAL).getExcessOption().getExcessVoluntaryPlanTypes().size());
	}	

	@Test
	public void getFundingDetailsByStrategyIdTest2() {
		Company company = new Company();
		RealmPlanYear rpy = new RealmPlanYear();
		company.setRealmPlanYear(rpy);
		Realm realm = new Realm();
		realm.setId(1);
		company.setRealm(realm);

		when(em.createNamedQuery("getMCFundingDetailsByStrategyId")).thenReturn(mockedQuery);
		when(em.createNamedQuery("MC_STRATEGY_FLAT_MAX_CONTRIBUTIONS")).thenReturn(mockedQuery1);
		when(em.createNamedQuery("MC_STRATEGY_BP_LIMITS")).thenReturn(mockedQuery2);
		when(em.createNamedQuery("STRATEGY_FUNDING_BEN_SUPP_EXCESS_PLAN_TYPES")).thenReturn(mockedQuery3);

		when(mockedQuery.getResultList()).thenReturn(prepareMCFundingDetailsMockDataWithEmptyPlanType());
		when(mockedQuery1.getResultList()).thenReturn(prepareStrategyFlatMaxMockData());
		when(mockedQuery2.getResultList()).thenReturn(prepareStrategyFlatMaxMockData());
		when(mockedQuery3.getResultList()).thenReturn(prepareModelCompareBenSuppExcessOption());

		Map<Long, ModelCompareStrategy> actualResults = strategyFundingDataDaoImpl.getFundingDetailsByStrategyId(Arrays.asList(strategy1), company, false, new Date());

		verify(mockedQuery, times(1)).getResultList();
		assertEquals("Future Benefits Solution", actualResults.get(strategy1).getName());
		assertEquals(0, actualResults.get(strategy1).getGroupFundingList().get(0).getOfferTypeFunding().size());
	}

	@Test
	public void getEecFundingTest() {
		when(em.createNamedQuery("GET_EEC_FUNDING")).thenReturn(mockedQuery);
		when(mockedQuery.getResultList()).thenReturn(prepareEecFundingData());
		Map<String, Set<String>> actualResults = strategyFundingDataDaoImpl.getEecFunding("COMPANY_CODE", 1);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResults.get("BENEFIT_GROUP_1").size());
		assertEquals(1, actualResults.get("BENEFIT_GROUP_2").size());
	}

	@Test
	public void hasFundingOverridesTest() {
		when(em.createNamedQuery("PLAN_LEVEL_FUNDING_OVERRIDES_BY_STRATEGY_GROUP")).thenReturn(mockedQuery);
		when(mockedQuery.getResultList()).thenReturn(null);
		Map<Long, Set<String>> actualResults = strategyFundingDataDaoImpl.getPlanLevelOverrides(1);
		assertEquals(true, actualResults == null || actualResults.isEmpty());

		when(mockedQuery.getResultList()).thenReturn(preparePlanLevelFundingData());
		actualResults = strategyFundingDataDaoImpl.getPlanLevelOverrides(1);
		assertEquals(true, actualResults != null && !actualResults.isEmpty());
		assertEquals(2, actualResults.get(1234L).size());

		verify(mockedQuery, times(2)).getResultList();
	}

	private List<Object[]> prepareBsuppVolPlanTypesData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[1];
		r[0] = "1D";
		results.add(r);
		r = new Object[1];
		r[0] = "1V";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareMCFundingDetailsMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[20];
		r[0] = BigDecimal.valueOf(companyId);
		r[1] = BigDecimal.valueOf(strategy1);
		r[2] = BigDecimal.valueOf(group1);
		r[3] = "BENEFITPROGRAM1";
		r[4] = "All Employees";
		r[5] = "1st of month on/after DOH";
		r[6] = BigDecimal.valueOf(1);
		r[7] = "11";
		r[8] = "BFPCT";
		r[9] = "000SR8";
		r[10] = null;
		r[11] = null;
		r[12] = null;
		r[13] = null;
		r[14] = "all";
		r[15] = BigDecimal.valueOf(100);
		r[16] = "Future Benefits Solution";
		r[17] = "MetLife Premium";
		r[18] = "All Levels";
		results.add(r);

		r = new Object[20];
		r[0] = BigDecimal.valueOf(companyId);
		r[1] = BigDecimal.valueOf(strategy1);
		r[2] = BigDecimal.valueOf(group1);
		r[3] = "BENEFITPROGRAM1";
		r[4] = "All Employees";
		r[5] = "1st of month on/after DOH";
		r[6] = BigDecimal.valueOf(2);
		r[7] = "10";
		r[8] = "BFPCT";
		r[9] = "001EKV";
		r[10] = BigDecimal.valueOf(350);
		r[11] = null;
		r[12] = null;
		r[13] = null;
		r[14] = "all";
		r[15] = BigDecimal.valueOf(100);
		r[16] = "Future Benefits Solution";
		r[17] = "UHC Enhanced";
		r[18] = "All Levels";
		results.add(r);

		r = new Object[20];
		r[0] = BigDecimal.valueOf(companyId);
		r[1] = BigDecimal.valueOf(strategy1);
		r[2] = BigDecimal.valueOf(group1);
		r[3] = "BENEFITPROGRAM1";
		r[4] = "All Employees";
		r[5] = "1st of month on/after DOH";
		r[6] = BigDecimal.valueOf(3);
		r[7] = "14";
		r[8] = "BFPCT";
		r[9] = "000SRB";
		r[10] = null;
		r[11] = null;
		r[12] = null;
		r[13] = null;
		r[14] = "all";
		r[15] = BigDecimal.valueOf(100);
		r[16] = "Future Benefits Solution";
		r[17] = "VSP Vision Standard";
		r[18] = "All Levels";
		results.add(r);
		
		// BSUPP FUNDING TYPE
		r = new Object[20];
		r[0] = BigDecimal.valueOf(companyId);
		r[1] = BigDecimal.valueOf(strategy1);
		r[2] = BigDecimal.valueOf(group2);
		r[3] = "BENEFITPROGRAM2";
		r[4] = "Other Employees";
		r[5] = "1st of month on/after DOH";
		r[6] = BigDecimal.valueOf(5);
		r[7] = "10";
		r[8] = "BSUPP";
		r[9] = null;
		r[10] = null;
		r[11] = BigDecimal.valueOf(ExcessOptionEnum.FORFEIT.getType());
		r[12] = ExcessOptionEnum.FORFEIT.getCode();
		r[13] = ExcessOptionEnum.FORFEIT.getName();
		r[14] = CoverageCodesEnums.COV_EMPLOYEE.getId();
		r[15] = BigDecimal.valueOf(1000);
		r[16] = "Future Benefits Solution";
		r[17] = null;
		r[18] = CoverageCodesEnums.COV_EMPLOYEE.getName();
		results.add(r);

		r = new Object[20];
		r[0] = BigDecimal.valueOf(companyId);
		r[1] = BigDecimal.valueOf(strategy1);
		r[2] = BigDecimal.valueOf(group2);
		r[3] = "BENEFITPROGRAM2";
		r[4] = "Other Employees";
		r[5] = "1st of month on/after DOH";
		r[6] = BigDecimal.valueOf(5);
		r[7] = "10";
		r[8] = "BSUPP";
		r[9] = null;
		r[10] = null;
		r[11] = BigDecimal.valueOf(ExcessOptionEnum.FORFEIT.getType());
		r[12] = ExcessOptionEnum.FORFEIT.getCode();
		r[13] = ExcessOptionEnum.FORFEIT.getName();
		r[14] = CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId();
		r[15] = BigDecimal.valueOf(1000);
		r[16] = "Future Benefits Solution";
		r[17] = null;
		r[18] = CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getName();
		results.add(r);

		r = new Object[20];
		r[0] = BigDecimal.valueOf(companyId);
		r[1] = BigDecimal.valueOf(strategy1);
		r[2] = BigDecimal.valueOf(group2);
		r[3] = "BENEFITPROGRAM2";
		r[4] = "Other Employees";
		r[5] = "1st of month on/after DOH";
		r[6] = BigDecimal.valueOf(5);
		r[7] = "10";
		r[8] = "BSUPP";
		r[9] = null;
		r[10] = null;
		r[11] = BigDecimal.valueOf(ExcessOptionEnum.FORFEIT.getType());
		r[12] = ExcessOptionEnum.FORFEIT.getCode();
		r[13] = ExcessOptionEnum.FORFEIT.getName();
		r[14] = CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId();
		r[15] = BigDecimal.valueOf(1000);
		r[16] = "Future Benefits Solution";
		r[17] = null;
		r[18] = CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getName();
		results.add(r);

		r = new Object[20];
		r[0] = BigDecimal.valueOf(companyId);
		r[1] = BigDecimal.valueOf(strategy1);
		r[2] = BigDecimal.valueOf(group2);
		r[3] = "BENEFITPROGRAM2";
		r[4] = "Other Employees";
		r[5] = "1st of month on/after DOH";
		r[6] = BigDecimal.valueOf(5);
		r[7] = "10";
		r[8] = "BSUPP";
		r[9] = null;
		r[10] = null;
		r[11] = BigDecimal.valueOf(ExcessOptionEnum.FORFEIT.getType());
		r[12] = ExcessOptionEnum.FORFEIT.getCode();
		r[13] = ExcessOptionEnum.FORFEIT.getName();
		r[14] = CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId();
		r[15] = BigDecimal.valueOf(1000);
		r[16] = "Future Benefits Solution";
		r[17] = null;
		r[18] = CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getName();
		results.add(r);
		
		return results;
	}

	private List<Object[]> prepareMCFundingDetailsMockDataWithEmptyPlanType() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[20];
		r[0] = BigDecimal.valueOf(companyId);
		r[1] = BigDecimal.valueOf(strategy1);
		r[2] = BigDecimal.valueOf(group1);
		r[3] = "BENEFITPROGRAM1";
		r[4] = "All Employees";
		r[5] = "1st of month on/after DOH";
		r[6] = BigDecimal.valueOf(1);
		r[7] = "";
		r[8] = "BFPCT";
		r[9] = "000SR8";
		r[10] = null;
		r[11] = null;
		r[12] = null;
		r[13] = null;
		r[14] = "all";
		r[15] = BigDecimal.ZERO;
		r[16] = "Future Benefits Solution";
		r[17] = "MetLife Premium";
		r[18] = "All Levels";
		results.add(r);
		return results;
	}
	
	private List<Object[]> prepareStrategyFlatMaxMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[4];
		r[0] = BigDecimal.valueOf(group1);
		r[1] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
		r[2] = CoverageCodesEnums.COV_EMPLOYEE.getId();
		r[3] = BigDecimal.valueOf(700);
		results.add(r);
		
		r = new Object[4];
		r[0] = BigDecimal.valueOf(group1);
		r[1] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
		r[2] = CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId();
		r[3] = BigDecimal.valueOf(800);
		results.add(r);
		
		r = new Object[4];
		r[0] = BigDecimal.valueOf(group1);
		r[1] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
		r[2] = CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId();
		r[3] = BigDecimal.valueOf(900);
		results.add(r);
		
		r = new Object[4];
		r[0] = BigDecimal.valueOf(group1);
		r[1] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
		r[2] = CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId();
		r[3] = BigDecimal.valueOf(1000);
		results.add(r);
		return results;
	}
	
	private List<Object[]> prepareModelCompareBenSuppExcessOption() {
		List<Object[]> results = new ArrayList<Object[]>();
		
		Object[] r = new Object[2];
		r[0] = BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE;
		r[1] = "Dental";
		results.add(r);

		r = new Object[2];
		r[0] = BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE;
		r[1] = "Vision";
		results.add(r);
		
		r = new Object[2];
		r[0] = BSSApplicationConstants.STD_CODE;
		r[1] = "Short Term Disability";
		results.add(r);
		
		return results;
	}
	
	private List<Object[]> prepareEecFundingData() {
		List<Object[]> results = new ArrayList<Object[]>();
		
		Object[] r = new Object[2];
		r[0] = "BENEFIT_GROUP_1";
		r[1] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
		results.add(r);

		r = new Object[2];
		r[0] = "BENEFIT_GROUP_1";
		r[1] = BSSApplicationConstants.DENTAL_PLAN_TYPE;
		results.add(r);

		r = new Object[2];
		r[0] = "BENEFIT_GROUP_2";
		r[1] = BSSApplicationConstants.DENTAL_PLAN_TYPE;
		results.add(r);
		
		return results;
	}
	
	private List<Object[]> preparePlanLevelFundingData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[2];
		r[0] = BigDecimal.valueOf(1234);
		r[1] = "10";
		results.add(r);
		r = new Object[2];
		r[0] = BigDecimal.valueOf(1234);
		r[1] = "11";
		results.add(r);
		return results;
	}
}
