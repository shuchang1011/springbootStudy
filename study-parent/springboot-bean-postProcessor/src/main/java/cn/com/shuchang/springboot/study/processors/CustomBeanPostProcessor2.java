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
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.core.Ordered;

/**
 * InstantiationAwareBanPostProcessor继承了BeanPostProcessor接口，并在其基础上新增了几个方法
 * postProcessBeforeInstantiation:在Bean实例化前执行，即new Bean()之前
 * postProcessAfterInstantiation: 在Bean实例化后执行，即new Bean()之后
 * postProcessProperties: bean已经实例化完成，在属性注入时阶段触发，@Autowired,@Resource等注解原理基于此方法实现
 * 触发顺序：
 * postProcessBeforeInstantiation > postProcessAfterInstantiation > postProcessProperties > BeanPostProcessor中的俩方法
 * BeanPostProcessor中的方法，是在Bean实例化后，将Bean注入到spring上下文前后触发的，因此最晚触发
 * 
 * 实现Ordered接口，保证CustomBeanPostProcessor2的实例化先于CustomBeanPostProcessor1，便于拦截通过Component声明加载的CustomBeanPostProcessor1
 * @author shuchang
 * @version 1.0
 * @date 2022/1/25 15:09
 */

public class CustomBeanPostProcessor2 implements InstantiationAwareBeanPostProcessor, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(CustomBeanPostProcessor2.class);

    private static final String POST_PROCESS_BEAN_NAME = "cn.com.shuchang.springboot.study.service.impl.CustomServiceImpl1";
    private static final String POST_PROCESS_NAME = "cn.com.shuchang.springboot.study.processors.CustomBeanPostProcessor1";
    
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        //由于注册成BeanPostProcessor的Bean后，会在每个Bean的实例化过程触发，会多次触发打印日志的操作
        //这里我们多加一个判断，只测试CustomrService类型的Bean实例化前后触发postProcess操作
        // 匹配通过Component声明装载的CustomBeanPostProcessor1，对其实例化进行拦截
        if (beanName.equals(POST_PROCESS_BEAN_NAME) || beanName.equals(POST_PROCESS_NAME)) {
            logger.info("invoke CustomBeanPostProcessor2.postProcessBeforeInstantiation {}", beanName);
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
        if (beanName.equals(POST_PROCESS_BEAN_NAME)) {
            logger.info("invoke CustomBeanPostProcessor2.postProcessBeforeInitialization");
        }
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.equals(POST_PROCESS_BEAN_NAME)) {
            logger.info("invoke CustomBeanPostProcessor2.postProcessAfterInitialization");
        }
        return bean;
    }


    @Override
    public int getOrder() {
        return 1;
    }
}
