/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.analyzers;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;


/**
 * @author shuchang
 * @version 1.0
 * @date 2021/12/30 14:21
 */
public class MyFailureAnalyzer extends AbstractFailureAnalyzer<IllegalArgumentException> {
    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, IllegalArgumentException cause) {
        return new FailureAnalysis(cause.getMessage(), "arguments may not be exist, please check it", cause);
    }
}
