/**
 * 
 */
package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupPlanSelectDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.impl.ModelCompareServiceImpl;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.StrategyBenefitPlanHeadCount;
import com.trinet.ambis.service.model.StrategyCoverageLevelHeadcount;

/**
 * @author hliddle
 *
 */
@RunWith(JUnit4.class)
public class ModelCompareServiceImpl2Test {

	@InjectMocks
	ModelCompareServiceImpl modelCompareService;

	@Mock
	StrategyDataDao strategyDataDao;

	@Mock
	RealmPlanYearService realmPlanYearService;

	@Mock
	CompanyService companyService;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	StrategyGroupPlanSelectDao strategyGroupPlanSelectDao;

	private static String PLAN_START_DATE = "10-JAN-2018";
	private static String QUARTER = "IV";
	private static String HEAD_QTR_STATE = "FL";
	private static final long COMPANY_ID = 9999;
	private static final String COMPANY_NAME = "Trinet Group";
	private static final String COMPANY_CODE = "5R9";
	private static final String KAISER_BAND_CODE = "KBC";
	private static final boolean IS_PAYROLL_PROCESSED_TRUE = true;
	private static boolean TRANSITION_PERIOD = true;
	private static long strategy1 = 1000L;
	private static long strategy2 = 2000L;
	private static long strategy3 = 3000L;

