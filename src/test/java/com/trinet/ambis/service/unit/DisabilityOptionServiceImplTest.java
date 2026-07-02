package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.util.RulesAndConfigsUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.service.impl.DisabilityOptionServiceImpl;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;


@RunWith(MockitoJUnitRunner.class)
public class DisabilityOptionServiceImplTest extends ServiceUnitTest {
	
	private static Long REALM_PLYR_ID = (long) 1111;
	private static String HQ = "MA";
	private static String BEN_EXCHANGE = "SOI";
	private static String OPTION_ID = "1111";
	private static Company COMPANY = prepareCompany(REALM_PLYR_ID, HQ, BEN_EXCHANGE);
	private static Map<String, AdditionalBenefitPlan> DISABILITY_OPTION_MAP = prepareDisabilityOptionsMap();
	private static ArgumentCaptor<Long> REALM_YRID_CAPTOR = ArgumentCaptor.forClass(Long.class);
	private static ArgumentCaptor<String> REGION_CAPTOR = ArgumentCaptor.forClass(String.class);
	private static ArgumentCaptor<String> BEN_EXCHG_CAPTOR = ArgumentCaptor.forClass(String.class);
	private static ArgumentCaptor<Set> SID_STATES = ArgumentCaptor.forClass(Set.class);

	@InjectMocks
	DisabilityOptionServiceImpl disabilityOptionServiceImpl;

	@Mock
	RealmDataDao realmDataDao;
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMock;

    @Before
    public void setUp() {
        rulesAndConfigsUtilsMock = Mockito.mockStatic(RulesAndConfigsUtils.class);
    }

    @After
    public void tearDown() {
        rulesAndConfigsUtilsMock.close();
    }

	@Test
	public void getDisabilityOptionsByRealmPlanYear() {
		when(realmDataDao.getDisabilityOptionPlans(REALM_YRID_CAPTOR.capture(), REGION_CAPTOR.capture(),
				BEN_EXCHG_CAPTOR.capture(), SID_STATES.capture())).thenReturn(DISABILITY_OPTION_MAP);
		
		Set<AdditionalBenefitPlan> benPlans = disabilityOptionServiceImpl.getDisabilityOptionsByRealmPlanYear(COMPANY);
		
		verify(realmDataDao, times(1)).getDisabilityOptionPlans(any(Long.class), any(String.class),
				any(String.class), any(Set.class) );
		assertEquals(REALM_PLYR_ID, REALM_YRID_CAPTOR.getValue());
		assertEquals(HQ, REGION_CAPTOR.getValue());
		assertEquals(REALM_PLYR_ID, REALM_YRID_CAPTOR.getValue());
		assertEquals(2, benPlans.size());
		assertTrue(benPlans.contains(DISABILITY_OPTION_MAP.get("1111")));
		assertTrue(benPlans.contains(DISABILITY_OPTION_MAP.get("2222")));
	}
	
	@Test
	public void getDisabilityOptionByPlans() {
		
		
		AdditionalBenefitPlan actualResult;
		List<String> benefitPlans = Arrays.asList("STD1", "LTD1");
		
		when(realmDataDao.getDisabilityOptionPlans(
				COMPANY.getRealmPlanYearId(), COMPANY.getHeadQuatersState(), COMPANY.getRealm().getBenExchange(), COMPANY.getSdiStates())).thenReturn(DISABILITY_OPTION_MAP);
		
		actualResult = disabilityOptionServiceImpl.getDisabilityOptionByPlans(benefitPlans, COMPANY, false);
		assertEquals(DISABILITY_OPTION_MAP.get("1111"), actualResult);

		actualResult = disabilityOptionServiceImpl.getDisabilityOptionByPlans(benefitPlans, COMPANY, true);
		assertNull(actualResult);

	}

