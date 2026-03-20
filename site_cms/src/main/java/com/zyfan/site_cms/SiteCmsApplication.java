package com.zyfan.site_cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients(basePackages = "com.zyfan.client")
@EnableDiscoveryClient
@EnableElasticsearchRepositories(basePackages = "com.zyfan.site_cms.elasticsearch.repository")
@SpringBootApplication(scanBasePackages = {"com.zyfan"})
public class SiteCmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SiteCmsApplication.class, args);
    }
}
