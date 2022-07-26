/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;

import java.util.Locale;

/**
 * 新建国际化资源Resource bundles
 * 创建配置类，加载一个名为messageSource的Bean(命名必须为messageSource)，加载Bean时，需要指定编码和文件名称
 * 构建启动类，在springboot启动完成后，获取上下文，并通过上下文获取messageSource的bean,通过该Bean来获取国际化资源里面的属性
 * @author shuchang
 * @version 1.0
 * @date 2022/7/25 16:13
 */

@SpringBootApplication
public class MessageSourceApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageSourceApplication.class);
    
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MessageSourceApplication.class, args);
        MessageSource messageSource = (MessageSource) context.getBean("messageSource");
        String zhMessage = messageSource.getMessage("user.name", null, null, Locale.CHINA);
        String enMessage = messageSource.getMessage("user.name", null, null, Locale.ENGLISH);
        logger.info("zhMessage:{}",zhMessage);
        logger.info("enMessage:{}",enMessage);
    }
}
