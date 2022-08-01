/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.runners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/8/1 17:27
 */
public class CustomApplicationRunner implements ApplicationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomApplicationRunner.class);
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("invoke CustomApplicationRunner's method [run]");
    }
}
