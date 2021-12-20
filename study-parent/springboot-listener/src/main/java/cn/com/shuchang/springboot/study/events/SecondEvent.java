/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

/**
 * @author shuchang
 * @version 1.0
 * @date 2021/12/20 15:58
 */
public class SecondEvent extends ApplicationEvent {

    private static final Logger logger = LoggerFactory.getLogger(SecondEvent.class);

    private String msg;

    public SecondEvent(Object source, String msg) {
        super(source);
        this.msg = msg;
    }

    public void printMsg() {
        logger.info(msg);
    }
}
