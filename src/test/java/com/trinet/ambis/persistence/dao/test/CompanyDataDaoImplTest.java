
package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.trinet.ambis.service.unit.ServiceUnitTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.dao.hrp.dto.CompanyDetailsDto;
import com.trinet.ambis.persistence.dao.hrp.dto.CompanyStrategyDetailsDto;
import com.trinet.ambis.persistence.dao.hrp.impl.CompanyDataDaoImpl;
import com.trinet.ambis.rest.controllers.dto.BundleSelectionDetailsRequest;
import com.trinet.ambis.service.model.BundleSelectionDetailsDto;
import com.trinet.ambis.service.model.CompanyRealmData;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.DaoUtils;

@RunWith(MockitoJUnitRunner.class)
public class CompanyDataDaoImplTest extends ServiceUnitTest {

	CompanyDataDaoImpl companyDataDao;

	Query mockedQuery;

	EntityManager mockedEm;
	
	@Captor
	ArgumentCaptor<Query> queryCaptor;

	@Captor
	ArgumentCaptor<String> queryNameCaptor;

	private MockedStatic<DaoUtils> daoUtilsMockedStatic;
	private MockedStatic<AppRulesAndConfigsUtils> appRulesAndConfigsUtilsMockedStatic;

	@Before
	public void setup() {
		daoUtilsMockedStatic = Mockito.mockStatic(DaoUtils.class);
		appRulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
		mockedEm = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		companyDataDao = new CompanyDataDaoImpl();
		companyDataDao.setEm(mockedEm);
		when(mockedEm.createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
	}

	@After
	public void tearDown() {
		if(daoUtilsMockedStatic != null)
			daoUtilsMockedStatic.close();
		if(appRulesAndConfigsUtilsMockedStatic != null)
			appRulesAndConfigsUtilsMockedStatic.close();
	}

	@Test
	public void getRegionsByCompanyId() {
		when(DaoUtils.getResultStringList(mockedQuery, "REGIONS_BY_COMPANY_ID"))
				.thenReturn(Arrays.asList("MA", "SC"));

		Set<String> actual = companyDataDao.getRegionsByCompanyId(1111L);

		assertEquals(2, actual.size());
	}

	@Test
	public void insertUpdateCompanyRegions() {
		when(DaoUtils.executeUpdate(mockedQuery, "DELETE_COMPANY_REGIONS")).thenReturn(1);
		when(DaoUtils.executeUpdate(mockedQuery, "INSERT_COMPANY_REGIONS")).thenReturn(1);

		Long companyId = 1111L;
		Set<String> regions = new HashSet<>();
		regions.add("MA");
		regions.add("NC");

		int actual = companyDataDao.insertUpdateCompanyRegions(companyId, regions);

		assertEquals(2, actual);

		verify(DaoUtils.class, VerificationModeFactory.times(1));
		DaoUtils.executeUpdate(mockedQuery, "DELETE_COMPANY_REGIONS");

		verify(DaoUtils.class, VerificationModeFactory.times(2));
		DaoUtils.executeUpdate(mockedQuery, "INSERT_COMPANY_REGIONS");
	}
	
	@Test
	public void getAvailableCompanyRealms() {
		when(DaoUtils.getResultList(Mockito.any(Query.class), Mockito.anyString()))
				.thenReturn(prepareCompanyRealmData());

		List<CompanyRealmData> actual = companyDataDao.getAvailableCompanyRealms("COMPANY_CODE", true);
		verify(DaoUtils.class, VerificationModeFactory.times(1));
		DaoUtils.getResultList(Mockito.any(Query.class), Mockito.anyString());
		assertEquals(2, actual.size());

	}
	
	/**
	 * given company code</br>
	 * when getCompanyStrategyDetails method is called</br>
	 * then return the map of company id and its CompanyStrategyDetailsDto
	 **/
	@Test
	public void getCompanyStrategyDetailsTest1() {
		// given
		// data
		String companyCode = "B5NP1PC1";
		List<Object[]> results = preparegetCompanyStrategyDetailsResult();
		// method mocks
		when(DaoUtils.getResultList(queryCaptor.capture(), queryNameCaptor.capture())).thenReturn(results);
		// when
		Map<Long, CompanyStrategyDetailsDto> actualResult = companyDataDao.getCompanyStrategyDetails(companyCode);
		// then
		// assertions
		assertNotNull(actualResult);
		assertEquals(1, actualResult.size());
		CompanyStrategyDetailsDto companyStrategyDetailsDto = actualResult.get(129059L);
		assertNotNull(companyStrategyDetailsDto);
		assertEquals(129059, companyStrategyDetailsDto.getCompanyId());
		assertEquals(Set.of(1L, 2L, 3L), companyStrategyDetailsDto.getAllStrategyIds());
		assertEquals(64L, companyStrategyDetailsDto.getRealmPlanYearId());
	}

	/**
	 * given  company code and results are not present</br>
	 * when getCompanyStrategyDetails method is called</br>
	 * then return empty map
	 **/
	@Test
	public void getCompanyStrategyDetailsTest2() {
		// given
		// data
		String companyCode = "B5NP1PC1";
		List<Object[]> results = Collections.emptyList();
		// method mocks
		when(DaoUtils.getResultList(queryCaptor.capture(), queryNameCaptor.capture())).thenReturn(results);
		// when
		Map<Long, CompanyStrategyDetailsDto> actualResult = companyDataDao.getCompanyStrategyDetails(companyCode);
		// then
		// assertions
		assertNotNull(actualResult);
		assertEquals(0, actualResult.size());
	}

	/**
	 * given company code and and allStategyIds is null</br>
	 * when getCompanyStrategyPortfolioIds method is called</br>
	 * then return the map of company id and its CompanyStrategyDetailsDto
	 **/
	@Test
	public void getCompanyStrategyDetailsTest3() {
		// given
		// data
		String companyCode = "B5NP1PC1";
		List<Object[]> results = preparegetCompanyStrategyDetailsResult1();
		// method mocks
		when(DaoUtils.getResultList(queryCaptor.capture(), queryNameCaptor.capture())).thenReturn(results);
		// when
		Map<Long, CompanyStrategyDetailsDto> actualResult = companyDataDao.getCompanyStrategyDetails(companyCode);
		// then
		// assertions
		assertNotNull(actualResult);
		assertEquals(1, actualResult.size());
		CompanyStrategyDetailsDto companyStrategyDetailsDto = actualResult.get(129059L);
		assertNotNull(companyStrategyDetailsDto);
		assertNotNull(companyStrategyDetailsDto.getAllStrategyIds());
		assertEquals(0, companyStrategyDetailsDto.getAllStrategyIds().size());
	}
	
	/*
	 * 
	 * Setup methods
	 * 
	 */
	
	private List<Object[]> prepareCompanyRealmData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[7];
		r[0] = "current";
		r[1] = new BigDecimal(1111);
		r[2] = new BigDecimal(1);
		r[3] = "PRODUCT";
		r[4] = "OE_QUARTER";
		r[5] = new Date();
		r[6] = new Date();
		results.add(r);

		r = new Object[7];
		r[0] = "future";
		r[1] = new BigDecimal(22222);
		r[2] = new BigDecimal(2);
		r[3] = "PRODUCT";
		r[4] = "OE_QUARTER";
		r[5] = new Date();
		r[6] = new Date();
		results.add(r);
		return results;
	}
	
