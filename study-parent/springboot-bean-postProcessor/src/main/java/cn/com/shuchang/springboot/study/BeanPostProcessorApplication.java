/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import cn.com.shuchang.springboot.study.service.CustomService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

/**
 * 通过BeanPostProcessor对Bean生命周期进行管理进行控制
 * Bean的实例化生命周期如下：
 *  实例化 Instantiation
 *  属性赋值 Populate
 *  初始化 Initialization
 *  销毁 Destruction
 * 实现步骤：
 *  1.定义一个服务提供Bean:CustomService，并通过@Service声明为Bean
 *  2.创建一个自定义BeanPostProcessor，并实现BeanPostProcessor，其声明了两个操作postProcessBeforeInitialization和postProcessAfterInitialization
 *      这两个操作对应了声明周期中初始化前后的过程，开发人员可以通过实现这两个方法来对Bean的初始化前后进行控制
 *  2.1 开发人员亦可以实现BeanPostProcessor的子接口InstantiationAwareBeanPostProcessor，其在前者的基础上增加了对于属性赋值过程的控制postProcessProperties
 *      通过postProcessProperties可以对属性赋值时修改赋值的内容
 *  2.2 亦可以通过实现InstantiationAwareBeanPostProcessor的子接口SmartInstantiationAwareBeanPostProcessor
 *      其在前者的基础上还新增了对于Bean构造函数调用的控制等
 *  3.上述创建的BeanPostProcessor需注册到IOC工厂，因此可以通过@Component注解的方式声明为Bean对象；也可以在spring.factories中指定BeanPostProcessors为当前实现；
 *      亦可以通过配置类的形式通过@Bean或者@Import导入该PostProcessor；
 *      若需要通过start提供给其他应用使用，可以在spring.factories中定义EnableAutoConfiguration为当前配置类，再通过配置注册postProcessor
 * @author shuchang
 * @version 1.0
 * @date 2022/1/25 14:36
 */

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"cn.com.shuchang.springboot.study.common","cn.com.shuchang.springboot.study.importer","cn.com.shuchang.springboot.study.processors","cn.com.shuchang.*"}, nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class BeanPostProcessorApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BeanPostProcessorApplication.class, args);
        CustomService serviceImpl1 = (CustomService)context.getBean("cn.com.shuchang.springboot.study.service.impl.CustomServiceImpl1");
        serviceImpl1.printDescription();
    }
}
