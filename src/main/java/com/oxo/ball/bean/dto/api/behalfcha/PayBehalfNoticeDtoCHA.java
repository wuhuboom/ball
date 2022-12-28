package com.oxo.ball.bean.dto.api.behalfcha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayBehalfNoticeDtoCHA {
    //订单状态
    private String tradeResult;
    //商家转账单号
    private String merTransferId;
    //商户代码
    private String merNo;
    //平台订单号
    private String tradeNo;
    //代付金额
    private String transferAmount;
    //订单时间
    private String applyDate;
    //版本号
    private String version;
    //回调状态
    private String respCode;
    //签名
    private String sign;
    //签名方式
    private String signType;
}
