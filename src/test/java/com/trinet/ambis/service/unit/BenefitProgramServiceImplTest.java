package com.trinet.ambis.service.unit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.ps.BenProgInactivateDao;
import com.trinet.ambis.persistence.dao.ps.BenefitProgramDao;
import com.trinet.ambis.persistence.dao.ps.EligConfigDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmCloneProgram;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.impl.BenefitProgramServiceImpl;
import com.trinet.ambis.util.ApplicationContextProvider;


@RunWith(MockitoJUnitRunner.class)
public class BenefitProgramServiceImplTest extends ServiceUnitTest {

	@Mock
	ApplicationContext context;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	BenefitProgramDao benefitProgramDao;

	@Mock
	BenProgInactivateDao benProgInactivateDao;

	@Mock
	EligConfigDao eligConfigDao;

	@Mock
	EntityManager em;

    private MockedStatic<ApplicationContextProvider> mockStaticApplicationContextProvider;
	@Before
	public void setUp() {
        if (mockStaticApplicationContextProvider == null) {
            mockStaticApplicationContextProvider = Mockito.mockStatic(ApplicationContextProvider.class);
            mockStaticApplicationContextProvider.when(ApplicationContextProvider::getApplicationContext).thenReturn(context);
        }
		when(context.getBean("realmDataDao")).thenReturn(realmDataDao);
	}

    @After
    public void tearDown() {
        if (mockStaticApplicationContextProvider != null) {
            mockStaticApplicationContextProvider.close();
        }
    }

