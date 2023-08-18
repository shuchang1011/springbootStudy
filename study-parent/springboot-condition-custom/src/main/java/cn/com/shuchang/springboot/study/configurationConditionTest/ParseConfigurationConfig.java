package cn.com.shuchang.springboot.study.configurationConditionTest;

import cn.com.shuchang.springboot.study.condition.ConditionOnParseConfiguration;
import cn.com.shuchang.springboot.study.condition.ConditionOnRegisterBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 测试ParseConfigurationCondition在解析配置类阶段，从BeanFactory中获取TestBeanA；此时TestBeanA还未注册到BeanFactory中，最多只是被ConfigurationClassPostProcessors扫描解析成了configClass
 *                  故，理论上来说，ParseConfigurationCondition是会返回false的，也就不会创建TestBeanC
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
@Configuration
@ConditionOnParseConfiguration
public class ParseConfigurationConfig {

    @Bean
    public TestBeanC testBeanC() {
        return new TestBeanC();
    }
}
