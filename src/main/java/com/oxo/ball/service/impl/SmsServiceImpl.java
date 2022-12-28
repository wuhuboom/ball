package com.oxo.ball.service.impl;

import com.oxo.ball.bean.dao.BallApiConfig;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.admin.PlayerSmsCodeDTO;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.service.ISmsService;
import com.oxo.ball.service.admin.IBallApiConfigService;
import com.oxo.ball.service.admin.IBallSystemConfigService;
import com.oxo.ball.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SmsServiceImpl implements ISmsService {
    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    SomeConfig someConfig;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    IBallApiConfigService apiConfigService;
    @Autowired
    IBallSystemConfigService systemConfigService;
    @Override
    public BaseResponse sendSms(String username, String phone, String code, String key) {
        //是否维护
        BallApiConfig apiConfig = apiConfigService.getApiConfig();
        if(apiConfig.getSmsUnhold()==1){
            return BaseResponse.failedWithMsg(BaseResponse.SMS_UNHOLD,StringUtils.isBlank(apiConfig.getSmsUnholdMessage())?"invalid":apiConfig.getSmsUnholdMessage());
        }
        //上一次获取的验证码是否到期
        //如果验证码被使用了，验证码将会删除不可使用，但是时间限制内也失效 ，所以需要
        //再加1个key,限制 时间内获取
        Object o1 = redisUtil.get(key);
        if(o1!=null){
            if(someConfig.getApiSwitch()!=null){
                return BaseResponse.successWithMsg(o1.toString());
            }
            return BaseResponse.SUCCESS;
        }
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        //缓存验证码
        redisUtil.set(key, code, TimeUtil.TIME_ONE_MIN * systemConfig.getSmsInterval() / 1000);
        //用于查询账号验证码
        redisUtil.set(RedisKeyContant.PLAYER_PHONE_CODE_LIST+phone,PlayerSmsCodeDTO.builder()
                .code(code)
                .sendTime(System.currentTimeMillis())
                .username(username)
                .phone(phone)
                .build().toString(),TimeUtil.TIME_ONE_MIN/1000*systemConfig.getSmsInterval());
        if(someConfig.getApiSwitch()!=null){
            // 模拟测试
            redisUtil.incr(RedisKeyContant.PLAYER_PHONE_SMS_COUNT+phone,1);
            if(!StringUtils.isBlank(username)){
                redisUtil.incr(RedisKeyContant.PLAYER_PHONE_SMS_COUNT+username,1);
            }
            return BaseResponse.successWithMsg(code);
        }
        apiLog.info("发送短信{},{}",phone,code);
        //请求头设置
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String,String> p = new LinkedMultiValueMap<>();
        p.add("appkey",StringUtils.isBlank(someConfig.getSmsAppKey())?apiConfig.getSmsAppKey():someConfig.getSmsAppKey());
        p.add("secretkey",StringUtils.isBlank(someConfig.getSmsSecretKey())?apiConfig.getSmsSecretKey():someConfig.getSmsSecretKey());
        p.add("phone","+"+phone);
        String format = MessageFormat.format(StringUtils.isBlank(someConfig.getSmsMessage())?apiConfig.getSmsMessage():someConfig.getSmsMessage(), code);
        try {
            p.add("content",URLEncoder.encode(format,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //提交请求
        try {
            HttpEntity< MultiValueMap<String,String>> entity = new HttpEntity< MultiValueMap<String,String>>(p,headers);
            String result = restTemplate.postForObject(StringUtils.isBlank(someConfig.getSmsServer())?apiConfig.getSmsServer():someConfig.getSmsServer(),entity,String.class);
            //{"result":"请求成功","code":"0","messageid":"3b41437ac68044c4ab4b7b7de3b8f399"}
            apiLog.info("短信响应{}:{}:{}:{}",phone,code,format,result);
            Map<String,Object> resultMap = null;
            resultMap = JsonUtil.fromJson(result,Map.class);
            if("0".equals(resultMap.get("code"))){
                //每日验证码次数+1
                redisUtil.incr(RedisKeyContant.PLAYER_PHONE_SMS_COUNT+phone,1);
                if(!StringUtils.isBlank(username)){
                    redisUtil.incr(RedisKeyContant.PLAYER_PHONE_SMS_COUNT+username,1);
                }
                return BaseResponse.SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return BaseResponse.failedWithMsg(BaseResponse.SMS_UNHOLD,StringUtils.isBlank(apiConfig.getSmsUnholdMessage())?"invalid":apiConfig.getSmsUnholdMessage());
    }

    @Override
    public List<String> smsList() {
        List<String> keys = redisUtil.keys(RedisKeyContant.PLAYER_PHONE_CODE_LIST + "*");
        List<String> list = new ArrayList<>();
        for(String key:keys){
            Object o = redisUtil.get(key);
            list.add(o.toString());
        }
        return list;
    }
}
