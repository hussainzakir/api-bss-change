package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.ps.impl.LifeAndDisabilityCalcDataImpl;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.model.AdditionalBenefitEmployeeDetails;
import com.trinet.ambis.service.model.AdditionalPlanRate;
import com.trinet.ambis.service.model.FormulaDefinition;
import com.trinet.ambis.service.model.FormulaProperties;
import com.trinet.ambis.service.model.RateProperties;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class LifeAndDisabilityCalcDataImplTest {

	@InjectMocks
	LifeAndDisabilityCalcDataImpl lifeAndDisabilityCalcData;
	
	@Mock
	RealmPlanYearService realmPlanYearService;
	
	@Mock
	EntityManager entityManager;

	@Mock
	EntityManager em;

	@Mock
	private Query mockedQuery;
	

	@Mock
	private Query mockedQuery1;

	@Mock
	private Query mockedFormulaQuery;

	Company comp = null;
	BenefitGroup group = null;
	String cloneBenenProg = "CloneBenProg";
	Date effDt = null;
	Set<String> planList = new HashSet<String>();
	String bandCode = "bandCode";
	String quarter = "SM";
	RealmPlanYear prevRealmPlanYear;

	@Before
	public void setup() throws ParseException {
		MockitoAnnotations.initMocks(this);
		comp = new Company();
		comp.setCode("G48");
		comp.setPlanStartDate("2018/10/02");
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
		effDt = format.parse("01-DEC-2018");
		group = new BenefitGroup();
		prevRealmPlanYear = new RealmPlanYear();
		prevRealmPlanYear.setId(1);		
		
		when(lifeAndDisabilityCalcData.getEntityManager().createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
		when(lifeAndDisabilityCalcData.getEntityManager().createNamedQuery( BSSQueryConstants.LIFE_CVG_FORMULA_PROPERTIES )).thenReturn(mockedFormulaQuery);
		when(lifeAndDisabilityCalcData.getEntityManager().createNamedQuery( BSSQueryConstants.DISABILITY_CVG_FORMULA_PROPERTIES )).thenReturn(mockedFormulaQuery);
		when(mockedFormulaQuery.getResultList()).thenReturn( this.prepareFormulaPropertiesMockData() );
		when(realmPlanYearService.getPreviousRealmPlanYear(Mockito.any(RealmPlanYear.class)))
		.thenReturn(prevRealmPlanYear);
	}

	@Test
	public void getRateProperties() {
		List<Object[]> mockedResult = prepareMedicalAutoSelectedPlansByRegionMockData();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);

		Map<String, RateProperties> result = lifeAndDisabilityCalcData.getRateProperties(cloneBenenProg, effDt,
				planList, bandCode, quarter);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals("0044D", result.get("0044D").getBenefitPlan());
		assertEquals("UPP", result.get("0044D").getBenProg());
		assertEquals("30", result.get("0044D").getPlanType());
		assertEquals("10", result.get("0044D").getRatePerUnit());
		assertEquals("Rate Tbl 1", result.get("0044D").getRateTblID());
		assertEquals(1111, result.get("0044D").getRateType());

		when(mockedQuery.getResultList()).thenReturn(Collections.emptyList());

		result = lifeAndDisabilityCalcData.getRateProperties(cloneBenenProg, effDt, planList, bandCode, quarter);

		assertNull(result);
	}

	@Test
	public void getFormulaProperties() {
		String queryStr = "someQuery";
		List<Object[]> mockedResult = prepareFormulaPropertiesMockData();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);

		Map<String, FormulaProperties> result = lifeAndDisabilityCalcData.getFormulaProperties(planList, effDt,
				queryStr);

		final String MOCK_PLAN = "00F4SE";
		assertEquals(2, result.size());
		assertEquals( MOCK_PLAN, result.get( MOCK_PLAN ).getBenefitPlan());
		assertEquals("01AS000FID", result.get( MOCK_PLAN ).getFormulaID());
		assertEquals("31", result.get( MOCK_PLAN ).getPlanType());
		assertEquals("ABBR", result.get( MOCK_PLAN ).getBaseSource());
		assertEquals(100, result.get( MOCK_PLAN ).getMaxCovrg().intValue());
		assertEquals(10, result.get( MOCK_PLAN ).getMinCovrg().intValue());

		when(mockedQuery.getResultList()).thenReturn(Collections.emptyList());

		result = lifeAndDisabilityCalcData.getFormulaProperties(planList, effDt, queryStr);

		assertEquals(null, result);
	}

	@Test
	public void getFormulaDefinition() throws ParseException {
		String formulaID = "00AM001KTA";
		List<Object[]> mockedResult = prepareFormulaDefinitionMockData1();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);

		List<FormulaDefinition> result = lifeAndDisabilityCalcData.getFormulaDefinition(formulaID, effDt);
		assertEquals( 4, result.size() );
		assertEquals( "CNST", result.get( 2 ).getBnEntryTyp() );
		assertEquals( "/", result.get( 2 ).getBenOperand() );
		assertEquals( new BigDecimal( "12" ), result.get( 2 ).getBnValue() );


		mockedResult = prepareFormulaDefinitionMockData2();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);

		result = lifeAndDisabilityCalcData.getFormulaDefinition(formulaID, effDt);
		assertEquals( 9, result.size() );
		assertEquals( "R", result.get( 8 ).getBenOperand() );
		assertEquals( new BigDecimal( "0.01"), result.get( 8 ).getRoundUpAmt() );
		assertEquals( new BigDecimal( "1000" ), result.get( 8 ).getRoundTo() );


		when(mockedQuery.getResultList()).thenReturn(Collections.emptyList());

		result = lifeAndDisabilityCalcData.getFormulaDefinition(formulaID, effDt);
		assertEquals( 0, result.size() );
	}

	@Test
	public void getPlanRates() {
		Set<String> rateIds = new HashSet<String>();
		rateIds.add("000110");
		rateIds.add("000112");
		List<Object[]> mockedResult = preparePlanRatesMockData();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);

		Map<String, List<AdditionalPlanRate>> results = lifeAndDisabilityCalcData.getPlanRates(rateIds, effDt);

		assertEquals(1, results.get("000110").size());
		assertEquals("000110", results.get("000110").get(0).getRateTblId());
		assertEquals(32, results.get("000110").get(0).getAge());
		assertEquals(BigDecimal.valueOf(221), results.get("000110").get(0).getRate());

		assertEquals(2, results.get("000112").size());
		assertEquals("000112", results.get("000112").get(0).getRateTblId());
		assertEquals(42, results.get("000112").get(0).getAge());
		assertEquals(BigDecimal.valueOf(186), results.get("000112").get(0).getRate());

		assertEquals("000112", results.get("000112").get(1).getRateTblId());
		assertEquals(45, results.get("000112").get(1).getAge());
		assertEquals(BigDecimal.valueOf(291), results.get("000112").get(1).getRate());

		when(mockedQuery.getResultList()).thenReturn(Collections.emptyList());

		results = lifeAndDisabilityCalcData.getPlanRates(rateIds, effDt);
	}

	@Test
	public void getGroupEmployeeSelections() {
		Company company = new Company();
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setPlanYearEnd( java.sql.Date.valueOf( "2019-09-30" ) );
		rpy.setPlanYearStart( java.sql.Date.valueOf( "2019-10-01" ) );
		company.setRealmPlanYear(rpy);
		boolean history = true;

		Query benProgQuery = mock(Query.class);
		Query offeredPlanQuery = mock(Query.class);
		Query lifeFormulaProps = mock(Query.class);
		Query disbFormulaProps = mock(Query.class);
		Query lifeDisCalcRules = mock(Query.class);
		Query empSalQuery = mock(Query.class);

		when( entityManager.createNamedQuery( "GET_BENEFIT_PROGRAMS_FOR_COMPANY" )).thenReturn( benProgQuery );
		when( entityManager.createNamedQuery( "GET_BENEFIT_PLANS_FOR_PLANYEAR" )).thenReturn( offeredPlanQuery );
		when( entityManager.createNamedQuery( BSSQueryConstants.LIFE_CVG_FORMULA_PROPERTIES )).thenReturn( lifeFormulaProps );
		when( entityManager.createNamedQuery( BSSQueryConstants.DISABILITY_CVG_FORMULA_PROPERTIES )).thenReturn( disbFormulaProps );
		when( entityManager.createNamedQuery( "LIFE_DISB_CALC_RULES" )).thenReturn( lifeDisCalcRules );
		when(entityManager.createNamedQuery("HISTORY_COMPANY_BENEFIT_SALARY_AS_OF")).thenReturn(empSalQuery);
		when(em.createNamedQuery("CENSUS_HISTORY_COMPANY_BENEFIT_SALARY_AS_OF")).thenReturn(empSalQuery);
		when(entityManager.createNamedQuery("STRATEGY_COMPANY_BENEFIT_SALARY_AS_OF")).thenReturn(empSalQuery);
		when(em.createNamedQuery("CENSUS_STRATEGY_COMPANY_BENEFIT_SALARY_AS_OF")).thenReturn(empSalQuery);

		when( benProgQuery.getResultList() ).thenReturn( prepareBenProgQueryData() );
		when( offeredPlanQuery.getResultList() ).thenReturn( prepareOfferedPlanQueryData() );
		when( lifeFormulaProps.getResultList() ).thenReturn( prepareLifeFormulaPropsData() );
		when( disbFormulaProps.getResultList() ).thenReturn( prepareDisbFormulaPropsData() );
		when( lifeDisCalcRules.getResultList() ).thenReturn( prepareCalcRuleData() );
		when( empSalQuery.getResultList() ).thenReturn( prepareEmpSalData() );

		Map<String, AdditionalBenefitEmployeeDetails> actualResult = lifeAndDisabilityCalcData
				.getGroupEmployeeSelections(company, history, 0L, false);

		assertEquals( 3, actualResult.size() );
		assertEquals( 9, actualResult.get("QHW").getPlans().size() );
		assertEquals( 9, actualResult.get("QII").getPlans().size() );
		assertEquals( 9, actualResult.get("TF3").getPlans().size() );
		assertEquals( 2, actualResult.get("QHW").selectDistinctAsOfDate().size() );
		assertEquals( new BigDecimal( "119000" ), actualResult.get("QHW").retrieveEmplSalaryMapForPlan("000Q6Y").get("00001234569"));
		assertEquals( new BigDecimal( "115000" ), actualResult.get("QHW").retrieveEmplSalaryMapForPlan("000Q6Y").get("00001234567"));
		assertEquals( new BigDecimal( "116000" ), actualResult.get("QHW").retrieveEmplSalaryMapForPlan("000Q6Y").get("00001234568"));

		actualResult = lifeAndDisabilityCalcData
				.getGroupEmployeeSelections(company, history, 0L, true);

		assertEquals( 3, actualResult.size() );
		assertEquals( 9, actualResult.get("QHW").getPlans().size() );
		assertEquals( 9, actualResult.get("QII").getPlans().size() );
		assertEquals( 9, actualResult.get("TF3").getPlans().size() );
		assertEquals( 2, actualResult.get("QHW").selectDistinctAsOfDate().size() );
		assertEquals( new BigDecimal( "119000" ), actualResult.get("QHW").retrieveEmplSalaryMapForPlan("000Q6Y").get("00001234569"));
		assertEquals( new BigDecimal( "115000" ), actualResult.get("QHW").retrieveEmplSalaryMapForPlan("000Q6Y").get("00001234567"));
		assertEquals( new BigDecimal( "116000" ), actualResult.get("QHW").retrieveEmplSalaryMapForPlan("000Q6Y").get("00001234568"));
	
	}
	
	// When history false and strategy id is 0
	@Test(expected = BSSApplicationException.class)
	public void getGroupEmployeeSelections_test1() {
		Company company = new Company();
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setPlanYearEnd(java.sql.Date.valueOf("2019-09-30"));
		rpy.setPlanYearStart(java.sql.Date.valueOf("2019-10-01"));
		company.setRealmPlanYear(rpy);
		boolean history = false;

		Query benProgQuery = mock(Query.class);
		Query offeredPlanQuery = mock(Query.class);
		Query lifeFormulaProps = mock(Query.class);
		Query disbFormulaProps = mock(Query.class);
		Query lifeDisCalcRules = mock(Query.class);
		Query empSalQuery = mock(Query.class);

		when(entityManager.createNamedQuery("GET_BENEFIT_PROGRAMS_FOR_COMPANY")).thenReturn(benProgQuery);
		when(entityManager.createNamedQuery("GET_BENEFIT_PLANS_FOR_PLANYEAR")).thenReturn(offeredPlanQuery);
		when(entityManager.createNamedQuery(BSSQueryConstants.LIFE_CVG_FORMULA_PROPERTIES))
				.thenReturn(lifeFormulaProps);
		when(entityManager.createNamedQuery(BSSQueryConstants.DISABILITY_CVG_FORMULA_PROPERTIES))
				.thenReturn(disbFormulaProps);
		when(entityManager.createNamedQuery("LIFE_DISB_CALC_RULES")).thenReturn(lifeDisCalcRules);
		when(entityManager.createNamedQuery("HISTORY_COMPANY_BENEFIT_SALARY_AS_OF")).thenReturn(empSalQuery);
		when(em.createNamedQuery("CENSUS_HISTORY_COMPANY_BENEFIT_SALARY_AS_OF")).thenReturn(empSalQuery);
		when(entityManager.createNamedQuery("STRATEGY_COMPANY_BENEFIT_SALARY_AS_OF")).thenReturn(empSalQuery);
		when(em.createNamedQuery("CENSUS_STRATEGY_COMPANY_BENEFIT_SALARY_AS_OF")).thenReturn(empSalQuery);

		when(benProgQuery.getResultList()).thenReturn(prepareBenProgQueryData());
		when(offeredPlanQuery.getResultList()).thenReturn(prepareOfferedPlanQueryData());
		when(lifeFormulaProps.getResultList()).thenReturn(prepareLifeFormulaPropsData());
		when(disbFormulaProps.getResultList()).thenReturn(prepareDisbFormulaPropsData());
		when(lifeDisCalcRules.getResultList()).thenReturn(prepareCalcRuleData());
		when(empSalQuery.getResultList()).thenReturn(prepareEmpSalData());

		Map<String, AdditionalBenefitEmployeeDetails> actualResult = lifeAndDisabilityCalcData.getGroupEmployeeSelections(company, history, 0L, false);
		
		assertEquals( 3, actualResult.size() );
		assertEquals( 9, actualResult.get("QHW").getPlans().size() );
		assertEquals( 9, actualResult.get("QII").getPlans().size() );
		assertEquals( 9, actualResult.get("TF3").getPlans().size() );
		assertEquals( 2, actualResult.get("QHW").selectDistinctAsOfDate().size() );
		assertEquals( new BigDecimal( "119000" ), actualResult.get("QHW").retrieveEmplSalaryMapForPlan("000Q6Y").get("00001234569"));
		assertEquals( new BigDecimal( "115000" ), actualResult.get("QHW").retrieveEmplSalaryMapForPlan("000Q6Y").get("00001234567"));
		assertEquals( new BigDecimal( "116000" ), actualResult.get("QHW").retrieveEmplSalaryMapForPlan("000Q6Y").get("00001234568"));
		
	}
	
	// When history false and strategy id is not 0
	@Test
	public void getGroupEmployeeSelections_test2() {
		Company company = new Company();
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setPlanYearEnd(java.sql.Date.valueOf("2019-09-30"));
		rpy.setPlanYearStart(java.sql.Date.valueOf("2019-10-01"));
		company.setRealmPlanYear(rpy);
		boolean history = false;

		Query benProgQuery = mock(Query.class);
		Query offeredPlanQuery = mock(Query.class);
		Query lifeFormulaProps = mock(Query.class);
		Query disbFormulaProps = mock(Query.class);
		Query lifeDisCalcRules = mock(Query.class);
		Query empSalQuery = mock(Query.class);

		when(entityManager.createNamedQuery("GET_BENEFIT_PROGRAMS_FOR_COMPANY")).thenReturn(benProgQuery);
		when(entityManager.createNamedQuery("GET_BENEFIT_PLANS_FOR_PLANYEAR")).thenReturn(offeredPlanQuery);
		when(entityManager.createNamedQuery(BSSQueryConstants.LIFE_CVG_FORMULA_PROPERTIES))
				.thenReturn(lifeFormulaProps);
		when(entityManager.createNamedQuery(BSSQueryConstants.DISABILITY_CVG_FORMULA_PROPERTIES))
				.thenReturn(disbFormulaProps);
		when(entityManager.createNamedQuery("LIFE_DISB_CALC_RULES")).thenReturn(lifeDisCalcRules);
		when(entityManager.createNamedQuery("HISTORY_COMPANY_BENEFIT_SALARY_AS_OF")).thenReturn(empSalQuery);
		when(em.createNamedQuery("CENSUS_HISTORY_COMPANY_BENEFIT_SALARY_AS_OF")).thenReturn(empSalQuery);
		when(entityManager.createNamedQuery("STRATEGY_COMPANY_BENEFIT_SALARY_AS_OF")).thenReturn(empSalQuery);
		when(em.createNamedQuery("CENSUS_STRATEGY_COMPANY_BENEFIT_SALARY_AS_OF")).thenReturn(empSalQuery);
		
		when(benProgQuery.getResultList()).thenReturn(prepareBenProgQueryData());
		when(offeredPlanQuery.getResultList()).thenReturn(prepareOfferedPlanQueryData());
		when(lifeFormulaProps.getResultList()).thenReturn(prepareLifeFormulaPropsData());
		when(disbFormulaProps.getResultList()).thenReturn(prepareDisbFormulaPropsData());
		when(lifeDisCalcRules.getResultList()).thenReturn(prepareCalcRuleData());
		when(empSalQuery.getResultList()).thenReturn(prepareEmpSalData());

		Map<String, AdditionalBenefitEmployeeDetails> actualResult = lifeAndDisabilityCalcData.getGroupEmployeeSelections(company, history, 1111L, true);

		assertEquals( 3, actualResult.size() );
		assertEquals( 9, actualResult.get("QHW").getPlans().size() );
		assertEquals( 9, actualResult.get("QII").getPlans().size() );
		assertEquals( 9, actualResult.get("TF3").getPlans().size() );
		assertEquals( 2, actualResult.get("QHW").selectDistinctAsOfDate().size() );
		assertEquals( new BigDecimal( "119000" ), actualResult.get("QHW").retrieveEmplSalaryMapForPlan("000Q6Y").get("00001234569"));
		assertEquals( new BigDecimal( "115000" ), actualResult.get("QHW").retrieveEmplSalaryMapForPlan("000Q6Y").get("00001234567"));
		assertEquals( new BigDecimal( "116000" ), actualResult.get("QHW").retrieveEmplSalaryMapForPlan("000Q6Y").get("00001234568"));
	}

	private List<Object[]> prepareFormulaDefinitionMockData1() {
		List<Object[]> data = new ArrayList<Object[]>();
		data.add( arrayFromDefData( 10, "(", " "   , 0, 0,0 ) );
		data.add( arrayFromDefData( 20, " ", "BASE", 0, 0,0 ) );
		data.add( arrayFromDefData( 30, "/", "CNST", 12,0,0 ) );
		data.add( arrayFromDefData( 40, ")", " "   , 0, 0,0 ) );
		return data;
	}

	private List<Object[]> prepareFormulaDefinitionMockData2() {
		List<Object[]> data = new ArrayList<Object[]>();
		data.add( arrayFromDefData( 10, "(", " ",   0,0,0 ));
		data.add( arrayFromDefData( 20, "(", " ",   0,0,0 ));
		data.add( arrayFromDefData( 30, "(", " ",   0,0,0 ));
		data.add( arrayFromDefData( 40, " ", "BASE",0,0,0 ));
		data.add( arrayFromDefData( 50, ")", " ",   0,0,0 ));
		data.add( arrayFromDefData( 60, "*", "CNST",6,0,0 ));
		data.add( arrayFromDefData( 70, ")", " ",   0,0,0 ));
		data.add( arrayFromDefData( 80, ")", " ",   0,0,0 ));
		data.add( arrayFromDefData( 90, "R", " ",   0,0.01,1000 ));
		return data;
	}

	private Object[] arrayFromDefData( long bnSeqNum, String benOperand
			, String bnEntryTyp, long bnValue, double roundUpAmt, long roundTo ) {
		Object[] r = new Object[6];
		r[0] = BigDecimal.valueOf( bnSeqNum );
		r[1] = benOperand;
		r[2] = bnEntryTyp;
		r[3] = BigDecimal.valueOf( bnValue );
		r[4] = BigDecimal.valueOf( roundUpAmt );
		r[5] = BigDecimal.valueOf( roundTo );
		return r;
	}

	private List<Object> prepareBenProgQueryData() {
		List<Object> data = new ArrayList<Object>();
		data.add( "QHW" );
		data.add( "QII" );
		data.add( "TF3" );
		return data;
	}

	private List<Object> prepareOfferedPlanQueryData() {
		List<Object> data = new ArrayList<Object>();
		data.add( "000Q6T" );
		data.add( "000Q6U" );
		data.add( "003D12" );
		data.add( "000Q6Y" );
		data.add( "000Q6Z" );
		data.add( "000Q70" );
		data.add( "000Q7M" );
		data.add( "000Q7N" );
		data.add( "000Q7O" );
		return data;
	}

	private List<Object[]> prepareLifeFormulaPropsData() {
		List<Object[]> data = new ArrayList<Object[]>();
		data.add( arrayFromPropsData( "23","000Q6T","29-JAN-2013","01BS0000TC","01-JUL-2019","ABBR",0,10000,1000000,4,1,"T",4,1,"T" ));
		data.add( arrayFromPropsData( "23","000Q6U","29-JAN-2013","03AS0000TD","01-JUL-2019","ABBR",0,10000,1000000,4,1,"T",4,1,"T" ));
		data.add( arrayFromPropsData( "23","003D12","23-JAN-2018","00AS010KQ3","01-JUL-2019","ABBR",0,10000,10000,  4,1,"T",4,1,"T" ));
		return data;
	}

	private List<Object[]> prepareDisbFormulaPropsData() {
		List<Object[]> data = new ArrayList<Object[]>();
		data.add( arrayFromPropsData( "30", "000Q6Y", "29-JAN-2013", "DSAS240KCD", "01-JUL-2019", "ABBR", 240000, 0, 0, 4, 1, "T", 4, 1, "T" ) );
		data.add( arrayFromPropsData( "30", "000Q6Z", "29-JAN-2013", "DSAS240KCD", "01-JUL-2019", "ABBR", 240000, 0, 0, 4, 1, "T", 4, 1, "T" ) );
		data.add( arrayFromPropsData( "30", "000Q70", "29-JAN-2013", "DSAS120KCD", "01-JUL-2019", "ABBR", 120000, 0, 0, 4, 1, "T", 4, 1, "T" ) );
		data.add( arrayFromPropsData( "31", "000Q7N", "01-JUL-2016", "DSAS300KCD", "01-JUL-2019", "ANRT", 300000, 0, 0, 4, 1, "L", 4, 1, "L" ) );
		data.add( arrayFromPropsData( "31", "000Q7M", "29-JAN-2013", "DSAS240KCD", "01-JUL-2019", "ABBR", 240000, 0, 0, 4, 1, "T", 4, 1, "T" ) );
		data.add( arrayFromPropsData( "31", "000Q7O", "01-JUL-2016", "DSAS300KCD", "01-JUL-2019", "ABBR", 300000, 0, 0, 4, 1, "T", 4, 1, "T" ) );
		return data;
	}

	private Object[] arrayFromPropsData( String planType, String benefitPlan, String date1, String formulaId
			, String date2, String rateSource, long maxBase, long covgMin, long covgMax
			, long covAsOfMM, long covAsOfDD, String covAsOfCd, long premAsOfMM, long premAsOfDD, String premAsOfCd ) {

		Object[] r = new Object[15];
		r[0] = planType;
		r[1] = benefitPlan;
		r[2] = date1;
		r[3] = formulaId;
		r[4] = date2;
		r[5] = rateSource;
		r[6] = BigDecimal.valueOf( maxBase );
		r[7] = BigDecimal.valueOf( covgMin );
		r[8] = BigDecimal.valueOf( covgMax );
		r[9] = BigDecimal.valueOf( covAsOfMM );
		r[10] = BigDecimal.valueOf( covAsOfDD );
		r[11] = covAsOfCd;
		r[12] = BigDecimal.valueOf( premAsOfMM );
		r[13] = BigDecimal.valueOf( premAsOfDD );
		r[14] = premAsOfCd;
		return r;
	}

	private List<Object[]> prepareCalcRuleData() {
		List<Object[]> data = new ArrayList<Object[]>();
		data.add( arrayFromCalcRuleData("000Q6T","002R","T",4,1,"ABBR") );
		data.add( arrayFromCalcRuleData("000Q6U","002R","T",4,1,"ABBR") );
		data.add( arrayFromCalcRuleData("003D12","002R","T",4,1,"ABBR") );
		data.add( arrayFromCalcRuleData("000Q6Y","002V","T",4,1,"ABBR") );
		data.add( arrayFromCalcRuleData("000Q6Z","002V","T",4,1,"ABBR") );
		data.add( arrayFromCalcRuleData("000Q70","002V","T",4,1,"ABBR") );
		data.add( arrayFromCalcRuleData("000Q7N","002W","L",4,1,"ANRT") );
		data.add( arrayFromCalcRuleData("000Q7M","002X","T",4,1,"ABBR") );
		data.add( arrayFromCalcRuleData("000Q7O","002W","T",4,1,"ABBR") );
		return data;
	}

	private Object[] arrayFromCalcRuleData( String benefitPlan, String calcRulesId, String baseAsOfCd
			, long baseAsOfMM, long baseAsOfDD, String baseSource ) {

		Object[] r = new Object[6];
		r[0] = benefitPlan;
		r[1] = calcRulesId;
		r[2] = baseAsOfCd;
		r[3] = BigDecimal.valueOf( baseAsOfMM );
		r[4] = BigDecimal.valueOf( baseAsOfDD );
		r[5] = baseSource;
		return r;
	}

	private List<Object[]> prepareEmpSalData() {
		List<Object[]> data = new ArrayList<Object[]>();
		data.add( arrayFromEmpSalData("QHW","00001234567",101000,115000) );
		data.add( arrayFromEmpSalData("QHW","00001234568",103000,116000) );
		data.add( arrayFromEmpSalData("QHW","00001234569",104000,119000) );
		data.add( arrayFromEmpSalData("QII","00001234570",32000,32640) );
		data.add( arrayFromEmpSalData("QII","00001234571",24000,24686) );
		data.add( arrayFromEmpSalData("QII","00001234572",35000,36010) );
		return data;
	}

	private Object[] arrayFromEmpSalData(String benefitProgram, String emplid, long annualRt, long abbr) {

		Object[] r = new Object[6];
		r[0] = benefitProgram;
		r[1] = emplid;
		r[2] = BigDecimal.valueOf(annualRt);
		r[3] = BigDecimal.valueOf(abbr);
		return r;
	}

	private List<Object[]> prepareMedicalAutoSelectedPlansByRegionMockData() {
		List<Object[]> data = new ArrayList<Object[]>();

		Object[] r = new Object[9];
		r[0] = "UPP";
		r[1] = "2018/12/01";
		r[2] = "30";
		r[3] = "Opt1";
		r[4] = "0044D";
		r[5] = "Rate Tbl 1";
		r[6] = "1111";
		r[7] = "Pay Freq 1";
		r[8] = "10";
		data.add(r);
		return data;
	}

	private List<Object[]> prepareFormulaPropertiesMockData() {
		List<Object[]> data = new ArrayList<Object[]>();

		Object[] r = new Object[15];
		r[0] = "23";
		r[1] = "0033WI";
		r[2] = "16-OCT-2017";
		r[3] = "00AS010KQ2";
		r[4] = "01-JAN-2019";
		r[5] = "ABBR";
		r[6] = new BigDecimal(0);
		r[7] = new BigDecimal(10);
		r[8] = new BigDecimal(10);
		r[9] = new BigDecimal(4);
		r[10] = new BigDecimal(1);
		r[11] = "L";
		r[12] = new BigDecimal(4);
		r[13] = new BigDecimal(1);
		r[14] = "L";
		data.add(r);
		
		r = new Object[15];
		r[0] = "31";
		r[1] = "00F4SE";
		r[2] = "01-JAN-2017";
		r[3] = "01AS000FID";
		r[4] = "01-JAN-2018";
		r[5] = "ABBR";
		r[6] = new BigDecimal(500);
		r[7] = new BigDecimal(10);
		r[8] = new BigDecimal(100);
		r[9] = new BigDecimal(6);
		r[10] = new BigDecimal(1);
		r[11] = "L";
		r[12] = new BigDecimal(6);
		r[13] = new BigDecimal(1);
		r[14] = "L";
		data.add(r);

		return data;
	}

	private List<Object[]> preparePlanRatesMockData() {
		List<Object[]> data = new ArrayList<Object[]>();

		Object[] r = new Object[9];
		r[0] = "000110";
		r[1] = "M";
		r[2] = "Y";
		r[3] = new BigDecimal(32);
		r[4] = new BigDecimal(221);
		data.add(r);
		r = new Object[9];
		r[0] = "000112";
		r[1] = "M";
		r[2] = "Y";
		r[3] = new BigDecimal(42);
		r[4] = new BigDecimal(186);
		data.add(r);
		r = new Object[9];
		r[0] = "000112";
		r[1] = "F";
		r[2] = "N";
		r[3] = new BigDecimal(45);
		r[4] = new BigDecimal(291);
		data.add(r);
		return data;
	}

	private List<Object[]> prepareLifeEnrollmentMockData() {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r = new Object[8];
		r[0] = "UPP";
		r[1] = "23";
		r[2] = "000TM9";
		r[3] = "00001415472";
		r[4] = new BigDecimal(1);
		r[5] = new BigDecimal(300000);
		r[6] = new BigDecimal(535750);
		r[7] = new BigDecimal(40);
		data.add(r);
		r = new Object[8];
		r[0] = "EF1";
		r[1] = "23";
		r[2] = "000TM9";
		r[3] = "00001529897";
		r[4] = new BigDecimal(1);
		r[5] = new BigDecimal(300000);
		r[6] = new BigDecimal(499999.92);
		r[7] = new BigDecimal(52);
		data.add(r);
		return data;
	}

	private List<Object[]> prepareDisabilityEnrollmentMockData() {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r = new Object[8];
		r[0] = "UPP";
		r[1] = "30";
		r[2] = "000WAB";
		r[3] = "00001415472";
		r[4] = new BigDecimal(1);
		r[5] = new BigDecimal(300000);
		r[6] = new BigDecimal(535750);
		r[7] = new BigDecimal(42);
		data.add(r);
		return data;
	}

	private List<Object[]> prepareAvgBenSalaryMockData() {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r = new Object[2];
		r[0] = "001RS3";
		r[1] = BigDecimal.valueOf(100);
		data.add(r);
		r = new Object[2];
		r[0] = "001RS7";
		r[1] = BigDecimal.valueOf(100);
		data.add(r);
		return data;
	}
}