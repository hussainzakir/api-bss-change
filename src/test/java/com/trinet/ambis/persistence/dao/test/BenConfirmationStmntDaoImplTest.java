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
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.impl.BenConfirmationStmntDaoImpl;
import com.trinet.ambis.service.model.BenConfirmationStatement;


@RunWith(JUnit4.class)

@WebAppConfiguration
public class BenConfirmationStmntDaoImplTest {
	
	@InjectMocks
	BenConfirmationStmntDaoImpl benConfirmationStmntDaoImpl;

	@Mock
	EntityManager em;

	@Mock
	EntityManager entityManager;

	private Query mockedQuery = null;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockedQuery = mock(Query.class);
		when(em.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		when(entityManager.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		
	}
	
	@Test
	public void getConfirmationStatements() {
		when(mockedQuery.getParameterValue(BSSQueryConstants.COMPANY)).thenReturn("L1M");
		when(mockedQuery.getResultList()).thenReturn(prepareConfirmationStatements());
		List<BenConfirmationStatement> actualResult = benConfirmationStmntDaoImpl.getBenefitConfirmationStatementsBy("L1M");
		assertEquals(1, actualResult.size());
	}
	
	
	private List<Object[]> prepareConfirmationStatements() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[10];
		r[0] = new BigDecimal(15);
		r[1] = new BigDecimal(18);
		r[2] = new Date(9999999999L);
		r[3] = new Date(9999999999L);
		r[4] = new Date(9999999999L);
		r[5] = "Corenda Snesrud";
		r[6] = "UJNDJ6TNBM0J";
		r[7] = new Date(9999999999L);
		r[8] = "Y";
        results.add(r);
		return results;
	}


}
