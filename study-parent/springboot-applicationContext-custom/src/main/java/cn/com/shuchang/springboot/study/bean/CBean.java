package cn.com.shuchang.springboot.study.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description:
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
public class CBean {

    private static final Logger logger = LoggerFactory.getLogger(CBean.class);

    public void print() {
        logger.info("CBean");
    }
}
