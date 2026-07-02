package com.trinet.ambis.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@RunWith(MockitoJUnitRunner.class)
public class MessagingConfigTest {

	MessagingConfig mc;

	@Mock
	ConnectionFactory publisherConnectionFactory;

	@Test
	public void connectionName() {
		mc = new MessagingConfig();

		assertEquals("com.trinet.ambis.configuration.ConnectionName", mc.connectionName().getClass().getTypeName());
	}

	@Test
	public void apiPublisherTemplate() {
		mc = new MessagingConfig();

		RabbitTemplate actual = mc.apiPublisherTemplate();

		assertEquals("", actual.getExchange());
	}
}
