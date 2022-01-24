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
 * @date 2022/1/21 16:34
 */
public class CustomServiceImpl4 implements CustomSerivce {
    private static Logger logger = LoggerFactory.getLogger(CustomServiceImpl4.class);

    @Override
    public void execute() {
        logger.info("CustomServiceImpl4 execute");
    }
}
