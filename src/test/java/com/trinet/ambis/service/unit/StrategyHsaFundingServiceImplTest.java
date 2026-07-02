package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.StrategyHsaFundingDao;
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyHsaFunding;
import com.trinet.ambis.service.impl.StrategyHsaFundingServiceImpl;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;

@RunWith(JUnit4.class)
public class StrategyHsaFundingServiceImplTest {

	@InjectMocks
	StrategyHsaFundingServiceImpl service;

	@Mock
	StrategyHsaFundingDao strategyHsaFundingDao;

	@Mock
	RenewalDataDao renewalDataDao;

	@Captor
	ArgumentCaptor<StrategyHsaFunding> entityCaptor;

	private static final long STRATEGY_ID1 = 1111L;
	private static final long STRATEGY_ID2 = 2222L;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void findById() {
		long strategyId = STRATEGY_ID1;
		BigDecimal annualEeAmount = BigDecimal.valueOf(100);
		BigDecimal annualFamilyAmount = BigDecimal.valueOf(200);
		BigDecimal quarterlyEeAmount = BigDecimal.valueOf(25);
		BigDecimal quarterlyFamilyAmount = BigDecimal.valueOf(50);
		int annualMonth = 1;
		String contributionFrequency = "M";
		String lumpSumFrequency = "A";
		int q1Month = 1;
		int q2Month = 1;
		int q3Month = 1;
		int q4Month = 1;
		int optionId = 1;
		StrategyHsaFunding strategyHsaFunding = prepareStrategyHsaFunding(strategyId, annualEeAmount,
				annualFamilyAmount, quarterlyEeAmount, quarterlyFamilyAmount, annualMonth, contributionFrequency,
				lumpSumFrequency, q1Month, q2Month, q3Month, q4Month, optionId);

		when(strategyHsaFundingDao.findByStrategyId(strategyId)).thenReturn(strategyHsaFunding);

		StrategyHsaFundingDto actual = service.findById(strategyId);

		assertEquals(strategyId, actual.getStrategyId());
		assertEquals(BigDecimal.valueOf(100), actual.getAnnualEeAmount());
		assertEquals(BigDecimal.valueOf(200), actual.getAnnualFamilyAmount());
		assertEquals(BigDecimal.valueOf(25), actual.getQuarterlyEeAmount());
		assertEquals(BigDecimal.valueOf(50), actual.getQuarterlyFamilyAmount());
		assertEquals(Integer.valueOf(1), actual.getAnnualMonth());
		assertEquals("M", actual.getContributionFrequency());
		assertEquals("A", actual.getLumpSumFrequency());
		assertEquals(Integer.valueOf(1), actual.getQ1Month());
		assertEquals(Integer.valueOf(1), actual.getQ2Month());
		assertEquals(Integer.valueOf(1), actual.getQ3Month());
		assertEquals(Integer.valueOf(1), actual.getQ4Month());
		assertEquals(Integer.valueOf(1), actual.getOptionId());
	}

