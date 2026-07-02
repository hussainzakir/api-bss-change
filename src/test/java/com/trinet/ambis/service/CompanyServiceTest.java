package com.trinet.ambis.service;

import static com.trinet.ambis.enums.OmsOfferingEnum.OMB_TLD;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.RiskTypeEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDataDao;
import com.trinet.ambis.persistence.dao.hrp.dto.CompanyStrategyDetailsDto;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.impl.CompanyServiceImpl;
import com.trinet.ambis.service.model.RealmTypeService;
import com.trinet.ambis.service.unit.ServiceUnitTest;

@RunWith(MockitoJUnitRunner.class)
public class CompanyServiceTest extends ServiceUnitTest {

	@InjectMocks
	CompanyServiceImpl companyService;

	@Mock
	CompanyDao companyDao;
	
	@Mock
	CompanyDataDao companyDataDao;

	@Mock
	ProcessStatusService processStatusService;

	@Mock
	RealmPlanYearService realmPlanYearService;

	@Mock
	RealmTypeService realmTypeService;

	@Captor
	ArgumentCaptor<Company> companyCaptor;

	private static final String PROSPECT_ID = "P12344";
	private static final BenExchngEnums BEN_EXCHANGE = BenExchngEnums.TRINET_III;

	/**
	 * given company and realm plan year id and company is present </br>
	 * when createUpdateCompany method is called </br>
	 * then update company and return the company id </br>
	 **/
	@Test
	public void createCompanyTest1() {
		// given
		// data
		Company company = buildCompany();
		String authBroker = "testbroker";
		long bundleId = 3;
		// method mocks
		when(companyDao.saveAndFlush(companyCaptor.capture())).thenReturn(company);
		// when
		long actualResult = companyService.createUpdateCompany(company,company.getCode(), company.getRealmPlanYearId(),authBroker,bundleId, company.getRiskType(), company.getBssNaicsCode());
		// then
		// assertions
		assertEquals(111, actualResult);
		// verify
		verify(companyDao, times(1)).saveAndFlush(companyCaptor.capture());
	}

	/**
	 * given company and realm plan year id and company is not present </br>
	 * when createCompany method is called </br>
	 * then create the company and return the company id </br>
	 **/
	@Test
	public void createCompanyTest2() {
		// given
		// data
		Company company = buildCompany();
		String omsOffering = OMB_TLD.name();
		long bundleId = 3;
		company.setOmsOffering(omsOffering);
		company.setBundleId(bundleId);
		// method mocks
		when(companyDao.saveAndFlush(companyCaptor.capture())).thenReturn(company);
		// when
		long actualResult = companyService.createUpdateCompany(null,"G48", 70,omsOffering,bundleId, RiskTypeEnum.BANDS, company.getBssNaicsCode());
		// then
		// assertions
		assertEquals(111, actualResult);
		Company toBesavedCompany = companyCaptor.getValue();
		assertEquals(company.getCode(), toBesavedCompany.getCode());
		assertEquals(company.getRealmPlanYear(), toBesavedCompany.getRealmPlanYear());
		assertEquals(BigDecimal.ZERO, toBesavedCompany.getCurrentYearTotalCost());
		assertEquals(company.getDescription(), toBesavedCompany.getDescription());
		assertEquals(company.getName(), toBesavedCompany.getName());
		assertEquals(BigDecimal.ZERO, toBesavedCompany.getPercentChange());
		assertEquals(omsOffering, toBesavedCompany.getOmsOffering());
		assertEquals(bundleId, toBesavedCompany.getBundleId().longValue());
		assertEquals(RiskTypeEnum.BANDS, toBesavedCompany.getRiskType());
		// verify
		verify(companyDao, times(1)).saveAndFlush(companyCaptor.getValue());
	}

