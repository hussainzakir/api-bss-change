package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.dao.hrp.impl.PortfolioHeadCountDataDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.CoverageLevelHeadCount;
import com.trinet.ambis.service.model.HeadCountBenefitPlan;

@RunWith(JUnit4.class)
public class PortfolioHeadCountDataDaoImplTest {
	
	@InjectMocks
	PortfolioHeadCountDataDaoImpl portfolioHeadCountDataDao;

	@Mock
	EntityManager em = null;

	@Mock
	Query mockedQuery = null;

	private Company company = new Company();
	private Long strategyId = 1L;
	private long prevRealmPlanYearId = 0; 
	Map<String, Map<String, Set<Long>>> benefitGroupPlanTypePortfolio = new HashMap<>();

	@Before
	public void setup() {

		MockitoAnnotations.initMocks(this);
		
		Set<Long> portfolioSet = new HashSet<>();
		portfolioSet.add(21L);
		portfolioSet.add(16L);

		Map<String, Set<Long>> map = new HashMap<>();
		map.put("10", portfolioSet);
		map.put("11", portfolioSet);
		benefitGroupPlanTypePortfolio.put("BENEFIT_PROGRAM_1", map);
		when(mockedQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockedQuery);
		when(portfolioHeadCountDataDao.getEm().createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
		
		company.setCode("CODE");
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId( 0L );
		company.setRealmPlanYear( rpy );
	}


	@Test
	public void getBenefitProgramHeadCountsTest() {
		List<Object[]> mockedResult = prepareHeadCountMockData();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);
		Map<String, Map<String, Map<String, CoverageLevelHeadCount>>> phcd = portfolioHeadCountDataDao
				.getBenefitProgramHeadCounts(strategyId, benefitGroupPlanTypePortfolio);
		assertEquals(1, phcd.size());

	}
	
