package com.trinet.ambis.persistence.dao.test;

import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.dao.hrp.impl.ContributionDataDaoImpl;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.ApplicationContextProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContributionDataDaoImplTest extends ServiceUnitTest {

	@InjectMocks
	ContributionDataDaoImpl contributionDataDaoImpl;

	@Mock
	EntityManager em;

	@Mock
	JdbcTemplate jdbcTemplate;

	private static final String MOCK_SQL = "MOCK-QUERY";

	Query mockedBatchQuery = null;
	org.hibernate.query.Query mockHibernateQuery;
    private MockedStatic<ApplicationContextProvider> mockedStaticApplicationContextProvider;

    @Before
    public void setup() {
        mockedBatchQuery = mock(Query.class);
        mockHibernateQuery = mock(org.hibernate.query.Query.class);
        mockedStaticApplicationContextProvider = org.mockito.Mockito.mockStatic(ApplicationContextProvider.class);

        ApplicationContext context = mock(ApplicationContext.class);
        mockedStaticApplicationContextProvider.when(ApplicationContextProvider::getApplicationContext).thenReturn(context);

        jdbcTemplate = mock(JdbcTemplate.class);
        when(context.getBean("hrpJdbcTemplate", JdbcTemplate.class)).thenReturn(jdbcTemplate);
    }

    @After
    public void tearDown() {
        if (mockedStaticApplicationContextProvider != null)
        mockedStaticApplicationContextProvider.close();
    }

	@Test
	public void saveContributionData() throws SQLException {

		List<Contribution> contributionsList = prepareContributions();

		when(em.createNamedQuery("MERGE_XBSS_CONTRIBUTION")).thenReturn(mockedBatchQuery);
		when(mockedBatchQuery.unwrap(org.hibernate.query.Query.class)).thenReturn( mockHibernateQuery );
		when(mockHibernateQuery.getQueryString()).thenReturn( MOCK_SQL );
		when(jdbcTemplate.batchUpdate(Mockito.anyString(), ArgumentMatchers.anyList(), Mockito.anyInt(), Mockito.any(ParameterizedPreparedStatementSetter.class)))
				.thenReturn(new int[][] { { 1 } });

		contributionDataDaoImpl.saveContributionData(contributionsList);

		verify(jdbcTemplate, times(1)).batchUpdate(Mockito.anyString(), ArgumentMatchers.anyList(), Mockito.anyInt(), Mockito.any(ParameterizedPreparedStatementSetter.class));
	}

	private List<Contribution> prepareContributions() {
		List<Contribution> contributionsList = new ArrayList<>();
		Contribution contribution = new Contribution();
		contribution.setBenefitPlan("MEDPLAN1");
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		contribution.setEmployeeContribution(BigDecimal.valueOf(500));
		contribution.setEmployerContribution(BigDecimal.valueOf(1000));
		contributionsList.add(contribution);
		return contributionsList;
	}

}