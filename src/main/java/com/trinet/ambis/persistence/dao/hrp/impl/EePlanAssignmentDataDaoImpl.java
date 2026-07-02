package com.trinet.ambis.persistence.dao.hrp.impl;

import com.trinet.ambis.persistence.dao.hrp.EePlanAssignmentDataDao;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class EePlanAssignmentDataDaoImpl implements EePlanAssignmentDataDao {

	@Override
	public void saveEmployeePlanAssignments(List<EePlanAssignment> eePlanAssignments) {

		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		String sqlString = "INSERT INTO XBSS_EE_PLAN_ASSIGNMENT (STRATEGY_ID, EMPLID, BENEFIT_TYPE, BENEFIT_PLAN, COVRG_CD, PORTFOLIO_ID, EE_RATE, ER_RATE) VALUES (?,?,?,?,?,?,?,?)";

		if (context != null) {
			JdbcTemplate jdbcTemplate = context.getBean("hrpJdbcTemplate", JdbcTemplate.class);
			jdbcTemplate.batchUpdate(sqlString,
					eePlanAssignments,
					1000,
					new ParameterizedPreparedStatementSetter<EePlanAssignment>() {
						public void setValues(PreparedStatement ps, EePlanAssignment eePlanAssignment)
								throws SQLException {
							ps.setLong(1, eePlanAssignment.getEePlanAssignmentPK().getStrategyId());
							ps.setString(2, eePlanAssignment.getEePlanAssignmentPK().getEmplId());
							ps.setString(3, eePlanAssignment.getEePlanAssignmentPK().getBenefitType());
							ps.setString(4, eePlanAssignment.getBenefitPlan());
							ps.setString(5, eePlanAssignment.getCovrgCD());
							ps.setLong(6, eePlanAssignment.getPortfolioId());
							ps.setBigDecimal(7, eePlanAssignment.getEeRate());
							ps.setBigDecimal(8, eePlanAssignment.getErRate());
						}
					});
		}
	}

}