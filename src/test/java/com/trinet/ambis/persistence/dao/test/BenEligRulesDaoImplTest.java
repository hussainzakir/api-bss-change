package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.ps.BenEligRulesDao;
import com.trinet.ambis.persistence.dao.ps.impl.BenEligRulesDaoImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class BenEligRulesDaoImplTest {

	BenEligRulesDao benEligRulesDao = null;
	EntityManager entityManager = null;
	Query mockedQuery1 = null;
	String effdtStr = "01-JAN-2020";

	@Before
	public void setup() {
		entityManager = mock(EntityManager.class);
		benEligRulesDao = new BenEligRulesDaoImpl();
		mockedQuery1 = mock(Query.class);
	}



	@Test
	public void getEligFlgCnfig1ExactTest() {
		when(entityManager.createNamedQuery("GET_DEFAULT_ELIG_RULE_EFFDT")).thenReturn(mockedQuery1);
		when(mockedQuery1.getSingleResult()).thenReturn( "X" );
		String elgFlag = benEligRulesDao.getEligFlgCnfig1Exact( "ABCD", effdtStr, this.entityManager );
		assertEquals( "X", elgFlag );

		when(mockedQuery1.getSingleResult()).thenThrow( new NoResultException() );
		elgFlag = benEligRulesDao.getEligFlgCnfig1Exact( "ABCD", effdtStr, this.entityManager );
		assertEquals( " ", elgFlag );
	}


	@Test
	public void getEligFlgCnfig1AsOfTest() {
		when(entityManager.createNamedQuery("GET_DEFAULT_ELIG_RULE")).thenReturn(mockedQuery1);
		when(mockedQuery1.getSingleResult()).thenReturn( "Z" );
		String elgFlag = benEligRulesDao.getEligFlgCnfig1AsOf( "ABCD", effdtStr, this.entityManager );
		assertEquals( "Z", elgFlag );

		when(mockedQuery1.getSingleResult()).thenThrow( new NoResultException() );
		elgFlag = benEligRulesDao.getEligFlgCnfig1AsOf( "ABCD", effdtStr, this.entityManager );
		assertEquals( " ", elgFlag );
	}


	@Test
	public void setEligUseCnfig12009Test() {
		when(entityManager.createNamedQuery("SET_ELIG_USE_CNFIG1")).thenReturn(mockedQuery1);
		benEligRulesDao.setEligUseCnfig1( "2009", effdtStr, "Y", this.entityManager );
		verify( mockedQuery1, times(0)).executeUpdate();
	}


	@Test
	public void insertBasEligCnfig12009Test() {
		when(entityManager.createNamedQuery("INSERT_ELIG_CNFIG1")).thenReturn(mockedQuery1);
		benEligRulesDao.insertBasEligCnfig1( "2009", effdtStr, "Y", this.entityManager );
		verify( mockedQuery1, times(0)).executeUpdate();
	}


	@Test
	public void setEligFlgCnfig12009Test() {
		when(entityManager.createNamedQuery("SET_ELIG_FLG_CNFIG1")).thenReturn(mockedQuery1);
		benEligRulesDao.setEligFlgCnfig1( "2009", effdtStr, "Y", this.entityManager );
		verify( mockedQuery1, times(0)).executeUpdate();
	}


	@Test
	public void setEligFlgEeclas2009Test() {
		when(entityManager.createNamedQuery("UPDATE_RULES_EECLAS")).thenReturn(mockedQuery1);
		benEligRulesDao.setEligFlgEeclas( "2009", effdtStr, "Y", this.entityManager );
		verify( mockedQuery1, times(0)).executeUpdate();
	}

}