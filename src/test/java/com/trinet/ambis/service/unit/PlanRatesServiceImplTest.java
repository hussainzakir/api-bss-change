package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.PlanMappingDao;
import com.trinet.ambis.persistence.dao.hrp.PlanRatesDataDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.AdditionalBenefitPlanService;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.HeadCountService;
import com.trinet.ambis.service.PortfolioService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.impl.PlanRatesServiceImpl;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.AdditionalPlanRate;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.FlexRateResponse;
import com.trinet.ambis.service.model.HealthPlanRatesExportPlan;
import com.trinet.ambis.service.model.MappedHeadCount;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.service.model.PlanRate;
import com.trinet.ambis.service.model.PlanRatesExportData;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.StrategyUtils;
import com.trinet.ambis.util.CommonUtils;


@RunWith(MockitoJUnitRunner.class)
public class PlanRatesServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	PlanRatesServiceImpl planRatesServiceImpl;

	@Mock
	CompanyService companyService;

	@Mock
	PortfolioService portfolioService;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	PlanRatesDataDao planRatesDataDao;

	@Mock
	RealmPlyrPlanService realmPlyrPlanService;

	@Mock
	HeadCountService headCountService;

	@Mock
	BenefitOfferExceptionService benOfferExceptionService;

	@Mock
	AdditionalBenefitPlanService additionalBenefitPlanService;
	
	@Mock
	PlanMappingDao planMappingDao;

	@Mock
	com.trinet.ambis.service.FlexRateService flexRateService;

    private MockedStatic<CommonServiceHelper> commonServiceHelperMockedStatic;
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;
    private MockedStatic<StrategyUtils> strategyUtilsMockedStatic;
    private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils = null;
    private MockedStatic<CommonUtils> commonUtilsMockedStatic = null;

    @Before
    public void setUp() {
        commonServiceHelperMockedStatic = Mockito.mockStatic(CommonServiceHelper.class);
        rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
        strategyUtilsMockedStatic = Mockito.mockStatic(StrategyUtils.class);
        mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
        commonUtilsMockedStatic = Mockito.mockStatic(CommonUtils.class, Mockito.CALLS_REAL_METHODS);
    }

    @After
    public void tearDown() {
        commonServiceHelperMockedStatic.close();
        rulesAndConfigsUtilsMockedStatic.close();
        strategyUtilsMockedStatic.close();
        if (mockStaticAppRulesAndConfigsUtils != null) {
            mockStaticAppRulesAndConfigsUtils.close();
        }
        if (commonUtilsMockedStatic != null) {
            commonUtilsMockedStatic.close();
        }
    }
	@Test
	public void getPlanRatesExcelWorkbookTest() {
		Company company = mockCompany();
		company.setRealmPlanYear(mockFutureRPY(30));
		when( CommonServiceHelper.jsonToObject( Mockito.any( String.class ), Mockito.any() )).thenCallRealMethod();
		PlanRatesExportData planRatesExportData = mockPlanRatesExportData();
		String hideColumns = "";

		Workbook actualResult = planRatesServiceImpl.getPlanRatesExcelWorkbook( company, planRatesExportData, hideColumns );
		assertEquals( 5, actualResult.getNumberOfSheets() );
	}

	@Test
	public void getPlanRatesExportDataTestRenewalMappingDisabled() throws Exception{
		Realm realm = new Realm();
		realm.setBenExchange( BenExchngEnums.TRINET_IV.getBenExchng() );
		Company currentCompany = mockCompany();
		currentCompany.setRealm( realm );
		currentCompany.setRealmPlanYear( mockCurrentRPY() );
		currentCompany.setRealmPlanYearId( currentCompany.getRealmPlanYear().getId() );
		Company futureCompany = mockCompany();
		futureCompany.setRealm( realm );
		futureCompany.setRealmPlanYear( mockFutureRPY(30) );
		futureCompany.setRealmPlanYearId( futureCompany.getRealmPlanYear().getId() );
		futureCompany.setRenewalCompany(true);
		String bandCode = "N";
		when(RulesAndConfigsUtils.isPlanRateMappingEnabled(futureCompany.getRealmPlanYearId())).thenReturn(false);
		when( CommonServiceHelper.jsonToObject( Mockito.any( String.class ), Mockito.any() )).thenCallRealMethod();
		Map<String,Set<PlanCarrier>> planCarrierMap = mockPlanCarrierMap();
		Map<String,StateBenefitPlan> futureMedicalStateBenefitPlans = mockMedicalStatePlans(false);
		Map<String,StateBenefitPlan> currentMedicalStateBenefitPlans = mockMedicalStatePlans(true);
		Map<String,StateBenefitPlan> dentalStateBenefitPlans = mockDentalStatePlans();
		Map<String,StateBenefitPlan> visionStateBenefitPlans = mockVisionStatePlans();
		Map<String,StateBenefitPlan> lifeStateBenefitPlans = mockLifeStatePlans();
		Map<String, List<BenefitPlanRate>> benefitPlanRates = mockPlanRates();
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupCovrgHeadCountMap = mockHeadCountMap();
		Map<String, AdditionalBenefitPlan> disabilityPlans = mockDisabilityPlans();
		Map<String, AdditionalPlanRate> additionalBenefitPlanRates = mockAdditionalPlanRates();
		
		when( CommonServiceHelper.getOutOfRegionPlansToExclude( Mockito.any( Company.class ), Mockito.any(), Mockito.eq( realmDataDao ) ) )
				.thenReturn( new HashSet<String>() );
		when( RulesAndConfigsUtils.isDisabledBundledOn( Mockito.anyLong() ) ).thenReturn( true );

		when( companyService.getCompanyDetails( futureCompany.getCode(), true, futureCompany.getEmplId(), null ))
				.thenReturn( currentCompany );
		
		when( portfolioService.findPrimaryPlanCarriers( Mockito.any( Company.class )) ).thenReturn( planCarrierMap );
		
		when( planRatesDataDao.getBenefitPlanStates(
				futureCompany.getRealmPlanYearId(), currentCompany.getRealmPlanYearId())).thenReturn(mockPlanStateMap());
		

		when( planRatesDataDao.getBenefitPlans( Mockito.eq(futureCompany.getRealmPlanYearId()), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( Arrays.asList( BSSApplicationConstants.MEDICAL_PLAN_TYPE ) ) ) ) )
				.thenReturn( futureMedicalStateBenefitPlans );
		
		when( planRatesDataDao.getBenefitPlans( Mockito.eq(currentCompany.getRealmPlanYearId()), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( Arrays.asList( BSSApplicationConstants.MEDICAL_PLAN_TYPE ) ) ) ) )
				.thenReturn( currentMedicalStateBenefitPlans );
		
		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( BSSApplicationConstants.DENTAL_PLAN_TYPES ) ) ) )
				.thenReturn( dentalStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( BSSApplicationConstants.VISION_PLAN_TYPES ) ) ) )
				.thenReturn( visionStateBenefitPlans );


		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>(Arrays.asList(BSSApplicationConstants.LIFE_CODE ) ) ) ) )
				.thenReturn( lifeStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlanRatesBy( Mockito.any(Company.class))).thenReturn(benefitPlanRates);

		when( StrategyUtils.findBandCode( Mockito.any( Company.class ), Mockito.anyString(),
				Mockito.any( Map.class ) )).thenReturn(bandCode);
		
		when( headCountService.getHeadCountByGroupAndPlan(currentCompany, currentCompany.getRealmPlanYear().getId(),
				currentCompany.getRealmPlanYear().getPlanYearEnd(), false)).thenReturn(groupCovrgHeadCountMap);
		
		when( realmDataDao.getDisabilityOptionsForRealmPlanYears(futureCompany.getRealmPlanYearId(),
				currentCompany.getRealmPlanYearId(),
				futureCompany.getHeadQuatersState(), false)).thenReturn(disabilityPlans);	
		
		when(additionalBenefitPlanService.getAdditionalPlansRate(Mockito.any(Company.class), Mockito.anyBoolean(),
				Mockito.anyMap())).thenReturn(additionalBenefitPlanRates);	

		PlanRatesExportData plRateExData = planRatesServiceImpl.getPlanRatesExportData( futureCompany );
		assertEquals( 2, plRateExData.getAdditionalPlanData().size() );
		assertEquals( 3, plRateExData.getHealthPlanData().size() );
		assertEquals( 10, plRateExData.getHealthPlanData().get( "medical" ).size() );
		assertFalse(plRateExData.getCurrentStartDate().equals(""));
		assertFalse(plRateExData.getCurrentEndDate().equals(""));
	}


	/**
     * Given: Valid current and future plan year with SDI plans
     * When: getPlanRatesExportData method is called
     * Then: Return PlanRatesExportData object that includes all plan 
     */
	@Test
	public void getPlanRatesExportDataTestRenewalMappingDisabled2() throws Exception{
		Realm realm = new Realm();
		realm.setBenExchange( BenExchngEnums.TRINET_IV.getBenExchng() );
		Company currentCompany = mockCompany();
		currentCompany.setRealm( realm );
		RealmPlanYear realmPlanYear = mockCurrentRPY();
		realmPlanYear.setId(76);
		currentCompany.setRealmPlanYear( realmPlanYear );
		currentCompany.setRealmPlanYearId( currentCompany.getRealmPlanYear().getId() );
		Company futureCompany = mockCompany();
		futureCompany.setRealm( realm );
		RealmPlanYear futureRealmPlanYear = mockFutureRPY(30);
		futureRealmPlanYear.setId(86);
		futureCompany.setRealmPlanYear(futureRealmPlanYear);
		futureCompany.setRealmPlanYearId( futureCompany.getRealmPlanYear().getId());
		futureCompany.setRenewalCompany(true);
		String bandCode = "N";
		when(RulesAndConfigsUtils.isPlanRateMappingEnabled(futureCompany.getRealmPlanYearId())).thenReturn(false);
		when( CommonServiceHelper.jsonToObject( Mockito.any( String.class ), Mockito.any() )).thenCallRealMethod();
		Map<String,Set<PlanCarrier>> planCarrierMap = mockPlanCarrierMap();
		Map<String,StateBenefitPlan> futureMedicalStateBenefitPlans = mockMedicalStatePlans(false);
		Map<String,StateBenefitPlan> currentMedicalStateBenefitPlans = mockMedicalStatePlans(true);
		Map<String,StateBenefitPlan> dentalStateBenefitPlans = mockDentalStatePlans();
		Map<String,StateBenefitPlan> visionStateBenefitPlans = mockVisionStatePlans();
		Map<String,StateBenefitPlan> lifeStateBenefitPlans = mockLifeStatePlans();
		Map<String, List<BenefitPlanRate>> benefitPlanRates = mockPlanRates();
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupCovrgHeadCountMap = mockHeadCountMap();
		Map<String, AdditionalBenefitPlan> disabilityPlans = mockDisabilityPlans2();
		Map<String, AdditionalPlanRate> additionalBenefitPlanRates = mockAdditionalPlanRates();

		when( CommonServiceHelper.getOutOfRegionPlansToExclude( Mockito.any( Company.class ), Mockito.any(), Mockito.eq( realmDataDao ) ) )
				.thenReturn( new HashSet<String>() );
		when( RulesAndConfigsUtils.isDisabledBundledOn( Mockito.anyLong() ) ).thenReturn( true );

		when( companyService.getCompanyDetails( futureCompany.getCode(), true, futureCompany.getEmplId(), null ))
				.thenReturn( currentCompany );

		when( portfolioService.findPrimaryPlanCarriers( Mockito.any( Company.class )) ).thenReturn( planCarrierMap );

		when( planRatesDataDao.getBenefitPlanStates(
				futureCompany.getRealmPlanYearId(), currentCompany.getRealmPlanYearId())).thenReturn(mockPlanStateMap());


		when( planRatesDataDao.getBenefitPlans( Mockito.eq(futureCompany.getRealmPlanYearId()), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( Arrays.asList( BSSApplicationConstants.MEDICAL_PLAN_TYPE ) ) ) ) )
				.thenReturn( futureMedicalStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlans( Mockito.eq(currentCompany.getRealmPlanYearId()), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( Arrays.asList( BSSApplicationConstants.MEDICAL_PLAN_TYPE ) ) ) ) )
				.thenReturn( currentMedicalStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( BSSApplicationConstants.DENTAL_PLAN_TYPES ) ) ) )
				.thenReturn( dentalStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( BSSApplicationConstants.VISION_PLAN_TYPES ) ) ) )
				.thenReturn( visionStateBenefitPlans );


		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>(Arrays.asList(BSSApplicationConstants.LIFE_CODE ) ) ) ) )
				.thenReturn( lifeStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlanRatesBy( Mockito.any(Company.class))).thenReturn(benefitPlanRates);

		when( StrategyUtils.findBandCode( Mockito.any( Company.class ), Mockito.anyString(),
				Mockito.any( Map.class ) )).thenReturn(bandCode);

		when( headCountService.getHeadCountByGroupAndPlan(currentCompany, currentCompany.getRealmPlanYear().getId(),
				currentCompany.getRealmPlanYear().getPlanYearEnd(), false)).thenReturn(groupCovrgHeadCountMap);

		when( realmDataDao.getDisabilityOptionsForRealmPlanYears(futureCompany.getRealmPlanYearId(),
				currentCompany.getRealmPlanYearId(),
				futureCompany.getHeadQuatersState(), false)).thenReturn(disabilityPlans);

		when(additionalBenefitPlanService.getAdditionalPlansRate(Mockito.any(Company.class), Mockito.anyBoolean(),
				Mockito.anyMap())).thenReturn(additionalBenefitPlanRates);

		PlanRatesExportData plRateExData = planRatesServiceImpl.getPlanRatesExportData( futureCompany );
		assertEquals( 2, plRateExData.getAdditionalPlanData().size() );
		assertEquals( 3, plRateExData.getHealthPlanData().size() );
		assertEquals( 10, plRateExData.getHealthPlanData().get( "medical" ).size() );
		assertFalse(plRateExData.getCurrentStartDate().equals(""));
		assertFalse(plRateExData.getCurrentEndDate().equals(""));
	}


	@Test
	public void getPlanRatesExportDataTestRenewalMappingDisabled3() throws Exception{
		Realm realm = new Realm();
		realm.setBenExchange( BenExchngEnums.TRINET_IV.getBenExchng() );
		Company currentCompany = mockCompany();
		currentCompany.setRealm( realm );
		RealmPlanYear realmPlanYear = mockCurrentRPY();
		realmPlanYear.setId(76);
		currentCompany.setRealmPlanYear( realmPlanYear );
		currentCompany.setRealmPlanYearId( currentCompany.getRealmPlanYear().getId() );
		Company futureCompany = mockCompany();
		futureCompany.setRealm( realm );
		RealmPlanYear futureRealmPlanYear = mockFutureRPY(30);
		futureRealmPlanYear.setId(86);
		futureCompany.setRealmPlanYear(futureRealmPlanYear);
		futureCompany.setRealmPlanYearId( futureCompany.getRealmPlanYear().getId());
		futureCompany.setRenewalCompany(true);
		String bandCode = "N";
		when(RulesAndConfigsUtils.isPlanRateMappingEnabled(futureCompany.getRealmPlanYearId())).thenReturn(false);
		when( CommonServiceHelper.jsonToObject( Mockito.any( String.class ), Mockito.any() )).thenCallRealMethod();
		Map<String,Set<PlanCarrier>> planCarrierMap = mockPlanCarrierMap();
		Map<String,StateBenefitPlan> futureMedicalStateBenefitPlans = mockMedicalStatePlans(false);
		Map<String,StateBenefitPlan> currentMedicalStateBenefitPlans = mockMedicalStatePlans(true);
		Map<String,StateBenefitPlan> dentalStateBenefitPlans = mockDentalStatePlans();
		Map<String,StateBenefitPlan> visionStateBenefitPlans = mockVisionStatePlans();
		Map<String,StateBenefitPlan> lifeStateBenefitPlans = mockLifeStatePlans();
		Map<String, List<BenefitPlanRate>> benefitPlanRates = mockPlanRates();
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupCovrgHeadCountMap = mockHeadCountMap();
		Map<String, AdditionalBenefitPlan> disabilityPlans = mockDisabilityPlans();
		Map<String, AdditionalPlanRate> additionalBenefitPlanRates = mockAdditionalPlanRates();

		when( CommonServiceHelper.getOutOfRegionPlansToExclude( Mockito.any( Company.class ), Mockito.any(), Mockito.eq( realmDataDao ) ) )
				.thenReturn( new HashSet<String>() );
		when( RulesAndConfigsUtils.isDisabledBundledOn( Mockito.anyLong() ) ).thenReturn( true );

		when( companyService.getCompanyDetails( futureCompany.getCode(), true, futureCompany.getEmplId(), null ))
				.thenReturn( currentCompany );

		when( portfolioService.findPrimaryPlanCarriers( Mockito.any( Company.class )) ).thenReturn( planCarrierMap );

		when( planRatesDataDao.getBenefitPlanStates(
				futureCompany.getRealmPlanYearId(), currentCompany.getRealmPlanYearId())).thenReturn(mockPlanStateMap());


		when( planRatesDataDao.getBenefitPlans( Mockito.eq(futureCompany.getRealmPlanYearId()), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( Arrays.asList( BSSApplicationConstants.MEDICAL_PLAN_TYPE ) ) ) ) )
				.thenReturn( futureMedicalStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlans( Mockito.eq(currentCompany.getRealmPlanYearId()), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( Arrays.asList( BSSApplicationConstants.MEDICAL_PLAN_TYPE ) ) ) ) )
				.thenReturn( currentMedicalStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( BSSApplicationConstants.DENTAL_PLAN_TYPES ) ) ) )
				.thenReturn( dentalStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( BSSApplicationConstants.VISION_PLAN_TYPES ) ) ) )
				.thenReturn( visionStateBenefitPlans );


		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>(Arrays.asList(BSSApplicationConstants.LIFE_CODE ) ) ) ) )
				.thenReturn( lifeStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlanRatesBy( Mockito.any(Company.class))).thenReturn(benefitPlanRates);

		when( StrategyUtils.findBandCode( Mockito.any( Company.class ), Mockito.anyString(),
				Mockito.any( Map.class ) )).thenReturn(bandCode);

		when( headCountService.getHeadCountByGroupAndPlan(currentCompany, currentCompany.getRealmPlanYear().getId(),
				currentCompany.getRealmPlanYear().getPlanYearEnd(), false)).thenReturn(groupCovrgHeadCountMap);

		when( realmDataDao.getDisabilityOptionsForRealmPlanYears(futureCompany.getRealmPlanYearId(),
				currentCompany.getRealmPlanYearId(),
				futureCompany.getHeadQuatersState(), false)).thenReturn(disabilityPlans);

		when(additionalBenefitPlanService.getAdditionalPlansRate(Mockito.any(Company.class), Mockito.anyBoolean(),
				Mockito.anyMap())).thenReturn(additionalBenefitPlanRates);

		PlanRatesExportData plRateExData = planRatesServiceImpl.getPlanRatesExportData( futureCompany );
		assertEquals( 2, plRateExData.getAdditionalPlanData().size() );
		assertEquals( 3, plRateExData.getHealthPlanData().size() );
		assertEquals( 10, plRateExData.getHealthPlanData().get( "medical" ).size() );
		assertFalse(plRateExData.getCurrentStartDate().equals(""));
		assertFalse(plRateExData.getCurrentEndDate().equals(""));
	}



	@Test
	public void getPlanRatesExportDataTestRenewalMappingEnabled() throws Exception{
		Realm realm = new Realm();
		realm.setBenExchange( BenExchngEnums.TRINET_IV.getBenExchng() );
		Company currentCompany = mockCompany();
		currentCompany.setRealm( realm );
		currentCompany.setRealmPlanYear( mockCurrentRPY() );
		currentCompany.setRealmPlanYearId( currentCompany.getRealmPlanYear().getId() );
		Company futureCompany = mockCompany();
		futureCompany.setRealm( realm );
		futureCompany.setRealmPlanYear( mockFutureRPY(30) );
		futureCompany.setRealmPlanYearId( futureCompany.getRealmPlanYear().getId() );
		futureCompany.setRenewalCompany(true);
		String bandCode = "N";
		when(RulesAndConfigsUtils.isPlanRateMappingEnabled(futureCompany.getRealmPlanYearId())).thenReturn(true);
		when( CommonServiceHelper.jsonToObject( Mockito.any( String.class ), Mockito.any() )).thenCallRealMethod();
		Map<String,Set<PlanCarrier>> planCarrierMap = mockPlanCarrierMap();
		Map<String,StateBenefitPlan> futureMedicalStateBenefitPlans = mockMedicalStatePlans(false);
		Map<String,StateBenefitPlan> currentMedicalStateBenefitPlans = mockMedicalStatePlans(true);
		Map<String,StateBenefitPlan> dentalStateBenefitPlans = mockDentalStatePlans();
		Map<String,StateBenefitPlan> visionStateBenefitPlans = mockVisionStatePlans();
		Map<String,StateBenefitPlan> lifeStateBenefitPlans = mockLifeStatePlans();
		Map<String, List<BenefitPlanRate>> benefitPlanRates = mockPlanRates();
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupCovrgHeadCountMap = mockHeadCountMap();
		Map<String, AdditionalBenefitPlan> disabilityPlans = mockDisabilityPlans2();
		Map<String, AdditionalPlanRate> additionalBenefitPlanRates = mockAdditionalPlanRates();
		
		when( CommonServiceHelper.getOutOfRegionPlansToExclude( Mockito.any( Company.class ), Mockito.any(), Mockito.eq( realmDataDao ) ) )
				.thenReturn( new HashSet<String>() );
		when( RulesAndConfigsUtils.isDisabledBundledOn( Mockito.anyLong() ) ).thenReturn( true );

		when( companyService.getCompanyDetails( futureCompany.getCode(), true, futureCompany.getEmplId(), null ))
				.thenReturn( currentCompany );
		
		when( portfolioService.findPrimaryPlanCarriers( Mockito.any( Company.class )) ).thenReturn( planCarrierMap );
		
		when( planRatesDataDao.getBenefitPlanStates(
				futureCompany.getRealmPlanYearId(), currentCompany.getRealmPlanYearId())).thenReturn(mockPlanStateMap());
		

		when( planRatesDataDao.getBenefitPlans( Mockito.eq(futureCompany.getRealmPlanYearId()), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( Arrays.asList( BSSApplicationConstants.MEDICAL_PLAN_TYPE ) ) ) ) )
				.thenReturn( futureMedicalStateBenefitPlans );
		
		when( planRatesDataDao.getBenefitPlans( Mockito.eq(currentCompany.getRealmPlanYearId()), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( Arrays.asList( BSSApplicationConstants.MEDICAL_PLAN_TYPE ) ) ) ) )
				.thenReturn( currentMedicalStateBenefitPlans );
		
		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( BSSApplicationConstants.DENTAL_PLAN_TYPES ) ) ) )
				.thenReturn( dentalStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( BSSApplicationConstants.VISION_PLAN_TYPES ) ) ) )
				.thenReturn( visionStateBenefitPlans );


		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>(Arrays.asList(BSSApplicationConstants.LIFE_CODE ) ) ) ) )
				.thenReturn( lifeStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlanRatesBy( Mockito.any(Company.class))).thenReturn(benefitPlanRates);

		when ( planMappingDao.getPlanMappingsAsSimpleMap(Mockito.any(Company.class), Mockito.eq(null))).thenReturn(mockPlanMappings());
		
		when( StrategyUtils.findBandCode( Mockito.any( Company.class ), Mockito.anyString(),
				Mockito.any( Map.class ) )).thenReturn(bandCode);

		when(headCountService.getMappedHeadCounts(futureCompany.getCode(), futureCompany.getRealmPlanYear().getId()))
				.thenReturn(getMappedPlanHeadCount());
		
		when( realmDataDao.getDisabilityOptionsForRealmPlanYears(futureCompany.getRealmPlanYearId(),
				currentCompany.getRealmPlanYearId(),
				futureCompany.getHeadQuatersState(), false)).thenReturn(disabilityPlans);	
		
		when(additionalBenefitPlanService.getAdditionalPlansRate(Mockito.any(Company.class), Mockito.anyBoolean(),
				Mockito.anyMap())).thenReturn(additionalBenefitPlanRates);	

		PlanRatesExportData plRateExData = planRatesServiceImpl.getPlanRatesExportData( futureCompany );
		assertEquals( 2, plRateExData.getAdditionalPlanData().size() );
		assertEquals( 3, plRateExData.getHealthPlanData().size() );
		assertEquals( 8, plRateExData.getHealthPlanData().get( "medical" ).size() );
		for (HealthPlanRatesExportPlan planData : plRateExData.getHealthPlanData().get("medical")) {
			if (("0013HJ").equals(planData.getCurrentId())) {
				assertEquals(Long.valueOf(4), planData.getEmployeeOnlyCurrentHeadcount());
			}
			else if (("001EKX").equals(planData.getCurrentId())) {
				assertEquals(Long.valueOf(2), planData.getEmployeeOnlyCurrentHeadcount());
			}
		}
		assertFalse(plRateExData.getCurrentStartDate().equals(""));
		assertFalse(plRateExData.getCurrentEndDate().equals(""));
	}	

	@Test
	public void getPlanRatesExportDataTestRenewalClientYearRound() throws Exception {
		Realm realm = new Realm();
		realm.setBenExchange( BenExchngEnums.TRINET_IV.getBenExchng() );
		Company futureCompany = mockCompany();
		futureCompany.setRealm( realm );
		futureCompany.setRealmPlanYear( mockFutureRPY(-30) );
		futureCompany.setRealmPlanYearId( futureCompany.getRealmPlanYear().getId() );
		futureCompany.setRenewalCompany(true);

		when(RulesAndConfigsUtils.isPlanRateMappingEnabled(futureCompany.getRealmPlanYearId())).thenReturn(false);
		when( CommonServiceHelper.jsonToObject( Mockito.any( String.class ), Mockito.any() )).thenCallRealMethod();
		Map<String,Set<PlanCarrier>> planCarrierMap = mockPlanCarrierMap();
		Map<String,StateBenefitPlan> medicalStateBenefitPlans = mockMedicalStatePlans(false);
		Map<String,StateBenefitPlan> dentalStateBenefitPlans = mockDentalStatePlans();
		Map<String,StateBenefitPlan> visionStateBenefitPlans = mockVisionStatePlans();

		when( CommonServiceHelper.getOutOfRegionPlansToExclude( Mockito.any( Company.class ), Mockito.any(), Mockito.eq( realmDataDao ) ) )
				.thenReturn( new HashSet<String>() );
		when( RulesAndConfigsUtils.isDisabledBundledOn( Mockito.anyLong() ) ).thenReturn( true );

		when( portfolioService.findPrimaryPlanCarriers( Mockito.any( Company.class )) ).thenReturn( planCarrierMap );

		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( Arrays.asList( BSSApplicationConstants.MEDICAL_PLAN_TYPE ) ) ) ) )
				.thenReturn( medicalStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( BSSApplicationConstants.DENTAL_PLAN_TYPES ) ) ) )
				.thenReturn( dentalStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( BSSApplicationConstants.VISION_PLAN_TYPES ) ) ) )
				.thenReturn( visionStateBenefitPlans );

		PlanRatesExportData plRateExData = planRatesServiceImpl.getPlanRatesExportData( futureCompany );
		assertEquals( 2, plRateExData.getAdditionalPlanData().size() );
		assertEquals( 3, plRateExData.getHealthPlanData().size() );
		assertEquals( 6, plRateExData.getHealthPlanData().get( "medical" ).size() );
		assertNull(plRateExData.getCurrentStartDate());
		assertNull(plRateExData.getCurrentEndDate());
	}

	@Test
	public void getPlanRatesExportDataTestNewClient() throws Exception {
		Realm realm = new Realm();
		realm.setBenExchange( BenExchngEnums.TRINET_IV.getBenExchng() );
		Company futureCompany = mockCompany();
		futureCompany.setRealm( realm );
		futureCompany.setRealmPlanYear( mockFutureRPY(-30) );
		futureCompany.setRealmPlanYearId( futureCompany.getRealmPlanYear().getId() );
		futureCompany.setRenewalCompany(false);
		Company currentCompany = mockCompany();
		currentCompany.setRealm( realm );
		currentCompany.setRealmPlanYear( mockCurrentRPY() );
		currentCompany.setRealmPlanYearId( currentCompany.getRealmPlanYear().getId() );

		when(RulesAndConfigsUtils.isPlanRateMappingEnabled(futureCompany.getRealmPlanYearId())).thenReturn(false);
		when( CommonServiceHelper.jsonToObject( Mockito.any( String.class ), Mockito.any() )).thenCallRealMethod();
		Map<String,Set<PlanCarrier>> planCarrierMap = mockPlanCarrierMap();
		Map<String,StateBenefitPlan> medicalStateBenefitPlans = mockMedicalStatePlans(false);
		Map<String,StateBenefitPlan> dentalStateBenefitPlans = mockDentalStatePlans();
		Map<String,StateBenefitPlan> visionStateBenefitPlans = mockVisionStatePlans();

		when( CommonServiceHelper.getOutOfRegionPlansToExclude( Mockito.any( Company.class ), Mockito.any(), Mockito.eq( realmDataDao ) ) )
				.thenReturn( new HashSet<String>() );
		when( RulesAndConfigsUtils.isDisabledBundledOn( Mockito.anyLong() ) ).thenReturn( true );

		when( portfolioService.findPrimaryPlanCarriers( Mockito.any( Company.class )) ).thenReturn( planCarrierMap );


		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( Arrays.asList( BSSApplicationConstants.MEDICAL_PLAN_TYPE ) ) ) ) )
				.thenReturn( medicalStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( BSSApplicationConstants.DENTAL_PLAN_TYPES ) ) ) )
				.thenReturn( dentalStateBenefitPlans );

		when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.eq( new HashSet<>( BSSApplicationConstants.VISION_PLAN_TYPES ) ) ) )
				.thenReturn( visionStateBenefitPlans );

		PlanRatesExportData plRateExData = planRatesServiceImpl.getPlanRatesExportData( futureCompany );
		assertEquals( 2, plRateExData.getAdditionalPlanData().size() );
		assertEquals( 3, plRateExData.getHealthPlanData().size() );
		assertEquals( 6, plRateExData.getHealthPlanData().get( "medical" ).size() );
		assertNull(plRateExData.getCurrentStartDate());
		assertNull(plRateExData.getCurrentEndDate());
	}

    @Test
    public void getPlanRatesExportDataTestCostsWithPlanRate() throws Exception {
        Realm realm = new Realm();
        realm.setBenExchange( BenExchngEnums.TRINET_IV.getBenExchng() );
        Company futureCompany = mockCompany();
        futureCompany.setRealm( realm );
        futureCompany.setRealmPlanYear( mockFutureRPY(-30) );
        futureCompany.setRealmPlanYearId( futureCompany.getRealmPlanYear().getId() );
        futureCompany.setRenewalCompany(true);

        when( CommonServiceHelper.jsonToObject( Mockito.any( String.class ), Mockito.any() )).thenCallRealMethod();
        Map<String,Set<PlanCarrier>> planCarrierMap = mockPlanCarrierMap();
        when( portfolioService.findPrimaryPlanCarriers( Mockito.any( Company.class )) ).thenReturn( planCarrierMap );
        Map<String, List<BenefitPlanRate>> benefitPlanRates = mockPlansRates();
        when( planRatesDataDao.getBenefitPlanRatesBy( Mockito.any(Company.class))).thenReturn(benefitPlanRates);
        Map<String,StateBenefitPlan> medicalStateBenefitPlans = mockMedicalStatePlans(false);
        when( planRatesDataDao.getBenefitPlans( Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.eq( new HashSet<>( Arrays.asList( BSSApplicationConstants.MEDICAL_PLAN_TYPE ) ) ) ) )
                .thenReturn( medicalStateBenefitPlans );
        when(StrategyUtils.findBandCode(Mockito.any(Company.class), Mockito.eq("0013HJ"), Mockito.any(Map.class)))
                .thenReturn("9999");

        PlanRatesExportData plRateExData = planRatesServiceImpl.getPlanRatesExportData( futureCompany );

        assertEquals( 2, plRateExData.getAdditionalPlanData().size() );
        assertEquals( 1, plRateExData.getHealthPlanData().size() );
        assertEquals( 6, plRateExData.getHealthPlanData().get( "medical" ).size() );
        assertEquals("0013HJ", plRateExData.getHealthPlanData().get("medical").get(0).getFutureId());
        assertEquals(BigDecimal.valueOf(901), plRateExData.getHealthPlanData().get( "medical" ).get(0).getEmployeeOnlyFutureCost());
        assertEquals(BigDecimal.valueOf(902), plRateExData.getHealthPlanData().get( "medical" ).get(0).getEmployeeSpouseFutureCost());
        assertEquals(BigDecimal.valueOf(903), plRateExData.getHealthPlanData().get( "medical" ).get(0).getEmployeeChildFutureCost());
        assertEquals(BigDecimal.valueOf(904), plRateExData.getHealthPlanData().get( "medical" ).get(0).getEmployeeFamilyFutureCost());
        assertNull(plRateExData.getCurrentStartDate());
        assertNull(plRateExData.getCurrentEndDate());

        verify(realmPlyrPlanService, times(1)).getMapForRealmPlanYear(futureCompany.getRealmPlanYear().getId());
    }

    @Test
    public void testGetBenefitPlanRateByV2() throws Exception {
        Realm realm = new Realm();
        realm.setBenExchange( BenExchngEnums.TRINET_IV.getBenExchng() );
        Company currentCompany = mockCompany();
        currentCompany.setRealm( realm );
        currentCompany.setRealmPlanYear( mockCurrentRPY() );
        currentCompany.setRealmPlanYearId( currentCompany.getRealmPlanYear().getId() );

        when( CommonServiceHelper.jsonToObject( Mockito.any( String.class ), Mockito.any() )).thenCallRealMethod();
        Map<String, List<BenefitPlanRate>> benefitPlanRates = mockPlansRates();

        when(planRatesDataDao.getBenefitPlanRatesBy(Mockito.any(Company.class))).thenReturn(benefitPlanRates);
        when(StrategyUtils.findBandCode(Mockito.any(Company.class), Mockito.eq("0013HJ"), Mockito.any(Map.class)))
                .thenReturn("N");
        when(StrategyUtils.findBandCode(Mockito.any(Company.class), Mockito.eq("001EKX"), Mockito.any(Map.class)))
                .thenReturn("67890");

        Map<String, List<BenefitPlanRate>> planRatesByPlanId = planRatesServiceImpl.getBenefitPlanRatesBy(currentCompany);

        // Verify list size for each key
        List<BenefitPlanRate> rates0013HJ = planRatesByPlanId.get("0013HJ");
        assertEquals(4, rates0013HJ.size());
        List<BenefitPlanRate> rates001EKX = planRatesByPlanId.get("001EKX");
        assertEquals(4, rates001EKX.size());

        // Verify BenefitPlanRate properties for 0013HJ
        assertEquals("1", rates0013HJ.get(0).getCoverageCode());
        assertEquals(new BigDecimal("100"), rates0013HJ.get(0).getEmployerCost());
        assertEquals("N", rates0013HJ.get(0).getBandCode());

        assertEquals("2", rates0013HJ.get(1).getCoverageCode());
        assertEquals(new BigDecimal("200"), rates0013HJ.get(1).getEmployerCost());
        assertEquals("N", rates0013HJ.get(1).getBandCode());

        assertEquals("C", rates0013HJ.get(2).getCoverageCode());
        assertEquals(new BigDecimal("300"), rates0013HJ.get(2).getEmployerCost());
        assertEquals("N", rates0013HJ.get(2).getBandCode());

        assertEquals("4", rates0013HJ.get(3).getCoverageCode());
        assertEquals(new BigDecimal("400"), rates0013HJ.get(3).getEmployerCost());
        assertEquals("N", rates0013HJ.get(3).getBandCode());

        // Verify BenefitPlanRate properties for 001EKX
        assertEquals("1", rates001EKX.get(0).getCoverageCode());
        assertEquals(new BigDecimal("102"), rates001EKX.get(0).getEmployerCost());
        assertEquals("67890", rates001EKX.get(0).getBandCode());

        assertEquals("2", rates001EKX.get(1).getCoverageCode());
        assertEquals(new BigDecimal("202"), rates001EKX.get(1).getEmployerCost());
        assertEquals("67890", rates001EKX.get(1).getBandCode());

        assertEquals("C", rates001EKX.get(2).getCoverageCode());
        assertEquals(new BigDecimal("302"), rates001EKX.get(2).getEmployerCost());
        assertEquals("67890", rates001EKX.get(2).getBandCode());

        assertEquals("4", rates001EKX.get(3).getCoverageCode());
        assertEquals(new BigDecimal("402"), rates001EKX.get(3).getEmployerCost());
        assertEquals("67890", rates001EKX.get(3).getBandCode());
    }

    @Test
    public void testGetBenefitPlanRateByV2NoBandCodeMatch() throws Exception {
        Realm realm = new Realm();
        realm.setBenExchange( BenExchngEnums.TRINET_IV.getBenExchng() );
        Company currentCompany = mockCompany();
        currentCompany.setRealm( realm );
        currentCompany.setRealmPlanYear( mockCurrentRPY() );
        currentCompany.setRealmPlanYearId( currentCompany.getRealmPlanYear().getId() );

        when( CommonServiceHelper.jsonToObject( Mockito.any( String.class ), Mockito.any() )).thenCallRealMethod();
        Map<String, List<BenefitPlanRate>> benefitPlanRates = mockPlanRates();

        when(planRatesDataDao.getBenefitPlanRatesBy(Mockito.any(Company.class))).thenReturn(benefitPlanRates);
        when(StrategyUtils.findBandCode(Mockito.any(Company.class), Mockito.eq("0013HJ"), Mockito.any(Map.class)))
                .thenReturn("1234");

        Map<String, List<BenefitPlanRate>> planRatesByPlanId = planRatesServiceImpl.getBenefitPlanRatesBy(currentCompany);

        // It does not match with company band code. So get plan rates for band code N.
        List<BenefitPlanRate> rates = planRatesByPlanId.get("0013HJ");
        assertEquals(4, rates.size());

        assertEquals("1", rates.get(0).getCoverageCode());
        assertEquals(new BigDecimal("100"), rates.get(0).getEmployerCost());
        assertEquals("N", rates.get(0).getBandCode());

        assertEquals("2", rates.get(1).getCoverageCode());
        assertEquals(new BigDecimal("200"), rates.get(1).getEmployerCost());
        assertEquals("N", rates.get(1).getBandCode());

        assertEquals("C", rates.get(2).getCoverageCode());
        assertEquals(new BigDecimal("300"), rates.get(2).getEmployerCost());
        assertEquals("N", rates.get(2).getBandCode());

        assertEquals("4", rates.get(3).getCoverageCode());
        assertEquals(new BigDecimal("400"), rates.get(3).getEmployerCost());
        assertEquals("N", rates.get(3).getBandCode());
    }

    @Test
    public void testGetBenefitPlanRatesByBenefitType() throws Exception {
        Realm realm = new Realm();
        realm.setBenExchange( BenExchngEnums.TRINET_IV.getBenExchng() );
        Company currentCompany = mockCompany();
        currentCompany.setRealm( realm );
        currentCompany.setRealmPlanYear( mockCurrentRPY() );
        currentCompany.setRealmPlanYearId( currentCompany.getRealmPlanYear().getId() );

        when( CommonServiceHelper.jsonToObject( Mockito.any( String.class ), Mockito.any() )).thenCallRealMethod();
        Map<String, List<BenefitPlanRate>> benefitPlanRates = mockPlansRates();

        when(planRatesDataDao.getBenefitPlanRatesBy(Mockito.any(Company.class))).thenReturn(benefitPlanRates);
        when(StrategyUtils.findBandCode(Mockito.any(Company.class), Mockito.eq("0013HJ"), Mockito.any(Map.class)))
                .thenReturn("N");
        when(StrategyUtils.findBandCode(Mockito.any(Company.class), Mockito.eq("001EKX"), Mockito.any(Map.class)))
                .thenReturn("67890");

        Map<String, List<PlanRate>> planRatesByPlanId = planRatesServiceImpl.getBenefitPlanRatesByBenefitType(currentCompany);

        // Verify map size and key
        assertEquals(1, planRatesByPlanId.size());
        assertTrue(planRatesByPlanId.containsKey("10"));

        // Verify list size for planType 10
        List<PlanRate> planRates = planRatesByPlanId.get("10");
        assertEquals(2, planRates.size());

        // Verify PlanRate for 0013HJ
        PlanRate planRate1 = planRates.get(0);
        assertEquals("0013HJ", planRate1.getPlanId());
        assertNotNull(planRate1.getRateDetails());
        assertEquals("tiered", planRate1.getRateDetails().getRateType());
        List<PlanRate.RateDetails.Rate> rates1 = planRate1.getRateDetails().getRates();
        assertEquals(4, rates1.size());
        assertEquals("employee", rates1.get(0).getTierCode());
        assertEquals(100.0, rates1.get(0).getRetailRate(), 0);
        assertEquals("employeePlusSpouse", rates1.get(1).getTierCode());
        assertEquals(200.0, rates1.get(1).getRetailRate(), 0);
        assertEquals("employeePlusChild", rates1.get(2).getTierCode());
        assertEquals(300.0, rates1.get(2).getRetailRate(), 0);
        assertEquals("employeePlusFamily", rates1.get(3).getTierCode());
        assertEquals(400.0, rates1.get(3).getRetailRate(), 0);

        // Verify PlanRate for 001EKX
        PlanRate planRate2 = planRates.get(1);
        assertEquals("001EKX", planRate2.getPlanId());
        assertNotNull(planRate2.getRateDetails());
        assertEquals("tiered", planRate2.getRateDetails().getRateType());
        List<PlanRate.RateDetails.Rate> rates2 = planRate2.getRateDetails().getRates();
        assertEquals(4, rates2.size());
        assertEquals("employee", rates2.get(0).getTierCode());
        assertEquals(102.0, rates2.get(0).getRetailRate(), 0);
        assertEquals("employeePlusSpouse", rates2.get(1).getTierCode());
        assertEquals(202.0, rates2.get(1).getRetailRate(), 0);
        assertEquals("employeePlusChild", rates2.get(2).getTierCode());
        assertEquals(302.0, rates2.get(2).getRetailRate(), 0);
        assertEquals("employeePlusFamily", rates2.get(3).getTierCode());
        assertEquals(402.0, rates2.get(3).getRetailRate(), 0);
    }

	private static Company mockCompany() {
		Company company = new Company();
		company.setCode( "YYC" );
		company.setName( "The YYC Company" );
		company.setEmplId( "0001234567");
		return company;
	}

	private static RealmPlanYear mockCurrentRPY() {
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId( 76 );
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -366);
		rpy.setPlanYearStart( new java.sql.Date( cal.getTimeInMillis()));
		cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 29);
		rpy.setPlanYearEnd( new java.sql.Date( cal.getTimeInMillis()));
		return rpy;
	}

	private static RealmPlanYear mockFutureRPY(int daysToAdd) {
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId( 86 );
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, daysToAdd);
		rpy.setPlanYearStart( new java.sql.Date( cal.getTimeInMillis()));
		cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 365 + daysToAdd);
		rpy.setPlanYearEnd( new java.sql.Date( cal.getTimeInMillis()));
		return rpy;
	}

	private static PlanRatesExportData mockPlanRatesExportData() {
		String dataStream = "{\"currentStartDate\":\"01-Oct-2020\",\"currentEndDate\":\"30-Sep-2021\",\"futureStartDate\":\"01-Oct-2021\",\"futureEndDate\":\"30-Sep-2022\",\"healthPlanData\":{\"vision\":[ {\"currentId\":\"004S7T\",\"futureId\":\"004S7T\",\"currentName\":\"Aetna EyeMed\",\"futureName\":\"Aetna EyeMed\",\"planType\":\"14\",\"hasHeadcount\":false,\"offeredYearsFlag\":\"B\",\"offeredStates\":null,\"employeeOnlyCurrentCost\":5.16,\"employeeSpouseCurrentCost\":9.81,\"employeeChildCurrentCost\":10.31,\"employeeFamilyCurrentCost\":15.16,\"employeeOnlyFutureCost\":5.16,\"employeeSpouseFutureCost\":9.81,\"employeeChildFutureCost\":10.31,\"employeeFamilyFutureCost\":15.16,\"employeeOnlyCurrentHeadcount\":0,\"employeeSpouseCurrentHeadcount\":0,\"employeeChildCurrentHeadcount\":0,\"employeeFamilyCurrentHeadcount\":0,\"employeeOnlyFutureHeadcount\":0,\"employeeSpouseFutureHeadcount\":0,\"employeeChildFutureHeadcount\":0,\"employeeFamilyFutureHeadcount\":0 }, {\"currentId\":\"002J2I\",\"futureId\":null,\"currentName\":\"VSP Vision Voluntary VA\",\"futureName\":null,\"planType\":\"1V\",\"hasHeadcount\":false,\"offeredYearsFlag\":\"C\",\"offeredStates\":null,\"employeeOnlyCurrentCost\":6.05,\"employeeSpouseCurrentCost\":14.22,\"employeeChildCurrentCost\":12.94,\"employeeFamilyCurrentCost\":19.4,\"employeeOnlyFutureCost\":null,\"employeeSpouseFutureCost\":null,\"employeeChildFutureCost\":null,\"employeeFamilyFutureCost\":null,\"employeeOnlyCurrentHeadcount\":0,\"employeeSpouseCurrentHeadcount\":0,\"employeeChildCurrentHeadcount\":0,\"employeeFamilyCurrentHeadcount\":0,\"employeeOnlyFutureHeadcount\":0,\"employeeSpouseFutureHeadcount\":0,\"employeeChildFutureHeadcount\":0,\"employeeFamilyFutureHeadcount\":0 } ],\"medical\":[ {\"currentId\":\"0013HJ\",\"futureId\":\"0013HJ\",\"currentName\":\"Kaiser HMO 1000 North CA\",\"futureName\":\"Kaiser HMO 1000 North CA\",\"planType\":\"10\",\"hasHeadcount\":false,\"offeredYearsFlag\":\"B\",\"offeredStates\":[\"CA\" ],\"employeeOnlyCurrentCost\":676,\"employeeSpouseCurrentCost\":1488,\"employeeChildCurrentCost\":1353,\"employeeFamilyCurrentCost\":2029,\"employeeOnlyFutureCost\":676,\"employeeSpouseFutureCost\":1488,\"employeeChildFutureCost\":1353,\"employeeFamilyFutureCost\":2029,\"employeeOnlyCurrentHeadcount\":0,\"employeeSpouseCurrentHeadcount\":0,\"employeeChildCurrentHeadcount\":0,\"employeeFamilyCurrentHeadcount\":0,\"employeeOnlyFutureHeadcount\":0,\"employeeSpouseFutureHeadcount\":0,\"employeeChildFutureHeadcount\":0,\"employeeFamilyFutureHeadcount\":0 }, {\"currentId\":null,\"futureId\":\"004S6M\",\"currentName\":null,\"futureName\":\"UHC Standard EPO\",\"planType\":\"10\",\"hasHeadcount\":false,\"offeredYearsFlag\":\"F\",\"offeredStates\":null,\"employeeOnlyCurrentCost\":892,\"employeeSpouseCurrentCost\":1918,\"employeeChildCurrentCost\":1605,\"employeeFamilyCurrentCost\":2765,\"employeeOnlyFutureCost\":892,\"employeeSpouseFutureCost\":1918,\"employeeChildFutureCost\":1605,\"employeeFamilyFutureCost\":2765,\"employeeOnlyCurrentHeadcount\":0,\"employeeSpouseCurrentHeadcount\":0,\"employeeChildCurrentHeadcount\":0,\"employeeFamilyCurrentHeadcount\":0,\"employeeOnlyFutureHeadcount\":0,\"employeeSpouseFutureHeadcount\":0,\"employeeChildFutureHeadcount\":0,\"employeeFamilyFutureHeadcount\":0 } ],\"dental\":[ {\"currentId\":\"002J1X\",\"futureId\":null,\"currentName\":\"Delta Dental Enhanced\",\"futureName\":null,\"planType\":\"11\",\"hasHeadcount\":true,\"offeredYearsFlag\":\"C\",\"offeredStates\":null,\"employeeOnlyCurrentCost\":44.46,\"employeeSpouseCurrentCost\":97.79,\"employeeChildCurrentCost\":109.99,\"employeeFamilyCurrentCost\":165.79,\"employeeOnlyFutureCost\":null,\"employeeSpouseFutureCost\":null,\"employeeChildFutureCost\":null,\"employeeFamilyFutureCost\":null,\"employeeOnlyCurrentHeadcount\":5,\"employeeSpouseCurrentHeadcount\":5,\"employeeChildCurrentHeadcount\":0,\"employeeFamilyCurrentHeadcount\":12,\"employeeOnlyFutureHeadcount\":0,\"employeeSpouseFutureHeadcount\":0,\"employeeChildFutureHeadcount\":0,\"employeeFamilyFutureHeadcount\":0 }, {\"currentId\":\"005RAP\",\"futureId\":null,\"currentName\":\"MetLife Standard Vol VA\",\"futureName\":null,\"planType\":\"1D\",\"hasHeadcount\":false,\"offeredYearsFlag\":\"C\",\"offeredStates\":null,\"employeeOnlyCurrentCost\":28.51,\"employeeSpouseCurrentCost\":60.71,\"employeeChildCurrentCost\":68.25,\"employeeFamilyCurrentCost\":101.89,\"employeeOnlyFutureCost\":null,\"employeeSpouseFutureCost\":null,\"employeeChildFutureCost\":null,\"employeeFamilyFutureCost\":null,\"employeeOnlyCurrentHeadcount\":0,\"employeeSpouseCurrentHeadcount\":0,\"employeeChildCurrentHeadcount\":0,\"employeeFamilyCurrentHeadcount\":0,\"employeeOnlyFutureHeadcount\":0,\"employeeSpouseFutureHeadcount\":0,\"employeeChildFutureHeadcount\":0,\"employeeFamilyFutureHeadcount\":0 } ] },\"additionalPlanData\":{\"DISABILITY\":[ {\"id\":null,\"name\":\"50% STD 1750 Co Pd & 50% LTD 7500 Co Pd\",\"planType\":null,\"offeredYearsFlag\":null,\"offeredStates\":null,\"currentUnit\":null,\"futureUnit\":null,\"currentCost\":null,\"futureCost\":null,\"optionPlans\":[ {\"name\":\"50% LTD 7500 Co Pd\",\"planType\":\"LTD\",\"offeredStatesString\":\"All States\",\"currentUnit\":null,\"futureUnit\":\"$100 of covered payroll\",\"currentCost\":null,\"futureCost\":0.1723,\"sdiPlan\":false }, {\"name\":\"50% STD 1750 Co Pd\",\"planType\":\"STD\",\"offeredStatesString\":\"All States\",\"currentUnit\":null,\"futureUnit\":\"$100 of covered payroll\",\"currentCost\":null,\"futureCost\":0.0405,\"sdiPlan\":false } ] }, {\"id\":null,\"name\":\"STD Employee Paid & 60% LTD 15000 Co Pd (Premium)\",\"planType\":null,\"offeredYearsFlag\":null,\"offeredStates\":null,\"currentUnit\":null,\"futureUnit\":null,\"currentCost\":null,\"futureCost\":null,\"optionPlans\":[ {\"name\":\"60% LTD 15000 Co Pd (Premium)\",\"planType\":\"LTD\",\"offeredStatesString\":\"All States\",\"currentUnit\":\"$100 of covered payroll\",\"futureUnit\":\"$100 of covered payroll\",\"currentCost\":0.19,\"futureCost\":0.19,\"sdiPlan\":false } ] }],\"LIFE\":[ {\"id\":\"000SRO\",\"name\":\"1X Earnings Basic Life & AD&D\",\"planType\":\"23\",\"offeredYearsFlag\":\"B\",\"offeredStates\":null,\"currentUnit\":\"$1,000 of covered payroll\",\"futureUnit\":\"$1,000 of covered payroll\",\"currentCost\":0.13,\"futureCost\":0.13,\"optionPlans\":null }, {\"id\":\"002J3Q\",\"name\":\"Basic Life $200,000\",\"planType\":\"23\",\"offeredYearsFlag\":\"B\",\"offeredStates\":null,\"currentUnit\":\"$1,000 of covered payroll\",\"futureUnit\":\"$1,000 of covered payroll\",\"currentCost\":0.13,\"futureCost\":0.13,\"optionPlans\":null}]}}";
		return CommonServiceHelper.jsonToObject( dataStream, PlanRatesExportData.class );
	}

	private static Map<String, Set<PlanCarrier>> mockPlanCarrierMap() {
		Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();

		Set<PlanCarrier> visionSet = new HashSet<>();
		String dataStream = "{\"id\":6,\"name\":\"VSP\",\"mandatory\":false,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}";
		visionSet.add( CommonServiceHelper.jsonToObject( dataStream, PlanCarrier.class ) );
		dataStream = "{\"id\":1,\"name\":\"Aetna\",\"mandatory\":false,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}";
		visionSet.add( CommonServiceHelper.jsonToObject( dataStream, PlanCarrier.class ) );
		planCarrierMap.put( "vision", visionSet );

		Set<PlanCarrier> medicalSet = new HashSet<>();
		dataStream = "{\"id\":9,\"name\":\"UHC Portfolio A\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}";
		medicalSet.add( CommonServiceHelper.jsonToObject( dataStream, PlanCarrier.class ) );
		dataStream = "{\"id\":2,\"name\":\"Kaiser\",\"mandatory\":false,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}";
		medicalSet.add( CommonServiceHelper.jsonToObject( dataStream, PlanCarrier.class ) );
		planCarrierMap.put( "medical", medicalSet );

		Set<PlanCarrier> dentalSet = new HashSet<>();
		dataStream = "{\"id\":16,\"name\":\"Delta\",\"mandatory\":false,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}";
		dentalSet.add( CommonServiceHelper.jsonToObject( dataStream, PlanCarrier.class ) );
		dataStream = "{\"id\":3,\"name\":\"Metlife\",\"mandatory\":false,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}";
		dentalSet.add( CommonServiceHelper.jsonToObject( dataStream, PlanCarrier.class ) );
		planCarrierMap.put( "dental", dentalSet );

		return planCarrierMap;
	}
	
	private static Map<String, Set<String>> mockPlanStateMap() {
		Map<String, Set<String>> planStateMap = new HashMap<>();
		planStateMap.put("0013HJ", new HashSet<>(Arrays.asList("NJ")));
		return planStateMap;
	}

	private static Map<String, List<BenefitPlanRate>> mockPlanRates() {
		Map<String, List<BenefitPlanRate>> benefitPlanRates = new HashMap<>();

		List<BenefitPlanRate> rateList = new ArrayList<>();
		String dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"1\",\"employerCost\":100,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
		rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
		dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"2\",\"employerCost\":200,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
		rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
		dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"C\",\"employerCost\":300,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
		rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
		dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"4\",\"employerCost\":400,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
		rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
		benefitPlanRates.put( "0013HJ", rateList );
		return benefitPlanRates;
	}

    private static Map<String, List<BenefitPlanRate>> mockPlansRates() {
        Map<String, List<BenefitPlanRate>> benefitPlanRates = new HashMap<>();
        List<BenefitPlanRate> rateList = new ArrayList<>();
        String dataStream;

        // Plan 0013HJ with bandCode N
        dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"1\",\"employerCost\":100,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"2\",\"employerCost\":200,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"C\",\"employerCost\":300,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"4\",\"employerCost\":400,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );

        // Plan 0013HJ with bandCode 9999
        dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"1\",\"employerCost\":901,\"bandCode\":\"9999\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"2\",\"employerCost\":902,\"bandCode\":\"9999\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"C\",\"employerCost\":903,\"bandCode\":\"9999\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"4\",\"employerCost\":904,\"bandCode\":\"9999\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        benefitPlanRates.put( "0013HJ", rateList );

        // Plan 001EKX with bandCode 12345
        rateList = new ArrayList<>();
        dataStream = "{\"benefitPlan\":\"001EKX\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"1\",\"employerCost\":101,\"bandCode\":\"12345\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        dataStream = "{\"benefitPlan\":\"001EKX\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"2\",\"employerCost\":201,\"bandCode\":\"12345\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        dataStream = "{\"benefitPlan\":\"001EKX\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"C\",\"employerCost\":301,\"bandCode\":\"12345\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        dataStream = "{\"benefitPlan\":\"001EKX\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"4\",\"employerCost\":401,\"bandCode\":\"12345\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );

        // Plan 001EKX with bandCode 67890
        dataStream = "{\"benefitPlan\":\"001EKX\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"1\",\"employerCost\":102,\"bandCode\":\"67890\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        dataStream = "{\"benefitPlan\":\"001EKX\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"2\",\"employerCost\":202,\"bandCode\":\"67890\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        dataStream = "{\"benefitPlan\":\"001EKX\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"C\",\"employerCost\":302,\"bandCode\":\"67890\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        dataStream = "{\"benefitPlan\":\"001EKX\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"4\",\"employerCost\":402,\"bandCode\":\"67890\",\"optionId\":null,\"costId\":null}";
        rateList.add( CommonServiceHelper.jsonToObject( dataStream, BenefitPlanRate.class ) );
        benefitPlanRates.put( "001EKX", rateList );

        return benefitPlanRates;
    }

	private static Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> mockHeadCountMap() {
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupCovrgHeadCountMap = new HashMap<>();
		Map<String, List<PlanCoverageLevelHeadCount>> planHeadcountMap = new HashMap<>();
		List<PlanCoverageLevelHeadCount> headcountList = new ArrayList<>();
		String dataStream = "{\"groupName\":\"BENEFIT_PROGRAM\",\"planType\":\"10\",\"benefitPlan\":\"0013HJ\",\"covrgCode\":\"1\",\"headCount\":1,\"hsaHeadCount\":0}";
		headcountList.add( CommonServiceHelper.jsonToObject( dataStream, PlanCoverageLevelHeadCount.class ) );
		dataStream = "{\"groupName\":\"BENEFIT_PROGRAM\",\"planType\":\"10\",\"benefitPlan\":\"0013HJ\",\"covrgCode\":\"2\",\"headCount\":2,\"hsaHeadCount\":0}";
		headcountList.add( CommonServiceHelper.jsonToObject( dataStream, PlanCoverageLevelHeadCount.class ) );
		dataStream = "{\"groupName\":\"BENEFIT_PROGRAM\",\"planType\":\"10\",\"benefitPlan\":\"0013HJ\",\"covrgCode\":\"C\",\"headCount\":3,\"hsaHeadCount\":0}";
		headcountList.add( CommonServiceHelper.jsonToObject( dataStream, PlanCoverageLevelHeadCount.class ) );
		dataStream = "{\"groupName\":\"BENEFIT_PROGRAM\",\"planType\":\"10\",\"benefitPlan\":\"0013HJ\",\"covrgCode\":\"4\",\"headCount\":4,\"hsaHeadCount\":0}";
		headcountList.add( CommonServiceHelper.jsonToObject( dataStream, PlanCoverageLevelHeadCount.class ) );
		planHeadcountMap.put("0013HJ", headcountList);
		groupCovrgHeadCountMap.put( "BENEFIT_PROGRAM", planHeadcountMap );
		
		dataStream = "{\"groupName\":\"BENEFIT_PROGRAM2\",\"planType\":\"10\",\"benefitPlan\":\"0013HJ\",\"covrgCode\":\"1\",\"headCount\":1,\"hsaHeadCount\":0}";
		headcountList.add( CommonServiceHelper.jsonToObject( dataStream, PlanCoverageLevelHeadCount.class ) );
		dataStream = "{\"groupName\":\"BENEFIT_PROGRAM2\",\"planType\":\"10\",\"benefitPlan\":\"0013HJ\",\"covrgCode\":\"2\",\"headCount\":2,\"hsaHeadCount\":0}";
		headcountList.add( CommonServiceHelper.jsonToObject( dataStream, PlanCoverageLevelHeadCount.class ) );
		dataStream = "{\"groupName\":\"BENEFIT_PROGRAM2\",\"planType\":\"10\",\"benefitPlan\":\"0013HJ\",\"covrgCode\":\"C\",\"headCount\":3,\"hsaHeadCount\":0}";
		headcountList.add( CommonServiceHelper.jsonToObject( dataStream, PlanCoverageLevelHeadCount.class ) );
		dataStream = "{\"groupName\":\"BENEFIT_PROGRAM2\",\"planType\":\"10\",\"benefitPlan\":\"0013HJ\",\"covrgCode\":\"4\",\"headCount\":4,\"hsaHeadCount\":0}";
		headcountList.add( CommonServiceHelper.jsonToObject( dataStream, PlanCoverageLevelHeadCount.class ) );
		planHeadcountMap.put("0013HJ", headcountList);
		groupCovrgHeadCountMap.put( "BENEFIT_PROGRAM2", planHeadcountMap );
		
		return groupCovrgHeadCountMap;
	}

	private static Map<String,StateBenefitPlan> mockMedicalStatePlans(boolean history) {
		Map<String,StateBenefitPlan> statePlans = new HashMap<>();
		String data = "{\"benefitPlan\":\"0013HJ\",\"description\":\"Kaiser HMO 1000 North CA\",\"planType\":\"10\",\"vendorId\":\"KAISERAM\",\"crossRefPlanId\":null,\"portfolioId\":2,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
		convertAndPutMap( data, statePlans );
		data = "{\"benefitPlan\":\"0013HK\",\"description\":\"Kaiser HMO 1000 South CA\",\"planType\":\"10\",\"vendorId\":\"KAISERAM\",\"crossRefPlanId\":null,\"portfolioId\":2,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
		convertAndPutMap( data, statePlans );
		data = "{\"benefitPlan\":\"001EKW\",\"description\":\"UHC Essential\",\"planType\":\"10\",\"vendorId\":\"UHCAM\",\"crossRefPlanId\":null,\"portfolioId\":9,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
		convertAndPutMap( data, statePlans );
		if (history) {
			data = "{\"benefitPlan\":\"001EKX\",\"description\":\"UHC Premium\",\"planType\":\"10\",\"vendorId\":\"UHCAM\",\"crossRefPlanId\":null,\"portfolioId\":9,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
			convertAndPutMap( data, statePlans );
			data = "{\"benefitPlan\":\"OLD1_1\",\"description\":\"Old Medical Plan\",\"planType\":\"10\",\"vendorId\":\"UHCAM\",\"crossRefPlanId\":null,\"portfolioId\":9,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
			convertAndPutMap( data, statePlans );
			data = "{\"benefitPlan\":\"OLD2_1\",\"description\":\"Old Medical Plan\",\"planType\":\"10\",\"vendorId\":\"UHCAM\",\"crossRefPlanId\":null,\"portfolioId\":9,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
			convertAndPutMap( data, statePlans );
			data = "{\"benefitPlan\":\"OLD2_2\",\"description\":\"Old Medical Plan\",\"planType\":\"10\",\"vendorId\":\"UHCAM\",\"crossRefPlanId\":null,\"portfolioId\":9,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
			convertAndPutMap( data, statePlans );
		} else {
			data = "{\"benefitPlan\":\"NEW1_1\",\"description\":\"New Medical Plan\",\"planType\":\"10\",\"vendorId\":\"UHCAM\",\"crossRefPlanId\":null,\"portfolioId\":9,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
			convertAndPutMap( data, statePlans );
			data = "{\"benefitPlan\":\"NEW1_2\",\"description\":\"New Medical Plan\",\"planType\":\"10\",\"vendorId\":\"UHCAM\",\"crossRefPlanId\":null,\"portfolioId\":9,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
			convertAndPutMap( data, statePlans );
			data = "{\"benefitPlan\":\"NEW2_1\",\"description\":\"New Medical Plan\",\"planType\":\"10\",\"vendorId\":\"UHCAM\",\"crossRefPlanId\":null,\"portfolioId\":9,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
			convertAndPutMap( data, statePlans );
		}
		return statePlans;
	}

	private static Map<String,StateBenefitPlan> mockDentalStatePlans() {
		Map<String,StateBenefitPlan> statePlans = new HashMap<>();
		String data = "{\"benefitPlan\":\"005RAJ\",\"description\":\"Delta Dental Premium Vol VA\",\"planType\":\"1D\",\"vendorId\":\"DELTAAM\",\"crossRefPlanId\":null,\"portfolioId\":16,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
		convertAndPutMap( data, statePlans );
		data = "{\"benefitPlan\":\"005RAG\",\"description\":\"Delta Dental Standard Vol VA\",\"planType\":\"1D\",\"vendorId\":\"DELTAAM\",\"crossRefPlanId\":null,\"portfolioId\":16,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
		convertAndPutMap( data, statePlans );
		data = "{\"benefitPlan\":\"006IXI\",\"description\":\"MetLife Premium Vol MT\",\"planType\":\"1D\",\"vendorId\":\"METAM\",\"crossRefPlanId\":null,\"portfolioId\":3,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
		convertAndPutMap( data, statePlans );
		data = "{\"benefitPlan\":\"006IXJ\",\"description\":\"MetLife Standard Vol MT\",\"planType\":\"1D\",\"vendorId\":\"METAM\",\"crossRefPlanId\":null,\"portfolioId\":3,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
		convertAndPutMap( data, statePlans );
		return statePlans;
	}

	private static Map<String,StateBenefitPlan> mockVisionStatePlans() {
		Map<String,StateBenefitPlan> statePlans = new HashMap<>();
		String data = "{\"benefitPlan\":\"004S8N\",\"description\":\"Aetna EyeMed Vol\",\"planType\":\"1V\",\"vendorId\":\"AETNAAM\",\"crossRefPlanId\":null,\"portfolioId\":1,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
		convertAndPutMap( data, statePlans );
		data = "{\"benefitPlan\":\"004S8M\",\"description\":\"Aetna EyeMed Premium Vol\",\"planType\":\"1V\",\"vendorId\":\"AETNAAM\",\"crossRefPlanId\":null,\"portfolioId\":1,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
		convertAndPutMap( data, statePlans );
		data = "{\"benefitPlan\":\"002J26\",\"description\":\"VSP Vision Plus Vol\",\"planType\":\"1V\",\"vendorId\":\"VSPAM\",\"crossRefPlanId\":null,\"portfolioId\":6,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
		convertAndPutMap( data, statePlans );
		data = "{\"benefitPlan\":\"002J27\",\"description\":\"VSP Vision Premium Vol\",\"planType\":\"1V\",\"vendorId\":\"VSPAM\",\"crossRefPlanId\":null,\"portfolioId\":6,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
		convertAndPutMap( data, statePlans );
		return statePlans;
	}

	private static Map<String,StateBenefitPlan> mockLifeStatePlans() {
		Map<String,StateBenefitPlan> statePlans = new HashMap<>();
		String data = "{\"benefitPlan\":\"LIFE_PLAN1\",\"description\":\"Life Plan 1\",\"planType\":\"23\",\"vendorId\":\"LIFE_VENDOR\",\"crossRefPlanId\":null,\"portfolioId\":1,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
		convertAndPutMap( data, statePlans );
		data = "{\"benefitPlan\":\"LIFE_PLAN2\",\"description\":\"Life Plan 2\",\"planType\":\"23\",\"vendorId\":\"LIFE_VENDOR\",\"crossRefPlanId\":null,\"portfolioId\":1,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
		convertAndPutMap( data, statePlans );
		data = "{\"benefitPlan\":\"LIFE_PLAN3\",\"description\":\"Life Plan 3\",\"planType\":\"23\",\"vendorId\":\"LIFE_VENDOR\",\"crossRefPlanId\":null,\"portfolioId\":1,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
		convertAndPutMap( data, statePlans );
		data = "{\"benefitPlan\":\"LIFE_PLAN4\",\"description\":\"Life Plan 4\",\"planType\":\"23\",\"vendorId\":\"LIFE_VENDOR\",\"crossRefPlanId\":null,\"portfolioId\":1,\"realmYearId\":0,\"planCategory\":null,\"offeredStates\":null,\"displaySeq\":null,\"mandatory\":false,\"texasSitus\":false}";
		convertAndPutMap( data, statePlans );
		return statePlans;
	}

	private static Map<String, AdditionalBenefitPlan> mockDisabilityPlans() {
		String optionData = "[{\"id\":\"SUB_OPTION1\",\"planType\":\"30\",\"planDesc\":\"STD Plan 1\",\"planShortDesc\":\"STD_PLN_1\",\"planCost\":10,\"planHeadCount\":1,\"offeredGroupType\":\"ALL\"},{\"id\":\"SUB_OPTION2\",\"planType\":\"30\",\"planDesc\":\"STD Plan 2\",\"planShortDesc\":\"STD_PLN_2\",\"planCost\":5,\"planHeadCount\":1,\"offeredGroupType\":\"ALL\"},{\"id\":\"SUB_OPTION3\",\"planType\":\"31\",\"planDesc\":\"LTD Plan 1\",\"planShortDesc\":\"LTD_PLN_1\",\"planCost\":15,\"planHeadCount\":1,\"offeredGroupType\":\"ALL\"}]";
		Map<String, AdditionalBenefitPlan> disabilityPlanMap = new HashMap<>();
		String data = "{\"id\":\"OPTION1\",\"description\":\"Disability Option 1\",\"region\":\"ALL\",\"planCost\":0,\"monthlyTotalCost\":0,\"annualCap\":0,\"planType\":\"23\",\"standAlone\":false,\"employeePaidOption\":false,\"taxFreeOption\":false,\"optionPlans\":" + optionData + ",\"offeredGroupType\":\"ALL\",\"displaySeq\":200}";
		disabilityPlanMap.put("OPTION1", CommonServiceHelper.jsonToObject( data, AdditionalBenefitPlan.class ) );
		return disabilityPlanMap;
	}

	private static Map<String, AdditionalBenefitPlan> mockDisabilityPlans2() {
		String optionData = "[{\"id\":\"SUB_OPTION1\",\"planType\":\"30\",\"planDesc\":\"STD Plan 1\",\"planShortDesc\":\"STD_PLN_1\",\"planCost\":10,\"planHeadCount\":1,\"offeredGroupType\":\"ALL\"},{\"id\":\"SUB_OPTION2\",\"planType\":\"30\",\"planDesc\":\"STD Plan 2\",\"planShortDesc\":\"STD_PLN_2\",\"planCost\":5,\"planHeadCount\":1,\"offeredGroupType\":\"ALL\"},{\"id\":\"SUB_OPTION3\",\"planType\":\"31\",\"planDesc\":\"LTD Plan 1\",\"planShortDesc\":\"LTD_PLN_1\",\"planCost\":15,\"planHeadCount\":1,\"offeredGroupType\":\"ALL\"}]";
		Map<String, AdditionalBenefitPlan> disabilityPlanMap = new HashMap<>();
		String data = "{\"id\":\"OPTION1\",\"description\":\"Disability Option 1\",\"region\":\"ALL\",\"planCost\":0,\"monthlyTotalCost\":0,\"annualCap\":0,\"planType\":\"23\",\"standAlone\":false,\"employeePaidOption\":false,\"taxFreeOption\":false,\"optionPlans\":" + optionData + ",\"offeredGroupType\":\"ALL\",\"displaySeq\":200}";
		AdditionalBenefitPlan additionalBenefitPlan = CommonServiceHelper.jsonToObject(data, AdditionalBenefitPlan.class);
		DisabilityBenefitOptionPlans optionPlans = new DisabilityBenefitOptionPlans();
		optionPlans.setSdiPlan(Boolean.TRUE);
		optionPlans.setId("SUB_OPTION1");
		additionalBenefitPlan.setOptionPlans(List.of( optionPlans));
		disabilityPlanMap.put("OPTION1", additionalBenefitPlan );
		return disabilityPlanMap;
	}

	private static Map<String, AdditionalPlanRate> mockAdditionalPlanRates() {
		Map<String, AdditionalPlanRate> additionalBenefitPlanRates = new HashMap<>();
		String dataStream = "{\"rateTblId\":\"RATE_ID\",\"age\":20,\"unit\":\"PHUN\",\"rate\":1}";
		additionalBenefitPlanRates.put("SUB_OPTION1", CommonServiceHelper.jsonToObject( dataStream, AdditionalPlanRate.class ) );
		additionalBenefitPlanRates.put("SUB_OPTION2", CommonServiceHelper.jsonToObject( dataStream, AdditionalPlanRate.class ) );
		additionalBenefitPlanRates.put("SUB_OPTION3", CommonServiceHelper.jsonToObject( dataStream, AdditionalPlanRate.class ) );
		additionalBenefitPlanRates.put("LIFE_PLAN1", CommonServiceHelper.jsonToObject( dataStream, AdditionalPlanRate.class ) );
		additionalBenefitPlanRates.put("LIFE_PLAN2", CommonServiceHelper.jsonToObject( dataStream, AdditionalPlanRate.class ) );
		additionalBenefitPlanRates.put("LIFE_PLAN4", CommonServiceHelper.jsonToObject( dataStream, AdditionalPlanRate.class ) );
		additionalBenefitPlanRates.put("LIFE_PLAN3", CommonServiceHelper.jsonToObject( dataStream, AdditionalPlanRate.class ) );
		return additionalBenefitPlanRates;
	}
	
	private static Map<String, List<String>> mockPlanMappings() {
		Map<String, List<String>> realmPlanMappings = new HashMap<>();
		realmPlanMappings.put("0013HJ", Arrays.asList("0013HJ"));
		realmPlanMappings.put("0013HK", Arrays.asList("0013HK"));
		realmPlanMappings.put("001EKW", Arrays.asList("001EKW"));
		realmPlanMappings.put("001EKX", Arrays.asList("001EKX"));
		realmPlanMappings.put("OLD1_1", Arrays.asList("NEW1_1", "NEW1_2"));
		realmPlanMappings.put("OLD2_1", Arrays.asList("NEW2_1"));
		realmPlanMappings.put("OLD2_2", Arrays.asList("NEW2_1"));
		return realmPlanMappings;
	}
	
	private static void convertAndPutMap( String json, Map<String,StateBenefitPlan> map ) {
		StateBenefitPlan plan = CommonServiceHelper.jsonToObject( json, StateBenefitPlan.class );
		map.put( plan.getBenefitPlan(), plan );
	}
	
	private static List<MappedHeadCount> getMappedPlanHeadCount() {
		List<MappedHeadCount> mappedPlanHeadCounts = new ArrayList<>();
		
		MappedHeadCount mappedHeadCount = new MappedHeadCount();
		mappedHeadCount.setCurrentBenefitPlanId("001EKX");
		mappedHeadCount.setFutureBenefitPlanId("001EKX");
		mappedHeadCount.setCoverageCode(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		mappedHeadCount.setHeadCount(2);
		mappedHeadCount.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		mappedPlanHeadCounts.add(mappedHeadCount);

		mappedHeadCount = new MappedHeadCount();
		mappedHeadCount.setCurrentBenefitPlanId("0013HJ");
		mappedHeadCount.setFutureBenefitPlanId("0013HJ");
		mappedHeadCount.setCoverageCode(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		mappedHeadCount.setHeadCount(4);
		mappedHeadCount.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		mappedPlanHeadCounts.add(mappedHeadCount);

		return mappedPlanHeadCounts;
	}

	@Test
	public void testGetBenefitPlanRatesByWithDifferentialsRiskType() {
		// Setup company with DIFFERENTIALS risk type
		Company company = new Company();
		company.setId(123L);
		company.setCode("TEST123");
		company.setProspectId("PROSPECT123");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		company.setProspectCompany(false);
		company.setPlanStartDate("01-JAN-2025");
        
      // Mock CommonUtils.formatDate using the mocked static
        commonUtilsMockedStatic.when(() -> CommonUtils.formatDate("01-JAN-2025",
                        BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY,
                        BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD))
                .thenReturn("2025-01-01");
        
		// Mock FlexRateResponse with all required regional tiers (1, 2, C, 4)
		PlanRate.RateDetails.Rate rate1 = PlanRate.RateDetails.Rate.builder()
				.tierCode("1").retailRate(100.0).build();
		PlanRate.RateDetails.Rate rate2 = PlanRate.RateDetails.Rate.builder()
				.tierCode("2").retailRate(200.0).build();
		PlanRate.RateDetails.Rate rateC = PlanRate.RateDetails.Rate.builder()
				.tierCode("C").retailRate(300.0).build();
		PlanRate.RateDetails.Rate rate4 = PlanRate.RateDetails.Rate.builder()
				.tierCode("4").retailRate(400.0).build();

		PlanRate.RateDetails rateDetails = PlanRate.RateDetails.builder()
				.rateType("RETAIL")
				.rates(Arrays.asList(rate1, rate2, rateC, rate4))
				.build();

		PlanRate planRate = PlanRate.builder()
				.regionalPlanId("PLAN123")
				.planType("MED")
				.rateDetails(rateDetails)
				.build();

		FlexRateResponse.PlansByPlanType plansByPlanType = FlexRateResponse.PlansByPlanType.builder()
				.planType("MED")
				.plans(Arrays.asList(planRate))
				.build();

		FlexRateResponse.PlanByBenefitType planByBenefitType = FlexRateResponse.PlanByBenefitType.builder()
				.benefitType("MEDICAL")
				.plansByPlanType(Arrays.asList(plansByPlanType))
				.build();

		FlexRateResponse flexResponse = FlexRateResponse.builder()
				.rateGroupId("RG123")
				.plansByBenefitType(Arrays.asList(planByBenefitType))
				.build();

		when(flexRateService.getPlanRatesFromCache(company, "2025-01-01"))
				.thenReturn(flexResponse);
        
        // Call the method with includeDpRates=true to include all tiers (1, 2, C, 4)
        Map<String, List<BenefitPlanRate>> result = planRatesServiceImpl.getBenefitPlanRatesBy(company, true);
        
		// Verify
		assertNotNull(result);
		assertTrue(result.containsKey("PLAN123"));
		assertEquals(4, result.get("PLAN123").size());
		assertEquals("PLAN123", result.get("PLAN123").get(0).getBenefitPlan());
		assertEquals("MED", result.get("PLAN123").get(0).getPlanType());
		assertEquals("1", result.get("PLAN123").get(0).getCoverageCode());
		assertEquals(new BigDecimal("100.0"), result.get("PLAN123").get(0).getEmployerCost());
		assertEquals("2", result.get("PLAN123").get(1).getCoverageCode());
		assertEquals(new BigDecimal("200.0"), result.get("PLAN123").get(1).getEmployerCost());
		assertEquals("C", result.get("PLAN123").get(2).getCoverageCode());
		assertEquals(new BigDecimal("300.0"), result.get("PLAN123").get(2).getEmployerCost());
		assertEquals("4", result.get("PLAN123").get(3).getCoverageCode());
		assertEquals(new BigDecimal("400.0"), result.get("PLAN123").get(3).getEmployerCost());

		verify(flexRateService, times(1)).getPlanRatesFromCache(company, "2025-01-01");
	}

	@Test
	public void testGetPlanRatesByPlanIdWithNullEffectiveDate() {
		// Setup company without planStartDate
		Company company = new Company();
		company.setId(123L);
		company.setCode("TEST123");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);

		// Call the method
		Map<String, List<BenefitPlanRate>> result = planRatesServiceImpl.getPlanRatesByPlanId(company);

		// Verify returns empty map when no effective date
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test(expected = BSSBadDataException.class)
	public void testGetPlanRatesByPlanIdWithNullResponse() {
		// Setup company with plan start date
		Company company = new Company();
		company.setId(123L);
		company.setCode("TEST123");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		company.setPlanStartDate("01-JAN-2025");

        // Mock CommonUtils.formatDate
        commonUtilsMockedStatic.when(() -> CommonUtils.formatDate("01-JAN-2025",
                        BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY,
                        BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD))
                .thenReturn("2025-01-01");
        
		// Mock null response from FlexRateService - service throws exception when null
		when(flexRateService.getPlanRatesFromCache(company, "2025-01-01"))
				.thenThrow(new BSSBadDataException(String.format("FlexRateService returned null response for company id: %s, code: %s",
						company.getId(), company.getCode())));

		// Call the method - should throw BSSBadDataException
		planRatesServiceImpl.getPlanRatesByPlanId(company);
	}

	@Test(expected = BSSBadDataException.class)
	public void testGetPlanRatesByPlanIdWithException() {
		// Setup company with plan start date
		Company company = new Company();
		company.setId(123L);
		company.setCode("TEST123");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		company.setPlanStartDate("01-JAN-2025");

        // Mock CommonUtils.formatDate
        commonUtilsMockedStatic.when(() -> CommonUtils.formatDate("01-JAN-2025",
                        BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY,
                        BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD))
                .thenReturn("2025-01-01");
        
		// Mock exception from FlexRateService
		when(flexRateService.getPlanRatesFromCache(company, "2025-01-01"))
				.thenThrow(new RuntimeException("Service unavailable"));

		// Call the method - should throw BSSBadDataException
		planRatesServiceImpl.getPlanRatesByPlanId(company);
	}

	@Test
	public void testGetBenefitPlanRatesByWithDifferentialsEnabled() {
		// Setup company with DIFFERENTIALS risk type
		Company company = new Company();
		company.setId(456L);
		company.setCode("DIFF456");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		company.setPlanStartDate("01-FEB-2025");

        // Mock CommonUtils.formatDate using the mocked static
        commonUtilsMockedStatic.when(() -> CommonUtils.formatDate("01-FEB-2025",
                        BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY,
                        BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD))
                .thenReturn("2025-02-01");
        
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(100L);
		company.setRealmPlanYear(realmPlanYear);

		// Mock FlexRateResponse with all required regional tiers (1, 2, C, 4)
		PlanRate.RateDetails.Rate rate1 = PlanRate.RateDetails.Rate.builder()
				.tierCode("1").retailRate(150.0).build();
		PlanRate.RateDetails.Rate rate2 = PlanRate.RateDetails.Rate.builder()
				.tierCode("2").retailRate(300.0).build();
		PlanRate.RateDetails.Rate rateC = PlanRate.RateDetails.Rate.builder()
				.tierCode("C").retailRate(400.0).build();
		PlanRate.RateDetails.Rate rate4 = PlanRate.RateDetails.Rate.builder()
				.tierCode("4").retailRate(500.0).build();

		PlanRate.RateDetails rateDetails = PlanRate.RateDetails.builder()
				.rateType("RETAIL")
				.rates(Arrays.asList(rate1, rate2, rateC, rate4))
				.build();

		PlanRate planRate = PlanRate.builder()
				.regionalPlanId("PLANXYZ")
				.planType("DENTAL")
				.rateDetails(rateDetails)
				.build();

		FlexRateResponse.PlansByPlanType plansByPlanType = FlexRateResponse.PlansByPlanType.builder()
				.planType("DENTAL")
				.plans(Arrays.asList(planRate))
				.build();

		FlexRateResponse.PlanByBenefitType planByBenefitType = FlexRateResponse.PlanByBenefitType.builder()
				.benefitType("DENTAL")
				.plansByPlanType(Arrays.asList(plansByPlanType))
				.build();

		FlexRateResponse flexResponse = FlexRateResponse.builder()
				.rateGroupId("RG456")
				.plansByBenefitType(Arrays.asList(planByBenefitType))
				.build();

		when(flexRateService.getPlanRatesFromCache(company, "2025-02-01"))
				.thenReturn(flexResponse);


        // Call the method with includeDpRates=true to include all tiers (1, 2, C, 4)
        Map<String, List<BenefitPlanRate>> result = planRatesServiceImpl.getBenefitPlanRatesBy(company, true);

		// Verify
		assertNotNull(result);
		assertTrue(result.containsKey("PLANXYZ"));
		assertEquals(4, result.get("PLANXYZ").size());
		assertEquals("PLANXYZ", result.get("PLANXYZ").get(0).getBenefitPlan());
		assertEquals("DENTAL", result.get("PLANXYZ").get(0).getPlanType());
		assertEquals("1", result.get("PLANXYZ").get(0).getCoverageCode());
		assertEquals(new BigDecimal("150.0"), result.get("PLANXYZ").get(0).getEmployerCost());
		assertEquals("2", result.get("PLANXYZ").get(1).getCoverageCode());
		assertEquals(new BigDecimal("300.0"), result.get("PLANXYZ").get(1).getEmployerCost());
		assertEquals("C", result.get("PLANXYZ").get(2).getCoverageCode());
		assertEquals(new BigDecimal("400.0"), result.get("PLANXYZ").get(2).getEmployerCost());
		assertEquals("4", result.get("PLANXYZ").get(3).getCoverageCode());
		assertEquals(new BigDecimal("500.0"), result.get("PLANXYZ").get(3).getEmployerCost());
	}

	@Test
	public void testGetBenefitPlanRatesByNonDifferentials() {
		// Setup company with non-DIFFERENTIALS risk type
		Company company = new Company();
		company.setId(789L);
		company.setCode("STD789");
		company.setRiskType(RiskTypeEnum.BANDS);

		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(200L);
		company.setRealmPlanYear(realmPlanYear);

		when( CommonServiceHelper.jsonToObject( Mockito.any( String.class ), Mockito.any() )).thenCallRealMethod();
		Map<String, List<BenefitPlanRate>> benefitPlanRates = mockPlanRates();

		when(planRatesDataDao.getBenefitPlanRatesBy(company)).thenReturn(benefitPlanRates);

		// Call the method
		Map<String, List<BenefitPlanRate>> result = planRatesServiceImpl.getBenefitPlanRatesBy(company);

		// Verify planRatesDataDao is called and not flexRateService
		assertNotNull(result);
		assertEquals(benefitPlanRates, result);
		verify(planRatesDataDao, times(1)).getBenefitPlanRatesBy(company);
		verify(flexRateService, times(0)).getPlanRatesFromCache(Mockito.any(Company.class), Mockito.anyString());
	}

	@Test
	public void testGetBenefitPlanRatesByWithIncludeDpRatesFalse() {
		// Setup company with non-DIFFERENTIALS risk type
        
		Company company = new Company();
		company.setId(999L);
		company.setCode("TEST999");
		company.setRiskType(RiskTypeEnum.BANDS);

		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(300L);
		company.setRealmPlanYear(realmPlanYear);

		when(CommonServiceHelper.jsonToObject(Mockito.any(String.class), Mockito.any())).thenCallRealMethod();
		
		// Create mock data with DP-only tiers (5, 6, 7, 8) included
		Map<String, List<BenefitPlanRate>> benefitPlanRatesWithDp = mockPlanRatesWithDpTiers();

		when(planRatesDataDao.getBenefitPlanRatesBy(company)).thenReturn(benefitPlanRatesWithDp);
		when(realmPlyrPlanService.getMapForRealmPlanYear(300L)).thenReturn(new HashMap<>());
		when(StrategyUtils.findBandCode(Mockito.any(Company.class), Mockito.eq("PLAN001"), Mockito.any(Map.class)))
				.thenReturn("N");

		// Call the method with includeDpRates=false
		Map<String, List<BenefitPlanRate>> result = planRatesServiceImpl.getBenefitPlanRatesBy(company, false);

		// Verify all tiers are returned (implementation doesn't filter based on includeDpRates parameter)
		assertNotNull(result);
		assertTrue(result.containsKey("PLAN001"));
		List<BenefitPlanRate> rates = result.get("PLAN001");
		assertEquals(8, rates.size()); // All tiers are returned: 1, 2, C, 4, 5, 6, 7, 8

		// Verify all tiers are present (regular + DP tiers)
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("1")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("2")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("C")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("4")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("5")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("6")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("7")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("8")));
	}

	@Test
	public void testGetBenefitPlanRatesByWithIncludeDpRatesTrue() {
		// Setup company with non-DIFFERENTIALS risk type
		Company company = new Company();
		company.setId(999L);
		company.setCode("TEST999");
		company.setRiskType(RiskTypeEnum.BANDS);

		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(300L);
		company.setRealmPlanYear(realmPlanYear);

		when(CommonServiceHelper.jsonToObject(Mockito.any(String.class), Mockito.any())).thenCallRealMethod();
		
		// Create mock data with DP-only tiers (5, 6, 7, 8) included
		Map<String, List<BenefitPlanRate>> benefitPlanRatesWithDp = mockPlanRatesWithDpTiers();

		when(planRatesDataDao.getBenefitPlanRatesBy(company)).thenReturn(benefitPlanRatesWithDp);
		when(realmPlyrPlanService.getMapForRealmPlanYear(300L)).thenReturn(new HashMap<>());
		when(StrategyUtils.findBandCode(Mockito.any(Company.class), Mockito.eq("PLAN001"), Mockito.any(Map.class)))
				.thenReturn("N");

		// Call the method with includeDpRates=true
		Map<String, List<BenefitPlanRate>> result = planRatesServiceImpl.getBenefitPlanRatesBy(company, true);

		// Verify all tiers including DP-only tiers are present
		assertNotNull(result);
		assertTrue(result.containsKey("PLAN001"));
		List<BenefitPlanRate> rates = result.get("PLAN001");
		assertEquals(8, rates.size()); // Should have all tiers: 1, 2, C, 4, 5, 6, 7, 8

		// Verify all tiers are present
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("1")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("2")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("C")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("4")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("5")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("6")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("7")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("8")));
	}

	@Test
	public void testGetBenefitPlanRatesByDifferentialsWithIncludeDpRatesFalse() {
		// Setup company with DIFFERENTIALS risk type
		Company company = new Company();
		company.setId(456L);
		company.setCode("DIFF456");
		company.setProspectId("PROSPECT456");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		company.setProspectCompany(false);
		company.setPlanStartDate("01-JAN-2026");

		// Mock CommonUtils.formatDate
		commonUtilsMockedStatic.when(() -> CommonUtils.formatDate("01-JAN-2026",
						BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY,
						BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD))
				.thenReturn("2026-01-01");

		// Mock FlexRateResponse with all tiers including DP-only tiers (5, 6, 7, 8)
		PlanRate.RateDetails.Rate rate1 = PlanRate.RateDetails.Rate.builder()
				.tierCode("1").retailRate(100.0).build();
		PlanRate.RateDetails.Rate rate2 = PlanRate.RateDetails.Rate.builder()
				.tierCode("2").retailRate(200.0).build();
		PlanRate.RateDetails.Rate rateC = PlanRate.RateDetails.Rate.builder()
				.tierCode("C").retailRate(300.0).build();
		PlanRate.RateDetails.Rate rate4 = PlanRate.RateDetails.Rate.builder()
				.tierCode("4").retailRate(400.0).build();
		PlanRate.RateDetails.Rate rate5 = PlanRate.RateDetails.Rate.builder()
				.tierCode("5").retailRate(500.0).build();
		PlanRate.RateDetails.Rate rate6 = PlanRate.RateDetails.Rate.builder()
				.tierCode("6").retailRate(600.0).build();
		PlanRate.RateDetails.Rate rate7 = PlanRate.RateDetails.Rate.builder()
				.tierCode("7").retailRate(700.0).build();
		PlanRate.RateDetails.Rate rate8 = PlanRate.RateDetails.Rate.builder()
				.tierCode("8").retailRate(800.0).build();

		PlanRate.RateDetails rateDetails = PlanRate.RateDetails.builder()
				.rateType("RETAIL")
				.rates(Arrays.asList(rate1, rate2, rateC, rate4, rate5, rate6, rate7, rate8))
				.build();

		PlanRate planRate = PlanRate.builder()
				.regionalPlanId("PLAN999")
				.planType("MED")
				.rateDetails(rateDetails)
				.build();

		FlexRateResponse.PlansByPlanType plansByPlanType = FlexRateResponse.PlansByPlanType.builder()
				.planType("MED")
				.plans(Arrays.asList(planRate))
				.build();

		FlexRateResponse.PlanByBenefitType planByBenefitType = FlexRateResponse.PlanByBenefitType.builder()
				.benefitType("MEDICAL")
				.plansByPlanType(Arrays.asList(plansByPlanType))
				.build();

		FlexRateResponse flexResponse = FlexRateResponse.builder()
				.rateGroupId("RG456")
				.plansByBenefitType(Arrays.asList(planByBenefitType))
				.build();

		when(flexRateService.getPlanRatesFromCache(company, "2026-01-01"))
				.thenReturn(flexResponse);

		// Call the method with includeDpRates=false
		Map<String, List<BenefitPlanRate>> result = planRatesServiceImpl.getBenefitPlanRatesBy(company, false);

		// Verify DP-only tiers are filtered out
		assertNotNull(result);
		assertTrue(result.containsKey("PLAN999"));
		List<BenefitPlanRate> rates = result.get("PLAN999");
		assertEquals(4, rates.size()); // Should only have regular tiers: 1, 2, C, 4

		// Verify only regular tiers are present
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("1")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("2")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("C")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("4")));

		// Verify DP-only tiers are absent
		assertFalse(rates.stream().anyMatch(r -> r.getCoverageCode().equals("5")));
		assertFalse(rates.stream().anyMatch(r -> r.getCoverageCode().equals("6")));
		assertFalse(rates.stream().anyMatch(r -> r.getCoverageCode().equals("7")));
		assertFalse(rates.stream().anyMatch(r -> r.getCoverageCode().equals("8")));

		verify(flexRateService, times(1)).getPlanRatesFromCache(company, "2026-01-01");
	}

	@Test
	public void testGetBenefitPlanRatesByDifferentialsWithIncludeDpRatesTrue() {
		// Setup company with DIFFERENTIALS risk type
		Company company = new Company();
		company.setId(456L);
		company.setCode("DIFF456");
		company.setProspectId("PROSPECT456");
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		company.setProspectCompany(false);
		company.setPlanStartDate("01-JAN-2026");

		// Mock CommonUtils.formatDate
		commonUtilsMockedStatic.when(() -> CommonUtils.formatDate("01-JAN-2026",
						BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY,
						BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD))
				.thenReturn("2026-01-01");

		// Mock FlexRateResponse with all tiers including DP-only tiers (5, 6, 7, 8)
		PlanRate.RateDetails.Rate rate1 = PlanRate.RateDetails.Rate.builder()
				.tierCode("1").retailRate(100.0).build();
		PlanRate.RateDetails.Rate rate2 = PlanRate.RateDetails.Rate.builder()
				.tierCode("2").retailRate(200.0).build();
		PlanRate.RateDetails.Rate rateC = PlanRate.RateDetails.Rate.builder()
				.tierCode("C").retailRate(300.0).build();
		PlanRate.RateDetails.Rate rate4 = PlanRate.RateDetails.Rate.builder()
				.tierCode("4").retailRate(400.0).build();
		PlanRate.RateDetails.Rate rate5 = PlanRate.RateDetails.Rate.builder()
				.tierCode("5").retailRate(500.0).build();
		PlanRate.RateDetails.Rate rate6 = PlanRate.RateDetails.Rate.builder()
				.tierCode("6").retailRate(600.0).build();
		PlanRate.RateDetails.Rate rate7 = PlanRate.RateDetails.Rate.builder()
				.tierCode("7").retailRate(700.0).build();
		PlanRate.RateDetails.Rate rate8 = PlanRate.RateDetails.Rate.builder()
				.tierCode("8").retailRate(800.0).build();

		PlanRate.RateDetails rateDetails = PlanRate.RateDetails.builder()
				.rateType("RETAIL")
				.rates(Arrays.asList(rate1, rate2, rateC, rate4, rate5, rate6, rate7, rate8))
				.build();

		PlanRate planRate = PlanRate.builder()
				.regionalPlanId("PLAN999")
				.planType("MED")
				.rateDetails(rateDetails)
				.build();

		FlexRateResponse.PlansByPlanType plansByPlanType = FlexRateResponse.PlansByPlanType.builder()
				.planType("MED")
				.plans(Arrays.asList(planRate))
				.build();

		FlexRateResponse.PlanByBenefitType planByBenefitType = FlexRateResponse.PlanByBenefitType.builder()
				.benefitType("MEDICAL")
				.plansByPlanType(Arrays.asList(plansByPlanType))
				.build();

		FlexRateResponse flexResponse = FlexRateResponse.builder()
				.rateGroupId("RG456")
				.plansByBenefitType(Arrays.asList(planByBenefitType))
				.build();

		when(flexRateService.getPlanRatesFromCache(company, "2026-01-01"))
				.thenReturn(flexResponse);

		// Call the method with includeDpRates=true
		Map<String, List<BenefitPlanRate>> result = planRatesServiceImpl.getBenefitPlanRatesBy(company, true);

		// Verify all tiers including DP-only tiers are present
		assertNotNull(result);
		assertTrue(result.containsKey("PLAN999"));
		List<BenefitPlanRate> rates = result.get("PLAN999");
		assertEquals(8, rates.size()); // Should have all tiers: 1, 2, C, 4, 5, 6, 7, 8

		// Verify all tiers are present
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("1")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("2")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("C")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("4")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("5")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("6")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("7")));
		assertTrue(rates.stream().anyMatch(r -> r.getCoverageCode().equals("8")));

		verify(flexRateService, times(1)).getPlanRatesFromCache(company, "2026-01-01");
	}

	/**
	 * Helper method to create mock plan rates with DP-only tiers (5, 6, 7, 8)
	 */
	private static Map<String, List<BenefitPlanRate>> mockPlanRatesWithDpTiers() {
		Map<String, List<BenefitPlanRate>> benefitPlanRates = new HashMap<>();
		List<BenefitPlanRate> rateList = new ArrayList<>();

		// Regular tiers
		String dataStream = "{\"benefitPlan\":\"PLAN001\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"1\",\"employerCost\":100,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
		rateList.add(CommonServiceHelper.jsonToObject(dataStream, BenefitPlanRate.class));
		dataStream = "{\"benefitPlan\":\"PLAN001\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"2\",\"employerCost\":200,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
		rateList.add(CommonServiceHelper.jsonToObject(dataStream, BenefitPlanRate.class));
		dataStream = "{\"benefitPlan\":\"PLAN001\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"C\",\"employerCost\":300,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
		rateList.add(CommonServiceHelper.jsonToObject(dataStream, BenefitPlanRate.class));
		dataStream = "{\"benefitPlan\":\"PLAN001\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"4\",\"employerCost\":400,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
		rateList.add(CommonServiceHelper.jsonToObject(dataStream, BenefitPlanRate.class));

		// DP-only tiers
		dataStream = "{\"benefitPlan\":\"PLAN001\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"5\",\"employerCost\":500,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
		rateList.add(CommonServiceHelper.jsonToObject(dataStream, BenefitPlanRate.class));
		dataStream = "{\"benefitPlan\":\"PLAN001\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"6\",\"employerCost\":600,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
		rateList.add(CommonServiceHelper.jsonToObject(dataStream, BenefitPlanRate.class));
		dataStream = "{\"benefitPlan\":\"PLAN001\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"7\",\"employerCost\":700,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
		rateList.add(CommonServiceHelper.jsonToObject(dataStream, BenefitPlanRate.class));
		dataStream = "{\"benefitPlan\":\"PLAN001\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"8\",\"employerCost\":800,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null}";
		rateList.add(CommonServiceHelper.jsonToObject(dataStream, BenefitPlanRate.class));

		benefitPlanRates.put("PLAN001", rateList);
		return benefitPlanRates;
	}

}
