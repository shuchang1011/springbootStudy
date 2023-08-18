package cn.com.shuchang.springboot.study.condition;

import cn.com.shuchang.springboot.study.configurationConditionTest.TestBeanA;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @Description: 自定义Condition，用于在注册BeanDefinition阶段，尝试获取Bean，并通过获取成功的结果来演示其是于配置类解析完成，并将相关configClass转换成BeanDefinition后，注册到工厂中，然后就能正常从工厂中获取到相关Bean
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
public class RegisterBeanCondition implements ConfigurationCondition {
    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            TestBeanA bean = context.getBeanFactory().getBean(TestBeanA.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
