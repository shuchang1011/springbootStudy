/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.async.method1.listener;

import cn.com.shuchang.springboot.study.async.method1.events.AsyncEvent1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

/**
 *
 *
 * @author shuchang
 * @version 1.0
 * @date 2021/12/21 15:01
 */
public class AsyncListener1 implements SmartApplicationListener {

    private static final Logger logger = LoggerFactory.getLogger(AsyncListener1.class);

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return eventType.equals(ApplicationStartingEvent.class)
                || eventType.equals(AsyncEvent1.class);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof AsyncEvent1) {
            ((AsyncEvent1) event).printMsg();
        } else {
            logger.info("invoke event:{}" + event.getClass().getTypeName());
        }
    }
}
