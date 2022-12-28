package com.oxo.ball.service.pay.impl;

import com.oxo.ball.bean.dao.BallLoggerRecharge;
import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.api.cha.PayCallBackDtoCHA;
import com.oxo.ball.bean.dto.api.cha.PayNoticeDtoCHA;
import com.oxo.ball.bean.dto.api.cha.PayRequestDtoCHA;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.service.impl.player.PlayerServiceImpl;
import com.oxo.ball.service.pay.IPlayerPayServiceAllPay;
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

import java.util.Date;

@Service
public class PlayerPayServiceImplAllPay implements IPlayerPayServiceAllPay {
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
    @Autowired
    IApiService apiService;
    @Autowired
    BallAdminService adminService;

    @Override
    public String requestPayUrl(BallPaymentManagement paymentManagement, PayRequestDtoCHA payRequest) {
        apiLog.info("ALL_PAY请求支付订单：{}",payRequest);
        String url = paymentManagement.getUstdServer();
        try {
            String time = TimeUtil.dateFormat(new Date(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
            MultiValueMap<String,Object> data = new LinkedMultiValueMap<>();
            //goods_name, mch_id, mch_order_no, notify_url, order_date, page_url, pay_type, trade_amount, version
            data.add("goods_name",paymentManagement.getGoodsName());
            data.add("mch_id",paymentManagement.getMerchantNo());
            data.add("mch_order_no",payRequest.getOrderNo());
            data.add("notify_url",paymentManagement.getUstdCallback());
            data.add("order_date",time);
            if(!StringUtils.isBlank(paymentManagement.getReturnUrl())&&!"null".equals(paymentManagement.getReturnUrl())){
                data.add("page_url",paymentManagement.getReturnUrl());
            }
            data.add("pay_type",paymentManagement.getPaymentCode());
            data.add("trade_amount",payRequest.getOrderAmount());
            data.add("version","1.0");
            data.add("sign_type","MD5");

            StringBuilder sb = new StringBuilder();
            sb.append("goods_name=");
            sb.append(paymentManagement.getGoodsName());
            sb.append("&mch_id=");
            sb.append(paymentManagement.getMerchantNo());
            sb.append("&mch_order_no=");
            sb.append(payRequest.getOrderNo());
            sb.append("&notify_url=");
            sb.append(paymentManagement.getUstdCallback());
            if(!StringUtils.isBlank(paymentManagement.getReturnUrl())&&!"null".equals(paymentManagement.getReturnUrl())) {
                sb.append("&page_url=");
                sb.append(paymentManagement.getPaymentCode());
            }
            sb.append("&order_date=");
            sb.append(time);
            sb.append("&pay_type=");
            sb.append(paymentManagement.getPaymentCode());
            sb.append("&trade_amount=");
            sb.append(payRequest.getOrderAmount());
            sb.append("&version=1.0");
            sb.append("&key=");
            sb.append(paymentManagement.getPaymentKey());
            apiLog.info("ALL_PAY请求签名数据：{},charset:{}",sb);
            String sign = PasswordUtil.genMd5(sb.toString()).toLowerCase();
            data.add("sign",sign);
            apiLog.info("ALL_PAY请求支付数据：{}",data);
            String response = restHttpsUtil.doPost(url,data,MediaType.APPLICATION_FORM_URLENCODED);
            apiLog.info("ALL_PAY请求支付响应:{}",response);
            PayCallBackDtoCHA payCallBackDto = JsonUtil.fromJson(response, PayCallBackDtoCHA.class);
            if(PayCallBackDtoCHA.CHA_SUCCESS.equals(payCallBackDto.getRespCode())){
                apiLog.info("ALL_PAY请求支付成功:{}",payCallBackDto.getPayInfo());
                return payCallBackDto.getPayInfo();
            }else{
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    @Override
    public BaseResponse payCallBack(PayNoticeDtoCHA payNotice) {
        apiLog.info("ALL_PAY支付回调：{}",payNotice);
        try {
            if("1".equals(payNotice.getTradeResult())){
                BallLoggerRecharge payOrder = loggerRechargeService.findByOrderNo(Long.parseLong(payNotice.getMchOrderNo()));
                if(payOrder==null){
                    apiLog.info("ALL_PAY无法找到支付订单");
                    return BaseResponse.SUCCESS;
                }
                Long payId = payOrder.getPayId();
                BallPaymentManagement paymentManagement = paymentManagementService.findById(payId);
                if(!StringUtils.isBlank(paymentManagement.getWhiteIp())
                        &&!paymentManagement.getWhiteIp().contains(payNotice.getSign())){
                    //有配置白名单，名单中未有IP
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
                long realMoney = Double.valueOf(Double.parseDouble(payNotice.getAmount())*100).longValue();
                BallLoggerRecharge edit = BallLoggerRecharge.builder()
                        .id(payOrder.getId())
                        //已到账
                        .status(2)
                        //实际到账
                        .moneyReal(realMoney)
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
                                .moneyParam(String.valueOf(realMoney/100F))
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
