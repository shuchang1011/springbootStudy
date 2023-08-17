package cn.com.shuchang.springboot.study.Bean;

import cn.com.shuchang.springboot.study.condition.ConditionalOnOS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
@Slf4j
@Component
@ConditionalOnOS(os = "windows")
public class WindowsBean implements OSBean{

    public void print(){
        log.info("windows bean");
    }
}
