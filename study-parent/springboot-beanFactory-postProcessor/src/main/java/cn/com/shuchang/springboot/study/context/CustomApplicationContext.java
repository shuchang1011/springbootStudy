package cn.com.shuchang.springboot.study.context;

import cn.com.shuchang.springboot.study.service.impl.ContextLoadServiceImpl;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @Description:
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
public class CustomApplicationContext extends GenericApplicationContext {

    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        super.postProcessBeanFactory(beanFactory);
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
        AbstractBeanDefinition definition = builder.getRawBeanDefinition();
        definition.setBeanClass(ContextLoadServiceImpl.class);
        definition.setScope(BeanDefinition.SCOPE_SINGLETON);
        definition.setPropertyValues(new MutablePropertyValues(ImmutableMap.of("pro1", "value1")));
        ((BeanDefinitionRegistry)beanFactory).registerBeanDefinition("contextLoadService", definition);
        System.out.println("load custom applicationContext");
    }
}
