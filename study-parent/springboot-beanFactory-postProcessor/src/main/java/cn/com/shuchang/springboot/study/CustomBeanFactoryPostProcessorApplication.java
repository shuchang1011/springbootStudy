/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import cn.com.shuchang.springboot.study.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 在bean实例化前，可以通过实现BeanFactoryPostProcessor接口创建一个自定义后置处理器，修改Bean的属性配置等
 * 实现步骤：
 * 1.自定义后置处理器实现BeanFactoryPostProcessor接口，在postProcessBeanFactory中，
 * 获取需要修改的BeanDefinition，设置其属性配置等;注意：需要通过@Component注解声明为Bean,否则BeanFactory无法加载到该自定义PostProcessor
 * 2.定义一个服务类，并通过@Service注解声明为一个Bean，方便自定义BeanFactoryPostProcessor对其做出修改
 *
 * @author shuchang
 * @version 1.0
 * @date 2022/1/18 14:28
 */

@SpringBootApplication
public class CustomBeanFactoryPostProcessorApplication {

    private static final Logger logger = LoggerFactory.getLogger(CustomBeanFactoryPostProcessorApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CustomBeanFactoryPostProcessorApplication.class, args);
        UserService userServiceImpl = (UserService)context.getBean("userServiceImpl");
        logger.info(userServiceImpl.getName());
    }
}
