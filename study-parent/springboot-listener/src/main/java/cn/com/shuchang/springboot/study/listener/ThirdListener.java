/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.listener;

import cn.com.shuchang.springboot.study.events.ThirdEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author shuchang
 * @version 1.0
 * @date 2021/12/20 16:54
 */
public class ThirdListener implements ApplicationListener<ThirdEvent> {
    @Override
    public void onApplicationEvent(ThirdEvent event) {
        event.printMsg();
    }
}
