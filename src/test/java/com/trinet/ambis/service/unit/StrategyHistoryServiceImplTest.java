package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.trinet.ambis.helper.BenefitCategoriesHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.service.BenefitClassService;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ContributionService;
import com.trinet.ambis.service.ExchangeService;
import com.trinet.ambis.service.GroupRuleService;
import com.trinet.ambis.service.HeadCountService;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.RealmPlanYearRuleService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategyHsaFundingService;
import com.trinet.ambis.service.impl.StrategyHistoryServiceImpl;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;
import com.trinet.ambis.util.RulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
public class StrategyHistoryServiceImplTest extends ServiceUnitTest {
	private static final String ACTIVE = "A";
	private static final long STRATEGY_ID = 2; 

	@InjectMocks
	StrategyHistoryServiceImpl strategyHistoryServiceImpl;

	@Mock
	RenewalDataDao renewalDataDao;

	@Mock
	CompanyService companyService;

	@Mock
	HeadCountService headCountService;

	@Mock
	RealmPlanYearRuleService realmPlanYearRuleService;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	PortfolioRuleDao portfolioRuleDao;

	@Mock
	BenefitPlanDao benefitPlanDao;

	@Mock
	StrategyDataDao strategyDataDao;

	@Mock
	StrategyDao strategyDao;

	@Mock
	BenefitGroupService benefitGroupService;

	@Mock
	BenefitClassService benefitClassService;

	@Mock
	StrategyGroupService strategyGroupService;

	@Mock
	GroupRuleService groupRuleService;

	@Mock
	BenefitOfferExceptionService benOfferExceptionService;

	@Mock
	StrategyHsaFundingService strategyHsaFundingService;

	@Mock
	PlanSelectionService planSelectionService;

	@Mock
	ContributionService contributionService;
	
	@Mock
	ExchangeService exchangeService;

    private MockedStatic<BenefitCategoriesHelper> benefitCategoriesHelperMockedStatic;
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

    @Before
    public void setUp() {
        benefitCategoriesHelperMockedStatic = Mockito.mockStatic(BenefitCategoriesHelper.class);
        rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
    }

    @After
    public void tearDown() {
        benefitCategoriesHelperMockedStatic.close();
        rulesAndConfigsUtilsMockedStatic.close();
    }
	/**
	 * This is the minimum required to run this method through a test.  Most of the "empty collection"
	 * conditions are captured by this test.
	 */
	@Test
	public void createHistoryStrategyFromPSBasicTest() {
		Company company = mockCompany();
		// Map  benefit program to benefit offer to benefit plan code to BenefitPlan
		Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap = mockHealthPlansMap();
		Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap = mockADPlansMap();

		ArgumentCaptor<Strategy> strategyArg = ArgumentCaptor.forClass( Strategy.class );
		when( strategyDao.saveAndFlush( strategyArg.capture() ) ).thenAnswer( (InvocationOnMock inv) -> {
			Strategy s = inv.getArgument( 0 );
			s.setId( STRATEGY_ID );
			return s;
		});
		when(exchangeService.isMedicalOffered(anyLong())).thenReturn(true);
		
		when( RulesAndConfigsUtils.findPickChooseWithExceptions( eq( company ) ) )
				.thenReturn( false );
		strategyHistoryServiceImpl.createHistoryStrategyFromPS( company, bgsHealthPlansMap, bgsADPlansMap );
		assertEquals( (long) STRATEGY_ID, (long) strategyArg.getValue().getId() );
	}