	@Test
	public void save() {
		long strategyId = STRATEGY_ID1;
		BigDecimal annualEeAmount = BigDecimal.valueOf(100);
		BigDecimal annualFamilyAmount = BigDecimal.valueOf(200);
		BigDecimal quarterlyEeAmount = BigDecimal.valueOf(25);
		BigDecimal quarterlyFamilyAmount = BigDecimal.valueOf(50);
		BigDecimal monthlyEeAmount = BigDecimal.valueOf(25);
		BigDecimal monthlyFamilyAmount = BigDecimal.valueOf(50);
		int annualMonth = 1;
		String contributionFrequency = "M";
		String lumpSumFrequency = "A";
		int q1Month = 1;
		int q2Month = 1;
		int q3Month = 1;
		int q4Month = 1;
		int optionId = 1;
		StrategyHsaFundingDto strategyHsaFundingDto = prepareStrategyHsaFundingDto(strategyId, annualEeAmount,
				annualFamilyAmount, quarterlyEeAmount, quarterlyFamilyAmount, monthlyEeAmount, monthlyFamilyAmount,
				annualMonth, contributionFrequency, lumpSumFrequency, q1Month, q2Month, q3Month, q4Month, optionId);

		when(strategyHsaFundingDao.save(entityCaptor.capture())).thenReturn(prepareStrategyHsaFunding(strategyId,
				annualEeAmount, annualFamilyAmount, quarterlyEeAmount, quarterlyFamilyAmount, annualMonth,
				contributionFrequency, lumpSumFrequency, q1Month, q2Month, q3Month, q4Month, optionId));

		when(strategyHsaFundingDao.findByStrategyId(STRATEGY_ID1)).thenReturn(prepareStrategyHsaFunding(strategyId,
				annualEeAmount, annualFamilyAmount, quarterlyEeAmount, quarterlyFamilyAmount, annualMonth,
				contributionFrequency, lumpSumFrequency, q1Month, q2Month, q3Month, q4Month, optionId));

		StrategyHsaFundingDto actual = service.save(strategyHsaFundingDto);

		assertEquals(strategyId, actual.getStrategyId());
		assertEquals(BigDecimal.valueOf(100), actual.getAnnualEeAmount());
		assertEquals(BigDecimal.valueOf(200), actual.getAnnualFamilyAmount());
		assertEquals(BigDecimal.valueOf(25), actual.getQuarterlyEeAmount());
		assertEquals(BigDecimal.valueOf(50), actual.getQuarterlyFamilyAmount());
		assertEquals(Integer.valueOf(1), actual.getAnnualMonth());
		assertEquals("M", actual.getContributionFrequency());
		assertEquals("A", actual.getLumpSumFrequency());
		assertEquals(Integer.valueOf(1), actual.getQ1Month());
		assertEquals(Integer.valueOf(1), actual.getQ2Month());
		assertEquals(Integer.valueOf(1), actual.getQ3Month());
		assertEquals(Integer.valueOf(1), actual.getQ4Month());
		assertEquals(Integer.valueOf(1), actual.getOptionId());

		assertEquals(strategyId, entityCaptor.getValue().getStrategyId());
		assertEquals(BigDecimal.valueOf(100), entityCaptor.getValue().getAnnualEeAmount());
		assertEquals(BigDecimal.valueOf(200), entityCaptor.getValue().getAnnualFamilyAmount());
		assertEquals(BigDecimal.valueOf(25), entityCaptor.getValue().getQuarterlyEeAmount());
		assertEquals(BigDecimal.valueOf(50), entityCaptor.getValue().getQuarterlyFamilyAmount());
		assertEquals(Integer.valueOf(1), entityCaptor.getValue().getAnnualMonth());
		assertEquals("M", entityCaptor.getValue().getContributionFrequency());
		assertEquals("A", entityCaptor.getValue().getLumpSumFrequency());
		assertEquals(Integer.valueOf(1), entityCaptor.getValue().getQ1Month());
		assertEquals(Integer.valueOf(1), entityCaptor.getValue().getQ2Month());
		assertEquals(Integer.valueOf(1), entityCaptor.getValue().getQ3Month());
		assertEquals(Integer.valueOf(1), entityCaptor.getValue().getQ4Month());
		assertEquals(Integer.valueOf(1), entityCaptor.getValue().getOptionId());
	}

