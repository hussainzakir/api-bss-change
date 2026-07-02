package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.ps.impl.SupplementalAuthDaoImpl;
import com.trinet.ambis.service.model.SupplementalLtdAuthReponse;

@RunWith(JUnit4.class)
public class SupplementalAuthDaoImplTest {

	@InjectMocks
	private SupplementalAuthDaoImpl supplementalAuthDao;

	@Mock
	private EntityManager entityManager;

	@Mock
	private Query query;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getExecSuppLtdAuthResponse() {
		Calendar cal = Calendar.getInstance();
		cal.set(2021, 8, 11);

		List<Object[]> results = new ArrayList<Object[]>();
		Object[] obj = new Object[4];
		obj[0] = "G48";
		obj[1] = "00002222262";
		obj[2] = "N";
		obj[3] = cal.getTime();
		results.add(obj);

		when(entityManager.createNamedQuery("GET_SUPP_LTD_AUTH_RESPONSE")).thenReturn(query);
		when(query.getResultList()).thenReturn(results);

		SupplementalLtdAuthReponse result = supplementalAuthDao.getExecSuppLtdAuthResponse("G48");

		assertEquals("N", String.valueOf(result.getAnswer()));
		assertEquals("00002222262", result.getAuthUserId());
		assertEquals(cal.getTime(), result.getAuthDate());
		verify(query, times(1)).setParameter(BSSQueryConstants.COMPANY_CODE, "G48");
	}

	@Test
	public void saveExecSuppLtdAuthResponse() {
		SupplementalLtdAuthReponse obj = SupplementalLtdAuthReponse.builder().answer('N').authUserId("00002222262")
				.build();
		ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Object> valCaptor = ArgumentCaptor.forClass(Object.class);

		when(entityManager.createNamedQuery("INSERT_SUPP_LTD_AUTH_RESPONSE")).thenReturn(query);

		supplementalAuthDao.saveExecSuppLtdAuthResponse("G48", obj);

		verify(query, times(1)).executeUpdate();
		verify(query, times(4)).setParameter(argCaptor.capture(), valCaptor.capture());
		assertEquals("G48", valCaptor.getAllValues().get(0));
		assertEquals("00002222262", valCaptor.getAllValues().get(1));
		assertEquals('N', valCaptor.getAllValues().get(2));
	}

}
