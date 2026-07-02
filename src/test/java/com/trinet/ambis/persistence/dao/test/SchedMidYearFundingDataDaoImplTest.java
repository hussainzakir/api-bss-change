package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import com.trinet.ambis.persistence.dao.hrp.impl.SchedMidYearFundingDataDaoImpl;
import com.trinet.ambis.persistence.model.SchedMidYearFunding;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class SchedMidYearFundingDataDaoImplTest {

	@InjectMocks
	SchedMidYearFundingDataDaoImpl schedMidYearFundingDataDaoImpl;

	@Mock
	EntityManager em;

	private Query mockedQuery = null;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockedQuery = mock(Query.class);
		when(em.createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
	}

	@Test
	public void getMidYearFundingScheduleForCompany() {
		String companyCode = "CODE";
		when(mockedQuery.getResultList()).thenReturn(prepareSchedMidYearFundingData());
		SchedMidYearFunding actualResult = schedMidYearFundingDataDaoImpl.getMidYearFundingScheduleForCompany(companyCode);
		assertEquals(1, actualResult.getCompanyId());
	}

	private List<Object[]> prepareSchedMidYearFundingData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[9];
		r[0] = BigDecimal.valueOf(1);
		r[1] = BigDecimal.valueOf(1);
		r[2] = BigDecimal.valueOf(1);
		r[3] = BigDecimal.valueOf(1);
		r[4] = "SERVICE_ORDER_NUMBER";
		r[5] = new Date();
		r[6] = BigDecimal.valueOf(1);
		r[7] = "LAST_UPDATED_BY";
		r[8] = new Date();
		results.add(r);
		return results;
	}
	
}