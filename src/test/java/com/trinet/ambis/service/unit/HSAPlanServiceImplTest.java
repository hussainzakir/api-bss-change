package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.dao.ps.HSAPlansDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.sp.GetNextBenefitPlan;
import com.trinet.ambis.service.impl.HSAPlanMapping;
import com.trinet.ambis.service.impl.HSAPlanServiceImpl;
import com.trinet.ambis.service.model.BenDefnOptnHSA;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;

@RunWith(MockitoJUnitRunner.class)
public class HSAPlanServiceImplTest {
	HSAPlanServiceImpl hsaPlanServiceImpl;
	HSAPlansDao mockHSADao;
	GetNextBenefitPlan mockStoredProc;
	EntityManager entityManager = null;
	Company comp = null;
	BenefitGroup group = null;
	HSAPlanMapping mockHSAPlanMapping = null;


	@Before
	public void setup() {
		hsaPlanServiceImpl = new HSAPlanServiceImpl();

		this.mockHSADao = mock(HSAPlansDao.class);
		when(mockHSADao.getAllHSAPlans( Mockito.anyString(), Mockito.any( java.sql.Date.class ) )).thenReturn( this.getAllHSAPlans() );
		when(mockHSADao.getActiveMedPlans( Mockito.anyString(), Mockito.any( java.sql.Date.class ) )).thenReturn( this.getActiveMedPlans() );
		when(mockHSADao.updateBenefPlanTable( Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any( java.sql.Date.class  ) )).thenReturn( 1 );
		hsaPlanServiceImpl.setHSAPlansDao( mockHSADao );

		this.mockStoredProc = mock( GetNextBenefitPlan.class );
		when( mockStoredProc.execute() ).thenReturn( "ABCDEF" );
		hsaPlanServiceImpl.setNextBenPlanSP( mockStoredProc );

		comp = new Company();
		comp.setPfClient("pfclient");
		comp.setCode("KIF");
		comp.setPlanStartDate("01-JAN-2020");
		group = new BenefitGroup();
		group.setBenefitProgram( "UV6" );
		entityManager = mock(EntityManager.class);

		mockHSAPlanMapping = mock(HSAPlanMapping.class);
		when( mockHSAPlanMapping.get( Mockito.anyString() )).thenReturn( null );
		when( mockHSAPlanMapping.put( Mockito.anyString(), Mockito.anyString() )).thenReturn( null );
		when(mockHSAPlanMapping.saveAll()).thenReturn( 1 );
		hsaPlanServiceImpl.setHSAPlanMapping( mockHSAPlanMapping );

	}

	@Test
	public void testHSAOptions() {

		StrategyHsaFundingDto hsaOptions = this.setupHsaFundingQ();
		assertEquals( true, hsaOptions.isCustomLevel() );

		hsaOptions = this.setupHsaFundingLevel6();
		assertEquals( true, hsaOptions.isCustomLevel() );

		hsaOptions.setOptionId( 1 );
		assertEquals( false, hsaOptions.isCustomLevel() );

		hsaOptions.setOptionId( null );
		assertEquals( false, hsaOptions.isCustomLevel() );

		BenDefnOptnHSA hsaOptn = new BenDefnOptnHSA();
		assertEquals( false, hsaOptn.isPlanActive() );
	}


	@Test
	public void testSetupHSALvl5() {

		StrategyHsaFundingDto hsaOptions = this.setupHsaFundingA5();
		hsaPlanServiceImpl.setupHSABenefitPlans( comp, group, hsaOptions, null );

		verify( mockHSADao, times( 1 )).updateHSAOptns( Mockito.anyList() );
		verify( mockHSADao, times( 2 )).updateBenefPlanTable( Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
				Mockito.any( java.sql.Date.class ));
		verify( mockHSADao, times( 2 )).updateFSABenefTable( Mockito.anyString(), Mockito.anyString(), Mockito.any( java.sql.Date.class ) );
		verify( mockHSADao, times( 2 )).deleteHSAContribLmt( Mockito.anyString(), Mockito.any( java.sql.Date.class ) );
		verify( mockHSADao, times( 2 )).insertHSAContribLmt( Mockito.anyString(), Mockito.anyString(), Mockito.any( BigDecimal.class ),
				Mockito.any( BigDecimal.class ), Mockito.any( java.sql.Date.class ) );
		verify( mockHSADao, times( 2 )).updateLimitIncludeTable( Mockito.anyString(), Mockito.any( java.sql.Date.class ) );
	}


