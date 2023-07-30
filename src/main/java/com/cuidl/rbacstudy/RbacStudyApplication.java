package com.cuidl.rbacstudy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author cuidl
 */
@SpringBootApplication
@MapperScan("com.cuidl.rbacstudy.mapper")
public class RbacStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(RbacStudyApplication.class, args);
    }

}
