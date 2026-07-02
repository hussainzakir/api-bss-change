/**
 * 
 */
package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.service.model.PlanRateDaoImpl;

/**
 * @author schaudhari
 *
 */
@RunWith(JUnit4.class)
@WebAppConfiguration
public class PlanRateDaoImplTest {

	@InjectMocks
	PlanRateDaoImpl planRateDao;

	@Mock
	EntityManager em;

	@Mock
	private Query mockedQuery1;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void getPrimaryBenefitPlansByRegions1() {
		when(em.createNamedQuery("getPortfolios")).thenReturn(mockedQuery1);
		when(mockedQuery1.getResultList()).thenReturn(preparePortfoliosQueryResult());
		
		Map<String, String> actualResult = planRateDao.getPortfolios(new Date());

		assertEquals(2, actualResult.size());
	}
	
	private List<Object[]> preparePortfoliosQueryResult() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[6];
		r[0] = BigDecimal.valueOf(11);
		r[1] = "Aetna";
		results.add(r);
		r = new Object[6];
		r[0] = BigDecimal.valueOf(12);
		r[1] = "BSCA";
		results.add(r);
		return results;
	}

}