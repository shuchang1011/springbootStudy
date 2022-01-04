/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.common;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.beans.Introspector;

/**
 * 参考Springboot默认BeanNameGenerator实现AnnotationBeanNameGenerator实现
 *
 * @author shuchang
 * @version 1.0
 * @date 2022/1/4 14:11
 */
public class CustomBeanNameGenerator implements BeanNameGenerator {
    @Override
    public String generateBeanName(BeanDefinition beanDefinition, BeanDefinitionRegistry beanDefinitionRegistry) {
        // 修改返回的beanName为全限定名称
        String beanClassName = beanDefinition.getBeanClassName();
        //Assert.state(beanClassName != null, "No bean class name set");
        //String shortClassName = ClassUtils.getShortName(beanClassName);
        return beanClassName;
    }
}
