package com.trinet.ambis.persistence.dao.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.ps.EligConfigDao;
import com.trinet.ambis.persistence.dao.ps.impl.BenEligRulesImpl;
import com.trinet.ambis.persistence.dao.ps.impl.EligConfigDaoImpl;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;

@RunWith(MockitoJUnitRunner.class)
public class BenEligRulesImplTest {

	BenEligRulesImpl benEligRulesImpl;
	EntityManager entityManager = null;
	EligConfigDao eligConfigDao = null;
	Query mockedQuery1 = null;
	Query mockedQuery2 = null;
	Query mockedQuery3 = null;
	Query mockedQuery4 = null;
	Query mockedQuery5 = null;
	Query mockedQuery6 = null;
	Company comp = null;
	BenefitGroup group = null;
	String effdtStr = "2018/10/01";
	Date targetDate = null;

	@Before
	public void setup() {
		comp = new Company();
		comp.setCode("G48");
		comp.setName("UnitTestingInc");
		comp.setQuater("SM");
		comp.setPfClient("pfClient");
		comp.setPlanStartDate( "01-AUG-2020" );
		comp.setCompanySetupDate( "11-AUG-2020" );
		Realm realm = new Realm();
		realm.setPeoid("peoid");
		comp.setRealm(realm);
		group = new BenefitGroup();
		benEligRulesImpl = new BenEligRulesImpl();
		entityManager = mock(EntityManager.class);
		benEligRulesImpl.setEntityManager(entityManager);
		eligConfigDao = mock(EligConfigDaoImpl.class);
		benEligRulesImpl.setEligConfigDao(eligConfigDao);
		mockedQuery1 = mock(Query.class);
		mockedQuery2 = mock(Query.class);
		mockedQuery3 = mock(Query.class);
		mockedQuery4 = mock(Query.class);
		mockedQuery5 = mock(Query.class);
		mockedQuery6 = mock(Query.class);
	}
	
	@Test
	public void setupEligConfig_defaultGroupTrue() {
		group.setDefaultGroup(true);
		comp.setName("Test comp");
		group.setName("Test Grp");
		List<BenefitGroup> groups = new ArrayList<BenefitGroup>();
		BenefitGroup grp = new BenefitGroup();
		grp.setDefaultGroup(true);
		groups.add(grp);

		when(entityManager.createNamedQuery("UPDATE_CLIENT_OPT2A")).thenReturn(mockedQuery1);
		when(entityManager.createNamedQuery("GET_DEFAULT_ELIG_RULE")).thenReturn(mockedQuery2);
		when(entityManager.createNamedQuery("CLEAN_ELIG_CNFIG1")).thenReturn(mockedQuery3);
		when(entityManager.createNamedQuery("SET_ELIG_USE_CNFIG1")).thenReturn(mockedQuery4);

		benEligRulesImpl.setupEligConfig(group, groups, comp, targetDate);

		verify(mockedQuery1, times(1)).executeUpdate();
		verify(mockedQuery2, times(1)).getSingleResult();
		verify(mockedQuery3, times(1)).executeUpdate();
		verify(mockedQuery4, times(1)).executeUpdate();
	}


	@Test
	public void setupEligConfig_defaultGroupFalse() {
		group.setDefaultGroup(false);
		comp.setName("Test comp");
		group.setName("Test Grp");
		List<BenefitGroup> groups = new ArrayList<BenefitGroup>();
		groups.add(  this.group  );
		BenefitGroup grp = new BenefitGroup();
		grp.setDefaultGroup(true);
		groups.add(grp);

		when(entityManager.createNamedQuery("UPDATE_CLIENT_OPT2A")).thenReturn(mockedQuery1);
		when(entityManager.createNamedQuery("GET_DEFAULT_ELIG_RULE")).thenReturn(mockedQuery2);
		when(entityManager.createNamedQuery("SET_ELIG_USE_CNFIG1")).thenReturn(mockedQuery4);
		when(entityManager.createNamedQuery("INSERT_ELIG_CNFIG1")).thenReturn(mockedQuery5);
		when(entityManager.createNamedQuery("SET_ELIG_FLG_CNFIG1")).thenReturn(mockedQuery6);

		benEligRulesImpl.setupEligConfig(group, groups, comp, targetDate);

		verify(mockedQuery1, times(1)).executeUpdate();
		verify(mockedQuery2, times(1)).getSingleResult();
		verify(mockedQuery3, times(0)).executeUpdate();
		verify(mockedQuery4, times(1)).executeUpdate();
		verify(mockedQuery6, times(2)).executeUpdate();
	}

