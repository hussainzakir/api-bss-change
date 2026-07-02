/**
 * 
 */
package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
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
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.HeadCountService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.impl.ModelCompareServiceImpl;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.ModelCompareGroupHeadcount;
import com.trinet.ambis.service.model.ModelComparePlanTypeCost;

/**
 * @author hliddle
 *
 */
@RunWith(JUnit4.class)
public class ModelCompareServiceImpl1Test {

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
	RenewalDataDao renewalDataDao;

	@Mock
	StrategyGroupPlanSelectDao strategyGroupPlanSelectDao;
	
	@Mock
	HeadCountService headCountService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getMCStrategyHeadcountCostByGroup() {

		Long strategy1 = 1000L;
		Long strategy2 = 2000L;
		Long strategy3 = 3000L;
		List<Long> strategyIds = Arrays.asList(strategy1, strategy2, strategy3);
		Company company = new Company();
		company.setRealmPlanYear(new RealmPlanYear());
		
		List<ModelCompareGroupHeadcount> modelCompareGroupHeadcount = prepareModelCompareGroupHeadcount();

		when(strategyDataDao.getStrategyGroupHeadcountCost(strategyIds)).thenReturn(modelCompareGroupHeadcount);
		when(headCountService.getEligibleEmployeeCount(Mockito.any(Company.class), Mockito.anyLong(),
				Mockito.any(RealmPlanYear.class), Mockito.eq(true))).thenReturn(prepareHistoryHeadcount());
		when(headCountService.getEligibleEmployeeCount(Mockito.any(Company.class), Mockito.anyLong(),
				Mockito.any(RealmPlanYear.class), Mockito.eq(false))).thenReturn(prepareFutureHeadcount());
		
		when(headCountService.getPrimaryHeadCountByBenefitProgram(Mockito.any(Company.class),
				Mockito.anyLong(), Mockito.eq(true))).thenReturn(prepareHistoryEnrolledHeadcount());
		when(headCountService.getPrimaryHeadCountByBenefitProgram(Mockito.any(Company.class),
				Mockito.anyLong(), Mockito.eq(false))).thenReturn(prepareFutureEnrolledHeadcount());
		
		when(realmPlanYearService.getPreviousRealmPlanYear(Mockito.any(RealmPlanYear.class)))
				.thenReturn(preparePrevRealmPlanYear());

		List<ModelCompareGroupHeadcount> actualResults;
		
		actualResults = modelCompareService.getMCStrategyHeadcountCostByGroup(strategyIds, company);
		assertEquals(2, actualResults.size());
		assertEquals(3, actualResults.get(0).getStrategyHeadcountMap().size());
		assertEquals(12, actualResults.get(0).getStrategyHeadcountMap().get(strategy1).size());
		assertEquals(BigDecimal.ZERO, actualResults.get(0).getStrategyHeadcountMap().get(strategy1).get(6).getCost());
		assertEquals(Long.valueOf(0), actualResults.get(0).getStrategyHeadcountMap().get(strategy1).get(8).getHeadcount());
		assertEquals(Long.valueOf(10), actualResults.get(0).getStrategyHeadcountMap().get(strategy1).get(9).getHeadcount());
		assertEquals(Long.valueOf(15), actualResults.get(0).getStrategyHeadcountMap().get(strategy1).get(10).getHeadcount());
		
		assertEquals(12, actualResults.get(0).getStrategyHeadcountMap().get(strategy2).size());
		assertEquals(BigDecimal.valueOf(339.3),
				actualResults.get(0).getStrategyHeadcountMap().get(strategy2).get(6).getCost());
		assertEquals(Long.valueOf(1), actualResults.get(0).getStrategyHeadcountMap().get(strategy2).get(9).getHeadcount());
		assertEquals(Long.valueOf(5), actualResults.get(0).getStrategyHeadcountMap().get(strategy2).get(10).getHeadcount());
		
		assertEquals(12, actualResults.get(0).getStrategyHeadcountMap().get(strategy3).size());
		assertEquals(BigDecimal.ZERO, actualResults.get(0).getStrategyHeadcountMap().get(strategy3).get(6).getCost());
		assertEquals(Long.valueOf(0), actualResults.get(0).getStrategyHeadcountMap().get(strategy3).get(8).getHeadcount());
		assertEquals(Long.valueOf(0), actualResults.get(0).getStrategyHeadcountMap().get(strategy3).get(10).getHeadcount());

		assertEquals(3, actualResults.get(1).getStrategyHeadcountMap().size());
		assertEquals(12, actualResults.get(1).getStrategyHeadcountMap().get(strategy1).size());
		assertEquals(BigDecimal.ZERO, actualResults.get(1).getStrategyHeadcountMap().get(strategy1).get(6).getCost());
		assertEquals(Long.valueOf(0), actualResults.get(0).getStrategyHeadcountMap().get(strategy1).get(8).getHeadcount());
		assertEquals(Long.valueOf(10), actualResults.get(0).getStrategyHeadcountMap().get(strategy1).get(9).getHeadcount());
		assertEquals(Long.valueOf(15), actualResults.get(0).getStrategyHeadcountMap().get(strategy1).get(10).getHeadcount());
		
		assertEquals(12, actualResults.get(1).getStrategyHeadcountMap().get(strategy2).size());
		assertEquals(BigDecimal.valueOf(400),
				actualResults.get(1).getStrategyHeadcountMap().get(strategy2).get(6).getCost());
		assertEquals(Long.valueOf(0), actualResults.get(0).getStrategyHeadcountMap().get(strategy2).get(8).getHeadcount());
		assertEquals(Long.valueOf(1), actualResults.get(0).getStrategyHeadcountMap().get(strategy2).get(9).getHeadcount());
		assertEquals(Long.valueOf(5), actualResults.get(0).getStrategyHeadcountMap().get(strategy2).get(10).getHeadcount());
		
		assertEquals(12, actualResults.get(1).getStrategyHeadcountMap().get(strategy3).size());
		assertEquals(BigDecimal.ZERO, actualResults.get(1).getStrategyHeadcountMap().get(strategy3).get(6).getCost());
		assertEquals(Long.valueOf(0), actualResults.get(0).getStrategyHeadcountMap().get(strategy3).get(8).getHeadcount());
		assertEquals(Long.valueOf(0), actualResults.get(0).getStrategyHeadcountMap().get(strategy3).get(10).getHeadcount());

	}

