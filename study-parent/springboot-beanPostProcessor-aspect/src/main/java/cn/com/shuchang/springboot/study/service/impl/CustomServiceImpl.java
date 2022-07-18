/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.service.impl;

import cn.com.shuchang.springboot.study.service.ICustomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/7/18 10:58
 */
@Service
public class CustomServiceImpl implements ICustomService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomServiceImpl.class);
    @Override
    public void test() {
        logger.info("invoke customService.method[test()]");
    }
}
