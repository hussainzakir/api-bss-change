package com.trinet.ambis.configuration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

@RunWith(JUnit4.class)
public class MicroServiceMessageConfigImplTest {

	@InjectMocks
	MicroServiceMessageConfigImpl microServiceMessageConfig;

	@Mock
	private Environment environment;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getProperty() {
		when(environment.getProperty("test")).thenReturn("result");
		String actual = microServiceMessageConfig.getProperty("test");

		assertEquals("result", actual);
	}
}
