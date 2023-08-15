package cn.com.shuchang.springboot.study.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
@Component
public class DuplicateB implements DuplicateInterface{

    private static final Logger logger = LoggerFactory.getLogger(ABean.class);

    @Override
    public void test() {
        logger.info("duplicate B");
    }
}
