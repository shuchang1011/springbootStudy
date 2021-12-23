/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.service.impl;

import cn.com.shuchang.springboot.study.events.FourthEvent;
import cn.com.shuchang.springboot.study.events.SecondEvent;
import cn.com.shuchang.springboot.study.events.ThirdEvent;
import cn.com.shuchang.springboot.study.service.ListenerService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * @author shuchang
 * @version 1.0
 * @date 2021/12/20 16:25
 */

@Service
public class ListenerServiceImpl implements ListenerService, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(org.springframework.context.ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void publishEvent() {
        //ApplicationContext实现了ApplicationEventPublisher接口，SpringBoot可以通过ApplicationEventPublisher来发布事件
        applicationContext.publishEvent(new SecondEvent(this, "invoke SecondEvent"));
        applicationContext.publishEvent(new ThirdEvent(this, "invoke ThirdEvent"));
        applicationContext.publishEvent(new FourthEvent(this, "invoke FourthEvent"));
    }
}
