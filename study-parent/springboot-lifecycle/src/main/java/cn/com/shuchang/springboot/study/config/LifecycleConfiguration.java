/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.config;

import cn.com.shuchang.springboot.study.lifecycle.LifecycleBean;
import cn.com.shuchang.springboot.study.lifecycle.SmartLifecycleBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/7/28 16:47
 */
@Configuration
public class LifecycleConfiguration {
    
    @Bean
    public LifecycleBean lifecycleBean() {
        return new LifecycleBean();
    }

    @Bean
    public SmartLifecycleBean smartLifecycleBean() {
        return new SmartLifecycleBean();
    }
}
