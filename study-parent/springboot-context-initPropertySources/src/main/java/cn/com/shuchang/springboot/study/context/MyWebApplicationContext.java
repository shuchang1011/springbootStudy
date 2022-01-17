/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.context;

import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.ConfigurableWebEnvironment;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/1/14 17:43
 */
public class MyWebApplicationContext extends AnnotationConfigServletWebApplicationContext {

    @Override
    protected void initPropertySources() {
        ConfigurableEnvironment env = getEnvironment();
        env.setRequiredProperties("MY_HOST");
    }

}
