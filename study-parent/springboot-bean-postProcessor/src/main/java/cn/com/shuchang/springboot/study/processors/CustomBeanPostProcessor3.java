/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;

import java.lang.reflect.Constructor;

/**
 * SmartInstantiationAwareBeanPostProcessor继承了InstantiationAwareBeanPostProcessor，在其基础上添加了以下三个方法
 * predictBeanType(不常用)：该触发点发生在postProcessBeforeInstantiation之前，这个方法用于预测Bean的类型，返回第一个预测成功的Class类型，如果不能预测返回null
 * determineCandidateConstructors：该触发点发生在postProcessBeforeInstantiation之后，用于确定该bean的构造函数之用，返回的是该bean的所有构造函数列表。用户可以扩展这个点，来自定义选择相应的构造器来实例化这个bean。
 * getEarlyBeanReference：该触发点发生在postProcessAfterInstantiation之后，当有循环依赖的场景，当bean实例化好之后，为了防止有循环依赖，会提前暴露回调方法，用于bean实例化的后置处理
 *
 * @author shuchang
 * @version 1.0
 * @date 2022/1/25 18:11
*/
public class CustomBeanPostProcessor3 implements SmartInstantiationAwareBeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CustomBeanPostProcessor2.class);

    private static final String POST_PROCESS_BEAN_NAME = "cn.com.shuchang.springboot.study.service.impl.CustomServiceImpl1";

    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        //由于注册成BeanPostProcessor的Bean后，会在每个Bean的实例化过程触发，会多次触发打印日志的操作
        //这里我们多加一个判断，只测试CustomrService类型的Bean实例化前后触发postProcess操作
        if (beanName.equals(POST_PROCESS_BEAN_NAME)) {
            logger.info("invoke CustomBeanPostProcessor2.postProcessBeforeInstantiation");
        }
        return null;
    }

    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        if (beanName.equals(POST_PROCESS_BEAN_NAME)) {
            logger.info("invoke CustomBeanPostProcessor2.postProcessAfterInstantiation");
        }
        return true;
    }

    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
            throws BeansException {
        if (beanName.equals(POST_PROCESS_BEAN_NAME)) {
            logger.info("invoke CustomBeanPostProcessor2.postProcessProperties");
        }
        //具体实现可以参考AutowiredAnnotationBeanPostProcessor
        //@Autowired自动注入就是基于此处理器实现
        return null;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {
        if (beanName.equals(POST_PROCESS_BEAN_NAME)) {
            logger.info("invoke CustomBeanPostProcessor2.predictBeanType");
        }
        return null;
    }

    public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName)
            throws BeansException {
        if (beanName.equals(POST_PROCESS_BEAN_NAME)) {
            logger.info("invoke CustomBeanPostProcessor2.determineCandidateConstructors");
        }
        return null;
    }

    public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
        if (beanName.equals(POST_PROCESS_BEAN_NAME)) {
            logger.info("invoke CustomBeanPostProcessor2.getEarlyBeanReference");
        }
        return bean;
    }

}