	@Test
	public void saveAll() {
		long strategyId = STRATEGY_ID1;
		BigDecimal annualEeAmount = BigDecimal.valueOf(100);
		BigDecimal annualFamilyAmount = BigDecimal.valueOf(200);
		BigDecimal quarterlyEeAmount = BigDecimal.valueOf(25);
		BigDecimal quarterlyFamilyAmount = BigDecimal.valueOf(50);
		BigDecimal monthlyEeAmount = BigDecimal.valueOf(25);
		BigDecimal monthlyFamilyAmount = BigDecimal.valueOf(50);
		int annualMonth = 1;
		String contributionFrequency = "M";
		String lumpSumFrequency = "A";
		int q1Month = 1;
		int q2Month = 1;
		int q3Month = 1;
		int q4Month = 1;
		int optionId = 1;
		StrategyHsaFunding funding1 = prepareStrategyHsaFunding(strategyId, annualEeAmount, annualFamilyAmount,
				quarterlyEeAmount, quarterlyFamilyAmount, annualMonth, contributionFrequency, lumpSumFrequency, q1Month,
				q2Month, q3Month, q4Month, optionId);

		long strategyId2 = STRATEGY_ID2;
		BigDecimal annualEeAmount2 = BigDecimal.valueOf(150);
		BigDecimal annualFamilyAmount2 = BigDecimal.valueOf(300);
		BigDecimal quarterlyEeAmount2 = BigDecimal.valueOf(50);
		BigDecimal quarterlyFamilyAmount2 = BigDecimal.valueOf(100);
		BigDecimal monthlyEeAmount2 = BigDecimal.valueOf(50);
		BigDecimal monthlyFamilyAmount2 = BigDecimal.valueOf(100);
		int annualMonth2 = 3;
		String contributionFrequency2 = "Q";
		String lumpSumFrequency2 = "A";
		int q1Month2 = 3;
		int q2Month2 = 3;
		int q3Month2 = 3;
		int q4Month2 = 3;
		int optionId2 = 2;

		StrategyHsaFunding funding2 = prepareStrategyHsaFunding(strategyId2, annualEeAmount2, annualFamilyAmount2,
				quarterlyEeAmount2, quarterlyFamilyAmount2, annualMonth2, contributionFrequency2, lumpSumFrequency2,
				q1Month2, q2Month2, q3Month2, q4Month2, optionId2);

		StrategyHsaFundingDto fundingDto1 = prepareStrategyHsaFundingDto(strategyId, annualEeAmount, annualFamilyAmount,
				quarterlyEeAmount, quarterlyFamilyAmount, monthlyEeAmount, monthlyFamilyAmount, annualMonth,
				contributionFrequency, lumpSumFrequency, q1Month, q2Month, q3Month, q4Month, optionId);

		StrategyHsaFundingDto fundingDto2 = prepareStrategyHsaFundingDto(strategyId2, annualEeAmount2,
				annualFamilyAmount2, quarterlyEeAmount2, quarterlyFamilyAmount2, monthlyEeAmount2, monthlyFamilyAmount2,
				annualMonth2, contributionFrequency2, lumpSumFrequency2, q1Month2, q2Month2, q3Month2, q4Month2,
				optionId2);

		List<StrategyHsaFundingDto> strategyHsaFundingDtoList = new ArrayList<>();
		strategyHsaFundingDtoList.add(fundingDto1);
		strategyHsaFundingDtoList.add(fundingDto2);

		when(strategyHsaFundingDao.save(entityCaptor.capture())).thenReturn(prepareStrategyHsaFunding(strategyId,
				annualEeAmount, annualFamilyAmount, quarterlyEeAmount, quarterlyFamilyAmount, annualMonth,
				contributionFrequency, lumpSumFrequency, q1Month, q2Month, q3Month, q4Month, optionId));
		when(strategyHsaFundingDao.findByStrategyId(strategyId)).thenReturn(funding1);
		when(strategyHsaFundingDao.findByStrategyId(strategyId2)).thenReturn(funding2);

		List<StrategyHsaFundingDto> actual = service.saveAll(strategyHsaFundingDtoList);

		verify(strategyHsaFundingDao, times(2)).save(Mockito.any(StrategyHsaFunding.class));
		assertEquals(strategyId, entityCaptor.getAllValues().get(0).getStrategyId());
		assertEquals(strategyId2, entityCaptor.getAllValues().get(1).getStrategyId());
		assertEquals(strategyId, actual.get(0).getStrategyId());
		assertEquals(strategyId2, actual.get(1).getStrategyId());
	}

	@Test
	public void createFutureStrategyHsaFunding() {
		int testNumber = 0;
		StrategyHsaFunding strategyHsaFundingCaptor;
		List<Strategy> strategyList = new ArrayList<>();
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID1);
		strategyList.add(strategy);
		strategy = new Strategy();
		strategy.setId(STRATEGY_ID2);
		strategyList.add(strategy);
		Company company = new Company();
		company.setCode("COMPANY_CODE");
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setPlanYearStart(new Date());
		company.setRealmPlanYear(realmPlanYear);
		Map<String, String> realmRuleConfigurations = prepareRealmRuleConfigurations(null, null, null, null);

		long strategyId = 0;

		BigDecimal annualEeAmount;
		BigDecimal annualFamilyAmount;
		BigDecimal quarterlyEeAmount;
		BigDecimal quarterlyFamilyAmount;
		BigDecimal monthlyEeAmount;
		BigDecimal monthlyFamilyAmount;
		int annualMonth = 1;
		String contributionFrequency = "M";
		String lumpSumFrequency = "A";
		int q1Month = 1;
		int q2Month = 1;
		int q3Month = 1;
		int q4Month = 1;
		int optionId = 1;

		when(strategyHsaFundingDao.save(entityCaptor.capture())).thenReturn(null);
		when(strategyHsaFundingDao.findByStrategyId(STRATEGY_ID1)).thenReturn(null);
		when(strategyHsaFundingDao.findByStrategyId(STRATEGY_ID2)).thenReturn(null);
		

		/*
		 * Test when current hsa setup is null
		 */
		StrategyHsaFundingDto currentStrategyHsaFundingDto = null;

		when(renewalDataDao.getPsHsaFundingDetails(Mockito.anyString(), Mockito.any(Date.class)))
				.thenReturn(currentStrategyHsaFundingDto);
		service.createFutureStrategyHsaFunding(strategyList, company, realmRuleConfigurations);

