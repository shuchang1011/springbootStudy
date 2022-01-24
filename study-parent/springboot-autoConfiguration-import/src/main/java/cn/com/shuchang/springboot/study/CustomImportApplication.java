/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import cn.com.shuchang.springboot.study.common.Constants;
import cn.com.shuchang.springboot.study.service.CustomSerivce;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/1/21 16:22
 */

@SpringBootApplication
public class CustomImportApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CustomImportApplication.class, args);
        CustomSerivce serviceImpl1 = (CustomSerivce)context.getBean(Constants.IMPORT_SERVICE_CLASS1);
        CustomSerivce serviceImpl2 = (CustomSerivce)context.getBean(Constants.IMPORT_SERVICE_CLASS2);
        CustomSerivce serviceImpl3 = (CustomSerivce)context.getBean(Constants.IMPORT_SERVICE_CLASS3);
        serviceImpl1.execute();
        serviceImpl2.execute();
        serviceImpl3.execute();
    }
}
