package cn.com.shuchang.springboot.study.annotation;/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */

import cn.com.shuchang.springboot.study.importer.config.CustomServiceConfiguration3;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
* CustomService使用注解
* 通过@Import注解引用配置类，加载配置类后，通过配置类来获取对应的BeanDefinitionRegistrar
*/

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CustomServiceConfiguration3.class)
public @interface EnableCustomService {
}
