package cn.com.shuchang.springboot.study;/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 通过自定义FailureAnalyzer,来实现对于指定的异常进行捕获分析，并将分析结果提交给SpringBootExceptionReporter进行打印输出
 * 实现过程：
 * 1.自定义FailureAnalyzer，实现AbstractFailureAnalyzer
 * 2.在springboot启动执行完第四阶段后，抛出自定义异常（在此阶段前，还尚未装载自定义的Analyzer）
 *   这里我们声明一个配置类，加载不存在的配置项，使其抛出IllegalArgumentException
 *
 * @author shuchang
 * @version 1.0
 * @date 2021/12/29 16:10
 */

@SpringBootApplication
public class FailureAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FailureAnalyzerApplication.class, args);
    }
}
