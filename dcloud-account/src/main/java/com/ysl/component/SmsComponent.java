package com.ysl.component;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.ysl.config.SmsConfig;
import com.ysl.model.SmsCode;
import com.ysl.util.HttpUtils;
import com.ysl.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;


@Component
@Slf4j
public class SmsComponent {
    private static final String URL_TEMPLATE = "https://gyytz.market.alicloudapi.com/sms/smsBatchSend?mobile=%s&templateId=%s&value=%s";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SmsConfig smsConfig;
    
    @Async("threadPoolTaskExecutor")
    public void send(String to, String templateId, String value) {
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = smsConfig.getAppCode();
        Map<String, String> headers = new HashMap<String, String>();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "APPCODE " + appcode);
        headers.put("Authorization", "APPCODE " + appcode);
        MultiValueMap<String, String> querys = new LinkedMultiValueMap<>();

        querys.add("mobile", to);
        querys.add("param", "**code**:" + value +",**minute**:5");

        // smsSignId（短信前缀）和templateId（短信模板），可登录国阳云控制台自助申请。参考文档：http://help.guoyangyun.com/Problem/Qm.html

        querys.add("smsSignId", smsConfig.getSmsSignId());
        querys.add("templateId", smsConfig.getTemplateId());
        Map<String, String> bodys = new HashMap<String, String>();

        try {
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(querys,
                    httpHeaders);
            SmsCode result = restTemplate.postForObject(host + path, request, SmsCode.class);
            System.out.println("========");
            System.out.println(result.balance);
            System.out.println(result.code);
            System.out.println(result.msg);
            System.out.println(result.smsid);
            System.out.println("========");

            // HttpResponse response = (HttpResponse) HttpUtils.doPost(host, path, method,
            // headers, querys, bodys);
            // System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
             
    }
    
}