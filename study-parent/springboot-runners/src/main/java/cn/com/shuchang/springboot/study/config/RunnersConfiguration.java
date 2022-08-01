/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.config;

import cn.com.shuchang.springboot.study.runners.CustomApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/8/1 17:31
 */

@Configuration
public class RunnersConfiguration {
    
    @Bean
    public CustomApplicationRunner customApplicationRunner() {
        return new CustomApplicationRunner();
    }
}
