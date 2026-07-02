package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.impl.PlanRatesDataDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.AppRulesConfigService;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.Constants;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class PlanRatesDataDaoImplTest {

	@InjectMocks
	PlanRatesDataDaoImpl planRatesDataDao;

	@Mock
	StrategyDataDao strategyDataDao;

	@Mock
	EntityManager hrdbEm;

	@Mock
	EntityManager hpdbEm;

	@Mock
	AppRulesConfigService appRulesConfigService;

	private Company company = new Company();

	private Query mockedQuery = null;
	private Query mockedQuery1 = null;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockedQuery = mock(Query.class);
		mockedQuery1 = mock(Query.class);
		planRatesDataDao.setHpdbEm(hpdbEm);
		planRatesDataDao.setHrdbEm(hrdbEm);
		when(hrdbEm.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		when(hpdbEm.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		company.setId(1L);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setPlanYearEnd(new Date());
		company.setCode("XYZ");
		company.setRealmPlanYear(realmPlanYear);
		AppRulesAndConfigsUtils.setAppRuleConfigService(appRulesConfigService);
	}

	@Test
	public void getBenefitPlans() {

		Set<String> emptySet = new HashSet<>();
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(new HashMap<>());
		when(mockedQuery.getResultList()).thenReturn(prepareBenefitPlanMockData());
		Map<String, StateBenefitPlan> actualResult = planRatesDataDao.getBenefitPlans(1, emptySet, company,
				emptySet, emptySet);
		assertEquals(1, actualResult.size());
		verify(hpdbEm, times(1)).createNamedQuery("BENEFIT_PLANS_FOR_PLAN_RATES");
	}

	@Test
	public void getBenefitPlansTX() {

		company.setTexasSitus(true);
		Set<String> mockSet = new HashSet<>();
		mockSet.add("MOCK");
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(new HashMap<>());
		when(mockedQuery.getResultList()).thenReturn(prepareBenefitPlanMockData());
		Map<String, StateBenefitPlan> actualResult = planRatesDataDao.getBenefitPlans(1, mockSet, company,
				mockSet, mockSet);
		assertEquals(1, actualResult.size());
		verify(hpdbEm, times(1)).createNamedQuery("BENEFIT_PLANS_FOR_PLAN_RATES");
	}

	@Test
	public void getBenefitPlansV2WhenBenefitBundlesV2Enabled() {

		Set<String> emptySet = new HashSet<>();
		Map<String, String> rules = new HashMap<>();
		rules.put("BUNDLE_V2_ENABLED", "true");
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(rules);
		when(mockedQuery.getResultList()).thenReturn(prepareBenefitPlanMockData());
		Map<String, StateBenefitPlan> actualResult = planRatesDataDao.getBenefitPlans(1, emptySet, company,
				emptySet, emptySet);
		assertEquals(1, actualResult.size());
		verify(hpdbEm, times(1)).createNamedQuery("BENEFIT_PLANS_FOR_PLAN_RATES_V2");
	}

	@Test
	public void getBenefitPlansV1WhenBenefitBundlesV2Disabled() {

		Set<String> emptySet = new HashSet<>();
		Map<String, String> rules = new HashMap<>();
		rules.put("BUNDLE_V2_ENABLED", "false");
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(rules);
		when(mockedQuery.getResultList()).thenReturn(prepareBenefitPlanMockData());
		Map<String, StateBenefitPlan> actualResult = planRatesDataDao.getBenefitPlans(1, emptySet, company,
				emptySet, emptySet);
		assertEquals(1, actualResult.size());
		verify(hpdbEm, times(1)).createNamedQuery("BENEFIT_PLANS_FOR_PLAN_RATES");
	}

	@Test
	public void getBenefitPlanStates() {

		when(mockedQuery.getResultList()).thenReturn(prepareBenefitPlanStatesMockData());
		Map<String, Set<String>> actualResult = planRatesDataDao.getBenefitPlanStates(2, 1);
		assertEquals(1, actualResult.size());
	}

	@Test
	public void getBenefitPlanRatesBy() {
		Set<String> benefitPlanList = new HashSet<String>();
		benefitPlanList.add("00039D");
		Company comp = new Company();
		Realm realm = new Realm();
		realm.setPeoid(Constants.PASSPORT_PEO_ID);
		comp.setRealm(realm);
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setPlanYearEnd(new Date(9999999L));
		comp.setRealmPlanYear(rpy);
		BandCodes bc = new BandCodes();
		comp.setBandCodes(bc);
		comp.setQuater("Q1");

		when(hrdbEm.createNamedQuery("REALM_ALL_RATES")).thenReturn(mockedQuery1);

		when(mockedQuery1.getResultList()).thenReturn(prepareRealmAllRatesMockData());

		Map<String, List<BenefitPlanRate>> actualResult = planRatesDataDao.getBenefitPlanRatesBy(comp);

		assertEquals(1, actualResult.size());
		assertEquals(2, actualResult.get("00039D").size());
		verify(mockedQuery1, times(1)).getResultList();
	}

	private List<Object[]> prepareBenefitPlanMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[10];
		r[0] = "MEDPLAN";
		r[1] = "MEDICAL PLAN DESCRIPTION";
		r[2] = "10";
		r[3] = "VENDOR";
		r[4] = BigDecimal.valueOf(5);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareBenefitPlanStatesMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[10];
		r[0] = "MEDPLAN";
		r[1] = "CA";
		results.add(r);

		r = new Object[10];
		r[0] = "MEDPLAN";
		r[1] = "SC";
		results.add(r);

		return results;
	}

	private List<Object[]> prepareRealmAllRatesMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[6];
		r[0] = "A";
		r[1] = "00039D";
		r[2] = "1";
		r[3] = "10";
		r[4] = BigDecimal.valueOf(278.76);
		r[5] = new Date(9999999L);
		results.add(r);
		r = new Object[6];
		r[0] = "B";
		r[1] = "00039D";
		r[2] = "2";
		r[3] = "11";
		r[4] = BigDecimal.valueOf(234.76);
		r[5] = new Date(9999999L);
		results.add(r);
		return results;
	}

}