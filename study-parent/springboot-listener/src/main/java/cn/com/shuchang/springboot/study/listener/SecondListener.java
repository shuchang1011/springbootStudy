/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.listener;

import cn.com.shuchang.springboot.study.events.SecondEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author shuchang
 * @version 1.0
 * @date 2021/12/20 16:23
 */
// TODO: 2.自定义监听器通过Component组件的形式装载的Bean工厂
@Component
public class SecondListener implements ApplicationListener<SecondEvent> {
    @Override
    public void onApplicationEvent(SecondEvent event) {
        event.printMsg();
    }
}