	/**
	 * This is a more extensive test of the method with thorough mocked data.
	 */
	@Test
	public void createHistoryStrategyFromPSTest() {
		Company company = mockCompany();
		// Map  benefit program to benefit offer to benefit plan code to BenefitPlan
		Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap = mockHealthPlansMap();
		Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap = mockADPlansMap();
		Map<String,Set<PlanCarrier>> planCarrierMap = mockPlanCarrierMap();

		when( renewalDataDao.getBenefitPrograms( any(), any() ) ).thenReturn( mockPsGroupList() );

		when( renewalDataDao.getWaitPeriodByClient( any(), any(), any() ) ).thenReturn( mockWaitPeriodMap() );

		when( portfolioRuleDao.getPortfoliosByHqRegion( anyLong(), any(), any(), any(),
				any(), anyBoolean() )).thenReturn( planCarrierMap );

		when( strategyDao.findByCompanyIdAndSubmitted( anyLong(), eq( true )) ).thenReturn( mockStrategies() );

		StrategyHsaFundingDto hsaFunding = new StrategyHsaFundingDto();
		when( renewalDataDao.getPsHsaFundingDetails( any(), any() )).thenReturn( hsaFunding );

		ArgumentCaptor<StrategyHsaFundingDto> strategyHsaFundingDtoArg = ArgumentCaptor.forClass( StrategyHsaFundingDto.class );
		when( strategyHsaFundingService.save( strategyHsaFundingDtoArg.capture() )).thenAnswer( 
				( InvocationOnMock inv ) -> inv.getArgument( 0 ) );

		when( benefitGroupService.getBenefitGroupByStrategy( anyLong(), eq( BSSApplicationConstants.STATUS_ACTIVE )) ).
				thenReturn( mockBssGroupList() );

		ArgumentCaptor<Strategy> strategyArg = ArgumentCaptor.forClass( Strategy.class );
		when( strategyDao.saveAndFlush( strategyArg.capture() ) ).thenAnswer( (InvocationOnMock inv) -> {
			Strategy s = inv.getArgument( 0 );
			s.setId( STRATEGY_ID );
			return s;
		});

		when( benefitClassService.generateAllClassCodes( any(), any() )).thenAnswer( 
				( InvocationOnMock inv ) -> inv.getArgument( 1 ) );
		when( benefitGroupService.saveAll( any() )).thenAnswer(
				( InvocationOnMock inv ) -> inv.getArgument( 0 ) );

		when( strategyGroupService.getBenefitGroupStrategy( anyLong(), eq( BSSApplicationConstants.STATUS_ACTIVE )) ).
				thenReturn( mockBenefitGroupStrategies() );
		when(exchangeService.isMedicalOffered(anyLong())).thenReturn(true);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<BenefitGroupStrategy>> benefitGroupStrategiesArg = ArgumentCaptor.forClass( List.class );
		when( strategyGroupService.saveBenefitGroupStrategies( benefitGroupStrategiesArg.capture() ) ).thenAnswer(
				( InvocationOnMock inv ) -> inv.getArgument( 0 ) );


		strategyHistoryServiceImpl.createHistoryStrategyFromPS( company, bgsHealthPlansMap, bgsADPlansMap );
		assertEquals( (long) STRATEGY_ID, (long) strategyArg.getValue().getId() );
		assertEquals( (long) STRATEGY_ID, (long) strategyHsaFundingDtoArg.getValue().getStrategyId() );
		assertEquals( 5, benefitGroupStrategiesArg.getValue().size() );
	}


	private static Company mockCompany() {
		Company company = new Company();
		company.setCode( "YYC" );
		company.setName( "The YYC Company" );
		company.setEmplId( "0001234567");
		company.setPlanStartDate( "02-FEB-2021" );
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId( 1L );
		rpy.setPlanYearStart( java.sql.Date.valueOf( "2021-01-01" ) );
		rpy.setPlanYearEnd( java.sql.Date.valueOf( "2021-12-31" ) );
		company.setRealmPlanYear( rpy );
		company.setRealmPlanYearId( rpy.getId() );
		Realm realm = new Realm();
		realm.setBenExchange( BenExchngEnums.TRINET_IV.getBenExchng() );
		company.setRealm( realm );
		return company;
	}

