package com.trinet.ambis.configuration;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class AsyncConfig implements AsyncConfigurer {
	
	@Override
	@Bean
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
		executor.setCorePoolSize(6);
        executor.setMaxPoolSize(12);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("BSS-");
        executor.initialize();
        return executor;
	}

	/**
	 * The {@link AsyncUncaughtExceptionHandler} instance to be used
	 * when an exception is thrown during an asynchronous method execution
	 * with {@code void} return type.
	 */
	@Override
	@Bean
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		
		return new AsyncUncaughtExceptionHandler() {
			private static final String EXCEPTION_MSG_FORMAT = "Exception occured in method: %s(%s). Reason: %s Throwable: %s";
			
			@Override
			public void handleUncaughtException(Throwable ex, Method method, Object... params) {
				log.error(() -> String.format(EXCEPTION_MSG_FORMAT, 
												method.getName(), 
												String.join(",", Arrays.asList(String.valueOf(params))), 
												ex.getMessage(), ex));
			}	
		};
	}
}
