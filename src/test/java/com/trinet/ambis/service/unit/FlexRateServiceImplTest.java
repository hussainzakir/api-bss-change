package com.trinet.ambis.service.unit;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CacheObjectLevelEnum;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.StrategySyncService;
import com.trinet.ambis.service.model.*;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.FlexRateRestClient;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.service.impl.FlexRateServiceImpl;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FlexRateServiceImplTest extends ServiceUnitTest {

	private static class CapturingAppender extends AbstractAppender {
		private final List<String> messages = new CopyOnWriteArrayList<>();

		protected CapturingAppender(String name) {
			super(name, null, PatternLayout.createDefaultLayout(), false, null);
		}

		@Override
		public void append(LogEvent event) {
			messages.add(event.getMessage().getFormattedMessage());
		}

		public boolean contains(String substring) {
			return messages.stream().anyMatch(m -> m.contains(substring));
		}
	}

    @InjectMocks
    private FlexRateServiceImpl flexRateService;

    @Mock
    FlexRateRestClient apiClient;

	@Mock
	CompanyService companyService;

	@Mock
	ProcessStatusService processStatusService;

	@Mock
	CacheService cacheService;

	@Mock
	StrategySyncService strategySyncService;

	@Mock
	CompanyDao companyDao;

	@Mock
	StrategyService strategyService;

	@Mock
	RealmPlyrPlanService realmPlyrPlanService;

	@Mock
	StrategyDao strategyDao;

	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;
	private MockedStatic<CompanyServiceHelper> mockStaticCompanyServiceHelper;

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn("00001234596");
		mockStaticCompanyServiceHelper = Mockito.mockStatic(CompanyServiceHelper.class);
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
		}
		if (mockStaticCompanyServiceHelper != null) {
			mockStaticCompanyServiceHelper.close();
		}
	}

    @Test
    public void testGetPlanRatesByCompany() {
        Company company = new Company();
        company.setCode("COMP123");
        company.setProspectCompany(false);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1L);
		company.setRealmPlanYear(realmPlanYear);

        when(apiClient.getPlanRatesByCompanyCode(eq("COMP123"), eq("2025-11-14"), eq(""),any()))
                .thenReturn(buildFlexRateResponse(false));
		when(realmPlyrPlanService.getForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(prepareRealmPlyrPlans(false));

        FlexRateResponse response = flexRateService.getPlanRatesWithoutCache(company, "2025-11-14");

        assertNotNull(response);
        assertNotNull(response.getPlansByBenefitType());
        assertEquals(1, response.getPlansByBenefitType().size());
        List<BenefitPlanRate> mapped = FlexRateResponseMapper.toBenefitPlanRates(response, "123");
        assertEquals(4, mapped.size());
    }

    @Test
    public void testGetPlanRatesByProspect() {
        Company company = new Company();
        company.setProspectCompany(true);
        company.setProposalId("555");
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1L);
		company.setRealmPlanYear(realmPlanYear);

        when(apiClient.getPlanRatesByProposalId(eq("555"), eq("2025-11-14"), eq(""), any()))
                .thenReturn(buildFlexRateResponse(false));
		when(realmPlyrPlanService.getForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(prepareRealmPlyrPlans(false));

        FlexRateResponse response = flexRateService.getPlanRatesWithoutCache(company, "2025-11-14");

        assertNotNull(response);
        assertNotNull(response.getPlansByBenefitType());
        assertEquals(1, response.getPlansByBenefitType().size());
        List<BenefitPlanRate> mapped = FlexRateResponseMapper.toBenefitPlanRates(response, company.getCode());
        assertEquals(4, mapped.size());
    }

    @Test
    public void testValidationBothProvidedException() {
        // This scenario cannot happen anymore since companyCode and proposalId are calculated internally
        // Test verifies that internal validation logic works correctly
        Company company = new Company();
        company.setCode("");
        company.setProposalId("");
        company.setProspectCompany(false);
        
        try {
            flexRateService.getPlanRatesWithoutCache(company, "2025-11-14");
            fail("Expected BSSApplicationException");
        } catch (BSSApplicationException ex) {
            assertEquals(400, ex.getBssError().getStatus());
            assertEquals(BSSErrorResponseCodes.REQUEST_VALIDATION_ERROR, ex.getBssError().getCode());
        }
    }

    @Test
    public void testValidationNoneProvidedException() {
        Company company = new Company();
        company.setProspectCompany(false);
        // No code set, so validation should fail
        
        try {
            flexRateService.getPlanRatesWithoutCache(company, "2025-11-14");
            fail("Expected BSSApplicationException");
        } catch (BSSApplicationException ex) {
            assertEquals(400, ex.getBssError().getStatus());
            assertEquals(BSSErrorResponseCodes.REQUEST_VALIDATION_ERROR, ex.getBssError().getCode());
        }
    }

    @Test
    public void testValidationInvalidDateFormatException() {
        Company company = new Company();
        company.setCode("COMP123");
        company.setProspectCompany(false);
        
        try {
            flexRateService.getPlanRatesWithoutCache(company, "14-11-2025");
            fail("Expected BSSApplicationException");
        } catch (BSSApplicationException ex) {
            assertEquals(400, ex.getBssError().getStatus());
            assertEquals(BSSErrorResponseCodes.REQUEST_VALIDATION_ERROR, ex.getBssError().getCode());
        }
    }

    @Test
    public void testValidationEffectiveDateNullException() {
        Company company = new Company();
        company.setCode("COMP123");
        company.setProspectCompany(false);
        
        try {
            flexRateService.getPlanRatesWithoutCache(company, null);
            fail("Expected BSSApplicationException");
        } catch (BSSApplicationException ex) {
            assertEquals(400, ex.getBssError().getStatus());
            assertEquals(BSSErrorResponseCodes.REQUEST_VALIDATION_ERROR, ex.getBssError().getCode());
        }
    }

    @Test
    public void testValidationEffectiveDateBlankException() {
        Company company = new Company();
        company.setCode("COMP123");
        company.setProspectCompany(false);
        
        try {
            flexRateService.getPlanRatesWithoutCache(company, "   ");
            fail("Expected BSSApplicationException");
        } catch (BSSApplicationException ex) {
            assertEquals(400, ex.getBssError().getStatus());
            assertEquals(BSSErrorResponseCodes.REQUEST_VALIDATION_ERROR, ex.getBssError().getCode());
        }
    }

    @Test
    public void testCacheHitOnRepeatedCalls() {
        // Note: Caching behavior cannot be tested in unit tests as @BSSCacheable requires Spring AOP context
        // This test verifies that the method works correctly with the new signature and caching annotation

        Company company = new Company();
        company.setCode("COMP123");
        company.setProspectCompany(false);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1L);
		company.setRealmPlanYear(realmPlanYear);

        // Arrange
        when(apiClient.getPlanRatesByCompanyCode(eq("COMP123"), eq("2025-11-14"), eq(""), any()))
                .thenReturn(buildFlexRateResponse(false));
		when(realmPlyrPlanService.getForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(prepareRealmPlyrPlans(false));

        // Act - First call
        FlexRateResponse firstResponse = flexRateService.getPlanRatesWithoutCache(company, "2025-11-14");

        // Act - Second call with same inputs
        FlexRateResponse secondResponse = flexRateService.getPlanRatesWithoutCache(company, "2025-11-14");

        // Assert - Both calls return valid responses
        assertNotNull(firstResponse);
        assertNotNull(secondResponse);
        assertEquals(firstResponse.getRateGroupId(), secondResponse.getRateGroupId());

        // In unit test environment, API is called twice since caching AOP is not active
        verify(apiClient, times(2)).getPlanRatesByCompanyCode(eq("COMP123"), eq("2025-11-14"), eq(""),any());
    }

    @Test
    public void testCacheMissOnDifferentInputs() {
        // Note: This test verifies method behavior with different companyIds
        // Actual cache behavior would be tested in integration tests with Spring context

        Company company1 = new Company();
        company1.setCode("COMP123");
        company1.setProspectCompany(false);
		RealmPlanYear realmPlanYear1 = new RealmPlanYear();
		realmPlanYear1.setId(1L);
		company1.setRealmPlanYear(realmPlanYear1);
        
        Company company2 = new Company();
        company2.setCode("COMP456");
        company2.setProspectCompany(false);
		RealmPlanYear realmPlanYear2 = new RealmPlanYear();
		realmPlanYear2.setId(2L);
		company2.setRealmPlanYear(realmPlanYear1);

        // Arrange
        when(apiClient.getPlanRatesByCompanyCode(eq("COMP123"), eq("2025-11-14"), eq(""),any()))
                .thenReturn(buildFlexRateResponse(false));
		when(realmPlyrPlanService.getForRealmPlanYear(company1.getRealmPlanYear().getId())).thenReturn(prepareRealmPlyrPlans(false));
        when(apiClient.getPlanRatesByCompanyCode(eq("COMP456"), eq("2025-11-14"), eq(""),any()))
                .thenReturn(buildFlexRateResponse(false));
		when(realmPlyrPlanService.getForRealmPlanYear(company2.getRealmPlanYear().getId())).thenReturn(prepareRealmPlyrPlans(false));

        // Act - Calls with different companyIds
        FlexRateResponse firstResponse = flexRateService.getPlanRatesWithoutCache(company1, "2025-11-14");
        FlexRateResponse secondResponse = flexRateService.getPlanRatesWithoutCache(company2, "2025-11-14");

        // Assert
        assertNotNull(firstResponse);
        assertNotNull(secondResponse);

        // Verify correct API calls for different company codes
        verify(apiClient, times(1)).getPlanRatesByCompanyCode(eq("COMP123"), eq("2025-11-14"),eq(""), any());
        verify(apiClient, times(1)).getPlanRatesByCompanyCode(eq("COMP456"), eq("2025-11-14"),eq(""), any());
    }

    @Test
    public void testGetPlanRatesFromCacheDelegates() {
        Company mockCompany = new Company();
        mockCompany.setCode("COMP123");
        mockCompany.setProspectCompany(false);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1L);
		mockCompany.setRealmPlanYear(realmPlanYear);

		when(apiClient.getPlanRatesByCompanyCode(eq("COMP123"), eq("2025-11-14"),eq(""), any()))
                .thenReturn(buildFlexRateResponse(false));
		when(realmPlyrPlanService.getForRealmPlanYear(mockCompany.getRealmPlanYear().getId())).thenReturn(prepareRealmPlyrPlans(false));

        FlexRateResponse response = flexRateService.getPlanRatesFromCache(mockCompany, "2025-11-14");

        assertNotNull(response);
        assertEquals("RG12345", response.getRateGroupId());
        verify(apiClient, times(1)).getPlanRatesByCompanyCode(eq("COMP123"), eq("2025-11-14"),eq(""), any());
    }

	/**
	 * GIVEN valid company code and rate group, and company exists with a different rate group
	 * WHEN processRateUpdateEvent is called
	 * THEN companyService.updateRateGroupId and processStatusService.createBandUpdateProcess are called, returns true
	 */
	@Test
	public void testprocessRateUpdateEvent_Success() {
		RateUpdateDto dto = new RateUpdateDto();
		dto.setCompanyCode("G48");
		dto.setRateGroupId("RG-NEW");
		dto.setQuarter("Q1");
		dto.setEffectiveDate("2026-04-23");
		Company company = new Company();
		company.setCode("G48");
		company.setRateGroupId("RG-OLD");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		// Set up RealmPlanYear with realmId
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setRealmId(3);
		company.setRealmPlanYear(realmPlanYear);
		company.setId(123L);
		// When
		when(companyService.findCompanyByQuarterAndEffDate(any(), eq("Q1"), eq("G48"))).thenReturn(company);
		when(processStatusService.findPendingQuarterChangeProcesses("G48")).thenReturn(new ArrayList<>());
		when(strategyDao.findByCompanyIdAndStatus(123L, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(prepareStrategyList());
		// Act
		boolean result = flexRateService.processRateUpdateEvent(dto);
		// Assert
		assertTrue(result);
		//Verify interactions
		verify(processStatusService, times(1)).createBandUpdateProcess(3L, "G48", 123L);
	}

	/**
	 * GIVEN valid company code and rate group, and company exists with the same rate group
	 * WHEN processRateUpdateEvent is called
	 * THEN no update occurs, returns false
	 */
	@Test
	public void testprocessRateUpdateEvent_NoChange() {
		RateUpdateDto dto = new RateUpdateDto();
		dto.setCompanyCode("G48");
		dto.setRateGroupId("RG-NEW");
		dto.setQuarter("Q1");
		dto.setEffectiveDate("2026-04-23");
		Company company = new Company();
		company.setCode("G48");
		company.setRateGroupId("RG-NEW");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		when(companyService.findCompanyByQuarterAndEffDate(any(), eq("Q1"), eq("G48"))).thenReturn(company);
		when(processStatusService.findPendingQuarterChangeProcesses("G48")).thenReturn(new ArrayList<>());
		boolean result = flexRateService.processRateUpdateEvent(dto);
		assertFalse(result);
		verify(processStatusService, never()).createBandUpdateProcess(any(), any(), any());
	}

	/**
	 * GIVEN a RateUpdateDto with null company code
	 * WHEN processRateUpdateEvent is called
	 * THEN throws BSSApplicationException
	 */
	@Test
	public void testprocessRateUpdateEvent_NullCompanyCode() {
		RateUpdateDto dto = new RateUpdateDto();
		dto.setCompanyCode(null);
		dto.setRateGroupId("RG-NEW");
		BSSApplicationException exception = assertThrows(BSSApplicationException.class, 
			() -> flexRateService.processRateUpdateEvent(dto));
		assertTrue(exception.getMessage().contains("Provide either companyCode OR both prospectId and proposalId"));
	}

	/**
	 * GIVEN valid prospectId and proposalId
	 * WHEN processRateUpdateEvent is called
	 * THEN companyService is called with prospectId and process is created
	 */
	@Test
	public void testprocessRateUpdateEvent_ProspectCompany_Success() {
		RateUpdateDto dto = new RateUpdateDto();
		dto.setProspectId("P123");
		dto.setProposalId("PROP456");
		dto.setRateGroupId("RG-NEW");
		dto.setQuarter("Q1");
		dto.setEffectiveDate("2026-04-23");

		Company company = new Company();
		company.setCode("P123");
		company.setRateGroupId("RG-OLD");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setRealmId(3);
		company.setRealmPlanYear(realmPlanYear);
		company.setId(123L);

		when(companyService.findCompanyByQuarterAndEffDate(any(), eq("Q1"), eq("P123"))).thenReturn(company);
		when(processStatusService.findPendingQuarterChangeProcesses("P123")).thenReturn(new ArrayList<>());
		when(strategyDao.findByCompanyIdAndStatus(123L, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(prepareStrategyList());

		// Act
		boolean result = flexRateService.processRateUpdateEvent(dto);

		// Assert
		assertTrue(result);
		verify(companyService, times(1)).findCompanyByQuarterAndEffDate(any(), eq("Q1"), eq("P123"));
		verify(processStatusService, times(1)).createBandUpdateProcess(3L, company.getCode(), 123L);
	}

	/**
	 * GIVEN prospectId without proposalId
	 * WHEN processRateUpdateEvent is called
	 * THEN throws BSSApplicationException
	 */
	@Test
	public void testprocessRateUpdateEvent_ProspectIdWithoutProposalId() {
		RateUpdateDto dto = new RateUpdateDto();
		dto.setProspectId("P123");
		dto.setProposalId(null);
		dto.setRateGroupId("RG-NEW");

		BSSApplicationException exception = assertThrows(BSSApplicationException.class,
				() -> flexRateService.processRateUpdateEvent(dto));
		assertTrue(exception.getMessage().contains("Provide either companyCode OR both prospectId and proposalId"));
	}

	/**
	 * GIVEN proposalId without prospectId
	 * WHEN processRateUpdateEvent is called
	 * THEN throws BSSApplicationException
	 */
	@Test
	public void testprocessRateUpdateEvent_ProposalIdWithoutProspectId() {
		RateUpdateDto dto = new RateUpdateDto();
		dto.setProspectId(null);
		dto.setProposalId("PROP456");
		dto.setRateGroupId("RG-NEW");

		BSSApplicationException exception = assertThrows(BSSApplicationException.class,
			() -> flexRateService.processRateUpdateEvent(dto));
		assertTrue(exception.getMessage().contains("Provide either companyCode OR both prospectId and proposalId"));
	}

	/**
	 * GIVEN a RateUpdateDto with null rate group id
	 * WHEN processRateUpdateEvent is called
	 * THEN returns false
	 */
	@Test
	public void testprocessRateUpdateEvent_NullRateGroupId() {
		RateUpdateDto dto = new RateUpdateDto();
		dto.setCompanyCode("G48");
		dto.setRateGroupId(null);
		dto.setQuarter("Q1");
		dto.setEffectiveDate("2026-04-23");
		Company company = new Company();
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		when(companyService.findCompanyByQuarterAndEffDate(any(), eq("Q1"), eq("G48"))).thenReturn(company);
		when(processStatusService.findPendingQuarterChangeProcesses("G48")).thenReturn(new ArrayList<>());
		boolean result = flexRateService.processRateUpdateEvent(dto);
		assertFalse(result);
	}

	/**
	 * GIVEN valid company code and rate group, and company exists with a different rate group
	 * WHEN processRateUpdateEvent is called and processStatusService.createBandUpdateProcess throws exception
	 * THEN catch block is executed and BSSBadDataException is thrown
	 */
	@Test(expected = BSSBadDataException.class)
	public void testprocessRateUpdateEvent_CatchBlockCoverage() {
		RateUpdateDto dto = new RateUpdateDto();
		dto.setCompanyCode("G48");
		dto.setRateGroupId("RG-NEW");
		dto.setQuarter("Q1");
		dto.setEffectiveDate("2026-04-23");
		Company company = new Company();
		company.setCode("G48");
		company.setRateGroupId("RG-OLD");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setRealmId(3);
		company.setRealmPlanYear(realmPlanYear);
		company.setId(123L);
		when(companyService.findCompanyByQuarterAndEffDate(any(), eq("Q1"), eq("G48"))).thenReturn(company);
		when(processStatusService.findPendingQuarterChangeProcesses("G48")).thenReturn(new ArrayList<>());
		when(strategyDao.findByCompanyIdAndStatus(123L, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(prepareStrategyList());
		doThrow(new RuntimeException("Simulated failure")).when(processStatusService)
				.createBandUpdateProcess(3L, "G48", 123L);
		flexRateService.processRateUpdateEvent(dto);
	}

	/**
	 * GIVEN valid company code
	 * WHEN processRateUpdateEvent is called and company is not found
	 * THEN returns false and logs error
	 */
	@Test
	public void processRateUpdateEvent_companyNotFound_returnsFalse() {
		// Given
		RateUpdateDto dto = new RateUpdateDto();
		dto.setCompanyCode("G48");
		dto.setRateGroupId("RG-NEW");
		dto.setQuarter("Q1");
		dto.setEffectiveDate("2026-04-23");

		when(companyService.findCompanyByQuarterAndEffDate(any(), eq("Q1"), eq("G48"))).thenReturn(null);

		Logger coreLogger = (Logger) LogManager.getLogger(FlexRateServiceImpl.class);
		CapturingAppender appender = new CapturingAppender("FlexRateServiceImplTest");
		appender.start();
		coreLogger.addAppender(appender);
		try {
			// When
			boolean result = flexRateService.processRateUpdateEvent(dto);

			// Then
			assertFalse(result);
			verify(processStatusService, never()).createBandUpdateProcess(any(), any(), any());
			assertTrue("Expected an error log containing 'Company not found'", appender.contains("Company not found"));
		} finally {
			coreLogger.removeAppender(appender);
			appender.stop();
		}
	}

	/**
	 * GIVEN valid company code and company with non-DIFFERENTIALS risk type
	 * WHEN processRateUpdateEvent is called
	 * THEN does not create process and returns false, logs error
	 */
	@Test
	public void processRateUpdateEvent_nonDifferentials_doesNotCreateProcessAndReturnsFalse() {
		// Given
		RateUpdateDto dto = new RateUpdateDto();
		dto.setCompanyCode("G48");
		dto.setRateGroupId("RG-NEW");
		dto.setQuarter("Q1");
		dto.setEffectiveDate("2026-04-23");

		Company company = new Company();
		company.setId(123L);
		company.setCode("G48");
		company.setRateGroupId("RG-OLD");
		company.setRiskType(RiskTypeEnum.BANDS);
		when(companyService.findCompanyByQuarterAndEffDate(any(), eq("Q1"), eq("G48"))).thenReturn(company);
		when(processStatusService.findPendingQuarterChangeProcesses("G48")).thenReturn(new ArrayList<>());

		Logger coreLogger = (Logger) LogManager.getLogger(FlexRateServiceImpl.class);
		CapturingAppender appender = new CapturingAppender("FlexRateServiceImplTest");
		appender.start();
		coreLogger.addAppender(appender);
		try {
			// When
			boolean result = flexRateService.processRateUpdateEvent(dto);

			// Then
			assertFalse(result);
			verify(processStatusService, never()).createBandUpdateProcess(any(), any(), any());
			assertTrue("Expected an error log containing 'not DIFFERENTIALS (found: BANDS) for company id: 123'",
					appender.contains("not DIFFERENTIALS (found: BANDS) for company id: 123"));
		} finally {
			coreLogger.removeAppender(appender);
			appender.stop();
		}
	}

	@Test
	public void processRateUpdateEvent_differentials_executesNormalFlow() {
		// Given
		RateUpdateDto dto = new RateUpdateDto();
		dto.setCompanyCode("G48");
		dto.setRateGroupId("RG-NEW");
		dto.setQuarter("Q1");
		dto.setEffectiveDate("2026-04-23");

		Company company = new Company();
		company.setCode("G48");
		company.setRateGroupId("RG-OLD");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setRealmId(3);
		company.setRealmPlanYear(realmPlanYear);
		company.setId(123L);
		when(companyService.findCompanyByQuarterAndEffDate(any(), eq("Q1"), eq("G48"))).thenReturn(company);
		when(processStatusService.findPendingQuarterChangeProcesses("G48")).thenReturn(new ArrayList<>());
		when(strategyDao.findByCompanyIdAndStatus(123L, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(prepareStrategyList());

		// When
		boolean result = flexRateService.processRateUpdateEvent(dto);

		// Then
		assertTrue(result);
		verify(processStatusService, times(1)).createBandUpdateProcess(3L, "G48", 123L);
	}

	@Test
	public void processRateGroupUpdate_happyPath_invokesAllSteps() {
		Company company = new Company();
		company.setId(123L);
		company.setCode("G48");
		String rateGroupId = "RG123";

		when(cacheService.invalidateCache(CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE.getObjectType(),
				CacheObjectLevelEnum.COMPANY.getObjectLevel(), company.getCode())).thenReturn(true);
		when(strategyService.getAllStrategies(company.getId())).thenReturn(prepareStrategyList());

		flexRateService.processRateGroupUpdate(company, rateGroupId);

		verify(cacheService, times(1)).invalidateCache(CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE.getObjectType(),
				CacheObjectLevelEnum.COMPANY.getObjectLevel(), company.getCode());
		verify(strategyService, times(1)).getAllStrategies(company.getId());
		verify(cacheService, times(1)).invalidateStrategyDataCache(company);
		verify(strategySyncService, times(1)).syncStrategyData(company, null);
		verify(companyDao, times(1)).saveAndFlush(company);
	}

	@Test
	public void processRateGroupUpdate_noStrategies_invokesOnlyRateCacheInvalidation() {
		Company company = new Company();
		company.setId(123L);
		company.setCode("G48");
		String rateGroupId = "RG123";

		when(cacheService.invalidateCache(CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE.getObjectType(),
				CacheObjectLevelEnum.COMPANY.getObjectLevel(), company.getCode())).thenReturn(true);
		when(strategyService.getAllStrategies(company.getId())).thenReturn(new ArrayList<>());

		flexRateService.processRateGroupUpdate(company, rateGroupId);

		verify(cacheService, times(1)).invalidateCache(CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE.getObjectType(),
				CacheObjectLevelEnum.COMPANY.getObjectLevel(), company.getCode());
		verify(strategyService, times(1)).getAllStrategies(company.getId());
		verify(cacheService, times(0)).invalidateStrategyDataCache(company);
		verify(strategySyncService, times(0)).syncStrategyData(company, null);
		verify(companyDao, times(0)).saveAndFlush(company);
	}

	@Test
	public void syncRateGroupWhenUpdated_callsProcessRateGroupUpdate_whenRateGroupDiffers() {
		// Given
		Company company = new Company();
		company.setId(123L);
		company.setCode("COMP123");
		company.setRateGroupId("RG123");
		company.setProposalId("Q-778977");
		company.setPlanStartDate("01-JAN-2026");
		FlexRateResponse response = new FlexRateResponse();
		response.setRateGroupId("RG900");

		FlexRateServiceImpl spyService = spy(flexRateService);
		doReturn(response).when(spyService).getPlanRatesWithoutCache(company, "2026-01-01");
		doNothing().when(spyService).processRateGroupUpdate(company, "RG900");

		// When
		spyService.syncRateGroupWhenUpdated(company);

		// Then
		verify(spyService, times(1)).processRateGroupUpdate(company, "RG900");
	}

	@Test
	public void syncRateGroupWhenUpdated_doesNotCallProcessRateGroupUpdate_whenRateGroupSame() {
		// Given
		Company company = new Company();
		company.setId(123L);
		company.setCode("COMP123");
		company.setRateGroupId("RG123");
		company.setProposalId("Q-778977");
		company.setPlanStartDate("01-JAN-2026");
		FlexRateResponse response = new FlexRateResponse();
		response.setRateGroupId("RG123");

		FlexRateServiceImpl spyService = spy(flexRateService);
		doReturn(response).when(spyService).getPlanRatesWithoutCache(company, "2026-01-01");
		// When
		spyService.syncRateGroupWhenUpdated(company);

		// Then
		verify(spyService, never()).processRateGroupUpdate(any(), any());
	}

	@Test
	public void syncRateGroupWhenUpdated_doesNotCallProcessRateGroupUpdate_whenRateGroupNull() {
		// Given
		Company company = new Company();
		company.setId(123L);
		company.setCode("COMP123");
		company.setRateGroupId("RG123");
		company.setProposalId("Q-778977");
		company.setPlanStartDate("01-JAN-2026");
		FlexRateResponse response = new FlexRateResponse();
		response.setRateGroupId(null);

		FlexRateServiceImpl spyService = spy(flexRateService);
		doReturn(response).when(spyService).getPlanRatesWithoutCache(company, "2026-01-01");
		// When
		spyService.syncRateGroupWhenUpdated(company);

		// Then
		verify(spyService, never()).processRateGroupUpdate(any(), any());
	}

	@Test
	public void syncRateGroupWhenUpdated_whenProcessRateGroupUpdate_throwsException() {
		// Given
		Company company = new Company();
		company.setId(123L);
		company.setCode("COMP123");
		company.setRateGroupId("RG123");
		company.setProposalId("Q-778977");
		company.setPlanStartDate("01-JAN-2026");
		FlexRateResponse response = new FlexRateResponse();
		response.setRateGroupId("RG900");

		FlexRateServiceImpl spyService = spy(flexRateService);
		doReturn(response).when(spyService).getPlanRatesWithoutCache(company, "2026-01-01");
		Mockito.doThrow(new RuntimeException("Exception while updating the rate group")).when(spyService)
				.processRateGroupUpdate(company, "RG900");
		try {
			// When
			spyService.syncRateGroupWhenUpdated(company);
			fail("Expected RuntimeException");
		} catch (RuntimeException ex) {
			// Then
			assertEquals("Exception while updating the rate group", ex.getMessage());
			// Exception is expected, so we can assert it was thrown and then verify that processRateGroupUpdate was called
			verify(spyService, times(1)).processRateGroupUpdate(company, "RG900");
			return; // Test passes if exception is thrown and verified
		}
		spyService.syncRateGroupWhenUpdated(company);
	}

	@Test
	public void testGetPlanRatesWithoutCache_nullRateGroupId_throwsException() {
		Company company = new Company();
		company.setId(123L);
		company.setCode("COMP123");
		company.setProspectCompany(false);
		FlexRateResponse response = new FlexRateResponse();
		response.setRateGroupId(null);
		when(apiClient.getPlanRatesByCompanyCode(eq("COMP123"), eq("2025-11-14"), eq(""),any())).thenReturn(response);
		try {
			flexRateService.getPlanRatesWithoutCache(company, "2025-11-14");
			fail("Expected BSSBadDataException");
		} catch (BSSBadDataException ex) {
			assertTrue(ex.getMessage().contains("FlexRateService returned null rateGroupId for company id: 123, code: COMP123"));
		}
	}

	/**
	 * GIVEN valid company
	 * WHEN flex rate service is called
	 * AND not all plans returned in the response are in the realm plan year
	 * THEN those plans should be filtered out
	 */
	@Test
	public void testGetPlanRatesByCompanyResponseIncludesExtraPlans() {
		Company company = new Company();
		company.setProspectCompany(true);
		company.setProposalId("555");
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1L);
		company.setRealmPlanYear(realmPlanYear);

		when(apiClient.getPlanRatesByProposalId(eq("555"), eq("2025-11-14"),eq(""), any()))
				.thenReturn(buildFlexRateResponse(true));
		when(realmPlyrPlanService.getForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(prepareRealmPlyrPlans(false));

		FlexRateResponse response = flexRateService.getPlanRatesWithoutCache(company, "2025-11-14");

		assertNotNull(response);
		assertNotNull(response.getPlansByBenefitType());
		assertEquals(1, response.getPlansByBenefitType().size());
		List<BenefitPlanRate> mapped = FlexRateResponseMapper.toBenefitPlanRates(response, company.getCode());
		assertEquals(4, mapped.size());
	}


	/**
	 * GIVEN valid company
	 * WHEN flex rate service is called
	 * AND plan returned in the realm plan year are not returned in the response
	 * THEN an exception should be thrown since we cannot find rates for the plans in the realm plan year
	 */
	@Test
	public void testGetPlanRatesByCompanyPlanYearPlanIncludesExtraPlans() {
		Company company = new Company();
		company.setProspectCompany(true);
		company.setProposalId("555");
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1L);
		company.setRealmPlanYear(realmPlanYear);

		when(apiClient.getPlanRatesByProposalId(eq("555"), eq("2025-11-14"), eq(""), any()))
				.thenReturn(buildFlexRateResponse(false));
		when(realmPlyrPlanService.getForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(prepareRealmPlyrPlans(true));

		try {
			flexRateService.getPlanRatesWithoutCache(company, "2025-11-14");
			fail("Expected BSSBadDataException");
		} catch (BSSBadDataException ex) {
			assertTrue(ex.getMessage().contains("The following plans are configured for realm plan year 1 but were missing from the Flex Rate Response for company null: 00NPR6"));
		}
	}


	/**
	 * GIVEN valid company
	 * WHEN flex rate service is called
	 * AND plan returned in the realm plan year are not returned in the response
	 * AND the message is more than 2000 characters long
	 * THEN an exception should be thrown with a truncated message since we cannot find rates for the plans in the realm plan year
	 */
	@Test
	public void testGetPlanRatesByCompanyPlanYearPlanIncludesExtraPlansMessageTruncated() {
		Company company = new Company();
		company.setProspectCompany(true);
		company.setProposalId("555");
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1L);
		company.setRealmPlanYear(realmPlanYear);

		when(apiClient.getPlanRatesByProposalId(eq("555"), eq("2025-11-14"), eq(""), any()))
				.thenReturn(buildFlexRateResponse(false));
		when(realmPlyrPlanService.getForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(prepareRealmPlyrPlansForValidationMessageTest());

		try {
			flexRateService.getPlanRatesWithoutCache(company, "2025-11-14");
			fail("Expected BSSBadDataException");
		} catch (BSSBadDataException ex) {
			assertTrue(ex.getMessage().contains("The following plans are configured for realm plan year 1 but were missing from the Flex Rate Response for company null: 000001"));
			assertTrue(ex.getMessage().contains("...(truncated)"));
		}
	}

	/**
	 * GIVEN a company with a pending QUARTER_CHANGE record
	 * WHEN processRateUpdateEvent is called with a different rate group
	 * THEN the quarter-change handling path is executed (createStrategySyncProcess is called)
	 * AND createBandUpdateProcess is NOT called
	 * AND processData is used from the existing record (not converted again)
	 */
	@Test
	public void testprocessRateUpdateEvent_ClientWithQuarterChange_CreatesStrategySyncPlyrChange() {
		// Given
		RateUpdateDto dto = new RateUpdateDto();
		dto.setCompanyCode("G48");
		dto.setRateGroupId("RG-NEW");
		dto.setQuarter("Q1");
		dto.setEffectiveDate("2026-04-23");

		Company company = new Company();
		company.setCode("G48");
		company.setRateGroupId("RG-OLD");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		company.setRealmPlanYearId(99L);
		company.setId(123L);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setRealmId(3);
		company.setRealmPlanYear(realmPlanYear);

		ProcessStatus quarterChangeRecord = new ProcessStatus();
		quarterChangeRecord.setProcessName(ProcessStatusEnum.QUARTER_CHANGE.getProcessName());
		quarterChangeRecord.setProcessStatus("N");
		quarterChangeRecord.setUserId("T2_CLIENTOPTION_MSG");
		quarterChangeRecord.setEffDt(new java.util.Date());
		quarterChangeRecord.setProcessIdentiferValue("G48");
		quarterChangeRecord.setProcessData("{\"oldRealmPlanYearId\":98,\"newRealmPlanYearId\":99}");

		when(processStatusService.findPendingQuarterChangeProcesses("G48"))
				.thenReturn(List.of(quarterChangeRecord));

		// When
		boolean result = flexRateService.processRateUpdateEvent(dto);

		// Then
		assertTrue(result);
		verify(processStatusService, times(1)).createStrategySyncProcess(
				eq("G48"),
				eq("{\"oldRealmPlanYearId\":98,\"newRealmPlanYearId\":99}"),
				eq(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName()),
				eq(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getIdentifierName()));
		verify(processStatusService, never()).saveAll(anyList());
		verify(processStatusService, never()).createBandUpdateProcess(any(), any(), any());
	}

	/**
	 * GIVEN a company without pending QUARTER_CHANGE record
	 * WHEN processRateUpdateEvent is called with a different rate group
	 * THEN the existing band update process is created
	 */
	@Test
	public void testprocessRateUpdateEvent_ClientWithoutQuarterChange_CreatesBandUpdate() {
		// Given
		RateUpdateDto dto = new RateUpdateDto();
		dto.setCompanyCode("G48");
		dto.setRateGroupId("RG-NEW");
		dto.setQuarter("Q1");
		dto.setEffectiveDate("2026-04-23");

		Company company = new Company();
		company.setCode("G48");
		company.setRateGroupId("RG-OLD");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setRealmId(3);
		company.setRealmPlanYear(realmPlanYear);
		company.setId(123L);

		when(companyService.findCompanyByQuarterAndEffDate(any(), eq("Q1"), eq("G48"))).thenReturn(company);
		when(processStatusService.findPendingQuarterChangeProcesses("G48"))
				.thenReturn(new ArrayList<>());
		when(strategyDao.findByCompanyIdAndStatus(123L, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(prepareStrategyList());

		// When
		boolean result = flexRateService.processRateUpdateEvent(dto);

		// Then
		assertTrue(result);
		verify(processStatusService, times(1)).createBandUpdateProcess(3L, "G48", 123L);
		verify(processStatusService, never()).saveAll(anyList());
	}

	/**
	 * GIVEN a prospect company
	 * WHEN processRateUpdateEvent is called with a different rate group
	 * THEN quarter-change lookup IS performed (now checks all companies)
	 * AND existing band update process is created when no quarter change found
	 */
	@Test
	public void testprocessRateUpdateEvent_ProspectCompany_ChecksQuarterChange() {
		// Given
		RateUpdateDto dto = new RateUpdateDto();
		dto.setProspectId("P123");
		dto.setProposalId("PROP456");
		dto.setRateGroupId("RG-NEW");
		dto.setQuarter("Q1");
		dto.setEffectiveDate("2026-04-23");

		Company company = new Company();
		company.setCode("P123");
		company.setRateGroupId("RG-OLD");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setRealmId(3);
		company.setRealmPlanYear(realmPlanYear);
		company.setId(123L);

		when(companyService.findCompanyByQuarterAndEffDate(any(), eq("Q1"), eq("P123"))).thenReturn(company);
		when(processStatusService.findPendingQuarterChangeProcesses("P123")).thenReturn(new ArrayList<>());
		when(strategyDao.findByCompanyIdAndStatus(123L, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(prepareStrategyList());

		// When
		boolean result = flexRateService.processRateUpdateEvent(dto);

		// Then
		assertTrue(result);
		verify(processStatusService, times(1)).findPendingQuarterChangeProcesses("P123");
		verify(processStatusService, times(1)).createBandUpdateProcess(3L, "P123", 123L);
	}

	/**
	 * GIVEN valid company with no active strategies
	 * WHEN processRateUpdateEvent is called with a different rate group
	 * THEN no band update process is created and returns false
	 * AND logs error message about no strategies available
	 */
	@Test
	public void testprocessRateUpdateEvent_NoActiveStrategies_ReturnsFalse() {
		// Given
		RateUpdateDto dto = new RateUpdateDto();
		dto.setCompanyCode("G48");
		dto.setRateGroupId("RG-NEW");
		dto.setQuarter("Q1");
		dto.setEffectiveDate("2026-04-23");

		Company company = new Company();
		company.setCode("G48");
		company.setRateGroupId("RG-OLD");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setRealmId(3);
		company.setRealmPlanYear(realmPlanYear);
		company.setId(123L);

		when(companyService.findCompanyByQuarterAndEffDate(any(), eq("Q1"), eq("G48"))).thenReturn(company);
		when(processStatusService.findPendingQuarterChangeProcesses("G48")).thenReturn(new ArrayList<>());
		when(strategyDao.findByCompanyIdAndStatus(123L, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(new ArrayList<>());

		Logger coreLogger = (Logger) LogManager.getLogger(FlexRateServiceImpl.class);
		CapturingAppender appender = new CapturingAppender("FlexRateServiceImplTest");
		appender.start();
		coreLogger.addAppender(appender);
		try {
			// When
			boolean result = flexRateService.processRateUpdateEvent(dto);

			// Then
			assertFalse(result);
			verify(processStatusService, never()).createBandUpdateProcess(any(), any(), any());
			assertTrue("Expected an error log containing 'No Strategies are available for company G48'",
					appender.contains("No Strategies are available for company G48"));
		} finally {
			coreLogger.removeAppender(appender);
			appender.stop();
		}
	}

	/**
	 * GIVEN a client (non-prospect) company with a bundleId assigned
	 * WHEN getPlanRatesWithoutCache is called
	 * THEN bundleId is forwarded to getPlanRatesByCompanyCode so TRS returns bundle-specific rates
	 */
	@Test
	public void testGetPlanRatesByClientCompanyWithBundleId() {
		Company company = new Company();
		company.setCode("COMP123");
		company.setProspectCompany(false);
		company.setBundleId(2L);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1L);
		company.setRealmPlanYear(realmPlanYear);

		when(apiClient.getPlanRatesByCompanyCode(eq("COMP123"), eq("2025-11-14"), eq("2"), any()))
				.thenReturn(buildFlexRateResponse(false));
		when(realmPlyrPlanService.getForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(prepareRealmPlyrPlans(false));

		FlexRateResponse response = flexRateService.getPlanRatesWithoutCache(company, "2025-11-14");

		assertNotNull(response);
		assertNotNull(response.getPlansByBenefitType());
		verify(apiClient, times(1)).getPlanRatesByCompanyCode(eq("COMP123"), eq("2025-11-14"), eq("2"), any());
	}

	/**
	 * GIVEN a prospect company with a bundleId assigned
	 * WHEN getPlanRatesWithoutCache is called
	 * THEN bundleId is forwarded to getPlanRatesByProposalId so TRS returns bundle-specific rates
	 */
	@Test
	public void testGetPlanRatesByProspectCompanyWithBundleId() {
		Company company = new Company();
		company.setProspectCompany(true);
		company.setProposalId("555");
		company.setBundleId(1L);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1L);
		company.setRealmPlanYear(realmPlanYear);

		when(apiClient.getPlanRatesByProposalId(eq("555"), eq("2025-11-14"), eq("1"), any()))
				.thenReturn(buildFlexRateResponse(false));
		when(realmPlyrPlanService.getForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(prepareRealmPlyrPlans(false));

		FlexRateResponse response = flexRateService.getPlanRatesWithoutCache(company, "2025-11-14");

		assertNotNull(response);
		assertNotNull(response.getPlansByBenefitType());
		verify(apiClient, times(1)).getPlanRatesByProposalId(eq("555"), eq("2025-11-14"), eq("1"), any());
	}

	private FlexRateResponse buildFlexRateResponse(boolean includeExtraPlans) {
        FlexRateResponse response = new FlexRateResponse();
        response.setRateGroupId("RG12345");

        // Prepare PlanRate for medical
        PlanRate planRate10_1 = PlanRate.builder()
                .planType("10")
                .dpPlanType("15")
                .planId("8acba25e99dbf5760199e3ca33e20082")
                .regionalPlanId("00NPQ6")
                .dpRegionalPlanId("00NPQ7")
                .rateDetails(
                        PlanRate.RateDetails.builder()
                                .rateType("tiered")
                                .rates(List.of(
                                        buildBenPlanRate("1", BigDecimal.valueOf(150.0)),
                                        buildBenPlanRate("2", BigDecimal.valueOf(300.0)),
                                        buildBenPlanRate("C", BigDecimal.valueOf(400.0)),
                                        buildBenPlanRate("4", BigDecimal.valueOf(500.0))))
                                .build()
                ).build();

		FlexRateResponse.PlansByPlanType plansByPlanTypeMedical = new FlexRateResponse.PlansByPlanType();
		plansByPlanTypeMedical.setPlans(new ArrayList<>());
		plansByPlanTypeMedical.getPlans().add(planRate10_1);


		if (includeExtraPlans) {
			PlanRate planRate10_2 = PlanRate.builder()
					.planType("10")
					.dpPlanType("15")
					.planId("8acba25e99dbf5760199e3ca33e20082")
					.regionalPlanId("00NPR6")
					.dpRegionalPlanId("00NPR7")
					.rateDetails(
							PlanRate.RateDetails.builder()
									.rateType("tiered")
									.rates(List.of(
											buildBenPlanRate("1", BigDecimal.valueOf(150.0)),
											buildBenPlanRate("2", BigDecimal.valueOf(300.0)),
											buildBenPlanRate("C", BigDecimal.valueOf(400.0)),
											buildBenPlanRate("4", BigDecimal.valueOf(500.0))))
									.build()
					).build();
			plansByPlanTypeMedical.getPlans().add(planRate10_2);
		}

        FlexRateResponse.PlanByBenefitType planByBenefitTypeMedical = new FlexRateResponse.PlanByBenefitType();
        planByBenefitTypeMedical.setBenefitType(BSSApplicationConstants.MEDICAL);
        planByBenefitTypeMedical.setPlansByPlanType(List.of(plansByPlanTypeMedical));

        response.setPlansByBenefitType(List.of(planByBenefitTypeMedical));
        return response;
    }

    private PlanRate.RateDetails.Rate buildBenPlanRate(
            String tierCode,
            BigDecimal rate
    ) {
        return PlanRate.RateDetails.Rate.builder()
                .tierCode(tierCode)
                .retailRate(rate.doubleValue())
                .build();
    }

	private List<Strategy> prepareStrategyList() {
		Strategy strategy1 = new Strategy();
		strategy1.setId(1L);
		strategy1.setCompanyId(123L);
		strategy1.setStatus(BSSApplicationConstants.STATUS_ACTIVE);

		Strategy strategy2 = new Strategy();
		strategy2.setId(2L);
		strategy2.setCompanyId(123L);
		strategy2.setStatus(BSSApplicationConstants.STATUS_ACTIVE);

		return List.of(strategy1, strategy2);
	}

	private List<XbssRealmPlyrPlan> prepareRealmPlyrPlans(boolean includeExtraPlans) {
		List<XbssRealmPlyrPlan> plans = new ArrayList<>();
		XbssRealmPlyrPlan plan1 = new XbssRealmPlyrPlan();
		plan1.setBenefitPlan("00NPQ6");
		plan1.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		plans.add(plan1);

		XbssRealmPlyrPlan plan2 = new XbssRealmPlyrPlan();
		plan2.setBenefitPlan("LIFE001");
		plan2.setPlanType(BSSApplicationConstants.LIFE_CODE);
		plans.add(plan2);

		if (includeExtraPlans) {
			XbssRealmPlyrPlan extraPlan = new XbssRealmPlyrPlan();
			extraPlan.setBenefitPlan("00NPR6");
			extraPlan.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
			plans.add(extraPlan);
		}

		return plans;
	}

	private List<XbssRealmPlyrPlan> prepareRealmPlyrPlansForValidationMessageTest() {
		List<XbssRealmPlyrPlan> plans = new ArrayList<>();
		List<String> planIds = List.of("000001", "000002", "000003", "000004", "000005", "000006",
				"000007", "000008", "000009", "000010", "000011", "000012", "000013", "000014",
				"000015", "000016", "000017", "000018", "000019", "000020", "000021", "000022",
				"000023", "000024", "000025", "000026", "000027", "000028", "000029", "000030",
				"000031", "000032", "000033", "000034", "000035", "000036", "000037", "000038",
				"000039", "000040", "000041", "000042", "000043", "000044", "000045", "000046",
				"000047", "000048", "000049", "000050", "000051", "000052", "000053", "000054",
				"000055", "000056", "000057", "000058", "000059", "000060", "000061", "000062",
				"000063", "000064", "000065", "000066", "000067", "000068", "000069", "000070",
				"000071", "000072", "000073", "000074", "000075", "000076", "000077", "000078",
				"000079", "000080", "000081", "000082", "000083", "000084", "000085", "000086",
				"000087", "000088", "000089", "000090", "000091", "000092", "000093", "000094",
				"000095", "000096", "000097", "000098", "000099", "000100", "000101", "000102",
				"000103", "000104", "000105", "000106", "000107", "000108", "000109", "000110",
				"000111", "000112", "000113", "000114", "000115", "000116", "000117", "000118",
				"000119", "000120", "000121", "000122", "000123", "000124", "000125", "000126",
				"000127", "000128", "000129", "000130", "000131", "000132", "000133", "000134",
				"000135", "000136", "000137", "000138", "000139", "000140", "000141", "000142",
				"000143", "000144", "000145", "000146", "000147", "000148", "000149", "000150",
				"000151", "000152", "000153", "000154", "000155", "000156", "000157", "000158",
				"000159", "000160", "000161", "000162", "000163", "000164", "000165", "000166",
				"000167", "000168", "000169", "000170", "000171", "000172", "000173", "000174",
				"000175", "000176", "000177", "000178", "000179", "000180", "000181", "000182",
				"000183", "000184", "000185", "000186", "000187", "000188", "000189", "000190",
				"000191", "000192", "000193", "000194", "000195", "000196", "000197", "000198",
				"000199", "000200", "000201", "000202", "000203", "000204", "000205", "000206",
				"000207", "000208", "000209", "000210", "000211", "000212", "000213", "000214",
				"000215", "000216", "000217", "000218", "000219", "000220", "000221", "000222",
				"000223", "000224", "000225", "000226", "000227", "000228", "000229", "000230",
				"000231", "000232", "000233", "000234", "000235", "000236", "000237", "000238",
				"000239", "000240", "000241", "000242", "000243", "000244", "000245", "000246",
				"000247", "000248", "000249", "000250", "000251", "000252", "000253", "000254",
				"000255");

		for (String planId : planIds) {
			XbssRealmPlyrPlan plan = new XbssRealmPlyrPlan();
			plan.setBenefitPlan(planId);
			plan.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
			plans.add(plan);
		}
		return plans;
	}

}
