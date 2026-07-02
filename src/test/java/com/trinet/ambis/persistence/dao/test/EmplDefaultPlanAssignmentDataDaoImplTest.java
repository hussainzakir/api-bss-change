package com.trinet.ambis.persistence.dao.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import com.trinet.ambis.persistence.dao.hrp.impl.EmplDefaultPlanAssignmentDataDaoImpl;
import com.trinet.ambis.persistence.model.EmplDefaultPlanAssignment;
import com.trinet.ambis.persistence.model.EmplDefaultPlanAssignmentId;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.ApplicationContextProvider;

@RunWith(MockitoJUnitRunner.class)
public class EmplDefaultPlanAssignmentDataDaoImplTest extends ServiceUnitTest {

	@InjectMocks
	EmplDefaultPlanAssignmentDataDaoImpl emplDefaultPlanAssignmentDataDaoImpl;

	@Mock
	EntityManager em;

	@Mock
	JdbcTemplate jdbcTemplate;

	private static final String MOCK_SQL = "MOCK-QUERY";
    private MockedStatic<ApplicationContextProvider> mockStaticApplicationContextProvider;

    @Before
    public void setup() {
        mockStaticApplicationContextProvider = Mockito.mockStatic(ApplicationContextProvider.class);
        ApplicationContext context = mock(ApplicationContext.class);
        when(ApplicationContextProvider.getApplicationContext()).thenReturn(context);
        jdbcTemplate = mock(JdbcTemplate.class);
        when(context.getBean("hrpJdbcTemplate", JdbcTemplate.class)).thenReturn(jdbcTemplate);
    }

    @After
    public void tearDown() {
        if (mockStaticApplicationContextProvider != null) {
            mockStaticApplicationContextProvider.close();
            mockStaticApplicationContextProvider = null;
        }
    }

	@Test
	public void saveEmplDefaultPlanAssignmentData() throws SQLException {

		List<EmplDefaultPlanAssignment> emplDefaultPlanAssignments = prepareEmplDefaultPlanAssignments();

		when(jdbcTemplate.batchUpdate(Mockito.anyString(), ArgumentMatchers.anyList(), Mockito.anyInt(), Mockito.any(ParameterizedPreparedStatementSetter.class)))
				.thenReturn(new int[][] { { 1 } });

		emplDefaultPlanAssignmentDataDaoImpl.saveEmplDefaultPlanAssignmentData(emplDefaultPlanAssignments);

		verify(jdbcTemplate, times(1)).batchUpdate(Mockito.anyString(), ArgumentMatchers.anyList(), Mockito.anyInt(), Mockito.any(ParameterizedPreparedStatementSetter.class));

	}

	private List<EmplDefaultPlanAssignment> prepareEmplDefaultPlanAssignments() {
		List<EmplDefaultPlanAssignment> emplDefaultPlanAssignments = List.of(
				EmplDefaultPlanAssignment.builder()
						.emplDefaultPlanAssignmentId(EmplDefaultPlanAssignmentId.builder().companyId(1).emplId("EMP1")
								.planType("10")
								.benefitPlanId("MEDPLAN1").build()).build(),
				EmplDefaultPlanAssignment.builder().emplDefaultPlanAssignmentId(
						EmplDefaultPlanAssignmentId.builder().companyId(1).emplId("EMP1").planType("11")
								.benefitPlanId("DENPLAN1").build()).build());
		return emplDefaultPlanAssignments;
	}

}