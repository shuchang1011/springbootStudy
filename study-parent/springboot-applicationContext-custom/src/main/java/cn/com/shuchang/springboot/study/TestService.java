package cn.com.shuchang.springboot.study;

import cn.com.shuchang.springboot.study.bean.CBean;
import cn.com.shuchang.springboot.study.bean.DuplicateInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
@Component
public class TestService implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(TestService.class);

    @Autowired
    CBean cBean;

    /*
    * 通过BeanFactory.registerResolvableDependency指定Autowired或Resource注入的指定Bean对象，可以解决注册了多个相同类型Bean的问题
    * */
    @Autowired
    DuplicateInterface impl;

    ApplicationContext context;

    public void invoke(){
        cBean.print();
    }

    public void invokeDuplicateBean() {
        impl.test();
        try {
            // 存在多个DuplicateInterface的Bean，会导致beanFactory无法明白通过类型加载哪个
            context.getBean(DuplicateInterface.class).test();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
