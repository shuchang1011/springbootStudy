/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Phased;
import org.springframework.context.SmartLifecycle;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/7/28 16:34
 */
public class SmartLifecycleBean implements SmartLifecycle, Phased {

    private static final Logger logger = LoggerFactory.getLogger(LifecycleBean.class);

    public boolean running = false;
    
    @Override
    public void start() {
        logger.info("invoke SmartLifecycleBean‘s start method");
        this.running = true;
    }

    @Override
    public void stop() {
        logger.info("invoke SmartLifecycleBean‘s stop method");
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        logger.info("invoke callback method while stoping applicationContext");
        stop();
        callback.run();
        this.running = false;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
