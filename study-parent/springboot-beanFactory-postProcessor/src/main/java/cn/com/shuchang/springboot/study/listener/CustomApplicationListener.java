/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.listener;

import cn.com.shuchang.springboot.study.processors.CustomBeanFactoryPostProcessor1;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/1/18 15:26
 */

//需要注意，这里不能通过@Component注解来声明listener
//因为通过该注解声明的Bean加载在refresh阶段，此时已经错过了ApplicationPrepared阶段，不会触发该事件
//因此，需要在spring.factories中指定ApplicationListener属性为该listener
//@Component
public class CustomApplicationListener implements SmartApplicationListener, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        if (eventType.equals(ApplicationPreparedEvent.class)) {
            return true;
        }
        return false;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) this.applicationContext;
        context.addBeanFactoryPostProcessor(new CustomBeanFactoryPostProcessor1());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
