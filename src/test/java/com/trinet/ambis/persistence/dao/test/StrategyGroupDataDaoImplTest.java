package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.QueryTimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.persistence.dao.hrp.impl.StrategyGroupDataDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.output.AdditionalBenefitPlanDto;
import com.trinet.ambis.util.CommonUtils;

@RunWith(MockitoJUnitRunner.class)
public class StrategyGroupDataDaoImplTest {

	@InjectMocks
	StrategyGroupDataDaoImpl strategyGroupDataDaoImpl;

	@Mock
	EntityManager em;

	@Mock
	EntityManager entityManager;

	private static final Logger logger = LoggerFactory.getLogger(StrategyGroupDataDaoImplTest.class);
	private Company company = new Company();
	private Long strategyId = 1L;
	private boolean history = true;

	private Query mockedQuery = null;

    private MockedStatic<CommonUtils> commonUtilsMockedStatic;

	@Before
	public void setup() {
		mockedQuery = mock(Query.class);
        commonUtilsMockedStatic = org.mockito.Mockito.mockStatic(CommonUtils.class);
        commonUtilsMockedStatic.when(() -> CommonUtils.logExceptions(
                ArgumentMatchers.any(Exception.class),
                ArgumentMatchers.any(org.slf4j.Logger.class),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()
        )).thenAnswer(invocation -> null);
		when(em.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		when(entityManager.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		company.setId(1L);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1);
		realmPlanYear.setPlanYearEnd(new Date());
		company.setCode("XYZ");
		company.setRealmPlanYear(realmPlanYear);
	}

    @After
    public void tearDown() {
        commonUtilsMockedStatic.close();
    }

	@Test
	public void deleteStrategyOutOfLocationPlans() {
		List<String> benefitPlans = Arrays.asList("PLAN_1", "PLAN_2");
		strategyGroupDataDaoImpl.deleteStrategyOutOfLocationPlans(strategyId, benefitPlans);
		verify(mockedQuery, times(2)).executeUpdate();
	}

	@Test
	public void updateEmployeeSelectionsForTerminations() {
		strategyGroupDataDaoImpl.resetStrategyContributionHeadcounts(strategyId);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void updateEmployeeSelectionsForTerminationsCaughtException() throws Exception {
		QueryTimeoutException exception = new QueryTimeoutException();
		doThrow(QueryTimeoutException.class).when(mockedQuery).executeUpdate();
		strategyGroupDataDaoImpl.resetStrategyContributionHeadcounts(strategyId);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void updateStrategyPlanSelectHeadcounts() {
		strategyGroupDataDaoImpl.resetStrategyPlanSelectHeadcounts(strategyId);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void updateStrategyPlanSelectHeadcountsCaughtException() throws Exception {
		QueryTimeoutException exception = new QueryTimeoutException();
		doThrow(QueryTimeoutException.class).when(mockedQuery).executeUpdate();
		strategyGroupDataDaoImpl.resetStrategyPlanSelectHeadcounts(strategyId);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void updateStrategyGroupStatus() {
		strategyGroupDataDaoImpl.updateStrategyGroupStatus(company, strategyId);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void updateStrategyGroupStatusCaughtException() throws Exception {
		QueryTimeoutException exception = new QueryTimeoutException();
		doThrow(QueryTimeoutException.class).when(mockedQuery).executeUpdate();
		strategyGroupDataDaoImpl.updateStrategyGroupStatus(company, strategyId);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void getStrategyWaiverHeadCountHistory() {
		when(mockedQuery.getResultList()).thenReturn(prepareWaiverHcMockData());
		Map<String, Long> actualResult = strategyGroupDataDaoImpl.getStrategyWaiverHeadCount(company, strategyId,
				history);
		assertEquals(1, actualResult.size());
	}

	@Test
	public void getStrategyWaiverHeadCount() {
		history = false;
		when(mockedQuery.getResultList()).thenReturn(prepareWaiverHcMockData());
		Map<String, Long> actualResult = strategyGroupDataDaoImpl.getStrategyWaiverHeadCount(company, strategyId,
				history);
		assertEquals(1, actualResult.size());
	}


	@Test
	public void getStrategyPortfoliosTest() {
		List<BigDecimal> results = getStrategyPortfolios();
		when(mockedQuery.getResultList()).thenReturn(results);
		List<String> portfolios = strategyGroupDataDaoImpl.getMedStrategyPortfolios(strategyId);
		assertEquals(portfolios.size(), 2);
	}

	@Test
	public void getExclMedPlansTest() {
		List<String> results = getExclMedPlanMockData();
		when(mockedQuery.getResultList()).thenReturn(results);
		List<String> exclMedPlans = strategyGroupDataDaoImpl.getExclMedPlanPortfolio(strategyId,
                company.getRealmPlanYear().getId());
		assertEquals(exclMedPlans.get(0), "BCBS");
	}

	@Test
	public void getPortfolioFsaPlansTest() {
		List<String> results = getPortfolioFsaPlans();
		when(mockedQuery.getResultList()).thenReturn(results);
		List<String> strategyPortfolios = new ArrayList<>();
		strategyPortfolios.add("12");
		List<String> portfolios = strategyGroupDataDaoImpl.getPortfolioFsaPlans(company, strategyPortfolios);
		assertEquals(portfolios.size(), 2);
	}

	@Test
	public void getStrategyPortfoliosByPlanType() {
		when(mockedQuery.getResultList()).thenReturn(prepareStrategyPortfoliosByPlanTypeMockData());
		Map<String, Map<String, Set<Long>>> actualResult = strategyGroupDataDaoImpl
				.getStrategyPortfoliosByPlanType(strategyId);
		assertEquals(2, actualResult.size());
	}

	@Test
	public void getAdditionalBenPlanSelectionsTest1() {
		// basic test with life and disability plans and selections
		List<Object[]> result = prepareDisabilitySelections();
		result.addAll(prepareLifePlanSelections());
		when(mockedQuery.getResultList()).thenReturn(result);

		// calling getAdditionalBenPlanSelections...
		Map<String, List<AdditionalBenefitPlanDto>> actualResult =
				strategyGroupDataDaoImpl.getAdditionalBenPlanSelections(strategyId, company.getRealmPlanYear().getId());

		// ...should produce a dataset with disability bundle selected and life plan selected
		assertEquals(1, actualResult.size());
		List<AdditionalBenefitPlanDto> selectedDisability = actualResult.get("BPG001").stream()
				.filter(p -> !p.getBundleId().equals(""))
				.filter(p -> p.isSelected())
				.collect(Collectors.toList());
		assertEquals(2, selectedDisability.size());
		assertEquals("1673", selectedDisability.get(0).getBundleId());

		List<AdditionalBenefitPlanDto> selectedList = actualResult.get("BPG001").stream()
				.filter(p -> p.getPlanType().equals("23"))
				.filter(p -> p.isSelected())
				.collect(Collectors.toList());
		assertEquals(1, selectedList.size());
		assertEquals("000TM9", selectedList.get(0).getBenefitPlan());

	}

	@Test
	public void getAdditionalBenPlanSelectionsTest2() {
		// test with no disability plans selected
		List<Object[]> result = prepareDisabilityWithoutSelections();
		result.addAll(prepareLifePlanSelections());
		when(mockedQuery.getResultList()).thenReturn(result);

		// calling getAdditionalBenPlanSelections...
		Map<String, List<AdditionalBenefitPlanDto>> actualResult =
				strategyGroupDataDaoImpl.getAdditionalBenPlanSelections(strategyId, company.getRealmPlanYear().getId());

		// ...should produce a dataset with available plans...
		assertEquals(1, actualResult.size());
		List<AdditionalBenefitPlanDto> availableDisability = actualResult.get("BPG001").stream()
				.filter(p -> !p.getBundleId().equals(""))
				.collect(Collectors.toList());
		assertEquals(17, availableDisability.size());

		// ...but no disability selected...
		Optional<AdditionalBenefitPlanDto> selectedDisability = availableDisability.stream()
				.filter(p -> p.isSelected())
				.findFirst();
		assertTrue(selectedDisability.isEmpty());

		// ... and life plan is still selected
		List<AdditionalBenefitPlanDto> selectedList = actualResult.get("BPG001").stream()
				.filter(p -> p.getPlanType().equals("23"))
				.filter(p -> p.isSelected())
				.collect(Collectors.toList());
		assertEquals(1, selectedList.size());
		assertEquals("000TM9", selectedList.get(0).getBenefitPlan());
	}

	@Test
	public void getAdditionalBenPlanSelectionsTest3() {
		// test with no life insurance plans selected
		List<Object[]> result = prepareDisabilitySelections();
		result.addAll(prepareLifePlansWithoutSelections());
		when(mockedQuery.getResultList()).thenReturn(result);

		// calling getAdditionalBenPlanSelections...
		Map<String, List<AdditionalBenefitPlanDto>> actualResult =
				strategyGroupDataDaoImpl.getAdditionalBenPlanSelections(strategyId, company.getRealmPlanYear().getId());

		// ...should produce a dataset with disability bundle selected...
		assertEquals(1, actualResult.size());
		List<AdditionalBenefitPlanDto> selectedDisability = actualResult.get("BPG001").stream()
				.filter(p -> !p.getBundleId().equals(""))
				.filter(p -> p.isSelected())
				.collect(Collectors.toList());
		assertEquals(2, selectedDisability.size());
		assertEquals("1673", selectedDisability.get(0).getBundleId());

		// ...and available life plans...
		List<AdditionalBenefitPlanDto> availableLife = actualResult.get("BPG001").stream()
				.filter(p -> p.getPlanType().equals("23"))
				.collect(Collectors.toList());
		assertEquals(10, availableLife.size());

		// ...but no life plan selected
		Optional<AdditionalBenefitPlanDto> selectedLife = availableLife.stream()
				.filter(p -> p.isSelected())
				.findFirst();
		assertTrue(selectedLife.isEmpty());
	}

	@Test
	public void getAdditionalBenPlanSelectionsTest4() {
		// test with no life insurance plans or disability selected
		List<Object[]> result = prepareDisabilityWithoutSelections();
		result.addAll(prepareLifePlansWithoutSelections());
		when(mockedQuery.getResultList()).thenReturn(result);

		// calling getAdditionalBenPlanSelections...
		Map<String, List<AdditionalBenefitPlanDto>> actualResult =
				strategyGroupDataDaoImpl.getAdditionalBenPlanSelections(strategyId, company.getRealmPlanYear().getId());

		// should produce a dataset with available disability and life plans but no plans selected
		assertEquals(1, actualResult.size());
		List<AdditionalBenefitPlanDto> availableDisability = actualResult.get("BPG001").stream()
				.filter(p -> !p.getBundleId().equals(""))
				.collect(Collectors.toList());
		assertEquals(17, availableDisability.size());

		List<AdditionalBenefitPlanDto> availableLife = actualResult.get("BPG001").stream()
				.filter(p -> p.getPlanType().equals("23"))
				.collect(Collectors.toList());
		assertEquals(10, availableLife.size());

		Optional<AdditionalBenefitPlanDto> selectedDisability = availableDisability.stream()
				.filter(p -> p.isSelected())
				.findFirst();
		assertTrue(selectedDisability.isEmpty());

		Optional<AdditionalBenefitPlanDto> selectedLife = availableLife.stream()
				.filter(p -> p.isSelected())
				.findFirst();
		assertTrue(selectedLife.isEmpty());
	}


	private List<Object[]> prepareWaiverHcMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[10];
		r[0] = "001PJQ";
		r[1] = BigDecimal.valueOf(5);
		results.add(r);
		return results;
	}

	private List<BigDecimal> getStrategyPortfolios() {
		List<BigDecimal> results = new ArrayList<>();
		results.add(new BigDecimal(13));
		results.add(new BigDecimal(14));
		return results;
	}

	private List<String> getExclMedPlanMockData() {
		List<String> results = new ArrayList<>();
		results.add( "BCBS" );
		return results;
	}

	private List<String> getPortfolioFsaPlans() {
		List<String> results = new ArrayList<>();
		results.add("000SRU");
		results.add("000TMG");
		return results;
	}

	private List<Object[]> prepareStrategyPortfoliosByPlanTypeMockData() {

		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[3];
		r[0] = "001RB5";
		r[1] = "10";
		r[2] = BigDecimal.valueOf(18);
		results.add(r);

		r = new Object[3];
		r[0] = "001RB5";
		r[1] = "10";
		r[2] = BigDecimal.valueOf(19);
		results.add(r);

		r = new Object[3];
		r[0] = "001RB5";
		r[1] = "10";
		r[2] = BigDecimal.valueOf(21);
		results.add(r);

		r = new Object[3];
		r[0] = "001RB5";
		r[1] = "11";
		r[2] = BigDecimal.valueOf(14);
		results.add(r);

		r = new Object[3];
		r[0] = "TG4";
		r[1] = "10";
		r[2] = BigDecimal.valueOf(19);
		results.add(r);

		r = new Object[3];
		r[0] = "TG4";
		r[1] = "10";
		r[2] = BigDecimal.valueOf(21);
		results.add(r);

		r = new Object[3];
		r[0] = "TG4";
		r[1] = "11";
		r[2] = BigDecimal.valueOf(14);
		results.add(r);

		return results;

	}

	private List<Object[]> prepareLifePlanSelections() {
		List<Object[]> list = new ArrayList<>();
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","006J2C","Basic Life $10,000","BPG001","Staff Benefits","0","LIFE", null, "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","006J27","Basic Life $20,000","BPG001","Staff Benefits","0","LIFE", null, "2" ) );
		list.add(buildAddlPlanSelection( "695839471","311990","336933", null," ","23","000TM9","Basic Life $50,000","BPG001","Staff Benefits","0","LIFE", null, "3" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","002J3O","Basic Life $100,000","BPG001","Staff Benefits","0","LIFE", null, "4" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","002J3P","Basic Life $150,000","BPG001","Staff Benefits","0","LIFE", null, "5" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","002J3Q","Basic Life $200,000","BPG001","Staff Benefits","0","LIFE", null, "6" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","002J3R","Basic Life $250,000","BPG001","Staff Benefits","0","LIFE", null, "7" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","000SRO","1X Earnings Basic Life & AD&D","BPG001","Staff Benefits","0","LIFE", null, "8" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","000TMA","2X Earnings Basic Life & AD&D","BPG001","Staff Benefits","0","LIFE", null, "9" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","000TMB","3X Earnings Basic Life & AD&D","BPG001","Staff Benefits","0","LIFE", null, "10" ) );
		return list;
	}

	private List<Object[]> prepareLifePlansWithoutSelections() {
		List<Object[]> list = new ArrayList<>();
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","006J2C","Basic Life $10,000","BPG001","Staff Benefits","0","LIFE", null, "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","006J27","Basic Life $20,000","BPG001","Staff Benefits","0","LIFE", null, "2" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","000TM9","Basic Life $50,000","BPG001","Staff Benefits","0","LIFE", null, "3" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","002J3O","Basic Life $100,000","BPG001","Staff Benefits","0","LIFE", null, "4" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","002J3P","Basic Life $150,000","BPG001","Staff Benefits","0","LIFE", null, "5" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","002J3Q","Basic Life $200,000","BPG001","Staff Benefits","0","LIFE", null, "6" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","002J3R","Basic Life $250,000","BPG001","Staff Benefits","0","LIFE", null, "7" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","000SRO","1X Earnings Basic Life & AD&D","BPG001","Staff Benefits","0","LIFE", null, "8" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","000TMA","2X Earnings Basic Life & AD&D","BPG001","Staff Benefits","0","LIFE", null, "9" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933", null," ","23","000TMB","3X Earnings Basic Life & AD&D","BPG001","Staff Benefits","0","LIFE", null, "10" ) );
		return list;
	}

	private List<Object[]> prepareDisabilitySelections() {
		List<Object[]> list = new ArrayList<>();
		list.add(buildAddlPlanSelection( "695839472","311990","336933","1662","60% STD 750 Co Pd (Standard)","30","000TMC","60% STD 750 Co Pd (Standard)","BPG001","Staff Benefits","0","SDI", "1", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1663","60% STD 1500 Co Pd (Enhanced)","30","000SRS","60% STD 1500 Co Pd (Enhanced)","BPG001","Staff Benefits","0","STD2", "2", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1664","50% STD 1750 Co Pd","30","006J2F","50% STD 1750 Co Pd","BPG001","Staff Benefits","0","STD5", "3", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1665","60% STD 2250 Co Pd","30","006J2E","60% STD 2250 Co Pd","BPG001","Staff Benefits","0","STD4", "4", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1666","60% STD 2500 Co Pd (Premium)","30","000TMD","60% STD 2500 Co Pd (Premium)","BPG001","Staff Benefits","0","STD3", "5", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1667","50% LTD 7500 Co Pd","31","006J4C","50% LTD 7500 Co Pd","BPG001","Staff Benefits","0","LTD2", "6", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1668","60% LTD 10000 Co Pd","31","006J4B","60% LTD 10000 Co Pd","BPG001","Staff Benefits","0","LTD1", "7", "1" ) );
		list.add(buildAddlPlanSelection( "695839473","311990","336933","1669","60% LTD 12500 Co Pd (Standard)","31","002J40","60% LTD 12500 Co Pd (Standard)","BPG001","Staff Benefits","0","LTDSTRD", "8", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1670","60% LTD 15000 Co Pd (Premium)","31","002J41","60% LTD 15000 Co Pd (Premium)","BPG001","Staff Benefits","0","LTDPREM", "9", "1" ) );
		list.add(buildAddlPlanSelection( "695839472","311990","336933","1671","60% STD 750 Co Pd (Standard) & 50% LTD 7500 Co Pd","30","000TMC","60% STD 750 Co Pd (Standard)","BPG001","Staff Benefits","0","SDI", "10", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1671","60% STD 750 Co Pd (Standard) & 50% LTD 7500 Co Pd","31","006J4C","50% LTD 7500 Co Pd","BPG001","Staff Benefits","0","LTD2", "10", "2" ) );
		list.add(buildAddlPlanSelection( "695839472","311990","336933","1672","60% STD 750 Co Pd (Standard) & 60% LTD 10000 Co Pd","30","000TMC","60% STD 750 Co Pd (Standard)","BPG001","Staff Benefits","0","SDI", "11", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1672","60% STD 750 Co Pd (Standard) & 60% LTD 10000 Co Pd","31","006J4B","60% LTD 10000 Co Pd","BPG001","Staff Benefits","0","LTD1", "11", "2" ) );
		list.add(buildAddlPlanSelection( "695839472","311990","336933","1673","60% STD 750 Co Pd (Standard) & 60% LTD 12500 Co Pd (Standard)","30","000TMC","60% STD 750 Co Pd (Standard)","BPG001","Staff Benefits","0","SDI", "12", "1" ) );
		list.add(buildAddlPlanSelection( "695839473","311990","336933","1673","60% STD 750 Co Pd (Standard) & 60% LTD 12500 Co Pd (Standard)","31","002J40","60% LTD 12500 Co Pd (Standard)","BPG001","Staff Benefits","0","LTDSTRD", "12", "2" ) );
		list.add(buildAddlPlanSelection( "695839472","311990","336933","1674","60% STD 750 Co Pd (Standard) & 60% LTD 15000 Co Pd (Premium)","30","000TMC","60% STD 750 Co Pd (Standard)","BPG001","Staff Benefits","0","SDI", "13", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1674","60% STD 750 Co Pd (Standard) & 60% LTD 15000 Co Pd (Premium)","31","002J41","60% LTD 15000 Co Pd (Premium)","BPG001","Staff Benefits","0","LTDPREM", "13", "2" ) );
		return list;
	}

	private List<Object[]> prepareDisabilityWithoutSelections() {
		List<Object[]> list = new ArrayList<>();
		list.add(buildAddlPlanSelection( null,"311990","336933","1662","60% STD 750 Co Pd (Standard)","30","000TMC","60% STD 750 Co Pd (Standard)","BPG001","Staff Benefits","0","SDI", "1", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1663","60% STD 1500 Co Pd (Enhanced)","30","000SRS","60% STD 1500 Co Pd (Enhanced)","BPG001","Staff Benefits","0","STD2", "2", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1664","50% STD 1750 Co Pd","30","006J2F","50% STD 1750 Co Pd","BPG001","Staff Benefits","0","STD5", "3", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1665","60% STD 2250 Co Pd","30","006J2E","60% STD 2250 Co Pd","BPG001","Staff Benefits","0","STD4", "4", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1666","60% STD 2500 Co Pd (Premium)","30","000TMD","60% STD 2500 Co Pd (Premium)","BPG001","Staff Benefits","0","STD3", "5", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1667","50% LTD 7500 Co Pd","31","006J4C","50% LTD 7500 Co Pd","BPG001","Staff Benefits","0","LTD2", "6", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1668","60% LTD 10000 Co Pd","31","006J4B","60% LTD 10000 Co Pd","BPG001","Staff Benefits","0","LTD1", "7", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1669","60% LTD 12500 Co Pd (Standard)","31","002J40","60% LTD 12500 Co Pd (Standard)","BPG001","Staff Benefits","0","LTDSTRD", "8", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1670","60% LTD 15000 Co Pd (Premium)","31","002J41","60% LTD 15000 Co Pd (Premium)","BPG001","Staff Benefits","0","LTDPREM", "9", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1671","60% STD 750 Co Pd (Standard) & 50% LTD 7500 Co Pd","30","000TMC","60% STD 750 Co Pd (Standard)","BPG001","Staff Benefits","0","SDI", "10", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1671","60% STD 750 Co Pd (Standard) & 50% LTD 7500 Co Pd","31","006J4C","50% LTD 7500 Co Pd","BPG001","Staff Benefits","0","LTD2", "10", "2" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1672","60% STD 750 Co Pd (Standard) & 60% LTD 10000 Co Pd","30","000TMC","60% STD 750 Co Pd (Standard)","BPG001","Staff Benefits","0","SDI", "11", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1672","60% STD 750 Co Pd (Standard) & 60% LTD 10000 Co Pd","31","006J4B","60% LTD 10000 Co Pd","BPG001","Staff Benefits","0","LTD1", "11", "2" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1673","60% STD 750 Co Pd (Standard) & 60% LTD 12500 Co Pd (Standard)","30","000TMC","60% STD 750 Co Pd (Standard)","BPG001","Staff Benefits","0","SDI", "12", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1673","60% STD 750 Co Pd (Standard) & 60% LTD 12500 Co Pd (Standard)","31","002J40","60% LTD 12500 Co Pd (Standard)","BPG001","Staff Benefits","0","LTDSTRD", "12", "2" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1674","60% STD 750 Co Pd (Standard) & 60% LTD 15000 Co Pd (Premium)","30","000TMC","60% STD 750 Co Pd (Standard)","BPG001","Staff Benefits","0","SDI", "13", "1" ) );
		list.add(buildAddlPlanSelection( null,"311990","336933","1674","60% STD 750 Co Pd (Standard) & 60% LTD 15000 Co Pd (Premium)","31","002J41","60% LTD 15000 Co Pd (Premium)","BPG001","Staff Benefits","0","LTDPREM", "13", "2" ) );
		return list;
	}

	private Object[] buildAddlPlanSelection( String selectionId, String strategyId, String groupId,
			String bundleId, String bundleDescr, String planType, String benefitPlan, String descr,
			String benefitProgram, String groupName, String employeePaid, String shortDescr, 
			String bundleSeq, String planSeq ) {
		Object[] row = { stringToDecimal(selectionId), stringToDecimal(strategyId),
				stringToDecimal(groupId), stringToDecimal(bundleId),
				bundleDescr, planType, benefitPlan, descr, benefitProgram, groupName,
				stringToDecimal(employeePaid), shortDescr,
				stringToDecimal(bundleSeq), stringToDecimal(planSeq) };
		return row;
	}

	private BigDecimal stringToDecimal( String number ) {
		return number == null ? null : new BigDecimal(number);
	}
}