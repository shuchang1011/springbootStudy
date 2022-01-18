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
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.stereotype.Component;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/1/18 14:29
 */
//需要通过@Component注解声明为Bean对象，否则BeanFactory无法加载到对应的Bean定义
@Component
public class CustomBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final String USER_SERVICE_NAME = "userServiceImpl";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 通过BeanFactory获取相应的beandefinition，在实例化前，通过postProcessBeanFactory修改bean的属性
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(USER_SERVICE_NAME);
        beanDefinition.getPropertyValues().addPropertyValue("name", "name_modified");
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
    }
}
