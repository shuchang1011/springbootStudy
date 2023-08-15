/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import cn.com.shuchang.springboot.study.bean.CBean;
import cn.com.shuchang.springboot.study.context.CustomAnnotationServletWebServerApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 自定义应用上下文，可以继承默认的web上下文，完成一些额外的操作。例如，提前装配一些bean，修改环境变量等等；在web
 * 实现方式：
 * 1.继承web应用上下文AnnotationConfigServletWebServerApplicationContext，自定义实现CustomAnnotationServletWebServerApplicationContext，并重写相关方法完成自定义操作
 * 2.在启动类中，通过SpringApplication装配指定的自定义上下文。
 *    原理：在创建上下文阶段createApplicationContext实现中，首先会判断是否设置了应用上下文的实现类，如果没有设置，则会根据webApplicationType来决定初始化的web上下文
 *          故，可以在创建SpringApplication.setApplicationContextClass来决定加载的上下文
 *          注意：需继承web上下文，否则web应用容器无法正常初始化启动
 *
 * @author shuchang
 * @version 1.0
 * @date 2022/1/4 17:20
 */

@SpringBootApplication
public class CustomApplicationContext {

    private static final Logger logger = LoggerFactory.getLogger(CustomApplicationContext.class);

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(CustomApplicationContext.class);
        springApplication.setApplicationContextClass(CustomAnnotationServletWebServerApplicationContext.class);
        ConfigurableApplicationContext context = springApplication.run(args);

        // 下面是错误案例，通过new创建的对象是不同于工厂中已经初始化的Bean的，故TestService中的CBean一定是空的
        /*TestService testService = new TestService();
        testService.invoke();*/

        //正确案例
        TestService testService = (TestService)context.getBean("testService");
        testService.invoke();
        // 通过beanFactory.registerResolvableDependency注册的Bean,无法通过BeanFactory.getBean获取到
        //  因为，BeanFactory在加载这两个注解注入的依赖时，会通过DefaultListableBeanFactory#doResolveDependency()去加载装配到BeanFactory中的resolvableDependencies的依赖
        try {
            if (context.getBean(CBean.class) != null) {
                logger.info("CBean has been got by BeanFactory.getBean");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        testService.invokeDuplicateBean();



    }


}
