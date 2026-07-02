package com.trinet.ambis.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.BenefitsBundleDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDataDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.Bundle;
import com.trinet.ambis.persistence.model.BundlePlans;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.embeddable.BundlePlansId;
import com.trinet.ambis.rest.controllers.dto.BundlePlanResponse;
import com.trinet.ambis.rest.controllers.dto.BundlePlansDto;
import com.trinet.ambis.rest.controllers.dto.BundleSelectionDetailsRequest;
import com.trinet.ambis.service.impl.BenefitsBundleServiceImpl;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.BenefitsBundleDto;
import com.trinet.ambis.service.model.BundleSelectionDetailsDto;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.bundle.BundleDetail;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
public class BenefitsBundleServiceTest {

    // ─── dependencies ─────────────────────────────────────────────────────────────
    @Mock private PortfolioService   portfolioService;
    @Mock private CompanyService     companyService;
    @Mock private PlanRatesService   planRatesService;
    @Mock private BenefitsBundleDao  benefitsBundleDao;
    @Mock private BenefitPlanDao     benefitPlanDao;
    @Mock private RealmDataDao       realmDataDao;
    @Mock private CompanyDataDao     companyDataDao;
    private Company                  company;

    private BenefitsBundleServiceImpl service;
    private MockedStatic<BenefitCategoriesHelper> mockBenefitCategoriesHelper;
    private MockedStatic<CommonServiceHelper>     mockCommonServiceHelper;
    private MockedStatic<AppRulesAndConfigsUtils> mockAppRulesAndConfigsUtils;

    // ─── constants ────────────────────────────────────────────────────────────────
    private static final String COMPANY_CODE  = "D07";
    private static final String EXCHANGE_ID   = "AMB";
    private static final String VALID_QUARTER = "Q1";
    private static final String VALID_DATE    = "2025-01-01";
    private static final LocalDate VALID_LOCAL_DATE = LocalDate.parse(VALID_DATE,
            DateTimeFormatter.ofPattern(BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD));
    private static final long   BUNDLE_ID     = 1L;

    private static final String MEDICAL_CODE          = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
    private static final String DENTAL_CODE           = BSSApplicationConstants.DENTAL_PLAN_TYPE;
    private static final String VISION_CODE           = BSSApplicationConstants.VISION_PLAN_TYPE;
    private static final String VOLUNTARY_DENTAL_CODE = BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE;
    private static final String VOLUNTARY_VISION_CODE = BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE;

    // valid coverage codes: 1=EE, 2=ES, C=EF, 4=FAM
    private static final String CVG_EE  = "1";
    private static final String CVG_ES  = "2";
    private static final String CVG_EC  = "C";
    private static final String CVG_FAM = "4";

    @Before
    public void setUp() {
        service = new BenefitsBundleServiceImpl(
                portfolioService, companyService, planRatesService, benefitsBundleDao, benefitPlanDao, realmDataDao, companyDataDao);
        company = new Company();
        Realm realm = new Realm();
        realm.setBenExchange(EXCHANGE_ID);
        company.setRealm(realm);
        mockBenefitCategoriesHelper = Mockito.mockStatic(BenefitCategoriesHelper.class);
        mockCommonServiceHelper     = Mockito.mockStatic(CommonServiceHelper.class);
        mockAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
        mockAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(false);
    }

    @After
    public void tearDown() {
        mockBenefitCategoriesHelper.close();
        mockCommonServiceHelper.close();
        mockAppRulesAndConfigsUtils.close();
    }

    // ─── getBundleDetails ─────────────────────────────────────────────────────────