	@Test
	public void getDisabilityPlansByOption() {
		when(realmDataDao.getDisabilityOptionPlans(REALM_YRID_CAPTOR.capture(), REGION_CAPTOR.capture(),
				BEN_EXCHG_CAPTOR.capture(), SID_STATES.capture())).thenReturn(DISABILITY_OPTION_MAP);
		
		List<DisabilityBenefitOptionPlans> optionPlans = disabilityOptionServiceImpl.getDisabilityPlansByOption(OPTION_ID, COMPANY);
		
		verify(realmDataDao, times(1)).getDisabilityOptionPlans(any(Long.class), any(String.class),
				any(String.class), any(Set.class));
		assertEquals(REALM_PLYR_ID, REALM_YRID_CAPTOR.getValue());
		assertEquals(HQ, REGION_CAPTOR.getValue());
		assertEquals(REALM_PLYR_ID, REALM_YRID_CAPTOR.getValue());
		assertEquals(2, optionPlans.size());
		assertEquals("STD1", optionPlans.get(0).getId());
		assertEquals("LTD1", optionPlans.get(1).getId());
	}
	
	@Test
	public void getDisabilitySTDPlanByLTDPlan() {
		String ltdPlan = "STD1";
		when(realmDataDao.getDisabilityOptionPlans(REALM_YRID_CAPTOR.capture(), REGION_CAPTOR.capture(),
				BEN_EXCHG_CAPTOR.capture(), SID_STATES.capture())).thenReturn(DISABILITY_OPTION_MAP);
		
		String selectedSTD = disabilityOptionServiceImpl.getDisabilitySTDPlanByLTDPlan(ltdPlan, REALM_PLYR_ID, HQ, BEN_EXCHANGE);
		
		verify(realmDataDao, times(1)).getDisabilityOptionPlans(any(Long.class), any(String.class),
				any(String.class), any(Set.class));
		assertEquals(REALM_PLYR_ID, REALM_YRID_CAPTOR.getValue());
		assertEquals(HQ, REGION_CAPTOR.getValue());
		assertEquals(REALM_PLYR_ID, REALM_YRID_CAPTOR.getValue());
		assertEquals("STD1", selectedSTD);
		

		selectedSTD = disabilityOptionServiceImpl.getDisabilitySTDPlanByLTDPlan("INVALID", REALM_PLYR_ID, HQ, BEN_EXCHANGE);	
		assertNull(selectedSTD);

		
		selectedSTD = disabilityOptionServiceImpl.getDisabilitySTDPlanByLTDPlan("STD2", REALM_PLYR_ID, HQ, BEN_EXCHANGE);	
		
	}

	private static Map<String, AdditionalBenefitPlan> prepareDisabilityOptionsMap() {
		Map<String, AdditionalBenefitPlan> map = new HashMap<String, AdditionalBenefitPlan>();
		AdditionalBenefitPlan benPlan = new AdditionalBenefitPlan();
		benPlan.setId("1111");
		List<DisabilityBenefitOptionPlans> optionPlans = new ArrayList<DisabilityBenefitOptionPlans>();
		DisabilityBenefitOptionPlans plan = new DisabilityBenefitOptionPlans();
		plan.setPlanType("30");
		plan.setId("STD1");
		optionPlans.add(plan);
		plan = new DisabilityBenefitOptionPlans();
		plan.setPlanType("31");
		plan.setId("LTD1");
		optionPlans.add(plan);
		benPlan.setOptionPlans(optionPlans);
		benPlan.setStandAlone(false);
		map.put("1111", benPlan);
		benPlan = new AdditionalBenefitPlan();
		benPlan.setId("2222");
		benPlan.setOptionPlans(Collections.<DisabilityBenefitOptionPlans>emptyList());
		benPlan.setStandAlone(true);
		map.put("2222", benPlan);
		benPlan.setId("3333");
		optionPlans = new ArrayList<DisabilityBenefitOptionPlans>();
		plan = new DisabilityBenefitOptionPlans();
		plan.setPlanType("30");
		plan.setId("STD2");
		optionPlans.add(plan);
		plan = new DisabilityBenefitOptionPlans();
		plan.setPlanType("31");
		plan.setId("LTD2");
		optionPlans.add(plan);
		benPlan.setOptionPlans(optionPlans);
		benPlan.setStandAlone(true);
		map.put("3333", benPlan);		
		return map;
	}

	private static Company prepareCompany(long realmPlanYearId, String hq, String benExchange) {
		Company company = new Company();
		company.setRealmPlanYearId(realmPlanYearId);
		company.setHeadQuatersState(hq);
		Realm realm = new Realm();
		realm.setBenExchange(benExchange);
		company.setRealm(realm);
		Set<String> sdiStates = new HashSet<>();
		sdiStates.add("MA");
		company.setSdiStates(sdiStates );
		return company;
	}

}