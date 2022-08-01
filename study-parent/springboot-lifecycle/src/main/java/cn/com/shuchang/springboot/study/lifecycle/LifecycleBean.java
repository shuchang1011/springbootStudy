/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/7/28 16:34
 */
public class LifecycleBean implements Lifecycle {
    
    private static final Logger logger = LoggerFactory.getLogger(LifecycleBean.class);
    
    public boolean running = false;
    
    
    @Override
    public void start() {
        logger.info("invoke LifecycleBean's start method");
        this.running = true;
    }

    @Override
    public void stop() {
        logger.info("invoke LifecycleBean's stop method");
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }
}
