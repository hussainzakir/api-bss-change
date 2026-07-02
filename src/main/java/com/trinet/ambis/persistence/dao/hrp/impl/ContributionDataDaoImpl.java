package com.trinet.ambis.persistence.dao.hrp.impl;

import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.hrp.ContributionDataDao;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.service.RealmPlanYearRuleConfigService;
import com.trinet.ambis.util.ApplicationContextProvider;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ContributionDataDaoImpl implements ContributionDataDao {

	@PersistenceContext(unitName = "bis-hrp")
	EntityManager em;

	private static final Logger logger = LoggerFactory.getLogger(ContributionDataDaoImpl.class);

	@Override
	public void saveContributionData(List<Contribution> contributionsList) {

		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		long startTime = System.currentTimeMillis();

		if (context != null) {
			JdbcTemplate jdbcTemplate = context.getBean("hrpJdbcTemplate", JdbcTemplate.class);
			String sqlString = em.createNamedQuery("MERGE_XBSS_CONTRIBUTION")
					.unwrap(org.hibernate.query.Query.class).getQueryString();
			int[][] updateCounts = jdbcTemplate.batchUpdate(sqlString,
					contributionsList,
					1000,
					new ParameterizedPreparedStatementSetter<Contribution>() {
						public void setValues(PreparedStatement ps, Contribution contribution)
								throws SQLException {
							ps.setLong(1, contribution.getPlanSelectionId());
							ps.setString(2, contribution.getCoverageLevel());
							ps.setBigDecimal(3, contribution.getEmployerPercent());
							ps.setLong(4, contribution.getHeadCount());
							ps.setBigDecimal(5, contribution.getEmployeeContribution());
							ps.setBigDecimal(6, contribution.getEmployerContribution());
							ps.setString(7, contribution.getOverrideType());
							ps.setLong(8, contribution.getHsaHeadCount());
						}
					});
		}
		long endTime = System.currentTimeMillis();
		logger.error("ContributionDataDao.saveAll() Template took {} ms - {} Rows", (endTime - startTime), contributionsList.size());

	}
}