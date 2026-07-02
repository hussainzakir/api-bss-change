package com.trinet.ambis.aop;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.RealmConfigurationDao;
import com.trinet.ambis.persistence.dao.hrp.impl.BenefitPlanDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmConfiguration;
import com.trinet.ambis.persistence.model.RealmConfigurationId;
import com.trinet.ambis.service.AppRulesConfigService;
import com.trinet.ambis.service.CacheTemplateService;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class BSSCacheableAspectTest extends ServiceUnitTest {

	@Autowired
	@InjectMocks
	private BSSCacheableAspect cacheableAspect;

	@Mock
	private CacheTemplateService cacheTemplateService;

	@Mock
	RealmConfigurationDao realmConfigurationDao;
	
	@Mock
	AppRulesConfigService appRulesConfigService;

	BenefitPlanDao benefitPlanDao;

	ArgumentCaptor<String> argCaptor;
	ArgumentCaptor<Type> argTypeCaptor;
	ProceedingJoinPoint joinPoint;
	MethodSignature signature;

	@Before
	public void setUp() throws Throwable {
		//MockitoAnnotations.initMocks(this);
		argCaptor = ArgumentCaptor.forClass(String.class);
		argTypeCaptor = ArgumentCaptor.forClass(Type.class);
		joinPoint = mock(ProceedingJoinPoint.class);
		signature = mock(MethodSignature.class);
		
		when(joinPoint.getSignature()).thenReturn(signature);
		when(signature.getMethod()).thenReturn(BenefitPlanDaoImpl.class.getMethod("getAllPrimaryBenefitPlans",
				Set.class, Company.class, Set.class));
		
		AppRulesAndConfigsUtils.setAppRuleConfigService(appRulesConfigService);
	}

	@Test
	public void resultShouldBeReturnedFromCache() {
		Set<String> portfolios = new HashSet<>();
		Company company = new Company();
		company.setId(1111);
		Realm realm = new Realm();
		realm.setId(27);
		company.setRealm(realm);
		Set<String> outOfRegionPlans = new HashSet<>();
		Object[] args = { portfolios, company, outOfRegionPlans };
		Map<String, Set<StateBenefitPlan>> expectedResult = prepareExpectedResult();
		
		prepareAppConfigRule("false");

		when(joinPoint.getArgs()).thenReturn(args);
		when(cacheTemplateService.retrieveFromCache(argCaptor.capture(), argTypeCaptor.capture()))
				.thenReturn(expectedResult);

		@SuppressWarnings("unchecked")
		Map<String, Set<StateBenefitPlan>> actualResult = (Map<String, Set<StateBenefitPlan>>) cacheableAspect
				.around(joinPoint);

		assertEquals("BSS:BENEFIT-PLANS:1111", argCaptor.getValue());
		assertEquals(1, actualResult.size());
	}

	@Test
	public void resultShouldBeReturnedFromMethodExecution() throws Throwable {
		Set<String> portfolios = new HashSet<>();
		Company company = new Company();
		company.setId(1111);
		Realm realm = new Realm();
		realm.setId(27);
		company.setRealm(realm);
		Set<String> outOfRegionPlans = new HashSet<>();
		Object[] args = { portfolios, company, outOfRegionPlans };
		Map<String, Set<StateBenefitPlan>> expectedResult = prepareExpectedResult();

		prepareAppConfigRule("false");
		
		when(joinPoint.getArgs()).thenReturn(args);
		when(cacheTemplateService.retrieveFromCache(argCaptor.capture(), argTypeCaptor.capture())).thenReturn(null);
		when(joinPoint.proceed()).thenReturn(expectedResult);
		when(realmConfigurationDao.findByIdRealmId(27)).thenReturn(prepareRealmConfigurations());

		@SuppressWarnings("unchecked")
		Map<String, Set<StateBenefitPlan>> actualResult = (Map<String, Set<StateBenefitPlan>>) cacheableAspect
				.around(joinPoint);

		assertEquals("BSS:BENEFIT-PLANS:1111", argCaptor.getValue());
		assertEquals(1, actualResult.size());
	}
	
	@Test
	public void resultShouldBeReturnedFromMethodExecutionWhenCacheDisabled() throws Throwable {
		Set<String> portfolios = new HashSet<>();
		Company company = new Company();
		company.setId(1111);
		Realm realm = new Realm();
		realm.setId(27);
		company.setRealm(realm);
		Set<String> outOfRegionPlans = new HashSet<>();
		Object[] args = { portfolios, company, outOfRegionPlans };
		Map<String, Set<StateBenefitPlan>> expectedResult = prepareExpectedResult();

		prepareAppConfigRule("true");
		
		when(joinPoint.getArgs()).thenReturn(args);
		when(joinPoint.proceed()).thenReturn(expectedResult);

		@SuppressWarnings("unchecked")
		Map<String, Set<StateBenefitPlan>> actualResult = (Map<String, Set<StateBenefitPlan>>) cacheableAspect
				.around(joinPoint);
		assertEquals(1, actualResult.size());
	}

	@Test(expected = BSSApplicationException.class)
	public void joinpointProceedThrowsBSSException() throws Throwable {
		Set<String> portfolios = new HashSet<>();
		Company company = new Company();
		company.setId(1111);
		Realm realm = new Realm();
		realm.setId(27);
		company.setRealm(realm);
		Set<String> outOfRegionPlans = new HashSet<>();
		Object[] args = { portfolios, company, outOfRegionPlans };

		when(joinPoint.getArgs()).thenReturn(args);
		when(cacheTemplateService.retrieveFromCache(anyString(), any(Type.class))).thenReturn(null);
		when(joinPoint.proceed()).thenThrow(new BSSApplicationException());

		cacheableAspect.around(joinPoint);
	}

	@Test(expected = BSSApplicationException.class)
	public void joinpointProceedThrowsRuntimeException() throws Throwable {
		Set<String> portfolios = new HashSet<>();
		Company company = new Company();
		company.setId(1111);
		Realm realm = new Realm();
		realm.setId(27);
		company.setRealm(realm);
		Set<String> outOfRegionPlans = new HashSet<>();
		Object[] args = { portfolios, company, outOfRegionPlans };

		when(joinPoint.getArgs()).thenReturn(args);
		when(cacheTemplateService.retrieveFromCache(anyString(), any(Type.class))).thenReturn(null);
		when(joinPoint.proceed()).thenThrow(new RuntimeException());

		cacheableAspect.around(joinPoint);
	}

	private Map<String, Set<StateBenefitPlan>> prepareExpectedResult() {
		Map<String, Set<StateBenefitPlan>> result = new HashMap<String, Set<StateBenefitPlan>>();
		Set<StateBenefitPlan> plans = new HashSet<>();
		result.put("MEDICAL", plans);
		return result;
	}

	private List<RealmConfiguration> prepareRealmConfigurations() {
		List<RealmConfiguration> data = new ArrayList<RealmConfiguration>();
		RealmConfiguration rc = new RealmConfiguration();
		RealmConfigurationId id = new RealmConfigurationId();
		id.setConfigKey("SOME_KEY");
		rc.setId(id);
		rc.setConfigValue("VALUE");
		data.add(rc);
		rc = new RealmConfiguration();
		id = new RealmConfigurationId();
		id.setConfigKey("CACHE_TTL");
		rc.setId(id);
		rc.setConfigValue("500");
		data.add(rc);

		return data;
	}

	private void prepareAppConfigRule(String val) {
		Map<String, String> appConfigRules = new HashMap<>();
		appConfigRules.put("DISABLE_CACHE", val);
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(appConfigRules);
	}

}
