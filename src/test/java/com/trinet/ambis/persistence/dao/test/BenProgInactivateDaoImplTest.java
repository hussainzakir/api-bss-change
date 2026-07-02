package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.ps.impl.BenProgInactivateDaoImpl;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class BenProgInactivateDaoImplTest {
	BenProgInactivateDaoImpl benProgInactivateDaoImpl;
	EntityManager em = null;
	Query mockedQuery = null;
	Company comp = null;
	BenefitGroup group = null;

	@Before
	public void setup() {
		comp = new Company();
		comp.setCode("G48");
		comp.setPlanStartDate("02-OCT-2018");
		comp.setPfClient("pfClient");
		Realm realm = new Realm();
		realm.setPeoid("peoid");
		comp.setRealm(realm);
		group = new BenefitGroup();
		group.setBenefitProgram("BENPRG");
		em = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		benProgInactivateDaoImpl = new BenProgInactivateDaoImpl();
		when(em.createNamedQuery( any( String.class ) )).thenReturn(mockedQuery);
	}

	@Test
	public void updateClientOption2() {
		when(mockedQuery.executeUpdate()).thenReturn(1);

		int actualResult = benProgInactivateDaoImpl.updateClientOption2(comp, group.getBenefitProgram(), em);

		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, actualResult);
	}

	@Test
	public void updateBenfitPrclBenPrgm() {
		when(mockedQuery.executeUpdate()).thenReturn(1);

		int actualResult = benProgInactivateDaoImpl.updateBenefitPrclBenPrgm(comp, group.getBenefitProgram(), em);

		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, actualResult);
	}

	@Test
	public void updateBenDefinitionPgm() {
		when(mockedQuery.executeUpdate()).thenReturn(1);

		int actualResult = benProgInactivateDaoImpl.updateBenDefinitionPgm(comp, group.getBenefitProgram(), em);

		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, actualResult);
	}

	@Test
	public void updateBenDefinitionOptionEligRule() {
		when(mockedQuery.executeUpdate()).thenReturn(1);

		int actualResult = benProgInactivateDaoImpl.updateBenDefinitionOptionEligRule(comp, group.getBenefitProgram(), em);

		verify(mockedQuery, times(2)).executeUpdate();
		assertEquals(2, actualResult);
	}

	@Test
	public void deleteClientOpt2a() {
		when(mockedQuery.executeUpdate()).thenReturn(1);

		int actualResult = benProgInactivateDaoImpl.deleteClientOpt2a(comp, group.getBenefitProgram(), em);

		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, actualResult);
	}

}