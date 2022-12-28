package com.oxo.ball.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class RestHttpsUtil {
    @Autowired
    @Qualifier("restTemplateHttps")
    RestTemplate restTemplate;

    public String doPost(String url, MultiValueMap<String, Object> params,MediaType type) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
//  请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
        headers.setContentType(type);
//  封装参数，千万不要替换为Map与HashMap，否则参数无法传递
//  也支持中文
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);
//  执行HTTP请求
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        return response.getBody();
    }

    public String doGet(String url, MultiValueMap<String, Object> params,MediaType type) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
//  请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
        headers.setContentType(type);
//  封装参数，千万不要替换为Map与HashMap，否则参数无法传递
//  也支持中文
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);
//  执行HTTP请求
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        return response.getBody();
    }

}
