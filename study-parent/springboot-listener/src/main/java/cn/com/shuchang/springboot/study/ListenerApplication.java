/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import cn.com.shuchang.springboot.study.listener.FirstListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 自定义监听器主要是为了方便用户在spring各个生命周期阶段添加自定义触发的事件，并实行监听
 * 实现方式主要有四种：
 * 1.在初始化启动的时候，手动装载listener
 * 2.自定义监听器通过Component组件的形式装载的Bean工厂
 * 3.在resources目录下添加META-INF/spring.factories文件，
 * 并在org.springframework.context.ApplicationListener属性中添加自定义监听器
 * 4.实现SmartApplicationListener，该接口继承了ApplicationListener，
 * 且添加了supportsEventType方法，可以对触发事件进行过滤
 *
 * @author shuchang
 * @version 1.0
 * @date 2021/12/20 15:39
 */
@SpringBootApplication
public class ListenerApplication {

    public static void main(String[] args) {
        //SpringApplication.run(ListenerApplication.class, args);
        // TODO: 1.在初始化启动的时候，手动装载listener
        SpringApplication springApplication = new SpringApplication(ListenerApplication.class);
        springApplication.addListeners(new FirstListener());
        ConfigurableApplicationContext context = springApplication.run(args);
    }
}