		assertEquals(true, entityCaptor.getAllValues().isEmpty());
		
		/*
		 * Test when everything is "normal"
		 */
		annualEeAmount = BigDecimal.valueOf(100);
		annualFamilyAmount = BigDecimal.valueOf(200);
		quarterlyEeAmount = BigDecimal.valueOf(25);
		quarterlyFamilyAmount = BigDecimal.valueOf(50);
		monthlyEeAmount = BigDecimal.valueOf(50);
		monthlyFamilyAmount = BigDecimal.valueOf(100);
		currentStrategyHsaFundingDto = prepareStrategyHsaFundingDto(strategyId, annualEeAmount,
				annualFamilyAmount, quarterlyEeAmount, quarterlyFamilyAmount, monthlyEeAmount, monthlyFamilyAmount,
				annualMonth, contributionFrequency, lumpSumFrequency, q1Month, q2Month, q3Month, q4Month, optionId);

		when(renewalDataDao.getPsHsaFundingDetails(Mockito.anyString(), Mockito.any(Date.class)))
				.thenReturn(currentStrategyHsaFundingDto);
		service.createFutureStrategyHsaFunding(strategyList, company, realmRuleConfigurations);

		assertEquals(STRATEGY_ID1, entityCaptor.getAllValues().get(testNumber).getStrategyId());
		testNumber++;
		assertEquals(STRATEGY_ID2, entityCaptor.getAllValues().get(testNumber).getStrategyId());

		/*
		 * Test when Family Amounts are less than Employee Amounts
		 */
		realmRuleConfigurations = prepareRealmRuleConfigurations("200", "400", "2000", "4000");
		testNumber++;
		strategyList.remove(1);
		annualEeAmount = BigDecimal.valueOf(100);
		annualFamilyAmount = BigDecimal.valueOf(50);
		quarterlyEeAmount = BigDecimal.valueOf(25);
		quarterlyFamilyAmount = BigDecimal.valueOf(12);
		monthlyEeAmount = BigDecimal.valueOf(50);
		monthlyFamilyAmount = BigDecimal.valueOf(25);

		currentStrategyHsaFundingDto = prepareStrategyHsaFundingDto(strategyId, annualEeAmount, annualFamilyAmount,
				quarterlyEeAmount, quarterlyFamilyAmount, monthlyEeAmount, monthlyFamilyAmount, annualMonth,
				contributionFrequency, lumpSumFrequency, q1Month, q2Month, q3Month, q4Month, optionId);

		when(renewalDataDao.getPsHsaFundingDetails(Mockito.anyString(), Mockito.any(Date.class)))
				.thenReturn(currentStrategyHsaFundingDto);
		service.createFutureStrategyHsaFunding(strategyList, company, realmRuleConfigurations);

		strategyHsaFundingCaptor = entityCaptor.getAllValues().get(testNumber);
		assertEquals(STRATEGY_ID1, strategyHsaFundingCaptor.getStrategyId());
		assertEquals(annualEeAmount, strategyHsaFundingCaptor.getAnnualFamilyAmount());
		assertEquals(quarterlyEeAmount, strategyHsaFundingCaptor.getQuarterlyFamilyAmount());
		assertEquals(monthlyEeAmount, strategyHsaFundingCaptor.getMonthlyFamilyAmount());

		/*
		 * Test when Amounts are less than minimum Amounts (Annual + Monthly)
		 */
		testNumber++;
		annualEeAmount = BigDecimal.valueOf(5);
		annualFamilyAmount = BigDecimal.valueOf(10);
		quarterlyEeAmount = BigDecimal.ZERO;
		quarterlyFamilyAmount = BigDecimal.ZERO;
		monthlyEeAmount = BigDecimal.valueOf(5);
		monthlyFamilyAmount = BigDecimal.valueOf(10);

		currentStrategyHsaFundingDto = prepareStrategyHsaFundingDto(strategyId, annualEeAmount, annualFamilyAmount,
				quarterlyEeAmount, quarterlyFamilyAmount, monthlyEeAmount, monthlyFamilyAmount, annualMonth,
				contributionFrequency, lumpSumFrequency, q1Month, q2Month, q3Month, q4Month, optionId);

		when(renewalDataDao.getPsHsaFundingDetails(Mockito.anyString(), Mockito.any(Date.class)))
				.thenReturn(currentStrategyHsaFundingDto);
		service.createFutureStrategyHsaFunding(strategyList, company, realmRuleConfigurations);

