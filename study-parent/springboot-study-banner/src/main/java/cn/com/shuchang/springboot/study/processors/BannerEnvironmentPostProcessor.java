/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.processors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * @author shuchang
 * @version 1.0
 * @date 2021/12/28 16:43
 */
public class BannerEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String IMAGE_BANNER = "banner.png";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Properties properties = new Properties();
        //需注意，springboot通过ApplicationClassloader去加载图片时，会去classpath路径下查找，因此不需要设置绝对路径
        properties.setProperty("spring.banner.image.location",IMAGE_BANNER);
        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("bannerProperties", properties);
        environment.getPropertySources().addLast(propertiesPropertySource);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
