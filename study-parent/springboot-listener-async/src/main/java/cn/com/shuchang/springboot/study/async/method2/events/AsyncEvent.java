/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.async.method2.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

/**
 * @author shuchang
 * @version 1.0
 * @date 2021/12/21 15:00
 */
public class AsyncEvent extends ApplicationEvent {

    private static final Logger logger = LoggerFactory.getLogger(AsyncEvent.class);

    private String msg;

    public AsyncEvent(Object source, String msg) {
        super(source);
        this.msg = msg;
    }

    public void printMsg() {
        logger.info("methods2 async event:{}", msg);
    }
}
