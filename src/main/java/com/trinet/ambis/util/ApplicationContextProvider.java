package com.trinet.ambis.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * utility class to get the application context
 * @author sbhalodia
 *
 */
public class ApplicationContextProvider implements ApplicationContextAware {
	private static ApplicationContext applicationContext;
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ApplicationContextProvider.applicationContext = applicationContext;
	}
	public static ApplicationContext getApplicationContext(){
		if (applicationContext == null){
			throw new IllegalStateException("ApplicationContext is null");
		}
		return applicationContext;
	}

}