	@Test
	public void updateAcaLargeEmplrAsTrue() {
		when(DaoUtils.executeUpdate(mockedQuery, "UPDATE_COMPANY_ACA_LARGE_EMPLR")).thenReturn(1);
		
		Long companyId = 1111L;
		int actual = companyDataDao.updateAcaLargeEmplr(companyId, true);
		assertEquals(1, actual);
		
		verify(DaoUtils.class, VerificationModeFactory.times(1));
		DaoUtils.executeUpdate(mockedQuery, "UPDATE_COMPANY_ACA_LARGE_EMPLR");
	}
	
	@Test
	public void updateAcaLargeEmplrAsFalse() {
		when(DaoUtils.executeUpdate(mockedQuery, "UPDATE_COMPANY_ACA_LARGE_EMPLR")).thenReturn(1);
		
		Long companyId = 1111L;
		int actual = companyDataDao.updateAcaLargeEmplr(companyId, false);
		assertEquals(1, actual);
		
		verify(DaoUtils.class, VerificationModeFactory.times(1));
		DaoUtils.executeUpdate(mockedQuery, "UPDATE_COMPANY_ACA_LARGE_EMPLR");
	}

	@Test
	public void getBundleSelectionDetails_ShouldReturnRows() {
		List<Object[]> queryResults = new ArrayList<>();
		queryResults.add(new Object[] { 192557L, 88L, "001Ea00001LXQzEIAX", "TriNet III" });

		Query nativeQuery = mock(Query.class);
		when(mockedEm.createNativeQuery(Mockito.anyString())).thenReturn(nativeQuery);
		when(nativeQuery.getResultList()).thenReturn(queryResults);

		List<BundleSelectionDetailsRequest.ExchangeDates> exchangeDatePairs = List.of(
				BundleSelectionDetailsRequest.ExchangeDates.builder()
						.exchange("TriNet III").effectiveDate("2026-03-01").build()
		);

		List<BundleSelectionDetailsDto> actual = companyDataDao
				.getBundleSelectionDetails("001Ea00001LXQzEIAX", exchangeDatePairs);

		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(Long.valueOf(192557L), actual.get(0).getCompanyId());
		assertEquals("TNIII", actual.get(0).getExchangeId());
		verify(nativeQuery).setParameter("companyCode", "001Ea00001LXQzEIAX");
		verify(nativeQuery).setParameter("exchange0", "TriNet III");
	}

