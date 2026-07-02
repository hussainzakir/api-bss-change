package com.trinet.ambis.persistence.dao.hrp.impl;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.trinet.ambis.persistence.dao.hrp.EmplDefaultPlanAssignmentDataDao;
import com.trinet.ambis.persistence.model.EmplDefaultPlanAssignment;
import com.trinet.ambis.util.ApplicationContextProvider;

public class EmplDefaultPlanAssignmentDataDaoImpl implements EmplDefaultPlanAssignmentDataDao {

	private static final Logger logger = LoggerFactory.getLogger(EmplDefaultPlanAssignmentDataDaoImpl.class);

	@Override
	public void saveEmplDefaultPlanAssignmentData(List<EmplDefaultPlanAssignment> assignments) {
		long startTime = System.currentTimeMillis();
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		if (context == null) {
			logger.warn("ApplicationContext is null. Cannot proceed with saving default plan assignments.");
			return;
		}
		String sql = "INSERT INTO XBSS_EE_DEFAULT_PLAN_ASSIGNMENT "
				+ "(COMPANY_ID, EMPLID, PLAN_TYPE, PORTFOLIO_ID, BENEFIT_PLAN, COVRG_CD) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";

		JdbcTemplate jdbcTemplate = context.getBean("hrpJdbcTemplate", JdbcTemplate.class);

		int[][] updateCounts = jdbcTemplate.batchUpdate(sql, assignments, 1000, (ps, assignment) -> {
			ps.setLong(1, assignment.getCompanyId());
			ps.setString(2, assignment.getEmplId());
			ps.setString(3, assignment.getPlanType());
			ps.setInt(4, assignment.getPortfolioId());
			ps.setString(5, assignment.getBenefitPlanId());
			ps.setString(6, assignment.getCoverageCode());
		});

		long endTime = System.currentTimeMillis();
		logger.info("Batch insert completed in {} ms for {} rows.", (endTime - startTime), assignments.size());
		logger.debug("Update counts: {}", Arrays.deepToString(updateCounts));
	}

}
