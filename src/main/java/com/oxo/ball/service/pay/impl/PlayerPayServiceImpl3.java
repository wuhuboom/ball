package com.oxo.ball.service.pay.impl;

import com.oxo.ball.bean.dao.BallLoggerRecharge;
import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.api.in.PayCallBackDtoIN;
import com.oxo.ball.bean.dto.api.in.PayNoticeDtoIN;
import com.oxo.ball.bean.dto.api.in.PayRequestDtoIN;
import com.oxo.ball.bean.dto.api.in3.PayCallBackDto3;
import com.oxo.ball.bean.dto.api.in3.PayNoticeDto3;
import com.oxo.ball.bean.dto.api.in3.PayRequestDto3;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.service.impl.player.PlayerServiceImpl;
import com.oxo.ball.service.pay.IPlayerPayService3;
import com.oxo.ball.service.pay.IPlayerPayServiceIN;
import com.oxo.ball.utils.*;
import com.oxo.ball.ws.WebSocketManager;
import com.oxo.ball.ws.dto.MessageResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.MessageFormat;

@Service
public class PlayerPayServiceImpl3 implements IPlayerPayService3 {
    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);

    @Autowired
    PlayerServiceImpl playerService;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    BasePlayerService basePlayerService;
    @Autowired
    IBallLoggerRechargeService loggerRechargeService;
    @Autowired
    RestHttpsUtil restHttpsUtil;
    @Autowired
    private IBallSystemConfigService systemConfigService;
    @Autowired
    IBallPaymentManagementService paymentManagementService;
    @Autowired
    private WebSocketManager webSocketManager;

    @Override
    public String requestPayUrl(BallPaymentManagement paymentManagement, PayRequestDto3 payRequest) {
        apiLog.info("IN3请求支付订单：{}",payRequest);
        String url = paymentManagement.getUstdServer();
        try {
            Long timestamp = TimeUtil.getNowTimeSec();
            MultiValueMap<String,Object> data = new LinkedMultiValueMap<>();
            data.add("merchantNo",paymentManagement.getMerchantNo());
            data.add("notifyUrl",paymentManagement.getUstdCallback());
            data.add("amount",payRequest.getOrderAmount());
            data.add("outTradeNo",payRequest.getOrderNo());
            data.add("type",paymentManagement.getPaymentCode());
            data.add("extra",payRequest.getOrderNo());
            String signStr = MessageFormat.format("amount={0}&extra={1}&merchantNo={2}&notifyUrl={3}&outTradeNo={4}&type={5}&signKey={6}",
                    payRequest.getOrderAmount(),payRequest.getOrderNo(),paymentManagement.getMerchantNo(),
                    paymentManagement.getUstdCallback(),payRequest.getOrderNo(),paymentManagement.getPaymentCode(),
                    paymentManagement.getPaymentKey());
            apiLog.info("IN3请求签名数据：{}",signStr);
            String sign = PasswordUtil.genMd5(signStr).toUpperCase();
            data.add("sign",sign);
            apiLog.info("IN3请求支付数据：{}",data);
            String response = restHttpsUtil.doPost(url,data,MediaType.MULTIPART_FORM_DATA);
            apiLog.info("IN3请求支付响应:{}",response);
            PayCallBackDto3 payCallBackDto = JsonUtil.fromJson(response, PayCallBackDto3.class);
            if(payCallBackDto.getStatus()==0){
                apiLog.info("IN3请求支付成功:{}",payCallBackDto.getData().getPayUrl());
                return payCallBackDto.getData().getPayUrl();
            }else{
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    @Override
    public BaseResponse payCallBack(PayNoticeDto3 payNotice) {
        apiLog.info("IN3支付回调：{}",payNotice);
        try {
            if(payNotice.getStatus()==1){
                BallLoggerRecharge payOrder = loggerRechargeService.findByOrderNo(Long.parseLong(payNotice.getOutTradeNo()));
                if(payOrder==null){
                    apiLog.info("IN3无法找到支付订单");
                    return BaseResponse.SUCCESS;
                }
                String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payOrder.getOrderNo();
                Object o = redisUtil.get(key);
                if(o!=null){
                    return BaseResponse.SUCCESS;
                }
                long incr = redisUtil.incr(key, 1);
                redisUtil.expire(key,5);
                if(incr>1){
                    return BaseResponse.SUCCESS;
                }

                if(payOrder.getStatus()==2||payOrder.getStatus()==3){
                    return BaseResponse.SUCCESS;
                }
                Long payId = payOrder.getPayId();
                BallPaymentManagement paymentManagement = paymentManagementService.findById(payId);
                if(!StringUtils.isBlank(paymentManagement.getWhiteIp())
                        &&!paymentManagement.getWhiteIp().contains(payNotice.getExtra())){
                    return BaseResponse.SUCCESS;
                }
                Double moneyRe = Double.valueOf(payNotice.getAmount());
                Double moneyReal = BigDecimalUtil.mul(moneyRe,BigDecimalUtil.PLAYER_MONEY_UNIT);
                BallLoggerRecharge edit = BallLoggerRecharge.builder()
                        .id(payOrder.getId())
                        //已到账
                        .status(2)
                        //实际到账
                        .moneyReal(moneyReal.longValue())
                        .build();
                boolean editRes = loggerRechargeService.edit(edit);
                if(editRes){
                    BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
                    boolean autoUp = false;
                    if(paymentManagement.getPayTypeOnff()==1){
                        //1线上2线下
                        if(systemConfig.getAutoUp()==1){
                            autoUp = true;
                        }
                    }else{
                        if(systemConfig.getAutoUpOff()==1){
                            autoUp=true;
                        }
                    }
                    if(autoUp){
                        //自动上分
                        loggerRechargeService.editRe(BallLoggerRecharge.builder()
                                .id(edit.getId())
                                .moneyParam(String.valueOf(moneyRe))
                                .operUser("sys")
                                .remark("auto")
                                .build());
                    }else{
                        //提示WS
                        webSocketManager.sendMessage(null,MessageResponse.builder()
                                .data("")
                                .type(MessageResponse.DEEP_TYPE_R)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BaseResponse.SUCCESS;
    }
}