    @Test
    public void getBundleDetails_validInput_returnsDtoList() {
        when(benefitsBundleDao.findAllByOeQuarterAndEffectiveDate(VALID_QUARTER, VALID_LOCAL_DATE))
                .thenReturn(mockBundles());

        List<BenefitsBundleDto> result = service.getBundleDetails(VALID_QUARTER, VALID_DATE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Q1", result.get(0).getQuarter());
        assertEquals("Bundle 1", result.get(0).getBundles().get(0).getName());
        verify(benefitsBundleDao).findAllByOeQuarterAndEffectiveDate(VALID_QUARTER, VALID_LOCAL_DATE);
    }

    @Test
    public void getBundleDetails_noRecords_returnsEmptyList() {
        when(benefitsBundleDao.findAllByOeQuarterAndEffectiveDate(VALID_QUARTER, VALID_LOCAL_DATE))
                .thenReturn(Collections.emptyList());

        List<BenefitsBundleDto> result = service.getBundleDetails(VALID_QUARTER, VALID_DATE);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getBundleDetails_invalidDateFormat_throwsIllegalArgument() {
        service.getBundleDetails(VALID_QUARTER, "01-01-2025");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getBundleDetails_emptyDate_throwsIllegalArgument() {
        service.getBundleDetails(VALID_QUARTER, "");
    }

    // ─── getBundleDetails V2 ─────────────────────────────────────────────────────────

    @Test
    public void getBundleDetails_v2Enabled_usesV2Query() {
        mockAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(true);
        when(benefitsBundleDao.findAllByOeQuarterAndEffectiveDateV2(VALID_QUARTER, VALID_LOCAL_DATE))
                .thenReturn(mockBundles());

        List<BenefitsBundleDto> result = service.getBundleDetails(VALID_QUARTER, VALID_DATE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Q1", result.get(0).getQuarter());
        assertEquals("Bundle 1", result.get(0).getBundles().get(0).getName());
        verify(benefitsBundleDao).findAllByOeQuarterAndEffectiveDateV2(VALID_QUARTER, VALID_LOCAL_DATE);
        verify(benefitsBundleDao, times(0)).findAllByOeQuarterAndEffectiveDate(VALID_QUARTER, VALID_LOCAL_DATE);
    }

    @Test
    public void getBundleDetails_v2Enabled_noRecords_returnsEmptyList() {
        mockAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(true);
        when(benefitsBundleDao.findAllByOeQuarterAndEffectiveDateV2(VALID_QUARTER, VALID_LOCAL_DATE))
                .thenReturn(Collections.emptyList());

        List<BenefitsBundleDto> result = service.getBundleDetails(VALID_QUARTER, VALID_DATE);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(benefitsBundleDao).findAllByOeQuarterAndEffectiveDateV2(VALID_QUARTER, VALID_LOCAL_DATE);
    }

    @Test
    public void getBundleDetails_v2Disabled_usesV1Query() {
        mockAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(false);
        when(benefitsBundleDao.findAllByOeQuarterAndEffectiveDate(VALID_QUARTER, VALID_LOCAL_DATE))
                .thenReturn(mockBundles());

        List<BenefitsBundleDto> result = service.getBundleDetails(VALID_QUARTER, VALID_DATE);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(benefitsBundleDao).findAllByOeQuarterAndEffectiveDate(VALID_QUARTER, VALID_LOCAL_DATE);
        verify(benefitsBundleDao, times(0)).findAllByOeQuarterAndEffectiveDateV2(VALID_QUARTER, VALID_LOCAL_DATE);
    }

    // ─── getBundleById ────────────────────────────────────────────────────────────

    @Test
    public void getBundleById_validId_returnsBundle() {
        when(benefitsBundleDao.findById(BUNDLE_ID)).thenReturn(mockBundle());

        Bundle result = service.getBundleById(BUNDLE_ID);

        assertEquals("Bundle 1", result.getName());
        verify(benefitsBundleDao).findById(BUNDLE_ID);
    }

    // ─── getCustomBundleCreatedStatus ─────────────────────────────────────────────

    @Test
    public void getCustomBundleCreatedStatus_bundleExists_returnsYes() {
        when(benefitsBundleDao.findByCompanyCodeAndType(COMPANY_CODE, BSSApplicationConstants.CUSTOM))
                .thenReturn(new Bundle());

        assertEquals(BSSApplicationConstants.YES,
                service.getCustomBundleCreatedStatus(COMPANY_CODE, BSSApplicationConstants.CUSTOM));
    }

    @Test
    public void getCustomBundleCreatedStatus_bundleNotFound_returnsNo() {
        when(benefitsBundleDao.findByCompanyCodeAndType(COMPANY_CODE, BSSApplicationConstants.CUSTOM))
                .thenReturn(null);

        assertEquals(BSSApplicationConstants.NO,
                service.getCustomBundleCreatedStatus(COMPANY_CODE, BSSApplicationConstants.CUSTOM));
    }

    // ─── getBundleByCompanyCode ───────────────────────────────────────────────────

    @Test
    public void getBundleByCompanyCode_returnsBundle() {
        Bundle expected = mockBundle();
        when(benefitsBundleDao.findByCompanyCode(COMPANY_CODE)).thenReturn(expected);

        assertEquals(expected, service.getBundleByCompanyCode(COMPANY_CODE));
        verify(benefitsBundleDao).findByCompanyCode(COMPANY_CODE);
    }

    // ─── save ─────────────────────────────────────────────────────────────────────

    @Test
    public void save_delegatesToDao() {
        Bundle bundle = mockBundle();
        service.save(bundle);
        verify(benefitsBundleDao).save(bundle);
    }

    // ─── getBundleSelectionDetails ─────────────────────────────────────────────────

    @Test
    public void getBundleSelectionDetails_delegatesToCompanyDataDao() {
        String companyCode = "G48";
        List<BundleSelectionDetailsRequest.ExchangeDates> exchangeDatePairs = List.of(
                BundleSelectionDetailsRequest.ExchangeDates.builder()
                        .exchange("TriNet III").effectiveDate("2026-01-01").build()
        );

        BundleSelectionDetailsRequest request = BundleSelectionDetailsRequest.builder()
                .companyCode(companyCode)
                .exchangeDatePairs(exchangeDatePairs)
                .build();

        List<BundleSelectionDetailsDto> expectedList = List.of(
                BundleSelectionDetailsDto.builder()
                        .companyId(100001L)
                        .exchangeId("TNIII")
                        .build()
        );

        when(companyDataDao.getBundleSelectionDetails(companyCode, exchangeDatePairs))
                .thenReturn(expectedList);

        List<BundleSelectionDetailsDto> result = service.getBundleSelectionDetails(request);

        assertEquals(1, result.size());
        assertEquals(100001L, result.get(0).getCompanyId().longValue());
        assertEquals("TNIII", result.get(0).getExchangeId());
        verify(companyDataDao).getBundleSelectionDetails(companyCode, exchangeDatePairs);
    }

    @Test
    public void getBundleSelectionDetails_returnsEmptyList_whenDaoReturnsEmpty() {
        String companyCode = "NONEXISTENT";
        List<BundleSelectionDetailsRequest.ExchangeDates> exchangeDatePairs = List.of(
                BundleSelectionDetailsRequest.ExchangeDates.builder()
                        .exchange("TriNet I").effectiveDate("2026-03-01").build()
        );

        BundleSelectionDetailsRequest request = BundleSelectionDetailsRequest.builder()
                .companyCode(companyCode)
                .exchangeDatePairs(exchangeDatePairs)
                .build();

        when(companyDataDao.getBundleSelectionDetails(companyCode, exchangeDatePairs))
                .thenReturn(Collections.emptyList());

        List<BundleSelectionDetailsDto> result = service.getBundleSelectionDetails(request);

        assertTrue(result.isEmpty());
        verify(companyDataDao).getBundleSelectionDetails(companyCode, exchangeDatePairs);
    }

    @Test
    public void getBundleSelectionDetails_returnsEmptyList_whenDaoReturnsNull() {
        String companyCode = "NONEXISTENT";
        List<BundleSelectionDetailsRequest.ExchangeDates> exchangeDatePairs = List.of(
                BundleSelectionDetailsRequest.ExchangeDates.builder()
                        .exchange("TriNet I").effectiveDate("2026-03-01").build()
        );

        BundleSelectionDetailsRequest request = BundleSelectionDetailsRequest.builder()
                .companyCode(companyCode)
                .exchangeDatePairs(exchangeDatePairs)
                .build();

        when(companyDataDao.getBundleSelectionDetails(companyCode, exchangeDatePairs))
                .thenReturn(null);

        List<BundleSelectionDetailsDto> result = service.getBundleSelectionDetails(request);

        assertTrue(result.isEmpty());
        verify(companyDataDao).getBundleSelectionDetails(companyCode, exchangeDatePairs);
    }

    // ─── getBundleAndExchangePlans – empty rows ───────────────────────────────────

    @Test
    public void getBundleAndExchangePlans_emptyRows_returnsEmptyList() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        assertTrue(service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null).isEmpty());
    }

    // ─── getBundleAndExchangePlans – voluntary plans are included ─────────────────

    @Test
    public void getBundleAndExchangePlans_voluntaryRows_areIncludedAsNonMedicalGroups() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(List.of(
                        row("VD-1", VOLUNTARY_DENTAL_CODE, 1, -1),
                        row("VV-1", VOLUNTARY_VISION_CODE, 1, -1)
                ));

        List<BundlePlansDto> result = service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);

        assertEquals(2, result.size());
        Set<String> benefitTypes = result.stream().map(BundlePlansDto::getBenefitType).collect(Collectors.toSet());
        assertTrue(benefitTypes.containsAll(Set.of(VOLUNTARY_DENTAL_CODE, VOLUNTARY_VISION_CODE)));
        result.forEach(dto -> {
            assertNull(dto.getBundleDetails().get(0).getPrimaryCarrierId());
            assertEquals(-1, dto.getBundleDetails().get(0).getBenefitBundleId().intValue());
        });
    }

