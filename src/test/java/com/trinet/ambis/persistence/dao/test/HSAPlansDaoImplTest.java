package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.ps.impl.HSAPlansDaoImpl;
import com.trinet.ambis.service.model.BenDefnOptnHSA;
import com.trinet.ambis.util.CommonUtils;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class HSAPlansDaoImplTest {

	@InjectMocks
	HSAPlansDaoImpl hsaPlansDao;

	@Mock
	EntityManager em;

	@Mock
	private Query mockedQuery;

	@Mock
	private org.hibernate.query.Query mockHibernateQuery;

	@Mock
	private Session mockSession;

	@Mock
	private Connection mockCn;

	@Mock
	private PreparedStatement mockStmt;

	private static final String CLONE_BENEFIT_PROGRAM = "CLONE_BENEFIT_PROGRAM";
	private static final String NEW_BENEFIT_PROGRAM = "NEW_BENEFIT_PROGRAM";
	private static final String PF_CLIENT = "PF_CLIENT";
	private static final String MOCK_SQL = "MOCK-SQL-STRING";
	private static final String EFF_DT_STR = "01-JAN-2020";
	private static final java.sql.Date EFF_DT = new java.sql.Date(
			CommonUtils.formatStringToDate(EFF_DT_STR, BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY).getTime());

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		hsaPlansDao.setEntityManager(em);
		when(em.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
	}

	@Test
	public void getAllHSAPlans() {

		when(mockedQuery.getResultList()).thenReturn(prepareHSAPlanData());

		List<BenDefnOptnHSA> actualResults = hsaPlansDao.getAllHSAPlans(NEW_BENEFIT_PROGRAM, EFF_DT);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResults.size());
	}

	@Test(expected = BSSApplicationException.class)
	public void getAllHSAPlansException() {
		String newBenefitProgram = "NEW_BENEFIT_PROGRAM";
		String effDtStr = "01-JAN-2020";
		java.sql.Date effDt = new java.sql.Date(
				CommonUtils.formatStringToDate(effDtStr, BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY).getTime());

		when(mockedQuery.getResultList()).thenThrow(new NoResultException());

		hsaPlansDao.getAllHSAPlans(newBenefitProgram, effDt);
	}

	@Test
	public void getActiveMedPlans() {
		String newBenefitProgram = "NEW_BENEFIT_PROGRAM";
		String effDtStr = "01-JAN-2020";
		java.sql.Date effDt = new java.sql.Date(
				CommonUtils.formatStringToDate(effDtStr, BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY).getTime());

		when(mockedQuery.getResultList()).thenReturn(prepareMedPlanData());

		Map<String, String> actualResults = hsaPlansDao.getActiveMedPlans(newBenefitProgram, effDt);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResults.size());
	}

	@Test(expected = NoResultException.class)
	public void getActiveMedPlansException() {
		String newBenefitProgram = "NEW_BENEFIT_PROGRAM";
		String effDtStr = "01-JAN-2020";
		java.sql.Date effDt = new java.sql.Date(
				CommonUtils.formatStringToDate(effDtStr, BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY).getTime());

		when(mockedQuery.getResultList()).thenThrow(new NoResultException());

		hsaPlansDao.getActiveMedPlans(newBenefitProgram, effDt);
	}

	@Test
	public void updateBenefPlanTable() {
		String pfClient = "PF_CLIENT";

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int actualResults = hsaPlansDao.updateBenefPlanTable(CLONE_BENEFIT_PROGRAM, NEW_BENEFIT_PROGRAM, pfClient, EFF_DT);

		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, actualResults);
	}

	@Test(expected = BSSApplicationException.class)
	public void updateBenefPlanTableException() {

		when(mockedQuery.executeUpdate()).thenThrow(new NoResultException());

		hsaPlansDao.updateBenefPlanTable(CLONE_BENEFIT_PROGRAM, NEW_BENEFIT_PROGRAM, PF_CLIENT, EFF_DT);
	}

	@Test
	public void updateFSABenefTable() {

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int actualResults = hsaPlansDao.updateFSABenefTable(CLONE_BENEFIT_PROGRAM, NEW_BENEFIT_PROGRAM, EFF_DT);

		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, actualResults);
	}

	@Test(expected = BSSApplicationException.class)
	public void updateFSABenefTableException() {

		when(mockedQuery.executeUpdate()).thenThrow(new NoResultException());

		hsaPlansDao.updateFSABenefTable(CLONE_BENEFIT_PROGRAM, NEW_BENEFIT_PROGRAM, EFF_DT);
	}

	@Test
	public void deleteHSAContribLmt() {

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int actualResults = hsaPlansDao.deleteHSAContribLmt(NEW_BENEFIT_PROGRAM, EFF_DT);

		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, actualResults);
	}

	@Test(expected = BSSApplicationException.class)
	public void deleteHSAContribLmtException() {

		when(mockedQuery.executeUpdate()).thenThrow(new NoResultException());

		hsaPlansDao.deleteHSAContribLmt(NEW_BENEFIT_PROGRAM, EFF_DT);
	}

	@Test
	public void insertHSAContribLmt() {
		BigDecimal eeContrib = BigDecimal.valueOf(100);
		BigDecimal famContrib = BigDecimal.valueOf(200);

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int actualResults = hsaPlansDao.insertHSAContribLmt(CLONE_BENEFIT_PROGRAM, NEW_BENEFIT_PROGRAM, eeContrib,
				famContrib, EFF_DT);

		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, actualResults);
	}

	@Test(expected = BSSApplicationException.class)
	public void insertHSAContribLmtException() {
		BigDecimal eeContrib = BigDecimal.valueOf(100);
		BigDecimal famContrib = BigDecimal.valueOf(200);

		when(mockedQuery.executeUpdate()).thenThrow(new NoResultException());

		hsaPlansDao.insertHSAContribLmt(CLONE_BENEFIT_PROGRAM, NEW_BENEFIT_PROGRAM, eeContrib,
				famContrib, EFF_DT);
	}

	@Test
	public void updateLimitIncludeTable() {

		when(mockedQuery.executeUpdate()).thenReturn(1);

		int actualResults = hsaPlansDao.updateLimitIncludeTable(NEW_BENEFIT_PROGRAM, EFF_DT);

		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, actualResults);
	}

	@Test(expected = BSSApplicationException.class)
	public void updateLimitIncludeTableException() {

		when(mockedQuery.executeUpdate()).thenThrow(new NoResultException());

		hsaPlansDao.updateLimitIncludeTable(NEW_BENEFIT_PROGRAM, EFF_DT);
	}

	@Test
	public void updateHSAOptns() throws SQLException {
		when( mockCn.prepareStatement( MOCK_SQL ) ).thenReturn( mockStmt );
		when( mockStmt.executeBatch() ).thenReturn( new int[] { -2 } );

		List<BenDefnOptnHSA> options = prepareHsaOptionsData();

		when(mockedQuery.unwrap(org.hibernate.query.Query.class)).thenReturn(mockHibernateQuery);
		when(mockHibernateQuery.getQueryString()).thenReturn( MOCK_SQL );
		when(em.unwrap( Session.class )).thenReturn( mockSession );

		Answer<Work> answer = new Answer<Work>() {
			public Work answer(InvocationOnMock invocation) throws Throwable {
				Work work = invocation.getArgument(0);
				work.execute( mockCn );
				return work;
			}
		};
		Mockito.doAnswer(answer).when(mockSession).doWork( ArgumentMatchers.any( Work.class ) );

		int actualResults = hsaPlansDao.updateHSAOptns(options);

		verify(mockSession, times(1)).doWork( ArgumentMatchers.any( Work.class ) );
		verify(mockStmt, times(2)).addBatch();
		assertEquals(2, actualResults);
	}


	@Test(expected = BSSApplicationException.class)
	public void updateHSAOptnsException() {
		try {
			when( mockCn.prepareStatement( MOCK_SQL ) ).thenReturn( mockStmt );
			when( mockStmt.executeBatch() ).thenThrow( new SQLException() );
		} catch( SQLException e ) {
			// I don't expect this exception to occur on a mock
		}

		List<BenDefnOptnHSA> options = prepareHsaOptionsData();

		when(mockedQuery.unwrap(org.hibernate.query.Query.class)).thenReturn(mockHibernateQuery);
		when(mockHibernateQuery.getQueryString()).thenReturn( MOCK_SQL );
		when(em.unwrap( Session.class )).thenReturn( mockSession );

		Answer<Work> answer = new Answer<Work>() {
			public Work answer(InvocationOnMock invocation) throws Throwable {
				Work work = invocation.getArgument(0);
				work.execute( mockCn );
				return work;
			}
		};
		Mockito.doAnswer(answer).when(mockSession).doWork( ArgumentMatchers.any( Work.class ) );

		hsaPlansDao.updateHSAOptns(options);
	}

	private List<Object[]> prepareHSAPlanData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] result = new Object[20];
		result[0] = NEW_BENEFIT_PROGRAM;
		result[1] = "01-JAN-2020";
		result[2] = "67";
		result[3] = BigDecimal.ONE;
		result[4] = BigDecimal.ONE;
		result[5] = "OPTION_TYPE";
		result[6] = "BENEFIT_PLAN_1";
		result[7] = CoverageCodesEnums.COV_EMPLOYEE.getCode();
		result[8] = "1";
		result[9] = BigDecimal.ONE;
		result[10] = "DEDCD";
		result[11] = "DEFAULT_OPTION_IND";
		result[12] = "ELIG_RULES_ID";
		result[13] = "LOCATION_TABLE_ID";
		result[14] = "CROSS_PLAN_TYPE";
		result[15] = "CROSS_BENEFIT_PLAN";
		result[16] = BigDecimal.ONE;
		result[17] = "CROSS_PLAN_DP";
		result[18] = "PF_CLIENT";
		results.add(result);

		result = new Object[20];
		result[0] = NEW_BENEFIT_PROGRAM;
		result[1] = "01-JAN-2020";
		result[2] = "67";
		result[3] = BigDecimal.ONE;
		result[4] = BigDecimal.ONE;
		result[5] = "OPTION_TYPE";
		result[6] = "BENEFIT_PLAN_2";
		result[7] = CoverageCodesEnums.COV_EMPLOYEE.getCode();
		result[8] = "1";
		result[9] = BigDecimal.ONE;
		result[10] = "DEDCD";
		result[11] = "DEFAULT_OPTION_IND";
		result[12] = "ELIG_RULES_ID";
		result[13] = "LOCATION_TABLE_ID";
		result[14] = "CROSS_PLAN_TYPE";
		result[15] = "CROSS_BENEFIT_PLAN";
		result[16] = BigDecimal.ONE;
		result[17] = "CROSS_PLAN_DP";
		result[18] = "PF_CLIENT";
		results.add(result);

		return results;
	}

	private List<Object[]> prepareMedPlanData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] result = new Object[2];
		result[0] = "BENEFIT_PLAN_1";
		result[1] = "eligRuleId1";
		results.add(result);

		result = new Object[2];
		result[0] = "BENEFIT_PLAN_2";
		result[1] = "eligRuleId2";
		results.add(result);

		return results;
	}

	private List<BenDefnOptnHSA> prepareHsaOptionsData() {
		List<BenDefnOptnHSA> optionList = new ArrayList<>();

		BenDefnOptnHSA option = new BenDefnOptnHSA();
		option.setBenefitProgram(NEW_BENEFIT_PROGRAM);
		option.setEffdt("2020-01-01");
		option.setPlanType("67");
		option.setOptionId(BigDecimal.ONE);
		option.setDisplayOptSeq(BigDecimal.ONE);
		option.setOptionType("OPTION_TYPE");
		option.setBenefitPlan("BENEFIT_PLAN_1");
		option.setCovrgCd(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		option.setOptionCd("1");
		option.setOptionLvl(BigDecimal.ONE);
		option.setDedcd("DEDCD");
		option.setDfltOptionInd("DEFAULT_OPTION_IND");
		option.setEligRulesId("ELIG_RULES_ID");
		option.setLocationTblId("LOCATION_TABLE_ID");
		option.setCrossPlanType("CROSS_PLAN_TYPE");
		option.setCrossBenefPlan("CROSS_BENEFIT_PLAN");
		option.setCoverageLimitPct(BigDecimal.ONE);
		option.setCrossPlnDpndChk("CROSS_PLAN_DP");
		optionList.add(option);

		option = new BenDefnOptnHSA();
		option.setBenefitProgram(NEW_BENEFIT_PROGRAM);
		option.setEffdt("2020-01-01");
		option.setPlanType("67");
		option.setOptionId(BigDecimal.ONE);
		option.setDisplayOptSeq(BigDecimal.ONE);
		option.setOptionType("OPTION_TYPE");
		option.setBenefitPlan("BENEFIT_PLAN_2");
		option.setCovrgCd(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		option.setOptionCd("1");
		option.setOptionLvl(BigDecimal.ONE);
		option.setDedcd("DEDCD");
		option.setDfltOptionInd("DEFAULT_OPTION_IND");
		option.setEligRulesId("ELIG_RULES_ID");
		option.setLocationTblId("LOCATION_TABLE_ID");
		option.setCrossPlanType("CROSS_PLAN_TYPE");
		option.setCrossBenefPlan("CROSS_BENEFIT_PLAN");
		option.setCoverageLimitPct(BigDecimal.ONE);
		option.setCrossPlnDpndChk("CROSS_PLAN_DP");
		optionList.add(option);

		return optionList;
	}

}