/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.config;

import cn.com.shuchang.springboot.study.importer.CustomImportSelector;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.context.annotation.Import;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/1/25 15:11
 */

@Configuration
@Import(CustomImportSelector.class)
/*
@ComponentScan(basePackages = "cn.com.shuchang.springboot.study.service", nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
*/
public class BeanPostProcessorConfig {
}
