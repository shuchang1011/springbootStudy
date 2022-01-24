/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.service;

import cn.com.shuchang.springboot.study.common.Constants;
import cn.com.shuchang.springboot.study.service.impl.CustomServiceImpl3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Service;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/1/24 15:34
 */

@Service
public class TestService {

    @Autowired
    @Qualifier(Constants.IMPORT_SERVICE_CLASS3)
    private CustomServiceImpl3 service;

    public void execute() {
        service.execute();
    }
}