	@Test
	public void createBenefitProgram() {
		String cloneK1Prog = "K1ClPrg";
		RealmCloneProgram realmClonePgm = new RealmCloneProgram();
		realmClonePgm.setCloneK1Program(cloneK1Prog);
		Company company = prepareCompany();
		BenefitGroup group = new BenefitGroup();
		group.setType("K1");
		group.setBenefitProgram("BenProg1");
		group.setDefaultGroup(true);

		when(realmDataDao.getRealmCloneProgram(company.getRealmPlanYear().getId())).thenReturn(realmClonePgm);
		Query benDefProgQuery = mock(Query.class);
		Query cleanPlanQuery = mock(Query.class);
		Query clonePlanQuery = mock(Query.class);
		Query benDefnPlanQuery = mock(Query.class);
		Query cleanOptQuery = mock(Query.class);
		Query benDefOptQuery = mock(Query.class);
		Query cleanCostQuery = mock(Query.class);
		Query benDefnCostQuery = mock(Query.class);

		when(em.createNamedQuery("INSERT_BEN_DEFN_PGM")).thenReturn(benDefProgQuery);
		when(em.createNamedQuery("CLEAN_PLAN_ALL")).thenReturn(cleanPlanQuery);
		when(em.createNamedQuery("GET_CLONE_PLAN_TYPES")).thenReturn(clonePlanQuery);
		when(em.createNamedQuery("INSERT_BEN_DEFN_PLAN")).thenReturn(benDefnPlanQuery);
		when(em.createNamedQuery("CLEAN_OPTN_ALL")).thenReturn(cleanOptQuery);
		when(em.createNamedQuery("INSERT_BEN_DEFN_OPTN")).thenReturn(benDefOptQuery);
		when(em.createNamedQuery("CLEAN_COST_ALL")).thenReturn(cleanCostQuery);
		when(em.createNamedQuery("INSERT_BEN_DEFN_COST")).thenReturn(benDefnCostQuery);

		BenefitProgramServiceImpl service = new BenefitProgramServiceImpl(em);

		service.createBenefitProgram(company, group);

		InOrder inOrder = Mockito.inOrder(benDefProgQuery, cleanPlanQuery, clonePlanQuery, benDefnPlanQuery,
				cleanOptQuery, benDefOptQuery, cleanCostQuery, benDefnCostQuery);

		inOrder.verify(benDefProgQuery, times(1)).executeUpdate();
		inOrder.verify(cleanPlanQuery, times(1)).executeUpdate();
		inOrder.verify(benDefnPlanQuery, times(1)).executeUpdate();
		inOrder.verify(cleanOptQuery, times(1)).executeUpdate();
		inOrder.verify(benDefOptQuery, times(1)).executeUpdate();
		inOrder.verify(cleanCostQuery, times(1)).executeUpdate();
		inOrder.verify(benDefnCostQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteFutureProgram() {
		Company company = prepareCompany();
		BenefitGroup group = new BenefitGroup();

		Query delFutProgQuery = mock(Query.class);
		Query delFutPlanQuery = mock(Query.class);
		Query delFutOptQuery = mock(Query.class);
		Query delFutCostQuery = mock(Query.class);

		when(em.createNamedQuery("DELETE_FUTURE_PGM")).thenReturn(delFutProgQuery);
		when(em.createNamedQuery("DELETE_FUTURE_PLAN")).thenReturn(delFutPlanQuery);
		when(em.createNamedQuery("DELETE_FUTURE_OPTN")).thenReturn(delFutOptQuery);
		when(em.createNamedQuery("DELETE_FUTURE_COST")).thenReturn(delFutCostQuery);

		BenefitProgramServiceImpl service = new BenefitProgramServiceImpl(em);

		service.deleteFutureProgram(company, group);

		InOrder inOrder = Mockito.inOrder(delFutProgQuery, delFutPlanQuery, delFutOptQuery, delFutCostQuery);

		inOrder.verify(delFutProgQuery, times(1)).executeUpdate();
		inOrder.verify(delFutPlanQuery, times(1)).executeUpdate();
		inOrder.verify(delFutOptQuery, times(1)).executeUpdate();
		inOrder.verify(delFutCostQuery, times(1)).executeUpdate();
	}

	@Test
	public void updateInactiveBenefitPrograms() {
		Company company = prepareCompany();
		String benefitProgram = "BenProg";
		String eligConfig1 = "eligCongig";

		Query clientOpt2Query = mock(Query.class);
		Query prclBenPgmQuery = mock(Query.class);
		Query benDefnPlan = mock(Query.class);
		Query benDefnOpt = mock(Query.class);
		Query benDefnProg = mock(Query.class);
		Query deleteOpt2A = mock(Query.class);
		Query eligConfigComp = mock(Query.class);
		Query eligConfigEfdt = mock(Query.class);
		Query eligConfigTbl = mock(Query.class);

		when(em.createNamedQuery("UPDATE_BENEFITGROUP_CLIENT_OPTION2")).thenReturn(clientOpt2Query);
		when(em.createNamedQuery("INACTIVATE_PRCL_BN_PGM")).thenReturn(prclBenPgmQuery);
		when(em.createNamedQuery("UPDATE_BENEFITGROUP_BEN_DEFN_PLAN")).thenReturn(benDefnPlan);
		when(em.createNamedQuery("UPDATE_BENEFITGROUP_BEN_DEFN_OPTN_ELIG_RULE")).thenReturn(benDefnOpt);
		when(em.createNamedQuery("UPDATE_BENEFITGROUP_BEN_DEFN_PROG")).thenReturn(benDefnProg);
		when(em.createNamedQuery("DELETE_EXACT_OPT2A")).thenReturn(deleteOpt2A);
		when(em.createNamedQuery("GET_ELIGCNFG_COMPONENT")).thenReturn(eligConfigComp);
		when(em.createNamedQuery("INSERT_ELIGCFG_EFDT")).thenReturn(eligConfigEfdt);
		when(em.createNamedQuery("INSERT_ELIGCNFG_TBL")).thenReturn(eligConfigTbl);
		
		BenefitProgramServiceImpl service = new BenefitProgramServiceImpl(em);
		service.updateInactiveBenefitPrograms(company, benefitProgram, eligConfig1, em);

		InOrder inOrder = Mockito.inOrder(clientOpt2Query, prclBenPgmQuery, benDefnPlan, benDefnOpt, benDefnProg, deleteOpt2A, eligConfigComp,
				eligConfigEfdt, eligConfigTbl);

		inOrder.verify(clientOpt2Query, times(1)).executeUpdate();
		inOrder.verify(prclBenPgmQuery, times(1)).executeUpdate();
		inOrder.verify(benDefnPlan, times(1)).executeUpdate();
		inOrder.verify(benDefnOpt, times(1)).executeUpdate();
		inOrder.verify(benDefnProg, times(1)).executeUpdate();
		inOrder.verify(deleteOpt2A, times(1)).executeUpdate();
		inOrder.verify(eligConfigEfdt, times(1)).executeUpdate();
		inOrder.verify(eligConfigTbl, times(1)).executeUpdate();
	}

	private Company prepareCompany() {
		Company comp = new Company();
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		comp.setCode("G48");
		realmPlanYear.setId(31);
		comp.setRealmPlanYear(realmPlanYear);
		comp.setPfClient("pfClient");
		comp.setPlanStartDate("01-JAN-2020");
		Realm realm = new Realm();
		realm.setId(4L);
		comp.setRealm(realm );
		return comp;
	}

}