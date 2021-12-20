/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import cn.com.shuchang.springboot.study.service.ListenerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author shuchang
 * @version 1.0
 * @date 2021/12/20 16:41
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SecondListenerTest {

    @Autowired
    @Qualifier("listenerServiceImpl")
    private ListenerService listenerService;

    @Test
    public void testEvent()
    {
        listenerService.publishEvent();
    }
}
