package com.trinet.ambis.service.unit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.mockito.InOrder;

import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.service.BandCodesService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.IndustryType;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.CompanyBandCodesDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDataDao;
import com.trinet.ambis.persistence.dao.hrp.HQExceptionDao;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.hrp.MandatoryRegionDao;
import com.trinet.ambis.persistence.dao.hrp.RealmCloneProgramDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.SchedMidYearFundingDataDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.HQException;
import com.trinet.ambis.persistence.model.HQExceptionsId;
import com.trinet.ambis.persistence.model.MandatoryRegion;
import com.trinet.ambis.persistence.model.MandatoryRegionPK;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmConfiguration;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.SchedMidYearFunding;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.EmployerEmployeePlansMappingService;
import com.trinet.ambis.service.MinFundExceptionService;
import com.trinet.ambis.service.RealmConfigurationService;
import com.trinet.ambis.service.RealmPlanYearRuleConfigService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.RealmRegionMinFundingService;
import com.trinet.ambis.service.RealmWaitPeriodService;
import com.trinet.ambis.service.RateSystemService;
import com.trinet.ambis.service.SchedTblService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.impl.CompanyServiceImpl;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.CompanyBandCodes;
import com.trinet.ambis.service.model.Industry;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.BssCoreServiceClient;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.Utils;
import com.trinet.common.DateUtils;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CompanyServiceImpl1Test extends ServiceUnitTest {

	@InjectMocks
	CompanyServiceImpl companyServiceImpl;

	@Mock
	CompanyDao companyDao;

	@Mock
	PsCompanyDao psCompanyDao;

	@Mock
	MandatoryRegionDao mandatoryRegionDao;

	@Mock
	SchedTblService schedTblService;

	@Mock
	StrategyService strategyService;

	@Mock
	RealmPlanYearService realmPlanYearService;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	RealmWaitPeriodService realmWaitPeriodService;

	@Mock
	EmployerEmployeePlansMappingService employerEmployeePlansMappingService;

	@Mock
	RealmCloneProgramDao realmCloneProgramDao;

	@Mock
	CompanyBandCodesDao companyBandCodesDao;

	@Mock
	SchedMidYearFundingDataDao schedMidYearFundingDataDao;

	@Mock
	RealmConfigurationService realmConfigurationService;

	@Mock
	HQExceptionDao hqExceptionDao;

	@Mock
	CompanyDataDao companyDataDao;

	@Mock
	RealmRegionMinFundingService realmRegionMinFundingService;

	@Mock
	MinFundExceptionService minFundExceptionService;

	@Mock
	RealmPlanYearRuleConfigService realmPlanYearRuleConfigService;

	@Mock
	HrpDao hrpDao;

	@Mock
	CacheService cacheService;

	@Mock
	BssCoreServiceClient bssCoreServiceClient;

	@Mock
	CompanyService companyService;

	@Mock
	BandCodesService bandCodesService;

	@Mock
	RateSystemService rateSystemService;

	@Mock
    ProcessStatusService processStatusService;

    private MockedStatic<AppRulesAndConfigsUtils> appRulesAndConfigsUtilsMock;
    private MockedStatic<CompanyServiceHelper> companyServiceHelperMock;
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMock;

    @Before
    public void setUp() {
        appRulesAndConfigsUtilsMock = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
        companyServiceHelperMock = Mockito.mockStatic(CompanyServiceHelper.class);
        rulesAndConfigsUtilsMock = Mockito.mockStatic(RulesAndConfigsUtils.class);
		appRulesAndConfigsUtilsMock.when(AppRulesAndConfigsUtils::isBssOutputPhase2Enabled).thenReturn(true);
        when(rateSystemService.getRateSystemRateType(any(Company.class))).thenReturn("REGIONAL");
    }

    @After
    public void tearDown() {
        appRulesAndConfigsUtilsMock.close();
        companyServiceHelperMock.close();
        rulesAndConfigsUtilsMock.close();
    }
	private static final String CODE = "5R9";
	private static final String EMP_ID = "1111";
	private static final int NEXT_YEAR = DateUtils.getCurrentYear()+1;
	private static final int NEXT_YEAR_PLUS_1 = DateUtils.getCurrentYear()+2;
	private static final String COMP_PLAN_START_DATE = "01-FEB-"+NEXT_YEAR;
	private static final Date MID_YEAR_FUNDING_EFF_DATE = Utils.convertStringToDate("01-OCT-"+NEXT_YEAR, Constants.DATE_FORMAT);
	private static final String QUARTER = "IV";
	private static final String HEAD_QTR_STATE = "FL";
	private static final String COMPANY_NAME = "Trinet Group";
	private static final String KAISER_BAND_CODE = "KBC";
	private static final boolean IS_PAYROLL_PROCESSED_TRUE = true;
	private static final boolean IS_PAYROLL_PROCESSED_FALSE = false;
	private static final boolean TRANSITION_PERIOD = true;
	private static final int REALM_ID = 2222;
	private static final long REALM_PLAN_YR_ID = 3333L;
	private static final long  nextRealmPlanYrId = 4444L;
	private static final String PEO_ID = "PAS";
	private static final String CURRENT_PLAN_YR_START_STR = "01-JAN-"+NEXT_YEAR;
	private static final Date CURRENT_PLAN_YR_START_DT = Utils.convertStringToDate("01-JAN-"+NEXT_YEAR, Constants.DATE_FORMAT);
	private static final Date NEXT_PLAN_YR_START_DT = Utils.convertStringToDate("01-JAN-"+NEXT_YEAR_PLUS_1, Constants.DATE_FORMAT);


	/*
	 * Test setUpRealmAndRealmPlanYearTest(). isRenewalCompany true
	 */
	@Test
	public void setUpRealmAndRealmPlanYearForRenewalCompanyTest() {
		String benExchange = Constants.PASSPORT_BEN_EXCHANGE;
		boolean realmPlanYrIsMgbNew = true;
		long realmId = 1L;

		Company basicCompany = prepareBasicCompany(CURRENT_PLAN_YR_START_STR, QUARTER, IS_PAYROLL_PROCESSED_TRUE);
		basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		Company psCompany = prepareCompany(COMPANY_NAME, CURRENT_PLAN_YR_START_STR, QUARTER, IS_PAYROLL_PROCESSED_TRUE,
				TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		Company aleHistCompany = prepareCompany(COMPANY_NAME, CURRENT_PLAN_YR_START_STR, QUARTER, IS_PAYROLL_PROCESSED_TRUE,
				TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		aleHistCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
		aleHistCompany.setEligAle( true );

		Company bssCompany = new Company();
		Realm realm = new Realm();
		realm.setId(realmId);
		bssCompany.setRealm(realm);
		SchedTbl schedTbl = new SchedTbl();
		List<RealmConfiguration> realmConfigs = new ArrayList<>();

		/*
		 * GIVEN nextRealmPlanYear is null and history is false
		 */
		boolean history = false;
		RealmPlanYear nextRealmPlanYear = null;
		boolean renewalPeriod = true;
		boolean mbgRenewal = false;
		final BigDecimal historyAleAmount = BigDecimal.valueOf(100.04);

		RealmPlanYear currentRealmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal, CURRENT_PLAN_YR_START_DT);
		currentRealmPlanYear.setAleAmount(historyAleAmount);

		Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(bssCompany);
		Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, CURRENT_PLAN_YR_START_DT)).thenReturn(psCompany);
		Mockito.when(psCompanyDao.getCompanyActualHeadCount(CODE)).thenReturn(2);
		Mockito.when(realmPlanYearService.getCurrentRealmPlanYear(REALM_ID, QUARTER)).thenReturn(currentRealmPlanYear);
		Mockito.when(realmPlanYearService.getNextRealmPlanYear(currentRealmPlanYear)).thenReturn(nextRealmPlanYear);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(currentRealmPlanYear)).thenReturn(currentRealmPlanYear);
		Mockito.when(schedTblService.getCalcuatedScheduleDates(CODE, QUARTER, REALM_PLAN_YR_ID)).thenReturn(schedTbl);
		when(CompanyServiceHelper.isRenewalOpen(basicCompany, schedTbl, currentRealmPlanYear))
				.thenReturn(renewalPeriod);
		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(REALM_PLAN_YR_ID, CODE)).thenReturn(prepareHQException());
		when( CompanyServiceHelper.isClientCompanyPattern( any() ) ).thenCallRealMethod();

		// when
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(true, psCompany.isRenewalOpen());
		assertEquals(false, psCompany.isMbg());
		assertEquals(currentRealmPlanYear, psCompany.getRealmPlanYear());
		assertEquals(REALM_PLAN_YR_ID, psCompany.getRealmPlanYearId());
		assertEquals("MN", psCompany.getHeadQuatersState());
		assertEquals("11111", psCompany.getZipCode());
		assertEquals(historyAleAmount, psCompany.getAleAmountHistory());

		/*
		 * GIVEN nextRealmPlanYear is not null, renewal period true and transition
		 * period false
		 */
		basicCompany.setTransitionPeriod(false);
		nextRealmPlanYear = prepareRealmPlanYr(nextRealmPlanYrId, realmPlanYrIsMgbNew, mbgRenewal, NEXT_PLAN_YR_START_DT);
		currentRealmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal, CURRENT_PLAN_YR_START_DT);

		Mockito.when(realmPlanYearService.getCurrentRealmPlanYear(REALM_ID, QUARTER)).thenReturn(currentRealmPlanYear);
		Mockito.when(realmPlanYearService.getNextRealmPlanYear(currentRealmPlanYear)).thenReturn(nextRealmPlanYear);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(nextRealmPlanYear)).thenReturn(currentRealmPlanYear);
		Mockito.when(schedTblService.getCalcuatedScheduleDates(CODE, QUARTER, nextRealmPlanYrId)).thenReturn(schedTbl);
		when(CompanyServiceHelper.isRenewalOpen(basicCompany, schedTbl, nextRealmPlanYear))
				.thenReturn(renewalPeriod);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, nextRealmPlanYrId)).thenReturn(bssCompany);
		Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, nextRealmPlanYear.getPlanYearStart())).thenReturn(psCompany);
		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(nextRealmPlanYrId, CODE)).thenReturn(Optional.empty());

		// when
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(nextRealmPlanYrId, psCompany.getRealmPlanYearId());
		assertEquals(nextRealmPlanYear, psCompany.getRealmPlanYear());

		/*
		 * GIVEN nextRealmPlanYear is not null, renewalPeriod false and transition
		 * period true
		 */
		basicCompany.setTransitionPeriod(true);
		schedTbl = new SchedTbl();
		nextRealmPlanYear = prepareRealmPlanYr(nextRealmPlanYrId, realmPlanYrIsMgbNew, mbgRenewal, NEXT_PLAN_YR_START_DT);
		currentRealmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal, CURRENT_PLAN_YR_START_DT);

		Mockito.when(realmPlanYearService.getCurrentRealmPlanYear(REALM_ID, QUARTER)).thenReturn(currentRealmPlanYear);
		Mockito.when(realmPlanYearService.getNextRealmPlanYear(currentRealmPlanYear)).thenReturn(nextRealmPlanYear);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(nextRealmPlanYear)).thenReturn(currentRealmPlanYear);
		when(CompanyServiceHelper.isRenewalOpen(basicCompany, schedTbl, nextRealmPlanYear))
				.thenReturn(renewalPeriod);
		Mockito.when(schedTblService.getCalcuatedScheduleDates(CODE, QUARTER, nextRealmPlanYrId)).thenReturn(schedTbl);

		// when
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(nextRealmPlanYrId, psCompany.getRealmPlanYearId());
		assertEquals(nextRealmPlanYear, psCompany.getRealmPlanYear());

		/*
		 * GIVEN nextRealmPlanYear is not null, renewalPeriod false and transitionPeriod
		 * false
		 */
		renewalPeriod = false;
		basicCompany.setTransitionPeriod(false);
		schedTbl = new SchedTbl();
		nextRealmPlanYear = prepareRealmPlanYr(nextRealmPlanYrId, realmPlanYrIsMgbNew, mbgRenewal, NEXT_PLAN_YR_START_DT);
		currentRealmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal, CURRENT_PLAN_YR_START_DT);

		Mockito.when(realmPlanYearService.getCurrentRealmPlanYear(REALM_ID, QUARTER)).thenReturn(currentRealmPlanYear);
		Mockito.when(realmPlanYearService.getNextRealmPlanYear(currentRealmPlanYear)).thenReturn(nextRealmPlanYear);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(currentRealmPlanYear)).thenReturn(currentRealmPlanYear);
		when(CompanyServiceHelper.isRenewalOpen(basicCompany, schedTbl, nextRealmPlanYear))
				.thenReturn(renewalPeriod);
		Mockito.when(schedTblService.getCalcuatedScheduleDates(CODE, QUARTER, nextRealmPlanYrId)).thenReturn(schedTbl);

		// when
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(REALM_PLAN_YR_ID, basicCompany.getRealmPlanYearId());
		assertEquals(currentRealmPlanYear, basicCompany.getRealmPlanYear());

		/*
		 * GIVEN nextRealmPlanYear is not null, renewalPeriod true and history true
		 */
		Company historyComp = new Company();
		BandCodes bandCodes = new BandCodes();
		bandCodes.setAetnaBandCode("aetnaBandCode");
		historyComp.setBandCodes(bandCodes );
		renewalPeriod = true;
		basicCompany.setTransitionPeriod(false);
		history = true;
		schedTbl = new SchedTbl();
		nextRealmPlanYear = prepareRealmPlanYr(nextRealmPlanYrId, realmPlanYrIsMgbNew, mbgRenewal, NEXT_PLAN_YR_START_DT);
		nextRealmPlanYear.setAleAmount(historyAleAmount);
		currentRealmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal, CURRENT_PLAN_YR_START_DT);
		currentRealmPlanYear.setAleAmount(historyAleAmount);
		psCompany.setEligAle(true);

		Mockito.when(realmPlanYearService.getCurrentRealmPlanYear(REALM_ID, QUARTER)).thenReturn(currentRealmPlanYear);
		when(CompanyServiceHelper.isRenewalOpen(basicCompany, schedTbl, nextRealmPlanYear))
				.thenReturn(renewalPeriod);
		Mockito.when(realmPlanYearService.getNextRealmPlanYear(currentRealmPlanYear)).thenReturn(nextRealmPlanYear);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(nextRealmPlanYear)).thenReturn(currentRealmPlanYear);
		Mockito.when(schedTblService.getCalcuatedScheduleDates(CODE, QUARTER, nextRealmPlanYrId)).thenReturn(schedTbl);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, currentRealmPlanYear.getId())).thenReturn(bssCompany);
		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(currentRealmPlanYear.getId(), CODE)).thenReturn(Optional.empty());
		Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(Mockito.any(Company.class), Mockito.any(Date.class)))
		.thenReturn(psCompany);

		// when
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(currentRealmPlanYear.getId(), basicCompany.getRealmPlanYearId());
		assertEquals(currentRealmPlanYear, basicCompany.getRealmPlanYear());
		assertEquals(historyAleAmount, psCompany.getAleAmountHistory());
	}

	/*
	 * Test setUpRealmAndRealmPlanYearTest(). net new company
	 */
	@Test
	public void setUpRealmAndRealmPlanYearForNonRenewalCompanyTest() throws Exception {
		String passportBenExchange = Constants.PASSPORT_BEN_EXCHANGE;
		boolean isMgbNew = true;
		// realmPlanYrId will be 0 for non renewalCompany
		// TODO need to change this once the business logic is fixed.
		long realmPlanYrId = 0;
		long prevRealmPlanYrId = 1111;

		Company basicCompany = prepareBasicCompany(COMP_PLAN_START_DATE, QUARTER, IS_PAYROLL_PROCESSED_TRUE);
		basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, passportBenExchange));
		basicCompany.setPayrollProcessed(false);

		Company psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, IS_PAYROLL_PROCESSED_TRUE,
				TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, passportBenExchange));

		Company bssCompany = new Company();
		SchedTbl schedTbl = new SchedTbl();

		/*
		 * GIVEN realmPlanYear is not null
		 */
		boolean history = false;
		boolean mbgRenewal = false;

		RealmPlanYear realmPlanYear = prepareRealmPlanYr(realmPlanYrId, isMgbNew, mbgRenewal, CURRENT_PLAN_YR_START_DT);

		Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, realmPlanYrId)).thenReturn(bssCompany);
		Mockito.when(psCompanyDao.getCompanyActualHeadCount(CODE)).thenReturn(0);
		Mockito.when(realmPlanYearService.getRealmPlanYear(REALM_ID, QUARTER,
				Utils.convertStringToDate(COMP_PLAN_START_DATE, Constants.DATE_FORMAT))).thenReturn(realmPlanYear);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(realmPlanYear)).thenReturn(realmPlanYear);
		Mockito.when(schedTblService.getCalcuatedScheduleDates(CODE, QUARTER, realmPlanYrId)).thenReturn(schedTbl);
		when(realmPlanYearService.getMaxRealmPlanYear(REALM_ID, QUARTER)).thenReturn(realmPlanYear);
		when(psCompanyDao.isNewBandsAvailable(basicCompany, realmPlanYear.getPlanYearStart())).thenReturn(true);
		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(realmPlanYrId, CODE)).thenReturn(Optional.empty());
		Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany,
				Utils.convertStringToDate(COMP_PLAN_START_DATE, Constants.DATE_FORMAT))).thenReturn(psCompany);
		when( CompanyServiceHelper.isClientCompanyPattern( any() ) ).thenCallRealMethod();

		// when
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(true, psCompany.isMbg());
		assertEquals(false, psCompany.isRenewalOpen());
		assertEquals(schedTbl, psCompany.getSchedTbl());
		assertEquals(true, psCompany.getSchedTbl().isPayrollProcessed());
		assertEquals(realmPlanYear, psCompany.getRealmPlanYear());
		assertEquals(realmPlanYrId, psCompany.getRealmPlanYear().getId());
		assertEquals(0, psCompany.getRealmPlanYear().getMinFunding());

		/*
		 * GIVEN realmPlanYear is null
		 */
		realmPlanYear = null;
		RealmPlanYear latestRealmPlan = prepareRealmPlanYr(prevRealmPlanYrId, isMgbNew, mbgRenewal, CURRENT_PLAN_YR_START_DT);
		schedTbl = null;
		basicCompany.getRealm().setBenExchange(passportBenExchange);

		ArgumentCaptor<Company> companyArgCaptor = ArgumentCaptor.forClass(Company.class);
		ArgumentCaptor<RealmPlanYear> realmPlanYearArgCaptor = ArgumentCaptor.forClass(RealmPlanYear.class);

		Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, prevRealmPlanYrId)).thenReturn(bssCompany);
		Mockito.when(realmPlanYearService.getRealmPlanYear(REALM_ID, QUARTER,
				Utils.convertStringToDate(COMP_PLAN_START_DATE, Constants.DATE_FORMAT))).thenReturn(realmPlanYear);
		Mockito.when(realmPlanYearService.getLatestRealmPlanYear(REALM_ID, QUARTER,
				Utils.convertStringToDate(COMP_PLAN_START_DATE, Constants.DATE_FORMAT))).thenReturn(latestRealmPlan);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(latestRealmPlan)).thenReturn(latestRealmPlan);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(latestRealmPlan)).thenReturn(latestRealmPlan);
		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(prevRealmPlanYrId, CODE)).thenReturn(Optional.empty());
        companyServiceHelperMock.when(() -> CompanyServiceHelper.isClientCompanyPattern( any() ) ).thenCallRealMethod();

		// when
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		CompanyServiceHelper.populateScheduleTableData(psCompany, latestRealmPlan);
		assertEquals(latestRealmPlan, psCompany.getRealmPlanYear());
		assertEquals(prevRealmPlanYrId, psCompany.getRealmPlanYear().getId());
		assertEquals(COMP_PLAN_START_DATE, psCompany.getBenefitStartDate());
	}

	/*
	 * Test setUser() in getCompanyDetails() net new client
	 */
	@Test
	public void setUserTest() throws Exception {
		boolean history = false;
		String benExchange = Constants.PASSPORT_BEN_EXCHANGE;
		boolean realmPlanYrIsMgbNew = true;
		boolean mbgRenewal = false;

		Company basicCompany = prepareBasicCompany(COMP_PLAN_START_DATE, QUARTER, IS_PAYROLL_PROCESSED_FALSE);
		basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		Company psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, IS_PAYROLL_PROCESSED_FALSE,
				TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal, CURRENT_PLAN_YR_START_DT);

		Company bssCompany = new Company();

		Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
		Mockito.when(realmPlanYearService.getRealmPlanYear(REALM_ID, QUARTER,
				Utils.convertStringToDate(COMP_PLAN_START_DATE, Constants.DATE_FORMAT))).thenReturn(realmPlanYear);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(realmPlanYear)).thenReturn(realmPlanYear);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(bssCompany);

		// Given BDMUser = false, BMGUser = false, CSAUser = false
		Mockito.when(psCompanyDao.isBDMUser(EMP_ID, CODE)).thenReturn(false);
		Mockito.when(psCompanyDao.isBMGUser(EMP_ID)).thenReturn(false);
		Mockito.when(psCompanyDao.isCSAUser(EMP_ID, psCompany.getRealm().getId())).thenReturn(false);
		when(realmPlanYearService.getMaxRealmPlanYear(REALM_ID, QUARTER)).thenReturn(realmPlanYear);
		when(psCompanyDao.isNewBandsAvailable(basicCompany, realmPlanYear.getPlanYearStart())).thenReturn(true);
		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(REALM_PLAN_YR_ID, CODE)).thenReturn(Optional.empty());
		Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany,
				Utils.convertStringToDate(basicCompany.getPlanStartDate(), Constants.DATE_FORMAT)))
				.thenReturn(psCompany);
		when( CompanyServiceHelper.isClientCompanyPattern( any() ) ).thenReturn( true );

		// When
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(false, basicCompany.isBMGUser());
		assertEquals(false, basicCompany.isCSAUser());

		// Given BDMUser = true, BMGUser = true, CSAUser = true
		Mockito.when(psCompanyDao.isBDMUser(EMP_ID, CODE)).thenReturn(true);
		Mockito.when(psCompanyDao.isBMGUser(EMP_ID)).thenReturn(true);
		Mockito.when(psCompanyDao.isCSAUser(EMP_ID, psCompany.getRealm().getId())).thenReturn(true);

		// When
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(false, basicCompany.isBMGUser());
		assertEquals(false, basicCompany.isCSAUser());

		// Given DMUser = true, BMGUser = true, CSAUser = false
		Mockito.when(psCompanyDao.isBDMUser(EMP_ID, CODE)).thenReturn(true);
		Mockito.when(psCompanyDao.isBMGUser(EMP_ID)).thenReturn(true);
		Mockito.when(psCompanyDao.isCSAUser(EMP_ID, psCompany.getRealm().getId())).thenReturn(false);

		// When
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(false, basicCompany.isBMGUser());
		assertEquals(false, basicCompany.isCSAUser());

		// Given DMUser = false, BMGUser = true, CSAUser = true
		Mockito.when(psCompanyDao.isBDMUser(EMP_ID, CODE)).thenReturn(false);
		Mockito.when(psCompanyDao.isBMGUser(EMP_ID)).thenReturn(true);
		Mockito.when(psCompanyDao.isCSAUser(EMP_ID, psCompany.getRealm().getId())).thenReturn(true);

		// When
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(false, basicCompany.isBMGUser());
		assertEquals(true, basicCompany.isCSAUser());

		// Given DMUser = false, BMGUser = true, CSAUser = false
		Mockito.when(psCompanyDao.isBDMUser(EMP_ID, CODE)).thenReturn(false);
		Mockito.when(psCompanyDao.isBMGUser(EMP_ID)).thenReturn(true);
		Mockito.when(psCompanyDao.isCSAUser(EMP_ID, psCompany.getRealm().getId())).thenReturn(false);

		// When
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(true, basicCompany.isBMGUser());
		assertEquals(false, basicCompany.isCSAUser());

		// Given DMUser = true, BMGUser = false, CSAUser = false
		Mockito.when(psCompanyDao.isBDMUser(EMP_ID, CODE)).thenReturn(true);
		Mockito.when(psCompanyDao.isBMGUser(EMP_ID)).thenReturn(false);
		Mockito.when(psCompanyDao.isCSAUser(EMP_ID, psCompany.getRealm().getId())).thenReturn(false);

		// When
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(false, basicCompany.isBMGUser());
		assertEquals(false, basicCompany.isCSAUser());

		// Given DMUser = false, BMGUser = false, CSAUser = true
		Mockito.when(psCompanyDao.isBDMUser(EMP_ID, CODE)).thenReturn(false);
		Mockito.when(psCompanyDao.isBMGUser(EMP_ID)).thenReturn(false);
		Mockito.when(psCompanyDao.isCSAUser(EMP_ID, psCompany.getRealm().getId())).thenReturn(true);

		// When
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(false, basicCompany.isBMGUser());
		assertEquals(true, basicCompany.isCSAUser());

		// Given DMUser = true, BMGUser = false, CSAUser = true
		Mockito.when(psCompanyDao.isBDMUser(EMP_ID, CODE)).thenReturn(true);
		Mockito.when(psCompanyDao.isBMGUser(EMP_ID)).thenReturn(false);
		Mockito.when(psCompanyDao.isCSAUser(EMP_ID, psCompany.getRealm().getId())).thenReturn(true);

		// When
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(false, basicCompany.isBMGUser());
		assertEquals(false, basicCompany.isCSAUser());

		//Given BDM User = false, BenAdvisor = true
		Mockito.when(psCompanyDao.isBDMUser(EMP_ID, CODE)).thenReturn(false);
		Mockito.when(psCompanyDao.isBenAdvisorUser(EMP_ID, CODE)).thenReturn(true);

		// When
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(true, basicCompany.isBenAdvisorUser());

		//Given BDM User = true, BenAdvisor = false
		Mockito.when(psCompanyDao.isBDMUser(EMP_ID, CODE)).thenReturn(true);
		Mockito.when(psCompanyDao.isBenAdvisorUser(EMP_ID, CODE)).thenReturn(false);

		// When
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(false, basicCompany.isBenAdvisorUser());


		//Given BDM User = true, BenAdvisor = true
		Mockito.when(psCompanyDao.isBDMUser(EMP_ID, CODE)).thenReturn(true);
		Mockito.when(psCompanyDao.isBenAdvisorUser(EMP_ID, CODE)).thenReturn(true);

		// When
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(false, basicCompany.isBenAdvisorUser());

		//Given BDM User = false, BenAdvisor = false
		Mockito.when(psCompanyDao.isBDMUser(EMP_ID, CODE)).thenReturn(false);
		Mockito.when(psCompanyDao.isBenAdvisorUser(EMP_ID, CODE)).thenReturn(false);

		// When
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		assertEquals(false, basicCompany.isBenAdvisorUser());

	}

	@Test(expected = RuntimeException.class)
	public void getCompanyDetailsThrowsBssApplicationExceptionTest() {
		// Given
		boolean history = false;
		String benExchange = Constants.PASSPORT_BEN_EXCHANGE;
		boolean realmPlanYrIsMgbNew = true;
		boolean mbgRenewal = false;
		HQException hqException= new HQException();
		HQExceptionsId id = new HQExceptionsId();
		id.setCompany(CODE);
		id.setRealmYrId(REALM_PLAN_YR_ID);
		hqException.setId(id);
		hqException.setHqState(HEAD_QTR_STATE);
		hqException.setPostalCode("29288");
		Optional<HQException> hqExceptionOptional = Optional.of(hqException);

		Company basicCompany = prepareBasicCompany(COMP_PLAN_START_DATE, QUARTER, IS_PAYROLL_PROCESSED_FALSE);
		basicCompany.setRenewalCompany(true);
		basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		Company psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, IS_PAYROLL_PROCESSED_FALSE,
				TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal, CURRENT_PLAN_YR_START_DT);

		// when
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);
	}

	@Test
	public void getCompanyDetailsCreateUpdateBssCompanyTest() {
		// Given
		boolean history = false;
		String benExchange = Constants.PASSPORT_BEN_EXCHANGE;
		boolean realmPlanYrIsMgbNew = true;
		boolean mbgRenewal = false;
		Set<String> bssCompanyRegions  = new HashSet<String>();
		bssCompanyRegions.add("TX");
		Company bssCompany = null;
		Company createdBssCompany = new Company();
		Company basicCompany = prepareBasicCompany(COMP_PLAN_START_DATE, QUARTER, IS_PAYROLL_PROCESSED_FALSE);
		basicCompany.setRenewalCompany(true);
		basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		Company psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, IS_PAYROLL_PROCESSED_FALSE,
				TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal, CURRENT_PLAN_YR_START_DT);

        when(AppRulesAndConfigsUtils.getProspectConversionCutOffDate()).thenReturn("1-OCT-2025");
		Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
		Mockito.when(realmPlanYearService.getRealmPlanYear(REALM_ID, QUARTER,
				Utils.convertStringToDate(COMP_PLAN_START_DATE, Constants.DATE_FORMAT))).thenReturn(realmPlanYear);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(realmPlanYear)).thenReturn(realmPlanYear);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(bssCompany);
		when(CompanyServiceHelper.createBssCompany(basicCompany)).thenReturn(createdBssCompany);
		Mockito.when(companyDao.saveAndFlush(createdBssCompany)).thenReturn(new Company());
		when(companyDataDao.getRegionsByCompanyId(createdBssCompany.getId())).thenReturn(bssCompanyRegions);
		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(REALM_PLAN_YR_ID, CODE)).thenReturn(Optional.empty());
		when(realmPlanYearService.getMaxRealmPlanYear(REALM_ID, QUARTER)).thenReturn(realmPlanYear);
		when(psCompanyDao.isNewBandsAvailable(basicCompany, realmPlanYear.getPlanYearStart())).thenReturn(true);		// when

		Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany,
				Utils.convertStringToDate(basicCompany.getPlanStartDate(), Constants.DATE_FORMAT)))
				.thenReturn(psCompany);
		when( CompanyServiceHelper.isClientCompanyPattern( any() ) ).thenCallRealMethod();

		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		CompanyServiceHelper.createBssCompany(basicCompany);
		verify(companyDao, times(1)).saveAndFlush(createdBssCompany);
	}

	@Test(expected = RuntimeException.class)
	public void getCompanyDetailsCreateUpdateBssCompanyThrowsExceptionTest() {
		// Given
		boolean history = false;
		String benExchange = Constants.PASSPORT_BEN_EXCHANGE;

		Company createdBssCompany = new Company();
		Company basicCompany = prepareBasicCompany(COMP_PLAN_START_DATE, QUARTER, IS_PAYROLL_PROCESSED_TRUE);
		basicCompany.setRenewalCompany(true);
		basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		Company psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, IS_PAYROLL_PROCESSED_TRUE,
				TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));


		when(CompanyServiceHelper.createBssCompany(basicCompany)).thenReturn(createdBssCompany);

		// when
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);
	}

	@Test
	public void getCompanyDetailsVerifyBandCodesForNonRenewalCompanyTest() {
		// Given
		boolean history = false;
		String benExchange = Constants.PASSPORT_BEN_EXCHANGE;
		boolean realmPlanYrIsMgbNew = true;
		boolean mbgRenewal = false;
		Date effdt = Utils.convertStringToDate(COMP_PLAN_START_DATE, Constants.DATE_FORMAT);
		final BandCodes bandCodes = new BandCodes();

		Company basicCompany = prepareBasicCompany(COMP_PLAN_START_DATE, QUARTER, IS_PAYROLL_PROCESSED_FALSE);
		basicCompany.setRenewalCompany(false);
		basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		Company psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, IS_PAYROLL_PROCESSED_FALSE,
				TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
		psCompany.setBandCodes(bandCodes);

		RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal, CURRENT_PLAN_YR_START_DT);

		Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
		Mockito.when(realmPlanYearService.getRealmPlanYear(REALM_ID, QUARTER, effdt)).thenReturn(realmPlanYear);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(realmPlanYear)).thenReturn(realmPlanYear);
		Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany,
				Utils.convertStringToDate(basicCompany.getPlanStartDate(), Constants.DATE_FORMAT)))
				.thenReturn(psCompany);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(new Company());
		when(realmPlanYearService.getMaxRealmPlanYear(REALM_ID, QUARTER)).thenReturn(realmPlanYear);
		when(psCompanyDao.isNewBandsAvailable(basicCompany, realmPlanYear.getPlanYearStart())).thenReturn(true);
		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(REALM_PLAN_YR_ID, CODE)).thenReturn(Optional.empty());
		when( CompanyServiceHelper.isClientCompanyPattern( any() ) ).thenCallRealMethod();

		// when
		companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		verify(psCompanyDao, times(1)).getCompanyDetailsByEffdt(any(Company.class), any(Date.class));

	}

	@Test
	public void getCompanyDetailsBssBandCodeTest() {
		/*
		 * GIVEN Scenario 1 : bssBandCodes is null
		 */
		String benExchange = Constants.PASSPORT_BEN_EXCHANGE;
		long bssCompanyId = 1234L;
		long bssCompanyId1 = 12345L;
		long bssCompanyId2 = 123456L;
		boolean history = false;
		boolean realmPlanYrIsMgbNew = true;
		boolean mbgRenewal = false;
		SchedTbl schedTbl = null;
		boolean renewalPeriod = true;

		Company bssCompany = new Company();
		bssCompany.setId(bssCompanyId);

		Company basicCompany = prepareBasicCompany(CURRENT_PLAN_YR_START_STR, QUARTER, IS_PAYROLL_PROCESSED_TRUE);
		basicCompany.setQuater(QUARTER);
		basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		Company psCompany = prepareCompany(COMPANY_NAME, CURRENT_PLAN_YR_START_STR, QUARTER, IS_PAYROLL_PROCESSED_TRUE,
				TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		RealmPlanYear nextRealmPlanYear = prepareRealmPlanYr(nextRealmPlanYrId, realmPlanYrIsMgbNew, mbgRenewal, NEXT_PLAN_YR_START_DT);
		RealmPlanYear currentRealmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal, CURRENT_PLAN_YR_START_DT);
		List<CompanyBandCodes> bssBandCodes = null;
		List<CompanyBandCodes> psBandCode = new ArrayList<>();

		Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(bssCompany);
		Mockito.when(realmPlanYearService.getCurrentRealmPlanYear(REALM_ID, QUARTER)).thenReturn(currentRealmPlanYear);
		Mockito.when(realmPlanYearService.getNextRealmPlanYear(currentRealmPlanYear)).thenReturn(nextRealmPlanYear);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(currentRealmPlanYear))
		.thenReturn(currentRealmPlanYear);
		Mockito.when(schedTblService.getCalcuatedScheduleDates(CODE, QUARTER, nextRealmPlanYear.getId())).thenReturn(schedTbl);
		when(CompanyServiceHelper.isRenewalOpen(basicCompany, schedTbl, currentRealmPlanYear))
				.thenReturn(renewalPeriod);
		when(CompanyServiceHelper.getBssBandCodeList(bssCompanyId, null)).thenReturn(psBandCode);
		when( CompanyServiceHelper.isClientCompanyPattern( any() ) ).thenReturn( true );
		Mockito.when(companyBandCodesDao.getBandCodesByCompanyId(bssCompanyId)).thenReturn(bssBandCodes);
		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(REALM_PLAN_YR_ID, CODE)).thenReturn(Optional.empty());
		Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, currentRealmPlanYear.getPlanYearStart()))
				.thenReturn(psCompany);

		Company actualCompany = companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		verify(companyBandCodesDao, times(1)).insertUpdateCompanyBandCodes(bssCompanyId, psBandCode);
		assertEquals(false, actualCompany.isBandCodeUpdated());

		/*
		 * GIVEN Scenario 2 : bssBandCodes is empty
		 */
		bssBandCodes = new ArrayList<>();
		psBandCode = new ArrayList<>();
		bssCompany = new Company();
		bssCompany.setId(bssCompanyId1);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(bssCompany);
		when(CompanyServiceHelper.getBssBandCodeList(bssCompanyId1, null)).thenReturn(psBandCode);

		// when
		actualCompany = companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		verify(companyBandCodesDao, times(1)).insertUpdateCompanyBandCodes(bssCompanyId1, psBandCode);
		assertEquals(false, actualCompany.isBandCodeUpdated());

		/*
		 * GIVEN Scenario 3 : bssBandCodes is not null and not empty + compareBandCodes
		 * is false
		 */
		reset(cacheService);
		bssBandCodes = new ArrayList<>();
		CompanyBandCodes code = prepareBandCode();
		bssBandCodes.add(code);
		psBandCode = new ArrayList<>();
		bssCompany = new Company();
		bssCompany.setId(bssCompanyId2);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(bssCompany);
		when(CompanyServiceHelper.getBssBandCodeList(bssCompanyId2, null)).thenReturn(psBandCode);
		Mockito.when(companyBandCodesDao.getBandCodesByCompanyId(bssCompanyId2)).thenReturn(bssBandCodes);
		when(CompanyServiceHelper.compareBandCodes(bssBandCodes, psBandCode)).thenReturn(false);

		// when
		actualCompany = companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		verify(companyBandCodesDao, times(0)).insertUpdateCompanyBandCodes(bssCompanyId2, psBandCode);
		assertEquals(true, actualCompany.isBandCodeUpdated());
		verify(cacheService, times(1)).invalidateOutofDateCache(bssCompany);

		/*
		 * GIVEN Scenario 4 : bssBandCodes is not null and not empty + compareBandCodes
		 * is true
		 */
		reset(cacheService);
		when(CompanyServiceHelper.compareBandCodes(bssBandCodes, psBandCode)).thenReturn(true);

		// when
		actualCompany = companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		verify(companyBandCodesDao, times(0)).insertUpdateCompanyBandCodes(bssCompanyId2, psBandCode);
		assertEquals(false, actualCompany.isBandCodeUpdated());

		verify(cacheService, times(1)).invalidateOutofDateCache(bssCompany);
	}

	/*
	 * GIVEN Scenario 1 : active mid-year funding is present and User is TMT
	 */
	@Test
	public void getCompanyDetailsMidYearFundingTest1() {
		String benExchange = Constants.PASSPORT_BEN_EXCHANGE;
		long companyId = 1234L;
		boolean history = false;
		Date currentDate = new Date();

		Company basicCompany = prepareBasicCompany(COMP_PLAN_START_DATE, QUARTER, IS_PAYROLL_PROCESSED_TRUE);
		basicCompany.setId(companyId);
		basicCompany.setQuater(QUARTER);
		basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		Company psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, IS_PAYROLL_PROCESSED_TRUE,
				TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		RealmPlanYear currentRealmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, true, true, CURRENT_PLAN_YR_START_DT);
		List<CompanyBandCodes> bssBandCodes = null;
		List<CompanyBandCodes> psBandCode = new ArrayList<>();

		SchedMidYearFunding smf = new SchedMidYearFunding();
		smf.setMidYearFundingEffDate(MID_YEAR_FUNDING_EFF_DATE);

        when(AppRulesAndConfigsUtils.getProspectConversionCutOffDate()).thenReturn("1-OCT-2025");
		Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
		Mockito.when(realmPlanYearService.getCurrentRealmPlanYear(REALM_ID, QUARTER)).thenReturn(currentRealmPlanYear);
		Mockito.when(realmPlanYearService.getNextRealmPlanYear(currentRealmPlanYear)).thenReturn(null);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(currentRealmPlanYear))
		.thenReturn(currentRealmPlanYear);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(null);
		Mockito.when(companyDao.saveAndFlush(basicCompany)).thenReturn(basicCompany);
		when(CompanyServiceHelper.createBssCompany(basicCompany)).thenReturn(basicCompany);
		when(CompanyServiceHelper.getBssBandCodeList(companyId, null)).thenReturn(psBandCode);
		Mockito.when(companyBandCodesDao.getBandCodesByCompanyId(companyId)).thenReturn(bssBandCodes);

		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(REALM_PLAN_YR_ID, CODE)).thenReturn(Optional.empty());
		Mockito.when(schedMidYearFundingDataDao.getMidYearFundingScheduleForCompany(CODE)).thenReturn(smf);
		Mockito.when(psCompanyDao.isTMTUser(EMP_ID)).thenReturn(true);
		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(REALM_PLAN_YR_ID, CODE)).thenReturn(Optional.empty());
		Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, smf.getMidYearFundingEffDate()))
		.thenReturn(psCompany);
		when( CompanyServiceHelper.isClientCompanyPattern( any() ) ).thenCallRealMethod();

		// when
		Company actualCompany = companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		verify(schedMidYearFundingDataDao, times(1)).getMidYearFundingScheduleForCompany(CODE);
		assertTrue(actualCompany.isActiveServiceOrder());
	}

	/*
	 * GIVEN Scenario 2 : no active mid-year funding is present and User is TMT
	 */
	@Test
	public void getCompanyDetailsMidYearFundingTest2() {
		String benExchange = Constants.PASSPORT_BEN_EXCHANGE;
		long companyId = 1234L;
		boolean history = false;
		Date currentDate = new Date();

		Company basicCompany = prepareBasicCompany(CURRENT_PLAN_YR_START_STR, QUARTER, IS_PAYROLL_PROCESSED_TRUE);
		basicCompany.setId(companyId);
		basicCompany.setQuater(QUARTER);
		basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		Company psCompany = prepareCompany(COMPANY_NAME, CURRENT_PLAN_YR_START_STR, QUARTER, IS_PAYROLL_PROCESSED_TRUE,
				TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		RealmPlanYear currentRealmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, true, true, CURRENT_PLAN_YR_START_DT);
		RealmPlanYear futureRealmPlanYear = null;
		List<CompanyBandCodes> bssBandCodes = null;
		List<CompanyBandCodes> psBandCode = new ArrayList<>();

		SchedMidYearFunding smf = new SchedMidYearFunding();
		smf.setMidYearFundingEffDate(currentDate);
		smf.setActive(false);
        when(AppRulesAndConfigsUtils.getProspectConversionCutOffDate()).thenReturn("1-OCT-2025");
		Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(null);
		Mockito.when(realmPlanYearService.getCurrentRealmPlanYear(REALM_ID, QUARTER)).thenReturn(currentRealmPlanYear);
		Mockito.when(realmPlanYearService.getNextRealmPlanYear(currentRealmPlanYear)).thenReturn(futureRealmPlanYear);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(currentRealmPlanYear))
		.thenReturn(currentRealmPlanYear);
		Mockito.when(companyDao.saveAndFlush(basicCompany)).thenReturn(basicCompany);
		when(CompanyServiceHelper.createBssCompany(basicCompany)).thenReturn(basicCompany);
		when(CompanyServiceHelper.getBssBandCodeList(companyId, null)).thenReturn(psBandCode);
		Mockito.when(companyBandCodesDao.getBandCodesByCompanyId(companyId)).thenReturn(bssBandCodes);
		Mockito.when(schedMidYearFundingDataDao.getMidYearFundingScheduleForCompany(CODE)).thenReturn(null);
		Mockito.when(psCompanyDao.isTMTUser(EMP_ID)).thenReturn(true);
		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(REALM_PLAN_YR_ID, CODE)).thenReturn(Optional.empty());
		Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, currentRealmPlanYear.getPlanYearStart()))
		.thenReturn(psCompany);
		when( CompanyServiceHelper.isClientCompanyPattern( any() ) ).thenCallRealMethod();

		//when
		Company actualCompany = companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		verify(schedMidYearFundingDataDao, times(1)).getMidYearFundingScheduleForCompany(anyString());
		assertFalse(actualCompany.isActiveServiceOrder());
	}

	/*
	 * GIVEN Scenario 3 : active mid-year funding is present and User is not TMT
	 */
	@Test
	public void getCompanyDetailsMidYearFundingTest3() {
		String benExchange = Constants.PASSPORT_BEN_EXCHANGE;
		long companyId = 1234L;
		boolean history = false;
		Date currentDate = new Date();

		Company basicCompany = prepareBasicCompany(CURRENT_PLAN_YR_START_STR, QUARTER, IS_PAYROLL_PROCESSED_TRUE);
		basicCompany.setId(companyId);
		basicCompany.setQuater(QUARTER);
		basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		Company psCompany = prepareCompany(COMPANY_NAME, CURRENT_PLAN_YR_START_STR, QUARTER, IS_PAYROLL_PROCESSED_TRUE,
				TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
		psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

		RealmPlanYear currentRealmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, true, true, CURRENT_PLAN_YR_START_DT);
		List<CompanyBandCodes> bssBandCodes = null;
		List<CompanyBandCodes> psBandCode = new ArrayList<>();

		RealmPlanYear futureRealmPlanYear = null;

		SchedMidYearFunding smf = new SchedMidYearFunding();
		smf.setMidYearFundingEffDate(currentDate);

//		mockStatic(CompanyServiceHelper.class, CommonServiceHelper.class, RulesAndConfigsUtils.class);
        when(AppRulesAndConfigsUtils.getProspectConversionCutOffDate()).thenReturn("1-OCT-2025");
		Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
		Mockito.when(realmPlanYearService.getCurrentRealmPlanYear(REALM_ID, QUARTER)).thenReturn(currentRealmPlanYear);
		Mockito.when(realmPlanYearService.getNextRealmPlanYear(currentRealmPlanYear)).thenReturn(futureRealmPlanYear);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(currentRealmPlanYear))
		.thenReturn(currentRealmPlanYear);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(null);
		Mockito.when(companyDao.saveAndFlush(basicCompany)).thenReturn(basicCompany);
		when(CompanyServiceHelper.createBssCompany(basicCompany)).thenReturn(basicCompany);
		when(CompanyServiceHelper.getBssBandCodeList(companyId, null)).thenReturn(psBandCode);
		Mockito.when(companyBandCodesDao.getBandCodesByCompanyId(companyId)).thenReturn(bssBandCodes);
		Mockito.when(psCompanyDao.isTMTUser(EMP_ID)).thenReturn(false);
		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(REALM_PLAN_YR_ID, CODE)).thenReturn(Optional.empty());
		Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, currentRealmPlanYear.getPlanYearStart()))
		.thenReturn(psCompany);
		when( CompanyServiceHelper.isClientCompanyPattern( any() ) ).thenCallRealMethod();

		//when
		Company actualCompany = companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		// then
		verify(schedMidYearFundingDataDao, times(0)).getMidYearFundingScheduleForCompany(anyString());
		assertFalse(actualCompany.isActiveServiceOrder());
		assertEquals(CURRENT_PLAN_YR_START_STR, psCompany.getBenefitStartDate());
	}

	/**
	 * Given: A Company object and a status value
	 * When: updateAleUpdatedFlag is called
	 * Then: The status should be set in the company and saved via companyDao
	 */
	@Test
	public void testUpdateAleUpdatedFlag() {
		// Given
		Company company = new Company();
		Integer status = 1;

		// When
		companyServiceImpl.updateAleUpdatedFlag(company, status);
		// Then
		//Assertions
		assertEquals(company.getAleUpdated(), status);

		// Verify
		verify(companyDao, times(1)).save(company);
	}

	@Test
	public void getCompanyDetailsTestForAutoRefreshCensusWhenEvenDrivenSyncDisabled() {
		// Given
		boolean history = false;
		boolean mbgRenewal = false;

		String naicsCode = "1234";
		String lifeBandCode = "LIFE_CODE_A";
		String disBandCode = "DIS_CODE_B";
		BenExchngEnums exchangeEnum = BenExchngEnums.TRINET_III;

		Company bssCompany = prepareProspectConvertedCompany();
		bssCompany.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		bssCompany.setBssNaicsCode(Integer.valueOf(naicsCode));
		Realm realm = prepareRealm(REALM_ID, PEO_ID, Constants.PASSPORT_BEN_EXCHANGE);
		bssCompany.setRealm(realm);
		BandCodes bandCodes = new BandCodes();
		bssCompany.setBandCodes(bandCodes);

		Industry industry = prepareIndustry(10, IndustryType.AG);
		RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, true, mbgRenewal, null, null);
		List<ProspectCensusResponse> bssCoreCensus = prepareCoreCensus();

		when(CompanyServiceHelper.constructSelectionDate(any())).thenCallRealMethod();
		when(CompanyServiceHelper.isClientCompanyPattern(any())).thenCallRealMethod();
		when(CompanyServiceHelper.isTibProspect(any())).thenCallRealMethod();
		when(CompanyServiceHelper.isOMSExchange(any())).thenCallRealMethod();
		when(CompanyServiceHelper.getIndustry(any(Company.class))).thenReturn(industry);
		when(AppRulesAndConfigsUtils.getProspectConversionCutOffDate()).thenReturn("1-JAN-2020");
		when(CompanyServiceHelper.isProspectConvertedOnboardingClient(any())).thenCallRealMethod();
		when(bandCodesService.getBandCodeByType(eq(naicsCode), any(), eq(BSSApplicationConstants.LIFE),
				eq(exchangeEnum))).thenReturn(lifeBandCode);
		when(bandCodesService.getBandCodeByType(eq(naicsCode), any(), eq(BSSApplicationConstants.DISABILITY),
				eq(exchangeEnum))).thenReturn(disBandCode);

		Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(bssCompany);
		Mockito.when(realmPlanYearService.getRealmPlanYear(REALM_ID, QUARTER,
				Utils.convertStringToDate(COMP_PLAN_START_DATE, Constants.DATE_FORMAT))).thenReturn(realmPlanYear);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(realmPlanYear)).thenReturn(realmPlanYear);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(bssCompany);
		when(CompanyServiceHelper.createBssCompany(bssCompany)).thenReturn(bssCompany);
		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(REALM_PLAN_YR_ID, CODE)).thenReturn(Optional.empty());
		Mockito.when(realmPlanYearService.getMaxRealmPlanYear(REALM_ID, QUARTER)).thenReturn(realmPlanYear);
		Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(bssCompany,
						Utils.convertStringToDate(bssCompany.getPlanStartDate(), Constants.DATE_FORMAT)))
				.thenReturn(bssCompany);
		Mockito.when(bssCoreServiceClient.getCensusByCompanyCode(CODE)).thenReturn(bssCoreCensus);
		when(RulesAndConfigsUtils.isAutoRefreshCensusOn(Mockito.anyLong())).thenReturn(true);
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(false);

		Company company = companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		assertNotNull(company);
		assertNotNull(company.getEmployeeRegions());
		verify(psCompanyDao, never()).refreshCompanyCensus(anyString(), anyLong());
	}

	/**
	 * Given: Valid company ID
	 * When: findByCompanyId is called
	 * Then: Return the Company object
	 */
	@Test
	public void testFindByCompanyId_WhenIdExists() {
		// given
		Company company = new Company();
		company.setId(1L);
		company.setCode("TEST123");

		// when
		when(companyDao.findById(1L)).thenReturn(Optional.of(company));
		// then
		Company result = companyServiceImpl.findByCompanyId(1L);

		//assertions
		assertNotNull(result);
		assertEquals("TEST123", result.getCode());

		//verify
		verify(companyDao, times(1)).findById(1L);
	}

	/**
	 * Given: Invalid company ID
	 * When: findByCompanyId is called
	 * Then: Return null
	 */
	@Test
	public void testFindByCompanyId_WhenIdNotFound() {
		// given
		Company company = new Company();
		company.setId(1L);
		company.setCode("TEST123");
		// when
		when(companyDao.findById(2L)).thenReturn(Optional.empty());

		// then
		Company result = companyServiceImpl.findByCompanyId(2L);

		// assertions
		assertNull(result);
		verify(companyDao, times(1)).findById(2L);
	}

	@Test
	public void getIdsByCodeAndExchangeTest() {
		long companyId = 101L;
		List<Company> companies = new ArrayList<>();
		Company company = prepareBasicCompany(CURRENT_PLAN_YR_START_STR, QUARTER, IS_PAYROLL_PROCESSED_TRUE);
		company.setId(companyId++);
		companies.add(company);
		company = prepareBasicCompany(CURRENT_PLAN_YR_START_STR, QUARTER, IS_PAYROLL_PROCESSED_TRUE);
		company.setId(companyId++);
		companies.add(company);

		when(companyDao.findCompaniesBy( any( String.class ), any( String.class ) )).thenReturn(companies);

		//when
		List<Long> result = companyServiceImpl.getIdsByCodeAndExchange(CODE, BenExchngEnums.TRINET_III);

		// then
		verify(companyDao, times(1)).findCompaniesBy(any(String.class),any(String.class));
		assertNotNull(result);
		assertEquals(2, result.size());
	}

	@Test
	public void isTermedCompanyTrueTest() {
		// given
		when(psCompanyDao.isTermedCompany(CODE)).thenReturn(Boolean.TRUE);
		// when
		boolean actual = psCompanyDao.isTermedCompany(CODE);
		// then
		assertTrue(actual);
	}

	@Test
	public void isTermedCompanyFalseTest() {
		// given
		when(psCompanyDao.isTermedCompany(CODE)).thenReturn(Boolean.FALSE);
		// when
		boolean actual = psCompanyDao.isTermedCompany(CODE);
		// then
		assertFalse(actual);
	}

	@Test
	public void getCompanyDetailsTest() {

		// Given
		boolean history = false;
		boolean mbgRenewal = false;

		String naicsCode = "1234";
		String lifeBandCode = "LIFE_CODE_A";
		String disBandCode = "DIS_CODE_B";
		BenExchngEnums exchangeEnum = BenExchngEnums.TRINET_III;

		Company bssCompany = prepareProspectConvertedCompany();
		bssCompany.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		bssCompany.setBssNaicsCode(Integer.valueOf(naicsCode));
		Realm realm = prepareRealm(REALM_ID, PEO_ID, Constants.PASSPORT_BEN_EXCHANGE);
		bssCompany.setRealm(realm);
		BandCodes bandCodes = new BandCodes();
		bssCompany.setBandCodes(bandCodes);

		Industry industry = prepareIndustry(10, IndustryType.AG);
		RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, true, mbgRenewal, null, null);
		List<ProspectCensusResponse> bssCoreCensus = prepareCoreCensus();

		when(CompanyServiceHelper.constructSelectionDate(any())).thenCallRealMethod();
		when(CompanyServiceHelper.isClientCompanyPattern(any())).thenCallRealMethod();
		when(CompanyServiceHelper.isTibProspect(any())).thenCallRealMethod();
		when(CompanyServiceHelper.isOMSExchange(any())).thenCallRealMethod();
		when(CompanyServiceHelper.getIndustry(any(Company.class))).thenReturn(industry);
		when(AppRulesAndConfigsUtils.getProspectConversionCutOffDate()).thenReturn("1-JAN-2020");
		when(CompanyServiceHelper.isProspectConvertedOnboardingClient(any())).thenCallRealMethod();
		when(bandCodesService.getBandCodeByType(eq(naicsCode), any(), eq(BSSApplicationConstants.LIFE), eq(exchangeEnum))).thenReturn(lifeBandCode);
		when(bandCodesService.getBandCodeByType(eq(naicsCode), any(), eq(BSSApplicationConstants.DISABILITY), eq(exchangeEnum))).thenReturn(disBandCode);

		Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(bssCompany);
		Mockito.when(realmPlanYearService.getRealmPlanYear(REALM_ID, QUARTER,
				Utils.convertStringToDate(COMP_PLAN_START_DATE, Constants.DATE_FORMAT))).thenReturn(realmPlanYear);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(realmPlanYear)).thenReturn(realmPlanYear);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(bssCompany);
		when(CompanyServiceHelper.createBssCompany(bssCompany)).thenReturn(bssCompany);
		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(REALM_PLAN_YR_ID, CODE)).thenReturn(Optional.empty());
		Mockito.when(realmPlanYearService.getMaxRealmPlanYear(REALM_ID, QUARTER)).thenReturn(realmPlanYear);
		Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(bssCompany,
				Utils.convertStringToDate(bssCompany.getPlanStartDate(), Constants.DATE_FORMAT)))
				.thenReturn(bssCompany);
		Mockito.when(bssCoreServiceClient.getCensusByCompanyCode(CODE)).thenReturn(bssCoreCensus);

		Company company = companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		assertNotNull(company);
		assertNotNull(company.getEmployeeRegions());
		assertEquals(2, company.getEmployeeRegions().size());
		assertEquals("CA", company.getEmployeeRegions().get(0));
		assertEquals("NY", company.getEmployeeRegions().get(1));
		assertEquals(lifeBandCode, company.getBandCodes().getLifeBandCode());
		assertEquals(disBandCode, company.getBandCodes().getDisBandCode());
	}

	@Test
	public void getCompanyDetailsLocationsTest() {

		// Given
		boolean history = false;
		boolean mbgRenewal = false;

		Company bssCompany = prepareProspectConvertedCompany();
		bssCompany.setProspectConvertedClient(false);
		bssCompany.setProspectConvertedOnboardingClient(false);
		Industry industry = prepareIndustry(10, IndustryType.AG);
		RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, true, mbgRenewal, null, null);
		List<ProspectCensusResponse> bssCoreCensus = prepareCoreCensus();

		Set<String> companyRegionsSet = new HashSet<>();
		Set<String> fundingRegionsSet = new HashSet<>();
		List<String> employeeRegionsList= new ArrayList<>();
		companyRegionsSet.add("AL");
		fundingRegionsSet.add("AK");
		employeeRegionsList.add("AZ");
		List<MandatoryRegion> mandatoryRegionList = new ArrayList<>();
		mandatoryRegionList = new ArrayList<>();
		MandatoryRegion mandatoryRegion = new MandatoryRegion();
		mandatoryRegion.setId( new MandatoryRegionPK( "QQ", "WY", java.sql.Date.valueOf( "2000-01-01" ) ) );
		mandatoryRegion.setEnddt( java.sql.Date.valueOf( "2099-12-31" ));
		mandatoryRegionList.add(mandatoryRegion);
		mandatoryRegion = new MandatoryRegion();
		MandatoryRegionPK regionPk = new MandatoryRegionPK();
		regionPk.setOeQuarter( "QQ" );
		regionPk.setRegion( "AR" );
		regionPk.setEffdt( java.sql.Date.valueOf( "2000-01-01" ) );
		mandatoryRegion.setId( regionPk );
		mandatoryRegion.setEnddt( java.sql.Date.valueOf( "2099-12-31" ));
		mandatoryRegionList.add(mandatoryRegion);


		when(CompanyServiceHelper.constructSelectionDate(any())).thenCallRealMethod();
		when(CompanyServiceHelper.isClientCompanyPattern(any())).thenCallRealMethod();
		when(CompanyServiceHelper.isTibProspect(any())).thenCallRealMethod();
		when(CompanyServiceHelper.isOMSExchange(any())).thenCallRealMethod();
		when(CompanyServiceHelper.getIndustry(any(Company.class))).thenReturn(industry);

		Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(bssCompany);
		Mockito.when(realmPlanYearService.getRealmPlanYear(REALM_ID, QUARTER,
				Utils.convertStringToDate(COMP_PLAN_START_DATE, Constants.DATE_FORMAT))).thenReturn(realmPlanYear);
		Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(realmPlanYear)).thenReturn(realmPlanYear);
		Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(bssCompany);
		when(CompanyServiceHelper.createBssCompany(bssCompany)).thenReturn(bssCompany);
		Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(REALM_PLAN_YR_ID, CODE)).thenReturn(Optional.empty());
		Mockito.when(realmPlanYearService.getMaxRealmPlanYear(REALM_ID, QUARTER)).thenReturn(realmPlanYear);
		Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(bssCompany,
				Utils.convertStringToDate(bssCompany.getPlanStartDate(), Constants.DATE_FORMAT)))
				.thenReturn(bssCompany);



		when(mandatoryRegionDao.findAllByRealmYrId(Mockito.anyLong())).thenReturn(mandatoryRegionList);
		when(realmDataDao.getCompanyLocationStates(anyString())).thenReturn(companyRegionsSet);
		when(realmDataDao.getFundingPlanStates(Mockito.any(Company.class), Mockito.anySet(), Mockito.any(RealmPlanYear.class)))
				.thenReturn(fundingRegionsSet);
		when(realmDataDao.getEmployeeHomeStates(anyString())).thenReturn(employeeRegionsList);


		Company company = companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

		assertNotNull(company);
		assertNotNull(company.getEmployeeRegions());

		assertEquals(3, company.getCompanyRegions().size());
		assertEquals(true, company.getCompanyRegions().contains("AL"));
		assertEquals(true, company.getCompanyRegions().contains("WY"));
		assertEquals(true, company.getCompanyRegions().contains("AR"));
		assertEquals(1, company.getFundingRegions().size());
		assertEquals(true, company.getFundingRegions().contains("AK"));
		assertEquals(1, company.getEmployeeRegions().size());
		assertEquals(true, company.getEmployeeRegions().contains("AZ"));


	}

    @Test
    public void getCompanyDetailsEmployeeLocationsWhenProspectConvertedClientTest() {
        // Given
        boolean history = false;
        boolean mbgRenewal = false;

        Company bssCompany = prepareProspectConvertedCompany();
        bssCompany.setProspectConvertedClient(false);
        bssCompany.setProspectConvertedOnboardingClient(false);
        Industry industry = prepareIndustry(10, IndustryType.AG);
        RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, true, mbgRenewal, null, null);
        List<ProspectCensusResponse> bssCoreCensus = prepareCoreCensus();

        Set<String> companyRegionsSet = new HashSet<>();
        Set<String> fundingRegionsSet = new HashSet<>();
        companyRegionsSet.add("AL");
        fundingRegionsSet.add("AK");
        List<MandatoryRegion> mandatoryRegionList = new ArrayList<>();
        MandatoryRegion mandatoryRegion = new MandatoryRegion();
        mandatoryRegion.setId( new MandatoryRegionPK( "QQ", "WY", java.sql.Date.valueOf( "2000-01-01" ) ) );
        mandatoryRegion.setEnddt( java.sql.Date.valueOf( "2099-12-31" ));
        mandatoryRegionList.add(mandatoryRegion);
        mandatoryRegion = new MandatoryRegion();
        MandatoryRegionPK regionPk = new MandatoryRegionPK();
        regionPk.setOeQuarter( "QQ" );
        regionPk.setRegion( "AR" );
        regionPk.setEffdt( java.sql.Date.valueOf( "2000-01-01" ) );
        mandatoryRegion.setId( regionPk );
        mandatoryRegion.setEnddt( java.sql.Date.valueOf( "2099-12-31" ));
        mandatoryRegionList.add(mandatoryRegion);

        when(CompanyServiceHelper.constructSelectionDate(any())).thenCallRealMethod();
        when(CompanyServiceHelper.isClientCompanyPattern(any())).thenCallRealMethod();
        when(CompanyServiceHelper.isTibProspect(any())).thenCallRealMethod();
        when(CompanyServiceHelper.isOMSExchange(any())).thenCallRealMethod();
        when(CompanyServiceHelper.getIndustry(any(Company.class))).thenReturn(industry);
        when(CompanyServiceHelper.isProspectConvertedOnboardingClient(any())).thenCallRealMethod();

        Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(bssCompany);
        Mockito.when(realmPlanYearService.getRealmPlanYear(REALM_ID, QUARTER,
                Utils.convertStringToDate(COMP_PLAN_START_DATE, Constants.DATE_FORMAT))).thenReturn(realmPlanYear);
        Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(realmPlanYear)).thenReturn(realmPlanYear);
        Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(bssCompany);
        when(CompanyServiceHelper.createBssCompany(bssCompany)).thenReturn(bssCompany);
        when(AppRulesAndConfigsUtils.getProspectConversionCutOffDate()).thenReturn("1-JAN-2020");
        Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(REALM_PLAN_YR_ID, CODE)).thenReturn(Optional.empty());
        Mockito.when(realmPlanYearService.getMaxRealmPlanYear(REALM_ID, QUARTER)).thenReturn(realmPlanYear);
        Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(bssCompany,
                        Utils.convertStringToDate(bssCompany.getPlanStartDate(), Constants.DATE_FORMAT)))
                .thenReturn(bssCompany);
        Mockito.when(bssCoreServiceClient.getCensusByCompanyCode(CODE)).thenReturn(bssCoreCensus);

        when(mandatoryRegionDao.findAllByRealmYrId(Mockito.anyLong())).thenReturn(mandatoryRegionList);
        when(realmDataDao.getCompanyLocationStates(anyString())).thenReturn(companyRegionsSet);
        when(realmDataDao.getFundingPlanStates(Mockito.any(Company.class), Mockito.anySet(), Mockito.any(RealmPlanYear.class)))
                .thenReturn(fundingRegionsSet);

        Company company = companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

        assertNotNull(company);
        assertNotNull(company.getEmployeeRegions());
        assertEquals(true, company.getEmployeeRegions().contains("NY"));
        assertEquals(true, company.getEmployeeRegions().contains("CA"));
        assertEquals(2, company.getEmployeeRegions().size());
        assertEquals(false, company.getEmployeeRegions().contains("AZ"));


    }

    @Test
    public void getCompanyDetailsEmployeeLocationsWhenProspectConvertedClientFalseTest() {
        // Given
        boolean history = false;
        boolean mbgRenewal = false;
        Company bssCompany = prepareProspectConvertedCompany();
        bssCompany.setProspectConvertedClient(false);
        bssCompany.setProspectConvertedOnboardingClient(false);
        Industry industry = prepareIndustry(10, IndustryType.AG);
        RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, true, mbgRenewal, null, null);
        List<ProspectCensusResponse> bssCoreCensus = prepareCoreCensus();

        Set<String> companyRegionsSet = new HashSet<>();
        Set<String> fundingRegionsSet = new HashSet<>();
        companyRegionsSet.add("AL");
        fundingRegionsSet.add("AK");
        List<MandatoryRegion> mandatoryRegionList = new ArrayList<>();
        MandatoryRegion mandatoryRegion = new MandatoryRegion();
        mandatoryRegion.setId( new MandatoryRegionPK( "QQ", "WY", java.sql.Date.valueOf( "2000-01-01" ) ) );
        mandatoryRegion.setEnddt( java.sql.Date.valueOf( "2099-12-31" ));
        mandatoryRegionList.add(mandatoryRegion);
        mandatoryRegion = new MandatoryRegion();
        MandatoryRegionPK regionPk = new MandatoryRegionPK();
        regionPk.setOeQuarter( "QQ" );
        regionPk.setRegion( "AR" );
        regionPk.setEffdt( java.sql.Date.valueOf( "2000-01-01" ) );
        mandatoryRegion.setId( regionPk );
        mandatoryRegion.setEnddt( java.sql.Date.valueOf( "2099-12-31" ));
        mandatoryRegionList.add(mandatoryRegion);

        when(CompanyServiceHelper.constructSelectionDate(any())).thenCallRealMethod();
        when(CompanyServiceHelper.isClientCompanyPattern(any())).thenCallRealMethod();
        when(CompanyServiceHelper.isTibProspect(any())).thenCallRealMethod();
        when(CompanyServiceHelper.isOMSExchange(any())).thenCallRealMethod();
        when(CompanyServiceHelper.getIndustry(any(Company.class))).thenReturn(industry);
        when(CompanyServiceHelper.isProspectConvertedOnboardingClient(any())).thenCallRealMethod();

        Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(bssCompany);
        Mockito.when(realmPlanYearService.getRealmPlanYear(REALM_ID, QUARTER,
                Utils.convertStringToDate(COMP_PLAN_START_DATE, Constants.DATE_FORMAT))).thenReturn(realmPlanYear);
        Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(realmPlanYear)).thenReturn(realmPlanYear);
        Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(bssCompany);
        when(CompanyServiceHelper.createBssCompany(bssCompany)).thenReturn(bssCompany);
        when(AppRulesAndConfigsUtils.getProspectConversionCutOffDate()).thenReturn("1-JAN-2027");
        Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(REALM_PLAN_YR_ID, CODE)).thenReturn(Optional.empty());
        Mockito.when(realmPlanYearService.getMaxRealmPlanYear(REALM_ID, QUARTER)).thenReturn(realmPlanYear);
        Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(bssCompany,
                        Utils.convertStringToDate(bssCompany.getPlanStartDate(), Constants.DATE_FORMAT)))
                .thenReturn(bssCompany);

        when(mandatoryRegionDao.findAllByRealmYrId(Mockito.anyLong())).thenReturn(mandatoryRegionList);
        when(realmDataDao.getCompanyLocationStates(anyString())).thenReturn(companyRegionsSet);
        when(realmDataDao.getFundingPlanStates(Mockito.any(Company.class), Mockito.anySet(), Mockito.any(RealmPlanYear.class)))
                .thenReturn(fundingRegionsSet);
        when(realmDataDao.getEmployeeHomeStates(anyString())).thenReturn(List.of("AZ"));

        Company company = companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

        assertNotNull(company);
        assertNotNull(company.getEmployeeRegions());
        assertEquals(1, company.getEmployeeRegions().size());
        assertEquals(false, company.getEmployeeRegions().contains("NY"));
        assertEquals(false, company.getEmployeeRegions().contains("CA"));
        assertEquals(true, company.getEmployeeRegions().contains("AZ"));
    }

	@Test
	public void testRefreshCompanyCensusForRenewalClient() {
		Timestamp createdTime = new Timestamp(System.currentTimeMillis() - (BSSApplicationConstants.ONE_DAY + 100000L));

		when(companyService.isRenewalCompany(CODE)).thenReturn(true);
		when(companyService.isTermedCompany(CODE)).thenReturn(false);
		when(psCompanyDao.getCompanyCensusCreateDt(CODE, REALM_PLAN_YR_ID)).thenReturn(createdTime);

		companyServiceImpl.refreshCompanyCensus(CODE, REALM_PLAN_YR_ID);

		verify(psCompanyDao).getCompanyCensusCreateDt(any(), anyLong());
		verify(psCompanyDao, timeout(500).times(1)).refreshCompanyCensus(CODE, REALM_PLAN_YR_ID);
	}

	@Test
	public void testRefreshCompanyCensusForRenewalClientWithCensusProcessed() {
		Timestamp createdTime = new Timestamp(System.currentTimeMillis());

		when(companyService.isRenewalCompany(CODE)).thenReturn(true);
		when(companyService.isTermedCompany(CODE)).thenReturn(false);
		when(psCompanyDao.getCompanyCensusCreateDt(CODE, REALM_PLAN_YR_ID)).thenReturn(createdTime);

		companyServiceImpl.refreshCompanyCensus(CODE, REALM_PLAN_YR_ID);

		verify(psCompanyDao, never()).refreshCompanyCensus(any(), anyLong());
	}

	@Test
	public void testRefreshCompanyCensusForTerminatedCompany() {
		when(companyService.isRenewalCompany(CODE)).thenReturn(true);
		when(companyService.isTermedCompany(CODE)).thenReturn(true);

		companyServiceImpl.refreshCompanyCensus(CODE, REALM_PLAN_YR_ID);

		verify(psCompanyDao, never()).getCompanyCensusCreateDt(any(), anyLong());
		verify(psCompanyDao, never()).refreshCompanyCensus(any(), anyLong());
	}

	@Test
	public void testRefreshCompanyCensuSForNewCompany() {
		when(companyService.isRenewalCompany(CODE)).thenReturn(false);
		when(companyService.isTermedCompany(CODE)).thenReturn(false);

		companyServiceImpl.refreshCompanyCensus(CODE, REALM_PLAN_YR_ID);

		verify(psCompanyDao, never()).getCompanyCensusCreateDt(any(), anyLong());
		verify(psCompanyDao, never()).refreshCompanyCensus(any(), anyLong());
	}

    @Test
    public void testThrowExceptionWhenCompanySetupDateAfterCutoffAndNoBSSCompanyFound() {
        String benExchange = Constants.PASSPORT_BEN_EXCHANGE;
        boolean realmPlanYrIsMgbNew = true;
        long realmId = 1L;

        Company basicCompany = prepareBasicCompany(CURRENT_PLAN_YR_START_STR, QUARTER, IS_PAYROLL_PROCESSED_FALSE);
        basicCompany.setCompanySetupDate("02-OCT-2025"); // Date after cutoff
        basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

        Company psCompany = prepareCompany(COMPANY_NAME, CURRENT_PLAN_YR_START_STR, QUARTER, IS_PAYROLL_PROCESSED_FALSE,
                TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
        psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

        boolean history = false;
        RealmPlanYear nextRealmPlanYear = null;
        boolean mbgRenewal = false;
        final BigDecimal historyAleAmount = BigDecimal.valueOf(100.04);

        // Mock only the methods that are actually called before the exception is thrown
        RealmPlanYear currentRealmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal, CURRENT_PLAN_YR_START_DT);
        currentRealmPlanYear.setAleAmount(historyAleAmount);

        when(AppRulesAndConfigsUtils.getProspectConversionCutOffDate()).thenReturn("01-Oct-2025");
        Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
        Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, CURRENT_PLAN_YR_START_DT)).thenReturn(psCompany);
        Mockito.when(psCompanyDao.getCompanyActualHeadCount(CODE)).thenReturn(2);
        when(hrpDao.getOlpHiringCompletedStatus(basicCompany)).thenReturn(null);
        when(realmPlanYearService.getMaxRealmPlanYear(REALM_ID, QUARTER)).thenReturn(new RealmPlanYear());
        when(realmPlanYearService.getRealmPlanYear(REALM_ID, QUARTER,
                Utils.convertStringToDate(CURRENT_PLAN_YR_START_STR, Constants.DATE_FORMAT))).thenReturn(currentRealmPlanYear);
        Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(currentRealmPlanYear)).thenReturn(currentRealmPlanYear);
        Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(null);
        when( CompanyServiceHelper.isClientCompanyPattern( any() ) ).thenCallRealMethod();

        Exception e = null;
        // when
        try {
            companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);
        } catch (Exception e1) {
            e = e1;
        }
        assertNotNull(e);
        assertEquals("Prospect to client conversion is pending for company: 5R9", e.getMessage());
        assertTrue(e instanceof BSSApplicationException);
        assertEquals("ERR-BSS-10030", ((BSSApplicationException) e).getBssError().getCode());
    }

	@Test
	public void getPsCompanyDetailsSuccessTest() {
		// Given
		String companyCode = "TEST123";
		Date liveDate = new Date();
		Date planStartDate = new Date();
		
		Company mockCompany = new Company();
		mockCompany.setLiveDate("01-OCT-2025");
		mockCompany.setPlanStartDate("01-JAN-2026");
		
		when(psCompanyDao.getBasicCompanyDetails(companyCode)).thenReturn(mockCompany);
		
		// When
		Company result = companyServiceImpl.getPsCompanyDetails(companyCode);
		
		// Then
		assertNotNull(result);
		assertEquals("01-OCT-2025", result.getLiveDate());
		assertEquals("01-JAN-2026", result.getPlanStartDate());
		verify(psCompanyDao, times(1)).getBasicCompanyDetails(companyCode);
		verify(cacheService, never()).invalidateCache(anyString(), anyString(), anyString());
	}

	@Test(expected = BSSBadDataException.class)
	public void getPsCompanyDetailsThrowsExceptionWhenLiveDateIsNullTest() {
		// Given
		String companyCode = "G48";
		Date planStartDate = new Date();
		
		Company mockCompany = new Company();
		mockCompany.setLiveDate(null); // null live date should trigger exception
		mockCompany.setPlanStartDate("01-JAN-2026");
		
		when(psCompanyDao.getBasicCompanyDetails(companyCode)).thenReturn(mockCompany);
		
		// When
		companyServiceImpl.getPsCompanyDetails(companyCode);

        verify(cacheService, never()).invalidateCache("BASIC_COMPANY_DETAILS", "COMPANY", "G48");
    }

	@Test(expected = BSSBadDataException.class)
	public void getPsCompanyDetailsThrowsExceptionWhenPlanStartDateIsNullTest() {
		// Given
		String companyCode = "G48";
		Date liveDate = new Date();
		
		Company mockCompany = new Company();
		mockCompany.setLiveDate("01-JAN-2026");
		mockCompany.setPlanStartDate(null); // null plan start date should trigger exception
		
		when(psCompanyDao.getBasicCompanyDetails(companyCode)).thenReturn(mockCompany);
		
		// When
		companyServiceImpl.getPsCompanyDetails(companyCode);
		
		// Then - BSSBadDataException should be thrown
        verify(cacheService, never()).invalidateCache("BASIC_COMPANY_DETAILS", "COMPANY", "G48");
	}

	@Test(expected = BSSBadDataException.class)
	public void getPsCompanyDetailsThrowsExceptionWhenBothDatesAreNullTest() {
		// Given
		String companyCode = "G48";
		
		Company mockCompany = new Company();
		mockCompany.setLiveDate(null);
		mockCompany.setPlanStartDate(null);
		
		when(psCompanyDao.getBasicCompanyDetails(companyCode)).thenReturn(mockCompany);
		
		// When
		companyServiceImpl.getPsCompanyDetails(companyCode);
		
		// Then - BSSBadDataException should be thrown
        verify(cacheService, never()).invalidateCache("BASIC_COMPANY_DETAILS", "COMPANY", "G48");
	}

	private Company prepareBasicCompany(String planStartDate, String quarter, boolean isPayrollProcessed) {
		Company cmp = new Company();
		cmp.setCode(CODE);
		cmp.setPfClient("pfClient");
		cmp.setLiveDate("01-Jan-2020");
        cmp.setCompanySetupDate("01-SEP-2025");
		cmp.setPlanStartDate(planStartDate);
		cmp.setQuater(quarter);
		cmp.setPayrollProcessed(isPayrollProcessed);
		return cmp;
	}

	private Company prepareCompany(String companyName, String planStartDate, String quarter, boolean isPayrollProcessed,
			boolean transitionPeriod, String kaiserBandCode, String headQtrState) {
		Company cmp = new Company();
		cmp.setCode(CODE);
		cmp.setName(companyName);
		cmp.setPlanStartDate(planStartDate);
		cmp.setBenefitStartDate(planStartDate);
		cmp.setQuater(quarter);
		cmp.setPayrollProcessed(isPayrollProcessed);
		cmp.setTransitionPeriod(transitionPeriod);
		BandCodes bandCodes = new BandCodes();
		bandCodes.setKaiserBandCode(kaiserBandCode);
		cmp.setHeadQuatersState(headQtrState);
		cmp.setBandCodes(bandCodes);
		Set<String> bssCompanyRegions  = new HashSet<String>();
		bssCompanyRegions.add("TX");
		cmp.setCompanyRegions(bssCompanyRegions);
		return cmp;
	}

	private Realm prepareRealm(int id, String peoId, String benExchange) {
		Realm realm = new Realm();
		realm.setId(id);
		realm.setPeoid(peoId);
		realm.setBenExchange(benExchange);
		return realm;
	}

	private RealmPlanYear prepareRealmPlanYr(long id, boolean realmPlanYrIsMgbNew, boolean mbgRenewal, Date planStartDt) {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(id);
		realmPlanYear.setMbgRenewal(mbgRenewal);
		realmPlanYear.setMbgNew(realmPlanYrIsMgbNew);
		realmPlanYear.setPlanYearStart(planStartDt);
		return realmPlanYear;
	}

	private CompanyBandCodes prepareBandCode() {
		return new CompanyBandCodes();
	}

	private Optional<HQException> prepareHQException() {
		HQException hqException = new HQException();
		HQExceptionsId hqExceptionId = new HQExceptionsId();
		hqException.setHqState("MN");
		hqException.setPostalCode("11111");
		hqException.setId(hqExceptionId);
		return Optional.ofNullable(hqException);
	}

	private Company prepareProspectConvertedCompany() {
		Company bssCompany = new Company();
		bssCompany.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		bssCompany.setCode(CODE);
		bssCompany.setPfClient("pfClient");
		bssCompany.setLiveDate("01-Jan-2020");
		bssCompany.setQuater(QUARTER);
		bssCompany.setPayrollProcessed(false);
		bssCompany.setProspectConvertedClient(true);
		bssCompany.setRenewalCompany(false);
		bssCompany.setCompanySetupDate("1-FEB-2026");
		bssCompany.setProspectId("122132");
		bssCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, Constants.PASSPORT_BEN_EXCHANGE));
		bssCompany.setPlanStartDate(COMP_PLAN_START_DATE);
		bssCompany.setProspectConvertedOnboardingClient(true);
		// In production, BandCodes is always initialized; reflect that in tests
		bssCompany.setBandCodes(new BandCodes());
		return bssCompany;
	}


	private List<ProspectCensusResponse> prepareCoreCensus() {
		return List.of(
				ProspectCensusResponse.builder().employeeId("EE001").employeeName("John Doe").state("CA").zip("12345")
						.build(),
				ProspectCensusResponse.builder().employeeId("EE002").employeeName("Jane Smith").state("NY").zip("54321")
						.build(),
				ProspectCensusResponse.builder().employeeId("EE003").employeeName("Shawn Smith").state("NY")
						.zip("54345").build());
	}

	private RealmPlanYear prepareRealmPlanYr(long id, boolean realmPlanYrIsMgbNew, boolean mbgRenewal,
			Date planYearStart, Date planYearEnd) {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(id);
		realmPlanYear.setMbgRenewal(mbgRenewal);
		realmPlanYear.setMbgNew(realmPlanYrIsMgbNew);
		realmPlanYear.setPlanYearStart(planYearStart);
		realmPlanYear.setPlanYearEnd(planYearEnd);
		return realmPlanYear;
	}

	private Industry prepareIndustry(int NAICSCode, IndustryType type) {
		Industry industry = new Industry(NAICSCode);
		industry.setIndustryType(type);
		return industry;
	}

	@Test
	public void getCompanyDetailsById_ShouldReturnCompanyDetails() {
		// given
		Long companyId = 12345L;
		com.trinet.ambis.persistence.dao.hrp.dto.CompanyDetailsDto expectedDto =
				com.trinet.ambis.persistence.dao.hrp.dto.CompanyDetailsDto.builder()
						.code("G48")
						.planYearStart(new Date())
						.cloneBenpgm("CLONE_PGM")
						.bundleSeq(100L)
						.oeQuarter("Q1")
						.naicsCode(541511)
						.largeDealProspect(1)
						.naicsBundleId(2L)
						.build();

		when(companyDataDao.getCompanyDetailsById(companyId)).thenReturn(expectedDto);

		// when
		com.trinet.ambis.persistence.dao.hrp.dto.CompanyDetailsDto actualDto =
				companyServiceImpl.getCompanyDetailsById(companyId);

		// then
		assertNotNull(actualDto);
		assertEquals("G48", actualDto.getCode());
		assertEquals("CLONE_PGM", actualDto.getCloneBenpgm());
		assertEquals(Long.valueOf(100L), actualDto.getBundleSeq());
		assertEquals("Q1", actualDto.getOeQuarter());
		assertEquals(Integer.valueOf(541511), actualDto.getNaicsCode());
		assertEquals(Integer.valueOf(1), actualDto.getLargeDealProspect());
		assertEquals(Long.valueOf(2L), actualDto.getNaicsBundleId());
		verify(companyDataDao, times(1)).getCompanyDetailsById(companyId);
	}

	@Test
	public void getCompanyDetailsById_ShouldReturnNull_WhenNotFound() {
		// given
		Long companyId = 99999L;
		when(companyDataDao.getCompanyDetailsById(companyId)).thenReturn(null);

		// when
		com.trinet.ambis.persistence.dao.hrp.dto.CompanyDetailsDto actualDto =
				companyServiceImpl.getCompanyDetailsById(companyId);

		// then
		assertNull(actualDto);
		verify(companyDataDao, times(1)).getCompanyDetailsById(companyId);
	}

	// -----------------------------------------------------------------------
	// resetCompany tests
	// -----------------------------------------------------------------------

	@Test
	public void testResetCompany_callsBothServicesInOrder() {
		// given
		long companyId = 12345L;
		doNothing().when(strategyService).deleteStrategies(companyId);
		doNothing().when(hrpDao).deleteCompanyDataByCompanyId(companyId);

		// when
		companyServiceImpl.resetCompany(companyId);

		// then — strategy delete must precede company-data delete
		InOrder order = inOrder(strategyService, hrpDao);
		order.verify(strategyService).deleteStrategies(eq(companyId));
		order.verify(hrpDao).deleteCompanyDataByCompanyId(eq(companyId));
		verify(hrpDao, never()).deleteCompanyByCompanyId(anyLong());
	}

	@Test
	public void testResetCompany_withHardDeleteTrue_deletesCompanyRowAfterCompanyData() {
		long companyId = 12345L;
		doNothing().when(strategyService).deleteStrategies(companyId);
		doNothing().when(hrpDao).deleteCompanyDataByCompanyId(companyId);
		doNothing().when(hrpDao).deleteCompanyByCompanyId(companyId);

		companyServiceImpl.resetCompany(companyId, true);

		InOrder order = inOrder(strategyService, hrpDao);
		order.verify(strategyService).deleteStrategies(eq(companyId));
		order.verify(hrpDao).deleteCompanyDataByCompanyId(eq(companyId));
		order.verify(hrpDao).deleteCompanyByCompanyId(eq(companyId));
	}

	@Test
	public void testResetCompany_noDataScenario_completesSuccessfully() {
		// given — company with no strategies or company data (DAO is a no-op)
		long companyId = 12345L;
		doNothing().when(strategyService).deleteStrategies(companyId);
		doNothing().when(hrpDao).deleteCompanyDataByCompanyId(companyId);

		// when / then — must not throw
		companyServiceImpl.resetCompany(companyId);

		verify(strategyService).deleteStrategies(companyId);
		verify(hrpDao).deleteCompanyDataByCompanyId(companyId);
	}

	@Test
	public void testResetCompany_whenDeleteStrategiesFails_companyDataNotDeleted() {
		// given
		long companyId = 12345L;
		doThrow(new RuntimeException("DB error")).when(strategyService).deleteStrategies(companyId);

		// when
		try {
			companyServiceImpl.resetCompany(companyId);
			fail("Expected RuntimeException to propagate");
		} catch (RuntimeException ex) {
			// expected — transaction manager rolls back
		}

		// then — hrpDao must never be called
		verifyNoInteractions(hrpDao);
	}

	@Test
	public void testResetCompany_whenCompanyDataDeleteFails_exceptionPropagated() {
		// given
		long companyId = 12345L;
		doNothing().when(strategyService).deleteStrategies(companyId);
		doThrow(new RuntimeException("DB error")).when(hrpDao).deleteCompanyDataByCompanyId(companyId);

		// when
		try {
			companyServiceImpl.resetCompany(companyId);
			fail("Expected RuntimeException to propagate");
		} catch (RuntimeException ex) {
			// expected
		}

		// then — strategies were deleted before the error
		verify(strategyService).deleteStrategies(companyId);
	}

	// -----------------------------------------------------------------------
	// deleteCompanyData tests
	// -----------------------------------------------------------------------

	@Test
	public void testDeleteCompanyData_delegatesToHrpDao() {
		// given
		long companyId = 12345L;
		doNothing().when(hrpDao).deleteCompanyDataByCompanyId(companyId);

		// when
		companyServiceImpl.deleteCompanyData(companyId);

		// then
		verify(hrpDao).deleteCompanyDataByCompanyId(eq(companyId));
	}

	@Test
	public void testDeleteCompanyData_doesNotTouchStrategyService() {
		// given
		long companyId = 12345L;
		doNothing().when(hrpDao).deleteCompanyDataByCompanyId(companyId);

		// when
		companyServiceImpl.deleteCompanyData(companyId);

		// then
		verifyNoInteractions(strategyService);
	}

	@Test(expected = RuntimeException.class)
	public void testDeleteCompanyData_whenHrpDaoFails_exceptionPropagated() {
		long companyId = 12345L;
		doThrow(new RuntimeException("DAO failure")).when(hrpDao).deleteCompanyDataByCompanyId(companyId);

		companyServiceImpl.deleteCompanyData(companyId);
	}
}
