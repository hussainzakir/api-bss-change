package com.trinet.ambis.configuration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.class)
public class BSSMessageConfigTest {

	@Autowired
	@InjectMocks
	BSSMessageConfig bssMessageConfig;

	@Mock
	private Environment environment;

	@Test
	public void getProperty() {
		when(environment.getProperty("test")).thenReturn("result");
		bssMessageConfig.init();
		String actual = bssMessageConfig.getProperty("test");

		assertEquals("result", actual);
	}

	@Test
	public void getPropertyAsList() {
		when(environment.getProperty("test")).thenReturn("result1,result2");
		bssMessageConfig.init();
		List<String> actual = bssMessageConfig.getPropertyAsList("test");

		assertEquals(2, actual.size());
	}
}
