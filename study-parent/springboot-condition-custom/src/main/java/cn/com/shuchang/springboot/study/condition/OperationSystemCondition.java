package cn.com.shuchang.springboot.study.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @Description: 自定义windows环境条件
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
public class OperationSystemCondition implements Condition {
    /**
     *
     * @param context the condition context 应用上下文
     * @param metadata metadata of the {@link org.springframework.core.type.AnnotationMetadata class}
     * or {@link org.springframework.core.type.MethodMetadata method} being checked  自定义Conditional注解中的元数据，包含注解上引用的其他注解，以及注解中声明的成员变量等
     * @return
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        String os = (String) metadata.getAnnotationAttributes(ConditionalOnOS.class.getName()).get("os");

        // 获取当前环境
        Environment environment = context.getEnvironment();
        // 判断是否是Windows系统
        String property = environment.getProperty("os.name");
        if (property.contains(os)){
            return true;
        }
        return false;
    }
}
