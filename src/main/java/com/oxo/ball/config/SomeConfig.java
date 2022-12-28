package com.oxo.ball.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@ConfigurationProperties(prefix = "some-comfig")
@Data
@Configuration
public class SomeConfig {
//    private String timeZone;
    /**
     * 接口key
     */
    private String apiKey;
    /**
     * 博彩公司
     */
    private Long apiBookmaker;
    /**
     * 有数据的联赛，先用着
     */
    private List<Long> apiLeagues;
    /**
     * 本地开发是否启动查询API
     */
    private Integer apiSwitch;

    /**
     * 邀请链接地址
     */
    private String invitationUrl;

    private String oddApiKey;

//    private String payUrl;
//    private String payCallbackUrl;
//    private String payPrivateKey;
//    private String payPublicKey;

    private String smsServer;
    private String smsAppKey;
    private String smsSecretKey;
    private String smsMessage;


    private String serverUrl;
}
