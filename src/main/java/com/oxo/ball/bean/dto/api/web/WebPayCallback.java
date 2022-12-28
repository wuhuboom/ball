package com.oxo.ball.bean.dto.api.web;

import lombok.Data;

@Data
public class WebPayCallback {
    private String tradeNo;//	系统订单号	string
    private String orderNo;//	商户订单号	string
    private Double orderAmount;//	订单金额	number
    private Double amount;//	实际支付金额	number
    private Integer payStatus;//	支付状态,0-订单生成,1-支付成功,2-支付失败	integer(int32)
    private String payTime;//	支付时间	string(date-time)
    private String charge;//	手续费	number
    private String otherData;//	扩展字段 支付中心回调时会原样返回	string
    private String sign;//	6a6c386a90ce5e37e8ab22a8e1ef9e5e 签名值，详见签名算法	string
}
