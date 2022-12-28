package com.oxo.ball.service.pay.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.bean.dao.BallLoggerRecharge;
import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.api.fast.PayParamDtoFast;
import com.oxo.ball.bean.dto.api.in.PayCallBackDtoIN;
import com.oxo.ball.bean.dto.api.in.PayNoticeDtoIN;
import com.oxo.ball.bean.dto.api.in.PayRequestDtoIN;
import com.oxo.ball.bean.dto.api.mp.MpPayCallBack;
import com.oxo.ball.bean.dto.api.mp.MpPayResponse;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.service.impl.player.PlayerServiceImpl;
import com.oxo.ball.service.pay.IPlayerPayServiceIN;
import com.oxo.ball.service.pay.IPlayerPayServiceMP;
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

@Service
public class PlayerPayServiceImplMP implements IPlayerPayServiceMP {
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
    public String requestPayUrl(BallPaymentManagement paymentManagement, PayParamDtoFast payRequest) {
        apiLog.info("MP请求支付订单：{}",payRequest);
        String url = paymentManagement.getUstdServer();
        try {
            String date = TimeUtil.longToStringYmd(System.currentTimeMillis(),TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
            MultiValueMap<String,Object> data = new LinkedMultiValueMap<>();
            data.add("currency",paymentManagement.getGoodsName());
            data.add("mer_no",paymentManagement.getMerchantNo());
            data.add("notifyUrl",paymentManagement.getUstdCallback());
            data.add("order_amount",payRequest.getAmount());
            data.add("order_date",date);
            data.add("order_no",payRequest.getOrderNo());
            data.add("pay_code",paymentManagement.getPaymentCode());
            StringBuilder sb = new StringBuilder();
            sb.append("currency=");
            sb.append(paymentManagement.getGoodsName());
            sb.append("&mer_no=");
            sb.append(paymentManagement.getMerchantNo());
            sb.append("&notifyUrl=");
            sb.append(paymentManagement.getUstdCallback());
            sb.append("&order_amount=");
            sb.append(payRequest.getAmount());
            sb.append("&order_date=");
            sb.append(date);
            sb.append("&order_no=");
            sb.append(payRequest.getOrderNo());
            sb.append("&pay_code=");
            sb.append(paymentManagement.getPaymentCode());
            sb.append("&key=");
            sb.append(paymentManagement.getPaymentKey());
            apiLog.info("MP请求签名数据：{}",sb);
            String sign = PasswordUtil.genMd5(sb.toString()).toLowerCase();
            data.add("sign",sign);
            apiLog.info("MP请求支付数据：{}",data);
            String response = restHttpsUtil.doPost(url,data,MediaType.APPLICATION_FORM_URLENCODED);
            apiLog.info("MP请求支付响应:{}",response);
            MpPayResponse payCallBackDto = JsonUtil.fromJson(response, MpPayResponse.class);
            if("SUCCESS".equals(payCallBackDto.getCode())){
                apiLog.info("MP请求支付成功:{}",payCallBackDto.getPay_url());
                return payCallBackDto.getPay_url();
            }else{
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    @Override
    public String payCallBack(MpPayCallBack payCallBackDto) {
        apiLog.info("MP支付回调：{}", payCallBackDto);
        try {
            if ("1".equals(payCallBackDto.getPayResult())) {
                BallLoggerRecharge payOrder = loggerRechargeService.findByOrderNo(Long.parseLong(payCallBackDto.getOrderNo()));
                if (payOrder == null) {
                    apiLog.info("MP无法找到支付订单");
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

                Double mul = BigDecimalUtil.mul(Double.valueOf(payCallBackDto.getPayAmount()), BigDecimalUtil.PLAYER_MONEY_UNIT);
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
