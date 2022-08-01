/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 这里主要演示ApplicationRunner和CommandLineRunner在应用启动完成后执行自定义操作的扩展实现
 * 具体实现如下所示：
 * 1.分别定义一个ApplicationRunner、CommandLineRunner接口的实现类CustomApplicationRunner、CommandLineRunner
 * 2.实现Ordered接口，保证同一类型的Runner的触发顺序
 * 3.通过@Component组件声明为Bean；
 *   或者定义一个配置类，然后在配置类中定义一个返回类型为Runner的方法，并使用@Bean注解声明；
 *   亦或是在配置类上声明@Import注解，通过指定的ImportSelector调用selectImport返回指定类型的BeanName
 * 4.定义两个Runner中的实现
 * 5.启动应用查看两个Runner的调用（ApplicationRunner 早于 CommandLineRunner调用）
 * 
 * @author shuchang
 * @version 1.0
 * @date 2022/8/1 17:17
 */
@SpringBootApplication
public class RunnersApplication {

    public static void main(String[] args) {
        new SpringApplication().run(RunnersApplication.class, args);
    }
}
