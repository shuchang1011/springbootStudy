/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study.service.impl;

import cn.com.shuchang.springboot.study.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 定义一个服务类，并声明为Bean，方便自定义BeanFactoryPostProcessor在Bean实例化前，修改其属性值
 *
 * @author shuchang
 * @version 1.0
 * @date 2022/1/18 14:30
 */

@Service
public class UserServiceImpl implements UserService {

    private String name = "default";

    private String description = "default";

    private String comment = "default";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getComment() {
        return comment;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
