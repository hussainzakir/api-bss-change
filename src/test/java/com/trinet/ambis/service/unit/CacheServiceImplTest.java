package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.service.StrategyService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.helper.CacheKeyGenerator;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CacheTemplateService;
import com.trinet.ambis.service.impl.CacheServiceImpl;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class CacheServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	CacheServiceImpl cacheService;

	@Mock
	CacheTemplateService cacheTemplateService;

	@Mock
	CompanyDao companyDao;

	@Mock
	CompanyDataDao companyDataDao;

	@Mock
	StrategyService strategyService;

    private MockedStatic<CacheKeyGenerator> mockStaticCacheKeyGenerator;

    @Before
    public void setUp() {
        mockStaticCacheKeyGenerator = Mockito.mockStatic(CacheKeyGenerator.class);
    }

    @After
    public void tearDown() {
        if (mockStaticCacheKeyGenerator != null) {
            mockStaticCacheKeyGenerator.close();
        }
    }


//	Delete benefit plans cache for realm plan year.
	@SuppressWarnings("unchecked")
	@Test
	public void invalidateCache() {
		String objectType = "BENEFIT-PLANS";
		String level = "REALM-PLAN-YEAR";
		String value = "26";

		ArgumentCaptor<Set> keysArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(companyDao.findByRealmPlanYearId(26L)).thenReturn(prepareCompanies());
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.BEN_PLANS_OBJECT_TYPE, "1111"))
				.thenReturn("BSS:BENEFIT-PLANS:1111");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.BEN_PLANS_OBJECT_TYPE, "2222"))
				.thenReturn("BSS:BENEFIT-PLANS:2222");
		when(cacheTemplateService.deleteFromCache(keysArgCaptor.capture())).thenReturn(true);

		boolean result = cacheService.invalidateCache(objectType, level, value);

		assertTrue(result);
		verify(companyDao, times(1)).findByRealmPlanYearId(26L);
		verify(companyDao, times(0)).findByCode(toString());
		verify(companyDao, times(0)).findAll();
		verify(cacheTemplateService, times(1)).deleteFromCache(anySet());
		assertTrue(keysArgCaptor.getValue().containsAll(Arrays.asList("BSS:BENEFIT-PLANS:1111","BSS:BENEFIT-PLANS:2222")));
	}

//	Delete benefit plans cache for company.
	@SuppressWarnings("unchecked")
	@Test
	public void invalidateCache_1() {
		String objectType = "BENEFIT-PLANS";
		String level = "COMPANY";
		String value = "G48";

		ArgumentCaptor<Set> keysArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(companyDao.findByCode("G48")).thenReturn(prepareSingleCompany());
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.BEN_PLANS_OBJECT_TYPE, "1111"))
				.thenReturn("BSS:BENEFIT-PLANS:1111");
		when(cacheTemplateService.deleteFromCache(keysArgCaptor.capture())).thenReturn(true);

		boolean result = cacheService.invalidateCache(objectType, level, value);

		assertTrue(result);
		verify(companyDao, times(0)).findByRealmPlanYearId(26L);
		verify(companyDao, times(1)).findByCode("G48");
		verify(companyDao, times(0)).findAll();
		verify(cacheTemplateService, times(1)).deleteFromCache(anySet());
		assertTrue(keysArgCaptor.getValue()
				.containsAll(Arrays.asList("BSS:BENEFIT-PLANS:1111")));
	}


