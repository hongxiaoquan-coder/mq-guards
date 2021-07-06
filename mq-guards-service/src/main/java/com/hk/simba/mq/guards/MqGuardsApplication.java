package com.hk.simba.mq.guards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 启动类
 *
 * @author lijianghui
 * @time 2020-04-30 16:30:22
 */
@EnableCaching
@SpringBootApplication
public class MqGuardsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MqGuardsApplication.class, args);
    }
}
