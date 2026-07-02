package com.trinet.ambis.service.unit;

import static com.trinet.ambis.enums.OmsOfferingEnum.OMB_TLD;
import static com.trinet.ambis.enums.OmsOfferingEnum.OM_OD_OV_TLD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.service.ProcessStatusService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.service.BandCodesService;
import com.trinet.ambis.service.FlexRateService;
import org.apache.commons.lang3.StringUtils;
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
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.IndustryType;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.CompanyBandCodesDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDataDao;
import com.trinet.ambis.persistence.dao.hrp.HQExceptionDao;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.hrp.MandatoryRegionDao;
import com.trinet.ambis.persistence.dao.hrp.RealmCloneProgramDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.model.Bundle;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.HQException;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.BenefitsBundleService;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.EmployerEmployeePlansMappingService;
import com.trinet.ambis.service.GroupRuleService;
import com.trinet.ambis.service.MinFundExceptionService;
import com.trinet.ambis.service.ProspectCompanyService;
import com.trinet.ambis.service.RateSystemService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.RealmRegionMinFundingService;
import com.trinet.ambis.service.RealmWaitPeriodService;
import com.trinet.ambis.service.SchedTblService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.impl.CompanyServiceImpl;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.CommonData;
import com.trinet.ambis.service.model.CompanyRealmData;
import com.trinet.ambis.service.model.Industry;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.BssCoreServiceClient;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.Utils;


@RunWith(MockitoJUnitRunner.class)
public class CompanyServiceImpl2Test extends ServiceUnitTest {

    @InjectMocks
    CompanyServiceImpl companyServiceImpl;

    @Mock
    CompanyService companyService;

    @Mock
    CompanyDao companyDao;

    @Mock
    PsCompanyDao psCompanyDao;

    @Mock
    HrpDao hrpDao;

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
    HQExceptionDao hqExceptionDao;

    @Mock
    CompanyDataDao companyDataDao;

    @Mock
    RealmRegionMinFundingService realmRegionMinFundingService;

    @Mock
    MinFundExceptionService minFundExceptionService;

    @Mock
    CacheService cacheService;

    @Mock
    BenefitOfferExceptionService benOfferExceptionService;

    @Mock
    GroupRuleService groupRuleService;

    @Mock
    ProspectCompanyService prospectCompanyService;

    @Mock
    BenefitsBundleService benefitsBundleService;

    @Captor
    private ArgumentCaptor<Set<String>> companyNamesSet;

    @Mock
    BssCoreServiceClient bssCoreServiceClient;

    @Mock
    MandatoryRegionDao mandatoryRegionDao;

    @Mock
    BandCodesService bandCodesService;

    @Mock
    FlexRateService flexRateService;

    @Mock
    RateSystemService rateSystemService;

    @Mock
    ProcessStatusService processStatusService;

    private static MockedStatic<CompanyServiceHelper> companyServiceHelperMockedStatic;
    private static MockedStatic<StrategyServiceHelper> strategyServiceHelperMockedStatic;
    private static MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;
    private static MockedStatic<AppRulesAndConfigsUtils> appRulesAndConfigsUtilsMockedStatic;
    private static MockedStatic<Utils> utilsMockedStatic;
    private static MockedStatic<CommonUtils> commonUtilsMockedStatic;

    @Before
    public void setUp() {
        companyServiceHelperMockedStatic = mockStatic(CompanyServiceHelper.class);
        strategyServiceHelperMockedStatic = mockStatic(StrategyServiceHelper.class);
        rulesAndConfigsUtilsMockedStatic = mockStatic(RulesAndConfigsUtils.class);
        appRulesAndConfigsUtilsMockedStatic = mockStatic(AppRulesAndConfigsUtils.class);
        utilsMockedStatic = mockStatic(Utils.class);
        commonUtilsMockedStatic = mockStatic(CommonUtils.class);
        appRulesAndConfigsUtilsMockedStatic.when(AppRulesAndConfigsUtils::getMaxBundleSeq).thenReturn(3);
        appRulesAndConfigsUtilsMockedStatic.when(AppRulesAndConfigsUtils::isBssOutputPhase2Enabled).thenReturn(true);
    }

    @After
    public void tearDown() {
        if (companyServiceHelperMockedStatic != null) {
            companyServiceHelperMockedStatic.close();
            companyServiceHelperMockedStatic = null;
        }
        if (strategyServiceHelperMockedStatic != null) {
            strategyServiceHelperMockedStatic.close();
            strategyServiceHelperMockedStatic = null;
        }
        if (rulesAndConfigsUtilsMockedStatic != null) {
            rulesAndConfigsUtilsMockedStatic.close();
            rulesAndConfigsUtilsMockedStatic = null;
        }
        if (appRulesAndConfigsUtilsMockedStatic != null) {
            appRulesAndConfigsUtilsMockedStatic.close();
            appRulesAndConfigsUtilsMockedStatic = null;
        }
        if (utilsMockedStatic != null) {
            utilsMockedStatic.close();
            utilsMockedStatic = null;
        }
        if (commonUtilsMockedStatic != null) {
            commonUtilsMockedStatic.close();
            commonUtilsMockedStatic = null;
        }
    }

    private static final String CODE = "5R9";
    private static final long LEGACY_COMPANY_ID = 1234L;
    private static String EMP_ID = "1111";
    private static final String COMP_PLAN_START_DATE = "10-JAN-2018";
    private static String PLAN_START_DATE_STR = "10-JAN-2018";
    private static String QUARTER = "IV";
    String HEAD_QTR_STATE = "FL";
    private static final String COMPANY_NAME = "Trinet Group";
    private static final String KAISER_BAND_CODE = "KBC";
    private static boolean TRANSITION_PERIOD = true;
    private static final int REALM_ID = 3;
    private static long REALM_PLAN_YR_ID = 3333L;
    private static long PRE_REALM_PLAN_YR_ID = 2222L;
    private static long NEXT_REALM_PLAN_YR_ID = 4444L;
    private static String PEO_ID = "peoid";
    final SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yy");
    boolean mbgRenewal = false;
    private static final Date nextPlanYrStartDt = Utils.convertStringToDate("01-JAN-2020", Constants.DATE_FORMAT);
    private static final Date nextPlanYrEndDt = Utils.convertStringToDate("31-DEC-2020", Constants.DATE_FORMAT);
    private static final Date planYrStartDt = Utils.convertStringToDate("01-JAN-2019", Constants.DATE_FORMAT);
    private static final Date planYrEndDt = Utils.convertStringToDate("31-DEC-2019", Constants.DATE_FORMAT);
    private static RealmPlanYear nextRealmPlanYear = null;