		strategyHsaFundingCaptor = entityCaptor.getAllValues().get(testNumber);
		assertEquals(STRATEGY_ID1, strategyHsaFundingCaptor.getStrategyId());
		assertEquals(BigDecimal.valueOf(140), strategyHsaFundingCaptor.getAnnualEeAmount());
		assertEquals(BigDecimal.valueOf(280), strategyHsaFundingCaptor.getAnnualFamilyAmount());
		assertEquals(BigDecimal.ZERO, strategyHsaFundingCaptor.getQuarterlyEeAmount());
		assertEquals(BigDecimal.ZERO, strategyHsaFundingCaptor.getQuarterlyFamilyAmount());
		assertEquals(BigDecimal.valueOf(5), strategyHsaFundingCaptor.getMonthlyEeAmount());
		assertEquals(BigDecimal.valueOf(10), strategyHsaFundingCaptor.getMonthlyFamilyAmount());

		/*
		 * Test when Amounts are less than minimum Amounts (Quarterly)
		 */
		testNumber++;
		contributionFrequency = null;
		lumpSumFrequency = BSSApplicationConstants.HSA_QUARTERLY;
		annualEeAmount = BigDecimal.ZERO;
		annualFamilyAmount = null;
		quarterlyEeAmount = BigDecimal.valueOf(5);
		quarterlyFamilyAmount = BigDecimal.valueOf(10);
		monthlyEeAmount = BigDecimal.ZERO;
		monthlyFamilyAmount = null;

		currentStrategyHsaFundingDto = prepareStrategyHsaFundingDto(strategyId, annualEeAmount, annualFamilyAmount,
				quarterlyEeAmount, quarterlyFamilyAmount, monthlyEeAmount, monthlyFamilyAmount, annualMonth,
				contributionFrequency, lumpSumFrequency, q1Month, q2Month, q3Month, q4Month, optionId);

		when(renewalDataDao.getPsHsaFundingDetails(Mockito.anyString(), Mockito.any(Date.class)))
				.thenReturn(currentStrategyHsaFundingDto);
		service.createFutureStrategyHsaFunding(strategyList, company, realmRuleConfigurations);

		strategyHsaFundingCaptor = entityCaptor.getAllValues().get(testNumber);
		assertEquals(STRATEGY_ID1, strategyHsaFundingCaptor.getStrategyId());
		assertEquals(annualEeAmount, strategyHsaFundingCaptor.getAnnualEeAmount());
		assertEquals(annualFamilyAmount, strategyHsaFundingCaptor.getAnnualFamilyAmount());
		assertEquals(0, BigDecimal.valueOf(50).compareTo(strategyHsaFundingCaptor.getQuarterlyEeAmount()));
		assertEquals(0, BigDecimal.valueOf(100).compareTo(strategyHsaFundingCaptor.getQuarterlyFamilyAmount()));
		assertEquals(monthlyEeAmount, strategyHsaFundingCaptor.getMonthlyEeAmount());
		assertEquals(monthlyFamilyAmount, strategyHsaFundingCaptor.getMonthlyFamilyAmount());

		/*
		 * Test when Amounts are less than minimum Amounts (Monthly)
		 */
		testNumber++;
		contributionFrequency = BSSApplicationConstants.HSA_MONTHLY;
		lumpSumFrequency = null;
		annualEeAmount = null;
		annualFamilyAmount = null;
		quarterlyEeAmount = BigDecimal.ZERO;
		quarterlyFamilyAmount = BigDecimal.ZERO;
		monthlyEeAmount = BigDecimal.valueOf(5);
		monthlyFamilyAmount = BigDecimal.valueOf(10);

		currentStrategyHsaFundingDto = prepareStrategyHsaFundingDto(strategyId, annualEeAmount, annualFamilyAmount,
				quarterlyEeAmount, quarterlyFamilyAmount, monthlyEeAmount, monthlyFamilyAmount, annualMonth,
				contributionFrequency, lumpSumFrequency, q1Month, q2Month, q3Month, q4Month, optionId);

		when(renewalDataDao.getPsHsaFundingDetails(Mockito.anyString(), Mockito.any(Date.class)))
				.thenReturn(currentStrategyHsaFundingDto);
		service.createFutureStrategyHsaFunding(strategyList, company, realmRuleConfigurations);

