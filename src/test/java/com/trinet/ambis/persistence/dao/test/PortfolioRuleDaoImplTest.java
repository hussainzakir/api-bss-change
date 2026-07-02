/**
 * 
 */
package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.hrp.dto.CompanyStrategyPortfolioDetailsDto;
import com.trinet.ambis.persistence.dao.hrp.impl.PortfolioRuleDaoImpl;
import com.trinet.ambis.service.model.PlanCarrier;

/**
 * @author rvutukuri
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class PortfolioRuleDaoImplTest {

	PortfolioRuleDaoImpl portfolioRuleDao = new PortfolioRuleDaoImpl();
	EntityManager entityManager = null;
	Query mockedQuery = null;

	@Before
	public void setup() {
		entityManager = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		portfolioRuleDao.setEntityManager(entityManager);
//		when(mockedQuery.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.anyObject())).thenReturn(mockedQuery);
		when(portfolioRuleDao.getEntityManager().createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);

	}

	@Test
	public void getPortfoliosByHqRegionTest() {
		List<Object[]> results = createPlanCarriers();
		when(mockedQuery.getResultList()).thenReturn(results);
		Map<String, Map<String, String>> defaultPlanMap = new HashMap<String, Map<String, String>>();
		Map<String, Set<PlanCarrier>> portfolios = portfolioRuleDao.getPortfoliosByHqRegion(4, "MA", "","","", false );
		Set<PlanCarrier> plc = portfolios.get("medical");
		for (PlanCarrier planCarrier : plc) {
			assertEquals(planCarrier.getName(), "Aethna");
		}

	}

	@Test
	public void getStrategyPortfoliosTest() {
		List<String> expectedNames = new ArrayList<String>();
		expectedNames.add("Kaiser");
		expectedNames.add("UHC Portfolio A");
		List<Object[]> results = createPlanCarriersByStrategy();
		when(mockedQuery.getResultList()).thenReturn(results);
		Map<String, Map<String, String>> defaultPlanMap = new HashMap<String, Map<String, String>>();
		Map<String, Set<PlanCarrier>> portfolios = portfolioRuleDao.getStrategyPortfolios(1234, 3, defaultPlanMap,
				"NY", true );
		Set<PlanCarrier> plc = portfolios.get("medical");
		assertEquals(2, plc.size());
		for (PlanCarrier planCarrier : plc) {
			assertTrue(expectedNames.contains(planCarrier.getName()));
		}
	}

	/**
	 * given list of company ids</br>
	 * when getCompanyStrategyPortfolioIds method is called</br>
	 * then return the map of company id and its CompanyStrategyPortfolioDetails
	 **/
	@Test
	public void getCompanyStrategyPortfolioIdsTest1() {
		// given
		// data
		List<Long> companyIds = prepareCompanyIds();
		List<Object[]> results = prepareCompanyStrategyPortfolioIdsResult();
		// method mocks
		when(mockedQuery.getResultList()).thenReturn(results);
		// when
		Map<Long, CompanyStrategyPortfolioDetailsDto> actualResult = portfolioRuleDao
				.getCompanyStrategyPortfolioIds(companyIds);
		// then
		assertNotNull(actualResult);
		assertEquals(1, actualResult.size());
		CompanyStrategyPortfolioDetailsDto companyStrategyPortfolioDetails = actualResult.get(129059L);
		assertNotNull(companyStrategyPortfolioDetails);
		assertEquals(129059, companyStrategyPortfolioDetails.getCompanyId());
		assertEquals(1, companyStrategyPortfolioDetails.getDefaultStrategyId());
		assertEquals(Set.of(1L, 2L, 3L), companyStrategyPortfolioDetails.getAllStrategyIds());
		assertEquals(List.of(1L, 5L, 10L), companyStrategyPortfolioDetails.getDefaultStrategyPortfolioIds());
		assertEquals(64L, companyStrategyPortfolioDetails.getRealmPlanYearId());
		assertEquals(3L, companyStrategyPortfolioDetails.getRealmId());
	}

	/**
	 * given list of company ids and results are not present</br>
	 * when getCompanyStrategyPortfolioIds method is called</br>
	 * then return empty map
	 **/
	@Test
	public void getCompanyStrategyPortfolioIdsTest2() {
		// given
		// data
		List<Long> companyIds = prepareCompanyIds();
		List<Object[]> results = Collections.emptyList();
		// method mocks
		when(mockedQuery.getResultList()).thenReturn(results);
		// when
		Map<Long, CompanyStrategyPortfolioDetailsDto> actualResult = portfolioRuleDao
				.getCompanyStrategyPortfolioIds(companyIds);
		// then
		assertNotNull(actualResult);
		assertEquals(0, actualResult.size());
	}

	/**
	 * given list of company ids and allStategyIds is null</br>
	 * when getCompanyStrategyPortfolioIds method is called</br>
	 * then return the map of company id and its CompanyStrategyPortfolioDetails
	 **/
	@Test
	public void getCompanyStrategyPortfolioIdsTest3() {
		// given
		// data
		List<Long> companyIds = prepareCompanyIds();
		List<Object[]> results = prepareCompanyStrategyPortfolioIdsResult1();
		// method mocks
		when(mockedQuery.getResultList()).thenReturn(results);
		// when
		Map<Long, CompanyStrategyPortfolioDetailsDto> actualResult = portfolioRuleDao
				.getCompanyStrategyPortfolioIds(companyIds);
		// then
		assertNotNull(actualResult);
		assertEquals(1, actualResult.size());
		CompanyStrategyPortfolioDetailsDto companyStrategyPortfolioDetails = actualResult.get(129059L);
		assertNotNull(companyStrategyPortfolioDetails);
		assertNotNull(companyStrategyPortfolioDetails.getAllStrategyIds());
		assertEquals(0, companyStrategyPortfolioDetails.getAllStrategyIds().size());
	}

	/**
	 * given list of company ids and defaultStrategyPortfolioIds is null</br>
	 * when getCompanyStrategyPortfolioIds method is called</br>
	 * then return the map of company id and its CompanyStrategyPortfolioDetails
	 **/
	@Test
	public void getCompanyStrategyPortfolioIdsTest5() {
		// given
		// data
		List<Long> companyIds = prepareCompanyIds();
		List<Object[]> results = prepareCompanyStrategyPortfolioIdsResult2();
		// method mocks
		when(mockedQuery.getResultList()).thenReturn(results);
		// when
		Map<Long, CompanyStrategyPortfolioDetailsDto> actualResult = portfolioRuleDao
				.getCompanyStrategyPortfolioIds(companyIds);
		// then
		assertNotNull(actualResult);
		assertEquals(1, actualResult.size());
		CompanyStrategyPortfolioDetailsDto companyStrategyPortfolioDetails = actualResult.get(129059L);
		assertNotNull(companyStrategyPortfolioDetails);
		assertNotNull(companyStrategyPortfolioDetails.getDefaultStrategyPortfolioIds());
		assertEquals(0, companyStrategyPortfolioDetails.getDefaultStrategyPortfolioIds().size());
	}

	/**
	 * This method is for creating Plan Carriers.
	 * 
	 * @return
	 */
	public List<Object[]> createPlanCarriers() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] protfolioM = new Object[6];
		protfolioM[0] = new BigDecimal(1);
		protfolioM[1] = "Aethna";
		protfolioM[2] = "10";
		protfolioM[3] = null;
		protfolioM[4] = new BigDecimal(13);
		protfolioM[5] = new BigDecimal(1);
		results.add(protfolioM);

		Object[] protfolioM1 = new Object[6];
		protfolioM1[0] = new BigDecimal(1);
		protfolioM1[1] = "Metlife";
		protfolioM1[2] = "10";
		protfolioM1[3] = null;
		protfolioM1[4] = new BigDecimal(12);
		protfolioM1[5] = new BigDecimal(1);
		results.add(protfolioM1);

		Object[] protfolioV = new Object[6];
		protfolioV[0] = new BigDecimal(14);
		protfolioV[1] = "Guardian";
		protfolioV[2] = "14";
		protfolioV[3] = "Restricted:T";
		protfolioV[4] = new BigDecimal(13);
		protfolioV[5] = BigDecimal.ZERO;
		results.add(protfolioV);
		
		protfolioV = new Object[6];
		protfolioV[0] = new BigDecimal(6);
		protfolioV[1] = "VSP";
		protfolioV[2] = "14";
		protfolioV[3] = null;
		protfolioV[4] = null;
		protfolioV[5] = BigDecimal.ZERO;
		results.add(protfolioV);

		Object[] protfolioD = new Object[6];
		protfolioD[0] = new BigDecimal(11);
		protfolioD[1] = "Metlife";
		protfolioD[2] = "11";
		protfolioD[3] = "Restricted:T";
		protfolioD[4] = new BigDecimal(13);
		protfolioD[5] = BigDecimal.ZERO;
		results.add(protfolioD);
		
		protfolioD = new Object[6];
		protfolioD[0] = new BigDecimal(16);
		protfolioD[1] = "Delta";
		protfolioD[2] = "11";
		protfolioD[3] = "Mandatory:F";
		protfolioD[4] = new BigDecimal(13);
		protfolioD[5] = BigDecimal.ZERO;
		results.add(protfolioD);
		return results;
	}

	/**
	 * This method is for creating Plan Carriers by Strategy.
	 * 
	 * @return
	 */
	public List<Object[]> createPlanCarriersByStrategy() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] protfolioM = new Object[4];
		protfolioM[0] = new BigDecimal(9);
		protfolioM[1] = "UHC Portfolio A";
		protfolioM[2] = "10";
		protfolioM[3] = "M";
		results.add(protfolioM);

		protfolioM = new Object[4];
		protfolioM[0] = new BigDecimal(2);
		protfolioM[1] = "Kaiser";
		protfolioM[2] = "10";
		protfolioM[3] = null;
		results.add(protfolioM);

		Object[] protfolioV = new Object[4];
		protfolioV[0] = new BigDecimal(5);
		protfolioV[1] = "UHC";
		protfolioV[2] = "14";
		protfolioV[3] = "";
		results.add(protfolioV);

		protfolioV = new Object[4];
		protfolioV[0] = new BigDecimal(1);
		protfolioV[1] = "Aetna";
		protfolioV[2] = "14";
		protfolioV[3] = "Restricted:T";
		results.add(protfolioV);

		Object[] protfolioD = new Object[4];
		protfolioD[0] = new BigDecimal(3);
		protfolioD[1] = "Metlife";
		protfolioD[2] = "11";
		protfolioD[3] = "";
		results.add(protfolioD);

		protfolioD = new Object[4];
		protfolioD[0] = new BigDecimal(16);
		protfolioD[1] = "Delta";
		protfolioD[2] = "11";
		protfolioD[3] = null;
		results.add(protfolioD);
		return results;
	}
	
	private List<Long> prepareCompanyIds() {
		return List.of(129059L, 129060L);
	}

	private List<Object[]> prepareCompanyStrategyPortfolioIdsResult() {
		Object[] object = { new BigDecimal(129059), "1", "1,2,3", "1,5,10", "64", new BigDecimal(3) };
		List<Object[]> results = new ArrayList<Object[]>();
		results.add(object);
		return results;
	}

	private List<Object[]> prepareCompanyStrategyPortfolioIdsResult1() {
		Object[] object = { new BigDecimal(129059), "1", null, "1,5,10", "64", new BigDecimal(3) };
		List<Object[]> results = new ArrayList<Object[]>();
		results.add(object);
		return results;
	}

	private List<Object[]> prepareCompanyStrategyPortfolioIdsResult2() {
		Object[] object = { new BigDecimal(129059), "1", "1,2,3", null, "64", new BigDecimal(3) };
		List<Object[]> results = new ArrayList<Object[]>();
		results.add(object);
		return results;
	}

}