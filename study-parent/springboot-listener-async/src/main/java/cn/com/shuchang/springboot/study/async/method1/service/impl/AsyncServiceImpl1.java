/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.async.method1.service.impl;

import cn.com.shuchang.springboot.study.async.method1.events.AsyncEvent1;
import cn.com.shuchang.springboot.study.async.method1.service.AsyncService1;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author shuchang
 * @version 1.0
 * @date 2021/12/21 15:42
 */
@Service
public class AsyncServiceImpl1 implements AsyncService1, ApplicationContextAware {

    private ApplicationContext applicationContext;

    //异步调用
    @Override
    @Async
    public void invokeAsyncMethod() {
        applicationContext.publishEvent(new AsyncEvent1(this, "random value = " + Math.random()));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
