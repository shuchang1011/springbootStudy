/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import cn.com.shuchang.springboot.study.common.CustomBeanNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

import java.util.Arrays;

/**
 * 通过自定义BeanNameGenerator实现beand definition装载时命名策略的自定义实现
 * 实现步骤如下：
 * 1.自定义CustomBeanNameGenerator实现接口BeanNameGenerator,可以springboot默认实现DefaultBeanNameGenerator
 * 2.使用配置类结合@ComponentScan注解，指定BeanNameGererator为CustomBeanNameGenerator
 * 3.由于入口类注解@SpringBootApplication种包含了@ComponentScan注解，因此，启动时，回基于默认的BeanNameGenerator去装载对应命名的Bean definition
 * 同时，加载到配置类时，又会触发一次扫描，基于自定义的BeanNameGenerator再次装载一个符合自定义命名策略的Bean Definition
 * 因此，自定义命名策略需要和默认的命名策略区分开，否则会导致命名冲突的问题出现
 * 4.如果存在不同包路径下有多个同名类，使用默认的BeanNameGenerator加载会有命名冲突的问题，这是可以使用自定义BeanNameGenerator实现全限定名的装载，
 * 或者使用spring提供的FullyQualifiedAnnotationBeanNameGenerator
 *
 * @author shuchang
 * @version 1.0
 * @date 2022/1/4 11:26
 */

@SpringBootApplication
//不使用时，多个同名文件会导致命名冲突
//使用spring提供的FullyQualifiedAnnotationBeanNameGenerator
//@ComponentScan(basePackages = "cn.com.shuchang.springboot.study", nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
//使用自定义BeanNameGenerator
@ComponentScan(basePackages = "cn.com.shuchang.springboot.study", nameGenerator = CustomBeanNameGenerator.class)
public class BeanNameGeneratorApplication {

    private static final Logger logger = LoggerFactory.getLogger(BeanNameGeneratorApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(BeanNameGeneratorApplication.class, args);
        Arrays.stream(applicationContext.getBeanDefinitionNames()).forEach(bean -> logger.info("bean name = {}", bean));
    }
}