//	Delete all cached objects for give company.
	@SuppressWarnings("unchecked")
	@Test
	public void invalidateCache_4() {
		String objectType = "ALL";
		String level = "COMPANY";
		String value = "G48";
		
		Strategy strategy1 = new Strategy();
		strategy1.setId(1l);
		Strategy strategy2 = new Strategy();
		strategy2.setId(2l);
		List<Strategy> strategies = Arrays.asList(strategy1, strategy2);

		ArgumentCaptor<Set> keysArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(companyDao.findByCode("G48")).thenReturn(prepareSingleCompany());
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE, "1111"))
				.thenReturn("BSS:PLAN-RATES:1111");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.BEN_PLANS_OBJECT_TYPE, "1111"))
				.thenReturn("BSS:BENEFIT-PLANS:1111");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "1111:medical"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:1111:medical");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "1111:vision"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:1111:vision");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "1111:dental"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:1111:dental");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE, "1"))
				.thenReturn("BSS:STRATEGY_DATA:1");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE, "2"))
				.thenReturn("BSS:STRATEGY_DATA:2");
		when(strategyService.getAllStrategies(1111L)).thenReturn(strategies);
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.BASIC_COMPANY_DETAILS, value))
		.thenReturn("BSS:G48");
		when(cacheTemplateService.deleteFromCache(keysArgCaptor.capture())).thenReturn(true);

		boolean result = cacheService.invalidateCache(objectType, level, value);

		assertTrue(result);
		verify(companyDao, times(0)).findByRealmPlanYearId(anyLong());
		verify(companyDao, times(4)).findByCode("G48");
		verify(companyDao, times(0)).findAll();
		verify(cacheTemplateService, times(5)).deleteFromCache(anySet());
		assertTrue(keysArgCaptor.getAllValues().get(0)
				.containsAll(Arrays.asList("BSS:BENEFIT-PLANS:1111")));
		assertTrue(keysArgCaptor.getAllValues().get(1).containsAll(Arrays.asList("BSS:PLAN-RATES:1111")));
		assertTrue(keysArgCaptor.getAllValues().get(2).containsAll(Arrays.asList(
				"BSS:OMS-BENEFIT-PLAN-RATES:1111:medical",
				"BSS:OMS-BENEFIT-PLAN-RATES:1111:vision",
				"BSS:OMS-BENEFIT-PLAN-RATES:1111:dental")));
	}
	
	//	Delete oms benefit plans rates cache for realm plan year.
	@SuppressWarnings("unchecked")
	@Test
	public void invalidateCache_5() {
		String objectType = "OMS-BENEFIT-PLAN-RATES";
		String level = "REALM-PLAN-YEAR";
		String value = "26";

		ArgumentCaptor<Set> keysArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(companyDao.findByRealmPlanYearId(26L)).thenReturn(prepareCompanies());
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "1111:medical"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:1111:medical");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "1111:vision"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:1111:vision");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "1111:dental"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:1111:dental");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "2222:medical"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:2222:medical");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "2222:vision"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:2222:vision");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "2222:dental"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:2222:dental");
		when(cacheTemplateService.deleteFromCache(keysArgCaptor.capture())).thenReturn(true);

		boolean result = cacheService.invalidateCache(objectType, level, value);

		assertTrue(result);
		verify(companyDao, times(1)).findByRealmPlanYearId(26L);
		verify(companyDao, times(0)).findByCode(toString());
		verify(companyDao, times(0)).findAll();
		verify(cacheTemplateService, times(1)).deleteFromCache(anySet());
		assertTrue(keysArgCaptor.getValue().containsAll(
				Arrays.asList("BSS:OMS-BENEFIT-PLAN-RATES:1111:medical",
						"BSS:OMS-BENEFIT-PLAN-RATES:1111:vision",
						"BSS:OMS-BENEFIT-PLAN-RATES:1111:dental",
						"BSS:OMS-BENEFIT-PLAN-RATES:2222:medical",
						"BSS:OMS-BENEFIT-PLAN-RATES:2222:vision",
						"BSS:OMS-BENEFIT-PLAN-RATES:2222:dental")));
	}

	//	Delete oms benefit plans rates cache for company.
	@SuppressWarnings("unchecked")
	@Test
	public void invalidateCache_6() {
		String objectType = "OMS-BENEFIT-PLAN-RATES";
		String level = "COMPANY";
		String value = "G48";

		ArgumentCaptor<Set> keysArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(companyDao.findByCode("G48")).thenReturn(prepareSingleCompany());
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "1111:medical"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:1111:medical");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "1111:vision"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:1111:vision");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "1111:dental"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:1111:dental");
		when(cacheTemplateService.deleteFromCache(keysArgCaptor.capture())).thenReturn(true);

		boolean result = cacheService.invalidateCache(objectType, level, value);

		assertTrue(result);
		verify(companyDao, times(0)).findByRealmPlanYearId(26L);
		verify(companyDao, times(1)).findByCode("G48");
		verify(companyDao, times(0)).findAll();
		verify(cacheTemplateService, times(1)).deleteFromCache(anySet());
		assertTrue(keysArgCaptor.getValue()
				.containsAll(Arrays.asList("BSS:OMS-BENEFIT-PLAN-RATES:1111:medical",
						"BSS:OMS-BENEFIT-PLAN-RATES:1111:vision",
						"BSS:OMS-BENEFIT-PLAN-RATES:1111:dental")));
	}

	//	Delete strategy data object
	@SuppressWarnings("unchecked")
	@Test
	public void invalidateCache_8() {
		String objectType = "STRATEGY_DATA";
		String level = "STRATEGY";
		String value = "1111";

		ArgumentCaptor<Set> keysArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE, "1111"))
				.thenReturn("BSS:STRATEGY_DATA:1111");
		when(cacheTemplateService.deleteFromCache(keysArgCaptor.capture())).thenReturn(true);

		boolean result = cacheService.invalidateCache(objectType, level, value);

		assertTrue(result);
		verify(cacheTemplateService, times(1)).deleteFromCache(anySet());
		assertTrue(keysArgCaptor.getValue()
				.containsAll(Arrays.asList(
						"BSS:STRATEGY_DATA:1111")));
	}
