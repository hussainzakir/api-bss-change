package com.trinet.ambis.service.unit;

import static com.trinet.ambis.enums.BenExchngEnums.TRINET_OMS;
import static com.trinet.ambis.enums.OmsOfferingEnum.OMB_TLD;
import static com.trinet.ambis.enums.OmsOfferingEnum.OM_OD_OV_TLD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.exception.BSSApplicationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trinet.ambis.enums.RiskTypeEnum;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.ApiBssPropertiesConstants;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.OmsOfferingEnum;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.exception.InvalidOmsOfferingException;
import com.trinet.ambis.persistence.dao.hrp.CompanyBandCodesDao;
import com.trinet.ambis.persistence.model.Bundle;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.ExchangeDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.dto.ExchangeCarrierBandDto;
import com.trinet.ambis.persistence.dao.hrp.dto.ExchangeCarrierDetailsDto;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.exchange.CarrierDto;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto.CarrierBand;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto.CarrierBand.CarrierBandDetails;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeCarrierDto;
import com.trinet.ambis.service.BandCodesService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.ProspectCompanyService;
import com.trinet.ambis.service.ProspectStrategySyncService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.StrategySyncService;
import com.trinet.ambis.service.dto.RealmPlanYearDetailsDto;
import com.trinet.ambis.service.impl.ExchangeServiceImpl;
import com.trinet.ambis.service.model.prospect.ProspectInfoResponse;
import com.trinet.ambis.service.BenefitsBundleService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Utils;

