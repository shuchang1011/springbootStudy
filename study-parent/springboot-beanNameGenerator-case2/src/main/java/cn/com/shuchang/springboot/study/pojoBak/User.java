/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.pojoBak;

import org.springframework.stereotype.Component;

/**
 * 构建不同包下出现同名类的情况
 *
 * @author shuchang
 * @version 1.0
 * @date 2022/1/4 14:49
 */
@Component
public class User {

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
