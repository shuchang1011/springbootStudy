/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.service.impl;

import cn.com.shuchang.springboot.study.service.IDeclareParent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/7/25 14:35
 */
public class DeclareParentImpl implements IDeclareParent {
    
    private static final Logger logger = LoggerFactory.getLogger(DeclareParentImpl.class);
    
    @Override
    public void commonMethod() {
        logger.info("this is IDeclareParent‘s method");
    }
}