    // ─── getBundleAndExchangePlans – invalid rows throw BSSBadDataException ───────

    @Test(expected = BSSBadDataException.class)
    public void getBundleAndExchangePlans_nullPlanId_throwsBSSBadDataException() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row(null, MEDICAL_CODE, 1, 10)));

        service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);
    }

    @Test(expected = BSSBadDataException.class)
    public void getBundleAndExchangePlans_nullBenefitType_throwsBSSBadDataException() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row("P1", null, 1, 10)));

        service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);
    }

    @Test(expected = BSSBadDataException.class)
    public void getBundleAndExchangePlans_nullPortfolioId_throwsBSSBadDataException() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row("P1", MEDICAL_CODE, null, 10)));

        service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);
    }

    // ─── getBundleAndExchangePlans – medical plan grouping ───────────────────────

    @Test
    public void getBundleAndExchangePlans_singleMedicalPlan_groupedCorrectly() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row("PLAN-A", MEDICAL_CODE, 1, 10)));

        List<BundlePlansDto> result = service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);

        assertEquals(1, result.size());
        BundlePlansDto dto = result.get(0);
        assertEquals(MEDICAL_CODE, dto.getBenefitType());
        assertEquals(1, dto.getBundleDetails().size());
        BundleDetail detail = dto.getBundleDetails().get(0);
        assertEquals(Integer.valueOf(1),  detail.getPrimaryCarrierId());
        assertEquals(Integer.valueOf(10), detail.getBenefitBundleId());
        assertPlanIds(List.of("PLAN-A"), detail);
    }

    @Test
    public void getBundleAndExchangePlans_multipleMedicalPlans_sameBundleAndPrimary_groupedTogether() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(List.of(
                        row("PLAN-A", MEDICAL_CODE, 1, 10),
                        row("PLAN-B", MEDICAL_CODE, 1, 10)
                ));

        List<BundlePlansDto> result = service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getBundleDetails().size());
        assertPlanIdsIgnoreOrder(List.of("PLAN-A", "PLAN-B"), result.get(0).getBundleDetails().get(0));
    }

    @Test
    public void getBundleAndExchangePlans_differentBundleIds_produceSeparateGroups() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(List.of(
                        row("PLAN-A", MEDICAL_CODE, 1, 10),
                        row("PLAN-B", MEDICAL_CODE, 1, 20)
                ));

        List<BundlePlansDto> result = service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getBundleDetails().size());
    }

    @Test
    public void getBundleAndExchangePlans_multiplePrimaryCarriers_sameRowAppearsUnderEachPrimary() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1), primaryCarrier(2)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row("PLAN-A", MEDICAL_CODE, 1, 10)));

        List<BundlePlansDto> result = service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);

        assertEquals(1, result.size());
        Set<Integer> primaryIds = new HashSet<>();
        result.get(0).getBundleDetails().forEach(d -> primaryIds.add(d.getPrimaryCarrierId()));
        assertTrue(primaryIds.contains(1));
    }

    // ─── getBundleAndExchangePlans – child-to-primary carrier mapping ─────────────

    @Test
    public void getBundleAndExchangePlans_childPortfolio_mapsToParentPrimary() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1), childCarrier(2, "1")));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row("PLAN-A", MEDICAL_CODE, 2, 10)));

        List<BundlePlansDto> result = service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);

        assertEquals(1, result.get(0).getBundleDetails().size());
        assertEquals(Integer.valueOf(1), result.get(0).getBundleDetails().get(0).getPrimaryCarrierId());
    }

    @Test
    public void getBundleAndExchangePlans_childWithNullParent_mapsToAllPrimaries() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1), primaryCarrier(2), childCarrier(3)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row("PLAN-A", MEDICAL_CODE, 3, 10)));

        List<BundlePlansDto> result = service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);

        assertEquals(2, result.get(0).getBundleDetails().size());
        Set<Integer> resolvedPrimaries = new HashSet<>();
        result.get(0).getBundleDetails().forEach(d -> resolvedPrimaries.add(d.getPrimaryCarrierId()));
        assertTrue(resolvedPrimaries.containsAll(Set.of(1, 2)));
    }

    @Test
    public void getBundleAndExchangePlans_unmappedPortfolioId_fallsBackToItself() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row("PLAN-A", MEDICAL_CODE, 99, 10)));

        List<BundlePlansDto> result = service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);

        assertEquals(1, result.get(0).getBundleDetails().size());
        assertEquals(Integer.valueOf(99), result.get(0).getBundleDetails().get(0).getPrimaryCarrierId());
    }

    // ─── getBundleAndExchangePlans – non-medical plan grouping ────────────────────

    @Test
    public void getBundleAndExchangePlans_dentalPlan_hasNullPrimaryCarrierId() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row("D1", DENTAL_CODE, 1, 10)));

        List<BundlePlansDto> result = service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);

        assertEquals(1, result.size());
        assertEquals(DENTAL_CODE, result.get(0).getBenefitType());
        assertNull(result.get(0).getBundleDetails().get(0).getPrimaryCarrierId());
    }

    @Test
    public void getBundleAndExchangePlans_visionPlan_hasNullPrimaryCarrierId() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row("V1", VISION_CODE, 1, 10)));

        List<BundlePlansDto> result = service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);

        assertEquals(1, result.size());
        assertEquals(VISION_CODE, result.get(0).getBenefitType());
        assertNull(result.get(0).getBundleDetails().get(0).getPrimaryCarrierId());
    }

    @Test
    public void getBundleAndExchangePlans_mixedPlanTypes_allGroupsProducedIncludingVoluntary() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(List.of(
                        row("M1",  MEDICAL_CODE,          1, 10),
                        row("D1",  DENTAL_CODE,            1, null),
                        row("V1",  VISION_CODE,            1, null),
                        row("VD1", VOLUNTARY_DENTAL_CODE,  1, null),
                        row("VV1", VOLUNTARY_VISION_CODE,  1, null)
                ));

        List<BundlePlansDto> result = service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);

        assertEquals(5, result.size());
        Set<String> benefitTypes = new HashSet<>();
        result.forEach(dto -> benefitTypes.add(dto.getBenefitType()));
        assertTrue(benefitTypes.containsAll(Set.of(MEDICAL_CODE, DENTAL_CODE, VISION_CODE,
                VOLUNTARY_DENTAL_CODE, VOLUNTARY_VISION_CODE)));
    }

    // ─── getBundleAndExchangePlans – BigDecimal row values (Oracle JDBC) ──────────

    @Test
    public void getBundleAndExchangePlans_bigDecimalPortfolioAndBundleId_handledCorrectly() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(rowBigDecimal("PLAN-A", MEDICAL_CODE,
                        new BigDecimal("1"), new BigDecimal("10"))));

        List<BundlePlansDto> result = service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);

        assertEquals(1, result.size());
        BundleDetail detail = result.get(0).getBundleDetails().get(0);
        assertEquals(Integer.valueOf(1),  detail.getPrimaryCarrierId());
        assertEquals(Integer.valueOf(10), detail.getBenefitBundleId());
        assertPlanIds(List.of("PLAN-A"), detail);
    }

    // ─── getBundleAndExchangePlans – plan rates enrichment ────────────────────────

    @Test
    public void getBundleAndExchangePlans_planRatesPresent_enrichesBundlePlanDetails() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row("PLAN-A", MEDICAL_CODE, 1, 10)));
        when(planRatesService.getBenefitPlanRatesBy(company, false))
                .thenReturn(Map.of("PLAN-A", List.of(
                        planRate("PLAN-A", CVG_EE,  new BigDecimal("100.00")),
                        planRate("PLAN-A", CVG_ES,  new BigDecimal("200.00")),
                        planRate("PLAN-A", CVG_EC,  new BigDecimal("250.00")),
                        planRate("PLAN-A", CVG_FAM, new BigDecimal("300.00"))
                )));

        List<BundlePlansDto> result = service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);

        BundleDetail detail = result.get(0).getBundleDetails().get(0);
        List<BundleDetail.BundlePlanDetail> planDetails = detail.getBundlePlanDetails();
        assertEquals(1, planDetails.size());
        assertEquals("PLAN-A", planDetails.get(0).getPlanId());

        List<BundleDetail.PlanCostRequest> costs = planDetails.get(0).getCvgLevelCost();
        assertEquals(4, costs.size());

        Map<String, BigDecimal> costByCovCode = costs.stream()
                .collect(Collectors.toMap(BundleDetail.PlanCostRequest::getCovrgCd,
                                          BundleDetail.PlanCostRequest::getTotalCost));
        assertEquals(new BigDecimal("100.00"), costByCovCode.get(CVG_EE));
        assertEquals(new BigDecimal("200.00"), costByCovCode.get(CVG_ES));
        assertEquals(new BigDecimal("250.00"), costByCovCode.get(CVG_EC));
        assertEquals(new BigDecimal("300.00"), costByCovCode.get(CVG_FAM));
    }

    @Test
    public void getBundleAndExchangePlans_noRatesForPlan_emptyBundlePlanCostList() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row("PLAN-A", MEDICAL_CODE, 1, 10)));

        List<BundlePlansDto> result = service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);

        BundleDetail.BundlePlanDetail planDetail = result.get(0).getBundleDetails().get(0).getBundlePlanDetails().get(0);
        assertEquals("PLAN-A", planDetail.getPlanId());
        assertTrue(planDetail.getCvgLevelCost().isEmpty());
    }

    @Test
    public void getBundleAndExchangePlans_multiplePlansWithRates_eachPlanEnrichedIndependently() {
        setupCompanyAndPortfolios(Set.of(primaryCarrier(1)));
        when(benefitPlanDao.getAllExchangeAndBundlesPlans(any(), any(), any(), any()))
                .thenReturn(List.of(
                        row("PLAN-A", MEDICAL_CODE, 1, 10),
                        row("PLAN-B", MEDICAL_CODE, 1, 10)
                ));
        when(planRatesService.getBenefitPlanRatesBy(company, false))
                .thenReturn(Map.of(
                        "PLAN-A", List.of(planRate("PLAN-A", CVG_EE, new BigDecimal("100.00"))),
                        "PLAN-B", List.of(
                                planRate("PLAN-B", CVG_EE,  new BigDecimal("150.00")),
                                planRate("PLAN-B", CVG_ES,  new BigDecimal("250.00")),
                                planRate("PLAN-B", CVG_EC,  new BigDecimal("275.00")),
                                planRate("PLAN-B", CVG_FAM, new BigDecimal("350.00"))
                        )
                ));

        List<BundlePlansDto> result = service.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);

        List<BundleDetail.BundlePlanDetail> planDetails = result.get(0).getBundleDetails().get(0).getBundlePlanDetails();
        assertEquals(2, planDetails.size());

        Map<String, List<BundleDetail.PlanCostRequest>> costsByPlanId = planDetails.stream()
                .collect(Collectors.toMap(BundleDetail.BundlePlanDetail::getPlanId,
                                          BundleDetail.BundlePlanDetail::getCvgLevelCost));
        assertEquals(1, costsByPlanId.get("PLAN-A").size());
        assertEquals(4, costsByPlanId.get("PLAN-B").size());

        Map<String, BigDecimal> planBCosts = costsByPlanId.get("PLAN-B").stream()
                .collect(Collectors.toMap(BundleDetail.PlanCostRequest::getCovrgCd,
                                          BundleDetail.PlanCostRequest::getTotalCost));
        assertEquals(new BigDecimal("150.00"), planBCosts.get(CVG_EE));
        assertEquals(new BigDecimal("250.00"), planBCosts.get(CVG_ES));
        assertEquals(new BigDecimal("275.00"), planBCosts.get(CVG_EC));
        assertEquals(new BigDecimal("350.00"), planBCosts.get(CVG_FAM));
    }

    // ─── helpers ─────────────────────────────────────────────────────────────────

    private void setupCompanyAndPortfolios(Set<PlanCarrier> medicalCarriers) {
        when(companyService.getCompanyDetails(any(), anyBoolean(), any(), any())).thenReturn(company);
        Map<String, Set<PlanCarrier>> carrierMap = new HashMap<>();
        carrierMap.put(BSSApplicationConstants.MEDICAL, medicalCarriers);
        when(portfolioService.findPrimaryPlanCarriers(company)).thenReturn(carrierMap);
        when(planRatesService.getBenefitPlanRatesBy(company, false)).thenReturn(Collections.emptyMap());
        mockBenefitCategoriesHelper
                .when(() -> BenefitCategoriesHelper.getPlanCarriers(any()))
                .thenReturn(new HashSet<>());
        mockCommonServiceHelper
                .when(() -> CommonServiceHelper.getOutOfRegionPlansToExclude(any(), any(), any()))
                .thenReturn(Collections.emptySet());
    }

    private Object[] row(String planId, String benefitType, Integer portfolioId, Integer bundleId) {
        Object[] row = new Object[11];
        row[0]  = planId;
        row[2]  = benefitType;
        row[5]  = portfolioId;
        row[10] = bundleId;
        return row;
    }

    private Object[] rowBigDecimal(String planId, String benefitType,
                                   BigDecimal portfolioId, BigDecimal bundleId) {
        Object[] row = new Object[11];
        row[0]  = planId;
        row[2]  = benefitType;
        row[5]  = portfolioId;
        row[10] = bundleId;
        return row;
    }

    private PlanCarrier primaryCarrier(int id) {
        PlanCarrier pc = new PlanCarrier();
        pc.setId(id);
        pc.setPrimaryCarrier(true);
        pc.setParentId(null);
        return pc;
    }

    private PlanCarrier childCarrier(int id, String... parentIds) {
        PlanCarrier pc = new PlanCarrier();
        pc.setId(id);
        pc.setPrimaryCarrier(false);
        pc.setParentId(parentIds != null && parentIds.length > 0
                ? Arrays.asList(parentIds) : null);
        return pc;
    }

    private BenefitPlanRate planRate(String planId, String coverageCode, BigDecimal employerCost) {
        BenefitPlanRate rate = new BenefitPlanRate();
        rate.setBenefitPlan(planId);
        rate.setCoverageCode(coverageCode);
        rate.setEmployerCost(employerCost);
        return rate;
    }

    /** Asserts plan IDs in bundlePlanDetails match expected list (order-sensitive). */
    private void assertPlanIds(List<String> expectedPlanIds, BundleDetail detail) {
        List<String> actualPlanIds = detail.getBundlePlanDetails().stream()
                .map(BundleDetail.BundlePlanDetail::getPlanId)
                .collect(Collectors.toList());
        assertEquals(expectedPlanIds, actualPlanIds);
    }

    /** Asserts plan IDs in bundlePlanDetails match expected set (order-insensitive). */
    private void assertPlanIdsIgnoreOrder(List<String> expectedPlanIds, BundleDetail detail) {
        Set<String> actualPlanIds = detail.getBundlePlanDetails().stream()
                .map(BundleDetail.BundlePlanDetail::getPlanId)
                .collect(Collectors.toSet());
        assertEquals(new HashSet<>(expectedPlanIds), actualPlanIds);
    }

    private Bundle mockBundle() {
        Bundle bundle = new Bundle();
        bundle.setId(BUNDLE_ID);
        bundle.setName("Bundle 1");
        return bundle;
    }

    private List<Bundle> mockBundles() {
        Bundle bundle = mockBundle();
        bundle.setEffectiveDate(VALID_LOCAL_DATE);
        bundle.setEndDate(VALID_LOCAL_DATE.plusDays(30));

        BundlePlansId id = new BundlePlansId();
        id.setOeQuarter("Q1");
        id.setEffectiveDate(VALID_LOCAL_DATE);
        id.setPortfolioId(100L);
        id.setBaseBenefitPlan("Base1");

        BundlePlans plan = new BundlePlans();
        plan.setId(id);
        plan.setEndDate(VALID_LOCAL_DATE.plusDays(30));
        plan.setPlanType("Medical");
        plan.setBundle(bundle);

        bundle.setBundlePlans(List.of(plan));
        return List.of(bundle);
    }
    /**
     * Given: DAO returns two rows for the same bundle with different regional plan IDs
     * When: getBundlesByEffectiveDateAndQuarter is called
     * Then: A single BundleDto is returned with both plan IDs in benefitPlanIds
     */
    @Test
    public void shouldReturnBundlePlanResponse_WhenValidInputsProvided() {
        // given
        LocalDate effectiveDate = LocalDate.of(2025, 7, 1);
        String quarter = "Q3";

        Object[] row1 = new Object[]{1L, "Bundle A", "Static", "REGIONAL-PLAN-001"};
        Object[] row2 = new Object[]{1L, "Bundle A", "Static" , "REGIONAL-PLAN-002"};

        when(benefitsBundleDao.findByEffectiveDateAndQuarter(effectiveDate, quarter))
                .thenReturn(List.of(row1, row2));

        // when
        BundlePlanResponse response = service.getBundlesByEffectiveDateAndQuarter(effectiveDate, quarter);

        // then
        assertNotNull(response);
        assertEquals(1, response.getBundles().size());
        BundlePlanResponse.BundleDto dto = response.getBundles().get(0);
        assertEquals(Long.valueOf(1L), dto.getId());
        assertEquals("Bundle A", dto.getName());
        assertEquals("Static", dto.getType());
        assertEquals(2, dto.getBenefitPlanIds().size());
        assertTrue(dto.getBenefitPlanIds().contains("REGIONAL-PLAN-001"));
        assertTrue(dto.getBenefitPlanIds().contains("REGIONAL-PLAN-002"));
        verify(benefitsBundleDao, times(1)).findByEffectiveDateAndQuarter(effectiveDate, quarter);
    }

    /**
     * Given: DAO returns rows for two different bundles
     * When: getBundlesByEffectiveDateAndQuarter is called
     * Then: Two BundleDto objects are returned, each with their own plan IDs
     */
    @Test
    public void shouldReturnMultipleBundles_WhenRowsSpanDifferentBundleIds() {
        // given
        LocalDate effectiveDate = LocalDate.of(2025, 7, 1);
        String quarter = "Q3";

        Object[] row1 = new Object[]{1L, "Bundle A", "Static" , "REGIONAL-PLAN-001"};
        Object[] row2 = new Object[]{2L, "Bundle B", null ,  "REGIONAL-PLAN-003"};

        when(benefitsBundleDao.findByEffectiveDateAndQuarter(effectiveDate, quarter))
                .thenReturn(List.of(row1, row2));

        // when
        BundlePlanResponse response = service.getBundlesByEffectiveDateAndQuarter(effectiveDate, quarter);

        // then
        assertNotNull(response);
        assertEquals(2, response.getBundles().size());

        BundlePlanResponse.BundleDto dtoA = response.getBundles().get(0);
        assertEquals("Bundle A", dtoA.getName());
        assertEquals("Static", dtoA.getType());
        assertEquals(List.of("REGIONAL-PLAN-001"), dtoA.getBenefitPlanIds());

        BundlePlanResponse.BundleDto dtoB = response.getBundles().get(1);
        assertEquals("Bundle B", dtoB.getName());
        assertNull( dtoB.getType());
        assertEquals(List.of("REGIONAL-PLAN-003"), dtoB.getBenefitPlanIds());
    }

    /**
     * Given: DAO returns an empty list (no matching records)
     * When: getBundlesByEffectiveDateAndQuarter is called
     * Then: A BundlePlanResponse with an empty bundles list is returned
     */
    @Test
    public void shouldReturnEmptyBundleList_WhenNoRowsFound() {
        // given
        LocalDate effectiveDate = LocalDate.of(2025, 7, 1);
        String quarter = "Q3";

        when(benefitsBundleDao.findByEffectiveDateAndQuarter(effectiveDate, quarter))
                .thenReturn(Collections.emptyList());

        // when
        BundlePlanResponse response = service.getBundlesByEffectiveDateAndQuarter(effectiveDate, quarter);

        // then
        assertNotNull(response);
        assertNotNull(response.getBundles());
        assertTrue(response.getBundles().isEmpty());
        verify(benefitsBundleDao, times(1)).findByEffectiveDateAndQuarter(effectiveDate, quarter);
    }

    /**
     * Given: DAO returns a single row with one bundle and one plan ID
     * When: getBundlesByEffectiveDateAndQuarter is called
     * Then: A BundlePlanResponse with one BundleDto containing one plan ID is returned
     */
    @Test
    public void shouldReturnSingleBundle_WhenSingleRowReturned() {
        // given
        LocalDate effectiveDate = LocalDate.of(2025, 7, 1);
        String quarter = "Q3";

        Object[] row = new Object[]{10L, "Bundle Alpha", "Static" ,"REGIONAL-PLAN-010"};

        when(benefitsBundleDao.findByEffectiveDateAndQuarter(effectiveDate, quarter))
                .thenReturn(List.<Object[]>of(row));

        // when
        BundlePlanResponse response = service.getBundlesByEffectiveDateAndQuarter(effectiveDate, quarter);

        // then
        assertNotNull(response);
        assertEquals(1, response.getBundles().size());
        BundlePlanResponse.BundleDto dto = response.getBundles().get(0);
        assertEquals(Long.valueOf(10L), dto.getId());
        assertEquals("Bundle Alpha", dto.getName());
        assertEquals("Static", dto.getType());
        assertEquals(1, dto.getBenefitPlanIds().size());
        assertEquals("REGIONAL-PLAN-010", dto.getBenefitPlanIds().get(0));
    }

    @Test
    public void shouldSkipNullRegionalPlanId_WhenUsingV1Dao() {
        LocalDate effectiveDate = LocalDate.of(2025, 7, 1);
        String quarter = "Q3";

        Object[] rowWithNullPlan = new Object[]{10L, "Bundle Alpha", "Static", null};
        Object[] rowWithPlan = new Object[]{10L, "Bundle Alpha", "Static", "REGIONAL-PLAN-010"};

        when(benefitsBundleDao.findByEffectiveDateAndQuarter(effectiveDate, quarter))
                .thenReturn(List.of(rowWithNullPlan, rowWithPlan));

        BundlePlanResponse response = service.getBundlesByEffectiveDateAndQuarter(effectiveDate, quarter);

        assertNotNull(response);
        assertEquals(1, response.getBundles().size());
        BundlePlanResponse.BundleDto dto = response.getBundles().get(0);
        assertEquals(1, dto.getBenefitPlanIds().size());
        assertEquals("REGIONAL-PLAN-010", dto.getBenefitPlanIds().get(0));
    }

    // ─── getBundlesByEffectiveDateAndQuarter – isBundleV2Enabled tests ─────────────

    /**
     * Given: isBundleV2Enabled returns true and V2 DAO returns two rows for the same bundle
     * When: getBundlesByEffectiveDateAndQuarter is called
     * Then: V2 DAO is used and a single BundleDto is returned with both plan IDs
     */
    @Test
    public void shouldUseV2Dao_WhenBundleV2Enabled() {
        mockAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(true);

            LocalDate effectiveDate = LocalDate.of(2025, 7, 1);
            String quarter = "Q3";

            Object[] row1 = new Object[]{1L, "Bundle A", "Static", "REGIONAL-PLAN-001"};
            Object[] row2 = new Object[]{1L, "Bundle A", "Static", "REGIONAL-PLAN-002"};

            when(benefitsBundleDao.findByEffectiveDateAndQuarterV2(effectiveDate, quarter))
                    .thenReturn(List.of(row1, row2));

            BundlePlanResponse response = service.getBundlesByEffectiveDateAndQuarter(effectiveDate, quarter);

            assertNotNull(response);
            assertEquals(1, response.getBundles().size());
            BundlePlanResponse.BundleDto dto = response.getBundles().get(0);
            assertEquals(Long.valueOf(1L), dto.getId());
            assertEquals("Bundle A", dto.getName());
            assertEquals(2, dto.getBenefitPlanIds().size());
            assertTrue(dto.getBenefitPlanIds().contains("REGIONAL-PLAN-001"));
            assertTrue(dto.getBenefitPlanIds().contains("REGIONAL-PLAN-002"));
            verify(benefitsBundleDao, times(1)).findByEffectiveDateAndQuarterV2(effectiveDate, quarter);
            verify(benefitsBundleDao, times(0)).findByEffectiveDateAndQuarter(effectiveDate, quarter);

    }

    /**
     * Given: isBundleV2Enabled returns true and V2 DAO returns rows for multiple bundles
     * When: getBundlesByEffectiveDateAndQuarter is called
     * Then: Multiple BundleDto objects are returned using V2 DAO
     */
    @Test
    public void shouldReturnMultipleBundles_WhenBundleV2EnabledAndRowsSpanDifferentBundleIds() {
        mockAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(true);

            LocalDate effectiveDate = LocalDate.of(2025, 7, 1);
            String quarter = "Q3";

            Object[] row1 = new Object[]{1L, "Bundle A", "Static", "REGIONAL-PLAN-001"};
            Object[] row2 = new Object[]{2L, "Bundle B", null, "REGIONAL-PLAN-003"};

            when(benefitsBundleDao.findByEffectiveDateAndQuarterV2(effectiveDate, quarter))
                    .thenReturn(List.of(row1, row2));

            BundlePlanResponse response = service.getBundlesByEffectiveDateAndQuarter(effectiveDate, quarter);

            assertNotNull(response);
            assertEquals(2, response.getBundles().size());
            assertEquals("Bundle A", response.getBundles().get(0).getName());
            assertEquals("Bundle B", response.getBundles().get(1).getName());
            verify(benefitsBundleDao, times(1)).findByEffectiveDateAndQuarterV2(effectiveDate, quarter);
            verify(benefitsBundleDao, times(0)).findByEffectiveDateAndQuarter(effectiveDate, quarter);

    }

    /**
     * Given: isBundleV2Enabled returns true and V2 DAO returns empty list
     * When: getBundlesByEffectiveDateAndQuarter is called
     * Then: Empty bundles list is returned using V2 DAO
     */
    @Test
    public void shouldReturnEmptyBundleList_WhenBundleV2EnabledAndNoRowsFound() {
        mockAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(true);
            LocalDate effectiveDate = LocalDate.of(2025, 7, 1);
            String quarter = "Q3";

            when(benefitsBundleDao.findByEffectiveDateAndQuarterV2(effectiveDate, quarter))
                    .thenReturn(Collections.emptyList());

            BundlePlanResponse response = service.getBundlesByEffectiveDateAndQuarter(effectiveDate, quarter);

            assertNotNull(response);
            assertNotNull(response.getBundles());
            assertTrue(response.getBundles().isEmpty());
            verify(benefitsBundleDao, times(1)).findByEffectiveDateAndQuarterV2(effectiveDate, quarter);
            verify(benefitsBundleDao, times(0)).findByEffectiveDateAndQuarter(effectiveDate, quarter);

    }

    /**
     * Given: isBundleV2Enabled returns true and V2 DAO returns a single row
     * When: getBundlesByEffectiveDateAndQuarter is called
     * Then: A single BundleDto is returned using V2 DAO
     */
    @Test
    public void shouldReturnSingleBundle_WhenBundleV2EnabledAndSingleRowReturned() {
        mockAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(true);

        LocalDate effectiveDate = LocalDate.of(2025, 7, 1);
            String quarter = "Q3";

            Object[] row = new Object[]{10L, "Bundle Alpha", "Static", "REGIONAL-PLAN-010"};

            when(benefitsBundleDao.findByEffectiveDateAndQuarterV2(effectiveDate, quarter))
                    .thenReturn(List.<Object[]>of(row));

            BundlePlanResponse response = service.getBundlesByEffectiveDateAndQuarter(effectiveDate, quarter);

            assertNotNull(response);
            assertEquals(1, response.getBundles().size());
            BundlePlanResponse.BundleDto dto = response.getBundles().get(0);
            assertEquals(Long.valueOf(10L), dto.getId());
            assertEquals("Bundle Alpha", dto.getName());
            assertEquals("Static", dto.getType());
            assertEquals(1, dto.getBenefitPlanIds().size());
            assertEquals("REGIONAL-PLAN-010", dto.getBenefitPlanIds().get(0));
            verify(benefitsBundleDao, times(1)).findByEffectiveDateAndQuarterV2(effectiveDate, quarter);
            verify(benefitsBundleDao, times(0)).findByEffectiveDateAndQuarter(effectiveDate, quarter);

    }

    @Test
    public void shouldSkipNullRegionalPlanId_WhenUsingV2Dao() {
        mockAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(true);

        LocalDate effectiveDate = LocalDate.of(2025, 7, 1);
        String quarter = "Q3";

        Object[] rowWithNullPlan = new Object[]{11L, "Bundle Beta", "Static", null};
        Object[] rowWithPlan = new Object[]{11L, "Bundle Beta", "Static", "REGIONAL-PLAN-011"};

        when(benefitsBundleDao.findByEffectiveDateAndQuarterV2(effectiveDate, quarter))
                .thenReturn(List.of(rowWithNullPlan, rowWithPlan));

        BundlePlanResponse response = service.getBundlesByEffectiveDateAndQuarter(effectiveDate, quarter);

        assertNotNull(response);
        assertEquals(1, response.getBundles().size());
        BundlePlanResponse.BundleDto dto = response.getBundles().get(0);
        assertEquals(1, dto.getBenefitPlanIds().size());
        assertEquals("REGIONAL-PLAN-011", dto.getBenefitPlanIds().get(0));
        verify(benefitsBundleDao, times(1)).findByEffectiveDateAndQuarterV2(effectiveDate, quarter);
        verify(benefitsBundleDao, times(0)).findByEffectiveDateAndQuarter(effectiveDate, quarter);
    }

    /**
     * Given: isBundleV2Enabled returns false
     * When: getBundlesByEffectiveDateAndQuarter is called
     * Then: Non-V2 DAO is used
     */
    @Test
    public void shouldUseNonV2Dao_WhenBundleV2Disabled() {
        mockAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(false);

            LocalDate effectiveDate = LocalDate.of(2025, 7, 1);
            String quarter = "Q3";

            Object[] row = new Object[]{1L, "Bundle A", "Static", "REGIONAL-PLAN-001"};

            when(benefitsBundleDao.findByEffectiveDateAndQuarter(effectiveDate, quarter))
                    .thenReturn(List.<Object[]>of(row));

            BundlePlanResponse response = service.getBundlesByEffectiveDateAndQuarter(effectiveDate, quarter);

            assertNotNull(response);
            assertEquals(1, response.getBundles().size());
            verify(benefitsBundleDao, times(1)).findByEffectiveDateAndQuarter(effectiveDate, quarter);
            verify(benefitsBundleDao, times(0)).findByEffectiveDateAndQuarterV2(effectiveDate, quarter);

    }
}

