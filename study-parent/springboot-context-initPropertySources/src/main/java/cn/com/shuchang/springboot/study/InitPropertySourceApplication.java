/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import cn.com.shuchang.springboot.study.context.MyWebApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 通过继承AbstractApplicationContext的子类，重写initPropertySources()方法来自定义设置一些属性到Environment中
 * 实现步骤：
 * 1.因为我们是以web应用的方式启动springboot，因此，可以继承其实现类AnnotationConfigServletWebApplicationContext，并重写重写initPropertySources
 * 2.在initPropertySources方法中，往环境Environment中设置必要属性，启动测试是否生效
 * 3.在启动类中，通过SpringApplication.setApplicationContext设置自定义容器
 *
 * @author shuchang
 * @version 1.0
 * @date 2022/1/14 16:52
 */

@SpringBootApplication
public class InitPropertySourceApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(InitPropertySourceApplication.class);
        application.setApplicationContextClass(MyWebApplicationContext.class);
        application.run(args);
    }
}
