/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.importer.select;

import cn.com.shuchang.springboot.study.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;


/**
 * 返回需要加载的Bean的全限定名，在ConfigurationClassPostProcessor进行parse解析完成后调用
 *
 * @author shuchang
 * @version 1.0
 * @date 2022/1/21 16:29
 */
public class CustomDeferredImportSelector implements DeferredImportSelector {
    private static final Logger logger = LoggerFactory.getLogger(CustomDeferredImportSelector.class);



    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        logger.info("CustomDeferredImportSelector invoked");
        return new String[]{Constants.IMPORT_SERVICE_CLASS2};
    }
}
