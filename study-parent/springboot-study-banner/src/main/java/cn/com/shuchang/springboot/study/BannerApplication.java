/*
 * Copyright(C) 2013 Agree Corporation. All rights reserved.
 *
 * Contributors:
 *     Agree Corporation - initial API and implementation
 */
package cn.com.shuchang.springboot.study;

import cn.com.shuchang.springboot.study.banner.Mybanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author shuchang
 * @version 1.0
 * @date 2021/12/28 16:27
 */
@SpringBootApplication
public class BannerApplication {

    public static void main(String[] args) {
        SpringApplication applicationContext = new SpringApplication(BannerApplication.class);
        applicationContext.setBanner(new Mybanner());
        applicationContext.run(args);
    }
}
