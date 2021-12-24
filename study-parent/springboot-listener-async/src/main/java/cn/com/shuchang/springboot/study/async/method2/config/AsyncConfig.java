/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.async.method2.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 配置自定义异步执行的线程池，并开启异步调用
 * 也可以将@EnableAsync注解添加在启动类上
 *
 * @author shuchang
 * @version 1.0
 * @date 2021/12/21 15:46
 */

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    private ApplicationContext applicationContext;

    private static final String DEFAULT_THREADPOOL_CORE_SIZE = "springboot.threadpool.coresize";
    private static final String DEFAULT_THREADPOOL_MAX_SIZE = "springboot.threadpool.maxsize";
    private static final String DEFAULT_THREADPOOL_KEEPALIVE_TIME = "springboot.threadpool.alivetime";
    private static final String DEFAULT_THREADPOOL_QUEUE_SIZE = "springboot.threadpool.queuesize";

    /**
     * 自定义异步线程池，若不重写会使用默认的线程池
     */
    @Override
    public Executor getAsyncExecutor(){
        String coreSize = applicationContext.getEnvironment().getProperty(DEFAULT_THREADPOOL_CORE_SIZE);
        String maxSize = applicationContext.getEnvironment().getProperty(DEFAULT_THREADPOOL_MAX_SIZE);
        String aliveTime = applicationContext.getEnvironment().getProperty(DEFAULT_THREADPOOL_KEEPALIVE_TIME);
        String queueSize = applicationContext.getEnvironment().getProperty(DEFAULT_THREADPOOL_QUEUE_SIZE);
        return  new ThreadPoolExecutor(Integer.parseInt(coreSize), Integer.parseInt(maxSize),
                Long.parseLong(aliveTime), TimeUnit.SECONDS, new ArrayBlockingQueue<>(Integer.parseInt(queueSize)));
    }

    /**
     * 自定义异步捕获处理器
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler(){
        return new MyAsyncUncaughtExceptionHandler();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    class MyAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler{

        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            logger.error(ex.getMessage());
        }
    }
}