	@Before
	public void setup() throws ParseException {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getMCPlanStrategyCoverageHeadcount() {

		Company company = prepareCompany(COMPANY_ID, COMPANY_NAME, COMPANY_CODE, PLAN_START_DATE, QUARTER,
				IS_PAYROLL_PROCESSED_TRUE, TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		List<Long> strategyIds = Arrays.asList(strategy1, strategy2, strategy3);

		List<StrategyBenefitPlanHeadCount> strategyBenefitPlanHeadCountList = prepareStrategyBenefitPlanHeadCountList();

		when(companyService.getCompanyDetails(company.getCode())).thenReturn(company);
		when(strategyDataDao.getHeadcountByPlanStrategyCoverage(Mockito.anyList(), Mockito.anyString()))
				.thenReturn(strategyBenefitPlanHeadCountList);

		List<StrategyBenefitPlanHeadCount> actualResult = modelCompareService
				.getMCPlanStrategyCoverageHeadcount(strategyIds, company);

		assertEquals(2, actualResult.size());
		assertEquals(3, actualResult.get(0).getStrategyCoverageLevelHeadcount().size());
		assertEquals(3, actualResult.get(1).getStrategyCoverageLevelHeadcount().size());

	}

	/// ********************************SETUP********************//

	private Company prepareCompany(long companyId, String companyName, String companyCode, String planStartDate,
			String quarter, boolean isPayrollProcessed, boolean transitionPeriod, String kaiserBandCode,
			String headQtrState) {

		Company cmp = new Company();
		cmp.setId(companyId);
		cmp.setName(companyName);
		cmp.setCode(companyCode);
		cmp.setPlanStartDate(planStartDate);
		cmp.setQuater(quarter);
		cmp.setPayrollProcessed(isPayrollProcessed);
		cmp.setTransitionPeriod(transitionPeriod);
		BandCodes bandCodes = new BandCodes();
		bandCodes.setKaiserBandCode(kaiserBandCode);
		cmp.setHeadQuatersState(headQtrState);
		cmp.setBandCodes(bandCodes);
		return cmp;
	}

	private List<StrategyBenefitPlanHeadCount> prepareStrategyBenefitPlanHeadCountList() {

		List<StrategyBenefitPlanHeadCount> strategyBenefitPlanHeadCountList = new ArrayList<StrategyBenefitPlanHeadCount>();

		// Medical Plan 1
		StrategyBenefitPlanHeadCount strategyBenefitPlanHeadCount = new StrategyBenefitPlanHeadCount();
		strategyBenefitPlanHeadCount.setBasePlanType(BSSApplicationConstants.MEDICAL);
		strategyBenefitPlanHeadCount.setBenefitPlan("TEST1");
		strategyBenefitPlanHeadCount.setBenefitPlanDescr("TEST PLAN NUMBER 1");
		strategyBenefitPlanHeadCount.setPlanType(BSSApplicationConstants.MEDICAL);

		List<StrategyCoverageLevelHeadcount> strategyCoverageLevelHeadcountList = new ArrayList<StrategyCoverageLevelHeadcount>();
		StrategyCoverageLevelHeadcount strategyCoverageLevelHeadcount = new StrategyCoverageLevelHeadcount();

		// strategy1
		strategyCoverageLevelHeadcount.setStrategyId(strategy1);
		strategyCoverageLevelHeadcount.setOffered(true);
		Map<String, Long> coverageHeadcount = new HashMap<String, Long>();
		coverageHeadcount.put("Employee Only", 0L);
		coverageHeadcount.put("Employee + Spouse", 2L);
		coverageHeadcount.put("Employee + Child(ren)", 1L);
		coverageHeadcount.put("Family", 3L);
		strategyCoverageLevelHeadcount.setCoverageHeadcount(coverageHeadcount);
		strategyCoverageLevelHeadcountList.add(strategyCoverageLevelHeadcount);

		// strategy2
		strategyCoverageLevelHeadcount.setStrategyId(strategy2);
		strategyCoverageLevelHeadcount.setOffered(true);
		coverageHeadcount = new HashMap<String, Long>();
		coverageHeadcount.put("Employee Only", 5L);
		coverageHeadcount.put("Employee + Spouse", 0L);
		coverageHeadcount.put("Employee + Child(ren)", 0L);
		coverageHeadcount.put("Family", 1L);
		strategyCoverageLevelHeadcount.setCoverageHeadcount(coverageHeadcount);
		strategyCoverageLevelHeadcountList.add(strategyCoverageLevelHeadcount);

		strategyBenefitPlanHeadCount.setStrategyCoverageLevelHeadcount(strategyCoverageLevelHeadcountList);
		strategyBenefitPlanHeadCountList.add(strategyBenefitPlanHeadCount);

		// Medical Plan 2
		strategyBenefitPlanHeadCount = new StrategyBenefitPlanHeadCount();
		strategyBenefitPlanHeadCount.setBasePlanType(BSSApplicationConstants.MEDICAL);
		strategyBenefitPlanHeadCount.setBenefitPlan("TEST2");
		strategyBenefitPlanHeadCount.setBenefitPlanDescr("TEST PLAN NUMBER 2");
		strategyBenefitPlanHeadCount.setPlanType(BSSApplicationConstants.MEDICAL);

		strategyCoverageLevelHeadcountList = new ArrayList<StrategyCoverageLevelHeadcount>();
		strategyCoverageLevelHeadcount = new StrategyCoverageLevelHeadcount();

		// strategy1
		strategyCoverageLevelHeadcount.setStrategyId(strategy1);
		strategyCoverageLevelHeadcount.setOffered(true);
		coverageHeadcount = new HashMap<String, Long>();
		coverageHeadcount.put("Employee Only", 1L);
		coverageHeadcount.put("Employee + Spouse", 2L);
		coverageHeadcount.put("Employee + Child(ren)", 3L);
		coverageHeadcount.put("Family", 4L);
		strategyCoverageLevelHeadcount.setCoverageHeadcount(coverageHeadcount);
		strategyCoverageLevelHeadcountList.add(strategyCoverageLevelHeadcount);

		// strategy2
		strategyCoverageLevelHeadcount.setStrategyId(strategy2);
		strategyCoverageLevelHeadcount.setOffered(true);
		coverageHeadcount = new HashMap<String, Long>();
		coverageHeadcount.put("Employee Only", 2L);
		coverageHeadcount.put("Employee + Spouse", 3L);
		coverageHeadcount.put("Employee + Child(ren)", 4L);
		coverageHeadcount.put("Family", 1L);
		strategyCoverageLevelHeadcount.setCoverageHeadcount(coverageHeadcount);
		strategyCoverageLevelHeadcountList.add(strategyCoverageLevelHeadcount);

		// strategy3
		strategyCoverageLevelHeadcount.setStrategyId(strategy3);
		strategyCoverageLevelHeadcount.setOffered(true);
		coverageHeadcount = new HashMap<String, Long>();
		coverageHeadcount.put("Employee Only", 3L);
		coverageHeadcount.put("Employee + Spouse", 4L);
		coverageHeadcount.put("Employee + Child(ren)", 1L);
		coverageHeadcount.put("Family", 2L);
		strategyCoverageLevelHeadcount.setCoverageHeadcount(coverageHeadcount);
		strategyCoverageLevelHeadcountList.add(strategyCoverageLevelHeadcount);

		strategyBenefitPlanHeadCount.setStrategyCoverageLevelHeadcount(strategyCoverageLevelHeadcountList);
		strategyBenefitPlanHeadCountList.add(strategyBenefitPlanHeadCount);
		return strategyBenefitPlanHeadCountList;

	}

}