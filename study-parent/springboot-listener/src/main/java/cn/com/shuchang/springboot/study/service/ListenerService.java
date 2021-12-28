/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.service;

/**
 * 获取上下文对象，并发布事件
 * 获取上下文对象方式：
 * 1.通过Autowire注解自动注入
 * 2.实现ApplicationContextAware接口
 *
 * @author shuchang
 * @version 1.0
 * @date 2021/12/20 16:25
 */
public interface ListenerService {

    public void publishEvent();
}