		strategyHsaFundingCaptor = entityCaptor.getAllValues().get(testNumber);
		assertEquals(STRATEGY_ID1, strategyHsaFundingCaptor.getStrategyId());
		assertEquals(annualEeAmount, strategyHsaFundingCaptor.getAnnualEeAmount());
		assertEquals(annualFamilyAmount, strategyHsaFundingCaptor.getAnnualFamilyAmount());
		assertEquals(BigDecimal.ZERO, strategyHsaFundingCaptor.getQuarterlyEeAmount());
		assertEquals(BigDecimal.ZERO, strategyHsaFundingCaptor.getQuarterlyFamilyAmount());
		assertEquals(BigDecimal.valueOf(16.67), strategyHsaFundingCaptor.getMonthlyEeAmount());
		assertEquals(BigDecimal.valueOf(33.33), strategyHsaFundingCaptor.getMonthlyFamilyAmount());

		/*
		 * Test when Amounts are greater than maximum Amounts (Annual + Monthly)
		 */
		testNumber++;
		contributionFrequency = BSSApplicationConstants.HSA_MONTHLY;
		lumpSumFrequency = BSSApplicationConstants.HSA_ANNUAL;
		annualEeAmount = BigDecimal.valueOf(2500);
		annualFamilyAmount = BigDecimal.valueOf(4500);
		quarterlyEeAmount = BigDecimal.ZERO;
		quarterlyFamilyAmount = BigDecimal.ZERO;
		monthlyEeAmount = BigDecimal.valueOf(50);
		monthlyFamilyAmount = BigDecimal.valueOf(100);

		currentStrategyHsaFundingDto = prepareStrategyHsaFundingDto(strategyId, annualEeAmount, annualFamilyAmount,
				quarterlyEeAmount, quarterlyFamilyAmount, monthlyEeAmount, monthlyFamilyAmount, annualMonth,
				contributionFrequency, lumpSumFrequency, q1Month, q2Month, q3Month, q4Month, optionId);

		when(renewalDataDao.getPsHsaFundingDetails(Mockito.anyString(), Mockito.any(Date.class)))
				.thenReturn(currentStrategyHsaFundingDto);
		service.createFutureStrategyHsaFunding(strategyList, company, realmRuleConfigurations);

		strategyHsaFundingCaptor = entityCaptor.getAllValues().get(testNumber);
		assertEquals(STRATEGY_ID1, strategyHsaFundingCaptor.getStrategyId());
		assertEquals(0, BigDecimal.valueOf(1400).compareTo(strategyHsaFundingCaptor.getAnnualEeAmount()));
		assertEquals(0, BigDecimal.valueOf(2800).compareTo(strategyHsaFundingCaptor.getAnnualFamilyAmount()));
		assertEquals(BigDecimal.ZERO, strategyHsaFundingCaptor.getQuarterlyEeAmount());
		assertEquals(BigDecimal.ZERO, strategyHsaFundingCaptor.getQuarterlyFamilyAmount());
		assertEquals(0, BigDecimal.valueOf(50).compareTo(strategyHsaFundingCaptor.getMonthlyEeAmount()));
		assertEquals(0, BigDecimal.valueOf(100).compareTo(strategyHsaFundingCaptor.getMonthlyFamilyAmount()));

		/*
		 * Test when Amounts are greater than maximum Amounts (Annual + Monthly)
		 */
		testNumber++;
		contributionFrequency = BSSApplicationConstants.HSA_MONTHLY;
		lumpSumFrequency = BSSApplicationConstants.HSA_ANNUAL;
		annualEeAmount = BigDecimal.valueOf(50);
		annualFamilyAmount = BigDecimal.valueOf(100);
		quarterlyEeAmount = BigDecimal.ZERO;
		quarterlyFamilyAmount = BigDecimal.ZERO;
		monthlyEeAmount = BigDecimal.valueOf(500);
		monthlyFamilyAmount = BigDecimal.valueOf(1000);

		currentStrategyHsaFundingDto = prepareStrategyHsaFundingDto(strategyId, annualEeAmount, annualFamilyAmount,
				quarterlyEeAmount, quarterlyFamilyAmount, monthlyEeAmount, monthlyFamilyAmount, annualMonth,
				contributionFrequency, lumpSumFrequency, q1Month, q2Month, q3Month, q4Month, optionId);

		when(renewalDataDao.getPsHsaFundingDetails(Mockito.anyString(), Mockito.any(Date.class)))
				.thenReturn(currentStrategyHsaFundingDto);
		service.createFutureStrategyHsaFunding(strategyList, company, realmRuleConfigurations);

