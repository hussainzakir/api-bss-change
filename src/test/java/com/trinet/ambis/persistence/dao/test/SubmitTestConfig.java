package com.trinet.ambis.persistence.dao.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;

import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.SchedMidYearFundingDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupDataDao;
import com.trinet.ambis.persistence.dao.ps.BenProgInactivateDao;
import com.trinet.ambis.persistence.dao.ps.BenefitOptionsDao;
import com.trinet.ambis.persistence.dao.ps.BenefitPlanDataDao;
import com.trinet.ambis.persistence.dao.ps.BenefitProgramDao;
import com.trinet.ambis.persistence.dao.ps.EligConfigDao;
import com.trinet.ambis.persistence.dao.ps.HSAPlansDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmCloneProgram;
import com.trinet.ambis.persistence.sp.GetNextBenefitPlan;
import com.trinet.ambis.persistence.sp.NextRateTblID;
import com.trinet.ambis.service.BandCodesService;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.BenefitPlanService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.DocManagementService;
import com.trinet.ambis.service.FlexRateService;
import com.trinet.ambis.service.HSAPlanService;
import com.trinet.ambis.service.PersonService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.RealmPlanYearRuleService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.StrategySyncService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.submit.SubmitService;


class SubmitTestConfig {
	private static final BenefitOptionsDao benefitOptionsDao = mockBenefitOptionsDao();
	private static final HrpDao hrpDao = mockHrpDao();
	private static final PsCompanyDao psCompanyDao = mockPsCompanyDao();
	private static final RealmDataDao realmDataDao = mockRealmDataDao();
	private static final StrategyGroupDataDao strategyGroupDataDao = mockStrategyGroupDataDao();
	private static final StrategyGroupService strategyGroupService = mockStrategyGroupService();

	private static BenefitOptionsDao mockBenefitOptionsDao() {
		BenefitOptionsDao dao = mock( BenefitOptionsDao.class );
		when( dao.insertOptn2( any( Company.class ), any( BenefitGroup.class ), any(), any() )).thenReturn( 1 );
		when( dao.insertOpt2A( any( Company.class ), any( BenefitGroup.class ) )).thenReturn( 1 );
		when( dao.insertOptn3( any( Company.class ), any( BenefitGroup.class ), any(), any() )).thenReturn( 10 );
		return dao;
	}

	private static HrpDao mockHrpDao() {
		HrpDao obj = mock( HrpDao.class );
		when( obj.getOLPStatus( any( Company.class ) )).thenReturn( 35 );
		return obj;
	}

	private static PsCompanyDao mockPsCompanyDao() {
		PsCompanyDao dao = mock( PsCompanyDao.class );
		return dao;
	}

	private static RealmDataDao mockRealmDataDao() {
		RealmDataDao dao = mock( RealmDataDao.class );
		RealmCloneProgram clone = new RealmCloneProgram();
		clone.setCloneProgram( "101" );
		clone.setCloneK1Program( "10K" );
		when( dao.getRealmCloneProgram( anyLong() )).thenReturn( clone );
		return dao;
	}

	private static StrategyGroupDataDao mockStrategyGroupDataDao() {
		StrategyGroupDataDao dao = mock( StrategyGroupDataDao.class );
		return dao;
	}

	private static StrategyGroupService mockStrategyGroupService() {
		StrategyGroupService svc = mock( StrategyGroupService.class );
		when( svc.getBenefitGroupStrategy( any( Long.class ), any( String.class ) )).thenReturn( null );
		return svc;
	}



	public static BenefitOptionsDao getBenefitOptionsDao() {
		return benefitOptionsDao;
	}

	public static HrpDao getHrpDao() {
		return hrpDao;
	}

	public static PsCompanyDao getPsCompanyDao() {
		return psCompanyDao;
	}

	public static RealmDataDao getRealmDataDao() {
		return realmDataDao;
	}

	public static StrategyGroupDataDao getStrategyGroupDataDao() {
		return strategyGroupDataDao;
	}

	public static StrategyGroupService getStrategyGroupService() {
		return strategyGroupService;
	}



	@Bean(name="bisSysadmEntityManagerFactory")
	public EntityManagerFactory bisSysadmEntityManagerFactory() {
		return mock(EntityManagerFactory.class);
	}

	@Bean(name="bisHrpEntityManagerFactory")
	public EntityManagerFactory bisHrpEntityManagerFactory() {
		return mock(EntityManagerFactory.class);
	}

	@Bean(name="emailGenService")
	public EmailGenService emailGenService() {
		return mock( EmailGenService.class );
	}

	@Bean(name="submitStatusService")
	public SubmitStatusService submitStatusService() {
		return mock( SubmitStatusService.class );
	}

	@Bean(name="psDao")
	public PsDao psDao() {
		return mock( PsDao.class );
	}

	@Bean(name="psCompanyDao")
	public PsCompanyDao psCompanyDao() {
		return SubmitTestConfig.psCompanyDao;
	}

	@Bean(name="companyService")
	public CompanyService companyService() {
		return mock( CompanyService.class );
	}