	@Test
	public void getBundleSelectionDetails_ShouldReturnEmptyList() {
		Query nativeQuery = mock(Query.class);
		when(mockedEm.createNativeQuery(Mockito.anyString())).thenReturn(nativeQuery);
		when(nativeQuery.getResultList()).thenReturn(Collections.emptyList());

		List<BundleSelectionDetailsRequest.ExchangeDates> exchangeDatePairs = List.of(
				BundleSelectionDetailsRequest.ExchangeDates.builder()
						.exchange("TriNet I").effectiveDate("2026-03-01").build()
		);

		List<BundleSelectionDetailsDto> actual = companyDataDao
				.getBundleSelectionDetails("NONEXISTENT_COMPANY", exchangeDatePairs);

		assertNotNull(actual);
		assertEquals(0, actual.size());
		verify(nativeQuery).setParameter("companyCode", "NONEXISTENT_COMPANY");
	}

	@Test
	public void getBundleSelectionDetails_ShouldReturnEmptyList_WhenExchangeDatePairsEmpty() {
		List<BundleSelectionDetailsDto> actual = companyDataDao
				.getBundleSelectionDetails("SOME_COMPANY", Collections.emptyList());

		assertNotNull(actual);
		assertEquals(0, actual.size());
	}

	private List<Object[]> preparegetCompanyStrategyDetailsResult() {
		Object[] object = { new BigDecimal(129059), "1,2,3", "64" };
		List<Object[]> results = new ArrayList<Object[]>();
		results.add(object);
		return results;
	}
	
	private List<Object[]> preparegetCompanyStrategyDetailsResult1() {
		Object[] object = { new BigDecimal(129059), null, "64" };
		List<Object[]> results = new ArrayList<Object[]>();
		results.add(object);
		return results;
	}