	/**
	 * given company code </br>
	 * when getCompanyStrategyDetails method is called </br>
	 * then return the map of company id and its CompanyStrategyDetailsDto</br>
	 **/
	@Test
	public void getCompanyStrategyDetailsTest1() {
		// given
		// data
		String companyCode = "B5NP1PC1";
		Map<Long, CompanyStrategyDetailsDto> companyStrategyDetailsDtos = prepareCompanyStrategyDetails();
		// method mocks
		when(companyDataDao.getCompanyStrategyDetails(companyCode)).thenReturn(companyStrategyDetailsDtos);
		// when
		Map<Long, CompanyStrategyDetailsDto> actualResult = companyService.getCompanyStrategyDetails(companyCode);
		// then
		// assertions
		assertNotNull(actualResult);
		assertEquals(1, actualResult.size());
		CompanyStrategyDetailsDto companyStrategyDetailsDto = actualResult.get(129059L);
		assertNotNull(companyStrategyDetailsDto);
		assertEquals(129059, companyStrategyDetailsDto.getCompanyId());
		assertEquals(Set.of(1L, 2L, 3L), companyStrategyDetailsDto.getAllStrategyIds());
		assertEquals(64L, companyStrategyDetailsDto.getRealmPlanYearId());
		// verify
		verify(companyDataDao, times(1)).getCompanyStrategyDetails(companyCode);
	}

	/**
	 * given company code and no records are present</br>
	 * when getCompanyStrategyDetails method is called </br>
	 * then return empty map</br>
	 **/
	@Test
	public void getCompanyStrategyDetailsTest2() {
		// given
		// data
		String companyCode = "B5NP1PC1";
		Map<Long, CompanyStrategyDetailsDto> companyStrategyDetailsDtos = Collections.emptyMap();
		// method mocks
		when(companyDataDao.getCompanyStrategyDetails(companyCode)).thenReturn(companyStrategyDetailsDtos);
		// when
		Map<Long, CompanyStrategyDetailsDto> actualResult = companyService.getCompanyStrategyDetails(companyCode);
		// then
		// assertions
		assertNotNull(actualResult);
		assertEquals(0, actualResult.size());
		// verify
		verify(companyDataDao, times(1)).getCompanyStrategyDetails(companyCode);
	}

	@Test
	public void testUpdatePlYrChangeSyncExecutedFlag_WithValidCompany_ShouldUpdateFlag() {
		// given
		Company mockCompany = new Company();
		mockCompany.setId(100L);
		mockCompany.setPlYrChangeSyncExcuted(1);

		CompanyServiceImpl spyService = spy(companyService);
		doReturn(mockCompany).when(spyService).getCompanyDetails(eq(PROSPECT_ID), eq(false), isNull(),
				eq(BEN_EXCHANGE));

		// when
		spyService.updatePlYrChangeSyncExecutedFlag(PROSPECT_ID, BEN_EXCHANGE);

		// then
		assertNull(mockCompany.getPlYrChangeSyncExcuted());
	}

	@Test
	public void testUpdatePlYrChangeSyncExecutedFlag_WithNullCompany_ShouldNotUpdate() {
		// given
		CompanyServiceImpl spyService = spy(companyService);
		doReturn(null).when(spyService).getCompanyDetails(eq(PROSPECT_ID), eq(false), isNull(), eq(BEN_EXCHANGE));

		// when
		spyService.updatePlYrChangeSyncExecutedFlag(PROSPECT_ID, BEN_EXCHANGE);

		// then
		// No exception thrown and no interaction to check;
	}
	
	@Test
	public void findCompanyByTest() {

		String companyCode = "G48";
		long realmPlanYearId = 70;

		Company company = buildCompany();
		when(companyDao.findByCodeAndRealmPlanYearId(companyCode, realmPlanYearId)).thenReturn(company);

		Company actualResult = companyService.findCompanyBy(companyCode, realmPlanYearId);

		assertNotNull(actualResult);
		assertEquals(111, actualResult.getId());
		verify(companyDao, times(1)).findByCodeAndRealmPlanYearId(companyCode, realmPlanYearId);
	}

	@Test
	public void updatePsCompanyCodeForProspectTest() {

		String companyCode = "G48";
		long companyId = 111;

		companyService.updatePsCompanyCodeForProspect(companyId, companyCode);

		verify(companyDao, times(1)).updatePsCompanyCodeForProspect(companyId, companyCode);
	}

