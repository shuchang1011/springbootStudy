/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.initializers;

import cn.com.shuchang.springboot.study.processors.CustomBeanFactoryPostProcessor2;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 通过自定义Initializer实现在准备容器prepareContext阶段，注入自定义BeanFactoryPostProcessor
 * 自定义Initializer需在spring.factories中指定org.springframework.context.ApplicationContextInitializer为该initializer
 * 由于ApplicationContextPrepared事件触发在initialize执行后，
 * 因此通过listener装载的后置处理器调用晚于通过Initializer装载的后置处理器，即前者会覆盖后者的值
 * @author shuchang
 * @version 1.0
 * @date 2022/1/18 16:02
 */

public class CustomApplicationContextInitializer implements ApplicationContextInitializer {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.addBeanFactoryPostProcessor(new CustomBeanFactoryPostProcessor2());
    }
}
