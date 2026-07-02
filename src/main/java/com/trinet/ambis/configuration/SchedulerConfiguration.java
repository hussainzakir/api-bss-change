package com.trinet.ambis.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;

/**
 * This class configures the lock provider for shed lock utility used by 
 * {@link SubmitScheduler} class 
 * 
 * @author schaudhari
 *
 */
@Profile("!test")
@Configuration
@EnableScheduling
public class SchedulerConfiguration {
	
	private static final String SHED_LOCK_TBL_NAME = "XBSS_SHED_LOCK";

	@Bean
	public LockProvider lockProvider(@Qualifier("hrpDataSource") DataSource dataSource) {
		return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder().withTableName(SHED_LOCK_TBL_NAME)
				.withJdbcTemplate(new JdbcTemplate(dataSource)).build());
	}
}