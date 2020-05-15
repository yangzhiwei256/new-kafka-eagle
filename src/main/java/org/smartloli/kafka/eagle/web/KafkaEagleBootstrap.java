package org.smartloli.kafka.eagle.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * kafka Eagle 启动类 <br/>
 * 默认用户名：admin/admin
 * @author zhiwei_yang
 * @time 2020-4-23-15:40
 */
@SpringBootApplication
@MapperScan("org.smartloli.kafka.eagle.web.dao")
@EnableScheduling
public class KafkaEagleBootstrap {

    public static void main(String[] args) {
        SpringApplication.run(KafkaEagleBootstrap.class, args);
    }
}
