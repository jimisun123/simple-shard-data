package com.jimisun.simplesharddata;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * application
 *
 * @author jimisun
 * @create 2022-03-22 1:25 PM
 **/
@SpringBootApplication
@EnableScheduling
@MapperScan("com.jimisun.simplesharddata.mapper")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
