/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.aspect;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 自定义Aspect
 * 注意：需要通过@Component组件将Aspect切面类注册到IOC容器中
 * 
 * @author shuchang
 * @version 1.0
 * @date 2022/7/18 10:57
 */

@Aspect
@Component
public class CustomAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomAspect.class);
    
    @Pointcut(value = "execution(* cn.com.shuchang.springboot.study.service.impl.*.*(..))")
    public void pointCut(){}
    
    @Before("pointCut()")
    public void beforeMethod(){
        logger.info("invoke aspect's beforeMethod");
    }

    @After("pointCut()")
    public void afterMethod(){
        logger.info("invoke aspect's afterMethod");
    }
    
}