	@Test
	public void updateCompany_WhenRiskTypeChanged_ShouldPersistAndReturnId() {
		Company foundCompany = buildCompany();
		foundCompany.setRiskType(RiskTypeEnum.BANDS);
		String omsOffering = OMB_TLD.name();
		Long bundleId = 3L;
		foundCompany.setOmsOffering(omsOffering);
		foundCompany.setBundleId(bundleId);

		when(companyDao.saveAndFlush(companyCaptor.capture())).thenReturn(foundCompany);

		long actualId = companyService.createUpdateCompany(
				foundCompany,
				foundCompany.getCode(),
				foundCompany.getRealmPlanYearId(),
				omsOffering,
				bundleId,
				RiskTypeEnum.DIFFERENTIALS,
				foundCompany.getBssNaicsCode()
		);

		assertEquals(111, actualId);
		Company saved = companyCaptor.getValue();
		assertEquals(RiskTypeEnum.DIFFERENTIALS, saved.getRiskType());
		assertEquals(omsOffering, saved.getOmsOffering());
		assertEquals(bundleId, saved.getBundleId());
		verify(companyDao, times(1)).saveAndFlush(companyCaptor.capture());
	}

	@Test
	public void updateCompany_WhenRiskTypeUnchanged_ShouldNotPersist() {
		Company foundCompany = buildCompany();
		String omsOffering = OMB_TLD.name();
		Long bundleId = 3L;
		RiskTypeEnum riskType = RiskTypeEnum.BANDS;

		foundCompany.setOmsOffering(omsOffering);
		foundCompany.setBundleId(bundleId);
		foundCompany.setRiskType(riskType);

		long actualId = companyService.createUpdateCompany(
				foundCompany,
				foundCompany.getCode(),
				foundCompany.getRealmPlanYearId(),
				omsOffering,
				bundleId,
				riskType,
				foundCompany.getBssNaicsCode()
		);

		assertEquals(111, actualId);
		verify(companyDao, times(0)).saveAndFlush(foundCompany);
	}

	@Test
	public void large_deal_prospect_should_be_true_when_bundle_id_is_custom() {
		// given
		Company company = buildCompany();
		company.setRiskType(com.trinet.ambis.enums.RiskTypeEnum.BANDS);
		long existingBundleId = 2L;
		company.setBundleId(existingBundleId);
		long bundleId = BSSApplicationConstants.CUSTOM_BUNDLE_ID;
		String authBroker = "testbroker";

		// when
		when(companyDao.saveAndFlush(any(Company.class))).thenAnswer(invocation -> {
			Company saved = invocation.getArgument(0);
			saved.setId(999L);
			return saved;
		});
		long resultId = companyService.createUpdateCompany(company, company.getCode(), company.getRealmPlanYearId(), authBroker, bundleId, company.getRiskType(), 423720);

		// then
		verify(companyDao, times(1)).saveAndFlush(any(Company.class));
		ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
		verify(companyDao).saveAndFlush(captor.capture());
		Company savedCompany = captor.getValue();
        assertEquals(1, savedCompany.getLargeDealProspect());
		assertEquals(999L, resultId);
	}

	@Test
	public void large_deal_prospect_should_be_false_when_bundle_id_is_not_custom() {
		// given
		Company company = buildCompany();
		company.setRiskType(com.trinet.ambis.enums.RiskTypeEnum.BANDS);
		long existingBundleId = 2L;
		company.setBundleId(existingBundleId);
		long bundleId = 435L;
		String authBroker = "testbroker";

		// when
		when(companyDao.saveAndFlush(any(Company.class))).thenAnswer(invocation -> {
			Company saved = invocation.getArgument(0);
			saved.setId(999L);
			return saved;
		});
		long resultId = companyService.createUpdateCompany(company, company.getCode(), company.getRealmPlanYearId(), authBroker, bundleId, company.getRiskType(), 423720);

		// then
		verify(companyDao, times(1)).saveAndFlush(any(Company.class));
		ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
		verify(companyDao).saveAndFlush(captor.capture());
		Company savedCompany = captor.getValue();
        assertNotEquals(1, savedCompany.getLargeDealProspect());
		assertEquals(999L, resultId);
	}

