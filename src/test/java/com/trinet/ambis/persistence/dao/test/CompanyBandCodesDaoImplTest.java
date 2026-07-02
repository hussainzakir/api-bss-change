package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.hrp.impl.CompanyBandCodesDaoImpl;
import com.trinet.ambis.service.model.CompanyBandCodes;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class CompanyBandCodesDaoImplTest {

	CompanyBandCodesDaoImpl compBandCodesDaoImpl;
	long companyId = 1111L;
	EntityManager em = null;
	Query mockedQuery = null;

	@Before
	public void setup() {
		em = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		when(em.createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
		compBandCodesDaoImpl = new CompanyBandCodesDaoImpl();
		compBandCodesDaoImpl.setEm(em);
	}

	@Test
	public void getBandCodesByCompanyId() {
		when(mockedQuery.getResultList()).thenReturn(prepareBandCodesByCompanyId());

		List<CompanyBandCodes> actualResults = compBandCodesDaoImpl.getBandCodesByCompanyId(companyId);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResults.size());
	}

	@Test
	public void insertUpdateCompanyBandCodes() {
		List<CompanyBandCodes> companyBandCodes = new ArrayList<CompanyBandCodes>();
		CompanyBandCodes cb = new CompanyBandCodes();
		cb.setBandCodeType("BCBS");
		companyBandCodes.add(cb);
		when(mockedQuery.executeUpdate()).thenReturn(1);

		int actualResults = compBandCodesDaoImpl.insertUpdateCompanyBandCodes(companyId, companyBandCodes);

		verify(mockedQuery, times(2)).executeUpdate();
		assertEquals(1, actualResults);
	}
	
	@Test
	public void getProspectBandEffDateTest() {
		when(mockedQuery.getSingleResult()).thenReturn(new Date());
		Date effectiveDate = compBandCodesDaoImpl.getProspectBandEffDate("23467gyt", 2L);
		verify(mockedQuery, times(1)).getSingleResult();
		assertNotNull(effectiveDate);
	}
	
	@Test
	public void getProspectBandEffDateExceptionTest() {
		when( mockedQuery.getSingleResult() ).thenThrow( new javax.persistence.NoResultException() );
		Date effectiveDate = compBandCodesDaoImpl.getProspectBandEffDate("23467gyt", 2L);
		verify(mockedQuery, times(1)).getSingleResult();
		assertNull(effectiveDate);
	}

	private List<Object[]> prepareBandCodesByCompanyId() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[3];
		r[0] = new BigDecimal(9407);
		r[1] = "AETNA";
		r[2] = "18";
		results.add(r);
		r = new Object[3];
		r[0] = new BigDecimal(9407);
		r[1] = "BCBS";
		r[2] = "46";
		results.add(r);
		return results;
	}

}