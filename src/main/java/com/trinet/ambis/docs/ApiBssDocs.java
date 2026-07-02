package com.trinet.ambis.docs;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.trinet.common.docs.BaseTriNetApiAdapter;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Profile("!test")
@Configuration
@EnableWebMvc
@EnableSwagger2
public class ApiBssDocs extends BaseTriNetApiAdapter{
	
}
