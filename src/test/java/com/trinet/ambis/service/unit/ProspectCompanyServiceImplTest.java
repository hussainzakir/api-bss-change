package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.enums.RiskTypeEnum;
import org.apache.commons.collections.CollectionUtils;
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
import org.springframework.web.client.RestTemplate;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.dao.hrp.CompanyBandCodesDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.model.Bundle;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmCloneProgram;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.BandCodesService;
import com.trinet.ambis.service.BenefitsBundleService;
import com.trinet.ambis.service.GroupRuleService;
import com.trinet.ambis.service.MinFundExceptionService;
import com.trinet.ambis.service.RateSystemService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.RealmRegionMinFundingService;
import com.trinet.ambis.service.dto.RealmPlanYearDetailsDto;
import com.trinet.ambis.service.impl.ProspectCompanyServiceImpl;
import com.trinet.ambis.service.model.CompanyBandCodes;
import com.trinet.ambis.service.model.RealmTypeService;
import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import com.trinet.ambis.service.model.prospect.ProspectInfoResponse;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.ProspectServiceRestClient;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.Utils;

@RunWith(MockitoJUnitRunner.class)
public class ProspectCompanyServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	ProspectCompanyServiceImpl prospectCompanyServiceImpl;

	@Mock
	CompanyDao companyDao;

	@Mock
	GroupRuleService groupRuleService;

	@Mock
	MinFundExceptionService minFundExceptionService;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	RealmPlanYearService realmPlanYearService;

	@Mock
	RealmRegionMinFundingService realmRegionMinFundingService;

	@Mock
	RealmTypeService realmTypeService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	CompanyBandCodesDao companyBandCodesDao;
	
	@Mock
	PsCompanyDao psCompanyDao;

	@Mock
	ProspectServiceRestClient prospectServiceRestClient;
	
	@Mock
	BenefitsBundleService benefitsBundleService;

	@Mock
	BandCodesService bandCodesService;

	@Mock
	RateSystemService rateSystemService;

	@Captor
	private ArgumentCaptor<Long> exchangeIdCaptor;

	private static final BigDecimal ALE_AMOUNT = new BigDecimal("103.28");
	private static final String GOOD_EXCHANGE = "TNIII";
	// private static final String INVALID_EXCHANGE = "EXIII";
	private static final String PROSPECT_API_URL = "http://localhost:8087/api-wf-hw-bss-prospect/v1";

    private MockedStatic<BSSMessageConfig> bssMessageConfigMockedStatic;
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;
    private MockedStatic<AppRulesAndConfigsUtils> appRulesAndConfigsUtilsMockedStatic;

    @Before
    public void setUp() {
        bssMessageConfigMockedStatic = Mockito.mockStatic(BSSMessageConfig.class);
        rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
        appRulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
        appRulesAndConfigsUtilsMockedStatic.when(AppRulesAndConfigsUtils::isBssOutputPhase2Enabled).thenReturn(true);
    }

    @After
    public void tearDown() {
        bssMessageConfigMockedStatic.close();
        rulesAndConfigsUtilsMockedStatic.close();
        appRulesAndConfigsUtilsMockedStatic.close();
    }

	@Test
	public void getProspectCompanyDetailsTest() {
		Company company = prepareCompany();
		String companyCode = company.getCode();
		BenExchngEnums benExchange = BenExchngEnums.getByExchangeId(GOOD_EXCHANGE);
		List<String> employeeHomeStates = new ArrayList<String>();
		employeeHomeStates.add("CA");
		Set<String> sdiStates = Set.of("CA", "NW");
		ProspectInfoResponse response = prospectInfoResponse(employeeHomeStates, null,false, null);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();
		
		// method mocks
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		when(realmPlanYearService.getRealmPlanYearById(anyLong())).thenReturn(prepareRPY("Q4"));
		when(realmTypeService.findById(anyLong())).thenReturn(prepareRealm());
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI)).thenReturn(PROSPECT_API_URL);
		when(realmDataDao.getRealmCloneProgram(anyLong())).thenReturn(prepareRealmCloneProgData());
		when(RulesAndConfigsUtils.getSDIStates(anyLong())).thenReturn(sdiStates);
		when(companyBandCodesDao.getBandCodesByCompanyIdAndEffDate(anyLong(), anyString()))
				.thenReturn(prepareCompanyBandCodes());
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(any(ProspectApiRequest.class)))
				.thenReturn(response);
		when(rateSystemService.getRateSystemRateType(any(Company.class))).thenReturn("REGIONAL");


		Company actualCompany = prospectCompanyServiceImpl.getProspectCompanyDetails(companyCode, benExchange);

		verify(psCompanyDao, times(0)).isTexasSitus(anyString(), any(Date.class));

		assertEquals("101", actualCompany.getRealmPlanYear().getCloneProgram());
		assertEquals(2, actualCompany.getSdiStates().size());
		assertTrue(sdiStates.containsAll(actualCompany.getSdiStates())
				&& actualCompany.getSdiStates().containsAll(sdiStates));
		assertEquals("202", actualCompany.getBandCodes().getAetnaBandCode());
		assertEquals("40", actualCompany.getBandCodes().getBcbsBandCode());
		assertEquals("CA", actualCompany.getHeadQuatersState());
		assertEquals("93456", actualCompany.getZipCode());
		assertEquals("2024-12-31", actualCompany.getExpiryDate());
		assertEquals("Q-778977", actualCompany.getProposalId());
		assertEquals("Q4", actualCompany.getQuater());
		assertEquals(74, actualCompany.getRealmPlanYear().getId());
		assertEquals(null, actualCompany.getNaicsCode());
		assertTrue(actualCompany.isTexasSitus());
	}
	
	@Test
	public void getProspectCompanyDetailsTest1() {

		Company company = prepareCompany();
		String companyCode = company.getCode();
		BenExchngEnums benExchange = BenExchngEnums.getByExchangeId(GOOD_EXCHANGE);
		Set<String> sdiStates = new HashSet<>();
		List<String> employeeHomeStates = new ArrayList<String>();
		employeeHomeStates.add("CA");
		List<CompanyBandCodes> companyBandCodesList = new ArrayList<>();
		ProspectInfoResponse response = prospectInfoResponse(employeeHomeStates, null,false, "523940");
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();
		company.setBundleId(1L);
		Bundle bundle =  new Bundle();
		bundle.setId(1L);
		bundle.setName("Test Bundle");
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		when(realmPlanYearService.getRealmPlanYearById(anyLong())).thenReturn(prepareRPY("Q4"));
		when(realmTypeService.findById(anyLong())).thenReturn(prepareRealm());
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI)).thenReturn(PROSPECT_API_URL);
		when(realmDataDao.getRealmCloneProgram(anyLong())).thenReturn(prepareRealmCloneProgData());
		when(RulesAndConfigsUtils.getSDIStates(anyLong())).thenReturn(sdiStates);
		when(companyBandCodesDao.getBandCodesByCompanyIdAndEffDate(anyLong(), anyString())).thenReturn(companyBandCodesList);
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(any(ProspectApiRequest.class)))
				.thenReturn(response);
		when(benefitsBundleService.getBundleById(anyLong())).thenReturn(bundle);
		when(rateSystemService.getRateSystemRateType(any(Company.class))).thenReturn("REGIONAL");

		Company actualCompany = prospectCompanyServiceImpl.getProspectCompanyDetails(companyCode, benExchange);

		verify(psCompanyDao, times(0)).isTexasSitus(anyString(), any(Date.class));
		
		assertEquals("101", actualCompany.getRealmPlanYear().getCloneProgram());
		assertEquals(0, actualCompany.getSdiStates().size());
		assertTrue(actualCompany.getSdiStates().isEmpty());
		assertNotNull(actualCompany.getBandCodes());
		assertEquals("CA", actualCompany.getHeadQuatersState());
		assertEquals("93456", actualCompany.getZipCode());
		assertEquals("2024-12-31", actualCompany.getExpiryDate());
		assertEquals(Long.valueOf(1L), actualCompany.getBundleId());
		assertEquals("Test Bundle", actualCompany.getBundleName());
		assertEquals(Integer.valueOf(523940), actualCompany.getNaicsCode());
		assertTrue(actualCompany.isTexasSitus());
	}

	@Test
	public void getProspectCompanyDetailsTest2() {

		Company company = prepareCompany();
		String companyCode = company.getCode();
		BenExchngEnums benExchange = BenExchngEnums.getByExchangeId(GOOD_EXCHANGE);
		Set<String> sdiStates = new HashSet<>();
		List<CompanyBandCodes> companyBandCodesList = new ArrayList<>();
		ProspectInfoResponse response = prospectInfoResponse(null, null, false, "523940");
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();
		
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		when(realmPlanYearService.getRealmPlanYearById(anyLong())).thenReturn(prepareRPY("Q4"));
		when(realmTypeService.findById(anyLong())).thenReturn(prepareRealm());
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI)).thenReturn(PROSPECT_API_URL);
		when(realmDataDao.getRealmCloneProgram(anyLong())).thenReturn(prepareRealmCloneProgData());
		when(RulesAndConfigsUtils.getSDIStates(anyLong())).thenReturn(sdiStates);
		when(companyBandCodesDao.getBandCodesByCompanyIdAndEffDate(anyLong(), anyString())).thenReturn(companyBandCodesList);
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(any(ProspectApiRequest.class)))
				.thenReturn(response);
		when(rateSystemService.getRateSystemRateType(any(Company.class))).thenReturn("REGIONAL");

		Company actualCompany = prospectCompanyServiceImpl.getProspectCompanyDetails(companyCode, benExchange);

		verify(psCompanyDao, times(0)).isTexasSitus(anyString(), any(Date.class));
		verify(companyDao, times(1)).findCompanyBy("a1b2c3", benExchange.getBenExchng(), "Q4",
				Utils.convertStringToDate("10/11/2024", BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY));
		assertEquals("101", actualCompany.getRealmPlanYear().getCloneProgram());
		assertEquals(0, actualCompany.getSdiStates().size());
		assertTrue(actualCompany.getSdiStates().isEmpty());
		assertNotNull(actualCompany.getBandCodes());
		assertEquals("Q4", actualCompany.getQuater());
		assertEquals("CA", actualCompany.getHeadQuatersState());
		assertEquals("93456", actualCompany.getZipCode());
		assertEquals(null, actualCompany.getEmployeeRegions());
		assertEquals("2024-12-31", actualCompany.getExpiryDate());
		assertEquals(Integer.valueOf(523940), actualCompany.getNaicsCode());
		assertTrue(actualCompany.isTexasSitus());
	}

	@Test
	public void getProspectCompanyDetailsTest3() {
		Company company = prepareCompany();
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		String companyCode = company.getCode();
		BenExchngEnums benExchange = BenExchngEnums.getByExchangeId(GOOD_EXCHANGE);
		List<String> employeeHomeStates = new ArrayList<String>();
		employeeHomeStates.add("CA");
		Set<String> sdiStates = Set.of("CA", "NW");
		ProspectInfoResponse response = prospectInfoResponse(employeeHomeStates, null,false, null);
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();

		// method mocks
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyDao.findCompanyBy(anyString(), anyString(), anyString(), any(Date.class))).thenReturn(company);
		when(realmPlanYearService.getRealmPlanYearById(anyLong())).thenReturn(prepareRPY("Q4"));
		when(realmTypeService.findById(anyLong())).thenReturn(prepareRealm());
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI)).thenReturn(PROSPECT_API_URL);
		when(realmDataDao.getRealmCloneProgram(anyLong())).thenReturn(prepareRealmCloneProgData());
		when(RulesAndConfigsUtils.getSDIStates(anyLong())).thenReturn(sdiStates);
		when(companyBandCodesDao.getBandCodesByCompanyIdAndEffDate(anyLong(), anyString()))
				.thenReturn(null);
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(any(ProspectApiRequest.class)))
				.thenReturn(response);
		when(bandCodesService.getBandCodeByType(any(), any(), eq(BSSApplicationConstants.LIFE), any()))
				.thenReturn("160");
		when(bandCodesService.getBandCodeByType(any(), any(), eq(BSSApplicationConstants.DISABILITY), any()))
				.thenReturn("170");
		when(rateSystemService.getRateSystemRateType(any(Company.class))).thenReturn("REGIONAL");


		Company actualCompany = prospectCompanyServiceImpl.getProspectCompanyDetails(companyCode, benExchange);

		verify(psCompanyDao, times(0)).isTexasSitus(anyString(), any(Date.class));

		assertEquals("101", actualCompany.getRealmPlanYear().getCloneProgram());
		assertEquals(2, actualCompany.getSdiStates().size());
		assertTrue(sdiStates.containsAll(actualCompany.getSdiStates())
				&& actualCompany.getSdiStates().containsAll(sdiStates));
		assertNull(actualCompany.getBandCodes().getAetnaBandCode());
		assertNull(actualCompany.getBandCodes().getBcbsBandCode());
		assertEquals("160", actualCompany.getBandCodes().getLifeBandCode());
		assertEquals("170", actualCompany.getBandCodes().getDisBandCode());
		assertEquals("CA", actualCompany.getHeadQuatersState());
		assertEquals("93456", actualCompany.getZipCode());
		assertEquals("2024-12-31", actualCompany.getExpiryDate());
		assertEquals("Q-778977", actualCompany.getProposalId());
		assertEquals("Q4", actualCompany.getQuater());
		assertEquals(74, actualCompany.getRealmPlanYear().getId());
		assertEquals(null, actualCompany.getNaicsCode());
		assertTrue(actualCompany.isTexasSitus());
	}

	@Test
	public void updateProspectExpiryDateTest() {
		String companyCode = "testCompany";

		prospectCompanyServiceImpl.updateProspectExpiryDate(companyCode, "OMS", LocalDate.now());

		verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(any());
	}
	
	@Test
	public void getProspectCompanyDetails_CommonOwnerCompanyWithQuarterException_Test3() {

		Company company = prepareCompany();
		BenExchngEnums benExchange = BenExchngEnums.getByExchangeId(GOOD_EXCHANGE);
		String companyCode = company.getCode();
		company.setCommonOwnerCompanyCode("G48");	
		Set<String> sdiStates = new HashSet<>();
		List<CompanyBandCodes> companyBandCodesList = new ArrayList<>();
		ProspectInfoResponse response = prospectInfoResponse(null, "G48", true, "523940");
		List<RealmPlanYearDetailsDto> realmPlanYearDetailsDtos = buildRealmPlanYearDetailsDtoTrinetIII();
		Date bandEffDt = Utils.convertStringToDate("01/01/2024", BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY);
		
		when(realmPlanYearService.findByRealmId(exchangeIdCaptor.capture())).thenReturn(realmPlanYearDetailsDtos);
		when(companyDao.findCompanyBy("a1b2c3", benExchange.getBenExchng(), "Q1",
				Utils.convertStringToDate("10/11/2024", BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY)))
				.thenReturn(company);
	
		when(realmPlanYearService.getRealmPlanYearById(anyLong())).thenReturn(prepareRPY("Q1"));

		when(realmTypeService.findById(anyLong())).thenReturn(prepareRealm());
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI)).thenReturn(PROSPECT_API_URL);
		when(realmDataDao.getRealmCloneProgram(anyLong())).thenReturn(prepareRealmCloneProgData());
		when(RulesAndConfigsUtils.getSDIStates(anyLong())).thenReturn(sdiStates);
		when(companyBandCodesDao.getBandCodesByCompanyIdAndEffDate(anyLong(), anyString()))
				.thenReturn(companyBandCodesList);
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(any(ProspectApiRequest.class)))
				.thenReturn(response);
		when(rateSystemService.getRateSystemRateType(any(Company.class))).thenReturn("REGIONAL");

		Company actualCompany = prospectCompanyServiceImpl.getProspectCompanyDetails(companyCode, benExchange);

		verify(companyDao, times(1)).findCompanyBy("a1b2c3", benExchange.getBenExchng(), "Q1",
				Utils.convertStringToDate("10/11/2024", BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY));
		assertEquals("101", actualCompany.getRealmPlanYear().getCloneProgram());
		assertEquals(0, actualCompany.getSdiStates().size());
		assertTrue(actualCompany.getSdiStates().isEmpty());
		assertNotNull(actualCompany.getBandCodes());
		assertEquals("CA", actualCompany.getHeadQuatersState());
		assertEquals("Q1", actualCompany.getQuater());
		assertEquals("93456", actualCompany.getZipCode());
		assertEquals(null, actualCompany.getEmployeeRegions());
		assertEquals("2024-12-31", actualCompany.getExpiryDate());
		assertEquals(Integer.valueOf(523940), actualCompany.getNaicsCode());
		assertTrue(actualCompany.isTexasSitus());
	}

	private List<RealmPlanYearDetailsDto> buildRealmPlanYearDetailsDtoTrinetIII() {
		return List.of(
				RealmPlanYearDetailsDto.builder().id(74).realmId(3).quarter("Q4")
						.startDate(CommonUtils.formatStringToDate("2024-10-01", "yyyy-MM-dd"))
						.endDate(CommonUtils.formatStringToDate("2025-09-30", "yyyy-MM-dd")).build(),
				RealmPlanYearDetailsDto.builder().id(68).realmId(3).quarter("Q1")
						.startDate(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"))
						.endDate(CommonUtils.formatStringToDate("2024-12-31", "yyyy-MM-dd")).build());
	}
	
	private Company prepareCompany() {
		Company company = new Company();
		company.setCode("a1b2c3");
		return company;
	}

	private RealmPlanYear prepareRPY(String quarter) {
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(74);
		rpy.setRealmId(3);
		rpy.setOeQuarter(quarter);
		rpy.setPlanYearStart(java.sql.Date.valueOf("2024-10-01"));
		rpy.setPlanYearEnd(java.sql.Date.valueOf("2025-09-30"));
		rpy.setMinFunding(70);
		rpy.setMbgNew(true);
		rpy.setMbgRenewal(true);
		rpy.setAleAmount(ALE_AMOUNT);
		rpy.setAvgSalary(new BigDecimal("100000"));
		rpy.setAcaFplOpt(0);
		rpy.setMicrositeUrl("https://www.trinet.com/oe/trinet-iii-q4-bss");
		rpy.setK1Flag(true);
		rpy.setCloneProgram("098");
		return rpy;
	}

	private Realm prepareRealm() {
		Realm r = new Realm();
		r.setId(3);
		r.setDescription("Passport Clients");
		r.setRealmType("X");
		r.setPeoid("PAS");
		r.setBenExchange("TriNet III");
		return r;
	}

	private RealmCloneProgram prepareRealmCloneProgData() {
		RealmCloneProgram clone = new RealmCloneProgram();
		clone.setCloneProgram("101");
		clone.setCloneK1Program("10K");
		return clone;
	}

	private List<CompanyBandCodes> prepareCompanyBandCodes() {
		CompanyBandCodes companyBandCodes1 = new CompanyBandCodes();
		companyBandCodes1.setBandCodeType("AETNA");
		companyBandCodes1.setBandCodeValue("202");
		companyBandCodes1.setCompanyId(null);

		CompanyBandCodes companyBandCodes2 = new CompanyBandCodes();
		companyBandCodes2.setBandCodeType("BCBS");
		companyBandCodes2.setBandCodeValue("40");

		return List.of(companyBandCodes1, companyBandCodes2);

	}
	
	private ProspectInfoResponse prospectInfoResponse(List<String> employeeHomeStates, String commonOwnerComp,
			boolean benefitsQuarterException, String primaryNaicsCode) {
		return new ProspectInfoResponse("Test Company", "CA", employeeHomeStates, "93456", "2024-10-11",
				primaryNaicsCode, true, "Q-778977", "2024-12-31", false,
				commonOwnerComp, benefitsQuarterException, "2024-08-25");
	}
	

}