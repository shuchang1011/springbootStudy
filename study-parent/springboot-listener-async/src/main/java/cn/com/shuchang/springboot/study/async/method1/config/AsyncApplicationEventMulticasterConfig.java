/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.async.method1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * 通过配置类，注册spring提供的SimpleApplicationEventMulticaster，其内部提供了线程池的设置
 *
 * @author shuchang
 * @version 1.0
 * @date 2021/12/23 17:45
 */

@Configuration
public class AsyncApplicationEventMulticasterConfig {

    @Bean("applicationEventMulticaster")
    public ApplicationEventMulticaster getApplicationEventMulticaster(){
        SimpleApplicationEventMulticaster simpleApplicationEventMulticaster = new SimpleApplicationEventMulticaster();
        //注入异步任务处理器
        simpleApplicationEventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return simpleApplicationEventMulticaster ;
    }
}
