package cn.com.shuchang.springboot.study.async;

import cn.com.shuchang.springboot.study.async.method1.service.AsyncService1;
import cn.com.shuchang.springboot.study.async.method2.service.AsyncService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AsyncServiceImplTest {

    @Autowired
    @Qualifier("asyncServiceImpl1")
    private AsyncService1 asyncService1;

    @Autowired
    @Qualifier("asyncServiceImpl2")
    private AsyncService asyncService2;

    @Test
    public void testEvent1()
    {
        for (int i = 0; i < 16; i++) {
            asyncService1.invokeAsyncMethod();
        }
    }


    @Test
    public void testEvent2()
    {
        for (int i = 0; i < 16; i++) {
            asyncService2.invokeAsyncMethod();
        }
    }

}