	/// ********************************SETUP********************//
	private List<ModelCompareGroupHeadcount> prepareModelCompareGroupHeadcount() {

		List<ModelCompareGroupHeadcount> modelCompareGroupHeadcountList = new ArrayList<>();

		// Group 1
		ModelCompareGroupHeadcount modelCompareGroupHeadcount = new ModelCompareGroupHeadcount();
		modelCompareGroupHeadcount.setBenefitProgram("PROGRAM1");
		modelCompareGroupHeadcount.setGroupDescr("BENEFIT PROGRAM 1");
		Map<Long, LinkedList<ModelComparePlanTypeCost>> strategyHeadcountMap = new HashMap<>();

		// Strategy 1
		LinkedList<ModelComparePlanTypeCost> modelComparePlanTypeCostList = new LinkedList<>();
		ModelComparePlanTypeCost modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.MEDICAL);
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(3719.7));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.DENTAL);
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(186.08));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.VISION);
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(26.16));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.LIFE);
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(13));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.DISABILITY);
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(102.98));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.PRIMARY);
		modelComparePlanTypeCost.setHeadcount(0L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(4047.92));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.ADDITIONAL);
		modelComparePlanTypeCost.setHeadcount(0L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(4047.92));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);		
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType("total");
		modelComparePlanTypeCost.setHeadcount(0L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(4047.92));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		strategyHeadcountMap.put(1000L, modelComparePlanTypeCostList);

		// Strategy 2
		modelComparePlanTypeCostList = new LinkedList<>();
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.MEDICAL);
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(3719.7));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.DENTAL);
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(186.08));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.VISION);
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(26.16));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.LIFE);
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(13));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.DISABILITY);
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(102.98));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.CMTR);
		modelComparePlanTypeCost.setHeadcount(57L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(339.3));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.PRIMARY);
		modelComparePlanTypeCost.setHeadcount(0L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(4047.92));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.ADDITIONAL);
		modelComparePlanTypeCost.setHeadcount(0L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(4047.92));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);		
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType("total");
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(4047.92));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		strategyHeadcountMap.put(2000L, modelComparePlanTypeCostList);

		modelCompareGroupHeadcount.setStrategyHeadcountMap(strategyHeadcountMap);
		modelCompareGroupHeadcountList.add(modelCompareGroupHeadcount);

		// Group 2
		modelCompareGroupHeadcount = new ModelCompareGroupHeadcount();
		modelCompareGroupHeadcount.setBenefitProgram("PROGRAM2");
		modelCompareGroupHeadcount.setGroupDescr("BENEFIT PROGRAM 2");
		strategyHeadcountMap = new HashMap<>();

		// Strategy 1
		modelComparePlanTypeCostList = new LinkedList<>();
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.MEDICAL);
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(3719.7));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.DENTAL);
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(186.08));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.VISION);
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(26.16));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.LIFE);
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(13));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.DISABILITY);
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(102.98));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.PRIMARY);
		modelComparePlanTypeCost.setHeadcount(0L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(4047.92));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.ADDITIONAL);
		modelComparePlanTypeCost.setHeadcount(0L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(4047.92));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);		
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType("total");
		modelComparePlanTypeCost.setHeadcount(2L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(4047.92));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		strategyHeadcountMap.put(1000L, modelComparePlanTypeCostList);

		// Strategy 2
		modelComparePlanTypeCostList = new LinkedList<>();
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.MEDICAL);
		modelComparePlanTypeCost.setHeadcount(59L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(114013.17));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.DENTAL);
		modelComparePlanTypeCost.setHeadcount(59L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(6257.98));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.VISION);
		modelComparePlanTypeCost.setHeadcount(58L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(743.21));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.LIFE);
		modelComparePlanTypeCost.setHeadcount(57L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(377));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.DISABILITY);
		modelComparePlanTypeCost.setHeadcount(57L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(2487.04));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.CMTR);
		modelComparePlanTypeCost.setHeadcount(57L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(400));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.PRIMARY);
		modelComparePlanTypeCost.setHeadcount(0L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(4047.92));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType(BSSApplicationConstants.ADDITIONAL);
		modelComparePlanTypeCost.setHeadcount(0L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(4047.92));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);				
		modelComparePlanTypeCost = new ModelComparePlanTypeCost();
		modelComparePlanTypeCost.setPlanType("total");
		modelComparePlanTypeCost.setHeadcount(57L);
		modelComparePlanTypeCost.setCost(BigDecimal.valueOf(124217.7));
		modelComparePlanTypeCost.setOffered(true);
		modelComparePlanTypeCostList.add(modelComparePlanTypeCost);
		strategyHeadcountMap.put(2000L, modelComparePlanTypeCostList);

		modelCompareGroupHeadcount.setStrategyHeadcountMap(strategyHeadcountMap);
		modelCompareGroupHeadcountList.add(modelCompareGroupHeadcount);

		return modelCompareGroupHeadcountList;
	}
	
	private Map<String, ActiveEligibleEECount> prepareHistoryHeadcount() {
		Map<String, ActiveEligibleEECount> returnValue = new HashMap<>();
		ActiveEligibleEECount activeEligibleEECount = new ActiveEligibleEECount();
		activeEligibleEECount.setBenProg("PROGRAM1");
		activeEligibleEECount.setPrimaryHeadCount(10);
		activeEligibleEECount.setSecondaryHeadCount(5);
		activeEligibleEECount.setTotalHeadCount(15);
		returnValue.put("PROGRAM1", activeEligibleEECount);

		activeEligibleEECount = new ActiveEligibleEECount();
		activeEligibleEECount.setBenProg("PROGRAM2");
		activeEligibleEECount.setPrimaryHeadCount(100);
		activeEligibleEECount.setSecondaryHeadCount(50);
		activeEligibleEECount.setTotalHeadCount(0);
		returnValue.put("PROGRAM2", activeEligibleEECount);

		return returnValue;
	}
	
	private Map<String, ActiveEligibleEECount> prepareFutureHeadcount() {
		Map<String, ActiveEligibleEECount> returnValue = new HashMap<>();
		ActiveEligibleEECount activeEligibleEECount = new ActiveEligibleEECount();
		activeEligibleEECount.setBenProg("PROGRAM1");
		activeEligibleEECount.setPrimaryHeadCount(5);
		activeEligibleEECount.setSecondaryHeadCount(0);
		activeEligibleEECount.setTotalHeadCount(5);
		returnValue.put("PROGRAM1", activeEligibleEECount);

		activeEligibleEECount = new ActiveEligibleEECount();
		activeEligibleEECount.setBenProg("PROGRAM2");
		activeEligibleEECount.setPrimaryHeadCount(110);
		activeEligibleEECount.setSecondaryHeadCount(50);
		activeEligibleEECount.setTotalHeadCount(160);
		returnValue.put("PROGRAM2", activeEligibleEECount);

		return returnValue;
	}	

	private Map<String, Integer> prepareHistoryEnrolledHeadcount() {
		Map<String, Integer> returnValue = new HashMap<>();
		returnValue.put("PROGRAM1", 10);
		returnValue.put("PROGRAM2", 100);
		return returnValue;
	}
	
	private Map<String, Integer> prepareFutureEnrolledHeadcount() {
		Map<String, Integer> returnValue = new HashMap<>();
		returnValue.put("PROGRAM1", 1);
		returnValue.put("PROGRAM2", 105);
		return returnValue;
	}		

	private RealmPlanYear preparePrevRealmPlanYear() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1);
		return realmPlanYear;
	}
}