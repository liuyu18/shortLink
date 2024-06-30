package com.ysl.service.impl;

import com.ysl.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class NotifyServiceImpl implements NotifyService {
    private final RestTemplate restTemplate;

    @Autowired
    public NotifyServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
}