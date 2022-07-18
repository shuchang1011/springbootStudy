/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.service.impl;

import cn.com.shuchang.springboot.study.service.CustomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/1/25 16:01
 */

@Service
public class CustomServiceImpl1 implements CustomService {

    private static final Logger logger = LoggerFactory.getLogger(CustomServiceImpl1.class);

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void execute() {
        logger.info(description);
    }

    @Override
    public void modifyDescription(String content) {
        this.description = content;
    }

    @Override
    public void printDescription() {
        logger.info("description:{}", description);
    }
}