	@Test
	public void large_deal_prospect_should_be_false_when_bundle_id_is_null() {
		// given
		Company company = buildCompany();
		company.setRiskType(com.trinet.ambis.enums.RiskTypeEnum.BANDS);
		company.setLargeDealProspect(1);
		String authBroker = "testbroker";

		// when
		when(companyDao.saveAndFlush(any(Company.class))).thenAnswer(invocation -> {
			Company saved = invocation.getArgument(0);
			saved.setId(999L);
			return saved;
		});
		long resultId = companyService.createUpdateCompany(company, company.getCode(), company.getRealmPlanYearId(), authBroker,
				null, company.getRiskType(), 423720);

		// then
		verify(companyDao, times(1)).saveAndFlush(any(Company.class));
		ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
		verify(companyDao).saveAndFlush(captor.capture());
		Company savedCompany = captor.getValue();
		assertEquals(0, savedCompany.getLargeDealProspect());
		assertNull(savedCompany.getBundleId());
		assertEquals(999L, resultId);
	}
	
	/**
     * Test that riskType and bssNaicsCode are set and persisted correctly.
     */
    @Test
    public void createCompany_ShouldSetRiskTypeAndBssNaicsCode() {
        // given
        Company company = buildCompany();
		company.setRiskType(com.trinet.ambis.enums.RiskTypeEnum.BANDS);
        company.setBssNaicsCode(null);
        String authBroker = "testbroker";
        long bundleId = 3;
        when(companyDao.saveAndFlush(companyCaptor.capture())).thenReturn(company);
        // when
        long actualResult = companyService.createUpdateCompany(company, company.getCode(), company.getRealmPlanYearId(), authBroker, bundleId, company.getRiskType(), 423720);
        // then
        assertEquals(111, actualResult);
        Company toBeSavedCompany = companyCaptor.getValue();
		assertEquals(com.trinet.ambis.enums.RiskTypeEnum.BANDS, toBeSavedCompany.getRiskType());
        assertEquals(Integer.valueOf(423720), toBeSavedCompany.getBssNaicsCode());
        verify(companyDao, times(1)).saveAndFlush(companyCaptor.capture());
    }

	@Test
	public void initiateQuarterChange_ShouldReturnFalse_WhenQuarterIsNotTriNetIIIQuarter() {
		String companyCode = "G48";
		String messageSeq = "123";

		boolean actualResult = companyService.initiateQuarterChange(companyCode, "8Y", messageSeq);

		assertFalse(actualResult);
		verify(companyDao, times(0)).findLatestCompanyBy(anyString());
		verify(processStatusService, times(0)).createStrategySyncProcess(
				anyString(), anyString(), anyString(), anyString());
	}

	@Test
	public void initiateQuarterChange_ShouldReturnFalse_WhenCompanyIsNotTriNetIII() {
		String companyCode = "G48";
		String messageSeq = "123";

		stubLatestCompanyLookup(companyCode, "8Y", BenExchngEnums.TRINET_IV.getBenExchng(), new Date(), null);

		boolean actualResult = companyService.initiateQuarterChange(companyCode,  "Q1", messageSeq);

		assertFalse(actualResult);
		verify(processStatusService, times(0)).createStrategySyncProcess(
				anyString(), anyString(), anyString(), anyString());
	}

	@Test
	public void initiateQuarterChange_ShouldReturnFalse_WhenRequestedQuarterIsSameAsCurrentQuarter() {
		String companyCode = "G48";
		String messageSeq = "123";

		stubLatestCompanyLookup(companyCode, "Q1", BenExchngEnums.TRINET_III.getBenExchng(), new Date(), null);

		boolean actualResult = companyService.initiateQuarterChange(companyCode, "Q1", messageSeq);

		assertFalse(actualResult);
		verify(processStatusService, times(0)).findPendingOrInProgressPSQuarterChangeProcessStatus(
				anyString(), anyString());
		verify(processStatusService, times(0)).createStrategySyncProcess(
				anyString(), anyString(), anyString(), anyString());
	}

	@Test
	public void initiateQuarterChange_ShouldReturnFalse_WhenQuarterChangeProcessAlreadyInProgress() {
		String companyCode = "G48";
		String messageSeq = "123";

		stubLatestCompanyLookup(companyCode, "Q1", BEN_EXCHANGE.getBenExchng(), new Date(), null);
		when(processStatusService.findPendingOrInProgressPSQuarterChangeProcessStatus(companyCode, "Q2"))
				.thenReturn("I");

		boolean actualResult = companyService.initiateQuarterChange(companyCode, "Q2", messageSeq);

		assertFalse(actualResult);
		verify(processStatusService, times(1))
				.findPendingOrInProgressPSQuarterChangeProcessStatus(companyCode, "Q2");
		verify(processStatusService, times(0)).createStrategySyncProcess(
				anyString(), anyString(), anyString(), anyString());
	}

