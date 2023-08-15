package cn.com.shuchang.springboot.study.context;

import cn.com.shuchang.springboot.study.bean.ABean;
import cn.com.shuchang.springboot.study.bean.CBean;
import cn.com.shuchang.springboot.study.bean.DuplicateInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @Description: 自定义应用上下文，继承web上下文AnnotationConfigServletWebServerApplicationContext
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
public class CustomAnnotationServletWebServerApplicationContext extends AnnotationConfigServletWebServerApplicationContext implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(CustomAnnotationServletWebServerApplicationContext.class);

    private ApplicationContext context;

    @Override
    protected void initPropertySources() {
        super.initPropertySources();
        logger.info("execute override initPropertySources");
        // 在refresh的prepareRefresh阶段，会按序调用initPropertySources-》getEnvironment().validateRequiredProperties()
        // 其中，第二个方法，会校验environment中设置到propertyResolver中的必备变量属性
        // 故我们可以在次校验方法前，也就是initPropertySources方法中插入必备参数，从而实现必备参数的定义扩展功能
        //((ConfigurableEnvironment)context.getEnvironment()).setRequiredProperties("host");
    }

    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        super.postProcessBeanFactory(beanFactory);
        logger.info("execute override postProcessBeanFactory");

        // 通过registerSingleton可以直接将实例化好的bean装配到beanFactory工厂中；
        // 注意，这种方式的活，是不会经历bean的初始化和销毁的生命周期的操作的，也就没法通过BeanPostProcessor拦截
        // 因为他是直接装载实例化好了的Bean到工厂中，而不是通过创建BeanDefinition，然后由工厂解析创建Bean对象
        beanFactory.registerSingleton("ABean", new ABean());

        // BeanDefinitionRegistry.registerBeanDefinition可以注册BeanDefinition到beanFactory中，然后后续在onRefresh操作中进行
        AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
        beanDefinition.setBeanClassName("cn.com.shuchang.springboot.study.bean.BBean");
        // 这里通过ApplicationContextAware设置的context实际上为空，因为应用上下文触发postProcessBeanFactory是在扫描装配BeanDefinition之前
        // 而ApplicationContextAwareProcessor是BeanPostProcessor的实现，是在Bean初始化时触发的，晚于这个调用时机
        // 故以下是错误案例，应该通过beanFactory来注册BeanDefinition
//        ((BeanDefinitionRegistry)context).registerBeanDefinition("BBean", beanDefinition);
        // 正确写法
        ((BeanDefinitionRegistry)beanFactory).registerBeanDefinition("BBean", beanDefinition);

        //向容器中注册一个可解析的依赖。
        //将对象注册成一个特殊的依赖类型。这种方式注册的对象可以通过 @Autowired 进行注入，但是它并没有在 BeanFactory 中定义为一个 bean。
        // 故，通过这种方法注册的依赖，只能通过@Autowired 或 @Resource获取
        // 因为，BeanFactory在加载这两个注解注入的依赖时，会通过DefaultListableBeanFactory#doResolveDependency()去加载装配到BeanFactory中的resolvableDependencies的依赖
        // 注意：通过这种方式注入的依赖，会优先使用该方式设置的Bean，即便存在多个CBean的定义
        beanFactory.registerResolvableDependency(CBean.class, new CBean());


    }

    @Override
    protected void onRefresh() {
        super.onRefresh();
        logger.info("execute override onRefresh");
    }

    /*
    * 注意：ApplicationContextAware的调用者是ApplicationContextAwareProcessor，他是BeanPostProcessor的实现发，是在bean初始化时触发调用的
    * */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