@RunWith(MockitoJUnitRunner.class)
public class ExchangeServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	private ExchangeServiceImpl exchangeServiceImpl;

	@Mock
	private ExchangeDao exchangeDaoMock;

	@Mock
	private RealmPlanYearService realmPlanYearService;

	@Mock
	private StrategySyncService strategySyncService;

	@Mock
	private CompanyService companyService;
	
	@Mock
	private ProspectCompanyService prospectCompanyService;

	@Mock
	private ProcessStatusService processStatusService;

	@Mock
	private BandCodesService bandCodesService;
	
	@Mock
	private StrategyDataDao strategyDataDao;
	
	@Mock
	private CompanyBandCodesDao companyBandCodesDao;
	
	@Mock
	ProspectStrategySyncService prospectStrategySyncService;

	@Mock
	private BenefitsBundleService benefitsBundleService;

	@Captor
	private ArgumentCaptor<Long> exchangeIdCaptor;

	@Captor
	private ArgumentCaptor<String> companyCodeCaptor;

	@Captor
	private ArgumentCaptor<Long> realmPlanYearIdCaptor;

	@Captor
	private ArgumentCaptor<CarrierBand> carrierBandCaptor;

	@Captor
	private ArgumentCaptor<Long> companyIdCaptor;
	
	@Captor
	private ArgumentCaptor<String> omsOfferingCaptor;

	@Captor
	private ArgumentCaptor<Long> bundleIdCaptor;
	
	@Captor
	private ArgumentCaptor<Company> companyCaptor;

	@Captor
	private ArgumentCaptor<RiskTypeEnum> riskTypeCaptor;

	@Captor
	private ArgumentCaptor<Integer> naicsCodeCaptor;

	@Mock
	CompanyDao companyDao;

	private static final long COMPANY_ID = 111;

	private static final String COMPANY_CODE = "G48";
	
	private static final String EXCHANGE_ID = "";

	private static final long OMS_REALM_PLAN_YEAR_ID = 85;
	private static final String OMS_QUARTER = "A1";
	private static final long OMS_REALM_ID = 6;
    private static MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;
    private static MockedStatic<AppRulesAndConfigsUtils> mockStaticRequestValidator;
    private static MockedStatic<BSSMessageConfig> mockStaticBSSMessageConfig;

    @Before
    public void setUpClass() {
        mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
        mockStaticRequestValidator = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
        mockStaticBSSMessageConfig = Mockito.mockStatic(BSSMessageConfig.class);
    }

    @After
    public void tearDownClass() {
        if (mockStaticBSSSecurityUtils != null) {
            mockStaticBSSSecurityUtils.close();
            mockStaticBSSSecurityUtils = null;
        }
        if (mockStaticRequestValidator != null) {
            mockStaticRequestValidator.close();
            mockStaticRequestValidator = null;
        }
        if (mockStaticBSSMessageConfig != null) {
            mockStaticBSSMessageConfig.close();
            mockStaticBSSMessageConfig = null;
        }
    }

	@Test
	public void getExchangeCarriersWithRecordsTest() {
		// given
		String companyCode = "PROSPECT-COMPANY-CODE";
		long realmYearIdXI = 77;
		long realmYearIdIV = 76;
		long realmYearIdOMS = 85;
		Company companyXI = new Company();
		Company companyIV = new Company();
		Company companyOMS = new Company();
		companyXI.setCode("comXI");
		companyXI.setLargeDealProspect(0);
		companyXI.setRealmPlanYearId(realmYearIdXI);
		companyIV.setCode("comIV");
		companyIV.setLargeDealProspect(1);
		companyIV.setRealmPlanYearId(realmYearIdIV);
		companyOMS.setCode("comOMS");
		companyOMS.setLargeDealProspect(5);
		companyOMS.setRealmPlanYearId(realmYearIdOMS);
		Set<String> planTypes = new HashSet<>(Set.of("10", "11", "14", "1D", "1V"));
		Set<String> planTypesXI = new HashSet<>(Set.of("11", "14", "1D", "1V"));
		Set<String> planTypesOMS = new HashSet<>(Set.of("11", "14", "1D", "1V"));
		List<ExchangeCarrierDetailsDto> exchangeCarrierDetailsDtos = prepareExchangeCarrierDetailsDtos();
		ProspectInfoResponse prospectInfoResponse = prepareProspectCompany();
		Date benStartDate = Utils.convertStringToDate(prospectInfoResponse.getBenStartDate(),
				BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD);

		when(prospectCompanyService.getProspectBasicDetails(companyCode, BenExchngEnums.getByExchangeId(EXCHANGE_ID))).thenReturn(prospectInfoResponse);
		when(exchangeDaoMock.getExchangeCarriers(companyCode, prospectInfoResponse.getHqState(),
				prospectInfoResponse.getZipCode(), benStartDate))
				.thenReturn(exchangeCarrierDetailsDtos);
		Bundle bundle_IV = new Bundle();
		bundle_IV.setId(2L);
		bundle_IV.setCompanyCode("comIV");
		bundle_IV.setType(BSSApplicationConstants.WAIT_PERIOD_NONE);
		when(benefitsBundleService.getCustomBundleCreatedStatus("comIV", BSSApplicationConstants.CUSTOM)).thenReturn("YES");
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, BenExchngEnums.TRINET_IV)).thenReturn(companyIV);
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, BenExchngEnums.TRINET_XI)).thenReturn(companyXI);
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, TRINET_OMS)).thenReturn(companyOMS);
		when(strategyDataDao.getRealmPlanTypes(realmYearIdXI)).thenReturn(planTypesXI);
		when(strategyDataDao.getRealmPlanTypes(realmYearIdIV)).thenReturn(planTypes);
		when(strategyDataDao.getRealmPlanTypes(realmYearIdOMS)).thenReturn(planTypesOMS);

		// when
		List<ExchangeCarrierDto> actualResult = exchangeServiceImpl.getExchangeCarriers(companyCode, BenExchngEnums.getByExchangeId(EXCHANGE_ID));
		System.out.println("actualResult : " + actualResult);
		// then
		// assertions
		assertNotNull(actualResult);
		assertEquals(3, actualResult.size());
		// TNIV assertion
		ExchangeCarrierDto actualResult1 = actualResult.get(0);
		assertEquals("TNIV", actualResult1.getExchangeId());
		assertEquals("Exchange IV", actualResult1.getExchangeName());
		assertEquals("YES", actualResult1.getCustomBundleCreated());
		assertFalse(actualResult1.isStrategyCreated());
		assertTrue( actualResult1.isCarrierSelectionRequired() );
		assertTrue( actualResult1.isBenefitsStartDateValid() );
		List<CarrierDto> carriers1 = actualResult1.getCarriers();
		assertNotNull(carriers1);
		assertEquals(2, carriers1.size());
		CarrierDto carrierDto1 = carriers1.get(0);
		assertEquals(13L, carrierDto1.getPortfolioId());
		assertEquals("Tufts", carrierDto1.getPortfolioName());
		CarrierDto carrierDto2 = carriers1.get(1);
		assertEquals(21L, carrierDto2.getPortfolioId());
		assertEquals("BCBS ID", carrierDto2.getPortfolioName());
		// TNXI assertion
		ExchangeCarrierDto actualResult2 = actualResult.get(1);
		assertEquals("TNXI", actualResult2.getExchangeId());
		assertEquals("Exchange XI", actualResult2.getExchangeName());
		assertEquals("NA", actualResult2.getCustomBundleCreated());
		assertTrue(actualResult2.isStrategyCreated());
		assertFalse(actualResult2.isCarrierSelectionRequired() );
		assertTrue( actualResult2.isBenefitsStartDateValid() );
		List<CarrierDto> carriers2 = actualResult2.getCarriers();
		assertNotNull(carriers2);
		assertEquals(0, carriers2.size());
		// Trinet-OMS
		ExchangeCarrierDto actualResult3 = actualResult.get(2);
		assertEquals("OMS", actualResult3.getExchangeId());
		assertEquals("Exchange OMS", actualResult3.getExchangeName());
		assertEquals("NA", actualResult3.getCustomBundleCreated());
		assertTrue(actualResult3.isStrategyCreated());
		assertFalse(actualResult3.isCarrierSelectionRequired() );
		assertTrue( actualResult3.isBenefitsStartDateValid() );
		List<CarrierDto> carriers3 = actualResult3.getCarriers();
		assertNotNull(carriers3);
		assertEquals(0, carriers3.size());
		// verify
		verify(exchangeDaoMock, times(1)).getExchangeCarriers(companyCode, prospectInfoResponse.getHqState(),
				prospectInfoResponse.getZipCode(), benStartDate);
		verify(benefitsBundleService, times(1)).getCustomBundleCreatedStatus("comIV", BSSApplicationConstants.CUSTOM);
		verify(benefitsBundleService, times(0)).getCustomBundleCreatedStatus("comXI", BSSApplicationConstants.CUSTOM);
		verify(benefitsBundleService, times(0)).getCustomBundleCreatedStatus("comOMS", BSSApplicationConstants.CUSTOM);
	}

	@Test
	public void getExchangeCarriersWithNoRecordsTest() {
		// given
		String companyCode = "PROSPECT-COMPANY-CODE";
		ProspectInfoResponse prospectInfoResponse = prepareProspectCompany();
		Date denStartDate = Utils.convertStringToDate(prospectInfoResponse.getBenStartDate(),
				BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD);
		when(prospectCompanyService.getProspectBasicDetails(companyCode, BenExchngEnums.getByExchangeId(EXCHANGE_ID))).thenReturn(prepareProspectCompany());
		when(exchangeDaoMock.getExchangeCarriers(companyCode, prospectInfoResponse.getHqState(),
				prospectInfoResponse.getZipCode(), denStartDate)).thenReturn(Collections.emptyList());
		// when
		List<ExchangeCarrierDto> actualResult = exchangeServiceImpl.getExchangeCarriers(companyCode, BenExchngEnums.getByExchangeId(EXCHANGE_ID));
		// then
		// asserts
		assertNotNull(actualResult);
		assertEquals(0, actualResult.size());
		// verify
		verify(exchangeDaoMock, times(1)).getExchangeCarriers(companyCode, prospectInfoResponse.getHqState(),
				prospectInfoResponse.getZipCode(), denStartDate);
	}

	@Test
	public void getExchangeCarriersWithNoCompanyFound() {
		// given
		String companyCode = "PROSPECT-COMPANY-CODE";
		long realmYearIdXI = 77;
		long realmYearIdOMS = 85;
		Company companyXI = new Company();
		Company companyOMS = new Company();
		companyXI.setCode("comXI");
		companyXI.setLargeDealProspect(0);
		companyXI.setRealmPlanYearId(realmYearIdXI);
		companyOMS.setCode("comOMS");
		companyOMS.setLargeDealProspect(5);
		companyOMS.setRealmPlanYearId(realmYearIdOMS);
		Set<String> planTypesXI = new HashSet<>(Set.of("11", "14", "1D", "1V"));
		Set<String> planTypesOMS = new HashSet<>(Set.of("11", "14", "1D", "1V"));
		List<ExchangeCarrierDetailsDto> exchangeCarrierDetailsDtos = prepareExchangeCarrierDetailsDtos();
		ProspectInfoResponse prospectInfoResponse = prepareProspectCompany();
		Date benStartDate = Utils.convertStringToDate(prospectInfoResponse.getBenStartDate(),
				BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD);

		when(prospectCompanyService.getProspectBasicDetails(companyCode, BenExchngEnums.getByExchangeId(EXCHANGE_ID)))
				.thenReturn(prospectInfoResponse);
		when(exchangeDaoMock.getExchangeCarriers(companyCode, prospectInfoResponse.getHqState(),
				prospectInfoResponse.getZipCode(), benStartDate)).thenReturn(exchangeCarrierDetailsDtos);

		when(prospectCompanyService.getProspectCompanyDetails(companyCode, BenExchngEnums.TRINET_IV))
				.thenThrow(new BSSApplicationException());
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, BenExchngEnums.TRINET_XI))
				.thenReturn(companyXI);
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, TRINET_OMS)).thenReturn(companyOMS);
		when(strategyDataDao.getRealmPlanTypes(realmYearIdXI)).thenReturn(planTypesXI);
		when(strategyDataDao.getRealmPlanTypes(realmYearIdOMS)).thenReturn(planTypesOMS);

		// when
		List<ExchangeCarrierDto> actualResult = exchangeServiceImpl.getExchangeCarriers(companyCode,
				BenExchngEnums.getByExchangeId(EXCHANGE_ID));
		System.out.println("actualResult : " + actualResult);
		// then
		// assertions
		assertNotNull(actualResult);
		assertEquals(3, actualResult.size());
		// TNIV assertion
		ExchangeCarrierDto actualResult1 = actualResult.get(0);
		assertEquals("TNIV", actualResult1.getExchangeId());
		assertEquals("Exchange IV", actualResult1.getExchangeName());
		assertEquals("NA", actualResult1.getCustomBundleCreated());
		assertFalse(actualResult1.isStrategyCreated());
		assertFalse(actualResult1.isCarrierSelectionRequired());
		assertFalse(actualResult1.isBenefitsStartDateValid());
		List<CarrierDto> carriers1 = actualResult1.getCarriers();
		assertNotNull(carriers1);
		assertEquals(0, carriers1.size());
		// TNXI assertion
		ExchangeCarrierDto actualResult2 = actualResult.get(1);
		assertEquals("TNXI", actualResult2.getExchangeId());
		assertEquals("Exchange XI", actualResult2.getExchangeName());
		assertEquals("NA", actualResult2.getCustomBundleCreated());
		assertTrue(actualResult2.isStrategyCreated());
		assertFalse(actualResult2.isCarrierSelectionRequired());
		assertTrue(actualResult2.isBenefitsStartDateValid());
		List<CarrierDto> carriers2 = actualResult2.getCarriers();
		assertNotNull(carriers2);
		assertEquals(0, carriers2.size());
		// Trinet-OMS
		ExchangeCarrierDto actualResult3 = actualResult.get(2);
		assertEquals("OMS", actualResult3.getExchangeId());
		assertEquals("Exchange OMS", actualResult3.getExchangeName());
		assertEquals("NA", actualResult3.getCustomBundleCreated());
		assertTrue(actualResult3.isStrategyCreated());
		assertFalse(actualResult3.isCarrierSelectionRequired());
		assertTrue(actualResult3.isBenefitsStartDateValid());
		List<CarrierDto> carriers3 = actualResult3.getCarriers();
		assertNotNull(carriers3);
		assertEquals(0, carriers3.size());
		// verify
		verify(exchangeDaoMock, times(1)).getExchangeCarriers(companyCode, prospectInfoResponse.getHqState(),
				prospectInfoResponse.getZipCode(), benStartDate);
		verify(benefitsBundleService, times(0)).getCustomBundleCreatedStatus("comIV", BSSApplicationConstants.CUSTOM);
	}

	private List<ExchangeCarrierDetailsDto> prepareExchangeCarrierDetailsDtos() {
		List<ExchangeCarrierDetailsDto> exchangeCarrierDetailsDtos = new ArrayList<>();
		exchangeCarrierDetailsDtos.add(ExchangeCarrierDetailsDto.builder().realmId(1L).portfolioId(13L)
				.portfolioName("Tufts").strategyCreated(false).build());
		exchangeCarrierDetailsDtos.add(ExchangeCarrierDetailsDto.builder().realmId(1L).portfolioId(21L)
				.portfolioName("BCBS ID").strategyCreated(false).build());
		exchangeCarrierDetailsDtos.add(ExchangeCarrierDetailsDto.builder().realmId(2L).portfolioId(0L)
				.portfolioName(" ").strategyCreated(true).build());
		exchangeCarrierDetailsDtos.add(ExchangeCarrierDetailsDto.builder().realmId(6L).portfolioId(11L)
				.portfolioName(" ").strategyCreated(true).build());
		return exchangeCarrierDetailsDtos;
	}

	@Test
	public void getExchangeBands_givenTwoCompaniesThatBelongsToSameRealmWithTwoEffectiveDatesThenReturn1EchangeAndTwoEffectiveDatedBands() {
		// Given
		String companyCode = "PROSPECT-COMPANY-CODE";
		ProspectInfoResponse prospectInfoResponse = prepareProspectCompany();
		Date benStartDate = Utils.convertStringToDate(prospectInfoResponse.getBenStartDate(),
				BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD);
		when(prospectCompanyService.getProspectBasicDetails(companyCode, BenExchngEnums.getByExchangeId(EXCHANGE_ID))).thenReturn(prospectInfoResponse);
		when(exchangeDaoMock.getExchangeCarriersBands(companyCode, benStartDate))
				.thenReturn(prepareExchangeCarrierBands());
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.CARRIER_CODE_TO_DESC_MAPPING)).thenReturn(
				"AETNA,Aetna;AETNAHMO,Aetna HMO-SM;AETNAPPO,Aetna PPO-SM;BCBS,FL Blue;BCBSCA,BS of CA;BCBSID,BC of ID;BCBSMN,BCBS MN;BCBSNC,BCBS NC;DIABILITY,Disability;EMPIRENY,Empire;KAISER,Kaiser CA;KAISERCO,Kaiser CO/GA;KAISERHI,Kaiser HI;KAISERMD,Kaiser DC/MD/VA;KAISERNW,Kaiser NW/WA;LIFE,Life;TUFFS,Tufts;UHC,UHC");
		// When
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.getExchangeBands(companyCode, BenExchngEnums.getByExchangeId(EXCHANGE_ID));
//		List.of("BCBS", "BCBSID", "LIFE", "DIABILITY"
		// Then
		assertEquals(1, actualResult.size());
		assertEquals(2, actualResult.get(0).getBands().size());
		assertEquals("TNIII", actualResult.get(0).getExchangeId());
		assertEquals("Exchange III", actualResult.get(0).getExchangeName());
		assertEquals(CommonUtils.formatStringToDate("17-APR-2023", "dd-MMM-yyyy"),
				actualResult.get(0).getBands().get(0).getEffectiveDate());
		assertEquals("primary", actualResult.get(0).getBands().get(0).getBandType());
		assertEquals(7, actualResult.get(0).getBands().get(0).getCarrierBands().size());
		assertEquals("Aetna", actualResult.get(0).getBands().get(0).getCarrierBands().get(0).getCarrier());
		assertEquals("21", actualResult.get(0).getBands().get(0).getCarrierBands().get(0).getBandCode());
		assertEquals("Aetna HMO-SM", actualResult.get(0).getBands().get(0).getCarrierBands().get(1).getCarrier());
		assertEquals("21", actualResult.get(0).getBands().get(0).getCarrierBands().get(1).getBandCode());
		assertEquals("Aetna PPO-SM", actualResult.get(0).getBands().get(0).getCarrierBands().get(2).getCarrier());
		assertEquals("21", actualResult.get(0).getBands().get(0).getCarrierBands().get(2).getBandCode());
		assertEquals("FL Blue", actualResult.get(0).getBands().get(0).getCarrierBands().get(3).getCarrier());
		assertEquals("", actualResult.get(0).getBands().get(0).getCarrierBands().get(3).getBandCode());
		assertEquals("Tufts", actualResult.get(0).getBands().get(0).getCarrierBands().get(4).getCarrier());
		assertEquals("19A", actualResult.get(0).getBands().get(0).getCarrierBands().get(4).getBandCode());
		assertEquals("Life", actualResult.get(0).getBands().get(0).getCarrierBands().get(5).getCarrier());
		assertEquals("3", actualResult.get(0).getBands().get(0).getCarrierBands().get(5).getBandCode());
		assertEquals("Disability", actualResult.get(0).getBands().get(0).getCarrierBands().get(6).getCarrier());
		assertEquals("2", actualResult.get(0).getBands().get(0).getCarrierBands().get(6).getBandCode());

		assertEquals(CommonUtils.formatStringToDate("10-JUL-2023", "dd-MMM-yyyy"),
				actualResult.get(0).getBands().get(1).getEffectiveDate());
		assertEquals("alternate", actualResult.get(0).getBands().get(1).getBandType());
		assertEquals(7, actualResult.get(0).getBands().get(1).getCarrierBands().size());
		assertEquals("Aetna", actualResult.get(0).getBands().get(1).getCarrierBands().get(0).getCarrier());
		assertEquals("20", actualResult.get(0).getBands().get(1).getCarrierBands().get(0).getBandCode());
		assertEquals("Aetna HMO-SM", actualResult.get(0).getBands().get(1).getCarrierBands().get(1).getCarrier());
		assertEquals("20", actualResult.get(0).getBands().get(1).getCarrierBands().get(1).getBandCode());
		assertEquals("Aetna PPO-SM", actualResult.get(0).getBands().get(1).getCarrierBands().get(2).getCarrier());
		assertEquals("20", actualResult.get(0).getBands().get(1).getCarrierBands().get(2).getBandCode());
		assertEquals("BC of ID", actualResult.get(0).getBands().get(1).getCarrierBands().get(3).getCarrier());
		assertEquals("18A", actualResult.get(0).getBands().get(1).getCarrierBands().get(3).getBandCode());
		assertEquals("FL Blue", actualResult.get(0).getBands().get(1).getCarrierBands().get(4).getCarrier());
		assertEquals("", actualResult.get(0).getBands().get(1).getCarrierBands().get(4).getBandCode());
		assertEquals("Life", actualResult.get(0).getBands().get(1).getCarrierBands().get(5).getCarrier());
		assertEquals("2", actualResult.get(0).getBands().get(1).getCarrierBands().get(5).getBandCode());
		assertEquals("Disability", actualResult.get(0).getBands().get(1).getCarrierBands().get(6).getCarrier());
		assertEquals("1", actualResult.get(0).getBands().get(1).getCarrierBands().get(6).getBandCode());
	}

	@Test(expected = RuntimeException.class)
	public void getExchangeBands_forCompanyCodeWithNoCompanies() {
		// Given
		String companyCode = "D2S";
		when(prospectCompanyService.getProspectBasicDetails(companyCode, BenExchngEnums.getByExchangeId(EXCHANGE_ID))).thenReturn(null);
		// When
		exchangeServiceImpl.getExchangeBands(companyCode, BenExchngEnums.getByExchangeId(EXCHANGE_ID));
		// Then
		verify(prospectCompanyService, times(1)).getProspectBasicDetails(companyCode, BenExchngEnums.getByExchangeId(EXCHANGE_ID));
	}

	private List<ExchangeCarrierBandDto> prepareExchangeCarrierBands() {
		List<ExchangeCarrierBandDto> dtos = new ArrayList<>();
		ExchangeCarrierBandDto dto = ExchangeCarrierBandDto.builder().realmId(3)
				.effectiveDt(CommonUtils.formatStringToDate("10-JUL-2023", "dd-MMM-yyyy")).companyId(11111)
				.carrierCode(List.of("BCBS", "BCBSID", "LIFE", "DIABILITY", "AETNA", "AETNAPPO", "AETNAHMO"))
				.bandCodeValue(List.of(" ", "18A", "2", "1", "20", "20", "20")).build();
		dtos.add(dto);
		dto = ExchangeCarrierBandDto.builder().realmId(3)
				.effectiveDt(CommonUtils.formatStringToDate("17-APR-2023", "dd-MMM-yyyy")).companyId(222222)
				.carrierCode(List.of("BCBS", "TUFFS", "LIFE", "DIABILITY", "AETNA", "AETNAPPO", "AETNAHMO"))
				.bandCodeValue(List.of(" ", "19A", "3", "2", "21", "21", "21")).build();
		dtos.add(dto);

		return dtos;
	}

	/**
	 * given bands for TriNet I exchange </br>
	 * when saveExchangeBands method is called </br>
	 * then return create company for TriNet I exchange and save bands</br>
	 **/
	@Test
	public void saveExchangeBandsTest1() {
		// given
		// data
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetI();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetI("423720");
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(false);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
				.thenReturn(company);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture(),
				omsOfferingCaptor.capture(), bundleIdCaptor.capture(), riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());

		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet I", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		CarrierBand carrierBand = bands.get(0);
		assertEquals("2024-01-01", CommonUtils.formatDateToString(carrierBand.getEffectiveDate(), "yyyy-MM-dd"));
		assertEquals(COMPANY_ID, carrierBand.getCompanyId());
		assertEquals("AC", carrierBand.getOeQuarter());
		// verify
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		verify(companyService, times(1)).findCompanyBy(companyCodeCaptor.getValue(), realmPlanYearIdCaptor.getValue());
		verify(companyService, times(1)).createUpdateCompany(company, companyCodeCaptor.getValue(),
			realmPlanYearIdCaptor.getValue(), omsOfferingCaptor.getValue(), bundleIdCaptor.getValue(), riskTypeCaptor.getValue(), naicsCodeCaptor.getValue());
		verify(bandCodesService, times(1)).save(carrierBandCaptor.getValue(), companyIdCaptor.getValue());
		verify(processStatusService, times(0)).createStrategySyncProcess(anyString(), anyString(), anyString(),
				anyString());
		verify(strategySyncService, times(1)).syncStrategiesForCompany(anyString(), any(BenExchngEnums.class), any());
	}

	/**
	 * given bands for TriNet I exchange and strategy queuing is enabled </br>
	 * when saveExchangeBands method is called </br>
	 * then return create company for TriNet I exchange and save bands, queue strategy sync</br>
	 **/
	@Test
	public void saveExchangeBandsTest1a() {
		// given
		// data
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetI();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetI("423720");
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
		.thenReturn(company);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture(),
				omsOfferingCaptor.capture(), bundleIdCaptor.capture(), riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());

		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet I", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		CarrierBand carrierBand = bands.get(0);
		assertEquals("2024-01-01", CommonUtils.formatDateToString(carrierBand.getEffectiveDate(), "yyyy-MM-dd"));
		assertEquals(COMPANY_ID, carrierBand.getCompanyId());
		assertEquals("AC", carrierBand.getOeQuarter());
		// verify
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		verify(companyService, times(1)).findCompanyBy(companyCodeCaptor.getValue(), realmPlanYearIdCaptor.getValue());
		verify(companyService, times(1)).createUpdateCompany(company, companyCodeCaptor.getValue(),
				realmPlanYearIdCaptor.getValue(), omsOfferingCaptor.getValue(), bundleIdCaptor.getValue(), riskTypeCaptor.getValue(), naicsCodeCaptor.getValue());
		verify(bandCodesService, times(1)).save(carrierBandCaptor.getValue(), companyIdCaptor.getValue());
		verify(processStatusService, times(1)).createBandUpdateProcess(
				exchangeIdCaptor.getValue(),
				companyCodeCaptor.getValue(),
				companyIdCaptor.getValue()
		);
		verify(strategySyncService, times(0)).syncStrategiesForCompany(anyString(), any(BenExchngEnums.class),  any());
	}

	/**
	 * given bands for TriNet II exchange with benefitsQuarterException as false</br>
	 * when saveExchangeBands method is called </br>
	 * then return create company for TriNet II exchange and save bands</br>
	 **/
	@Test
	public void saveExchangeBandsTest2() {
		// given
		// data
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetII();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetII();
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(false);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
		.thenReturn(company);
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture(),
				omsOfferingCaptor.capture(), bundleIdCaptor.capture(), riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet II", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		CarrierBand carrierBand = bands.get(0);
		assertEquals("2024-04-01", CommonUtils.formatDateToString(carrierBand.getEffectiveDate(), "yyyy-MM-dd"));
		assertEquals(COMPANY_ID, carrierBand.getCompanyId());
		assertEquals("SY", carrierBand.getOeQuarter());
		// verify
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		verify(companyService, times(1)).findCompanyBy(companyCodeCaptor.getValue(), realmPlanYearIdCaptor.getValue());
		verify(companyService, times(1)).createUpdateCompany(company, companyCodeCaptor.getValue(),
				realmPlanYearIdCaptor.getValue(), omsOfferingCaptor.getValue(), bundleIdCaptor.getValue(), riskTypeCaptor.getValue(), naicsCodeCaptor.getValue());
		verify(bandCodesService, times(1)).save(carrierBandCaptor.getValue(), companyIdCaptor.getValue());
	}

	/**
	 * given bands for TriNet IV exchange with benefitsQuarterException as false </br>
	 * when saveExchangeBands method is called </br>
	 * then return create company for TriNet IV exchange and save bands and add entry to process_status with PROSPECT_BAND_UPDATE_EVENT</br>
	 **/
	@Test
	public void saveExchangeBandsTest3() {
		// given
		// data
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIV();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetIV();
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
		.thenReturn(company);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture(),
				omsOfferingCaptor.capture(), bundleIdCaptor.capture(), riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet IV", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		CarrierBand carrierBand = bands.get(0);
		assertEquals("2024-10-01", CommonUtils.formatDateToString(carrierBand.getEffectiveDate(), "yyyy-MM-dd"));
		assertEquals(COMPANY_ID, carrierBand.getCompanyId());
		assertEquals("8Y", carrierBand.getOeQuarter());
		// verify
		verify(processStatusService, times(1)).createBandUpdateProcess(
				exchangeIdCaptor.getValue(),
				companyCodeCaptor.getValue(),
				companyIdCaptor.getValue()
		);
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		verify(companyService, times(1)).findCompanyBy(companyCodeCaptor.getValue(), realmPlanYearIdCaptor.getValue());
		verify(companyService, times(1)).createUpdateCompany(company, companyCodeCaptor.getValue(),
				realmPlanYearIdCaptor.getValue(), omsOfferingCaptor.getValue(), bundleIdCaptor.getValue(), riskTypeCaptor.getValue(), naicsCodeCaptor.getValue());
		verify(bandCodesService, times(1)).save(carrierBandCaptor.getValue(), companyIdCaptor.getValue());
	}

	/**
	 * given bands for TriNet XI exchange with benefitsQuarterException as false</br>
	 * when saveExchangeBands method is called </br>
	 * then return create company for TriNet XI exchange and save bands and add entry to process_status with PROSPECT_STRATEGY_SYNC_PLYR_CHANGE</br>
	 **/
	@Test
	public void saveExchangeBandsTest4() {
		// given
		// data
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetXI();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetXI();
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
		.thenReturn(company);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture(),
				omsOfferingCaptor.capture(), bundleIdCaptor.capture(), riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet XI", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		CarrierBand carrierBand = bands.get(0);
		assertEquals("2024-01-01", CommonUtils.formatDateToString(carrierBand.getEffectiveDate(), "yyyy-MM-dd"));
		assertEquals(COMPANY_ID, carrierBand.getCompanyId());
		assertEquals("AL", carrierBand.getOeQuarter());
		// verify
		verify(processStatusService, times(1)).createBandUpdateProcess(
				exchangeIdCaptor.getValue(),
				companyCodeCaptor.getValue(),
				companyIdCaptor.getValue()
		);
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		verify(companyService, times(1)).findCompanyBy(companyCodeCaptor.getValue(), realmPlanYearIdCaptor.getValue());
		verify(companyService, times(1)).createUpdateCompany(company, companyCodeCaptor.getValue(),
				realmPlanYearIdCaptor.getValue(), omsOfferingCaptor.getValue(), bundleIdCaptor.getValue(), riskTypeCaptor.getValue(), naicsCodeCaptor.getValue());
		verify(bandCodesService, times(1)).save(carrierBandCaptor.getValue(), companyIdCaptor.getValue());
	}

	/**
	 * given bands for TriNet III exchange </br>
	 * when saveExchangeBands method is called </br>
	 * then return create company for TriNet III exchange and save bands</br>
	 **/
	@Test
	public void saveExchangeBandsTest5() {
		// given
		// data
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetIII();
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(false);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
		.thenReturn(company);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture(),
				omsOfferingCaptor.capture(), bundleIdCaptor.capture(), riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID).thenReturn(COMPANY_ID + 1);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet III", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(2, bands.size());
		CarrierBand carrierBand1 = bands.get(0);
		assertEquals("2024-01-01", CommonUtils.formatDateToString(carrierBand1.getEffectiveDate(), "yyyy-MM-dd"));
		assertEquals(COMPANY_ID, carrierBand1.getCompanyId());
		assertEquals("Q1", carrierBand1.getOeQuarter());
		assertEquals("Q1", carrierBand1.getOeQuarter());
		CarrierBand carrierBand2 = bands.get(1);
		assertEquals("2024-04-01", CommonUtils.formatDateToString(carrierBand2.getEffectiveDate(), "yyyy-MM-dd"));
		assertEquals(COMPANY_ID + 1, carrierBand2.getCompanyId());
		assertEquals("Q2", carrierBand2.getOeQuarter());
		// verify
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		List<String> companyCodeCaptors = companyCodeCaptor.getAllValues();
		List<Long> realmPlanYearIdCaptors = realmPlanYearIdCaptor.getAllValues();
		List<CarrierBand> carrierBandCaptors = carrierBandCaptor.getAllValues();
		List<Long> companyIdCaptors = companyIdCaptor.getAllValues();
		List<String> omsOfferingCaptors = omsOfferingCaptor.getAllValues();
		List<Long> bundleIdCaptors = bundleIdCaptor.getAllValues();
		List<Company> companyCaptors = companyCaptor.getAllValues();
		List<RiskTypeEnum> riskTypeCaptors = riskTypeCaptor.getAllValues();
		List<Integer> naicsCodeCaptors = naicsCodeCaptor.getAllValues();
		// verify
		verify(processStatusService, times(0)).createStrategySyncProcess(anyString(), anyString(), anyString(),
				anyString());
		verify(companyService, times(1)).findCompanyBy(companyCodeCaptor.getValue(), realmPlanYearIdCaptor.getValue());
		verify(companyService, times(1)).createUpdateCompany(companyCaptors.get(0), companyCodeCaptors.get(0),
				realmPlanYearIdCaptors.get(0),omsOfferingCaptors.get(0), bundleIdCaptors.get(0), riskTypeCaptors.get(0), naicsCodeCaptors.get(0));
		verify(bandCodesService, times(1)).save(carrierBandCaptors.get(0), companyIdCaptors.get(0));
		verify(companyService, times(1)).createUpdateCompany(companyCaptors.get(1), companyCodeCaptors.get(1),
				realmPlanYearIdCaptors.get(1),omsOfferingCaptors.get(1), bundleIdCaptors.get(1), riskTypeCaptors.get(0), naicsCodeCaptors.get(0));
		verify(bandCodesService, times(1)).save(carrierBandCaptors.get(1), companyIdCaptors.get(1));
	}
	
	/**
	 * given bands for TriNet III exchange </br>
	 * when saveExchangeBands method is called </br>
	 * then test if the carrier bands are getting transformed correctly</br>
	 **/
	@Test
	public void saveExchangeBandsTest6() {
		// given
		// data
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoCarrierBandTest();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosForCarrierBandTest();
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
		.thenReturn(company);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture(),
				omsOfferingCaptor.capture(), bundleIdCaptor.capture(), riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID).thenReturn(COMPANY_ID + 1);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet III", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		List<CarrierBandDetails> carrierBandDetails = bands.get(0).getCarrierBands();
		assertEquals(19, carrierBandDetails.size());
		assertEquals("Q1", bands.get(0).getOeQuarter());

		assertEquals("UHC", carrierBandDetails.get(0).getCarrier());
		assertEquals("20A", carrierBandDetails.get(0).getBandCode());
		assertEquals("TUFFS", carrierBandDetails.get(1).getCarrier());
		assertEquals("12A", carrierBandDetails.get(1).getBandCode());
		assertEquals("KAISERNW", carrierBandDetails.get(2).getCarrier());
		assertEquals("20A", carrierBandDetails.get(2).getBandCode());
		assertEquals("KAISERMD", carrierBandDetails.get(3).getCarrier());
		assertEquals("12B", carrierBandDetails.get(3).getBandCode());
		assertEquals("KAISERHI", carrierBandDetails.get(4).getCarrier());
		assertEquals(" ", carrierBandDetails.get(4).getBandCode());
		assertEquals("KAISERCO", carrierBandDetails.get(5).getCarrier());
		assertEquals("12C", carrierBandDetails.get(5).getBandCode());
		assertEquals("KAISER", carrierBandDetails.get(6).getCarrier());
		assertEquals("20A", carrierBandDetails.get(6).getBandCode());
		assertEquals("HARVARD", carrierBandDetails.get(7).getCarrier());
		assertEquals("12D", carrierBandDetails.get(7).getBandCode());
		assertEquals("EMPIRENY", carrierBandDetails.get(8).getCarrier());
		assertEquals(" ", carrierBandDetails.get(8).getBandCode());
		assertEquals("BCBSNC", carrierBandDetails.get(9).getCarrier());
		assertEquals("12A", carrierBandDetails.get(9).getBandCode());
		assertEquals("BCBSMN", carrierBandDetails.get(10).getCarrier());
		assertEquals("25A", carrierBandDetails.get(10).getBandCode());
		assertEquals("BCBSID", carrierBandDetails.get(11).getCarrier());
		assertEquals("15A", carrierBandDetails.get(11).getBandCode());
		assertEquals("BCBSCA", carrierBandDetails.get(12).getCarrier());
		assertEquals("23A", carrierBandDetails.get(12).getBandCode());
		assertEquals("BCBS", carrierBandDetails.get(13).getCarrier());
		assertEquals(" ", carrierBandDetails.get(13).getBandCode());
		assertEquals("AETNA", carrierBandDetails.get(14).getCarrier());
		assertEquals("20A", carrierBandDetails.get(14).getBandCode());
		assertEquals("AETNAPPO", carrierBandDetails.get(15).getCarrier());
		assertEquals("20A", carrierBandDetails.get(15).getBandCode());
		assertEquals("AETNAHMO", carrierBandDetails.get(16).getCarrier());
		assertEquals("20A", carrierBandDetails.get(16).getBandCode());
		assertEquals("LIFE", carrierBandDetails.get(17).getCarrier());
		assertEquals("1", carrierBandDetails.get(17).getBandCode());
		assertEquals("DIABILITY", carrierBandDetails.get(18).getCarrier());
		assertEquals("4", carrierBandDetails.get(18).getBandCode());
		// verify
		verify(processStatusService, times(1)).createBandUpdateProcess(
				exchangeIdCaptor.getValue(),
				companyCodeCaptor.getValue(),
				companyIdCaptor.getValue()
		);
	}
	
	/**
	 * given bands for TriNet III exchange with Anthem </br>
	 * when saveExchangeBands method is called </br>
	 * then test if the carrier bands are getting transformed correctly and Anthem is replaced with EMPIRENY</br>
	 **/
	@Test
	public void saveExchangeBandsTest7() {
		// given
		// data
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoCarrierBandTest();
		List<ExchangeBandsDto> exchangeBandsDtos = new ArrayList<>();
		exchangeBandsDtos
				.add(ExchangeBandsDto.builder().exchangeId("TriNet III")
						.benefitsStartDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
						.oldBenefitsStartDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
						.bands(Arrays.asList(CarrierBand.builder()
								.effectiveDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
								.carrierBands(Arrays.asList(CarrierBandDetails.builder().carrier("Anthem")
										.bandCode("4").buildCarrierBandDetails()))
								.buildCarrierBand()))
						.riskType(RiskTypeEnum.BANDS)
						.naicsCode("423720")
						.buildExchangeBands());
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
		.thenReturn(company);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture(),
				omsOfferingCaptor.capture(), bundleIdCaptor.capture(), riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID).thenReturn(COMPANY_ID + 1);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);

		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);

		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet III", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		List<CarrierBandDetails> carrierBandDetails = bands.get(0).getCarrierBands();
		assertEquals(1, carrierBandDetails.size());
		assertEquals("EMPIRENY", carrierBandDetails.get(0).getCarrier());
		assertEquals("4", carrierBandDetails.get(0).getBandCode());
		// verify
		verify(processStatusService, times(1)).createBandUpdateProcess(
				exchangeIdCaptor.getValue(),
				companyCodeCaptor.getValue(),
				companyIdCaptor.getValue()
		);
	}
	
	/**
	 * Given bands for the TriNet III exchange with plan year change enabled, and both old and new
	 * benefit quarter exceptions set to false, and old and new benefit start dates are in different
	 * quarters.<br> When the saveExchangeBands method is called,<br> Then a company is created for
	 * the TriNet III exchange and the plan year change event should be triggered.<br>
	 **/
	@Test
	public void saveExchangeBandsTest8() {
		// given
		// data
		when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(true);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetIII();
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(false);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
		.thenReturn(company);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture(),
				omsOfferingCaptor.capture(), bundleIdCaptor.capture(), riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID).thenReturn(COMPANY_ID + 1);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet III", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(2, bands.size());
		CarrierBand carrierBand1 = bands.get(0);
		assertEquals("2024-01-01", CommonUtils.formatDateToString(carrierBand1.getEffectiveDate(), "yyyy-MM-dd"));
		assertEquals(COMPANY_ID, carrierBand1.getCompanyId());
		assertEquals("Q1", carrierBand1.getOeQuarter());
		assertEquals("Q1", carrierBand1.getOeQuarter());
		CarrierBand carrierBand2 = bands.get(1);
		assertEquals("2024-04-01", CommonUtils.formatDateToString(carrierBand2.getEffectiveDate(), "yyyy-MM-dd"));
		assertEquals(COMPANY_ID + 1, carrierBand2.getCompanyId());
		assertEquals("Q2", carrierBand2.getOeQuarter());
		// verify
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		List<String> companyCodeCaptors = companyCodeCaptor.getAllValues();
		List<Long> realmPlanYearIdCaptors = realmPlanYearIdCaptor.getAllValues();
		List<CarrierBand> carrierBandCaptors = carrierBandCaptor.getAllValues();
		List<Long> companyIdCaptors = companyIdCaptor.getAllValues();
		List<String> omsOfferingCaptors = omsOfferingCaptor.getAllValues();
		List<Long> bundleIdCaptors = bundleIdCaptor.getAllValues();
		List<Company> companyCaptors = companyCaptor.getAllValues();
		List<RiskTypeEnum> riskTypeCaptors = riskTypeCaptor.getAllValues();
		List<Integer> naicsCodeCaptors = naicsCodeCaptor.getAllValues();
		// verify
		verify(processStatusService, times(1)).createStrategySyncProcess(anyString(), anyString(), anyString(),
				anyString());
		verify(companyService, times(2)).findCompanyBy(companyCodeCaptor.getValue(), realmPlanYearIdCaptor.getValue());
		verify(companyService, times(1)).createUpdateCompany(companyCaptors.get(0), companyCodeCaptors.get(0),
				realmPlanYearIdCaptors.get(0),omsOfferingCaptors.get(0),bundleIdCaptors.get(0), riskTypeCaptors.get(0), naicsCodeCaptors.get(0));
		verify(bandCodesService, times(1)).save(carrierBandCaptors.get(0), companyIdCaptors.get(0));
		verify(companyService, times(1)).createUpdateCompany(companyCaptors.get(1), companyCodeCaptors.get(1),
				realmPlanYearIdCaptors.get(1),omsOfferingCaptors.get(1),bundleIdCaptors.get(1), riskTypeCaptors.get(0), naicsCodeCaptors.get(1));
		verify(bandCodesService, times(1)).save(carrierBandCaptors.get(1), companyIdCaptors.get(1));
	}

	/**
	 * Given bands for the TriNet III exchange with plan year change enabled, with quarter exception
	 * and old quarter exception set to false. </br> When the saveExchangeBands method is
	 * called,<br> Then a company is created for the TriNet III exchange and the plan year change
	 * event should be triggered.<br>
	 **/
	@Test
	public void saveExchangeBandsTest9() {
		// given
		// data
		when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(true);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetIIIWithQuarterException2();
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
		.thenReturn(company);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture(),
				omsOfferingCaptor.capture(), bundleIdCaptor.capture(), riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID).thenReturn(COMPANY_ID + 1);
		when(companyBandCodesDao.getProspectBandEffDate(anyString(),anyLong())).thenReturn(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"));
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		doNothing().when(processStatusService).createStrategySyncProcess(companyCodeCaptor.capture(), anyString(), anyString(), anyString());
		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet III", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		CarrierBand carrierBand1 = bands.get(0);
		assertEquals(COMPANY_ID, carrierBand1.getCompanyId());
		assertEquals("Q2", carrierBand1.getOeQuarter());
		
		// verify
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		List<String> companyCodeCaptors = companyCodeCaptor.getAllValues();
		List<Long> realmPlanYearIdCaptors = realmPlanYearIdCaptor.getAllValues();
		List<CarrierBand> carrierBandCaptors = carrierBandCaptor.getAllValues();
		List<Long> companyIdCaptors = companyIdCaptor.getAllValues();
		List<String> omsOfferingCaptors = omsOfferingCaptor.getAllValues();
		List<Long> bundleIdCaptors = bundleIdCaptor.getAllValues();
		List<Company> companyCaptors = companyCaptor.getAllValues();
		List<RiskTypeEnum> riskTypeCaptors = riskTypeCaptor.getAllValues();
		List<Integer> naicsCodeCaptors = naicsCodeCaptor.getAllValues();
		// verify
		verify(processStatusService, times(1)).createStrategySyncProcess(anyString(), anyString(), anyString(),
				anyString());
		verify(companyService, times(1)).findCompanyBy(companyCodeCaptor.getValue(), realmPlanYearIdCaptor.getValue());
		verify(companyService, times(1)).createUpdateCompany(companyCaptors.get(0), companyCodeCaptors.get(0),
				realmPlanYearIdCaptors.get(0),omsOfferingCaptors.get(0),bundleIdCaptors.get(0), riskTypeCaptors.get(0), naicsCodeCaptors.get(0));
		verify(bandCodesService, times(1)).save(carrierBandCaptors.get(0), companyIdCaptors.get(0));
	}


	/**
	 * Given bands for the TriNet III exchange with plan year change enabled, with quarter exception
	 * and old quarter exception set to false. </br> When the saveExchangeBands method is
	 * called,<br> Then a company is created for the TriNet III exchange and the plan year change
	 * event should be triggered.<br>
	 **/
	@Test
	public void saveExchangeBandsTest91() {
		// given
		// data
		when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(true);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetIIIWithQuarterException2();
		exchangeBandsDtos.get(0).setOldBenefitsQuarterException(true);
		exchangeBandsDtos.get(0).setOldQuarterEffectiveDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"));
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
				.thenReturn(company);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture(),
				omsOfferingCaptor.capture(), bundleIdCaptor.capture(), riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID).thenReturn(COMPANY_ID + 1);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		doNothing().when(processStatusService).createStrategySyncProcess(companyCodeCaptor.capture(), anyString(), anyString(), anyString());
		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet III", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		CarrierBand carrierBand1 = bands.get(0);
		assertEquals(COMPANY_ID, carrierBand1.getCompanyId());
		assertEquals("Q2", carrierBand1.getOeQuarter());

		// verify
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		List<String> companyCodeCaptors = companyCodeCaptor.getAllValues();
		List<Long> realmPlanYearIdCaptors = realmPlanYearIdCaptor.getAllValues();
		List<CarrierBand> carrierBandCaptors = carrierBandCaptor.getAllValues();
		List<Long> companyIdCaptors = companyIdCaptor.getAllValues();
		List<String> omsOfferingCaptors = omsOfferingCaptor.getAllValues();
		List<Long> bundleIdCaptors = bundleIdCaptor.getAllValues();
		List<Company> companyCaptors = companyCaptor.getAllValues();
		List<RiskTypeEnum> riskTypeCaptors = riskTypeCaptor.getAllValues();
		List<Integer> naicsCodeCaptors = naicsCodeCaptor.getAllValues();
		// verify
		verify(processStatusService, times(1)).createStrategySyncProcess(anyString(), anyString(), anyString(),
				anyString());
		verify(companyService, times(1)).findCompanyBy(companyCodeCaptor.getValue(), realmPlanYearIdCaptor.getValue());
		verify(companyService, times(1)).createUpdateCompany(companyCaptors.get(0), companyCodeCaptors.get(0),
				realmPlanYearIdCaptors.get(0),omsOfferingCaptors.get(0),bundleIdCaptors.get(0), riskTypeCaptors.get(0), naicsCodeCaptors.get(0));
		verify(bandCodesService, times(1)).save(carrierBandCaptors.get(0), companyIdCaptors.get(0));
	}

	/**
	 * given bands for TriNet III exchange </br>
	 * when saveExchangeBands method is called </br>
	 * then reset the strategies if company found for TriNet III exchange for given
	 * plan year and save bands</br>
	 **/
	@Test
	public void saveExchangeBandsTest10() {
		// given
		// data
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();
		List<ExchangeBandsDto> exchangeBandsDtos = new ArrayList<>();
		exchangeBandsDtos
				.add(ExchangeBandsDto.builder().exchangeId("TriNet III")
						.benefitsStartDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
						.oldBenefitsStartDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
						.bundleId("1")
						.bands(Arrays.asList(CarrierBand.builder()
								.effectiveDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
								.carrierBands(Arrays.asList(CarrierBandDetails.builder().carrier("Anthem")
										.bandCode("4").buildCarrierBandDetails()))
								.buildCarrierBand()))
						.riskType(RiskTypeEnum.BANDS)
						.naicsCode("423720")
						.buildExchangeBands());
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(false);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture(),
				omsOfferingCaptor.capture(), bundleIdCaptor.capture(), riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		Company company = new Company();
		company.setRealmPlanYearId(68);
		company.setId(COMPANY_ID);
		company.setCode(COMPANY_CODE);
		when(companyService.findCompanyBy(COMPANY_CODE, 68L)).thenReturn(company);
		doNothing().when(prospectStrategySyncService).resetStrategiesBy(company, 68L);
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet III", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		CarrierBand carrierBand1 = bands.get(0);
		assertEquals("2024-01-01", CommonUtils.formatDateToString(carrierBand1.getEffectiveDate(), "yyyy-MM-dd"));
		assertEquals(COMPANY_ID, carrierBand1.getCompanyId());
		assertEquals("Q1", carrierBand1.getOeQuarter());
		// verify
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		verify(companyService, times(1)).findCompanyBy(COMPANY_CODE, 68L);
		verify(prospectStrategySyncService, times(1)).resetStrategiesBy(company, 68L);
		verify(processStatusService, times(0)).createStrategySyncProcess(anyString(), anyString(), anyString(),
				anyString());
		verify(companyService, times(1)).createUpdateCompany(company, companyCodeCaptor.getValue(),
				realmPlanYearIdCaptor.getValue(), omsOfferingCaptor.getValue(), bundleIdCaptor.getValue(), riskTypeCaptor.getValue(), naicsCodeCaptor.getValue());
		verify(bandCodesService, times(1)).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
	}

	/**
	 * given bands for TriNet OMS exchange with valid Oms Offering </br>
	 * when saveExchangeBands method is called </br>
	 * then return create company for TriNet OMS exchange and save bands</br>
	 **/
	@Test
	public void saveExchangeBandsTest11() {

		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsForTrinetOMS(OMB_TLD);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsForTrinetOMS();
		Company company = buildCompany();
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);

		mockServiceCalls(company, realmPlanYearDetailsDtos);

		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		assertSuccessfulResponseForOMS(actualResult);
	}

	/**
	 * given bands for TriNet OMS exchange with null Oms Offering </br>
	 * when saveExchangeBands method is called </br>
	 * then should throw InvalidOMSOfferingException</br>
	 **/
	@Test(expected = InvalidOmsOfferingException.class)
	public void saveExchangeBandsTest12() {

		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsForTrinetOMS(null);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsForTrinetOMS();
		Company company = buildCompany();

		mockServiceCalls(company, realmPlanYearDetailsDtos);

		exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
	}

	/**
	 * given bands for TriNet OMS exchange with null Oms Offering </br>
	 * when saveExchangeBands method is called </br>
	 * then should throw InvalidOMSOfferingException</br>
	 **/
	@Test(expected = InvalidOmsOfferingException.class)
	public void saveExchangeBandsTest13() {

		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetIII();
		exchangeBandsDtos.forEach(dto -> dto.setOmsOffering(OM_OD_OV_TLD));

		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();
		Company company = buildCompany();

		mockServiceCalls(company, realmPlanYearDetailsDtos);

		exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
	}

	@Test
	public void saveExchangeBands_shouldNotCallSaveForDifferentialsRiskType() {
		// Arrange
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetI();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetI("423720");
		exchangeBandsDtos.forEach(dto -> dto.setRiskType(RiskTypeEnum.DIFFERENTIALS));
		Company company = buildCompany();

		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(false);
		when(realmPlanYearService.findByRealmId(anyLong())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(anyString(), anyLong())).thenReturn(company);
		when(companyService.createUpdateCompany(any(), anyString(), anyLong(), any(), any(), eq(RiskTypeEnum.DIFFERENTIALS), any()))
				.thenReturn(company.getId());

		// Act
		exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);

		// Assert
		verify(bandCodesService, org.mockito.Mockito.never()).save(any(), anyLong());
	}
	
	/**
	 * Scenario 1: Plan year changed and risk type is BANDS
	 * Given the plan year has changed and the exchange band risk type is BANDS
	 * When createStrategySyncEvent is executed
	 * Then a STRATEGY_SYNC_PLYR_CHANGE process status is created
	 */
	@Test
	public void createStrategySyncEvent_planYearChanged_riskTypeBands_createsStrategySyncPlyrChange() {
		// given
		when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(true);
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetIII();
		// ensure BANDS risk type
		exchangeBandsDtos.forEach(dto -> dto.setRiskType(RiskTypeEnum.BANDS));
		Company company = buildCompany();
		when(realmPlanYearService.findByRealmId(anyLong())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(anyString(), anyLong())).thenReturn(company);
		when(companyService.createUpdateCompany(any(), anyString(), anyLong(), any(), any(), any(), any())).thenReturn(COMPANY_ID);
		doNothing().when(bandCodesService).save(any(), anyLong());
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		// when
		exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		verify(processStatusService, times(1)).createStrategySyncProcess(
				anyString(),
				anyString(),
				eq(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName()),
				eq(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getIdentifierName()));
		verify(processStatusService, times(0)).createBandUpdateProcess(anyLong(), anyString(), anyLong());
	}

	/**
	 * Scenario 2: Plan year changed and risk type is DIFFERENTIALS
	 * Given the plan year has changed and the exchange band risk type is DIFFERENTIALS
	 * When createStrategySyncEvent is executed
	 * Then a process status record with process name QUARTER_CHANGE is created
	 * And no band update process is created
	 */
	@Test
	public void createStrategySyncEvent_planYearChanged_riskTypeDifferentials_createsQuarterChange() {
		// given
		when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(true);
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetIII();
		exchangeBandsDtos.forEach(dto -> dto.setRiskType(RiskTypeEnum.DIFFERENTIALS));
		Company company = buildCompany();
		when(realmPlanYearService.findByRealmId(anyLong())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(anyString(), anyLong())).thenReturn(company);
		when(companyService.createUpdateCompany(any(), anyString(), anyLong(), any(), any(), any(), any())).thenReturn(COMPANY_ID);
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		// when
		exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		verify(processStatusService, times(1)).createStrategySyncProcess(
				anyString(),
				anyString(),
				eq(ProcessStatusEnum.QUARTER_CHANGE.getProcessName()),
				eq(ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName()));
		verify(processStatusService, times(0)).createStrategySyncProcess(
				anyString(),
				anyString(),
				eq(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName()),
				anyString());
		verify(processStatusService, times(0)).createBandUpdateProcess(anyLong(), anyString(), anyLong());
	}

	/**
	 * Scenario 3: Plan year not changed and risk type is BANDS
	 * Given the plan year has not changed, queued strategy sync is enabled, and risk type is BANDS
	 * When createStrategySyncEvent is executed
	 * Then processStatusService.createBandUpdateProcess is called
	 */
	@Test
	public void createStrategySyncEvent_planYearNotChanged_riskTypeBands_createsBandUpdateProcess() {
		// given
		when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(false);
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetI();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetI("423720");
		// ensure BANDS risk type
		exchangeBandsDtos.forEach(dto -> dto.setRiskType(RiskTypeEnum.BANDS));
		Company company = buildCompany();
		when(realmPlanYearService.findByRealmId(anyLong())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(anyString(), anyLong())).thenReturn(company);
		when(companyService.createUpdateCompany(any(), anyString(), anyLong(), any(), any(), any(), any())).thenReturn(COMPANY_ID);
		doNothing().when(bandCodesService).save(any(), anyLong());
		// when
		exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		verify(processStatusService, times(1)).createBandUpdateProcess(anyLong(), anyString(), anyLong());
		verify(processStatusService, times(0)).createStrategySyncProcess(anyString(), anyString(), anyString(), anyString());
	}

	/**
	 * Scenario 4: Plan year not changed and risk type is DIFFERENTIALS
	 * Given the plan year has not changed and the exchange band risk type is DIFFERENTIALS
	 * When createStrategySyncEvent is executed
	 * Then no band update process is created
	 */
	@Test
	public void createStrategySyncEvent_planYearNotChanged_riskTypeDifferentials_noBandUpdateProcess() {
		// given
		when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(false);
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetI();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetI("423720");
		exchangeBandsDtos.forEach(dto -> dto.setRiskType(RiskTypeEnum.DIFFERENTIALS));
		Company company = buildCompany();
		when(realmPlanYearService.findByRealmId(anyLong())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(anyString(), anyLong())).thenReturn(company);
		when(companyService.createUpdateCompany(any(), anyString(), anyLong(), any(), any(), any(), any())).thenReturn(COMPANY_ID);
		// when
		exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		verify(processStatusService, times(0)).createBandUpdateProcess(anyLong(), anyString(), anyLong());
		verify(processStatusService, times(0)).createStrategySyncProcess(anyString(), anyString(), anyString(), anyString());
	}

	@Test(expected = BSSBadDataException.class)
	public void saveExchangeBandsTest14() {
		// given
		// data
		when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(true);
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetIIIWithQuarterException();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		
		// when
		exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		
	}

	/**
	 * Test for saveExchangeBands when plan year change is enabled and both old and new benefit quarter exceptions are set to false, but the old
	 * company is not available. In this scenario, the band update event should
	 * be triggered.
	 */
	@Test
	public void saveExchangeBandsTest15() {
		// given
		// data
		when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(true);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetIII();
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
				.thenReturn(null);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(),
				realmPlanYearIdCaptor.capture(), omsOfferingCaptor.capture(), bundleIdCaptor.capture(),
				riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID).thenReturn(COMPANY_ID + 1);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet III", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(2, bands.size());
		CarrierBand carrierBand1 = bands.get(0);
		assertEquals("2024-01-01", CommonUtils.formatDateToString(carrierBand1.getEffectiveDate(), "yyyy-MM-dd"));
		assertEquals(COMPANY_ID, carrierBand1.getCompanyId());
		assertEquals("Q1", carrierBand1.getOeQuarter());
		assertEquals("Q1", carrierBand1.getOeQuarter());
		CarrierBand carrierBand2 = bands.get(1);
		assertEquals("2024-04-01", CommonUtils.formatDateToString(carrierBand2.getEffectiveDate(), "yyyy-MM-dd"));
		assertEquals(COMPANY_ID + 1, carrierBand2.getCompanyId());
		assertEquals("Q2", carrierBand2.getOeQuarter());
		// verify
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		List<String> companyCodeCaptors = companyCodeCaptor.getAllValues();
		List<Long> realmPlanYearIdCaptors = realmPlanYearIdCaptor.getAllValues();
		List<CarrierBand> carrierBandCaptors = carrierBandCaptor.getAllValues();
		List<Long> companyIdCaptors = companyIdCaptor.getAllValues();
		List<String> omsOfferingCaptors = omsOfferingCaptor.getAllValues();
		List<Long> bundleIdCaptors = bundleIdCaptor.getAllValues();
		List<Company> companyCaptors = companyCaptor.getAllValues();
		List<RiskTypeEnum> riskTypeCaptors = riskTypeCaptor.getAllValues();
		List<Integer> naicsCodeCaptors = naicsCodeCaptor.getAllValues();
		// verify
		verify(processStatusService, times(0)).createStrategySyncProcess(anyString(), anyString(), anyString(),
				anyString());
		verify(processStatusService, times(1)).createBandUpdateProcess(anyLong(), anyString(), anyLong());
		verify(companyService, times(2)).findCompanyBy(companyCodeCaptor.getValue(), realmPlanYearIdCaptor.getValue());
		verify(companyService, times(1)).createUpdateCompany(companyCaptors.get(0), companyCodeCaptors.get(0),
				realmPlanYearIdCaptors.get(0), omsOfferingCaptors.get(0), bundleIdCaptors.get(0),
				riskTypeCaptors.get(0), naicsCodeCaptors.get(0));
		verify(bandCodesService, times(1)).save(carrierBandCaptors.get(0), companyIdCaptors.get(0));
		verify(companyService, times(1)).createUpdateCompany(companyCaptors.get(1), companyCodeCaptors.get(1),
				realmPlanYearIdCaptors.get(1), omsOfferingCaptors.get(1), bundleIdCaptors.get(1),
				riskTypeCaptors.get(0), naicsCodeCaptors.get(1));
		verify(bandCodesService, times(1)).save(carrierBandCaptors.get(1), companyIdCaptors.get(1));
	}

	@Test
	public void bundleId_should_be_set_to_negative_1_when_bundleId_is_custom() {
		// given
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetI();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetI("423720");
		exchangeBandsDtos.get(0).setBundleId(BSSApplicationConstants.CUSTOM_BUNDLE_NAME);
		Company company = buildCompany();
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(false);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
				.thenReturn(company);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture(),
				omsOfferingCaptor.capture(), bundleIdCaptor.capture(), riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());

		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);

		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals(String.valueOf(BSSApplicationConstants.CUSTOM_BUNDLE_ID), exchangeBandsDto.getBundleId());
	}

	/**
	 * Test for saveExchangeBands when plan year change is enabled and the old
	 * benefit quarter exception is true and new benefit quarter exception is false.
	 * In this scenario, the plan year change event should be triggered.
	 */
	@Test
	public void saveExchangeBandsTest16() {
		// given
		// data
		when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(true);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetIIIWithQuarterException2();
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(false);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
				.thenReturn(company);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(),
				realmPlanYearIdCaptor.capture(), omsOfferingCaptor.capture(), bundleIdCaptor.capture(),
				riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID).thenReturn(COMPANY_ID + 1);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet III", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		CarrierBand carrierBand1 = bands.get(0);

		// verify
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		List<String> companyCodeCaptors = companyCodeCaptor.getAllValues();
		List<Long> realmPlanYearIdCaptors = realmPlanYearIdCaptor.getAllValues();
		List<CarrierBand> carrierBandCaptors = carrierBandCaptor.getAllValues();
		List<Long> companyIdCaptors = companyIdCaptor.getAllValues();
		List<String> omsOfferingCaptors = omsOfferingCaptor.getAllValues();
		List<Long> bundleIdCaptors = bundleIdCaptor.getAllValues();
		List<Company> companyCaptors = companyCaptor.getAllValues();
		List<RiskTypeEnum> riskTypeCaptors = riskTypeCaptor.getAllValues();
		List<Integer> naicsCodeCaptors = naicsCodeCaptor.getAllValues();
		// verify
		verify(processStatusService, times(1)).createStrategySyncProcess(anyString(), anyString(), anyString(),
				anyString());
		verify(processStatusService, times(0)).createBandUpdateProcess(anyLong(), anyString(), anyLong());
		verify(companyService, times(1)).createUpdateCompany(companyCaptors.get(0), companyCodeCaptors.get(0),
				realmPlanYearIdCaptors.get(0), omsOfferingCaptors.get(0), bundleIdCaptors.get(0),
				riskTypeCaptors.get(0), naicsCodeCaptors.get(0));
		verify(bandCodesService, times(1)).save(carrierBandCaptors.get(0), companyIdCaptors.get(0));
	}

	/**
	 * Test for saveExchangeBands when plan year change is enabled and the old
	 * benefit quarter exception is true and new benefit quarter exception is true.
	 * In this scenario, the band update event should be triggered.
	 */
	@Test
	public void saveExchangeBandsTest17() {
		// given
		// data
		when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(true);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTNIII2026();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTriNetIIIWithQuarterException3();
		exchangeBandsDtos.get(0).setOldBenefitsQuarterException(true);
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
				.thenReturn(company);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(),
				realmPlanYearIdCaptor.capture(), omsOfferingCaptor.capture(), bundleIdCaptor.capture(),
				riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID).thenReturn(COMPANY_ID + 1);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet III", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		CarrierBand carrierBand1 = bands.get(0);
		assertEquals("2026-04-01", CommonUtils.formatDateToString(carrierBand1.getEffectiveDate(), "yyyy-MM-dd"));

		// verify
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		List<CarrierBand> carrierBandCaptors = carrierBandCaptor.getAllValues();
		List<Long> companyIdCaptors = companyIdCaptor.getAllValues();
		// verify
		verify(processStatusService, times(0)).createStrategySyncProcess(anyString(), anyString(), anyString(),
				anyString());
		verify(processStatusService, times(1)).createBandUpdateProcess(anyLong(), anyString(), anyLong());
		verify(bandCodesService, times(1)).save(carrierBandCaptors.get(0), companyIdCaptors.get(0));
	}

	/**
	 * Test for saveExchangeBands when plan year change is enabled and the old
	 * benefit quarter exception is true and new benefit quarter exception is true,
	 * but old company is not available. In this scenario, the band update event
	 * should be triggered.
	 */
	@Test
	public void saveExchangeBandsTest18() {
		// given
		// data
		when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(true);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTNIII2026();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTriNetIIIWithQuarterException3();
		exchangeBandsDtos.get(0).setOldBenefitsQuarterException(true);
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
				.thenReturn(null);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(),
				realmPlanYearIdCaptor.capture(), omsOfferingCaptor.capture(), bundleIdCaptor.capture(),
				riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID).thenReturn(COMPANY_ID + 1);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet III", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		CarrierBand carrierBand1 = bands.get(0);
		assertEquals("2026-04-01", CommonUtils.formatDateToString(carrierBand1.getEffectiveDate(), "yyyy-MM-dd"));

		// verify
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		List<CarrierBand> carrierBandCaptors = carrierBandCaptor.getAllValues();
		List<Long> companyIdCaptors = companyIdCaptor.getAllValues();
		// verify
		verify(processStatusService, times(0)).createStrategySyncProcess(anyString(), anyString(), anyString(),
				anyString());
		verify(processStatusService, times(1)).createBandUpdateProcess(anyLong(), anyString(), anyLong());
		verify(bandCodesService, times(1)).save(carrierBandCaptors.get(0), companyIdCaptors.get(0));
	}

	/**
	 * Test for saveExchangeBands when plan year change is enabled and the old
	 * benefit quarter exception is true and new benefit quarter exception is false,
	 * but old company is not available. In this scenario, the band update event
	 * should be triggered.
	 */
	@Test
	public void saveExchangeBandsTest19() {
		// given
		// data
		when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(true);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTNIII2026();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTriNetIIIWithQuarterException3();
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
				.thenReturn(null);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(),
				realmPlanYearIdCaptor.capture(), omsOfferingCaptor.capture(), bundleIdCaptor.capture(),
				riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID).thenReturn(COMPANY_ID + 1);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet III", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		CarrierBand carrierBand1 = bands.get(0);
		assertEquals("2026-04-01", CommonUtils.formatDateToString(carrierBand1.getEffectiveDate(), "yyyy-MM-dd"));

		// verify
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		List<CarrierBand> carrierBandCaptors = carrierBandCaptor.getAllValues();
		List<Long> companyIdCaptors = companyIdCaptor.getAllValues();
		// verify
		verify(processStatusService, times(0)).createStrategySyncProcess(anyString(), anyString(), anyString(),
				anyString());
		verify(processStatusService, times(1)).createBandUpdateProcess(anyLong(), anyString(), anyLong());
		verify(bandCodesService, times(1)).save(carrierBandCaptors.get(0), companyIdCaptors.get(0));
	}

	/**
	 * Test for saveExchangeBands when plan year change is enabled and the old
	 * benefit quarter exception is true and new benefit quarter exception is false,
	 * but old company is not available. In this scenario, the band update event
	 * should be triggered.
	 */
	@Test
	public void saveExchangeBandsTest20() {
		// given
		// data
		when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(true);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();
		List<ExchangeBandsDto> exchangeBandsDtos = buildExchangeBandsDtosTrinetIIIWithQuarterException2();
		Company company = buildCompany();
		// method mocks
		when(AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()).thenReturn(true);
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture()))
				.thenReturn(null);
		when(companyService.createUpdateCompany(companyCaptor.capture(), companyCodeCaptor.capture(),
				realmPlanYearIdCaptor.capture(), omsOfferingCaptor.capture(), bundleIdCaptor.capture(),
				riskTypeCaptor.capture(), naicsCodeCaptor.capture())).thenReturn(COMPANY_ID).thenReturn(COMPANY_ID + 1);
		doNothing().when(bandCodesService).save(carrierBandCaptor.capture(), companyIdCaptor.capture());
		// when
		List<ExchangeBandsDto> actualResult = exchangeServiceImpl.saveExchangeBands(exchangeBandsDtos, COMPANY_CODE);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals("TriNet III", exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		CarrierBand carrierBand1 = bands.get(0);

		// verify
		verify(realmPlanYearService, times(1)).findByRealmId(exchangeIdCaptor.getValue());
		List<String> companyCodeCaptors = companyCodeCaptor.getAllValues();
		List<Long> realmPlanYearIdCaptors = realmPlanYearIdCaptor.getAllValues();
		List<CarrierBand> carrierBandCaptors = carrierBandCaptor.getAllValues();
		List<Long> companyIdCaptors = companyIdCaptor.getAllValues();
		List<String> omsOfferingCaptors = omsOfferingCaptor.getAllValues();
		List<Long> bundleIdCaptors = bundleIdCaptor.getAllValues();
		List<Company> companyCaptors = companyCaptor.getAllValues();
		List<RiskTypeEnum> riskTypeCaptors = riskTypeCaptor.getAllValues();
		List<Integer> naicsCodeCaptors = naicsCodeCaptor.getAllValues();
		// verify
		verify(processStatusService, times(0)).createStrategySyncProcess(anyString(), anyString(), anyString(),
				anyString());
		verify(processStatusService, times(1)).createBandUpdateProcess(anyLong(), anyString(), anyLong());
		verify(companyService, times(1)).createUpdateCompany(companyCaptors.get(0), companyCodeCaptors.get(0),
				realmPlanYearIdCaptors.get(0), omsOfferingCaptors.get(0), bundleIdCaptors.get(0),
				riskTypeCaptors.get(0), naicsCodeCaptors.get(0));
		verify(bandCodesService, times(1)).save(carrierBandCaptors.get(0), companyIdCaptors.get(0));
	}

	private void assertSuccessfulResponseForOMS(List<ExchangeBandsDto> actualResult) {
		assertEquals(1, actualResult.size());
		ExchangeBandsDto exchangeBandsDto = actualResult.get(0);
		assertEquals(TRINET_OMS.getBenExchng(), exchangeBandsDto.getExchangeId());
		List<CarrierBand> bands = exchangeBandsDto.getBands();
		assertEquals(1, bands.size());
		CarrierBand carrierBand1 = bands.get(0);
		assertEquals("2025-01-01", CommonUtils.formatDateToString(carrierBand1.getEffectiveDate(), "yyyy-MM-dd"));
		assertEquals(COMPANY_ID, carrierBand1.getCompanyId());
		assertEquals(OMS_QUARTER, carrierBand1.getOeQuarter());

		omsOfferingCaptor = ArgumentCaptor.forClass(String.class);
		verify(companyService).findCompanyBy(COMPANY_CODE, OMS_REALM_PLAN_YEAR_ID);
		verify(companyService).createUpdateCompany(any(), companyCodeCaptor.capture(), realmPlanYearIdCaptor.capture(),
                omsOfferingCaptor.capture(), anyLong(), riskTypeCaptor.capture(), naicsCodeCaptor.capture());

		assertEquals(COMPANY_CODE, companyCodeCaptor.getValue());
		assertEquals(85, realmPlanYearIdCaptor.getValue().longValue());
		assertEquals(OMB_TLD.name(), omsOfferingCaptor.getValue());
	}

	private void mockServiceCalls(Company company, List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos) {
		when(realmPlanYearService.findByRealmId(anyLong())).thenReturn(realmPlanYearDetailsDtos);
		when(companyService.findCompanyBy(anyString(), anyLong()))
				.thenReturn(company);
		when(companyService.createUpdateCompany(any(Company.class),anyString(),anyLong(),anyString(),anyLong(), riskTypeCaptor.capture(), any())).thenReturn(COMPANY_ID);
		doNothing().when(bandCodesService).save(any(), eq(COMPANY_ID));
	}


	private List<RealmPlanYearDetailsDto> buildRealmPlanYearDetailsDtoTrinetI() {
		return List.of(RealmPlanYearDetailsDto.builder().id(69).realmId(5).quarter("AC")
				.startDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
				.endDate(CommonUtils.formatStringToDate("2024-12-31", "yyyy-MM-dd")).build());
	}

	private List<ExchangeBandsDto> buildExchangeBandsDtosTrinetI(String naicsCode) {
		ExchangeBandsDto dto = ExchangeBandsDto.builder().exchangeId("TriNet I")
				.benefitsQuarterException(true)
				.benefitsStartDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
				.bands(Arrays.asList(
						CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
								.carrierBands(Arrays.asList(
										CarrierBandDetails.builder().carrier("Aetna").bandCode("20A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Kaiser HI").bandCode("12A")
												.buildCarrierBandDetails()))
								.buildCarrierBand()))
				.riskType(RiskTypeEnum.BANDS)
				.naicsCode(naicsCode)
				.buildExchangeBands();
		return Arrays.asList(dto);
	}
	

	private List<RealmPlanYearDetailsDto> buildRealmPlanYearDetailsDtoTrinetII() {
		return List.of(RealmPlanYearDetailsDto.builder().id(72).realmId(4).quarter("SY")
				.startDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
				.endDate(CommonUtils.formatStringToDate("2025-03-31", "yyyy-MM-dd")).build());
	}

	private List<ExchangeBandsDto> buildExchangeBandsDtosTrinetII() {
		ExchangeBandsDto dto = ExchangeBandsDto.builder().exchangeId("TriNet II")
				.benefitsStartDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
				.oldBenefitsStartDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
				.bands(Arrays.asList(
						CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
								.carrierBands(Arrays.asList(
										CarrierBandDetails.builder().carrier("Aetna").bandCode("20A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Kaiser HI").bandCode("12A")
												.buildCarrierBandDetails()))
								.buildCarrierBand()))
				.riskType(RiskTypeEnum.BANDS)
				.naicsCode("423720")
				.buildExchangeBands();
		return Arrays.asList(dto);
	}

	private List<RealmPlanYearDetailsDto> buildRealmPlanYearDetailsDtoTrinetIV() {
		return List.of(RealmPlanYearDetailsDto.builder().id(66).realmId(1).quarter("8Y")
				.startDate(CommonUtils.formatStringToDate("2024-10-01", "yyyy-MM-dd"))
				.endDate(CommonUtils.formatStringToDate("2025-09-30", "yyyy-MM-dd")).build());
	}

	private List<ExchangeBandsDto> buildExchangeBandsDtosTrinetIV() {
		ExchangeBandsDto dto = ExchangeBandsDto.builder().exchangeId("TriNet IV")
				.benefitsStartDate(CommonUtils.formatStringToDate("2025-01-01", "yyyy-MM-dd"))
				.oldBenefitsStartDate(CommonUtils.formatStringToDate("2024-10-01", "yyyy-MM-dd"))
				.bands(Arrays.asList(
						CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2024-10-01", "yyyy-MM-dd"))
								.carrierBands(Arrays.asList(
										CarrierBandDetails.builder().carrier("Aetna").bandCode("20A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Kaiser HI").bandCode("12A")
												.buildCarrierBandDetails()))
								.buildCarrierBand()))
				.riskType(RiskTypeEnum.BANDS)
				.naicsCode("423720")
				.buildExchangeBands();
		return Arrays.asList(dto);
	}

	private List<RealmPlanYearDetailsDto> buildRealmPlanYearDetailsForTrinetOMS() {
		return List.of(RealmPlanYearDetailsDto.builder().id(OMS_REALM_PLAN_YEAR_ID).realmId(OMS_REALM_ID).quarter(OMS_QUARTER)
				.startDate(CommonUtils.formatStringToDate("2025-01-01", "yyyy-MM-dd"))
				.endDate(CommonUtils.formatStringToDate("2025-12-31", "yyyy-MM-dd")).build());
	}

	private List<RealmPlanYearDetailsDto> buildRealmPlanYearDetailsDtoTrinetXI() {
		return List.of(RealmPlanYearDetailsDto.builder().id(67).realmId(2).quarter("AL")
						.startDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
						.endDate(CommonUtils.formatStringToDate("2024-12-31", "yyyy-MM-dd")).build(),
				RealmPlanYearDetailsDto.builder().id(77).realmId(2).quarter("AL")
						.startDate(CommonUtils.formatStringToDate("2025-01-01", "yyyy-MM-dd"))
						.endDate(CommonUtils.formatStringToDate("2025-12-31", "yyyy-MM-dd")).build());
	}

	private List<ExchangeBandsDto> buildExchangeBandsForTrinetOMS(OmsOfferingEnum omsOffering) {
		ExchangeBandsDto dto = ExchangeBandsDto.builder().exchangeId(TRINET_OMS.getBenExchng())
				.omsOffering(omsOffering)
				.bundleId("1")
				.benefitsStartDate(CommonUtils.formatStringToDate("2025-01-01", "yyyy-MM-dd"))
				.oldBenefitsStartDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
				.bands(Arrays.asList(
						CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2025-01-01", "yyyy-MM-dd"))
								.carrierBands(Arrays.asList(
										CarrierBandDetails.builder().carrier("Aetna").bandCode("20A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Kaiser HI").bandCode("12A")
												.buildCarrierBandDetails()))
								.buildCarrierBand()))
				.riskType(RiskTypeEnum.BANDS)
				.naicsCode("423720")
				.buildExchangeBands();
		return Arrays.asList(dto);
	}

	private List<ExchangeBandsDto> buildExchangeBandsDtosTrinetXI() {
		ExchangeBandsDto dto = ExchangeBandsDto.builder().exchangeId("TriNet XI")
				.benefitsStartDate(CommonUtils.formatStringToDate("2025-01-01", "yyyy-MM-dd"))
				.oldBenefitsStartDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
				.bands(Arrays.asList(
						CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
								.carrierBands(Arrays.asList(
										CarrierBandDetails.builder().carrier("Aetna").bandCode("20A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Kaiser HI").bandCode("12A")
												.buildCarrierBandDetails()))
								.buildCarrierBand()))
				.riskType(RiskTypeEnum.BANDS)
				.naicsCode("423720")
				.buildExchangeBands();
		return Arrays.asList(dto);
	}

	private List<RealmPlanYearDetailsDto> buildRealmPlanYearDetailsDtoTrinetIII() {
		return List.of(
				RealmPlanYearDetailsDto.builder().id(68).realmId(3).quarter("Q1")
						.startDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
						.endDate(CommonUtils.formatStringToDate("2024-03-31", "yyyy-MM-dd")).build(),
				RealmPlanYearDetailsDto.builder().id(70).realmId(3).quarter("Q2")
						.startDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
						.endDate(CommonUtils.formatStringToDate("2024-06-30", "yyyy-MM-dd")).build());
	}

	private List<ExchangeBandsDto> buildExchangeBandsDtosTrinetIII() {
		ExchangeBandsDto dto = ExchangeBandsDto.builder().exchangeId("TriNet III")
				.benefitsStartDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
				.oldBenefitsStartDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
				.benefitsQuarterException(false)
				.oldBenefitsQuarterException(false)
				.bands(Arrays.asList(
						CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
								.carrierBands(Arrays.asList(
										CarrierBandDetails.builder().carrier("Aetna").bandCode("20A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Kaiser HI").bandCode("12A")
												.buildCarrierBandDetails()))
								.buildCarrierBand(),
						CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
								.carrierBands(Arrays.asList(
										CarrierBandDetails.builder().carrier("Aetna").bandCode("20A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Kaiser HI").bandCode("12A")
												.buildCarrierBandDetails()))
								.buildCarrierBand()))
				.riskType(RiskTypeEnum.BANDS)
				.naicsCode("423720")
				.buildExchangeBands();
		return Arrays.asList(dto);
	}
	
	private List<ExchangeBandsDto> buildExchangeBandsDtosTrinetIIIWithQuarterException2() {
		ExchangeBandsDto dto = ExchangeBandsDto.builder().exchangeId("TriNet III")
				.benefitsStartDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
				.oldBenefitsStartDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
				.benefitsQuarterException(true)
				.oldBenefitsQuarterException(false)
				.bands(Collections.singletonList(CarrierBand.builder()
                        .effectiveDate(CommonUtils.formatStringToDate("2024-05-30", "yyyy-MM-dd"))
                        .carrierBands(Arrays.asList(
                                CarrierBandDetails.builder().carrier("Aetna").bandCode("20A")
                                        .buildCarrierBandDetails(),
                                CarrierBandDetails.builder().carrier("Kaiser HI").bandCode("12A")
                                        .buildCarrierBandDetails()))
                        .buildCarrierBand()))
				.riskType(RiskTypeEnum.BANDS)
				.naicsCode("423720")
				.buildExchangeBands();
		return Collections.singletonList(dto);
	}
	
	private List<ExchangeBandsDto> buildExchangeBandsDtosTrinetIIIWithQuarterException() {
		ExchangeBandsDto dto = ExchangeBandsDto.builder().exchangeId("TriNet III")
				.benefitsStartDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
				.oldBenefitsStartDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
				.benefitsQuarterException(true)
				.bands(Arrays.asList(CarrierBand.builder()
						.effectiveDate(CommonUtils.formatStringToDate("2024-05-30", "yyyy-MM-dd"))
						.carrierBands(Arrays.asList(
								CarrierBandDetails.builder().carrier("Aetna").bandCode("20A").buildCarrierBandDetails(),
								CarrierBandDetails.builder().carrier("Kaiser HI").bandCode("12A")
										.buildCarrierBandDetails()))
						.buildCarrierBand(),
						CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2024-05-30", "yyyy-MM-dd"))
								.carrierBands(Arrays.asList(
										CarrierBandDetails.builder().carrier("Aetna").bandCode("20A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Kaiser HI").bandCode("12A")
												.buildCarrierBandDetails()))
								.buildCarrierBand()))
				.buildExchangeBands();
		return Arrays.asList(dto);
	}
	
	private List<RealmPlanYearDetailsDto> buildRealmPlanYearDetailsDtoCarrierBandTest() {
		return List.of(
				RealmPlanYearDetailsDto.builder().id(68).realmId(3).quarter("Q1")
						.startDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
						.endDate(CommonUtils.formatStringToDate("2024-12-31", "yyyy-MM-dd")).build(),
						RealmPlanYearDetailsDto.builder().id(78).realmId(3).quarter("Q2")
								.startDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
								.endDate(CommonUtils.formatStringToDate("2024-03-31", "yyyy-MM-dd")).build());
	}
	
	private List<ExchangeBandsDto> buildExchangeBandsDtosForCarrierBandTest() {
		ExchangeBandsDto dto = ExchangeBandsDto.builder().exchangeId("TriNet III")
				.benefitsStartDate(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"))
				.oldBenefitsStartDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
				.bands(Arrays.asList(
						CarrierBand.builder().effectiveDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
								.carrierBands(Arrays.asList(
										CarrierBandDetails.builder().carrier("UHC").bandCode("20A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Tufts").bandCode("12A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Kaiser NW/WA").bandCode("20A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Kaiser DC/MD/VA")
												.bandCode("12B").buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Kaiser HI").bandCode(null)
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Kaiser CO/GA").bandCode("12C")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Kaiser CA").bandCode("20A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Harvard Pilgrim")
												.bandCode("12D").buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Empire").bandCode("   ")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("BCBSNC").bandCode("12A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("BCBSMN").bandCode("25A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("BC Idaho").bandCode("15A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("BSCA").bandCode("23A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("BCBSFL").bandCode("")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("Aetna").bandCode("20A")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("LIFE").bandCode("1")
												.buildCarrierBandDetails(),
										CarrierBandDetails.builder().carrier("DISABILITY").bandCode("4")
												.buildCarrierBandDetails()))
								.buildCarrierBand()))
				.riskType(RiskTypeEnum.BANDS)
				.naicsCode("423720")
				.buildExchangeBands();
		return Arrays.asList(dto);
	}

	private ProspectInfoResponse prepareProspectCompany() {
		ProspectInfoResponse prospectInfoResponse = new ProspectInfoResponse();
		prospectInfoResponse.setZipCode("28277");
		prospectInfoResponse.setHqState("NC");
		prospectInfoResponse.setBenStartDate("2024-04-01");
		return prospectInfoResponse;
	}
	
	private Company buildCompany() {
		Company company = new Company();
		company.setId(COMPANY_ID);
		company.setCode(COMPANY_CODE);
		company.setDescription(COMPANY_CODE);
		company.setName(COMPANY_CODE);
		company.setRealmPlanYearId(70);
		company.setRiskType(RiskTypeEnum.BANDS);
		return company;
	}

	private List<ExchangeBandsDto> buildExchangeBandsDtosTriNetIIIWithQuarterException3() {
		ExchangeBandsDto dto = ExchangeBandsDto.builder().exchangeId("TriNet III")
				.benefitsStartDate(CommonUtils.formatStringToDate("2026-04-01", "yyyy-MM-dd"))
				.oldBenefitsStartDate(CommonUtils.formatStringToDate("2026-04-01", "yyyy-MM-dd"))
				.benefitsQuarterException(false).oldBenefitsQuarterException(true)
				.bands(Arrays.asList(CarrierBand.builder()
						.effectiveDate(CommonUtils.formatStringToDate("2026-04-01", "yyyy-MM-dd"))
						.carrierBands(Arrays.asList(
								CarrierBandDetails.builder().carrier("Aetna").bandCode("20A").buildCarrierBandDetails(),
								CarrierBandDetails.builder().carrier("Kaiser HI").bandCode("12A")
										.buildCarrierBandDetails()))
						.buildCarrierBand()))
				.riskType(RiskTypeEnum.BANDS).naicsCode("423720").buildExchangeBands();
		return Collections.singletonList(dto);
	}

	private List<RealmPlanYearDetailsDto> buildRealmPlanYearDetailsDtoTNIII2026() {
		return List.of(
				RealmPlanYearDetailsDto.builder().id(88).realmId(3).quarter("Q1")
						.startDate(CommonUtils.formatStringToDate("2026-01-01", "yyyy-MM-dd"))
						.endDate(CommonUtils.formatStringToDate("2026-03-31", "yyyy-MM-dd")).build(),
				RealmPlanYearDetailsDto.builder().id(90).realmId(3).quarter("Q2")
						.startDate(CommonUtils.formatStringToDate("2026-04-01", "yyyy-MM-dd"))
						.endDate(CommonUtils.formatStringToDate("2026-06-30", "yyyy-MM-dd")).build());
	}
}

