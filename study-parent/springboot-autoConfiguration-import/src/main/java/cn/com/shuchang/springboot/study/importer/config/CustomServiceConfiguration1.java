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
import org.springframework.context.annotation.Import;

/**
 * 创建一个配置类，并声明@Import注解
 * 在refresh阶段，通过ConfigurationClassPstProcessor解析配置类时，扫描@Import注解中的Selector实现
 * 在processImport中，调用Selector的selectImports实现，获取需要加载的Bean的类信息
 * 最后通过processConfigurationClass方法，将bean全部转化成ConfigurationClass对象
 * 最后在parse解析完成后，通过this.reader.loadBeanDefinitions()变成BeanDefinition，再放入到BeanDefinitionMap中
 *
 * @author shuchang
 * @version 1.0
 * @date 2022/1/24 10:11
 */

@Configuration
@ComponentScan("cn.com.shuchang.springboot.study.service.impl")
@Import(CustomImportSelector.class)
public class CustomServiceConfiguration1 {
}