	@Test
	public void setupEligConfig_defaultGroupFalse_EligFlagIsI() {
		group.setDefaultGroup(false);
		comp.setName("Test comp");
		group.setName("Test Grp");
		List<BenefitGroup> groups = new ArrayList<BenefitGroup>();
		BenefitGroup grp = new BenefitGroup();
		grp.setDefaultGroup(true);
		groups.add(grp);

		when(entityManager.createNamedQuery("UPDATE_CLIENT_OPT2A")).thenReturn(mockedQuery1);
		when(entityManager.createNamedQuery("GET_DEFAULT_ELIG_RULE")).thenReturn(mockedQuery2);
		when(entityManager.createNamedQuery("SET_ELIG_USE_CNFIG1")).thenReturn(mockedQuery4);
		when(entityManager.createNamedQuery("INSERT_ELIG_CNFIG1")).thenReturn(mockedQuery5);
		when(entityManager.createNamedQuery("SET_ELIG_FLG_CNFIG1")).thenReturn(mockedQuery6);

		when(mockedQuery2.getSingleResult()).thenReturn("I");
		benEligRulesImpl.setupEligConfig(group, groups, comp, targetDate);

		verify(mockedQuery1, times(1)).executeUpdate();
		verify(mockedQuery2, times(1)).getSingleResult();
		verify(mockedQuery3, times(0)).executeUpdate();
		verify(mockedQuery4, times(1)).executeUpdate();
		verify(mockedQuery6, times(1)).executeUpdate();
	}


	@Test
	public void setupEligConfig_emptyGroups() {
		group.setDefaultGroup(true);
		comp.setName("Test comp");
		group.setName("Test Grp");
		List<BenefitGroup> groups = new ArrayList<BenefitGroup>();

		when(entityManager.createNamedQuery("UPDATE_CLIENT_OPT2A")).thenReturn(mockedQuery1);
		benEligRulesImpl.setupEligConfig(group, groups, comp, targetDate);

		verify(mockedQuery1, times(1)).executeUpdate();
		verify(mockedQuery2, times(0)).getSingleResult();
		verify(mockedQuery3, times(0)).executeUpdate();
		verify(mockedQuery4, times(0)).executeUpdate();
	}