	/**
	 * given valid company id with results present</br>
	 * when getCompanyDetailsById method is called</br>
	 * then return the CompanyDetailsDto with all fields populated
	 **/
	@Test
	public void getCompanyDetailsById_ShouldReturnCompanyDetails() {
		// given
		Long companyId = 1111L;
		Date planYearStart = new Date();
		List<Object[]> results = new ArrayList<>();
		Object[] row = new Object[9];
		row[0] = "G48";
		row[1] = planYearStart;
		row[2] = "CLONE_BENPGM_VALUE";
		row[3] = new BigDecimal(123);
		row[4] = "Q1";
		row[5] = 541511;
		row[6] = 1;
		row[7] = new BigDecimal(2);
		row[8] = "TriNet III";
		results.add(row);

		when(DaoUtils.getResultList(mockedQuery, "GET_COMPANY_DETAILS_BY_ID")).thenReturn(results);

		// when
		CompanyDetailsDto actual = companyDataDao.getCompanyDetailsById(companyId);

		// then
		assertNotNull(actual);
		assertEquals("G48", actual.getCode());
		assertEquals(planYearStart, actual.getPlanYearStart());
		assertEquals("CLONE_BENPGM_VALUE", actual.getCloneBenpgm());
		assertEquals(Long.valueOf(123L), actual.getBundleSeq());
		assertEquals("Q1", actual.getOeQuarter());
		assertEquals(Integer.valueOf(541511), actual.getNaicsCode());
		assertEquals(Integer.valueOf(1), actual.getLargeDealProspect());
		assertEquals(Long.valueOf(2L), actual.getNaicsBundleId());
		assertEquals("TNIII", actual.getExchangeId());
		verify(mockedEm).createNamedQuery("GET_COMPANY_DETAILS_BY_ID");
		verify(mockedQuery).setParameter("companyId", companyId);
	}

	/**
	 * given valid company id but no results present</br>
	 * when getCompanyDetailsById method is called</br>
	 * then return null
	 **/
	@Test
	public void getCompanyDetailsById_ShouldReturnNull_WhenNoResults() {
		// given
		Long companyId = 9999L;
		when(DaoUtils.getResultList(mockedQuery, "GET_COMPANY_DETAILS_BY_ID")).thenReturn(Collections.emptyList());

		// when
		CompanyDetailsDto actual = companyDataDao.getCompanyDetailsById(companyId);

		// then
		assertNull(actual);
		verify(mockedEm).createNamedQuery("GET_COMPANY_DETAILS_BY_ID");
		verify(mockedQuery).setParameter("companyId", companyId);
	}

	/**
	 * given valid company id with null bundle_seq</br>
	 * when getCompanyDetailsById method is called</br>
	 * then return the CompanyDetailsDto with bundleSeq as null
	 **/
	@Test
	public void getCompanyDetailsById_ShouldHandleNullBundleSeq() {
		// given
		Long companyId = 2222L;
		Date planYearStart = new Date();
		List<Object[]> results = new ArrayList<>();
		Object[] row = new Object[9];
		row[0] = "G49";
		row[1] = planYearStart;
		row[2] = "CLONE_PGM";
		row[3] = null; // null bundle_seq
		row[4] = "Q2";
		row[5] = null; // null naics_code
		row[6] = null; // null large_deal_prospect
		row[7] = null; // null naics_bundle_id
		row[8] = "TriNet III";
		results.add(row);

		when(DaoUtils.getResultList(mockedQuery, "GET_COMPANY_DETAILS_BY_ID")).thenReturn(results);

		// when
		CompanyDetailsDto actual = companyDataDao.getCompanyDetailsById(companyId);

		// then
		assertNotNull(actual);
		assertEquals("G49", actual.getCode());
		assertEquals(planYearStart, actual.getPlanYearStart());
		assertEquals("CLONE_PGM", actual.getCloneBenpgm());
		assertNull(actual.getBundleSeq());
		assertEquals("Q2", actual.getOeQuarter());
		assertNull(actual.getNaicsCode());
		assertNull(actual.getLargeDealProspect());
		assertNull(actual.getNaicsBundleId());
	}

}
