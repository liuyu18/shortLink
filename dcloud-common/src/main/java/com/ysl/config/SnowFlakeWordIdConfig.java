package com.ysl.config;

import java.net.InetAddress;

import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SnowFlakeWordIdConfig {
    static {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String hostAddressIp = inetAddress.getHostAddress();
            String workId = Math.abs(hostAddressIp.hashCode()) % 1024+"";
             System.setProperty("workId",workId);
            log.info("workId:{}",workId);
    

        } catch (Exception e) {
             e.printStackTrace();
        }
        
    }
}