	@Test
	public void testSetupHSALvl7() {

		StrategyHsaFundingDto hsaOptions = this.setupHsaFundingA7();
		hsaPlanServiceImpl.setupHSABenefitPlans( comp, group, hsaOptions, null );

		verify( mockHSADao, times( 1 )).updateHSAOptns( Mockito.anyList() );
		verify( mockHSADao, times( 2 )).updateBenefPlanTable( Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
				Mockito.any( java.sql.Date.class ));
		verify( mockHSADao, times( 2 )).updateFSABenefTable( Mockito.anyString(), Mockito.anyString(), Mockito.any( java.sql.Date.class ) );
		verify( mockHSADao, times( 2 )).deleteHSAContribLmt( Mockito.anyString(), Mockito.any( java.sql.Date.class ) );
		verify( mockHSADao, times( 2 )).insertHSAContribLmt( Mockito.anyString(), Mockito.anyString(), Mockito.any( BigDecimal.class ),
				Mockito.any( BigDecimal.class ), Mockito.any( java.sql.Date.class ) );
		verify( mockHSADao, times( 2 )).updateLimitIncludeTable( Mockito.anyString(), Mockito.any( java.sql.Date.class ) );
	}


	@Test
	public void testSetupHSALevel0() {

		StrategyHsaFundingDto hsaOptions = this.setupHsaFundingLevel0();
		hsaPlanServiceImpl.setupHSABenefitPlans( comp, group, hsaOptions, null );

		verify( mockHSADao, times( 1 )).updateHSAOptns( Mockito.anyList() );
		verify( mockHSADao, times( 0 )).updateBenefPlanTable( Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
				Mockito.any( java.sql.Date.class ));
		verify( mockHSADao, times( 0 )).updateFSABenefTable( Mockito.anyString(), Mockito.anyString(), Mockito.any( java.sql.Date.class ) );
		verify( mockHSADao, times( 0 )).deleteHSAContribLmt( Mockito.anyString(), Mockito.any( java.sql.Date.class ) );
		verify( mockHSADao, times( 0 )).insertHSAContribLmt( Mockito.anyString(), Mockito.anyString(), Mockito.any( BigDecimal.class ),
				Mockito.any( BigDecimal.class ), Mockito.any( java.sql.Date.class ) );
		verify( mockHSADao, times( 0 )).updateLimitIncludeTable( Mockito.anyString(), Mockito.any( java.sql.Date.class ) );
	}


	@Test
	public void testSetupHSALevel6() {

		StrategyHsaFundingDto hsaOptions = this.setupHsaFundingLevel6();
		hsaPlanServiceImpl.setupHSABenefitPlans( comp, group, hsaOptions, null );

		verify( mockHSADao, times( 1 )).updateHSAOptns( Mockito.anyList() );
		verify( mockHSADao, times( 0 )).updateBenefPlanTable( Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
				Mockito.any( java.sql.Date.class ));
		verify( mockHSADao, times( 0 )).updateFSABenefTable( Mockito.anyString(), Mockito.anyString(), Mockito.any( java.sql.Date.class ) );
		verify( mockHSADao, times( 0 )).deleteHSAContribLmt( Mockito.anyString(), Mockito.any( java.sql.Date.class ) );
		verify( mockHSADao, times( 0 )).insertHSAContribLmt( Mockito.anyString(), Mockito.anyString(), Mockito.any( BigDecimal.class ),
				Mockito.any( BigDecimal.class ), Mockito.any( java.sql.Date.class ) );
		verify( mockHSADao, times( 0 )).updateLimitIncludeTable( Mockito.anyString(), Mockito.any( java.sql.Date.class ) );
	}


