package com.oxo.ball.service.pay.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.bean.dao.BallLoggerRecharge;
import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.api.fast.PayCallBackDtoFast;
import com.oxo.ball.bean.dto.api.fast.PayParamBackDtoFast;
import com.oxo.ball.bean.dto.api.fast.PayParamDtoFast;
import com.oxo.ball.bean.dto.api.tnz.PayCallBackDtoTnz;
import com.oxo.ball.bean.dto.api.tnz.PayParamBackDtoTnz;
import com.oxo.ball.bean.dto.api.tnz.PayParamDtoTnz;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.service.impl.player.PlayerServiceImpl;
import com.oxo.ball.service.pay.IPlayerPayServiceFAST;
import com.oxo.ball.service.pay.IPlayerPayServiceTNZ;
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
public class PlayerPayServiceImplTNZ implements IPlayerPayServiceTNZ {
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

    @Override
    public String requestPayUrl(BallPaymentManagement paymentManagement, PayParamDtoTnz payParam) {
        String url = paymentManagement.getUstdServer();
        try {
            String nowTime = TimeUtil.dateFormat(new Date(),TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
            Map<String, Object> payMap = new HashMap<>();
            payMap.put("appId", paymentManagement.getGoodsName());
            payMap.put("channel", paymentManagement.getPaymentCode());
            payMap.put("mchId", paymentManagement.getMerchantNo());
            payMap.put("notifyUrl", paymentManagement.getUstdCallback());
            payMap.put("outTradeNo", payParam.getOrderNo());
            payMap.put("requestTime", nowTime);
            payMap.put("signType", "MD5");
            payMap.put("subject", paymentManagement.getPrivateKey());
            payMap.put("transAmt", payParam.getAmount());
            String signStr = MessageFormat.format("appId={0}&channel={1}&mchId={2}&notifyUrl={3}&outTradeNo={4}&requestTime={5}&signType=MD5&subject={6}&transAmt={7}&key={8}",
                    paymentManagement.getGoodsName(),
                    paymentManagement.getPaymentCode(),
                    paymentManagement.getMerchantNo(),
                    paymentManagement.getUstdCallback(),
                    payParam.getOrderNo(),
                    nowTime,
                    paymentManagement.getPrivateKey(),
                    payParam.getAmount(),
                    paymentManagement.getPaymentKey());
            payMap.put("sign", PasswordUtil.genMd5(signStr).toUpperCase());
            apiLog.info("TNZ请求支付数据：{},签名串:{}", payMap,signStr);
            String response = HttpUtil.doPost(url, null, JsonUtil.toJson(payMap));
            apiLog.info("TNZ请求支付地址:{}", response);
            PayParamBackDtoTnz payCallBackDto = JsonUtil.fromJson(response, PayParamBackDtoTnz.class);
            if (payCallBackDto.getSuccess()) {
                Map<String, Object> result = payCallBackDto.getResult();
                return result.get("link").toString();
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String payCallBack(PayCallBackDtoTnz payCallBackDto) {
        apiLog.info("TNZ支付回调：{}", payCallBackDto);
        try {
            if ("SUCCESS".equals(payCallBackDto.getTransStatus())) {
                BallLoggerRecharge payOrder = loggerRechargeService.findByOrderNo(Long.parseLong(payCallBackDto.getOutTradeNo()));
                if (payOrder == null) {
                    apiLog.info("TNZ无法找到支付订单");
                    return "OK";
                }
                String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payCallBackDto.getOutTradeNo();
                Object o = redisUtil.get(key);
                if(o!=null){
                    return "OK";
                }
                long incr = redisUtil.incr(key, 1);
                redisUtil.expire(key,5);
                if(incr>1){
                    return "OK";
                }

                BallPaymentManagement paymentManagement = paymentManagementService.findById(payOrder.getPayId());
                if(!StringUtils.isBlank(paymentManagement.getWhiteIp())
                        &&!paymentManagement.getWhiteIp().contains(payCallBackDto.getExtInfo())){
                    return "OK";
                }
                if(payOrder.getStatus()==2||payOrder.getStatus()==3){
                    return "OK";
                }

                Double mul = BigDecimalUtil.mul(Double.parseDouble(payCallBackDto.getTransAmt()), BigDecimalUtil.PLAYER_MONEY_UNIT);
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
        return "OK";
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
