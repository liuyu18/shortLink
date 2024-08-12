package com.ysl.config;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ysl.interceptor.LoginInterceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/api/account/*/**", "/api/traffic/*/**")
                .excludePathPatterns(
                        "/api/account/*/register", "/api/account/*/upload", "/api/account/*/login",
                        "/api/notify/v1/captcha", "/api/notify/*/send_code");
    }

}
