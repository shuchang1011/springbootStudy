/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.service.impl;

import cn.com.shuchang.springboot.study.service.CustomSerivce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author shuchang
 * @version 1.0
 * @date 2022/1/21 16:33
 */
public class CustomServiceImpl1 implements CustomSerivce {
    private static Logger logger = LoggerFactory.getLogger(CustomServiceImpl1.class);

    @Override
    public void execute() {
        logger.info("CustomServiceImpl1 execute");
    }
}
