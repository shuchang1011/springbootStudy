package cn.com.shuchang.springboot.study.configurationConditionTest;

import cn.com.shuchang.springboot.study.condition.ConditionOnRegisterBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 测试RegisterBeanCondition在注册BeanDefition阶段，其只会在注册BeanDefition阶段触发，从BeanFactory中获取TestBeanA；此时TestBeanA的Bean定义文件已经注册到BeanFactory中
 *                   故，理论上来说，RegisterBeanCondition会返回true，并创建TestBeanB
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
@Configuration
@ConditionOnRegisterBean
public class RegisterBeanConfiguration {

    @Bean
    public TestBeanB testBeanB() {
        return new TestBeanB();
    }
}