	@Bean(name="strategyService")
	public StrategyService strategyService() {
		return mock( StrategyService.class );
	}

	@Bean(name="realmDataDao")
	public RealmDataDao realmDataDao() {
		return SubmitTestConfig.realmDataDao;
	}

	@Bean(name="realmPlyrPlanService")
	public RealmPlyrPlanService realmPlyrPlanService() {
		return mock( RealmPlyrPlanService.class );
	}

	@Bean(name="benefitPlanDataDao")
	public BenefitPlanDataDao benefitPlanDataDao() {
		return mock( BenefitPlanDataDao.class );
	}

	@Bean(name="benefitPlanDao")
	public BenefitPlanDao benefitPlanDao() {
		return mock( BenefitPlanDao.class );
	}

	@Bean(name="benOfferExceptionService")
	public BenefitOfferExceptionService benOfferExceptionService() {
		return mock( BenefitOfferExceptionService.class );
	}

	@Bean(name="strategyGroupService")
	public StrategyGroupService strategyGroupService() {
		return SubmitTestConfig.strategyGroupService;
	}

	@Bean(name="schedMidYearFundingDao")
	public SchedMidYearFundingDao schedMidYearFundingDao() {
		return mock( SchedMidYearFundingDao.class );
	}

	@Bean(name="strategyDao")
	public StrategyDao strategyDao() {
		return mock( StrategyDao.class );
	}

	@Bean(name="strategyGroupDataDao")
	public StrategyGroupDataDao strategyGroupDataDao() {
		return SubmitTestConfig.strategyGroupDataDao;
	}

	@Bean(name="benefitProgramDao")
	public BenefitProgramDao benefitProgramDao() {
		return mock( BenefitProgramDao.class );
	}

	@Bean(name="hrpDao")
	public HrpDao hrpDao() {
		return SubmitTestConfig.hrpDao;
	}

	@Bean(name="planRatesService")
	public PlanRatesService planRatesService() {
		return mock( PlanRatesService.class );
	}

	@Bean(name="strategySyncService")
	public StrategySyncService strategySyncService() {
		return mock( StrategySyncService.class );
	}

	@Bean(name="psDataSource")
	public DataSource psDataSource() {
		return mock( DataSource.class );
	}

	@Bean(name="realmPlanYearService")
	public RealmPlanYearService realmPlanYearService() {
		return mock( RealmPlanYearService.class );
	}

	@Bean(name="realmPlanYearRuleService")
	public RealmPlanYearRuleService realmPlanYearRuleService() {
		return mock( RealmPlanYearRuleService.class );
	}

	@Bean(name="benefitOptionsDao")
	public BenefitOptionsDao benefitOptionsDao() {
		return SubmitTestConfig.benefitOptionsDao;
	}

	@Bean(name="benProgInactivateDao")
	public BenProgInactivateDao benProgInactivateDao() {
		return mock( BenProgInactivateDao.class );
	}

	@Bean(name="eligConfigDao")
	public EligConfigDao eligConfigDao() {
		return mock( EligConfigDao.class );
	}

	@Bean(name="hsaPlanService")
	public HSAPlanService hsaPlanService() {
		return mock( HSAPlanService.class );
	}

	@Bean(name="hsaPlansDao")
	public HSAPlansDao hsaPlansDao() {
		return mock( HSAPlansDao.class );
	}

	private static int rateIdSeq = 0;
	@Bean(name="nextRateTblID")
	public NextRateTblID nextRateTblID() {
		NextRateTblID sp = mock( NextRateTblID.class );
		when( sp.execute() ).thenAnswer( new Answer<String>() {
			public String answer( InvocationOnMock inv ) throws Throwable {
				return "RATE" + ++rateIdSeq;
			}
		});
		return sp;
	}

	private static int planIdSeq = 0;
	@Bean(name="spGetNextBenefitPlan")
	public GetNextBenefitPlan spGetNextBenefitPlan() {
		GetNextBenefitPlan sp = mock( GetNextBenefitPlan.class );
        lenient().when( sp.execute() ).thenAnswer( new Answer<String>() {
			public String answer( InvocationOnMock inv ) throws Throwable {
				return "PLAN" + ++planIdSeq;
			}
		});
		return sp;
	}

	@Bean(name="docManagementService")
	public DocManagementService docManagementService() {
		return mock( DocManagementService.class );
	}

	@Bean(name="personService")
	public PersonService personService() {
		return mock( PersonService.class );
	}

	@Bean(name="submitService")
	public SubmitService submitService() {
		return mock( SubmitService.class );
	}
	
	@Bean(name = "strategyDataDao")
	public StrategyDataDao strategyDataDao() {
		return mock(StrategyDataDao.class);
	}
	
	@Bean(name = "benefitPlanService")
	public BenefitPlanService benefitPlanService() {
		return mock(BenefitPlanService.class);
	}

	@Bean(name = "bandCodesService")
	public BandCodesService bandCodesService() {
		return mock(BandCodesService.class);
	}

	@Bean(name = "flexRateService")
	public FlexRateService flexRateService() {
		return mock(FlexRateService.class);
	}
}
