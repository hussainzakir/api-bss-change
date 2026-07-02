package com.trinet.ambis.persistence.dao.hrp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.dao.hrp.dto.ExchangeCarrierDetailsDto;
import com.trinet.ambis.persistence.dao.hrp.impl.ExchangeDaoImpl;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.DaoUtils;

@RunWith(MockitoJUnitRunner.class)
public class ExchangeDaoImplTest extends ServiceUnitTest {

	@InjectMocks
	private ExchangeDaoImpl exchangeDaoImplMocks;

	@Mock
	EntityManager em;

	@Mock
	Query query;
    private MockedStatic<DaoUtils> mockedStaticDaoUtils;

    @Before
    public void setUp() {
        mockedStaticDaoUtils = org.mockito.Mockito.mockStatic(DaoUtils.class);
    }

    @After
    public void tearDown() {
        if (mockedStaticDaoUtils != null) {
            mockedStaticDaoUtils.close();
            mockedStaticDaoUtils = null;
        }
    }

	@Test
	public void getExchangeCarriersWithRecordsTest() {
		// given
		List<Object[]> carriers = prepareCarriers();
		String companyCode = "PROSPECT-COMPANY-CODE";
		Date benStartDt = new Date();
		when(em.createNamedQuery("GET_EXCHANGE_CARRIERS")).thenReturn(query);
		when(DaoUtils.getResultList(query, "GET_EXCHANGE_CARRIERS")).thenReturn(carriers);
		// when
		List<ExchangeCarrierDetailsDto> actualResult = exchangeDaoImplMocks.getExchangeCarriers(companyCode, "NY",
				"10601", benStartDt);
		// then
		// asserts
		assertNotNull(actualResult);
		assertEquals(2, actualResult.size());
		ExchangeCarrierDetailsDto actualResult1 = actualResult.get(0);
		assertEquals(1L, actualResult1.getRealmId());
		assertEquals(13L, actualResult1.getPortfolioId());
		assertEquals("Tufts", actualResult1.getPortfolioName());
		assertTrue(actualResult1.isStrategyCreated());
		ExchangeCarrierDetailsDto actualResult2 = actualResult.get(1);
		assertEquals(2L, actualResult2.getRealmId());
		assertEquals(14L, actualResult2.getPortfolioId());
		assertEquals("Guardian", actualResult2.getPortfolioName());
		assertFalse(actualResult2.isStrategyCreated());
		// verify
		verify(em, times(1)).createNamedQuery("GET_EXCHANGE_CARRIERS");
		verify(DaoUtils.class, times(1));
		DaoUtils.getResultList(query, "GET_EXCHANGE_CARRIERS");
	}

	@Test
	public void getExchangeCarriersWithNoRecordsTest() {
		// given
		String companyCode = "PROSPECT-COMPANY-CODE";
		Date benStartDt = new Date();
		when(em.createNamedQuery("GET_EXCHANGE_CARRIERS")).thenReturn(query);
		when(DaoUtils.getResultList(query, "GET_EXCHANGE_CARRIERS")).thenReturn(Collections.emptyList());
		// when
		List<ExchangeCarrierDetailsDto> actualResult = exchangeDaoImplMocks.getExchangeCarriers(companyCode, "NY",
				"10952", benStartDt);
		// then
		// asserts
		assertNotNull(actualResult);
		assertEquals(0, actualResult.size());
		// verify
		verify(em, times(1)).createNamedQuery("GET_EXCHANGE_CARRIERS");
		verify(DaoUtils.class, times(1));
		DaoUtils.getResultList(query, "GET_EXCHANGE_CARRIERS");
	}

	/**
	 * Mock rows returned from GET_EXCHANGE_CARRIERS query
		<li>code
		<li>companyId
		<li>realmId
		<li>realmYearId
		<li>portfolioId
		<li>descr
		<li>planType
		<li>rule
		<li>parentPortfolioId
		<li>isStrategyCreated
	 * @return
	 */
	private List<Object[]> prepareCarriers() {
		List<Object[]> carriers = new ArrayList<>();
		Object[] carrier1 = { "PROSPECT-COMPANY-CODE", new BigDecimal(1), new BigDecimal(1), new BigDecimal(68), new BigDecimal(13), "Tufts", "10", "Restricted:T", null, "true" };
		carriers.add(carrier1);
		Object[] carrier2 = { "PROSPECT-COMPANY-CODE", new BigDecimal(2), new BigDecimal(2), new BigDecimal(70), new BigDecimal(14), "Guardian", "10", "Restricted:T", null, "false" };
		carriers.add(carrier2);
		return carriers;
	}

}
