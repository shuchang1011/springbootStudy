package cn.com.shuchang.springboot.study.processor;

import cn.com.shuchang.springboot.study.bean.ABean;
import cn.com.shuchang.springboot.study.bean.BBean;
import cn.com.shuchang.springboot.study.bean.CBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @Description: 自定义BeanPostProcessor拦截ABean的初始化操作，从而验证registerSingleton方法装配的bean是否会经历BeanFactory的实例化过程
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
@Component
public class CustomBeanPostProcessor  implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CustomBeanPostProcessor.class);

    private static final String POST_PROCESS_BEAN_A = "cn.com.shuchang.springboot.study.bean.ABean";
    private static final String POST_PROCESS_BEAN_B = "cn.com.shuchang.springboot.study.bean.BBean";
    private static final String POST_PROCESS_BEAN_C = "cn.com.shuchang.springboot.study.bean.CBean";

        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ABean) {
            logger.info("{} before initialize", POST_PROCESS_BEAN_A);
        } else if (bean instanceof BBean) {
            logger.info("{} before initialize", POST_PROCESS_BEAN_B);
        } else if (bean instanceof CBean) {
            logger.info("{} before initialize", POST_PROCESS_BEAN_C);
        }
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ABean) {
            logger.info("{} after initialize", POST_PROCESS_BEAN_A);
        } else if (bean instanceof BBean) {
            logger.info("{} after initialize", POST_PROCESS_BEAN_B);
        } else if (bean instanceof CBean) {
            logger.info("{} after initialize", POST_PROCESS_BEAN_C);
        }
        return bean;
    }
}
