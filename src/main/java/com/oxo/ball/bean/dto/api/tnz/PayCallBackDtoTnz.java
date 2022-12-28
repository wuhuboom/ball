package com.oxo.ball.bean.dto.api.tnz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayCallBackDtoTnz {
    private String event;   // 通知事件             PAYIN:收款事件，PAYOUT：付款事件
    private String sign;   // 签名值           签名值（不参与签名）
    private String outTradeNo;   // 商户订单号           商户订单号
    private String transNo;   // 支付平台流水号           支付平台生成的交易流水号
    private String transAmt;   // 支付金额           注意是订单实际支付的金额
    private String transStatus;   // 支付状态           支付状态 SUCCESS：支付成功，FAIL: 支付失败
    private String completionTime;   // 支付完成时间           支付完成时间
    private String failReason;   // 失败原因           失败原因
    private String extInfo;   // 扩展字段           业务扩展字段
}
