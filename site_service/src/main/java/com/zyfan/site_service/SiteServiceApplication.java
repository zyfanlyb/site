package com.zyfan.site_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients(basePackages = "com.zyfan.client")
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.zyfan"})
public class SiteServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SiteServiceApplication.class, args);
    }
}