    private static final Date internalOpenDate = Utils.convertStringToDate("29-DEC-2020", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
    private static final Date internalCloseDate = Utils.convertStringToDate("15-JAN-2022", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
    private static final Date externalOpenDate = Utils.convertStringToDate("05-JAN-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
    private static final Date externalCloseDate = Utils.convertStringToDate("02-FEB-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
    private static final Date extensionDate = Utils.convertStringToDate("10-FEB-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
    private static final String benefitStartDate = "10-JAN-2018";

    private static final String PROSPECT_CODE = "PROSPECT_CODE_1";
    private static final String PROSPECT_NAME = "PROSPECT_TEST";
    private static final String BEN_END_DATE = "31-Dec-2025";

    /*
     * Test setUpRealmAndRealmPlanYearTest(). isRenewalCompany true
     */
    @Test
    public void getCompanyCommonDataVerifyStrategyHistoryCount_test() {
        String benExchange = Constants.PASSPORT_BEN_EXCHANGE;
        boolean realmPlanYrIsMgbNew = true;
        boolean mbgRenewal = false;

        /*
         * GIVEN - when previous realm plan year is null then strategyHistoryAvailable
         * should be false.
         */
        boolean isPayrollProcessed = true;
        Industry industry = prepareIndustry(10, IndustryType.AG);
        boolean expectedStrategyHistoryAvailable = false;

        HQException hqException = new HQException();
        hqException.setHqState("SC");
        hqException.setPostalCode("29717");
        Optional<HQException> hqOverride = Optional.of(hqException);
        ArgumentCaptor<Company> compArgCaptor = ArgumentCaptor.forClass(Company.class);

        Company basicCompany = prepareCompany(COMPANY_NAME, PLAN_START_DATE_STR, QUARTER, isPayrollProcessed, TRANSITION_PERIOD,
                KAISER_BAND_CODE, HEAD_QTR_STATE);
        basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        basicCompany.setIndustry(industry);

        Company psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, isPayrollProcessed,
                TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
        psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

        RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal,
                planYrStartDt, planYrEndDt);

        prepareCommonMock(psCompany, industry, realmPlanYear);
        Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
        when(psCompanyDao.getCompanyActualHeadCount(CODE)).thenReturn(1);
        when(realmPlanYearService.getPreviousRealmPlanYear(realmPlanYear)).thenReturn(null);
        Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(NEXT_REALM_PLAN_YR_ID, CODE)).thenReturn(hqOverride);
        Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, CommonUtils.getCurrentDate())).thenReturn(psCompany);
        when(companyDao.findByCodeAndRealmPlanYearId(CODE, NEXT_REALM_PLAN_YR_ID)).thenReturn(basicCompany);

        // when
        CommonData commonData = companyServiceImpl.getCompanyCommonData(CODE, EMP_ID, null,false);

        // then
        verify(CompanyServiceHelper.class, times(1));
        assertEquals(LEGACY_COMPANY_ID, commonData.getCompanyCommonData().getLegacyCompanyId());
        CompanyServiceHelper.getIndustry(compArgCaptor.capture());
        assertEquals("SC", compArgCaptor.getValue().getHeadQuatersState());
        assertEquals("29717", compArgCaptor.getValue().getZipCode());
        verify(realmPlanYearService, times(2)).getPreviousRealmPlanYear(nextRealmPlanYear);
        verify(strategyService, times(1)).getStrategiesHistoryCount(any(String.class), any(Long.class));
        assertEquals(expectedStrategyHistoryAvailable,
                commonData.getCompanyCommonData().isStrategyHistoryAvailable());
        assertEquals(extensionDate,
                commonData.getCompanyCommonData().getSelectionDate().getExternalCloseDate());
        assertEquals(externalOpenDate,
                commonData.getCompanyCommonData().getSelectionDate().getExternalOpenDate());
        assertEquals(internalCloseDate,
                commonData.getCompanyCommonData().getSelectionDate().getInternalCloseDate());
        assertEquals(internalOpenDate,
                commonData.getCompanyCommonData().getSelectionDate().getInternalOpenDate());

        /*
         * GIVEN - when previous realm plan year is not null and
         * strategiesHistoryCount is 0 then strategiesHistoryAvailable should be false.
         */
        reset(strategyService, realmPlanYearService);
        Date prevPlanYrStartDt = Utils.convertStringToDate("01-JAN-2019", Constants.DATE_FORMAT);
        Date prevPlanYrEndDt = Utils.convertStringToDate("31-DEC-2019", Constants.DATE_FORMAT);
        expectedStrategyHistoryAvailable = false;

        basicCompany = prepareCompany(COMPANY_NAME, PLAN_START_DATE_STR, QUARTER, isPayrollProcessed, TRANSITION_PERIOD,
                KAISER_BAND_CODE, HEAD_QTR_STATE);
        basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        basicCompany.setIndustry(industry);

        psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, isPayrollProcessed,
                TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
        psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

        realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal, planYrStartDt,
                planYrEndDt);
        RealmPlanYear prevRealmPlYr = prepareRealmPlanYr(PRE_REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal,
                prevPlanYrStartDt, prevPlanYrEndDt);

        industry = prepareIndustry(10, IndustryType.AG);
        prepareCommonMock(psCompany, industry, realmPlanYear);
        Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
        when(psCompanyDao.getCompanyActualHeadCount(CODE)).thenReturn(1);
        when(realmPlanYearService.getPreviousRealmPlanYear(nextRealmPlanYear)).thenReturn(prevRealmPlYr);
        when(strategyService.getStrategiesHistoryCount(CODE, PRE_REALM_PLAN_YR_ID)).thenReturn(0);

        Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, CommonUtils.getCurrentDate())).thenReturn(psCompany);

        // when
        commonData = companyServiceImpl.getCompanyCommonData(CODE, EMP_ID, null,false);

        // then
        verify(strategyService, times(1)).getStrategiesHistoryCount(CODE, PRE_REALM_PLAN_YR_ID);
        assertEquals(expectedStrategyHistoryAvailable,
                commonData.getCompanyCommonData().isStrategyHistoryAvailable());

        /*
         * GIVEN - when previous realm plan year is not null and
         * strategiesHistoryCount is > 0 then strategiesHistoryAvailable should be true.
         */
        reset(strategyService, realmPlanYearService);
        expectedStrategyHistoryAvailable = true;

        basicCompany = prepareCompany(COMPANY_NAME, PLAN_START_DATE_STR, QUARTER, isPayrollProcessed, TRANSITION_PERIOD,
                KAISER_BAND_CODE, HEAD_QTR_STATE);
        basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        basicCompany.setIndustry(industry);


        realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal, prevPlanYrStartDt,
                prevPlanYrEndDt);

        prepareCommonMock(psCompany, industry, realmPlanYear);
        when(realmPlanYearService.getPreviousRealmPlanYear(nextRealmPlanYear)).thenReturn(prevRealmPlYr);
        when(strategyService.getStrategiesHistoryCount(CODE, PRE_REALM_PLAN_YR_ID)).thenReturn(2);
        when(companyDao.findByCodeAndRealmPlanYearId(CODE, NEXT_REALM_PLAN_YR_ID)).thenReturn(basicCompany);

        // when
        commonData = companyServiceImpl.getCompanyCommonData(CODE, EMP_ID, null,false);

        // then
        verify(strategyService, times(1)).getStrategiesHistoryCount(CODE, PRE_REALM_PLAN_YR_ID);
        assertEquals(LEGACY_COMPANY_ID, commonData.getCompanyCommonData().getLegacyCompanyId());
        assertEquals(expectedStrategyHistoryAvailable, commonData.getCompanyCommonData().isStrategyHistoryAvailable());
        assertEquals(true,
                commonData.getCompanyCommonData().isRenewalCompany());
    }

    @Test
    public void getCompanyCommonDataVerifySelectedBenefits_Test() {
        String benExchange = Constants.PASSPORT_BEN_EXCHANGE;
        boolean realmPlanYrIsMgbNew = true;
        Date planYearEnd = null;
        Date planYearStart = null;
        boolean isPayrollProcessed = false;
        Industry industry = prepareIndustry(10, IndustryType.AG);

        RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal,
                planYearStart, planYearEnd);

        Company basicCompany = prepareCompany(COMPANY_NAME, PLAN_START_DATE_STR, QUARTER, isPayrollProcessed, TRANSITION_PERIOD,
                KAISER_BAND_CODE, HEAD_QTR_STATE);
        basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        basicCompany.setIndustry(industry);
        basicCompany.setPayrollProcessed(true);
        basicCompany.setCompanyRegions(new HashSet<>());
        basicCompany.setFundingRegions(new HashSet<>());

        Company psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, isPayrollProcessed,
                TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
        psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        psCompany.setCompanyRegions(new HashSet<>());
        psCompany.setFundingRegions(new HashSet<>());

        ArgumentCaptor<Company> compArgCaptor = ArgumentCaptor.forClass(Company.class);
        ArgumentCaptor<Long> argCaptor1 = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Set> argCaptor2 = ArgumentCaptor.forClass(Set.class);

        Set<String> regions = new HashSet<String>();
        regions.add(null);

        Map<String, Boolean> selectedBenefits = new HashMap<String, Boolean>();
        selectedBenefits.put(Constants.CMTR, false);

        /*
         * Given When BenExchange is not TrinetIV then CMTR should be false.
         */
        prepareCommonMock(psCompany, industry, realmPlanYear);
        when(psCompanyDao.getCompanyActualHeadCount(CODE)).thenReturn(1);

        Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
        // TODO make below mock working
        when(StrategyServiceHelper.getHqStateCity(compArgCaptor.capture())).thenReturn(regions);
        Mockito.when(realmDataDao.getSelectedBenefits(argCaptor1.capture(), argCaptor2.capture())).thenReturn(selectedBenefits);
        Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, CommonUtils.getCurrentDate())).thenReturn(psCompany);

        // when
        CommonData commonData = companyServiceImpl.getCompanyCommonData(CODE, EMP_ID, null,false);

        // then
        assertEquals(1, commonData.getCompanyCommonData().getSelectedBenefits().size());
        assertEquals(false, commonData.getCompanyCommonData().getSelectedBenefits().get(Constants.CMTR));
        assertEquals("Test Bundle", commonData.getCompanyCommonData().getBundleName());

        /*
         * Given When selected benefits is other than CMTR then benefits should be
         * returned result.
         */
        selectedBenefits = new HashMap<String, Boolean>();
        selectedBenefits.put(Constants.DISABILITY, true);
        benExchange = BenExchngEnums.TRINET_IV.getBenExchng();
        planYearStart = new Date();

        basicCompany = prepareCompany(COMPANY_NAME, PLAN_START_DATE_STR, QUARTER, isPayrollProcessed, TRANSITION_PERIOD,
                KAISER_BAND_CODE, HEAD_QTR_STATE);
        basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        basicCompany.setIndustry(industry);
        basicCompany.setPayrollProcessed(true);
        realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal, planYearStart,
                planYearEnd);

        prepareCommonMock(psCompany, industry, realmPlanYear);
        // TODO make below mock working
        when(StrategyServiceHelper.getHqStateCity(any(Company.class))).thenReturn(regions);
        Mockito.when(realmDataDao.getSelectedBenefits(NEXT_REALM_PLAN_YR_ID, regions)).thenReturn(selectedBenefits);

        // when
        commonData = companyServiceImpl.getCompanyCommonData(CODE, EMP_ID, null,false);

        // then
        assertEquals(1, commonData.getCompanyCommonData().getSelectedBenefits().size());
        assertEquals(true, commonData.getCompanyCommonData().getSelectedBenefits().get(Constants.DISABILITY));
        assertEquals(null, commonData.getCompanyCommonData().getSelectedBenefits().get(Constants.CMTR));
        assertEquals("Test Bundle", commonData.getCompanyCommonData().getBundleName());
    }
    @Test
    public void getCompanyCommonData_VerifySelectedBenefits_OMSCExchange_Test() {
        String benExchange = BenExchngEnums.TRINET_OMS.getBenExchng();
        boolean realmPlanYrIsMgbNew = true;
        Date planYearEnd = null;
        Date planYearStart = null;
        boolean isPayrollProcessed = false;
        Industry industry = prepareIndustry(10, IndustryType.AG);

        RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal,
                planYearStart, planYearEnd);

        // GIVEN company is not TIB and has OMS prospect
        Company basicCompany = prepareCompany(COMPANY_NAME, PLAN_START_DATE_STR, QUARTER, isPayrollProcessed, TRANSITION_PERIOD,
                KAISER_BAND_CODE, HEAD_QTR_STATE);
        basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        basicCompany.setOmsOffering(OMB_TLD.name());
        basicCompany.setPayrollProcessed(true);

        Company psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, isPayrollProcessed,
                TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
        psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

        ArgumentCaptor<Long> argCaptor1 = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Set> argCaptor2 = ArgumentCaptor.forClass(Set.class);
        Map<String, Boolean> selectedBenefits = new HashMap<String, Boolean>();
        selectedBenefits.put(Constants.DENTAL, true);
        selectedBenefits.put(Constants.MEDICAL, true);
        selectedBenefits.put(Constants.DENTAL, true);
        selectedBenefits.put(Constants.VISION, true);
        selectedBenefits.put(Constants.DISABILITY, true);

        prepareCommonMock(psCompany, industry, realmPlanYear);
        Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
        Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, CommonUtils.getCurrentDate())).thenReturn(basicCompany);
        Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, NEXT_REALM_PLAN_YR_ID)).thenReturn(basicCompany);
        Mockito.when(realmDataDao.getSelectedBenefits(argCaptor1.capture(), argCaptor2.capture())).thenReturn(selectedBenefits);

        // when
        CommonData commonData = companyServiceImpl.getCompanyCommonData(CODE, EMP_ID, null,false);

        // then
        assertEquals(1, commonData.getCompanyCommonData().getSelectedBenefits().size());
        assertEquals(true, commonData.getCompanyCommonData().getSelectedBenefits().get(Constants.DISABILITY));
        assertEquals(null, commonData.getCompanyCommonData().getSelectedBenefits().get(Constants.MEDICAL));
        assertEquals(null, commonData.getCompanyCommonData().getSelectedBenefits().get(Constants.DENTAL));
        assertEquals(null, commonData.getCompanyCommonData().getSelectedBenefits().get(Constants.VISION));


        // GIVEN company is TIB and has OMS prospect
        selectedBenefits = new HashMap<String, Boolean>();
        selectedBenefits.put(Constants.DENTAL, true);
        selectedBenefits.put(Constants.MEDICAL, true);
        selectedBenefits.put(Constants.DENTAL, true);
        selectedBenefits.put(Constants.VISION, true);
        selectedBenefits.put(Constants.DISABILITY, true);
        Mockito.when(realmDataDao.getSelectedBenefits(argCaptor1.capture(), argCaptor2.capture())).thenReturn(selectedBenefits);

        basicCompany.setOmsOffering(OM_OD_OV_TLD.name());
        Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
        Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, NEXT_REALM_PLAN_YR_ID)).thenReturn(basicCompany);
        Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, CommonUtils.getCurrentDate())).thenReturn(basicCompany);

        // when
        commonData = companyServiceImpl.getCompanyCommonData(CODE, EMP_ID, null,false);

        // then
        assertEquals(4, commonData.getCompanyCommonData().getSelectedBenefits().size());
        assertEquals(true, commonData.getCompanyCommonData().getSelectedBenefits().get(Constants.DISABILITY));
        assertEquals(true, commonData.getCompanyCommonData().getSelectedBenefits().get(Constants.MEDICAL));
        assertEquals(true, commonData.getCompanyCommonData().getSelectedBenefits().get(Constants.DENTAL));
        assertEquals(true, commonData.getCompanyCommonData().getSelectedBenefits().get(Constants.VISION));
    }


    @Test
    public void commonDataStrategyAccessed_ProspectConvertedTest() throws ParseException {
        String benExchange = BenExchngEnums.TRINET_III.getBenExchng();
        boolean realmPlanYrIsMgbNew = true;
        Date planYearEnd = null;
        Date planYearStart = null;
        boolean isPayrollProcessed = false;
        Industry industry = prepareIndustry(10, IndustryType.AG);

        RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal,
                planYearStart, planYearEnd);

        // GIVEN company is not TIB and has OMS prospect
        Company basicCompany = prepareCompany(COMPANY_NAME, PLAN_START_DATE_STR, QUARTER, isPayrollProcessed, TRANSITION_PERIOD,
                KAISER_BAND_CODE, HEAD_QTR_STATE);
        basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        basicCompany.setOmsOffering(null);
        basicCompany.setPayrollProcessed(true);
        basicCompany.setProspectId("XYZ");
        basicCompany.setCompanySetupDate("1-FEB-2026");
        basicCompany.setStrategyAccessed(1);
        basicCompany.setPlYrChangeSyncExcuted(1);
        Company psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, isPayrollProcessed,
                TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
        psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        ArgumentCaptor<Long> argCaptor1 = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Set> argCaptor2 = ArgumentCaptor.forClass(Set.class);
        Map<String, Boolean> selectedBenefits = new HashMap<String, Boolean>();
        selectedBenefits.put(Constants.DENTAL, true);
        selectedBenefits.put(Constants.MEDICAL, true);
        selectedBenefits.put(Constants.VISION, true);
        prepareCompanyNewClientMock(psCompany, industry, realmPlanYear);
        Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
        Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, CommonUtils.getCurrentDate())).thenReturn(basicCompany);
        Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, NEXT_REALM_PLAN_YR_ID)).thenReturn(basicCompany);
        Mockito.when(realmDataDao.getSelectedBenefits(argCaptor1.capture(), argCaptor2.capture())).thenReturn(selectedBenefits);
        utilsMockedStatic.when(() -> Utils.convertStringToDate(AppRulesAndConfigsUtils.getProspectConversionCutOffDate(), Constants.DATE_FORMAT))
                .thenReturn(new SimpleDateFormat(Constants.DATE_FORMAT).parse("1-FEB-2026"));
        utilsMockedStatic.when(() -> Utils.convertStringToDate(basicCompany.getCompanySetupDate(), Constants.DATE_FORMAT))
                .thenReturn(new SimpleDateFormat(Constants.DATE_FORMAT).parse("1-FEB-2026"));  // when
        CommonData commonData = companyServiceImpl.getCompanyCommonData(CODE, EMP_ID, null,false);
        verify(flexRateService, times(0)).syncRateGroupWhenUpdated(basicCompany);
        verify(bandCodesService, times(0)).getBandCodeByType("1234", CommonUtils.getCurrentDate(), BSSApplicationConstants.LIFE, BenExchngEnums.TRINET_III);
        verify(bandCodesService, times(0)).getBandCodeByType("1234", CommonUtils.getCurrentDate(), BSSApplicationConstants.DISABILITY, BenExchngEnums.TRINET_III);
        //then Prospect Converted True
        //then Strategy Accessed True
        assertEquals(true, commonData.getCompanyCommonData().isProspectConvertedClient());
        assertEquals(true, commonData.getCompanyCommonData().isStrategyAccessed());
        assertEquals(true, commonData.getCompanyCommonData().isPlanYearChangeSyncExcuted());
        basicCompany.setCompanySetupDate("1-FEB-2024");
        basicCompany.setStrategyAccessed(0);
        Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
        Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, NEXT_REALM_PLAN_YR_ID)).thenReturn(basicCompany);
        Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, CommonUtils.getCurrentDate())).thenReturn(basicCompany);
        utilsMockedStatic.when(() -> Utils.convertStringToDate(AppRulesAndConfigsUtils.getProspectConversionCutOffDate(), Constants.DATE_FORMAT))
                .thenReturn(new SimpleDateFormat(Constants.DATE_FORMAT).parse("5-FEB-2024"));
        utilsMockedStatic.when(() -> Utils.convertStringToDate(basicCompany.getCompanySetupDate(), Constants.DATE_FORMAT))
                .thenReturn(new SimpleDateFormat(Constants.DATE_FORMAT).parse("1-FEB-2024"));  // when

        // when
        commonData = companyServiceImpl.getCompanyCommonData(CODE, EMP_ID, null,false);
        //then Prospect Converted false
        //then Strategy Accessed false
        assertEquals(false, commonData.getCompanyCommonData().isProspectConvertedClient());
        assertEquals(false, commonData.getCompanyCommonData().isStrategyAccessed());
        
        //when client is not prospectConverted
        basicCompany.setProspectConvertedClient(false);
        commonData = companyServiceImpl.getCompanyCommonData(CODE, EMP_ID, null,false);
        
        //then
        assertEquals(false, commonData.getCompanyCommonData().isProspectConvertedClient());
        assertEquals(true, commonData.getCompanyCommonData().isPlanYearChangeSyncExcuted());
        
        //when prospect
        basicCompany.setProspectCompany(true);
        commonData = companyServiceImpl.getCompanyCommonData(CODE, EMP_ID, null,false);
        
        //then
        assertEquals(true, commonData.getCompanyCommonData().isProspectCompany());
        assertEquals(true, commonData.getCompanyCommonData().isPlanYearChangeSyncExcuted());
    }

    @Test
    public void commonData_shouldSyncRateGroupWhenDifferentials() throws ParseException {
        String benExchange = BenExchngEnums.TRINET_III.getBenExchng();
        boolean realmPlanYrIsMgbNew = true;
        Date planYearEnd = null;
        Date planYearStart = null;
        boolean isPayrollProcessed = false;
        Industry industry = prepareIndustry(10, IndustryType.AG);
        RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal,
                planYearStart, planYearEnd);
        Company basicCompany = prepareCompany(COMPANY_NAME, "01-JAN-2026", QUARTER, isPayrollProcessed, TRANSITION_PERIOD,
                KAISER_BAND_CODE, HEAD_QTR_STATE);
        basicCompany.setBssNaicsCode(1234);
        basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        basicCompany.setOmsOffering(null);
        basicCompany.setPayrollProcessed(true);
        basicCompany.setProspectId("XYZ");
        basicCompany.setCompanySetupDate("1-FEB-2026");
        basicCompany.setStrategyAccessed(1);
        basicCompany.setPlYrChangeSyncExcuted(1);
        basicCompany.setRiskType(RiskTypeEnum.DIFFERENTIALS);
        Company psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, isPayrollProcessed,
                TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
        psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        ArgumentCaptor<Long> argCaptor1 = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Set> argCaptor2 = ArgumentCaptor.forClass(Set.class);
        Map<String, Boolean> selectedBenefits = new HashMap<String, Boolean>();
        selectedBenefits.put(Constants.DENTAL, true);
        selectedBenefits.put(Constants.MEDICAL, true);
        selectedBenefits.put(Constants.VISION, true);
        prepareCompanyNewClientMock(psCompany, industry, realmPlanYear);
        Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
        Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, CommonUtils.getCurrentDate())).thenReturn(basicCompany);
        Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, NEXT_REALM_PLAN_YR_ID)).thenReturn(basicCompany);
        Mockito.when(realmDataDao.getSelectedBenefits(argCaptor1.capture(), argCaptor2.capture())).thenReturn(selectedBenefits);
        utilsMockedStatic.when(() -> Utils.convertStringToDate(AppRulesAndConfigsUtils.getProspectConversionCutOffDate(), Constants.DATE_FORMAT))
                .thenReturn(new SimpleDateFormat(Constants.DATE_FORMAT).parse("1-FEB-2026"));
        utilsMockedStatic.when(() -> Utils.convertStringToDate(basicCompany.getCompanySetupDate(), Constants.DATE_FORMAT))
                .thenReturn(new SimpleDateFormat(Constants.DATE_FORMAT).parse("1-FEB-2026"));  // when
        CommonData commonData = companyServiceImpl.getCompanyCommonData(CODE, EMP_ID, null, false);

        assertEquals(true, commonData.getCompanyCommonData().isProspectConvertedClient());
        assertEquals(true, commonData.getCompanyCommonData().isStrategyAccessed());
        assertEquals(true, commonData.getCompanyCommonData().isPlanYearChangeSyncExcuted());

        verify(flexRateService, times(1)).syncRateGroupWhenUpdated(
                eq(basicCompany)
        );
        verify(bandCodesService, times(1)).getBandCodeByType("1234", CommonUtils.getCurrentDate(),
                BSSApplicationConstants.LIFE, BenExchngEnums.TRINET_III);
        verify(bandCodesService, times(1)).getBandCodeByType("1234", CommonUtils.getCurrentDate(),
                BSSApplicationConstants.DISABILITY, BenExchngEnums.TRINET_III);
    }

    @Test
    public void getCompanyPlanYearData_clientRenewalCompany() {

        List<CompanyRealmData> actualResults = companyServiceImpl.getCompanyPlanYearData(CODE, EMP_ID);

        verify(companyDataDao, times(1)).getAvailableCompanyRealms(CODE, false);
        assertEquals(0, actualResults.size());
    }

    @Test
    public void getCompanyPlanYearData_prospect() {
        String prospectCompCode = "0010z00001aloe4AAA";
        List<CompanyRealmData> expectedResults = prepareCompanyPlanYearData();

        when(companyDataDao.getAvailableCompanyRealms(prospectCompCode, false)).thenReturn(expectedResults);

        List<CompanyRealmData> actualResults = companyServiceImpl.getCompanyPlanYearData(prospectCompCode, EMP_ID);

        verify(companyDataDao, times(1)).getAvailableCompanyRealms(prospectCompCode, false);
        assertEquals(2, actualResults.size());
    }

    @Test
    public void getCompanyNameCompanyExistsTest() {
        String companyCode = "G48";
        String companyName = "G48 LLC";
        Map<String, String> companies = Map.of(companyCode, companyName);
        // given
        when(psCompanyDao.findCompaniesNames(companyNamesSet.capture())).thenReturn(companies);
        // when
        String outputCompanyName = companyServiceImpl.getCompanyName(companyCode);
        // then
        assertNotNull(outputCompanyName);
        assertEquals(companyName, outputCompanyName);
        verify(psCompanyDao, times(1)).findCompaniesNames(companyNamesSet.capture());
    }

    @Test
    public void getCompanyNameCompanyNotExistsTest() {
        String companyCode = "G48";
        Map<String, String> companies = Collections.emptyMap();
        // given
        when(psCompanyDao.findCompaniesNames(companyNamesSet.capture())).thenReturn(companies);
        // when
        String outputCompanyName = companyServiceImpl.getCompanyName(companyCode);
        // then
        assertEquals(StringUtils.EMPTY, outputCompanyName);
        verify(psCompanyDao, times(1)).findCompaniesNames(companyNamesSet.capture());
    }

    @Test
    public void planYearChangeExecutedWithPositiveTest() {

        // Given
        prepareCommonDataMockForProspect(1);

        Company prospectData = prepareProspect();
        ArgumentCaptor<Long> argCaptor1 = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Set> argCaptor2 = ArgumentCaptor.forClass(Set.class);
        Map<String, Boolean> selectedBenefits = new HashMap<String, Boolean>();
        selectedBenefits.put(Constants.DENTAL, true);
        selectedBenefits.put(Constants.MEDICAL, true);

        // When
        CommonData commonData = companyServiceImpl.getCompanyCommonData(PROSPECT_CODE, EMP_ID, null,false);


        // Then
        assertEquals(1, commonData.getCompanyCommonData().getSelectedBenefits().size());
        assertEquals(true, commonData.getCompanyCommonData().isPlanYearChangeSyncExcuted());
    }

    @Test
    public void findCompanyByTest() {
        String companyCode = "G48";
        long realmPlanYearId = 83;
        Company company = new Company();
        company.setId(123);
        company.setCode(companyCode);
        company.setRealmPlanYearId(realmPlanYearId);
        // given
        when(companyDao.findByCodeAndRealmPlanYearId(companyCode, realmPlanYearId)).thenReturn(company);
        // when
        Company retCompany = companyServiceImpl.findCompanyBy(companyCode, realmPlanYearId);
        // then
        assertNotNull(retCompany);
        assertEquals(123, retCompany.getId());
        assertEquals("G48", retCompany.getCode());

        verify(companyDao, times(1)).findByCodeAndRealmPlanYearId(companyCode, realmPlanYearId);
    }

    @Test
    public void planYearChangeExecutedWithNegativeTest() {

        // Given
        prepareCommonDataMockForProspect(0);

        Company prospectData = prepareProspect();
        ArgumentCaptor<Long> argCaptor1 = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Set> argCaptor2 = ArgumentCaptor.forClass(Set.class);
        Map<String, Boolean> selectedBenefits = new HashMap<String, Boolean>();
        selectedBenefits.put(Constants.DENTAL, true);
        selectedBenefits.put(Constants.MEDICAL, true);

        // When
        CommonData commonData = companyServiceImpl.getCompanyCommonData(PROSPECT_CODE, EMP_ID, null,false);

        // Then
        assertEquals(1, commonData.getCompanyCommonData().getSelectedBenefits().size());
        assertEquals(false, commonData.getCompanyCommonData().isPlanYearChangeSyncExcuted());
    }

    @Test
    public void planYearChangeExecutedWithNullTest() {

        // Given
        prepareCommonDataMockForProspect(null);

        Company prospectData = prepareProspect();
        ArgumentCaptor<Long> argCaptor1 = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Set> argCaptor2 = ArgumentCaptor.forClass(Set.class);
        Map<String, Boolean> selectedBenefits = new HashMap<String, Boolean>();
        selectedBenefits.put(Constants.DENTAL, true);
        selectedBenefits.put(Constants.MEDICAL, true);

        // When
        CommonData commonData = companyServiceImpl.getCompanyCommonData(PROSPECT_CODE, EMP_ID, null,false);

        // Then
        assertEquals(1, commonData.getCompanyCommonData().getSelectedBenefits().size());
        assertEquals(false, commonData.getCompanyCommonData().isPlanYearChangeSyncExcuted());
    }

    /**
     * GIVEN a company is retrieved and strategyAccessed is true,
     * WHEN getCompanyCommonData is called,
     * THEN the company is saved with strategyAccessed set and selected benefits are correctly returned.
     */
    @Test
    public void getCompanyCommonData_VerifyStrategyAccessedUpdated() {
        String benExchange = BenExchngEnums.TRINET_III.getBenExchng();
        Industry industry = prepareIndustry(10, IndustryType.AG);

        RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, true, mbgRenewal,
                null, null);

        Company basicCompany = prepareCompany(COMPANY_NAME, PLAN_START_DATE_STR, QUARTER, false, TRANSITION_PERIOD,
                KAISER_BAND_CODE, HEAD_QTR_STATE);
        basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        basicCompany.setOmsOffering(null);
        basicCompany.setPayrollProcessed(true);

        Company psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, false,
                TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);

        ArgumentCaptor<Long> argCaptor1 = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Set> argCaptor2 = ArgumentCaptor.forClass(Set.class);
        Map<String, Boolean> selectedBenefits = new HashMap<>();
        selectedBenefits.put(Constants.DENTAL, true);
        selectedBenefits.put(Constants.MEDICAL, true);

        prepareCommonMock(psCompany, industry, realmPlanYear);
        Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
        Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, CommonUtils.getCurrentDate())).thenReturn(basicCompany);
        Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, NEXT_REALM_PLAN_YR_ID)).thenReturn(basicCompany);
        Mockito.when(realmDataDao.getSelectedBenefits(argCaptor1.capture(), argCaptor2.capture())).thenReturn(selectedBenefits);

        // when
        CommonData commonData = companyServiceImpl.getCompanyCommonData(CODE, EMP_ID, null, true);

        // then
        assertEquals(2, commonData.getCompanyCommonData().getSelectedBenefits().size());
        assertEquals(true, commonData.getCompanyCommonData().getSelectedBenefits().get(Constants.DENTAL));
        assertEquals(true, commonData.getCompanyCommonData().getSelectedBenefits().get(Constants.MEDICAL));

        //verify
        verify(companyDao, times(1)).save(basicCompany);
        verify(psCompanyDao, times(1)).getBasicCompanyDetails(CODE);
        verify(companyDao, times(2)).findByCodeAndRealmPlanYearId(basicCompany.getCode(), basicCompany.getRealmPlanYearId());
    }

    @Test
    public void getCompanyDetailsAleTest() throws ParseException {
        // Given
        boolean history = false;
        boolean mbgRenewal = false;
        String benExchange = Constants.PASSPORT_BEN_EXCHANGE;
        Set<String> bssCompanyRegions = new HashSet<>();
        bssCompanyRegions.add("TX");

        Company createdBssCompany = new Company();

        Company basicCompany = prepareCompany(COMPANY_NAME, PLAN_START_DATE_STR, QUARTER, false, TRANSITION_PERIOD,
                KAISER_BAND_CODE, HEAD_QTR_STATE);
        basicCompany.setRenewalCompany(true);
        basicCompany.setProspectId("XYZ");
        basicCompany.setCompanySetupDate("1-FEB-2026");
        basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

        Industry industry = prepareIndustry(10, IndustryType.AG);

        Company psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, false, TRANSITION_PERIOD,
                KAISER_BAND_CODE, HEAD_QTR_STATE);
        psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));

        RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, true, mbgRenewal, null, null);

        prepareCompanyNewClientMock(psCompany, industry, realmPlanYear);


        Mockito.when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
        Mockito.when(realmPlanYearService.getRealmPlanYear(REALM_ID, QUARTER,
                Utils.convertStringToDate(COMP_PLAN_START_DATE, Constants.DATE_FORMAT))).thenReturn(realmPlanYear);
        Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(realmPlanYear)).thenReturn(realmPlanYear);
        Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, REALM_PLAN_YR_ID)).thenReturn(basicCompany);
        when(CompanyServiceHelper.createBssCompany(basicCompany)).thenReturn(createdBssCompany);
        Mockito.when(hqExceptionDao.findByIdRealmYrIdAndIdCompany(REALM_PLAN_YR_ID, CODE)).thenReturn(Optional.empty());
        Mockito.when(realmPlanYearService.getMaxRealmPlanYear(REALM_ID, QUARTER)).thenReturn(realmPlanYear);
        Mockito.when(psCompanyDao.isNewBandsAvailable(basicCompany, realmPlanYear.getPlanYearStart())).thenReturn(true);

        Mockito.when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany,
                        Utils.convertStringToDate(basicCompany.getPlanStartDate(), Constants.DATE_FORMAT)))
                .thenReturn(basicCompany);

        companyServiceHelperMockedStatic.when(() -> CompanyServiceHelper.isClientCompanyPattern(any())).thenCallRealMethod();
        when(AppRulesAndConfigsUtils.getProspectConversionCutOffDate()).thenReturn("1-JAN-2020");
        Mockito.when(bssCoreServiceClient.getAleStatus(basicCompany.getId())).thenReturn(true);
        utilsMockedStatic.when(() -> Utils.convertStringToDate(AppRulesAndConfigsUtils.getProspectConversionCutOffDate(), Constants.DATE_FORMAT))
                .thenReturn(new SimpleDateFormat(Constants.DATE_FORMAT).parse("1-FEB-2026"));
        utilsMockedStatic.when(() -> Utils.convertStringToDate(basicCompany.getCompanySetupDate(), Constants.DATE_FORMAT))
                .thenReturn(new SimpleDateFormat(Constants.DATE_FORMAT).parse("1-FEB-2026"));  // when

        // When Company is ALE
        Company aleCompany = companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

        // Then
        assertEquals(true, aleCompany.isEligAle());

        Mockito.when(bssCoreServiceClient.getAleStatus(basicCompany.getId())).thenReturn(false);

        // When Company is non ALE
        Company nonAleCompany = companyServiceImpl.getCompanyDetails(CODE, history, EMP_ID, null);

        // Then
        assertEquals(false, nonAleCompany.isEligAle());
    }

    @Test
    public void getCompanyCommonData_preferencesLinkStatus_showWhenRuleEnabledAndBundleSeqIsOneOrTwo() {
        CommonData commonData = fetchCommonDataForPreferencesStatus(1, true, 3);
        assertEquals("show", commonData.getCompanyCommonData().getPreferencesLinkStatus());

        commonData = fetchCommonDataForPreferencesStatus(2, true, 3);
        assertEquals("show", commonData.getCompanyCommonData().getPreferencesLinkStatus());
    }

    @Test
    public void getCompanyCommonData_preferencesLinkStatus_hideWhenRuleDisabled() {
        CommonData commonData = fetchCommonDataForPreferencesStatus(1, false, 3);
        assertEquals("hide", commonData.getCompanyCommonData().getPreferencesLinkStatus());
    }

    @Test
    public void getCompanyCommonData_preferencesLinkStatus_readOnlyWhenRuleEnabledAndBundleSeqMatchesMax() {
        CommonData commonData = fetchCommonDataForPreferencesStatus(3, true, 3);
        assertEquals("readOnly", commonData.getCompanyCommonData().getPreferencesLinkStatus());
    }

    @Test
    public void getCompanyCommonData_preferencesLinkStatus_disabledWhenRuleEnabledAndBundleSeqGreaterThanMax() {
        CommonData commonData = fetchCommonDataForPreferencesStatus(4, true, 3);
        assertEquals("disabled", commonData.getCompanyCommonData().getPreferencesLinkStatus());
    }

    @Test
    public void getCompanyCommonData_preferencesLinkStatus_showWhenRuleEnabledAndBundleSeqLessThanConfiguredMax() {
        CommonData commonData = fetchCommonDataForPreferencesStatus(3, true, 5);
        assertEquals("show", commonData.getCompanyCommonData().getPreferencesLinkStatus());
    }

    @Test
    public void getCompanyCommonData_preferencesLinkStatus_hideWhenBundleSeqIsZero() {
        CommonData commonData = fetchCommonDataForPreferencesStatus(0, true, 3);
        assertEquals("hide", commonData.getCompanyCommonData().getPreferencesLinkStatus());
    }

    private void prepareCommonMock(Company psCompany, Industry industry, RealmPlanYear realmPlanYear) {
        boolean realmPlanYrIsMgbNew = true;
        nextRealmPlanYear = prepareRealmPlanYr(NEXT_REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal,
                nextPlanYrStartDt, nextPlanYrEndDt);
        final Company bssCompany = new Company();
        SchedTbl schedTbl = new SchedTbl();
        schedTbl.setInternalOpenDate(internalOpenDate);
        schedTbl.setInternalCloseDate(internalCloseDate);
        schedTbl.setOpenDate(externalOpenDate);
        schedTbl.setCloseDate(externalCloseDate);
        schedTbl.setExtensionEndDate(extensionDate);
        bssCompany.setBundleId(1L);
        Bundle bundle =  prepareBundle();
        final ArgumentCaptor<Company> compArgCapture = ArgumentCaptor.forClass(Company.class);
        final ArgumentCaptor<Company> bssCompArgCapture = ArgumentCaptor.forClass(Company.class);
        companyServiceHelperMockedStatic.when(() -> CompanyServiceHelper.constructSelectionDate(any())).thenCallRealMethod();
        companyServiceHelperMockedStatic.when(() -> CompanyServiceHelper.isClientCompanyPattern(any())).thenCallRealMethod();
        companyServiceHelperMockedStatic.when(() -> CompanyServiceHelper.isTibProspect(any())).thenCallRealMethod();
        companyServiceHelperMockedStatic.when(() -> CompanyServiceHelper.isOMSExchange(any())).thenCallRealMethod();
        Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, NEXT_REALM_PLAN_YR_ID)).thenReturn(bssCompany);
        Mockito.when(realmPlanYearService.getCurrentRealmPlanYear(REALM_ID, QUARTER)).thenReturn(realmPlanYear);
        Mockito.when(realmPlanYearService.getNextRealmPlanYear(realmPlanYear)).thenReturn(nextRealmPlanYear);
        Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(nextRealmPlanYear)).thenReturn(realmPlanYear);
        Mockito.when(schedTblService.getCalcuatedScheduleDates(CODE, QUARTER, NEXT_REALM_PLAN_YR_ID)).thenReturn(schedTbl);
        Mockito.when(benefitsBundleService.getBundleById(bssCompany.getBundleId().longValue())).thenReturn(bundle);
        when(CompanyServiceHelper.getIndustry(any(Company.class))).thenReturn(industry);
        when(AppRulesAndConfigsUtils.getProspectConversionCutOffDate()).thenReturn("1-JAN-2020");
        when(rateSystemService.getRateSystemRateType(any(Company.class))).thenReturn("REGIONAL");
        // Assuming companyServiceHelperMockedStatic is a MockedStatic<CompanyServiceHelper>
        companyServiceHelperMockedStatic.when(() ->
                CompanyServiceHelper.mapPSCompanyDataToBSSCompany(
                        compArgCapture.capture(),
                        bssCompArgCapture.capture()
                )
        ).thenAnswer(invocation -> {
            Company company = compArgCapture.getValue();
            Company bssCompany1 = bssCompArgCapture.getValue();
            bssCompany1.setIndustry(company.getIndustry());
            bssCompany1.setRealmPlanYear(company.getRealmPlanYear());
            bssCompany1.setRealmPlanYearId(company.getRealmPlanYearId());
            bssCompany1.setRealm(company.getRealm());
            bssCompany1.setPayrollProcessed(company.isPayrollProcessed());
            bssCompany1.setCode(company.getCode());
            bssCompany1.setRenewalCompany(company.isRenewalCompany());
            bssCompany1.setCompanyRegions(company.getCompanyRegions());
            bssCompany1.setFundingRegions(company.getFundingRegions());
            bssCompany1.setSchedTbl(company.getSchedTbl());
            if (company.getBenefitStartDate() != null) {
                bssCompany1.setBenefitStartDate(company.getBenefitStartDate());
            }
            if (company.getRealmPlanYear().getPlanYearStart() != null)
                bssCompany1.setPlanStartDate(formatter.format(company.getRealmPlanYear().getPlanYearStart()));
            else
                bssCompany1.setPlanStartDate(company.getPlanStartDate());
            return null;
        });
    }

    private void prepareCompanyNewClientMock(Company psCompany, Industry industry, RealmPlanYear realmPlanYear) {
        boolean realmPlanYrIsMgbNew = true;
        nextRealmPlanYear = prepareRealmPlanYr(NEXT_REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal,
                nextPlanYrStartDt, nextPlanYrEndDt);
        final Company bssCompany = new Company();
        SchedTbl schedTbl = new SchedTbl();
        schedTbl.setInternalOpenDate(internalOpenDate);
        schedTbl.setInternalCloseDate(internalCloseDate);
        schedTbl.setOpenDate(externalOpenDate);
        schedTbl.setCloseDate(externalCloseDate);
        schedTbl.setExtensionEndDate(extensionDate);
        bssCompany.setBundleId(1L);
        bssCompany.setCompanySetupDate("1-FEB-2026");
        bssCompany.setProspectId(PROSPECT_CODE);
        Bundle bundle =  prepareBundle();
        final ArgumentCaptor<Company> compArgCapture = ArgumentCaptor.forClass(Company.class);
        final ArgumentCaptor<Company> bssCompArgCapture = ArgumentCaptor.forClass(Company.class);
        when( CompanyServiceHelper.constructSelectionDate( any() ) ).thenCallRealMethod();
        when( CompanyServiceHelper.isClientCompanyPattern( any() ) ).thenCallRealMethod();
        when( CompanyServiceHelper.isTibProspect( any() ) ).thenCallRealMethod();
        when( CompanyServiceHelper.isOMSExchange( any() ) ).thenCallRealMethod();
        Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, NEXT_REALM_PLAN_YR_ID)).thenReturn(bssCompany);
        Mockito.when(realmPlanYearService.getCurrentRealmPlanYear(REALM_ID, QUARTER)).thenReturn(realmPlanYear);
        Mockito.when(realmPlanYearService.getNextRealmPlanYear(realmPlanYear)).thenReturn(nextRealmPlanYear);
        Mockito.when(realmPlanYearService.getPreviousRealmPlanYear(nextRealmPlanYear)).thenReturn(realmPlanYear);
        Mockito.when(schedTblService.getCalcuatedScheduleDates(CODE, QUARTER, NEXT_REALM_PLAN_YR_ID)).thenReturn(schedTbl);
        when(CompanyServiceHelper.getIndustry(any(Company.class))).thenReturn(industry);
        when(AppRulesAndConfigsUtils.getProspectConversionCutOffDate()).thenReturn("1-JAN-2025");
        when(rateSystemService.getRateSystemRateType(any(Company.class))).thenReturn("REGIONAL");
        // This assumes companyServiceHelperMockedStatic is a MockedStatic<CompanyServiceHelper>
        companyServiceHelperMockedStatic.when(() ->
                CompanyServiceHelper.mapPSCompanyDataToBSSCompany(
                        compArgCapture.capture(),
                        bssCompArgCapture.capture()
                )
        ).thenAnswer(invocation -> {
            Company company = compArgCapture.getValue();
            Company bssCompany1 = bssCompArgCapture.getValue();
            bssCompany1.setIndustry(company.getIndustry());
            bssCompany1.setRealmPlanYear(company.getRealmPlanYear());
            bssCompany1.setRealmPlanYearId(company.getRealmPlanYearId());
            bssCompany1.setRealm(company.getRealm());
            bssCompany1.setPayrollProcessed(company.isPayrollProcessed());
            bssCompany1.setCode(company.getCode());
            bssCompany1.setRenewalCompany(company.isRenewalCompany());
            bssCompany1.setCompanyRegions(company.getCompanyRegions());
            bssCompany1.setFundingRegions(company.getFundingRegions());
            bssCompany1.setSchedTbl(company.getSchedTbl());
            if (company.getBenefitStartDate() != null) {
                bssCompany1.setBenefitStartDate(company.getBenefitStartDate());
            }
            if (company.getRealmPlanYear().getPlanYearStart() != null)
                bssCompany1.setPlanStartDate(formatter.format(company.getRealmPlanYear().getPlanYearStart()));
            else
                bssCompany1.setPlanStartDate(company.getPlanStartDate());
            return null;
        });
    }

    private Bundle prepareBundle() {
        Bundle bundle = new Bundle();
        bundle.setId(1L);
        bundle.setName("Test Bundle");
        return bundle;

    }

    private void prepareCommonDataMockForProspect(Integer isPlYrChangeSyncExecuted) {

        String benExchange = Constants.PASSPORT_BEN_EXCHANGE;
        boolean realmPlanYrIsMgbNew = true;
        boolean isPayrollProcessed = false;
        Industry industry = prepareIndustry(10, IndustryType.AG);

        RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal,
                planYrStartDt, planYrEndDt);

        Company psCompany = prepareCompany(PROSPECT_NAME, COMP_PLAN_START_DATE, QUARTER, isPayrollProcessed,
                TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
        psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        psCompany.setCompanyRegions(new HashSet<>());
        psCompany.setFundingRegions(new HashSet<>());

        ArgumentCaptor<Company> compArgCaptor = ArgumentCaptor.forClass(Company.class);
        ArgumentCaptor<Long> argCaptor1 = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Set> argCaptor2 = ArgumentCaptor.forClass(Set.class);

        Set<String> regions = new HashSet<String>();
        regions.add(null);

        Map<String, Boolean> selectedBenefits = new HashMap<String, Boolean>();
        selectedBenefits.put(Constants.CMTR, false);

        nextRealmPlanYear = prepareRealmPlanYr(NEXT_REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal,
                nextPlanYrStartDt, nextPlanYrEndDt);
        final Company bssCompany = new Company();
        SchedTbl schedTbl = new SchedTbl();
        schedTbl.setInternalOpenDate(internalOpenDate);
        schedTbl.setInternalCloseDate(internalCloseDate);
        schedTbl.setOpenDate(externalOpenDate);
        schedTbl.setCloseDate(externalCloseDate);
        schedTbl.setExtensionEndDate(extensionDate);

        bssCompany.setSchedTbl(schedTbl);
        bssCompany.setIndustry(industry);

        Company basicCompany = prepareCompany(PROSPECT_NAME, COMP_PLAN_START_DATE, QUARTER, isPayrollProcessed,
                TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
        basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        basicCompany.setIndustry(industry);
        basicCompany.setPayrollProcessed(true);
        basicCompany.setCompanyRegions(new HashSet<>());
        basicCompany.setFundingRegions(new HashSet<>());

        bssCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        bssCompany.setIndustry(industry);
        bssCompany.setPayrollProcessed(false);
        bssCompany.setCompanyRegions(new HashSet<>());
        bssCompany.setFundingRegions(new HashSet<>());
        bssCompany.setRealmPlanYear(realmPlanYear);
        bssCompany.setBenefitStartDate(benefitStartDate);
        bssCompany.setProspectCompany(true);
        bssCompany.setPlYrChangeSyncExcuted(isPlYrChangeSyncExecuted);

        final ArgumentCaptor<Company> compArgCapture = ArgumentCaptor.forClass(Company.class);
        final ArgumentCaptor<Company> bssCompArgCapture = ArgumentCaptor.forClass(Company.class);
        when(CompanyServiceHelper.constructSelectionDate(any())).thenCallRealMethod();
        when(CompanyServiceHelper.isClientCompanyPattern(any())).thenCallRealMethod();
        when(CompanyServiceHelper.isTibProspect(any())).thenCallRealMethod();
        when(CompanyServiceHelper.isOMSExchange(any())).thenCallRealMethod();
        Mockito.when(prospectCompanyService.getProspectCompanyDetails(PROSPECT_CODE, null)).thenReturn(bssCompany);
        when(CompanyServiceHelper.getIndustry(any(Company.class))).thenReturn(industry);

        when(StrategyServiceHelper.getHqStateCity(compArgCaptor.capture())).thenReturn(regions);
        Mockito.when(realmDataDao.getSelectedBenefits(argCaptor1.capture(), argCaptor2.capture()))
                .thenReturn(selectedBenefits);
        companyServiceHelperMockedStatic.when(() ->
                CompanyServiceHelper.mapPSCompanyDataToBSSCompany(
                        compArgCapture.capture(),
                        bssCompArgCapture.capture()
                )
        ).thenAnswer(invocation -> {
            Company company = compArgCapture.getValue();
            Company bssCompany1 = bssCompArgCapture.getValue();
            bssCompany1.setIndustry(company.getIndustry());
            bssCompany1.setRealmPlanYear(company.getRealmPlanYear());
            bssCompany1.setRealmPlanYearId(company.getRealmPlanYearId());
            bssCompany1.setRealm(company.getRealm());
            bssCompany1.setPayrollProcessed(company.isPayrollProcessed());
            bssCompany1.setCode(company.getCode());
            bssCompany1.setRenewalCompany(company.isRenewalCompany());
            bssCompany1.setCompanyRegions(company.getCompanyRegions());
            bssCompany1.setFundingRegions(company.getFundingRegions());
            bssCompany1.setSchedTbl(company.getSchedTbl());
            if (company.getBenefitStartDate() != null) {
                bssCompany1.setBenefitStartDate(company.getBenefitStartDate());
            }
            if (company.getRealmPlanYear().getPlanYearStart() != null)
                bssCompany1.setPlanStartDate(formatter.format(company.getRealmPlanYear().getPlanYearStart()));
            else
                bssCompany1.setPlanStartDate(company.getPlanStartDate());
            return null;
        });
    }

    private Company prepareProspect() {
        Company cmp = new Company();
        cmp.setId(164484);
        cmp.setCode(PROSPECT_CODE);
        cmp.setName(PROSPECT_NAME);
        cmp.setHeadQuatersState(HEAD_QTR_STATE);
        cmp.setRealmPlanYearId(REALM_ID);
        cmp.setQuater(QUARTER);
        cmp.setPlanStartDate(COMP_PLAN_START_DATE);
        cmp.setPlanEndDate(BEN_END_DATE);
        cmp.setQuater(Constants.PASSPORT_BEN_EXCHANGE);
        cmp.setPayrollProcessed(false);
        cmp.setTransitionPeriod(false);
        BandCodes bandCodes = new BandCodes();
        bandCodes.setKaiserBandCode(KAISER_BAND_CODE);
        cmp.setBandCodes(bandCodes);
        cmp.setBenefitStartDate(COMP_PLAN_START_DATE);
        cmp.setPlYrChangeSyncExcuted(1);
        cmp.setProspectCompany(true);
        return cmp;
    }


    private Company prepareCompany(String companyName, String planStartDate, String quarter, boolean isPayrollProcessed,
            boolean transitionPeriod, String kaiserBandCode, String headQtrState) {
        Company cmp = new Company();
        cmp.setCode(CODE);
        cmp.setId(LEGACY_COMPANY_ID);
        cmp.setName(companyName);
        cmp.setPlanStartDate(planStartDate);
        cmp.setQuater(quarter);
        cmp.setPayrollProcessed(isPayrollProcessed);
        cmp.setTransitionPeriod(transitionPeriod);
        BandCodes bandCodes = new BandCodes();
        bandCodes.setKaiserBandCode(kaiserBandCode);
        cmp.setHeadQuatersState(headQtrState);
        cmp.setBandCodes(bandCodes);
        cmp.setBenefitStartDate(benefitStartDate);
        return cmp;
    }

    private Realm prepareRealm(int id, String peoId, String benExchange) {
        Realm realm = new Realm();
        realm.setId(id);
        realm.setPeoid(peoId);
        realm.setBenExchange(benExchange);
        return realm;
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

    private List<CompanyRealmData> prepareCompanyPlanYearData() {
        List<CompanyRealmData> returnList = new ArrayList<>();
        CompanyRealmData companyRealmData = new CompanyRealmData();
        companyRealmData.setRecordType("future");
        returnList.add(companyRealmData);
        companyRealmData = new CompanyRealmData();
        companyRealmData.setRecordType("current");
        returnList.add(companyRealmData);
        return returnList;
    }



    private CommonData fetchCommonDataForPreferencesStatus(Integer bundleSeq, boolean displayPreferenceEnabled,
                                                            int maxBundleSeq) {
        String benExchange = Constants.PASSPORT_BEN_EXCHANGE;
        boolean realmPlanYrIsMgbNew = true;
        boolean isPayrollProcessed = false;
        Industry industry = prepareIndustry(10, IndustryType.AG);

        RealmPlanYear realmPlanYear = prepareRealmPlanYr(REALM_PLAN_YR_ID, realmPlanYrIsMgbNew, mbgRenewal,
                null, null);

        Company basicCompany = prepareCompany(COMPANY_NAME, PLAN_START_DATE_STR, QUARTER, isPayrollProcessed,
                TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
        basicCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        basicCompany.setIndustry(industry);
        basicCompany.setPayrollProcessed(true);
        basicCompany.setCompanyRegions(new HashSet<>());
        basicCompany.setFundingRegions(new HashSet<>());

        Company psCompany = prepareCompany(COMPANY_NAME, COMP_PLAN_START_DATE, QUARTER, isPayrollProcessed,
                TRANSITION_PERIOD, KAISER_BAND_CODE, HEAD_QTR_STATE);
        psCompany.setRealm(prepareRealm(REALM_ID, PEO_ID, benExchange));
        psCompany.setCompanyRegions(new HashSet<>());
        psCompany.setFundingRegions(new HashSet<>());

        prepareCommonMock(psCompany, industry, realmPlanYear);

        Company bssCompany = new Company();
        bssCompany.setBundleId(1L);
        bssCompany.setBundleSeq(bundleSeq);
        Mockito.when(companyDao.findByCodeAndRealmPlanYearId(CODE, NEXT_REALM_PLAN_YR_ID)).thenReturn(bssCompany);

        Set<String> regions = new HashSet<>();
        regions.add(null);
        Map<String, Boolean> selectedBenefits = new HashMap<>();
        selectedBenefits.put(Constants.CMTR, false);

        when(psCompanyDao.getBasicCompanyDetails(CODE)).thenReturn(basicCompany);
        when(psCompanyDao.getCompanyActualHeadCount(CODE)).thenReturn(1);
        when(psCompanyDao.getCompanyDetailsByEffdt(basicCompany, CommonUtils.getCurrentDate())).thenReturn(psCompany);
        when(StrategyServiceHelper.getHqStateCity(any(Company.class))).thenReturn(regions);
        when(realmDataDao.getSelectedBenefits(any(Long.class), any(Set.class))).thenReturn(selectedBenefits);
        when(realmDataDao.getCompanyLocationStates(CODE)).thenReturn(new HashSet<>());
        when(realmDataDao.getRealmFundingTypes(any(Long.class))).thenReturn(Collections.emptyList());
        when(realmWaitPeriodService.getWaitPeriodsByRelamPlanYear(any(Long.class))).thenReturn(Collections.emptyList());

        rulesAndConfigsUtilsMockedStatic.when(() -> RulesAndConfigsUtils.isDisplayPreferenceProspectEnabled(any(Long.class)))
                .thenReturn(displayPreferenceEnabled);
        appRulesAndConfigsUtilsMockedStatic.when(AppRulesAndConfigsUtils::getMaxBundleSeq)
                .thenReturn(maxBundleSeq);

        return companyServiceImpl.getCompanyCommonData(CODE, EMP_ID, null, false);
    }
}