//	Company band code is changed.
	@Test
	public void invalidateOutofDateCache_1() {
		Company company = new Company();
		company.setCode("G48");
		company.setId(1111L);
		company.setBandCodeUpdated(true);
		company.setRegionsUpdated(false);
		company.setCompanyRegions(new HashSet<>());
		company.setEmployeeRegions(new ArrayList());

		ArgumentCaptor<Set> keysArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE, "1111"))
				.thenReturn("BSS:PLAN-RATES:1111");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.BEN_PLANS_OBJECT_TYPE, "1111"))
				.thenReturn("BSS:BENEFIT-PLANS:1111");
		when(cacheTemplateService.deleteFromCache(keysArgCaptor.capture())).thenReturn(true);

		cacheService.invalidateOutofDateCache(company);

		verify(companyDao, times(0)).findByRealmPlanYearId(anyLong());
		verify(companyDao, times(0)).findByCode("G48");
		verify(companyDao, times(0)).findAll();
		verify(cacheTemplateService, times(2)).deleteFromCache(anySet());
		verify(companyDataDao, times(0)).insertUpdateCompanyRegions(anyLong(), anySet());
		assertTrue(keysArgCaptor.getAllValues().get(0)
				.containsAll(Arrays.asList("BSS:BENEFIT-PLANS:1111")));
		assertTrue(keysArgCaptor.getAllValues().get(1).containsAll(Arrays.asList("BSS:PLAN-RATES:1111")));
	}