		strategyHsaFundingCaptor = entityCaptor.getAllValues().get(testNumber);
		assertEquals(STRATEGY_ID1, strategyHsaFundingCaptor.getStrategyId());
		assertEquals(0, BigDecimal.ZERO.compareTo(strategyHsaFundingCaptor.getAnnualEeAmount()));
		assertEquals(0, BigDecimal.ZERO.compareTo(strategyHsaFundingCaptor.getAnnualFamilyAmount()));
		assertEquals(0, BigDecimal.ZERO.compareTo(strategyHsaFundingCaptor.getQuarterlyEeAmount()));
		assertEquals(0, BigDecimal.ZERO.compareTo(strategyHsaFundingCaptor.getQuarterlyFamilyAmount()));
		assertEquals(BigDecimal.valueOf(166.67), strategyHsaFundingCaptor.getMonthlyEeAmount());
		assertEquals(BigDecimal.valueOf(333.33), strategyHsaFundingCaptor.getMonthlyFamilyAmount());

		/*
		 * Test when Amounts are greater than maximum Amounts (Quarterly)
		 */
		testNumber++;
		contributionFrequency = null;
		lumpSumFrequency = BSSApplicationConstants.HSA_QUARTERLY;
		annualEeAmount = BigDecimal.ZERO;
		annualFamilyAmount = BigDecimal.ZERO;
		quarterlyEeAmount = BigDecimal.valueOf(1000);
		quarterlyFamilyAmount = BigDecimal.valueOf(2000);
		monthlyEeAmount = BigDecimal.ZERO;
		monthlyFamilyAmount = BigDecimal.ZERO;

		currentStrategyHsaFundingDto = prepareStrategyHsaFundingDto(strategyId, annualEeAmount, annualFamilyAmount,
				quarterlyEeAmount, quarterlyFamilyAmount, monthlyEeAmount, monthlyFamilyAmount, annualMonth,
				contributionFrequency, lumpSumFrequency, q1Month, q2Month, q3Month, q4Month, optionId);

		when(renewalDataDao.getPsHsaFundingDetails(Mockito.anyString(), Mockito.any(Date.class)))
				.thenReturn(currentStrategyHsaFundingDto);
		service.createFutureStrategyHsaFunding(strategyList, company, realmRuleConfigurations);

		strategyHsaFundingCaptor = entityCaptor.getAllValues().get(testNumber);
		assertEquals(STRATEGY_ID1, strategyHsaFundingCaptor.getStrategyId());
		assertEquals(0, BigDecimal.ZERO.compareTo(strategyHsaFundingCaptor.getAnnualEeAmount()));
		assertEquals(0, BigDecimal.ZERO.compareTo(strategyHsaFundingCaptor.getAnnualFamilyAmount()));
		assertEquals(0, BigDecimal.valueOf(500).compareTo(strategyHsaFundingCaptor.getQuarterlyEeAmount()));
		assertEquals(0, BigDecimal.valueOf(1000).compareTo(strategyHsaFundingCaptor.getQuarterlyFamilyAmount()));
		assertEquals(0, BigDecimal.ZERO.compareTo(strategyHsaFundingCaptor.getMonthlyEeAmount()));
		assertEquals(0, BigDecimal.ZERO.compareTo(strategyHsaFundingCaptor.getMonthlyFamilyAmount()));

		/*
		 * Test when Amounts are greater than maximum Amounts (Quarterly +
		 * Monthly) This is an invalid setup but using to test logic
		 */
		testNumber++;
		contributionFrequency = BSSApplicationConstants.HSA_MONTHLY;
		lumpSumFrequency = BSSApplicationConstants.HSA_QUARTERLY;
		annualEeAmount = BigDecimal.ZERO;
		annualFamilyAmount = BigDecimal.ZERO;
		quarterlyEeAmount = BigDecimal.valueOf(1000);
		quarterlyFamilyAmount = BigDecimal.valueOf(2000);
		monthlyEeAmount = BigDecimal.valueOf(500);
		monthlyFamilyAmount = BigDecimal.valueOf(1000);

		currentStrategyHsaFundingDto = prepareStrategyHsaFundingDto(strategyId, annualEeAmount, annualFamilyAmount,
				quarterlyEeAmount, quarterlyFamilyAmount, monthlyEeAmount, monthlyFamilyAmount, annualMonth,
				contributionFrequency, lumpSumFrequency, q1Month, q2Month, q3Month, q4Month, optionId);

		when(renewalDataDao.getPsHsaFundingDetails(Mockito.anyString(), Mockito.any(Date.class)))
				.thenReturn(currentStrategyHsaFundingDto);
		service.createFutureStrategyHsaFunding(strategyList, company, realmRuleConfigurations);

