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
 * 通过自定义监听器，监听refresh前的事件
 * 并在事件触发时，通过ConfigurableApplicationContext.addBeanFactoryPostProcessor来装载该后置处理器
 * 且通过监听器装载的BeanFactoryPostProcessor在refresh前，因此比通过@Component声明的后置处理器调用要早
 * （调用顺序：CustomBeanFactoryPostProcessor1 > CustomBeanFactoryPostProcessor）
 * 且后调用的会覆盖前调用的
 *
 * @author shuchang
 * @version 1.0
 * @date 2022/1/18 15:24
 */
public class CustomBeanFactoryPostProcessor1 implements BeanFactoryPostProcessor {

    private static final String USER_SERVICE_NAME = "userServiceImpl";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 通过BeanFactory获取相应的beandefinition，在实例化前，通过postProcessBeanFactory修改bean的属性
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(USER_SERVICE_NAME);
        beanDefinition.getPropertyValues().addPropertyValue("description", "description_modified");
    }
}
