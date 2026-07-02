package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.trinet.ambis.service.impl.AppRuleServiceImpl;
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

import com.trinet.ambis.persistence.dao.hrp.AppRulesDao;
import com.trinet.ambis.persistence.model.AppRules;
import com.trinet.ambis.service.AppRuleService;


@RunWith(MockitoJUnitRunner.class)
public class AppRuleServiceImplTest extends ServiceUnitTest {

	@InjectMocks
    AppRuleServiceImpl appRuleService;

	@Mock
	AppRulesDao appRulesDao;

	@Test
	public void findAll() {
		AppRules appRule = new AppRules();
		
		when(appRulesDao.findAll()).thenReturn(Arrays.asList(appRule));
		
		List<AppRules> actualResult = appRuleService.findAll();
		
		assertEquals(1, actualResult.size());
	}

}