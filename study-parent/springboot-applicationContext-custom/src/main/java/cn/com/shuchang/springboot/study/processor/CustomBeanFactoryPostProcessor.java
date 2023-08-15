package cn.com.shuchang.springboot.study.processor;

import cn.com.shuchang.springboot.study.bean.DuplicateInterface;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * @Description: 自定义BeanFactoryPostProcessor需通过@Component声明为Bean，或者在spring.factories中指定BeanFactoryPostProcessor实现
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
@Component
public class CustomBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        /*
        * 在Bean实例化前，通过BeanFactoryPostProcessor，指定类型相同的多个Bean实现中，使用的Bean，保证@Autowired或@Resource注入的bean不会导致重复Bean的问题
        * 还有其他的解决方法，例如，@Qualifier指定实际的beanName，又或者是@Primary声明在优先使用的实现Bean上
        * */
        beanFactory.registerResolvableDependency(DuplicateInterface.class, beanFactory.getBean("duplicateA"));
    }
}
