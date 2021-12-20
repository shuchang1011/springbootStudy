/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 在springboot启动的第二阶段：准备环境prepareEnvironment
 * 在这个阶段中，springboot会去加载容器环境，并且加载相应的配置，方便再后续Configuration中使用
 * 那么在该阶段如何设置相应配置呢？主要是通过EnvironmentPostProcessor后置处理器来实现的
 * 该处理器可以在环境准备阶段前后进行一定的逻辑处理
 *
 * 具体实现方式如下：
 * 1.自定义后置处理器，并实现EnvironmentPostProcess
 * 2.在resources目录下添加META-INF/spring.factories文件，
 * 并为org.springframework.boot.env.EnvironmentPostProcessor添加自定义处理器
 * 3.官方推荐在EnvironmentPostProcessor处理器上添加@Order注解，可以指定顺序设置配置
 *
 * @author shuchang
 * @version 1.0
 * @date 2021/12/20 18:07
 */

@SpringBootApplication
public class EnvironmentPostProcessorApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(EnvironmentPostProcessorApplication.class, args);
        String name = context.getEnvironment().getProperty("user.name");
        String gender = context.getEnvironment().getProperty("user.gender");
        System.out.println("name==="+name);
        System.out.println("gender==="+gender);
        context.close();
    }
}
