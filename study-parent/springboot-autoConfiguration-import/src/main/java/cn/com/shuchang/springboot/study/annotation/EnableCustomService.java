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
 * 亦可以通过@Import加载配置类，配置类中通过ImportSelector来导入beanDefinition至beanFactory中
 * 两种方法的执行顺序不一致，后者先于前者执行
*/

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CustomServiceConfiguration3.class)
public @interface EnableCustomService {
}
