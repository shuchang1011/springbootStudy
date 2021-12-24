/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.async;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 在某些业务完成后，用户需要推送相关通知，这时，可以通过异步事件监听的机制来达到这一目的。
 *
 * 实现原理：事件监听其实都是靠ApplicationEventMulticaster来传播到装载的listener,然后触发各个listener的事件方法
 *         在传播事件到各个listener的过程中，会判断是否设置了线程池，来决定是否开启多线程触发各个listener的事件方法
 *         因此，只需要在applicationEventMulticaster实例化时，配置相应的线程池，即可实现事件的异步监听机制
 *
 * 具体操作流程如下：
 * 1.构建自定义异步监听事件，继承ApplicationEvent父类
 * 2.构建自定义异步监听器，实现SmartApplicationListener
 * 3.通过@Component注解或者在spring.factories文件中配置当前AsyncListener，将该异步监听器注册到bean容器中
 * 4.以上是正常注册自定义监听器的过程，下面阐述开启异步事件的步骤，主要有两种方法：
 * 4.1通过在初始化时配置名称为applicationEventMulticaster的bean的线程池，使得事件触发后，能多线程调用监听器的事件方法
 *    这个异步执行的过程仅限于事件触发后，调用监听器的事件方法
 * 4.2使用springboot的异步注解@Async,使用前提，需在启动类上使用@EnableAsync开启异步执行的功能。
 *    然后将@Async修饰在需要异步执行的方法上，这里也就对应着listener中的onApplicationEvent方法
 *    springboot使用的是默认的线程池，可以通过创建一个配置类，实现AsyncConfigurer的getExecutor方法，来生成对应的线程池
 *
 *
 * @author shuchang
 * @version 1.0
 * @date 2021/12/23 16:45
 */

@SpringBootApplication
public class AsyncListenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsyncListenerApplication.class, args);
    }
}
