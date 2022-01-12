/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 自定义ApplicationContextInitializer可以在容器刷新之前，执行回调函数，往spring的容器中注入属性
 * 实现方式：
 * 1.通过自定义`ApplicationContextInitializer`实现`ApplicationContextInitializer`接口
 * 2.通过配置spring.factories指定org.springframework.context.ApplicationContextInitializer为自定义ApplicationContextInitializer
 *   或者通过在配置文件application.yaml中配置contex.initializer.classes属性为自定义ApplicationContextInitializer
 *   后者调用优先级高于前者
 *
 * @author shuchang
 * @version 1.0
 * @date 2022/1/4 17:20
 */

@SpringBootApplication
public class CustomInitializerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomInitializerApplication.class, args);
    }
}
