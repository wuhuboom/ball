package com.oxo.ball.service.pay.impl;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallLoggerRecharge;
import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.api.in.PayCallBackDtoIN;
import com.oxo.ball.bean.dto.api.in.PayNoticeDtoIN;
import com.oxo.ball.bean.dto.api.in.PayRequestDtoIN;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.service.pay.IPlayerPayServiceIN;
import com.oxo.ball.service.impl.player.PlayerServiceImpl;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PlayerPayServiceImplIN implements IPlayerPayServiceIN {
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
    private IApiService apiService;
    @Autowired
    private BallAdminService adminService;

    @Override
    public String requestPayUrl(BallPaymentManagement paymentManagement, PayRequestDtoIN payRequest) {
        apiLog.info("IN请求支付订单：{}",payRequest);
        String url = paymentManagement.getUstdServer();
        try {
            Long timestamp = TimeUtil.getNowTimeSec();
            MultiValueMap<String,Object> data = new LinkedMultiValueMap<>();
//            data.add("attach",paymentManagement.getId().toString());
            data.add("merchant_no",paymentManagement.getMerchantNo());
            data.add("notice_url",paymentManagement.getUstdCallback());
            data.add("order_amount",payRequest.getOrderAmount());
            data.add("order_no",payRequest.getOrderNo());
            data.add("payment_code",paymentManagement.getPaymentCode());
            if(!StringUtils.isBlank(paymentManagement.getReturnUrl())&&!"null".equals(paymentManagement.getReturnUrl())){
                data.add("return_url",paymentManagement.getReturnUrl());
            }
            data.add("timestamp",timestamp);
            StringBuilder sb = new StringBuilder();
//            sb.append("attach=");
//            sb.append(paymentManagement.getId().toString());
            sb.append("merchant_no=");
            sb.append(paymentManagement.getMerchantNo());
            sb.append("&notice_url=");
            sb.append(paymentManagement.getUstdCallback());
            sb.append("&order_amount=");
            sb.append(payRequest.getOrderAmount());
            sb.append("&order_no=");
            sb.append(payRequest.getOrderNo());
            sb.append("&payment_code=");
            sb.append(paymentManagement.getPaymentCode());
            if(!StringUtils.isBlank(paymentManagement.getReturnUrl())&&!"null".equals(paymentManagement.getReturnUrl())) {
                sb.append("&return_url=");
                sb.append(paymentManagement.getReturnUrl());
            }
            sb.append("&timestamp=");
            sb.append(timestamp);
            sb.append("&key=");
            sb.append(paymentManagement.getPaymentKey());
            apiLog.info("IN请求签名数据：{}",sb);
            String sign = PasswordUtil.genMd5(sb.toString()).toLowerCase();
            data.add("sign",sign);
            apiLog.info("IN请求支付数据：{}",data);
            String response = restHttpsUtil.doPost(url,data,MediaType.APPLICATION_FORM_URLENCODED);
            apiLog.info("IN请求支付响应:{}",response);
            PayCallBackDtoIN payCallBackDto = JsonUtil.fromJson(response, PayCallBackDtoIN.class);
            if(payCallBackDto.getCode()==0){
                apiLog.info("IN请求支付成功:{}",payCallBackDto.getData().getUrl());
                return payCallBackDto.getData().getUrl();
            }else{
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    @Override
    public BaseResponse payCallBack(PayNoticeDtoIN payNotice) {
        apiLog.info("IN支付回调：{}",payNotice);
        try {
            if(payNotice.getPay_status()==1){
                BallLoggerRecharge payOrder = loggerRechargeService.findByOrderNo(Long.parseLong(payNotice.getOrder_no()));
                if(payOrder==null){
                    apiLog.info("IN无法找到支付订单");
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
                        &&!paymentManagement.getWhiteIp().contains(payNotice.getAttach())){
                    //有配置白名单，名单中未有IP
                    return BaseResponse.SUCCESS;
                }
                BallLoggerRecharge edit = BallLoggerRecharge.builder()
                        .id(payOrder.getId())
                        //已到账
                        .status(2)
                        //实际到账
                        .moneyReal(payNotice.getPay_amount().longValue())
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
                                .moneyParam(String.valueOf(payNotice.getPay_amount()/100))
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
                //                BallPlayer player = basePlayerService.findOne(payOrder.getPlayerId());
//                playerService.recharge(player,payOrder,payNotice);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BaseResponse.SUCCESS;
    }
}
