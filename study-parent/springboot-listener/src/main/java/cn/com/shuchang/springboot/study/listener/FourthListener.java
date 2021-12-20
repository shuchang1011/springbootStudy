/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.listener;

import cn.com.shuchang.springboot.study.events.FourthEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

/**
 * @author shuchang
 * @version 1.0
 * @date 2021/12/20 17:10
 */
public class FourthListener implements SmartApplicationListener {

    private static final Logger logger = LoggerFactory.getLogger(FourthListener.class);

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return eventType.equals(ApplicationStartingEvent.class)
                || eventType.equals(FourthEvent.class);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent) {
            logger.info("fourthListener: application is starting");
        } else {
            ((FourthEvent)event).printMsg();
        }
    }
}
