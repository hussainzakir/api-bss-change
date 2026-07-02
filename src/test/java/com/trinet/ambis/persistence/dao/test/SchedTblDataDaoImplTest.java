/**
 * 
 */
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

import com.trinet.ambis.persistence.dao.hrp.impl.SchedTblDataDaoImpl;
import com.trinet.ambis.service.model.SchedTblAdminDto;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class SchedTblDataDaoImplTest {

	@InjectMocks
	SchedTblDataDaoImpl schedTblDataDaoImpl;

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
	public void getPlansSelectionsByCompany() {
		when(mockedQuery.getResultList()).thenReturn(prepareScheduleTableData());

		String companyCode = "COMPANY_CODE";
		String quarter = "QUARTER";

		List<SchedTblAdminDto> actualResult = schedTblDataDaoImpl.getSchedTblAdminDates(companyCode, quarter);

		assertEquals(2, actualResult.size());
		assertEquals(1, actualResult.get(0).getRealmYearId());
		assertEquals(2, actualResult.get(1).getRealmYearId());
	}

	private List<Object[]> prepareScheduleTableData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[9];
		r[0] = "current";
		r[1] = new BigDecimal(1);
		r[2] = new Date();
		r[3] = new Date();
		r[4] = new Date();
		r[5] = new Date();
		r[6] = new Date();
		r[7] = new Date();
		r[8] = new Date();
		results.add(r);
		r = new Object[9];
		r[0] = "future";
		r[1] = new BigDecimal(2);
		r[2] = new Date();
		r[3] = new Date();
		r[4] = new Date();
		r[5] = new Date();
		r[6] = new Date();
		r[7] = new Date();
		r[8] = new Date();
		results.add(r);
		return results;
	}

}