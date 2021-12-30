/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 构建配置类，通过@Value注解加载配置中的属性(实际不存在)，
 *
 * @author shuchang
 * @version 1.0
 * @date 2021/12/30 15:12
 */

@Configuration
public class TestConfig {

    @Value("${application.name}")
    private String value;

}
