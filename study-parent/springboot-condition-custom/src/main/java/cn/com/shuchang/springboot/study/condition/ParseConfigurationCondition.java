package cn.com.shuchang.springboot.study.condition;

import cn.com.shuchang.springboot.study.configurationConditionTest.TestBeanA;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @Description: 自定义Condition，用于在解析配置类阶段，尝试获取Bean，并通过获取失败，跳过解析相关应用的配置类，从而演示ConfigurationPhase的触发时期
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
public class ParseConfigurationCondition implements ConfigurationCondition {
    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.PARSE_CONFIGURATION;
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
