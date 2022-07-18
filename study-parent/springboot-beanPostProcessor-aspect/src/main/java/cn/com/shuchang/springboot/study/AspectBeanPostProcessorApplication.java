/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import cn.com.shuchang.springboot.study.service.ICustomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/7/18 10:53
 */

@SpringBootApplication
@EnableAspectJAutoProxy
public class AspectBeanPostProcessorApplication {

    private static final Logger logger = LoggerFactory.getLogger(AspectBeanPostProcessorApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AspectBeanPostProcessorApplication.class, args);
        ICustomService service = (ICustomService)context.getBean("customServiceImpl");
        service.test();
    }
}
