package com.h;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 * @author Lin
 */
@SpringBootApplication
@MapperScan("com.h.mapper")
public class WxJavaProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxJavaProjectApplication.class, args);
    }

}
