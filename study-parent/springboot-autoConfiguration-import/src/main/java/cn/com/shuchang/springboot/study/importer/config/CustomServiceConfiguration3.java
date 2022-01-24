/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.importer.config;

import cn.com.shuchang.springboot.study.importer.registrar.CustomImportBeanDefinitionRegistrar;
import cn.com.shuchang.springboot.study.importer.select.CustomImportSelector;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.context.annotation.Import;

/**
 * 创建一个配置类，并声明@Import注解
 * 在refresh阶段，通过ConfigurationClassPstProcessor解析配置类时，扫描@Import注解中的ImportBeanDefinitionRegistrar实现
 * 将该注册扩展类缓存到ConfigClass中，等到parse解析完成后，调用this.reader.loadBeanDefinition(configClasses)
 * 触发ImportBeanDefinitionRegistrar中的registerBeanDefinitions()方法，注册相关的BeanDefinition至BeanFactory
 *
 * @author shuchang
 * @version 1.0
 * @date 2022/1/24 10:11
 */

@Configuration
@ComponentScan(basePackages = "cn.com.shuchang.springboot.study.service.impl")
@Import(CustomImportBeanDefinitionRegistrar.class)
public class CustomServiceConfiguration3 {
}
