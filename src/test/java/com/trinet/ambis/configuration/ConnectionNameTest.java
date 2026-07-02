package com.trinet.ambis.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionNameTest {

	@Mock
	ConnectionFactory connectionFactory;

	@Test
	public void obtainNewConnectionName() {
		ConnectionName name = new ConnectionName();

		String actual = name.obtainNewConnectionName(connectionFactory);

		assertEquals("api-bss", actual);
	}

}