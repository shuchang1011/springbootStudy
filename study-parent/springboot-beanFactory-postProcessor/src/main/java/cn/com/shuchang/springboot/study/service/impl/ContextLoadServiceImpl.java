package cn.com.shuchang.springboot.study.service.impl;

import cn.com.shuchang.springboot.study.service.ContextLoadService;

/**
 * @Description:
 * @CreateDate: Created in 2023/7/14 16:44
 * @Author: shuchang
 */
public class ContextLoadServiceImpl implements ContextLoadService {

    private String pro1;

    @Override
    public void apply() {
        System.out.println("invoke apply method, pro1="+pro1);
    }
}