	@Test
	public void initiateQuarterChange_ShouldReturnTrueAndCreateStrategySyncProcess_WhenNonDifferentialsRiskType() {
		String companyCode = "G48";
		String messageSeq = "123";
		Date planYearEnd = new Date();

		stubLatestCompanyLookup(companyCode, "Q1", BEN_EXCHANGE.getBenExchng(), planYearEnd, RiskTypeEnum.BANDS);
		when(processStatusService.findPendingOrInProgressPSQuarterChangeProcessStatus(companyCode, "Q2"))
				.thenReturn(null);
		when(realmPlanYearService.getRenewalRiskTypeForLatestPlanYearInQuarter("Q2")).thenReturn(RiskTypeEnum.BANDS);

		boolean actualResult = companyService.initiateQuarterChange(companyCode, "Q2", messageSeq);

		assertTrue(actualResult);
		verify(processStatusService, times(1)).findPendingOrInProgressPSQuarterChangeProcessStatus(
				companyCode, "Q2");
		verify(processStatusService, times(1)).createStrategySyncProcess(
				eq(companyCode), anyString(),
				eq("STRATEGY_SYNC_PLYR_CHANGE"), eq("PROSPECT_ID"));
		verify(realmPlanYearService, times(1)).getRenewalRiskTypeForLatestPlanYearInQuarter("Q2");
	}

	@Test
	public void initiateQuarterChange_ShouldReturnTrueAndCreateQuarterChangeProcess_WhenDifferentialsRiskType() {
		String companyCode = "G48";
		String messageSeq = "123";
		Date planYearEnd = new Date();

		stubLatestCompanyLookup(companyCode, "Q1", BEN_EXCHANGE.getBenExchng(), planYearEnd, RiskTypeEnum.DIFFERENTIALS);
		when(processStatusService.findPendingOrInProgressPSQuarterChangeProcessStatus(companyCode, "Q2"))
				.thenReturn(null);
		when(realmPlanYearService.getRenewalRiskTypeForLatestPlanYearInQuarter("Q2")).thenReturn(RiskTypeEnum.DIFFERENTIALS);

		boolean actualResult = companyService.initiateQuarterChange(companyCode, "Q2", messageSeq);

		assertTrue(actualResult);
		verify(processStatusService, times(1)).findPendingOrInProgressPSQuarterChangeProcessStatus(
				companyCode, "Q2");
		verify(processStatusService, times(1)).createStrategySyncProcess(
				eq(companyCode), anyString(),
				eq("QUARTER_CHANGE"), eq("COMPANY_CODE"));
		verify(realmPlanYearService, times(1)).getRenewalRiskTypeForLatestPlanYearInQuarter("Q2");
	}

	private void stubLatestCompanyLookup(String companyCode, String currentQuarter, String benExchange, Date planYearEnd, RiskTypeEnum riskType) {
		Company company = buildCompany();
		company.setRealmPlanYearId(70);
		company.setRiskType(riskType);

		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(70);
		realmPlanYear.setRealmId(1);
		realmPlanYear.setOeQuarter(currentQuarter);
		realmPlanYear.setPlanYearEnd(planYearEnd);

		Realm realm = new Realm();
		realm.setBenExchange(benExchange);

		when(companyDao.findLatestCompanyBy(companyCode)).thenReturn(company);
		when(realmPlanYearService.getRealmPlanYearById(70)).thenReturn(realmPlanYear);
		when(realmTypeService.findById(1L)).thenReturn(realm);
	}

	private Company buildCompany() {
		Company company = new Company();
		company.setId(111);
		company.setCode("G48");
		company.setDescription("G48");
		company.setName("G48");
		company.setRealmPlanYearId(70);
		company.setBssNaicsCode(123456);
		return company;
	}
	
	private Map<Long, CompanyStrategyDetailsDto> prepareCompanyStrategyDetails() {
		return Map.of(129059L, CompanyStrategyDetailsDto.builder().companyId(129059L).allStrategyIds(Set.of(1L, 2L, 3L))
				.realmPlanYearId(64).build());
	}

}