	@Test
	public void getProspectBenefitProgramHeadCountsTest() {
		List<Object[]> mockedResult = prepareProspectHeadCountMockData();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);
		Map<String, Map<String, Map<String, CoverageLevelHeadCount>>> phcd = portfolioHeadCountDataDao
				.getProspectBenefitProgramHeadCounts(strategyId, benefitGroupPlanTypePortfolio);
		assertEquals(1, phcd.size());

	}

	@Test
	public void getHeadCountPlansTest() {
		List<Object[]> mockedResult = prepareHeadCountMockData();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);
		Map<String, List<HeadCountBenefitPlan>> phcd = portfolioHeadCountDataDao.getHeadCountPlans(strategyId);
		assertEquals(2, phcd.size());
	}
	
	@Test
	public void getProspectHeadCountPlansTest() {
		List<Object[]> mockedResult = prepareProspectHeadCountMockData();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);
		Map<String, List<HeadCountBenefitPlan>> phcd = portfolioHeadCountDataDao.getProspectHeadCountPlans(strategyId);
		assertEquals(2, phcd.size());

	}

	@Test
	public void getMirrorPlanHeadCounts() {
		List<Object[]> mockedResult = prepareMirrorPlanHeadCountMockData();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);
		Map<String, Map<String, Map<String, Long>>> actualResult = portfolioHeadCountDataDao.getMirrorPlanHeadCounts(company.getCode(), prevRealmPlanYearId);
		assertEquals(2, actualResult.size());
	}
	
	private List<Object[]> prepareHeadCountMockData() {
		List<Object[]> data = new ArrayList<>();
		data.add(prepareHeadCountMockData("BENEFIT_PROGRAM_1", "10", "MED_PLAN_1", 21, "4", 1, 1));
		data.add(prepareHeadCountMockData("BENEFIT_PROGRAM_1", "11", "GROUP_DENTAL_PLAN_1", 16, "1", 1, 1));
		data.add(prepareHeadCountMockData("BENEFIT_PROGRAM_1", "1D", "OPT_DENTAL_PLAN_1", 21, "4", 1, 1));
		data.add(prepareHeadCountMockData("BENEFIT_PROGRAM_1", "1V", "OPT_VISION_PLAN_1", 21, "4", 1, 1));
		data.add(prepareHeadCountMockData("BENEFIT_PROGRAM_1", "10", "MED_PLAN_2", 21, "C", 1, 1));
		data.add(prepareHeadCountMockData("BENEFIT_PROGRAM_1", "10", "MED_PLAN_1", 21, "C", 1, 1));
		data.add(prepareHeadCountMockData("BENEFIT_PROGRAM_1", "10", "MED_PLAN_1", 10, "C", 1, 1));
		data.add(prepareHeadCountMockData("BENEFIT_PROGRAM_2", "10", "MED_PLAN_1", 10, "C", 1, 1));
		data.add(prepareHeadCountMockData("BENEFIT_PROGRAM_2", "10", "MED_PLAN_1", 10, "C", 1, 1));
		data.add(prepareHeadCountMockData("BENEFIT_PROGRAM_2", "10", "MED_PLAN_2", 10, "1", 1, 1));
		return data;
	}
	
	private List<Object[]> prepareProspectHeadCountMockData() {
		List<Object[]> data = new ArrayList<>();
		data.add(prepareProspectHeadCountMockData("BENEFIT_PROGRAM_1", "10", "MED_PLAN_1", 21, "4", 1, 2));
		data.add(prepareProspectHeadCountMockData("BENEFIT_PROGRAM_1", "11", "GROUP_DENTAL_PLAN_1", 16, "1", 1, 0));
		data.add(prepareProspectHeadCountMockData("BENEFIT_PROGRAM_1", "1D", "OPT_DENTAL_PLAN_1", 21, "4", 1, 0));
		data.add(prepareProspectHeadCountMockData("BENEFIT_PROGRAM_1", "1V", "OPT_VISION_PLAN_1", 21, "4", 1, 0));
		data.add(prepareProspectHeadCountMockData("BENEFIT_PROGRAM_1", "10", "MED_PLAN_2", 21, "C", 1, 0));
		data.add(prepareProspectHeadCountMockData("BENEFIT_PROGRAM_1", "10", "MED_PLAN_1", 21, "C", 1, 0));
		data.add(prepareProspectHeadCountMockData("BENEFIT_PROGRAM_1", "10", "MED_PLAN_1", 10, "C", 1, 0));
		data.add(prepareProspectHeadCountMockData("BENEFIT_PROGRAM_2", "10", "MED_PLAN_1", 10, "1", 1, 1));
		data.add(prepareProspectHeadCountMockData("BENEFIT_PROGRAM_2", "10", "MED_PLAN_1", 10, "C", 1, 0));
		data.add(prepareProspectHeadCountMockData("BENEFIT_PROGRAM_2", "10", "MED_PLAN_3", 10, "1", 1, 0));
		return data;
	}

	private Object[] prepareHeadCountMockData(String benPrg, String benType, String benPlan,
			int portfolioId, String cvgCode, int headCnt, int hsaHeadCnt) {

		Object[] r = new Object[8];
		r[0] = benPrg;
		r[1] = benType;
		r[2] = benPlan;
		r[3] = new BigDecimal(portfolioId);
		r[4] = "PORTFOLIO_" + portfolioId;
		r[5] = cvgCode;
		r[6] = new BigDecimal(headCnt);
		r[7] = new BigDecimal(hsaHeadCnt);

		return r;
	}
	
	private Object[] prepareProspectHeadCountMockData(String benPrg, String benType, String benPlan,
			int portfolioId, String cvgCode, int headCnt, int hsaHeadCnt) {
		Object[] r = new Object[7];
		r[0] = benPrg;
		r[1] = benType;
		r[2] = benPlan;
		r[3] = new BigDecimal(portfolioId);
		r[4] = cvgCode;
		r[5] = new BigDecimal(headCnt);
		r[6] = new BigDecimal(hsaHeadCnt);

		return r;
	}

	private List<Object[]> prepareMirrorPlanHeadCountMockData() {
		List<Object[]> data = new ArrayList<>();
		Object[] r = new Object[4];
		r[0] = "PROGRAM_1";
		r[1] = "MEDICAL_PLAN_1";
		r[2] = CoverageCodesEnums.COV_EMPLOYEE.getCode();
		r[3] = new BigDecimal(21);
		data.add(r);
		
		r = new Object[4];
		r[0] = "PROGRAM_1";
		r[1] = "MEDICAL_PLAN_1";
		r[2] = CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode();
		r[3] = new BigDecimal(3);
		data.add(r);
		
		r = new Object[4];
		r[0] = "PROGRAM_1";
		r[1] = "MEDICAL_PLAN_2";
		r[2] = CoverageCodesEnums.COV_EMPLOYEE.getCode();
		r[3] = new BigDecimal(15);
		data.add(r);
		
		r = new Object[4];
		r[0] = "PROGRAM_2";
		r[1] = "MEDICAL_PLAN_1";
		r[2] = CoverageCodesEnums.COV_EMPLOYEE.getCode();
		r[3] = new BigDecimal(10);
		data.add(r);

		return data;
	}
}
