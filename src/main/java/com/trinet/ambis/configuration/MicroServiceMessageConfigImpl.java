package com.trinet.ambis.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.trinet.common.MicroServiceMessageConfig;

@Component
public class MicroServiceMessageConfigImpl implements MicroServiceMessageConfig{
    private static final Logger LOGGER = LoggerFactory.getLogger(MicroServiceMessageConfigImpl.class);

    @Autowired
    private Environment environment;

    @Override
    public String getProperty(String key) {
        LOGGER.debug("Getting property  : {} ", key);
        return environment.getProperty(key);
    }
}