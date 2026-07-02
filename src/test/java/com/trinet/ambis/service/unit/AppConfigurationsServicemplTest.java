package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.trinet.ambis.service.impl.AppConfigurationServiceImpl;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.persistence.dao.hrp.AppConfigurationsDao;
import com.trinet.ambis.persistence.model.AppConfigurations;
import com.trinet.ambis.service.AppConfigurationService;

@RunWith(MockitoJUnitRunner.class)
public class AppConfigurationsServicemplTest extends ServiceUnitTest {

	@InjectMocks
    AppConfigurationServiceImpl appConfigurationService;

	@Mock
	AppConfigurationsDao appConfigurationsDao;

	@Test
	public void findAll() {
		AppConfigurations appConfigurations = new AppConfigurations();
		
		when(appConfigurationsDao.findAll()).thenReturn(Arrays.asList(appConfigurations));
		
		List<AppConfigurations> actualResult = appConfigurationService.findAll();
		
		assertEquals(1, actualResult.size());
	}

}