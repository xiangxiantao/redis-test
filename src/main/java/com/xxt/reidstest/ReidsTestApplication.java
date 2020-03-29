package com.xxt.reidstest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.xxt"})
public class ReidsTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReidsTestApplication.class, args);
    }

}