	@Test
	public void setupEligConfigRenewalTest() {
		List<BenefitGroup> groups = new ArrayList<BenefitGroup>();
		group.setDefaultGroup(true);
		group.setName("Default Grp");
		group.setEligRuleId( "RUSH" );
		group.setEligConfig1( "AG48" );
		groups.add( group );
		
		group = new BenefitGroup();
		group.setDefaultGroup( false );
		group.setName( "Alternate" );
		group.setEligRuleId( "LARC" );
		group.setEligConfig1( "BG48" );
		groups.add( group );
		
		comp.setRenewalCompany( true );

		when(entityManager.createNamedQuery("UPDATE_CLIENT_OPT2A")).thenReturn(mockedQuery1);
		when(entityManager.createNamedQuery("GET_DEFAULT_ELIG_RULE_EFFDT")).thenReturn(mockedQuery2);
		when(mockedQuery2.getSingleResult()).thenReturn( " " );
		when(entityManager.createNamedQuery("CLEAN_ELIG_CNFIG1")).thenReturn(mockedQuery3);
		when(entityManager.createNamedQuery("SET_ELIG_USE_CNFIG1")).thenReturn(mockedQuery4);
		when(entityManager.createNamedQuery("SET_ELIG_FLG_CNFIG1")).thenReturn(mockedQuery4);


		when(entityManager.createNamedQuery("CLEAN_ELIG_PYGRP")).thenReturn(mockedQuery5);
		when(entityManager.createNamedQuery("CLEAN_ELIG_BNSTAT")).thenReturn(mockedQuery5);
		when(entityManager.createNamedQuery("CLEAN_ELIG_EECLAS")).thenReturn(mockedQuery5);
		when(entityManager.createNamedQuery("CLEAN_ELIG_CNFIG1")).thenReturn(mockedQuery5);
		when(entityManager.createNamedQuery("CLEAN_ELIG_RULES")).thenReturn(mockedQuery5);
		when(entityManager.createNamedQuery("GET_CLONE_ELIG_RULE")).thenReturn(mockedQuery6);
		when(mockedQuery6.getSingleResult()).thenReturn( "WXYZ" );
		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_RULES")).thenReturn(mockedQuery6);
		when(entityManager.createNamedQuery("INSERT_ELIG_CNFIG1")).thenReturn(mockedQuery6);
		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_PYGRP")).thenReturn(mockedQuery6);
		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_BNSTAT")).thenReturn(mockedQuery6);
		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_EECLAS")).thenReturn(mockedQuery6);

		Query mockedUpdate = mock(Query.class);
		when(entityManager.createNamedQuery("UPDATE_PGM_ELIG_RULE")).thenReturn(mockedUpdate);

		when(eligConfigDao.putEligConfigRow(any(String.class), any(String.class), any(String.class), any(String.class),
				any(String.class), any(EntityManager.class))).thenReturn(1);
		
		benEligRulesImpl.setupEligConfig(group, groups, comp, targetDate);

		verify(mockedQuery1, times(1)).executeUpdate();
		verify(mockedQuery2, times(1)).getSingleResult();
		verify(mockedQuery3, times(0)).executeUpdate();
		verify(mockedQuery4, times(3)).executeUpdate();
		verify(mockedQuery5, times(5)).executeUpdate();
		verify(mockedQuery6, times(6)).executeUpdate();
		verify(mockedUpdate, times(1)).executeUpdate();
		}


	@Test
	public void createBasEligRules() {

		when(entityManager.createNamedQuery("CLEAN_ELIG_PYGRP")).thenReturn(mockedQuery1);
		when(entityManager.createNamedQuery("CLEAN_ELIG_BNSTAT")).thenReturn(mockedQuery1);
		when(entityManager.createNamedQuery("CLEAN_ELIG_EECLAS")).thenReturn(mockedQuery1);
		when(entityManager.createNamedQuery("CLEAN_ELIG_CNFIG1")).thenReturn(mockedQuery1);
		when(entityManager.createNamedQuery("CLEAN_ELIG_RULES")).thenReturn(mockedQuery1);

		when(entityManager.createNamedQuery("GET_CLONE_ELIG_RULE")).thenReturn(mockedQuery2);
		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_RULES")).thenReturn(mockedQuery2);
		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_PYGRP")).thenReturn(mockedQuery2);
		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_BNSTAT")).thenReturn(mockedQuery2);
		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_EECLAS")).thenReturn(mockedQuery2);
		when(entityManager.createNamedQuery("UPDATE_RULES_EECLAS")).thenReturn(mockedQuery2);
		when(mockedQuery2.executeUpdate()).thenReturn(1);

		when(entityManager.createNamedQuery("UPDATE_PGM_ELIG_RULE")).thenReturn(mockedQuery3);

		benEligRulesImpl.createBasEligRules(group, comp);

		verify(mockedQuery1, times(5)).executeUpdate();
		verify(mockedQuery2, times(1)).getSingleResult();
		verify(mockedQuery2, times(5)).executeUpdate();
		verify(mockedQuery3, times(2)).executeUpdate();
	}


