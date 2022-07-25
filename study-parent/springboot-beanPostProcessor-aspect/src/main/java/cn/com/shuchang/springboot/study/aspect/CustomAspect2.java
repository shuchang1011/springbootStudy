/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.aspect;

import cn.com.shuchang.springboot.study.service.IDeclareParent;
import cn.com.shuchang.springboot.study.service.impl.DeclareParentImpl;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;
import org.springframework.stereotype.Component;

/**
 * 演示切面类中@DeclareParents使用方式
 * @DeclareParents主要是为被代理类添加一个接口的所有方法实现，使得被代理类在未实现指定接口时，也能调用接口方法
 * 
 * @author shuchang
 * @version 1.0
 * @date 2022/7/25 14:30
 */

@Aspect
@Component
public class CustomAspect2 {
    
    @DeclareParents(value = "cn.com.shuchang.springboot.study.service.impl.CustomServiceImpl2", defaultImpl = DeclareParentImpl.class)
    public IDeclareParent declareParent;
}
