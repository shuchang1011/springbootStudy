package cn.com.shuchang.springboot.study.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * @Description: 自定义Conditional注解，指定作用在类和方法上
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(OperationSystemCondition.class)
public @interface ConditionalOnOS {

    /**
     * 设置Bean使用的操作系统
     * @return
     */
    String os() default "";
}
