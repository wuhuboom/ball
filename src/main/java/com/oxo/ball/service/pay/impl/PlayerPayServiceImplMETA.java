package com.oxo.ball.service.pay.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.bean.dao.BallLoggerRecharge;
import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.api.meta.PayCallBackDtoMeta;
import com.oxo.ball.bean.dto.api.meta.PayResponseDtoMeta;
import com.oxo.ball.bean.dto.api.tnz.PayCallBackDtoTnz;
import com.oxo.ball.bean.dto.api.tnz.PayParamBackDtoTnz;
import com.oxo.ball.bean.dto.api.tnz.PayParamDtoTnz;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.service.impl.player.PlayerServiceImpl;
import com.oxo.ball.service.pay.IPlayerPayServiceMETA;
import com.oxo.ball.utils.*;
import com.oxo.ball.ws.WebSocketManager;
import com.oxo.ball.ws.dto.MessageResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PlayerPayServiceImplMETA implements IPlayerPayServiceMETA {
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
    IBallSystemConfigService systemConfigService;
    @Autowired
    IBallPaymentManagementService paymentManagementService;
    @Autowired
    private WebSocketManager webSocketManager;

    @Override
    public String requestPayUrl(BallPaymentManagement paymentManagement, PayParamDtoTnz payParam) {
        String url = paymentManagement.getUstdServer();
        try {
            Map<String,String> headerMap = new HashMap<>();
            headerMap.put("appid",paymentManagement.getMerchantNo());
            headerMap.put("appkey",paymentManagement.getPaymentKey());

            Map<String, Object> payMap = new HashMap<>();
            payMap.put("orderId", payParam.getOrderNo());
            payMap.put("orderMoney", payParam.getAmount());
            payMap.put("notifyUrl", paymentManagement.getUstdCallback());
            payMap.put("userId", paymentManagement.getPaymentCode());
            payMap.put("currencyType", paymentManagement.getGoodsName());
            payMap.put("type", "1");

            apiLog.info("META请求支付数据：{}", payMap);
            String response = HttpUtil.doPost(url, headerMap, JsonUtil.toJson(payMap));
            apiLog.info("META请求支付地址:{}", response);
            PayResponseDtoMeta payCallBackDto = JsonUtil.fromJson(response, PayResponseDtoMeta.class);
            if (payCallBackDto.getCode()==200) {
                Map<String, String> result = payCallBackDto.getData();
                return result.get("url");
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String payCallBack(PayCallBackDtoMeta payCallBackDto) {
        apiLog.info("META支付回调：{}", payCallBackDto);
        try {
            if ("2".equals(payCallBackDto.getStatus())) {
                BallLoggerRecharge payOrder = loggerRechargeService.findByOrderNo(Long.parseLong(payCallBackDto.getOrderId()));
                if (payOrder == null) {
                    apiLog.info("META无法找到支付订单");
                    return "SUCCESS";
                }
                String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payCallBackDto.getOrderId();
                Object o = redisUtil.get(key);
                if(o!=null){
                    return "SUCCESS";
                }
                long incr = redisUtil.incr(key, 1);
                redisUtil.expire(key,5);
                if(incr>1){
                    return "SUCCESS";
                }

                BallPaymentManagement paymentManagement = paymentManagementService.findById(payOrder.getPayId());
                if(!StringUtils.isBlank(paymentManagement.getWhiteIp())
                        &&!paymentManagement.getWhiteIp().contains(payCallBackDto.getExtInfo())){
                    return "SUCCESS";
                }
                if(payOrder.getStatus()==2||payOrder.getStatus()==3){
                    return "SUCCESS";
                }

                Double mul = BigDecimalUtil.mul(Double.parseDouble(payCallBackDto.getAmount2()), BigDecimalUtil.PLAYER_MONEY_UNIT);
                BallLoggerRecharge edit = BallLoggerRecharge.builder()
                        .id(payOrder.getId())
                        //已到账
                        .status(2)
                        .moneyReal(mul.longValue())
                        .build();
                boolean editRes = loggerRechargeService.edit(edit);
                onIndSuccess(payOrder, paymentManagement, mul, edit, editRes, systemConfigService, loggerRechargeService, webSocketManager);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "SUCCESS";
    }

    static void onIndSuccess(BallLoggerRecharge payOrder, BallPaymentManagement paymentManagement, Double mul, BallLoggerRecharge edit, boolean editRes, IBallSystemConfigService systemConfigService, IBallLoggerRechargeService loggerRechargeService, WebSocketManager ws) throws JsonProcessingException {
        if (editRes) {
            BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
            boolean autoUp = false;
            if (paymentManagement.getPayTypeOnff() == 1) {
                //1线上2线下
                if (systemConfig.getAutoUp() == 1) {
                    autoUp = true;
                }
            } else {
                if (systemConfig.getAutoUpOff() == 1) {
                    autoUp = true;
                }
            }
            if (autoUp) {
                //自动上分
                loggerRechargeService.editRe(BallLoggerRecharge.builder()
                        .id(edit.getId())
                        .moneyParam(String.valueOf(mul.longValue() / 100f))
                        .operUser("sys")
                        .remark("auto")
                        .build());
            }else{
                //提示WS
                ws.sendMessage(null,MessageResponse.builder()
                        .data("")
                        .type(MessageResponse.DEEP_TYPE_R)
                        .build());
            }
        }
    }
}
