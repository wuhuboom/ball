package com.oxo.ball.service.pay.impl;

import com.oxo.ball.bean.dao.BallLoggerRecharge;
import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dto.api.PayBackDto;
import com.oxo.ball.bean.dto.api.PayCallBackDto;
import com.oxo.ball.bean.dto.api.PayParamBackDto;
import com.oxo.ball.bean.dto.api.PayParamDto;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.service.pay.IPlayerPayService;
import com.oxo.ball.service.admin.IBallLoggerRechargeService;
import com.oxo.ball.service.admin.IBallSystemConfigService;
import com.oxo.ball.service.impl.player.PlayerServiceImpl;
import com.oxo.ball.utils.*;
import com.oxo.ball.ws.WebSocketManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PlayerPayServiceImpl implements IPlayerPayService {
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
    WebSocketManager webSocketManager;
    @Override
    public String requestPayUrl(BallPaymentManagement paymentManagement, PayParamDto payParam) {
        apiLog.info("请求支付数据：{}",payParam);
        String url = paymentManagement.getUstdServer();
        apiLog.info("请求支付地址：{}",url);
        try {
            String encrypt = RSAUtil.encrypt(JsonUtil.toJson(payParam), paymentManagement.getPublicKey());
//            String encrypt = RSAUtil.encrypt(JsonUtil.toJson(payParam));
            Map<String,Object> data = new HashMap<>();
            data.put("data",encrypt);
            String response = HttpUtil.doPost(url, null, JsonUtil.toJson(data));
            apiLog.info("请求支付地址结果:{}",response);
            PayParamBackDto payCallBackDto = JsonUtil.fromJson(response, PayParamBackDto.class);
            if(payCallBackDto.getCode()==200){
                String decrypt = RSAUtil.decrypt(payCallBackDto.getResult().toString(), paymentManagement.getPrivateKey());
//                String decrypt = RSAUtil.decrypt(payCallBackDto.getResult().toString());
                //"UrlAddress": "http://8.136.97.179:7777/static/pay/#/?Address=TW2HWaLWy9pwiRN4yLju6YKW3aQ6Fw8888&RechargeType=USDT&AccountOrders=2000.00&PlatformOrder=6271000011"
                apiLog.info("请求支付地址:{}",decrypt);
                return decrypt;
            }else{
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public BaseResponse payCallBack(BallPaymentManagement paymentManagement,PayCallBackDto payCallBackDto) {
        apiLog.info("支付回调：{}",payCallBackDto);
        Object result = payCallBackDto.getResult();
        if(result instanceof Map){
            Map<String,Object> data = (Map<String, Object>) result;
            String dataStr = data.get("Data").toString();
            try {
                String decrypt = RSAUtil.decrypt(dataStr, paymentManagement.getPrivateKey());
//                String decrypt = RSAUtil.decrypt(dataStr);
                apiLog.info("支付回调:{}",decrypt);
                PayBackDto payBackDto = JsonUtil.fromJson(decrypt, PayBackDto.class);
                if(payCallBackDto.getCode()==200){
                    BallLoggerRecharge payOrder = loggerRechargeService.findByOrderNo(Long.parseLong(payBackDto.getPlatformOrder()));
//                    BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
                    if(payOrder==null){
                        apiLog.info("无法找到支付订单");
                        return BaseResponse.SUCCESS;
                    }
                    String key = RedisKeyContant.PLAYER_PAY_CALLBACK + payBackDto.getPlatformOrder();
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
                    Double mul = BigDecimalUtil.mul(payBackDto.getAccountPractical(), BigDecimalUtil.PLAYER_MONEY_UNIT);
                    BallLoggerRecharge edit = BallLoggerRecharge.builder()
                            .id(payOrder.getId())
                            //已到账
                            .status(2)
                            .moneyReal(mul.longValue())
                            .build();
//                    if(systemConfig.getUsdtWithdrawPer()>0){
//                        edit.setMoneySys(edit.getMoneyReal()*systemConfig.getUsdtWithdrawPer());
//                    }
                    boolean editRes = loggerRechargeService.edit(edit);
                    PlayerPayServiceImplFAST.onIndSuccess(payOrder,paymentManagement, mul, edit, editRes, systemConfigService, loggerRechargeService, webSocketManager);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return BaseResponse.SUCCESS;
    }
}