	@Test
	public void createBasEligRulesK1() {

		group.setType( "K1" );
		when(entityManager.createNamedQuery("CLEAN_ELIG_PYGRP")).thenReturn(mockedQuery1);
		when(entityManager.createNamedQuery("CLEAN_ELIG_BNSTAT")).thenReturn(mockedQuery1);
		when(entityManager.createNamedQuery("CLEAN_ELIG_EECLAS")).thenReturn(mockedQuery1);
		when(entityManager.createNamedQuery("CLEAN_ELIG_CNFIG1")).thenReturn(mockedQuery1);
		when(entityManager.createNamedQuery("CLEAN_ELIG_RULES")).thenReturn(mockedQuery1);

		when(entityManager.createNamedQuery("GET_CLONE_ELIG_RULE")).thenReturn(mockedQuery2);
		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_RULES")).thenReturn(mockedQuery2);
		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_PYGRP")).thenReturn(mockedQuery2);
		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_BNSTAT")).thenReturn(mockedQuery2);
		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_EECLAS")).thenReturn(mockedQuery2);
		when(entityManager.createNamedQuery("UPDATE_RULES_EECLAS")).thenReturn(mockedQuery2);
		when(mockedQuery2.executeUpdate()).thenReturn(1);

		when(entityManager.createNamedQuery("UPDATE_PGM_ELIG_RULE")).thenReturn(mockedQuery3);

		benEligRulesImpl.createBasEligRules(group, comp);

		verify(mockedQuery1, times(5)).executeUpdate();
		verify(mockedQuery2, times(1)).getSingleResult();
		verify(mockedQuery2, times(5)).executeUpdate();
		verify(mockedQuery3, times(2)).executeUpdate();
	}


	/**
	 * Test elig rule setup when group ELIG_RULES_ID is set to 2009
	 * No changes of PS_BAS_ELIG% tables should be made.
	 */
	@Test
	public void createBasEligRules2009() {

		// Delete SQL belongs to BenEligRulesDaoImpl
//		when(entityManager.createNamedQuery("CLEAN_ELIG_PYGRP")).thenReturn(mockedQuery1);
//		when(entityManager.createNamedQuery("CLEAN_ELIG_BNSTAT")).thenReturn(mockedQuery1);
//		when(entityManager.createNamedQuery("CLEAN_ELIG_EECLAS")).thenReturn(mockedQuery1);
//		when(entityManager.createNamedQuery("CLEAN_ELIG_CNFIG1")).thenReturn(mockedQuery1);
//		when(entityManager.createNamedQuery("CLEAN_ELIG_RULES")).thenReturn(mockedQuery1);

		// Query belongs to BenEligRulesImpl
		when(entityManager.createNamedQuery("GET_CLONE_ELIG_RULE")).thenReturn(mockedQuery2);
		
		// Insert/Update SQL belongs to BenEligRulesDaoImpl
//		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_RULES")).thenReturn(mockedQuery3);
//		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_PYGRP")).thenReturn(mockedQuery3);
//		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_BNSTAT")).thenReturn(mockedQuery3);
//		when(entityManager.createNamedQuery("INSERT_BAS_ELIG_EECLAS")).thenReturn(mockedQuery3);
//		when(entityManager.createNamedQuery("UPDATE_RULES_EECLAS")).thenReturn(mockedQuery3);

		// Update SQL belongs to BenEligRulesImpl
		when(entityManager.createNamedQuery("UPDATE_PGM_ELIG_RULE")).thenReturn(mockedQuery4);

		group.setEligRuleId( "2009" );
		benEligRulesImpl.createBasEligRules(group, comp);

		verify(mockedQuery1, times(0)).executeUpdate();
		verify(mockedQuery2, times(1)).getSingleResult();
		verify(mockedQuery2, times(0)).executeUpdate();
		verify(mockedQuery3, times(0)).executeUpdate();
		verify(mockedQuery4, times(2)).executeUpdate();
	}

}