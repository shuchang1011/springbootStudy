/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.initializers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/1/4 17:31
 */
public class CustomApplicationContextInitializer1 implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger logger = LoggerFactory.getLogger(CustomApplicationContextInitializer1.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");
        PropertiesPropertySource initializerProperties = new PropertiesPropertySource("initializerProperties", properties);
        environment.getPropertySources().addLast(initializerProperties);
        logger.info("invoke CustomApplicationContextInitializer1");
    }
}
