package com.trinet.ambis.persistence.dao.test;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.impl.EePlanAssignmentDataDaoImpl;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.EePlanAssignmentPK;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.ApplicationContextProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EePlanAssignmentDataDaoImplTest extends ServiceUnitTest {

	@InjectMocks
	EePlanAssignmentDataDaoImpl eePlanAssignmentDataDaoImpl;

	@Mock
	JdbcTemplate jdbcTemplate;
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
	public void saveEmployeePlanAssignmentsTest() throws SQLException {

		List<EePlanAssignment> eePlanAssignments = prepareEePlanAssignments();
		doAnswer(invocation -> {
			List<EePlanAssignment> assignments = invocation.getArgument(1);
			ParameterizedPreparedStatementSetter<EePlanAssignment> setter = invocation.getArgument(3);
			PreparedStatement ps = mock(PreparedStatement.class);
			for (EePlanAssignment assignment : assignments) {
				setter.setValues(ps, assignment);
			}
			return new int[][] { { 1 } };
		}).when(jdbcTemplate).batchUpdate(anyString(), anyList(), anyInt(), any(ParameterizedPreparedStatementSetter.class));

		eePlanAssignmentDataDaoImpl.saveEmployeePlanAssignments(eePlanAssignments);

		verify(jdbcTemplate, times(1))
				.batchUpdate(Mockito.anyString(), anyList(), anyInt(), Mockito.any(ParameterizedPreparedStatementSetter.class));

	}

	private List<EePlanAssignment> prepareEePlanAssignments() {
		List<EePlanAssignment> eePlanAssignments = List.of(
				EePlanAssignment.builder()
						.eePlanAssignmentPK(EePlanAssignmentPK.builder()
								.strategyId(123456)
								.emplId("EMPLOYEE1")
								.benefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).build())
						.benefitPlan("MEDPLAN1")
						.covrgCD("2")
						.portfolioId(5)
						.eeRate(BigDecimal.ZERO)
						.erRate(BigDecimal.valueOf(500.00)).build(),
				EePlanAssignment.builder()
						.eePlanAssignmentPK(EePlanAssignmentPK.builder()
								.strategyId(123456)
								.emplId("EMPLOYEE2")
								.benefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).build())
						.benefitPlan("MEDPLAN1")
						.covrgCD("4")
						.portfolioId(5)
						.eeRate(BigDecimal.ZERO)
						.erRate(BigDecimal.valueOf(2000.00)).build()
				);
		return eePlanAssignments;
	}

}