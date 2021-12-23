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
//TODO: 3.在resources目录下添加META-INF/spring.factories文件，
// 并在org.springframework.context.ApplicationListener属性中添加自定义监听器
public class ThirdListener implements ApplicationListener<ThirdEvent> {
    @Override
    public void onApplicationEvent(ThirdEvent event) {
        event.printMsg();
    }
}