	/*
	 * The test data below is designed to trigger two new plans in the HSAPlanService object.
	 * This can be verified in the unit test by counting the number of times each of the DAO object's
	 * methods are called to create plan data.
	 */
	private List<BenDefnOptnHSA> getAllHSAPlans() {
		List<BenDefnOptnHSA> list = new ArrayList<>();
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8275", "117", "O", "004C3F", " ", "CS5", "117", "HSATU", "N", "0200", "Q151", "10", "001302", "0", " ", "9EON" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8276", "118", "O", "004C3J", " ", "DI5", "118", "HSATU", "N", "0200", "Q151", "10", "0013G9", "0", " ", "9EON" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8192", "26", "O", "004C3K", " ", "DJ5", "26", "HSA", "N", "0200", "Q127", "10", "001GG8", "0", " ", "9EON" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8217", "51", "O", "004C3L", " ", "EW5", "51", "HSA", "N", "0200", "Q127", "10", "001GQC", "0", " ", "9EON" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8479", "129", "O", "00532O", " ", "OB0", "129", "HSAMN", "N", "0200", " ", "10", "0052YX", "0", " ", "0000" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8478", "130", "O", "00532P", " ", "OB1", "130", "HSAMN", "N", "0200", " ", "10", "0052YX", "0", " ", "0000" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8477", "131", "O", "00532Q", " ", "OB2", "131", "HSAMN", "N", "0200", " ", "10", "0052YX", "0", " ", "0000" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8476", "132", "O", "00532R", " ", "OB3", "132", "HSAMN", "N", "0200", " ", "10", "0052YX", "0", " ", "0000" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8479", "129", "O", "0053AO", " ", "OA0", "129", "HSAMN", "N", "0200", " ", "10", "0052ZX", "0", " ", "0000" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8478", "130", "O", "0053AP", " ", "OA1", "130", "HSAMN", "N", "0200", " ", "10", "0052ZX", "0", " ", "0000" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8477", "131", "O", "0053AQ", " ", "OA2", "131", "HSAMN", "N", "0200", " ", "10", "0052ZX", "0", " ", "0000" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8476", "132", "O", "0053AR", " ", "OA3", "132", "HSAMN", "N", "0200", " ", "10", "0052ZX", "0", " ", "0000" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8264", "102", "O", "004C4X", " ", "JI5", "102", "HSABS", "N", "2009", "Q142", "10", "003LDI", "0", " ", "9EON" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8263", "97", "O", "004C4Y", " ", "JJ5", "97", "HSABS", "N", "2009", "Q143", "10", "003LDJ", "0", " ", "9EON" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8265", "99", "O", "004C4Z", " ", "JK5", "99", "HSABS", "N", "2009", " ", "10", "003LDK", "0", " ", "9EON" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8267", "101", "O", "004C50", " ", "JL5", "101", "HSABS", "N", "2009", " ", "10", "003LDL", "0", " ", "9EON" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8475", "127", "O", "00532S", " ", "OC0", "127", "HSAMN", "N", "236Q", " ", "10", "0052YY", "0", " ", "0000" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8474", "122", "O", "00532T", " ", "OC1", "122", "HSAMN", "N", "236Q", " ", "10", "0052YY", "0", " ", "0000" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8473", "123", "O", "00532U", " ", "OC2", "123", "HSAMN", "N", "236Q", " ", "10", "0052YY", "0", " ", "0000" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8472", "124", "O", "00532V", " ", "OC3", "124", "HSAMN", "N", "236Q", " ", "10", "0052YY", "0", " ", "0000" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8485", "227", "O", "00533S", " ", "XC0", "227", "HSAMN", "N", "020Q", " ", "10", "0052YY", "0", " ", "0000" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8484", "222", "O", "00533T", " ", "XC1", "222", "HSAMN", "N", "020Q", " ", "10", "0052YY", "0", " ", "0000" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8483", "223", "O", "00533U", " ", "XC2", "223", "HSAMN", "N", "020Q", " ", "10", "0052YY", "0", " ", "0000" ) );
		list.add(  buildBenDefnOptnHSA( "0G6", "2020-01-01", "67", "8482", "224", "O", "00533V", " ", "XC3", "224", "HSAMN", "N", "020Q", " ", "10", "0052YY", "0", " ", "0000" ) );

		return list;
	}

	private BenDefnOptnHSA buildBenDefnOptnHSA( String benefitProgram, String effdtStr, String planType, String optionId, String displayOptSeq, 
			String optionType, String benefitPlan, String covrgCd, String optionCd, String optionLvl, String dedcd, String dfltOptionInd,
			String eligRulesId, String locationTblId, String crossPlanType, String crossBenefPlan, String coverageLimitPct, 
			String crossPlnDpndChk, String hsaPfClient) {
		BenDefnOptnHSA bdo = new BenDefnOptnHSA();
		bdo.setBenefitProgram(benefitProgram);
		bdo.setEffdt(effdtStr);
		bdo.setPlanType(planType);
		bdo.setOptionId(new BigDecimal(optionId));
		bdo.setDisplayOptSeq(new BigDecimal(displayOptSeq));
		bdo.setOptionType(optionType);
		bdo.setBenefitPlan(benefitPlan);
		bdo.setCovrgCd(covrgCd);
		bdo.setOptionCd(optionCd);
		bdo.setOptionLvl(new BigDecimal(optionLvl));
		bdo.setDedcd(dedcd);
		bdo.setDfltOptionInd(dfltOptionInd);
		bdo.setEligRulesId(eligRulesId);
		bdo.setLocationTblId(locationTblId);
		bdo.setCrossPlanType(crossPlanType);
		bdo.setCrossBenefPlan(crossBenefPlan);
		bdo.setCoverageLimitPct(new BigDecimal(coverageLimitPct));
		bdo.setCrossPlnDpndChk(crossPlnDpndChk);
		bdo.setPfClient(hsaPfClient);
		return bdo;
	}


