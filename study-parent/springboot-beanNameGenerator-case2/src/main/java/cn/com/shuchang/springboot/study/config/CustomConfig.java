/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.config;

import cn.com.shuchang.springboot.study.common.CustomBeanNameGenerator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/1/4 14:28
 */

@Configuration
@ComponentScan(basePackages = "cn.com.shuchang.springboot.study.pojo", nameGenerator = CustomBeanNameGenerator.class)
public class CustomConfig {
}
