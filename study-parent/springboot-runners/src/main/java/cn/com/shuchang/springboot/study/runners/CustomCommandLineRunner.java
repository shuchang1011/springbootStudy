/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.runners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/8/1 17:28
 */

@Component
public class CustomCommandLineRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CustomCommandLineRunner.class);
   
    @Override
    public void run(String... args) throws Exception {
        logger.info("invoke CustomCommandLineRunner's method [run]");
    }
}

