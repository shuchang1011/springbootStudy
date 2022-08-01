/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/7/28 15:30
 */

@SpringBootApplication
public class LifecycleApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(LifecycleApplication.class, args);
        // 显示调用start和stop，触发Lifecycle实现Bean
        // context.start();
        // context.stop();
    }
}
