/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.processors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author shuchang
 * @version 1.0
 * @date 2021/12/21 11:39
 */
public class ProfilesEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 加载配置
        PropertySource<?> source = null;
        try {
            source = loadProfiles(new FileUrlResource(this.getClass().getClassLoader().getResource("config.yml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 添加到Environment
        environment.getPropertySources().addFirst(source);
    }

    @Override
    public int getOrder() {
        return 1;
    }

    private PropertySource<?> loadProfiles(Resource resource) throws IOException {
        YamlPropertySourceLoader sourceLoader = new YamlPropertySourceLoader();
        List<PropertySource<?>> propertySources = sourceLoader.load(resource.getFilename(), resource);
        return propertySources.get(0);
    }

}