//	Company regions updated.
	@Test
	public void invalidateOutofDateCache_2() {
		Company company = new Company();
		company.setCode("G48");
		company.setId(1111L);
		company.setBandCodeUpdated(false);
		company.setRegionsUpdated(true);
		company.setCompanyRegions(new HashSet<>());
		company.setEmployeeRegions(new ArrayList());

		ArgumentCaptor<Set> keysArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE, "1111"))
				.thenReturn("BSS:PLAN-RATES:1111");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.BEN_PLANS_OBJECT_TYPE, "1111"))
				.thenReturn("BSS:BENEFIT-PLANS:1111");
		when(cacheTemplateService.deleteFromCache(keysArgCaptor.capture())).thenReturn(true);

		cacheService.invalidateOutofDateCache(company);

		verify(companyDao, times(0)).findByRealmPlanYearId(anyLong());
		verify(companyDao, times(0)).findByCode("G48");
		verify(companyDao, times(0)).findAll();
		verify(cacheTemplateService, times(2)).deleteFromCache(anySet());
		assertTrue(keysArgCaptor.getAllValues().get(0)
				.containsAll(Arrays.asList("BSS:BENEFIT-PLANS:1111")));
		assertTrue(keysArgCaptor.getAllValues().get(1).containsAll(Arrays.asList("BSS:PLAN-RATES:1111")));
	}

	// Company ale updated.
	@Test
	public void invalidateOutOfDateCache_3() {
		Company company = new Company();
		company.setCode("G48");
		company.setId(1111L);
		company.setBandCodeUpdated(false);
		company.setRegionsUpdated(false);
		company.setAleUpdatedNewClient(true);
		company.setAcaLargeEmplrStatusUpdated(true);
		company.setCompanyRegions(new HashSet<>());
		company.setEmployeeRegions(new ArrayList());

		Strategy strategy1 = new Strategy();
		strategy1.setId(1l);
		Strategy strategy2 = new Strategy();
		strategy2.setId(2l);
		List<Strategy> strategies = Arrays.asList(strategy1, strategy2);

		ArgumentCaptor<Set> keysArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE, "1"))
				.thenReturn("BSS:STRATEGY_DATA:1");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE, "2"))
				.thenReturn("BSS:STRATEGY_DATA:2");
		when(cacheTemplateService.deleteFromCache(keysArgCaptor.capture())).thenReturn(true);
		when(strategyService.getAllStrategies(company.getId())).thenReturn(strategies);
		when(companyDao.findByCode("G48")).thenReturn(prepareSingleCompany());

		cacheService.invalidateOutofDateCache(company);

		verify(companyDao, times(0)).findByRealmPlanYearId(anyLong());
		verify(companyDao, times(1)).findByCode("G48");
		verify(companyDao, times(0)).findAll();
		verify(strategyService, times(1)).getAllStrategies(company.getId());
		verify(cacheTemplateService, times(1)).deleteFromCache(anySet());
	}

	@Test
	public void validateRequest_test1() {
		String objectName = "ALL1";
		String level = "APP";
		String value = "";
		BSSApplicationException e = null;
		
		try {
			cacheService.invalidateCache(objectName, level, value);
		} catch (BSSApplicationException e1) {
			e = e1;
		}
		assertEquals("objectName is invalid : ALL1", e.getLocalizedMessage());
	}
	
	@Test
	public void validateRequest_test2() {
		String objectName = "ALL";
		String level = "APP1";
		String value = "";
		BSSApplicationException e = null;
		
		try {
			cacheService.invalidateCache(objectName, level, value);
		} catch (BSSApplicationException e1) {
			e = e1;
		}
		assertEquals("level is invalid : APP1", e.getLocalizedMessage());
	}
	
	@Test
	public void validateRequest_test3() {
		String objectName = "ALL";
		String level = "COMPANY";
		String value = "";
		BSSApplicationException e = null;
		
		try {
			cacheService.invalidateCache(objectName, level, value);
		} catch (BSSApplicationException e1) {
			e = e1;
		}
		assertEquals("value is required when Level is COMPANY", e.getLocalizedMessage());
		
		level = "REALM-PLAN-YEAR";
		value = "1";
		
		try {
			cacheService.invalidateCache(objectName, level, value);
		} catch (BSSApplicationException e1) {
			e = e1;
		}
		assertEquals("value is required when Level is COMPANY", e.getLocalizedMessage());
	}

    @Test
    public void testInvalidateStrategyDataCache() {
        Company company = new Company();
        company.setId(123L);
        company.setCode("G48");

        Strategy strategy1 = new Strategy();
        strategy1.setId(1L);
        Strategy strategy2 = new Strategy();
        strategy2.setId(2L);

        List<Strategy> strategies = Arrays.asList(strategy1, strategy2);
        
        List<Company> companies = new ArrayList<>();
		companies.add(company);

        Set<String> expectedKeys = new HashSet<>();
        expectedKeys.add("strategy_key_1");
        expectedKeys.add("strategy_key_2");

        when(strategyService.getAllStrategies(123L)).thenReturn(strategies);
        when(cacheTemplateService.deleteFromCache(expectedKeys)).thenReturn(true);

        when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE, "1"))
                .thenReturn("strategy_key_1");
        when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE, "2"))
                .thenReturn("strategy_key_2");
        when(companyDao.findByCode("G48")).thenReturn(companies);

        cacheService.invalidateStrategyDataCache(company);

        verify(strategyService).getAllStrategies(123L);
        verify(cacheTemplateService).deleteFromCache(expectedKeys);
    }
    
    @SuppressWarnings("unchecked")
	@Test
	public void invalidateCache_9() {
		String objectType = "ALL";
		String level = "REALM-PLAN-YEAR";
		String value = "30";

		Strategy strategy1 = new Strategy();
		strategy1.setId(1l);
		Strategy strategy2 = new Strategy();
		strategy2.setId(2l);
		List<Strategy> strategies = Arrays.asList(strategy1, strategy2);

		ArgumentCaptor<Set> keysArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(companyDao.findByRealmPlanYearId(30L)).thenReturn(prepareCompanies());
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.BEN_PLANS_OBJECT_TYPE, "1111"))
				.thenReturn("BSS:BENEFIT-PLANS:1111");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.BEN_PLANS_OBJECT_TYPE, "2222"))
				.thenReturn("BSS:BENEFIT-PLANS:2222");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "1111:medical"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:1111:medical");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "1111:vision"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:1111:vision");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "1111:dental"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:1111:dental");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "2222:medical"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:2222:medical");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "2222:vision"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:2222:vision");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, "2222:dental"))
				.thenReturn("BSS:OMS-BENEFIT-PLAN-RATES:2222:dental");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE, "1111"))
				.thenReturn("BSS:PLAN-RATES:1111");
		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE, "2222"))
				.thenReturn("BSS:PLAN-RATES:2222");
		when(strategyService.getAllStrategies(1111L)).thenReturn(strategies);
		when(cacheTemplateService.deleteFromCache(keysArgCaptor.capture())).thenReturn(true);

		boolean result = cacheService.invalidateCache(objectType, level, value);

		assertTrue(result);
		verify(companyDao, times(5)).findByRealmPlanYearId(anyLong());
		verify(companyDao, times(0)).findByCode("G48");
		verify(companyDao, times(0)).findAll();
		verify(cacheTemplateService, times(5)).deleteFromCache(anySet());
		assertTrue(keysArgCaptor.getAllValues().get(0).containsAll(Arrays.asList("BSS:BENEFIT-PLANS:1111")));
		assertTrue(keysArgCaptor.getAllValues().get(1).containsAll(Arrays.asList("BSS:PLAN-RATES:1111")));
		assertTrue(
				keysArgCaptor.getAllValues().get(2).containsAll(Arrays.asList("BSS:OMS-BENEFIT-PLAN-RATES:1111:medical",
						"BSS:OMS-BENEFIT-PLAN-RATES:1111:vision", "BSS:OMS-BENEFIT-PLAN-RATES:1111:dental")));
	}
    
	@SuppressWarnings("unchecked")
	@Test
	public void invalidateCache_10() {
		String objectType = "BASIC_COMPANY_DETAILS";
		String level = "COMPANY";
		String value = "G48";

		ArgumentCaptor<Set> keysArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.BASIC_COMPANY_DETAILS, value))
				.thenReturn("BSS:G48");
		when(cacheTemplateService.deleteFromCache(keysArgCaptor.capture())).thenReturn(true);

		boolean result = cacheService.invalidateCache(objectType, level, value);

		assertTrue(result);
		verify(companyDao, times(0)).findByRealmPlanYearId(anyLong());
		verify(companyDao, times(0)).findByCode("G48");
		verify(companyDao, times(0)).findAll();
		verify(cacheTemplateService, times(1)).deleteFromCache(anySet());
	}

    private List<Company> prepareCompanies() {
		List<Company> companies = new ArrayList<>();
		Company company = new Company();
		company.setId(1111L);
		companies.add(company);
		company = new Company();
		company.setId(2222L);
		companies.add(company);
		return companies;
	}

	private List<Company> prepareSingleCompany() {
		List<Company> companies = new ArrayList<>();
		Company company = new Company();
		company.setId(1111L);
		companies.add(company);
		return companies;
	}
}