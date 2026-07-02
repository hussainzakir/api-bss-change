package com.trinet.ambis.configuration;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;

import com.trinet.ambis.common.BSSApplicationConstants;

public class ConnectionName implements ConnectionNameStrategy {

	@Override
	public String obtainNewConnectionName(ConnectionFactory connectionFactory) {
		return BSSApplicationConstants.API_NAME;
	}

}