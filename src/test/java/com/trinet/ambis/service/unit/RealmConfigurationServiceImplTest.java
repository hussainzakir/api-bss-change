
package com.trinet.ambis.service.unit;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

import com.trinet.ambis.persistence.dao.hrp.RealmConfigurationDao;
import com.trinet.ambis.persistence.model.RealmConfiguration;
import com.trinet.ambis.service.impl.RealmConfigurationServiceImpl;

/**
 * @author hliddle
 *
 */
@RunWith(JUnit4.class)
public class RealmConfigurationServiceImplTest {

	@InjectMocks
	private RealmConfigurationServiceImpl realmConfigurationService;

	@Mock
	RealmConfigurationDao realmConfigurationDao;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void findByRealmPlanYearId() {
		long realmId = 1;
		List<RealmConfiguration> realmConfigurationList = new ArrayList<>();
		when(realmConfigurationDao.findByIdRealmId(realmId)).thenReturn(realmConfigurationList);
		realmConfigurationService.findByRealmId(realmId);
		verify(realmConfigurationDao, times(1)).findByIdRealmId(realmId);
	}
}
