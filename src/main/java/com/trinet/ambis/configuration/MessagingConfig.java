package com.trinet.ambis.configuration;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"classpath:messaging-context.xml"})
@ComponentScan("com.trinet.messaging")
public class MessagingConfig
{
	@Value("${platform.exchange}")
	private String platfromExchangeName;
	
	@Autowired
	@Qualifier("publisherConnectionFactory")
	ConnectionFactory publisherConnectionFactory;
	
	// ConnectionName Bean Definition
	@Bean 
	ConnectionName connectionName(){
		return new ConnectionName();
	}
	
	// RabbitMQ Publisher Template
	@Bean 
	RabbitTemplate apiPublisherTemplate() {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(publisherConnectionFactory);
		rabbitTemplate.setExchange(platfromExchangeName);
		return rabbitTemplate;
	}
}