package com.oxo.ball.service.pay.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallLoggerRecharge;
import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.api.fast.PayCallBackDtoFast;
import com.oxo.ball.bean.dto.api.fast.PayParamBackDtoFast;
import com.oxo.ball.bean.dto.api.fast.PayParamDtoFast;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.service.impl.player.PlayerServiceImpl;
import com.oxo.ball.service.pay.IPlayerPayServiceFAST;
import com.oxo.ball.utils.*;
import com.oxo.ball.ws.WebSocketManager;
import com.oxo.ball.ws.dto.MessageResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

@Service
public class PlayerPayServiceImplFAST implements IPlayerPayServiceFAST {
    @Autowired
    private static IApiService apiService;
    @Autowired
    private static BallAdminService adminService;
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

    //    {  "amount": "100",  "ext": "你的传透参数",
// "merchantNo": "1178632860589223937",
// "notifyUrl": "http://localhost/okexadmin/merchant/simulatormerchantorder/orderNotifyV2",
// "orderNo": "1399327655118245890",
// "sign": "FB72BD4714C8A21FA5C0066130E993C1",
// "type": 8,  "userName": "paul",  "version": "2.0.0" }
    @Override
    public String requestPayUrl(BallPaymentManagement paymentManagement, PayParamDtoFast payParam) {
        String url = paymentManagement.getUstdServer();
        try {
            Map<String, Object> payMap = new HashMap<>();
            payMap.put("amount", payParam.getAmount());
            payMap.put("ext", "ext");
            payMap.put("merchantNo", paymentManagement.getMerchantNo());
            payMap.put("notifyUrl", paymentManagement.getUstdCallback());
            payMap.put("orderNo", payParam.getOrderNo());
            payMap.put("type", paymentManagement.getPaymentCode());
            payMap.put("userName", payParam.getUsername());
            payMap.put("version", "2.0.0");
            String signStr = MessageFormat.format("amount={0}&ext=ext&merchantNo={1}&notifyUrl={2}&orderNo={3}&type={4}&userName={5}&version=2.0.0&key={6}",
                    payParam.getAmount(),
                    paymentManagement.getMerchantNo(),
                    paymentManagement.getUstdCallback(),
                    payParam.getOrderNo(),
                    paymentManagement.getPaymentCode(),
                    payParam.getUsername(),
                    paymentManagement.getPaymentKey());
            payMap.put("sign", PasswordUtil.genMd5(signStr).toUpperCase());
            apiLog.info("FAST请求支付数据：{},签名串:{}", payMap,signStr);
            String response = HttpUtil.doPost(url, null, JsonUtil.toJson(payMap));
            apiLog.info("FAST请求支付地址:{}", response);
            PayParamBackDtoFast payCallBackDto = JsonUtil.fromJson(response, PayParamBackDtoFast.class);
            if ("0".equals(payCallBackDto.getCode())) {
                return payCallBackDto.getUrl();
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String payCallBack(PayCallBackDtoFast payCallBackDto) {
        apiLog.info("FAST支付回调：{}", payCallBackDto);
        try {
            if ("1".equals(payCallBackDto.getStatus())) {
                BallLoggerRecharge payOrder = loggerRechargeService.findByOrderNo(Long.parseLong(payCallBackDto.getOrderNo()));
                if (payOrder == null) {
                    apiLog.info("FAST无法找到支付订单");
                    return "success";
                }
                String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payCallBackDto.getOrderNo();
                Object o = redisUtil.get(key);
                if(o!=null){
                    return "success";
                }
                long incr = redisUtil.incr(key, 1);
                redisUtil.expire(key,5);
                if(incr>1){
                    return "success";
                }

                BallPaymentManagement paymentManagement = paymentManagementService.findById(payOrder.getPayId());
                if(!StringUtils.isBlank(paymentManagement.getWhiteIp())
                        &&!paymentManagement.getWhiteIp().contains(payCallBackDto.getExt())){
                    return "success";
                }
                if(payOrder.getStatus()==2||payOrder.getStatus()==3){
                    return "success";
                }

                Double mul = BigDecimalUtil.mul(Double.parseDouble(payCallBackDto.getRealAmount()), BigDecimalUtil.PLAYER_MONEY_UNIT);
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
        return "success";
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
