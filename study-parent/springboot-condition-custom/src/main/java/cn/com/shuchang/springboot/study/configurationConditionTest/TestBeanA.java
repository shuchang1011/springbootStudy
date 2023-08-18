package cn.com.shuchang.springboot.study.configurationConditionTest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
@Slf4j
@Component
public class TestBeanA {
    public void print(){
        log.info("TestBeanA");
    }
}