	private static List<Strategy> mockStrategies() {
		List<Strategy> mockedList = new ArrayList<>();
		Strategy strat = new Strategy();
		strat.setCompanyId( 1L );
		strat.setId( STRATEGY_ID );
		strat.setName( "Unit Test Strategy" );
		mockedList.add( strat );
		return mockedList;
	}

	private static Map<String,String> mockWaitPeriodMap() {
		Map<String,String> mockMap = new HashMap<>();
		for( BenefitGroup group : mockPsGroupList() ) {
			mockMap.put( group.getBenefitProgram(), "NONE" );
		}
		return mockMap;
	}

	private static List<BenefitGroup> mockPsGroupList() {
		List<BenefitGroup> mockedList = new ArrayList<>();
		mockedList.add( buildGroup( 101, "PROG01", "Part Time Pgm", "STD" ) );
		mockedList.add( buildGroup( 102, "PROG02", "Royal Pgm", "STD" ) );
		mockedList.add( buildGroup( 104, "PROG04", "Golden Pgm", "STD" ) );
		mockedList.add( buildGroup( 105, "PROG05", "Titanium Pgm", "STD" ) );
		return mockedList;
	}

	private static List<BenefitGroup> mockBssGroupList() {
		List<BenefitGroup> mockedList = new ArrayList<>();
		mockedList.add( buildGroup( 101, "PROG01", "Part Time Pgm", "STD" ) );
		mockedList.add( buildGroup( 102, "PROG02", "Royal Pgm", "STD" ) );
		mockedList.add( buildGroup( 103, "PROG03", "Government Pgm", "STD" ) );
		mockedList.add( buildGroup( 104, "PROG04", "Golden Pgm", "STD" ) );
		return mockedList;
	}

	private static BenefitGroup buildGroup( long id, String benefitProgram, String name, String type ) {
		BenefitGroup group = new BenefitGroup();
		group.setId( id );
		group.setName( name );
		group.setBenefitProgram( benefitProgram );
		group.setType( type );
		return group;
	}

	private static List<BenefitGroupStrategy> mockBenefitGroupStrategies() {
		List<BenefitGroupStrategy> mockedList = new ArrayList<>();
		for( BenefitGroup group : mockBssGroupList() ) {
			BenefitGroupStrategy bgs = new BenefitGroupStrategy();
			bgs.setStrategyId( STRATEGY_ID );
			bgs.setBenefitGroup( group );
			bgs.setStatus( ACTIVE );
			mockedList.add( bgs );
		}
		return mockedList;
	}

	private static Map<String,Map<String,Map<String,BenefitPlan>>> mockHealthPlansMap() {
		BenefitPlan bp = new BenefitPlan();
		bp.setId( "PLAN01" );
		Map<String,BenefitPlan> benefitPlanMap = new HashMap<>();
		benefitPlanMap.put( bp.getId(), bp );

		Map<String,Map<String,BenefitPlan>> benefitOfferMap = new HashMap<>();
		benefitOfferMap.put( "medical", benefitPlanMap );

		Map<String,Map<String,Map<String,BenefitPlan>>> benefitProgramMap = new HashMap<>();
		benefitProgramMap.put( "PROG01", benefitOfferMap );

		return benefitProgramMap;
	}

	private static Map<String,Map<String,Map<String,BenefitPlan>>> mockADPlansMap() {
		BenefitPlan bp = new BenefitPlan();
		bp.setId( "LIFE01" );
		Map<String,BenefitPlan> benefitPlanMap = new HashMap<>();
		benefitPlanMap.put( bp.getId(), bp );

		Map<String,Map<String,BenefitPlan>> benefitOfferMap = new HashMap<>();
		benefitOfferMap.put( "LIFE", benefitPlanMap );

		Map<String,Map<String,Map<String,BenefitPlan>>> benefitProgramMap = new HashMap<>();
		benefitProgramMap.put( "PROG01", benefitOfferMap );

		return benefitProgramMap;
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
}