	private Map<String,String> getActiveMedPlans() {
		Map<String,String> medPlans = new HashMap<>();
		medPlans.put( "001302", "0200" );
		medPlans.put( "0013G9", "0201" );
		medPlans.put( "001GG8", "0202" );
		medPlans.put( "001GQC", "0203" );
		medPlans.put( "003LJU", "0204" );
		medPlans.put( "0012WO", "0205" );
		medPlans.put( "0012WQ", "0206" );
		medPlans.put( "0052ZX", "0207" );	// triggers a new plan creation
		medPlans.put( "0012WS", "0208" );
		medPlans.put( "0012WX", "0209" );
		medPlans.put( "0052YX", "020A" );	// triggers a new plan creation
		medPlans.put( "0012WT", "020B" );
		medPlans.put( "002LV0", "020C" );
		return medPlans;
	}


	private StrategyHsaFundingDto setupHsaFundingA5() {
		StrategyHsaFundingDto hsa = new StrategyHsaFundingDto();
		hsa.setStrategyId( 123550L );
		hsa.setOptionId( 5 );
		hsa = this.completeAnnual( hsa );
		return hsa;
	}

	private StrategyHsaFundingDto setupHsaFundingA7() {
		StrategyHsaFundingDto hsa = new StrategyHsaFundingDto();
		hsa.setStrategyId( 123770L );
		hsa.setOptionId( 7 );
		hsa = this.completeAnnual( hsa );
		return hsa;
	}

	private StrategyHsaFundingDto completeAnnual( StrategyHsaFundingDto hsa ) {
		hsa.setLumpSumFrequency( "A" );
		hsa.setAnnualMonth( 3 );
		hsa.setAnnualEeAmount( new BigDecimal( "1001" ) );
		hsa.setAnnualFamilyAmount( new BigDecimal( "2001" ) );
		hsa.setQ1Month( null );
		hsa.setQ2Month( null );
		hsa.setQ3Month( null );
		hsa.setQ4Month( null );
		hsa.setQuarterlyEeAmount( null );
		hsa.setQuarterlyFamilyAmount( null );
		hsa.setContributionFrequency( "M" );
		hsa.setMonthlyEeAmount( new BigDecimal( "21" ) );
		hsa.setMonthlyFamilyAmount( new BigDecimal( "41" ) );
		return hsa;
	}

	private StrategyHsaFundingDto setupHsaFundingQ() {
		StrategyHsaFundingDto hsa = new StrategyHsaFundingDto();

		hsa.setStrategyId( 113550L );
		hsa.setOptionId( 5 );

		hsa.setLumpSumFrequency( "Q" );
		hsa.setAnnualMonth( null );
		hsa.setAnnualEeAmount( null );
		hsa.setAnnualFamilyAmount( null );

		hsa.setQ1Month( 2 );
		hsa.setQ2Month( 5 );
		hsa.setQ3Month( 8 );
		hsa.setQ4Month( 11 );
		hsa.setQuarterlyEeAmount( new BigDecimal( "201" ) );
		hsa.setQuarterlyFamilyAmount( new BigDecimal( "301" ) );

		hsa.setContributionFrequency( "M" );
		hsa.setMonthlyEeAmount( new BigDecimal( "20" ) );
		hsa.setMonthlyFamilyAmount( new BigDecimal( "30" ) );

		return hsa;
	}

	private StrategyHsaFundingDto setupHsaFundingLevel6() {
		StrategyHsaFundingDto hsa = new StrategyHsaFundingDto();

		hsa.setStrategyId( 113660L );
		hsa.setOptionId( 6 );

		hsa.setLumpSumFrequency( "Q" );
		hsa.setAnnualMonth( null );
		hsa.setAnnualEeAmount( null );
		hsa.setAnnualFamilyAmount( null );
		hsa.setQ1Month( 2 );
		hsa.setQ2Month( 5 );
		hsa.setQ3Month( 8 );
		hsa.setQ4Month( 11 );
		hsa.setQuarterlyEeAmount( new BigDecimal( "201" ) );
		hsa.setQuarterlyFamilyAmount( new BigDecimal( "301" ) );
		hsa.setContributionFrequency( null );
		hsa.setMonthlyEeAmount( null );
		hsa.setMonthlyFamilyAmount( null );

		return hsa;
	}

	private StrategyHsaFundingDto setupHsaFundingLevel0() {
		StrategyHsaFundingDto hsa = new StrategyHsaFundingDto();

		hsa.setStrategyId( 113000L );
		hsa.setOptionId( 0 );

		hsa.setLumpSumFrequency( null );
		hsa.setAnnualMonth( null );
		hsa.setAnnualEeAmount( null );
		hsa.setAnnualFamilyAmount( null );
		hsa.setQ1Month( null );
		hsa.setQ2Month( null );
		hsa.setQ3Month( null );
		hsa.setQ4Month( null );
		hsa.setQuarterlyEeAmount( null );
		hsa.setQuarterlyFamilyAmount( null );
		hsa.setContributionFrequency( null );
		hsa.setMonthlyEeAmount( null );
		hsa.setMonthlyFamilyAmount( null );

		return hsa;
	}
}
