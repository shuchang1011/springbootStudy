package cn.com.shuchang.springboot.study.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * @Description:
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(ParseConfigurationCondition.class)
public @interface ConditionOnParseConfiguration {
}
