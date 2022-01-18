/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.processors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/1/18 15:59
 */
public class CustomBeanFactoryPostProcessor2 implements BeanFactoryPostProcessor {

    private static final String USER_SERVICE_NAME = "userServiceImpl";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 通过BeanFactory获取相应的beandefinition，在实例化前，通过postProcessBeanFactory修改bean的属性
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(USER_SERVICE_NAME);
        beanDefinition.getPropertyValues().addPropertyValue("comment", "comment_modified");
    }
}
