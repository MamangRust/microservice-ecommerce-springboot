package com.merchantdetailservice.merchantdetailservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class MerchantDetailServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MerchantDetailServiceApplication.class, args);
    }
}