/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.importer;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author shuchang
 * @version 1.0
 * @date 2022/1/25 15:13
 */
public class CustomImportSelector implements ImportSelector {

    private static final String IMPORT_CLASS = "cn.com.shuchang.springboot.study.processors.CustomBeanPostProcessor2";

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{IMPORT_CLASS};
    }
}
