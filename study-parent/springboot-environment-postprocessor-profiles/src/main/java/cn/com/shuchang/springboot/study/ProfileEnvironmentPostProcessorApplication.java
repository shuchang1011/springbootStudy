/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 在PrepareEnvironment阶段,会触发environmentPrepared事件,这时会去遍历注册的监听器
 * 其中，就包含了监听器ConfigFileApplicationListener.它会去加载项目的配置文件，例如profiles和yml都是由其内部加载
 *
 * 而ConfigFileApplicationListener会按照以下顺序
 * classpath:/,classpath:/config/,file:./,file:./config/加载配置，且前者会覆盖后者
 * 其中，可以通过spring.profiles.active属性来决定激活哪个配置文件，默认格式：application-{}.yml
 * 还可以通过spring.profiles.include，来导入应用的其他配置文件，默认格式：application-{}.yml
 *
 * 同时，还支持自定义加载配置文件，具体原理如下：
 * 因为ConfigFileApplicationListener也是通过实现EnvironmentPostProcessor来实现将配置文件加载到环境中，
 * 因此我们可以自定义一个ProfileEnvironmentPostProcessor,实现它的postProcessEnvironment方法来决定加载的配置文件
 * 具体流程如下：
 * 1.创建一个ProfileEnvironmentPostProcessor类，实现EnvironmentPostProcessor的postProcessEnvironment方法
 * 2.在resource目录下创建META-INF/spring.factories文件，
 * 并为org.springframework.boot.env.EnvironmentPostProcessor添加一个后置处理器ProfileEnvironmentPostProcessor
 *
 * @author shuchang
 * @version 1.0
 * @date 2021/12/21 11:07
 */
@SpringBootApplication
public class ProfileEnvironmentPostProcessorApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ProfileEnvironmentPostProcessorApplication.class, args);
        String name = context.getEnvironment().getProperty("user.name");
        String gender = context.getEnvironment().getProperty("user.gender");
        System.out.println("name==="+name);
        System.out.println("gender==="+gender);
        // active和include加载顺序校验
        System.out.println("name.value==" + context.getEnvironment().getProperty("name"));
        context.close();
    }
}