		strategyHsaFundingCaptor = entityCaptor.getAllValues().get(testNumber);
		assertEquals(STRATEGY_ID1, strategyHsaFundingCaptor.getStrategyId());
		assertEquals(0, BigDecimal.ZERO.compareTo(strategyHsaFundingCaptor.getAnnualEeAmount()));
		assertEquals(0, BigDecimal.ZERO.compareTo(strategyHsaFundingCaptor.getAnnualFamilyAmount()));
		assertEquals(0, BigDecimal.ZERO.compareTo(strategyHsaFundingCaptor.getQuarterlyEeAmount()));
		assertEquals(0, BigDecimal.ZERO.compareTo(strategyHsaFundingCaptor.getQuarterlyFamilyAmount()));
		assertEquals(BigDecimal.valueOf(166.67), strategyHsaFundingCaptor.getMonthlyEeAmount());
		assertEquals(BigDecimal.valueOf(333.33), strategyHsaFundingCaptor.getMonthlyFamilyAmount());

	}

	public StrategyHsaFunding prepareStrategyHsaFunding(long strategyId, BigDecimal annualEeAmount,
			BigDecimal annualFamilyAmount, BigDecimal quarterlyEeAmount, BigDecimal quarterlyFamilyAmount,
			int annualMonth, String contributionFrequency, String lumpSumFrequency, int q1Month, int q2Month,
			int q3Month, int q4Month, int optionId) {
		StrategyHsaFunding shf = new StrategyHsaFunding();
		shf.setStrategyId(strategyId);
		shf.setAnnualEeAmount(annualEeAmount);
		shf.setAnnualFamilyAmount(annualFamilyAmount);
		shf.setQuarterlyEeAmount(quarterlyEeAmount);
		shf.setQuarterlyFamilyAmount(quarterlyFamilyAmount);
		shf.setAnnualMonth(annualMonth);
		shf.setContributionFrequency(contributionFrequency);
		shf.setLumpSumFrequency(lumpSumFrequency);
		shf.setQ1Month(q1Month);
		shf.setQ2Month(q2Month);
		shf.setQ3Month(q3Month);
		shf.setQ4Month(q4Month);
		shf.setOptionId(optionId);

		return shf;
	}

	public StrategyHsaFundingDto prepareStrategyHsaFundingDto(long strategyId, BigDecimal annualEeAmount,
			BigDecimal annualFamilyAmount, BigDecimal quarterlyEeAmount, BigDecimal quarterlyFamilyAmount,
			BigDecimal monthlyEeAmount, BigDecimal monthlyFamilyAmount, int annualMonth, String contributionFrequency,
			String lumpSumFrequency, int q1Month, int q2Month, int q3Month, int q4Month, int optionId) {
		StrategyHsaFundingDto shfDto = new StrategyHsaFundingDto();
		shfDto.setStrategyId(strategyId);
		shfDto.setAnnualEeAmount(annualEeAmount);
		shfDto.setAnnualFamilyAmount(annualFamilyAmount);
		shfDto.setQuarterlyEeAmount(quarterlyEeAmount);
		shfDto.setQuarterlyFamilyAmount(quarterlyFamilyAmount);
		shfDto.setMonthlyEeAmount(monthlyEeAmount);
		shfDto.setMonthlyFamilyAmount(monthlyFamilyAmount);
		shfDto.setAnnualMonth(annualMonth);
		shfDto.setContributionFrequency(contributionFrequency);
		shfDto.setLumpSumFrequency(lumpSumFrequency);
		shfDto.setQ1Month(q1Month);
		shfDto.setQ2Month(q2Month);
		shfDto.setQ3Month(q3Month);
		shfDto.setQ4Month(q4Month);
		shfDto.setOptionId(optionId);

		return shfDto;
	}

	public Map<String, String> prepareRealmRuleConfigurations(String annualEeMinimum, String annualFamilyMinimum,
			String annualEeMaximum, String annualFamilyMaximum) {

		Map<String, String> realmRuleConfigurations = new HashMap<>();
		realmRuleConfigurations.put("HSA_ANNUAL_EMPLOYEE_MINIMUM", annualEeMinimum);
		realmRuleConfigurations.put("HSA_ANNUAL_FAMILY_MINIMUM", annualFamilyMinimum);
		realmRuleConfigurations.put("HSA_ANNUAL_EMPLOYEE_MAXIMUM", annualEeMaximum);
		realmRuleConfigurations.put("HSA_ANNUAL_FAMILY_MAXIMUM", annualFamilyMaximum);
		return realmRuleConfigurations;
	}

}