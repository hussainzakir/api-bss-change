package com.trinet.ambis.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.support.XmlWebApplicationContext;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class ApplicationContextProviderTest {
	ApplicationContext applicationContext;

	@Test
	public void getApplicationContext() {
		applicationContext = new XmlWebApplicationContext();
		ApplicationContextProvider provider = new ApplicationContextProvider();
		provider.setApplicationContext(applicationContext);

		ApplicationContext actualResult = ApplicationContextProvider.getApplicationContext();

		assertEquals(applicationContext, actualResult);
	}

	@Test(expected = IllegalStateException.class)
	public void getApplicationContext1() {
		ApplicationContextProvider provider = new ApplicationContextProvider();
		provider.setApplicationContext(null);

		ApplicationContextProvider.getApplicationContext();
	}

}
