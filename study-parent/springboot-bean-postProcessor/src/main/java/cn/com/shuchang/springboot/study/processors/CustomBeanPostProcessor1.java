/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.processors;

import cn.com.shuchang.springboot.study.service.CustomService;
import cn.com.shuchang.springboot.study.service.impl.CustomServiceImpl1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * BeanPostProcessor是在refresh阶段通过ConfigurationClassPostProcessor解析Component组件时，
 * 将该类的BeanDefinition装载到BeanFactory的BeanDefinitionMap中，
 * 然后在触发registerBeanPostProcessors时从beanDefinitionMap中加载出来进行实例化
 * @author shuchang
 * @version 1.0
 * @date 2022/1/25 15:09
 */
@Component
public class CustomBeanPostProcessor1 implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CustomBeanPostProcessor1.class);

    private static final String POST_PROCESS_BEAN_NAME = "cn.com.shuchang.springboot.study.service.impl.CustomServiceImpl1";

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof CustomService && beanName.equals(POST_PROCESS_BEAN_NAME)) {
            logger.info(POST_PROCESS_BEAN_NAME + "{} before initialize, property[description={}]", POST_PROCESS_BEAN_NAME, ((CustomServiceImpl1)bean).getDescription());
        }
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof CustomService && beanName.equals(POST_PROCESS_BEAN_NAME)) {
            ((CustomService) bean).modifyDescription("modified_description");
            logger.info(POST_PROCESS_BEAN_NAME + "has been modified by {}, the value is:{}", this.getClass().getName(), ((CustomServiceImpl1)bean).getDescription());
        }
        return bean;
    }
}
