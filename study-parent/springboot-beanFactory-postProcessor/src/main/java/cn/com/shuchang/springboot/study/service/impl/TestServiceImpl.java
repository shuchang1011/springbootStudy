/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.service.impl;

import cn.com.shuchang.springboot.study.service.TestService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/7/13 10:24
 */
@Component
@PropertySource(value = {"test.properties"})
@ConfigurationProperties(value = "test")
public class TestServiceImpl implements TestService {
    private String name;

    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
