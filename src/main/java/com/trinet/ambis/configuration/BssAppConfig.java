package com.trinet.ambis.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableAsync
public class BssAppConfig {

}
