package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearConfigurationDao;
import com.trinet.ambis.persistence.model.RealmPlanYearConfiguration;
import com.trinet.ambis.persistence.model.RealmPlanYearConfigurationId;
import com.trinet.ambis.service.impl.RealmPlanYearConfigurationServiceImpl;

@RunWith(JUnit4.class)
public class RealmPlanYearConfigurationServiceImplTest {

	@InjectMocks
	RealmPlanYearConfigurationServiceImpl service;

	@Mock
	RealmPlanYearConfigurationDao realmPlanYearConfigurationDao;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void findByRealmPlanYearId() {
		String oeQuarter = "Q1";
		long realmPlanYearId = 31L;

		List<RealmPlanYearConfiguration> configs = new ArrayList<>();
		RealmPlanYearConfiguration config = new RealmPlanYearConfiguration();
		config.setConfigValue("");
		RealmPlanYearConfigurationId id = new RealmPlanYearConfigurationId();
		id.setOeQuarter( oeQuarter );
		id.setConfigKey("MIN_FUNDING");
		id.setEffdt( java.sql.Date.valueOf( "2022-01-01" ) );
		config.setId(id);
		config.setConfigValue("200.50");
		configs.add(config);

		when( realmPlanYearConfigurationDao.findByIdRealmPlanYearId(realmPlanYearId) ).thenReturn(configs);

		List<RealmPlanYearConfiguration> actual = service.findByRealmPlanYearId(realmPlanYearId);

		assertEquals("MIN_FUNDING", actual.get(0).getId().getConfigKey());
		assertEquals("200.50", actual.get(0).getConfigValue());
	}

}