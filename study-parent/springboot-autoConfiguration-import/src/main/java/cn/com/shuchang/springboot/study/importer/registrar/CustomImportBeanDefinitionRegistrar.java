/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.importer.registrar;

import cn.com.shuchang.springboot.study.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.MultiValueMap;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * 通过配置类引用该ImportBeanDefinitionRegistrar，然后在registerBeanDefinitions方法中完成BeanDefinition的注册
 * 实现大致有两种（我知道的）：
 * 1.创建一个BeanDefinition，然后直接通过入参中的registry，
 * 调用registerBeanDefinition(String beanName, BeanDefinition beanDefinition)，完成BeanDefinition的注册
 * 2.通过ClassPathBeanDefinitionScanner来扫描指定包，可以设置相应的类型或注解的Filter过滤器，来扫描指定类型或注解声明的类
 *
 * @author shuchang
 * @version 1.0
 * @date 2022/1/21 16:53
 */
public class CustomImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(CustomImportBeanDefinitionRegistrar.class);


    /**
    * 功能说明
    * @param importingClassMetadata 通过@Import注解引用CustomImportBeanDefinitionRegistrar的类上声明的注解信息
     * @param registry 等同于BeanFactory，缓存了加载的BeanDefinition
    * @return
    */
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        //registerBeanDefinitionsByMethod1(importingClassMetadata, registry);
        registerBeanDefinitionsByMethod2(importingClassMetadata,registry);
    }

    public void registerBeanDefinitionsByMethod1(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClassName(Constants.IMPORT_SERVICE_CLASS3);
        beanDefinition.setAttribute("default", "description_modified");
        //注册构建的beanDefinition到registry中（对应的就是BeanFactory）
        registry.registerBeanDefinition("customServiceImpl3", beanDefinition);
    }

    public void registerBeanDefinitionsByMethod2(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取配置类中的注解信息
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(ComponentScan.class.getName(), false);
        //扫描classpath下的beanDefinition，且不适用默认Filter(扫描@Component、@Service、@Controller等)
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false);
        //通过其父类实现的方法addIncludeFilter添加自定义扫描过滤器
        scanner.addIncludeFilter(new CustomScanFilter());
        scanner.setBeanNameGenerator(new FullyQualifiedAnnotationBeanNameGenerator());
        String[] basePackages = (String[])annotationAttributes.get("basePackages");
        scanner.scan(basePackages);
    }

    class CustomScanFilter implements TypeFilter{

        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
            Set<String> types = metadataReader.getAnnotationMetadata().getAnnotationTypes();
            Iterator<String> iterator = types.iterator();
            while(iterator.hasNext()) {
                String annotation = iterator.next();
                if (annotation.equals(Constants.IMPORT_ANNOTATION_CUSTOM)) {
                    return true;
                }
            }
            return false;
        }
    }
}
